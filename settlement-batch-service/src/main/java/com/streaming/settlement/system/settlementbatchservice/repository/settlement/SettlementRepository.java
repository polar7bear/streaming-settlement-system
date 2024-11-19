package com.streaming.settlement.system.settlementbatchservice.repository.settlement;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("SELECT s FROM Settlement s WHERE s.settlementDate <= :date " +
            "AND s.id IN (SELECT MAX(s2.id) FROM Settlement s2 GROUP BY s2.streamingId)")
    List<Settlement> findLatestSettlementsByDate(LocalDate date);
}
