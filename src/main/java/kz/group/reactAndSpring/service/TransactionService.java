package kz.group.reactAndSpring.service;

import kz.group.reactAndSpring.dto.TransactionDto;

import java.util.Collection;

public interface TransactionService {

    void creditTransaction(String sourcePhone, String destPhone, String amount);

    Collection<TransactionDto> getTransactions(String userId);
}
