# 공통 레이아웃 사용 가이드

## 개요
공통 레이아웃은 헤더와 왼쪽 사이드바만 제공합니다. 각 페이지에서 콘텐츠만 작성하면 됩니다.

## 파일 구조
```
templates/
├── layout/
│   └── layout.html          # 공통 레이아웃 (헤더 + 사이드바만)
├── fragments/
│   ├── admin-header.html    # 관리자용 헤더
│   ├── user-header.html     # 사용자용 헤더
│   └── sidebar.html         # 사이드바 shell
└── sidebar/
    ├── admin-sidebar.html   # 관리자용 사이드바 메뉴
    └── attendance-sidebar.html  # 사용자용 사이드바 메뉴
```

## 사용 방법

### 1. HTML 템플릿 파일 작성

```html
<!DOCTYPE html>
<html lang="ko" xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layout/layout :: layout(
          ~{::content},
          ~{fragments/admin-header :: header},    <!-- 또는 user-header -->
          ~{sidebar/admin-sidebar :: menu}        <!-- 또는 attendance-sidebar -->
      )}">
<div th:fragment="content">
    <!-- 여기에 페이지 콘텐츠 작성 -->
    <div class="content-area">
        <h1>페이지 제목</h1>
        <!-- ... -->
    </div>
</div>
</html>
```

### 2. 컨트롤러에서 설정

```java
@GetMapping("/your-page")
public String yourPage(Model model, @AuthenticationPrincipal SecurityUser user) {
    // 필수: 공통 레이아웃 정보
    model.addAttribute("companyName", "멀티캠퍼스");
    model.addAttribute("userName", user != null ? user.getUsername() : "사용자");
    model.addAttribute("pageTitle", "페이지 제목");
    
    // 필수: 헤더 프래그먼트 (관리자용 또는 사용자용)
    model.addAttribute("headerFragment", "fragments/admin-header :: header");
    // 또는
    // model.addAttribute("headerFragment", "fragments/user-header :: header");
    
    // 필수: 사이드바 프래그먼트
    model.addAttribute("sidebarFragment", "sidebar/admin-sidebar :: menu");
    // 또는
    // model.addAttribute("sidebarFragment", "sidebar/attendance-sidebar :: menu");
    
    // 선택: 현재 활성 메뉴 (GNB, 사이드바 하이라이트용)
    model.addAttribute("currentGNB", "admin");  // 또는 "work", "approval" 등
    model.addAttribute("currentMenu", "your-menu-id");
    
    // 선택: 페이지별 CSS
    model.addAttribute("pageStyles", java.util.List.of("/css/your-page.css"));
    
    return "your-template-name";
}
```

## 헤더 종류

### 관리자용 헤더 (`fragments/admin-header :: header`)
- 어두운 배경 (#2c3e50)
- 관리자 전용 메뉴 (관리자, 사원 관리, 결재 관리, 설정)
- 관리자 아이콘 (fa-user-shield)

### 사용자용 헤더 (`fragments/user-header :: header`)
- 연한 배경 (#e1e2ef)
- 일반 사용자 메뉴 (홈, 결재, 근태, 일정, 예약, 게시판)
- 일반 사용자 아이콘 (fa-user-circle)

## 사이드바 메뉴 추가

### 1. 사이드바 메뉴 파일 생성
`src/main/resources/templates/sidebar/your-sidebar.html` 파일 생성:

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
<ul th:fragment="menu">
    <li th:classappend="${currentMenu} == 'menu1' ? ' selected' : ''">
        <a th:href="@{/your-path1}" class="sidebar-link">메뉴 1</a>
    </li>
    <li th:classappend="${currentMenu} == 'menu2' ? ' selected' : ''">
        <a th:href="@{/your-path2}" class="sidebar-link">메뉴 2</a>
    </li>
</ul>
</body>
</html>
```

### 2. 컨트롤러에서 사용
```java
model.addAttribute("sidebarFragment", "sidebar/your-sidebar :: menu");
model.addAttribute("currentMenu", "menu1");  // 활성 메뉴 강조
```

## CSS 작성
- 레이아웃 CSS는 `static/css/layout.css`에 포함되어 있습니다 (헤더와 사이드바 스타일만)
- 각 페이지의 콘텐츠 스타일은 페이지별 CSS 파일을 만들어서 사용하세요
- 페이지별 CSS는 `pageStyles` 모델 속성으로 추가하거나 인라인 스타일로 작성

## 예시: 관리자 페이지
```java
@GetMapping("/admin/your-page")
public String adminPage(Model model, @AuthenticationPrincipal SecurityUser user) {
    model.addAttribute("companyName", "멀티캠퍼스");
    model.addAttribute("userName", user != null ? user.getUsername() : "관리자");
    model.addAttribute("pageTitle", "관리 페이지");
    model.addAttribute("headerFragment", "fragments/admin-header :: header");
    model.addAttribute("sidebarFragment", "sidebar/admin-sidebar :: menu");
    model.addAttribute("currentGNB", "admin");
    model.addAttribute("currentMenu", "your-menu");
    return "admin/your-page";
}
```

## 예시: 사용자 페이지
```java
@GetMapping("/work/your-page")
public String userPage(Model model, @AuthenticationPrincipal SecurityUser user) {
    model.addAttribute("companyName", "멀티캠퍼스");
    model.addAttribute("userName", user != null ? user.getUsername() : "사용자");
    model.addAttribute("pageTitle", "사용자 페이지");
    model.addAttribute("headerFragment", "fragments/user-header :: header");
    model.addAttribute("sidebarFragment", "sidebar/attendance-sidebar :: menu");
    model.addAttribute("currentGNB", "work");
    model.addAttribute("currentMenu", "your-menu");
    return "work/your-page";
}
```


