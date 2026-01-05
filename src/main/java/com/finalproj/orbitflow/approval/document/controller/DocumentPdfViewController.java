package com.finalproj.orbitflow.approval.document.controller;

import com.finalproj.orbitflow.approval.document.dto.DocumentPdfViewDto;
import com.finalproj.orbitflow.approval.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentPdfViewController
 * @since : 26. 1. 3. 토요일
 **/


@Controller
@RequiredArgsConstructor
@RequestMapping("/internal/documents")
public class DocumentPdfViewController {

    private final DocumentService documentService;

    @GetMapping("/{documentId}/pdf")
    public String renderPdfView(
            @PathVariable Long documentId,
            Model model
    ) {
        DocumentPdfViewDto dto =
                documentService.getPdfViewData(documentId);

        model.addAttribute("document", dto);
        model.addAttribute(
                "documentContentHtml",
                dto.getDocumentContentHtml()
        );

        return "/user-document/pdf-view";
    }

}
