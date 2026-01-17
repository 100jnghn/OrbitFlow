use orbitflow;

/* =====================================================
   insert_all_companies_safe.sql  (MySQL 8.x)
   - 3개 회사(OrbitFlow/TechSolution/GlobalService) 통합
   - ID 하드코딩 없음 (전부 name + company 기준 조회)
   - 재실행 가능 (Upsert/Ignore)
   - 각 회사 employee 20명
===================================================== */

START TRANSACTION;

SET @pw := '$2a$10$1CQx3GSceOnsh1Lne0nQzeR4ZH.OcD/WWayDba4BBorqwVcjxBuhK';
-- 12345678

/* =====================================================
   공통 템플릿: 회사 1세트 생성 (회사별로 변수만 다르게)
===================================================== */

/* =========================
   [COMPANY #1] OrbitFlow 본사
========================= */
INSERT INTO company (name, business_number, address, representative_name, representative_contact, created_at,
                     updated_at)
VALUES ('OrbitFlow 본사', '1234567890', '서울특별시 강남구 테헤란로 123', '김도윤', '01011110001', NOW(), NOW())
ON DUPLICATE KEY UPDATE address                = VALUES(address),
                        representative_name    = VALUES(representative_name),
                        representative_contact = VALUES(representative_contact),
                        updated_at             = NOW();

SET @c1 := (SELECT id
            FROM company
            WHERE name = 'OrbitFlow 본사'
            LIMIT 1);

-- ORG_CATEGORY
-- ORG_CATEGORY (is_root 반영)
INSERT INTO org_category (company_id, name, order_index, is_root)
VALUES (@c1, '회사', NULL, TRUE),
       (@c1, '본부', 1, FALSE),
       (@c1, '부서', 2, FALSE),
       (@c1, '팀', 3, FALSE)
ON DUPLICATE KEY UPDATE order_index = VALUES(order_index),
                        is_root     = VALUES(is_root),
                        is_active   = TRUE,
                        updated_at  = NOW();

SET @c1_cat_company := (SELECT id
                        FROM org_category
                        WHERE company_id = @c1
                          AND name = '회사'
                        LIMIT 1);
SET @c1_cat_hq := (SELECT id
                   FROM org_category
                   WHERE company_id = @c1
                     AND name = '본부'
                   LIMIT 1);
SET @c1_cat_dept := (SELECT id
                     FROM org_category
                     WHERE company_id = @c1
                       AND name = '부서'
                     LIMIT 1);
SET @c1_cat_team := (SELECT id
                     FROM org_category
                     WHERE company_id = @c1
                       AND name = '팀'
                     LIMIT 1);

-- ORGANIZATION (루트)
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c1, @c1_cat_company, NULL, 'OrbitFlow', 1, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE category_id = VALUES(category_id),
                        order_index = VALUES(order_index),
                        is_active   = TRUE,
                        updated_at  = NOW();

SET @c1_root := (SELECT id
                 FROM organization
                 WHERE company_id = @c1
                   AND parent_org_id IS NULL
                   AND name = 'OrbitFlow'
                 LIMIT 1);

-- 본부(4)
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c1, @c1_cat_hq, @c1_root, '플랫폼본부', 1, TRUE, NOW(), NOW()),
       (@c1, @c1_cat_hq, @c1_root, '경영지원본부', 2, TRUE, NOW(), NOW()),
       (@c1, @c1_cat_hq, @c1_root, '서비스본부', 3, TRUE, NOW(), NOW()),
       (@c1, @c1_cat_hq, @c1_root, '영업본부', 4, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE category_id = VALUES(category_id),
                        order_index = VALUES(order_index),
                        is_active   = TRUE,
                        updated_at  = NOW();

SET @c1_hq_platform := (SELECT id
                        FROM organization
                        WHERE company_id = @c1
                          AND parent_org_id = @c1_root
                          AND name = '플랫폼본부'
                        LIMIT 1);
SET @c1_hq_support := (SELECT id
                       FROM organization
                       WHERE company_id = @c1
                         AND parent_org_id = @c1_root
                         AND name = '경영지원본부'
                       LIMIT 1);
SET @c1_hq_service := (SELECT id
                       FROM organization
                       WHERE company_id = @c1
                         AND parent_org_id = @c1_root
                         AND name = '서비스본부'
                       LIMIT 1);
SET @c1_hq_sales := (SELECT id
                     FROM organization
                     WHERE company_id = @c1
                       AND parent_org_id = @c1_root
                       AND name = '영업본부'
                     LIMIT 1);

-- 부서(8)
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
SELECT @c1,
       @c1_cat_dept,
       @c1_hq_platform,
       '개발부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_dept,
       @c1_hq_platform,
       '플랫폼운영부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_dept,
       @c1_hq_support,
       '인사부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_dept,
       @c1_hq_support,
       '재무부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_dept,
       @c1_hq_service,
       '서비스기획부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_dept,
       @c1_hq_service,
       'QA부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_dept,
       @c1_hq_sales,
       '영업기획부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_dept,
       @c1_hq_sales,
       '고객관리부',
       2,
       TRUE,
       NOW(),
       NOW()
ON DUPLICATE KEY UPDATE category_id = VALUES(category_id),
                        order_index = VALUES(order_index),
                        is_active   = TRUE,
                        updated_at  = NOW();

SET @c1_dept_dev := (SELECT id
                     FROM organization
                     WHERE company_id = @c1
                       AND parent_org_id = @c1_hq_platform
                       AND name = '개발부'
                     LIMIT 1);
SET @c1_dept_ops := (SELECT id
                     FROM organization
                     WHERE company_id = @c1
                       AND parent_org_id = @c1_hq_platform
                       AND name = '플랫폼운영부'
                     LIMIT 1);
SET @c1_dept_hr := (SELECT id
                    FROM organization
                    WHERE company_id = @c1
                      AND parent_org_id = @c1_hq_support
                      AND name = '인사부'
                    LIMIT 1);
SET @c1_dept_fin := (SELECT id
                     FROM organization
                     WHERE company_id = @c1
                       AND parent_org_id = @c1_hq_support
                       AND name = '재무부'
                     LIMIT 1);
SET @c1_dept_plan := (SELECT id
                      FROM organization
                      WHERE company_id = @c1
                        AND parent_org_id = @c1_hq_service
                        AND name = '서비스기획부'
                      LIMIT 1);
SET @c1_dept_qa := (SELECT id
                    FROM organization
                    WHERE company_id = @c1
                      AND parent_org_id = @c1_hq_service
                      AND name = 'QA부'
                    LIMIT 1);
SET @c1_dept_salesplan := (SELECT id
                           FROM organization
                           WHERE company_id = @c1
                             AND parent_org_id = @c1_hq_sales
                             AND name = '영업기획부'
                           LIMIT 1);
SET @c1_dept_cs := (SELECT id
                    FROM organization
                    WHERE company_id = @c1
                      AND parent_org_id = @c1_hq_sales
                      AND name = '고객관리부'
                    LIMIT 1);

-- 팀(12)
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
SELECT @c1,
       @c1_cat_team,
       @c1_dept_dev,
       '백엔드팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_dev,
       '프론트엔드팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_ops,
       '운영팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_ops,
       '모니터링팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_hr,
       '인사기획팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_hr,
       '채용팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_fin,
       '회계팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_plan,
       '서비스기획팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_qa,
       'QA팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_salesplan,
       '영업전략팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_cs,
       'CS팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c1,
       @c1_cat_team,
       @c1_dept_cs,
       '고객지원팀',
       2,
       TRUE,
       NOW(),
       NOW()
ON DUPLICATE KEY UPDATE category_id = VALUES(category_id),
                        order_index = VALUES(order_index),
                        is_active   = TRUE,
                        updated_at  = NOW();

SET @c1_team_be := (SELECT id
                    FROM organization
                    WHERE company_id = @c1
                      AND parent_org_id = @c1_dept_dev
                      AND name = '백엔드팀'
                    LIMIT 1);
SET @c1_team_fe := (SELECT id
                    FROM organization
                    WHERE company_id = @c1
                      AND parent_org_id = @c1_dept_dev
                      AND name = '프론트엔드팀'
                    LIMIT 1);
SET @c1_team_hrp := (SELECT id
                     FROM organization
                     WHERE company_id = @c1
                       AND parent_org_id = @c1_dept_hr
                       AND name = '인사기획팀'
                     LIMIT 1);
SET @c1_team_plan := (SELECT id
                      FROM organization
                      WHERE company_id = @c1
                        AND parent_org_id = @c1_dept_plan
                        AND name = '서비스기획팀'
                      LIMIT 1);
SET @c1_team_sales := (SELECT id
                       FROM organization
                       WHERE company_id = @c1
                         AND parent_org_id = @c1_dept_salesplan
                         AND name = '영업전략팀'
                       LIMIT 1);
SET @c1_team_cs := (SELECT id
                    FROM organization
                    WHERE company_id = @c1
                      AND parent_org_id = @c1_dept_cs
                      AND name = 'CS팀'
                    LIMIT 1);

-- HR_RANK (Upsert) + parent 체인 업데이트
INSERT INTO hr_rank (company_id, parent_hr_rank_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c1, NULL, '사원', 1, TRUE, NOW(), NOW()),
       (@c1, NULL, '대리', 2, TRUE, NOW(), NOW()),
       (@c1, NULL, '과장', 3, TRUE, NOW(), NOW()),
       (@c1, NULL, '차장', 4, TRUE, NOW(), NOW()),
       (@c1, NULL, '부장', 5, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c1_rank_staff := (SELECT id
                       FROM hr_rank
                       WHERE company_id = @c1
                         AND name = '사원'
                       LIMIT 1);
SET @c1_rank_asst := (SELECT id
                      FROM hr_rank
                      WHERE company_id = @c1
                        AND name = '대리'
                      LIMIT 1);
SET @c1_rank_mgr := (SELECT id
                     FROM hr_rank
                     WHERE company_id = @c1
                       AND name = '과장'
                     LIMIT 1);
SET @c1_rank_sr := (SELECT id
                    FROM hr_rank
                    WHERE company_id = @c1
                      AND name = '차장'
                    LIMIT 1);
SET @c1_rank_head := (SELECT id
                      FROM hr_rank
                      WHERE company_id = @c1
                        AND name = '부장'
                      LIMIT 1);

UPDATE hr_rank
SET parent_hr_rank_id = @c1_rank_asst
WHERE company_id = @c1
  AND name = '사원';
UPDATE hr_rank
SET parent_hr_rank_id = @c1_rank_mgr
WHERE company_id = @c1
  AND name = '대리';
UPDATE hr_rank
SET parent_hr_rank_id = @c1_rank_sr
WHERE company_id = @c1
  AND name = '과장';
UPDATE hr_rank
SET parent_hr_rank_id = @c1_rank_head
WHERE company_id = @c1
  AND name = '차장';
UPDATE hr_rank
SET parent_hr_rank_id = NULL
WHERE company_id = @c1
  AND name = '부장';

-- POSITION_CATEGORY (Upsert)
INSERT INTO position_category (company_id, org_category_id, name, order_index, is_head, is_active, created_at,
                               updated_at)
VALUES (@c1, @c1_cat_company, '사장', 1, TRUE, TRUE, NOW(), NOW()),
       (@c1, @c1_cat_hq, '본부장', 2, TRUE, TRUE, NOW(), NOW()),
       (@c1, @c1_cat_dept, '부장', 3, TRUE, TRUE, NOW(), NOW()),
       (@c1, @c1_cat_team, '팀장', 4, TRUE, TRUE, NOW(), NOW()),
       (@c1, @c1_cat_team, '팀원', 5, FALSE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE order_index=VALUES(order_index),
                        is_head=VALUES(is_head),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c1_pos_ceo := (SELECT id
                    FROM position_category
                    WHERE company_id = @c1
                      AND org_category_id = @c1_cat_company
                      AND name = '사장'
                    LIMIT 1);
SET @c1_pos_hq := (SELECT id
                   FROM position_category
                   WHERE company_id = @c1
                     AND org_category_id = @c1_cat_hq
                     AND name = '본부장'
                   LIMIT 1);
SET @c1_pos_dept := (SELECT id
                     FROM position_category
                     WHERE company_id = @c1
                       AND org_category_id = @c1_cat_dept
                       AND name = '부장'
                     LIMIT 1);
SET @c1_pos_tl := (SELECT id
                   FROM position_category
                   WHERE company_id = @c1
                     AND org_category_id = @c1_cat_team
                     AND name = '팀장'
                   LIMIT 1);
SET @c1_pos_tm := (SELECT id
                   FROM position_category
                   WHERE company_id = @c1
                     AND org_category_id = @c1_cat_team
                     AND name = '팀원'
                   LIMIT 1);

-- 팀원의 상위 → 팀장
UPDATE position_category
SET parent_position_id = @c1_pos_tl
WHERE id = @c1_pos_tm;

-- 팀장의 상위 → 부장
UPDATE position_category
SET parent_position_id = @c1_pos_dept
WHERE id = @c1_pos_tl;

-- 부장의 상위 → 본부장
UPDATE position_category
SET parent_position_id = @c1_pos_hq
WHERE id = @c1_pos_dept;

-- 본부장의 상위 → 사장
UPDATE position_category
SET parent_position_id = @c1_pos_ceo
WHERE id = @c1_pos_hq;

-- 사장은 최상위
UPDATE position_category
SET parent_position_id = NULL
WHERE id = @c1_pos_ceo;


-- ORG_POSITION_USAGE (Ignore로 재실행 안전)
INSERT IGNORE INTO org_position_usage (company_id, org_id, position_category_id, created_at, updated_at)
SELECT @c1,
       o.id,
       CASE o.category_id
           WHEN @c1_cat_company THEN @c1_pos_ceo
           WHEN @c1_cat_hq THEN @c1_pos_hq
           WHEN @c1_cat_dept THEN @c1_pos_dept
           WHEN @c1_cat_team THEN @c1_pos_tl
           END,
       NOW(),
       NOW()
FROM organization o
WHERE o.company_id = @c1
  AND o.category_id IN (@c1_cat_company, @c1_cat_hq, @c1_cat_dept, @c1_cat_team);

INSERT IGNORE INTO org_position_usage (company_id, org_id, position_category_id, created_at, updated_at)
SELECT @c1, o.id, @c1_pos_tm, NOW(), NOW()
FROM organization o
WHERE o.company_id = @c1
  AND o.category_id = @c1_cat_team;

-- EMPLOYEE 20명 (Upsert)
-- EMPLOYEE 20명 (Upsert) - 이메일 testN@test.com
INSERT INTO employee
(company_id, employee_no, internal_phone, phone,
 org_id, hr_rank_id, position_category_id,
 name, email, password,
 role, gender, birth_date, employment_type, status, work_status, hire_date,
 created_at, updated_at)
VALUES
-- 1) CEO
(@c1, 'OF-001', '1001', '01011110001', @c1_root, @c1_rank_head, @c1_pos_ceo,
 '김도윤', 'test1@test.com', @pw,
 'COMPANY_ADMIN', 'MALE', '1975-01-01', 'REGULAR', 'ACTIVE', 'WORKING', '2008-01-01', NOW(), NOW()),

-- 2~5) HQ Heads
(@c1, 'OF-010', '1010', '01011111010', @c1_hq_platform, @c1_rank_head, @c1_pos_hq,
 '박준형', 'test2@test.com', @pw,
 'ADMIN', 'MALE', '1978-03-03', 'REGULAR', 'ACTIVE', 'WORKING', '2010-03-01', NOW(), NOW()),

(@c1, 'OF-011', '1011', '01011111011', @c1_hq_support, @c1_rank_head, @c1_pos_hq,
 '이수연', 'test3@test.com', @pw,
 'ADMIN', 'FEMALE', '1979-05-05', 'REGULAR', 'ACTIVE', 'WORKING', '2011-05-01', NOW(), NOW()),

(@c1, 'OF-012', '1012', '01011111012', @c1_hq_service, @c1_rank_head, @c1_pos_hq,
 '정민호', 'test4@test.com', @pw,
 'ADMIN', 'MALE', '1980-07-07', 'REGULAR', 'ACTIVE', 'WORKING', '2012-07-01', NOW(), NOW()),

(@c1, 'OF-013', '1013', '01011111013', @c1_hq_sales, @c1_rank_head, @c1_pos_hq,
 '한지은', 'test5@test.com', @pw,
 'ADMIN', 'FEMALE', '1981-09-09', 'REGULAR', 'ACTIVE', 'WORKING', '2013-09-01', NOW(), NOW()),

-- 6~9) Dept Heads
(@c1, 'OF-020', '1020', '01011112020', @c1_dept_dev, @c1_rank_head, @c1_pos_dept,
 '김태훈', 'test6@test.com', @pw,
 'ADMIN', 'MALE', '1982-02-02', 'REGULAR', 'ACTIVE', 'WORKING', '2014-02-01', NOW(), NOW()),

