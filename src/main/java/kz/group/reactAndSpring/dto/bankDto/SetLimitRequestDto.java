package kz.group.reactAndSpring.dto.bankDto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SetLimitRequestDto {
    @NotEmpty(message = "Card Name cannot be empty or null")
    private String cardName;
    @NotEmpty(message = "Limit cannot be empty or null")
    private BigDecimal limit;
}
