package org.kuali.student.myplan.schedulebuilder.support;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeComparator;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.dto.PossibleScheduleOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ReservedTimeInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.ClassMeetingTime;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleErrors;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildEvent;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildHelper;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilderConstants;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

/**
 * default ksap implementation of schedule builder helper methods.
 *
 * @author David McClellan
 * @version 0.1
 */

public class DefaultScheduleBuildHelper implements ScheduleBuildHelper {

    private final Logger logger = Logger.getLogger(DefaultScheduleBuildHelper.class);

    private CalendarUtil calendarUtil;

    private Properties properties;

    public DefaultScheduleBuildHelper() {
        properties = new Properties();
        InputStream file = getClass().getResourceAsStream(PlanConstants.PROPERTIES_FILE_PATH);
        try {
            properties.load(file);
        } catch (Exception e) {
            logger.error("Could not find the properties file" + e);
        }
    }

    // There are 288 5-minute segments in a 24-hour day, which can be represented
    // in 5 long's (64 bits * 5 = 320 bits), so the 5-minute segments a class meets
    // can be represented by the bits in those 5 long's.
    //
    // BLOCK_CACHE is an optimization by caching the long[5] with the
    // appropriate bits set for every [5-minute segment when a class begins]
    // [5-minute segment when a class ends] element
    // for which such a long[5] 'bitset' is requested
    private long[][][] BLOCK_CACHE = new long[288][288][];

    private static class EventAggregateData {
        private final Calendar cal = Calendar.getInstance();
        private final Date minDate;
        private final Date maxDate;
        private final Date displayDate;
        private final Set<Date> breakDates = new TreeSet<Date>();

        private boolean weekends;
        private int minTime = 8;
        private int maxTime = 17;
        private Date lastUntilDate;

        private EventAggregateData(Date minDate, Date maxDate, Date displayDate) {
            this.minDate = minDate;
            this.maxDate = maxDate;
            this.displayDate = displayDate;
        }

        private Date getDatePortion(Date date) {
            if (date == null)
                return null;

            cal.setTime(date);
            if (cal.get(Calendar.YEAR) == 1970
                    && cal.get(Calendar.MONTH) == Calendar.JANUARY
                    && cal.get(Calendar.DATE) == 1)
                return null;

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }

        private Date getTimePortion(Date date) {
            if (date == null) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            } else
                cal.setTime(date);

