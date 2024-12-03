package com.streaming.settlement.system.streamingadservice.repository;

import com.streaming.settlement.system.streamingadservice.domain.entity.Streaming;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface StreamingRepository extends JpaRepository<Streaming, Long> {

    @Modifying
    @Query("UPDATE Streaming s " +
            "SET s.views = s.views + 1 " +
            "WHERE s.id = :streamingId")
    void incrementViews(Long streamingId);


    @Modifying
    @Query("UPDATE Streaming s " +
            "SET s.adViewCount = s.adViewCount + 1 " +
            "WHERE s.id = :streamingId")
    void incrementAdViewCount(Long streamingId);
}
