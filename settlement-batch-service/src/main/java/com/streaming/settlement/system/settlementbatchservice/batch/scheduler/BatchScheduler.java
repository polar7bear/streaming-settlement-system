package com.streaming.settlement.system.settlementbatchservice.batch.scheduler;

import com.streaming.settlement.system.common.api.exception.batch.BatchJobException;
import com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.NoSuchJobException;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static com.streaming.settlement.system.settlementbatchservice.batch.BatchConstant.Job.*;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchScheduler {

    private final JobLauncher jobLauncher;
    private final JobRegistry jobRegistry;

    private final static String STR_DATE = "date";
    private final static String STR_TIME = "time";

    /*//@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "10 * * * * *", zone = "Asia/Seoul")
    public void dailyTop5Job() {
        executeJob(DAILY_JOB);
    }

    //@Scheduled(cron = "0 0 0 * * MON", zone = "Asia/Seoul")
    @Scheduled(cron = "10 * * * * *", zone = "Asia/Seoul")
    public void weeklyTop5Job() {
        executeJob(WEEKLY_JOB);
    }

    //@Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Seoul")
    @Scheduled(cron = "10 * * * * *", zone = "Asia/Seoul")
    public void monthlyTop5Job() {
        executeJob(MONTHLY_JOB);
    }*/

    //@Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    @Scheduled(cron = "30 48 * * * *", zone = "Asia/Seoul")
    public void settlementJob() {
        executeJob(SETTLEMENT_JOB);
    }



    private void executeJob(String jobName) {
        try {
            Job job = jobRegistry.getJob(jobName);
            runJob(job, jobName);
        } catch (NoSuchJobException e) {
            log.error("JobRegistry에 등록된 Job이 아닙니다: {}", jobName, e);
            throw new BatchJobException("찾을 수 없는 Job: " + jobName, e);
        }
    }

    private void runJob(Job job, String jobName) {
        log.info("{} 시작 - {}", jobName, LocalDateTime.now());

        JobParameters jobParameters = new JobParametersBuilder()
                .addString(STR_DATE, LocalDate.now().toString())
                .addLong(STR_TIME, System.currentTimeMillis())
                .toJobParameters();

        try {
            JobExecution run = jobLauncher.run(job, jobParameters);
            logJobResult(run, jobName);
        } catch (Exception e) {
            handleJobError(jobName, e);
        }
    }


    private void logJobResult(JobExecution run, String jobName) {
        if (run.getStatus() == BatchStatus.COMPLETED) {
            log.info("{} 완료 - {}", jobName, LocalDateTime.now());
        } else {
            log.error("{} 실패: {}", jobName, run.getStatus());
        }
    }

    private void handleJobError(String jobName, Exception e) {
        log.error("{} 잡 실행 중 오류 발생", jobName, e);
        throw new BatchJobException(jobName + " 실행 실패", e);
    }

}
