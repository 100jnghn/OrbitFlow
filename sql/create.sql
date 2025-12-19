drop database if exists orbitflow;
create database orbitflow;

use orbitflow;

CREATE TABLE company
(
    id                     BIGINT AUTO_INCREMENT PRIMARY KEY,
    name                   VARCHAR(100) NOT NULL UNIQUE,
    business_number        VARCHAR(20)  NOT NULL UNIQUE,
    address                VARCHAR(255) NOT NULL,
    representative_name    VARCHAR(50)  NOT NULL,
    representative_contact VARCHAR(20)  NOT NULL,
    created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;

CREATE TABLE org_category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT      NOT NULL,
    name        VARCHAR(50) NOT NULL,
    order_index INT         NOT NULL,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT uk_org_category_company_order -- orderIndex 유니크 제약 추가
        UNIQUE (company_id, order_index),

    CONSTRAINT fk_org_category_company
        FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE = InnoDB;

CREATE TABLE organization
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id    BIGINT       NOT NULL,
    category_id   BIGINT       NOT NULL,
    parent_org_id BIGINT       NULL,
    name          VARCHAR(100) NOT NULL,
    order_index   INT          NOT NULL,
    is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_org_company
        FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_org_category
        FOREIGN KEY (category_id) REFERENCES org_category (id),
    CONSTRAINT fk_org_parent
        FOREIGN KEY (parent_org_id) REFERENCES organization (id)
) ENGINE = InnoDB;

CREATE TABLE hr_rank
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id        BIGINT      NOT NULL,
    parent_hr_rank_id BIGINT      NULL,
    name              VARCHAR(50) NOT NULL,
    order_index       INT         NOT NULL,
    is_active         BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_hr_rank_company
        FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_hr_rank_parent
        FOREIGN KEY (parent_hr_rank_id) REFERENCES hr_rank (id)
) ENGINE = InnoDB;

CREATE TABLE position_category
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT      NOT NULL,
    name        VARCHAR(50) NOT NULL,
    order_index INT         NOT NULL,
    is_active   BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_pos_cat_company
        FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE = InnoDB;

CREATE TABLE position
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id         BIGINT      NOT NULL,
    category_id        BIGINT      NOT NULL,
    parent_position_id BIGINT      NULL,
    name               VARCHAR(50) NOT NULL,
    order_index        INT         NOT NULL,
    is_active          BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_position_company
        FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_position_category
        FOREIGN KEY (category_id) REFERENCES position_category (id),
    CONSTRAINT fk_position_parent
        FOREIGN KEY (parent_position_id) REFERENCES position (id)
) ENGINE = InnoDB;

CREATE TABLE org_position_usage
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT    NOT NULL,
    org_id      BIGINT    NOT NULL,
    position_id BIGINT    NOT NULL,
    is_enabled  BOOLEAN   NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_usage_company
        FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_usage_org
        FOREIGN KEY (org_id) REFERENCES organization (id),
    CONSTRAINT fk_usage_position
        FOREIGN KEY (position_id) REFERENCES position (id)
) ENGINE = InnoDB;

CREATE TABLE employee
(
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,                                                   -- 사원 아이디
    company_id      BIGINT                                           NOT NULL,                           -- 회사 아이디 (fk)
    employee_no     VARCHAR(20)                                      NOT NULL,                           -- 사번
    internal_phone  VARCHAR(20),                                                                         -- 내선번호
    phone           VARCHAR(20),                                                                         -- 전화번호
    org_id          BIGINT                                           NOT NULL,                           -- 조직 아이디 (fk)
    hr_rank_id      BIGINT                                           NULL,                               -- 직급 아이디 (fk)
    position_id     BIGINT                                           NULL,                               -- 직책 아이디 (fk)

    name            VARCHAR(50)                                      NOT NULL,                           -- 이름
    email           VARCHAR(100)                                     NOT NULL,                           -- 이메일 (로그인 id)
    password        VARCHAR(255)                                     NOT NULL,                           -- 비밀번호
    role            ENUM ('COMPANY_ADMIN', 'ADMIN', 'EMPLOYEE')      NOT NULL DEFAULT 'EMPLOYEE',
    gender          ENUM ('MALE', 'FEMALE')                          NOT NULL,                           -- 성별
    birth_date      DATE,                                                                                -- 생년월일
    employment_type ENUM ('REGULAR', 'NON_REGULAR')                  NOT NULL,                           -- 고용 형태
    status          ENUM ('TEMP', 'ACTIVE', 'SUSPENDED', 'RESIGNED') NOT NULL,                           -- 재직 상태

    work_status     ENUM ('WORKING', 'AWAY', 'ON_LEAVE', 'OFF_WORK')
                                                                     NOT NULL DEFAULT 'OFF_WORK',        -- 근무 상태

    created_at      TIMESTAMP                                        NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 생성일시
    updated_at      TIMESTAMP                                        NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    UNIQUE KEY uk_employee_company_no (company_id, employee_no),
    UNIQUE KEY uk_employee_email (email),

    CONSTRAINT fk_emp_company
        FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_emp_org
        FOREIGN KEY (org_id) REFERENCES organization (id),
    CONSTRAINT fk_emp_hr_rank
        FOREIGN KEY (hr_rank_id) REFERENCES hr_rank (id),
    CONSTRAINT fk_emp_position
        FOREIGN KEY (position_id) REFERENCES position (id)
) ENGINE = InnoDB;


