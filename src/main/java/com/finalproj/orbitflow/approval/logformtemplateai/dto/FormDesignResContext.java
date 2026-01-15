package com.finalproj.orbitflow.approval.logformtemplateai.dto;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormDesignResContext
 * @since : 26. 1. 8. 목요일
 **/


@Getter
public class FormDesignResContext {
    private final List<String> appliedRules = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();

    public void rule(String r) {
        appliedRules.add(r);
    }

    public void warn(String w) {
        warnings.add(w);
    }

}