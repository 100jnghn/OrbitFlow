package com.finalproj.orbitflow.resource.car.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : CarViewController
 * @since : 2025-12-20 오후 4:35 토요일
 */
@RequestMapping("/view/resource")
@Controller
public class CarViewController {

    @GetMapping("/admin/cars")
    public String getCarsPage(Model model) {

        model.addAttribute("currentGNB", "admin");

        return "admin-car/admin-cars";
    }

    @GetMapping("/admin/cars/detail")
    public String getCarPage(
            Model model,
            @RequestParam Long id
    ) {
        model.addAttribute("carId", id);
        return "admin-car/admin-car";
    }

}
