package com.finalproj.orbitflow.hr.employee.dto;

import com.finalproj.orbitflow.hr.employee.enums.EmployeeRole;
import com.finalproj.orbitflow.hr.employee.enums.EmploymentType;
import com.finalproj.orbitflow.hr.employee.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 관리자용 사원 생성 요청 DTO
 * - 사용자 관리 > 사원 추가 팝업에서 사용
 * - 최초 생성 시 TEMP 상태로 생성된다.
 *
 * @author : seunga03
 * @filename : EmployeeCreateReqDto
 * @since : 2025-12-29 월요일
 */
@Getter
public class EmployeeCreateReqDto {

    /* ==============================
       기본 정보
       ============================== */

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email; // 로그인 ID

    @NotBlank(message = "사번은 필수입니다.")
    private String employeeNo;

    @NotNull(message = "입사일은 필수입니다.")
    private LocalDate hireDate;

    @NotNull(message = "성별은 필수입니다.")
    private Gender gender;

    private LocalDate birthDate;

    @Pattern(
            regexp = "^[0-9]*$",
            message = "연락처는 숫자만 입력 가능합니다."
    )
    private String phone;

    @Pattern(
            regexp = "^[0-9]*$",
            message = "내선 번호는 숫자만 입력 가능합니다."
    )
    private String internalPhone;



    /* ==============================
       조직 정보
       ============================== */

    @NotNull(message = "조직은 필수입니다.")
    private Long orgId;

    private Long rankId;

    private Long positionCategoryId;


    /* ==============================
       고용 / 권한 정보
       ============================== */

    @NotNull(message = "고용 형태는 필수입니다.")
    private EmploymentType employmentType;

//    /**
//     * 생성 시 기본값: TEMP
//     * - 프론트에서 보내지 않아도 되며
//     * - 서버에서 강제 설정하는 것을 권장
//     */
//    private EmployeeStatus status;

    @NotNull(message = "사원 유형(권한)은 필수입니다.")
    private EmployeeRole role;
}
