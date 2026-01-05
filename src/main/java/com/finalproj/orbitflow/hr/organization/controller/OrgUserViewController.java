package com.finalproj.orbitflow.hr.organization.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : OrgUserViewController
 * @since : 2026-01-05 월요일
 */

@Controller
@RequestMapping("/view/organizations")
public class OrgUserViewController {
    @GetMapping
    public String orgTree(
            @RequestParam(required = false) Long employeeId,
            Model model
    ) {
        model.addAttribute("employeeId", employeeId);
        model.addAttribute("pageTitle", "조직도");
        model.addAttribute("sidebarFragment", "user-sidebar");
        return "organization/tree";
    }

}
