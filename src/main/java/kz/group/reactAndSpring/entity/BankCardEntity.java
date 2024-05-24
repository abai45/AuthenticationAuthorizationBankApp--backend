package kz.group.reactAndSpring.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name="bank_cards")
public class BankCardEntity extends Autitable{
    @Column(nullable = false, unique = true)
    private String cardNumber;
    @Column(nullable = false)
    private String last4Digits;
    @Column(nullable = false)
    private String cardHolderName;
    @Column(nullable = false)
    private String cardName;
    @Column(nullable = false)
    private BigDecimal balance;
    @Column(nullable = false)
    private String cardExpiryDate;
    @Column(nullable = false)
    private String cardCVV;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id", foreignKey = @ForeignKey(name = "fk_card_user"))
    private UserEntity owner;
}
