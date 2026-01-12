package com.finalproj.orbitflow.global.common;

import com.finalproj.orbitflow.global.security.SecurityUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@Slf4j
public class GlobalModelAdvice {

    @ModelAttribute("companyName")
    public String companyName(@AuthenticationPrincipal SecurityUser user) {
        if (user != null) {
            return user.getCompanyName();
        }
        return null;
    }
}
