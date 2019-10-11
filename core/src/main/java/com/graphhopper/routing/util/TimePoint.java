package com.graphhopper.routing.util;

import ch.poole.openinghoursparser.TimeSpan;

import java.util.Calendar;

public class TimePoint {
    int year;
    int week;
    int month;
    int day;
    int weekday;
    int minutes = 0;

    TimePoint(Calendar calendar, boolean shift) {
        if (shift) {
            calendar.add(Calendar.HOUR_OF_DAY, -24);
            minutes = TimeSpan.MAX_TIME;
        }
        year = calendar.get(Calendar.YEAR);
        week = calendar.get(Calendar.WEEK_OF_YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);
        weekday = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7; //normalize to match ordinals from OpeningHoursParser FIXME: might be locale specific
        minutes += calendar.get(Calendar.HOUR_OF_DAY) * 60 + calendar.get(Calendar.MINUTE);
    }

    public int getYear() {
        return year;
    }

    public int getWeek() {
        return week;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getWeekday() {
        return weekday;
    }

    public int getMinutes() {
        return minutes;
    }
}
