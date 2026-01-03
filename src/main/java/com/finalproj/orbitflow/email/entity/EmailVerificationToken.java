package com.finalproj.orbitflow.email.entity;

import com.finalproj.orbitflow.email.enums.EmailTokenType;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmailVerificationToken
 * @since : 2026-01-01 목요일
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "email_verification_token")
public class EmailVerificationToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    @Column(nullable = false, unique = true, length = 64)
    private String token;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private EmailTokenType type;

    @Column(nullable = false)
    private LocalDateTime expiredAt;

    @Column
    private LocalDateTime usedAt;

    public static EmailVerificationToken create(
            Employee employee,
            EmailTokenType type,
            int expireMinutes
    ) {
        EmailVerificationToken t = new EmailVerificationToken();
        t.employee = employee;
        t.type = type;
        t.token = UUID.randomUUID().toString();
        t.expiredAt = LocalDateTime.now().plusMinutes(expireMinutes);
        return t;
    }

    public void markUsed() {
        this.usedAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return expiredAt.isBefore(LocalDateTime.now());
    }

    public boolean isUsed() {
        return usedAt != null;
    }
}
