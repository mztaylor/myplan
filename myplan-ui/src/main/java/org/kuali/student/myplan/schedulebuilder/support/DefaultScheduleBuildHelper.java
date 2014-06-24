package org.kuali.student.myplan.schedulebuilder.support;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.joda.time.DateTimeComparator;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.exception.AuthorizationException;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.PossibleScheduleOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ReservedTimeInfo;
import org.kuali.student.myplan.schedulebuilder.dto.SecondaryActivityOptionsInfo;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.PossibleScheduleErrorsInfo;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildHelper;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilderConstants;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.stream.JsonGenerator;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

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

    private CourseHelper courseHelper;

    private PlanHelper planHelper;

    private UserSessionHelper userSessionHelper;

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private CourseOfferingService courseOfferingService;

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
     * Validates Saved activities against current versions.
     *
     * @param activityOptions
     * @param invalidOptions
     * @return ActivityOption list which are validated items and also populates all invalidActivityOptions course code wise.
     */
    public List<ActivityOption> validatedSavedActivities(List<ActivityOption> activityOptions, Map<String, Map<String, List<String>>> invalidOptions, List<ReservedTime> reservedTimes, List<String> plannedActivities, PossibleScheduleOption registered, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData, Map<String, Map<String, ActivityOption>> courseCodeToActivities) {
        List<ActivityOption> activityOptionList = new ArrayList<ActivityOption>();
        for (ActivityOption activityOption : activityOptions) {
            ActivityOptionInfo savedActivity = new ActivityOptionInfo(activityOption);
            List<ActivityOption> validatedAlternates = new ArrayList<ActivityOption>();
            if (!CollectionUtils.isEmpty(savedActivity.getSecondaryOptions())) {
                boolean isValid = true;
                for (SecondaryActivityOptions secondaryActivityOption : savedActivity.getSecondaryOptions()) {
                    boolean containsPlannedItems = activityOptionsContainsPlannedItems(secondaryActivityOption.getActivityOptions(), plannedActivities);
                    if (!CollectionUtils.isEmpty(secondaryActivityOption.getActivityOptions())) {
                        List<ActivityOption> validatedSecondaryActivities = validatedSavedActivities(secondaryActivityOption.getActivityOptions(), invalidOptions, reservedTimes, plannedActivities, registered, enrollmentData, courseCodeToActivities);
                        if (CollectionUtils.isEmpty(validatedSecondaryActivities)) {
                            isValid = false;
                            break;
                        } else {
                            ((SecondaryActivityOptionsInfo) secondaryActivityOption).setActivityOptions(validatedSecondaryActivities);
                            /*Chance that at least one secondary activity option is available without errors for going to registration*/
                            if (!CollectionUtils.isEmpty(invalidOptions) && invalidOptions.containsKey(activityOption.getCourseCd())) {
                                List<String> keySet = new ArrayList<String>(invalidOptions.get(activityOption.getCourseCd()).keySet());
                                for (String errorKey : keySet) {
                                    /*For all withdrawn, conflicts with reserved times, Time changes, and suspended reasons we need to add a No error error type
                                     since there is a viable secondary option to go with, we silently take off the ones which come under these error type category */
                                    if (!containsPlannedItems && (ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN.equals(errorKey) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED.equals(errorKey) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED.equals(errorKey) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED.equals(errorKey))) {
                                        invalidOptions.get(activityOption.getCourseCd()).put(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_ERROR, invalidOptions.get(activityOption.getCourseCd()).get(errorKey));
                                    }
                                    if (!containsPlannedItems) {
                                        invalidOptions.get(activityOption.getCourseCd()).remove(errorKey);
                                    }
                                }
                            }
                        }
                    }
                }
                /*NO secondaries available for this primary are available*/
                if (!isValid) {
                    if (invalidOptions == null) {
                        invalidOptions = new HashMap<String, Map<String, List<String>>>();
                    }
                    Map<String, List<String>> errorList = invalidOptions.get(savedActivity.getCourseCd());
                    if (errorList == null) {
                        errorList = new HashMap<String, List<String>>();
                        errorList.put(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES, new ArrayList<String>());
                    }
                    List<String> activityList = errorList.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES);
                    if (CollectionUtils.isEmpty(activityList)) {
                        activityList = new ArrayList<String>();
                    }
                    activityList.add(savedActivity.getActivityCode());
                    errorList.put(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES, activityList);
                    invalidOptions.put(savedActivity.getCourseCd(), errorList);
                    continue;
                }
            } else if (!CollectionUtils.isEmpty(savedActivity.getAlternateActivties())) {
                validatedAlternates = savedActivity.getAlternateActivties() == null ? new ArrayList<ActivityOption>() : validatedSavedActivities(savedActivity.getAlternateActivties(), invalidOptions, reservedTimes, plannedActivities, registered, enrollmentData, courseCodeToActivities);
            }

            //ActivityOption currentActivity = getScheduleBuildStrategy().getActivityOption(savedActivity.getTermId(), savedActivity.getCourseId(), savedActivity.getCourseCd(), savedActivity.getActivityCode(), enrollmentData);
            ActivityOption currentActivity = null;
            if (!CollectionUtils.isEmpty(courseCodeToActivities) && courseCodeToActivities.containsKey(savedActivity.getCourseCd()) && !CollectionUtils.isEmpty(courseCodeToActivities.get(savedActivity.getCourseCd())) && courseCodeToActivities.get(savedActivity.getCourseCd()).get(savedActivity.getActivityOfferingId()) != null) {
                currentActivity = courseCodeToActivities.get(savedActivity.getCourseCd()).get(savedActivity.getActivityOfferingId());
            }
            String reasonForChange = areEqual(savedActivity, currentActivity, reservedTimes, registered);
            if (StringUtils.isEmpty(reasonForChange)) {
                savedActivity.setAlternateActivities(validatedAlternates);
                activityOptionList.add(savedActivity);
            } else if (StringUtils.hasText(reasonForChange) && !CollectionUtils.isEmpty(validatedAlternates)) {
                ActivityOptionInfo alAo = (ActivityOptionInfo) validatedAlternates.get(0);
                validatedAlternates.remove(0);
                alAo.setAlternateActivities(validatedAlternates.size() > 0 ? validatedAlternates : new ArrayList<ActivityOption>());
                activityOptionList.add(alAo);
                if (plannedActivities.contains(savedActivity.getActivityOfferingId()) && (ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN.equals(reasonForChange) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED.equals(reasonForChange) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED.equals(reasonForChange) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED.equals(reasonForChange))) {
                    if (invalidOptions == null) {
                        invalidOptions = new HashMap<String, Map<String, List<String>>>();
                    }
                    Map<String, List<String>> errorList = invalidOptions.get(savedActivity.getCourseCd());
                    if (errorList == null) {
                        errorList = new HashMap<String, List<String>>();
                        errorList.put(reasonForChange, new ArrayList<String>());
                    }
                    List<String> activityList = errorList.get(reasonForChange);
                    if (CollectionUtils.isEmpty(activityList)) {
                        activityList = new ArrayList<String>();
                    }
                    activityList.add(savedActivity.getActivityCode());
                    errorList.put(reasonForChange, activityList);
                    invalidOptions.put(savedActivity.getCourseCd(), errorList);
                }
            } else if (StringUtils.hasText(reasonForChange) && (ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED.equals(reasonForChange) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR.equals(reasonForChange) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED.equals(reasonForChange))) {
                savedActivity.setAlternateActivities(validatedAlternates);
                activityOptionList.add(savedActivity);
                if (invalidOptions == null) {
                    invalidOptions = new HashMap<String, Map<String, List<String>>>();
                }
                Map<String, List<String>> errorList = invalidOptions.get(savedActivity.getCourseCd());
                if (errorList == null) {
                    errorList = new HashMap<String, List<String>>();
                    errorList.put(reasonForChange, new ArrayList<String>());
                }
                List<String> activityList = errorList.get(reasonForChange);
                if (CollectionUtils.isEmpty(activityList)) {
                    activityList = new ArrayList<String>();
                }
                activityList.add(savedActivity.getActivityCode());
                errorList.put(reasonForChange, activityList);
                invalidOptions.put(savedActivity.getCourseCd(), errorList);
            } else {
                if (invalidOptions == null) {
                    invalidOptions = new HashMap<String, Map<String, List<String>>>();
                }
                Map<String, List<String>> errorList = invalidOptions.get(savedActivity.getCourseCd());
                if (errorList == null) {
                    errorList = new HashMap<String, List<String>>();
                    errorList.put(reasonForChange, new ArrayList<String>());
                }
                List<String> activityList = errorList.get(reasonForChange);
                if (CollectionUtils.isEmpty(activityList)) {
                    activityList = new ArrayList<String>();
                }
                activityList.add(savedActivity.getActivityCode());
                errorList.put(reasonForChange, activityList);
                invalidOptions.put(savedActivity.getCourseCd(), errorList);

            }
        }

        return activityOptionList;

    }

    /**
     * recursive method used to check if a activity cd is present in any of the primary, secondary or alternate activity options
     *
     * @param activityOptions
     * @param activityIds
     * @return
     */
    private boolean activityOptionsContainsPlannedItems(List<ActivityOption> activityOptions, List<String> activityIds) {
        for (ActivityOption activityOption : activityOptions) {
            if (activityIds.contains(activityOption.getActivityOfferingId())) {
                return true;
            }
            if (activityOptionsContainsPlannedItems(activityOption.getAlternateActivties(), activityIds)) {
                return true;
            }

            for (SecondaryActivityOptions so : activityOption.getSecondaryOptions()) {
                if (activityOptionsContainsPlannedItems(so.getActivityOptions(), activityIds)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares the saved ActivityOption with withdrawnFlag, current ActivityOption, plannedActivities and ReservedTimes
     *
     * @param saved
     * @param current
     * @return (((currentStatusClosed) OR currentIsWithdrawn OR ((currentMeeting and savedMeeting times vary) OR (reservedTime and savedMeeting time conflict))) AND savedActivity is not in PlannedActivities) then false otherwise true.
     */
    private String areEqual(ActivityOption saved, ActivityOption current, List<ReservedTime> reservedTimes, PossibleScheduleOption registered) {
        if (current == null) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN;
        }
        long[][] savedClassMeetingTime = xlateClassMeetingTimeList2WeekBits(saved.getClassMeetingTimes());
        long[][] currentClassMeetingTime = xlateClassMeetingTimeList2WeekBits(current.getClassMeetingTimes());
        long[][] reservedTime = xlateClassMeetingTimeList2WeekBits(reservedTimes);
        if (!Arrays.deepEquals(savedClassMeetingTime, currentClassMeetingTime)) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED;
        } else if (checkForConflictsWeeks(savedClassMeetingTime, reservedTime)) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED;
        } else if (current.isWithdrawn()) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN;
        } else if (current.isSuspended()) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED;
        } else if (conflictsWithRegistered(registered, saved)) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED;
        } else if (current.isClosed()) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED;
        } else if (current.isEnrollmentRestriction()) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR;
        }
        return null;
    }


    /**
     * Checks to see if the activityOption conflicts with any of the registered activity class meeting times
     *
     * @param registered
     * @param ao
     * @return
     */
    private boolean conflictsWithRegistered(PossibleScheduleOption registered, ActivityOption ao) {
        long[][] aoClassMeetingTime = xlateClassMeetingTimeList2WeekBits(ao.getClassMeetingTimes());
        if (registered != null) {
            for (ActivityOption registeredAO : registered.getActivityOptions()) {
                long[][] registeredTime = xlateClassMeetingTimeList2WeekBits(registeredAO.getClassMeetingTimes());
                if (checkForConflictsWeeks(aoClassMeetingTime, registeredTime)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Updates enrollment data for given activityOptions
     *
     * @param activityOptions
     */
    @Override
    public void updateEnrollmentInfo(List<ActivityOption> activityOptions, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData) {
        List<ActivityOption> activityOptionList = populateEnrollInfo(activityOptions, enrollmentData);
        activityOptions = new ArrayList<ActivityOption>();
        activityOptions.addAll(activityOptionList);
    }

    /**
     * Gives the enrollment data for activityOptions given by courses level
     *
     * @param activityOptions
     */
    @Override
    public LinkedHashMap<String, LinkedHashMap<String, Object>> getEnrollmentDataForActivities(List<ActivityOption> activityOptions) {
        LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
        for (ActivityOption activityOption : activityOptions) {
            String termId = activityOption.getTermId();
            Course c = getCourseHelper().getCourseInfoByIdAndCd(activityOption.getCourseId(), activityOption.getCourseCd());
            String curriculum = c.getSubjectArea();
            String num = c.getCourseNumberSuffix();
            LinkedHashMap<String, LinkedHashMap<String, Object>> data = new LinkedHashMap<String, LinkedHashMap<String, Object>>();

            try {
                getCourseHelper().getAllSectionStatus(data, termId, curriculum, num);
            } catch (DocumentException e) {
                logger.error("Could not load enrollmentInformation for course : " + c.getCode() + " for term : " + termId, e);
            }
            if (!CollectionUtils.isEmpty(data)) {
                enrollmentData.putAll(data);
            }
        }
        return enrollmentData;
    }


    /**
     * Gives the enrollment data for possible schedule option activityOptions given by courses level
     * Makes sure that the calls made to the section status service is limited to per course in all activities in all possibles schedules
     *
     * @param possibleScheduleOptions
     * @return
     */
    public LinkedHashMap<String, LinkedHashMap<String, Object>> getEnrollmentDataForPossibleSchedules(List<PossibleScheduleOption> possibleScheduleOptions) {
        Map<String, LinkedHashMap<String, LinkedHashMap<String, Object>>> courseToEnrollments = new LinkedHashMap<String, LinkedHashMap<String, LinkedHashMap<String, Object>>>();
        LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
        for (PossibleScheduleOption possibleScheduleOption : possibleScheduleOptions) {
            for (ActivityOption activityOption : possibleScheduleOption.getActivityOptions()) {
                if (courseToEnrollments.containsKey(activityOption.getCourseCd())) {
                    continue;
                }
                String termId = activityOption.getTermId();
                Course c = getCourseHelper().getCourseInfoByIdAndCd(activityOption.getCourseId(), activityOption.getCourseCd());
                String curriculum = c.getSubjectArea();
                String num = c.getCourseNumberSuffix();
                LinkedHashMap<String, LinkedHashMap<String, Object>> data = new LinkedHashMap<String, LinkedHashMap<String, Object>>();

                try {
                    getCourseHelper().getAllSectionStatus(data, termId, curriculum, num);
                } catch (DocumentException e) {
                    logger.error("Could not load enrollmentInformation for course : " + c.getCode() + " for term : " + termId, e);
                }
                if (!CollectionUtils.isEmpty(data)) {
                    courseToEnrollments.put(activityOption.getCourseCd(), data);
                    enrollmentData.putAll(data);
                }

            }

        }
        return enrollmentData;
    }

    /**
     * validates the given learningPlanId if it belongs to the user or not.
     * if the learningPlanId provided is null then the it is populated with the learningPlanId of the student
     *
     * @param requestedLearningPlanId
     */
    public String validateOrPopulateLearningPlanId(String requestedLearningPlanId) {
        LearningPlan learningPlan = getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId());
        if (learningPlan != null) {
            if (StringUtils.isEmpty(requestedLearningPlanId)) {
                requestedLearningPlanId = learningPlan.getId();
            } else {
                /*validating to see if the user requesting learningPlan is same as the id provided*/
                if (!learningPlan.getId().equals(requestedLearningPlanId)) {
                    throw new AuthorizationException(learningPlan.getStudentId(), "Un authorized", null);
                }
            }
        } else {
            logger.error("User does not have learningPlan, user might be a new user. StudentId: " + getUserSessionHelper().getStudentId());
            return null;
        }
        return requestedLearningPlanId;
    }

    /**
     * recursive method goes through activity options, secondary activity options, and alternate activities to populate enrollment data.
     *
     * @param activityOptions
     * @param enrollmentData
     */
    private List<ActivityOption> populateEnrollInfo(List<ActivityOption> activityOptions, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData) {
        List<ActivityOption> activityOptionList = new ArrayList<ActivityOption>();
        for (ActivityOption activityOption : activityOptions) {
            ActivityOptionInfo activityOptionInfo = (ActivityOptionInfo) activityOption;
            try {
                ActivityOfferingDisplayInfo aodi = getCourseOfferingService().getActivityOfferingDisplay(activityOption.getActivityOfferingId(), PlanConstants.CONTEXT_INFO);
                getScheduleBuildStrategy().populateEnrollmentInfo(activityOptionInfo, aodi, enrollmentData);
                activityOptionInfo.setAlternateActivities(populateEnrollInfo(activityOption.getAlternateActivties(), enrollmentData));
                List<SecondaryActivityOptions> secondaryActivityOptions = new ArrayList<SecondaryActivityOptions>();
                for (SecondaryActivityOptions secondaryActivityOption : activityOption.getSecondaryOptions()) {
                    SecondaryActivityOptionsInfo secondaryActivityOptionsInfo = (SecondaryActivityOptionsInfo) secondaryActivityOption;
                    secondaryActivityOptionsInfo.setActivityOptions(populateEnrollInfo(secondaryActivityOption.getActivityOptions(), enrollmentData));
                    secondaryActivityOptions.add(secondaryActivityOptionsInfo);
                }
                activityOptionList.add(activityOptionInfo);
            } catch (Exception e) {
                logger.error("Could not load activity offering details for activityOffering Id: " + activityOption.getActivityOfferingId(), e);
            }
        }
        return activityOptionList;
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
            jEvents.writeStartObject("popoverContent").write("courseCd", ao.getCourseCd()).write("courseId", ao.getCourseId()).write("courseTitle", ao.getCourseTitle().trim()).write("courseCredit", ao.getCourseCredit()).write("registered", ao.isLockedIn()).write("termId", ao.getTermId());

            List<ActivityOption> activityOptions = scheduledCourseActivities.get(ao.getCourseCd());
            if (possibleScheduleErrors != null) {
                PossibleScheduleErrorsInfo possibleScheduleErrorsInfo = new PossibleScheduleErrorsInfo();
                possibleScheduleErrorsInfo.setInvalidOptions(possibleScheduleErrors.getInvalidOptions());
                possibleScheduleErrorsInfo.setErrorType(possibleScheduleErrors.getErrorType());
                validateForErrors(possibleScheduleErrorsInfo, ao.getCourseCd(), activityOptions, invalidatedActivities);
                if (StringUtils.hasText(possibleScheduleErrorsInfo.getErrorMessage()) && StringUtils.hasText(possibleScheduleErrorsInfo.getErrorType())) {
                    jEvents.write("errorType", possibleScheduleErrorsInfo.getErrorType()).write("errorMessage", possibleScheduleErrorsInfo.getErrorMessage());
                }
            }
            jEvents.writeStartArray("activities");
            buildCoursePopoverEvents(activityOptions, jEvents, isTBD, invalidatedActivities);
            jEvents.writeEnd().writeEnd().flush();
        }
        jEvents.writeEnd().flush();
    }

    /**
     * Validates the error messages for the activity options of saved schedule
     *
     * @param possibleScheduleErrors
     * @param courseCd
     * @param activityOptions
     * @param invalidatedActivities
     */
    public void validateForErrors(PossibleScheduleErrorsInfo possibleScheduleErrors, String courseCd, List<ActivityOption> activityOptions, List<String> invalidatedActivities) {
        List<String> activitiesToExclude = new ArrayList<String>();
        if (possibleScheduleErrors != null && StringUtils.hasText(possibleScheduleErrors.getErrorType()) && !CollectionUtils.isEmpty(possibleScheduleErrors.getInvalidOptions()) && !CollectionUtils.isEmpty(possibleScheduleErrors.getInvalidOptions().get(courseCd))) {
            Map<String, List<String>> invalidActivities = possibleScheduleErrors.getInvalidOptions().get(courseCd);

            List<String> withdrawnErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN);
            List<String> closedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED);
            List<String> suspendedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED);
            List<String> enrollErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR);
            List<String> timeChangedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED);
            List<String> conflictedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED);
            List<String> registeredConflictedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED);
            List<String> unAvailableSecondariesErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES);
            List<String> noErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_ERROR) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_ERROR);

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
            activitiesToExclude.addAll(registeredConflictedErrorActivities);
            activitiesToExclude.addAll(unAvailableSecondariesErrorActivities);
            invalidatedActivities.addAll(unAvailableSecondariesErrorActivities);
            activitiesToExclude.addAll(noErrorActivities);
            invalidatedActivities.addAll(noErrorActivities);
            Collections.sort(activitiesToExclude);
            Collections.sort(invalidatedActivities);
            if (activityOptionsContains(activityOptions, activitiesToExclude)) {
                if (invalidatedActivities.size() != activitiesToExclude.size()) {
                    StringBuffer errorMessage = new StringBuffer();
                    if (!CollectionUtils.isEmpty(withdrawnErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                        if (withdrawnErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_WITHDRAWN_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(withdrawnErrorActivities.subList(0, withdrawnErrorActivities.size() - 1), ", "), withdrawnErrorActivities.get(withdrawnErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_WITHDRAWN_SINGLE), courseCd, withdrawnErrorActivities.get(0))).append("</p>");
                        }

                    }
                    if (!CollectionUtils.isEmpty(closedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                        if (closedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_CLOSED_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(closedErrorActivities.subList(0, closedErrorActivities.size() - 1), ", "), closedErrorActivities.get(closedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_CLOSED_SINGLE), courseCd, closedErrorActivities.get(0))).append("</p>");
                        }

                    }
                    if (!CollectionUtils.isEmpty(suspendedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                        if (suspendedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_SUSPENDED_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(suspendedErrorActivities.subList(0, suspendedErrorActivities.size() - 1), ", "), suspendedErrorActivities.get(suspendedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_SUSPENDED_SINGLE), courseCd, suspendedErrorActivities.get(0))).append("</p>");
                        }

                    }
                    if (!CollectionUtils.isEmpty(enrollErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                        if (enrollErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_ENROLL_RESTR_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(enrollErrorActivities.subList(0, enrollErrorActivities.size() - 1), ", "), enrollErrorActivities.get(enrollErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_ENROLL_RESTR_SINGLE), courseCd, enrollErrorActivities.get(0))).append("</p>");
                        }

                    }
                    if (!CollectionUtils.isEmpty(timeChangedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                        if (timeChangedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_TIME_CHANGED_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(timeChangedErrorActivities.subList(0, timeChangedErrorActivities.size() - 1), ", "), timeChangedErrorActivities.get(timeChangedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_TIME_CHANGED_SINGLE), courseCd, timeChangedErrorActivities.get(0))).append("</p>");
                        }
                    }
                    if (!CollectionUtils.isEmpty(conflictedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                        if (conflictedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_RESERVED_CONFLICT_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(conflictedErrorActivities.subList(0, conflictedErrorActivities.size() - 1), ", "), conflictedErrorActivities.get(conflictedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_RESERVED_CONFLICT_SINGLE), courseCd, conflictedErrorActivities.get(0))).append("</p>");
                        }
                    }
                    if (!CollectionUtils.isEmpty(registeredConflictedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                        if (registeredConflictedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_REGISTERED_CONFLICT_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(registeredConflictedErrorActivities.subList(0, registeredConflictedErrorActivities.size() - 1), ", "), registeredConflictedErrorActivities.get(registeredConflictedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_REGISTERED_CONFLICT_SINGLE), courseCd, registeredConflictedErrorActivities.get(0))).append("</p>");
                        }
                    }
                    if (!CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                        unAvailableSecondariesErrorActivities.addAll(withdrawnErrorActivities);
                        unAvailableSecondariesErrorActivities.addAll(timeChangedErrorActivities);
                        unAvailableSecondariesErrorActivities.addAll(conflictedErrorActivities);
                        if (unAvailableSecondariesErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_NO_SECONDARIES_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(unAvailableSecondariesErrorActivities.subList(0, unAvailableSecondariesErrorActivities.size() - 1), ", "), unAvailableSecondariesErrorActivities.get(unAvailableSecondariesErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_NO_SECONDARIES_SINGLE), courseCd, unAvailableSecondariesErrorActivities.get(0))).append("</p>");
                        }
                    }
                    if (StringUtils.hasText(errorMessage)) {
                        possibleScheduleErrors.setErrorType(ScheduleBuilderConstants.PINNED_SCHEDULES_PASSIVE_ERROR);
                        possibleScheduleErrors.setErrorMessage(errorMessage.toString());
                    }
                } else if (invalidatedActivities.size() == activitiesToExclude.size()) {
                    StringBuffer errorMessage = new StringBuffer();
                    errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.INVALID_PINNED_SCHEDULE))).append("</p>");
                    if (StringUtils.hasText(errorMessage)) {
                        possibleScheduleErrors.setErrorType(ScheduleBuilderConstants.PINNED_SCHEDULES_MODAL_ERROR);
                        possibleScheduleErrors.setErrorMessage(errorMessage.toString());
                    }
                }
            }
        }
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
                jEvents.writeStartObject().write("sectionCd", activityOption.getActivityCode()).write("primary", activityOption.isPrimary()).write("activityId", activityOption.getActivityOfferingId()).write("registrationCode", activityOption.getRegistrationCode()).write("instituteCd", instituteCd).write("enrollRestriction", activityOption.isEnrollmentRestriction()).write("enrollStatus", String.format("%s/%s", activityOption.getFilledSeats(), activityOption.getTotalSeats())).write("enrollState", activityOption.getEnrollStatus() != null ? activityOption.getEnrollStatus() : "").writeStartArray("meetings");
                if (!isTBD) {
                    for (ClassMeetingTime meetingTime : activityOption.getClassMeetingTimes()) {
                        jEvents.writeStartObject().write("meetingDay", org.apache.commons.lang.StringUtils.join(meetingTime.getDays(), "")).write("meetingTime", meetingTime.getTimes() != null ? meetingTime.getTimes() : "").write("location", meetingTime.getLocation() != null ? meetingTime.getLocation() : "");
                        String campus = meetingTime.getCampus();
                        String building = "";
                        String buildingUrl = "";
                        if (meetingTime.getBuilding() != null) {
                            if (!"NOC".equals(meetingTime.getBuilding()) && !meetingTime.getBuilding().startsWith("*") && "seattle".equalsIgnoreCase(campus)) {
                                building = meetingTime.getBuilding();
                                buildingUrl = PlanConstants.BUILDING_URL + building.toLowerCase();
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
            } else if (!CollectionUtils.isEmpty(activityOption.getAlternateActivties())) {
                buildCoursePopoverEvents(activityOption.getAlternateActivties(), jEvents, isTBD, invalidActivities);
            }
        }
    }

    /**
     * recursive method used to extract the meeting time bits from a list of ActivityOption.
     * will traverse through primaries, secondaries, etc and sum all the bits, return them 2-D array of long.
     * does not process AlternateActivities list
     *
     * @param aoList list of ActivityOption
     * @return 2 D array (7 days x 5 longs per day) with the CMT bits
     */
    public long[][] extractClassMeetingTimeWeekBitsFromAOList(List<ActivityOption> aoList) {
        long[][] unionWeekBits = new long[7][5];

        for (ActivityOption activityOption : aoList) {
            long[][] cmtListWeekBits = xlateClassMeetingTimeList2WeekBits(activityOption.getClassMeetingTimes());
            unionWeeks(unionWeekBits, cmtListWeekBits);
            for (SecondaryActivityOptions secondaryActivityOptions : activityOption.getSecondaryOptions()) {
                cmtListWeekBits = extractClassMeetingTimeWeekBitsFromAOList(secondaryActivityOptions.getActivityOptions());
                unionWeeks(unionWeekBits, cmtListWeekBits);
            }
        }
        return unionWeekBits;
    }

    /**
     * extract the meeting time bits from a list of CourseOptions.
     * will traverse through list and the ActivityOptions list in each CO and sum all the bits,
     * return them as a 2-D array of long.
     * does not process AlternateActivities list.
     *
     * @param coList list of CourseOption
     * @return 2 D array (7 days x 5 longs per day) with the CMT bits
     */
    public long[][] extractClassMeetingTimeWeekBitsFromCourseOptionList(List<CourseOption> coList) {
        long[][] unionCoBits = new long[7][5];

        for (CourseOption co : coList) {
            long[][] aoListWeekBits = extractClassMeetingTimeWeekBitsFromAOList(co.getActivityOptions());
            unionWeeks(unionCoBits, aoListWeekBits);
        }

        return unionCoBits;
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

    public ScheduleBuildStrategy getScheduleBuildStrategy() {
        if (scheduleBuildStrategy == null) {
            scheduleBuildStrategy = UwMyplanServiceLocator.getInstance().getScheduleBuildStrategy();
        }
        return scheduleBuildStrategy;
    }

    public void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        this.scheduleBuildStrategy = scheduleBuildStrategy;
    }

    public CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = KsapFrameworkServiceLocator.getCourseHelper();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = UwMyplanServiceLocator.getInstance().getPlanHelper();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}


