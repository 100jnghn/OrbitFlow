package com.finalproj.orbitflow.hr.organization.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgViewController
 * @since : 2025-12-26 금요일
 */
@Controller
@RequestMapping("/view/admin/organizations")
public class OrgViewController {

    @GetMapping
    public String orgList(Model model) {

        model.addAttribute("pageTitle", "조직 관리");
        model.addAttribute("currentMenu", "organization");
        model.addAttribute("sidebarFragment", "admin-sidebar");

        return "admin/hr/organization/list";
    }
}
