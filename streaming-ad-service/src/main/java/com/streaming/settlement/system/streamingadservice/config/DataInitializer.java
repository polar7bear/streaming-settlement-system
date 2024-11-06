package com.streaming.settlement.system.streamingadservice.config;

import com.streaming.settlement.system.streamingadservice.domain.entity.Advertisement;
import com.streaming.settlement.system.streamingadservice.domain.entity.Streaming;
import com.streaming.settlement.system.streamingadservice.domain.entity.StreamingAdMapping;
import com.streaming.settlement.system.streamingadservice.repository.AdvertisementRepository;
import com.streaming.settlement.system.streamingadservice.repository.StreamingAdMappingRepository;
import com.streaming.settlement.system.streamingadservice.repository.StreamingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();
    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2024, 11, 4, 0, 0);

    private static final int STREAMING_COUNT = 50;
    private static final int AD_COUNT = 100;

    @Override
    public void run(String... args) {
        log.info("더미 데이터 생성 시작");
        cleanupData();
        insertStreamingData();
        insertAdvertisementData();
        insertStreamingAdMapping();
        log.info("더미 데이터 생성 완료");
    }

    private void cleanupData() {
        log.info("기존 데이터 삭제 시작");
        //jdbcTemplate.execute("DELETE FROM settlement_ad_views");
        jdbcTemplate.execute("DELETE FROM streaming_ad_mapping");
        jdbcTemplate.execute("DELETE FROM streaming");
        jdbcTemplate.execute("DELETE FROM advertisement");
        log.info("기존 데이터 삭제 완료");
    }

    private void insertStreamingData() {
        log.info("스트리밍 데이터 생성 시작");
        String sql = "INSERT INTO streaming (total_length, views, acc_play_time, member_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();

        for (long i = 1; i <= STREAMING_COUNT; i++) {
            int totalLength = random.nextInt(3600) + 300;
            long views = random.nextInt(2_000_000);
            int accPlayTime = (int)(views * (random.nextDouble() * 0.8 + 0.2) * totalLength);

            Object[] params = new Object[]{
                    totalLength,
                    views,
                    accPlayTime,
                    random.nextInt(1000) + 1,
                    BASE_DATE.plusSeconds(random.nextInt(86400)),
                    BASE_DATE.plusSeconds(random.nextInt(86400))
            };
            batchArgs.add(params);
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.info("스트리밍 데이터 생성 완료: 총 {}건", STREAMING_COUNT);
    }

    private void insertAdvertisementData() {
        log.info("광고 데이터 생성 시작");
        String sql = "INSERT INTO advertisement (created_at, updated_at) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();

        for (long i = 1; i <= AD_COUNT; i++) {
            Object[] params = new Object[]{
                    BASE_DATE,
                    BASE_DATE
            };
            batchArgs.add(params);
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.info("광고 데이터 생성 완료: 총 {}건", AD_COUNT);
    }

    private void insertStreamingAdMapping() {
        log.info("스트리밍-광고 매핑 데이터 생성 시작");
        String sql = "INSERT INTO streaming_ad_mapping (streaming_id, ad_id, play_time, views, last_settled_count, last_settlement_date, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String streamingSql = "SELECT id, total_length, views FROM streaming";

        jdbcTemplate.query(streamingSql, (rs) -> {
            long streamingId = rs.getLong("id");
            int totalLength = rs.getInt("total_length");
            long streamingViews = rs.getLong("views");

            // 5분(300초)당 1개 광고 삽입
            int adCount = totalLength / 300;
            List<Object[]> batchArgs = new ArrayList<>();

            Set<Integer> usedAdIds = new HashSet<>();
            for (int i = 0; i < adCount; i++) {
                int adId;
                do {
                    adId = random.nextInt(AD_COUNT) + 1;
                } while (usedAdIds.contains(adId));

                usedAdIds.add(adId);
                long adViews = (long) (streamingViews * (0.6 + random.nextDouble() * 0.3));

                Object[] params = new Object[]{
                        streamingId,
                        adId,
                        (i + 1) * 300,    // 5분, 10분, 15분... 시점
                        adViews,  // 0~200만 조회수
                        0L,               // 마지막 정산 시점의 조회수
                        BASE_DATE.toLocalDate(),  // 마지막 정산 날짜
                        BASE_DATE,
                        BASE_DATE
                };
                batchArgs.add(params);
            }

            if (!batchArgs.isEmpty()) {
                jdbcTemplate.batchUpdate(sql, batchArgs);
            }

            log.info("스트리밍 ID {} (길이: {}초) 처리 완료 (광고 {}개 매핑)", streamingId, totalLength, adCount);
        });

        log.info("스트리밍-광고 매핑 데이터 생성 완료");
    }
}
