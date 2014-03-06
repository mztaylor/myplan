package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildEvent;

import java.util.Calendar;
import java.util.List;

/**
 * default ksap implementation of schedule builder helper methods.
 *
 * @author David McClellan
 * @version 0.1
 */

public class DefaultScheduleBuildHelper {

    // There are 288 5-minute segments in a 24-hour day, which can be represented
    // in 5 long's (64 bits * 5 = 320 bits), so the 5-minute segments a class meets
    // can be represented by the bits in those 5 long's.
    //
    // BLOCK_CACHE is an optimization by caching the long[5] with the
    // appropriate bits set for every [5-minute segment when a class begins]
    // [5-minute segment when a class ends] element
    // for which such a long[5] 'bitset' is requested
    private static long[][][] BLOCK_CACHE = new long[288][288][];

    public static long[] block(int fromSlot, int toSlot) {
        long[] rv = BLOCK_CACHE[fromSlot][toSlot];
        if (rv != null)
            return rv;
        rv = new long[5];
        for (int i = fromSlot; i <= toSlot; i++)
            rv[i / 64] |= 1L << i % 64;
        return BLOCK_CACHE[fromSlot][toSlot] = rv;
    }


    /**
     * add together (do a union) 2 weeks of activity time bits.
     * does not check for conflicts.
     *
     * @param week long[][] representing a week of activity bit flags which are added to
     * @param add  long[][] representing a week of activity bit flags
     *
     */

    public static void unionWeeks(long[][] week, long[][] add) {
        if (week.length != 7 || add.length != 7 ||
                week[0].length != 5 || add[0].length != 5)
            return;

        for (int i = 0; i < 7; i++)
            for (int j = 0; j < 5; j++)
                week[i][j] |= add[i][j];
    }


    /**
     * @param week1 long[][] representing a week of activity bit flags
     * @param week2 long[][] representing a week of activity bit flags
     *
     * @return true if there IS a conflict (or the data is invalid),
     *          false if there is NO conflict
     */
    public static boolean checkForConflictsWeeks(long[][] week1, long[][] week2) {
        if (week1.length != 7 || week2.length != 7 ||
                week1[0].length != 5 || week2[0].length != 5)
            return true;

        for (int i = 0; i < 7; i++)
            if (dayIntersects(week1[i], week2[i]))
                return true;

        return false;
    }

    public static boolean dayIntersects(long[] day1, long[] day2) {
        for (int i = 0; i < 5; i++) {
            if (0L != (day1[i] & day2[i])) {
                return true;
            }
        }
        return false;
    }


    /**
     * take the meeting times from an activity option, calculate the bit map showing which
     * times on which days of the week that the activity meets.
     *
     * @param event a ScheduleBuildEvent, most often a class meeting times
     *
     * @return array of longs, 7 days x 5 (to hold 288 5 min periods in 24 hrs)
     */

    public static long[][] xlateClassMeetingTime2WeekOfBits(ScheduleBuildEvent event) {
        long[][] week = new long[7][5];
        long[] day;

        Calendar c = Calendar.getInstance();
        c.setTime(event.getStartDate());
        int fromSlot = c.get(Calendar.HOUR_OF_DAY) * 12 + (c.get(Calendar.MINUTE) / 5);
        c.setTime(event.getUntilDate());
        int toSlot = c.get(Calendar.HOUR_OF_DAY) * 12 + (c.get(Calendar.MINUTE) / 5);

        day = block(fromSlot, toSlot);

        if (event.isSunday()) {
            week[0] = day;
        }
        if (event.isMonday()) {
            week[1] = day;
        }
        if (event.isTuesday()) {
            week[2] = day;
        }
        if (event.isWednesday()) {
            week[3] = day;
        }
        if (event.isThursday()) {
            week[4] = day;
        }
        if (event.isFriday()) {
            week[5] = day;
        }
        if (event.isSaturday()) {
            week[6] = day;
        }

        return week;
    }

    /**
     * take a list of  meeting times from an activity option, calculate the bit map showing which
     * times on which days the activities meet.
     * <p/>
     * this does not check for conflicts among the meeting times
     *
     * @param meetingTimes list of meetings times, presumably from an ActivityOption
     *
     * @return array of longs, 7 days x 5 (to hold 288 5 min periods in 24 hrs)
     */

    public static long[][] xlateClassMeetingTimeList2WeekBits(List<? extends ScheduleBuildEvent> meetingTimes) {
        long[][] week = new long[7][5];  // 7 days x 5 longs per day

        for (ScheduleBuildEvent meetingTime : meetingTimes) {
            long[][] meetingTimeWeekBits = xlateClassMeetingTime2WeekOfBits(meetingTime);
            unionWeeks(week, meetingTimeWeekBits);
        }
        return week;
    }


}


