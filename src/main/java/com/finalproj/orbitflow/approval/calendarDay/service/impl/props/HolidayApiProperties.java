package com.finalproj.orbitflow.approval.calendarDay.service.impl.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Please explain the class!!!
 *
 * @author : Choi MinHyeok
 * @filename : HolidayApiProperties
 * @since : 26. 1. 6. 화요일
 **/


@Data
@ConfigurationProperties(prefix = "holiday.api")
public class HolidayApiProperties {
    private String url;
    private String apiKey;
}