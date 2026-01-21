# **AI 기반 멀티테넌트 전자결재 중심 SaaS 그룹웨어**

> 조직에 맞게 확장되는 전자결재 중심 올인원 그룹웨어
> 
> 
> 복잡한 업무 프로세스를 하나의 안정적인 궤도(Orbit)에 올려놓다.
> 

<img width="1920" height="1080" alt="image" src="https://github.com/user-attachments/assets/b5f35827-3603-47a9-9108-d44a0ba66162" />


---

## 📌 프로젝트 개요

**OrbitFlow**는 전자결재를 중심으로 
근태, 일정, 자원 예약, 게시판, 알림, AI 기능을 하나로 통합한
**AI-Powered SaaS Groupware**입니다.

- **멀티테넌트 아키텍처** 기반 기업별 데이터 완전 분리
- **AI 기반 문서 자동화**로 업무 생산성 극대화
- **정책 기반 조직/결재/근태 관리**로 높은 커스터마이징 지원
- **API-First 설계**로 외부 시스템 연동 및 확장성 확보

📅 개발 기간: `2025.12 ~ 2026.01`

👥 개발 인원: `5명 (Team Orbit)`

🎓 멀티캠퍼스 파이널 프로젝트

---

## 🎯 타겟 사용자

- 중소기업 ~ 중견기업(SMB)
- 빠른 도입과 유연한 커스터마이징이 필요한 조직
- 전자결재 + 근태 + 협업을 하나의 시스템으로 관리하고 싶은 기업

---

## ✨ 핵심 가치 (Value Proposition)

- **업무 효율 극대화**
    - AI 문서 초안 생성 → 작성 시간 최대 50% 단축
    - 결재 대기 시간 최소화
- **운영 편의성**
    - 관리자 정책 설정 중심 구조
    - 조직도 기반 권한 자동 상속
- **비용 절감**
    - 멀티테넌트 기반 인프라 최적화
    - SaaS 구독형 구조
- **업무 투명성**
    - 결재·근태·변경 이력 전면 감사 로그
- **확장성**
    - API-First 구조
    - Kubernetes 기반 자동 확장

---

## 🧩 주요 기능

### 📝 전자결재

- 결재 양식 생성/버전 관리
- 결재선 자동 생성 및 검증
- 단계별 승인 / 반려 / 재기안
- AI 문서 요약 & 변경점 비교(Diff)
- 결재 이력 및 상태 실시간 반영

### ⏱ 근태 / 휴가

- 실시간 출퇴근 관리
- 출장·외근 자동 출근 정규화
- 연차 자동 산정 (입사일 기준)
- 결재 승인 시 연차 자동 차감

### 📅 일정 / 자원 예약

- 회의실·차량 등 자원 관리
- 충돌 없는 예약 일괄 승인 알고리즘
- 개인/조직/전사 일정 관리
- AI 기반 일정 요약 브리핑

### 🔔 알림

- SSE + Redis Pub/Sub 실시간 알림
- 다중 서버 환경에서도 안정적 Push
- 읽음/미읽음 상태 관리

### 🏢 조직 & HR 관리

- 계층형 조직 트리 구조
- 직급 / 직책 / 정책 관리
- 사원 상태 기반 접근 제어
- 조직 변경 시 게시판/권한 자동 동기화

### 🤖 AI 기능

- 결재 문서 자동 초안 생성
- 문서 요약 및 변경점 비교
- 사내 매뉴얼 기반 지능형 챗봇 (RAG)
- AI 일정 요약

---

## 🏗 시스템 아키텍처

<img width="5056" height="3392" alt="Gemini_Generated_Image_qfdxvgqfdxvgqfdx (1)" src="https://github.com/user-attachments/assets/8a82e9b2-0d9a-42fe-bc3c-f5cf4c2ee6a2" />


