package kz.group.reactAndSpring.dto.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransferRequestDto {
    @NotEmpty(message = "Source phone number cannot be empty or null")
    private String sourcePhone;
    @NotEmpty(message = "Destination phone number cannot be empty or null")
    private String destPhone;
    @NotEmpty(message = "Amount cannot be empty or null")
    private String Amount;
}
