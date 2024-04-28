package kz.group.reactAndSpring.repository;

import kz.group.reactAndSpring.entity.CredentionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentionalRepository extends JpaRepository<CredentionalEntity,Long> {
    Optional<CredentionalEntity> getCredentionalEntitieByUserEntityId(Long userId);
}
