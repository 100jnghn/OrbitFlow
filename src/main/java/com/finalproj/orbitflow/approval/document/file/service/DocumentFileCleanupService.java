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
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentFileCleanupService
 * @since : 26. 1. 21. 수요일
 **/


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
