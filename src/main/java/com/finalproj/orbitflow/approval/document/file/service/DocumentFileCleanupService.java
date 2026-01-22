package com.finalproj.orbitflow.approval.document.file.service;

import com.finalproj.orbitflow.approval.document.file.entity.DocumentFile;
import com.finalproj.orbitflow.approval.document.file.repository.DocumentFileRepository;
import com.finalproj.orbitflow.global.file.entity.File;
import com.finalproj.orbitflow.global.file.repository.FileRepository;
import com.finalproj.orbitflow.global.file.service.FileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 문서와의 연결이 끊어진 파일 리소스를 정리하는 서비스.
 * <p>
 * 문서 수정, 첨부파일 교체, 문서 복사 등의 과정에서
 * 더 이상 어떤 문서에도 참조되지 않는 파일이 발생할 수 있으며,
 * 이 서비스는 그러한 파일을 안전하게 정리하는 역할을 한다.
 * <p>
 * DocumentFile 엔티티를 기준으로 실제 참조 여부를 확인한 뒤,
 * 참조가 전혀 없는 경우에만 파일 메타데이터(DB)와
 * 스토리지 객체(S3 등)를 함께 삭제한다.
 * <p>
 * 스토리지 객체 삭제는 트랜잭션 커밋 이후 수행되도록 분리하여,
 * 데이터 정합성이 깨지는 상황을 방지하도록 설계되었다.
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileCleanupService
 * @since : 26. 1. 21. 수요일
 */


@Service
@RequiredArgsConstructor
public class DocumentFileCleanupService {

    private final DocumentFileRepository documentFileRepository;
    private final FileRepository fileRepository;
    private final FileService fileService;

    @Transactional
    public void cleanupDetachedFiles(List<DocumentFile> detachedFiles) {

        for (DocumentFile df : detachedFiles) {

            File file = df.getFile();
            if (file == null) {
                continue;
            }

            long refCount =
                    documentFileRepository.countByFile_Id(file.getId());

            if (refCount == 0) {
                fileRepository.delete(file);
                fileService.deleteObjectAfterCommit(file.getObjectKey());
            }
        }
    }
}
