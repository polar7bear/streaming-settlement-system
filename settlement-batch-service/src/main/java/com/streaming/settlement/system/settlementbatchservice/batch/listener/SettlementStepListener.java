package com.streaming.settlement.system.settlementbatchservice.batch.listener;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Component
@RequiredArgsConstructor
public class SettlementStepListener implements StepExecutionListener {

    private final MeterRegistry meterRegistry;
    private static final String METRIC_PREFIX = "settlement_batch";

    private Counter stepStartsCounter;
    private Counter stepWriteCountCounter;
    private Timer stepDurationTimer;
    private StepExecution currentStepExecution;    // 추가
    private long stepStartTimeMillis;

    @PostConstruct
    public void init() {
        stepStartsCounter = meterRegistry.counter(METRIC_PREFIX + ".step.starts");
        stepWriteCountCounter = meterRegistry.counter(METRIC_PREFIX + ".step.write_count");
        stepDurationTimer = Timer.builder(METRIC_PREFIX + ".step.duration_seconds").register(meterRegistry);

        meterRegistry.gauge(METRIC_PREFIX + ".step.avg_time_per_item_ms",
                this,
                listener -> {
                    if (currentStepExecution != null) {
                        long duration = System.currentTimeMillis() - stepStartTimeMillis;
                        long writeCount = currentStepExecution.getWriteCount();
                        return writeCount > 0 ? (double) duration / writeCount : 0.0;
                    }
                    return 0.0;
                });

        meterRegistry.gauge(METRIC_PREFIX + ".step.tps",
                this,
                listener -> {
                    if (currentStepExecution != null) {
                        long duration = System.currentTimeMillis() - stepStartTimeMillis;
                        long writeCount = currentStepExecution.getWriteCount();
                        return duration > 0 ? (double) writeCount / (duration / 1000.0) : 0.0;
                    }
                    return 0.0;
                });

        meterRegistry.gauge(METRIC_PREFIX + ".step.throughput_per_minute",
                this,
                listener -> {
                    if (currentStepExecution != null) {
                        long duration = System.currentTimeMillis() - stepStartTimeMillis;
                        long writeCount = currentStepExecution.getWriteCount();
                        return duration > 0 ? (double) writeCount / (duration / 1000.0) * 60 : 0.0;
                    }
                    return 0.0;
                });

        meterRegistry.gauge(METRIC_PREFIX + ".step.items_per_chunk",
                this,
                listener -> {
                    if (currentStepExecution != null) {
                        long writeCount = currentStepExecution.getWriteCount();
                        long commitCount = currentStepExecution.getCommitCount();
                        return commitCount > 0 ? (double) writeCount / commitCount : 0.0;
                    }
                    return 0.0;
                });
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        this.currentStepExecution = stepExecution;  // 현재 Step 저장
        stepStartTimeMillis = System.currentTimeMillis();
        stepStartsCounter.increment();
        log.info("Step [{}] started.", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long stepDurationMillis = System.currentTimeMillis() - stepStartTimeMillis;

        // Timer에 시간 기록
        stepDurationTimer.record(Duration.ofMillis(stepDurationMillis));

        // 메트릭 업데이트
        long writeCount = stepExecution.getWriteCount();
        stepWriteCountCounter.increment(writeCount);

        // 추가 메트릭 기록
        meterRegistry.counter(METRIC_PREFIX + ".step.read_count").increment(stepExecution.getReadCount());
        meterRegistry.counter(METRIC_PREFIX + ".step.commit_count").increment(stepExecution.getCommitCount());
        meterRegistry.counter(METRIC_PREFIX + ".step.rollback_count").increment(stepExecution.getRollbackCount());
        meterRegistry.counter(METRIC_PREFIX + ".step.skip_count").increment(stepExecution.getSkipCount());

        log.info("Step [{}] finished. Duration: {} ms, Items Processed: {}, TPS: {}",
                stepExecution.getStepName(),
                stepDurationMillis,
                writeCount,
                writeCount > 0 ? ((double) writeCount / (stepDurationMillis / 1000.0)) : 0.0);

        this.currentStepExecution = null;  // Step 완료 후 초기화
        return stepExecution.getExitStatus();
    }
}
    /*private final MeterRegistry meterRegistry;
    private static final String METRIC_PREFIX = "settlement_batch";

    // Counters
    private Counter stepStartsCounter;
    private Counter stepWriteCountCounter;

    // Timers
    private Timer stepDurationTimer;

    // Gauges using AtomicReference
    private final AtomicReference<Double> avgTimePerItemMs = new AtomicReference<>(0.0);
    private final AtomicReference<Double> tps = new AtomicReference<>(0.0);
    private final AtomicReference<Double> throughputPerMinute = new AtomicReference<>(0.0);
    private final AtomicReference<Double> itemsPerChunk = new AtomicReference<>(0.0);

    @PostConstruct
    public void init() {
        stepStartsCounter = meterRegistry.counter(METRIC_PREFIX + ".step.starts");
        stepWriteCountCounter = meterRegistry.counter(METRIC_PREFIX + ".step.write_count");

        stepDurationTimer = meterRegistry.timer(METRIC_PREFIX + ".step.duration_seconds");

        // Register Gauges
        Gauge.builder(METRIC_PREFIX + ".step.avg_time_per_item_ms", avgTimePerItemMs, AtomicReference::get)
                .description("스텝 건당 평균 처리 시간 (ms)")
                .register(meterRegistry);

        Gauge.builder(METRIC_PREFIX + ".step.tps", tps, AtomicReference::get)
                .description("스텝 초당 처리 건수 (TPS)")
                .register(meterRegistry);

        Gauge.builder(METRIC_PREFIX + ".step.throughput_per_minute", throughputPerMinute, AtomicReference::get)
                .description("스텝 분당 처리 건수")
                .register(meterRegistry);

        Gauge.builder(METRIC_PREFIX + ".step.items_per_chunk", itemsPerChunk, AtomicReference::get)
                .description("스텝 청크당 처리 건수")
                .register(meterRegistry);
    }

    private long stepStartTimeMillis;

    @Override
    public void beforeStep(StepExecution stepExecution) {
        stepStartsCounter.increment();
        stepStartTimeMillis = System.currentTimeMillis();
        log.info("Step [{}] started.", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        long stepDurationMillis = System.currentTimeMillis() - stepStartTimeMillis;
        // Timer에 millisecond 단위로 기록
        stepDurationTimer.record(stepDurationMillis, TimeUnit.MILLISECONDS);

        // Write Count 업데이트
        long writeCount = stepExecution.getWriteCount();
        stepWriteCountCounter.increment(writeCount);

        // Avg Time, TPS, Throughput, Items per Chunk 계산 및 업데이트
        if (writeCount > 0) {
            double avgTime = (double) stepDurationMillis / writeCount;
            double tpsValue = (double) writeCount / (stepDurationMillis / 1000.0);
            double throughputPerMinValue = tpsValue * 60;
            double itemsPerChunkValue = stepExecution.getCommitCount() > 0 ? (double) writeCount / stepExecution.getCommitCount() : 0.0;

            avgTimePerItemMs.set(avgTime);
            tps.set(tpsValue);
            throughputPerMinute.set(throughputPerMinValue);
            itemsPerChunk.set(itemsPerChunkValue);

            log.debug("Updated Step Metrics - Avg Time: {} ms, TPS: {}, Throughput/Min: {}, Items/Chunk: {}",
                    avgTime, tpsValue, throughputPerMinValue, itemsPerChunkValue);
        } else {
            log.warn("Write Count is zero. Metrics for Avg Time, TPS, Throughput, Items per Chunk are not updated.");
        }

        log.info("Step [{}] finished. Duration: {} ms, Items Processed: {}, TPS: {}",
                stepExecution.getStepName(),
                stepDurationMillis,
                writeCount,
                writeCount > 0 ? ((double) writeCount / (stepDurationMillis / 1000.0)) : 0.0);

        return stepExecution.getExitStatus();
    }
}*/