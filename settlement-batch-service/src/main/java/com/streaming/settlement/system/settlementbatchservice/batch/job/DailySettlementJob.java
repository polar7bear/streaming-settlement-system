package com.streaming.settlement.system.settlementbatchservice.batch.job;

import com.streaming.settlement.system.settlementbatchservice.batch.listener.SettlementChunkListener;
import com.streaming.settlement.system.settlementbatchservice.batch.listener.SettlementItemWriteListener;
import com.streaming.settlement.system.settlementbatchservice.batch.listener.SettlementJobListener;
import com.streaming.settlement.system.settlementbatchservice.batch.listener.SettlementStepListener;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.Settlement;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.ViewPricing;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.SettlementRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.ViewPricingRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.streaming.StreamingAdMappingRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.streaming.StreamingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Job.SETTLEMENT_JOB;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Parameter.ID;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.QueryMethod.FIND_STREAMINGS_FOR_SETTLEMENT;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Reader.SETTLEMENT_READER;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Step.SETTLEMENT_STEP;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DailySettlementJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final StreamingRepository streamingRepository;
    private final SettlementRepository settlementRepository;
    private final ViewPricingRepository viewPricingRepository;

    private final SettlementJobListener settlementJobListener;
    private final SettlementStepListener settlementStepListener;
    private final SettlementChunkListener settlementChunkListener;
    private final SettlementItemWriteListener settlementItemWriteListener;

    private List<ViewPricing> cachedPricingList;


    @PostConstruct
    public void init() {
        this.cachedPricingList = viewPricingRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ViewPricing::getMinViews))
                .toList();
    }


    @Bean
    public Job settlementJob() {
        return new JobBuilder(SETTLEMENT_JOB, jobRepository)
                .listener(settlementJobListener)
                .start(settlementStep())
                .build();
    }

    @Bean
    public Step settlementStep() {
        return new StepBuilder(SETTLEMENT_STEP, jobRepository)
                .<Streaming, Settlement>chunk(1000, transactionManager)
                .reader(settlementReader())
                .processor(settlementProcessor())
                .writer(settlementWriter())
                .listener(settlementStepListener)
                .listener(settlementItemWriteListener)
                .listener(settlementChunkListener)
                .build();
    }

    @Bean
    public RepositoryItemReader<Streaming> settlementReader() {
        return new RepositoryItemReaderBuilder<Streaming>()
                .name(SETTLEMENT_READER)
                .repository(streamingRepository)
                .methodName(FIND_STREAMINGS_FOR_SETTLEMENT)
                .pageSize(1000)
                .sorts(Map.of(ID, Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Streaming, Settlement> settlementProcessor() {
        return item -> {

            // 첫 정산이 아닌 경우, 조회수 증가분 체크
            if (item.getLastSettlementDate() != null) {
                boolean hasIncreasedViews = item.getViews() > item.getLastSettlementViews();
                boolean hasIncreasedAdViews = item.getAdViewCount() > item.getLastSettlementAdCount();
                if (!hasIncreasedViews && !hasIncreasedAdViews) {
                    return null;
                }
            }

            Long todayViews = item.getViews() - item.getLastSettlementViews();
            Long todayAdViews = item.getAdViewCount() - item.getLastSettlementAdCount();

            // 수익 계산
            BigDecimal streamingRevenue = calculateRevenue(item.getLastSettlementViews(), todayViews, ViewPricing::getStreamRate);
            BigDecimal adRevenue = calculateRevenue(item.getLastSettlementAdCount(), todayAdViews, ViewPricing::getAdRate);
            BigDecimal totalRevenue = streamingRevenue.add(adRevenue)
                    .setScale(0, RoundingMode.FLOOR);

            // 새로운 정산 데이터 생성
            return Settlement.of(item, todayViews, todayAdViews, streamingRevenue, adRevenue, totalRevenue);
        };
    }

    @Bean
    public ItemWriter<Settlement> settlementWriter() {
        return items -> {
            for (Settlement settlement : items) {
                settlementRepository.save(settlement);

                streamingRepository.updateLastSettlementInfo(
                        settlement.getStreamingId(),
                        settlement.getStreamingViews(),
                        settlement.getAdViewCount(),
                        settlement.getSettlementDate().atStartOfDay()
                );
            }
        };
    }

    private BigDecimal calculateRevenue(Long prevViews, Long todayViews, Function<ViewPricing, BigDecimal> rateExtractor) {
        BigDecimal revenue = BigDecimal.ZERO;

        long processedViews = prevViews;
        long remainingViews = todayViews;

        for (ViewPricing pricing : cachedPricingList) {
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
                BigDecimal rate = rateExtractor.apply(pricing);
                revenue = revenue.add(new BigDecimal(viewsInRange).multiply(rate));
                processedViews += viewsInRange;
                remainingViews -= viewsInRange;
            }
        }
        return revenue;
    }

}
