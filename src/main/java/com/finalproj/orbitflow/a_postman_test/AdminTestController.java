package com.finalproj.orbitflow.a_postman_test;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Please explain the class!!!
 *
 * @author : seunga03
 * @filename : AdminTestController
 * @since : 2025-12-18 목요일
 */
@RestController
@RequestMapping("/api/admin")
public class AdminTestController {

    @GetMapping("/test")
    public String test() {
        return "ADMIN OK";
    }
}
