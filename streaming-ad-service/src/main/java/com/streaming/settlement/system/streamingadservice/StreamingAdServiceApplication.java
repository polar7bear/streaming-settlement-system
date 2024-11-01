package com.streaming.settlement.system.streamingadservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class StreamingAdServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(StreamingAdServiceApplication.class, args);
    }

}
