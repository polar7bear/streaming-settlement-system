package com.streaming.settlement.system.streamingadservice.config;

import com.streaming.settlement.system.streamingadservice.domain.entity.Advertisement;
import com.streaming.settlement.system.streamingadservice.domain.entity.Streaming;
import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingAdMapping;
import com.streaming.settlement.system.streamingadservice.repository.AdvertisementRepository;
import com.streaming.settlement.system.streamingadservice.repository.StreamingAdMappingRepository;
import com.streaming.settlement.system.streamingadservice.repository.StreamingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StreamingRepository streamingRepository;
    private final AdvertisementRepository advertisementRepository;
    private final StreamingAdMappingRepository streamingAdMappingRepository;

    @Override
    public void run(String... args) throws Exception {
        Streaming streaming1 = Streaming.builder()
                .totalLength(600)
                .views(100L)
                .isSettled(false)
                .accPlayTime(300)
                .memberId(1L)
                .build();
        Streaming streaming2 = Streaming.builder()
                .totalLength(1200)
                .views(250L)
                .isSettled(true)
                .accPlayTime(600)
                .memberId(2L)
                .build();
        Streaming streaming3 = Streaming.builder()
                .totalLength(1800)
                .views(50L)
                .isSettled(false)
                .accPlayTime(900)
                .memberId(3L)
                .build();
        streamingRepository.save(streaming1);
        streamingRepository.save(streaming2);
        streamingRepository.save(streaming3);
        
        Advertisement ad1 = Advertisement.builder()
                .adPlayTime(300)
                .count(10)
                .adRevenue(BigDecimal.valueOf(50.00))
                .build();
        Advertisement ad2 = Advertisement.builder()
                .adPlayTime(600)
                .count(20)
                .adRevenue(BigDecimal.valueOf(100.00))
                .build();
        Advertisement ad3 = Advertisement.builder()
                .adPlayTime(900)
                .count(15)
                .adRevenue(BigDecimal.valueOf(75.00))
                .build();
        Advertisement ad4 = Advertisement.builder()
                .adPlayTime(300)
                .count(5)
                .adRevenue(BigDecimal.valueOf(25.00))
                .build();
        Advertisement ad5 = Advertisement.builder()
                .adPlayTime(600)
                .count(10)
                .adRevenue(BigDecimal.valueOf(50.00))
                .build();
        Advertisement ad6 = Advertisement.builder()
                .adPlayTime(1200)
                .count(8)
                .adRevenue(BigDecimal.valueOf(40.00))
                .build();
        advertisementRepository.save(ad1);
        advertisementRepository.save(ad2);
        advertisementRepository.save(ad3);
        advertisementRepository.save(ad4);
        advertisementRepository.save(ad5);
        advertisementRepository.save(ad6);

        // Streaming과 Advertisement의 매핑 테이블 데이터 삽입
        streamingAdMappingRepository.save(StreamingAdMapping.builder()
                .streaming(streaming1)
                .advertisement(ad1)
                .build());
        streamingAdMappingRepository.save(StreamingAdMapping.builder()
                .streaming(streaming1)
                .advertisement(ad2)
                .build());
        streamingAdMappingRepository.save(StreamingAdMapping.builder()
                .streaming(streaming2)
                .advertisement(ad1)
                .build());
        streamingAdMappingRepository.save(StreamingAdMapping.builder()
                .streaming(streaming2)
                .advertisement(ad2)
                .build());
        streamingAdMappingRepository.save(StreamingAdMapping.builder()
                .streaming(streaming2)
                .advertisement(ad3)
                .build());
        streamingAdMappingRepository.save(StreamingAdMapping.builder()
                .streaming(streaming3)
                .advertisement(ad4)
                .build());
        streamingAdMappingRepository.save(StreamingAdMapping.builder()
                .streaming(streaming3)
                .advertisement(ad5)
                .build());
        streamingAdMappingRepository.save(StreamingAdMapping.builder()
                .streaming(streaming3)
                .advertisement(ad6)
                .build());
    }

}
