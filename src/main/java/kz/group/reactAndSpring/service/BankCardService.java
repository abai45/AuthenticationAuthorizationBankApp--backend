package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.UserDto;
import kz.group.reactAndSpring.dto.bankDto.BankCardDto;
import kz.group.reactAndSpring.dto.bankDto.BankCardFullDataDto;

import java.math.BigDecimal;
import java.util.List;

public interface BankCardService {
    BankCardDto createBankCard(UserDto user, String bankCardName);
    List<BankCardDto> getBankCard(UserDto user);
    BankCardFullDataDto getFullCardInfo(UserDto user, String cardName);
    BigDecimal getTotalBalance(UserDto userId);
    BigDecimal getBonuses(UserDto user);
    void deleteBankCard(UserDto user, String cardName);
    void setLimitToCard(UserDto user, String cardName, BigDecimal limit);
}
