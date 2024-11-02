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

import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Job.DAILY_JOB;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Parameter.END_DATE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Parameter.START_DATE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Query.TOP_PLAY_TIME_QUERY;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Query.TOP_VIEWS_QUERY;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.QueryMethod.SAVE;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Reader.TOP_PLAY_TIME_READER;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Reader.TOP_VIEW_READER;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Step.DAILY_TOP_PLAY_TIME_STEP;
import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Step.DAILY_TOP_VIEW_STEP;
import static java.util.Objects.requireNonNull;

@Slf4j
@Configuration
public class DailyStatisticsJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final TopStreamingStatisticsRepository topStreamingStatisticsRepository;
    private final StatisticsSummaryRepository statisticsSummaryRepository;

    private final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    public DailyStatisticsJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, TopStreamingStatisticsRepository topStreamingStatisticsRepository, StatisticsSummaryRepository statisticsSummaryRepository, @Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.topStreamingStatisticsRepository = topStreamingStatisticsRepository;
        this.statisticsSummaryRepository = statisticsSummaryRepository;
        this.entityManagerFactoryBean = entityManagerFactoryBean;
    }


    // 일별 통계
    @Bean
    public Job dailyJob() {
        return new JobBuilder(DAILY_JOB, jobRepository)
                .start(dailyTop5ViewStep())
                .next(dailyTop5PlayTimeStep())
                .build();
    }

    // 조회수 top5
    @Bean
    public Step dailyTop5ViewStep() {
        return new StepBuilder(DAILY_TOP_VIEW_STEP, jobRepository)
                .<Streaming, TopStreamingStatistics>chunk(1, transactionManager)
                .reader(dailyTop5ViewReader(entityManagerFactoryBean))
                .processor(dailyTop5ViewProcessor())
                .writer(dailyTop5ViewWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Streaming> dailyTop5ViewReader(@Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaCursorItemReaderBuilder<Streaming>()
                .name(TOP_VIEW_READER)
                .entityManagerFactory(requireNonNull(entityManagerFactoryBean.getObject()))
                .queryString(TOP_VIEWS_QUERY)
                .parameterValues(Map.of(
                        START_DATE, LocalDate.now().minusDays(1).atStartOfDay(),
                        END_DATE, LocalDate.now().atStartOfDay()
                ))
                .maxItemCount(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Streaming, TopStreamingStatistics> dailyTop5ViewProcessor() {
        AtomicInteger rank = new AtomicInteger(0);

        return streaming -> {
            StatisticsSummary summary = statisticsSummaryRepository.findByDateRangeAndTargetDate(DateRange.DAILY, LocalDate.now().minusDays(1))
                    .orElseGet(() ->
                            statisticsSummaryRepository.save(
                                    StatisticsSummary.builder()
                                            .dateRange(DateRange.DAILY)
                                            .targetDate(LocalDate.now().minusDays(1))
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
    public RepositoryItemWriter<TopStreamingStatistics> dailyTop5ViewWriter() {
        return new RepositoryItemWriterBuilder<TopStreamingStatistics>()
                .repository(topStreamingStatisticsRepository)
                .methodName(SAVE)
                .build();
    }


    // 누적 재생시간 탑5
    @Bean
    public Step dailyTop5PlayTimeStep() {
        return new StepBuilder(DAILY_TOP_PLAY_TIME_STEP, jobRepository)
                .<Streaming, TopStreamingStatistics>chunk(5, transactionManager)
                .reader(dailyTop5PlayTimeReader(entityManagerFactoryBean))
                .processor(dailyTop5PlayTimeProcessor())
                .writer(dailyTop5PlayTimeWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Streaming> dailyTop5PlayTimeReader(@Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaCursorItemReaderBuilder<Streaming>()
                .name(TOP_PLAY_TIME_READER)
                .entityManagerFactory(requireNonNull(entityManagerFactoryBean.getObject()))
                .queryString(TOP_PLAY_TIME_QUERY)
                .parameterValues(Map.of(
                        START_DATE, LocalDate.now().minusDays(1).atStartOfDay(),
                        END_DATE, LocalDate.now().atStartOfDay()
                ))
                .maxItemCount(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Streaming, TopStreamingStatistics> dailyTop5PlayTimeProcessor() {
        AtomicInteger rank = new AtomicInteger(0);

        return streaming -> {
            StatisticsSummary summary = statisticsSummaryRepository.findByDateRangeAndTargetDate(DateRange.DAILY, LocalDate.now().minusDays(1))
                    .orElseGet(() ->
                            statisticsSummaryRepository.save(
                                    StatisticsSummary.builder()
                                            .dateRange(DateRange.DAILY)
                                            .targetDate(LocalDate.now().minusDays(1))
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
    public RepositoryItemWriter<TopStreamingStatistics> dailyTop5PlayTimeWriter() {
        return new RepositoryItemWriterBuilder<TopStreamingStatistics>()
                .repository(topStreamingStatisticsRepository)
                .methodName(SAVE)
                .build();
    }

}

