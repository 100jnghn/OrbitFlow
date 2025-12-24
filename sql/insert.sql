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
-- test1234!

/* =====================================================
   공통 템플릿: 회사 1세트 생성 (회사별로 변수만 다르게)
===================================================== */

/* =========================
   [COMPANY #1] OrbitFlow 본사
========================= */
INSERT INTO company (name, business_number, address, representative_name, representative_contact, created_at,
                     updated_at)
VALUES ('OrbitFlow 본사', '123-45-67890', '서울특별시 강남구 테헤란로 123', '김도윤', '010-1111-0001', NOW(), NOW())
ON DUPLICATE KEY UPDATE address                = VALUES(address),
                        representative_name    = VALUES(representative_name),
                        representative_contact = VALUES(representative_contact),
                        updated_at             = NOW();

SET @c1 := (SELECT id
            FROM company
            WHERE name = 'OrbitFlow 본사'
            LIMIT 1);

-- ORG_CATEGORY
INSERT INTO org_category (company_id, name, order_index)
VALUES (@c1, '회사', 1),
       (@c1, '본부', 2),
       (@c1, '부서', 3),
       (@c1, '팀', 4)
ON DUPLICATE KEY UPDATE order_index = VALUES(order_index),
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
SET parent_hr_rank_id=@c1_rank_staff
WHERE company_id = @c1
  AND name = '대리';
UPDATE hr_rank
SET parent_hr_rank_id=@c1_rank_asst
WHERE company_id = @c1
  AND name = '과장';
UPDATE hr_rank
SET parent_hr_rank_id=@c1_rank_mgr
WHERE company_id = @c1
  AND name = '차장';
