package org.kuali.student.myplan.schedulebuilder.controller;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleForm;
import org.kuali.student.myplan.schedulebuilder.util.ShoppingCartForm;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(value = "/sb")
public class ScheduleBuildController extends UifControllerBase {

    private static final Logger LOG = Logger
            .getLogger(ScheduleBuildController.class);

    private static String EVENT_CSS_PREFIX = "ks-schedule-Color";

    public static final String SB_FORM = "ScheduleBuild-FormView";

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private static CalendarUtil calendarUtil;

    private static CourseHelper courseHelper;


    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return (UifFormBase) getScheduleBuildStrategy().getInitialForm();
    }

    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form,
                              BindingResult result, HttpServletRequest request,
                              HttpServletResponse response) {
        super.start(form, result, request, response);

        ScheduleForm sbform = (ScheduleForm) form;
        sbform.reset();
        return getUIFModelAndView(form);
    }


    @RequestMapping(params = "methodToCall=build")
    public ModelAndView build(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ScheduleBuildForm sbform = (ScheduleBuildForm) form;
        sbform.buildSchedules();
        return getUIFModelAndView(form);
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

    private static void addEvents(Term term, ScheduleBuildEvent meeting,
                                  String description, ActivityOption ao, String cssClass,
                                  JsonArrayBuilder jevents, EventAggregateData aggregate, Map<String, List<ActivityOption>> scheduledCourseActivities) {
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

    private static JsonObjectBuilder createEvent(Date startDate,
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


    @RequestMapping(params = "methodToCall=save")
    public ModelAndView saveSchedule(@ModelAttribute("KualiForm") ScheduleBuildForm form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {

        PossibleScheduleOption sso = form.saveSchedule();

        JsonObjectBuilder json = Json.createObjectBuilder();

        if (sso != null) {
            json.add("success", true);
            json.add("id", sso.getId());
            json.add("uniqueId", sso.getUniqueId());
        } else {
            json.add("success", false);
        }
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");
        JsonWriter jwriter = Json.createWriter(response.getWriter());
        jwriter.writeObject(json.build());
        jwriter.close();

        return null;
    }

    @RequestMapping(params = "methodToCall=remove")
    public ModelAndView removeSchedule(@ModelAttribute("KualiForm") ScheduleBuildForm form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String removedId = form.removeSchedule();

        JsonObjectBuilder json = Json.createObjectBuilder();

        json.add("success", true);
        json.add("scheduleIdRemoved", removedId);

        response.setContentType("application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");
        JsonWriter jwriter = Json.createWriter(response.getWriter());
        jwriter.writeObject(json.build());
        jwriter.close();

        return null;
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

    public static CalendarUtil getCalendarUtil() {
        if (calendarUtil == null) {
            calendarUtil = KsapFrameworkServiceLocator.getCalendarUtil();
        }
        return calendarUtil;
    }

    public static void setCalendarUtil(CalendarUtil calendarUtil) {
        ScheduleBuildController.calendarUtil = calendarUtil;
    }

    public static CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = KsapFrameworkServiceLocator.getCourseHelper();
        }
        return courseHelper;
    }

    public static void setCourseHelper(CourseHelper courseHelper) {
        ScheduleBuildController.courseHelper = courseHelper;
    }
}
