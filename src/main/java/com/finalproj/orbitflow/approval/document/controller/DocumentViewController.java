package com.finalproj.orbitflow.approval.document.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
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
}
