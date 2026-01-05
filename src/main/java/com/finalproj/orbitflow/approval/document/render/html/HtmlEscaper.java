package com.finalproj.orbitflow.approval.document.render.html;

import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : HtmlEscaper
 * @since : 26. 1. 3. 토요일
 **/

@Component
public final class HtmlEscaper {

    private HtmlEscaper() {
    }

    public static String escape(Object value) {
        if (value == null) return "-";

        return String.valueOf(value)
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }
}
