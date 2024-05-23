package kz.group.reactAndSpring.utils;

import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import kz.group.reactAndSpring.repository.BankCardRepository;
import kz.group.reactAndSpring.service.EncryptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.MonthDay;
import java.time.Year;
import java.time.YearMonth;
import java.util.Random;

@Component
public class BankCardUtils {
    public static final int CARD_NUMBER_LENGTH = 16;
    public static final int CVV_LENGTH = 3;
    private static BankCardRepository bankCardRepository;
    private static EncryptionService encryptionService;

    @Autowired
    public BankCardUtils(BankCardRepository bankCardRepository) {
        this.bankCardRepository = bankCardRepository;
    }

    public static BankCardEntity createBankCardEntity(UserEntity owner,String cardName) {
        String cardNumber = encryptionService.encrypt(generateCardNumber());
        String last4Digits= cardNumber.substring(15);
        String cvv = encryptionService.encrypt(generateCVV());
        return BankCardEntity.builder()
                .cardNumber(cardNumber)
                .last4Digits(last4Digits)
                .cardHolderName(owner.getFirstName()+" "+owner.getLastName())
                .cardExpiryDate(generateExpityDate())
                .cardCVV(cvv)
                .owner(owner)
                .cardName(cardName)
                .build();
    }

    private static String generateCardNumber() {
        var cardNumber = new StringBuilder();
        var random = new Random();
        for (int i = 0; i < CARD_NUMBER_LENGTH; i++) {
            cardNumber.append(random.nextInt(10));
            if(cardNumber.length()==4 || cardNumber.length()==9 || cardNumber.length()==14) {
                cardNumber.append(' ');
            }
        }
        return cardNumber.toString();
    }

    private static String generateExpityDate() {
        YearMonth yearMonth = YearMonth.now().plusYears(3);
        int month = yearMonth.getMonthValue();
        int year = yearMonth.getYear() % 100;
        return String.format("%02d/%02d", month, year);
    }

    private static String generateCVV() {
        var cvv = new StringBuilder();
        var random = new Random();
        for (int i = 0; i < CVV_LENGTH; i++) {
            cvv.append(random.nextInt(10));
        }
        return cvv.toString();
    }
}
