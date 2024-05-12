package kz.group.reactAndSpring.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_DEFAULT;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="transactions")
@JsonInclude(NON_DEFAULT)
public class TransactionEntity extends Autitable{
    @Column(updatable = false, unique = true, nullable = false)
    private String transactionId;
    private String transactionType;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name="user_id",
            referencedColumnName = "id",
            foreignKey = @ForeignKey(name = "fk_transaction_owner", foreignKeyDefinition = "foreign key /* FK */ references UserEntity", value = ConstraintMode.CONSTRAINT)
    )
    private UserEntity owner;
    private LocalDateTime transactionDate;
    private BigDecimal amount;
    private String status;
}
