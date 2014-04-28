package org.kuali.student.myplan.schedulebuilder.support;

import org.apache.log4j.Logger;
import org.dom4j.DocumentException;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.schedulebuilder.dto.*;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.*;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.TimeOfDayInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.infc.Attribute;
import org.kuali.student.r2.common.util.constants.LuiServiceConstants;
import org.kuali.student.r2.core.room.infc.Building;
import org.kuali.student.r2.core.room.infc.Room;
import org.kuali.student.r2.core.scheduling.dto.ScheduleDisplayInfo;
import org.kuali.student.r2.core.scheduling.infc.ScheduleComponentDisplay;
import org.kuali.student.r2.core.scheduling.infc.TimeSlot;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

public class DefaultScheduleBuildStrategy implements ScheduleBuildStrategy,
        Serializable {

    private transient AcademicPlanService academicPlanService;

    private transient CourseHelper courseHelper;
    private transient PlanHelper planHelper;
    private UserSessionHelper userSessionHelper;
    private transient TermHelper termHelper;
    private transient CalendarUtil calendarUtil;
    private transient AcademicRecordService academicRecordService;
    private transient ShoppingCartStrategy shoppingCartStrategy;
    private CourseDetailsInquiryHelperImpl courseDetailsHelper;
    private static final long serialVersionUID = -3524818039744728212L;

    private static final String SCHEDULE_BUILD_ATTR = ScheduleBuildStrategy.class
            .getName() + ".scheduleBuild";

    private static final Logger LOG = Logger
            .getLogger(DefaultScheduleBuildStrategy.class);

    /**
     * Simple XML wrapper for storing a list of reserved times as a dynamic
     * attribute on a learning plan.
     */
    @XmlRootElement(name = "schedule-build", namespace = "http://sb.ap.student.kuali.org/")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class ScheduleBuildAttribute {
        private int savedScheduleSequence;
        private List<ReservedTimeInfo> reservedTime;
        private List<PossibleScheduleOptionInfo> schedule;
    }

    /**
     * Retrieve schedule build saved information as a dynamic attribute on the
     * requested learning plan.
     *
     * @param requestedLearningPlanId The requested learning plan ID.
     * @return The schedule build saved info, stored as a dynamic attribute on
     * the requested learning plan.
     * @throws PermissionDeniedException If the current user does not have access to the requested
     *                                   learning plan.
     * @see #getLearningPlan(String)
     */
    private ScheduleBuildAttribute getScheduleBuildAttribute(String requestedLearningPlanId) throws PermissionDeniedException {
        LearningPlanInfo learningPlanInfo = (LearningPlanInfo) getLearningPlan(requestedLearningPlanId);
        List<AttributeInfo> attributes = learningPlanInfo.getAttributes();
        Iterator<AttributeInfo> attributeIterator = attributes.iterator();
        AttributeInfo scheduleBuildAttribute = null;
        while (attributeIterator.hasNext() && scheduleBuildAttribute == null) {
            AttributeInfo attribute = attributeIterator.next();
            if (attribute.getKey().equals(SCHEDULE_BUILD_ATTR)) {
                scheduleBuildAttribute = attribute;
            }
        }
        ScheduleBuildAttribute rv;
        if (scheduleBuildAttribute != null) {
            @SuppressWarnings("deprecation")
            ScheduleBuildAttribute schduleBuildInfo = XmlMarshalUtil.unmarshal(scheduleBuildAttribute.getValue(), ScheduleBuildAttribute.class, ReservedTimeInfo.class, PossibleScheduleOptionInfo.class);
            rv = schduleBuildInfo;
        } else {
            rv = new ScheduleBuildAttribute();
        }

        if (rv.reservedTime == null) {
            rv.reservedTime = new ArrayList<ReservedTimeInfo>(0);
        }

        if (rv.schedule == null) {
            rv.schedule = new ArrayList<PossibleScheduleOptionInfo>(0);
        }
        return rv;
    }

    /**
     * Update or create a dynamic attribute on the requested learning plan to
     * represent stored information related to schedule build.
     *
     * @param requestedLearningPlanId The requested learning plan ID.
     * @return The saved data, to be stored as a dynamic attribute on the
     * requested learning plan.
     * @throws PermissionDeniedException If the current user does not have access to the requested
     *                                   learning plan.
     * @see #getLearningPlan(String)
     */
    private void updateScheduleBuildAttribute(String requestedLearningPlanId,
                                              ScheduleBuildAttribute scheduleBuildInfo)
            throws PermissionDeniedException {
        LearningPlanInfo learningPlanInfo = (LearningPlanInfo) getLearningPlan(requestedLearningPlanId);

        @SuppressWarnings("deprecation")
        AttributeInfo newScheduleBuildAttribute = new AttributeInfo(SCHEDULE_BUILD_ATTR, XmlMarshalUtil.marshal(scheduleBuildInfo, ReservedTimeInfo.class, PossibleScheduleOptionInfo.class));

        List<AttributeInfo> attributes = new ArrayList<AttributeInfo>(learningPlanInfo.getAttributes());
        ListIterator<AttributeInfo> attributeListIterator = attributes.listIterator();

        boolean found = false;
        while (!found && attributeListIterator.hasNext()) {
            AttributeInfo attributeInfo = attributeListIterator.next();
            if (attributeInfo.getKey()
                    .equals(SCHEDULE_BUILD_ATTR)) {
                newScheduleBuildAttribute.setId(attributeInfo.getId());
                attributeListIterator.set(newScheduleBuildAttribute);
                found = true;
            }
        }

        if (!found) {
            attributes.add(newScheduleBuildAttribute);
        }

        learningPlanInfo.setAttributes(attributes);
        try {

            getAcademicPlanService().updateLearningPlan(learningPlanInfo.getId(), learningPlanInfo, PlanConstants.CONTEXT_INFO);

        } catch (DataValidationErrorException e) {
            throw new IllegalArgumentException(
                    "Error saving reserved time attributes in learning plan", e);
        } catch (InvalidParameterException e) {
            throw new IllegalArgumentException(
                    "Error saving reserved time attributes in learning plan", e);
        } catch (MissingParameterException e) {
            throw new IllegalArgumentException(
                    "Error saving reserved time attributes in learning plan", e);
        } catch (DoesNotExistException e) {
            throw new IllegalArgumentException(
                    "Error saving reserved time attributes in learning plan", e);
        } catch (OperationFailedException e) {
            throw new IllegalStateException(
                    "Error saving reserved time attributes in learning plan", e);
        }
    }

    @Override
    public ScheduleBuildForm getInitialForm() {
        return new DefaultScheduleBuildForm();
    }

    protected ClassMeetingTime adaptClassMeeting(TimeSlot timeSlot,
                                                 ScheduleComponentDisplay scdi, Calendar tcal, Calendar sdcal,
                                                 Calendar edcal, DateFormat tdf, DateFormat ddf, String instructor,
                                                 Date sessionStartDate, Date sessionEndDate) {
        ClassMeetingTimeInfo meeting = new ClassMeetingTimeInfo();
        sdcal.setTime(sessionStartDate);
        edcal.setTime(sessionEndDate);
        meeting.setAllDay(true);

        String location = null;
        String building = null;
        String campus = null;
        Room roomInfo = scdi.getRoom();
        Building buildingInfo = scdi.getBuilding();
        if (roomInfo != null) {
            //location = roomInfo.getDescr().getPlain();
            location = roomInfo.getRoomCode();
        }

        if (buildingInfo != null) {
            building = buildingInfo.getBuildingCode();
            campus = buildingInfo.getCampusKey();
        }

        StringBuilder daysAndTimes = new StringBuilder();
        List<String> days = new ArrayList<String>();
        Set<Integer> weekdays = new java.util.TreeSet<Integer>();
        if (timeSlot.getWeekdays() != null)
            weekdays.addAll(timeSlot.getWeekdays());

        for (int weekday : weekdays)
            switch (weekday) {
                case Calendar.MONDAY:
                    days.add(getCalendarUtil().getShortName(Calendar.MONDAY));
                    meeting.setMonday(true);
                    break;
                case Calendar.TUESDAY:
                    days.add(getCalendarUtil().getShortName(Calendar.TUESDAY));
                    meeting.setTuesday(true);
                    break;
                case Calendar.WEDNESDAY:
                    days.add(getCalendarUtil().getShortName(Calendar.WEDNESDAY));
                    meeting.setWednesday(true);
                    break;
                case Calendar.THURSDAY:
                    days.add(getCalendarUtil().getShortName(Calendar.THURSDAY));
                    meeting.setThursday(true);
                    break;
                case Calendar.FRIDAY:
                    days.add(getCalendarUtil().getShortName(Calendar.FRIDAY));
                    meeting.setFriday(true);
                    break;
                case Calendar.SATURDAY:
                    days.add(getCalendarUtil().getShortName(Calendar.SATURDAY));
                    meeting.setSaturday(true);
                    break;
                case Calendar.SUNDAY:
                    days.add(getCalendarUtil().getShortName(Calendar.SUNDAY));
                    meeting.setSunday(true);
                    break;
                default:
                    /*throw new IllegalArgumentException("Unexpected day code "
                            + weekday);*/
                    //skip
            }

        TimeOfDayInfo startInfo = timeSlot.getStartTime();
        TimeOfDayInfo endInfo = timeSlot.getEndTime();
        String times = "";
        if (startInfo != null && endInfo != null) {
            meeting.setAllDay(false);
            daysAndTimes.append(org.apache.commons.lang.StringUtils.join(days, ""));
            if (daysAndTimes.length() > 0)
                daysAndTimes.append(" ");
            Date startTime = new Date(startInfo.getMilliSeconds());
            Date endTime = new Date(endInfo.getMilliSeconds());
            times = String.format("%s - %s", tdf.format(startTime), tdf.format(endTime));
            daysAndTimes.append(times);
            tcal.setTime(startTime);
            sdcal.set(Calendar.HOUR_OF_DAY, tcal.get(Calendar.HOUR_OF_DAY));
            sdcal.set(Calendar.MINUTE, tcal.get(Calendar.MINUTE));
            sdcal.set(Calendar.SECOND, tcal.get(Calendar.SECOND));
            sdcal.set(Calendar.MILLISECOND, tcal.get(Calendar.MILLISECOND));
            meeting.setStartDate(sdcal.getTime());
            tcal.setTime(endTime);
            edcal.set(Calendar.HOUR_OF_DAY, tcal.get(Calendar.HOUR_OF_DAY));
            edcal.set(Calendar.MINUTE, tcal.get(Calendar.MINUTE));
            edcal.set(Calendar.SECOND, tcal.get(Calendar.SECOND));
            edcal.set(Calendar.MILLISECOND, tcal.get(Calendar.MILLISECOND));
            meeting.setUntilDate(edcal.getTime());
        } else {
            edcal.set(Calendar.HOUR_OF_DAY, 0);
            edcal.set(Calendar.MINUTE, 0);
            edcal.set(Calendar.SECOND, 0);
            edcal.set(Calendar.MILLISECOND, 0);
        }
        meeting.setLocation(location);
        meeting.setBuilding(building);
        meeting.setCampus(campus);
        meeting.setArranged(StringUtils.hasLength(daysAndTimes));
        meeting.setDaysAndTimes(daysAndTimes.toString());
        meeting.setDays(days);
        meeting.setTimes(times);
        meeting.setInstructorName(instructor);
        meeting.setStartDate(sdcal.getTime());
        meeting.setUntilDate(edcal.getTime());
        meeting.setDescription(ddf.format(meeting.getStartDate()) + " - "
                + ddf.format(meeting.getUntilDate()));
        return meeting;
    }

    protected ActivityOptionInfo getActivityOption(Term term, ActivityOfferingDisplayInfo aodi,
                                                   int courseIndex, String courseId, String courseCd, String courseTitle, String campusCode, String courseCredit, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData, Map<String, String> plannedActivities, StringBuilder msg,
                                                   DateFormat tdf, DateFormat udf, DateFormat ddf, Calendar sdcal, Calendar edcal,
                                                   Calendar tcal) {

        ActivityOptionInfo activityOption = new ActivityOptionInfo();
        activityOption.setCourseIndex(courseIndex);
        activityOption.setUniqueId(UUID.randomUUID().toString());
        activityOption.setCourseId(courseId);
        activityOption.setCourseCd(courseCd);
        activityOption.setCourseCredit(courseCredit);
        activityOption.setCourseTitle(courseTitle);
        activityOption.setPlanItemId(plannedActivities != null ? plannedActivities.get(aodi.getId()) : null);
        activityOption.setTermId(term.getId());
        activityOption.setActivityOfferingId(aodi.getId());
        activityOption.setActivityTypeDescription(aodi.getTypeName());
        activityOption.setCourseOfferingCode((campusCode == null ? "" : campusCode + " ") + aodi.getCourseOfferingCode());
        activityOption.setActivityName(aodi.getName());
        activityOption.setActivityCode(aodi.getActivityOfferingCode());
        populateEnrollmentInfo(activityOption, aodi, enrollmentData);
        if (msg != null)
            msg.append("\nActivity ")
                    .append(activityOption.getUniqueId()).append(": ")
                    .append(activityOption.getCourseOfferingCode())
                    .append(" ")
                    .append(activityOption.getActivityCode());

        boolean enrollmentGroup = false;
        String instructor = aodi.getInstructorName();
        String primaryOfferingId = null;

        String sessionDescr = term.getName();
        Date sessionStartDate = term.getStartDate();
        Date sessionEndDate = term.getEndDate();
        BigDecimal minCredits = BigDecimal.ZERO;
        BigDecimal maxCredits = BigDecimal.ZERO;
        for (AttributeInfo attrib : aodi.getAttributes()) {
            String key = attrib.getKey();
            String value = attrib.getValue();
            if ("PrimaryActivityOfferingId".equalsIgnoreCase(key)) {
                primaryOfferingId = value;
                activityOption.setPrimary(aodi.getId().equals(
                        primaryOfferingId));
                activityOption.setParentActivityId(primaryOfferingId);
            }
            if ("PermissionRequired".equalsIgnoreCase(key)) {
                activityOption.setRequiresPermission("true"
                        .equals(value));
            }
            if ("BlockEnrollment".equalsIgnoreCase(key)) {
                enrollmentGroup = "true".equals(value);
            }
            if ("Closed".equalsIgnoreCase(key)) {
                activityOption.setClosed("true".equals(value));
            }
            if ("enrollOpen".equalsIgnoreCase(key)) {
                activityOption.setOpenSeats(Integer.parseInt(value));
            }
            if ("SessionDescr".equalsIgnoreCase(key)) {
                sessionDescr = value;
            }
            if ("SessionStartDate".equalsIgnoreCase(key)) {
                try {
                    sessionStartDate = udf.parse(value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(
                            "Invalid session start date "
                                    + sessionStartDate
                    );
                }
            }
            if ("SessionEndDate".equalsIgnoreCase(key)) {
                try {
                    sessionEndDate = udf.parse(value);
                } catch (ParseException e) {
                    throw new IllegalArgumentException(
                            "Invalid session start date "
                                    + sessionEndDate
                    );
                }
            }

            if (CourseSearchConstants.SLN.equalsIgnoreCase(key)) {
                activityOption.setRegistrationCode(value);
            }

            if ("CourseCode".equalsIgnoreCase(key)) {
                activityOption.setCourseOfferingCode((campusCode == null ? ""
                        : campusCode + " ") + value);
            }

            // TODO: Add getResultValuesGroup() to
            // ActivityOfferingDisplayInfo and use it instead.
            if ("minUnits".equalsIgnoreCase(key)) {
                minCredits = new BigDecimal(value);
            }
            if ("maxUnits".equalsIgnoreCase(key)) {
                maxCredits = new BigDecimal(value);
            }

            if ("CourseHasVariableContent".equalsIgnoreCase(key)) {
                activityOption.setCourseHasVariableContent("true".equals(value));
            }

            if ("SameVariableContentAs".equalsIgnoreCase(key)) {
                List<String> sectionIdsList = new LinkedList<String>();
                String[] sectionIds = value.split(",");
                for (String sectionId : sectionIds) {
                    sectionIdsList.add(sectionId);
                }
                activityOption.setSameVariableContentAs(sectionIdsList);
            }
        }

        sessionDescr += " " + ddf.format(sessionStartDate) + " - "
                + ddf.format(sessionEndDate);
        activityOption.setAcademicSessionDescr(sessionDescr);
        activityOption.setMinCredits(minCredits);
        activityOption.setMaxCredits(maxCredits);

        List<ClassMeetingTime> meetingTimes = new LinkedList<ClassMeetingTime>();
        ScheduleDisplayInfo sdi = aodi.getScheduleDisplay();
        for (ScheduleComponentDisplay scdi : sdi
                .getScheduleComponentDisplays())
            for (TimeSlot timeSlot : scdi.getTimeSlots())
                meetingTimes.add(adaptClassMeeting(timeSlot, scdi,
                        tcal, sdcal, edcal, tdf, ddf, instructor,
                        sessionStartDate, sessionEndDate));
        activityOption.setClassMeetingTimes(meetingTimes);
        activityOption.setEnrollmentGroup(enrollmentGroup);

        return activityOption;
    }

    protected void populateEnrollmentInfo(ActivityOptionInfo activityOption, ActivityOfferingDisplayInfo aodi, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData) {
        activityOption.setClosed(!LuiServiceConstants.LUI_AO_STATE_OFFERED_KEY.equals(aodi.getStateKey()));
        activityOption.setTotalSeats(aodi.getMaximumEnrollment() != null ? aodi.getMaximumEnrollment() : 0);
    }

    @Override
    public ActivityOption getActivityOption(String termId, String courseId, String courseCd, String regCode) {
        if (regCode == null)
            return null;

        CourseHelper courseHelper = getCourseHelper();
        Course course = courseHelper.getCourseInfo(courseId);
        if (course == null)
            return null;

        String campusCode = getCampusCode(course);

        Term term = getTermHelper().getTermByAtpId(termId);
        String curric = course.getSubjectArea();
        String num = course.getCourseNumberSuffix();

        LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData = new LinkedHashMap<String, LinkedHashMap<String, Object>>();

        try {
            getCourseHelper().getAllSectionStatus(enrollmentData, termId, curric, num);
        } catch (DocumentException e) {
            LOG.error("Could not load enrollmentInformation for course : " + course.getCode() + " for term : " + termId, e);
        }

        if (term == null)
            return null;

        Map<String, String> plannedActivities = new LinkedHashMap<String, String>();
        try {
            plannedActivities = getPlanHelper().getPlanItemIdAndRefObjIdByRefObjType(getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId()).getId(), PlanConstants.SECTION_TYPE, termId);
        } catch (Exception e) {
            LOG.error("Could not load planned Activities for course : " + course.getCode() + " for term : " + termId, e);
        }

        for (ActivityOfferingDisplayInfo aodi : getCourseHelper().getActivityOfferingDisplaysByCourseAndTerm(courseId, courseCd, termId))
            if (regCode.equals(aodi.getActivityOfferingCode())) {
                DateFormat tdf = new SimpleDateFormat("h:mm a");
                DateFormat udf = new SimpleDateFormat("MM/dd/yyyy");
                DateFormat ddf = new SimpleDateFormat("M/d");
                Calendar sdcal = Calendar.getInstance();
                Calendar edcal = Calendar.getInstance();
                Calendar tcal = Calendar.getInstance();
                return getActivityOption(term, aodi, 0, courseId, course.getCode(), course.getCourseTitle(), campusCode, CreditsFormatter.formatCredits((CourseInfo) course), enrollmentData, plannedActivities, null, tdf, udf, ddf,
                        sdcal, edcal, tcal);
            }

        return null;
    }

    @Override
    public List<CourseOption> getCourseOptions(List<String> courseIds, Map<String, String> courseIdsTOCourseCds, String termId) {
        Term term = getTermHelper().getTermByAtpId(termId);
        CourseHelper courseHelper = getCourseHelper();
        ShoppingCartStrategy cartStrategy = getShoppingCartStrategy();
        DateFormat tdf = new SimpleDateFormat("h:mm a");
        DateFormat udf = new SimpleDateFormat("MM/dd/yyyy");
        DateFormat ddf = new SimpleDateFormat("M/d");
        Calendar sdcal = Calendar.getInstance();
        Calendar edcal = Calendar.getInstance();
        Calendar tcal = Calendar.getInstance();
        List<CourseOption> rv = new LinkedList<CourseOption>();
        courseHelper.frontLoad(courseIds, termId);
        int courseIndex = -1;
        for (String courseId : courseIds) {
            courseIndex++;
            Course c = courseHelper.getCourseInfoByIdTermAndCd(courseId, courseIdsTOCourseCds.get(courseId), AtpHelper.getCurrentAtpId());
            if (c == null)
                continue;
            CourseOptionInfo courseOption = new CourseOptionInfo();
            courseOption.setSelected(true);
            courseOption.setUniqueId(UUID.randomUUID().toString());
            courseOption.setCourseId(c.getId());
            String credits = CreditsFormatter.formatCredits((CourseInfo) c);
            courseOption.setCredits(credits);

            StringBuilder code = new StringBuilder();
            String campusCode = getCampusCode(c);
            if (!cartStrategy.isCartAvailable(termId, campusCode))
                continue;

            code.append(c.getCode());
            courseOption.setCourseCode(code.toString());

            courseOption.setCourseTitle(c.getCourseTitle());
            List<ActivityOption> primaryActivities = new LinkedList<ActivityOption>();
            Map<String, Map<String, List<ActivityOptionInfo>>> secondaryActivities = new LinkedHashMap<String, Map<String, List<ActivityOptionInfo>>>();

            StringBuilder msg = null;
            if (LOG.isDebugEnabled())
                msg = new StringBuilder();

            String curric = c.getSubjectArea();
            String num = c.getCourseNumberSuffix();

            LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData = new LinkedHashMap<String, LinkedHashMap<String, Object>>();

            try {
                getCourseHelper().getAllSectionStatus(enrollmentData, termId, curric, num);
            } catch (DocumentException e) {
                LOG.error("Could not load enrollmentInformation for course : " + c.getCode() + " for term : " + termId, e);
            }
            Map<String, String> plannedActivities = new LinkedHashMap<String, String>();
            try {
                plannedActivities = getPlanHelper().getPlanItemIdAndRefObjIdByRefObjType(getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId()).getId(), PlanConstants.SECTION_TYPE, termId);
            } catch (Exception e) {
                LOG.error("Could not load planned Activities for course : " + c.getCode() + " for term : " + termId, e);
            }

            for (ActivityOfferingDisplayInfo aodi : courseHelper.getActivityOfferingDisplaysByCourseAndTerm(courseId, c.getCode(), termId)) {
                ActivityOptionInfo activityOption = getActivityOption(term, aodi, courseIndex, courseId, c.getCode(), c.getCourseTitle(), campusCode, credits, enrollmentData, plannedActivities, msg, tdf, udf, ddf, sdcal, edcal, tcal);

                boolean enrollmentGroup = false;
                String primaryOfferingId = null;
                for (AttributeInfo attrib : aodi.getAttributes()) {
                    String key = attrib.getKey();
                    String value = attrib.getValue();

                    if ("PrimaryActivityOfferingId".equalsIgnoreCase(key))
                        primaryOfferingId = value;

                    if ("BlockEnrollment".equalsIgnoreCase(key))
                        enrollmentGroup = "true".equals(value);
                }

                if (activityOption.isPrimary()) {
                    activityOption
                            .setParentUniqueId(courseOption.getUniqueId());
                    if (msg != null)
                        msg.append("\nPrimary ")
                                .append(activityOption.getUniqueId())
                                .append(": ")
                                .append(activityOption.getCourseOfferingCode())
                                .append(" ")
                                .append(activityOption.getActivityCode());
                    primaryActivities.add(activityOption);
                } else {
                    Map<String, List<ActivityOptionInfo>> secondaryGroup = secondaryActivities
                            .get(primaryOfferingId);
                    if (msg != null)
                        msg.append("\nSecondary ")
                                .append(activityOption.getUniqueId())
                                .append(": ")
                                .append(activityOption.getCourseOfferingCode())
                                .append(" ")
                                .append(activityOption.getActivityCode())
                                .append(" -> ").append(primaryOfferingId);
                    if (secondaryGroup == null)
                        secondaryActivities
                                .put(primaryOfferingId,
                                        secondaryGroup = new LinkedHashMap<String, List<ActivityOptionInfo>>());
                    String groupKey = enrollmentGroup ? "kuali.ap.enrollmentGroup"
                            : activityOption.getActivityTypeDescription();
                    List<ActivityOptionInfo> aol = secondaryGroup.get(groupKey);
                    if (aol == null)
                        secondaryGroup
                                .put(groupKey,
                                        aol = new LinkedList<ActivityOptionInfo>());
                    aol.add(activityOption);
                }
            }
            if (msg != null)
                LOG.debug(msg.toString());

            for (ActivityOption primary : primaryActivities) {
                String parentUniqueId = primary.getUniqueId();
                Map<String, List<ActivityOptionInfo>> secondaryGroup = secondaryActivities
                        .get(primary.getActivityOfferingId());
                if (secondaryGroup != null) {
                    List<SecondaryActivityOptions> sao = new ArrayList<SecondaryActivityOptions>();
                    int i = 0;
                    for (Entry<String, List<ActivityOptionInfo>> e : secondaryGroup
                            .entrySet()) {
                        SecondaryActivityOptionsInfo secondaryOptions = new SecondaryActivityOptionsInfo();
                        secondaryOptions.setUniqueId(parentUniqueId);
                        secondaryOptions.setIndex(i);
                        secondaryOptions.setActivityTypeDescription(e.getKey());
                        secondaryOptions
                                .setEnrollmentGroup("kuali.ap.enrollmentGroup"
                                        .equals(e.getKey()));
                        List<ActivityOptionInfo> secondaryOptionList = e
                                .getValue();
                        for (ActivityOptionInfo secondaryOption : secondaryOptionList) {
                            secondaryOption.setParentUniqueId(parentUniqueId);
                            secondaryOption.setParentIndex(i);
                        }
                        Collections.sort(secondaryOptionList);
                        secondaryOptions
                                .setActivityOptions(secondaryOptionList);
                        sao.add(secondaryOptions);
                        i++;
                    }
                    ((ActivityOptionInfo) primary).setSecondaryOptions(sao);
                }
            }
            Collections.sort(primaryActivities);
            courseOption.setActivityOptions(primaryActivities);

            if (courseOption.getActivityCount(false) == 0) {
                courseOption.setSelected(false);
            }

            rv.add(courseOption);
        }
        Collections.sort(rv);
        return rv;
    }

    @Override
    public List<CourseOption> getRegisteredCourseOptions(String studentId, String termId, ScheduleBuildFilters buildFilters) {
        return null;
    }

    protected void buildCourseOptions(String termId, boolean courseLockIn, boolean lockIn,
                                      Map<String, List<String>> courseIdsActivityCodes, Map<String, String> courseIdsToCourseCds, List<CourseOption> rv, List<ActivityOptionFilter> filterList, ScheduleBuildFilters buildFilters) {
        if (!courseIdsActivityCodes.isEmpty()) {
            StringBuilder msg = null;
            if (LOG.isDebugEnabled()) {
                msg = new StringBuilder("Course options for ");
                msg.append(termId);
                msg.append("\nCourse lock-in ? ");
                msg.append(courseLockIn);
                msg.append("\nActivity lock-in ? ");
                msg.append(lockIn);
                msg.append("\nSelections :");
                msg.append(courseIdsActivityCodes);
            }
            Queue<ActivityOptionInfo> toCourseLockIn = courseLockIn ? new LinkedList<ActivityOptionInfo>() : null;
            for (CourseOption co : getCourseOptions(new ArrayList<String>(courseIdsActivityCodes.keySet()), new LinkedHashMap<String, String>(), termId)) {
                List<String> acodes = courseIdsActivityCodes.get(co.getCourseId());
                if (msg != null) {
                    msg.append("\n  Course ").append(co.getCourseCode());
                    msg.append(" ").append(co.getCourseId());
                    msg.append(" ").append(acodes);
                }
                boolean found = false;
                for (ActivityOption ao : co.getActivityOptions()) {
                    ActivityOptionInfo aoi = (ActivityOptionInfo) ao;

                    if (courseLockIn)
                        if (found)
                            aoi.setCourseLockedIn(true);
                        else
                            toCourseLockIn.add(aoi);

                    boolean foundHere = false;
                    if (acodes.isEmpty() || acodes.contains(ao.getActivityCode())) {
                        aoi.setSelected(true);
                        if (lockIn)
                            aoi.setLockedIn(true);
                        foundHere = found = true;
                    }
                    if (msg != null) {
                        msg.append("\n    Activity ")
                                .append(ao.getActivityCode()).append(" ")
                                .append(ao.getActivityOfferingId());
                        if (found)
                            msg.append(" found");
                        if (foundHere)
                            msg.append(" here");
                    }

                    for (SecondaryActivityOptions so : ao.getSecondaryOptions())
                        if (!so.isEnrollmentGroup())
                            for (ActivityOption sao : so.getActivityOptions()) {
                                ActivityOptionInfo saoi = (ActivityOptionInfo) sao;
                                if (courseLockIn)
                                    if (found)
                                        saoi.setCourseLockedIn(true);
                                    else
                                        toCourseLockIn.add(saoi);

                                boolean select = foundHere && (acodes.isEmpty() || acodes.contains(sao.getActivityCode()));
                                if (select) {
                                    saoi.setSelected(true);
                                    if (lockIn)
                                        saoi.setLockedIn(true);
                                }
                                if (msg != null) {
                                    msg.append("\n      ")
                                            .append(so
                                                    .getActivityTypeDescription())
                                            .append(" ")
                                            .append(sao.getActivityCode())
                                            .append(" ")
                                            .append(sao.getActivityOfferingId());
                                    if (select)
                                        msg.append(" selected");
                                }

                            }
                }

                CourseOptionInfo coi = (CourseOptionInfo) co;
                coi.setSelected(found);
                if (found && courseLockIn) {
                    coi.setLockedIn(found);
                    while (!toCourseLockIn.isEmpty())
                        toCourseLockIn.poll().setCourseLockedIn(true);
                }

                rv.add(co);
            }

            if (msg != null) {
                LOG.debug(msg);
            }
        }

    }

    @Override
    public List<CourseOption> getCourseOptions(String learningPlanId, String termId, ScheduleBuildFilters buildFilters) {
        String studentId = getUserSessionHelper().getStudentId();

        AcademicPlanService academicPlanService = getAcademicPlanService();
        ContextInfo context = PlanConstants.CONTEXT_INFO;
        List<PlanItemInfo> planItems;
        try {
            planItems = academicPlanService.getPlanItemsInPlan(learningPlanId, context);
        } catch (DoesNotExistException e) {
            throw new IllegalArgumentException("CO lookup failure", e);
        } catch (InvalidParameterException e) {
            throw new IllegalArgumentException("CO lookup failure", e);
        } catch (MissingParameterException e) {
            throw new IllegalArgumentException("CO lookup failure", e);
        } catch (OperationFailedException e) {
            throw new IllegalArgumentException("CO lookup failure", e);
        }

        List<StudentCourseRecordInfo> completedRecords;
        try {
            completedRecords = getAcademicRecordService().getCompletedCourseRecords(
                    studentId, context);
        } catch (DoesNotExistException e) {
            throw new IllegalArgumentException("AR lookup failure", e);
        } catch (InvalidParameterException e) {
            throw new IllegalArgumentException("AR lookup failure", e);
        } catch (MissingParameterException e) {
            throw new IllegalArgumentException("AR lookup failure", e);
        } catch (OperationFailedException e) {
            throw new IllegalStateException("AR lookup failure", e);
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("AR lookup failure", e);
        }

        Map<String, List<String>> registeredCourseIdsAndActivityCodes = new LinkedHashMap<String, List<String>>();
        for (StudentCourseRecordInfo completedRecord : completedRecords) {
            if (!termId.equals(completedRecord.getTermName()))
                continue;

            String acodeattr = completedRecord.getActivityCode();
            List<String> acodes;
            if (acodeattr == null) {
                acodes = Collections.emptyList();
            } else {
                acodes = Arrays.asList(acodeattr.split(","));
            }

            // TODO: switch to attribute, ID is incorrect here
            // courseId = completedRecord.getAttributeValue("courseId");
            registeredCourseIdsAndActivityCodes.put(completedRecord.getId(),
                    acodes);
        }

        Map<String, List<String>> cartCourseIdsAndActivityCodes = new LinkedHashMap<String, List<String>>();
        Map<String, List<String>> plannedCourseIdsAndActivityCodes = new LinkedHashMap<String, List<String>>();

        List<String> backupCourseIds = new LinkedList<String>();
        for (PlanItemInfo planItem : planItems) {
            if (!PlanConstants.COURSE_TYPE.equals(planItem.getRefObjectType()))
                continue;

            List<String> periods = planItem.getPlanPeriods();
            if (periods == null || !periods.contains(termId))
                continue;


            CourseInfo courseInfo = getCourseHelper().getCourseInfo(planItem.getRefObjectId());
            if (courseInfo == null)
                continue;

            /*ActivityOfferings for the courseId is fetched*/
            List<ActivityOfferingItem> activityOfferingItems = getCourseDetailsHelper().getActivityOfferingItemsByIdAndCd(courseInfo.getId(), courseInfo.getCode(), termId);

            List<String> acodes = new ArrayList<String>();

            /*Activities that are planned are added to acodes*/
            for (ActivityOfferingItem activityOfferingItem : activityOfferingItems) {
                if (StringUtils.hasText(activityOfferingItem.getPlanItemId())) {
                    acodes.add(activityOfferingItem.getCode());
                }
            }

            AcademicPlanServiceConstants.ItemCategory category = planItem.getCategory();

            if (courseInfo != null) {
                String courseId = courseInfo.getId();
                if (AcademicPlanServiceConstants.ItemCategory.CART.equals(category))
                    cartCourseIdsAndActivityCodes.put(courseId, acodes);
                else if (AcademicPlanServiceConstants.ItemCategory.PLANNED.equals(category))
                    plannedCourseIdsAndActivityCodes.put(courseId, acodes);
                else if (AcademicPlanServiceConstants.ItemCategory.BACKUP.equals(category))
                    backupCourseIds.add(courseId);
            }
        }

        List<CourseOption> rv = new ArrayList<CourseOption>(registeredCourseIdsAndActivityCodes.size() + cartCourseIdsAndActivityCodes.size() + plannedCourseIdsAndActivityCodes.size() + backupCourseIds.size());
        buildCourseOptions(termId, true, true, registeredCourseIdsAndActivityCodes, new LinkedHashMap<String, String>(), rv, new ArrayList<ActivityOptionFilter>(), buildFilters);
        buildCourseOptions(termId, false, true, cartCourseIdsAndActivityCodes, new LinkedHashMap<String, String>(), rv, new ArrayList<ActivityOptionFilter>(), buildFilters);
        buildCourseOptions(termId, false, false, plannedCourseIdsAndActivityCodes, new LinkedHashMap<String, String>(), rv, new ArrayList<ActivityOptionFilter>(), buildFilters);
        if (!backupCourseIds.isEmpty()) {
            for (CourseOption co : getCourseOptions(backupCourseIds, new LinkedHashMap<String, String>(), termId)) {
                rv.add(co);
            }
        }
        return rv;
    }

    @Override
    public LearningPlan getLearningPlan(String requestedLearningPlanId)
            throws PermissionDeniedException {
        if (!getUserSessionHelper().isStudent()) {
            throw new PermissionDeniedException(
                    "Must be a student to build a schedule");
        }
        String studentId = getUserSessionHelper().getStudentId();
        ContextInfo ctx = PlanConstants.CONTEXT_INFO;
        if (requestedLearningPlanId != null) {
            try {
                LearningPlanInfo rv =
                        getAcademicPlanService().getLearningPlan(
                                requestedLearningPlanId, ctx);
                if (!studentId.equals(rv.getStudentId()))
                    throw new PermissionDeniedException("Learning plan "
                            + requestedLearningPlanId
                            + " belongs to another student");
                if (!PlanConstants.LEARNING_PLAN_TYPE_PLAN.equals(rv
                        .getTypeKey()))
                    throw new PermissionDeniedException(
                            "Not a viable learning plan for building a schedule "
                                    + requestedLearningPlanId
                    );
                return rv;
            } catch (DoesNotExistException e) {
                throw new IllegalArgumentException("Learning plan "
                        + requestedLearningPlanId + " does not exist", e);
            } catch (InvalidParameterException e) {
                throw new IllegalArgumentException("Invalid learning plan ID "
                        + requestedLearningPlanId, e);
            } catch (MissingParameterException e) {
                throw new IllegalArgumentException("Invalid learning plan ID "
                        + requestedLearningPlanId, e);
            } catch (OperationFailedException e) {
                throw new IllegalStateException("LP lookup error "
                        + requestedLearningPlanId, e);
            }
        } else {
            try {
                List<LearningPlanInfo> lps =
                        getAcademicPlanService()
                                .getLearningPlansForStudentByType(studentId,
                                        PlanConstants.LEARNING_PLAN_TYPE_PLAN, ctx);
                if (lps.isEmpty()) {
                    throw new PermissionDeniedException(
                            "No learning plans found for student " + studentId);
                }
                return lps.get(0);
            } catch (DoesNotExistException e) {
                throw new IllegalArgumentException(
                        "No learning plan exists for student " + studentId, e);
            } catch (InvalidParameterException e) {
                throw new IllegalArgumentException(
                        "Invalid student ID or learning plan type " + studentId
                                + " " + PlanConstants.LEARNING_PLAN_TYPE_PLAN,
                        e
                );
            } catch (MissingParameterException e) {
                throw new IllegalArgumentException(
                        "Invalid student ID or learning plan type " + studentId
                                + " " + PlanConstants.LEARNING_PLAN_TYPE_PLAN,
                        e
                );
            } catch (OperationFailedException e) {
                throw new IllegalStateException("LP lookup error", e);
            }
        }
    }

    @Override
    // TODO: Convert from dynamic attributes to DAO service
    public List<ReservedTime> getReservedTimesForTermId(String requestedLearningPlanId, String termId)
            throws PermissionDeniedException {
        List<ReservedTime> reservedTimeInfosForTermId = new ArrayList<ReservedTime>();
        List<ReservedTimeInfo> reservedTimes = getScheduleBuildAttribute(requestedLearningPlanId).reservedTime;
        if (!CollectionUtils.isEmpty(reservedTimes)) {
            for (ReservedTimeInfo reservedTime : reservedTimes) {
                if (reservedTime.getTermId().equals(termId)) {
                    reservedTimeInfosForTermId.add(reservedTime);
                }
            }
        }
        return reservedTimeInfosForTermId;
    }

    @Override
    // TODO: Convert from dynamic attributes to DAO service
    public ReservedTime createReservedTime(String requestedLearningPlanId,
                                           ReservedTime reservedTime) throws PermissionDeniedException {
        ReservedTimeInfo createReservedTime = new ReservedTimeInfo(reservedTime);
        assert createReservedTime.getId() == null : "Already has an Id "
                + createReservedTime.getId();
        createReservedTime.setId(UUID.randomUUID().toString());
        ScheduleBuildAttribute reservedTimes = getScheduleBuildAttribute(requestedLearningPlanId);
        reservedTimes.reservedTime.add(createReservedTime);
        updateScheduleBuildAttribute(requestedLearningPlanId, reservedTimes);
        return createReservedTime;
    }

    @Override
    // TODO: Convert from dynamic attributes to DAO service
    public ReservedTime updateReservedTime(String requestedLearningPlanId,
                                           ReservedTime reservedTime) throws PermissionDeniedException {
        ScheduleBuildAttribute reservedTimes = getScheduleBuildAttribute(requestedLearningPlanId);
        ReservedTimeInfo updateReservedTime = null;
        ListIterator<ReservedTimeInfo> reservedTimeListIterator = reservedTimes.reservedTime
                .listIterator();
        while (updateReservedTime == null && reservedTimeListIterator.hasNext()) {
            ReservedTimeInfo reservedTimeInfo = reservedTimeListIterator.next();
            if (reservedTimeInfo.getId().equals(reservedTime.getId())) {
                updateReservedTime = reservedTimeInfo;
            }
        }
        if (updateReservedTime == null) {
            throw new IllegalArgumentException("Reserved time "
                    + reservedTime.getId()
                    + " does not exist on learning plan "
                    + requestedLearningPlanId == null ? "for student "
                    + getUserSessionHelper()
                    .getStudentId() : requestedLearningPlanId);
        }
        ReservedTimeInfo reservedTimeInfo = new ReservedTimeInfo(reservedTime);
        reservedTimeListIterator.set(reservedTimeInfo);
        updateScheduleBuildAttribute(requestedLearningPlanId, reservedTimes);
        return reservedTimeInfo;
    }

    @Override
    // TODO: Convert from dynamic attributes to DAO service
    public void deleteReservedTime(String requestedLearningPlanId,
                                   String reservedTimeId) throws PermissionDeniedException {
        ScheduleBuildAttribute reservedTimes = getScheduleBuildAttribute(requestedLearningPlanId);
        boolean found = false;
        Iterator<ReservedTimeInfo> reservedTimeIterator = reservedTimes.reservedTime
                .iterator();
        while (!found && reservedTimeIterator.hasNext()) {
            ReservedTimeInfo reservedTimeInfo = reservedTimeIterator.next();
            if (reservedTimeInfo.getId().equals(reservedTimeId)) {
                reservedTimeIterator.remove();
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Reserved time "
                    + reservedTimeId + " does not exist on learning plan "
                    + requestedLearningPlanId == null ? "for student "
                    + getUserSessionHelper()
                    .getStudentId() : requestedLearningPlanId);
        }
        updateScheduleBuildAttribute(requestedLearningPlanId, reservedTimes);
    }

    @Override
    // TODO: Convert from dynamic attributes to DAO service
    public List<PossibleScheduleOption> getSchedules(
            String requestedLearningPlanId) throws PermissionDeniedException {
        return new ArrayList<PossibleScheduleOption>(
                getScheduleBuildAttribute(requestedLearningPlanId).schedule);
    }

    public List<PossibleScheduleOption> getSchedulesForTerm(String requestedLearningPlanId, String termId)
            throws PermissionDeniedException {
        List<PossibleScheduleOption> possibleScheduleOptions = new ArrayList<PossibleScheduleOption>();
        List<PossibleScheduleOption> schedules = getSchedules(requestedLearningPlanId);
        if (!CollectionUtils.isEmpty(schedules) && StringUtils.hasText(termId)) {
            for (PossibleScheduleOption possibleScheduleOption : schedules) {
                if (termId.equals(possibleScheduleOption.getTermId())) {
                    possibleScheduleOptions.add(possibleScheduleOption);
                }
            }
        }
        return possibleScheduleOptions;
    }

    @Override
    // TODO: Convert from dynamic attributes to DAO service
    public PossibleScheduleOption createSchedule(
            String requestedLearningPlanId, PossibleScheduleOption schedule)
            throws PermissionDeniedException {
        PossibleScheduleOptionInfo createSchedule = new PossibleScheduleOptionInfo(schedule);
        assert createSchedule.getId() == null : "Already has an Id " + createSchedule.getId();
        createSchedule.setId(UUID.randomUUID().toString());
        createSchedule.setUniqueId(StringUtils.isEmpty(schedule.getUniqueId()) ? UUID.randomUUID().toString() : schedule.getUniqueId());
        createSchedule.setSelected(true);
        createSchedule.setTermId(schedule.getTermId());

        ScheduleBuildAttribute scheduleBuildInfo = getScheduleBuildAttribute(requestedLearningPlanId);
        createSchedule.setDescription("Saved " + (++scheduleBuildInfo.savedScheduleSequence));
        scheduleBuildInfo.schedule.add(createSchedule);
        updateScheduleBuildAttribute(requestedLearningPlanId, scheduleBuildInfo);
        return createSchedule;
    }

    @Override
    // TODO: Convert from dynamic attributes to DAO service
    public void updateSchedule(String requestedLearningPlanId,
                               PossibleScheduleOption schedule) throws PermissionDeniedException {
        ScheduleBuildAttribute scheduleBuildInfo = getScheduleBuildAttribute(requestedLearningPlanId);
        PossibleScheduleOptionInfo updateschedule = null;
        ListIterator<PossibleScheduleOptionInfo> scheduleListIterator = scheduleBuildInfo.schedule
                .listIterator();
        while (updateschedule == null && scheduleListIterator.hasNext()) {
            PossibleScheduleOptionInfo scheduleInfo = scheduleListIterator
                    .next();
            if (scheduleInfo.getId().equals(schedule.getId())) {
                updateschedule = scheduleInfo;
            }
        }
        if (updateschedule == null) {
            throw new IllegalArgumentException("Reserved time "
                    + schedule.getId() + " does not exist on learning plan "
                    + requestedLearningPlanId == null ? "for student "
                    + getUserSessionHelper()
                    .getStudentId() : requestedLearningPlanId);
        }
        scheduleListIterator.set(new PossibleScheduleOptionInfo(schedule));
        updateScheduleBuildAttribute(requestedLearningPlanId, scheduleBuildInfo);
    }

    @Override
    // TODO: Convert from dynamic attributes to DAO service
    public void deleteSchedule(String requestedLearningPlanId, String scheduleId)
            throws PermissionDeniedException {
        ScheduleBuildAttribute scheduleBuildInfo = getScheduleBuildAttribute(requestedLearningPlanId);
        boolean found = false;
        Iterator<PossibleScheduleOptionInfo> scheduleIterator = scheduleBuildInfo.schedule
                .iterator();
        while (!found && scheduleIterator.hasNext()) {
            PossibleScheduleOptionInfo reservedTimeInfo = scheduleIterator
                    .next();
            if (reservedTimeInfo.getId().equals(scheduleId)) {
                scheduleIterator.remove();
                found = true;
            }
        }

        Iterator<ReservedTimeInfo> reservedTimeIterator = scheduleBuildInfo.reservedTime.iterator();
        while (!found && reservedTimeIterator.hasNext()) {
            ReservedTimeInfo reservedTimeInfo = reservedTimeIterator.next();
            if (reservedTimeInfo.getId().equals(scheduleId)) {
                reservedTimeIterator.remove();
                found = true;
            }
        }
        if (!found) {
            throw new IllegalArgumentException("Schedule " + scheduleId
                    + " does not exist on learning plan "
                    + requestedLearningPlanId == null ? "for student "
                    + getUserSessionHelper()
                    .getStudentId() : requestedLearningPlanId);
        }
        updateScheduleBuildAttribute(requestedLearningPlanId, scheduleBuildInfo);
    }

    @Override
    public void coalesceLeafActivities(CourseOption co) {
    }

    @Override
    public String getCampusCode(Course course) {
        String campusCode = null;
        for (Attribute ca : course.getAttributes())
            if ("campusCode".equals(ca.getKey()))
                campusCode = ca.getValue();
        return campusCode;
    }

    @Override
    public ScheduleBuilder getScheduleBuilder(Term term, List<CourseOption> courseOptions, List<ReservedTime> reservedTimes, List<PossibleScheduleOption> savedSchedules, ScheduleBuildFilters buildFilters) {
        return new ScheduleBuilder(term, courseOptions, reservedTimes, savedSchedules, buildFilters);
    }

    @Override
    public ShoppingCartForm getInitialCartForm() {
        return new DefaultShoppingCartForm();
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
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

    public TermHelper getTermHelper() {
        if (termHelper == null) {
            termHelper = KsapFrameworkServiceLocator.getTermHelper();
        }
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
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

    public AcademicRecordService getAcademicRecordService() {
        if (this.academicRecordService == null) {
            //   TODO: Use constants for namespace.
            this.academicRecordService = (AcademicRecordService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/academicrecord", "arService"));
        }
        return this.academicRecordService;
    }

    public void setAcademicRecordService(AcademicRecordService academicRecordService) {
        this.academicRecordService = academicRecordService;
    }

    public ShoppingCartStrategy getShoppingCartStrategy() {
        if (shoppingCartStrategy == null) {
            shoppingCartStrategy = UwMyplanServiceLocator.getInstance().getShoppingCartStrategy();
        }
        return shoppingCartStrategy;
    }

    public void setShoppingCartStrategy(ShoppingCartStrategy shoppingCartStrategy) {
        this.shoppingCartStrategy = shoppingCartStrategy;
    }

    public CourseDetailsInquiryHelperImpl getCourseDetailsHelper() {
        if (courseDetailsHelper == null) {
            courseDetailsHelper = UwMyplanServiceLocator.getInstance().getCourseDetailsHelper();
        }
        return courseDetailsHelper;
    }

    public void setCourseDetailsHelper(CourseDetailsInquiryHelperImpl courseDetailsHelper) {
        this.courseDetailsHelper = courseDetailsHelper;
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
