package com.finalproj.orbitflow.resource.itemcategory.controller;

import com.finalproj.orbitflow.global.security.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Please explain the class!!!
 *
 * @author : 종훈
 * @filename : ItemCategoryViewController
 * @since : 2025-12-22 오전 10:16 월요일
 */

@RequestMapping("/view/resource")
@Controller
public class ItemCategoryViewController {

    @GetMapping("/admin/item-categories")
    public String getItemCategoriesPage(
            @AuthenticationPrincipal SecurityUser user
    ) {
        return "admin-item-category/admin-item-categories";
    }
}
