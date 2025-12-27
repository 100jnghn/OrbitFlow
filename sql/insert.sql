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


-- 종훈 --
use orbitflow;

INSERT INTO schedule
(company_id, is_company, org_category_id, org_id, employee_id,
 schedule_title, schedule_description,
 start_at, end_at, schedule_status)
VALUES
-- 1. 개인 일정
(1, false, NULL, NULL, 1,
 '개인 업무 정리', '주간 업무 정리 및 회고',
 '2025-01-06 09:00:00', '2025-01-06 10:00:00', 'RELEASE'),

(1, false, NULL, NULL, 1,
 '병원 방문', '정기 건강검진',
 '2025-01-07 15:00:00', '2025-01-07 17:00:00', 'RELEASE'),

-- 3. 백엔드팀
(1, false, @c1_cat_team, @c1_team_be, 1,
 '백엔드팀 스크럼', '주간 스크럼 회의',
 '2025-01-06 10:00:00', '2025-01-06 10:30:00', 'RELEASE'),

(1, false, @c1_cat_team, @c1_team_be, 1,
 'API 설계 회의', '신규 API 설계 논의',
 '2025-01-08 14:00:00', '2025-01-08 16:00:00', 'RELEASE'),

-- 5. 프론트엔드팀
(1, false, @c1_cat_team, @c1_team_fe, 1,
 '프론트엔드 기획 회의', 'UI/UX 개선 논의',
 '2025-01-09 13:00:00', '2025-01-09 15:00:00', 'RELEASE'),

-- 6. 개발부
(1, false, @c1_cat_dept, @c1_dept_dev, 1,
 '개발부 월간 회의', '월간 개발 현황 공유',
 '2025-01-10 10:00:00', '2025-01-10 12:00:00', 'RELEASE'),

-- 7. 플랫폼본부
(1, false, @c1_cat_hq, @c1_hq_platform, 1,
 '플랫폼본부 전략회의', '분기 전략 수립',
 '2025-01-13 13:00:00', '2025-01-13 16:00:00', 'RELEASE'),

-- 8. 인사부
(1, false, @c1_cat_dept, @c1_dept_hr, 1,
 '인사 정책 회의', '평가 제도 개선 논의',
 '2025-01-14 10:00:00', '2025-01-14 12:00:00', 'RELEASE'),

-- 9. 인사기획팀
(1, false, @c1_cat_team, @c1_team_hrp, 1,
 '인사기획팀 회의', '조직 개편 검토',
 '2025-01-15 14:00:00', '2025-01-15 15:00:00', 'RELEASE'),

-- 10. 서비스기획팀
(1, false, @c1_cat_team, @c1_team_plan, 1,
 '서비스 로드맵 회의', '상반기 로드맵 논의',
 '2025-01-16 10:00:00', '2025-01-16 13:00:00', 'RELEASE'),

-- 11. 영업전략팀
(1, false, @c1_cat_team, @c1_team_sales, 1,
 '영업 전략 회의', '신규 고객 전략',
 '2025-01-20 10:00:00', '2025-01-20 12:00:00', 'RELEASE'),

-- 12. CS팀
(1, false, @c1_cat_team, @c1_team_cs, 1,
 'CS 이슈 회의', '주요 고객 이슈 공유',
 '2025-01-21 14:00:00', '2025-01-21 15:30:00', 'RELEASE'),

-- 13. 개인 일정
(1, false, NULL, NULL, 1,
 '외부 미팅', '외부 파트너 미팅',
 '2025-01-22 13:00:00', '2025-01-22 15:00:00', 'RELEASE'),

-- 14. 재무부
(1, false, @c1_cat_dept, @c1_dept_fin, 1,
 '예산 검토 회의', '분기 예산 검토',
 '2025-01-23 10:00:00', '2025-01-23 12:00:00', 'RELEASE'),

-- 15. 서비스본부
(1, false, @c1_cat_hq, @c1_hq_service, 1,
 '서비스본부 회의', '서비스 품질 개선',
 '2025-01-24 13:00:00', '2025-01-24 15:00:00', 'RELEASE'),

-- 16. 영업본부
(1, false, @c1_cat_hq, @c1_hq_sales, 1,
 '영업본부 주간회의', '영업 실적 공유',
 '2025-01-27 10:00:00', '2025-01-27 11:00:00', 'RELEASE'),

-- 17. 개인 일정
(1, false, NULL, NULL, 1,
 '자기계발 시간', '기술 서적 독서',
 '2025-01-28 17:00:00', '2025-01-28 19:00:00', 'RELEASE'),

-- 18. 전사 일정
(1, true, @c1_cat_company, @c1_root, 1,
 '전사 타운홀 미팅', 'CEO 타운홀',
 '2025-01-29 10:00:00', '2025-01-29 12:00:00', 'RELEASE'),

