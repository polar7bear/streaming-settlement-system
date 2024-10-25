package com.streaming.settlement.system.streamingadservice.service;

import com.streaming.settlement.system.streamingadservice.domain.entity.AdViewLog;
import com.streaming.settlement.system.streamingadservice.domain.entity.Advertisement;
import com.streaming.settlement.system.streamingadservice.domain.entity.Streaming;
import com.streaming.settlement.system.streamingadservice.repository.AdViewLogRepository;
import com.streaming.settlement.system.streamingadservice.repository.AdvertisementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdvertisementService {

    private final AdvertisementRepository advertisementRepository;
    private final AdViewLogRepository adViewLogRepository;

    // 광고 시청 처리
    public void processAdViews(Streaming streamingEntity, Integer currentTime, Long memberId, String ipAddress) {
        List<Advertisement> ads = advertisementRepository.findByStreamingId(streamingEntity.getId());

        for (Advertisement ad : ads) {
            // 예를들어 10분(600초)짜리 영상에 300초가 첫광고 시청 시점임 getadPlayTime은 스트리밍 등록되는 순간 광고도 자동적으로 insert된다. (일단 둘다 더미데이터로 추가하는게 맞음)
            // 그리고 사용자의 재생시간이 등록된 영상의 광고 재생타임인 300초를 넘기게되면 광고 시청 처리 하면된다.
            if (ad.getAdPlayTime() <= currentTime) {
                ad.incrementCount(); // TODO: 수익은 배치 처리할 때 알아보도록 하자.
                advertisementRepository.save(ad);

                AdViewLog adViewLogEntity = AdViewLog.builder()
                        .ipAddress(ipAddress)
                        .viewedAt(LocalDateTime.now())
                        .advertisement(ad)
                        .memberId(memberId) //스트리밍 엔티티의 memberId는 동영상 등록한 creator의 회원 고유번호임. 당연하게도 엔드포인트에서 요청으로 들어온 회원의 id로 저장해야한다.
                        .build();
                adViewLogRepository.save(adViewLogEntity);
            }
        }
    }
}
