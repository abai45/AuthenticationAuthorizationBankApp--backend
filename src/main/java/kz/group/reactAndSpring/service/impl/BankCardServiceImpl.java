package kz.group.reactAndSpring.service.impl;

import jakarta.transaction.Transactional;
import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.bankDto.BankCardDto;
import kz.group.reactAndSpring.dto.bankDto.BankCardFullDataDto;
import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.enumeration.BankType;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.BankCardRepository;
import kz.group.reactAndSpring.repository.TransactionRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.BankCardService;
import kz.group.reactAndSpring.service.EncryptionService;
import kz.group.reactAndSpring.utils.BankCardUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import static kz.group.reactAndSpring.utils.BankCardUtils.createBankCardEntity;

@Service
@RequiredArgsConstructor
@Transactional(rollbackOn = Exception.class)
@Slf4j
public class BankCardServiceImpl implements BankCardService {
    private final BankCardRepository bankCardRepository;
    private final UserRepository userRepository;
    private final EncryptionService encryptionService;
    private final TransactionRepository transactionRepository;

    @Override
    public BankCardDto createBankCard(UserDto user, String bankCardName) {
        var userEntity = getUser(user);
        for(BankCardEntity bankcard: userEntity.getBankCards()) {
            if(bankcard.getCardName().equals(bankCardName)) {
                throw new ApiException("You already have a bank card");
            }
        }
        var limit = BigDecimal.ZERO;
        var bonusAmount = BigDecimal.ZERO;
        if(BankType.PREMIUM.getValue().equals(bankCardName)) {
            limit = BigDecimal.valueOf(100000);
            bonusAmount = BigDecimal.valueOf(5000);
        } else if (BankType.STANDART.getValue().equals(bankCardName) || BankType.DEPOSIT.getValue().equals(bankCardName)) {
            limit = BigDecimal.valueOf(25000);
            bonusAmount = BigDecimal.valueOf(5000);
        } else {
            throw new ApiException("Invalid bank card name");
        }
        var bankCardEntity = createBankCardEntity(userEntity, bankCardName, limit, bonusAmount);
        userEntity.getBankCards().add(bankCardEntity);
        userRepository.save(userEntity);
        return BankCardUtils.fromBankCardEntity(bankCardEntity);
    }

    @Override
    public void deleteBankCard(UserDto user, String cardName) {
        var userEntity = getUser(user);
        var bankCardEntity = new BankCardEntity();
        for(BankCardEntity bankcard: userEntity.getBankCards()) {
            if(bankcard.getCardName().equals(cardName)) {
                bankCardEntity = bankcard;
            }
        }
        var transactionsEntity = transactionRepository.findAllBySourceCard(bankCardEntity);
        transactionRepository.deleteAll(transactionsEntity);
        userEntity.getBankCards().remove(bankCardEntity);
        userRepository.save(userEntity);
    }

    @Override
    public void setLimitToCard(UserDto user, String cardName, String limit) {
        var userEntity = getUser(user);
        var newLimit = new BigDecimal(limit);
        for(BankCardEntity bankcard: userEntity.getBankCards()) {
            if(bankcard.getCardName().equals(cardName)) {
                bankcard.setTransactionLimit(newLimit);
                bankCardRepository.save(bankcard);
            }
        }
    }


    @Override
    public List<BankCardDto> getBankCard(UserDto user) {
        var userEntity = getUser(user);
        List<BankCardEntity> bankCards = bankCardRepository.findAllByOwner(userEntity);

        return bankCards.stream()
                .map(card -> BankCardDto.builder()
                        .cardName(card.getCardName())
                        .last4Digits(card.getLast4Digits())
                        .cardHolderName(card.getCardHolderName())
                        .balance(card.getBalance().toString())
                        .cardExpiryDate(card.getCardExpiryDate())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public BankCardFullDataDto getFullCardInfo(UserDto user, String cardName) {
        var userEntity = getUser(user);
        var bankCardEntity = bankCardRepository.findAllByOwner(userEntity).stream()
                .filter(card -> card.getCardName().equals(cardName))
                .findFirst()
                .orElseThrow(() -> new ApiException("No bank card found"));
        var bankCardFullData = new BankCardFullDataDto();
        return bankCardFullData.builder()
                .cardNumber(encryptionService.decrypt(bankCardEntity.getCardNumber()))
                .cardHolderName(bankCardEntity.getCardHolderName())
                .cardCVV(encryptionService.decrypt(bankCardEntity.getCardCVV()))
                .cardExpiryDate(bankCardEntity.getCardExpiryDate())
                .cardName(bankCardEntity.getCardName())
                .last4Digits(bankCardFullData.getLast4Digits())
                .build();
    }

    @Override
    public BigDecimal getTotalBalance(UserDto userId) {
        var userEntity = getUser(userId);
        var totalBalance = new BigDecimal(BigInteger.ZERO);
        for(BankCardEntity bankcard: userEntity.getBankCards()) {
            totalBalance = totalBalance.add(bankcard.getBalance());
        }
        return totalBalance;
    }

    @Override
    public BigDecimal getBonuses(UserDto user) {
        var userEntity = getUser(user);
        var bonuses = new BigDecimal(BigInteger.ZERO);
        for(BankCardEntity bankcard: userEntity.getBankCards()) {
            bonuses = bonuses.add(bankcard.getBonuses());
        }
        return bonuses;
    }

    private UserEntity getUser(UserDto user) {
        var userEntity = userRepository.findUserByUserId(user.getUserId());
        return userEntity.orElseThrow(() -> new ApiException("User by id is not found"));
    }
}
