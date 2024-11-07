package com.streaming.settlement.system.streamingadservice.service;

import com.streaming.settlement.system.streamingadservice.domain.entity.AdViewLog;
import com.streaming.settlement.system.streamingadservice.domain.entity.Advertisement;
import com.streaming.settlement.system.streamingadservice.domain.entity.Streaming;
import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingAdMapping;
import com.streaming.settlement.system.streamingadservice.repository.AdViewLogRepository;
import com.streaming.settlement.system.streamingadservice.repository.AdvertisementRepository;
import com.streaming.settlement.system.streamingadservice.repository.StreamingAdMappingRepository;
import com.streaming.settlement.system.streamingadservice.repository.StreamingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdViewLogRepository adViewLogRepository;
    private final StreamingAdMappingRepository streamingAdMappingRepository;
    private final StreamingRepository streamingRepository;


    public void processAdViews(Streaming streamingEntity, Integer currentTime, Long memberId, String ipAddress, boolean isAbuse) {
        List<StreamingAdMapping> mappings = streamingAdMappingRepository.findByStreamingId(streamingEntity.getId());
        log.info("[AdvertisementService processAdViews] mappings: {}", mappings);

        for (StreamingAdMapping mapping : mappings) {
            if (mapping.getPlayTime() <= currentTime) {
                boolean hasViewed = adViewLogRepository.existsByMappingAndIpAddressAndMemberId(
                        mapping, ipAddress, memberId
                );

                if (!hasViewed) {

                    if (!isAbuse) {
                        streamingEntity.incrementAdViewCount();
                        streamingRepository.save(streamingEntity);
                    }

                    AdViewLog adViewLogEntity = AdViewLog.builder()
                            .ipAddress(ipAddress)
                            .viewedAt(LocalDateTime.now())
                            .mapping(mapping)
                            .memberId(memberId)
                            .build();

                    adViewLogRepository.save(adViewLogEntity);
                }
            }
        }
    }
}
