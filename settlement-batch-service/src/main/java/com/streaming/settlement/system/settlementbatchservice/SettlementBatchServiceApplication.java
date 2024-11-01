package com.streaming.settlement.system.settlementbatchservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableJpaAuditing
@SpringBootApplication
public class SettlementBatchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementBatchServiceApplication.class, args);
    }

}
