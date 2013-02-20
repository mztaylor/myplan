package org.kuali.student.myplan.course.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.common.exceptions.DoesNotExistException;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.exceptions.OperationFailedException;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.common.search.dto.SearchResultCell;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.core.atp.service.AtpService;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.organization.dto.OrgInfo;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.dto.ScheduleComponentDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.ScheduleDisplayInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.dataobject.*;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.controller.PlanController;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.AtpHelper.YearTerm;
import org.kuali.student.myplan.plan.util.DateFormatHelper;
import org.kuali.student.myplan.plan.util.EnumerationHelper;
import org.kuali.student.myplan.plan.util.OrgHelper;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.myplan.utils.TimeStringMillisConverter;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.TimeOfDayInfo;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.kuali.student.r2.core.room.dto.BuildingInfo;
import org.kuali.student.r2.core.room.dto.RoomInfo;
import org.kuali.student.r2.core.scheduling.dto.TimeSlotInfo;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equalIgnoreCase;


public class CourseDetailsInquiryViewHelperServiceImpl extends KualiInquirableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryViewHelperServiceImpl.class);

    private final static String[] WEEKDAYS_FIRST_LETTER = {"M", "T", "W", "Th", "F", "Sa", "Su"};

    public static final String NOT_OFFERED_IN_LAST_TEN_YEARS = "Not offered for more than 10 years.";

    private transient CourseService courseService;

    private transient CourseOfferingService courseOfferingService;

    private transient AcademicCalendarService academicCalendarService;

    private transient AtpService atpService;

    private transient LuService luService;


    private transient AcademicPlanService academicPlanService;

    private transient AcademicRecordService academicRecordService;

    private transient boolean isAcademicCalendarServiceUp = true;

    private transient boolean isAcademicRecordServiceUp = true;

    private transient boolean isCourseOfferingServiceUp = true;


    public boolean isAcademicCalendarServiceUp() {
        return isAcademicCalendarServiceUp;
    }

    public void setAcademicCalendarServiceUp(boolean academicCalendarServiceUp) {
        isAcademicCalendarServiceUp = academicCalendarServiceUp;
    }

    public boolean isAcademicRecordServiceUp() {
        return isAcademicRecordServiceUp;
    }

    public void setAcademicRecordServiceUp(boolean academicRecordServiceUp) {
        isAcademicRecordServiceUp = academicRecordServiceUp;
    }

    public boolean isCourseOfferingServiceUp() {
        return isCourseOfferingServiceUp;
    }

    public void setCourseOfferingServiceUp(boolean courseOfferingServiceUp) {
        isCourseOfferingServiceUp = courseOfferingServiceUp;
    }

    //TODO: These should be changed to an ehCache spring bean
    private Map<String, List<OrgInfo>> campusLocationCache;
    private Map<String, String> atpCache;
    private HashMap<String, Map<String, String>> hashMap;
    private HashMap<String, String> courseComments = new HashMap<String, String>();

    public HashMap<String, String> getCourseComments() {
        return courseComments;
    }

    public void setCourseComments(HashMap<String, String> courseComments) {
        this.courseComments = courseComments;
    }

    public HashMap<String, Map<String, String>> getHashMap() {
        if (hashMap == null) {
            hashMap = new HashMap<String, Map<String, String>>();
        }
        return hashMap;
    }

    public void setHashMap(HashMap<String, Map<String, String>> hashMap) {
        this.hashMap = hashMap;
    }

    public Map<String, List<OrgInfo>> getCampusLocationCache() {
        if (this.campusLocationCache == null) {
            this.campusLocationCache = new HashMap<String, List<OrgInfo>>();
        }
        return this.campusLocationCache;
    }

    public void setCampusLocationCache(Map<String, List<OrgInfo>> campusLocationCache) {
        this.campusLocationCache = campusLocationCache;
    }

    protected LuService getLuService() {
        if (this.luService == null) {
            this.luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    private transient CourseLinkBuilder courseLinkBuilder;

    // default is to create real links
    private CourseLinkBuilder.LINK_TEMPLATE courseLinkTemplateStyle = CourseLinkBuilder.LINK_TEMPLATE.COURSE_DETAILS;

    public CourseLinkBuilder getCourseLinkBuilder() {
        if (courseLinkBuilder == null) {
            this.courseLinkBuilder = new CourseLinkBuilder();
        }
        return courseLinkBuilder;
    }

    public void setCourseLinkBuilder(CourseLinkBuilder courseLinkBuilder) {
        this.courseLinkBuilder = courseLinkBuilder;
    }

    @Override
    public CourseDetails retrieveDataObject(Map fieldValues) {
        String studentId = UserSessionHelper.getStudentId();
        return retrieveCourseDetails((String) fieldValues.get(PlanConstants.PARAM_COURSE_ID), studentId);
    }

    /**
     * Populates course with catalog information (title, id, code, description) and next offering information.
     * Other properties are left empty and a flag is set to indicate only summary view
     *
     * @param courseId
     * @return
     */
    public CourseDetails retrieveCourseSummary(String courseId, String studentId) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        boolean courseOfferingServiceUp = Boolean.parseBoolean(request.getAttribute(CourseSearchConstants.IS_COURSE_OFFERING_SERVICE_UP).toString());
        boolean academicCalendarServiceUp = Boolean.parseBoolean(request.getAttribute(CourseSearchConstants.IS_ACADEMIC_CALENDER_SERVICE_UP).toString());
        boolean academicRecordServiceUp = Boolean.parseBoolean(request.getAttribute(CourseSearchConstants.IS_ACADEMIC_RECORD_SERVICE_UP).toString());
        if (!courseOfferingServiceUp || !academicCalendarServiceUp || !academicRecordServiceUp) {
            AtpHelper.addServiceError("curriculumTitle");
            setAcademicCalendarServiceUp(academicCalendarServiceUp);
            setAcademicRecordServiceUp(academicRecordServiceUp);
            setCourseOfferingServiceUp(courseOfferingServiceUp);
        }


        try {
            CourseDetails courseDetails = new CourseDetails();
            courseDetails.setSummaryOnly(true);

            /*Get version verified course*/
            String verifiedCourseId = getVerifiedCourseId(courseId);
            CourseInfo course = getCourseService().getCourse(verifiedCourseId);
            courseDetails.setVersionIndependentId(course.getVersionInfo().getVersionIndId());
            courseDetails.setCourseId(course.getId());
            courseDetails.setCode(course.getCode());

            if (course.getDescr() != null) {
                String formatted = course.getDescr().getFormatted();

                // Split course description "AAA Prerequisite: BBB Offering: CCC" and pull out "AAA" and "BBB"
                // Guarantee result arrays will always have at least one element
                if (formatted == null) formatted = "";
                String[] aaa = formatted.split("Offered:");
                String[] bbb = aaa[0].split("Prerequisite:");

                String descr = bbb[0].trim();
                descr = getCourseLinkBuilder().makeLinks(descr);
                courseDetails.setCourseDescription(descr);

                if (bbb.length > 1) {
                    String prereq = bbb[1].trim();
                    prereq = prereq.substring(0, 1).toUpperCase().concat(prereq.substring(1));
                    prereq = getCourseLinkBuilder().makeLinks(prereq);
                    courseDetails.getRequisites().add(prereq);
                }
            }

            courseDetails.setCredit(CreditsFormatter.formatCredits(course));
            courseDetails.setCourseTitle(course.getCourseTitle());

            // Terms Offered
            Map<String, String> atpMap = initializeAtpTypesCache();
            for (String term : course.getTermsOffered()) {
                String atp = atpMap.get(term);
                courseDetails.getTermsOffered().add(atp);
            }

            return courseDetails;

        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }
    }

    public CourseDetails retrieveCourseDetails(String courseId, String studentId) {
        CourseDetails courseDetails = retrieveCourseSummary(courseId, studentId);
        courseDetails.setSummaryOnly(false);

        CourseInfo course = null;
        try {
            /*Get version verified course*/
            course = getCourseService().getCourse(getVerifiedCourseId(courseId));
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }

        // Campus Locations
        List<OrgInfo> orgInfoList = OrgHelper.getOrgInfo(CourseSearchConstants.CAMPUS_LOCATION, CourseSearchConstants.ORG_QUERY_SEARCH_BY_TYPE_REQUEST, CourseSearchConstants.ORG_TYPE_PARAM);
        getCampusLocationCache().put(CourseSearchConstants.CAMPUS_LOCATION, orgInfoList);

        for (String campus : getCampusLocationsOfferedIn(courseId)) {
            for (OrgInfo orgInfo : orgInfoList) {
                if (campus.equalsIgnoreCase(orgInfo.getId())) {
                    String longName = orgInfo.getLongName();
                    courseDetails.getCampusLocations().add(longName);
                    break;
                }
            }
        }

        Map<String, String> abbrAttributes = course.getAttributes();
        for (Map.Entry<String, String> entry : abbrAttributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if ("Y".equals(value) && key.startsWith(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX)) {

                // Get only the abbre_val of gen ed requirements
                String abbrev = EnumerationHelper.getEnumAbbrValForCode(key);
                courseDetails.getAbbrGenEdRequirements().add(abbrev);

                //  Get general education requirements.
                EnumeratedValueInfo info = EnumerationHelper.getGenEdReqEnumInfo(key);
                String genEdText = String.format("%s (%s)", info.getValue(), info.getAbbrevValue());
                courseDetails.getGenEdRequirements().add(genEdText);
            }
        }

        /*
          Use the course offering service to see if the course is being offered in the selected term.
          Note: In the UW implementation of the Course Offering service, course id is actually course code.
        */
        List<String> scheduledTerms = new ArrayList<String>();
        try {
            //  Fetch the available terms from the Academic Calendar Service.
            if (isAcademicCalendarServiceUp() && isCourseOfferingServiceUp()) {
                try {
                    QueryByCriteria predicates = QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PUBLISHED));
                    List<TermInfo> termInfos = getAcademicCalendarService().searchForTerms(predicates, CourseSearchConstants.CONTEXT_INFO);
                    CourseOfferingService cos = getCourseOfferingService();
                    for (TermInfo term : termInfos) {
                        String key = term.getId();
                        String subject = course.getSubjectArea();

                        List<String> offerings = cos
                                .getCourseOfferingIdsByTermAndSubjectArea(key, subject, CourseSearchConstants.CONTEXT_INFO);

                        if (offerings.contains(course.getCode())) {
                            scheduledTerms.add(term.getName());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Web service call failed.", e);
                }

            }

            AcademicPlanService academicPlanService = getAcademicPlanService();

            //   Get the first learning plan. There should only be one ...
            String planTypeKey = AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;
            List<LearningPlanInfo> plans = academicPlanService.getLearningPlansForStudentByType(studentId, planTypeKey, PlanConstants.CONTEXT_INFO);
            if (plans.size() > 0) {
                LearningPlan plan = plans.get(0);

                //  Fetch the plan items which are associated with the plan.
                List<PlanItemInfo> planItemsInPlan = academicPlanService.getPlanItemsInPlan(plan.getId(), PlanConstants.CONTEXT_INFO);

                //  Iterate through the plan items and set flags to indicate whether the item is a planned/backup or saved course.
                for (PlanItem planItemInPlanTemp : planItemsInPlan) {
                    if (planItemInPlanTemp.getRefObjectId().equals(courseDetails.getVersionIndependentId())) {
                        //  Assuming type is planned or backup if not wishlist.
                        String typeKey = planItemInPlanTemp.getTypeKey();
                        if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
                            courseDetails.setSavedItemId(planItemInPlanTemp.getId());
                            String dateStr = planItemInPlanTemp.getMeta().getCreateTime().toString();
                            dateStr = DateFormatHelper.getDateFomatted(dateStr);
                            courseDetails.setSavedItemDateCreated(dateStr);
                        } else {
                            PlanItemDataObject planItem = PlanItemDataObject.build(planItemInPlanTemp);
                            if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
                                courseDetails.getPlannedList().add(planItem);
                            } else if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {

                                courseDetails.getBackupList().add(planItem);

                            }
                        }
                    }
                }
            }


            List<YearTerm> ytList = new ArrayList<YearTerm>();
            List<String> termList = scheduledTerms;
            for (String term : termList) {
                YearTerm yt = AtpHelper.termToYearTerm(term);
                ytList.add(yt);
            }
            Collections.sort(ytList, Collections.reverseOrder());

            List<CourseOfferingInstitution> instituteList = courseDetails.getCourseOfferingInstitutionList();

            for (YearTerm yt : ytList) {
                String atp = yt.toATP();
                List<ActivityOfferingItem> list = getActivityOfferingItems(courseId, atp, courseDetails.getCode());
                for (ActivityOfferingItem activityOfferingItem : list) {
                    String instituteCode = activityOfferingItem.getInstituteCode();
                    String instituteName = activityOfferingItem.getInstituteName();
                    CourseOfferingInstitution courseOfferingInstitution = null;
                    for (CourseOfferingInstitution temp : instituteList) {
                        if (instituteCode.equals(temp.getCode())) {
                            courseOfferingInstitution = temp;
                            break;
                        }
                    }
                    if (courseOfferingInstitution == null) {
                        courseOfferingInstitution = new CourseOfferingInstitution();
                        courseOfferingInstitution.setCode(instituteCode);
                        courseOfferingInstitution.setName(instituteName);
                        instituteList.add(courseOfferingInstitution);
                    }

                    List<CourseOfferingTerm> courseOfferingTermList = courseOfferingInstitution.getCourseOfferingTermList();
                    CourseOfferingTerm courseOfferingTerm = null;
                    for (CourseOfferingTerm temp : courseOfferingTermList) {
                        if (yt.equals(temp.getYearTerm())) {
                            courseOfferingTerm = temp;
                        }
                    }
                    if (courseOfferingTerm == null) {
                        courseOfferingTerm = new CourseOfferingTerm();
                        courseOfferingTerm.setYearTerm(yt);
                        courseOfferingTerm.setTerm(yt.toLabel());
                        courseOfferingTerm.setCourseComments(courseComments.get(atp));
                        courseOfferingTerm.setInstituteCode(courseOfferingInstitution.getCode());
                        courseOfferingTermList.add(courseOfferingTerm);
                    }

                    courseOfferingTerm.getActivityOfferingItemList().add(activityOfferingItem);
                }
            }
            Collections.sort(instituteList, Collections.reverseOrder());

        } catch (Exception e) {
            logger.error("Exception loading course offering for:" + course.getCode(), e);
        }


        //Curriculum
        String courseCode = courseDetails.getCode();
        String subject = null;
        String number = null;
        if (courseCode != null) {
            String[] splitStr = courseCode.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            subject = splitStr[0];
            number = splitStr[1];
            String temp = getTitle(subject);
            StringBuffer value = new StringBuffer();
            value = value.append(temp);
            value = value.append(" (").append(subject.trim()).append(")");

            courseDetails.setCurriculumTitle(value.toString());
        }
        //  If course not scheduled for future terms, Check for the last term when course was offered


        if (isCourseOfferingServiceUp()) {
            CourseOfferingService cos = getCourseOfferingService();

            List<String> termList = scheduledTerms;
            if (termList.isEmpty()) {
                int year = Calendar.getInstance().get(Calendar.YEAR) - 10;
                try {
                    // The right strategy would be using the multiple equal predicates joined using an and
                    String values = String.format("%s, %s, %s", year, subject, number);
                    QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("values", values));
                    List<CourseOfferingInfo> courseOfferingInfo = cos.searchForCourseOfferings(criteria, CourseSearchConstants.CONTEXT_INFO);

                    if (courseOfferingInfo != null && courseOfferingInfo.size() > 0) {
                        String lastOffered = courseOfferingInfo.get(0).getTermId();
                        lastOffered = lastOffered.substring(0, 1).toUpperCase().concat(lastOffered.substring(1, lastOffered.length()));
                        String atpId = AtpHelper.getAtpIdFromTermYear(lastOffered);
                        if (AtpHelper.isAtpCompletedTerm(atpId)) {
                            courseDetails.setLastOffered(lastOffered);
                        }
                    } else {
                        courseDetails.setLastOffered(NOT_OFFERED_IN_LAST_TEN_YEARS);
                    }
                } catch (Exception e) {
                    String[] params = {};
                    GlobalVariables.getMessageMap().putWarningForSectionId(CourseSearchConstants.COURSE_SEARCH_PAGE, PlanConstants.ERROR_TECHNICAL_PROBLEMS, params);
                    logger.error("Could not load courseOfferingInfo list.", e);
                }
            }
        }

        // Get  Academic Record Data from the SWS and set that to CourseDetails acadRecordList
        try {
            List<StudentCourseRecordInfo> studentCourseRecordInfos = getAcademicRecordService().getCompletedCourseRecords(studentId, PlanConstants.CONTEXT_INFO);
            for (StudentCourseRecordInfo studentInfo : studentCourseRecordInfos) {
                AcademicRecordDataObject acadrec = new AcademicRecordDataObject();
                acadrec.setAtpId(studentInfo.getTermName());
                acadrec.setPersonId(studentInfo.getPersonId());
                acadrec.setCourseCode(studentInfo.getCourseCode());
                acadrec.setCourseTitle(studentInfo.getCourseTitle());
                acadrec.setCourseId(studentInfo.getId());
                acadrec.setCredit(studentInfo.getCreditsEarned());
                acadrec.setGrade(studentInfo.getCalculatedGradeValue());
                acadrec.setRepeated(studentInfo.getIsRepeated());
                acadrec.setActivityCode(studentInfo.getActivityCode());

                courseDetails.getAcadRecList().add(acadrec);

                if (courseDetails.getCourseId().equalsIgnoreCase(studentInfo.getId())) {
                    String[] str = AtpHelper.atpIdToTermNameAndYear(studentInfo.getTermName());
                    courseDetails.getAcademicTerms().add(str[0] + " " + str[1]);
                }
            }
        } catch (Exception e) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS");
        }


        return courseDetails;
    }

    public CourseDetails getCourseSummaryWithSections(String courseId, String studentId, String termId) {
        CourseDetails courseDetails = retrieveCourseSummary(courseId, studentId);
        List<CourseOfferingInstitution> courseOfferingInstitutionList = courseDetails.getCourseOfferingInstitutionList();
        CourseOfferingInstitution courseOfferingInstitution = new CourseOfferingInstitution();
        courseOfferingInstitutionList.add(courseOfferingInstitution);
        List<CourseOfferingTerm> courseOfferingTermList = courseOfferingInstitution.getCourseOfferingTermList();
        CourseOfferingTerm courseOfferingTerm = new CourseOfferingTerm();
        courseOfferingTermList.add(courseOfferingTerm);
        List<ActivityOfferingItem> activityOfferingItemList = getActivityOfferingItems(courseId, termId, null);
        courseOfferingTerm.setActivityOfferingItemList(activityOfferingItemList);

        return courseDetails;
    }

    public List<ActivityOfferingItem> getActivityOfferingItems(String courseId, String termId, String courseCode) {
        // Get version verified course
        courseId = getVerifiedCourseId(courseId);
        if (courseCode == null) {
            try {
                CourseInfo course = getCourseService().getCourse(courseId);
                courseCode = course.getCode();
            } catch (DoesNotExistException e) {
                throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
            } catch (Exception e) {
                throw new RuntimeException("Query failed.", e);
            }
        }

        List<ActivityOfferingItem> activityOfferingItemList = new ArrayList<ActivityOfferingItem>();
        try {
            CourseOfferingService cos = getCourseOfferingService();

            List<CourseOfferingInfo> courseOfferingInfoList = cos.getCourseOfferingsByCourseAndTerm(courseId, termId, CourseSearchConstants.CONTEXT_INFO);

            for (CourseOfferingInfo courseInfo : courseOfferingInfoList) {
                for (AttributeInfo attributeInfo : courseInfo.getAttributes()) {
                    String key = attributeInfo.getKey();
                    String value = attributeInfo.getValue();
                    if ("CourseComments".equalsIgnoreCase(key) && value.length() > 0 && !courseComments.containsKey(courseInfo.getTermId())) {
                        courseComments.put(courseInfo.getTermId(), value);
                        break;
                    }
                }

                // Activity offerings come back as a list, the first item is primary, the remaining are secondary
                String courseOfferingID = courseInfo.getCourseId();
                List<ActivityOfferingDisplayInfo> aodiList = cos.getActivityOfferingDisplaysForCourseOffering(courseOfferingID, CourseSearchConstants.CONTEXT_INFO);
                boolean primary = true;

                for (ActivityOfferingDisplayInfo aodi : aodiList) {
                    ActivityOfferingItem activity = new ActivityOfferingItem();
                    String sectionId = aodi.getActivityOfferingCode();
                    activity.setCode(sectionId);

                    String typeName = aodi.getTypeName();
                    activity.setActivityOfferingType(typeName);

                    activity.setCredits(courseInfo.getCreditOptionName());
                    activity.setGradingOption(courseInfo.getGradingOptionName());
                    List<MeetingDetails> meetingDetailsList = activity.getMeetingDetailsList();
                    {
                        ScheduleDisplayInfo sdi = aodi.getScheduleDisplay();
                        for (ScheduleComponentDisplayInfo scdi : sdi.getScheduleComponentDisplays()) {
                            MeetingDetails meeting = new MeetingDetails();

                            BuildingInfo building = scdi.getBuilding();
                            if (building != null) {
                                meeting.setCampus(building.getCampusKey());
                                meeting.setBuilding(building.getBuildingCode());
                            }

                            RoomInfo roomInfo = scdi.getRoom();
                            if (roomInfo != null) {
                                meeting.setRoom(roomInfo.getRoomCode());
                            }

                            for (TimeSlotInfo timeSlot : scdi.getTimeSlots()) {

                                String days = "";
                                for (int weekday : timeSlot.getWeekdays()) {
                                    if (weekday > -1 && weekday < 7) {
                                        String letter = WEEKDAYS_FIRST_LETTER[weekday];
                                        days += letter;
                                    }
                                }
                                if (!"".equals(days)) {
                                    meeting.setDays(days);
                                }

                                TimeOfDayInfo startInfo = timeSlot.getStartTime();
                                TimeOfDayInfo endInfo = timeSlot.getEndTime();
                                if (startInfo != null && endInfo != null) {
                                    long startTimeMillis = startInfo.getMilliSeconds();
                                    String startTime = TimeStringMillisConverter.millisToStandardTime(startTimeMillis);

                                    long endTimeMillis = endInfo.getMilliSeconds();
                                    String endTime = TimeStringMillisConverter.millisToStandardTime(endTimeMillis);

                                    String time = startTime + " - " + endTime;

                                    meeting.setTime(time);
                                }
                                meetingDetailsList.add(meeting);
                            }
                        }
                    }


                    String instituteCode = "";
                    String instituteName = "";

                    // Use campus as default institute code and name
                    if (!meetingDetailsList.isEmpty()) {
                        MeetingDetails details = meetingDetailsList.get(0);
                        String campus = details.getCampus();
                        instituteCode = campus;
                        instituteName = campus;
                    }

                    String enrollCount = null;
                    String enrollMaximum = null;
                    String enrollEstimate = null;
                    for (AttributeInfo attrib : aodi.getAttributes()) {
                        String key = attrib.getKey();
                        String value = attrib.getValue();
                        if ("FeeAmount".equalsIgnoreCase(key) && !"".equals(value)) {
                            activity.setFeeAmount(value);
                            continue;
                        }
                        if ("SLN".equalsIgnoreCase(key)) {
                            activity.setRegistrationCode(value);
                            continue;
                        }
                        if ("instituteCode".equals(key)) {
                            instituteCode = value;
                            continue;
                        }
                        if ("instituteName".equals(key) && !"".equals(value)) {
                            instituteName = value;
                            continue;
                        }

                        if ("currentEnrollment".equals(key) && !"".equals(value)) {
                            enrollCount = value;
                            continue;
                        }

                        if ("enrollmentLimit".equals(key) && !"".equals(value)) {
                            enrollMaximum = value;
                            continue;
                        }

                        if ("limitEstimate".equals(key) && "E".equals(value)) {
                            enrollEstimate = value;
                            continue;
                        }

                        if ("SectionComments".equalsIgnoreCase(key)) {
                            activity.setSectionComments(value);
                            continue;
                        }

                        Boolean flag = Boolean.valueOf(value);
                        if ("ServiceLearning".equalsIgnoreCase(key)) {
                            activity.setServiceLearning(flag);
                        } else if ("ResearchCredit".equalsIgnoreCase(key)) {
                            activity.setResearch(flag);
                        } else if ("DistanceLearning".equalsIgnoreCase(key)) {
                            activity.setDistanceLearning(flag);
                        } else if ("JointSections".equalsIgnoreCase(key)) {
                            activity.setJointOffering(flag);
                        } else if ("Writing".equalsIgnoreCase(key)) {
                            activity.setWritingSection(flag);
                        } else if ("FinancialAidEligible".equalsIgnoreCase(key)) {
                            activity.setIneligibleForFinancialAid(flag);
                        } else if ("AddCodeRequired".equalsIgnoreCase(key)) {
                            activity.setAddCodeRequired(flag);
                        } else if ("IndependentStudy".equalsIgnoreCase(key)) {
                            activity.setIndependentStudy(flag);
                        } else if ("EnrollmentRestrictions".equalsIgnoreCase(key)) {
                            activity.setEnrollRestriction(flag);
                        }

                    }
                    activity.setEnrollOpen(true);
                    activity.setEnrollCount(enrollCount);
                    activity.setEnrollMaximum(enrollMaximum);
                    activity.setEnrollEstimate(enrollEstimate);
                    activity.setInstructor(aodi.getInstructorName());

                    activity.setHonorsSection(aodi.getIsHonorsOffering());
                    activity.setNewThisYear(false);

                    activity.setDetails("View more details");

                    // Added this flag to know if the activityoffering is planned/backup
                    boolean planned = isPlanned(courseCode + " " + sectionId, termId);
                    activity.setPlanned(planned);
                    activity.setAtpId(termId);
                    YearTerm yt = AtpHelper.atpToYearTerm(termId);
                    activity.setQtryr(yt.toQTRYRParam());

                    activity.setInstituteName(instituteName);
                    activity.setInstituteCode(instituteCode);

                    activity.setPrimary(primary);
                    primary = false;
                    activityOfferingItemList.add(activity);

                }

            }
        } catch (Exception e) {
            logger.error("Could not load the Section Details");
        }
        return activityOfferingItemList;

    }


    /**
     * To get the title for the respective display name
     *
     * @param display
     * @return
     */
    protected String getTitle(String display) {
        String titleValue = null;
        Map<String, String> subjects = new HashMap<String, String>();
        if (!this.getHashMap().containsKey(CourseSearchConstants.SUBJECT_AREA)) {
            subjects = OrgHelper.getTrimmedSubjectAreas();
            getHashMap().put(CourseSearchConstants.SUBJECT_AREA, subjects);

        } else {
            subjects = getHashMap().get(CourseSearchConstants.SUBJECT_AREA);
        }

        if (subjects != null && subjects.size() > 0) {
            titleValue = subjects.get(display.trim());
        }

        return titleValue;
    }

    private List<String> getCampusLocationsOfferedIn(String courseId) {
        List<String> campusLocations = new ArrayList<String>();
        SearchRequest searchRequest = new SearchRequest("myplan.course.getCampusLocations");
        searchRequest.addParam("cluId", courseId);
        searchRequest.addParam("lastScheduledTerm", AtpHelper.getLastScheduledAtpId());
        SearchResult searchResult = null;
        try {
            searchResult = getLuService().search(searchRequest);
        } catch (MissingParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        if (searchResult != null) {
            for (SearchResultRow row : searchResult.getRows()) {
                campusLocations.add(OrgHelper.getCellValue(row, "lu.resultColumn.campusVal"));
            }
        }
        return campusLocations;
    }


    /**
     * Validates if the courseId/versionIndependentId is valid or not.
     *
     * @param courseId
     * @return
     */
    public boolean isCourseIdValid(String courseId) {
        boolean isCourseIdValid = false;

        CourseInfo course = null;
        try {
            /*Get version verified course*/
            course = getCourseService().getCourse(getVerifiedCourseId(courseId));
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }
        if (course != null) {
            isCourseIdValid = true;
        }
        return isCourseIdValid;
    }

    public boolean isPlanned(String refObjId, String atpId) {
        boolean isPlanned = false;
        PlanItemInfo planItem = null;
        PlanController planController = new PlanController();
        planItem = planController.getPlannedOrBackupPlanItem(refObjId, atpId);
        if (planItem != null) {
            isPlanned = true;
        }
        return isPlanned;
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

    protected synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    public synchronized void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }


    /**
     * Provides an instance of the AtpService client.
     */
    protected AtpService getAtpService() {
        if (atpService == null) {
            // TODO: Namespace should not be hard-coded.
            atpService = (AtpService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/atp", "AtpService"));
        }
        return this.atpService;
    }

    public void setAtpService(AtpService atpService) {
        this.atpService = atpService;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    protected AcademicCalendarService getAcademicCalendarService() {
        if (this.academicCalendarService == null) {
            this.academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return this.academicCalendarService;
    }

    public void setAcademicCalendarService(AcademicCalendarService academicCalendarService) {
        this.academicCalendarService = academicCalendarService;
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(AcademicPlanServiceConstants.NAMESPACE,
                            AcademicPlanServiceConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }


    /**
     * Takes a courseId that can be either a version independent Id or a version dependent Id and
     * returns a version dependent Id. In case of being passed in a version depend
     *
     * @param courseId
     * @return
     */
    private String getVerifiedCourseId(String courseId) {
        String verifiedCourseId = null;
        try {
            SearchRequest req = new SearchRequest("myplan.course.version.id");
            req.addParam("courseId", courseId);
            req.addParam("courseId", courseId);
            req.addParam("lastScheduledTerm", AtpHelper.getLastScheduledAtpId());
            SearchResult result = getLuService().search(req);
            for (SearchResultRow row : result.getRows()) {
                for (SearchResultCell cell : row.getCells()) {
                    if ("lu.resultColumn.cluId".equals(cell.getKey())) {
                        verifiedCourseId = cell.getValue();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("version verified Id retrieval failed", e);
        }
        return verifiedCourseId;
    }


    /**
     * Initializes ATP term cache.
     * AtpSeasonalTypes rarely change, so fetch them all and store them in a Map.
     */
    private synchronized Map<String, String> initializeAtpTypesCache() {
        if (null == atpCache) {
            try {
                List<AtpTypeInfo> atpTypeInfos = getAtpService().getAtpTypes();
                atpCache = new HashMap<String, String>();
                for (AtpTypeInfo ti : atpTypeInfos) {
                    atpCache.put(ti.getId(), ti.getName().substring(0, 1).toUpperCase() + ti.getName().substring(1));
                }
            } catch (OperationFailedException e) {
                logger.error("ATP types lookup failed.", e);
            }
        }
        return atpCache;
    }
}