package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.bankDto.TransactionDto;

import java.util.Collection;

public interface TransactionService {

    TransactionDto transferTransaction(String sourcePhone, String destPhone, String amount);

    Collection<TransactionDto> getTransactions(String userId);

    TransactionDto debitTransaction(String phoneNumber, String amount);

    TransactionDto creditTransfer(String phoneNumber, String amount);

    void deleteTransaction(String transactionId);
}
