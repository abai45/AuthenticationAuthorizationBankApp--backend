package kz.group.reactAndSpring.dto.bankDto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionId {
    @NotEmpty(message = "Transaction ID cannot be empty or null")
    private String transactionId;
}