(@c1, 'OF-021', '1021', '01011112021', @c1_dept_hr, @c1_rank_head, @c1_pos_dept,
 '서지현', 'test7@test.com', @pw,
 'ADMIN', 'FEMALE', '1983-03-03', 'REGULAR', 'ACTIVE', 'WORKING', '2014-03-01', NOW(), NOW()),

(@c1, 'OF-022', '1022', '01011112022', @c1_dept_plan, @c1_rank_head, @c1_pos_dept,
 '윤상민', 'test8@test.com', @pw,
 'ADMIN', 'MALE', '1984-04-04', 'REGULAR', 'ACTIVE', 'WORKING', '2015-04-01', NOW(), NOW()),

(@c1, 'OF-023', '1023', '01011112023', @c1_dept_salesplan, @c1_rank_head, @c1_pos_dept,
 '정다은', 'test9@test.com', @pw,
 'ADMIN', 'FEMALE', '1985-05-05', 'REGULAR', 'ACTIVE', 'WORKING', '2015-05-01', NOW(), NOW()),

-- 10~13) Team Leads
(@c1, 'OF-030', '1030', '01011113030', @c1_team_be, @c1_rank_sr, @c1_pos_tl,
 '이준호', 'test10@test.com', @pw,
 'EMPLOYEE', 'MALE', '1988-06-06', 'REGULAR', 'ACTIVE', 'WORKING', '2016-06-01', NOW(), NOW()),

(@c1, 'OF-031', '1031', '01011113031', @c1_team_fe, @c1_rank_sr, @c1_pos_tl,
 '김나연', 'test11@test.com', @pw,
 'EMPLOYEE', 'FEMALE', '1989-07-07', 'REGULAR', 'ACTIVE', 'WORKING', '2016-07-01', NOW(), NOW()),

(@c1, 'OF-032', '1032', '01011113032', @c1_team_hrp, @c1_rank_sr, @c1_pos_tl,
 '최성훈', 'test12@test.com', @pw,
 'EMPLOYEE', 'MALE', '1987-08-08', 'REGULAR', 'ACTIVE', 'WORKING', '2016-08-01', NOW(), NOW()),

(@c1, 'OF-033', '1033', '01011113033', @c1_team_sales, @c1_rank_sr, @c1_pos_tl,
 '문지은', 'test13@test.com', @pw,
 'EMPLOYEE', 'FEMALE', '1988-09-09', 'REGULAR', 'ACTIVE', 'WORKING', '2016-09-01', NOW(), NOW()),

-- 14~20) Team Members
(@c1, 'OF-040', '1040', '01011114040', @c1_team_be, @c1_rank_asst, @c1_pos_tm,
 '박성민', 'test14@test.com', @pw,
 'EMPLOYEE', 'MALE', '1995-01-01', 'REGULAR', 'ACTIVE', 'WORKING', '2020-01-01', NOW(), NOW()),

(@c1, 'OF-041', '1041', '01011114041', @c1_team_be, @c1_rank_staff, @c1_pos_tm,
 '한지민', 'test15@test.com', @pw,
 'EMPLOYEE', 'FEMALE', '1997-02-02', 'REGULAR', 'ACTIVE', 'WORKING', '2021-02-01', NOW(), NOW()),

(@c1, 'OF-042', '1042', '01011114042', @c1_team_fe, @c1_rank_asst, @c1_pos_tm,
 '오세훈', 'test16@test.com', @pw,
 'EMPLOYEE', 'MALE', '1996-03-03', 'REGULAR', 'ACTIVE', 'WORKING', '2020-03-01', NOW(), NOW()),

(@c1, 'OF-043', '1043', '01011114043', @c1_team_fe, @c1_rank_staff, @c1_pos_tm,
 '정유진', 'test17@test.com', @pw,
 'EMPLOYEE', 'FEMALE', '1998-04-04', 'REGULAR', 'ACTIVE', 'WORKING', '2022-04-01', NOW(), NOW()),

(@c1, 'OF-044', '1044', '01011114044', @c1_team_hrp, @c1_rank_asst, @c1_pos_tm,
 '김현우', 'test18@test.com', @pw,
 'EMPLOYEE', 'MALE', '1994-05-05', 'REGULAR', 'ACTIVE', 'WORKING', '2019-05-01', NOW(), NOW()),

(@c1, 'OF-045', '1045', '01011114045', @c1_team_plan, @c1_rank_asst, @c1_pos_tm,
 '서예린', 'test19@test.com', @pw,
 'EMPLOYEE', 'FEMALE', '1995-06-06', 'REGULAR', 'ACTIVE', 'WORKING', '2020-06-01', NOW(), NOW()),

(@c1, 'OF-046', '1046', '01011114046', @c1_team_cs, @c1_rank_staff, @c1_pos_tm,
 '임동혁', 'test20@test.com', @pw,
 'EMPLOYEE', 'MALE', '1999-07-07', 'REGULAR', 'ACTIVE', 'WORKING', '2023-07-01', NOW(), NOW())
ON DUPLICATE KEY UPDATE internal_phone=VALUES(internal_phone),
                        phone=VALUES(phone),
                        org_id=VALUES(org_id),
                        hr_rank_id=VALUES(hr_rank_id),
                        position_category_id=VALUES(position_category_id),
                        name=VALUES(name),
                        password=VALUES(password),
                        role=VALUES(role),
                        gender=VALUES(gender),
                        birth_date=VALUES(birth_date),
                        employment_type=VALUES(employment_type),
                        status=VALUES(status),
                        work_status=VALUES(work_status),
                        hire_date=VALUES(hire_date),
                        updated_at=NOW();

/* =====================================================
   정다은 → 재무부 부장으로 조직 변경
===================================================== */

UPDATE employee
SET org_id               = @c1_dept_fin,
    hr_rank_id           = @c1_rank_head,
    position_category_id = @c1_pos_dept,
    updated_at           = NOW()
WHERE company_id = @c1
  AND name = '정다은';


/* =========================
   [COMPANY #2] TechSolution 테크솔루션
========================= */
INSERT INTO company (name, business_number, address, representative_name, representative_contact, created_at,
                     updated_at)
VALUES ('TechSolution 테크솔루션', '2345678901', '경기도 판교역로 456', '강태양', '01022220001', NOW(), NOW())
ON DUPLICATE KEY UPDATE address                = VALUES(address),
                        representative_name    = VALUES(representative_name),
                        representative_contact = VALUES(representative_contact),
                        updated_at             = NOW();

SET @c2 := (SELECT id
            FROM company
            WHERE name = 'TechSolution 테크솔루션'
            LIMIT 1);

-- ORG_CATEGORY
INSERT INTO org_category (company_id, name, order_index, is_root)
VALUES (@c2, '회사', NULL, TRUE),
       (@c2, '본부', 1, FALSE),
       (@c2, '부서', 2, FALSE),
       (@c2, '팀', 3, FALSE)
ON DUPLICATE KEY UPDATE order_index = VALUES(order_index),
                        is_root     = VALUES(is_root),
                        is_active   = TRUE,
                        updated_at  = NOW();

SET @c2_cat_company := (SELECT id
                        FROM org_category
                        WHERE company_id = @c2
                          AND name = '회사'
                        LIMIT 1);
SET @c2_cat_hq := (SELECT id
                   FROM org_category
                   WHERE company_id = @c2
                     AND name = '본부'
                   LIMIT 1);
SET @c2_cat_dept := (SELECT id
                     FROM org_category
                     WHERE company_id = @c2
                       AND name = '부서'
                     LIMIT 1);
SET @c2_cat_team := (SELECT id
                     FROM org_category
                     WHERE company_id = @c2
                       AND name = '팀'
                     LIMIT 1);

-- 루트
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c2, @c2_cat_company, NULL, 'TechSolution', 1, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id),
                        order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c2_root := (SELECT id
                 FROM organization
                 WHERE company_id = @c2
                   AND parent_org_id IS NULL
                   AND name = 'TechSolution'
                 LIMIT 1);

-- 본부(4)
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c2, @c2_cat_hq, @c2_root, '플랫폼본부', 1, TRUE, NOW(), NOW()),
       (@c2, @c2_cat_hq, @c2_root, '경영지원본부', 2, TRUE, NOW(), NOW()),
       (@c2, @c2_cat_hq, @c2_root, '서비스본부', 3, TRUE, NOW(), NOW()),
       (@c2, @c2_cat_hq, @c2_root, '영업본부', 4, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id),
                        order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c2_hq_platform := (SELECT id
                        FROM organization
                        WHERE company_id = @c2
                          AND parent_org_id = @c2_root
                          AND name = '플랫폼본부'
                        LIMIT 1);
SET @c2_hq_support := (SELECT id
                       FROM organization
                       WHERE company_id = @c2
                         AND parent_org_id = @c2_root
                         AND name = '경영지원본부'
                       LIMIT 1);
SET @c2_hq_service := (SELECT id
                       FROM organization
                       WHERE company_id = @c2
                         AND parent_org_id = @c2_root
                         AND name = '서비스본부'
                       LIMIT 1);
SET @c2_hq_sales := (SELECT id
                     FROM organization
                     WHERE company_id = @c2
                       AND parent_org_id = @c2_root
                       AND name = '영업본부'
                     LIMIT 1);

-- 부서/팀 동일 구조
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
SELECT @c2,
       @c2_cat_dept,
       @c2_hq_platform,
       '개발부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_dept,
       @c2_hq_platform,
       '플랫폼운영부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_dept,
       @c2_hq_support,
       '인사부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_dept,
       @c2_hq_support,
       '재무부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_dept,
       @c2_hq_service,
       '서비스기획부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_dept,
       @c2_hq_service,
       'QA부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_dept,
       @c2_hq_sales,
       '영업기획부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_dept,
       @c2_hq_sales,
       '고객관리부',
       2,
       TRUE,
       NOW(),
       NOW()
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id),
                        order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c2_dept_dev := (SELECT id
                     FROM organization
                     WHERE company_id = @c2
                       AND parent_org_id = @c2_hq_platform
                       AND name = '개발부'
                     LIMIT 1);
SET @c2_dept_hr := (SELECT id
                    FROM organization
                    WHERE company_id = @c2
                      AND parent_org_id = @c2_hq_support
                      AND name = '인사부'
                    LIMIT 1);
SET @c2_dept_plan := (SELECT id
                      FROM organization
                      WHERE company_id = @c2
                        AND parent_org_id = @c2_hq_service
                        AND name = '서비스기획부'
                      LIMIT 1);
SET @c2_dept_salesplan := (SELECT id
                           FROM organization
                           WHERE company_id = @c2
                             AND parent_org_id = @c2_hq_sales
                             AND name = '영업기획부'
                           LIMIT 1);
SET @c2_dept_cs := (SELECT id
                    FROM organization
                    WHERE company_id = @c2
                      AND parent_org_id = @c2_hq_sales
                      AND name = '고객관리부'
                    LIMIT 1);

INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
SELECT @c2,
       @c2_cat_team,
       @c2_dept_dev,
       '백엔드팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       @c2_dept_dev,
       '프론트엔드팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       (SELECT id
        FROM organization
        WHERE company_id = @c2
          AND parent_org_id = @c2_hq_platform
          AND name = '플랫폼운영부'
        LIMIT 1),
       '운영팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       (SELECT id
        FROM organization
        WHERE company_id = @c2
          AND parent_org_id = @c2_hq_platform
          AND name = '플랫폼운영부'
        LIMIT 1),
       '모니터링팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       @c2_dept_hr,
       '인사기획팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       @c2_dept_hr,
       '채용팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       (SELECT id
        FROM organization
        WHERE company_id = @c2
          AND parent_org_id = (SELECT id
                               FROM organization
                               WHERE company_id = @c2
                                 AND parent_org_id = @c2_hq_support
                                 AND name = '재무부'
                               LIMIT 1)
          AND name = '재무부'
        LIMIT 1),
       '회계팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       @c2_dept_plan,
       '서비스기획팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       (SELECT id FROM organization WHERE company_id = @c2 AND parent_org_id = @c2_hq_service AND name = 'QA부' LIMIT 1),
       'QA팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       @c2_dept_salesplan,
       '영업전략팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       @c2_dept_cs,
       'CS팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c2,
       @c2_cat_team,
       @c2_dept_cs,
       '고객지원팀',
       2,
       TRUE,
       NOW(),
       NOW()
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id),
                        order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c2_team_be := (SELECT id
                    FROM organization
                    WHERE company_id = @c2
                      AND name = '백엔드팀'
                    LIMIT 1);
SET @c2_team_fe := (SELECT id
                    FROM organization
                    WHERE company_id = @c2
                      AND name = '프론트엔드팀'
                    LIMIT 1);
SET @c2_team_hrp := (SELECT id
                     FROM organization
                     WHERE company_id = @c2
                       AND name = '인사기획팀'
                     LIMIT 1);
SET @c2_team_plan := (SELECT id
                      FROM organization
                      WHERE company_id = @c2
                        AND name = '서비스기획팀'
                      LIMIT 1);
SET @c2_team_sales := (SELECT id
                       FROM organization
                       WHERE company_id = @c2
                         AND name = '영업전략팀'
                       LIMIT 1);
SET @c2_team_cs := (SELECT id
                    FROM organization
                    WHERE company_id = @c2
                      AND name = 'CS팀'
                    LIMIT 1);

