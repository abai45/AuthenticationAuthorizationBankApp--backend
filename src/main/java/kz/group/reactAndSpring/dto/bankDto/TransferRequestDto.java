package kz.group.reactAndSpring.dto.bankDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequestDto {
    @NotEmpty(message = "Source card number cannot be empty or null")
    private String sourceCardNumber;
    @NotEmpty(message = "Destination card number cannot be empty or null")
    private String destCardNumber;
    @NotEmpty(message = "Amount cannot be empty or null")
    private BigDecimal amount;
}
