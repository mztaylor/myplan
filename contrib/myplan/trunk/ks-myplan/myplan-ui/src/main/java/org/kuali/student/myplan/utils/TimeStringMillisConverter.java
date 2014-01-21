package org.kuali.student.myplan.utils;

import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;


public class TimeStringMillisConverter {

    public static final long MILLIS_PER_SECOND = 1000L;
    public static final long MILLIS_PER_MINUTE = 60L * MILLIS_PER_SECOND;
    public static final long MILLIS_PER_HOUR = 60L * MILLIS_PER_MINUTE;
    public static final long MILLIS_PER_DAY = 60L * MILLIS_PER_HOUR;
    public static final String defaultDate = "1970-01-01";

    public final static Pattern militaryTimeRegex = Pattern.compile("([0-9][0-9]):([0-9][0-9])");

    public static long militaryTimeToMillis(String time) {
        Matcher m = militaryTimeRegex.matcher(time);
        if (m.find()) {
            int hours = Integer.parseInt(m.group(1));
            int minutes = Integer.parseInt(m.group(2));
            if (hours < 24 && minutes < 60) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                String inputString = String.format("%s:%s:00", hours, minutes);
                try {
                    Date date = sdf.parse(String.format("%s %s", defaultDate, inputString));
                    return date.getTime();
                } catch (Exception e) {
                    throw new NumberFormatException(time);
                }
            }

        }
        throw new NumberFormatException(time);
    }

    public static String millisToMilitaryTime(long millis) {
        // Limit to 24 hours (one day) max
        Date date = new Date(millis);
        int hours = date.getHours();
        int minutes = date.getMinutes();
        String time = String.format("%02d:%02d", hours, minutes);
        return time;
    }

    public static String displayCurrentDateTime(String format) {
        DateFormat dateFormat = new SimpleDateFormat(format);
        //get current date time with Date()
        Date date = new Date();
        return dateFormat.format(date);
    }

    public static String millisToStandardTime(long millis, String format) {
        // Limit to 24 hours (one day) max
        boolean am = true;
        Date date = new Date(millis);
        int hours = date.getHours();
        int minutes = date.getMinutes();
        if (hours > 11) {
            am = false;
            hours -= 12;
        }
        if (hours == 0) {
            hours = 12;
        }
        if (StringUtils.hasText(format)) {
            DateFormat dateFormat = new SimpleDateFormat(format);
            Date dateSt = new Date(millis);
            return dateFormat.format(dateSt);
        } else {
            return String.format("%d:%02d %s", hours, minutes, am ? "AM" : "PM");
        }
    }


    public static void main(String[] args)
            throws Exception {

        testMilitaryRoundTrip("X");
        testMilitaryRoundTrip("08:30");
        testMilitaryRoundTrip("00:00");
        testMilitaryRoundTrip("00:01");
        testMilitaryRoundTrip("01:00");
        testMilitaryRoundTrip("23:59");
        testMilitaryRoundTrip("12:00");
        testMilitaryRoundTrip("24:00");
        testMilitaryRoundTrip("00:60");

        testStandardToMilitary("08:30", "8:30 AM");
        testStandardToMilitary("00:00", "12:00 AM");
        testStandardToMilitary("12:00", "12:00 PM");
        testStandardToMilitary("12:30", "12:30 PM");
        testStandardToMilitary("13:30", "1:30 PM");
    }

    private static void testMilitaryRoundTrip(String example) {
        try {
            long timeofday = militaryTimeToMillis(example);
            String time = millisToMilitaryTime(timeofday);
            System.out.printf("\n%s == %s -> %b", example, time, example.equals(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testStandardToMilitary(String example, String expected) {
        try {
            long timeofday = militaryTimeToMillis(example);
            String time = millisToStandardTime(timeofday, null);
            System.out.printf("\n%s == %s -> %b", time, expected, expected.equals(time));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