-- HR_RANK / POSITION_CATEGORY / USAGE
INSERT INTO hr_rank (company_id, parent_hr_rank_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c2, NULL, '사원', 1, TRUE, NOW(), NOW()),
       (@c2, NULL, '대리', 2, TRUE, NOW(), NOW()),
       (@c2, NULL, '과장', 3, TRUE, NOW(), NOW()),
       (@c2, NULL, '차장', 4, TRUE, NOW(), NOW()),
       (@c2, NULL, '부장', 5, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c2_rank_staff := (SELECT id
                       FROM hr_rank
                       WHERE company_id = @c2
                         AND name = '사원'
                       LIMIT 1);
SET @c2_rank_asst := (SELECT id
                      FROM hr_rank
                      WHERE company_id = @c2
                        AND name = '대리'
                      LIMIT 1);
SET @c2_rank_mgr := (SELECT id
                     FROM hr_rank
                     WHERE company_id = @c2
                       AND name = '과장'
                     LIMIT 1);
SET @c2_rank_sr := (SELECT id
                    FROM hr_rank
                    WHERE company_id = @c2
                      AND name = '차장'
                    LIMIT 1);
SET @c2_rank_head := (SELECT id
                      FROM hr_rank
                      WHERE company_id = @c2
                        AND name = '부장'
                      LIMIT 1);

UPDATE hr_rank
SET parent_hr_rank_id = @c2_rank_asst
WHERE company_id = @c2
  AND name = '사원';
UPDATE hr_rank
SET parent_hr_rank_id = @c2_rank_mgr
WHERE company_id = @c2
  AND name = '대리';
UPDATE hr_rank
SET parent_hr_rank_id = @c2_rank_sr
WHERE company_id = @c2
  AND name = '과장';
UPDATE hr_rank
SET parent_hr_rank_id = @c2_rank_head
WHERE company_id = @c2
  AND name = '차장';
UPDATE hr_rank
SET parent_hr_rank_id = NULL
WHERE company_id = @c2
  AND name = '부장';

INSERT INTO position_category (company_id, org_category_id, name, order_index, is_head, is_active, created_at,
                               updated_at)
VALUES (@c2, @c2_cat_company, '사장', 1, TRUE, TRUE, NOW(), NOW()),
       (@c2, @c2_cat_hq, '본부장', 2, TRUE, TRUE, NOW(), NOW()),
       (@c2, @c2_cat_dept, '부장', 3, TRUE, TRUE, NOW(), NOW()),
       (@c2, @c2_cat_team, '팀장', 4, TRUE, TRUE, NOW(), NOW()),
       (@c2, @c2_cat_team, '팀원', 5, FALSE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c2_pos_ceo := (SELECT id
                    FROM position_category
                    WHERE company_id = @c2
                      AND org_category_id = @c2_cat_company
                      AND name = '사장'
                    LIMIT 1);
SET @c2_pos_hq := (SELECT id
                   FROM position_category
                   WHERE company_id = @c2
                     AND org_category_id = @c2_cat_hq
                     AND name = '본부장'
                   LIMIT 1);
SET @c2_pos_dept := (SELECT id
                     FROM position_category
                     WHERE company_id = @c2
                       AND org_category_id = @c2_cat_dept
                       AND name = '부장'
                     LIMIT 1);
SET @c2_pos_tl := (SELECT id
                   FROM position_category
                   WHERE company_id = @c2
                     AND org_category_id = @c2_cat_team
                     AND name = '팀장'
                   LIMIT 1);
SET @c2_pos_tm := (SELECT id
                   FROM position_category
                   WHERE company_id = @c2
                     AND org_category_id = @c2_cat_team
                     AND name = '팀원'
                   LIMIT 1);


-- 팀원의 상위 → 팀장
UPDATE position_category
SET parent_position_id = @c2_pos_tl
WHERE id = @c2_pos_tm;

-- 팀장의 상위 → 부장
UPDATE position_category
SET parent_position_id = @c2_pos_dept
WHERE id = @c2_pos_tl;

-- 부장의 상위 → 본부장
UPDATE position_category
SET parent_position_id = @c2_pos_hq
WHERE id = @c2_pos_dept;

-- 본부장의 상위 → 사장
UPDATE position_category
SET parent_position_id = @c2_pos_ceo
WHERE id = @c2_pos_hq;

-- 사장은 최상위
UPDATE position_category
SET parent_position_id = NULL
WHERE id = @c2_pos_ceo;


INSERT IGNORE INTO org_position_usage (company_id, org_id, position_category_id, created_at, updated_at)
SELECT @c2,
       o.id,
       CASE o.category_id
           WHEN @c2_cat_company THEN @c2_pos_ceo
           WHEN @c2_cat_hq THEN @c2_pos_hq
           WHEN @c2_cat_dept THEN @c2_pos_dept
           WHEN @c2_cat_team THEN @c2_pos_tl
           END,
       NOW(),
       NOW()
FROM organization o
WHERE o.company_id = @c2
  AND o.category_id IN (@c2_cat_company, @c2_cat_hq, @c2_cat_dept, @c2_cat_team);

INSERT IGNORE INTO org_position_usage (company_id, org_id, position_category_id, created_at, updated_at)
SELECT @c2, o.id, @c2_pos_tm, NOW(), NOW()
FROM organization o
WHERE o.company_id = @c2
  AND o.category_id = @c2_cat_team;

-- EMPLOYEE 20명 (TechSolution)
INSERT INTO employee
(company_id, employee_no, internal_phone, phone,
 org_id, hr_rank_id, position_category_id,
 name, email, password,
 role, gender, birth_date, employment_type, status, work_status, hire_date,
 created_at, updated_at)
VALUES (@c2, 'TS-001', '2001', '01022220001', @c2_root, @c2_rank_head, @c2_pos_ceo, '강태양', 'ceo@techsolution.com', @pw,
        'COMPANY_ADMIN', 'MALE', '1976-02-02', 'REGULAR', 'ACTIVE', 'WORKING', '2009-02-01', NOW(), NOW()),
       (@c2, 'TS-010', '2010', '01022221010',
        (SELECT id FROM organization WHERE company_id = @c2 AND name = '플랫폼본부' LIMIT 1), @c2_rank_head, @c2_pos_hq,
        '최민석', 'hq_platform@techsolution.com', @pw, 'ADMIN', 'MALE', '1979-03-03', 'REGULAR', 'ACTIVE', 'WORKING',
        '2011-03-01', NOW(), NOW()),
       (@c2, 'TS-011', '2011', '01022221011',
        (SELECT id FROM organization WHERE company_id = @c2 AND name = '경영지원본부' LIMIT 1), @c2_rank_head, @c2_pos_hq,
        '장서연', 'hq_support@techsolution.com', @pw, 'ADMIN', 'FEMALE', '1980-04-04', 'REGULAR', 'ACTIVE', 'WORKING',
        '2012-04-01', NOW(), NOW()),
       (@c2, 'TS-012', '2012', '01022221012',
        (SELECT id FROM organization WHERE company_id = @c2 AND name = '서비스본부' LIMIT 1), @c2_rank_head, @c2_pos_hq,
        '박정우', 'hq_service@techsolution.com', @pw, 'ADMIN', 'MALE', '1981-05-05', 'REGULAR', 'ACTIVE', 'WORKING',
        '2013-05-01', NOW(), NOW()),
       (@c2, 'TS-013', '2013', '01022221013',
        (SELECT id FROM organization WHERE company_id = @c2 AND name = '영업본부' LIMIT 1), @c2_rank_head, @c2_pos_hq,
        '이하린', 'hq_sales@techsolution.com', @pw, 'ADMIN', 'FEMALE', '1982-06-06', 'REGULAR', 'ACTIVE', 'WORKING',
        '2014-06-01', NOW(), NOW()),
       (@c2, 'TS-020', '2020', '01022222020',
        (SELECT id FROM organization WHERE company_id = @c2 AND name = '개발부' LIMIT 1), @c2_rank_head, @c2_pos_dept,
        '한도윤', 'dev_head@techsolution.com', @pw, 'ADMIN', 'MALE', '1983-07-07', 'REGULAR', 'ACTIVE', 'WORKING',
        '2015-07-01', NOW(), NOW()),
       (@c2, 'TS-021', '2021', '01022222021',
        (SELECT id FROM organization WHERE company_id = @c2 AND name = '인사부' LIMIT 1), @c2_rank_head, @c2_pos_dept,
        '김세은', 'hr_head@techsolution.com', @pw, 'ADMIN', 'FEMALE', '1984-08-08', 'REGULAR', 'ACTIVE', 'WORKING',
        '2015-08-01', NOW(), NOW()),
       (@c2, 'TS-022', '2022', '01022222022',
        (SELECT id FROM organization WHERE company_id = @c2 AND name = '서비스기획부' LIMIT 1), @c2_rank_head, @c2_pos_dept,
        '윤지훈', 'svc_head@techsolution.com', @pw, 'ADMIN', 'MALE', '1985-09-09', 'REGULAR', 'ACTIVE', 'WORKING',
        '2016-09-01', NOW(), NOW()),
       (@c2, 'TS-023', '2023', '01022222023',
        (SELECT id FROM organization WHERE company_id = @c2 AND name = '영업기획부' LIMIT 1), @c2_rank_head, @c2_pos_dept,
        '정다현', 'sales_head@techsolution.com', @pw, 'ADMIN', 'FEMALE', '1986-10-10', 'REGULAR', 'ACTIVE', 'WORKING',
        '2016-10-01', NOW(), NOW()),
       (@c2, 'TS-030', '2030', '01022223030', @c2_team_be, @c2_rank_sr, @c2_pos_tl, '배준호', 'be_lead@techsolution.com',
        @pw, 'EMPLOYEE', 'MALE', '1989-01-01', 'REGULAR', 'ACTIVE', 'WORKING', '2017-01-01', NOW(), NOW()),
       (@c2, 'TS-031', '2031', '01022223031', @c2_team_fe, @c2_rank_sr, @c2_pos_tl, '서가은', 'fe_lead@techsolution.com',
        @pw, 'EMPLOYEE', 'FEMALE', '1990-02-02', 'REGULAR', 'ACTIVE', 'WORKING', '2017-02-01', NOW(), NOW()),
       (@c2, 'TS-032', '2032', '01022223032', @c2_team_hrp, @c2_rank_sr, @c2_pos_tl, '문성훈', 'hr_lead@techsolution.com',
        @pw, 'EMPLOYEE', 'MALE', '1988-03-03', 'REGULAR', 'ACTIVE', 'WORKING', '2017-03-01', NOW(), NOW()),
       (@c2, 'TS-033', '2033', '01022223033', @c2_team_sales, @c2_rank_sr, @c2_pos_tl, '오지은',
        'sales_lead@techsolution.com', @pw, 'EMPLOYEE', 'FEMALE', '1989-04-04', 'REGULAR', 'ACTIVE', 'WORKING',
        '2017-04-01', NOW(), NOW()),
       (@c2, 'TS-040', '2040', '01022224040', @c2_team_be, @c2_rank_asst, @c2_pos_tm, '신현수', 'be1@techsolution.com',
        @pw, 'EMPLOYEE', 'MALE', '1995-05-05', 'REGULAR', 'ACTIVE', 'WORKING', '2020-05-01', NOW(), NOW()),
       (@c2, 'TS-041', '2041', '01022224041', @c2_team_be, @c2_rank_staff, @c2_pos_tm, '조유리', 'be2@techsolution.com',
        @pw, 'EMPLOYEE', 'FEMALE', '1997-06-06', 'REGULAR', 'ACTIVE', 'WORKING', '2021-06-01', NOW(), NOW()),
       (@c2, 'TS-042', '2042', '01022224042', @c2_team_fe, @c2_rank_asst, @c2_pos_tm, '임도현', 'fe1@techsolution.com',
        @pw, 'EMPLOYEE', 'MALE', '1996-07-07', 'REGULAR', 'ACTIVE', 'WORKING', '2020-07-01', NOW(), NOW()),
       (@c2, 'TS-043', '2043', '01022224043', @c2_team_fe, @c2_rank_staff, @c2_pos_tm, '강수빈', 'fe2@techsolution.com',
        @pw, 'EMPLOYEE', 'FEMALE', '1998-08-08', 'REGULAR', 'ACTIVE', 'WORKING', '2022-08-01', NOW(), NOW()),
       (@c2, 'TS-044', '2044', '01022224044', @c2_team_hrp, @c2_rank_asst, @c2_pos_tm, '김동우', 'hr1@techsolution.com',
        @pw, 'EMPLOYEE', 'MALE', '1994-09-09', 'REGULAR', 'ACTIVE', 'WORKING', '2019-09-01', NOW(), NOW()),
       (@c2, 'TS-045', '2045', '01022224045', @c2_team_plan, @c2_rank_asst, @c2_pos_tm, '장예린', 'svc1@techsolution.com',
        @pw, 'EMPLOYEE', 'FEMALE', '1995-10-10', 'REGULAR', 'ACTIVE', 'WORKING', '2020-10-01', NOW(), NOW()),
       (@c2, 'TS-046', '2046', '01022224046', @c2_team_cs, @c2_rank_staff, @c2_pos_tm, '박동혁', 'cs1@techsolution.com',
        @pw, 'EMPLOYEE', 'MALE', '1999-11-11', 'REGULAR', 'ACTIVE', 'WORKING', '2023-11-01', NOW(), NOW())
ON DUPLICATE KEY UPDATE internal_phone=VALUES(internal_phone),
                        phone=VALUES(phone),
                        org_id=VALUES(org_id),
                        hr_rank_id=VALUES(hr_rank_id),
                        position_category_id=VALUES(position_category_id),
                        name=VALUES(name),
                        password=VALUES(password),
                        role=VALUES(role),
                        gender=VALUES(gender),
                        birth_date=VALUES(birth_date),
                        employment_type=VALUES(employment_type),
                        status=VALUES(status),
                        work_status=VALUES(work_status),
                        hire_date=VALUES(hire_date),
                        updated_at=NOW();

/* =========================
   [COMPANY #3] GlobalService 글로벌서비스
========================= */
INSERT INTO company (name, business_number, address, representative_name, representative_contact, created_at,
                     updated_at)
VALUES ('GlobalService 글로벌서비스', '3456789012', '부산광역시 해운대구 센텀로 789', '신유진', '01033330001', NOW(), NOW())
ON DUPLICATE KEY UPDATE address                = VALUES(address),
                        representative_name    = VALUES(representative_name),
                        representative_contact = VALUES(representative_contact),
                        updated_at             = NOW();

SET @c3 := (SELECT id
            FROM company
            WHERE name = 'GlobalService 글로벌서비스'
            LIMIT 1);

-- ORG_CATEGORY
INSERT INTO org_category (company_id, name, order_index, is_root)
VALUES (@c3, '회사', NULL, TRUE),
       (@c3, '본부', 1, FALSE),
       (@c3, '부서', 2, FALSE),
       (@c3, '팀', 3, FALSE)
ON DUPLICATE KEY UPDATE order_index = VALUES(order_index),
                        is_root     = VALUES(is_root),
                        is_active   = TRUE,
                        updated_at  = NOW();

SET @c3_cat_company := (SELECT id
                        FROM org_category
                        WHERE company_id = @c3
                          AND name = '회사'
                        LIMIT 1);
SET @c3_cat_hq := (SELECT id
                   FROM org_category
                   WHERE company_id = @c3
                     AND name = '본부'
                   LIMIT 1);
SET @c3_cat_dept := (SELECT id
                     FROM org_category
                     WHERE company_id = @c3
                       AND name = '부서'
                     LIMIT 1);
SET @c3_cat_team := (SELECT id
                     FROM org_category
                     WHERE company_id = @c3
                       AND name = '팀'
                     LIMIT 1);

-- 루트
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c3, @c3_cat_company, NULL, 'GlobalService', 1, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id),
                        order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c3_root := (SELECT id
                 FROM organization
                 WHERE company_id = @c3
                   AND parent_org_id IS NULL
                   AND name = 'GlobalService'
                 LIMIT 1);

-- 본부/부서/팀 동일 구조
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c3, @c3_cat_hq, @c3_root, '플랫폼본부', 1, TRUE, NOW(), NOW()),
       (@c3, @c3_cat_hq, @c3_root, '경영지원본부', 2, TRUE, NOW(), NOW()),
       (@c3, @c3_cat_hq, @c3_root, '서비스본부', 3, TRUE, NOW(), NOW()),
       (@c3, @c3_cat_hq, @c3_root, '영업본부', 4, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id),
                        order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c3_hq_platform := (SELECT id
                        FROM organization
                        WHERE company_id = @c3
                          AND parent_org_id = @c3_root
                          AND name = '플랫폼본부'
                        LIMIT 1);
SET @c3_hq_support := (SELECT id
                       FROM organization
                       WHERE company_id = @c3
                         AND parent_org_id = @c3_root
                         AND name = '경영지원본부'
                       LIMIT 1);
SET @c3_hq_service := (SELECT id
                       FROM organization
                       WHERE company_id = @c3
                         AND parent_org_id = @c3_root
                         AND name = '서비스본부'
                       LIMIT 1);
SET @c3_hq_sales := (SELECT id
                     FROM organization
                     WHERE company_id = @c3
                       AND parent_org_id = @c3_root
                       AND name = '영업본부'
                     LIMIT 1);

INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
SELECT @c3,
       @c3_cat_dept,
       @c3_hq_platform,
       '개발부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_dept,
       @c3_hq_platform,
       '플랫폼운영부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_dept,
       @c3_hq_support,
       '인사부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_dept,
       @c3_hq_support,
       '재무부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_dept,
       @c3_hq_service,
       '서비스기획부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_dept,
       @c3_hq_service,
       'QA부',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_dept,
       @c3_hq_sales,
       '영업기획부',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_dept,
       @c3_hq_sales,
       '고객관리부',
       2,
       TRUE,
       NOW(),
       NOW()
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id),
                        order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c3_dept_dev := (SELECT id
                     FROM organization
                     WHERE company_id = @c3
                       AND parent_org_id = @c3_hq_platform
                       AND name = '개발부'
                     LIMIT 1);
SET @c3_dept_hr := (SELECT id
                    FROM organization
                    WHERE company_id = @c3
                      AND parent_org_id = @c3_hq_support
                      AND name = '인사부'
                    LIMIT 1);
SET @c3_dept_plan := (SELECT id
                      FROM organization
                      WHERE company_id = @c3
                        AND parent_org_id = @c3_hq_service
                        AND name = '서비스기획부'
                      LIMIT 1);
SET @c3_dept_salesplan := (SELECT id
                           FROM organization
                           WHERE company_id = @c3
                             AND parent_org_id = @c3_hq_sales
                             AND name = '영업기획부'
                           LIMIT 1);
