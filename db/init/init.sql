CREATE
DATABASE IF NOT EXISTS `member-service-db`;
CREATE
DATABASE IF NOT EXISTS `streaming-ad-service-db`;

CREATE
DATABASE IF NOT EXISTS `settlement-service-db`;

/*use `streaming-ad-service-db`;

-- Streaming 테이블에 동영상 데이터 삽입
INSERT INTO streaming (total_length, views, isSettled, acc_play_time, member_id)
VALUES (600, 100, FALSE, 300, 1), -- 10분 길이의 동영상
       (1200, 250, TRUE, 600, 2), -- 20분 길이의 동영상
       (1800, 50, FALSE, 900, 3);
-- 30분 길이의 동영상

-- Advertisement 테이블에 광고 데이터 삽입
INSERT INTO advertisement (ad_play_time, count, ad_revenue)
VALUES (300, 10, 50.00),  -- 5분 시점 광고
       (600, 20, 100.00), -- 10분 시점 광고
       (900, 15, 75.00),  -- 15분 시점 광고
       (300, 5, 25.00),   -- 다른 동영상용 5분 시점 광고
       (600, 10, 50.00),  -- 다른 동영상용 10분 시점 광고
       (1200, 8, 40.00);
-- 20분 시점 광고


-- Streaming과 Advertisement의 매핑 테이블에 데이터 삽입
-- 첫 번째 동영상 (10분 길이)에 5분(300초) 및 10분(600초) 광고를 매핑
INSERT INTO streaming_ad_mapping (streaming_id, ad_id)
VALUES (1, 1), -- 첫 번째 동영상에 첫 번째 광고 (5분 시점)
       (1, 2);
-- 첫 번째 동영상에 두 번째 광고 (10분 시점)

-- 두 번째 동영상 (20분 길이)에 5분(300초), 10분(600초), 15분(900초) 광고 매핑
INSERT INTO streaming_ad_mapping (streaming_id, ad_id)
VALUES (2, 1), -- 두 번째 동영상에 첫 번째 광고 (5분 시점)
       (2, 2), -- 두 번째 동영상에 두 번째 광고 (10분 시점)
       (2, 3);
-- 두 번째 동영상에 세 번째 광고 (15분 시점)

-- 세 번째 동영상 (30분 길이)에 5분(300초), 10분(600초), 20분(1200초) 광고 매핑
INSERT INTO streaming_ad_mapping (streaming_id, ad_id)
VALUES (3, 4), -- 세 번째 동영상에 네 번째 광고 (5분 시점)
       (3, 5), -- 세 번째 동영상에 다섯 번째 광고 (10분 시점)
       (3, 6); -- 세 번째 동영상에 여섯 번째 광고 (20분 시점)*/