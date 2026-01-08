package com.finalproj.orbitflow.approval.logFormTemplateAi.policy;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : FormInputConstraints
 * @since : 26. 1. 7. 수요일
 **/


public final class FormInputConstraints {

    private FormInputConstraints() {
    }
    public static final int LABEL_MAX_LENGTH = 20;
    public static final int TEXT_MAX_LENGTH = 25;
    public static final int TEXTAREA_MAX_LENGTH = 500;
    public static final int IMAGE_COMPONENT_MAX = 5;
    public static final int NOTICE_MESSAGE_MAX = 200;
    public static final int TABLE_ROW_MAX = 10;
    public static final int TABLE_COLUMN_MAX = 10;
    public static final int TABLE_CELL_TEXT_MAX_LENGTH = 20;
}
