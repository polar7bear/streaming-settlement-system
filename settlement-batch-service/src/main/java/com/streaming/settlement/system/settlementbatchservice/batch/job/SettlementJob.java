package com.streaming.settlement.system.settlementbatchservice.batch.job;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.Settlement;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.ViewPricing;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.Status;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.SettlementRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.ViewPricingRepository;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.Sort;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Job.SETTLEMENT_JOB;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Parameter.ID;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.QueryMethod.FIND_BY_CREATED_AT_BETWEEN;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.QueryMethod.SAVE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Reader.SETTLEMENT_READER;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Step.SETTLEMENT_STEP;

@Configuration
@RequiredArgsConstructor
public class SettlementJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final StreamingRepository streamingRepository;
    private final SettlementRepository settlementRepository;
    private final ViewPricingRepository viewPricingRepository;


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
                .methodName(FIND_BY_CREATED_AT_BETWEEN)
                .arguments(
                        LocalDate.now().minusDays(1).atStartOfDay(),
                        LocalDate.now().atStartOfDay()
                )
                .pageSize(100)
                .sorts(Map.of(ID, Sort.Direction.ASC))
                .build();
    }

    @Bean
    public ItemProcessor<Streaming, Settlement> settlementProcessor() {
        return new ItemProcessor<Streaming, Settlement>() {
            @Override
            public Settlement process(Streaming item) throws Exception {
                Optional<Settlement> prevSettlement = settlementRepository.findTopByStreamingIdOrderByCreatedAtDesc(item.getId());
                Long prevViews = prevSettlement
                        .map(Settlement::getViews)
                        .orElse(0L);

                Long todayViews = item.getViews() - prevViews;

                BigDecimal streamingRevenue = calculateStreamRevenue(prevViews, todayViews);
                int adCount = calculateAdCount(item.getTotalLength());
                BigDecimal adRevenue = calculateAdRevenue(prevViews, todayViews, adCount);

                BigDecimal totalRevenue = streamingRevenue.add(adRevenue)
                        .setScale(0, RoundingMode.FLOOR);

                return Settlement.builder()
                        .streamingRevenue(streamingRevenue)
                        .adRevenue(adRevenue)
                        .totalRevenue(totalRevenue)
                        .views(item.getViews())
                        .status(Status.PENDING)
                        .settlementDate(LocalDate.now().minusDays(1))
                        .memberId(item.getMemberId())
                        .streamingId(item.getId())
                        .build();
            }
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
        List<ViewPricing> pricingList = viewPricingRepository.findAll() // TODO: 캐싱 고려
                .stream()
                .sorted(Comparator.comparing(ViewPricing::getMinViews))
                .toList();

        long currentViews = prevViews;
        long remainingViews = todayViews;

        for (ViewPricing pricing : pricingList) {
            if (remainingViews <= 0) break;

            long viewsInRange;
            if (pricing.getMaxViews() == null) {
                viewsInRange = remainingViews;
            } else {
                viewsInRange = Math.min(remainingViews, pricing.getMaxViews() - currentViews);
            }

            if (viewsInRange > 0) {
                revenue = revenue.add(new BigDecimal(viewsInRange).multiply(pricing.getStreamRate()));
                currentViews += viewsInRange;
                remainingViews -= viewsInRange;
            }
        }
        return revenue;
    }

    private BigDecimal calculateAdRevenue(Long previousViews, Long todayViews, int adCount) {
        BigDecimal revenue = BigDecimal.ZERO;
        List<ViewPricing> pricings = viewPricingRepository.findAll()
                .stream()
                .sorted(Comparator.comparing(ViewPricing::getMinViews))
                .toList();

        long currentViews = previousViews;
        long remainingViews = todayViews;

        for (ViewPricing pricing : pricings) {
            if (remainingViews <= 0) break;

            long viewsInRange;
            if (pricing.getMaxViews() == null) {
                viewsInRange = remainingViews;
            } else {
                viewsInRange = Math.min(remainingViews, pricing.getMaxViews() - currentViews);
            }

            if (viewsInRange > 0) {
                revenue = revenue.add(new BigDecimal(viewsInRange).multiply(pricing.getAdRate()).multiply(BigDecimal.valueOf(adCount)));
                currentViews += viewsInRange;
                remainingViews -= viewsInRange;
            }
        }

        return revenue;
    }

    private int calculateAdCount(int totalLength) {
        return (totalLength / 300) + (totalLength % 300 > 0 ? 1 : 0);
    }
}
