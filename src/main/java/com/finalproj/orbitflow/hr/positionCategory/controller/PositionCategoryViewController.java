package com.finalproj.orbitflow.hr.positionCategory.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : PositionCategoryViewController
 * @since : 2025-12-28 일요일
 */
@Controller
@RequestMapping("/view/admin/position-categories")
public class PositionCategoryViewController {

    @GetMapping
    public String list(Model model) {
        model.addAttribute("pageTitle", "직책 관리");
        model.addAttribute("currentMenu", "position-category");
        model.addAttribute("sidebarFragment", "admin-sidebar");
        return "admin/hr/position-category/list";
    }
}