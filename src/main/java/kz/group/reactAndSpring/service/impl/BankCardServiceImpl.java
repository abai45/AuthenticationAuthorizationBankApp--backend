package kz.group.reactAndSpring.service.impl;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.bankDto.BankCardDto;
import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.exception.ApiException;
import kz.group.reactAndSpring.repository.BankCardRepository;
import kz.group.reactAndSpring.repository.UserRepository;
import kz.group.reactAndSpring.service.BankCardService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

import static kz.group.reactAndSpring.utils.BankCardUtils.createBankCardEntity;

@Service
public class BankCardServiceImpl implements BankCardService {
    BankCardRepository bankCardRepository;
    UserRepository userRepository;

    @Override
    public BankCardEntity createBankCard(UserDto user, String bankCardName) {
        var userEntity = getUser(user);
        BankCardEntity bankCard = createBankCardEntity(userEntity, bankCardName);
        bankCardRepository.save(bankCard);
        return bankCard;
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
                        .cardExpiryDate(card.getCardExpiryDate())
                        .build()
                ).collect(Collectors.toList());
    }
    private UserEntity getUser(UserDto user) {
        var userEntity = userRepository.findById(user.getId());
        return userEntity.orElseThrow(() -> new ApiException("User by id is not found"));
    }
}
