package kz.group.reactAndSpring.dto.bankDto;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankCardDto {
    private String last4Digits;
    private String bankCardNumber;
    private String cardHolderName;
    private String balance;
    private String cardName;
    private String cardExpiryDate;
}
