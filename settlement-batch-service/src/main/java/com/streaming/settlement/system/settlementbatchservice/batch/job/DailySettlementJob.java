package com.streaming.settlement.system.settlementbatchservice.batch.job;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.Settlement;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.ViewPricing;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.StreamingAdMapping;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.Status;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.SettlementRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.ViewPricingRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.streaming.StreamingAdMappingRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.streaming.StreamingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Job.SETTLEMENT_JOB;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Parameter.ID;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.QueryMethod.*;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Reader.SETTLEMENT_READER;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Step.SETTLEMENT_STEP;

@Configuration
@RequiredArgsConstructor
public class DailySettlementJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final StreamingRepository streamingRepository;
    private final SettlementRepository settlementRepository;
    private final ViewPricingRepository viewPricingRepository;
    private final StreamingAdMappingRepository streamingAdMappingRepository;


    @Bean
    public Job settlementJob() {
        return new JobBuilder(SETTLEMENT_JOB, jobRepository)
                .start(settlementStep())
                .build();
    }

    @Bean
    public Step settlementStep() {
        return new StepBuilder(SETTLEMENT_STEP, jobRepository)
                .<Streaming, Settlement>chunk(1, transactionManager)
                .reader(settlementReader())
                .processor(settlementProcessor())
                .writer(settlementWriter())
                .build();
    }

    @Bean
    public RepositoryItemReader<Streaming> settlementReader() {
        return new RepositoryItemReaderBuilder<Streaming>()
                .name(SETTLEMENT_READER)
                .repository(streamingRepository)
                .methodName(FIND_ALL)

                .pageSize(100)
                .sorts(Map.of(ID, Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Streaming, Settlement> settlementProcessor() {
        return item -> {
            LocalDateTime startDate = LocalDate.now().minusDays(1).atStartOfDay();
            LocalDateTime endDate = LocalDate.now().atStartOfDay();

            // 이전 정산 내역 조회
            Optional<Settlement> prevSettlement = settlementRepository
                    .findTopByStreamingIdOrderBySettlementEndDateDesc(item.getId());

            // 스트리밍 조회수 증가분 계산
            Long prevViews = prevSettlement
                    .map(Settlement::getStreamingViews)
                    .orElse(0L);
            Long todayViews = item.getViews() - prevViews;

            // 광고 조회수 증가분 계산
            Long prevAdViews = prevSettlement
                    .map(Settlement::getAdViewCount)
                    .orElse(0L);
            Long todayAdViews = item.getAdViewCount() - prevAdViews;

            // 수익 계산
            BigDecimal streamingRevenue = calculateStreamRevenue(prevViews, todayViews);
            BigDecimal adRevenue = calculateAdRevenue(prevAdViews, todayAdViews);
            BigDecimal totalRevenue = streamingRevenue.add(adRevenue)
                    .setScale(0, RoundingMode.FLOOR);

            // 새로운 정산 데이터 생성
            return Settlement.builder()
                    .streamingRevenue(streamingRevenue)
                    .adRevenue(adRevenue)
                    .totalRevenue(totalRevenue)
                    .streamingViews(item.getViews())
                    .adViewCount(item.getAdViewCount())
                    .status(Status.PENDING)
                    .settlementDate(LocalDate.now().minusDays(1))
                    .settlementStartDate(startDate)
                    .settlementEndDate(endDate)
                    .memberId(item.getMemberId())
                    .streamingId(item.getId())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<Settlement> settlementWriter() {
        return new RepositoryItemWriterBuilder<Settlement>()
                .repository(settlementRepository)
                .methodName(SAVE)
                .build();
    }

    private BigDecimal calculateStreamRevenue(Long prevViews, Long todayViews) {
        BigDecimal revenue = BigDecimal.ZERO;
        List<ViewPricing> pricingList = viewPricingRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ViewPricing::getMinViews))
                .toList();

        long processedViews = prevViews;
        long remainingViews = todayViews;

        for (ViewPricing pricing : pricingList) {
            if (remainingViews <= 0) break;

            long minViews = pricing.getMinViews();
            long maxViews = pricing.getMaxViews() != null ? pricing.getMaxViews() : Long.MAX_VALUE;

            if (processedViews >= maxViews) continue;

            long viewsInRange;
            if (processedViews < minViews) {
                viewsInRange = Math.min(remainingViews, maxViews - minViews);
            } else {
                viewsInRange = Math.min(remainingViews, maxViews - processedViews);
            }

            if (viewsInRange > 0) {
                revenue = revenue.add(new BigDecimal(viewsInRange).multiply(pricing.getStreamRate()));
                processedViews += viewsInRange;
                remainingViews -= viewsInRange;
            }
        }
        return revenue;
    }

    private BigDecimal calculateAdRevenue(Long prevAdViews, Long todayAdViews) {
        BigDecimal revenue = BigDecimal.ZERO;
        List<ViewPricing> pricings = viewPricingRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ViewPricing::getMinViews))
                .toList();

        long processedViews = prevAdViews;
        long remainingViews = todayAdViews;

        for (ViewPricing pricing : pricings) {
            if (remainingViews <= 0) break;

            long minViews = pricing.getMinViews();
            long maxViews = pricing.getMaxViews() != null ? pricing.getMaxViews() : Long.MAX_VALUE;

            if (processedViews >= maxViews) continue;

            long viewsInRange;
            if (processedViews < minViews) {
                viewsInRange = Math.min(remainingViews, maxViews - minViews);
            } else {
                viewsInRange = Math.min(remainingViews, maxViews - processedViews);
            }

            if (viewsInRange > 0) {
                revenue = revenue.add(new BigDecimal(viewsInRange).multiply(pricing.getAdRate()));
                processedViews += viewsInRange;
                remainingViews -= viewsInRange;
            }
        }
        return revenue;
    }

}
