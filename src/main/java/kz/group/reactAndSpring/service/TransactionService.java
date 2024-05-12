package kz.group.reactAndSpring.service;

public interface TransactionService {

    void creditTransaction(String sourcePhone, String destPhone, String amount);
}
