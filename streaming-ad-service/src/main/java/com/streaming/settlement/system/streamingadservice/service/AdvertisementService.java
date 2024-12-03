package com.streaming.settlement.system.streamingadservice.service;

import com.streaming.settlement.system.streamingadservice.domain.entity.AdViewLog;
import com.streaming.settlement.system.streamingadservice.domain.entity.Streaming;
import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingAdMapping;
import com.streaming.settlement.system.streamingadservice.repository.AdViewLogRepository;
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

        for (StreamingAdMapping mapping : mappings) {
            if (mapping.getPlayTime() <= currentTime) {
                boolean hasViewed;
                if (memberId != null) {
                    hasViewed = adViewLogRepository.existsByMappingAndIpAddressAndMemberId(mapping, ipAddress, memberId);
                } else {
                    hasViewed = adViewLogRepository.existsByMappingAndIpAddressAndMemberIdIsNull(mapping, ipAddress);
                }

                if (!hasViewed) {

                    if (!isAbuse) {
                        streamingRepository.incrementAdViewCount(streamingEntity.getId());
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
