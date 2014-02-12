package org.kuali.student.myplan.schedulebuilder.service;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.dto.PossibleScheduleOptionInfo;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.myplan.schedulebuilder.util.ShoppingCartForm;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hemanthg on 2/11/14.
 */
public class PossibleSchedulesLookupableHelperImpl extends MyPlanLookupableImpl {

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private ScheduleBuildForm scheduleBuildForm;

    private TermHelper termHelper;

    private static CalendarUtil calendarUtil;
    private static CourseHelper courseHelper;

    private static int DEFAULT_SCHEDULE_COUNT = 25;


    private static String EVENT_CSS_PREFIX = "ks-schedule-Color";

    @Override
    protected List<PossibleScheduleOption> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String termId = request.getParameter(PlanConstants.TERM_ID_KEY);
        Term term = getTermHelper().getTermByAtpId(termId);
        String requestedLearningPlanId = request.getParameter(PlanConstants.LEARNING_PLAN_KEY);
        List<CourseOption> courseOptions = getScheduleBuildStrategy().getCourseOptions(requestedLearningPlanId, termId);
        List<ReservedTime> reservedTimes = new ArrayList<ReservedTime>();
        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(term, courseOptions, reservedTimes);
        List<PossibleScheduleOption> possibleScheduleOptions = scheduleBuilder.getNext(DEFAULT_SCHEDULE_COUNT, Collections.<PossibleScheduleOption>emptySet());
        buildEvents(term, possibleScheduleOptions);
        return possibleScheduleOptions;
    }


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
                            + (cal.get(Calendar.MINUTE) > 0 ? 1 : 0));
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

    protected void buildEvents(Term term, List<PossibleScheduleOption> possibleScheduleOptions) {
        /**
         * This is used to adjust minDate and maxDate which is a week from min date and adjust min date to be monday if it is not.
         * Used in building a week worth of schedules instead of whole term.
         * */
        Date minDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date maxDate = getCalendarUtil().getDateAfterXdays(minDate, 6);
        Date displayDate = term.getEndDate();


        EventAggregateData aggregate = new EventAggregateData(minDate, maxDate, displayDate);
        JsonObjectBuilder json = Json.createObjectBuilder();
        List<PossibleScheduleOption> psos = possibleScheduleOptions;
        Map<String, PossibleScheduleOption> cartOptions = new HashMap<String, PossibleScheduleOption>(psos.size());

        int discardCount = 0;
        for (int i = 0; i < psos.size(); i++) {
            PossibleScheduleOption pso = psos.get(i);
            if (pso.isDiscarded()) {
                discardCount++;
                continue;
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

            String cssClass = EVENT_CSS_PREFIX + "--"
                    + ((i - discardCount) % 26);
            JsonObjectBuilder jpso = Json.createObjectBuilder();
            JsonArrayBuilder jevents = Json.createArrayBuilder();
            jpso.add("path", "possibleScheduleOptions[" + i + "]");
            jpso.add("uniqueId", pso.getUniqueId());
            jpso.add("selected", pso.isSelected());
            jpso.add("saved", false);
            jpso.add("htmlDescription", pso.getDescription().getFormatted());
            JsonArrayBuilder acss = Json.createArrayBuilder();
            acss.add(EVENT_CSS_PREFIX);
            acss.add(cssClass);
            jpso.add("eventClass", acss);
            for (ActivityOption ao : pso.getActivityOptions())
                if (!ao.isPrimary() || !ao.isEnrollmentGroup()) {
                    for (ClassMeetingTime meeting : ao.getClassMeetingTimes())
                        addEvents(
                                term,
                                meeting,
                                ao.getCourseOfferingCode()
                                        + (meeting.getLocation() == null ? ""
                                        : " (" + meeting.getLocation()
                                        + ") - " + pso.getDescription().getPlain()
                                ), ao, cssClass,
                                jevents, aggregate, scheduledCourseActivities);
                }
            jpso.add("weekends", aggregate.weekends);
            jpso.add("minTime", aggregate.minTime);
            jpso.add("maxTime", aggregate.maxTime);
            jpso.add("events", jevents);
            PossibleScheduleOptionInfo possibleScheduleOptionInfo = (PossibleScheduleOptionInfo) pso;
            ObjectMapper mapper = new ObjectMapper();
            try {
                possibleScheduleOptionInfo.setEvent(mapper.writeValueAsString(jpso.build()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    private void addEvents(Term term, ScheduleBuildEvent meeting,
                           String description, ActivityOption ao, String cssClass,
                           JsonArrayBuilder jevents, EventAggregateData aggregate, Map<String, List<ActivityOption>> scheduledCourseActivities) {
        /**
         * This is used to adjust minDate and maxDate which is a week from min date and adjust min date to be monday if it is not.
         * Used in building a week worth of schedules instead of whole term.
         * */
        Date termStartDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date termEndDate = getCalendarUtil().getDateAfterXdays(termStartDate, 6);
        Date meetingStartDate = getCalendarUtil().getNextMonday(meeting.getStartDate());
        Date meetingEndDate = getCalendarUtil().getDateAfterXdays(termStartDate, 6);


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
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.SUNDAY, durationSeconds, cssClass, ao,
                        description, meeting, scheduledCourseActivities));
            if (meeting.isMonday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.MONDAY, durationSeconds, cssClass, ao,
                        description, meeting, scheduledCourseActivities));
            if (meeting.isTuesday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.TUESDAY, durationSeconds, cssClass, ao,
                        description, meeting, scheduledCourseActivities));
            if (meeting.isWednesday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.WEDNESDAY, durationSeconds, cssClass, ao,
                        description, meeting, scheduledCourseActivities));
            if (meeting.isThursday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.THURSDAY, durationSeconds, cssClass, ao,
                        description, meeting, scheduledCourseActivities));
            if (meeting.isFriday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.FRIDAY, durationSeconds, cssClass, ao,
                        description, meeting, scheduledCourseActivities));
            if (meeting.isSaturday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.SATURDAY, durationSeconds, cssClass, ao,
                        description, meeting, scheduledCourseActivities));
            startDate = aggregate.addOneWeek(startDate);
        }
    }

    private JsonObjectBuilder createEvent(Date startDate,
                                          Date eventStart, Calendar cal, int dow, long durationSeconds,
                                          String cssClass, ActivityOption ao, String description,
                                          ScheduleBuildEvent sbEvent, Map<String, List<ActivityOption>> scheduledCourseActivities) {

        // Calculate the date for the event in seconds since the epoch
        cal.setTime(startDate);
        cal.add(Calendar.DATE, dow - cal.get(Calendar.DAY_OF_WEEK));
        long eventStartSeconds = cal.getTime().getTime() / 1000L;

        // Add the time for the event in seconds since midnight GMT
        cal.setTime(eventStart);
        eventStartSeconds += cal.getTime().getTime() / 1000L;

        // Adjust for time zone by subtracting midnight prior to the event.
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        eventStartSeconds -= cal.getTime().getTime() / 1000L;

        JsonObjectBuilder event = Json.createObjectBuilder();
        String uid = ao == null ? ((HasUniqueId) sbEvent).getUniqueId() : ao
                .getUniqueId();
        event.add("id", uid + "_" + eventStartSeconds);
        event.add("title", description);
        event.add("start", eventStartSeconds);
        JsonArrayBuilder acss = Json.createArrayBuilder();
        acss.add(EVENT_CSS_PREFIX);
        acss.add(cssClass);
        event.add("className", acss);
        if (durationSeconds == 0) {
            event.add("allDay", true);
        } else {
            event.add("allDay", false);
            event.add("end", eventStartSeconds + durationSeconds);
        }
        event.add("editable", false);

        if (ao != null) {
            Course course = getCourseHelper().getCourseInfo(ao.getCourseId());
            JsonObjectBuilder popoverEvent = Json.createObjectBuilder();
            popoverEvent.add("courseCd", course.getCode());
            popoverEvent.add("courseId", course.getId());
            popoverEvent.add("courseTitle", course.getCourseTitle().trim());

            List<ActivityOption> activityOptions = scheduledCourseActivities.get(ao.getCourseCd());

            JsonArrayBuilder activityArray = Json.createArrayBuilder();
            for (ActivityOption activityOption : activityOptions) {
                JsonObjectBuilder activity = Json.createObjectBuilder();
                activity.add("sectionCd", activityOption.getRegistrationCode());
                JsonArrayBuilder meetingArray = Json.createArrayBuilder();
                JsonObjectBuilder meeting = Json.createObjectBuilder();
                for (ClassMeetingTime meetingTime : activityOption.getClassMeetingTimes()) {
                    meeting.add("meetingTime", meetingTime.getDaysAndTimes());
                    meeting.add("location", meetingTime.getLocation());
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
                    meeting.add("building", building);
                    meeting.add("buildingUrl", buildingUrl);
                    meetingArray.add(meeting);
                }

                activity.add("meetings", meetingArray);
                activityArray.add(activity);
            }


            popoverEvent.add("activities", activityArray);
            popoverEvent.add("termId", ao.getTermId());
            event.add("popoverContent", popoverEvent);
        }

        DateFormat edf = new SimpleDateFormat("E MMM d");
        Element modal = DocumentHelper.createElement("div");
        modal.addAttribute("class", "ksap-sb-event-dialog");
        Element titleDiv = DocumentHelper.createElement("div");
        titleDiv.addAttribute("class", "ksap-sb-event-dialog-title");
        Element titleSpan = titleDiv.addElement("span");
        if (ao != null) {
            titleSpan.setText(ao.getCourseOfferingCode() + " "
                    + ao.getActivityName());
        } else {
            titleSpan.setText(description);
        }
        Element dl = modal.addElement("dl"), dt, dd;
        if (ao != null) {
            dt = dl.addElement("dt");
            dt.setText("Class Number");
            dd = dl.addElement("dd");
            dd.setText(ao.getRegistrationCode());
            dt = dl.addElement("dt");
            dt.setText("Available Seats");
            dd = dl.addElement("dd");
            dd.setText(ao.getOpenSeats() + " / " + ao.getTotalSeats());
            dt = dl.addElement("dt");
            dt.setText("Permission Required");
            dd = dl.addElement("dd");
            dd.setText(ao.isRequiresPermission() ? "Yes" : "No");
        }
        dt = dl.addElement("dt");
        dt.setText("Time");
        dd = dl.addElement("dd");
        dd.setText(sbEvent.getDaysAndTimes() + " from "
                + edf.format(sbEvent.getStartDate()) + " to "
                + edf.format(sbEvent.getUntilDate()));

        event.add("dialogHtml", modal.asXML());
        return event;
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

    public ScheduleBuildForm getScheduleBuildForm() {
        if (scheduleBuildForm == null) {
            scheduleBuildForm = UwMyplanServiceLocator.getInstance().getScheduleBuildForm();
        }
        return scheduleBuildForm;
    }

    public void setScheduleBuildForm(ScheduleBuildForm scheduleBuildForm) {
        this.scheduleBuildForm = scheduleBuildForm;
    }

    public TermHelper getTermHelper() {
        if (termHelper == null) {
            termHelper = KsapFrameworkServiceLocator.getTermHelper();
        }
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
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

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = KsapFrameworkServiceLocator.getCourseHelper();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }
}
