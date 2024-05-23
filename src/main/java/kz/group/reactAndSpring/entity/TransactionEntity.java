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
public class TransactionEntity extends Autitable {
    @Column(updatable = false, unique = true, nullable = false)
    private String transactionId;
    private String transactionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_transaction_source"))
    private UserEntity sourceUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dest_user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_transaction_dest"))
    private UserEntity destUser;

    private LocalDateTime transactionDate;
    private BigDecimal amount;
    private String status;
}

