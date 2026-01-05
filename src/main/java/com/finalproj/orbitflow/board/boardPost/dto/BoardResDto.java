package com.finalproj.orbitflow.board.boardPost.dto;

import com.finalproj.orbitflow.board.boardPost.entity.Board;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

public class BoardResDto {

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
        private String boardTitle;
        private String categoryName;
        private WriterInfo writer;
        private int viewCount;
        private Instant createdAt;
        private boolean hasFile;

        public static ListInfo from(Board board) {
            return ListInfo.builder()
                    .id(board.getId())
                    .boardTitle(board.getBoardTitle())
                    .categoryName(board.getCategory() != null ? board.getCategory().getBoardName() : "N/A")
                    .writer(WriterInfo.from(board.getWriter()))
                    .viewCount(board.getViewCount())
                    .createdAt(board.getCreatedAt())
                    .hasFile(board.getFiles() != null)
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
        private String categoryName;
        private WriterInfo writer;
        private int viewCount;
        private Instant createdAt;
        private Instant updatedAt;
        private List<FileInfo> files;
        private boolean commentActivated; // 댓글 기능 활성화 여부

        public static DetailInfo from(Board board) {
            return DetailInfo.builder()
                    .id(board.getId())
                    .boardTitle(board.getBoardTitle())
                    .boardContent(board.getBoardContent())
                    .categoryName(board.getCategory() != null ? board.getCategory().getBoardName() : "N/A")
                    .writer(WriterInfo.from(board.getWriter()))
                    .viewCount(board.getViewCount())
                    .createdAt(board.getCreatedAt())
                    .updatedAt(board.getUpdatedAt())
                    .files(FileInfo.fromFiles(board.getFiles()))
                    .commentActivated(board.getCategory() != null ? board.getCategory().isCommentActivated() : true)
                    .build();
        }
    }
}