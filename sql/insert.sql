use orbitflow;

/* =====================================================
   COMPANY (2개 회사)
===================================================== */
INSERT INTO company
(name, business_number, address, representative_name, representative_contact)
VALUES ('OrbitFlow', '1234567890', '서울 강남구 테헤란로 123', '홍대표', '01011112222'),
       ('NovaWorks', '9876543210', '서울 서초구 서초대로 77', '김대표', '01022223333');


/* =====================================================
   ORG_CATEGORY (조직 유형)
===================================================== */
INSERT INTO org_category (company_id, name, order_index)
VALUES
-- OrbitFlow
(1, '회사', 1),
(1, '본부', 2),
(1, '부서', 3),
(1, '팀', 4),

-- NovaWorks
(2, '회사', 1),
(2, '본부', 2),
(2, '부서', 3),
(2, '팀', 4);


/* =====================================================
   ORGANIZATION (조직 트리)
===================================================== */
-- OrbitFlow
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index)
VALUES (1, 1, NULL, 'OrbitFlow', 1),
       (1, 2, 1, '플랫폼본부', 1),
       (1, 2, 1, '경영지원본부', 2),
       (1, 3, 2, '개발부', 1),
       (1, 3, 3, '인사부', 1),
       (1, 4, 4, '백엔드팀', 1),
       (1, 4, 4, '프론트엔드팀', 2),
       (1, 4, 5, '인사팀', 1);

-- NovaWorks
INSERT INTO organization (company_id, category_id, parent_org_id, name, order_index)
VALUES (2, 5, NULL, 'NovaWorks', 1),
       (2, 6, 9, '기술본부', 1),
       (2, 7, 10, '플랫폼부', 1),
       (2, 8, 11, 'AI팀', 1);


/* =====================================================
   HR_RANK (직급)
===================================================== */
INSERT INTO hr_rank (company_id, parent_hr_rank_id, name, order_index)
VALUES (1, NULL, '사원', 1),
       (1, 1, '대리', 2),
       (1, 2, '과장', 3),
       (1, 3, '차장', 4),
       (1, 4, '부장', 5),

       (2, NULL, '사원', 1),
       (2, 6, '선임', 2),
       (2, 7, '책임', 3);


/* =====================================================
   POSITION_CATEGORY
===================================================== */
INSERT INTO position_category
(company_id, org_category_id, name, order_index, is_head)
VALUES
-- OrbitFlow
(1, 1, '사장',   1, TRUE),
(1, 2, '본부장', 2, TRUE),
(1, 3, '부장',   3, TRUE),
(1, 4, '팀장',   4, TRUE),
(1, 4, '팀원',   5, FALSE),

-- NovaWorks
(2, 5, '사장',   1, TRUE),
(2, 6, '본부장', 2, TRUE),
(2, 7, '부장',   3, TRUE),
(2, 8, '팀장',   4, TRUE),
(2, 8, '팀원',   5, FALSE);






/* =====================================================
   ORG_POSITION_USAGE
===================================================== */
INSERT INTO org_position_usage
(company_id, org_id, position_category_id)
VALUES
-- OrbitFlow
(1, 1, 1), -- 회사 → 사장
(1, 2, 2), -- 본부 → 본부장
(1, 3, 2),
(1, 4, 3), -- 개발부 → 부장
(1, 5, 3), -- 인사부 → 부장
(1, 6, 4), -- 백엔드팀 → 팀장
(1, 6, 5), -- 백엔드팀 → 팀원
(1, 7, 4),
(1, 7, 5),
(1, 8, 4),
(1, 8, 5),

-- NovaWorks
(2, 9, 6),
(2,10, 7),
(2,11, 8),
(2,11, 9);



/* =====================================================
   EMPLOYEE
===================================================== */
INSERT INTO employee
(company_id, employee_no, internal_phone, phone,
 org_id, hr_rank_id, position_category_id,
 name, email, password,
 gender, birth_date, employment_type,
 status, work_status, role)
VALUES
-- OrbitFlow
(1, 'OF-001', '1001', '01012345678',
 6, 5, 4, '홍길동', 'test1@test.com',
 '$2a$10$1CQx3GSceOnsh1Lne0nQzeR4ZH.OcD/WWayDba4BBorqwVcjxBuhK',
 'MALE', '1990-01-01', 'REGULAR',
 'ACTIVE', 'WORKING', 'COMPANY_ADMIN'),

(1, 'OF-002', '1002', '01074108529',
 6, 2, 5, '김철수', 'test2@test.com',
 '$2a$10$1CQx3GSceOnsh1Lne0nQzeR4ZH.OcD/WWayDba4BBorqwVcjxBuhK',
 'MALE', '1996-03-03', 'REGULAR',
 'ACTIVE', 'AWAY', 'ADMIN'),

(1, 'OF-003', '1003', '01098765432',
 8, 3, 4, '이영희', 'test3@test.com',
 '$2a$10$1CQx3GSceOnsh1Lne0nQzeR4ZH.OcD/WWayDba4BBorqwVcjxBuhK',
 'FEMALE', '1994-06-06', 'REGULAR',
 'ACTIVE', 'ON_LEAVE', 'EMPLOYEE'),

-- NovaWorks
(2, 'NW-001', '2001', '01055555555',
 12, 7, 8, '박민수', 'test4@test.com',
 '$2a$10$1CQx3GSceOnsh1Lne0nQzeR4ZH.OcD/WWayDba4BBorqwVcjxBuhK',
 'MALE', '1992-02-02', 'REGULAR',
 'ACTIVE', 'WORKING', 'COMPANY_ADMIN'),

(2, 'NW-002', '2002', '01099999999',
 12, 6, 9, '정수빈', 'test5@test.com',
 '$2a$10$1CQx3GSceOnsh1Lne0nQzeR4ZH.OcD/WWayDba4BBorqwVcjxBuhK',
 'FEMALE', '1999-09-09', 'NON_REGULAR',
 'TEMP', 'OFF_WORK', 'ADMIN');



/* =====================================================
   NOTIFICATION (알림)
===================================================== */
INSERT INTO notification
    (company_id, employee_id, type, content, is_read)
VALUES
-- OrbitFlow (company_id = 1)
(1, 1, 'ORG', '백엔드팀 팀장으로 지정되었습니다.', FALSE),
(1, 2, 'ORG', '백엔드팀에 배정되었습니다.', TRUE),
(1, 3, 'ORG', '인사팀 팀장으로 지정되었습니다.', FALSE),
(1, 2, 'SYSTEM', '비밀번호가 초기화되었습니다. 새 비밀번호로 변경해주세요.', TRUE),
(1, 3, 'SYSTEM', '계정 권한이 변경되었습니다.', FALSE),

-- NovaWorks (company_id = 2)
(2, 4, 'ORG', 'AI팀 팀장으로 지정되었습니다.', FALSE),
(2, 5, 'ORG', 'AI팀에 배정되었습니다.', FALSE),
(2, 5, 'SYSTEM', '임시 계정이 생성되었습니다. 최초 로그인 후 비밀번호를 변경하세요.', FALSE),
(2, 4, 'SYSTEM', '부서 책임자로 지정되었습니다.', TRUE);