UPDATE hr_rank
SET parent_hr_rank_id=@c1_rank_sr
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
ON DUPLICATE KEY UPDATE
                     internal_phone=VALUES(internal_phone),
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
   [COMPANY #2] TechSolution 테크솔루션
========================= */
INSERT INTO company (name, business_number, address, representative_name, representative_contact, created_at,
                     updated_at)
VALUES ('TechSolution 테크솔루션', '234-56-78901', '경기도 판교역로 456', '강태양', '010-2222-0001', NOW(), NOW())
ON DUPLICATE KEY UPDATE address                = VALUES(address),
                        representative_name    = VALUES(representative_name),
                        representative_contact = VALUES(representative_contact),
                        updated_at             = NOW();

SET @c2 := (SELECT id
            FROM company
            WHERE name = 'TechSolution 테크솔루션'
            LIMIT 1);

-- ORG_CATEGORY
INSERT INTO org_category (company_id, name, order_index)
VALUES (@c2, '회사', 1),
       (@c2, '본부', 2),
       (@c2, '부서', 3),
       (@c2, '팀', 4)
ON DUPLICATE KEY UPDATE order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

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
        WHERE company_id = @c2 AND parent_org_id = @c2_hq_platform AND name = '플랫폼운영부'
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
        WHERE company_id = @c2 AND parent_org_id = @c2_hq_platform AND name = '플랫폼운영부'
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
                               WHERE company_id = @c2 AND parent_org_id = @c2_hq_support AND name = '재무부'
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
SET parent_hr_rank_id=@c2_rank_staff
WHERE company_id = @c2
  AND name = '대리';
UPDATE hr_rank
SET parent_hr_rank_id=@c2_rank_asst
WHERE company_id = @c2
  AND name = '과장';
UPDATE hr_rank
SET parent_hr_rank_id=@c2_rank_mgr
WHERE company_id = @c2
  AND name = '차장';
UPDATE hr_rank
SET parent_hr_rank_id=@c2_rank_sr
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
VALUES ('GlobalService 글로벌서비스', '345-67-89012', '부산광역시 해운대구 센텀로 789', '신유진', '010-3333-0001', NOW(), NOW())
ON DUPLICATE KEY UPDATE address                = VALUES(address),
                        representative_name    = VALUES(representative_name),
                        representative_contact = VALUES(representative_contact),
                        updated_at             = NOW();

SET @c3 := (SELECT id
            FROM company
            WHERE name = 'GlobalService 글로벌서비스'
            LIMIT 1);

-- ORG_CATEGORY
INSERT INTO org_category (company_id, name, order_index)
VALUES (@c3, '회사', 1),
       (@c3, '본부', 2),
       (@c3, '부서', 3),
       (@c3, '팀', 4)
ON DUPLICATE KEY UPDATE order_index=VALUES(order_index),
                        is_active= TRUE,
                        updated_at=NOW();

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
        WHERE company_id = @c3 AND parent_org_id = @c3_hq_platform AND name = '플랫폼운영부'
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
        WHERE company_id = @c3 AND parent_org_id = @c3_hq_platform AND name = '플랫폼운영부'
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
                               WHERE company_id = @c3 AND parent_org_id = @c3_hq_support AND name = '재무부'
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
SET parent_hr_rank_id=@c3_rank_staff
WHERE company_id = @c3
  AND name = '대리';
UPDATE hr_rank
SET parent_hr_rank_id=@c3_rank_asst
WHERE company_id = @c3
  AND name = '과장';
UPDATE hr_rank
SET parent_hr_rank_id=@c3_rank_mgr
WHERE company_id = @c3
  AND name = '차장';
UPDATE hr_rank
SET parent_hr_rank_id=@c3_rank_sr
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

COMMIT;









#
# /* =====================================================
#    NOTIFICATION (알림)
# ===================================================== */
# INSERT INTO notification
#     (company_id, employee_id, type, content, is_read)
# VALUES
# -- OrbitFlow (company_id = 1)
# (1, 1, 'ORG', '백엔드팀 팀장으로 지정되었습니다.', FALSE),
# (1, 2, 'ORG', '백엔드팀에 배정되었습니다.', TRUE),
# (1, 3, 'ORG', '인사팀 팀장으로 지정되었습니다.', FALSE),
# (1, 2, 'SYSTEM', '비밀번호가 초기화되었습니다. 새 비밀번호로 변경해주세요.', TRUE),
# (1, 3, 'SYSTEM', '계정 권한이 변경되었습니다.', FALSE),
#
# -- NovaWorks (company_id = 2)
# (2, 4, 'ORG', 'AI팀 팀장으로 지정되었습니다.', FALSE),
# (2, 5, 'ORG', 'AI팀에 배정되었습니다.', FALSE),
# (2, 5, 'SYSTEM', '임시 계정이 생성되었습니다. 최초 로그인 후 비밀번호를 변경하세요.', FALSE),
# (2, 4, 'SYSTEM', '부서 책임자로 지정되었습니다.', TRUE);
#
#
# /* =====================================================
#    LOG_AUDIT (인사 감사 로그)
# ===================================================== */
# INSERT INTO log_audit
# (company_id, actor_employee_id, entity_type, entity_id,
#  event_type, before_data, after_data)
# VALUES
# -- OrbitFlow
# (1, 1, 'EMPLOYEE', 2, 'CREATE',
#  NULL,
#  JSON_OBJECT('name', '김철수', 'org', '백엔드팀', 'position', '백엔드팀원')),
#
# (1, 1, 'EMPLOYEE', 2, 'ASSIGN',
#  JSON_OBJECT('position', NULL),
#  JSON_OBJECT('position', '백엔드팀원')),
#
# (1, 1, 'EMPLOYEE', 1, 'ASSIGN',
#  JSON_OBJECT('position', NULL),
#  JSON_OBJECT('position', '백엔드팀장')),
#
# (1, 1, 'EMPLOYEE', 3, 'MOVE',
#  JSON_OBJECT('org', '인사부'),
#  JSON_OBJECT('org', '인사팀')),
#
# (1, 1, 'EMPLOYEE', 3, 'ASSIGN',
#  JSON_OBJECT('position', NULL),
#  JSON_OBJECT('position', '인사팀장')),
#
# -- NovaWorks
# (2, 4, 'EMPLOYEE', 5, 'CREATE',
#  NULL,
#  JSON_OBJECT('name', '정수빈', 'org', 'AI팀', 'status', 'TEMP')),
#
# (2, 4, 'EMPLOYEE', 5, 'ASSIGN',
#  JSON_OBJECT('position', NULL),
#  JSON_OBJECT('position', 'AI팀원')),
#
# (2, 4, 'EMPLOYEE', 4, 'ASSIGN',
#  JSON_OBJECT('position', NULL),
#  JSON_OBJECT('position', 'AI팀장')),
#
# (2, 4, 'EMPLOYEE', 5, 'SUSPENDED',
#  JSON_OBJECT('status', 'ACTIVE'),
#  JSON_OBJECT('status', 'SUSPENDED'));
#
#
#
# -- =========================================================
# -- LEAVE TYPE (휴가 유형)
# -- =========================================================
# INSERT INTO leave_type
#     (type_name, is_countable, description)
# VALUES ('연차', TRUE, '연 단위로 부여되며 차감되는 기본 유급 휴가'),
#        ('오전 반차', TRUE, '오전 근무 시간에 사용하는 반일 휴가'),
#        ('오후 반차', TRUE, '오후 근무 시간에 사용하는 반일 휴가'),
#        ('병가', TRUE, '질병 사유로 사용하는 휴가'),
#        ('공가', FALSE, '회사 공적인 사유로 부여되는 휴가'),
#        ('대체휴무', TRUE, '초과 근무에 따른 보상 휴가'),
#        ('무급휴가', FALSE, '급여 차감 대상 휴가');
#
#
# /* =========================================================
#  * TEMPLATE CATEGORY (GLOBAL)
#  * ========================================================= */
# INSERT INTO template_category (code, name)
# VALUES ('ATTENDANCE', '근태'),
#        ('SCHEDULE', '일정'),
#        ('GENERAL', '일반');
#
#
# /* =========================================================
#  * FORM TEMPLATE GROUP (10)
#  * ========================================================= */
# INSERT INTO form_template_group
#     (company_id, name, description, created_by)
# VALUES (1, '휴가 신청', '연차/반차/병가 신청', 1),
#        (1, '출장 보고', '출장 계획 및 결과 보고', 1),
#        (1, '초과근무 신청', '야근/주말근무 신청', 1),
#        (1, '비용 정산', '법인카드 및 비용 정산', 1),
#        (1, '장비 구매', '업무용 장비 구매 요청', 1),
#
#        (2, '휴가 신청', '연차/반차/병가 신청', 4),
#        (2, '출장 보고', '출장 계획 및 결과 보고', 4),
#        (2, '초과근무 신청', '야근/주말근무 신청', 4),
#        (2, '비용 정산', '법인카드 및 비용 정산', 4),
#        (2, '장비 구매', '업무용 장비 구매 요청', 4);
#
#
# /* =========================================================
#  * FORM TEMPLATE (10)
#  * ========================================================= */
# INSERT INTO form_template
# (company_id, template_group_id, version, template_category_id,
#  affect_tags, template_json, approval_rule_json, status)
# VALUES
# -- 회사 1
# (1, 1, 1, 1, JSON_ARRAY('ATTENDANCE'),
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'start_date'),
#          JSON_OBJECT('key', 'end_date'),
#          JSON_OBJECT('key', 'leave_type')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'MANAGER'),
#          JSON_OBJECT('order', 2, 'approverType', 'ADMIN')
#      )), 'ACTIVE'),
#
# (1, 2, 1, 2, JSON_ARRAY('SCHEDULE'),
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'destination'),
#          JSON_OBJECT('key', 'period')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
#      )), 'ACTIVE'),
#
# (1, 3, 1, 1, JSON_ARRAY('ATTENDANCE'),
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'date'),
#          JSON_OBJECT('key', 'hours')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'MANAGER')
#      )), 'ACTIVE'),
#
# (1, 4, 1, 3, NULL,
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'amount'),
#          JSON_OBJECT('key', 'reason')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
#      )), 'ACTIVE'),
#
# (1, 5, 1, 3, NULL,
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'item'),
#          JSON_OBJECT('key', 'price')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
#      )), 'ACTIVE'),
#
# -- 회사 2
# (2, 6, 1, 1, JSON_ARRAY('ATTENDANCE'),
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'start_date'),
#          JSON_OBJECT('key', 'end_date'),
#          JSON_OBJECT('key', 'leave_type')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'MANAGER'),
#          JSON_OBJECT('order', 2, 'approverType', 'ADMIN')
#      )), 'ACTIVE'),
#
# (2, 7, 1, 2, JSON_ARRAY('SCHEDULE'),
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'destination'),
#          JSON_OBJECT('key', 'period')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
#      )), 'ACTIVE'),
#
# (2, 8, 1, 1, JSON_ARRAY('ATTENDANCE'),
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'date'),
#          JSON_OBJECT('key', 'hours')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'MANAGER')
#      )), 'ACTIVE'),
#
# (2, 9, 1, 3, NULL,
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'amount'),
#          JSON_OBJECT('key', 'reason')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
#      )), 'ACTIVE'),
#
# (2, 10, 1, 3, NULL,
#  JSON_OBJECT('fields', JSON_ARRAY(
#          JSON_OBJECT('key', 'item'),
#          JSON_OBJECT('key', 'price')
#      )),
#  JSON_OBJECT('lines', JSON_ARRAY(
#          JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
#      )), 'ACTIVE');
#
#
# /* =========================================================
#  * FORM TEMPLATE AI LOG (company_id 포함)
#  * ========================================================= */
# INSERT INTO log_form_template_ai
# (company_id, template_group_id, created_template_id, prompt,
#  generated_template_json, generated_rule_json,
#  model, status, created_by)
# VALUES
# -- company_id = 1
# (1, 1, 1, '연차 신청 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
# (1, 2, 2, '출장 보고 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
# (1, 3, 3, '야근 신청 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
# (1, 4, 4, '비용 정산 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
# (1, 5, 5, '장비 구매 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
#
# -- company_id = 2
# (2, 6, 6, '연차 신청 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4),
# (2, 7, 7, '출장 보고 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4),
# (2, 8, 8, '야근 신청 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4),
# (2, 9, 9, '비용 정산 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4),
# (2, 10, 10, '장비 구매 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4);
#
#
# /* =========================================================
#  * DOCUMENT (10)
#  * ========================================================= */
# INSERT INTO document
# (company_id, template_group_id, template_version,
#  writer_id, title, status)
# VALUES (1, 1, 1, 2, '연차 신청', 'SUBMITTED'),
#        (1, 2, 1, 2, '출장 보고', 'IN_PROGRESS'),
#        (1, 3, 1, 3, '야근 신청', 'SUBMITTED'),
#        (1, 4, 1, 2, '비용 정산 요청', 'DRAFT'),
#        (1, 5, 1, 3, '장비 구매 요청', 'SUBMITTED'),
#
#        (2, 6, 1, 4, '연차 신청', 'SUBMITTED'),
#        (2, 7, 1, 4, '출장 보고', 'IN_PROGRESS'),
#        (2, 8, 1, 5, '야근 신청', 'SUBMITTED'),
#        (2, 9, 1, 4, '비용 정산 요청', 'DRAFT'),
#        (2, 10, 1, 5, '장비 구매 요청', 'SUBMITTED');
#
#
# /* =========================================================
#  * DOCUMENT CONTENT (10)
#  * ========================================================= */
# INSERT INTO document_content (document_id, content_json)
# VALUES (1, JSON_OBJECT('start_date', '2025-01-05', 'end_date', '2025-01-07')),
#        (2, JSON_OBJECT('destination', '부산', 'period', '2박3일')),
#        (3, JSON_OBJECT('date', '2025-01-10', 'hours', 4)),
#        (4, JSON_OBJECT('amount', 120000, 'reason', '식대')),
#        (5, JSON_OBJECT('item', '노트북', 'price', 1500000)),
#
#        (6, JSON_OBJECT('start_date', '2025-02-05', 'end_date', '2025-02-07')),
#        (7, JSON_OBJECT('destination', '대전', 'period', '1박2일')),
#        (8, JSON_OBJECT('date', '2025-02-10', 'hours', 3)),
#        (9, JSON_OBJECT('amount', 98000, 'reason', '회의비')),
#        (10, JSON_OBJECT('item', '모니터', 'price', 420000));
#
#
# /* =========================================================
#  * APPROVAL LINE (10)
#  * ========================================================= */
# INSERT INTO approval_line
#     (document_id, company_id, approver_id, order_no, status)
# VALUES (1, 1, 1, 1, 'WAITING'),
#        (1, 1, 1, 2, 'WAITING'),
#        (2, 1, 1, 1, 'WAITING'),
#        (3, 1, 1, 1, 'APPROVED'),
#        (4, 1, 1, 1, 'WAITING'),
#
#        (6, 2, 4, 1, 'WAITING'),
#        (6, 2, 4, 2, 'WAITING'),
#        (7, 2, 4, 1, 'WAITING'),
#        (8, 2, 4, 1, 'APPROVED'),
#        (9, 2, 4, 1, 'WAITING');
#
#
# /* =========================================================
#  * DOCUMENT AI SUMMARY
#  * ========================================================= */
# INSERT INTO document_ai_summary
# (document_id, company_id, summary_type, content, model, before_document_id)
# VALUES (1, 1, 'CONTENT', '연차 신청 요약', 'gpt-4', NULL),
#        (2, 1, 'CONTENT', '출장 보고 요약', 'gpt-4', NULL),
#        (3, 1, 'CONTENT', '야근 신청 요약', 'gpt-4', NULL),
#        (4, 1, 'CONTENT', '비용 정산 요약', 'gpt-4', NULL),
#        (5, 1, 'CONTENT', '장비 구매 요약', 'gpt-4', NULL),
#
#        (6, 2, 'CONTENT', '연차 신청 요약', 'gpt-4', NULL),
#        (7, 2, 'CONTENT', '출장 보고 요약', 'gpt-4', NULL),
#        (8, 2, 'CONTENT', '야근 신청 요약', 'gpt-4', NULL),
#        (9, 2, 'CONTENT', '비용 정산 요약', 'gpt-4', NULL),
#        (10, 2, 'CONTENT', '장비 구매 요약', 'gpt-4', NULL);
#
#
# /* =========================================================
#  * ATTENDANCE RECORD (10)
#  * ========================================================= */
# INSERT INTO attendance_record
# (employee_id, company_id, start_date, end_date,
#  days, type_id, reason, source_document_id, status)
# VALUES
# -- Company 1
# (2, 1, '2025-01-05', '2025-01-07', 3.0, 1, '연차', 1, 'IN_PROGRESS'),
# (3, 1, '2025-01-10', '2025-01-10', 0.5, 2, '반차', 3, 'APPROVED'),
# (2, 1, '2025-01-15', '2025-01-15', 1.0, 1, '연차', 1, 'DRAFT'),
# (3, 1, '2025-01-20', '2025-01-21', 2.0, 1, '연차', 1, 'SUBMITTED'),
# (2, 1, '2025-01-25', '2025-01-25', 1.0, 1, '연차', 1, 'APPROVED'),
# -- Company 2
# (4, 2, '2025-02-05', '2025-02-07', 3.0, 1, '연차', 6, 'IN_PROGRESS'),
# (5, 2, '2025-02-10', '2025-02-10', 0.5, 3, '반차', 8, 'APPROVED'),
# (4, 2, '2025-02-15', '2025-02-15', 1.0, 1, '연차', 6, 'DRAFT'),
# (5, 2, '2025-02-20', '2025-02-21', 2.0, 1, '연차', 6, 'SUBMITTED'),
# (4, 2, '2025-02-25', '2025-02-25', 1.0, 1, '연차', 6, 'APPROVED');
#
#
# /* =========================================================
#  * FILE (10)
#  * ========================================================= */
# INSERT INTO file
# (company_id, object_key, origin_file, sys_file,
#  content_type, file_size, created_by)
# VALUES (1, 'docs/leave.pdf', '연차신청서.pdf', 'f001.pdf', 'application/pdf', 102400, 2),
#        (1, 'docs/trip.pdf', '출장보고서.pdf', 'f002.pdf', 'application/pdf', 204800, 2),
#        (1, 'docs/overtime.pdf', '야근신청서.pdf', 'f003.pdf', 'application/pdf', 51200, 3),
#        (1, 'docs/expense.pdf', '정산서.pdf', 'f004.pdf', 'application/pdf', 102400, 2),
#        (1, 'docs/laptop.pdf', '구매요청서.pdf', 'f005.pdf', 'application/pdf', 307200, 3),
#
#        (2, 'docs2/leave.pdf', '연차신청서.pdf', 'f101.pdf', 'application/pdf', 102400, 4),
#        (2, 'docs2/trip.pdf', '출장보고서.pdf', 'f102.pdf', 'application/pdf', 204800, 4),
#        (2, 'docs2/overtime.pdf', '야근신청서.pdf', 'f103.pdf', 'application/pdf', 51200, 5),
#        (2, 'docs2/expense.pdf', '정산서.pdf', 'f104.pdf', 'application/pdf', 102400, 4),
#        (2, 'docs2/laptop.pdf', '구매요청서.pdf', 'f105.pdf', 'application/pdf', 307200, 5);
#
#
# /* =========================================================
#  * DOCUMENT FILE (10)
#  * ========================================================= */
# INSERT INTO document_file
#     (document_id, file_id, reference_type)
# VALUES (1, 1, 'ATTACHMENT'),
#        (2, 2, 'ATTACHMENT'),
#        (3, 3, 'ATTACHMENT'),
#        (4, 4, 'ATTACHMENT'),
#        (5, 5, 'ATTACHMENT'),
#        (6, 6, 'ATTACHMENT'),
#        (7, 7, 'ATTACHMENT'),
#        (8, 8, 'ATTACHMENT'),
#        (9, 9, 'ATTACHMENT'),
#        (10, 10, 'ATTACHMENT');
#
#
# /* =========================================================
#  * FILE (EMPLOYEE SIGNATURE IMAGE)
#  * ========================================================= */
# INSERT INTO file
# (company_id, object_key, origin_file, sys_file,
#  content_type, file_size, created_by)
# VALUES
# -- OrbitFlow
# (1, 'signatures/employee/1/sign.png', 'hong_signature.png', 'sig_001.png',
#  'image/png', 20480, 1), -- file_id = 1 (홍길동)
#
# (1, 'signatures/employee/2/sign.png', 'kim_signature.png', 'sig_002.png',
#  'image/png', 19800, 2), -- file_id = 2 (김철수)
#
# (1, 'signatures/employee/3/sign.png', 'lee_signature.png', 'sig_003.png',
#  'image/png', 21500, 3), -- file_id = 3 (이영희)
#
# -- NovaWorks
# (2, 'signatures/employee/4/sign.png', 'park_signature.png', 'sig_006.png',
#  'image/png', 22300, 4), -- file_id = 6 (박민수)
#
# (2, 'signatures/employee/5/sign.png', 'jung_signature.png', 'sig_007.png',
#  'image/png', 20100, 5); -- file_id = 7 (정수빈)
#
#
# INSERT INTO employee_signature
#     (company_id, employee_id, file_id, is_active)
# VALUES (1, 1, 11, TRUE), -- 홍길동 서명
#        (1, 2, 12, TRUE), -- 김철수 서명
#        (1, 3, 13, TRUE), -- 이영희 서명
#        (2, 4, 14, TRUE), -- 박민수 서명
#        (2, 5, 15, TRUE);
# -- 정수빈 서명
#
#
# INSERT INTO document_signature
# (document_id, approval_line_id, company_id, signer_id, signature_file_id)
# VALUES
# -- 문서 3 (야근 신청) 승인
# (3, 4, 1, 1, 1),
#
# -- 문서 8 (야근 신청 - NovaWorks)
# (8, 9, 2, 4, 15);
#
#
#
# /* =========================================================
#    BOARD / MESSAGE INSERT DATA
# ========================================================= */
#
# -- board_category
# -- board_category
# INSERT INTO board_category
# (company_id, organization_id, board_name, board_type, is_activated, comment_activated)
# VALUES
#     -- 회사 1 : 공용 게시판
#     (1, NULL, '전사 공지사항', 'FREE', 1, 1),
#     (1, NULL, '자유 게시판', 'FREE', 1, 1),
#     (1, NULL, '전사 자료실', 'FREE', 1, 0),
#     (1, NULL, '주간 업무 공유', 'FREE', 1, 1),
#     (1, NULL, '경조사 게시판', 'FREE', 1, 1),
#     -- 회사 1 : 조직 게시판
#     (1, 8, '인사팀 게시판', 'FREE', 1, 1),
#     (1, 6, '백엔드팀 게시판', 'FREE', 1, 1),
#
#     -- 회사 2 : 공용 게시판
#     (2, NULL, '전사 공지사항', 'FREE', 1, 1),
#     (2, NULL, '전사 건의 게시판', 'FREE', 1, 0),
#     (2, NULL, '기술 Q&A', 'FREE', 1, 1),
#     (2, NULL, '운영 매뉴얼', 'FREE', 1, 0),
#     (2, NULL, '전사 자료실', 'FREE', 1, 0),
#     -- 회사 2 : 조직 게시판
#     (2, 12, 'AI팀 게시판', 'FREE', 1, 1),
#     (2, 11, '플랫폼부 게시판', 'FREE', 1, 1);
#
# -- board
# INSERT INTO board (board_category_id, employee_id, board_title, board_content, view_count, file_id)
# VALUES
#     -- 회사 1 : 공용
#     (1, 1, '공지: 시스템 점검 안내', '내일 새벽 2시부터 4시까지 전사 시스템 점검이 예정되어 있습니다.', 530, NULL),
#     (2, 2, '주말 영화 추천 요청', '주말에 볼 만한 영화 추천 부탁드립니다.', 125, NULL),
#     (5, 1, '직원 결혼 소식', '이번 달 우리 회사 직원 결혼 소식입니다. 많은 축하 부탁드립니다.', 880, NULL),
#     (4, 3, '이번 주 업무 공유', '각 팀별 이번 주 주요 업무 내용을 공유해주세요.', 20, 4),
#     (2, 2, '회사 근처 맛집 공유', '회사 근처 점심 맛집 리스트 공유합니다.', 450, NULL),
#     -- 회사 1: 조직
#     (6, 4, '2025년 인사 평가 일정 안내', '상반기 인사 평가 일정 공유드립니다.', 120, NULL),
#     (6, 4, '연차 사용 가이드', '연차 사용 기준 및 프로세스 안내', 95, 6),
#     (7, 5, 'API 성능 개선 논의', 'DB 인덱스 개선 관련 논의합니다.', 80, NULL),
#     (7, 5, '배포 자동화 제안', 'CI/CD 개선 제안서 공유', 60, 8),
#
#     -- 회사 2: 공용
#     (8, 4, '공지: 서버 점검 안내', '금주 토요일 새벽 서버 점검이 진행될 예정입니다.', 410, NULL),
#     (9, 5, '복지 제도 개선 제안', '식대 및 복지 포인트 확대를 제안합니다.', 300, NULL),
#     (10, 4, 'API 응답 속도 관련 질문', '특정 API 호출 시 지연이 발생하는 원인이 궁금합니다.', 80, NULL),
#     (11, 5, '신규 직원 온보딩 매뉴얼', '신규 입사자 온보딩 절차 문서입니다.', 50, 8),
#     (9, 4, '사무실 환경 개선 요청', '회의실 예약 시스템 개선이 필요합니다.', 210, NULL),
#     -- 회사 2 : 조직
#     (13, 4, '모델 성능 리포트', '12월 모델 정확도 리포트 공유', 150, NULL),
#     (13, 4, '데이터 전처리 기준', '전처리 표준 가이드 공유', 90, NULL),
#     (14, 5, '서버 증설 계획', '트래픽 증가 대응 서버 증설 계획', 110, NULL),
#     (14, 5, '장애 대응 프로세스', '플랫폼 장애 대응 절차 정리', 70, NULL);
#
# # -- board_permission
# # INSERT INTO board_permission
# #     (employee_id, board_category_id)
# # VALUES (2, 1),
# #        (1, 3),
# #        (3, 2),
# #        (1, 4),
# #        (2, 5),
# #
# #        (4, 6),
# #        (5, 7),
# #        (4, 8),
# #        (5, 9),
# #        (4, 10);
#
# -- comment
# INSERT INTO message
#     (company_id, employee_id, message_title, message_content, file_id)
# VALUES (1, 1, '회의 자료 요청', '회의 자료를 오늘 오후 5시까지 보내주세요.', NULL),
#        (1, 2, '휴가 승인 요청', '내일 오전 반차 승인 부탁드립니다.', NULL),
#        (1, 3, '부서 이동 문의', '인사팀 TO 여부 문의드립니다.', NULL),
#        (1, 1, '회식 장소 추천', '금요일 회식 장소 추천 부탁드립니다.', NULL),
#        (1, 2, '커피 쿠폰 전달', '팀원들에게 커피 쿠폰 전달드립니다.', NULL),
#
#        (2, 4, '신규 프로젝트 안내', 'AI 프로젝트 착수 안내드립니다.', 6),
#        (2, 5, '사원증 재발급', '사원증 재발급 요청드립니다.', NULL),
#        (2, 4, '주간 보고 요청', '월요일까지 주간 보고 제출 바랍니다.', NULL),
#        (2, 5, '휴게실 문의', '냉장고 사용 규정 문의드립니다.', NULL),
#        (2, 4, '파일 암호 전달', '첨부 파일 암호 전달드립니다.', 7);
#
#
# -- message
# INSERT INTO message
#     (company_id, employee_id, message_title, message_content, file_id)
# VALUES (1, 1, '회의 자료 요청', '회의 자료를 오늘 오후 5시까지 보내주세요.', NULL),
#        (1, 2, '휴가 승인 요청', '내일 오전 반차 승인 부탁드립니다.', NULL),
#        (1, 3, '부서 이동 문의', '인사팀 TO 여부 문의드립니다.', NULL),
#        (1, 1, '회식 장소 추천', '금요일 회식 장소 추천 부탁드립니다.', NULL),
#        (1, 2, '커피 쿠폰 전달', '팀원들에게 커피 쿠폰 전달드립니다.', NULL),
#
#        (2, 4, '신규 프로젝트 안내', 'AI 프로젝트 착수 안내드립니다.', 6),
#        (2, 5, '사원증 재발급', '사원증 재발급 요청드립니다.', NULL),
#        (2, 4, '주간 보고 요청', '월요일까지 주간 보고 제출 바랍니다.', NULL),
#        (2, 5, '휴게실 문의', '냉장고 사용 규정 문의드립니다.', NULL),
#        (2, 4, '파일 암호 전달', '첨부 파일 암호 전달드립니다.', 7);
#
# -- message_recipient
# INSERT INTO message_recipient
#     (company_id, message_id, employee_id, is_read, read_at)
# VALUES (1, 1, 2, 0, NULL),
#        (1, 2, 1, 1, NOW()),
#        (1, 3, 1, 0, NULL),
#        (1, 4, 3, 1, NOW()),
#        (1, 5, 3, 1, NOW()),
#
#        (2, 6, 5, 1, NOW()),
#        (2, 7, 4, 0, NULL),
#        (2, 8, 5, 0, NULL),
#        (2, 9, 4, 1, NOW()),
#        (2, 10, 5, 0, NULL);
#
#
# -- =========================================================
# -- RESOURCE STATUS
# -- =========================================================
# -- =========================================================
# -- 1. RESOURCE STATUS (가장 먼저 실행)
# -- =========================================================
# -- ID 매핑: 1=AVAILABLE, 2=INSPECTION, 3=UNAVAILABLE, 4=DELETED, 5=ETC
# INSERT INTO resource_status (status_code, status_name)
# VALUES ('AVAILABLE', '사용 가능'),
#        ('INSPECTION', '점검 중'),
#        ('UNAVAILABLE', '사용 불가'),
#        ('DELETED', '삭제됨'),
#        ('ETC', '기타');
#
# -- =========================================================
# -- 2. RESERVATION STATUS (가장 먼저 실행)
# -- =========================================================
# -- ID 매핑: 1=PENDING, 2=CONFIRM, 3=REJECT, 4=CANCELED, 5=DELETED, 6=ETC
# INSERT INTO reservation_status (status_code, status_name)
# VALUES ('PENDING', '승인 대기'),
#        ('CONFIRM', '예약 확정'),
#        ('REJECT', '예약 반려'),
#        ('CANCELED', '예약 취소'),
#        ('DELETED', '삭제됨'),
#        ('ETC', '기타');
#
# -- =========================================================
# -- 3. ITEM CATEGORY (Company 1)
# -- =========================================================
# INSERT INTO item_category (company_id, name)
# VALUES (1, '카메라'), -- id = 1
#        (1, '모니터'), -- id = 2
#        (1, '노트북'), -- id = 3
#        (1, '카드 단말기');
# -- id = 4
#
# -- =========================================================
# -- 4. MEETING ROOM (Company 1)
# -- =========================================================
# INSERT INTO meetingroom
#     (company_id, name, position, description, resource_status_id)
# VALUES (1, '중회의실 B', '본관 2층 203호', '12인 수용, TV 모니터 구비', 1),
#        (1, '중회의실 C', '본관 2층 204호', '팀 회의 전용, 원형 테이블', 1),
#        (1, '화상 회의실 2', '본관 5층 501호', '화상회의 전용 장비 구축', 1),
#        (1, '프로젝트 룸 Beta', '별관 3층 302호', '단기 TF팀 사용 공간', 2),
#        (1, '교육실 1', '본관 지하 1층', '사내 교육 및 세미나용', 1),
#        (1, '교육실 2', '본관 지하 2층', '신입사원 교육 전용', 1),
#        (1, '미팅 부스 A', '본관 6층', '2인용 미팅 부스', 1),
#        (1, '미팅 부스 B', '본관 6층', '전화 및 화상 미팅용', 1),
#        (1, '임원 회의실', '본관 9층', '임원 전용 회의 공간', 3),
#        (1, '아이디어 스튜디오', '별관 1층', '자유 토론 및 기획 공간', 1);
#
#
# -- =========================================================
# -- 5. CAR (Company 1)
# -- =========================================================
# INSERT INTO car
# (company_id, number, name, driver_age, description, resource_status_id, file_id)
# VALUES (1, '11가1111', 'K5', 26, '영업팀 공용 차량', 1, NULL),
#        (1, '22나2222', '쏘나타 DN8', 26, '외근 및 출장용', 1, NULL),
#        (1, '33다3333', '투싼 NX4', 26, '팀 단위 이동용', 1, NULL),
#        (1, '44라4444', '스포티지', 21, '근거리 업무용', 2, NULL),
#        (1, '55마5555', 'EV6', 21, '전기차 시범 운영', 1, NULL),
#        (1, '66바6666', '봉고3', 26, '물품 운반용 차량', 1, NULL),
#        (1, '77사7777', '스타렉스', 26, '대규모 인원 이동용', 2, NULL),
#        (1, '88아8888', '제네시스 GV70', 30, '임원 공용 SUV', 1, NULL),
#        (1, '99자9999', '코나 EV', 21, '친환경 차량', 1, NULL),
#        (1, '10차1010', 'BMW 520i', 30, '외부 VIP 미팅용', 3, NULL);
#
#
# -- =========================================================
# -- 6. ITEM (Company 1)
# -- =========================================================
# INSERT INTO item
# (company_id, item_category_id, name, description, resource_status_id, file_id)
# VALUES (1, 3, 'MacBook Air 15 (M3)', '기획팀 지급용 노트북', 1, NULL),
#        (1, 3, 'MacBook Pro 14 (M3)', '개발팀 공용 테스트 장비', 1, NULL),
#        (1, 2, '삼성 스마트 모니터 M8', '회의실 공용 디스플레이', 1, NULL),
#        (1, 2, 'LG 듀얼업 모니터', '개발자 세로형 모니터', 1, NULL),
#        (1, 4, '윈도우 노트북 A', '단기 파견 인력 대여용', 2, NULL),
#        (1, 4, '윈도우 노트북 B', '외부 협력사 대여용', 3, NULL),
#        (1, 1, '후지 x100v', '스냅샷 촬영용', 1, NULL),
#        (1, 1, '캐논 550d', '영상 촬영용', 1, NULL),
#        (1, 2, '고성능 모니터', '진짜 좋음', 1, NULL),
#        (1, 3, 'LG 그램', '신규 입사자 지급용', 1, NULL);
#
#
# -- =========================================================
# -- 7. RESERVATION (Company 1)
# -- =========================================================
# -- reservation_status_id: 1=PENDING, 2=CONFIRM, 3=REJECT
# INSERT INTO reservation
# (company_id, employee_id, type_code, item_category_id, resource_id,
#  reservation_date, start_time, end_time,
#  reservation_reason, reject_reason, reservation_status_id)
# VALUES
# -- 회의실 예약 (resource_id=1, CONFIRM=2)
# (1, 1, 'MEETING', NULL, 1,
#  CURDATE() + INTERVAL 1 DAY, 10, 12,
#  '주간 개발 팀 정기 회의', NULL, 2),
# -- 차량 예약 (resource_id=2, PENDING=1)
# (1, 1, 'CAR', NULL, 2,
#  CURDATE() + INTERVAL 2 DAY, 9, 18,
#  '외부 파트너사 미팅 및 장비 운송', NULL, 1),
# -- 비품 예약 (노트북, category_id=4, resource_id=3, CONFIRM=2)
# (1, 1, 'ITEM', 4, 3,
#  CURDATE() + INTERVAL 3 DAY, 13, 17,
#  '모바일 앱 신규 기능 QA 테스트', NULL, 2),
# -- 회의실 예약 반려 (resource_id=4, REJECT=3)
# (1, 1, 'MEETING', NULL, 4,
#  CURDATE() + INTERVAL 4 DAY, 14, 15,
#  '신규 기획 브레인스토밍', '해당 시간대 시설 긴급 점검 예정', 3),
# -- 비품 예약 (모니터, category_id=2, resource_id=4, CONFIRM=2)
# (1, 1, 'ITEM', 2, 4,
#  CURDATE() + INTERVAL 5 DAY, 9, 18,
#  '사내 스터디 발제 준비를 위한 대여', NULL, 2);
#
# -- =========================================================
# -- 8. SCHEDULE (Company 1)
# -- =========================================================
# INSERT INTO schedule
# (company_id, category_id, org_id, type, employee_id, schedule_title, schedule_description, start_date, end_date,
#  start_time, end_time, schedule_status)
# VALUES
# -- [COMPANY] 전사 일정 (창립기념일)
# (1, NULL, NULL, 'COMPANY', 1, '창립기념일 행사', '전사 휴무 및 오전 기념식 진행',
#  CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 10 DAY, 9, 12, 'RELEASE'),
# -- [COMPANY] 전사 일정 (타운홀 미팅)
# (1, NULL, NULL, 'COMPANY', 1, '4분기 전사 타운홀 미팅', '분기 실적 발표 및 Q&A 세션',
#  CURDATE() + INTERVAL 5 DAY, CURDATE() + INTERVAL 5 DAY, 14, 16, 'RELEASE'),
# -- [PERSONAL] 개인 일정 (연차)
# (1, NULL, NULL, 'PERSONAL', 1, '개인 연차 휴가', '가족 여행',
#  CURDATE() + INTERVAL 3 DAY, CURDATE() + INTERVAL 4 DAY, 9, 18, 'RELEASE'),
# -- [PERSONAL] 개인 일정 (병원)
# (1, NULL, NULL, 'PERSONAL', 2, '건강검진', '오전 반차 사용 예정',
#  CURDATE() + INTERVAL 7 DAY, CURDATE() + INTERVAL 7 DAY, 8, 13, 'RELEASE'),
# -- [PERSONAL] 개인 일정 (미팅) - DELETED
# (1, NULL, NULL, 'PERSONAL', 3, '외부 멘토링 세션', '멘토님 사정으로 취소됨',
#  CURDATE() + INTERVAL 1 DAY, CURDATE() + INTERVAL 1 DAY, 19, 21, 'DELETED');
#
# -- =========================================================
# -- 9. SCHEDULE SUMMARY (Company 1)
# -- =========================================================
# INSERT INTO schedule_summary
#     (company_id, employee_id, week_summary, month_summary)
# VALUES (1, 1,
#         '12월 3주차 주간 요약: 이번 주는 전사 창립기념일 행사와 개발팀 주간 스프린트 회의가 주요 일정이었습니다.',
#         '12월 월간 요약: 인증/인가 모듈(Spring Security) 개발 완료 및 주요 프로젝트 진행.');
#
#
# -- =========================================================
# -- [추가] ITEM CATEGORY (Company 2)
# -- =========================================================
# -- auto_increment로 인해 5, 6, 7, 8, 9번 ID 할당 가정
# INSERT INTO item_category (company_id, name)
# VALUES (2, '태블릿'),   -- id = 5
#        (2, '서버 장비'), -- id = 6
#        (2, '촬영 장비'), -- id = 7
#        (2, '사무 가구'), -- id = 8
#        (2, '소프트웨어');
# -- id = 9
#
# -- =========================================================
# -- [추가] MEETING ROOM (Company 2)
# -- =========================================================
# INSERT INTO meetingroom
#     (company_id, name, position, description, resource_status_id)
# VALUES (2, '전략 회의실', '글로벌 센터 501호', '임원 전용 회의실, 최고급 화상 장비', 1),     -- AVAILABLE
#        (2, '아이디어 랩', '글로벌 센터 3층 휴게공간', '전면 화이트보드 벽면, 자유로운 분위기', 1), -- AVAILABLE
#        (2, '세미나실 B', '글로벌 센터 지하 1층', '50인 수용 가능, 강연대 및 마이크 설비', 2), -- INSPECTION
#        (2, '화상 미팅룸 1', '글로벌 센터 505호', '1인용 포커스 룸, 방음 부스', 1),       -- AVAILABLE
#        (2, '프로젝트 룸 Alpha', '글로벌 센터 401호', 'TF팀 전용 장기 대관실', 3);
# -- UNAVAILABLE
#
#
# -- =========================================================
# -- [추가] CAR (Company 2)
# -- =========================================================
# INSERT INTO car
# (company_id, number, name, driver_age, description, resource_status_id, file_id)
# VALUES (2, '99호1111', '벤츠 E-Class', 30, 'CEO 의전 차량', 1, NULL),           -- AVAILABLE
#        (2, '88하2222', '쏘렌토 MQ4', 26, '영업 1팀 공용 차량', 1, NULL),            -- AVAILABLE
#        (2, '77호3333', '레이 EV', 21, '근거리 문서 수발신 및 마트 장보기용', 2, NULL),     -- INSPECTION
#        (2, '66하4444', '스타리아 라운지', 26, '공항 픽업 및 바이어 접대용 (7인승)', 1, NULL), -- AVAILABLE
#        (2, '55호5555', '테슬라 Model Y', 26, 'IT 사업부 테스트 및 업무용', 3, NULL);
# -- UNAVAILABLE
#
#
# -- =========================================================
# -- [추가] ITEM (Company 2)
# -- =========================================================
# -- category_id는 위에서 생성된 순서(5,6,7,8,9) 매핑
# INSERT INTO item
# (company_id, item_category_id, name, description, resource_status_id, file_id)
# VALUES (2, 5, 'iPad Pro 12.9 (6세대)', '디자인 시안 검토용 태블릿', 1, NULL),
#        (2, 6, 'Dell PowerEdge R750', '사내 테스트 서버, 전산실 B구역 위치', 1, NULL),
#        (2, 7, 'Sony Alpha 7 IV', '마케팅팀 유튜브 촬영용 카메라', 2, NULL),
#        (2, 8, '허먼밀러 에어론', '허리 디스크 환자 전용 의자 (신청 필요)', 3, NULL),
#        (2, 9, 'JetBrains All Products', '개발팀 공용 라이선스 계정', 1, NULL);
#
# -- =========================================================
# -- [추가] RESERVATION (Company 2)
# -- =========================================================
# -- Resource ID 가정: Meetingroom(6~10), Car(6~10), Item(5~9)
# -- Status ID: 1=PENDING, 2=CONFIRM, 3=REJECT
# INSERT INTO reservation
# (company_id, employee_id, type_code, item_category_id, resource_id,
#  reservation_date, start_time, end_time,
#  reservation_reason, reject_reason, reservation_status_id)
# VALUES
# -- 회의실 예약 (전략 회의실: resource_id=6, CONFIRM=2)
# (2, 4, 'MEETING', NULL, 6,
#  CURDATE() + INTERVAL 2 DAY, 14, 16,
#  '내년도 사업 계획 보고', NULL, 2),
# -- 차량 예약 (쏘렌토: resource_id=7, PENDING=1)
# (2, 5, 'CAR', NULL, 7,
#  CURDATE() + INTERVAL 3 DAY, 9, 18,
#  '지방 공장 실사 방문', NULL, 1),
# -- 비품 예약 (iPad: resource_id=5, category_id=5, CONFIRM=2)
# (2, 4, 'ITEM', 5, 5,
#  CURDATE() + INTERVAL 1 DAY, 10, 18,
#  '외부 미팅 시안 프레젠테이션', NULL, 2),
# -- 회의실 예약 (반려됨, resource_id=7, REJECT=3)
# (2, 5, 'MEETING', NULL, 7,
#  CURDATE() + INTERVAL 5 DAY, 13, 15,
#  '점심 회식 후 티타임', '업무 외 목적 사용 불가', 3),
# -- 비품 예약 (카메라: resource_id=7, category_id=7, CONFIRM=2)
# (2, 5, 'ITEM', 7, 7,
#  CURDATE() + INTERVAL 10 DAY, 9, 18,
#  '사내 브이로그 촬영', NULL, 2);
#
# -- =========================================================
# -- [추가] SCHEDULE (Company 2)
# -- =========================================================
# INSERT INTO schedule
# (company_id, category_id, org_id, type, employee_id, schedule_title, schedule_description, start_date, end_date,
#  start_time, end_time, schedule_status)
# VALUES
# -- [COMPANY] 전사 일정
# (2, NULL, NULL, 'COMPANY', 4, '전사 체육대회', '잠실 보조경기장 집결',
#  CURDATE() + INTERVAL 20 DAY, CURDATE() + INTERVAL 20 DAY, 9, 18, 'RELEASE'),
# -- [COMPANY] 전사 일정
# (2, NULL, NULL, 'COMPANY', 4, '건강검진 기간 안내', '지정 병원 예약 필수',
#  CURDATE(), CURDATE() + INTERVAL 30 DAY, 9, 18, 'RELEASE'),
# -- [PERSONAL] 개인 일정 (사원 4)
# (2, NULL, NULL, 'PERSONAL', 4, '재택 근무', '집중 근무일 신청',
#  CURDATE() + INTERVAL 2 DAY, CURDATE() + INTERVAL 2 DAY, 9, 18, 'RELEASE'),
# -- [PERSONAL] 개인 일정 (사원 5)
# (2, NULL, NULL, 'PERSONAL', 5, '치과 예약', '오후 반차 사용',
#  CURDATE() + INTERVAL 5 DAY, CURDATE() + INTERVAL 5 DAY, 14, 18, 'RELEASE'),
# -- [PERSONAL] 개인 일정 (사원 5 - 삭제됨)
# (2, NULL, NULL, 'PERSONAL', 5, '저녁 약속', '친구 결혼식 뒤풀이 (취소됨)',
#  CURDATE() + INTERVAL 7 DAY, CURDATE() + INTERVAL 7 DAY, 19, 22, 'DELETED');
#
# -- =========================================================
# -- [추가] SCHEDULE SUMMARY (Company 2)
# -- =========================================================
# INSERT INTO schedule_summary
#     (company_id, employee_id, week_summary, month_summary)
# VALUES (2, 4, '12월 2주차: 전사 체육대회 준비 위원회 참석 및 재택 근무 진행.', '12월 요약: 체육대회 기획 완료 및 개인 업무 목표 90% 달성.'),
#        (2, 5, '12월 2주차: 지방 공장 실사 및 보고서 작성 완료.', '12월 요약: 현장 방문 일정 위주로 소화, 건강 문제로 인한 반차 사용 1회.');
#
# -- 1. attendance_rule (근태 규칙)
# INSERT INTO attendance_rule (company_id, name, default_start_time, default_end_time, default_break_minutes,
#                              late_threshold_min, early_leave_threshold_min, is_default)
# VALUES (1, '표준 9-6 근무', '09:00:00', '18:00:00', 60, 10, 10, TRUE),
#        (1, '유연 근무 A (10-7)', '10:00:00', '19:00:00', 60, 15, 15, FALSE);
#
#
# -- 2. employee_att_rule (직원 예외 규칙)
# INSERT INTO employee_att_rule (company_id, employee_id, start_time, end_time, break_minutes, reason, valid_from,
#                                valid_to, applied_at, is_active)
# VALUES (1, 2, '10:00:00', '19:00:00', 60, '개인 사유로 인한 유연 근무 신청', CURDATE() - INTERVAL 15 DAY,
#         CURDATE() + INTERVAL 15 DAY, NOW(), TRUE),
#        (1, 3, '09:30:00', '18:30:00', 30, '단기 프로젝트 투입으로 인한 시간 조정', CURDATE(), CURDATE() + INTERVAL 30 DAY, NOW(),
#         TRUE),
#        (1, 2, NULL, NULL, 90, '육아 시간 조정 (휴게 90분 적용)', CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 40 DAY,
#         NOW(), TRUE);
#
#
# -- 3. attendance (근태 기록)
# -- 기존 테스트 데이터와 충돌을 방지하려면 아래 주석을 해제하고 실행하세요.
# -- DELETE FROM attendances WHERE employee_id = 3 AND work_date BETWEEN '2025-12-01' AND '2025-12-20';
#
# INSERT INTO attendance
# (company_id, employee_id, work_date, commute_at, leave_at, status, is_corrected)
# VALUES (1, 3, '2025-12-01', '2025-12-01 08:52:00', '2025-12-01 18:05:00', 'ON_TIME', 0),
#        (1, 3, '2025-12-02', '2025-12-02 09:15:00', '2025-12-02 18:10:00', 'LATE', 0),
#        (1, 3, '2025-12-03', '2025-12-03 08:45:00', '2025-12-03 18:00:00', 'ON_TIME', 0),
#        (1, 3, '2025-12-04', NULL, NULL, 'ABSENT', 0),      -- 결근
#        (1, 3, '2025-12-05', '2025-12-05 08:58:00', '2025-12-05 18:30:00', 'ON_TIME', 0),
#        (1, 3, '2025-12-06', NULL, NULL, 'BEFORE_WORK', 0), -- 주말/출근 전
#        (1, 3, '2025-12-07', NULL, NULL, 'BEFORE_WORK', 0),
#        (1, 3, '2025-12-08', '2025-12-08 09:20:00', '2025-12-08 18:00:00', 'LATE', 0),
#        (1, 3, '2025-12-09', '2025-12-09 08:50:00', '2025-12-09 18:05:00', 'ON_TIME', 0),
#        (1, 3, '2025-12-10', '2025-12-10 08:59:00', '2025-12-10 18:15:00', 'ON_TIME', 0),
# -- 2페이지 데이터 (10개 단위 페이징 테스트용)
#        (1, 3, '2025-12-11', '2025-12-11 09:05:00', '2025-12-11 18:00:00', 'LATE', 0),
#        (1, 3, '2025-12-12', '2025-12-12 08:40:00', '2025-12-12 18:05:00', 'ON_TIME', 0),
#        (1, 3, '2025-12-13', NULL, NULL, 'ABSENT', 0),
#        (1, 3, '2025-12-14', NULL, NULL, 'ABSENT', 0),
#        (1, 3, '2025-12-15', '2025-12-15 08:50:00', '2025-12-15 18:10:00', 'ON_TIME', 0),
#        (1, 3, '2025-12-16', '2025-12-16 09:12:00', '2025-12-16 18:05:00', 'LATE', 0),
#        (1, 3, '2025-12-17', '2025-12-17 08:55:00', '2025-12-17 18:00:00', 'ON_TIME', 0),
#        (1, 3, '2025-12-18', NULL, NULL, 'ABSENT', 0),
#        (1, 3, '2025-12-19', '2025-12-19 08:58:00', '2025-12-19 18:05:00', 'ON_TIME', 0),
#        (1, 3, '2025-12-20', '2025-12-20 09:02:00', '2025-12-20 18:10:00', 'LATE', 0);
#
# -- 4. correction_history (근태 정정 이력)
# INSERT INTO correction_history (attendance_id, original_commute_at, original_leave_at, corrected_commute_at,
#                                 corrected_leave_at, correction_reason, correction_status, processed_by, processed_at,
#                                 rejection_reason)
# VALUES (4, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 09:00:00'), NULL, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 09:00:00'),
#         CONCAT(CURDATE() - INTERVAL 1 DAY, ' 18:10:00'), '퇴근 시 카드 태그 누락', 'APPROVED', 1, NOW(), NULL),
#        (2, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 18:00:00'),
#         CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 17:30:00'),
#         '긴급 치과 방문으로 인한 조퇴', 'PENDING', NULL, NULL, NULL),
#        (1, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 10:05:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 19:00:00'),
#         CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 19:00:00'), '출근 기록 오류',
#         'REJECTED', 1, NOW(), '지각 기록 명확함');
#
#
# -- 5. leave_balance (휴가 잔여 일수)
# INSERT INTO leave_balance (company_id, employee_id, year, total_granted, remaining_days)
# VALUES (1, 1, 2026, 15.00, 15.00),
#        (1, 2, 2026, 15.00, 12.50),
#        (1, 3, 2026, 10.00, 9.00);
#
#
# -- 7. manual_category (매뉴얼 카테고리)
# INSERT INTO manual_category (company_id, category_name, description, is_active, sort_order)
# VALUES (1, '근태 관리', '출퇴근, 휴가, 재택 정책 안내', TRUE, 10),
#        (1, '결재/증명서', '전자결재 사용법 및 양식 가이드', TRUE, 20),
#        (1, '자원 관리', '회의실, 차량, 비품 예약 정책', TRUE, 30),
#        (1, '급여/복지', '급여일, 상여금, 복지 제도 안내', TRUE, 40),
#        (1, '신규 입사자', '온보딩 가이드 및 필수 정보', TRUE, 50),
#        (1, 'IT 가이드', '사내 시스템 및 보안 정책', TRUE, 60);
#
#
# -- 8. manual_link (매뉴얼 연결)
# -- File ID (1~5)는 이전 섹션의 더미 데이터를 가정합니다.
# INSERT INTO manual_link (file_id, category_id, company_id, status, is_active, vectorized_at)
# VALUES (1, 1, 1, 'READY', TRUE, NOW()),
#        (2, 2, 1, 'READY', TRUE, NOW()),
#        (3, 3, 1, 'READY', TRUE, NOW()),
#        (4, 4, 1, 'FAILED', TRUE, NULL),
#        (5, 5, 1, 'PROCESSING', TRUE, NULL),
#        (5, 1, 1, 'UPLOADED', FALSE, NULL);
#
#
# -- 9. grant_history (연차 부여 이력)
# INSERT INTO grant_history (employee_id, company_id, grant_date, granted_days, grant_type, expiration_date, is_expired,
#                            created_at)
# VALUES (2, 1, '2026-01-01', 15.00, 'ANNUAL', '2027-01-31', FALSE, NOW()),
#        (3, 1, '2026-01-01', 10.00, 'ANNUAL', '2027-01-31', FALSE, NOW()),
#        (2, 1, '2025-07-01', 5.00, 'PERIODIC', '2026-06-30', FALSE, NOW()),
#        (3, 1, '2024-01-01', 15.00, 'ANNUAL', '2025-01-31', TRUE, NOW()),
#        (1, 1, '2026-01-01', 15.00, 'ANNUAL', '2027-01-31', FALSE, NOW()),
#        (1, 1, '2025-03-01', 3.00, 'COMPENSATION', '2026-03-31', FALSE, NOW());