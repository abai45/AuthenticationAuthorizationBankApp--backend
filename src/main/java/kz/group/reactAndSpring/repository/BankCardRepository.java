package kz.group.reactAndSpring.repository;

import kz.group.reactAndSpring.entity.BankCardEntity;
import kz.group.reactAndSpring.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankCardRepository extends JpaRepository<BankCardEntity, Long> {
    List<BankCardEntity> findAllByOwner(UserEntity owner);
}