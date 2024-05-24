package kz.group.reactAndSpring.repository;

import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.TransactionEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> getTransactionEntitiesBySourceCard(BankCardEntity bankCard);
    List<TransactionEntity> getTransactionEntitiesByStatus(String status);
    TransactionEntity findByTransactionId(String transactionId);
}
