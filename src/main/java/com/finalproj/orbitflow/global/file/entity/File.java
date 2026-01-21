package com.finalproj.orbitflow.global.file.entity;

/*
 * Please explain the class!!!
 *
 * @filename    : File
 * @author      : Choi MinHyeok
 * @since       : 25. 12. 15. 월요일
 */

import com.finalproj.orbitflow.board.boardpost.entity.BoardPost;
import com.finalproj.orbitflow.global.common.BaseEntity;
import com.finalproj.orbitflow.global.file.enums.FileDomain;
import com.finalproj.orbitflow.hr.company.entity.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Entity
@Table(name = "file")
@Getter
@NoArgsConstructor
@Builder
public class File extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "object_key", nullable = false, unique = true, length = 512)
    private String objectKey;

    @Column(name = "origin_file", length = 255)
    private String originFile;

    @Column(name = "sys_file", length = 255)
    private String sysFile;

    @Enumerated(EnumType.STRING)
    @Column(name = "domain", length = 30, nullable = false)
    private FileDomain domain;

    @Column(name = "content_type", length = 255)
    private String contentType;

    @Column(name = "file_size")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_post_id")
    private BoardPost boardPost;
}