SET @c3_dept_cs := (SELECT id
                    FROM organization
                    WHERE company_id = @c3
                      AND parent_org_id = @c3_hq_sales
                      AND name = '고객관리부'
                    LIMIT 1);

INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index, is_active, created_at, updated_at)
SELECT @c3,
       @c3_cat_team,
       @c3_dept_dev,
       '백엔드팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       @c3_dept_dev,
       '프론트엔드팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       (SELECT id
        FROM organization
        WHERE company_id = @c3
          AND parent_org_id = @c3_hq_platform
          AND name = '플랫폼운영부'
        LIMIT 1),
       '운영팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       (SELECT id
        FROM organization
        WHERE company_id = @c3
          AND parent_org_id = @c3_hq_platform
          AND name = '플랫폼운영부'
        LIMIT 1),
       '모니터링팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       @c3_dept_hr,
       '인사기획팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       @c3_dept_hr,
       '채용팀',
       2,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       (SELECT id
        FROM organization
        WHERE company_id = @c3
          AND parent_org_id = (SELECT id
                               FROM organization
                               WHERE company_id = @c3
                                 AND parent_org_id = @c3_hq_support
                                 AND name = '재무부'
                               LIMIT 1)
          AND name = '재무부'
        LIMIT 1),
       '회계팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       @c3_dept_plan,
       '서비스기획팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       (SELECT id FROM organization WHERE company_id = @c3 AND parent_org_id = @c3_hq_service AND name = 'QA부' LIMIT 1),
       'QA팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       @c3_dept_salesplan,
       '영업전략팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       @c3_dept_cs,
       'CS팀',
       1,
       TRUE,
       NOW(),
       NOW()
UNION ALL
SELECT @c3,
       @c3_cat_team,
       @c3_dept_cs,
       '고객지원팀',
       2,
       TRUE,
       NOW(),
       NOW()
ON DUPLICATE KEY UPDATE category_id=VALUES(category_id),
                        order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c3_team_be := (SELECT id
                    FROM organization
                    WHERE company_id = @c3
                      AND name = '백엔드팀'
                    LIMIT 1);
SET @c3_team_fe := (SELECT id
                    FROM organization
                    WHERE company_id = @c3
                      AND name = '프론트엔드팀'
                    LIMIT 1);
SET @c3_team_hrp := (SELECT id
                     FROM organization
                     WHERE company_id = @c3
                       AND name = '인사기획팀'
                     LIMIT 1);
SET @c3_team_plan := (SELECT id
                      FROM organization
                      WHERE company_id = @c3
                        AND name = '서비스기획팀'
                      LIMIT 1);
SET @c3_team_sales := (SELECT id
                       FROM organization
                       WHERE company_id = @c3
                         AND name = '영업전략팀'
                       LIMIT 1);
SET @c3_team_cs := (SELECT id
                    FROM organization
                    WHERE company_id = @c3
                      AND name = 'CS팀'
                    LIMIT 1);

