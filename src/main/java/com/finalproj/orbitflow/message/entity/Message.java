package com.finalproj.orbitflow.message.entity;

import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee sender;

    @Column(name = "message_title", nullable = false)
    private String title;

    @Column(name = "message_content", nullable = false)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_id")
    private File file;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL)
    private List<MessageRecipient> recipients = new ArrayList<>();

    @Builder
    public Message(Long companyId, Employee sender, String title, String content, File file) {
        this.companyId = companyId;
        this.sender = sender;
        this.title = title;
        this.content = content;
        this.file = file;
    }
}