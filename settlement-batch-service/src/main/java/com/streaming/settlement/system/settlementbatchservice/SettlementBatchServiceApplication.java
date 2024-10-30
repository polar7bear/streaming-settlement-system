package com.streaming.settlement.system.settlementbatchservice;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

//@EnableBatchProcessing 스프링부트 3.x 버전 이상에서는 비활성화
@SpringBootApplication
public class SettlementBatchServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(SettlementBatchServiceApplication.class, args);
    }

}
