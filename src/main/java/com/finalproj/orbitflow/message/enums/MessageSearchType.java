package com.finalproj.orbitflow.message.enums;

public enum MessageSearchType {
    TITLE, CONTENT, SENDER, RECIPIENT, ALL;

    public static MessageSearchType from(String value) {
        if (value == null || value.isBlank()) return ALL;
        try {
            return MessageSearchType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ALL;
        }
    }
}

