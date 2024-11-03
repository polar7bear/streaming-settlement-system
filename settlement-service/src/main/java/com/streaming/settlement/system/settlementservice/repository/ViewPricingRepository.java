package com.streaming.settlement.system.settlementservice.repository;

import com.streaming.settlement.system.settlementservice.domain.entity.ViewPricing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewPricingRepository extends JpaRepository<ViewPricing, Long> {
}
