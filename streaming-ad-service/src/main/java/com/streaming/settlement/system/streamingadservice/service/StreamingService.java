package com.streaming.settlement.system.streamingadservice.service;

import com.streaming.settlement.system.common.api.exception.streaming.NotFoundStreamingException;
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

    public void playStreaming(Long streamingId, Long memberId, Integer currentTime, String ipAddress) {
        Streaming streamingEntity = streamingRepository.findById(streamingId)
                .orElseThrow(() -> new NotFoundStreamingException("존재하지 않는 동영상 혹은 재생 불가능한 동영상입니다."));

        Optional<StreamingViewLog> viewLog = streamingViewLogRepository.findByMemberIdAndStreamingId(memberId, streamingId);

        currentTime = (viewLog.isPresent()) ? viewLog.get().getLastPlayTime() : 0;

        // TODO: 어뷰징 방지 구현 -> 어뷰징 방지는 스트리밍, 광고 둘 다 적용되어야한다. 스트리밍 단에서 먼저처리 하는식으로?

        StreamingViewLog viewLogEntity = StreamingViewLog.builder()
                .ipAddress(ipAddress)
                .lastPlayTime(currentTime)
                .viewedAt(LocalDateTime.now())
                .streaming(streamingEntity)
                .memberId(memberId)
                .build();

        streamingViewLogRepository.save(viewLogEntity);

        advertisementService.processAdViews(streamingEntity, currentTime, memberId, ipAddress);

        streamingEntity.incrementViews(); // 조회수 1증가
        streamingRepository.save(streamingEntity);
    }

    //private boolean isAubsing(Streaming)
}
