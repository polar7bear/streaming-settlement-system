package com.streaming.settlement.system.settlementbatchservice.repository.streaming;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

public interface StreamingRepository extends JpaRepository<Streaming, Long> {

    @Query("""
                SELECT s FROM Streaming s 
                WHERE MOD(s.id, :gridSize) = :partitionIndex
                AND (
                    (s.lastSettlementDate IS NULL AND (s.views >= 1000 OR s.adViewCount >= 500))
                    OR (s.lastSettlementDate IS NOT NULL 
                        AND (s.views > s.lastSettlementViews OR s.adViewCount > s.lastSettlementAdCount))
                )
            """)
    Page<Streaming> findStreamingsForSettlement(int gridSize, int partitionIndex, Pageable pageable);

    @Modifying
    @Transactional
    @Query("UPDATE Streaming s SET " +
            "s.lastSettlementViews = :views, " +
            "s.lastSettlementAdCount = :adViews, " +
            "s.lastSettlementDate = :settlementDate " +
            "WHERE s.id = :id")
    void updateLastSettlementInfo(Long id, Long views, Long adViews, LocalDateTime settlementDate);
}
