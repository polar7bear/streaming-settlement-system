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
import com.streaming.settlement.system.settlementbatchservice.repository.streaming.StreamingRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.item.data.builder.RepositoryItemReaderBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Job.SETTLEMENT_JOB;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Numeric.CHUNK_SIZE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Numeric.GRID_SIZE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Parameter.*;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.QueryMethod.FIND_STREAMINGS_FOR_SETTLEMENT;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Reader.SETTLEMENT_READER;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Step.MASTER_STEP;
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
                .start(masterStep())
                .build();
    }

    @Bean
    public Step masterStep() {
        return new StepBuilder(MASTER_STEP, jobRepository)
                .partitioner(SETTLEMENT_STEP, partitioner())
                .step(workerStep())
                .gridSize(GRID_SIZE)
                .taskExecutor(new SimpleAsyncTaskExecutor())
                .build();
    }

    @Bean
    public Step workerStep() {
        return new StepBuilder(SETTLEMENT_STEP, jobRepository)
                .<Streaming, Settlement>chunk(CHUNK_SIZE, transactionManager)
                .reader(settlementReader(null, null))
                .processor(settlementProcessor())
                .writer(settlementWriter())
                .listener(settlementStepListener)
                .listener(settlementItemWriteListener)
                .listener(settlementChunkListener)
                .build();
    }

    @Bean
    public Partitioner partitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>(gridSize);

            Long minId = streamingRepository.findMinId();
            Long maxId = streamingRepository.findMaxId();
            long targetSize = (maxId - minId) / gridSize + 1;

            for (int i = 0; i < gridSize; i++) {
                long start = minId + (i * targetSize);
                long end = Math.min(start + targetSize - 1, maxId);

                ExecutionContext context = new ExecutionContext();
                context.putLong(START_ID, start);
                context.putLong(END_ID, end);
                partitions.put(PARTITION + i, context);
            }
            return partitions;
        };
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Streaming> settlementReader(
            @Value("#{stepExecutionContext['startId']}") Long startId,
            @Value("#{stepExecutionContext['endId']}") Long endId) {
        return new RepositoryItemReaderBuilder<Streaming>()
                .name(SETTLEMENT_READER)
                .repository(streamingRepository)
                .methodName(FIND_STREAMINGS_FOR_SETTLEMENT)
                .arguments(startId, endId)
                .pageSize(CHUNK_SIZE)
                .sorts(Map.of(ID, Sort.Direction.ASC))
                .saveState(false)
                .build();
    }


    @Bean
    @StepScope
    public ItemProcessor<Streaming, Settlement> settlementProcessor() {
        return item -> {
            Long totalViews = item.getViews();
            Long totalAdViews = item.getAdViewCount();
            Long prevViews = item.getLastSettlementViews();
            Long prevAdViews = item.getLastSettlementAdCount();

            Long todayViews = totalViews - prevViews;
            Long todayAdViews = totalAdViews - prevAdViews;

            BigDecimal streamingRevenue = calculateRevenue(prevViews, todayViews, ViewPricing::getStreamRate);
            BigDecimal adRevenue = calculateRevenue(prevAdViews, todayAdViews, ViewPricing::getAdRate);
            BigDecimal totalRevenue = streamingRevenue.add(adRevenue).setScale(0, RoundingMode.FLOOR);

            // 새로운 정산 데이터 생성
            return Settlement.of(item, todayViews, todayAdViews, streamingRevenue, adRevenue, totalRevenue);
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Settlement> settlementWriter() {
        return items -> {
            List<Settlement> settlements = new ArrayList<>(items.getItems());
            // Bulk insert settlements
            List<Settlement> savedSettlements = settlementRepository.saveAll(settlements);

            // Prepare bulk update for streaming
            Map<Long, Settlement> updateBatch = savedSettlements.stream()
                    .collect(Collectors.toMap(
                            Settlement::getStreamingId,
                            settlement -> settlement
                    ));

            // Bulk update streaming
            if (!updateBatch.isEmpty()) {
                streamingRepository.bulkUpdateLastSettlementInfo(
                        new ArrayList<>(updateBatch.keySet()),
                        LocalDate.now().minusDays(1).atStartOfDay()
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

            /*long viewsInRange;
            if (processedViews < minViews) {
                viewsInRange = Math.min(remainingViews, maxViews - minViews);
            } else {
                viewsInRange = Math.min(remainingViews, maxViews - processedViews);
            }*/
            long startViews = Math.max(processedViews, minViews);
            long endViews = Math.min(processedViews + remainingViews, maxViews);
            long viewsInRange = endViews - startViews;

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
