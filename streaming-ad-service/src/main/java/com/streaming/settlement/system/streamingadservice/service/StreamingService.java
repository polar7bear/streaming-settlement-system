package com.streaming.settlement.system.streamingadservice.service;

import com.streaming.settlement.system.common.api.exception.streaming.NotFoundStreamingException;
import com.streaming.settlement.system.common.api.exception.streaming.NotFoundStreamingViewLogException;
import com.streaming.settlement.system.streamingadservice.domain.entity.Streaming;
import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingViewLog;
import com.streaming.settlement.system.streamingadservice.repository.StreamingRepository;
import com.streaming.settlement.system.streamingadservice.repository.StreamingViewLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class StreamingService {

    private final StreamingRepository streamingRepository;
    private final StreamingViewLogRepository streamingViewLogRepository;

    private final AdvertisementService advertisementService;

    @Transactional
    public Integer playStreaming(Long streamingId, Long memberId, Integer currentTime, String ipAddress) {
        Streaming streamingEntity = streamingRepository.findById(streamingId)
                .orElseThrow(() -> new NotFoundStreamingException("존재하지 않는 동영상 혹은 재생 불가능한 동영상입니다."));

        boolean isAbuse = isAbusing(streamingId, ipAddress) || streamingEntity.getMemberId().equals(memberId);

        Optional<StreamingViewLog> viewLog = Optional.empty();
        if (memberId == null) {
            viewLog = streamingViewLogRepository.findFirstByIpAddressAndStreamingIdOrderByViewedAtDesc(ipAddress, streamingId);
        } else if (memberId != null) {
            viewLog = streamingViewLogRepository.findFirstByMemberIdAndStreamingIdOrderByViewedAtDesc(memberId, streamingId);
        }

        if (currentTime == null) {
            currentTime = viewLog.map(StreamingViewLog::getLastPlayTime).orElse(0);
        }

        if (!isAbuse) {
            streamingRepository.incrementViews(streamingId);

            if (viewLog.isEmpty()) { //시청 기록이 없을경우에만 시청기록 생성
                // 사용자가 영상을 중지하지않고 그냥 창을 꺼버릴 경우에는 마지막 재생시간을 어떻게 관리해야할지 고민해보자 (해당 페이지에서 다른 페이지로 이동하거나 윈도우가 꺼졌을 경우 특정 api를 서버로 요청?)
                //
                StreamingViewLog viewLogEntity = StreamingViewLog.builder()
                        .ipAddress(ipAddress)
                        .lastPlayTime(currentTime)
                        .viewedAt(LocalDateTime.now())
                        .streaming(streamingEntity)
                        .memberId(memberId)
                        .build();

                streamingViewLogRepository.save(viewLogEntity);
            }
        }

        advertisementService.processAdViews(streamingEntity, currentTime, memberId, ipAddress, isAbuse);

        return currentTime;
    }

    public void pauseStreaming(Long streamingId, Long memberId, String ipAddress, Integer currentTime) {
        StreamingViewLog streamingViewLogEntity = null;
        if (memberId != null) {
            streamingViewLogEntity = streamingViewLogRepository.findFirstByMemberIdAndStreamingIdOrderByViewedAtDesc(memberId, streamingId)
                    .orElseThrow(() -> new NotFoundStreamingViewLogException("시청 기록을 찾을 수 없습니다."));
        } else if (memberId == null) {
            streamingViewLogEntity = streamingViewLogRepository.findFirstByIpAddressAndStreamingIdOrderByViewedAtDesc(ipAddress, streamingId)
                    .orElseThrow(() -> new NotFoundStreamingViewLogException("시청 기록을 찾을 수 없습니다."));
        }

        streamingViewLogEntity.saveLastPlayTimeByPause(currentTime);
        streamingViewLogRepository.save(streamingViewLogEntity);
    }

    private boolean isAbusing(Long streamingId, String ipAddress) {
        LocalDateTime thirtySecondsAgo = LocalDateTime.now().minusSeconds(30);
        return streamingViewLogRepository.existsByStreamingIdAndIpAddressAndViewedAtAfter(streamingId, ipAddress, thirtySecondsAgo);
    }
}
