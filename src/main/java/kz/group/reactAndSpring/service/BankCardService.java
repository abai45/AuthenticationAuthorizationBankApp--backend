package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.bankDto.BankCardDto;
import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.UserEntity;

import java.util.List;

public interface BankCardService {
    BankCardEntity createBankCard(UserDto user, String bankCardName);
    List<BankCardDto> getBankCard(UserDto user);
}
