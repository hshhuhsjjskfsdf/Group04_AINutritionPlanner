package com.example.a23110035_23110060.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateHelper {
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    private DateHelper() {
    }

    public static String today() {
        return formatDate(new Date());
    }

    public static String formatDate(Date date) {
        return new SimpleDateFormat(DATE_PATTERN, Locale.US).format(date);
    }

    public static String formatTime(long timestamp) {
        if (timestamp <= 0) return "--:--";
        return new SimpleDateFormat("HH:mm", Locale.US).format(new Date(timestamp));
    }

    public static String getStartOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        return formatDate(calendar.getTime());
    }

    public static String getEndOfWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.add(Calendar.DAY_OF_MONTH, 6);
        return formatDate(calendar.getTime());
    }
}
