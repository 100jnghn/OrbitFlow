package com.finalproj.orbitflow.global.file.enums;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FileDomain
 * @since : 26. 1. 1. 목요일
 **/


public enum FileDomain {
    DOCUMENT("document"),
    BOARD("board"),
    CHAT("chat"),
    SIGNATURE("signature"),
    RESOURCE("resource"),
    PDF_FINAL("pdf-final"),
    MESSAGE("message");

    private final String path;

    FileDomain(String path) {
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}