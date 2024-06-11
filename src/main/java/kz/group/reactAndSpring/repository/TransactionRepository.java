package kz.group.reactAndSpring.repository;

import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.TransactionEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<TransactionEntity, Long> {
    List<TransactionEntity> getTransactionEntitiesBySourceCard(BankCardEntity bankCard);
    TransactionEntity findByTransactionId(String transactionId);
    List<TransactionEntity> findAllByOwner(UserEntity user);
    List<TransactionEntity> findAllBySourceCard(BankCardEntity sourceCard);
    List<TransactionEntity> findAllByDestCard(BankCardEntity destCard);
}
