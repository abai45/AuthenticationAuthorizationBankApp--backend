package kz.group.reactAndSpring.entity;

import jakarta.persistence.Column;

public class BankCardEntity extends Autitable{
    @Column(nullable = false, unique = true)
    private String cardNumberEncrypted;

    @Column(nullable = false)
    private String cardHolderName;
    @Column(nullable = false)
    private String cardExpiryDate;
}
