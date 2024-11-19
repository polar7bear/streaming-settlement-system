package com.streaming.settlement.system.settlementbatchservice.batch.listener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementJobListener implements JobExecutionListener {

    private final MeterRegistry meterRegistry;
    private static final String METRIC_PREFIX = "settlement_batch";

    private Counter jobStartsCounter;
    private Counter jobStatusCounter;
    private Counter jobTotalProcessedCounter;
    private Timer jobDurationTimer;
    private JobExecution currentJobExecution;      // 추가
    private long jobStartTimeMillis;

    @PostConstruct
    public void init() {
        jobStartsCounter = meterRegistry.counter(METRIC_PREFIX + ".job.starts");
        jobStatusCounter = meterRegistry.counter(METRIC_PREFIX + ".job.status");
        jobTotalProcessedCounter = meterRegistry.counter(METRIC_PREFIX + ".job.total_processed");
        jobDurationTimer = Timer.builder(METRIC_PREFIX + ".job.duration_seconds").register(meterRegistry);

        // Gauge를 이용해 실시간 계산
        meterRegistry.gauge(METRIC_PREFIX + ".job.avg_time_per_item_ms",
                this,
                listener -> {
                    if (currentJobExecution != null) {
                        long duration = System.currentTimeMillis() - jobStartTimeMillis;
                        long totalCount = getTotalProcessedCount(currentJobExecution);
                        return totalCount > 0 ? (double) duration / totalCount : 0.0;
                    }
                    return 0.0;
                });

        meterRegistry.gauge(METRIC_PREFIX + ".job.total_tps",
                this,
                listener -> {
                    if (currentJobExecution != null) {
                        long duration = System.currentTimeMillis() - jobStartTimeMillis;
                        long totalCount = getTotalProcessedCount(currentJobExecution);
                        return duration > 0 ? (double) totalCount / (duration / 1000.0) : 0.0;
                    }
                    return 0.0;
                });
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        this.currentJobExecution = jobExecution;   // 현재 Job 저장
        jobStartTimeMillis = System.currentTimeMillis();
        jobStartsCounter.increment();
        log.info("Job [{}] started.", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long jobDurationMillis = System.currentTimeMillis() - jobStartTimeMillis;

        // Timer에 시간 기록
        jobDurationTimer.record(Duration.ofMillis(jobDurationMillis));

        // Job Status 업데이트
        String status = jobExecution.getStatus().name().toLowerCase();
        jobStatusCounter.increment();

        // Total Processed Count 업데이트
        long totalProcessedCount = getTotalProcessedCount(jobExecution);
        jobTotalProcessedCounter.increment(totalProcessedCount);

        log.info("Job [{}] finished with status [{}]. Duration: {} ms, Total Processed: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                jobDurationMillis,
                totalProcessedCount);

        this.currentJobExecution = null;   // Job 완료 후 초기화
    }

    private long getTotalProcessedCount(JobExecution jobExecution) {
        return jobExecution.getStepExecutions().stream()
                .mapToLong(StepExecution::getWriteCount)
                .sum();
    }
}
    /*private final MeterRegistry meterRegistry;
    private static final String METRIC_PREFIX = "settlement_batch";

    // Counters
    private Counter jobStartsCounter;
    private Counter jobStatusCounter;
    private Counter jobTotalProcessedCounter;

    // Timers
    private Timer jobDurationTimer;

    // Gauges using AtomicReference
    private final AtomicReference<Double> avgTimePerItemMs = new AtomicReference<>(0.0);
    private final AtomicReference<Double> totalTps = new AtomicReference<>(0.0);

    @PostConstruct
    public void init() {
        jobStartsCounter = meterRegistry.counter(METRIC_PREFIX + ".job.starts");
        jobStatusCounter = meterRegistry.counter(METRIC_PREFIX + ".job.status");
        jobTotalProcessedCounter = meterRegistry.counter(METRIC_PREFIX + ".job.total_processed");

        jobDurationTimer = meterRegistry.timer(METRIC_PREFIX + ".job.duration_seconds");

        // Register Gauges
        Gauge.builder(METRIC_PREFIX + ".job.avg_time_per_item_ms", avgTimePerItemMs, AtomicReference::get)
                .description("배치 작업 건당 평균 처리 시간 (ms)")
                .register(meterRegistry);

        Gauge.builder(METRIC_PREFIX + ".job.total_tps", totalTps, AtomicReference::get)
                .description("배치 작업 초당 처리 건수 (TPS)")
                .register(meterRegistry);
    }

    private long jobStartTimeMillis;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        jobStartTimeMillis = System.currentTimeMillis();
        jobStartsCounter.increment();
        log.info("Job [{}] started.", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        long jobDurationMillis = System.currentTimeMillis() - jobStartTimeMillis;

        // Timer에 millisecond 단위로 기록
        jobDurationTimer.record(jobDurationMillis, TimeUnit.MILLISECONDS);

        // Job Status 업데이트
        String status = jobExecution.getStatus().name().toLowerCase();
        jobStatusCounter.increment();

        // Total Processed Count 업데이트
        long totalProcessedCount = getTotalProcessedCount(jobExecution);
        jobTotalProcessedCounter.increment(totalProcessedCount);

        // Avg Time 및 TPS 계산 및 업데이트
        if (totalProcessedCount > 0) {
            double avgTime = (double) jobDurationMillis / totalProcessedCount;
            double tps = (double) totalProcessedCount / (jobDurationMillis / 1000.0);

            avgTimePerItemMs.set(avgTime);
            totalTps.set(tps);

            log.debug("Updated Job Metrics - Avg Time: {} ms, TPS: {}", avgTime, tps);
        } else {
            log.warn("Total Processed Count is zero. Metrics for Avg Time and TPS are not updated.");
        }

        log.info("Job [{}] finished with status [{}]. Duration: {} ms, Total Processed: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus(),
                jobDurationMillis,
                totalProcessedCount);
    }

    private long getTotalProcessedCount(JobExecution jobExecution) {
        long total = jobExecution.getStepExecutions().stream()
                .mapToLong(step -> step.getWriteCount())
                .sum();
        log.debug("Total Processed Count: {}", total);
        return total;
    }
}
*/