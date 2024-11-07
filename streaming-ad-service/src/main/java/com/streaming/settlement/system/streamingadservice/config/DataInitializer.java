package com.streaming.settlement.system.streamingadservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.*;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;
    private final Random random = new Random();

    // 현재 기준: 2024-11-07 목요일
    private static final LocalDateTime YESTERDAY = LocalDateTime.of(2024, 11, 6, 0, 0);  // 어제
    private static final LocalDateTime LAST_WEEK = LocalDateTime.of(2024, 10, 28, 0, 0);  // 지난주 월요일
    private static final LocalDateTime LAST_MONTH = LocalDateTime.of(2024, 10, 1, 0, 0);  // 지난달 1일

    private static final int STREAMING_COUNT = 50;  // 각 기간별 50건씩
    private static final int AD_COUNT = 100;

    @Override
    public void run(String... args) {
        log.info("더미 데이터 생성 시작");
        cleanupData();
        insertAdvertisementData();
        insertStreamingDataForStatistics();
        insertStreamingAdMapping();
        log.info("더미 데이터 생성 완료");
    }

    private void cleanupData() {
        log.info("기존 데이터 삭제 시작");
        jdbcTemplate.execute("DELETE FROM streaming_ad_mapping");
        jdbcTemplate.execute("DELETE FROM streaming");
        jdbcTemplate.execute("DELETE FROM advertisement");
        log.info("기존 데이터 삭제 완료");
    }

    private void insertStreamingDataForStatistics() {
        // 어제 데이터 (일간 통계용)
        insertStreamingData("일간", YESTERDAY);

        // 지난주 데이터 (주간 통계용)
        insertStreamingData("주간", LAST_WEEK);

        // 지난달 데이터 (월간 통계용)
        insertStreamingData("월간", LAST_MONTH);
    }

    private void insertStreamingData(String period, LocalDateTime baseDate) {
        log.info("{} 통계용 스트리밍 데이터 생성 시작 - {}", period, baseDate.toLocalDate());
        String sql = "INSERT INTO streaming (total_length, views, ad_view_count, acc_play_time, member_id, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();

        for (long i = 1; i <= STREAMING_COUNT; i++) {
            int totalLength = random.nextInt(3600) + 300; // 5분~1시간
            long views = random.nextInt(2_000_000); // 0~200만 조회수
            long adViewCount = (long) (views * (0.6 + random.nextDouble() * 0.3)); // 광고 조회수는 스트리밍 조회수의 60~90%
            int accPlayTime = (int) (views * (random.nextDouble() * 0.8 + 0.2) * totalLength); // 20~100% 시청

            Object[] params = new Object[]{
                    totalLength,
                    views,
                    adViewCount,
                    accPlayTime,
                    random.nextInt(1000) + 1, // member_id 1~1000
                    baseDate.plusSeconds(random.nextInt(86400)), // 하루 동안 랜덤하게 분포
                    baseDate.plusSeconds(random.nextInt(86400))
            };
            batchArgs.add(params);
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.info("{} 통계용 스트리밍 데이터 생성 완료: 총 {}건", period, STREAMING_COUNT);
    }

    private void insertAdvertisementData() {
        log.info("광고 데이터 생성 시작");
        String sql = "INSERT INTO advertisement (created_at, updated_at) VALUES (?, ?)";

        List<Object[]> batchArgs = new ArrayList<>();

        for (long i = 1; i <= AD_COUNT; i++) {
            Object[] params = new Object[]{
                    YESTERDAY,  // 어제 기준으로 생성
                    YESTERDAY
            };
            batchArgs.add(params);
        }

        jdbcTemplate.batchUpdate(sql, batchArgs);
        log.info("광고 데이터 생성 완료: 총 {}건", AD_COUNT);
    }

    private void insertStreamingAdMapping() {
        log.info("스트리밍-광고 매핑 데이터 생성 시작");
        String sql = "INSERT INTO streaming_ad_mapping (streaming_id, ad_id, play_time, created_at, updated_at) " +
                "VALUES (?, ?, ?, ?, ?)";

        String streamingSql = "SELECT id, total_length FROM streaming";

        jdbcTemplate.query(streamingSql, (rs) -> {
            long streamingId = rs.getLong("id");
            int totalLength = rs.getInt("total_length");

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

                Object[] params = new Object[]{
                        streamingId,
                        adId,
                        (i + 1) * 300,    // 5분, 10분, 15분... 시점
                        YESTERDAY,
                        YESTERDAY
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