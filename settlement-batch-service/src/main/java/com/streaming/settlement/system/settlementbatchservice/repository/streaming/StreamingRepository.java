package com.streaming.settlement.system.settlementbatchservice.repository.streaming;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.hibernate.jpa.HibernateHints.*;

public interface StreamingRepository extends JpaRepository<Streaming, Long> {

    @Query("""
            SELECT s FROM Streaming s
            WHERE s.id BETWEEN :startId AND :endId
            AND (
                (s.lastSettlementDate IS NULL AND (s.views >= 1000 OR s.adViewCount >= 500))
                OR (s.lastSettlementDate IS NOT NULL AND (s.views > s.lastSettlementViews OR s.adViewCount > s.lastSettlementAdCount))
            )
            """)
    @QueryHints(value = {
            @QueryHint(name = HINT_COMMENT, value = "idx_streaming_settlement"),
            @QueryHint(name = HINT_READ_ONLY, value = "true"),
            @QueryHint(name = HINT_FETCH_SIZE, value = "5000")
    })
    Page<Streaming> findStreamingsForSettlement(Long startId, Long endId, Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Transactional
    @Query("""
            UPDATE Streaming s SET s.lastSettlementViews = s.views,
                s.lastSettlementAdCount = s.adViewCount,
                s.lastSettlementDate = :settlementDate
            WHERE s.id IN :ids
            """)
    @QueryHints(@QueryHint(name = HINT_COMMENT, value = "idx_streaming_update"))
    void bulkUpdateLastSettlementInfo(List<Long> ids, LocalDateTime settlementDate);

    @Query("SELECT MIN(s.id) FROM Streaming s")
    Long findMinId();

    @Query("SELECT MAX(s.id) FROM Streaming s")
    Long findMaxId();

}