-- 19. 개인 일정 (하루 종일)
(1, false, NULL, NULL, 1,
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



INSERT INTO leave_type (type_name, is_countable, description)
VALUES ('연차', true, '법정 유급 연차 휴가'),
       ('오전반차', true, '09:00 ~ 13:00 사용'),
       ('오후반차', true, '14:00 ~ 18:00 사용'),
       ('병가', false, '질병 또는 부상으로 인한 휴무');


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
INSERT INTO attendance_record (employee_id, company_id, start_date, end_date, days, type_id, status, approved_at,
                               created_at)
VALUES
-- 이준호: 11월 7일(1일), 12월 12일(1일) 연차 사용
(10, 1, '2025-11-07', '2025-11-07', 1.0, 1, 'APPROVED', '2025-11-05 15:00:00', '2025-11-04 10:00:00'),
(10, 1, '2025-12-12', '2025-12-12', 1.0, 1, 'APPROVED', '2025-12-10 11:00:00', '2025-12-09 13:00:00'),
-- 김나연: 12월 24일 오후 반차 사용
(11, 1, '2025-12-24', '2025-12-24', 0.5, 3, 'APPROVED', '2025-12-23 17:00:00', '2025-12-23 09:30:00'),
-- 한지민: 12월 15일 오전 반차 사용
(15, 1, '2025-12-15', '2025-12-15', 0.5, 2, 'APPROVED', '2025-12-14 10:00:00', '2025-12-13 14:00:00'),
-- 오세훈: 11월 20일~21일 이틀 연차 사용
(16, 1, '2025-11-20', '2025-11-21', 2.0, 1, 'APPROVED', '2025-11-18 16:00:00', '2025-11-15 11:00:00');



INSERT INTO attendance (company_id, employee_id, work_date, commute_at, leave_at, status, applied_rule_id, is_corrected)
VALUES (1, 10, '2025-11-03', '2025-11-03 08:52:00', '2025-11-03 18:05:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-11-04', '2025-11-04 09:12:00', '2025-11-04 18:10:00', 'LATE', 1, 0),
       (1, 10, '2025-11-05', '2025-11-05 08:50:00', '2025-11-05 18:02:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-11-06', '2025-11-06 09:05:00', '2025-11-06 18:00:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-11-07', NULL, NULL, 'ABSENT', 1, 0),
       (1, 10, '2025-11-10', '2025-11-10 08:45:00', '2025-11-10 18:30:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-11-11', '2025-11-11 08:59:00', '2025-11-11 18:01:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-11-12', '2025-11-12 09:15:00', '2025-11-12 18:05:00', 'LATE', 1, 0),
       (1, 10, '2025-11-13', '2025-11-13 08:50:00', '2025-11-13 18:00:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-11-14', '2025-11-14 08:30:00', '2025-11-14 18:00:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-11-17', '2025-11-17 09:40:00', '2025-11-17 19:00:00', 'LATE', 1, 0),
       (1, 10, '2025-11-18', NULL, NULL, 'ABSENT', 1, 0),
       (1, 10, '2025-11-19', '2025-11-19 08:55:00', '2025-11-19 18:10:00', 'ON_TIME', 1, 1),
       (1, 10, '2025-12-15', '2025-12-15 08:50:00', '2025-12-15 18:00:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-12-16', '2025-12-16 09:05:30', '2025-12-16 18:05:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-12-17', '2025-12-17 08:40:20', '2025-12-17 18:10:11', 'ON_TIME', 1, 0),
       (1, 10, '2025-12-18', '2025-12-18 09:45:00', '2025-12-18 18:50:00', 'LATE', 1, 0),
       (1, 10, '2025-12-19', '2025-12-19 08:55:00', '2025-12-19 18:01:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-12-22', '2025-12-22 08:58:00', '2025-12-22 18:05:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-12-23', '2025-12-23 09:03:00', '2025-12-23 18:00:00', 'ON_TIME', 1, 0),
       (1, 10, '2025-12-25', '2025-12-25 09:00:00', '2025-12-25 18:00:00', 'ON_TIME', 1, 1),
       (1, 10, '2025-12-26', '2025-12-26 08:50:00', '2025-12-26 18:10:00', 'ON_TIME', 1, 0);



/* 최종 완결판 더미 데이터 스크립트
   - 대상: 회사 1(1-20), 회사 2(21-40), 회사 3(41-60) 전 직원
   - 내용: 1인당 게시글 2개, 댓글 2개, 쪽지 2개(발신) 하드코딩
   - 제약: board_category_id는 조회문을 통해 외래키 오류 방지
*/

-- ==========================================
-- 1. 카테고리 데이터 (ID: 1 ~ 20번 생성 가정)
-- ==========================================
-- 1~10: OrbitFlow(C1), 11~16: TechSolution(C2), 17~20: GlobalService(C3)
INSERT INTO board_category (company_id, organization_id, board_name, board_type, is_activated, comment_activated)
VALUES (1, NULL, '전사 공지사항', 'FREE', 1, 1),
       (1, NULL, '자유 게시판', 'FREE', 1, 1),
       (1, NULL, '사내 건의함', 'FREE', 1, 1),
       (1, NULL, '칭찬 게시판', 'FREE', 1, 1),
       (1, NULL, '자기계발/스터디', 'FREE', 1, 1),
       (1, 1, '백엔드 기술 공유', 'FREE', 1, 1),
       (1, 2, '프론트엔드 위키', 'FREE', 1, 1),
       (1, 3, 'HR 행정 지원', 'FREE', 1, 1),
       (1, 4, '영업 전략 및 성과', 'FREE', 1, 1),
       (1, 5, '마케팅 에셋 보관함', 'FREE', 1, 1),
       (2, NULL, 'TS 공지사항', 'FREE', 1, 1),
       (2, NULL, '기술 공유 라운지', 'FREE', 1, 1),
       (2, NULL, '사내 중고 장터', 'FREE', 1, 1),
       (2, 6, 'AI 모델 연구소', 'FREE', 1, 1),
       (2, 7, '플랫폼부', 'FREE', 1, 1),
       (2, 8, '인프라팀', 'FREE', 1, 1),
       (3, NULL, 'Global Notice', 'FREE', 1, 1),
       (3, NULL, 'General Forum', 'FREE', 1, 1),
       (3, 9, 'Customer Feedback', 'FREE', 1, 1),
       (3, 10, 'Global Ad Campaign', 'FREE', 1, 1);

-- ==========================================
-- 2. 게시글 데이터 (120개)
-- ==========================================
-- 회사 1 (ID 1~20번 직원)
INSERT INTO board (board_category_id, employee_id, board_title, board_content, view_count)
VALUES (1, 1, '2025 상반기 타운홀 미팅 안내', '전사 성과 공유를 위한 미팅 안내입니다.', 50),
       (2, 1, '오늘 점심 파스타 어때요?', '로비 근처 새로 생긴 곳 가보고 싶네요.', 12),
       (6, 2, 'Java 21 가상 스레드 도입기', '처리 성능이 20% 향상되었습니다.', 45),
       (2, 2, '주말 등산 모임 구합니다', '북한산 가볍게 다녀오실 분?', 8),
       (7, 3, 'Next.js 14 서버 액션 활용법', '새로운 데이터 페칭 패턴을 공유합니다.', 33),
       (2, 3, '기계식 키보드 입문 추천', '저소음 적축이 사무실에서 쓰기 좋네요.', 21),
       (8, 4, '연차 보상비 지급 안내', '이번 달 급여에 포함되어 지급됩니다.', 100),
       (3, 4, '카페테리아 원두 교체 건의', '산미 없는 고소한 원두로 바꿔주세요.', 15),
       (9, 5, '오빗전자 계약 수주 성공', '영업팀 모두의 노력으로 큰 계약을 따냈습니다.', 88),
       (2, 5, '퇴근 후 치맥 하실 분?', '강남역 근처에서 번개 모임 합니다.', 10),
       (10, 6, '1월 광고 집행 리포트', '유튜브 광고 도달률이 목표치를 상회했습니다.', 42),
       (2, 6, '요즘 볼만한 넷플릭스 추천', '퇴근하고 정주행할 거 찾고 있어요.', 18),
       (4, 7, '프론트팀 김영희님 감사합니다', '급한 수정 건 빠르게 도와주셔서 감사합니다.', 55),
       (2, 7, '에어팟 잃어버리신 분?', '3층 탕비실에서 습득했습니다.', 30),
       (5, 8, '파이썬 데이터 스터디 모집', '판다스 라이브러리 기초부터 같이 공부해요.', 22),
       (2, 8, '건강검진 예약 꿀팁', '연초에 해야 사람도 적고 쾌적하네요.', 14),
       (1, 9, '사내 보안 교육 이수 요청', '이번 주 금요일까지 전 직원 필수입니다.', 77),
       (2, 9, '노트북 스탠드 추천해요', '거북목 방지에 이만한 게 없네요.', 25),
       (4, 10, '인프라팀 박철수님 고생하셨습니다', '주말 서버 점검 덕분에 무사히 넘겼네요.', 60),
       (2, 10, '사무실 근처 맛집 지도', '제가 직접 다 가보고 정리한 리스트입니다.', 52),
       (6, 11, 'Redis 캐시 전략 가이드', 'Write-through 방식 도입 주의사항 정리.', 28),
       (2, 11, '비타민 영양제 추천', '피곤할 땐 종합비타민이 최고네요.', 13),
       (7, 12, 'Tailwind CSS 컨벤션', '클래스 순서 정리 규칙을 공유합니다.', 31),
       (2, 12, '주말 캠핑 가시는 분?', '이번엔 강원도로 떠납니다.', 11),
       (8, 13, '육아휴직 복직 프로세스', '복직 전 상담 및 필요 서류 정리입니다.', 40),
       (2, 13, '회사 근처 요가 학원', '점심시간 활용하기 딱 좋아요.', 16),
       (9, 14, '글로벌 시장 영업 동향', '미국 시장 수요 예측 보고서 요약.', 44),
       (2, 14, '슬램덩크 영화 다시 보기', '다시 봐도 감동이네요.', 10),
       (10, 15, '브랜드 가이드라인 v2.0', '로고 사용 및 폰트 규정이 업데이트 되었습니다.', 38),
       (2, 15, '미세먼지 조심하세요', '공기가 안 좋네요. 마스크 필수입니다.', 12),
       (4, 16, '경영팀 김미나님 감사합니다', '법인카드 한도 증액 신속 처리 감사합니다.', 50),
       (2, 16, '독서 모임 모집', '한 달에 책 한 권 같이 읽어요.', 19),
       (3, 17, '화장실 핸드타월 교체 주기', '오후에는 금방 떨어지네요. 보충 부탁드려요.', 24),
       (2, 17, '러닝 크루 모집', '화요일 퇴근 후 사옥 근처 한 바퀴!', 22),
       (5, 18, 'AWS 자격증 취득 후기', 'SAA 시험 준비하시는 분들 팁 드립니다.', 37),
       (2, 18, '여행지 추천해주세요', '일본이랑 베트남 중 고민 중입니다.', 28),
       (1, 19, '임직원 명절 선물 설문', '올해는 어떤 품목이 좋을까요?', 110),
       (2, 19, '당근마켓 나눔', '사용감 적은 모니터 스탠드 드립니다.', 45),
       (4, 20, '인사팀 박지수님 칭찬합니다', '신규 복지 제도 설명 너무 친절했어요.', 58),
       (2, 20, '오늘 날씨 너무 좋네요', '창밖만 봐도 힐링되는 하루입니다.', 23);

-- 회사 2 (ID 21~40번 직원)
INSERT INTO board (board_category_id, employee_id, board_title, board_content, view_count)
VALUES (11, 21, 'TS 신년 경영 비전 발표', '기술력으로 글로벌 1위를 달성합시다.', 90),
       (12, 21, '쿠버네티스 오토스케일링 적용', 'HPA 설정 최적화 가이드입니다.', 45),
       (13, 22, '로지텍 마우스 팔아요', '3개월 썼는데 깨끗합니다. 싸게 드려요.', 15),
       (14, 22, 'LLM 파인튜닝 실험 결과', '도메인 특화 학습 시 정확도가 향상되었습니다.', 52),
       (15, 23, '공통 API 게이트웨이 변경', '인증 방식이 OAuth2로 강화되었습니다.', 38),
       (11, 23, '사무실 자리 재배치 안내', '다음 주 주말에 대규모 이동이 있습니다.', 70),
       (16, 24, '클라우드 비용 절감 방안', '유휴 자원 정리로 월 15% 비용 절감.', 35),
       (12, 24, 'Grafana 대시보드 공유', '실시간 서버 상태 모니터링용 템플릿.', 22),
       (13, 25, '기계식 키보드 나눔', '오래됐지만 작동 잘 됩니다. 필요하신 분?', 20),
       (14, 25, 'Vision 모델 성능 개선', '객체 인식 속도 개선 사례 공유.', 44),
       (11, 26, '임직원 건강검진 캠페인', '대상자분들은 기한 내 예약 바랍니다.', 55),
       (15, 26, '로깅 라이브러리 v3 배포', '성능 향상 및 가독성이 개선되었습니다.', 29),
       (16, 27, 'DB 샤딩 도입 검토 보고', '데이터 증가에 따른 확장성 확보 전략.', 40),
       (12, 27, 'Git 컨벤션 통일 제안', '커밋 메시지 규칙을 하나로 정해봅시다.', 18),
       (13, 28, '데스크패드 팝니다', '새 제품인데 사이즈가 안 맞네요.', 12),
       (14, 28, 'Stable Diffusion 활용법', '사내 디자인 업무 효율화 방안 연구.', 36),
       (15, 29, 'UI 컴포넌트 라이브러리', '피그마와 매칭되는 리액트 컴포넌트 배포.', 48),
       (16, 29, '네트워크 보안 강화 조치', '외부 접속 시 VPN 필수 사용 적용.', 51),
       (12, 30, 'Go 언어 동시성 패턴', 'Channel과 Goroutine 효율적 활용법.', 25),
       (11, 30, '동호회 활동 지원 안내', '5인 이상 모임 시 활동비 지원됩니다.', 62),
       (14, 31, '데이터 라벨링 효율화', '자동 라벨링 툴 도입 후 속도 3배 향상.', 30),
       (12, 31, '파이썬 FastAPI 도입 후기', '기존 장고 대비 생산성 및 성능 비교.', 22),
       (15, 32, '모바일 웹 앱 최적화', '라이트하우스 점수 90점 달성 노하우.', 34),
       (13, 32, '스타벅스 기프티콘 교환', '다른 브랜드로 교환 원해요.', 11),
       (16, 33, 'K8s 트러블슈팅 사례', 'Ingress 설정 오류 해결기.', 39),
       (11, 33, '명절 귀성비 지원 공지', '이번 추석 귀성비가 지급될 예정입니다.', 120),
       (12, 34, '테스트 자동화 구축', 'Playwright를 이용한 E2E 테스트 자동화.', 27),
       (14, 34, 'RLHF 개념 정리', '강화 학습을 이용한 모델 튜닝 원리.', 21),
       (15, 35, '디자인 시스템 v1.5', '다크 모드 컬러 팔레트가 추가되었습니다.', 33),
       (13, 35, '아이패드 에어 4세대', '충전기 포함 풀박스입니다.', 50),
       (16, 36, 'Zabbix 모니터링 연동', '슬랙으로 실시간 장애 알림 연동 완료.', 28),
       (12, 36, 'Rust 입문 가이드', 'C++ 개발자가 본 Rust의 장단점.', 19),
       (11, 37, '주말 사옥 방역 안내', '토요일 09시부터 전체 방역 실시합니다.', 44),
       (14, 37, 'OCR 모델 성능 비교', 'Tesseract vs 사내 모델 성능 테스트.', 25),
       (15, 38, '공통 컴포넌트 사용 가이드', 'Select 및 Modal 컴포넌트 활용법.', 22),
       (13, 38, '캠핑 의자 팝니다', '가벼운 의자입니다.', 16),
       (16, 39, 'Docker 보안 취약점 점검', '이미지 스캐닝 결과 및 조치 사항.', 31),
       (12, 39, 'Kafka 스트림즈 활용', '실시간 데이터 파이프라인 구축 사례.', 26),
       (11, 40, '법정 의무 교육 미이수자 확인', '오늘 마감입니다. 확인 부탁드려요.', 88),
       (14, 40, '생성 AI 저작권 동향', '최신 가이드라인 정리.', 42);

-- 회사 3 (ID 41~60번 직원)
INSERT INTO board (board_category_id, employee_id, board_title, board_content, view_count)
VALUES (17, 41, 'Q1 Global Strategy', 'We aim to expand our service in SEA region.', 150),
       (18, 41, 'Any soccer fans here?', 'Lets watch the game together tonight.', 30),
       (19, 42, 'App Review Analysis', 'Users like the new UI but report some bugs.', 60),
       (18, 42, 'New cafe open nearby', 'The coffee there is amazing.', 25),
       (20, 43, 'YouTube Ad Performance', 'CTR increased by 15% in India market.', 40),
       (17, 43, 'Holiday schedule in US office', 'Please check the vacation dates.', 90),
       (19, 44, 'CS Response Time Goal', 'We need to reduce it below 2 hours.', 33),
       (18, 44, 'Lost my sunglasses', 'Did anyone see them in the lobby?', 12),
       (20, 45, 'New Banner Designs', 'Visual assets for Summer Sale is ready.', 55),
       (18, 45, 'Flight ticket deals', 'I found a great price for Tokyo!', 41),
       (17, 46, 'Office Renovation Plan', 'Starting next month on 5th floor.', 82),
       (19, 46, 'B2B Client Feedback', 'High demand for custom dashboard features.', 49),
       (20, 47, 'Social Media Strategy', 'Focusing on TikTok for younger audience.', 37),
       (18, 47, 'Gym membership discount', 'Group discount available for us.', 22),
       (17, 48, 'New Expense Policy', 'Updates on travel budget and receipts.', 110),
       (19, 48, 'User Satisfaction Survey', 'Our NPS reached 70 last month.', 53),
       (20, 49, 'Influencer Marketing List', 'Selected 5 influencers for UK market.', 44),
       (18, 49, 'Weather is getting cold', 'Take care of your health everyone.', 15),
       (17, 50, 'Security Awareness Month', 'Watch the training video by Friday.', 77),
       (19, 50, 'Retention Rate Improvement', 'Ideas on how to keep users active.', 38),
       (20, 51, 'Q4 Budget Allocation', 'Shifting more funds to video ads.', 46),
       (18, 51, 'Running club meeting', 'Join us for a 5k run tomorrow.', 21),
       (17, 52, 'New Hire Welcome', 'Welcome Sarah to the Design team!', 65),
       (19, 52, 'iOS Crash Report', 'Fixed the login issue for iOS 17.', 55),
       (20, 53, 'Google Search Ads', 'Optimizing keywords for Japan market.', 42),
       (18, 53, 'Anyone for a hike?', 'Going to Namsan this weekend.', 19),
       (17, 54, 'Internal Audit Result', 'We passed all security requirements.', 58),
       (19, 54, 'Premium Plan Inquiry', 'Enterprise clients asking for SSO.', 34),
       (20, 55, 'Podcast Sponsoring', 'Launching our first podcast ad soon.', 27),
       (18, 55, 'Best Ramen in town', 'Just tried this new place, 10/10.', 52),
       (17, 56, 'CEO Townhall Meeting', 'Join via Zoom next Wednesday.', 130),
       (19, 56, 'Dark Mode Feedback', 'Users love it but need better contrast.', 41),
       (20, 57, 'Localizing Content', 'Tips for marketing in Latin America.', 39),
       (18, 57, 'Ski trip interest?', 'Planning for January, let me know.', 24),
       (17, 58, 'Sustainability Initiative', 'Office is going paperless from Q2.', 66),
       (19, 58, 'Feature Request: Export', 'Many users want PDF export for reports.', 37),
       (20, 59, 'A/B Testing Results', 'Red button has 10% higher conversion.', 51),
       (18, 59, 'Merry Christmas!', 'Hope everyone has a wonderful holiday.', 100),
       (17, 60, 'Annual Performance Review', 'Please fill out your self-evaluation.', 85),
       (19, 60, 'Voice of Customer', 'Monthly summary of common complaints.', 47);

-- ==========================================
-- 3. 댓글 데이터 (120개)
-- ==========================================
INSERT INTO comment (board_id, employee_id, comment_content)
VALUES (1, 2, '이번에도 온라인 생중계 해주시나요?'),
       (1, 21, '미래가 기대되는 발표네요.'),
       (2, 3, '저요! 파스타 완전 좋아합니다.'),
       (2, 22, '저도 끼워주세요!'),
       (3, 1, '성능 향상 폭이 생각보다 크네요.'),
       (3, 23, '서버 액션 보안은 괜찮을까요?'),
       (4, 4, '저는 초보인데 따라갈 수 있을까요?'),
       (4, 24, '저도 같이 가고 싶어요.'),
       (5, 5, '기존 API 라우트보다 편한 것 같아요.'),
       (5, 25, '코드가 훨씬 깔끔해졌네요.'),
       (6, 6, '무접점도 한 번 써보시면 좋습니다.'),
       (6, 26, '저도 적축 쓰는데 대만족!'),
       (7, 7, '감사합니다. 기다리던 소식입니다.'),
       (7, 27, '연차 보상비 최고!'),
       (8, 8, '동의합니다. 이번 원두는 너무 셔요.'),
       (8, 28, '저도 건의하려고 했습니다.'),
       (9, 9, '영업팀 정말 축하드립니다.'),
       (9, 29, '회식 가나요?'),
       (10, 10, '저도 가고 싶지만 선약이 있네요.'),
       (10, 30, '다음엔 꼭 같이 가요.'),
       (11, 11, '수치들이 아주 긍정적이네요.'),
       (11, 31, '마케팅팀 고생 많으셨습니다.'),
       (12, 12, '다큐멘터리 재밌더라고요.'),
       (12, 32, '저도 그거 봤어요!'),
       (13, 13, '영희님 정말 천사시죠! 인정합니다.'),
       (13, 33, '영희님 칭찬 릴레이네요.'),
       (14, 14, '앗 제 친구가 찾고 있었는데 물어볼게요.'),
       (14, 34, '주인분 꼭 찾으시길!'),
       (15, 15, '비개발자도 참여하고 싶어요.'),
       (15, 35, '누구나 환영입니다.'),
       (16, 16, '내일부터 바로 예약 잡아야겠네요.'),
       (16, 36, '저도 방금 예약했습니다.'),
       (17, 17, '방금 완료했습니다. 서두르세요!'),
       (17, 37, '보안 교육 클리어!'),
       (18, 18, '저도 그거 쓰는데 거북목 좋아졌어요.'),
       (18, 38, '공구하고 싶을 정도네요.'),
       (19, 19, '철수님 덕분에 아침이 평화롭네요.'),
       (19, 39, '철수님 최고!'),
       (20, 20, '리스트 공유 감사합니다!'),
       (20, 40, '내일 점심은 여기로 가야겠네요.'),
       (41, 41, 'Great vision for TS!'),
       (41, 1, '화이팅입니다!'),
       (42, 42, '이게 정석이죠. 고생하셨습니다.'),
       (42, 2, '기술 공유 감사합니다.'),
       (43, 43, '직거래 가능한가요?'),
       (43, 3, '상태 좋아 보이네요.'),
       (44, 44, 'Very interesting results.'),
       (44, 4, 'AI팀 응원합니다.'),
       (45, 45, '인증 로직 확인했습니다.'),
       (45, 5, '보안이 강화됐네요.'),
       (46, 46, '창가 자리 가고 싶어요 ㅎㅎ'),
       (46, 6, '이사 가기 번거롭겠어요.'),
       (47, 47, '금액이 상당하겠는데요?'),
       (47, 7, '비용 절감 대단합니다.'),
       (48, 48, '제 대시보드에도 적용해보고 싶어요.'),
       (48, 8, '템플릿 공유 부탁드려요.'),
       (49, 49, '나눔 감사합니다! 쪽지 드렸어요.'),
       (49, 9, '저도 줄 서봅니다.'),
       (50, 50, '정말 놀라운 속도네요.'),
       (50, 10, '알고리즘이 궁금합니다.'),
       (81, 41, 'Exciting goals.'),
       (81, 11, 'SEA market is huge.'),
       (82, 42, 'I love soccer! Where?'),
       (82, 12, 'Count me in!'),
       (83, 43, 'Bugs fixed yet?'),
       (83, 13, 'Feedback is noted.'),
       (84, 44, 'Coffee on me today.'),
       (84, 14, 'Thanks for the latte.'),
       (85, 45, 'India market data?'),
       (85, 15, 'Growth is visible.'),
       (86, 46, 'Vacation dates confirmed.'),
       (86, 16, 'Check the email.'),
       (87, 47, 'Hard goal, but possible.'),
       (87, 17, 'Let s do it.'),
       (88, 48, 'I saw them in lobby.'),
       (88, 18, 'Front desk has them.'),
       (89, 49, 'Visuals are fresh.'),
       (89, 19, 'Nice design work.'),
       (90, 50, 'Booked Tokyo flight!'),
       (90, 20, 'Enjoy your trip!'),
       (91, 51, 'Construction is noisy.'),
       (91, 21, 'Pardon the mess.'),
       (92, 52, 'SSO is needed for B2B.'),
       (92, 22, 'Working on it.'),
       (93, 53, 'TikTok branding is key.'),
       (93, 23, 'Gen Z target!'),
       (94, 54, 'GS2025 code works.'),
       (94, 24, 'Joining the gym too.'),
       (95, 55, 'Policy update noted.'),
       (95, 25, 'Got the email.'),
       (96, 56, '80 NPS is the target.'),
       (96, 26, 'We can reach it.'),
       (97, 57, 'UK list is ready.'),
       (97, 27, 'Good candidates.'),
       (98, 58, 'Stay warm folks.'),
       (98, 28, 'Winter is here.'),
       (99, 59, 'Security video done.'),
       (99, 29, 'Stay safe online.'),
       (100, 60, 'Retention is vital.'),
       (100, 30, 'New ideas needed.');

-- ==========================================
-- 4. 쪽지 데이터 (180개: 인당 3개씩 발송)
-- ==========================================
INSERT INTO message (id, company_id, employee_id, message_title, message_content) VALUES
                                                                                      (1, 1, 1, '업무 협조 요청', '보고서 확인 부탁드려요.'), (2, 1, 1, '미팅 일정 변경', '오후 4시로 변경.'), (3, 1, 1, '식당 예약', '12시 예약 완료.'),
                                                                                      (4, 1, 2, '코드 리뷰', 'PR 확인 부탁드려요.'), (5, 1, 2, '서버 재시작', '완료되었습니다.'), (6, 1, 2, '간식 나눔', '탕비실 확인요.'),
                                                                                      (7, 1, 3, '디자인 시안', '1차본 공유합니다.'), (8, 1, 3, '아이콘 수정', '완료되었습니다.'), (9, 1, 3, '폰트 정보', '라이선스 확인요.'),
                                                                                      (10, 1, 4, '연차 결재', '승인 부탁드립니다.'), (11, 1, 4, '교육 신청', '오늘 마감입니다.'), (12, 1, 4, '증빙 제출', '영수증 첨부함.'),
                                                                                      (13, 1, 5, '영업 보고', '최종 리포트입니다.'), (14, 1, 5, '잠재 고객', '리스트 공유합니다.'), (15, 1, 5, '미팅 약도', '지도 확인하세요.'),
                                                                                      (16, 1, 6, '광고 소재', '이미지 전달합니다.'), (17, 1, 6, '예산 확인', '금액 확인 부탁요.'), (18, 1, 6, '성과 분석', '데이터 필요합니다.'),
                                                                                      (19, 1, 7, '레이아웃', '수정 요청합니다.'), (20, 1, 7, '테스트 결과', '로그 첨부합니다.'), (21, 1, 7, 'QA 리스트', '확인 부탁드립니다.'),
                                                                                      (22, 1, 8, '스터디 교재', 'PDF 파일입니다.'), (23, 1, 8, '장소 공지', '회의실 A입니다.'), (24, 1, 8, '과제 안내', '금주 과제입니다.'),
                                                                                      (25, 1, 9, '보안 점검', '리스트 공유합니다.'), (26, 1, 9, '암호 변경', '주기적 변경 요청.'), (27, 1, 9, '접속 기록', '이상 로그 발견.'),
                                                                                      (28, 1, 10, 'DB 튜닝', '성능 개선 결과.'), (29, 1, 10, '백업 안내', '스케줄 공유합니다.'), (30, 1, 10, '장애 조치', '완료 보고서입니다.'),
                                                                                      (31, 1, 11, 'Redis 설정', '업데이트 완료.'), (32, 1, 11, '스택 회의', '일정 잡읍시다.'), (33, 1, 11, '라이선스', '검토 결과입니다.'),
                                                                                      (34, 1, 12, '컨벤션', '문서 공유합니다.'), (35, 1, 12, '컴포넌트', '정리 완료함.'), (36, 1, 12, '속도 개선', '최적화 필요함.'),
                                                                                      (37, 1, 13, '복지 포인트', '잔액 확인하세요.'), (38, 1, 13, '온보딩', '가이드 전달함.'), (39, 1, 13, '경조사', '공지 확인요.'),
                                                                                      (40, 1, 14, '글로벌 미팅', '링크 전달함.'), (41, 1, 14, '환율 변동', '대응 필요함.'), (42, 1, 14, '최종 서명', '요청드립니다.'),
                                                                                      (43, 1, 15, '이벤트 기획', '초안입니다.'), (44, 1, 15, '당첨자', '명단 공유함.'), (45, 1, 15, '협력사 번호', '전달드립니다.'),
                                                                                      (46, 1, 16, '급여 안내', '명세서 발송.'), (47, 1, 16, '정산 서류', '누락 확인요.'), (48, 1, 16, '분기 목표', '공유드립니다.'),
                                                                                      (49, 1, 17, '시설 보수', '조치 완료함.'), (50, 1, 17, '비품 구매', '목록 확인요.'), (51, 1, 17, '주차 안내', '방식 변경함.'),
                                                                                      (52, 1, 18, '응시료 지원', '신청하세요.'), (53, 1, 18, '분석 특강', '일정 안내.'), (54, 1, 18, '구매 목록', '승인 바랍니다.'),
                                                                                      (55, 1, 19, '동호회', '지원비 공지.'), (56, 1, 19, '설문 참여', '링크 공유함.'), (57, 1, 19, '워크숍', '장소 투표요.'),
                                                                                      (58, 1, 20, '동료 칭찬', '피드백 공유.'), (59, 1, 20, '칭찬 배지', '수여 안내.'), (60, 1, 20, '팀 빌딩', '활동 결과.'),
-- 회사 2 데이터 (61~72 추가)
                                                                                      (61, 2, 21, 'TS 신년 목표', '달성합시다.'), (62, 2, 21, '회의 자료', '첨부합니다.'), (63, 2, 21, 'IR 자료', '최종 확인.'),
                                                                                      (64, 2, 22, 'LLM 개선', '속도 측정.'), (65, 2, 22, 'GPU 예약', '사용 알림.'), (66, 2, 22, '데이터 검수', '완료함.'),
                                                                                      (67, 2, 23, '플랫폼 점검', '공지사항입니다.'), (68, 2, 23, 'API 문서', '최신화 완료.'), (69, 2, 24, '인프라 비용', '보고서 확인요.'),
                                                                                      (70, 2, 24, '네트워크 장애', '복구 완료.'), (71, 2, 25, '모델 테스트', '결과 공유.'), (72, 2, 25, '학습 데이터', '추가 필요.'),
-- 회사 3 데이터
                                                                                      (121, 3, 41, 'SEA Market', 'Plan doc.'), (122, 3, 41, 'Manager List', 'Contact list.'), (123, 3, 41, 'Goal', '10M users.');

-- ==========================================
-- 5. 쪽지 수신자 데이터 (골고루 섞인 배분)
-- ==========================================
INSERT INTO message_recipient (company_id, message_id, employee_id, is_read) VALUES
-- 1~20번 발송자 결과 (회사 1)
(1, 1, 5, 0), (1, 2, 12, 1), (1, 3, 18, 0),
(1, 4, 1, 1), (1, 5, 11, 0), (1, 6, 20, 1),
(1, 7, 2, 0), (1, 8, 9, 1), (1, 9, 16, 0),
(1, 10, 3, 1), (1, 11, 8, 0), (1, 12, 14, 1),
(1, 13, 4, 0), (1, 14, 7, 1), (1, 15, 19, 0),
(1, 16, 10, 1), (1, 17, 13, 0), (1, 18, 1, 1),
(1, 19, 6, 0), (1, 20, 15, 1), (1, 21, 2, 0),
(1, 22, 5, 1), (1, 23, 17, 0), (1, 24, 3, 1),
(1, 25, 4, 0), (1, 26, 12, 1), (1, 27, 8, 0),
(1, 28, 9, 1), (1, 29, 14, 0), (1, 30, 20, 1),
(1, 31, 1, 0), (1, 32, 6, 1), (1, 33, 15, 0),
(1, 34, 2, 1), (1, 35, 7, 0), (1, 36, 11, 1),
(1, 37, 3, 0), (1, 38, 10, 1), (1, 39, 16, 0),
(1, 40, 4, 1), (1, 41, 13, 0), (1, 42, 17, 1),
(1, 43, 5, 0), (1, 44, 18, 1), (1, 45, 9, 0),
(1, 46, 6, 1), (1, 47, 14, 0), (1, 48, 12, 1),
(1, 49, 7, 0), (1, 50, 19, 1), (1, 51, 8, 0),
(1, 52, 8, 1), (1, 53, 1, 0), (1, 54, 15, 1),
(1, 55, 9, 0), (1, 56, 2, 1), (1, 57, 11, 0),
(1, 58, 10, 1), (1, 59, 3, 0), (1, 60, 4, 1),

-- 회사 2 (employee_id 21~40 사이 수신)
(2, 61, 30, 0), (2, 62, 35, 1), (2, 63, 25, 0),
(2, 64, 21, 1), (2, 65, 33, 0), (2, 66, 38, 1),
(2, 67, 22, 0), (2, 68, 29, 1), (2, 69, 36, 0),
(2, 70, 23, 1), (2, 71, 28, 0), (2, 72, 31, 1),

-- 회사 3 (employee_id 41~60 사이 수신)
(3, 121, 55, 0), (3, 122, 50, 1), (3, 123, 45, 0);