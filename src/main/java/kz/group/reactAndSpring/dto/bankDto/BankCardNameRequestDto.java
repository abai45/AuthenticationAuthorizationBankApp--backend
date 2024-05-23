package kz.group.reactAndSpring.dto.bankDto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankCardNameRequestDto {
    @NotEmpty(message = "Card Name cannot be empty or null")
    private String cardName;
}
