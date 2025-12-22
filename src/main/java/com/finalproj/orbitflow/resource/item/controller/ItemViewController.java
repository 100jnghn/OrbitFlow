package com.finalproj.orbitflow.resource.item.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemViewController
 * @since : 2025-12-22 오전 9:29 월요일
 */
@RequestMapping("/view/resource")
@Controller
public class ItemViewController {

    @GetMapping("/admin/items")
    public String getItemsPage() {
        return "admin-item/admin-items";
    }
}
