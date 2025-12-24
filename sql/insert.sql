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



-- 종훈 --
use orbitflow;

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



INSERT INTO leave_type (type_name, is_countable, description) VALUES
                                                                  ('연차', true, '법정 유급 연차 휴가'),
                                                                  ('오전반차', true, '09:00 ~ 13:00 사용'),
                                                                  ('오후반차', true, '14:00 ~ 18:00 사용'),
                                                                  ('병가', false, '질병 또는 부상으로 인한 휴무');


INSERT INTO attendance_rule (company_id, name, default_start_time, default_end_time, default_break_minutes, late_threshold_min, is_default)
VALUES (1, 'OrbitFlow 기본 근무규칙', '09:00:00', '18:00:00', 60, 10, true);


-- 연차 부여 이력 (정기 및 월별)
INSERT INTO grant_history (employee_id, company_id, grant_date, granted_days, grant_type, created_at) VALUES
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
INSERT INTO leave_balance (company_id, employee_id, year, total_granted, remaining_days) VALUES
                                                                                             (1, 10, 2025, 15.0, 11.5), -- 15개 중 3.5개 사용 (12월 현재)
                                                                                             (1, 11, 2025, 15.0, 14.0), -- 15개 중 1개 사용
                                                                                             (1, 14, 2025, 15.0, 15.0), -- 미사용자

                                                                                             (1, 15, 2025, 11.0, 10.5), -- 신입사원 (매달 1개씩 총 11개 부여됨)

                                                                                             (1, 16, 2025, 15.0, 13.0);  -- 15개 중 2개 사용

-- 실제 휴가 사용 기록 (결재 완료 건들)
INSERT INTO attendance_record (employee_id, company_id, start_date, end_date, days, type_id, status, approved_at, created_at) VALUES
-- 이준호: 11월 7일(1일), 12월 12일(1일) 연차 사용
(10, 1, '2025-11-07', '2025-11-07', 1.0, 1, 'APPROVED', '2025-11-05 15:00:00', '2025-11-04 10:00:00'),
(10, 1, '2025-12-12', '2025-12-12', 1.0, 1, 'APPROVED', '2025-12-10 11:00:00', '2025-12-09 13:00:00'),
-- 김나연: 12월 24일 오후 반차 사용
(11, 1, '2025-12-24', '2025-12-24', 0.5, 3, 'APPROVED', '2025-12-23 17:00:00', '2025-12-23 09:30:00'),
-- 한지민: 12월 15일 오전 반차 사용
(15, 1, '2025-12-15', '2025-12-15', 0.5, 2, 'APPROVED', '2025-12-14 10:00:00', '2025-12-13 14:00:00'),
-- 오세훈: 11월 20일~21일 이틀 연차 사용
(16, 1, '2025-11-20', '2025-11-21', 2.0, 1, 'APPROVED', '2025-11-18 16:00:00', '2025-11-15 11:00:00');



INSERT INTO attendance (company_id, employee_id, work_date, commute_at, leave_at, status, applied_rule_id, is_corrected) VALUES
                                                                                                                             (1, 10, '2025-11-03', '2025-11-03 08:52:00', '2025-11-03 18:05:00', 'ON_TIME', 1, 0),
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



