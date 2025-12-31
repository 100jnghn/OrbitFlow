package com.finalproj.orbitflow.approval.document.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : DocumentViewController
 * @since : 25. 12. 23. 화요일
 **/


@Controller
@RequestMapping("/view/document")
public class DocumentViewController {

    @GetMapping("/my-documents")
    public String myWrittenDocumentList(Model model) {
        model.addAttribute("pageTitle", "기안함");
        model.addAttribute("currentMenu", "approval");
        return "my-documents/my-documents";
    }
    @GetMapping("/inbox")
    public String approvalInboxList(Model model) {
        model.addAttribute("pageTitle", "결재대기함");
        model.addAttribute("currentMenu", "approval");
        return "approval-inbox/approval-inbox";
    }

    @GetMapping("/write/{documentId}")
    public String writeDocument(
            Model model,
            @PathVariable Long documentId
    ) {
        model.addAttribute("pageTitle", "문서 작성");
        model.addAttribute("currentMenu", "approval");
        model.addAttribute("documentId", documentId);
        return "document-write/document-write";
    }

    @GetMapping("/{documentId}")
    public String readDocument(
            Model model,
            @PathVariable Long documentId
    ) {
        model.addAttribute("pageTitle", "문서 조회");
        model.addAttribute("currentMenu", "approval");
        model.addAttribute("documentId", documentId);
        return "document-detail/document-detail";
    }

}
