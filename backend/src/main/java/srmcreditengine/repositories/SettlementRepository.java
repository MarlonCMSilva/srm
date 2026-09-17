package srmcreditengine.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import srmcreditengine.entities.Settlement;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByReceivableId(Long receivableId);
    boolean existsByReceivableId(Long receivableId);
}