## 🛠 Tech stack 
분야| 사용 기술|
:--------:|:------------------------------:|
**Frontend** | <img src="https://img.shields.io/badge/javascript-%23323330.svg?style=for-the-badge&logo=javascript&logoColor=%23F7DF1E"> ![HTML5](https://img.shields.io/badge/html5-%23E34F26.svg?style=for-the-badge&logo=html5&logoColor=white) ![CSS](https://img.shields.io/badge/css-%23663399.svg?style=for-the-badge&logo=css&logoColor=white) ![Thymeleaf](https://img.shields.io/badge/Thymeleaf-%23005C0F.svg?style=for-the-badge&logo=Thymeleaf&logoColor=white) ![Bootstrap](https://img.shields.io/badge/bootstrap-%238511FA.svg?style=for-the-badge&logo=bootstrap&logoColor=white) ![Chart.js](https://img.shields.io/badge/chart.js-F5788D.svg?style=for-the-badge&logo=chart.js&logoColor=white)
**Backend** | ![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=for-the-badge&logo=openjdk&logoColor=white) ![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=for-the-badge&logo=spring&logoColor=white) ![MySQL](https://img.shields.io/badge/mysql-4479A1.svg?style=for-the-badge&logo=mysql&logoColor=white) ![ChromaDB](https://img.shields.io/badge/ChromaDB-%23785DA6.svg?style=for-the-badge&logo=chroma&logoColor=white) ![JPA](https://img.shields.io/badge/JPA-%236B8EC6.svg?style=for-the-badge&logo=jpa&logoColor=white) ![Redis](https://img.shields.io/badge/redis-%23DD0031.svg?style=for-the-badge&logo=redis&logoColor=white) ![ChatGPT](https://img.shields.io/badge/OpenAI-74aa9c?style=for-the-badge&logo=openai&logoColor=white) ![LangChain](https://img.shields.io/badge/langchain-%231C3C3C.svg?style=for-the-badge&logo=langchain&logoColor=white)
**DevOps** | ![Docker](https://img.shields.io/badge/docker-%230db7ed.svg?style=for-the-badge&logo=docker&logoColor=white) ![Amazon S3](https://img.shields.io/badge/Amazon%20S3-FF9900?style=for-the-badge&logo=amazons3&logoColor=white) ![AWS](https://img.shields.io/badge/AWS-%23FF9900.svg?style=for-the-badge&logo=amazon-aws&logoColor=white) ![Jenkins](https://img.shields.io/badge/jenkins-%232C5263.svg?style=for-the-badge&logo=jenkins&logoColor=white)
**Monitoring** | ![Grafana](https://img.shields.io/badge/grafana-%23F46800.svg?style=for-the-badge&logo=grafana&logoColor=white)
**etc** | ![Slack](https://img.shields.io/static/v1?style=for-the-badge&message=Slack&color=4A154B&logo=Slack&logoColor=FFFFFF&label=) ![Notion](https://img.shields.io/static/v1?style=for-the-badge&message=Notion&color=000000&logo=Notion&logoColor=FFFFFF&label=) ![Figma](https://img.shields.io/static/v1?style=for-the-badge&message=Figma&color=F24E1E&logo=Figma&logoColor=FFFFFF&label=) ![Git](https://img.shields.io/badge/git-%23F05033.svg?style=for-the-badge&logo=git&logoColor=white)
</div>


## 📚 Directory Structure

<details>
  <summary><b>Backend</b></summary>
  <pre>
    <code>
        📦 OrbitFlow
         ┣ 📂 src
         ┃ ┗ 📂 main
         ┃   ┣ 📂 java
         ┃   ┃ ┗ 📂 com/finalproj/orbitflow
         ┃   ┃   ┣ 📜 OrbitflowApplication.java
         ┃   ┃   ┣ 📂 approval
         ┃   ┃   ┃ ┣ 📂 approvalLine
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┃ ┗ 📜 ApprovalLineController.java
         ┃   ┃   ┃ ┃ ┣ 📂 dto
         ┃   ┃   ┃ ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┃ ┣ 📂 repository
         ┃   ┃   ┃ ┃ ┗ 📂 service
         ┃   ┃   ┃ ┣ 📂 document
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┃ ┣ 📜 DocumentController.java
         ┃   ┃   ┃ ┃ ┃ ┗ 📜 DocumentViewController.java
         ┃   ┃   ┃ ┃ ┣ 📂 dto
         ┃   ┃   ┃ ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┃ ┣ 📂 render
         ┃   ┃   ┃ ┃ ┃ ┣ 📂 context
         ┃   ┃   ┃ ┃ ┃ ┣ 📂 factory
         ┃   ┃   ┃ ┃ ┃ ┣ 📂 field
         ┃   ┃   ┃ ┃ ┃ ┣ 📂 html
         ┃   ┃   ┃ ┃ ┃ ┗ 📂 pdf
         ┃   ┃   ┃ ┃ ┣ 📂 repository
         ┃   ┃   ┃ ┃ ┗ 📂 service
         ┃   ┃   ┃ ┣ 📂 documentAISummary
         ┃   ┃   ┃ ┃ ┣ 📂 aiBuilder
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┣ 📂 service
         ┃   ┃   ┃ ┃ ┗ 📜 ...
         ┃   ┃   ┃ ┣ 📂 formTemplate
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┃ ┗ 📂 service
         ┃   ┃   ┃ ┗ 📂 ... (logApprovalAction, etc.)
         ┃   ┃   ┣ 📂 attendance
         ┃   ┃   ┃ ┣ 📂 commute
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┗ 📂 service
         ┃   ┃   ┃ ┣ 📂 dashboard
         ┃   ┃   ┃ ┣ 📂 leave
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┃ ┣ 📂 repository
         ┃   ┃   ┃ ┃ ┗ 📂 scheduler
         ┃   ┃   ┃ ┗ 📂 rule
         ┃   ┃   ┣ 📂 auth
         ┃   ┃   ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┣ 📂 dto
         ┃   ┃   ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┃ ┗ 📜 RefreshToken.java
         ┃   ┃   ┃ ┣ 📂 repository
         ┃   ┃   ┃ ┗ 📂 service
         ┃   ┃   ┣ 📂 board
         ┃   ┃   ┃ ┣ 📂 boardCategory
         ┃   ┃   ┃ ┣ 📂 boardPost
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┃ ┗ 📂 repository
         ┃   ┃   ┃ ┗ 📂 comment
         ┃   ┃   ┣ 📂 chatbot
         ┃   ┃   ┃ ┣ 📂 chatbot
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┃ ┗ 📂 repository
         ┃   ┃   ┃ ┣ 📂 chroma
         ┃   ┃   ┃ ┃ ┣ 📂 config
         ┃   ┃   ┃ ┃ ┗ 📂 service
         ┃   ┃   ┃ ┗ 📂 manual
         ┃   ┃   ┣ 📂 email
         ┃   ┃   ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┗ 📂 service
         ┃   ┃   ┣ 📂 global
         ┃   ┃   ┃ ┣ 📂 analytics
         ┃   ┃   ┃ ┣ 📂 common
         ┃   ┃   ┃ ┃ ┗ 📜 BaseEntity.java
         ┃   ┃   ┃ ┣ 📂 config
         ┃   ┃   ┃ ┃ ┣ 📜 SecurityConfig.java
         ┃   ┃   ┃ ┃ ┣ 📜 S3Config.java
         ┃   ┃   ┃ ┃ ┗ 📜 JpaAuditingConfig.java
         ┃   ┃   ┃ ┣ 📂 exception
         ┃   ┃   ┃ ┃ ┗ 📜 GlobalExceptionHandler.java
         ┃   ┃   ┃ ┣ 📂 file
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┗ 📂 storage
         ┃   ┃   ┃ ┗ 📂 security
         ┃   ┃   ┃   ┗ 📂 jwt
         ┃   ┃   ┣ 📂 hr
         ┃   ┃   ┃ ┣ 📂 company
         ┃   ┃   ┃ ┣ 📂 employee
         ┃   ┃   ┃ ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┃ ┣ 📂 dto
         ┃   ┃   ┃ ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┃ ┗ 📂 service
         ┃   ┃   ┃ ┣ 📂 organization
         ┃   ┃   ┃ ┣ 📂 orgCategory
         ┃   ┃   ┃ ┗ 📂 rank
         ┃   ┃   ┣ 📂 message
         ┃   ┃   ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┗ 📂 service
         ┃   ┃   ┣ 📂 notification
         ┃   ┃   ┃ ┣ 📂 channel
         ┃   ┃   ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┗ 📂 service
         ┃   ┃   ┣ 📂 openai
         ┃   ┃   ┃ ┣ 📂 config
         ┃   ┃   ┃ ┗ 📂 dto
         ┃   ┃   ┣ 📂 redis
         ┃   ┃   ┃ ┣ 📂 config
         ┃   ┃   ┃ ┣ 📂 publisher
         ┃   ┃   ┃ ┗ 📂 subscriber
         ┃   ┃   ┣ 📂 reservation
         ┃   ┃   ┃ ┣ 📂 controller
         ┃   ┃   ┃ ┣ 📂 entity
         ┃   ┃   ┃ ┗ 📂 service
         ┃   ┃   ┣ 📂 resource
         ┃   ┃   ┃ ┣ 📂 car
         ┃   ┃   ┃ ┣ 📂 item
         ┃   ┃   ┃ ┗ 📂 meetingroom
         ┃   ┃   ┗ 📂 schedule
         ┃   ┃     ┣ 📂 aimodel
         ┃   ┃     ┣ 📂 controller
         ┃   ┃     ┣ 📂 entity
         ┃   ┃     ┗ 📂 service
         ┃   ┗ 📂 resources
         ┃     ┣ 📜 application.yml
         ┃     ┣ 📜 application-dev.yml
         ┃     ┣ 📂 fonts
         ┃     ┣ 📂 static
         ┃     ┃ ┣ 📂 css
         ┃     ┃ ┃ ┣ 📂 admin
         ┃     ┃ ┃ ┣ 📂 attendance
         ┃     ┃ ┃ ┣ 📂 board
         ┃     ┃ ┃ ┣ 📂 ui
         ┃     ┃ ┃ ┗ 📂 user-document
         ┃     ┃ ┣ 📂 images
         ┃     ┃ ┗ 📂 js
         ┃     ┃   ┣ 📂 admin
         ┃     ┃   ┣ 📂 user-document
         ┃     ┃   ┗ 📂 pdf
         ┃     ┗ 📂 templates
         ┃       ┣ 📂 admin
         ┃       ┃ ┣ 📂 admin-board
         ┃       ┃ ┣ 📂 employee
         ┃       ┃ ┗ 📂 hr
         ┃       ┣ 📂 attendance
         ┃       ┣ 📂 auth
         ┃       ┣ 📂 board
         ┃       ┣ 📂 fragments
         ┃       ┃ ┣ 📜 header.html
         ┃       ┃ ┗ 📜 sidebar.html
         ┃       ┣ 📂 layout
         ┃       ┣ 📂 main
         ┃       ┣ 📂 message
         ┃       ┣ 📂 organization
         ┃       ┣ 📂 reservation
         ┃       ┣ 📂 schedule
         ┃       ┣ 📂 sidebar
         ┃       ┗ 📂 user-document
    </code>
  </pre>
</details>
<br>

## 🔐 인증 & 보안 설계

- JWT Access / Refresh Token 구조
- Refresh Token: **DB + HttpOnly Cookie**
- 동일 브라우저 다중 로그인 시 **강제 세션 종료 정책**
- 토큰 만료 30분 전 **세션 연장 알림 모달**
- 회사 단위 멀티테넌시 접근 제어
- Global Exception Handler 기반 예외 일원화
- 모든 주요 변경 사항 **Audit Log 기록**

---

## 🧠 트러블슈팅 하이라이트

- 결재선 자동 생성 시 **유령 결재 단계 제거**
- 결재 진행 중 결재자 퇴사/이동 시 **대체 결재자 자동 탐색**
- 승인 후 후처리 로직 **이벤트 기반 + 비동기 분리**
- 근태 출장/외근 정합성 문제 → **스케줄러 기반 자동 보정**
- 챗봇 전사 TopK 검색 → **company_id 사전 필터링으로 성능 개선**
- SSE 단일 서버 한계 → **Redis Pub/Sub 도입**

---

## 👥 팀 구성 (Team Orbit)

| 이름 | 역할 | 담당 |
| --- | --- | --- |
| 최민혁 | 팀장 | 전자결재, 인프라, CI/CD |
| 김하연 | 팀원 | 근태, 휴가, AI 챗봇 |
| 백종훈 | 팀원 | 자원, 일정, 알림 |
| 이승아 | 팀원 | 보안, 인증, 조직/HR, 감사 로그 |
| 전성구 | 팀원 | 게시판, 메시지, 대시보드 |

---

## 📎 참고 자료

- OpenAI API
- 한국천문연구원 특일 정보 API
- 국세청 사업자등록정보 API
- 그룹웨어 시장 리서치 자료

---

## 📬 Contact

📧 orbitflowmlp6@gmail.com
