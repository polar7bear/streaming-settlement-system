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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Job.WEEKLY_JOB;
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
public class WeeklyStatisticsJob {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final TopStreamingStatisticsRepository topStreamingStatisticsRepository;
    private final StatisticsSummaryRepository statisticsSummaryRepository;

    private final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    public WeeklyStatisticsJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, TopStreamingStatisticsRepository topStreamingStatisticsRepository, StatisticsSummaryRepository statisticsSummaryRepository, @Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.topStreamingStatisticsRepository = topStreamingStatisticsRepository;
        this.statisticsSummaryRepository = statisticsSummaryRepository;
        this.entityManagerFactoryBean = entityManagerFactoryBean;
    }


    @Bean
    public Job weeklyJob() {
        return new JobBuilder(WEEKLY_JOB, jobRepository)
                .start(weeklyTop5ViewStep())
                .next(weeklyTop5PlayTimeStep())
                .build();
    }

    @Bean
    public Step weeklyTop5ViewStep() {
        return new StepBuilder(WEEKLY_TOP_VIEW_STEP, jobRepository)
                .<Streaming, TopStreamingStatistics>chunk(5, transactionManager)
                .reader(weeklyTop5ViewReader(entityManagerFactoryBean))
                .processor(weeklyTop5ViewProcessor())
                .writer(weeklyTop5ViewWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Streaming> weeklyTop5ViewReader(@Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaCursorItemReaderBuilder<Streaming>()
                .name(TOP_VIEW_READER)
                .entityManagerFactory(requireNonNull(entityManagerFactoryBean.getObject()))
                .queryString(TOP_VIEWS_QUERY)
                .parameterValues(Map.of(
                        START_DATE, LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(),
                        END_DATE, LocalDate.now().minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay()
                ))
                .maxItemCount(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Streaming, TopStreamingStatistics> weeklyTop5ViewProcessor() {
        AtomicInteger rank = new AtomicInteger(0);

        return streaming -> {
            StatisticsSummary summary = statisticsSummaryRepository.findByDateRangeAndTargetDate(DateRange.WEEKLY, LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
                    .orElseGet(() ->
                            statisticsSummaryRepository.save(
                                    StatisticsSummary.builder()
                                            .dateRange(DateRange.WEEKLY)
                                            .targetDate(LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
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
    public RepositoryItemWriter<TopStreamingStatistics> weeklyTop5ViewWriter() {
        return new RepositoryItemWriterBuilder<TopStreamingStatistics>()
                .repository(topStreamingStatisticsRepository)
                .methodName(SAVE)
                .build();
    }

    @Bean
    public Step weeklyTop5PlayTimeStep() {
        return new StepBuilder(WEEKLY_TOP_PLAY_TIME_STEP, jobRepository)
                .<Streaming, TopStreamingStatistics>chunk(1, transactionManager)
                .reader(weeklyTop5PlayTimeReader(entityManagerFactoryBean))
                .processor(weeklyTop5PlayTimeProcessor())
                .writer(weeklyTop5PlayTimeWriter())
                .build();
    }

    @Bean
    public JpaCursorItemReader<Streaming> weeklyTop5PlayTimeReader(@Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaCursorItemReaderBuilder<Streaming>()
                .name(TOP_PLAY_TIME_READER)
                .entityManagerFactory(requireNonNull(entityManagerFactoryBean.getObject()))
                .queryString(TOP_PLAY_TIME_QUERY)
                .parameterValues(Map.of(
                        START_DATE, LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay(),
                        END_DATE, LocalDate.now().minusWeeks(1).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay()
                ))
                .maxItemCount(5)
                .build();
    }

    @Bean
    @StepScope
    public ItemProcessor<Streaming, TopStreamingStatistics> weeklyTop5PlayTimeProcessor() {
        AtomicInteger rank = new AtomicInteger(0);

        return streaming -> {
            StatisticsSummary summary = statisticsSummaryRepository.findByDateRangeAndTargetDate(DateRange.WEEKLY, LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
                    .orElseGet(() ->
                            statisticsSummaryRepository.save(
                                    StatisticsSummary.builder()
                                            .dateRange(DateRange.WEEKLY)
                                            .targetDate(LocalDate.now().minusWeeks(1).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)))
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
    public RepositoryItemWriter<TopStreamingStatistics> weeklyTop5PlayTimeWriter() {
        return new RepositoryItemWriterBuilder<TopStreamingStatistics>()
                .repository(topStreamingStatisticsRepository)
                .methodName(SAVE)
                .build();
    }

}

