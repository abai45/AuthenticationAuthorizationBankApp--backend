package kz.group.reactAndSpring.dto.bankDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class CreditDebitRequestDto {
    @NotEmpty(message = "Card number cannot be empty or null")
    private String cardNumber;
    @NotEmpty(message = "Amount cannot be empty or null")
    private String amount;
}
