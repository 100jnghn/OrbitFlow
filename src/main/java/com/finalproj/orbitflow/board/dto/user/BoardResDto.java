package com.finalproj.orbitflow.board.dto.user;

import com.finalproj.orbitflow.board.entity.Board;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.hr.employee.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

public class BoardResDto {
    /**
     * 목록 및 상세 조회 시 필요한 작성자 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class WriterInfo {
        private Long id;                            // 작성자 ID (PK)
        private String name;                        // 작성자 이름
        private String employeeNo;                  // 작성자 사번

        public static WriterInfo from(Employee employee) {
            return WriterInfo.builder()
                    .id(employee.getId())
                    .name(employee.getName())
                    .employeeNo(employee.getEmployeeNo())
                    .build();
        }
    }

    /**
     * 상세 조회 시 필요한 첨부파일 정보
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FileInfo {
        private Long id;                            // 파일 ID
        private String originalFileName;            // 원본 파일명
        private String filePath;                    // 파일 저장 경로 (다운로드에 사용)

        public static FileInfo from(File file) {
            if (file == null) {
                return null;
            }
            return FileInfo.builder()
                    .id(file.getId())
                    // File 엔티티 필드를 사용하여 매핑한다고 가정
                    .originalFileName(file.getOriginFile())
                    .filePath(file.getSysFile())
                    .build();
        }
    }

    /**
     * 게시글 목록 조회 응답 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ListInfo {

        private Long id;                            // 게시글 ID
        private String boardTitle;                  // 게시글 제목
        private String categoryName;                // 게시판 카테고리 이름
        private WriterInfo writer;                  // 작성자 정보 (중첩 DTO)
        private int viewCount;                      // 조회수
        private Instant createdAt;                  // 작성 일시
        private boolean hasFile;                    // 첨부파일 유무

        public static ListInfo from(Board board) {
            return ListInfo.builder()
                    .id(board.getId())
                    .boardTitle(board.getBoardTitle())
                    // BoardCategory 엔티티의 getBoardName() 사용 가정
                    .categoryName(board.getCategory() != null ? board.getCategory().getBoardName() : "N/A")
                    .writer(WriterInfo.from(board.getWriter()))
                    .viewCount(board.getViewCount())
                    .createdAt(board.getCreatedAt())
                    .hasFile(board.getFile() != null) // 파일 존재 여부 확인
                    .build();
        }

        /**
         * Board 엔티티 리스트를 ListInfo DTO 리스트로 변환하는 유틸리티 메서드
         */
        public static List<ListInfo> fromList(List<Board> boards) {
            return boards.stream()
                    .map(ListInfo::from)
                    .collect(Collectors.toList());
        }
    }

    /**
     * 게시글 상세 조회 응답 DTO
     */
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class DetailInfo {

        private Long id;                            // 게시글 ID
        private String boardTitle;                  // 게시글 제목
        private String boardContent;                // 게시글 내용
        private String categoryName;                // 게시판 카테고리 이름
        private WriterInfo writer;                  // 작성자 상세 정보 (중첩 DTO)
        private int viewCount;                      // 조회수
        private Instant createdAt;                  // 작성 일시
        private Instant updatedAt;                  // 수정 일시
        private FileInfo file;                      // 첨부파일 상세 정보 (중첩 DTO, 파일이 없으면 null)

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
                    .file(FileInfo.from(board.getFile()))
                    .build();
        }
    }
}