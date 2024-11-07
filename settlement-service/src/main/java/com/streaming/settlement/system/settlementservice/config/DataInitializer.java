package com.streaming.settlement.system.settlementservice.config;

import com.streaming.settlement.system.settlementservice.domain.entity.ViewPricing;
import com.streaming.settlement.system.settlementservice.repository.ViewPricingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final ViewPricingRepository viewPricingRepository;

    @Override
    public void run(String... args) throws Exception {
        if (viewPricingRepository.count() > 0) {
            return;
        }

        List<ViewPricing> pricings = List.of(
                ViewPricing.builder()  // 10만 미만
                        .minViews(0L)
                        .maxViews(99999L)
                        .streamRate(new BigDecimal("1"))
                        .adRate(new BigDecimal("10"))
                        .build(),
                ViewPricing.builder()  // 10만 이상 50만 미만
                        .minViews(100000L)
                        .maxViews(499999L)
                        .streamRate(new BigDecimal("1.1"))
                        .adRate(new BigDecimal("12"))
                        .build(),
                ViewPricing.builder()  // 50만 이상 100만 미만
                        .minViews(500000L)
                        .maxViews(999999L)
                        .streamRate(new BigDecimal("1.3"))
                        .adRate(new BigDecimal("15"))
                        .build(),
                ViewPricing.builder()  // 100만 이상
                        .minViews(1000000L)
                        .maxViews(null)
                        .streamRate(new BigDecimal("1.5"))
                        .adRate(new BigDecimal("20"))
                        .build()
        );

        viewPricingRepository.saveAll(pricings);
    }

}
