package com.finalproj.orbitflow.board.enums;

public enum BoardSearchType {
    TITLE, CONTENT, AUTHOR, ALL;

    public static BoardSearchType from(String value) {
        if (value == null || value.isBlank()) return ALL;
        try {
            return BoardSearchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ALL;
        }
    }
}
