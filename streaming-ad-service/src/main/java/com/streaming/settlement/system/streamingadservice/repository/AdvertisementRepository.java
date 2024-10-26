package com.streaming.settlement.system.streamingadservice.repository;

import com.streaming.settlement.system.streamingadservice.domain.entity.Advertisement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdvertisementRepository extends JpaRepository<Advertisement, Long> {
}
