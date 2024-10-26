package com.streaming.settlement.system.streamingadservice.service;

import com.streaming.settlement.system.common.api.exception.streaming.NotFoundStreamingException;
import com.streaming.settlement.system.common.api.exception.streaming.NotFoundStreamingViewLogException;
import com.streaming.settlement.system.streamingadservice.domain.entity.Streaming;
import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingViewLog;
import com.streaming.settlement.system.streamingadservice.repository.StreamingRepository;
import com.streaming.settlement.system.streamingadservice.repository.StreamingViewLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class StreamingService {

    private final StreamingRepository streamingRepository;
    private final StreamingViewLogRepository streamingViewLogRepository;

    private final AdvertisementService advertisementService;

    public Integer playStreaming(Long streamingId, Long memberId, Integer currentTime, String ipAddress) {
        Streaming streamingEntity = streamingRepository.findById(streamingId)
                .orElseThrow(() -> new NotFoundStreamingException("존재하지 않는 동영상 혹은 재생 불가능한 동영상입니다."));

        boolean isAbuse = isAbusing(streamingId, ipAddress) || streamingEntity.getMemberId().equals(memberId);

        Optional<StreamingViewLog> viewLog = streamingViewLogRepository.findByMemberIdAndStreamingId(memberId, streamingId);
        currentTime = (viewLog.isPresent()) ? viewLog.get().getLastPlayTime() : 0;

        StreamingViewLog viewLogEntity = StreamingViewLog.builder()
                .ipAddress(ipAddress)
                .lastPlayTime(currentTime)
                .viewedAt(LocalDateTime.now())
                .streaming(streamingEntity)
                .memberId(memberId)
                .build();

        streamingViewLogRepository.save(viewLogEntity);

        advertisementService.processAdViews(streamingEntity, currentTime, memberId, ipAddress, isAbuse);

        if (!isAbuse) {
            streamingEntity.incrementViews(); // 조회수 1증가
            streamingRepository.save(streamingEntity);
        }

        return currentTime;
    }

    public void pauseStreaming(Long streamingId, Long memberId, Integer currentTime) {
        StreamingViewLog streamingViewLogEntity = streamingViewLogRepository.findByMemberIdAndStreamingId(memberId, streamingId)
                .orElseThrow(() -> new NotFoundStreamingViewLogException("시청 기록을 찾을 수 없습니다."));

        streamingViewLogEntity.saveLastPlayTimeByPause(currentTime);
        streamingViewLogRepository.save(streamingViewLogEntity);
    }

    private boolean isAbusing(Long streamingId, String ipAddress) {
        LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(30);
        return streamingViewLogRepository.existsByStreamingIdAndIpAddressAndViewedAtAfter(streamingId, ipAddress, thirtySecondsAgo);
    }
}
