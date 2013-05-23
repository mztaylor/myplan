package org.kuali.student.myplan.course.service;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
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
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.controller.PlanController;
import org.kuali.student.myplan.plan.dataobject.*;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equalIgnoreCase;


public class CourseDetailsInquiryHelperImpl extends KualiInquirableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryHelperImpl.class);

    private final static String[] WEEKDAYS_FIRST_LETTER = {"M", "T", "W", "Th", "F", "Sa", "Su"};

    private final static List<String> QUARTERS = Arrays.asList("Autumn", "Winter", "Spring", "Summer");

    public static final String NOT_OFFERED_IN_LAST_TEN_YEARS = "Not offered for more than 10 years.";

    private transient CourseService courseService;

    private transient CourseOfferingService courseOfferingService;

    private transient AcademicCalendarService academicCalendarService;

    private transient AtpService atpService;

    private transient LuService luService;


    private transient AcademicPlanService academicPlanService;

    private transient AcademicRecordService academicRecordService;


    @Autowired
    CourseHelper courseHelper;


    //TODO: These should be changed to an ehCache spring bean
//    private HashMap<String, Map<String, String>> hashMap;

    private HashMap<String, String> courseComments = new HashMap<String, String>();

    public HashMap<String, String> getCourseComments() {
        return courseComments;
    }

    public void setCourseComments(HashMap<String, String> courseComments) {
        this.courseComments = courseComments;
    }

    protected LuService getLuService() {
        if (this.luService == null) {
            this.luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    private transient CourseLinkBuilder courseLinkBuilder;

    public CourseLinkBuilder getCourseLinkBuilder() {
        if (courseLinkBuilder == null) {
            this.courseLinkBuilder = new CourseLinkBuilder();
        }
        return courseLinkBuilder;
    }

    public void setCourseLinkBuilder(CourseLinkBuilder courseLinkBuilder) {
        this.courseLinkBuilder = courseLinkBuilder;
    }


    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    @Override
    public CourseDetails retrieveDataObject(Map fieldValues) {
        String studentId = UserSessionHelper.getStudentRegId();
        boolean loadActivityOffering = false;
        if (fieldValues.get(PlanConstants.PARAM_OFFERINGS_FLAG) != null) {
            loadActivityOffering = Boolean.valueOf(fieldValues.get(PlanConstants.PARAM_OFFERINGS_FLAG).toString());
        }
        return retrieveCourseDetails((String) fieldValues.get(PlanConstants.PARAM_COURSE_ID), studentId, loadActivityOffering);
    }


    //TODO: All of this needs to be moved into a separate class of its own with an interface to allow institutions to easily override


    /**
     * Populates course with catalog information (title, id, code, description) and next offering information.
     * Other properties are left empty and a flag is set to indicate only summary view
     *
     * @param courseId
     * @return
     */
    public CourseSummaryDetails retrieveCourseSummaryById(String courseId) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        ServicesStatusDataObject servicesStatusDataObject = (ServicesStatusDataObject) request.getSession().getAttribute(CourseSearchConstants.SWS_SERVICES_STATUS);
        if (!servicesStatusDataObject.isCourseOfferingServiceUp() || !servicesStatusDataObject.isAcademicCalendarServiceUp() || !servicesStatusDataObject.isAcademicRecordServiceUp()) {
            AtpHelper.addServiceError("curriculumTitle");
        }


        /**
         * If version identpendent Id provided, retrieve the right course version Id based on current term/date
         * else get the same id as the provided course version specific Id
         */
        CourseInfo course = getCourseHelper().getCourseInfo(courseId);
        CourseSummaryDetails courseDetails = retrieveCourseSummary(course);
        return courseDetails;
    }


    /**
     * Retrieves course details based on the course argument
     *
     * @param course
     * @return
     */
    protected CourseSummaryDetails retrieveCourseSummary(CourseInfo course) {

        if (null == course) {
            return null;
        }

        // Check for status of the services
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        ServicesStatusDataObject servicesStatusDataObject = (ServicesStatusDataObject) request.getSession().getAttribute(CourseSearchConstants.SWS_SERVICES_STATUS);


        String subject = course.getSubjectArea().trim();

        CourseSummaryDetails courseDetails = new CourseSummaryDetails();
        courseDetails.setVersionIndependentId(course.getVersionInfo().getVersionIndId());
        courseDetails.setCourseId(course.getId());
        courseDetails.setCode(course.getCode());
        courseDetails.setCredit(CreditsFormatter.formatCredits(course));
        courseDetails.setCourseTitle(course.getCourseTitle());
        courseDetails.setSubjectArea(subject);
        courseDetails.setCourseNumber(course.getCourseNumberSuffix());

        // -- Curriculum  Title
        Map<String, String> subjectAreaMap = OrgHelper.getTrimmedSubjectAreas();
        String curriculumTitle = subjectAreaMap.get(subject);
        courseDetails.setCurriculumTitle(curriculumTitle);

        // -- Course Description
        // -- Course Requisites
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


        // -- Terms Offered
        for (String term : course.getTermsOffered()) {
            String atp = AtpHelper.getAtpTypeName(term);
            if (null != atp) {
                courseDetails.getTermsOffered().add(atp);
            }
        }

        //Sorting Terms Offered
        if (courseDetails.getTermsOffered().size() > 0) {
            Collections.sort(courseDetails.getTermsOffered(), new Comparator<String>() {
                @Override
                public int compare(String val1, String val2) {
                    return String.valueOf(QUARTERS.indexOf(val1)).compareTo(String.valueOf(QUARTERS.indexOf(val2)));
                }
            });
        }

        // Load campus location map
        List<OrgInfo> campusList = OrgHelper.getOrgInfo(CourseSearchConstants.CAMPUS_LOCATION_ORG_TYPE, CourseSearchConstants.ORG_QUERY_SEARCH_BY_TYPE_REQUEST, CourseSearchConstants.ORG_TYPE_PARAM);

        Map<String, String> abbrAttributes = course.getAttributes();

        for (Map.Entry<String, String> entry : abbrAttributes.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();


            // -- Gen Ed requirements
            if ("Y".equals(value) && key.startsWith(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX)) {

                // Get only the abbre_val of gen ed requirements
                String abbrev = EnumerationHelper.getEnumAbbrValForCode(key);
                courseDetails.getAbbrGenEdRequirements().add(abbrev);

                //  Get general education requirements.
                EnumeratedValueInfo info = EnumerationHelper.getGenEdReqEnumInfo(key);
                String genEdText = String.format("%s (%s)", info.getValue(), info.getAbbrevValue());
                courseDetails.getGenEdRequirements().add(genEdText);
            }


            // -- Campus Locations
            if (key.startsWith(CourseSearchConstants.CAMPUS_LOCATION_COURSE_ATTRIBUTE)) {
                for (OrgInfo campusOrg : campusList) {
                    if (campusOrg.getId().equals(value)) {
                        courseDetails.getCampusLocations().add(campusOrg.getLongName());
                    }
                }
            }
        }


        //  Fetch the available terms from the Academic Calendar Service.
        if (servicesStatusDataObject.isAcademicCalendarServiceUp() && servicesStatusDataObject.isCourseOfferingServiceUp()) {
            try {
                QueryByCriteria predicates = QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PUBLISHED));
                List<TermInfo> termInfos = getAcademicCalendarService().searchForTerms(predicates, CourseSearchConstants.CONTEXT_INFO);
                for (TermInfo term : termInfos) {
                    List<CourseOfferingInfo> offerings = getCourseOfferingService().getCourseOfferingsByCourseAndTerm(course.getId(), term.getId(), CourseSearchConstants.CONTEXT_INFO);
                    if (offerings != null && !offerings.isEmpty()) {
                        courseDetails.getScheduledTerms().add(term.getName());
                    }
                }
            } catch (Exception e) {
                logger.error("Web service call failed.", e);
            }
        } else {
            logger.info("Could not load scheduled terms. AcademicCalServiceStatus:" + servicesStatusDataObject.isAcademicCalendarServiceUp() + " CourseOfferingServiceStatus:" + servicesStatusDataObject.isCourseOfferingServiceUp());
        }

        Collections.sort(courseDetails.getScheduledTerms(), new Comparator<String>() {
            @Override
            public int compare(String val1, String val2) {
                return AtpHelper.termToYearTerm(val1).compareTo(AtpHelper.termToYearTerm(val2));
            }
        });


        // Last Offered
        //  If course not scheduled for future terms, Check for the last term when course was offered
        if (servicesStatusDataObject.isCourseOfferingServiceUp()) {
            CourseOfferingService cos = getCourseOfferingService();

            if (courseDetails.getScheduledTerms().isEmpty()) {
                //TODO: The number 10 should really come from a property at the very least a static constant
                int year = Calendar.getInstance().get(Calendar.YEAR) - 10;
                try {
                    // The right strategy would be using the multiple equal predicates joined using an and
                    String values = String.format("%s, %s, %s", year, subject, course.getCourseNumberSuffix());
                    QueryByCriteria criteria = QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("values", values));
                    List<CourseOfferingInfo> courseOfferingInfo = cos.searchForCourseOfferings(criteria, CourseSearchConstants.CONTEXT_INFO);

                    if (courseOfferingInfo != null && courseOfferingInfo.size() > 0) {
                        String lastOffered = courseOfferingInfo.get(0).getTermId();
                        // TODO: this needs to be moved into ATP helper
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

        return courseDetails;

    }

    /**
     * Method returns a courseDetails object with course summary, academic record, course offering and planned information for the course
     *
     * @param courseId
     * @param studentId
     * @return
     */
    public CourseDetails retrieveCourseDetails(String courseId, String studentId, boolean loadActivityOffering) {

        CourseDetails courseDetails = new CourseDetails();

        CourseInfo course = getCourseHelper().getCourseInfo(courseId);

        // Get Course Summary first
        CourseSummaryDetails courseSummaryDetails = retrieveCourseSummary(course);
        courseDetails.setCourseSummaryDetails(courseSummaryDetails);

        // Course Plan + Academic Records
        courseDetails.setPlannedCourseSummary(getPlannedCourseSummary(course, studentId));

        if (loadActivityOffering) {
            // Course offerings
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
            List<String> termList = null;
            if (request.getParameter("section_term") != null) {
                String termId = AtpHelper.atpIdToTermName(request.getParameter("section_term"));
                termList = new ArrayList<String>();
                termList.add(termId);
            } else {
                termList = courseDetails.getCourseSummaryDetails().getScheduledTerms();
            }
            List<CourseOfferingInstitution> courseOfferingInstitutions = getCourseOfferingInstitutions(course, termList);
            courseDetails.setCourseOfferingInstitutionList(courseOfferingInstitutions);
        }
        return courseDetails;
    }


    /**
     * Retrieves plan summary for the course. Finds all the plan, backup and academic record information spread across
     * multiple terms for the provided course Id and Student Id
     *
     * @param courseId
     * @param studentId
     * @return
     */
    public PlannedCourseSummary getPlannedCourseSummaryById(String courseId, String studentId) {

        /**
         * If version identpendent Id provided, retrieve the right course version Id based on current term/date
         * else get the same id as the provided course version specific Id
         */
        CourseInfo course = getCourseHelper().getCourseInfo(courseId);
        return getPlannedCourseSummary(course, studentId);
    }


    /**
     * Retrieves plan summary for the course. Finds all the plan, backup and academic record information spread across
     * multiple terms for the provided course and student Id
     *
     * @param course
     * @param studentId
     * @return
     */
    public PlannedCourseSummary getPlannedCourseSummary(CourseInfo course, String studentId) {

        PlannedCourseSummary plannedCourseSummary = new PlannedCourseSummary();


        // Planned, backup and Saved Item
        AcademicPlanService academicPlanService = getAcademicPlanService();

        //   Get the first learning plan. There should only be one ...
        String planTypeKey = AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;
        try {
            List<LearningPlanInfo> plans = academicPlanService.getLearningPlansForStudentByType(studentId, planTypeKey, PlanConstants.CONTEXT_INFO);
            if (plans.size() > 0) {
                LearningPlan plan = plans.get(0);

                //  Fetch the plan items which are associated with the plan.
                List<PlanItemInfo> planItemsInPlan = academicPlanService.getPlanItemsInPlan(plan.getId(), PlanConstants.CONTEXT_INFO);

                //  Iterate through the plan items and set flags to indicate whether the item is a planned/backup or saved course.
                for (PlanItem planItemInPlanTemp : planItemsInPlan) {
                    if (planItemInPlanTemp.getRefObjectId().equals(course.getVersionInfo().getVersionIndId())) {
                        //  Assuming type is planned or backup if not wishlist.
                        String typeKey = planItemInPlanTemp.getTypeKey();
                        if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
                            plannedCourseSummary.setSavedItemId(planItemInPlanTemp.getId());
                            String dateStr = planItemInPlanTemp.getMeta().getCreateTime().toString();
                            dateStr = DateFormatHelper.getDateFomatted(dateStr);
                            plannedCourseSummary.setSavedItemDateCreated(dateStr);
                        } else {
                            PlanItemDataObject planItem = PlanItemDataObject.build(planItemInPlanTemp);
                            if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
                                plannedCourseSummary.getPlannedList().add(planItem);
                            } else if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
                                plannedCourseSummary.getBackupList().add(planItem);
                            }
                        }
                    }
                }
            }
        } catch (org.kuali.student.r2.common.exceptions.DoesNotExistException e) {
            // Ignore and not load any plan data
        } catch (Exception e1) {
            logger.error(" Error loading plan information for course :" + course.getCode() + " " + e1.getMessage());
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

                if (course.getId().equalsIgnoreCase(studentInfo.getId())) {
                    plannedCourseSummary.getAcadRecList().add(acadrec);

                    String[] str = AtpHelper.atpIdToTermNameAndYear(studentInfo.getTermName());
                    plannedCourseSummary.getAcademicTerms().add(str[0] + " " + str[1]);
                }
            }
        } catch (Exception e) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS");
        }

        return plannedCourseSummary;

    }


    /**
     * Get courseOffering information broken down by institution code across the terms requested
     *
     * @param courseId
     * @param terms
     * @return
     */
    public List<CourseOfferingInstitution> getCourseOfferingInstitutionsById(String courseId, List<String> terms) {

        /**
         * If version identpendent Id provided, retrieve the right course version Id based on current term/date
         * else get the same id as the provided course version specific Id
         */
        CourseInfo course = getCourseHelper().getCourseInfo(courseId);
        return getCourseOfferingInstitutions(course, terms);
    }


    /**
     * Get courseOffering information broken down by institution code across the terms requested.
     *
     * @param course
     * @param terms
     * @return list of course offering institution
     */
    public List<CourseOfferingInstitution> getCourseOfferingInstitutions(CourseInfo course, List<String> terms) {
        List<CourseOfferingInstitution> instituteList = new ArrayList<CourseOfferingInstitution>();


        List<YearTerm> ytList = new ArrayList<YearTerm>();
        for (String term : terms) {
            YearTerm yt = AtpHelper.termToYearTerm(term);
            ytList.add(yt);
        }
        Collections.sort(ytList, Collections.reverseOrder());

        Map<String, Map<String, PlanItem>> planItemsByTerm = loadStudentsPlanItems();

        for (YearTerm yt : ytList) {
            String atp = yt.toATP();
            if (AtpHelper.getPublishedTerms().contains(atp)) {
                // Load course offering comments
                List<CourseOfferingInfo> courseOfferingInfoList = null;
                try {
                    courseOfferingInfoList = getCourseOfferingService().getCourseOfferingsByCourseAndTerm(course.getId(), atp, CourseSearchConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error(" Could not load course offerings for : " + course.getCode() + " atp : " + atp);
                    return instituteList;
                }

                String courseComments = null;
                String curriculumComments = null;
                for (CourseOfferingInfo courseInfo : courseOfferingInfoList) {

                    if (null != courseComments && null != curriculumComments) break;

                    for (AttributeInfo attributeInfo : courseInfo.getAttributes()) {
                        String key = attributeInfo.getKey();
                        String value = attributeInfo.getValue();
                        if ("CourseComments".equalsIgnoreCase(key) && value.length() > 0) {
                            courseComments = value;
                            if (null != curriculumComments) break;
                        } else if ("CurriculumComments".equalsIgnoreCase(key) && value.length() > 0) {
                            curriculumComments = value;
                            if (null != courseComments) break;
                        }

                    }
                }

                List<ActivityOfferingItem> list = getActivityOfferingItems(course, courseOfferingInfoList, atp, planItemsByTerm.get(atp));
                for (ActivityOfferingItem activityOfferingItem : list) {
                    int instituteCode = Integer.valueOf(activityOfferingItem.getInstituteCode());
                    String instituteName = activityOfferingItem.getInstituteName();
                    CourseOfferingInstitution courseOfferingInstitution = null;
                    for (CourseOfferingInstitution temp : instituteList) {
                        if (instituteCode == temp.getCode()) {
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
                        courseOfferingTerm.setCourseComments(courseComments);
                        courseOfferingTerm.setCurriculumComments(curriculumComments);
                        courseOfferingTerm.setInstituteCode(courseOfferingInstitution.getCode());
                        courseOfferingTerm.setCoursePlanType(getPlanType(getPlanItem(course.getVersionInfo().getVersionIndId(), yt.toATP())));
                        courseOfferingTermList.add(courseOfferingTerm);
                    }

                    courseOfferingTerm.getActivityOfferingItemList().add(activityOfferingItem);
                }
            }
        }
        Collections.sort(instituteList);

        return instituteList;

    }

    /**
     * @param courseId
     * @param termId
     * @return
     */
    public List<ActivityOfferingItem> getActivityOfferingItemsById(String courseId, String termId) {

        List<ActivityOfferingItem> activityOfferingItems = new ArrayList<ActivityOfferingItem>();

        CourseInfo course = getCourseHelper().getCourseInfo(courseId);
        try {

            List<CourseOfferingInfo> courseOfferingInfoList = getCourseOfferingService().getCourseOfferingsByCourseAndTerm(course.getId(), termId, CourseSearchConstants.CONTEXT_INFO);

            Map<String, Map<String, PlanItem>> planItemsByTerm = loadStudentsPlanItems();
            activityOfferingItems = getActivityOfferingItems(course, courseOfferingInfoList, termId, planItemsByTerm.get(termId));

        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }

        return activityOfferingItems;
    }


    /**
     * Returns activity Offerings for given courseId and term
     *
     * @param course
     * @param courseOfferingInfoList List of course offerings for the given term
     * @param termId                 Term for which activity offerings are requested
     * @param planItemMap            Map of refObjectId to planItemId for the requested term
     * @return
     */

    protected List<ActivityOfferingItem> getActivityOfferingItems(CourseInfo course, List<CourseOfferingInfo> courseOfferingInfoList, String termId, Map<String, PlanItem> planItemMap) {

        List<String> plannedSections = new ArrayList<String>();
        if (planItemMap != null) {
            for (PlanItem planItem : planItemMap.values()) {
                if (PlanConstants.SECTION_TYPE.equals(planItem.getRefObjectType())) {
                    String courseCode = getCourseHelper().getCourseCdFromActivityId(planItem.getRefObjectId());
                    if (course.getCode().equalsIgnoreCase(courseCode)) {
                        plannedSections.add(planItem.getRefObjectId());
                    }
                }
            }
        }
        List<ActivityOfferingItem> activityOfferingItemList = new ArrayList<ActivityOfferingItem>();

        if (AtpHelper.getPublishedTerms().contains(termId)) {
            boolean openForPlanning = AtpHelper.isAtpSetToPlanning(termId);
            for (CourseOfferingInfo courseInfo : courseOfferingInfoList) {

                // Activity offerings come back as a list, the first item is primary, the remaining are secondary
                String courseOfferingID = courseInfo.getId();
                List<ActivityOfferingDisplayInfo> aodiList = null;

                try {
                    aodiList = getCourseOfferingService().getActivityOfferingDisplaysForCourseOffering(courseOfferingID, CourseSearchConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.info("Not able to load activity offering for courseOffering: " + courseOfferingID + " Term:" + termId);
                    continue;
                }

                for (ActivityOfferingDisplayInfo aodi : aodiList) {
                    String planRefObjId = aodi.getId();
                    String planItemId = null;
                    if (null != planItemMap) {
                        PlanItem planItem = planItemMap.get(planRefObjId);
                        if (planItem != null) {
                            planItemId = planItem.getId();
                        }
                    }
                    ActivityOfferingItem activityOfferingItem = getActivityItem(aodi, courseInfo, openForPlanning, termId, planItemId);
                    activityOfferingItemList.add(activityOfferingItem);
                    if (plannedSections.contains(planRefObjId)) {
                        plannedSections.remove(planRefObjId);
                    }
                }
            }
            //Sections withdrawn and planned are included in activities
            for (String activityId : plannedSections) {
                ActivityOfferingDisplayInfo activityDisplayInfo = null;
                try {
                    activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityId, PlanConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error("Could not retrieve ActivityOffering data for" + activityId, e);
                    continue;
                }
                if (activityDisplayInfo != null) {
                    String courseOfferingId = null;
                    for (AttributeInfo attributeInfo : activityDisplayInfo.getAttributes()) {
                        if ("PrimaryActivityOfferingId".equalsIgnoreCase(attributeInfo.getKey())) {
                            courseOfferingId = attributeInfo.getValue();
                            break;
                        }
                    }
                    CourseOfferingInfo courseOfferingInfo = null;
                    try {
                        courseOfferingInfo = getCourseOfferingService().getCourseOffering(courseOfferingId, CourseSearchConstants.CONTEXT_INFO);
                    } catch (Exception e) {
                        logger.error("Could not retrieve CourseOffering data for" + courseOfferingId, e);
                        continue;
                    }
                    String planItemId = planItemMap.get(activityId).getId();
                    ActivityOfferingItem activityOfferingItem = getActivityItem(activityDisplayInfo, courseOfferingInfo, openForPlanning, termId, planItemId);
                    activityOfferingItemList.add(activityOfferingItem);
                }
            }
            Collections.sort(activityOfferingItemList, new Comparator<ActivityOfferingItem>() {
                @Override
                public int compare(ActivityOfferingItem item1, ActivityOfferingItem item2) {
                    return item1.getCode().compareTo(item2.getCode());
                }
            });
        }

        return activityOfferingItemList;
    }

    /**
     * Used to retrieve a ActivityOffering using the following params
     *
     * @param displayInfo
     * @param courseOfferingInfo
     * @param openForPlanning
     * @param termId
     * @param planItemId
     * @return
     */
    public ActivityOfferingItem getActivityItem(ActivityOfferingDisplayInfo displayInfo, CourseOfferingInfo courseOfferingInfo, boolean openForPlanning, String termId, String planItemId) {
        ActivityOfferingItem activity = new ActivityOfferingItem();
        /*Data from ActivityOfferingDisplayInfo*/
        activity.setCourseId(courseOfferingInfo.getCourseId());
        activity.setCode(displayInfo.getActivityOfferingCode());
        activity.setStateKey(displayInfo.getStateKey());
        activity.setActivityOfferingType(displayInfo.getTypeName());
        List<MeetingDetails> meetingDetailsList = activity.getMeetingDetailsList();
        {
            ScheduleDisplayInfo sdi = displayInfo.getScheduleDisplay();
            for (ScheduleComponentDisplayInfo scdi : sdi.getScheduleComponentDisplays()) {
                MeetingDetails meeting = new MeetingDetails();

                if (!PlanConstants.WITHDRAWN_STATE.equalsIgnoreCase(activity.getStateKey())) {
                    BuildingInfo building = scdi.getBuilding();
                    if (building != null) {
                        meeting.setCampus(building.getCampusKey());
                        meeting.setBuilding(building.getBuildingCode());
                    }
                }

                if (!PlanConstants.WITHDRAWN_STATE.equalsIgnoreCase(activity.getStateKey())) {
                    RoomInfo roomInfo = scdi.getRoom();
                    if (roomInfo != null) {
                        meeting.setRoom(roomInfo.getRoomCode());
                    }
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
        String instituteCode = null;
        String instituteName = null;

        String campus = null;
        for (AttributeInfo attrib : displayInfo.getAttributes()) {
            String key = attrib.getKey();
            String value = attrib.getValue();
            if ("Campus".equalsIgnoreCase(key) && !"".equals(value)) {
                campus = value;
                continue;
            }
            if ("FeeAmount".equalsIgnoreCase(key) && !"".equals(value)) {
                activity.setFeeAmount(value);
                continue;
            }
            if ("SLN".equalsIgnoreCase(key)) {
                activity.setRegistrationCode(value);
                continue;
            }
            if ("InstituteCode".equals(key)) {
                instituteCode = value;
                continue;
            }
            if ("InstituteName".equals(key) && !"".equals(value)) {
                instituteName = value;
                continue;
            }

            if ("SectionComments".equalsIgnoreCase(key)) {
                activity.setSectionComments(value);
                continue;
            }

            if ("SummerTerm".equalsIgnoreCase(key) && StringUtils.hasText(value)) {
                activity.setSummerTerm(value);
                continue;
            }

            if ("PrimaryActivityOfferingId".equalsIgnoreCase(key)) {
                activity.setPrimaryActivityOfferingId(value);
                continue;
            }

            /*PrimarySectionCode is for the add button hover text in secondary sections
            * Which have primary section not planned eg: COM 320 AA:"Add Section AA and A to Plan"*/
            if ("PrimaryActivityOfferingCode".equalsIgnoreCase(key)) {
                activity.setPrimaryActivityOfferingCode(value);
                activity.setPrimary(value.equalsIgnoreCase(activity.getCode()));
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
        activity.setInstructor(displayInfo.getInstructorName());
        activity.setHonorsSection(displayInfo.getIsHonorsOffering());

        /*data from CourseOfferingInfo*/
        activity.setCredits(courseOfferingInfo.getCreditOptionName());
        activity.setGradingOption(courseOfferingInfo.getGradingOptionName());


        /*Data from other params*/
        activity.setNewThisYear(false);
        activity.setDetails("View more details");
        activity.setPlanItemId(planItemId);
        activity.setAtpId(termId);
        activity.setOpenForPlanning(openForPlanning);
        YearTerm yt = AtpHelper.atpToYearTerm(termId);
        activity.setQtryr(yt.toQTRYRParam());
        if (instituteCode == null) {
            instituteCode = campus;
        }
        if (instituteName == null) {
            instituteName = campus;
        }
        activity.setInstituteName(instituteName);
        activity.setInstituteCode(instituteCode);
        return activity;
    }

    /**
     * Validates if the courseId/versionIndependentId is valid or not.
     *
     * @param courseId
     * @return
     */
    public boolean isCourseIdValid(String courseId) {
        boolean isCourseIdValid = false;
        CourseInfo course = getCourseHelper().getCourseInfo(courseId);
        if (course != null) {
            isCourseIdValid = true;
        }
        return isCourseIdValid;
    }


    /**
     * Loads all plan items for the logged in user.  Returns the plan items grouped by terms
     *
     * @return A Map of term to a Map of refObjectId to planItemId
     */

    protected Map<String, Map<String, PlanItem>> loadStudentsPlanItems() {
        String studentId = UserSessionHelper.getStudentRegId();
        Map<String, Map<String, PlanItem>> planItemsByTerm = new HashMap<String, Map<String, PlanItem>>();
        try {
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(studentId, PlanConstants.LEARNING_PLAN_TYPE_PLAN, CourseSearchConstants.CONTEXT_INFO);
            for (LearningPlanInfo learningPlan : learningPlanList) {
                List<PlanItemInfo> planItems = getAcademicPlanService().getPlanItemsInPlan(learningPlan.getId(), CourseSearchConstants.CONTEXT_INFO);
                if (null != planItems) {
                    for (PlanItem item : planItems) {
                        for (String planPeriod : item.getPlanPeriods()) {
                            Map<String, PlanItem> planMap = planItemsByTerm.get(planPeriod);
                            if (null == planMap) {
                                planMap = new HashMap<String, PlanItem>();
                                planItemsByTerm.put(planPeriod, planMap);
                            }

                            planMap.put(item.getRefObjectId(), item);
                        }
                    }
                }
            }


        } catch (org.kuali.student.r2.common.exceptions.DoesNotExistException e) {
            // Ignore : Student does not have any plan
        } catch (Exception e) {
            logger.error(" Could not load plan information for student: " + studentId);
        }

        return planItemsByTerm;
    }


    /**
     * used to know if the given planItemInfo is a back up or a plantype.
     *
     * @param planItemInfo
     * @return
     */
    public String getPlanType(PlanItemInfo planItemInfo) {
        if (planItemInfo.getTypeKey() != null && planItemInfo.getTypeKey().equalsIgnoreCase(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
            return PlanConstants.PLANNED_TYPE;
        } else if (planItemInfo.getTypeKey() != null && planItemInfo.getTypeKey().equalsIgnoreCase(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            return PlanConstants.BACKUP_TYPE;
        }
        return null;
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
     * Checks if the Given refObjId for a section (eg: com 453 A or com 453 AA or can use a versionIndependentId) for the given atpId exists in Plan/backup
     * returns planItemInfo if exists otherwise returns empty PlanitemInfo.
     *
     * @param refObjId
     * @param atpId
     * @return
     */
    public PlanItemInfo getPlanItem(String refObjId, String atpId) {
        try {
            PlanController planController = new PlanController();
            PlanItemInfo planItem = planController.getPlannedOrBackupPlanItem(refObjId, atpId);
            if (planItem != null) {
                return planItem;
            }
        } catch (Exception e) {
            logger.error(" Exception loading plan item :" + refObjId + " for atp: " + atpId + " " + e.getMessage());
        }
        return new PlanItemInfo();
    }


}