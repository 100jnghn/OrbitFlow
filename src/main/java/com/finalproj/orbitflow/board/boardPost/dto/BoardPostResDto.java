package com.finalproj.orbitflow.board.boardPost.dto;

import com.finalproj.orbitflow.board.boardPost.entity.BoardPost;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

public class BoardPostResDto {

    /** 작성자 정보 DTO */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WriterInfo {
        private Long id;
        private String name;
        private String employeeNo;

        public static WriterInfo from(Employee employee) {
            return WriterInfo.builder()
                    .id(employee.getId())
                    .name(employee.getName())
                    .employeeNo(employee.getEmployeeNo())
                    .build();
        }
    }

    /** 첨부파일 정보 DTO */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FileInfo {
        private Long id;
        private String originalFileName;
        private String filePath;
        private Long fileSize;

        public static FileInfo from(File file) {
            if (file == null)
                return null;
            return FileInfo.builder()
                    .id(file.getId())
                    .originalFileName(file.getOriginFile())
                    .filePath(file.getSysFile()) // 엔티티 필드 기준
                    .fileSize(file.getFileSize())
                    .build();
        }

        public static List<FileInfo> fromFiles(List<File> files) {
            if (files == null || files.isEmpty())
                return List.of();
            return files.stream()
                    .map(FileInfo::from)
                    .toList();
        }
    }

    /** 게시글 목록 조회 DTO */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListInfo {
        private Long id;
        @com.fasterxml.jackson.annotation.JsonProperty("categoryId")
        private Long categoryId;
        private String boardTitle;
        private String categoryName;
        private WriterInfo writer;
        private int viewCount;
        private Instant createdAt;
        @com.fasterxml.jackson.annotation.JsonProperty("hasFile")
        private boolean fileAttached;
        private int commentCount;

        public static ListInfo from(BoardPost boardPost) {
            boolean attached = boardPost.getFiles() != null && !boardPost.getFiles().isEmpty();
            return ListInfo.builder()
                    .id(boardPost.getId())
                    .categoryId(boardPost.getCategory() != null ? boardPost.getCategory().getId() : null)
                    .boardTitle(boardPost.getBoardTitle())
                    .categoryName(boardPost.getCategory() != null ? boardPost.getCategory().getBoardName() : "N/A")
                    .writer(WriterInfo.from(boardPost.getWriter()))
                    .viewCount(boardPost.getViewCount())
                    .createdAt(boardPost.getCreatedAt())
                    .fileAttached(attached)
                    .commentCount(boardPost.getCommentCount())
                    .build();
        }

    }

    /** 게시글 상세 조회 DTO */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {
        private Long id;
        private String boardTitle;
        private String boardContent;
        @com.fasterxml.jackson.annotation.JsonProperty("categoryId")
        private Long categoryId;
        private String categoryName;
        private WriterInfo writer;
        private int viewCount;
        private Instant createdAt;
        private Instant updatedAt;
        private List<FileInfo> files;
        private boolean commentActivated; // 댓글 기능 활성화 여부

        public static DetailInfo from(BoardPost boardPost) {
            return DetailInfo.builder()
                    .id(boardPost.getId())
                    .boardTitle(boardPost.getBoardTitle())
                    .boardContent(boardPost.getBoardContent())
                    .categoryId(boardPost.getCategory() != null ? boardPost.getCategory().getId() : null)
                    .categoryName(boardPost.getCategory() != null ? boardPost.getCategory().getBoardName() : "N/A")
                    .writer(WriterInfo.from(boardPost.getWriter()))
                    .viewCount(boardPost.getViewCount())
                    .createdAt(boardPost.getCreatedAt())
                    .updatedAt(boardPost.getUpdatedAt())
                    .files(FileInfo.fromFiles(boardPost.getFiles()))
                    .commentActivated(
                            boardPost.getCategory() != null ? boardPost.getCategory().isCommentActivated() : true)
                    .build();
        }
    }
}