/* =====================================================
   LOG_AUDIT (인사 감사 로그)
===================================================== */
INSERT INTO log_audit
(company_id, actor_employee_id, entity_type, entity_id,
 event_type, before_data, after_data)
VALUES
-- OrbitFlow
(1, 1, 'EMPLOYEE', 2, 'CREATE',
 NULL,
 JSON_OBJECT('name', '김철수', 'org', '백엔드팀', 'position', '백엔드팀원')),

(1, 1, 'EMPLOYEE', 2, 'ASSIGN',
 JSON_OBJECT('position', NULL),
 JSON_OBJECT('position', '백엔드팀원')),

(1, 1, 'EMPLOYEE', 1, 'ASSIGN',
 JSON_OBJECT('position', NULL),
 JSON_OBJECT('position', '백엔드팀장')),

(1, 1, 'EMPLOYEE', 3, 'MOVE',
 JSON_OBJECT('org', '인사부'),
 JSON_OBJECT('org', '인사팀')),

(1, 1, 'EMPLOYEE', 3, 'ASSIGN',
 JSON_OBJECT('position', NULL),
 JSON_OBJECT('position', '인사팀장')),

-- NovaWorks
(2, 4, 'EMPLOYEE', 5, 'CREATE',
 NULL,
 JSON_OBJECT('name', '정수빈', 'org', 'AI팀', 'status', 'TEMP')),

(2, 4, 'EMPLOYEE', 5, 'ASSIGN',
 JSON_OBJECT('position', NULL),
 JSON_OBJECT('position', 'AI팀원')),

(2, 4, 'EMPLOYEE', 4, 'ASSIGN',
 JSON_OBJECT('position', NULL),
 JSON_OBJECT('position', 'AI팀장')),

(2, 4, 'EMPLOYEE', 5, 'SUSPENDED',
 JSON_OBJECT('status', 'ACTIVE'),
 JSON_OBJECT('status', 'SUSPENDED'));



-- =========================================================
-- LEAVE TYPE (휴가 유형)
-- =========================================================
INSERT INTO leave_type
    (type_name, is_countable, description)
VALUES ('연차', TRUE, '연 단위로 부여되며 차감되는 기본 유급 휴가'),
       ('오전 반차', TRUE, '오전 근무 시간에 사용하는 반일 휴가'),
       ('오후 반차', TRUE, '오후 근무 시간에 사용하는 반일 휴가'),
       ('병가', TRUE, '질병 사유로 사용하는 휴가'),
       ('공가', FALSE, '회사 공적인 사유로 부여되는 휴가'),
       ('대체휴무', TRUE, '초과 근무에 따른 보상 휴가'),
       ('무급휴가', FALSE, '급여 차감 대상 휴가');


/* =========================================================
 * TEMPLATE CATEGORY (GLOBAL)
 * ========================================================= */
INSERT INTO template_category (code, name)
VALUES ('ATTENDANCE', '근태'),
       ('SCHEDULE', '일정'),
       ('GENERAL', '일반');


/* =========================================================
 * FORM TEMPLATE GROUP (10)
 * ========================================================= */
INSERT INTO form_template_group
    (company_id, name, description, created_by)
VALUES (1, '휴가 신청', '연차/반차/병가 신청', 1),
       (1, '출장 보고', '출장 계획 및 결과 보고', 1),
       (1, '초과근무 신청', '야근/주말근무 신청', 1),
       (1, '비용 정산', '법인카드 및 비용 정산', 1),
       (1, '장비 구매', '업무용 장비 구매 요청', 1),

       (2, '휴가 신청', '연차/반차/병가 신청', 4),
       (2, '출장 보고', '출장 계획 및 결과 보고', 4),
       (2, '초과근무 신청', '야근/주말근무 신청', 4),
       (2, '비용 정산', '법인카드 및 비용 정산', 4),
       (2, '장비 구매', '업무용 장비 구매 요청', 4);


/* =========================================================
 * FORM TEMPLATE (10)
 * ========================================================= */
INSERT INTO form_template
(company_id, template_group_id, version, template_category_id,
 affect_tags, template_json, approval_rule_json, status)
VALUES
-- 회사 1
(1, 1, 1, 1, JSON_ARRAY('ATTENDANCE'),
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'start_date'),
         JSON_OBJECT('key', 'end_date'),
         JSON_OBJECT('key', 'leave_type')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'MANAGER'),
         JSON_OBJECT('order', 2, 'approverType', 'ADMIN')
                      )), 'ACTIVE'),

(1, 2, 1, 2, JSON_ARRAY('SCHEDULE'),
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'destination'),
         JSON_OBJECT('key', 'period')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
                      )), 'ACTIVE'),

(1, 3, 1, 1, JSON_ARRAY('ATTENDANCE'),
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'date'),
         JSON_OBJECT('key', 'hours')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'MANAGER')
                      )), 'ACTIVE'),

(1, 4, 1, 3, NULL,
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'amount'),
         JSON_OBJECT('key', 'reason')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
                      )), 'ACTIVE'),

(1, 5, 1, 3, NULL,
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'item'),
         JSON_OBJECT('key', 'price')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
                      )), 'ACTIVE'),

-- 회사 2
(2, 6, 1, 1, JSON_ARRAY('ATTENDANCE'),
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'start_date'),
         JSON_OBJECT('key', 'end_date'),
         JSON_OBJECT('key', 'leave_type')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'MANAGER'),
         JSON_OBJECT('order', 2, 'approverType', 'ADMIN')
                      )), 'ACTIVE'),

(2, 7, 1, 2, JSON_ARRAY('SCHEDULE'),
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'destination'),
         JSON_OBJECT('key', 'period')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
                      )), 'ACTIVE'),

(2, 8, 1, 1, JSON_ARRAY('ATTENDANCE'),
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'date'),
         JSON_OBJECT('key', 'hours')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'MANAGER')
                      )), 'ACTIVE'),

(2, 9, 1, 3, NULL,
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'amount'),
         JSON_OBJECT('key', 'reason')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
                      )), 'ACTIVE'),

(2, 10, 1, 3, NULL,
 JSON_OBJECT('fields', JSON_ARRAY(
         JSON_OBJECT('key', 'item'),
         JSON_OBJECT('key', 'price')
                       )),
 JSON_OBJECT('lines', JSON_ARRAY(
         JSON_OBJECT('order', 1, 'approverType', 'ADMIN')
                      )), 'ACTIVE');


/* =========================================================
 * FORM TEMPLATE AI LOG (company_id 포함)
 * ========================================================= */
INSERT INTO log_form_template_ai
(company_id, template_group_id, created_template_id, prompt,
 generated_template_json, generated_rule_json,
 model, status, created_by)
