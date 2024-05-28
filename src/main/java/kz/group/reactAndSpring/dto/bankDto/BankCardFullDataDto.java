package kz.group.reactAndSpring.dto.bankDto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankCardFullDataDto {
    private String cardNumber;
    private String last4Digits;
    private String cardHolderName;
    private String balance;
    private String cardName;
    private String cardExpiryDate;
    private String cardCVV;
}
