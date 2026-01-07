package com.finalproj.orbitflow.approval.calendarDay.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QCalendarDay is a Querydsl query type for CalendarDay
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QCalendarDay extends EntityPathBase<CalendarDay> {

    private static final long serialVersionUID = 1256049040L;

    public static final QCalendarDay calendarDay = new QCalendarDay("calendarDay");

    public final DatePath<java.time.LocalDate> date = createDate("date", java.time.LocalDate.class);

    public final NumberPath<Integer> dayOfWeek = createNumber("dayOfWeek", Integer.class);

    public final EnumPath<com.finalproj.orbitflow.approval.calendarDay.enums.CalendarDayType> dayType = createEnum("dayType", com.finalproj.orbitflow.approval.calendarDay.enums.CalendarDayType.class);

    public final StringPath holidayName = createString("holidayName");

    public final BooleanPath isPublicHoliday = createBoolean("isPublicHoliday");

    public QCalendarDay(String variable) {
        super(CalendarDay.class, forVariable(variable));
    }

    public QCalendarDay(Path<? extends CalendarDay> path) {
        super(path.getType(), path.getMetadata());
    }

    public QCalendarDay(PathMetadata metadata) {
        super(CalendarDay.class, metadata);
    }

}

