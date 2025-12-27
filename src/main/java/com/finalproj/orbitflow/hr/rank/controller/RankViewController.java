package com.finalproj.orbitflow.hr.rank.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : RankViewController
 * @since : 2025-12-27 토요일
 */
@Controller
@RequestMapping("/view/admin/ranks")
public class RankViewController {

    /**
     * 직급 관리 목록 화면
     */
    @GetMapping
    public String orgCategoryList(Model model) {

        model.addAttribute("pageTitle", "직급 관리");
        model.addAttribute("currentMenu", "rank");
        model.addAttribute("sidebarFragment", "admin-sidebar");

        return "admin/hr/rank/list";
    }
}
