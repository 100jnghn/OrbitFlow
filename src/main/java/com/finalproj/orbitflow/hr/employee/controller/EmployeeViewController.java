package com.finalproj.orbitflow.hr.employee.controller;

import com.finalproj.orbitflow.hr.employee.service.EmployeeService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : EmployeeViewController
 * @since : 2025-12-30 화요일
 */
@Controller
@AllArgsConstructor
@RequestMapping("/view/admin/employees")
public class EmployeeViewController {

    private final EmployeeService employeeService;

    @GetMapping
    public String empList(Model model) {
        model.addAttribute("pageTitle", "사원 관리");
        model.addAttribute("currentMenu", "employee");
        model.addAttribute("sidebarFragment", "admin-sidebar");

        return "admin/employee/list";
    }

    @GetMapping("/{employeeId}")
    public String detail(@PathVariable Long employeeId, Model model) {

        model.addAttribute("employeeId", employeeId);

        model.addAttribute("pageTitle", "사원 상세");
        model.addAttribute("currentMenu", "employee");
        model.addAttribute("sidebarFragment", "admin-sidebar");

        return "admin/employee/detail";
    }

}