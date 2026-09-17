package srmcreditengine.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import srmcreditengine.entities.Receivable;

public interface ReceivableRepository extends JpaRepository<Receivable, Long> {

    @Query("SELECT obj FROM Receivable obj " +
            "WHERE UPPER(obj.assignor) LIKE UPPER(CONCAT('%', :assignor, '%'))")
    Page<Receivable> searchByAssignor(String assignor, Pageable pageable);
}
