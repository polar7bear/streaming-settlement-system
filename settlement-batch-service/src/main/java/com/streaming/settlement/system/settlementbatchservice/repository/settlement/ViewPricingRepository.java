package com.streaming.settlement.system.settlementbatchservice.repository.settlement;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.ViewPricing;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ViewPricingRepository extends JpaRepository<ViewPricing, Long> {
}
