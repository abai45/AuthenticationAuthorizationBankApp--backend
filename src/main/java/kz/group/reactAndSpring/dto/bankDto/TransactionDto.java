package kz.group.reactAndSpring.dto.bankDto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDto {
    private Long id;
    private String transactionId;
    private String transactionType;
    private String ownerName;
    private String ownerEmail;
    private String ownerPhone;
    private String ownerLastLogin;
    private LocalDateTime transactionDate;
    private BigDecimal amount;
    private String status;
}