            cal.set(Calendar.YEAR, 1970);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DATE, 1);
            return cal.getTime();
        }

        private void addBreakDate(Date date) {
            getDatePortion(date); // adjusts cal
            cal.add(Calendar.DATE,
                    -(cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
            breakDates.add(minDate.after(cal.getTime()) ? minDate : cal
                    .getTime());
        }

        private void updateLastUntilDate(Date untilDate) {
            getDatePortion(untilDate); // adjusts cal
            cal.add(Calendar.DATE,
                    Calendar.SUNDAY + 7 - cal.get(Calendar.DAY_OF_WEEK));
            if (maxDate.after(cal.getTime()))
                breakDates.add(cal.getTime());
            if (!maxDate.before(untilDate)
                    && (lastUntilDate == null || (untilDate != null && lastUntilDate
                    .before(untilDate))))
                lastUntilDate = untilDate;
        }

        private void updateMinMaxTime(Date eventStart, Date eventEnd) {
            cal.setTime(eventStart);
            minTime = Math.min(minTime, cal.get(Calendar.HOUR_OF_DAY));
            cal.setTime(eventEnd);
            maxTime = Math.max(maxTime,
                    cal.get(Calendar.HOUR_OF_DAY)
                            + (cal.get(Calendar.MINUTE) > 0 ? 1 : 0)
            );
        }

        public void updateWeekends(boolean weekend) {
            weekends = weekend;
        }

        private Date addOneWeek(Date eventStart) {
            cal.setTime(eventStart);
            cal.add(Calendar.DATE, 7);
            return cal.getTime();
        }

        private void addWeekBreaks(JsonArrayBuilder jweeks, Term term) {
            int weekNumber = 1;
            Iterator<Date> weekBreaks = breakDates.iterator();
            Date start = weekBreaks.hasNext() ? weekBreaks.next() : term.getStartDate();
            if (lastUntilDate == null)
                lastUntilDate = term.getEndDate();
            DateFormat df = new SimpleDateFormat("MMM d");
            boolean done;
            do {
                Date end = (done = !weekBreaks.hasNext()) ? lastUntilDate
                        : weekBreaks.next();
                long sd = start.getTime();
                long ed = end.getTime();
                int weeks = (int) ((ed - sd) / 604800000);
                JsonObjectBuilder jweek = Json.createObjectBuilder();
                jweek.add("title", "Week" + (weeks > 1 ? "s " : " ")
                        + weekNumber
                        + (weeks > 1 ? "-" + (weekNumber + weeks - 1) : ""));
                jweek.add("subtitle", df.format(start) + " - " + df.format(end));
                cal.setTime(start);
                jweek.add("gotoYear", cal.get(Calendar.YEAR));
                jweek.add("gotoMonth", cal.get(Calendar.MONTH) + 1);
                jweek.add("gotoDate", cal.get(Calendar.DATE));
                jweeks.add(jweek);
                weekNumber += weeks;
                start = end;
            } while (!done);
        }
    }


    public long[] block(int fromSlot, int toSlot) {
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
     */

    public void unionWeeks(long[][] week, long[][] add) {
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
     * @return true if there IS a conflict (or the data is invalid),
     * false if there is NO conflict
     */
    public boolean checkForConflictsWeeks(long[][] week1, long[][] week2) {
        if (week1.length != 7 || week2.length != 7 ||
                week1[0].length != 5 || week2[0].length != 5)
            return true;

        for (int i = 0; i < 7; i++)
            if (dayIntersects(week1[i], week2[i]))
                return true;

        return false;
    }

    public boolean dayIntersects(long[] day1, long[] day2) {
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
     * @return array of longs, 7 days x 5 (to hold 288 5 min periods in 24 hrs)
     */

    public long[][] xlateClassMeetingTime2WeekOfBits(ScheduleBuildEvent event) {
        long[][] week = new long[7][5];
        long[] day;

        if (event.getStartDate() == null) {
            // in case TBA does not have a Date, use the default zeroes
            return week;
        }

        Calendar c = Calendar.getInstance();
        c.setTime(event.getStartDate());
        int fromSlot = c.get(Calendar.HOUR_OF_DAY) * 12 + (c.get(Calendar.MINUTE) / 5);

        c.setTime(event.getUntilDate());
        // Jira MYPLAN-2664; bump end-of-time-period minute back by 1 to avoid conflicts
        // between time periods where one ends at the same time the next starts
        int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
        int minutes = c.get(Calendar.MINUTE) - 1;
        if (-1 == minutes) { // it was top of the hour; knock hour back as well
            minutes = 59;
            // was it midnight? back to 23:59
            hourOfDay = (0 != hourOfDay ? hourOfDay - 1 : 23);
        }
        int toSlot = (hourOfDay * 12) + (minutes / 5);

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
     * @return array of longs, 7 days x 5 (to hold 288 5 min periods in 24 hrs)
     */

    public long[][] xlateClassMeetingTimeList2WeekBits(List<? extends ScheduleBuildEvent> meetingTimes) {
        long[][] week = new long[7][5];  // 7 days x 5 longs per day

        for (ScheduleBuildEvent meetingTime : meetingTimes) {
            long[][] meetingTimeWeekBits = xlateClassMeetingTime2WeekOfBits(meetingTime);
            unionWeeks(week, meetingTimeWeekBits);
        }
        return week;
    }


    /**
     * Builds the json string with events  required for the calendar and sets that to the reservedTime event property
     *
     * @param rt
     */
    public void buildReservedTimeEvents(ReservedTime rt, Term term) {
        Date minDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date maxDate = getCalendarUtil().getDateAfterXdays(minDate, 5);
        Date displayDate = term.getEndDate();
        SimpleDateFormat ddf = new SimpleDateFormat("MM/dd/yyyy");

        EventAggregateData aggregate = new EventAggregateData(minDate, maxDate, displayDate);
        StringWriter stringWriter = new StringWriter();
        JsonGenerator jPso = Json.createGenerator(stringWriter);
        jPso.writeStartObject();
        jPso.write("id", rt.getId()).write("uniqueId", rt.getUniqueId()).write("termId", term.getId()).write("daysTimes", rt.getDaysAndTimes()).write("startDate", ddf.format(rt.getStartDate())).write("untilDate", ddf.format(rt.getUntilDate()));
        jPso.writeStartArray("events");
        addEvents(term, rt, null, jPso, aggregate, null, rt.getUniqueId(), null);
        jPso.writeEnd().flush();
        jPso.writeEnd().flush();
        ReservedTimeInfo reservedTimeInfo = (ReservedTimeInfo) rt;
        reservedTimeInfo.setEvent(stringWriter.toString());

    }

    /**
     * Builds the json string with events  required for the calendar and sets that to the possibleScheduleOption event property
     *
     * @param pso
     */
    public void buildPossibleScheduleEvents(PossibleScheduleOption pso, Term term) {
        Date minDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date maxDate = getCalendarUtil().getDateAfterXdays(minDate, 6);
        Date displayDate = term.getEndDate();
        EventAggregateData aggregate = new EventAggregateData(minDate, maxDate, displayDate);
        if (pso.isDiscarded()) {
            return;
        }

        Map<String, List<ActivityOption>> scheduledCourseActivities = new LinkedHashMap<String, List<ActivityOption>>();
        for (ActivityOption activityOption : pso.getActivityOptions()) {
            String key = activityOption.getCourseCd();
            if (scheduledCourseActivities.containsKey(key)) {
                scheduledCourseActivities.get(key).add(activityOption);
            } else {
                List<ActivityOption> activityOptions = new ArrayList<ActivityOption>();
                activityOptions.add(activityOption);
                scheduledCourseActivities.put(key, activityOptions);
            }
        }

        StringWriter stringWriter = new StringWriter();
        JsonGenerator jPso = Json.createGenerator(stringWriter);
        jPso.writeStartObject();
        jPso.write("uniqueId", pso.getUniqueId()).write("selected", pso.isSelected());
        if (StringUtils.hasText(pso.getId())) {
            jPso.write("id", pso.getId());
        }

        jPso.writeStartArray("events");

        /*Defaulting to 8:00Am*/
        Calendar defaultStart = Calendar.getInstance();
        defaultStart.set(defaultStart.get(Calendar.YEAR), defaultStart.get(Calendar.MONTH), defaultStart.get(Calendar.DATE), 8, 0);

        /*Defaulting to 5:00Pm*/
        Calendar defaultEnd = Calendar.getInstance();
        defaultEnd.set(defaultEnd.get(Calendar.YEAR), defaultEnd.get(Calendar.MONTH), defaultEnd.get(Calendar.DATE), 17, 0);


        PossibleScheduleOptionInfo possibleScheduleOptionInfo = (PossibleScheduleOptionInfo) pso;

        processActivitiesForEvents(pso.getActivityOptions(), defaultStart, defaultEnd, term, jPso, aggregate, scheduledCourseActivities, possibleScheduleOptionInfo);

        jPso.writeEnd().writeEnd().flush();

        possibleScheduleOptionInfo.setEvent(stringWriter.toString());
        possibleScheduleOptionInfo.setMinTime(defaultStart.getTime().getTime());
        possibleScheduleOptionInfo.setMaxTime(defaultEnd.getTime().getTime());

    }

    /**
     * Process possible schedule activity options to generate JsonEvents required for the UI.
     * And also takes care of whether there are any TBD's or WEEKEND's included in any of the activity class meetings.
     *
     * @param activityOptions
     * @param defaultStart
     * @param defaultEnd
     * @param term
     * @param jEvents
     * @param aggregate
     * @param scheduledCourseActivities
     * @param pso
     */
    private void processActivitiesForEvents(List<ActivityOption> activityOptions, Calendar defaultStart, Calendar defaultEnd, Term term, JsonGenerator jEvents, EventAggregateData aggregate, Map<String, List<ActivityOption>> scheduledCourseActivities, PossibleScheduleOptionInfo pso) {
        for (ActivityOption ao : activityOptions) {

            for (ClassMeetingTime meeting : ao.getClassMeetingTimes()) {
                if (!pso.isWeekend()) {
                    pso.setWeekend(meeting.isSaturday() || meeting.isSunday());
                }
                if (!pso.isTbd()) {
                    pso.setTbd(!meeting.isArranged());
                }
                if (meeting.isArranged()) {
                    Calendar startCal = Calendar.getInstance();
                    startCal.setTime(meeting.getStartDate());
                    Calendar endCal = Calendar.getInstance();
                    endCal.setTime(meeting.getUntilDate());

                    DateTimeComparator comparator = DateTimeComparator.getTimeOnlyInstance();
                    if (comparator.compare(startCal.getTime(), defaultStart.getTime()) == -1) {
                        defaultStart.set(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DATE), startCal.get(Calendar.HOUR_OF_DAY), 0);
                    }
                    if (comparator.compare(endCal.getTime(), defaultEnd.getTime()) == 1) {
                        defaultEnd.set(endCal.get(Calendar.YEAR), endCal.get(Calendar.MONTH), endCal.get(Calendar.DATE), endCal.get(Calendar.MINUTE) == 0 ? endCal.get(Calendar.HOUR_OF_DAY) : (endCal.get(Calendar.HOUR_OF_DAY) == 23 ? 00 : endCal.get(Calendar.HOUR_OF_DAY) + 1), 0);
                    }

                    addEvents(term, meeting, ao, jEvents, aggregate, scheduledCourseActivities, pso.getUniqueId(), pso.getPossibleErrors());


                } else {
                    /*Creating minimal events for TBD activities*/
                    createEvent(null, null, null, 0, 0, ao, scheduledCourseActivities, pso.getUniqueId(), jEvents, true, pso.getPossibleErrors());
                }
            }

            for (SecondaryActivityOptions secondaryActivityOptions : ao.getSecondaryOptions()) {
                processActivitiesForEvents(secondaryActivityOptions.getActivityOptions(), defaultStart, defaultEnd, term, jEvents, aggregate, scheduledCourseActivities, pso);
            }

        }
    }

    /**
     * Builds json events for given params
     *
     * @param term
     * @param meeting
     * @param ao
     * @param jEvents
     * @param aggregate
     * @param scheduledCourseActivities
     * @param parentUniqueId
     */
    protected void addEvents(Term term, ScheduleBuildEvent meeting, ActivityOption ao, JsonGenerator jEvents, EventAggregateData aggregate, Map<String, List<ActivityOption>> scheduledCourseActivities, String parentUniqueId, PossibleScheduleErrors possibleScheduleErrors) {
        /**
         * This is used to adjust minDate and maxDate which is a week from min date and adjust min date to be monday if it is not.
         * Used in building a week worth of schedules instead of whole term.
         * */
        Date termStartDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date termEndDate = getCalendarUtil().getDateAfterXdays(termStartDate, 5);
        Date meetingStartDate = getCalendarUtil().getNextMonday(meeting.getStartDate());
        Date until = getCalendarUtil().getNextMonday(meeting.getStartDate());
        until.setTime(meeting.getUntilDate().getTime());
        Date meetingEndDate = getCalendarUtil().getDateAfterXdays(until, 5);

        Date startDate = aggregate.getDatePortion(meetingStartDate);
        if (startDate == null || startDate.before(termStartDate))
            startDate = aggregate.getDatePortion(termStartDate);
        aggregate.addBreakDate(startDate);

        Date untilDate = aggregate.getDatePortion(meetingEndDate);
        if (untilDate == null || untilDate.after(termEndDate))
            untilDate = aggregate.getDatePortion(termEndDate);
        aggregate.updateLastUntilDate(untilDate);

        aggregate.updateWeekends(meeting.isSunday() || meeting.isSaturday());

        Date eventStart = aggregate.getTimePortion(meetingStartDate);
        long durationSeconds;
        if (meeting.isAllDay())
            durationSeconds = 0L;
        else {
            Date eventEnd = aggregate.getTimePortion(meetingEndDate);
            durationSeconds = (eventEnd.getTime() - eventStart.getTime()) / 1000;
            aggregate.updateMinMaxTime(eventStart, eventEnd);
        }

        while (!startDate.after(untilDate)) {
            if (meeting.isSunday())
                createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.SUNDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId, jEvents, false, possibleScheduleErrors);
            if (meeting.isMonday())
                createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.MONDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId, jEvents, false, possibleScheduleErrors);
            if (meeting.isTuesday())
                createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.TUESDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId, jEvents, false, possibleScheduleErrors);
            if (meeting.isWednesday())
                createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.WEDNESDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId, jEvents, false, possibleScheduleErrors);
            if (meeting.isThursday())
                createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.THURSDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId, jEvents, false, possibleScheduleErrors);
            if (meeting.isFriday())
                createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.FRIDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId, jEvents, false, possibleScheduleErrors);
            if (meeting.isSaturday())
                createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.SATURDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId, jEvents, false, possibleScheduleErrors);
            startDate = aggregate.addOneWeek(startDate);
        }
    }


    /**
     * Builds the actual Json events required for calendar
     *
     * @param startDate
     * @param eventStart
     * @param cal
     * @param dow
     * @param durationSeconds
     * @param ao
     * @param scheduledCourseActivities
     * @param parentUniqueId
     * @return
     */
    protected void createEvent(Date startDate,
                               Date eventStart, Calendar cal, int dow, long durationSeconds,
                               ActivityOption ao, Map<String, List<ActivityOption>> scheduledCourseActivities, String parentUniqueId, JsonGenerator jEvents, boolean isTBD, PossibleScheduleErrors possibleScheduleErrors) {
        long eventStartSeconds = 0;
        if (!isTBD) {
            // Calculate the date for the event in seconds since the epoch
            cal.setTime(startDate);
            cal.add(Calendar.DATE, dow - cal.get(Calendar.DAY_OF_WEEK));
            eventStartSeconds = cal.getTime().getTime() / 1000L;

            // Add the time for the event in seconds since midnight GMT
            cal.setTime(eventStart);
            eventStartSeconds += cal.getTime().getTime() / 1000L;

            // Adjust for time zone by subtracting midnight prior to the event.
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            eventStartSeconds -= cal.getTime().getTime() / 1000L;
        }
        jEvents.writeStartObject();
        /*using parentUniqueId for UI purpose of hiding and showing the events based on scheduleId*/
        jEvents.write("parentUniqueid", parentUniqueId).write("tbd", isTBD);
        /*Title value is populated in JS because we don't know what is the index value of the possibleSchedule*/
        jEvents.write("title", "");
        if (!isTBD) {
            jEvents.write("start", eventStartSeconds);
        }
        if (!isTBD) {
            if (durationSeconds == 0) {
                jEvents.write("allDay", true);
            } else {
                jEvents.write("allDay", false).write("end", eventStartSeconds + durationSeconds);
            }
        }
        List<String> invalidatedActivities = new ArrayList<String>();
        if (ao != null) {
            jEvents.writeStartObject("popoverContent").write("courseCd", ao.getCourseCd()).write("courseId", ao.getCourseId()).write("courseTitle", ao.getCourseTitle().trim()).write("termId", ao.getTermId());

            List<String> activitiesToExclude = new ArrayList<String>();
            List<ActivityOption> activityOptions = scheduledCourseActivities.get(ao.getCourseCd());

            if (possibleScheduleErrors != null && StringUtils.hasText(possibleScheduleErrors.getErrorType()) && !CollectionUtils.isEmpty(possibleScheduleErrors.getInvalidOptions()) && !CollectionUtils.isEmpty(possibleScheduleErrors.getInvalidOptions().get(ao.getCourseCd()))) {
                Map<String, List<String>> invalidActivities = possibleScheduleErrors.getInvalidOptions().get(ao.getCourseCd());

                List<String> withdrawnErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN);
                List<String> closedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED);
                List<String> suspendedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED);
                List<String> enrollErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR);
                List<String> timeChangedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED);
                List<String> conflictedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED);
                List<String> unAvailableSecondariesErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES);

                activitiesToExclude.addAll(withdrawnErrorActivities);
                invalidatedActivities.addAll(withdrawnErrorActivities);
                activitiesToExclude.addAll(closedErrorActivities);
                activitiesToExclude.addAll(enrollErrorActivities);
                activitiesToExclude.addAll(suspendedErrorActivities);
                invalidatedActivities.addAll(suspendedErrorActivities);
                activitiesToExclude.addAll(timeChangedErrorActivities);
                invalidatedActivities.addAll(timeChangedErrorActivities);
                activitiesToExclude.addAll(conflictedErrorActivities);
                invalidatedActivities.addAll(conflictedErrorActivities);
                activitiesToExclude.addAll(unAvailableSecondariesErrorActivities);
                invalidatedActivities.addAll(unAvailableSecondariesErrorActivities);
                Collections.sort(activitiesToExclude);
                Collections.sort(invalidatedActivities);
                if (activityOptionsContains(activityOptions, activitiesToExclude)) {
                    if (ScheduleBuilderConstants.PINNED_SCHEDULES_PASSIVE_ERROR.equals(possibleScheduleErrors.getErrorType())) {

                        StringBuffer errorMessage = new StringBuffer();
                        if (!CollectionUtils.isEmpty(withdrawnErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                            if (withdrawnErrorActivities.size() > 1) {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_WITHDRAWN_MULTIPLE), ao.getCourseCd(), org.apache.commons.lang.StringUtils.join(withdrawnErrorActivities.subList(0, withdrawnErrorActivities.size() - 1), ", "), withdrawnErrorActivities.get(withdrawnErrorActivities.size() - 1))).append("</p>");
                            } else {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_WITHDRAWN_SINGLE), ao.getCourseCd(), withdrawnErrorActivities.get(0))).append("</p>");
                            }

                        }
                        if (!CollectionUtils.isEmpty(closedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                            if (closedErrorActivities.size() > 1) {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_CLOSED_MULTIPLE), ao.getCourseCd(), org.apache.commons.lang.StringUtils.join(closedErrorActivities.subList(0, closedErrorActivities.size() - 1), ", "), closedErrorActivities.get(closedErrorActivities.size() - 1))).append("</p>");
                            } else {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_CLOSED_SINGLE), ao.getCourseCd(), closedErrorActivities.get(0))).append("</p>");
                            }

                        }
                        if (!CollectionUtils.isEmpty(suspendedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                            if (suspendedErrorActivities.size() > 1) {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_SUSPENDED_MULTIPLE), ao.getCourseCd(), org.apache.commons.lang.StringUtils.join(suspendedErrorActivities.subList(0, suspendedErrorActivities.size() - 1), ", "), suspendedErrorActivities.get(suspendedErrorActivities.size() - 1))).append("</p>");
                            } else {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_SUSPENDED_SINGLE), ao.getCourseCd(), suspendedErrorActivities.get(0))).append("</p>");
                            }

                        }
                        if (!CollectionUtils.isEmpty(enrollErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                            if (enrollErrorActivities.size() > 1) {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_SUSPENDED_MULTIPLE), ao.getCourseCd(), org.apache.commons.lang.StringUtils.join(enrollErrorActivities.subList(0, enrollErrorActivities.size() - 1), ", "), enrollErrorActivities.get(enrollErrorActivities.size() - 1))).append("</p>");
                            } else {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_SUSPENDED_SINGLE), ao.getCourseCd(), enrollErrorActivities.get(0))).append("</p>");
                            }

                        }
                        if (!CollectionUtils.isEmpty(timeChangedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                            if (timeChangedErrorActivities.size() > 1) {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_TIME_CHANGED_MULTIPLE), ao.getCourseCd(), org.apache.commons.lang.StringUtils.join(timeChangedErrorActivities.subList(0, timeChangedErrorActivities.size() - 1), ", "), timeChangedErrorActivities.get(timeChangedErrorActivities.size() - 1))).append("</p>");
                            } else {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_TIME_CHANGED_SINGLE), ao.getCourseCd(), timeChangedErrorActivities.get(0))).append("</p>");
                            }
                        }
                        if (!CollectionUtils.isEmpty(conflictedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                            if (conflictedErrorActivities.size() > 1) {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_RESERVED_CONFLICT_MULTIPLE), ao.getCourseCd(), org.apache.commons.lang.StringUtils.join(conflictedErrorActivities.subList(0, conflictedErrorActivities.size() - 1), ", "), conflictedErrorActivities.get(conflictedErrorActivities.size() - 1))).append("</p>");
                            } else {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_RESERVED_CONFLICT_SINGLE), ao.getCourseCd(), conflictedErrorActivities.get(0))).append("</p>");
                            }
                        }
                        if (!CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                            unAvailableSecondariesErrorActivities.addAll(withdrawnErrorActivities);
                            unAvailableSecondariesErrorActivities.addAll(timeChangedErrorActivities);
                            unAvailableSecondariesErrorActivities.addAll(conflictedErrorActivities);
                            if (unAvailableSecondariesErrorActivities.size() > 1) {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_NO_SECONDARIES_MULTIPLE), ao.getCourseCd(), org.apache.commons.lang.StringUtils.join(unAvailableSecondariesErrorActivities.subList(0, unAvailableSecondariesErrorActivities.size() - 1), ", "), unAvailableSecondariesErrorActivities.get(unAvailableSecondariesErrorActivities.size() - 1))).append("</p>");
                            } else {
                                errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_NO_SECONDARIES_SINGLE), ao.getCourseCd(), unAvailableSecondariesErrorActivities.get(0))).append("</p>");
                            }
                        }
                        if (StringUtils.hasText(errorMessage)) {
                            jEvents.write("errorType", possibleScheduleErrors.getErrorType()).write("errorMessage", errorMessage.toString());
                        }
                    } else if (ScheduleBuilderConstants.PINNED_SCHEDULES_MODAL_ERROR.equals(possibleScheduleErrors.getErrorType())) {
                        StringBuffer errorMessage = new StringBuffer();
                        if (activitiesToExclude.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.INVALID_PINNED_SCHEDULE_MULTIPLE), ao.getCourseCd(), org.apache.commons.lang.StringUtils.join(activitiesToExclude.subList(0, activitiesToExclude.size() - 1), ", "), activitiesToExclude.get(activitiesToExclude.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.INVALID_PINNED_SCHEDULE_SINGLE), ao.getCourseCd(), activitiesToExclude.get(0))).append("</p>");
                        }
                        if (StringUtils.hasText(errorMessage)) {
                            jEvents.write("errorType", possibleScheduleErrors.getErrorType()).write("errorMessage", errorMessage.toString());
                        }
                    }
                }
            }

            jEvents.writeStartArray("activities");
            buildCoursePopoverEvents(activityOptions, jEvents, isTBD, invalidatedActivities);
            jEvents.writeEnd().writeEnd().flush();
        }
        jEvents.writeEnd().flush();
    }


    /**
     * recursive method used to check if a activity cd is present in any of the primary, secondary or alternate activity options
     *
     * @param activityOptions
     * @param activityCds
     * @return
     */
    private boolean activityOptionsContains(List<ActivityOption> activityOptions, List<String> activityCds) {
        for (ActivityOption activityOption : activityOptions) {
            if (activityCds.contains(activityOption.getActivityCode())) {
                return true;
            }
            if (activityOptionsContains(activityOption.getAlternateActivties(), activityCds)) {
                return true;
            }

            for (SecondaryActivityOptions so : activityOption.getSecondaryOptions()) {
                if (activityOptionsContains(so.getActivityOptions(), activityCds)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Recursive method which buildsPopoverEvents for Activities, Secondary Activities, AlternateActivities at the same time.
     */
    private void buildCoursePopoverEvents(List<ActivityOption> activityOptions, JsonGenerator jEvents, boolean isTBD, List<String> invalidActivities) {
        for (ActivityOption activityOption : activityOptions) {
            if (!invalidActivities.contains(activityOption.getActivityCode())) {
                String instituteCd = "";
                if (ScheduleBuilderConstants.PCE_INSTITUTE_CODE.equals(activityOption.getInstituteCode())) {
                    instituteCd = ScheduleBuilderConstants.PCE_INSTITUTE_NAME;
                } else if (ScheduleBuilderConstants.ROTC_INSTITUTE_CODE.equals(activityOption.getInstituteCode())) {
                    instituteCd = ScheduleBuilderConstants.ROTC_INSTITUTE_NAME;
                }
                jEvents.writeStartObject().write("sectionCd", activityOption.getActivityCode()).write("primary", activityOption.isPrimary()).write("activityId", activityOption.getActivityOfferingId()).write("registrationCode", activityOption.getRegistrationCode()).write("instituteCd", instituteCd).write("registered", activityOption.isLockedIn()).write("enrollRestriction", activityOption.isEnrollmentRestriction()).write("enrollStatus", String.format("%s/%s", activityOption.getFilledSeats(), activityOption.getTotalSeats())).write("enrollState", activityOption.getEnrollStatus()).writeStartArray("meetings");
                if (!isTBD) {
                    for (ClassMeetingTime meetingTime : activityOption.getClassMeetingTimes()) {
                        jEvents.writeStartObject().write("meetingDay", org.apache.commons.lang.StringUtils.join(meetingTime.getDays(), "")).write("meetingTime", meetingTime.getTimes()).write("location", meetingTime.getLocation());
                        String campus = meetingTime.getCampus();
                        String building = "";
                        String buildingUrl = "";
                        if (meetingTime.getBuilding() != null) {
                            if (!"NOC".equals(meetingTime.getBuilding()) && !meetingTime.getBuilding().startsWith("*") && campus.equalsIgnoreCase("seattle")) {
                                building = meetingTime.getBuilding();
                                buildingUrl = PlanConstants.BUILDING_URL + building;
                            } else {
                                building = meetingTime.getBuilding();
                            }
                        }
                        jEvents.write("building", building).write("buildingUrl", buildingUrl).writeEnd().flush();
                    }
                }
                jEvents.writeEnd().writeEnd().flush();
                jEvents.flush();
                buildCoursePopoverEvents(activityOption.getAlternateActivties(), jEvents, isTBD, invalidActivities);

                for (SecondaryActivityOptions secondaryActivityOptions : activityOption.getSecondaryOptions()) {
                    buildCoursePopoverEvents(secondaryActivityOptions.getActivityOptions(), jEvents, isTBD, invalidActivities);
                }
            }
        }
    }

    public CalendarUtil getCalendarUtil() {
        if (calendarUtil == null) {
            calendarUtil = KsapFrameworkServiceLocator.getCalendarUtil();
        }
        return calendarUtil;
    }

    public void setCalendarUtil(CalendarUtil calendarUtil) {
        this.calendarUtil = calendarUtil;
    }


}


