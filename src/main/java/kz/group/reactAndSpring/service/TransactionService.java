package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.bankDto.TransactionDto;

import java.math.BigDecimal;
import java.util.Collection;

public interface TransactionService {

    TransactionDto transferTransaction(String sourcePhone, String destPhone, BigDecimal amount);

    Collection<TransactionDto> getTransactions(String userId);

    TransactionDto debitTransaction(String phoneNumber, BigDecimal amount);

    TransactionDto creditTransfer(String phoneNumber, BigDecimal amount);

    void deleteTransaction(String transactionId);
}