CREATE TABLE log_audit
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id        BIGINT      NOT NULL,
    actor_employee_id BIGINT      NOT NULL,
    entity_type       VARCHAR(30) NOT NULL,
    entity_id         BIGINT      NOT NULL,
    event_type        VARCHAR(30) NOT NULL,
    before_data       JSON        NULL,
    after_data        JSON        NULL,
    created_at        TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_audit_company
        FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_audit_actor
        FOREIGN KEY (actor_employee_id) REFERENCES employee (id)
) ENGINE = InnoDB;

CREATE TABLE notification
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT       NOT NULL,
    employee_id BIGINT       NOT NULL,
    type        VARCHAR(30)  NOT NULL,
    content     VARCHAR(255) NOT NULL,
    is_read     BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_notification_company
        FOREIGN KEY (company_id) REFERENCES company (id),
    CONSTRAINT fk_notification_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id)
) ENGINE = InnoDB;

CREATE TABLE refresh_token
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT       NOT NULL,
    employee_id BIGINT       NOT NULL,
    token       VARCHAR(500) NOT NULL UNIQUE ,
    expires_at  TIMESTAMP    NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id),
    CONSTRAINT fk_refresh_company
        FOREIGN KEY (company_id) REFERENCES company (id)

);


-- =========================================================
-- 1. LEAVE TYPE
-- =========================================================
CREATE TABLE leave_type
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    type_name    VARCHAR(50) NOT NULL,
    is_countable BOOLEAN     NOT NULL,
    description  VARCHAR(255),
    created_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB;


-- =========================================================
-- 2. TEMPLATE CATEGORY
-- =========================================================
CREATE TABLE template_category
(
    id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(50)  NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL
) ENGINE = InnoDB;


-- =========================================================
-- 3. FORM TEMPLATE GROUP
-- =========================================================
CREATE TABLE form_template_group
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,

    company_id  BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description TEXT,

    active      BOOLEAN      NOT NULL DEFAULT TRUE,

    created_by  BIGINT       NULL,
    modified_by BIGINT       NULL,

    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_ftg_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_ftg_created_by
        FOREIGN KEY (created_by)
            REFERENCES employee (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_ftg_modified_by
        FOREIGN KEY (modified_by)
            REFERENCES employee (id)
            ON DELETE SET NULL,

    CONSTRAINT uq_form_template_group_company_name
        UNIQUE (company_id, name)
) ENGINE = InnoDB;



