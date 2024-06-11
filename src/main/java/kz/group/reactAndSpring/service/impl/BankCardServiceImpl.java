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
            bonusAmount = BigDecimal.valueOf(2500);
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
        var bankCardEntity = getBankCardEntity(userEntity.getBankCards(), cardName);
        var transactionsEntity = transactionRepository.findAllBySourceCard(bankCardEntity);
        transactionRepository.deleteAll(transactionsEntity);
        userEntity.getBankCards().remove(bankCardEntity);
        userRepository.save(userEntity);
    }

    @Override
    public void setLimitToCard(UserDto user, String cardName, BigDecimal limit) {
        var userEntity = getUser(user);
        var bankCardEntity = getBankCardEntity(userEntity.getBankCards(), cardName);
        bankCardEntity.setTransactionLimit(limit);
        bankCardRepository.save(bankCardEntity);
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
                        .bankCardNumber(encryptionService.decrypt(card.getCardNumber()))
                        .cardExpiryDate(card.getCardExpiryDate())
                        .build()
                ).collect(Collectors.toList());
    }

    @Override
    public BankCardFullDataDto getFullCardInfo(UserDto user, String cardName) {
        var userEntity = getUser(user);
        var bankCards = bankCardRepository.findAllByOwner(userEntity);
        var bankCardEntity = getBankCardEntity(bankCards, cardName);
        return BankCardFullDataDto.builder()
                .cardNumber(encryptionService.decrypt(bankCardEntity.getCardNumber()))
                .cardName(bankCardEntity.getCardName())
                .last4Digits(bankCardEntity.getLast4Digits())
                .cardHolderName(bankCardEntity.getCardHolderName())
                .balance(bankCardEntity.getBalance().toString())
                .cardExpiryDate(bankCardEntity.getCardExpiryDate())
                .cardCVV(encryptionService.decrypt(bankCardEntity.getCardCVV()))
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

    private BankCardEntity getBankCardEntity(List<BankCardEntity> bankCardEntities, String cardName) {
        return bankCardEntities.stream()
                .filter(bankCard -> bankCard.getCardName().equalsIgnoreCase(cardName))
                .findFirst().orElseThrow(() -> new ApiException("No bank card found"));
    }

    private UserEntity getUser(UserDto user) {
        var userEntity = userRepository.findUserByUserId(user.getUserId());
        return userEntity.orElseThrow(() -> new ApiException("User by id is not found"));
    }
}
