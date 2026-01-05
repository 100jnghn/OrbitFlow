package com.finalproj.orbitflow.approval.document.documentContentRender;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : PdfContentSchema
 * @since : 26. 1. 3. 토요일
 **/


@Getter
@Builder
public class PdfContentSchema {

    private final List<PdfField> fields;

    public PdfContentSchema(List<PdfField> fields) {
        this.fields = fields;
    }
}