-- =========================================================
-- 4. FORM TEMPLATE
-- =========================================================
CREATE TABLE form_template
(
    id                   BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id           BIGINT                             NOT NULL,
    template_group_id    BIGINT                             NOT NULL,
    version              INT                                NOT NULL,
    template_category_id BIGINT                             NOT NULL,

    status               ENUM ('DRAFT','ACTIVE','INACTIVE') NOT NULL,

    affect_tags          JSON,
    template_json        JSON                               NOT NULL,
    approval_rule_json   JSON                               NULL,

    created_at           TIMESTAMP                          NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_template_group_company_version
        UNIQUE (company_id, template_group_id, version),

    CONSTRAINT fk_ft_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_ft_group
        FOREIGN KEY (template_group_id)
            REFERENCES form_template_group (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_ft_category
        FOREIGN KEY (template_category_id)
            REFERENCES template_category (id)
            ON DELETE RESTRICT
) ENGINE = InnoDB;

CREATE INDEX idx_ft_company_status
    ON form_template (company_id, status);

CREATE INDEX idx_ft_group_status_version
    ON form_template (template_group_id, status, version);


-- =========================================================
-- 5. FORM TEMPLATE AI LOG
-- =========================================================
CREATE TABLE log_form_template_ai
(
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,

    company_id              BIGINT                  NOT NULL,
    template_group_id       BIGINT                  NULL,
    created_template_id     BIGINT                  NULL,

    prompt                  TEXT                    NOT NULL,
    generated_template_json JSON,
    generated_rule_json     JSON,

    model                   VARCHAR(50),
    status                  ENUM ('SUCCESS','FAIL') NOT NULL,
    error_message           TEXT,

    created_by              BIGINT                  NULL,
    created_at              TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,

    /* =========================
       Foreign Keys
       ========================= */

    CONSTRAINT fk_log_ai_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_log_ai_group
        FOREIGN KEY (template_group_id)
            REFERENCES form_template_group (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_log_ai_template
        FOREIGN KEY (created_template_id)
            REFERENCES form_template (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_log_ai_employee
        FOREIGN KEY (created_by)
            REFERENCES employee (id)
            ON DELETE SET NULL

) ENGINE = InnoDB;

CREATE INDEX idx_log_ai_company_created_at
    ON log_form_template_ai (company_id, created_at DESC);

CREATE INDEX idx_log_ai_group
    ON log_form_template_ai (template_group_id);

CREATE INDEX idx_log_ai_status
    ON log_form_template_ai (status);


-- =========================================================
-- 6. DOCUMENT (Soft Delete)
-- =========================================================
CREATE TABLE document
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id         BIGINT                                                         NOT NULL,
    template_group_id  BIGINT                                                         NOT NULL,
    template_version   INT                                                            NOT NULL,
    writer_id          BIGINT                                                         NULL,
    title              VARCHAR(255)                                                   NOT NULL,
    status             ENUM ('DRAFT','SUBMITTED','IN_PROGRESS','APPROVED','REJECTED') NOT NULL,
    before_document_id BIGINT                                                         NULL,
    is_deleted         BOOLEAN                                                        NOT NULL DEFAULT FALSE,
    created_at         TIMESTAMP                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_doc_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_doc_template_group
        FOREIGN KEY (template_group_id)
            REFERENCES form_template_group (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_doc_writer
        FOREIGN KEY (writer_id)
            REFERENCES employee (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_doc_before
        FOREIGN KEY (before_document_id)
            REFERENCES document (id)
            ON DELETE SET NULL
) ENGINE = InnoDB;


CREATE INDEX idx_doc_company_created
    ON document (company_id, created_at DESC);

CREATE INDEX idx_doc_writer_status
    ON document (writer_id, status);

CREATE INDEX idx_doc_company_status
    ON document (company_id, status);

CREATE INDEX idx_doc_template
    ON document (template_group_id, template_version);


-- =========================================================
-- 7. DOCUMENT CONTENT
-- =========================================================
CREATE TABLE document_content
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id  BIGINT    NOT NULL UNIQUE,
    content_json JSON      NOT NULL,
    created_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_doc_content
        FOREIGN KEY (document_id)
            REFERENCES document (id)
            ON DELETE CASCADE
) ENGINE = InnoDB;


-- =========================================================
-- 8. APPROVAL LINE (결재선 + 이력 통합)
-- =========================================================
CREATE TABLE approval_line
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id BIGINT                                                         NOT NULL,
    company_id  BIGINT                                                         NOT NULL,
    approver_id BIGINT                                                         NULL,
    order_no    INT                                                            NOT NULL,
    status      ENUM ('DRAFT','SUBMITTED','IN_PROGRESS','APPROVED','REJECTED') NOT NULL,
    comment     TEXT,
    decided_at  TIMESTAMP,
    CONSTRAINT uk_al_document_order
        UNIQUE (document_id, order_no),

    CONSTRAINT fk_al_document
        FOREIGN KEY (document_id)
            REFERENCES document (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_al_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_al_approver
        FOREIGN KEY (approver_id)
            REFERENCES employee (id)
            ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_al_document
    ON approval_line (document_id);

CREATE INDEX idx_al_approver_status
    ON approval_line (approver_id, status);

CREATE INDEX idx_al_company
    ON approval_line (company_id);

-- =========================================================
-- 9. DOCUMENT AI SUMMARY
-- =========================================================
CREATE TABLE document_ai_summary
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    document_id        BIGINT                  NOT NULL,
    company_id         BIGINT                  NOT NULL,
    summary_type       ENUM ('CONTENT','DIFF') NOT NULL,
    content            TEXT                    NOT NULL,
    model              VARCHAR(50),
    before_document_id BIGINT                  NULL,
    created_at         TIMESTAMP               NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_doc_ai_summary
        UNIQUE (document_id, summary_type),

    CONSTRAINT fk_ai_doc
        FOREIGN KEY (document_id)
            REFERENCES document (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_ai_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_ai_before_doc
        FOREIGN KEY (before_document_id)
            REFERENCES document (id)
            ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_ai_summary_document
    ON document_ai_summary (document_id);

CREATE INDEX idx_ai_summary_company
    ON document_ai_summary (company_id);

CREATE INDEX idx_ai_summary_type
    ON document_ai_summary (summary_type);


-- =========================================================
-- 10. ATTENDANCE RECORD
-- =========================================================
CREATE TABLE attendance_record
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id        BIGINT                                                         NULL,
    company_id         BIGINT                                                         NOT NULL,
    start_date         DATE                                                           NOT NULL,
    end_date           DATE                                                           NOT NULL,
    days               DECIMAL(4, 1)                                                  NOT NULL,
    type_id            BIGINT                                                         NOT NULL,
    reason             VARCHAR(255),
    source_document_id BIGINT                                                         NULL,
    status             ENUM ('DRAFT','SUBMITTED','IN_PROGRESS','APPROVED','REJECTED') NOT NULL,
    approved_at        TIMESTAMP,
    created_at         TIMESTAMP                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP                                                      NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    CONSTRAINT fk_att_employee
        FOREIGN KEY (employee_id)
            REFERENCES employee (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_att_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_att_document
        FOREIGN KEY (source_document_id)
            REFERENCES document (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_att_leave_type
        FOREIGN KEY (type_id)
            REFERENCES leave_type (id)
            ON DELETE RESTRICT
            ON UPDATE CASCADE
) ENGINE = InnoDB;

CREATE INDEX idx_att_company_period
    ON attendance_record (company_id, start_date, end_date);

CREATE INDEX idx_att_employee_period
    ON attendance_record (employee_id, start_date, end_date);

CREATE INDEX idx_att_document
    ON attendance_record (source_document_id);

CREATE INDEX idx_att_status
    ON attendance_record (status);


-- =========================================================
-- 11. FILE
-- =========================================================
CREATE TABLE file
(
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id   BIGINT       NOT NULL,
    object_key   VARCHAR(512) NOT NULL UNIQUE,
    origin_file  VARCHAR(255),
    sys_file     VARCHAR(255),
    content_type VARCHAR(50),
    file_size    BIGINT,
    created_by   BIGINT       NULL,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_file_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_file_employee
        FOREIGN KEY (created_by)
            REFERENCES employee (id)
            ON DELETE SET NULL
) ENGINE = InnoDB;

CREATE INDEX idx_file_company_created
    ON file (company_id, created_at DESC);

CREATE INDEX idx_file_created_by
    ON file (created_by);


-- =========================================================
-- 12. DOCUMENT FILE
-- =========================================================
CREATE TABLE document_file
(
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,

    document_id         BIGINT                               NOT NULL,
    file_id             BIGINT                               NULL,

    reference_type      ENUM ('ATTACHMENT','DOCUMENT','URL') NOT NULL,
    reference_target_id BIGINT                               NULL,
    reference_url       VARCHAR(255),


    CONSTRAINT fk_df_document
        FOREIGN KEY (document_id)
            REFERENCES document (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_df_file
        FOREIGN KEY (file_id)
            REFERENCES file (id)
            ON DELETE SET NULL,

    CONSTRAINT uk_df_document_file
        UNIQUE (document_id, file_id),

    CONSTRAINT uk_df_document_reference
        UNIQUE (document_id, reference_type, reference_target_id)

) ENGINE = InnoDB;


/* =========================================================
   SIGNATURE (전자 서명)
========================================================= */
CREATE TABLE employee_signature
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,

    employee_id BIGINT    NOT NULL,
    company_id  BIGINT    NOT NULL,
    file_id     BIGINT    NOT NULL, -- 서명 이미지 (S3)

    is_active   BOOLEAN   NOT NULL DEFAULT TRUE,

    -- 활성 서명만 UNIQUE 제약을 걸기 위한 가상 컬럼
    active_flag TINYINT
                GENERATED ALWAYS AS (
                    CASE
                        WHEN is_active = TRUE THEN 1
                        ELSE NULL
                        END
                    ),

    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_emp_sig_employee
        FOREIGN KEY (employee_id)
            REFERENCES employee (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_emp_sig_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_emp_sig_file
        FOREIGN KEY (file_id)
            REFERENCES file (id)
            ON DELETE RESTRICT,

    -- 사원 + 회사 기준 활성 서명은 1개만 허용
    CONSTRAINT uk_emp_sig_active
        UNIQUE (employee_id, company_id, active_flag)

) ENGINE = InnoDB;



CREATE TABLE document_signature
(
    id                BIGINT AUTO_INCREMENT PRIMARY KEY,

    document_id       BIGINT    NOT NULL,
    approval_line_id  BIGINT    NOT NULL,
    company_id        BIGINT    NOT NULL,

    signer_id         BIGINT    NULL,
    signature_file_id BIGINT    NOT NULL, -- 당시 서명 스냅샷
    signed_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    /* =========================
       Foreign Keys
       ========================= */

    CONSTRAINT fk_doc_sig_document
        FOREIGN KEY (document_id)
            REFERENCES document (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_doc_sig_approval_line
        FOREIGN KEY (approval_line_id)
            REFERENCES approval_line (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_doc_sig_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_doc_sig_signer
        FOREIGN KEY (signer_id)
            REFERENCES employee (id)
            ON DELETE SET NULL,

    CONSTRAINT fk_doc_sig_file
        FOREIGN KEY (signature_file_id)
            REFERENCES file (id)
            ON DELETE RESTRICT,

    -- 문서 내 결재 단계별 서명은 1개만 허용
    CONSTRAINT uk_doc_approval
        UNIQUE (document_id, approval_line_id)

) ENGINE = InnoDB;

CREATE INDEX idx_doc_sig_document
    ON document_signature (document_id);

CREATE INDEX idx_doc_sig_signer
    ON document_signature (signer_id);


/* =========================================================
   BOARD / MESSAGE TABLES
   (company_id 반영, 초기 DB 세팅 기준)
========================================================= */

CREATE TABLE board_category
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    company_id        BIGINT       NOT NULL,
    organization_id   BIGINT       NULL,
    board_name        VARCHAR(100) NOT NULL,
    board_type        VARCHAR(50)  NOT NULL,
    is_activated      TINYINT(1)   NOT NULL DEFAULT 1,
    comment_activated TINYINT(1)   NOT NULL DEFAULT 1,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        TIMESTAMP    NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (company_id) REFERENCES company (id),
    FOREIGN KEY (organization_id) REFERENCES organization (id)
);

CREATE TABLE board
(
    id                BIGINT       NOT NULL AUTO_INCREMENT,
    board_category_id BIGINT       NOT NULL,
    employee_id       BIGINT       NOT NULL,
    board_title       VARCHAR(255) NOT NULL,
    board_content     TEXT         NOT NULL,
    view_count        INT          NOT NULL DEFAULT 0,
    file_id           BIGINT       NULL,
    created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at        TIMESTAMP    NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (board_category_id) REFERENCES board_category (id),
    FOREIGN KEY (employee_id) REFERENCES employee (id),
    FOREIGN KEY (file_id) REFERENCES file (id)
);

CREATE TABLE board_permission
(
    id                BIGINT    NOT NULL AUTO_INCREMENT,
    employee_id       BIGINT    NOT NULL,
    board_category_id BIGINT    NOT NULL,
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_employee_board (employee_id, board_category_id),
    FOREIGN KEY (employee_id) REFERENCES employee (id),
    FOREIGN KEY (board_category_id) REFERENCES board_category (id)
);

CREATE TABLE comment
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    board_id        BIGINT       NOT NULL,
    employee_id     BIGINT       NOT NULL,
    comment_content VARCHAR(500) NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at      TIMESTAMP    NULL,
    PRIMARY KEY (id),
    FOREIGN KEY (board_id) REFERENCES board (id),
    FOREIGN KEY (employee_id) REFERENCES employee (id)
);

CREATE TABLE message
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    company_id      BIGINT       NOT NULL,
    employee_id     BIGINT       NOT NULL,
    message_title   VARCHAR(255) NOT NULL,
    message_content TEXT         NOT NULL,
    file_id         BIGINT       NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    FOREIGN KEY (company_id) REFERENCES company (id),
    FOREIGN KEY (employee_id) REFERENCES employee (id),
    FOREIGN KEY (file_id) REFERENCES file (id)
);

CREATE TABLE message_recipient
(
    id          BIGINT     NOT NULL AUTO_INCREMENT,
    company_id  BIGINT     NOT NULL,
    message_id  BIGINT     NOT NULL,
    employee_id BIGINT     NOT NULL,
    is_read     TINYINT(1) NOT NULL DEFAULT 0,
    read_at     TIMESTAMP  NULL,
    created_at  TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_message_recipient (company_id, message_id, employee_id),
    FOREIGN KEY (company_id) REFERENCES company (id),
    FOREIGN KEY (message_id) REFERENCES message (id),
    FOREIGN KEY (employee_id) REFERENCES employee (id)
);

-- /////////////////////////////////// 종훈 /////////////////////////////////// --
-- 1. 자원 상태 (Charset 명시 추가)
CREATE TABLE resource_status
(
    id          BIGINT AUTO_INCREMENT,
    status_code VARCHAR(50) NOT NULL UNIQUE,
    status_name VARCHAR(50),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 2. 회의실
CREATE TABLE meetingroom
(
    id                 BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id         BIGINT      NOT NULL,
    name               VARCHAR(30) NOT NULL,
    position           VARCHAR(50) NOT NULL,
    description        VARCHAR(255),
    resource_status_id BIGINT,
    created_at         TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP            DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_meetingroom_company
        FOREIGN KEY (company_id)
            REFERENCES company (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_meetingroom_resource_status
        FOREIGN KEY (resource_status_id)
            REFERENCES resource_status (id)
            ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 3. 차량
CREATE TABLE car
(
    id                 BIGINT AUTO_INCREMENT,
    company_id         BIGINT      NOT NULL,
    number             VARCHAR(15) NOT NULL,
    name               VARCHAR(50) NOT NULL,
    driver_age         INT         NOT NULL,
    description        VARCHAR(255),
    resource_status_id BIGINT,
    file_id            BIGINT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_car_company
        FOREIGN KEY (company_id) REFERENCES company (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_car_resource_status
        FOREIGN KEY (resource_status_id) REFERENCES resource_status (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_car_file
        FOREIGN KEY (file_id) REFERENCES file (id)
            ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 4. 아이템 카테고리
CREATE TABLE item_category
(
    id         BIGINT AUTO_INCREMENT,
    company_id BIGINT      NOT NULL,
    name       VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_category_company
        FOREIGN KEY (company_id) REFERENCES company (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 5. 아이템 (기타 자원)
CREATE TABLE item
(
    id                 BIGINT AUTO_INCREMENT,
    company_id         BIGINT      NOT NULL,
    item_category_id   BIGINT      NOT NULL,
    name               VARCHAR(50) NOT NULL,
    description        VARCHAR(255),
    resource_status_id BIGINT,
    file_id            BIGINT,
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_item_company
        FOREIGN KEY (company_id) REFERENCES company (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_item_item_category
        FOREIGN KEY (item_category_id) REFERENCES item_category (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_item_resource_status
        FOREIGN KEY (resource_status_id) REFERENCES resource_status (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_item_file
        FOREIGN KEY (file_id) REFERENCES file (id)
            ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 6. 예약 상태
CREATE TABLE reservation_status
(
    id          BIGINT AUTO_INCREMENT,
    status_code VARCHAR(50) NOT NULL UNIQUE,
    status_name VARCHAR(50) NOT NULL,
    PRIMARY KEY (id)

) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 7. 예약
CREATE TABLE reservation
(
    id                    BIGINT AUTO_INCREMENT,
    company_id            BIGINT       NOT NULL,
    employee_id           BIGINT       NOT NULL,
    type_code             VARCHAR(20)  NOT NULL,
    item_category_id      BIGINT,
    resource_id           BIGINT       NOT NULL,
    reservation_date      DATE         NOT NULL,
    start_time            INT          NOT NULL,
    end_time              INT          NOT NULL,
    reservation_reason    VARCHAR(255) NOT NULL,
    reject_reason         VARCHAR(255),
    reservation_status_id BIGINT,
    created_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_reservation_company
        FOREIGN KEY (company_id) REFERENCES company (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_reservation_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_reservation_item_category
        FOREIGN KEY (item_category_id) REFERENCES item_category (id)
            ON DELETE SET NULL,
    CONSTRAINT fk_reservation_status
        FOREIGN KEY (reservation_status_id) REFERENCES reservation_status (id)
            ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 8. 일정
CREATE TABLE schedule
(
    id                   BIGINT AUTO_INCREMENT,
    company_id           BIGINT       NOT NULL,
    category_id          BIGINT,
    org_id               BIGINT,
    type                 VARCHAR(20)  NOT NULL,
    employee_id          BIGINT       NOT NULL,
    schedule_title       VARCHAR(100) NOT NULL,
    schedule_description VARCHAR(255),
    start_date           DATE         NOT NULL,
    end_date             DATE         NOT NULL,
    start_time           INT          NOT NULL,
    end_time             INT          NOT NULL,
    schedule_status      VARCHAR(20)  NOT NULL,
    created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_company
        FOREIGN KEY (company_id) REFERENCES company (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_schedule_org_category
        FOREIGN KEY (category_id) REFERENCES org_category (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_schedule_organization
        FOREIGN KEY (org_id) REFERENCES organization (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_schedule_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 9. 일정 요약
CREATE TABLE schedule_summary
(
    id            BIGINT AUTO_INCREMENT,
    company_id    BIGINT NOT NULL,
    employee_id   BIGINT NOT NULL,
    week_summary  TEXT,
    month_summary TEXT,
    created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_schedule_summary_company
        FOREIGN KEY (company_id) REFERENCES company (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_schedule_summary_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id)
            ON DELETE CASCADE
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;
-- 10. AI 일정 요약 로그
CREATE TABLE log_schedule_summary
(
    id                  BIGINT AUTO_INCREMENT,
    company_id          BIGINT      NOT NULL,
    employee_id         BIGINT,
    summary_type        VARCHAR(10) NOT NULL,
    schedule_start_date DATE        NOT NULL,
    schedule_end_date   DATE        NOT NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    model               VARCHAR(50) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_log_schedule_summary_company
        FOREIGN KEY (company_id) REFERENCES company (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_log_schedule_summary_employee
        FOREIGN KEY (employee_id) REFERENCES employee (id)
            ON DELETE SET NULL
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;



CREATE TABLE attendance_rule
(
    id                        BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '규칙 아이디',
    company_id                BIGINT    NOT NULL COMMENT '회사 아이디',
    name                      VARCHAR(100) COMMENT '규칙 명칭',
    default_start_time        TIME      NOT NULL COMMENT '기본 출근 기준 시간 (09:00)',
    default_end_time          TIME      NOT NULL COMMENT '기본 퇴근 기준 시간 (18:00)',
    default_break_minutes     INT                DEFAULT 60 COMMENT '기본 휴게 시간 (분 단위, 60분)',
    late_threshold_min        INT                DEFAULT 10 COMMENT '지각 판정 허용 시간 (분)',
    early_leave_threshold_min INT                DEFAULT 10 COMMENT '조퇴 판정 허용 시간 (분)',
    is_default                BOOLEAN   NOT NULL DEFAULT TRUE COMMENT '회사의 기본 주 규칙 여부',
    created_at                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at                TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_att_rule_name (company_id, name),
    FOREIGN KEY (company_id) REFERENCES company (id)
);



CREATE TABLE employee_att_rule
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '아이디',
    company_id    BIGINT  NOT NULL COMMENT '회사 아이디',
    employee_id   BIGINT  NOT NULL COMMENT '직원 아이디',
    start_time    TIME COMMENT '지정 출근 시간 (오버라이드)',
    end_time      TIME COMMENT '지정 퇴근 시간 (오버라이드)',
    break_minutes INT COMMENT '지정 휴게 시간 (분)',
    reason        VARCHAR(255) COMMENT '예외규칙 적용 사유',
    valid_from    DATE    NOT NULL COMMENT '규칙 적용 시작일',
    valid_to      DATE    NOT NULL COMMENT '규칙 적용 종료일',
    applied_at    DATETIME COMMENT '적용된 시각',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE COMMENT '규칙 활성화 상태',
    -- [수정] 한 직원에 대해 특정 기간에 대한 규칙 중복 방지 (기간 겹침은 로직으로 처리 필요)
    UNIQUE KEY uk_emp_rule_period (employee_id, valid_from, valid_to),
    FOREIGN KEY (company_id) REFERENCES company (id),
    FOREIGN KEY (employee_id) REFERENCES employee (id)
);



CREATE TABLE attendance
(
    id                BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '아이디',
    company_id        BIGINT  NOT NULL COMMENT '회사 아이디',
    employee_id       BIGINT  NOT NULL COMMENT '기록한 사원 아이디',
    work_date         DATE    NOT NULL COMMENT '근무 일자',
    commute_at        DATETIME COMMENT '실제 출근 시간',
    leave_at          DATETIME COMMENT '실제 퇴근 시간',
    status            VARCHAR(50) COMMENT '최종 근태 상태 (지각/결근/비출근)',
    applied_rule_id   BIGINT COMMENT '최종 적용된 규칙 아이디',
    is_corrected      BOOLEAN NOT NULL DEFAULT FALSE COMMENT '정정 처리 여부',
    correction_reason VARCHAR(255) COMMENT '정정 사유',
    -- [수정] 회사/직원/근무 일자 조합이 유일하도록 변경
    UNIQUE KEY uk_att_date (company_id, employee_id, work_date),
    FOREIGN KEY (company_id) REFERENCES company (id),
    FOREIGN KEY (employee_id) REFERENCES employee (id),
    FOREIGN KEY (applied_rule_id) REFERENCES attendance_rule (id)
);



CREATE TABLE correction_history
(
    id                   BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '근태 정정 이력 아이디',
    attendance_id        BIGINT       NOT NULL COMMENT '근태 기록 아이디',
    original_commute_at  DATETIME COMMENT '정정 전 출근 시각',
    original_leave_at    DATETIME COMMENT '정정 전 퇴근 시각',
    corrected_commute_at DATETIME     NOT NULL COMMENT '정정 요청 출근 시각',
    corrected_leave_at   DATETIME     NOT NULL COMMENT '정정 요청 퇴근 시각',
    correction_reason    VARCHAR(255) NOT NULL COMMENT '직원 입력 정정 사유',
    correction_status    VARCHAR(50)  NOT NULL DEFAULT 'PENDING' COMMENT '정정 처리 상태 (PENDING, APPROVED, REJECTED)',
    processed_by         BIGINT COMMENT '정정 처리 관리자 아이디',
    processed_at         DATETIME COMMENT '정정 처리 시각',
    rejection_reason     VARCHAR(255) COMMENT '관리자 입력 반려 사유',
    FOREIGN KEY (attendance_id) REFERENCES attendance (id),
    FOREIGN KEY (processed_by) REFERENCES employee (id)
);



CREATE TABLE leave_balance
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '아이디',
    company_id      BIGINT    NOT NULL,
    employee_id     BIGINT    NOT NULL COMMENT '직원 아이디',
    year            INT       NOT NULL COMMENT '연차 기준 연도',
    total_granted   DECIMAL(5, 2)      DEFAULT 0.0 COMMENT '총 부여 일수',
    remaining_days  DECIMAL(5, 2)      DEFAULT 0.0 COMMENT '잔여 일수',
    last_updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '최종 업데이트 시각',
    UNIQUE KEY uk_employee_year (company_id, employee_id, year),
    FOREIGN KEY (company_id) REFERENCES company (id),
    FOREIGN KEY (employee_id) REFERENCES employee (id)
);



CREATE TABLE manual_category
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '카테고리 아이디',
    company_id    BIGINT      NOT NULL,
    category_name VARCHAR(50) NOT NULL COMMENT '카테고리 이름',
    description   VARCHAR(255) COMMENT '카테고리 설명',
    is_active     BOOLEAN     NOT NULL DEFAULT TRUE COMMENT '카테고리 사용 여부',
    sort_order    INT COMMENT '정렬 순서',
    created_at    TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (company_id) REFERENCES company (id)
);
CREATE TABLE manual_link
(
    id            BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '연결 고유 아이디',
    file_id       BIGINT  NOT NULL COMMENT '파일 아이디 (첨부파일 테이블 참조)',
    category_id   BIGINT  NOT NULL COMMENT '카테고리 아이디',
    company_id    BIGINT  NOT NULL,
    status        VARCHAR(50) COMMENT '파일 처리 상태 (UPLOADED, PROCESSING, READY, FAILED)',
    is_active     BOOLEAN NOT NULL DEFAULT TRUE COMMENT '사용할지 여부',
    vectorized_at DATETIME COMMENT '벡터화 처리 시각',
    FOREIGN KEY (file_id) REFERENCES file (id),
    FOREIGN KEY (category_id) REFERENCES manual_category (id),
    FOREIGN KEY (company_id) REFERENCES company (id)
);
CREATE TABLE grant_history
(
    id              BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '아이디',
    employee_id     BIGINT        NOT NULL COMMENT '직원 아이디',
    company_id      BIGINT        NOT NULL COMMENT '회사 아이디',
    grant_date      DATE          NOT NULL COMMENT '부여 발생일',
    granted_days    DECIMAL(4, 2) NOT NULL COMMENT '부여 일수',
    grant_type      VARCHAR(50) COMMENT '부여 유형',
    expiration_date DATE COMMENT '소멸 예정일',
    is_expired      BOOLEAN       NOT NULL DEFAULT FALSE COMMENT '소멸 처리 여부',
    created_at      DATETIME      NOT NULL COMMENT '기록 시각',
    FOREIGN KEY (employee_id) REFERENCES employee (id),
    FOREIGN KEY (company_id) REFERENCES company (id)
);