-- HR_RANK / POSITION_CATEGORY / USAGE
INSERT INTO hr_rank (company_id, parent_hr_rank_id, name, order_index, is_active, created_at, updated_at)
VALUES (@c3, NULL, '사원', 1, TRUE, NOW(), NOW()),
       (@c3, NULL, '대리', 2, TRUE, NOW(), NOW()),
       (@c3, NULL, '과장', 3, TRUE, NOW(), NOW()),
       (@c3, NULL, '차장', 4, TRUE, NOW(), NOW()),
       (@c3, NULL, '부장', 5, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c3_rank_staff := (SELECT id
                       FROM hr_rank
                       WHERE company_id = @c3
                         AND name = '사원'
                       LIMIT 1);
SET @c3_rank_asst := (SELECT id
                      FROM hr_rank
                      WHERE company_id = @c3
                        AND name = '대리'
                      LIMIT 1);
SET @c3_rank_mgr := (SELECT id
                     FROM hr_rank
                     WHERE company_id = @c3
                       AND name = '과장'
                     LIMIT 1);
SET @c3_rank_sr := (SELECT id
                    FROM hr_rank
                    WHERE company_id = @c3
                      AND name = '차장'
                    LIMIT 1);
SET @c3_rank_head := (SELECT id
                      FROM hr_rank
                      WHERE company_id = @c3
                        AND name = '부장'
                      LIMIT 1);

UPDATE hr_rank
SET parent_hr_rank_id = @c3_rank_asst
WHERE company_id = @c3
  AND name = '사원';
UPDATE hr_rank
SET parent_hr_rank_id = @c3_rank_mgr
WHERE company_id = @c3
  AND name = '대리';
UPDATE hr_rank
SET parent_hr_rank_id = @c3_rank_sr
WHERE company_id = @c3
  AND name = '과장';
UPDATE hr_rank
SET parent_hr_rank_id = @c3_rank_head
WHERE company_id = @c3
  AND name = '차장';
UPDATE hr_rank
SET parent_hr_rank_id = NULL
WHERE company_id = @c3
  AND name = '부장';

INSERT INTO position_category (company_id, org_category_id, name, order_index, is_head, is_active, created_at,
                               updated_at)
VALUES (@c3, @c3_cat_company, '사장', 1, TRUE, TRUE, NOW(), NOW()),
       (@c3, @c3_cat_hq, '본부장', 2, TRUE, TRUE, NOW(), NOW()),
       (@c3, @c3_cat_dept, '부장', 3, TRUE, TRUE, NOW(), NOW()),
       (@c3, @c3_cat_team, '팀장', 4, TRUE, TRUE, NOW(), NOW()),
       (@c3, @c3_cat_team, '팀원', 5, FALSE, TRUE, NOW(), NOW())
ON DUPLICATE KEY UPDATE order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

SET @c3_pos_ceo := (SELECT id
                    FROM position_category
                    WHERE company_id = @c3
                      AND org_category_id = @c3_cat_company
                      AND name = '사장'
                    LIMIT 1);
SET @c3_pos_hq := (SELECT id
                   FROM position_category
                   WHERE company_id = @c3
                     AND org_category_id = @c3_cat_hq
                     AND name = '본부장'
                   LIMIT 1);
SET @c3_pos_dept := (SELECT id
                     FROM position_category
                     WHERE company_id = @c3
                       AND org_category_id = @c3_cat_dept
                       AND name = '부장'
                     LIMIT 1);
SET @c3_pos_tl := (SELECT id
                   FROM position_category
                   WHERE company_id = @c3
                     AND org_category_id = @c3_cat_team
                     AND name = '팀장'
                   LIMIT 1);
SET @c3_pos_tm := (SELECT id
                   FROM position_category
                   WHERE company_id = @c3
                     AND org_category_id = @c3_cat_team
                     AND name = '팀원'
                   LIMIT 1);


-- 팀원의 상위 → 팀장
UPDATE position_category
SET parent_position_id = @c3_pos_tl
WHERE id = @c3_pos_tm;

-- 팀장의 상위 → 부장
UPDATE position_category
SET parent_position_id = @c3_pos_dept
WHERE id = @c3_pos_tl;

-- 부장의 상위 → 본부장
UPDATE position_category
SET parent_position_id = @c3_pos_hq
WHERE id = @c3_pos_dept;

-- 본부장의 상위 → 사장
UPDATE position_category
SET parent_position_id = @c3_pos_ceo
WHERE id = @c3_pos_hq;

-- 사장은 최상위
UPDATE position_category
SET parent_position_id = NULL
WHERE id = @c3_pos_ceo;


INSERT IGNORE INTO org_position_usage (company_id, org_id, position_category_id, created_at, updated_at)
SELECT @c3,
       o.id,
       CASE o.category_id
           WHEN @c3_cat_company THEN @c3_pos_ceo
           WHEN @c3_cat_hq THEN @c3_pos_hq
           WHEN @c3_cat_dept THEN @c3_pos_dept
           WHEN @c3_cat_team THEN @c3_pos_tl
           END,
       NOW(),
       NOW()
FROM organization o
WHERE o.company_id = @c3
  AND o.category_id IN (@c3_cat_company, @c3_cat_hq, @c3_cat_dept, @c3_cat_team);

INSERT IGNORE INTO org_position_usage (company_id, org_id, position_category_id, created_at, updated_at)
SELECT @c3, o.id, @c3_pos_tm, NOW(), NOW()
FROM organization o
WHERE o.company_id = @c3
  AND o.category_id = @c3_cat_team;

-- EMPLOYEE 20명 (GlobalService)
INSERT INTO employee
(company_id, employee_no, internal_phone, phone,
 org_id, hr_rank_id, position_category_id,
 name, email, password,
 role, gender, birth_date, employment_type, status, work_status, hire_date,
 created_at, updated_at)
VALUES (@c3, 'GS-001', '3001', '01033330001', @c3_root, @c3_rank_head, @c3_pos_ceo, '신유진', 'ceo@globalservice.com', @pw,
        'COMPANY_ADMIN', 'FEMALE', '1977-01-15', 'REGULAR', 'ACTIVE', 'WORKING', '2009-01-01', NOW(), NOW()),
       (@c3, 'GS-010', '3010', '01033331010',
        (SELECT id FROM organization WHERE company_id = @c3 AND name = '플랫폼본부' LIMIT 1), @c3_rank_head, @c3_pos_hq,
        '김도현', 'hq_platform@globalservice.com', @pw, 'ADMIN', 'MALE', '1979-02-02', 'REGULAR', 'ACTIVE', 'WORKING',
        '2011-02-01', NOW(), NOW()),
       (@c3, 'GS-011', '3011', '01033331011',
        (SELECT id FROM organization WHERE company_id = @c3 AND name = '경영지원본부' LIMIT 1), @c3_rank_head, @c3_pos_hq,
        '박서윤', 'hq_support@globalservice.com', @pw, 'ADMIN', 'FEMALE', '1980-03-03', 'REGULAR', 'ACTIVE', 'WORKING',
        '2012-03-01', NOW(), NOW()),
       (@c3, 'GS-012', '3012', '01033331012',
        (SELECT id FROM organization WHERE company_id = @c3 AND name = '서비스본부' LIMIT 1), @c3_rank_head, @c3_pos_hq,
        '이민재', 'hq_service@globalservice.com', @pw, 'ADMIN', 'MALE', '1981-04-04', 'REGULAR', 'ACTIVE', 'WORKING',
        '2013-04-01', NOW(), NOW()),
       (@c3, 'GS-013', '3013', '01033331013',
        (SELECT id FROM organization WHERE company_id = @c3 AND name = '영업본부' LIMIT 1), @c3_rank_head, @c3_pos_hq,
        '정하은', 'hq_sales@globalservice.com', @pw, 'ADMIN', 'FEMALE', '1982-05-05', 'REGULAR', 'ACTIVE', 'WORKING',
        '2014-05-01', NOW(), NOW()),
       (@c3, 'GS-020', '3020', '01033332020',
        (SELECT id FROM organization WHERE company_id = @c3 AND name = '개발부' LIMIT 1), @c3_rank_head, @c3_pos_dept,
        '오태훈', 'dev_head@globalservice.com', @pw, 'ADMIN', 'MALE', '1983-06-06', 'REGULAR', 'ACTIVE', 'WORKING',
        '2015-06-01', NOW(), NOW()),
       (@c3, 'GS-021', '3021', '01033332021',
        (SELECT id FROM organization WHERE company_id = @c3 AND name = '인사부' LIMIT 1), @c3_rank_head, @c3_pos_dept,
        '서지은', 'hr_head@globalservice.com', @pw, 'ADMIN', 'FEMALE', '1984-07-07', 'REGULAR', 'ACTIVE', 'WORKING',
        '2015-07-01', NOW(), NOW()),
       (@c3, 'GS-022', '3022', '01033332022',
        (SELECT id FROM organization WHERE company_id = @c3 AND name = '서비스기획부' LIMIT 1), @c3_rank_head, @c3_pos_dept,
        '문상민', 'svc_head@globalservice.com', @pw, 'ADMIN', 'MALE', '1985-08-08', 'REGULAR', 'ACTIVE', 'WORKING',
        '2016-08-01', NOW(), NOW()),
       (@c3, 'GS-023', '3023', '01033332023',
        (SELECT id FROM organization WHERE company_id = @c3 AND name = '영업기획부' LIMIT 1), @c3_rank_head, @c3_pos_dept,
        '김다은', 'sales_head@globalservice.com', @pw, 'ADMIN', 'FEMALE', '1986-09-09', 'REGULAR', 'ACTIVE', 'WORKING',
        '2016-09-01', NOW(), NOW()),
       (@c3, 'GS-030', '3030', '01033333030', @c3_team_be, @c3_rank_sr, @c3_pos_tl, '이준서', 'be_lead@globalservice.com',
        @pw, 'EMPLOYEE', 'MALE', '1989-10-10', 'REGULAR', 'ACTIVE', 'WORKING', '2017-10-01', NOW(), NOW()),
       (@c3, 'GS-031', '3031', '01033333031', @c3_team_fe, @c3_rank_sr, @c3_pos_tl, '한나연', 'fe_lead@globalservice.com',
        @pw, 'EMPLOYEE', 'FEMALE', '1990-11-11', 'REGULAR', 'ACTIVE', 'WORKING', '2017-11-01', NOW(), NOW()),
       (@c3, 'GS-032', '3032', '01033333032', @c3_team_hrp, @c3_rank_sr, @c3_pos_tl, '최성민', 'hr_lead@globalservice.com',
        @pw, 'EMPLOYEE', 'MALE', '1988-12-12', 'REGULAR', 'ACTIVE', 'WORKING', '2017-12-01', NOW(), NOW()),
       (@c3, 'GS-033', '3033', '01033333033', @c3_team_sales, @c3_rank_sr, @c3_pos_tl, '박지은',
        'sales_lead@globalservice.com', @pw, 'EMPLOYEE', 'FEMALE', '1989-01-20', 'REGULAR', 'ACTIVE', 'WORKING',
        '2018-01-01', NOW(), NOW()),
       (@c3, 'GS-040', '3040', '01033334040', @c3_team_be, @c3_rank_asst, @c3_pos_tm, '김현수', 'be1@globalservice.com',
        @pw, 'EMPLOYEE', 'MALE', '1995-02-02', 'REGULAR', 'ACTIVE', 'WORKING', '2020-02-01', NOW(), NOW()),
       (@c3, 'GS-041', '3041', '01033334041', @c3_team_be, @c3_rank_staff, @c3_pos_tm, '정유리', 'be2@globalservice.com',
        @pw, 'EMPLOYEE', 'FEMALE', '1997-03-03', 'REGULAR', 'ACTIVE', 'WORKING', '2021-03-01', NOW(), NOW()),
       (@c3, 'GS-042', '3042', '01033334042', @c3_team_fe, @c3_rank_asst, @c3_pos_tm, '오도현', 'fe1@globalservice.com',
        @pw, 'EMPLOYEE', 'MALE', '1996-04-04', 'REGULAR', 'ACTIVE', 'WORKING', '2020-04-01', NOW(), NOW()),
       (@c3, 'GS-043', '3043', '01033334043', @c3_team_fe, @c3_rank_staff, @c3_pos_tm, '강수진', 'fe2@globalservice.com',
        @pw, 'EMPLOYEE', 'FEMALE', '1998-05-05', 'REGULAR', 'ACTIVE', 'WORKING', '2022-05-01', NOW(), NOW()),
       (@c3, 'GS-044', '3044', '01033334044', @c3_team_hrp, @c3_rank_asst, @c3_pos_tm, '임동우', 'hr1@globalservice.com',
        @pw, 'EMPLOYEE', 'MALE', '1994-06-06', 'REGULAR', 'ACTIVE', 'WORKING', '2019-06-01', NOW(), NOW()),
       (@c3, 'GS-045', '3045', '01033334045', @c3_team_plan, @c3_rank_asst, @c3_pos_tm, '서예린', 'svc1@globalservice.com',
        @pw, 'EMPLOYEE', 'FEMALE', '1995-07-07', 'REGULAR', 'ACTIVE', 'WORKING', '2020-07-01', NOW(), NOW()),
       (@c3, 'GS-046', '3046', '01033334046', @c3_team_cs, @c3_rank_staff, @c3_pos_tm, '정동혁', 'cs1@globalservice.com',
        @pw, 'EMPLOYEE', 'MALE', '1999-08-08', 'REGULAR', 'ACTIVE', 'WORKING', '2023-08-01', NOW(), NOW())
ON DUPLICATE KEY UPDATE internal_phone=VALUES(internal_phone),
                        phone=VALUES(phone),
                        org_id=VALUES(org_id),
                        hr_rank_id=VALUES(hr_rank_id),
                        position_category_id=VALUES(position_category_id),
                        name=VALUES(name),
                        password=VALUES(password),
                        role=VALUES(role),
                        gender=VALUES(gender),
                        birth_date=VALUES(birth_date),
                        employment_type=VALUES(employment_type),
                        status=VALUES(status),
                        work_status=VALUES(work_status),
                        hire_date=VALUES(hire_date),
                        updated_at=NOW();




INSERT INTO company (id, name, business_number, address,
                     representative_name, representative_contact, created_at, updated_at)
VALUES (999, 'OrbitFlow Service', '0000000000', 'SYSTEM', 'SYSTEM', '0000000000', NOW(), NOW());

INSERT INTO org_category (id, company_id, name) VALUES (999, 999, 'OrbitFlow');

INSERT INTO organization (id, company_id, category_id, name, parent_org_id, created_at, updated_at)
VALUES (999, 999, 999, 'SYSTEM_ORG', NULL, NOW(), NOW());


INSERT INTO employee (id, company_id, employee_no, org_id,
                      name, email, password, role, gender, employment_type, status,
                      hire_date, created_at, updated_at)
VALUES (999, 999, 'ORBITFLOW-01', 999,
        'Orbit 관리자', 'test@test.com', @pw, 'TEAM_ORBIT', 'MALE', 'REGULAR', 'ACTIVE',
        '2026-01-17', NOW(), NOW());




COMMIT;


-- 종훈 --
use orbitflow;

INSERT INTO schedule
(company_id, is_company, is_personal, org_category_id, org_id, employee_id,
 schedule_title, schedule_description,
 start_at, end_at, schedule_status)
VALUES
-- 1. 개인 일정
(1, false, true, NULL, NULL, 1,
 '개인 업무 정리', '주간 업무 정리 및 회고',
 '2025-01-06 09:00:00', '2025-01-06 10:00:00', 'RELEASE'),

(1, false, true, NULL, NULL, 1,
 '병원 방문', '정기 건강검진',
 '2025-01-07 15:00:00', '2025-01-07 17:00:00', 'RELEASE'),

-- 3. 백엔드팀
(1, false, false, @c1_cat_team, @c1_team_be, 1,
 '백엔드팀 스크럼', '주간 스크럼 회의',
 '2025-01-06 10:00:00', '2025-01-06 10:30:00', 'RELEASE'),

(1, false, false, @c1_cat_team, @c1_team_be, 1,
 'API 설계 회의', '신규 API 설계 논의',
 '2025-01-08 14:00:00', '2025-01-08 16:00:00', 'RELEASE'),

-- 5. 프론트엔드팀
(1, false, false, @c1_cat_team, @c1_team_fe, 1,
 '프론트엔드 기획 회의', 'UI/UX 개선 논의',
 '2025-01-09 13:00:00', '2025-01-09 15:00:00', 'RELEASE'),

-- 6. 개발부
(1, false, false, @c1_cat_dept, @c1_dept_dev, 1,
 '개발부 월간 회의', '월간 개발 현황 공유',
 '2025-01-10 10:00:00', '2025-01-10 12:00:00', 'RELEASE'),

-- 7. 플랫폼본부
(1, false, false, @c1_cat_hq, @c1_hq_platform, 1,
 '플랫폼본부 전략회의', '분기 전략 수립',
 '2025-01-13 13:00:00', '2025-01-13 16:00:00', 'RELEASE'),

-- 8. 인사부
(1, false, false, @c1_cat_dept, @c1_dept_hr, 1,
 '인사 정책 회의', '평가 제도 개선 논의',
 '2025-01-14 10:00:00', '2025-01-14 12:00:00', 'RELEASE'),

-- 9. 인사기획팀
(1, false, false, @c1_cat_team, @c1_team_hrp, 1,
 '인사기획팀 회의', '조직 개편 검토',
 '2025-01-15 14:00:00', '2025-01-15 15:00:00', 'RELEASE'),

-- 10. 서비스기획팀
(1, false, false, @c1_cat_team, @c1_team_plan, 1,
 '서비스 로드맵 회의', '상반기 로드맵 논의',
 '2025-01-16 10:00:00', '2025-01-16 13:00:00', 'RELEASE'),

-- 11. 영업전략팀
(1, false, false, @c1_cat_team, @c1_team_sales, 1,
 '영업 전략 회의', '신규 고객 전략',
 '2025-01-20 10:00:00', '2025-01-20 12:00:00', 'RELEASE'),

-- 12. CS팀
(1, false, false, @c1_cat_team, @c1_team_cs, 1,
 'CS 이슈 회의', '주요 고객 이슈 공유',
 '2025-01-21 14:00:00', '2025-01-21 15:30:00', 'RELEASE'),

-- 13. 개인 일정
(1, false, false, NULL, NULL, 1,
 '외부 미팅', '외부 파트너 미팅',
 '2025-01-22 13:00:00', '2025-01-22 15:00:00', 'RELEASE'),

-- 14. 재무부
(1, false, false, @c1_cat_dept, @c1_dept_fin, 1,
 '예산 검토 회의', '분기 예산 검토',
 '2025-01-23 10:00:00', '2025-01-23 12:00:00', 'RELEASE'),

-- 15. 서비스본부
(1, false, false, @c1_cat_hq, @c1_hq_service, 1,
 '서비스본부 회의', '서비스 품질 개선',
 '2025-01-24 13:00:00', '2025-01-24 15:00:00', 'RELEASE'),

-- 16. 영업본부
(1, false, false, @c1_cat_hq, @c1_hq_sales, 1,
 '영업본부 주간회의', '영업 실적 공유',
 '2025-01-27 10:00:00', '2025-01-27 11:00:00', 'RELEASE'),

-- 17. 개인 일정
(1, false, true, NULL, NULL, 1,
 '자기계발 시간', '기술 서적 독서',
 '2025-01-28 17:00:00', '2025-01-28 19:00:00', 'RELEASE'),

-- 18. 전사 일정
(1, true, false, @c1_cat_company, @c1_root, 1,
 '전사 타운홀 미팅', 'CEO 타운홀',
 '2025-01-29 10:00:00', '2025-01-29 12:00:00', 'RELEASE'),

-- 19. 개인 일정 (하루 종일)
(1, false, true, NULL, NULL, 1,
 '연차', '개인 연차 사용',
 '2025-01-30 00:00:00', '2025-01-31 00:00:00', 'RELEASE');


INSERT INTO reservation_status (status_code, status_name)
VALUES ('PENDING', '승인 대기'),
       ('CONFIRM', '예약 확정'),
       ('REJECT', '예약 반려'),
       ('CANCELED', '예약 취소'),
       ('DELETED', '삭제됨'),
       ('ETC', '기타');

INSERT INTO resource_status (status_code, status_name)
VALUES ('AVAILABLE', '사용 가능'),
       ('INSPECTION', '점검 중'),
       ('UNAVAILABLE', '사용 불가'),
       ('DELETED', '삭제됨'),
       ('ETC', '기타');

INSERT INTO item_category (company_id, name)
VALUES (1, '노트북'),
       (1, '모니터'),
       (1, '카메라'),
       (1, '기타');

INSERT INTO meetingroom
(company_id, name, position, description, resource_status_id, created_by)
VALUES (1, '중회의실 A', '본관 2층', '10인 수용, TV 구비',
        (SELECT id FROM resource_status WHERE status_code = 'AVAILABLE'), 1),

       (1, '중회의실 B', '본관 3층', '12인 수용, 화상회의 가능',
        (SELECT id FROM resource_status WHERE status_code = 'AVAILABLE'), 1),

       (1, '프로젝트룸', '별관 1층', 'TF팀 전용',
        (SELECT id FROM resource_status WHERE status_code = 'INSPECTION'), 1);

INSERT INTO car
(company_id, number, name, driver_age, description, resource_status_id, created_by)
VALUES (1, '11가1111', 'K5', 26, '영업팀 공용 차량',
        (SELECT id FROM resource_status WHERE status_code = 'AVAILABLE'), 1),

       (1, '22나2222', '쏘나타', 26, '외근 및 출장용',
        (SELECT id FROM resource_status WHERE status_code = 'AVAILABLE'), 1),

       (1, '33다3333', '스타렉스', 30, '대형 이동 차량',
        (SELECT id FROM resource_status WHERE status_code = 'UNAVAILABLE'), 1);

INSERT INTO item
(company_id, item_category_id, name, description, resource_status_id, created_by)
VALUES (1,
        (SELECT id FROM item_category WHERE company_id = 1 AND name = '노트북'),
        'MacBook Pro 14',
        '개발팀 공용 노트북',
        (SELECT id FROM resource_status WHERE status_code = 'AVAILABLE'),
        1),

       (1,
        (SELECT id FROM item_category WHERE company_id = 1 AND name = '모니터'),
        'LG 27인치 모니터',
        '회의실 보조 모니터',
        (SELECT id FROM resource_status WHERE status_code = 'AVAILABLE'),
        1),

       (1,
        (SELECT id FROM item_category WHERE company_id = 1 AND name = '카메라'),
        'Sony A7C',
        '마케팅 촬영 장비',
        (SELECT id FROM resource_status WHERE status_code = 'INSPECTION'),
        1);

INSERT INTO reservation
(company_id, employee_id, type_code, item_category_id, resource_id,
 reservation_date, start_time, end_time,
 reservation_reason, reservation_status_id)
VALUES
-- 회의실 예약
(1, 1, 'MEETING', NULL,
 (SELECT id FROM meetingroom WHERE name = '중회의실 A' AND company_id = 1),
 CURDATE() + INTERVAL 1 DAY, 10, 12,
 '주간 개발 회의',
 (SELECT id FROM reservation_status WHERE status_code = 'CONFIRM')),

-- 차량 예약
(1, 1, 'CAR', NULL,
 (SELECT id FROM car WHERE number = '11가1111'),
 CURDATE() + INTERVAL 2 DAY, 9, 18,
 '외부 미팅 방문',
 (SELECT id FROM reservation_status WHERE status_code = 'PENDING')),

-- 비품 예약
(1, 1, 'ITEM',
 (SELECT id FROM item_category WHERE name = '노트북' AND company_id = 1),
 (SELECT id FROM item WHERE name = 'MacBook Pro 14' AND company_id = 1),
 CURDATE() + INTERVAL 3 DAY, 13, 17,
 'QA 테스트용 장비 대여',
 (SELECT id FROM reservation_status WHERE status_code = 'CONFIRM'));


INSERT INTO template_category (code, name)
VALUES ('ATTENDANCE', '근태'),
       ('SCHEDULE', '일정'),
       ('GENERAL', '일반');



INSERT INTO leave_type (type_name, is_countable, description, unit_days, created_at, updated_at)
VALUES

-- [차감 항목]
('연차', 1, '근로기준법에 따른 정기 유급휴가 (1일 단위)', 1.000, NOW(), NOW()),
('오전반차', 1, '오전 근무 후 14시 퇴근 (0.5일 차감)', 0.500, NOW(), NOW()),
('오후반차', 1, '14시 출근 후 오후 근무 (0.5일 차감)', 0.500, NOW(), NOW()),
('반반차', 1, '2시간 단위 유급휴가 (0.25일 차감)', 0.250, NOW(), NOW()),
('보상휴가', 1, '연장근로 보상으로 발생한 대체 휴가 (1일 단위)', 1.000, NOW(), NOW()),

-- [비차감 항목]
('경조휴가', 0, '본인 및 가족 경조사 관련 유급휴가', 0.000, NOW(), NOW()),
('병가', 0, '업무 외 질병/부상 치료를 위한 휴가', 0.000, NOW(), NOW()),
('포상휴가', 0, '우수 성과자 등에게 부여되는 특별 유급휴가', 0.000, NOW(), NOW()),
('공가', 0, '예비군, 투표 등 공적 의무 수행을 위한 휴가', 0.000, NOW(), NOW()),
('하계휴가', 0, '회사에서 별도로 부여하는 하절기 특별 휴가', 0.000, NOW(), NOW()),
('가족돌봄휴가', 0, '가족의 질병, 사고, 양육을 위한 긴급 휴가 (무급 원칙)', 0.000, NOW(), NOW());



INSERT INTO attendance_rule (company_id, name, default_start_time, default_end_time, default_break_minutes,
                             late_threshold_min, is_default)
VALUES (1, 'OrbitFlow 기본 근무규칙', '09:00:00', '18:00:00', 60, 10, true);


-- 연차 부여 이력 (정기 및 월별)
INSERT INTO grant_history (employee_id, company_id, grant_date, granted_days, grant_type, created_at)
VALUES
-- 이준호: 1월 정기 부여
(10, 1, '2025-01-01', 15.0, 'ANNUAL_REGULAR', '2025-01-01 09:00:00'),
-- 김나연: 1월 정기 부여
(11, 1, '2025-01-01', 15.0, 'ANNUAL_REGULAR', '2025-01-01 09:00:00'),
-- 한지민: 신입사원 월별 부여 (11월, 12월)
(15, 1, '2025-11-01', 1.0, 'ANNUAL_MONTHLY', '2025-11-01 00:01:00'),
(15, 1, '2025-12-01', 1.0, 'ANNUAL_MONTHLY', '2025-12-01 00:01:00'),
-- 오세훈: 포상 휴가 부여 (12월)
(16, 1, '2025-12-10', 1.0, 'REWARD_LEAVE', '2025-12-10 14:00:00');

-- 사원별 2025년도 연차 잔합 상황 (ID 10, 11, 14, 15, 16)
INSERT INTO leave_balance (company_id, employee_id, year, total_granted, remaining_days)
VALUES (1, 10, 2025, 15.0, 11.5), -- 15개 중 3.5개 사용 (12월 현재)
       (1, 11, 2025, 15.0, 14.0), -- 15개 중 1개 사용
       (1, 14, 2025, 15.0, 15.0), -- 미사용자

       (1, 15, 2025, 11.0, 10.5), -- 신입사원 (매달 1개씩 총 11개 부여됨)

       (1, 16, 2025, 15.0, 13.0);
-- 15개 중 2개 사용

-- 실제 휴가 사용 기록 (결재 완료 건들)
-- 2025년 상반기부터 현재까지의 다양한 휴가 신청 내역 (최신순 조회를 위해 작성일 역순 배치)

INSERT INTO attendance_record (employee_id, company_id, start_date, end_date, days, type_id, reason, status, created_at,
                               updated_at)
VALUES
-- 1~5: 최근 신청 (대기 및 승인 섞임)
(1, 1, '2025-12-24', '2025-12-24', 1.0, 1, '크리스마스 이브 휴가', 'SUBMITTED', '2025-12-20 09:00:00', NOW()),
(1, 1, '2025-12-15', '2025-12-15', 0.5, 3, '오후 개인 용무', 'APPROVED', '2025-12-10 14:00:00', NOW()),
(1, 1, '2025-12-01', '2025-12-05', 5.0, 1, '겨울 리프레시 여행', 'REJECTED', '2025-11-25 10:00:00', NOW()),
(1, 1, '2025-11-20', '2025-11-20', 1.0, 5, '심한 몸살로 인한 병가', 'APPROVED', '2025-11-20 08:30:00', NOW()),
(1, 1, '2025-11-11', '2025-11-11', 0.5, 2, '은행 업무(오전)', 'APPROVED', '2025-11-08 11:00:00', NOW()),

-- 6~10: 가을철 내역
(1, 1, '2025-10-25', '2025-10-25', 1.0, 1, '이사 날짜', 'APPROVED', '2025-10-15 09:00:00', NOW()),
(1, 1, '2025-10-10', '2025-10-12', 3.0, 4, '조모상(경조사)', 'APPROVED', '2025-10-10 07:00:00', NOW()),
(1, 1, '2025-09-20', '2025-09-20', 0.5, 3, '오후 검진', 'APPROVED', '2025-09-15 13:00:00', NOW()),
(1, 1, '2025-09-05', '2025-09-05', 1.0, 6, '프로젝트 성공 포상휴가', 'APPROVED', '2025-09-01 10:00:00', NOW()),
(1, 1, '2025-08-30', '2025-08-30', 1.0, 1, '개인 휴식', 'SUBMITTED', '2025-08-25 17:00:00', NOW()),

-- 11~15: 여름 휴가 시즌
(1, 1, '2025-08-01', '2025-08-05', 5.0, 1, '여름 정기 휴가', 'APPROVED', '2025-07-10 09:00:00', NOW()),
(1, 1, '2025-07-15', '2025-07-15', 0.5, 2, '자녀 학교 행사', 'REJECTED', '2025-07-05 10:00:00', NOW()),
(1, 1, '2025-07-01', '2025-07-01', 1.0, 5, '치과 진료', 'APPROVED', '2025-06-30 15:00:00', NOW()),
(1, 1, '2025-06-20', '2025-06-20', 0.5, 3, '오후 조기 퇴근', 'APPROVED', '2025-06-18 11:00:00', NOW()),
(1, 1, '2025-06-05', '2025-06-05', 1.0, 1, '현충일 징검다리 휴가', 'APPROVED', '2025-05-30 09:00:00', NOW()),

-- 16~20: 상반기 내역
(1, 1, '2025-05-15', '2025-05-15', 1.0, 1, '스승의 날 모임', 'APPROVED', '2025-05-10 13:00:00', NOW()),
(1, 1, '2025-05-01', '2025-05-01', 1.0, 6, '근로자의 날 포상', 'APPROVED', '2025-04-25 10:00:00', NOW()),
(1, 1, '2025-04-10', '2025-04-10', 0.5, 2, '오전 건강검진', 'APPROVED', '2025-04-05 09:00:00', NOW()),
(1, 1, '2025-03-15', '2025-03-17', 3.0, 4, '친척 결혼식(지방)', 'APPROVED', '2025-03-01 11:00:00', NOW()),
(1, 1, '2025-02-10', '2025-02-10', 1.0, 1, '연초 개인 정비', 'APPROVED', '2025-02-01 09:00:00', NOW()),
(1, 1, '2025-10-10', '2025-10-14', 5.0, 6, '본인 결혼 경조사', 'APPROVED', '2025-09-20 09:00:00', NOW()),
(1, 1, '2025-11-05', '2025-11-07', 3.0, 6, '가족 상례 참석', 'APPROVED', '2025-11-05 07:30:00', NOW()),
(1, 1, '2025-12-20', '2025-12-20', 1.0, 6, '친척 결혼식 지참', 'SUBMITTED', '2025-12-15 11:00:00', NOW()),

-- 병가 (ID: 7)
(1, 1, '2025-11-15', '2025-11-15', 1.0, 7, '심한 몸살감기', 'APPROVED', '2025-11-15 08:20:00', NOW()),
(1, 1, '2025-10-01', '2025-10-02', 2.0, 7, '수술 후 회복 기간', 'APPROVED', '2025-09-25 14:00:00', NOW()),
(1, 1, '2025-08-20', '2025-08-20', 1.0, 7, '치과 진료 및 통증', 'REJECTED', '2025-08-18 10:00:00', NOW()),
(1, 1, '2025-12-10', '2025-12-10', 1.0, 7, '정기 건강검진 재검', 'SUBMITTED', '2025-12-05 09:00:00', NOW()),

-- 포상휴가 (ID: 8)
(1, 1, '2025-09-30', '2025-09-30', 1.0, 8, '3분기 우수사원 포상', 'APPROVED', '2025-09-15 10:00:00', NOW()),
(1, 1, '2025-05-01', '2025-05-01', 1.0, 8, '근로자의 날 기념 포상', 'APPROVED', '2025-04-20 09:00:00', NOW()),
(1, 1, '2025-11-28', '2025-11-28', 1.0, 8, '장기근속 5주년 포상', 'SUBMITTED', '2025-11-20 13:00:00', NOW()),

-- 공가 (ID: 9)
(1, 1, '2025-06-15', '2025-06-15', 1.0, 9, '민방위 훈련 소집', 'APPROVED', '2025-06-01 09:00:00', NOW()),
(1, 1, '2025-04-10', '2025-04-10', 1.0, 9, '선거 투표 참여(공가)', 'APPROVED', '2025-04-05 10:00:00', NOW()),
(1, 1, '2025-03-20', '2025-03-20', 1.0, 9, '국가 공인 시험 응시', 'REJECTED', '2025-03-10 11:00:00', NOW()),

-- 하계휴가 (ID: 10)
(1, 1, '2025-08-01', '2025-08-05', 5.0, 10, '하절기 정기 휴가', 'APPROVED', '2025-07-15 09:00:00', NOW()),
(1, 1, '2025-07-20', '2025-07-24', 5.0, 10, '개인 리프레시 여행', 'APPROVED', '2025-07-01 10:00:00', NOW()),

-- 가족돌봄휴가 (ID: 11)
(1, 1, '2025-12-26', '2025-12-26', 1.0, 11, '자녀 어린이집 행사', 'SUBMITTED', '2025-12-24 16:00:00', NOW()),
(1, 1, '2025-05-05', '2025-05-05', 1.0, 11, '자녀 돌봄 필요', 'APPROVED', '2025-04-30 09:00:00', NOW()),
(1, 1, '2025-02-15', '2025-02-15', 1.0, 11, '부모님 병원 동행', 'APPROVED', '2025-02-10 14:00:00', NOW()),
(1, 1, '2025-01-20', '2025-01-20', 1.0, 11, '가족 긴급 상황 발생', 'APPROVED', '2025-01-15 08:00:00', NOW()),
(1, 1, '2025-12-29', '2025-12-29', 1.0, 11, '연말 가족 돌봄', 'SUBMITTED', '2025-12-28 17:00:00', NOW());
INSERT INTO attendance (company_id, employee_id, work_date, commute_at, leave_at, status, applied_rule_id, is_corrected,
                        correction_reason)
VALUES (1, 1, '2025-01-02', '2025-01-02 08:55:00', '2025-01-02 18:05:00', '정상출근', 1, 0, NULL),
       (1, 2, '2025-01-02', '2025-01-02 09:10:00', '2025-01-02 18:30:00', '지각', 1, 0, NULL),
       (1, 1, '2025-01-03', '2025-01-03 08:50:00', '2025-01-03 18:10:00', '정상출근', 1, 0, NULL),
       (1, 2, '2025-01-03', '2025-01-03 08:58:00', '2025-01-03 18:00:00', '정상출근', 1, 0, NULL),
       (1, 1, '2025-01-06', '2025-01-06 08:45:00', '2025-01-06 19:00:00', '연장근무', 1, 0, NULL),
       (1, 2, '2025-01-06', '2025-01-06 09:05:00', '2025-01-06 18:05:00', '지각', 1, 0, NULL),
       (1, 1, '2025-01-07', '2025-01-07 08:52:00', '2025-01-07 18:02:00', '정상출근', 1, 0, NULL),
       (1, 2, '2025-01-07', '2025-01-07 08:59:00', '2025-01-07 18:15:00', '정상출근', 1, 0, NULL),
       (1, 1, '2025-01-08', '2025-01-08 09:30:00', '2025-01-08 18:30:00', '정상출근', 1, 1, '오전 반차 사용 후 출근'),
       (1, 2, '2025-01-08', '2025-01-08 08:50:00', '2025-01-08 18:00:00', '정상출근', 1, 0, NULL),
       (1, 1, '2025-01-09', '2025-01-09 08:55:00', '2025-01-09 18:05:00', '정상출근', 1, 0, NULL),
       (1, 2, '2025-01-09', '2025-01-09 09:15:00', '2025-01-09 18:40:00', '지각', 1, 0, NULL),
       (1, 1, '2025-01-10', '2025-01-10 08:40:00', '2025-01-10 17:00:00', '조퇴', 1, 1, '개인 사정으로 조퇴'),

       (1, 1, '2025-01-13', '2025-01-13 08:50:00', '2025-01-13 18:10:00', '정상출근', 1, 0, NULL),
       (1, 2, '2025-01-13', '2025-01-13 08:55:00', '2025-01-13 18:00:00', '정상출근', 1, 0, NULL),
       (1, 1, '2025-01-14', '2025-01-14 08:58:00', '2025-01-14 20:30:00', '연장근무', 1, 0, NULL),
       (1, 2, '2025-01-14', '2025-01-14 09:02:00', '2025-01-14 18:05:00', '지각', 1, 0, NULL),
       (1, 1, '2025-01-15', '2025-01-15 08:50:00', '2025-01-15 18:05:00', '정상출근', 1, 0, NULL),
       (1, 2, '2025-01-15', '2025-01-15 08:45:00', '2025-01-15 18:10:00', '정상출근', 1, 0, NULL),
       (1, 10, '2025-01-15', '2025-01-15 08:45:00', '2025-01-15 18:10:00', '정상출근', 1, 0, NULL),
       (1, 10, '2025-01-10', '2025-01-10 08:40:00', '2025-01-10 17:00:00', '조퇴', 1, 1, '개인 사정으로 조퇴');



/* 최종 완결판 더미 데이터 스크립트
   - 대상: 회사 1(1-20), 회사 2(21-40), 회사 3(41-60) 전 직원
   - 내용: 1인당 게시글 2개, 댓글 2개, 쪽지 2개(발신) 하드코딩
   - 제약: board_category_id는 조회문을 통해 외래키 오류 방지
*/

-- ==========================================
-- 1. 카테고리 데이터 (ID: 1 ~ 20번 생성 가정)
-- ==========================================
-- 공통 게시판(organization_id NULL)은 그대로 insert
INSERT INTO board_category (company_id, organization_id, board_name, board_type, is_activated, comment_activated)
VALUES (1, NULL, '공지사항', 'NOTICE', 1, 1),
       (1, NULL, '자유 게시판', 'FREE', 1, 1),
       (1, NULL, '사내 건의함', 'FREE', 1, 1),
       (1, NULL, '칭찬 게시판', 'FREE', 1, 1),
       (1, NULL, '자기계발/스터디', 'FREE', 1, 1),
       (2, NULL, '공지사항', 'NOTICE', 1, 1),
       (2, NULL, '기술 공유 라운지', 'FREE', 1, 1),
       (2, NULL, '사내 중고 장터', 'FREE', 1, 1),
       (3, NULL, 'Notice', 'NOTICE', 1, 1),
       (3, NULL, 'General Forum', 'FREE', 1, 1);

-- 부서 게시판은 organization 테이블에서 조직명 끌어와 생성
INSERT INTO board_category (company_id, organization_id, board_name, board_type, is_activated, comment_activated)
SELECT o.company_id,
       o.id                   AS organization_id,
       CONCAT(o.name, ' 게시판') AS board_name,
       'ORGANIZATION',
       1,
       1
FROM organization o
WHERE o.id IN (1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

-- ==========================================
-- 2. 게시글 데이터 (120개)
-- ==========================================
INSERT INTO board_post (id, board_category_id, employee_id, board_title, board_content, view_count)
VALUES
-- =========================
-- 회사 1 (board_id 1~40) / employee_id 1~20 / category 1~10
-- =========================
(1, 1, 1, '2025년 상반기 전사 목표 및 KPI 공유', '본부별 KPI 및 핵심 과제를 공유드립니다. 세부 지표는 첨부자료 참고 바랍니다.', 132),
(2, 1, 9, '사내 정보보안 점검 일정 안내', '외부 보안 감사가 1월 2주차에 진행됩니다. 점검 항목 사전 확인 바랍니다.', 98),
(3, 1, 19, '임직원 복지 제도 설문 안내', '복지 제도 개선을 위한 설문입니다. 1/10(금)까지 참여 부탁드립니다.', 121),
(4, 1, 1, '타운홀 Q&A 정리 공유', '타운홀에서 나온 질문과 답변을 정리했습니다. 추가 문의는 댓글로 남겨주세요.', 86),

(5, 2, 3, '팀 회식 장소 추천 부탁', '다음 주 회식 예정인데 접근성 좋은 곳 추천 부탁드려요.', 33),
(6, 2, 7, '메신저 알림 설정 팁 공유', '집중 시간대 알림 줄이는 설정 팁 공유합니다.', 28),
(7, 2, 14, '업무용 노트북 교체 경험 공유', '개발/분석 용도로 교체하신 분들 모델 추천 부탁드립니다.', 46),
(8, 2, 20, '사내 좌석 주변 소음 이슈', '콜존 근처 소음이 커서 개선 의견이 있으면 공유 부탁드립니다.', 24),

(9, 3, 17, '회의실 예약 시스템 개선 요청', '중복 예약/노쇼가 잦습니다. 예약 정책 개선 제안드립니다.', 52),
(10, 3, 4, '법인카드 증빙 제출 프로세스 개선', '증빙 제출 단계가 많아 누락이 발생합니다. 개선 부탁드립니다.', 41),
(11, 3, 13, '재택 장비 지원 기준 명확화 요청', '모니터/허브 지원 기준을 문서로 정리 요청드립니다.', 37),
(12, 3, 16, '사내 교육 신청 페이지 UX 개선', '검색/필터가 없어 신청이 어렵습니다. 개선 제안드립니다.', 29),

(13, 4, 10, '주말 장애 대응 감사 인사', '주말 점검 중 이슈를 빠르게 대응해주셔서 감사합니다.', 66),
(14, 4, 7, '긴급 핫픽스 지원 감사', '고객 이슈 발생 시 빠르게 수정 배포해주셔서 감사드립니다.', 58),
(15, 4, 16, '정산 처리 신속 지원 감사', '정산 오류 건을 당일 처리해주셔서 감사드립니다.', 49),
(16, 4, 20, '복지 제도 설명회 진행 감사', '신규 복지 안내가 명확했고 Q&A도 좋았습니다.', 44),

(17, 5, 8, 'Spring Boot 3 스터디 모집', '매주 금요일 점심(12:10~12:50) 진행. 초보자도 환영합니다.', 39),
(18, 5, 18, 'AWS SAA 준비 자료 공유', '요약 노트/문제풀이 루틴 공유드립니다.', 47),
(19, 5, 12, '프론트 접근성(ARIA) 세미나 후기', '실무 적용 포인트 위주로 정리했습니다.', 31),
(20, 5, 5, 'SQL 튜닝 미니 세션 제안', '실무에서 자주 쓰는 실행계획/인덱스 사례 중심으로 진행하면 좋겠습니다.', 26),

(21, 6, 2, '결제 API 응답 속도 개선 결과', '캐시 및 쿼리 최적화로 P95 응답시간 30% 개선되었습니다.', 54),
(22, 6, 11, 'Redis 장애 재발 방지 대책', 'Failover 설정 보강 및 모니터링 알림 룰을 추가했습니다.', 46),
(23, 6, 6, '배치 작업 스케줄 변경 제안', '피크타임 영향 최소화를 위해 야간 배치 시간을 조정 제안드립니다.', 38),
(24, 6, 18, '로그 적재 구조 개선안', '파티셔닝/압축 적용으로 저장 비용 절감 가능 예상됩니다.', 35),

(25, 7, 3, '접근성(ARIA) 가이드 초안 공유', '컴포넌트별 필수 속성/금지 패턴을 정리했습니다.', 41),
(26, 7, 12, '디자인 시스템 컴포넌트 정리 계획', '중복 컴포넌트 정리 및 네이밍 규칙 통일 예정입니다.', 36),
(27, 7, 7, '빌드 속도 개선 공유', '캐시 전략 변경으로 빌드 시간이 평균 18% 단축되었습니다.', 29),
(28, 7, 15, '모바일 Safari UI 깨짐 이슈', '특정 뷰에서 스크롤/버튼 영역 겹침 이슈가 있습니다.', 25),

(29, 8, 4, '연차/반차 기준 변경 안내', '반반차 사용 기준이 일부 변경됩니다. 시행일: 1/15', 88),
(30, 8, 13, '육아휴직 복직 프로세스 안내', '복직 2주 전 상담/서류 제출 절차를 안내드립니다.', 40),
(31, 8, 16, '급여 명세서 확인 요청', '이번 달 급여 명세 확인 부탁드립니다. 문의는 경영지원으로.', 55),
(32, 8, 19, '명절 선물 지급 일정 안내', '설 선물은 1/20 주간 배송 예정입니다.', 61),

(33, 9, 5, 'CS SLA 주간 리포트', '평균 응답 1.9시간, 목표(2시간) 이내 유지 중입니다.', 52),
(34, 9, 14, '반복 고객 이슈 Top 3 공유', '로그인/결제/알림 관련 이슈가 반복됩니다. 대응 현황 공유드립니다.', 44),
(35, 9, 10, '고객 공지 배포 완료', '1/5 점검 공지가 고객센터 및 앱에 배포되었습니다.', 36),
(36, 9, 1, '장애 공지 커뮤니케이션 회고', '공지 템플릿 개선과 승인 프로세스 단축이 필요합니다.', 48),

(37, 10, 6, '3월 신규 계약 성과 공유', '중견기업 3곳 신규 계약 체결, 파이프라인 업데이트합니다.', 63),
(38, 10, 15, 'Q2 영업 파이프라인 현황', '리드→기회 전환율 및 예상 매출 공유드립니다.', 58),
(39, 10, 5, '제안서 템플릿 업데이트', '최신 레퍼런스 및 가격 정책 반영 완료했습니다.', 42),
(40, 10, 9, '계약 프로세스 변경 안내', '법무 검토 단계가 추가됩니다. 체크리스트 참고 바랍니다.', 39),

-- =========================
-- 회사 2 (board_id 41~80) / employee_id 21~40 / category 11~16
-- =========================
(41, 11, 21, '2025 기술 전략 방향 공유', '플랫폼 안정성 + AI 적용 확대를 핵심으로 추진합니다.', 141),
(42, 11, 30, '사내 기술 세미나 운영 안내', '월 2회 세미나 진행, 발표 신청은 폼으로 받습니다.', 92),
(43, 11, 33, '명절 귀성비 지급 공지', '지급일: 1/23, 지급 기준은 HR 공지 참고.', 125),
(44, 11, 40, '법정 의무 교육 미이수자 안내', '오늘 18:00 마감입니다. 미이수자 목록 확인 바랍니다.', 103),

(45, 12, 21, 'K8s 오토스케일링 운영 사례', 'HPA 튜닝 포인트와 장애 케이스를 공유합니다.', 58),
(46, 12, 27, 'Git 컨벤션 통일 제안', '릴리즈/핫픽스 브랜치 규칙과 커밋 메시지 템플릿 공유.', 47),
(47, 12, 34, 'Playwright E2E 자동화 도입기', '핵심 시나리오부터 단계적으로 확장하는 전략을 공유합니다.', 42),
(48, 12, 36, 'Rust 파일럿 결과 공유', '메모리 안정성 장점 있으나 러닝커브 고려 필요.', 33),

(49, 13, 22, '개발용 키보드 판매', '3개월 사용, 상태 양호. 사내 직거래.', 19),
(50, 13, 25, '모니터암 나눔', '한 개 남았습니다. 필요하신 분 댓글 주세요.', 24),
(51, 13, 32, '기프티콘 교환', '스타벅스→다른 브랜드 교환 원합니다.', 15),
(52, 13, 38, '캠핑 의자 판매', '가벼운 모델, 2회 사용.', 17),

(53, 14, 22, 'LLM 파인튜닝 실험 결과', '도메인 데이터 추가 시 정확도 상승, 비용/효율 정리.', 71),
(54, 14, 25, 'Vision 모델 추론 속도 개선', '전처리 최적화로 TPS 향상. 벤치마크 공유.', 56),
(55, 14, 34, 'RLHF 적용 검토 메모', 'ROI 관점에서 단계적 적용을 제안합니다.', 45),
(56, 14, 37, 'OCR 모델 성능 비교', 'Tesseract 대비 사내 모델 우수. 케이스별 한계도 공유.', 41),

(57, 15, 23, 'API Gateway 인증 방식 변경', 'OAuth2 기반으로 전환 완료. 클라이언트 적용 가이드 포함.', 52),
(58, 15, 29, 'UI 컴포넌트 라이브러리 배포', '버튼/모달/테이블 공통화. 버전/마이그레이션 안내.', 59),
(59, 15, 32, '모바일 웹 성능 개선 결과', 'LCP/INP 개선 수치 및 작업 내역 공유.', 44),
(60, 15, 38, '공통 컴포넌트 가이드 업데이트', 'Select/Modal 사용 예시 보강했습니다.', 37),

(61, 16, 24, '클라우드 비용 절감 결과', '유휴 리소스 정리로 월 15% 절감.', 49),
(62, 16, 27, 'DB 샤딩 도입 검토', '데이터 증가 대비 확장 전략 및 리스크 공유.', 46),
(63, 16, 33, 'K8s 트러블슈팅 사례 공유', 'Ingress 설정 오류 원인/해결 과정 정리.', 41),
(64, 16, 39, 'Docker 이미지 보안 취약점 점검', '고위험 항목 조치 완료, 재발 방지 프로세스 공유.', 36),

(65, 12, 31, 'FastAPI 도입 후기', '기존 대비 개발 생산성/성능 비교 및 운영 이슈 정리.', 34),
(66, 12, 39, 'Kafka Streams 적용 경험', '실시간 파이프라인 운영 팁과 모니터링 포인트 공유.', 29),
(67, 14, 28, 'Stable Diffusion 업무 적용 사례', '디자인 시안 생성 워크플로우 정리.', 38),
(68, 15, 26, '로깅 라이브러리 v3 배포', '성능 향상 및 API 변경점 안내.', 31),

(69, 16, 36, 'Zabbix 알림 슬랙 연동', '장애 알림 채널 분리 및 라우팅 룰 공유.', 33),
(70, 11, 23, '사무실 자리 재배치 안내', '다음 주말 대규모 자리 이동이 있습니다.', 74),
(71, 12, 30, 'Go 동시성 패턴 정리', 'Channel/Goroutine 운영 팁 정리.', 28),
(72, 14, 31, '데이터 라벨링 효율화 공유', '자동 라벨링 도입 후 처리량 3배 증가.', 32),

(73, 15, 35, '디자인 시스템 v1.5 릴리즈', '다크 모드 팔레트 및 토큰 정리 반영.', 36),
(74, 13, 35, '아이패드 에어 판매', '풀박스, 상태 양호. 직거래.', 52),
(75, 12, 12, '테크 글쓰기 스터디 제안', '주 1회 기술 블로그 초안 리뷰 방식 제안.', 21),
(76, 16, 29, '네트워크 보안 강화 조치', '외부 접속 시 VPN 의무화 적용 안내.', 54),

(77, 11, 26, '임직원 건강검진 캠페인', '대상자는 기한 내 예약 부탁드립니다.', 57),
(78, 12, 24, 'Grafana 대시보드 템플릿 공유', '실시간 상태 모니터링용 템플릿입니다.', 24),
(79, 13, 28, '데스크패드 판매', '미개봉, 사이즈가 맞지 않아 판매합니다.', 13),
(80, 14, 40, '생성 AI 저작권 동향 정리', '최근 가이드라인 요약 및 주의사항 공유.', 45),

-- =========================
-- 회사 3 (board_id 81~120) / employee_id 41~60 / category 17~20
-- =========================
(81, 17, 41, 'Global Strategy 2025', 'Expansion plan across SEA and LATAM. Please review milestones.', 165),
(82, 17, 48, 'New Expense Policy', 'Updated rules for travel, receipts, and approvals effective next month.', 124),
(83, 17, 50, 'Security Awareness Month', 'Mandatory training required by Friday. Link in the description.', 102),
(84, 17, 56, 'CEO Global Townhall', 'Join via Zoom next Wednesday. Agenda attached.', 148),

(85, 18, 41, 'Singapore office visit plans?', 'Anyone visiting next month? Let’s coordinate schedules.', 35),
(86, 18, 45, 'Flight ticket deals update', 'Found discounted routes for Tokyo and Bangkok. Sharing links internally.',
 47),
(87, 18, 51, 'Running club meetup', '5K run this Friday morning. Meeting point: lobby.', 29),
(88, 18, 59, 'Holiday greetings', 'Wishing everyone a great holiday season!', 105),

(89, 19, 42, 'App Review Summary', 'UI feedback improved, but crash reports still observed on iOS.', 63),
(90, 19, 46, 'B2B Client Feedback', 'Demand for SSO and custom dashboards increased this quarter.', 52),
(91, 19, 52, 'iOS Crash Report - Fix deployed', 'Login crash fixed in v1.2.3. Monitoring continues.', 58),
(92, 19, 60, 'Voice of Customer Report', 'Top 5 recurring complaints and proposed fixes are summarized.', 49),

(93, 20, 43, 'YouTube Ads Performance', 'CTR up 15% in India. Next steps: creative refresh A/B.', 46),
(94, 20, 49, 'Influencer Campaign List', 'UK market influencers shortlist and contact status.', 42),
(95, 20, 53, 'Search Ads Optimization', 'Keyword tuning for Japan; CPC reduced by 8%.', 44),
(96, 20, 59, 'A/B Test Result Summary', 'Red CTA performs better; rollout plan proposed.', 53),

(97, 17, 43, 'US office holiday schedule', 'Please check dates and update your calendars.', 96),
(98, 17, 54, 'Internal Audit Result', 'We passed security requirements. Action items attached.', 61),
(99, 17, 58, 'Sustainability Initiative', 'Office is going paperless from Q2. Printing policy updated.', 69),
(100, 17, 60, 'Annual Performance Review', 'Please submit self-evaluation by end of month.', 89),

(101, 18, 42, 'Local cafe near office', 'Great coffee place opened nearby. Team lunch anyone?', 27),
(102, 18, 44, 'Lost item at lobby', 'Lost sunglasses. Please let me know if found.', 13),
(103, 18, 47, 'Gym membership discount', 'Group discount available for employees. Details inside.', 24),
(104, 18, 53, 'Anyone up for a hike?', 'Planning a weekend hike at Namsan. Join if interested.', 21),

(105, 19, 44, 'CS Response Time Goal', 'We need to reduce response time below 2 hours. Proposal included.', 35),
(106, 19, 48, 'User Satisfaction Survey', 'NPS reached 70 last month; drivers and next actions listed.', 55),
(107, 19, 54, 'Premium Plan Inquiry', 'Enterprise clients asking for SSO; scope discussion needed.', 36),
(108, 19, 50, 'Retention Rate Improvement', 'Ideas to keep users active—feature nudges and onboarding changes.', 40),

(109, 20, 45, 'New Banner Designs ready', 'Summer Sale visual assets uploaded to the shared drive.', 57),
(110, 20, 47, 'Social Media Strategy', 'Focusing on TikTok for younger audience; content calendar attached.', 39),
(111, 20, 51, 'Q4 Budget Allocation', 'Shifting more funds to video ads; breakdown included.', 48),
(112, 20, 55, 'Podcast Sponsoring plan', 'Launching our first podcast ad soon. Tracking metrics proposed.', 29),

(113, 17, 46, 'Office Renovation Plan', 'Renovation starts next month on the 5th floor. Schedule attached.', 86),
(114, 17, 52, 'New Hire Welcome', 'Welcome Sarah to the Design team!', 67),
(115, 17, 50, 'Security training reminder', 'Please complete training by Friday to avoid access restrictions.', 78),
(116, 17, 41, 'Global OKR alignment notes', 'Notes from OKR alignment meeting with key decisions and owners.', 72),

(117, 18, 49, 'Weather getting colder', 'Take care of your health everyone. Dress warm.', 18),
(118, 18, 57, 'Ski trip interest check', 'Planning for January. Interested folks please comment.', 26),
(119, 18, 55, 'Best ramen place discovered', 'Tried a new place—highly recommended for team dinner.', 54),
(120, 18, 45, 'Travel tips for Tokyo', 'Sharing transit and hotel tips from last trip.', 43);


-- ==========================================
-- 3. 댓글 데이터 (120개) - 하드코딩 버전
-- ==========================================
INSERT INTO comment (board_post_id, employee_id, comment_content)
VALUES
-- 회사 1 (1~40)
(1, 4, 'KPI 세부 정의(산식)도 공유 가능할까요?'),
(2, 10, '점검 당일 영향 범위(서비스 영향 유무)도 같이 안내 부탁드립니다.'),
(3, 16, '설문 문항 중 “복지 포인트” 항목에 의견 남겼습니다.'),
(4, 7, 'Q&A에 포함 안 된 질문이 있어 댓글로 남깁니다. 확인 부탁드려요.'),

(5, 6, '강남역/역삼역 사이에 회식하기 괜찮은 곳 리스트 공유드릴게요.'),
(6, 3, '집중 시간대 알림 OFF 설정은 정말 필요하네요. 공감합니다.'),
(7, 11, '개발/도커 빌드 고려하면 RAM 32GB 이상 추천드립니다.'),
(8, 17, '콜존 위치 조정 또는 흡음 패널 설치도 검토해볼 수 있을 것 같아요.'),

(9, 19, '노쇼 패널티 기준이 있으면 개선될 것 같습니다.'),
(10, 13, '증빙 누락 방지 체크리스트가 있으면 좋겠어요.'),
(11, 4, '지원 범위를 “직군/근무형태” 기준으로 정리 부탁드립니다.'),
(12, 8, '신청 내역/승인 상태가 한 화면에서 보이면 좋겠습니다.'),

(13, 1, '주말 대응 덕분에 월요일 장애 없이 시작했습니다. 감사합니다!'),
(14, 5, '고객 커뮤니케이션까지 같이 챙겨주셔서 감사했습니다.'),
(15, 19, '정산 일정 촉박했는데 큰 도움 됐습니다.'),
(16, 9, '신규 복지 Q&A가 특히 유익했습니다.'),

(17, 2, '커리큘럼/자료 공유 방식도 안내 부탁드립니다.'),
(18, 11, '정리 자료 감사합니다. 스터디 참여 희망합니다.'),
(19, 15, '접근성 체크리스트도 함께 공유해주시면 적용이 더 쉬울 것 같아요.'),
(20, 6, '실무 사례 중심이면 참여율 높을 것 같습니다.'),

(21, 10, '장애/피크타임 기준으로 P99도 같이 보면 좋을 것 같아요.'),
(22, 2, '모니터링 룰 변경 사항 문서 링크 부탁드립니다.'),
(23, 11, '피크타임 영향 데이터(그래프) 있으면 공유 부탁드려요.'),
(24, 6, '비용 절감 추정치 산정 근거도 함께 공유 가능할까요?'),

(25, 12, '컴포넌트별 예제 코드가 있으면 더 좋겠습니다.'),
(26, 15, '마이그레이션 일정/범위도 공지 부탁드립니다.'),
(27, 3, 'CI 캐시 설정 값도 공유해주시면 참고하겠습니다.'),
(28, 7, '재현 조건(iOS 버전/기기) 추가로 남겨둘게요.'),

(29, 16, '시행일 전까지 잔여 연차 처리 기준도 안내 부탁드립니다.'),
(30, 4, '복직 전 상담 예약 방법이 궁금합니다.'),
(31, 19, '명세서 항목 중 수당 계산 기준도 문의드립니다.'),
(32, 13, '배송지 변경 가능 여부도 확인 부탁드려요.'),

(33, 14, '반복 이슈는 원인 분류(서버/클라)도 같이 보면 좋겠습니다.'),
(34, 5, 'Top3 이슈 중 결제 이슈는 FAQ 업데이트도 같이 가면 좋겠어요.'),
(35, 20, '공지 템플릿 업데이트 버전 공유 부탁드립니다.'),
(36, 10, '승인 프로세스 단축은 좋은 방향 같습니다. 제안안 기대합니다.'),

(37, 15, '성과 축하드립니다! 레퍼런스 문구 업데이트도 같이 부탁드려요.'),
(38, 6, '전환율 하락 구간(리드→기회) 원인도 함께 보고 싶습니다.'),
(39, 9, '템플릿 바뀐 부분(가격/구성) 요약 부탁드립니다.'),
(40, 4, '법무 체크리스트 업데이트되면 공유 부탁드립니다.'),

-- 회사 2 (41~80)
(41, 24, '안정성 지표(가용성/장애 MTTR) 목표도 포함되나요?'),
(42, 27, '발표 신청/리뷰 기준이 있으면 같이 안내 부탁드립니다.'),
(43, 21, '지급 기준(재직 조건 등) 요약도 부탁드립니다.'),
(44, 30, '미이수자 확인 링크가 안 열립니다. 재공유 가능할까요?'),

(45, 33, 'HPA 튜닝 시 CPU/메모리 기준 설정 근거도 공유 부탁드려요.'),
(46, 34, '커밋 템플릿 예시를 PR 템플릿에도 넣으면 좋겠어요.'),
(47, 21, '핵심 시나리오 우선순위 선정 기준이 궁금합니다.'),
(48, 39, '러닝커브 최소화 교육 자료도 있으면 좋겠습니다.'),

(49, 25, '가격/거래 장소 댓글로 남겨주세요.'),
(50, 22, '받고 싶습니다! 오늘 퇴근 후 가능할까요?'),
(51, 38, '교환 가능 브랜드 목록 남겨주시면 확인해볼게요.'),
(52, 32, '제품 상태 사진 있으면 부탁드립니다.'),

(53, 31, '학습 데이터 구성(비율/정제) 정보도 공유 가능할까요?'),
(54, 22, '벤치마크 환경(GPU/배치)도 함께 적어주시면 좋아요.'),
(55, 25, 'ROI 계산에 포함된 비용 항목 정의가 궁금합니다.'),
(56, 24, '케이스별 오류 유형(회전/노이즈)도 표로 정리되면 좋겠어요.'),

(57, 29, '클라이언트 마이그레이션 체크리스트 공유 부탁드립니다.'),
(58, 23, '버전 업 시 Breaking Change 요약이 있으면 좋겠습니다.'),
(59, 38, 'INP 개선에 영향 큰 작업 항목이 무엇이었나요?'),
(60, 32, '가이드 문서에 코드 샘플이 더 추가되면 좋겠습니다.'),

(61, 39, '절감 항목별(Compute/Storage/Network) 비중도 공유 부탁드려요.'),
(62, 33, '리스크(쿼리 라우팅/트랜잭션) 대응안도 궁금합니다.'),
(63, 27, 'Ingress 설정 실수 방지용 린트/가드레일도 검토 부탁드립니다.'),
(64, 36, '취약점 점검 주기/자동화 계획도 공유 부탁드립니다.'),

(65, 21, '운영에서 가장 힘들었던 부분(로깅/모니터링)이 뭐였나요?'),
(66, 24, '컨슈머 그룹 리밸런싱 이슈 케이스도 궁금합니다.'),
(67, 29, '저작권/라이선스 이슈는 어떻게 처리하셨나요?'),
(68, 31, 'API 변경점 마이그레이션 가이드 링크 부탁드립니다.'),

(69, 33, '채널별 알림 라우팅 룰 예시 공유 부탁드려요.'),
(70, 21, '이동 대상 좌석 배치도(도면)도 공유되나요?'),
(71, 27, '실전에서 흔한 데드락/경합 케이스도 있으면 좋겠습니다.'),
(72, 22, '라벨링 품질 측정 기준도 같이 공유해주세요.'),

(73, 32, '토큰/테마 변경 사항을 한 장 요약으로 보고 싶습니다.'),
(74, 22, '구매 희망합니다. 직거래 시간 가능 여부 알려주세요.'),
(75, 30, '리뷰 룰(분량/형식) 가이드가 있으면 참여하기 쉬울 것 같아요.'),
(76, 39, 'VPN 적용 범위(내부망/외부망) 명확히 부탁드립니다.'),

(77, 24, '예약 링크/대상자 명단 확인 방법도 공유 부탁드려요.'),
(78, 21, '템플릿 적용 방법(Import)도 같이 적어주시면 좋겠습니다.'),
(79, 22, '사이즈/재질 정보 부탁드립니다.'),
(80, 23, '업무 적용 시 주의사항(저작권/데이터) 섹션이 특히 유용했습니다.'),

-- 회사 3 (81~120)
(81, 46, 'Milestones look good. Can we add risk owners per region?'),
(82, 60, 'Approval workflow changes—please confirm who the approvers are.'),
(83, 41, 'Training link received. Will there be a short quiz at the end?'),
(84, 52, 'Townhall agenda looks solid. Will it be recorded for APAC teams?'),

(85, 49, 'I’ll be in SG around that time. Let’s sync offline.'),
(86, 43, 'Thanks—please also share refundable fare options if possible.'),
(87, 44, 'Count me in. What’s the target pace?'),
(88, 57, 'Happy holidays! Thanks for all the support this year.'),

(89, 48, 'Can you share top crash stack traces for iOS?'),
(90, 42, 'SSO demand is real—let’s align with product for scope and timeline.'),
(91, 60, 'Monitoring dashboard link for post-release tracking would be helpful.'),
(92, 46, 'Top complaint #2 seems tied to onboarding—agree with proposed fix.'),

(93, 55, 'Creative refresh A/B plan approved. Please share the test matrix.'),
(94, 53, 'Any concerns about compliance/disclosure for UK influencers?'),
(95, 49, 'Great CPC reduction. Can we replicate this in KR market too?'),
(96, 41, 'Rollout plan looks safe. Suggest gradual ramp-up with guardrails.'),

(97, 58, 'Thanks—will update my calendar once confirmed.'),
(98, 50, 'Action items list is clear. Owners and deadlines are helpful.'),
(99, 52, 'Paperless is great. Please clarify exception cases for legal docs.'),
(100, 41, 'Self-eval template link? I couldn’t find it in the portal.'),

(101, 56, 'Lunch sounds good. What’s the address?'),
(102, 45, 'If found, please drop it at the front desk.'),
(103, 59, 'Can you share pricing tiers and contract terms for the discount?'),
(104, 42, 'Weekend works. Weather looks okay—let’s finalize time.'),

(105, 48, 'Agree—suggest triage tags by issue type to speed up routing.'),
(106, 54, 'NPS drivers are insightful. Let’s share with the onboarding squad.'),
(107, 60, 'SSO scope needs clarity: SAML vs OIDC first?'),
(108, 46, 'Feature nudges + email cadence could help. Let’s test incrementally.'),

(109, 41, 'Assets look great. Please confirm dimensions for each placement.'),
(110, 49, 'TikTok content calendar is solid—need localization owners.'),
(111, 43, 'Budget shift makes sense. Can we add expected ROI by channel?'),
(112, 53, 'Tracking metrics list is good. Please include attribution model assumption.'),

(113, 57, 'Construction schedule might affect meetings—any alternative rooms?'),
(114, 41, 'Welcome Sarah! Excited to collaborate.'),
(115, 56, 'Reminder received—will complete today.'),
(116, 60, 'Thanks for the notes. Can you add decision logs for the unresolved items?'),

(117, 44, 'Yes, it’s getting cold. Stay warm everyone!'),
(118, 45, 'Interested. Estimated budget and dates?'),
(119, 52, 'Add it to the team dinner list—looks great.'),
(120, 49, 'Tokyo tips helpful. Please share a shortlist of hotels near the office area.');


-- ==========================================
-- 4. 쪽지 데이터 (180개: 인당 3개씩 발송)
-- ==========================================
INSERT INTO message (id, company_id, employee_id, message_title, message_content)
VALUES (1, 1, 1, '업무 협조 요청', '보고서 확인 부탁드려요.'),
       (2, 1, 1, '미팅 일정 변경', '오후 4시로 변경.'),
       (3, 1, 1, '식당 예약', '12시 예약 완료.'),
       (4, 1, 2, '코드 리뷰', 'PR 확인 부탁드려요.'),
       (5, 1, 2, '서버 재시작', '완료되었습니다.'),
       (6, 1, 2, '간식 나눔', '탕비실 확인요.'),
       (7, 1, 3, '디자인 시안', '1차본 공유합니다.'),
       (8, 1, 3, '아이콘 수정', '완료되었습니다.'),
       (9, 1, 3, '폰트 정보', '라이선스 확인요.'),
       (10, 1, 4, '연차 결재', '승인 부탁드립니다.'),
       (11, 1, 4, '교육 신청', '오늘 마감입니다.'),
       (12, 1, 4, '증빙 제출', '영수증 첨부함.'),
       (13, 1, 5, '영업 보고', '최종 리포트입니다.'),
       (14, 1, 5, '잠재 고객', '리스트 공유합니다.'),
       (15, 1, 5, '미팅 약도', '지도 확인하세요.'),
       (16, 1, 6, '광고 소재', '이미지 전달합니다.'),
       (17, 1, 6, '예산 확인', '금액 확인 부탁요.'),
       (18, 1, 6, '성과 분석', '데이터 필요합니다.'),
       (19, 1, 7, '레이아웃', '수정 요청합니다.'),
       (20, 1, 7, '테스트 결과', '로그 첨부합니다.'),
       (21, 1, 7, 'QA 리스트', '확인 부탁드립니다.'),
       (22, 1, 8, '스터디 교재', 'PDF 파일입니다.'),
       (23, 1, 8, '장소 공지', '회의실 A입니다.'),
       (24, 1, 8, '과제 안내', '금주 과제입니다.'),
       (25, 1, 9, '보안 점검', '리스트 공유합니다.'),
       (26, 1, 9, '암호 변경', '주기적 변경 요청.'),
       (27, 1, 9, '접속 기록', '이상 로그 발견.'),
       (28, 1, 10, 'DB 튜닝', '성능 개선 결과.'),
       (29, 1, 10, '백업 안내', '스케줄 공유합니다.'),
       (30, 1, 10, '장애 조치', '완료 보고서입니다.'),
       (31, 1, 11, 'Redis 설정', '업데이트 완료.'),
       (32, 1, 11, '스택 회의', '일정 잡읍시다.'),
       (33, 1, 11, '라이선스', '검토 결과입니다.'),
       (34, 1, 12, '컨벤션', '문서 공유합니다.'),
       (35, 1, 12, '컴포넌트', '정리 완료함.'),
       (36, 1, 12, '속도 개선', '최적화 필요함.'),
       (37, 1, 13, '복지 포인트', '잔액 확인하세요.'),
       (38, 1, 13, '온보딩', '가이드 전달함.'),
       (39, 1, 13, '경조사', '공지 확인요.'),
       (40, 1, 14, '글로벌 미팅', '링크 전달함.'),
       (41, 1, 14, '환율 변동', '대응 필요함.'),
       (42, 1, 14, '최종 서명', '요청드립니다.'),
       (43, 1, 15, '이벤트 기획', '초안입니다.'),
       (44, 1, 15, '당첨자', '명단 공유함.'),
       (45, 1, 15, '협력사 번호', '전달드립니다.'),
       (46, 1, 16, '급여 안내', '명세서 발송.'),
       (47, 1, 16, '정산 서류', '누락 확인요.'),
       (48, 1, 16, '분기 목표', '공유드립니다.'),
       (49, 1, 17, '시설 보수', '조치 완료함.'),
       (50, 1, 17, '비품 구매', '목록 확인요.'),
       (51, 1, 17, '주차 안내', '방식 변경함.'),
       (52, 1, 18, '응시료 지원', '신청하세요.'),
       (53, 1, 18, '분석 특강', '일정 안내.'),
       (54, 1, 18, '구매 목록', '승인 바랍니다.'),
       (55, 1, 19, '동호회', '지원비 공지.'),
       (56, 1, 19, '설문 참여', '링크 공유함.'),
       (57, 1, 19, '워크숍', '장소 투표요.'),
       (58, 1, 20, '동료 칭찬', '피드백 공유.'),
       (59, 1, 20, '칭찬 배지', '수여 안내.'),
       (60, 1, 20, '팀 빌딩', '활동 결과.'),
-- 회사 2 데이터 (61~72 추가)
       (61, 2, 21, 'TS 신년 목표', '달성합시다.'),
       (62, 2, 21, '회의 자료', '첨부합니다.'),
       (63, 2, 21, 'IR 자료', '최종 확인.'),
       (64, 2, 22, 'LLM 개선', '속도 측정.'),
       (65, 2, 22, 'GPU 예약', '사용 알림.'),
       (66, 2, 22, '데이터 검수', '완료함.'),
       (67, 2, 23, '플랫폼 점검', '공지사항입니다.'),
       (68, 2, 23, 'API 문서', '최신화 완료.'),
       (69, 2, 24, '인프라 비용', '보고서 확인요.'),
       (70, 2, 24, '네트워크 장애', '복구 완료.'),
       (71, 2, 25, '모델 테스트', '결과 공유.'),
       (72, 2, 25, '학습 데이터', '추가 필요.'),
-- 회사 3 데이터
       (121, 3, 41, 'SEA Market', 'Plan doc.'),
       (122, 3, 41, 'Manager List', 'Contact list.'),
       (123, 3, 41, 'Goal', '10M users.');

-- ==========================================
-- 5. 쪽지 수신자 데이터 (INBOX / SEND 구조 반영)
-- 수신 기록은 모두 INBOX 폴더 타입으로 설정합니다.
-- ==========================================
INSERT INTO message_recipient (company_id, message_id, employee_id, is_read, message_folder_type)
VALUES
-- 회사 1 수신 데이터
(1, 1, 5, 0, 'INBOX'),
(1, 2, 12, 1, 'INBOX'),
(1, 3, 18, 0, 'INBOX'),
(1, 4, 1, 1, 'INBOX'),
(1, 5, 11, 0, 'INBOX'),
(1, 6, 20, 1, 'INBOX'),
(1, 7, 2, 0, 'INBOX'),
(1, 8, 9, 1, 'INBOX'),
(1, 9, 16, 0, 'INBOX'),
(1, 10, 3, 1, 'INBOX'),
(1, 11, 8, 0, 'INBOX'),
(1, 12, 14, 1, 'INBOX'),
(1, 13, 4, 0, 'INBOX'),
(1, 14, 7, 1, 'INBOX'),
(1, 15, 19, 0, 'INBOX'),
(1, 16, 10, 1, 'INBOX'),
(1, 17, 13, 0, 'INBOX'),
(1, 18, 1, 1, 'INBOX'),
(1, 19, 6, 0, 'INBOX'),
(1, 20, 15, 1, 'INBOX'),
(1, 21, 2, 0, 'INBOX'),
(1, 22, 5, 1, 'INBOX'),
(1, 23, 17, 0, 'INBOX'),
(1, 24, 3, 1, 'INBOX'),
(1, 25, 4, 0, 'INBOX'),
(1, 26, 12, 1, 'INBOX'),
(1, 27, 8, 0, 'INBOX'),
(1, 28, 9, 1, 'INBOX'),
(1, 29, 14, 0, 'INBOX'),
(1, 30, 20, 1, 'INBOX'),
(1, 31, 1, 0, 'INBOX'),
(1, 32, 6, 1, 'INBOX'),
(1, 33, 15, 0, 'INBOX'),
(1, 34, 2, 1, 'INBOX'),
(1, 35, 7, 0, 'INBOX'),
(1, 36, 11, 1, 'INBOX'),
(1, 37, 3, 0, 'INBOX'),
(1, 38, 10, 1, 'INBOX'),
(1, 39, 16, 0, 'INBOX'),
(1, 40, 4, 1, 'INBOX'),
(1, 41, 13, 0, 'INBOX'),
(1, 42, 17, 1, 'INBOX'),
(1, 43, 5, 0, 'INBOX'),
(1, 44, 18, 1, 'INBOX'),
(1, 45, 9, 0, 'INBOX'),
(1, 46, 6, 1, 'INBOX'),
(1, 47, 14, 0, 'INBOX'),
(1, 48, 12, 1, 'INBOX'),
(1, 49, 7, 0, 'INBOX'),
(1, 50, 19, 1, 'INBOX'),
(1, 51, 8, 0, 'INBOX'),
(1, 52, 8, 1, 'INBOX'),
(1, 53, 1, 0, 'INBOX'),
(1, 54, 15, 1, 'INBOX'),
(1, 55, 9, 0, 'INBOX'),
(1, 56, 2, 1, 'INBOX'),
(1, 57, 11, 0, 'INBOX'),
(1, 58, 10, 1, 'INBOX'),
(1, 59, 3, 0, 'INBOX'),
(1, 60, 4, 1, 'INBOX'),

-- 회사 2 수신 데이터
(2, 61, 30, 0, 'INBOX'),
(2, 62, 35, 1, 'INBOX'),
(2, 63, 25, 0, 'INBOX'),
(2, 64, 21, 1, 'INBOX'),
(2, 65, 33, 0, 'INBOX'),
(2, 66, 38, 1, 'INBOX'),
(2, 67, 22, 0, 'INBOX'),
(2, 68, 29, 1, 'INBOX'),
(2, 69, 36, 0, 'INBOX'),
(2, 70, 23, 1, 'INBOX'),
(2, 71, 28, 0, 'INBOX'),
(2, 72, 31, 1, 'INBOX'),

-- 회사 3 수신 데이터
(3, 121, 55, 0, 'INBOX'),
(3, 122, 50, 1, 'INBOX'),
(3, 123, 45, 0, 'INBOX');

