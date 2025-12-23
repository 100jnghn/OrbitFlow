package com.finalproj.orbitflow.resource.item.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemViewController
 * @since : 2025-12-22 오전 9:29 월요일
 */
@RequestMapping("/view/resource")
@Controller
@Slf4j
public class ItemViewController {

    @GetMapping("/admin/items")
    public String getItemsPage() {
        return "admin-item/admin-items";
    }

    @GetMapping("/admin/items/detail")
    public String getItemPage(
            Model model,
            @RequestParam Long id
    ) {
        log.info("아이디 : " + id);
        model.addAttribute("itemId", id);
        return "admin-item/admin-item";
    }

    @GetMapping("/admin/items/insert")
    public String getInsertItemPage() {
        return "admin-item/admin-item-insert";
    }
}
