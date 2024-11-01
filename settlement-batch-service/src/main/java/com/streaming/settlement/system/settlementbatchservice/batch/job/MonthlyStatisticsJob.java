package com.streaming.settlement.system.settlementbatchservice.batch.job;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.StatisticsSummary;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.TopStreamingStatistics;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.DateRange;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.StatisticsType;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.StatisticsSummaryRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.TopStreamingStatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Job.MONTHLY_JOB;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Parameter.END_DATE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Parameter.START_DATE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Query.TOP_PLAY_TIME_QUERY;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Query.TOP_VIEWS_QUERY;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.QueryMethod.SAVE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Reader.TOP_PLAY_TIME_READER;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Reader.TOP_VIEW_READER;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Step.*;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
public class MonthlyStatisticsJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final TopStreamingStatisticsRepository topStreamingStatisticsRepository;
    private final StatisticsSummaryRepository statisticsSummaryRepository;

    private final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    public MonthlyStatisticsJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, TopStreamingStatisticsRepository topStreamingStatisticsRepository, StatisticsSummaryRepository statisticsSummaryRepository, @Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.topStreamingStatisticsRepository = topStreamingStatisticsRepository;
        this.statisticsSummaryRepository = statisticsSummaryRepository;
        this.entityManagerFactoryBean = entityManagerFactoryBean;
    }


    @Bean
    public Job monthlyJob() {
        return new JobBuilder(MONTHLY_JOB, jobRepository)
                .start(monthlyTop5ViewStep())
                .next(monthlyTop5PlayTimeStep())
                .build();
    }

    @Bean
    public Step monthlyTop5ViewStep() {
        return new StepBuilder(MONTHLY_TOP_VIEW_STEP, jobRepository)
                .<Streaming, TopStreamingStatistics>chunk(5, transactionManager)
                .reader(monthlyTop5ViewReader(entityManagerFactoryBean))
                .processor(monthlyTop5ViewProcessor())
                .writer(monthlyTop5ViewWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Streaming> monthlyTop5ViewReader(@Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaCursorItemReaderBuilder<Streaming>()
                .name(TOP_VIEW_READER)
                .entityManagerFactory(requireNonNull(entityManagerFactoryBean.getObject()))
                .queryString(TOP_VIEWS_QUERY)
                .parameterValues(Map.of(
                        START_DATE, LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay(),
                        END_DATE, LocalDate.now().withDayOfMonth(1).atStartOfDay()
                ))
                .maxItemCount(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Streaming, TopStreamingStatistics> monthlyTop5ViewProcessor() {
        AtomicInteger rank = new AtomicInteger(0);

        return streaming -> {
            StatisticsSummary summary = statisticsSummaryRepository.findByDateRangeAndTargetDate(DateRange.MONTHLY, LocalDate.now().minusMonths(1).withDayOfMonth(1))
                    .orElseGet(() ->
                            statisticsSummaryRepository.save(
                                    StatisticsSummary.builder()
                                            .dateRange(DateRange.MONTHLY)
                                            .targetDate(LocalDate.now().minusMonths(1).withDayOfMonth(1))
                                            .build()
                            ));

            return TopStreamingStatistics.builder()
                    .statisticsSummary(summary)
                    .streamingId(streaming.getId())
                    .views(streaming.getViews())
                    .totalPlayTime(streaming.getAccPlayTime())
                    .statisticsType(StatisticsType.VIEWS)
                    .ranking(rank.incrementAndGet())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<TopStreamingStatistics> monthlyTop5ViewWriter() {
        return new RepositoryItemWriterBuilder<TopStreamingStatistics>()
                .repository(topStreamingStatisticsRepository)
                .methodName(SAVE)
                .build();
    }


    @Bean
    public Step monthlyTop5PlayTimeStep() {
        return new StepBuilder(MONTHLY_TOP_PLAY_TIME_STEP, jobRepository)
                .<Streaming, TopStreamingStatistics>chunk(5, transactionManager)
                .reader(monthlyTop5PlayTimeReader(entityManagerFactoryBean))
                .processor(monthlyTop5PlayTimeProcessor())
                .writer(monthlyTop5PlayTimeWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Streaming> monthlyTop5PlayTimeReader(@Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaCursorItemReaderBuilder<Streaming>()
                .name(TOP_PLAY_TIME_READER)
                .entityManagerFactory(requireNonNull(entityManagerFactoryBean.getObject()))
                .queryString(TOP_PLAY_TIME_QUERY)
                .parameterValues(Map.of(
                        START_DATE, LocalDate.now().minusMonths(1).withDayOfMonth(1).atStartOfDay(),
                        END_DATE, LocalDate.now().withDayOfMonth(1).atStartOfDay()
                ))
                .maxItemCount(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Streaming, TopStreamingStatistics> monthlyTop5PlayTimeProcessor() {
        AtomicInteger rank = new AtomicInteger(0);

        return streaming -> {
            StatisticsSummary summary = statisticsSummaryRepository.findByDateRangeAndTargetDate(DateRange.MONTHLY, LocalDate.now().minusMonths(1).withDayOfMonth(1))
                    .orElseGet(() ->
                            statisticsSummaryRepository.save(
                                    StatisticsSummary.builder()
                                            .dateRange(DateRange.MONTHLY)
                                            .targetDate(LocalDate.now().minusMonths(1).withDayOfMonth(1))
                                            .build()
                            ));

            return TopStreamingStatistics.builder()
                    .statisticsSummary(summary)
                    .streamingId(streaming.getId())
                    .views(streaming.getViews())
                    .totalPlayTime(streaming.getAccPlayTime())
                    .statisticsType(StatisticsType.PLAY_TIME)
                    .ranking(rank.incrementAndGet())
                    .build();
        };
    }

    @Bean
    public RepositoryItemWriter<TopStreamingStatistics> monthlyTop5PlayTimeWriter() {
        return new RepositoryItemWriterBuilder<TopStreamingStatistics>()
                .repository(topStreamingStatisticsRepository)
                .methodName(SAVE)
                .build();
    }

}

