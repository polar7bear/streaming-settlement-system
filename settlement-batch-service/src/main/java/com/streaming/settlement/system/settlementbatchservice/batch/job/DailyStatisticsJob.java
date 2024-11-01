package com.streaming.settlement.system.settlementbatchservice.batch.job;

import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.StatisticsSummary;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.settlement.TopStreamingStatistics;
import com.streaming.settlement.system.settlementbatchservice.domain.entity.streaming.Streaming;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.DateRange;
import com.streaming.settlement.system.settlementbatchservice.domain.enums.StatisticsType;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.StatisticsSummaryRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.settlement.TopStreamingStatisticsRepository;
import com.streaming.settlement.system.settlementbatchservice.repository.streaming.StreamingRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.data.RepositoryItemWriter;
import org.springframework.batch.item.data.builder.RepositoryItemWriterBuilder;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cglib.core.Local;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Configuration
public class DailyStatisticsJob {

    private final DataSource dataSource;
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final StreamingRepository streamingRepository;
    private final TopStreamingStatisticsRepository topStreamingStatisticsRepository;
    private final StatisticsSummaryRepository statisticsSummaryRepository;

    private final LocalContainerEntityManagerFactoryBean entityManagerFactoryBean;

    private final static String JDBC_QUERY =
            "SELECT * FROM streaming " +
            "WHERE created_at >= ? " +
            "AND created_at < ? " +
            "ORDER BY views DESC LIMIT 5";

    private final static String JPQL_QUERY =
            "SELECT s FROM Streaming s " +
            "WHERE s.createdAt >= :startDate " +
            "AND s.createdAt < :endDate " +
            "ORDER BY s.views DESC";


    public DailyStatisticsJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, StreamingRepository streamingRepository, TopStreamingStatisticsRepository topStreamingStatisticsRepository, StatisticsSummaryRepository statisticsSummaryRepository, @Qualifier("streamingAdDataSource") DataSource dataSource, @Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.streamingRepository = streamingRepository;
        this.topStreamingStatisticsRepository = topStreamingStatisticsRepository;
        this.statisticsSummaryRepository = statisticsSummaryRepository;
        this.dataSource = dataSource;
        this.entityManagerFactoryBean = entityManagerFactoryBean;
    }


    // 일별 통계
    @Bean
    public Job dailyJob() {
        return new JobBuilder("dailyJob", jobRepository)
                .start(dailyTop5ViewStep())
                //.next(dailyTop5AccPlayTimeStep())
                .build();
    }

    // 조회수 top5
    @Bean
    public Step dailyTop5ViewStep() {
        return new StepBuilder("dailyTop5ViewStep", jobRepository)
                .<Streaming, TopStreamingStatistics>chunk(5, transactionManager)
                .reader(top5ViewReader(entityManagerFactoryBean))
                .processor(top5ViewProcessor())
                .writer(top5ViewWriter())
                .build();
    }


    /*@Bean
    public JdbcCursorItemReader<Streaming> top5ViewReader() {
        Object[] params = new Object[]{
                LocalDate.now().atStartOfDay(),
                LocalDate.now().plusDays(1).atStartOfDay()
        };

        log.info("Start Date: {}", params[0]);
        log.info("End Date: {}", params[1]);
        JdbcCursorItemReader<Streaming> top5ViewReader = new JdbcCursorItemReaderBuilder<Streaming>()
                .name("top5ViewReader")
                .dataSource(dataSource)
                .sql(QUERY)
                .queryArguments(params)
                .rowMapper(new StreamingRowMapper())
                .build();
        top5ViewReader.setVerifyCursorPosition(true);
        return top5ViewReader;
    }*/


    @Bean
    public JpaCursorItemReader<Streaming> top5ViewReader(@Qualifier("streamingAdEntityManager") LocalContainerEntityManagerFactoryBean entityManagerFactoryBean) {
        return new JpaCursorItemReaderBuilder<Streaming>()
                .name("top5ViewReader")
                .entityManagerFactory(Objects.requireNonNull(entityManagerFactoryBean.getObject()))
                .queryString(JPQL_QUERY)
                .parameterValues(Map.of(
                        "startDate", LocalDate.now().atStartOfDay(),
                        "endDate", LocalDate.now().plusDays(1).atStartOfDay()
                ))
                .maxItemCount(5)
                .build();
    }

    @Bean
    public ItemProcessor<Streaming, TopStreamingStatistics> top5ViewProcessor() {
        AtomicInteger rank = new AtomicInteger(0);

        return streaming -> {
            log.info("Processing streaming: {}", streaming);

            StatisticsSummary summary = statisticsSummaryRepository.findByDateRangeAndTargetDate(DateRange.DAILY, LocalDate.now().minusDays(1))
                    .orElseGet(() ->
                            statisticsSummaryRepository.save(
                                    StatisticsSummary.builder()
                                            .dateRange(DateRange.DAILY)
                                            .targetDate(LocalDate.now().minusDays(1))
                                            .build()
                            ));

            TopStreamingStatistics result = TopStreamingStatistics.builder()
                    .statisticsSummary(summary)
                    .streamingId(streaming.getId())
                    .views(streaming.getViews())
                    .totalPlayTime(streaming.getAccPlayTime())
                    .statisticsType(StatisticsType.VIEWS)
                    .ranking(rank.incrementAndGet())
                    .build();
            log.info("Created TopStreamingStatistics: {}", result);


            return result;
        };
    }

    @Bean
    public RepositoryItemWriter<TopStreamingStatistics> top5ViewWriter() {
        return new RepositoryItemWriterBuilder<TopStreamingStatistics>()
                .repository(topStreamingStatisticsRepository)
                .methodName("save")
                .build();
    }


    /*// 누적 재생시간 탑5
    @Bean
    public Step dailyTop5AccPlayTimeStep() {
        return new StepBuilder("dailyTop5AccPlayTimeStep", jobRepository)
                .<Streaming, TopStreamingStatistics>chunk(5, transactionManager)
                .reader()
                .processor()
                .writer()
                .build();
    }*/


    /*private static class StreamingRowMapper implements RowMapper<Streaming> {
        @Override
        public Streaming mapRow(ResultSet rs, int rowNum) throws SQLException {
            return Streaming.builder()
                    .id(rs.getLong("id"))
                    .totalLength(rs.getInt("total_length"))
                    .views(rs.getLong("views"))
                    .isSettled(rs.getBoolean("is_settled"))
                    .accPlayTime(rs.getInt("acc_play_time"))
                    .memberId(rs.getLong("member_id"))
                    .build();
        }
    }*/
}

