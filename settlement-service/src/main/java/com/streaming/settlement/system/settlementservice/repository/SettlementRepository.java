package com.streaming.settlement.system.settlementservice.repository;

import com.streaming.settlement.system.settlementservice.domain.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    @Query("SELECT s FROM Settlement s " +
            "WHERE s.memberId = :memberId " +
            "AND s.settlementDate = :date")
    List<Settlement> findByMemberIdAndDate(Long memberId, LocalDate date);

    @Query("SELECT s FROM Settlement s " +
            "WHERE s.memberId = :memberId " +
            "AND s.settlementDate BETWEEN :startDate AND :endDate")
    List<Settlement> findByMemberIdAndDateBetween(Long memberId, LocalDate startDate, LocalDate endDate);
}