VALUES
-- company_id = 1
(1, 1, 1, '연차 신청 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
(1, 2, 2, '출장 보고 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
(1, 3, 3, '야근 신청 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
(1, 4, 4, '비용 정산 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),
(1, 5, 5, '장비 구매 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 1),

-- company_id = 2
(2, 6, 6, '연차 신청 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4),
(2, 7, 7, '출장 보고 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4),
(2, 8, 8, '야근 신청 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4),
(2, 9, 9, '비용 정산 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4),
(2, 10, 10, '장비 구매 양식 생성', JSON_OBJECT(), JSON_OBJECT(), 'gpt-4', 'SUCCESS', 4);


/* =========================================================
 * DOCUMENT (10)
 * ========================================================= */
INSERT INTO document
(company_id, template_group_id, template_version,
 writer_id, title, status)
VALUES (1, 1, 1, 2, '연차 신청', 'SUBMITTED'),
       (1, 2, 1, 2, '출장 보고', 'IN_PROGRESS'),
       (1, 3, 1, 3, '야근 신청', 'SUBMITTED'),
       (1, 4, 1, 2, '비용 정산 요청', 'DRAFT'),
       (1, 5, 1, 3, '장비 구매 요청', 'SUBMITTED'),

       (2, 6, 1, 4, '연차 신청', 'SUBMITTED'),
       (2, 7, 1, 4, '출장 보고', 'IN_PROGRESS'),
       (2, 8, 1, 5, '야근 신청', 'SUBMITTED'),
       (2, 9, 1, 4, '비용 정산 요청', 'DRAFT'),
       (2, 10, 1, 5, '장비 구매 요청', 'SUBMITTED');


/* =========================================================
 * DOCUMENT CONTENT (10)
 * ========================================================= */
INSERT INTO document_content (document_id, content_json)
VALUES (1, JSON_OBJECT('start_date', '2025-01-05', 'end_date', '2025-01-07')),
       (2, JSON_OBJECT('destination', '부산', 'period', '2박3일')),
       (3, JSON_OBJECT('date', '2025-01-10', 'hours', 4)),
       (4, JSON_OBJECT('amount', 120000, 'reason', '식대')),
       (5, JSON_OBJECT('item', '노트북', 'price', 1500000)),

       (6, JSON_OBJECT('start_date', '2025-02-05', 'end_date', '2025-02-07')),
       (7, JSON_OBJECT('destination', '대전', 'period', '1박2일')),
       (8, JSON_OBJECT('date', '2025-02-10', 'hours', 3)),
       (9, JSON_OBJECT('amount', 98000, 'reason', '회의비')),
       (10, JSON_OBJECT('item', '모니터', 'price', 420000));


/* =========================================================
 * APPROVAL LINE (10)
 * ========================================================= */
INSERT INTO approval_line
    (document_id, company_id, approver_id, order_no, status)
VALUES (1, 1, 1, 1, 'IN_PROGRESS'),
       (1, 1, 1, 2, 'DRAFT'),
       (2, 1, 1, 1, 'IN_PROGRESS'),
       (3, 1, 1, 1, 'APPROVED'),
       (4, 1, 1, 1, 'DRAFT'),

       (6, 2, 4, 1, 'IN_PROGRESS'),
       (6, 2, 4, 2, 'DRAFT'),
       (7, 2, 4, 1, 'IN_PROGRESS'),
       (8, 2, 4, 1, 'APPROVED'),
       (9, 2, 4, 1, 'DRAFT');


/* =========================================================
 * DOCUMENT AI SUMMARY
 * ========================================================= */
INSERT INTO document_ai_summary
(document_id, company_id, summary_type, content, model, before_document_id)
VALUES (1, 1, 'CONTENT', '연차 신청 요약', 'gpt-4', NULL),
       (2, 1, 'CONTENT', '출장 보고 요약', 'gpt-4', NULL),
       (3, 1, 'CONTENT', '야근 신청 요약', 'gpt-4', NULL),
       (4, 1, 'CONTENT', '비용 정산 요약', 'gpt-4', NULL),
       (5, 1, 'CONTENT', '장비 구매 요약', 'gpt-4', NULL),

       (6, 2, 'CONTENT', '연차 신청 요약', 'gpt-4', NULL),
       (7, 2, 'CONTENT', '출장 보고 요약', 'gpt-4', NULL),
       (8, 2, 'CONTENT', '야근 신청 요약', 'gpt-4', NULL),
       (9, 2, 'CONTENT', '비용 정산 요약', 'gpt-4', NULL),
       (10, 2, 'CONTENT', '장비 구매 요약', 'gpt-4', NULL);


/* =========================================================
 * ATTENDANCE RECORD (10)
 * ========================================================= */
INSERT INTO attendance_record
(employee_id, company_id, start_date, end_date,
 days, type_id, reason, source_document_id, status)
VALUES
-- Company 1
(2, 1, '2025-01-05', '2025-01-07', 3.0, 1, '연차', 1, 'IN_PROGRESS'),
(3, 1, '2025-01-10', '2025-01-10', 0.5, 2, '반차', 3, 'APPROVED'),
(2, 1, '2025-01-15', '2025-01-15', 1.0, 1, '연차', 1, 'DRAFT'),
(3, 1, '2025-01-20', '2025-01-21', 2.0, 1, '연차', 1, 'SUBMITTED'),
(2, 1, '2025-01-25', '2025-01-25', 1.0, 1, '연차', 1, 'APPROVED'),
-- Company 2
(4, 2, '2025-02-05', '2025-02-07', 3.0, 1, '연차', 6, 'IN_PROGRESS'),
(5, 2, '2025-02-10', '2025-02-10', 0.5, 3, '반차', 8, 'APPROVED'),
(4, 2, '2025-02-15', '2025-02-15', 1.0, 1, '연차', 6, 'DRAFT'),
(5, 2, '2025-02-20', '2025-02-21', 2.0, 1, '연차', 6, 'SUBMITTED'),
(4, 2, '2025-02-25', '2025-02-25', 1.0, 1, '연차', 6, 'APPROVED');


/* =========================================================
 * FILE (10)
 * ========================================================= */
INSERT INTO file
(company_id, object_key, origin_file, sys_file,
 content_type, file_size, created_by)
VALUES (1, 'docs/leave.pdf', '연차신청서.pdf', 'f001.pdf', 'application/pdf', 102400, 2),
       (1, 'docs/trip.pdf', '출장보고서.pdf', 'f002.pdf', 'application/pdf', 204800, 2),
       (1, 'docs/overtime.pdf', '야근신청서.pdf', 'f003.pdf', 'application/pdf', 51200, 3),
       (1, 'docs/expense.pdf', '정산서.pdf', 'f004.pdf', 'application/pdf', 102400, 2),
       (1, 'docs/laptop.pdf', '구매요청서.pdf', 'f005.pdf', 'application/pdf', 307200, 3),

       (2, 'docs2/leave.pdf', '연차신청서.pdf', 'f101.pdf', 'application/pdf', 102400, 4),
       (2, 'docs2/trip.pdf', '출장보고서.pdf', 'f102.pdf', 'application/pdf', 204800, 4),
       (2, 'docs2/overtime.pdf', '야근신청서.pdf', 'f103.pdf', 'application/pdf', 51200, 5),
       (2, 'docs2/expense.pdf', '정산서.pdf', 'f104.pdf', 'application/pdf', 102400, 4),
       (2, 'docs2/laptop.pdf', '구매요청서.pdf', 'f105.pdf', 'application/pdf', 307200, 5);


/* =========================================================
 * DOCUMENT FILE (10)
 * ========================================================= */
INSERT INTO document_file
    (document_id, file_id, reference_type)
VALUES (1, 1, 'ATTACHMENT'),
       (2, 2, 'ATTACHMENT'),
       (3, 3, 'ATTACHMENT'),
       (4, 4, 'ATTACHMENT'),
       (5, 5, 'ATTACHMENT'),
       (6, 6, 'ATTACHMENT'),
       (7, 7, 'ATTACHMENT'),
       (8, 8, 'ATTACHMENT'),
       (9, 9, 'ATTACHMENT'),
       (10, 10, 'ATTACHMENT');


/* =========================================================
 * FILE (EMPLOYEE SIGNATURE IMAGE)
 * ========================================================= */
INSERT INTO file
(company_id, object_key, origin_file, sys_file,
 content_type, file_size, created_by)
VALUES
-- OrbitFlow
(1, 'signatures/employee/1/sign.png', 'hong_signature.png', 'sig_001.png',
 'image/png', 20480, 1), -- file_id = 1 (홍길동)

(1, 'signatures/employee/2/sign.png', 'kim_signature.png', 'sig_002.png',
 'image/png', 19800, 2), -- file_id = 2 (김철수)

(1, 'signatures/employee/3/sign.png', 'lee_signature.png', 'sig_003.png',
 'image/png', 21500, 3), -- file_id = 3 (이영희)

-- NovaWorks
(2, 'signatures/employee/4/sign.png', 'park_signature.png', 'sig_006.png',
 'image/png', 22300, 4), -- file_id = 6 (박민수)

(2, 'signatures/employee/5/sign.png', 'jung_signature.png', 'sig_007.png',
 'image/png', 20100, 5); -- file_id = 7 (정수빈)


INSERT INTO employee_signature
    (company_id, employee_id, file_id, is_active)
VALUES (1, 1, 11, TRUE), -- 홍길동 서명
       (1, 2, 12, TRUE), -- 김철수 서명
       (1, 3, 13, TRUE), -- 이영희 서명
       (2, 4, 14, TRUE), -- 박민수 서명
       (2, 5, 15, TRUE);
-- 정수빈 서명


INSERT INTO document_signature
(document_id, approval_line_id, company_id, signer_id, signature_file_id)
VALUES
-- 문서 3 (야근 신청) 승인
(3, 4, 1, 1, 1),

-- 문서 8 (야근 신청 - NovaWorks)
(8, 9, 2, 4, 15);



/* =========================================================
   BOARD / MESSAGE INSERT DATA
========================================================= */

-- board_category
-- board_category
INSERT INTO board_category
(company_id, organization_id, board_name, board_type, is_activated, comment_activated)
VALUES
    -- 회사 1 : 공용 게시판
    (1, NULL, '전사 공지사항', 'FREE', 1, 1),
    (1, NULL, '자유 게시판', 'FREE', 1, 1),
    (1, NULL, '전사 자료실', 'FREE', 1, 0),
    (1, NULL, '주간 업무 공유', 'FREE', 1, 1),
    (1, NULL, '경조사 게시판', 'FREE', 1, 1),
    -- 회사 1 : 조직 게시판
    (1, 8, '인사팀 게시판', 'FREE', 1, 1),
    (1, 6, '백엔드팀 게시판', 'FREE', 1, 1),

    -- 회사 2 : 공용 게시판
    (2, NULL, '전사 공지사항', 'FREE', 1, 1),
    (2, NULL, '전사 건의 게시판', 'FREE', 1, 0),
    (2, NULL, '기술 Q&A', 'FREE', 1, 1),
    (2, NULL, '운영 매뉴얼', 'FREE', 1, 0),
    (2, NULL, '전사 자료실', 'FREE', 1, 0),
    -- 회사 2 : 조직 게시판
    (2, 12, 'AI팀 게시판', 'FREE', 1, 1),
    (2, 11, '플랫폼부 게시판', 'FREE', 1, 1);

-- board
INSERT INTO board (board_category_id, employee_id, board_title, board_content, view_count, file_id)
VALUES
    -- 회사 1 : 공용
    (1, 1, '공지: 시스템 점검 안내', '내일 새벽 2시부터 4시까지 전사 시스템 점검이 예정되어 있습니다.', 530, NULL),
    (2, 2, '주말 영화 추천 요청', '주말에 볼 만한 영화 추천 부탁드립니다.', 125, NULL),
    (5, 1, '직원 결혼 소식', '이번 달 우리 회사 직원 결혼 소식입니다. 많은 축하 부탁드립니다.', 880, NULL),
    (4, 3, '이번 주 업무 공유', '각 팀별 이번 주 주요 업무 내용을 공유해주세요.', 20, 4),
    (2, 2, '회사 근처 맛집 공유', '회사 근처 점심 맛집 리스트 공유합니다.', 450, NULL),
    -- 회사 1: 조직
    (6, 4, '2025년 인사 평가 일정 안내', '상반기 인사 평가 일정 공유드립니다.', 120, NULL),
    (6, 4, '연차 사용 가이드', '연차 사용 기준 및 프로세스 안내', 95, 6),
    (7, 5, 'API 성능 개선 논의', 'DB 인덱스 개선 관련 논의합니다.', 80, NULL),
    (7, 5, '배포 자동화 제안', 'CI/CD 개선 제안서 공유', 60, 8),

    -- 회사 2: 공용
    (8, 4, '공지: 서버 점검 안내', '금주 토요일 새벽 서버 점검이 진행될 예정입니다.', 410, NULL),
    (9, 5, '복지 제도 개선 제안', '식대 및 복지 포인트 확대를 제안합니다.', 300, NULL),
    (10, 4, 'API 응답 속도 관련 질문', '특정 API 호출 시 지연이 발생하는 원인이 궁금합니다.', 80, NULL),
    (11, 5, '신규 직원 온보딩 매뉴얼', '신규 입사자 온보딩 절차 문서입니다.', 50, 8),
    (9, 4, '사무실 환경 개선 요청', '회의실 예약 시스템 개선이 필요합니다.', 210, NULL),
    -- 회사 2 : 조직
    (13, 4, '모델 성능 리포트', '12월 모델 정확도 리포트 공유', 150, NULL),
    (13, 4, '데이터 전처리 기준', '전처리 표준 가이드 공유', 90, NULL),
    (14, 5, '서버 증설 계획', '트래픽 증가 대응 서버 증설 계획', 110, NULL),
    (14, 5, '장애 대응 프로세스', '플랫폼 장애 대응 절차 정리', 70, NULL);

# -- board_permission
# INSERT INTO board_permission
#     (employee_id, board_category_id)
# VALUES (2, 1),
#        (1, 3),
#        (3, 2),
#        (1, 4),
#        (2, 5),
#
#        (4, 6),
#        (5, 7),
#        (4, 8),
#        (5, 9),
#        (4, 10);

-- comment
INSERT INTO message
    (company_id, employee_id, message_title, message_content, file_id)
VALUES (1, 1, '회의 자료 요청', '회의 자료를 오늘 오후 5시까지 보내주세요.', NULL),
       (1, 2, '휴가 승인 요청', '내일 오전 반차 승인 부탁드립니다.', NULL),
       (1, 3, '부서 이동 문의', '인사팀 TO 여부 문의드립니다.', NULL),
       (1, 1, '회식 장소 추천', '금요일 회식 장소 추천 부탁드립니다.', NULL),
       (1, 2, '커피 쿠폰 전달', '팀원들에게 커피 쿠폰 전달드립니다.', NULL),

       (2, 4, '신규 프로젝트 안내', 'AI 프로젝트 착수 안내드립니다.', 6),
       (2, 5, '사원증 재발급', '사원증 재발급 요청드립니다.', NULL),
       (2, 4, '주간 보고 요청', '월요일까지 주간 보고 제출 바랍니다.', NULL),
       (2, 5, '휴게실 문의', '냉장고 사용 규정 문의드립니다.', NULL),
       (2, 4, '파일 암호 전달', '첨부 파일 암호 전달드립니다.', 7);


-- message
INSERT INTO message
    (company_id, employee_id, message_title, message_content, file_id)
VALUES (1, 1, '회의 자료 요청', '회의 자료를 오늘 오후 5시까지 보내주세요.', NULL),
       (1, 2, '휴가 승인 요청', '내일 오전 반차 승인 부탁드립니다.', NULL),
       (1, 3, '부서 이동 문의', '인사팀 TO 여부 문의드립니다.', NULL),
       (1, 1, '회식 장소 추천', '금요일 회식 장소 추천 부탁드립니다.', NULL),
       (1, 2, '커피 쿠폰 전달', '팀원들에게 커피 쿠폰 전달드립니다.', NULL),

       (2, 4, '신규 프로젝트 안내', 'AI 프로젝트 착수 안내드립니다.', 6),
       (2, 5, '사원증 재발급', '사원증 재발급 요청드립니다.', NULL),
       (2, 4, '주간 보고 요청', '월요일까지 주간 보고 제출 바랍니다.', NULL),
       (2, 5, '휴게실 문의', '냉장고 사용 규정 문의드립니다.', NULL),
       (2, 4, '파일 암호 전달', '첨부 파일 암호 전달드립니다.', 7);

-- message_recipient
INSERT INTO message_recipient
    (company_id, message_id, employee_id, is_read, read_at)
VALUES (1, 1, 2, 0, NULL),
       (1, 2, 1, 1, NOW()),
       (1, 3, 1, 0, NULL),
       (1, 4, 3, 1, NOW()),
       (1, 5, 3, 1, NOW()),

       (2, 6, 5, 1, NOW()),
       (2, 7, 4, 0, NULL),
       (2, 8, 5, 0, NULL),
       (2, 9, 4, 1, NOW()),
       (2, 10, 5, 0, NULL);


-- =========================================================
-- RESOURCE STATUS
-- =========================================================
-- =========================================================
-- 1. RESOURCE STATUS (가장 먼저 실행)
-- =========================================================
-- ID 매핑: 1=AVAILABLE, 2=INSPECTION, 3=UNAVAILABLE, 4=DELETED, 5=ETC
INSERT INTO resource_status (status_code, status_name)
VALUES ('AVAILABLE', '사용 가능'),
       ('INSPECTION', '점검 중'),
       ('UNAVAILABLE', '사용 불가'),
       ('DELETED', '삭제됨'),
       ('ETC', '기타');

-- =========================================================
-- 2. RESERVATION STATUS (가장 먼저 실행)
-- =========================================================
-- ID 매핑: 1=PENDING, 2=CONFIRM, 3=REJECT, 4=CANCELED, 5=DELETED, 6=ETC
INSERT INTO reservation_status (status_code, status_name)
VALUES ('PENDING', '승인 대기'),
       ('CONFIRM', '예약 확정'),
       ('REJECT', '예약 반려'),
       ('CANCELED', '예약 취소'),
       ('DELETED', '삭제됨'),
       ('ETC', '기타');

-- =========================================================
-- 3. ITEM CATEGORY (Company 1)
-- =========================================================
INSERT INTO item_category (company_id, name)
VALUES (1, '카메라'), -- id = 1
       (1, '모니터'), -- id = 2
       (1, '노트북'), -- id = 3
       (1, '카드 단말기');
-- id = 4

-- =========================================================
-- 4. MEETING ROOM (Company 1)
-- =========================================================
INSERT INTO meetingroom
(company_id, name, position, description, resource_status_id)
VALUES
    (1, '중회의실 B', '본관 2층 203호', '12인 수용, TV 모니터 구비', 1),
    (1, '중회의실 C', '본관 2층 204호', '팀 회의 전용, 원형 테이블', 1),
    (1, '화상 회의실 2', '본관 5층 501호', '화상회의 전용 장비 구축', 1),
    (1, '프로젝트 룸 Beta', '별관 3층 302호', '단기 TF팀 사용 공간', 2),
    (1, '교육실 1', '본관 지하 1층', '사내 교육 및 세미나용', 1),
    (1, '교육실 2', '본관 지하 2층', '신입사원 교육 전용', 1),
    (1, '미팅 부스 A', '본관 6층', '2인용 미팅 부스', 1),
    (1, '미팅 부스 B', '본관 6층', '전화 및 화상 미팅용', 1),
    (1, '임원 회의실', '본관 9층', '임원 전용 회의 공간', 3),
    (1, '아이디어 스튜디오', '별관 1층', '자유 토론 및 기획 공간', 1);


-- =========================================================
-- 5. CAR (Company 1)
-- =========================================================
INSERT INTO car
(company_id, number, name, driver_age, description, resource_status_id, file_id)
VALUES
    (1, '11가1111', 'K5', 26, '영업팀 공용 차량', 1, NULL),
    (1, '22나2222', '쏘나타 DN8', 26, '외근 및 출장용', 1, NULL),
    (1, '33다3333', '투싼 NX4', 26, '팀 단위 이동용', 1, NULL),
    (1, '44라4444', '스포티지', 21, '근거리 업무용', 2, NULL),
    (1, '55마5555', 'EV6', 21, '전기차 시범 운영', 1, NULL),
    (1, '66바6666', '봉고3', 26, '물품 운반용 차량', 1, NULL),
    (1, '77사7777', '스타렉스', 26, '대규모 인원 이동용', 2, NULL),
    (1, '88아8888', '제네시스 GV70', 30, '임원 공용 SUV', 1, NULL),
    (1, '99자9999', '코나 EV', 21, '친환경 차량', 1, NULL),
    (1, '10차1010', 'BMW 520i', 30, '외부 VIP 미팅용', 3, NULL);


-- =========================================================
-- 6. ITEM (Company 1)
-- =========================================================
INSERT INTO item
(company_id, item_category_id, name, description, resource_status_id, file_id)
VALUES
    (1, 3, 'MacBook Air 15 (M3)', '기획팀 지급용 노트북', 1, NULL),
    (1, 3, 'MacBook Pro 14 (M3)', '개발팀 공용 테스트 장비', 1, NULL),
    (1, 2, '삼성 스마트 모니터 M8', '회의실 공용 디스플레이', 1, NULL),
    (1, 2, 'LG 듀얼업 모니터', '개발자 세로형 모니터', 1, NULL),
    (1, 4, '윈도우 노트북 A', '단기 파견 인력 대여용', 2, NULL),
    (1, 4, '윈도우 노트북 B', '외부 협력사 대여용', 3, NULL),
    (1, 1, '후지 x100v', '스냅샷 촬영용', 1, NULL),
    (1, 1, '캐논 550d', '영상 촬영용', 1, NULL),
    (1, 2, '고성능 모니터', '진짜 좋음', 1, NULL),
    (1, 3, 'LG 그램', '신규 입사자 지급용', 1, NULL);


-- =========================================================
-- 7. RESERVATION (Company 1)
-- =========================================================
-- reservation_status_id: 1=PENDING, 2=CONFIRM, 3=REJECT
INSERT INTO reservation
(company_id, employee_id, type_code, item_category_id, resource_id,
 reservation_date, start_time, end_time,
 reservation_reason, reject_reason, reservation_status_id)
VALUES
-- 회의실 예약 (resource_id=1, CONFIRM=2)
(1, 1, 'MEETING', NULL, 1,
 CURDATE() + INTERVAL 1 DAY, 10, 12,
 '주간 개발 팀 정기 회의', NULL, 2),
-- 차량 예약 (resource_id=2, PENDING=1)
(1, 1, 'CAR', NULL, 2,
 CURDATE() + INTERVAL 2 DAY, 9, 18,
 '외부 파트너사 미팅 및 장비 운송', NULL, 1),
-- 비품 예약 (노트북, category_id=4, resource_id=3, CONFIRM=2)
(1, 1, 'ITEM', 4, 3,
 CURDATE() + INTERVAL 3 DAY, 13, 17,
 '모바일 앱 신규 기능 QA 테스트', NULL, 2),
-- 회의실 예약 반려 (resource_id=4, REJECT=3)
(1, 1, 'MEETING', NULL, 4,
 CURDATE() + INTERVAL 4 DAY, 14, 15,
 '신규 기획 브레인스토밍', '해당 시간대 시설 긴급 점검 예정', 3),
-- 비품 예약 (모니터, category_id=2, resource_id=4, CONFIRM=2)
(1, 1, 'ITEM', 2, 4,
 CURDATE() + INTERVAL 5 DAY, 9, 18,
 '사내 스터디 발제 준비를 위한 대여', NULL, 2);

-- =========================================================
-- 8. SCHEDULE (Company 1)
-- =========================================================
INSERT INTO schedule
(company_id, category_id, org_id, type, employee_id, schedule_title, schedule_description, start_date, end_date,
 start_time, end_time, schedule_status)
VALUES
-- [COMPANY] 전사 일정 (창립기념일)
(1, NULL, NULL, 'COMPANY', 1, '창립기념일 행사', '전사 휴무 및 오전 기념식 진행',
 CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 10 DAY, 9, 12, 'RELEASE'),
-- [COMPANY] 전사 일정 (타운홀 미팅)
(1, NULL, NULL, 'COMPANY', 1, '4분기 전사 타운홀 미팅', '분기 실적 발표 및 Q&A 세션',
 CURDATE() + INTERVAL 5 DAY, CURDATE() + INTERVAL 5 DAY, 14, 16, 'RELEASE'),
-- [PERSONAL] 개인 일정 (연차)
(1, NULL, NULL, 'PERSONAL', 1, '개인 연차 휴가', '가족 여행',
 CURDATE() + INTERVAL 3 DAY, CURDATE() + INTERVAL 4 DAY, 9, 18, 'RELEASE'),
-- [PERSONAL] 개인 일정 (병원)
(1, NULL, NULL, 'PERSONAL', 2, '건강검진', '오전 반차 사용 예정',
 CURDATE() + INTERVAL 7 DAY, CURDATE() + INTERVAL 7 DAY, 8, 13, 'RELEASE'),
-- [PERSONAL] 개인 일정 (미팅) - DELETED
(1, NULL, NULL, 'PERSONAL', 3, '외부 멘토링 세션', '멘토님 사정으로 취소됨',
 CURDATE() + INTERVAL 1 DAY, CURDATE() + INTERVAL 1 DAY, 19, 21, 'DELETED');

-- =========================================================
-- 9. SCHEDULE SUMMARY (Company 1)
-- =========================================================
INSERT INTO schedule_summary
    (company_id, employee_id, week_summary, month_summary)
VALUES (1, 1,
        '12월 3주차 주간 요약: 이번 주는 전사 창립기념일 행사와 개발팀 주간 스프린트 회의가 주요 일정이었습니다.',
        '12월 월간 요약: 인증/인가 모듈(Spring Security) 개발 완료 및 주요 프로젝트 진행.');


-- =========================================================
-- [추가] ITEM CATEGORY (Company 2)
-- =========================================================
-- auto_increment로 인해 5, 6, 7, 8, 9번 ID 할당 가정
INSERT INTO item_category (company_id, name)
VALUES (2, '태블릿'),   -- id = 5
       (2, '서버 장비'), -- id = 6
       (2, '촬영 장비'), -- id = 7
       (2, '사무 가구'), -- id = 8
       (2, '소프트웨어');
-- id = 9

-- =========================================================
-- [추가] MEETING ROOM (Company 2)
-- =========================================================
INSERT INTO meetingroom
(company_id, name, position, description, resource_status_id)
VALUES
    (2, '전략 회의실', '글로벌 센터 501호', '임원 전용 회의실, 최고급 화상 장비', 1),     -- AVAILABLE
    (2, '아이디어 랩', '글로벌 센터 3층 휴게공간', '전면 화이트보드 벽면, 자유로운 분위기', 1), -- AVAILABLE
    (2, '세미나실 B', '글로벌 센터 지하 1층', '50인 수용 가능, 강연대 및 마이크 설비', 2),  -- INSPECTION
    (2, '화상 미팅룸 1', '글로벌 센터 505호', '1인용 포커스 룸, 방음 부스', 1),        -- AVAILABLE
    (2, '프로젝트 룸 Alpha', '글로벌 센터 401호', 'TF팀 전용 장기 대관실', 3);         -- UNAVAILABLE


-- =========================================================
-- [추가] CAR (Company 2)
-- =========================================================
INSERT INTO car
(company_id, number, name, driver_age, description, resource_status_id, file_id)
VALUES
    (2, '99호1111', '벤츠 E-Class', 30, 'CEO 의전 차량', 1, NULL),                 -- AVAILABLE
    (2, '88하2222', '쏘렌토 MQ4', 26, '영업 1팀 공용 차량', 1, NULL),              -- AVAILABLE
    (2, '77호3333', '레이 EV', 21, '근거리 문서 수발신 및 마트 장보기용', 2, NULL), -- INSPECTION
    (2, '66하4444', '스타리아 라운지', 26, '공항 픽업 및 바이어 접대용 (7인승)', 1, NULL), -- AVAILABLE
    (2, '55호5555', '테슬라 Model Y', 26, 'IT 사업부 테스트 및 업무용', 3, NULL);  -- UNAVAILABLE


-- =========================================================
-- [추가] ITEM (Company 2)
-- =========================================================
-- category_id는 위에서 생성된 순서(5,6,7,8,9) 매핑
INSERT INTO item
(company_id, item_category_id, name, description, resource_status_id, file_id)
VALUES
    (2, 5, 'iPad Pro 12.9 (6세대)', '디자인 시안 검토용 태블릿', 1, NULL),
    (2, 6, 'Dell PowerEdge R750', '사내 테스트 서버, 전산실 B구역 위치', 1, NULL),
    (2, 7, 'Sony Alpha 7 IV', '마케팅팀 유튜브 촬영용 카메라', 2, NULL),
    (2, 8, '허먼밀러 에어론', '허리 디스크 환자 전용 의자 (신청 필요)', 3, NULL),
    (2, 9, 'JetBrains All Products', '개발팀 공용 라이선스 계정', 1, NULL);

-- =========================================================
-- [추가] RESERVATION (Company 2)
-- =========================================================
-- Resource ID 가정: Meetingroom(6~10), Car(6~10), Item(5~9)
-- Status ID: 1=PENDING, 2=CONFIRM, 3=REJECT
INSERT INTO reservation
(company_id, employee_id, type_code, item_category_id, resource_id,
 reservation_date, start_time, end_time,
 reservation_reason, reject_reason, reservation_status_id)
VALUES
-- 회의실 예약 (전략 회의실: resource_id=6, CONFIRM=2)
(2, 4, 'MEETING', NULL, 6,
 CURDATE() + INTERVAL 2 DAY, 14, 16,
 '내년도 사업 계획 보고', NULL, 2),
-- 차량 예약 (쏘렌토: resource_id=7, PENDING=1)
(2, 5, 'CAR', NULL, 7,
 CURDATE() + INTERVAL 3 DAY, 9, 18,
 '지방 공장 실사 방문', NULL, 1),
-- 비품 예약 (iPad: resource_id=5, category_id=5, CONFIRM=2)
(2, 4, 'ITEM', 5, 5,
 CURDATE() + INTERVAL 1 DAY, 10, 18,
 '외부 미팅 시안 프레젠테이션', NULL, 2),
-- 회의실 예약 (반려됨, resource_id=7, REJECT=3)
(2, 5, 'MEETING', NULL, 7,
 CURDATE() + INTERVAL 5 DAY, 13, 15,
 '점심 회식 후 티타임', '업무 외 목적 사용 불가', 3),
-- 비품 예약 (카메라: resource_id=7, category_id=7, CONFIRM=2)
(2, 5, 'ITEM', 7, 7,
 CURDATE() + INTERVAL 10 DAY, 9, 18,
 '사내 브이로그 촬영', NULL, 2);

-- =========================================================
-- [추가] SCHEDULE (Company 2)
-- =========================================================
INSERT INTO schedule
(company_id, category_id, org_id, type, employee_id, schedule_title, schedule_description, start_date, end_date,
 start_time, end_time, schedule_status)
VALUES
-- [COMPANY] 전사 일정
(2, NULL, NULL, 'COMPANY', 4, '전사 체육대회', '잠실 보조경기장 집결',
 CURDATE() + INTERVAL 20 DAY, CURDATE() + INTERVAL 20 DAY, 9, 18, 'RELEASE'),
-- [COMPANY] 전사 일정
(2, NULL, NULL, 'COMPANY', 4, '건강검진 기간 안내', '지정 병원 예약 필수',
 CURDATE(), CURDATE() + INTERVAL 30 DAY, 9, 18, 'RELEASE'),
-- [PERSONAL] 개인 일정 (사원 4)
(2, NULL, NULL, 'PERSONAL', 4, '재택 근무', '집중 근무일 신청',
 CURDATE() + INTERVAL 2 DAY, CURDATE() + INTERVAL 2 DAY, 9, 18, 'RELEASE'),
-- [PERSONAL] 개인 일정 (사원 5)
(2, NULL, NULL, 'PERSONAL', 5, '치과 예약', '오후 반차 사용',
 CURDATE() + INTERVAL 5 DAY, CURDATE() + INTERVAL 5 DAY, 14, 18, 'RELEASE'),
-- [PERSONAL] 개인 일정 (사원 5 - 삭제됨)
(2, NULL, NULL, 'PERSONAL', 5, '저녁 약속', '친구 결혼식 뒤풀이 (취소됨)',
 CURDATE() + INTERVAL 7 DAY, CURDATE() + INTERVAL 7 DAY, 19, 22, 'DELETED');

-- =========================================================
-- [추가] SCHEDULE SUMMARY (Company 2)
-- =========================================================
INSERT INTO schedule_summary
    (company_id, employee_id, week_summary, month_summary)
VALUES (2, 4, '12월 2주차: 전사 체육대회 준비 위원회 참석 및 재택 근무 진행.', '12월 요약: 체육대회 기획 완료 및 개인 업무 목표 90% 달성.'),
       (2, 5, '12월 2주차: 지방 공장 실사 및 보고서 작성 완료.', '12월 요약: 현장 방문 일정 위주로 소화, 건강 문제로 인한 반차 사용 1회.');

-- 1. attendance_rule (근태 규칙)
INSERT INTO attendance_rule (company_id, name, default_start_time, default_end_time, default_break_minutes,
                             late_threshold_min, early_leave_threshold_min, is_default)
VALUES (1, '표준 9-6 근무', '09:00:00', '18:00:00', 60, 10, 10, TRUE),
       (1, '유연 근무 A (10-7)', '10:00:00', '19:00:00', 60, 15, 15, FALSE);


-- 2. employee_att_rule (직원 예외 규칙)
INSERT INTO employee_att_rule (company_id, employee_id, start_time, end_time, break_minutes, reason, valid_from,
                               valid_to, applied_at, is_active)
VALUES (1, 2, '10:00:00', '19:00:00', 60, '개인 사유로 인한 유연 근무 신청', CURDATE() - INTERVAL 15 DAY,
        CURDATE() + INTERVAL 15 DAY, NOW(), TRUE),
       (1, 3, '09:30:00', '18:30:00', 30, '단기 프로젝트 투입으로 인한 시간 조정', CURDATE(), CURDATE() + INTERVAL 30 DAY, NOW(),
        TRUE),
       (1, 2, NULL, NULL, 90, '육아 시간 조정 (휴게 90분 적용)', CURDATE() + INTERVAL 10 DAY, CURDATE() + INTERVAL 40 DAY,
        NOW(), TRUE);


-- 3. attendance (근태 기록)
-- 기존 테스트 데이터와 충돌을 방지하려면 아래 주석을 해제하고 실행하세요.
-- DELETE FROM attendances WHERE employee_id = 3 AND work_date BETWEEN '2025-12-01' AND '2025-12-20';

INSERT INTO attendance
(company_id, employee_id, work_date, commute_at, leave_at, status, is_corrected)
VALUES
    (1, 3, '2025-12-01', '2025-12-01 08:52:00', '2025-12-01 18:05:00', 'ON_TIME', 0),
    (1, 3, '2025-12-02', '2025-12-02 09:15:00', '2025-12-02 18:10:00', 'LATE', 0),
    (1, 3, '2025-12-03', '2025-12-03 08:45:00', '2025-12-03 18:00:00', 'ON_TIME', 0),
    (1, 3, '2025-12-04', NULL, NULL, 'ABSENT', 0), -- 결근
    (1, 3, '2025-12-05', '2025-12-05 08:58:00', '2025-12-05 18:30:00', 'ON_TIME', 0),
    (1, 3, '2025-12-06', NULL, NULL, 'BEFORE_WORK', 0), -- 주말/출근 전
    (1, 3, '2025-12-07', NULL, NULL, 'BEFORE_WORK', 0),
    (1, 3, '2025-12-08', '2025-12-08 09:20:00', '2025-12-08 18:00:00', 'LATE', 0),
    (1, 3, '2025-12-09', '2025-12-09 08:50:00', '2025-12-09 18:05:00', 'ON_TIME', 0),
    (1, 3, '2025-12-10', '2025-12-10 08:59:00', '2025-12-10 18:15:00', 'ON_TIME', 0),
-- 2페이지 데이터 (10개 단위 페이징 테스트용)
    (1, 3, '2025-12-11', '2025-12-11 09:05:00', '2025-12-11 18:00:00', 'LATE', 0),
    (1, 3, '2025-12-12', '2025-12-12 08:40:00', '2025-12-12 18:05:00', 'ON_TIME', 0),
    (1, 3, '2025-12-13', NULL, NULL, 'ABSENT', 0),
    (1, 3, '2025-12-14', NULL, NULL, 'ABSENT', 0),
    (1, 3, '2025-12-15', '2025-12-15 08:50:00', '2025-12-15 18:10:00', 'ON_TIME', 0),
    (1, 3, '2025-12-16', '2025-12-16 09:12:00', '2025-12-16 18:05:00', 'LATE', 0),
    (1, 3, '2025-12-17', '2025-12-17 08:55:00', '2025-12-17 18:00:00', 'ON_TIME', 0),
    (1, 3, '2025-12-18', NULL, NULL, 'ABSENT', 0),
    (1, 3, '2025-12-19', '2025-12-19 08:58:00', '2025-12-19 18:05:00', 'ON_TIME', 0),
    (1, 3, '2025-12-20', '2025-12-20 09:02:00', '2025-12-20 18:10:00', 'LATE', 0);

-- 4. correction_history (근태 정정 이력)
INSERT INTO correction_history (attendance_id, original_commute_at, original_leave_at, corrected_commute_at,
                                corrected_leave_at, correction_reason, correction_status, processed_by, processed_at,
                                rejection_reason)
VALUES (4, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 09:00:00'), NULL, CONCAT(CURDATE() - INTERVAL 1 DAY, ' 09:00:00'),
        CONCAT(CURDATE() - INTERVAL 1 DAY, ' 18:10:00'), '퇴근 시 카드 태그 누락', 'APPROVED', 1, NOW(), NULL),
       (2, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 18:00:00'),
        CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 17:30:00'),
        '긴급 치과 방문으로 인한 조퇴', 'PENDING', NULL, NULL, NULL),
       (1, CONCAT(CURDATE() - INTERVAL 2 DAY, ' 10:05:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 19:00:00'),
        CONCAT(CURDATE() - INTERVAL 2 DAY, ' 09:00:00'), CONCAT(CURDATE() - INTERVAL 2 DAY, ' 19:00:00'), '출근 기록 오류',
        'REJECTED', 1, NOW(), '지각 기록 명확함');


-- 5. leave_balance (휴가 잔여 일수)
INSERT INTO leave_balance (company_id, employee_id, year, total_granted, remaining_days)
VALUES (1, 1, 2026, 15.00, 15.00),
       (1, 2, 2026, 15.00, 12.50),
       (1, 3, 2026, 10.00, 9.00);


-- 7. manual_category (매뉴얼 카테고리)
INSERT INTO manual_category (company_id, category_name, description, is_active, sort_order)
VALUES (1, '근태 관리', '출퇴근, 휴가, 재택 정책 안내', TRUE, 10),
       (1, '결재/증명서', '전자결재 사용법 및 양식 가이드', TRUE, 20),
       (1, '자원 관리', '회의실, 차량, 비품 예약 정책', TRUE, 30),
       (1, '급여/복지', '급여일, 상여금, 복지 제도 안내', TRUE, 40),
       (1, '신규 입사자', '온보딩 가이드 및 필수 정보', TRUE, 50),
       (1, 'IT 가이드', '사내 시스템 및 보안 정책', TRUE, 60);


-- 8. manual_link (매뉴얼 연결)
-- File ID (1~5)는 이전 섹션의 더미 데이터를 가정합니다.
INSERT INTO manual_link (file_id, category_id, company_id, status, is_active, vectorized_at)
VALUES (1, 1, 1, 'READY', TRUE, NOW()),
       (2, 2, 1, 'READY', TRUE, NOW()),
       (3, 3, 1, 'READY', TRUE, NOW()),
       (4, 4, 1, 'FAILED', TRUE, NULL),
       (5, 5, 1, 'PROCESSING', TRUE, NULL),
       (5, 1, 1, 'UPLOADED', FALSE, NULL);


-- 9. grant_history (연차 부여 이력)
INSERT INTO grant_history (employee_id, company_id, grant_date, granted_days, grant_type, expiration_date, is_expired,
                           created_at)
VALUES (2, 1, '2026-01-01', 15.00, 'ANNUAL', '2027-01-31', FALSE, NOW()),
       (3, 1, '2026-01-01', 10.00, 'ANNUAL', '2027-01-31', FALSE, NOW()),
       (2, 1, '2025-07-01', 5.00, 'PERIODIC', '2026-06-30', FALSE, NOW()),
       (3, 1, '2024-01-01', 15.00, 'ANNUAL', '2025-01-31', TRUE, NOW()),
       (1, 1, '2026-01-01', 15.00, 'ANNUAL', '2027-01-31', FALSE, NOW()),
       (1, 1, '2025-03-01', 3.00, 'COMPENSATION', '2026-03-31', FALSE, NOW());