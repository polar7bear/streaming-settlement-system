package com.streaming.settlement.system.settlementbatchservice.batch;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BatchConstant {

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Job {
        public static final String DAILY_JOB = "dailyJob";
        public static final String WEEKLY_JOB = "weeklyJob";
        public static final String MONTHLY_JOB = "monthlyJob";

        public static final String SETTLEMENT_JOB = "settlementJob";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Step {
        public static final String DAILY_TOP_VIEW_STEP = "dailyTop5ViewStep";
        public static final String DAILY_TOP_PLAY_TIME_STEP = "dailyTop5PlayTimeStep";

        public static final String WEEKLY_TOP_VIEW_STEP = "weeklyTop5ViewStep";
        public static final String WEEKLY_TOP_PLAY_TIME_STEP = "weeklyTop5PlayTimeStep";

        public static final String MONTHLY_TOP_VIEW_STEP = "monthlyTop5ViewStep";
        public static final String MONTHLY_TOP_PLAY_TIME_STEP = "monthlyTop5PlayTimeStep";

        public static final String SETTLEMENT_STEP = "settlementStep";
        public static final String MASTER_STEP = "masterStep";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Reader {
        public static final String TOP_VIEW_READER = "top5ViewReader";
        public static final String TOP_PLAY_TIME_READER = "top5PlayTimeReader";

        public static final String SETTLEMENT_READER = "settlementReader";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Query {

        public static final String TOP_VIEWS_QUERY =
                "SELECT s FROM Streaming s " +
                        "WHERE s.createdAt >= :startDate " +
                        "AND s.createdAt < :endDate " +
                        "ORDER BY s.views DESC";

        public static final String TOP_PLAY_TIME_QUERY =
                "SELECT s FROM Streaming s " +
                        "WHERE s.createdAt >= :startDate " +
                        "AND s.createdAt < :endDate " +
                        "ORDER BY s.accPlayTime DESC";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Parameter {

        public static final String START_DATE = "startDate";
        public static final String END_DATE = "endDate";

        public static final String ID = "id";

        public static final String PARTITION_INDEX = "partitionIndex";
        public static final String PARTITION = "partition";
        public static final String START_ID = "startId";
        public static final String END_ID = "endId";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class QueryMethod {
        public static final String SAVE = "save";

        public static final String FIND_STREAMINGS_FOR_SETTLEMENT = "findStreamingsForSettlement";
        public static final String FIND_ALL = "findAll";
    }

    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static final class Numeric {
        public static final int CHUNK_SIZE = 5000;
        public static final int THREAD_COUNT = 8;
        public static final int GRID_SIZE = 8;
    }

}
