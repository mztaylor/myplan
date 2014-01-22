package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.EnumerationHelper;
import org.kuali.student.myplan.plan.util.OrgHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 1/25/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class SingleQuarterInquiryHelperImpl extends KualiInquirableImpl {

    private final Logger logger = Logger.getLogger(SingleQuarterInquiryHelperImpl.class);

    private transient AcademicRecordService academicRecordService;

    private transient AcademicPlanService academicPlanService;

    private transient CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper;

    private transient CourseOfferingService courseOfferingService;

    @Autowired
    private CourseHelper courseHelper;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Autowired
    private PlanHelper planHelper;


    @Override
    public PlannedTerm retrieveDataObject(Map fieldValues) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String termAtpId = request.getParameter("term_atp_id");
        String studentId = getUserSessionHelper().getStudentId();


        /*************PlannedCourseList**************/
        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();

        try {
            plannedCoursesList = getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, studentId, termAtpId);
        } catch (Exception e) {
            logger.error("Could not load plannedCourseslist", e);

        }

        /****academic record SWS call to get the studentCourseRecordInfo list *****/
        List<StudentCourseRecordInfo> studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();

        try {
            studentCourseRecordInfos = getAcademicRecordService().getCompletedCourseRecords(studentId, PlanConstants.CONTEXT_INFO);
        } catch (OperationFailedException ofe) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS due to OperationFailedException.", ofe);
            GlobalVariables.getMessageMap().putWarningForSectionId(PlanConstants.TERM_PAGE_ID,
                     PlanConstants.ERROR_ACA_RECORD_SWS_PROBLEMS);
        } catch (Exception e) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS.", e);
        }


        /*************BackupCourseList**************/
        List<PlannedCourseDataObject> backupCoursesList = new ArrayList<PlannedCourseDataObject>();

        try {
            backupCoursesList = getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP, studentId, termAtpId);
        } catch (Exception e) {
            logger.error("Could not load backupCourseList", e);

        }

        /*************RecommendedCourseList**************/
        List<PlannedCourseDataObject> recommendedCoursesList = new ArrayList<PlannedCourseDataObject>();

        try {
            recommendedCoursesList = getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, studentId, termAtpId);
        } catch (Exception e) {
            logger.error("Could not load recommendedCourseList", e);

        }


        PlannedTerm perfectPlannedTerm = SingleQuarterHelperBase.populatePlannedTerms(plannedCoursesList, backupCoursesList, recommendedCoursesList, studentCourseRecordInfos, termAtpId);
        return perfectPlannedTerm;
    }

    protected List<PlannedCourseDataObject> getPlanItemListByTermId(String planItemType, String studentId, String termId)
            throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {

        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();

        Map<String, String> subjectAreas = OrgHelper.getTrimmedSubjectAreas();

        AcademicPlanService academicPlanService = getAcademicPlanService();
        ContextInfo context = CourseSearchConstants.CONTEXT_INFO;

        String planTypeKey = PlanConstants.LEARNING_PLAN_TYPE_PLAN;

        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentId, planTypeKey, CourseSearchConstants.CONTEXT_INFO);
        for (LearningPlanInfo learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemList = academicPlanService.getPlanItemsInPlan(learningPlanID, context);
            Collections.sort(planItemList, new Comparator<PlanItemInfo>() {
                public int compare(PlanItemInfo o1, PlanItemInfo o2) {
                    return o2.getMeta().getCreateTime().compareTo(o1.getMeta().getCreateTime());
                }
            });
            Map<String, List<String>> sectionsWithdrawn = new HashMap<String, List<String>>();
            Map<String, List<String>> sectionsSuspended = new HashMap<String, List<String>>();
            Map<String, List<ActivityOfferingItem>> plannedSections = new HashMap<String, List<ActivityOfferingItem>>();
            for (PlanItemInfo planItemInfo : planItemList) {

                if (planItemInfo.getPlanPeriods() != null && planItemInfo.getPlanPeriods().size() > 0 && planItemInfo.getPlanPeriods().get(0).equalsIgnoreCase(termId) && planItemInfo.getTypeKey().equalsIgnoreCase(planItemType) && planItemInfo.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
                    PlannedCourseDataObject plannedCourse = new PlannedCourseDataObject();
                    if (planItemInfo.getDescr() != null && StringUtils.hasText(planItemInfo.getDescr().getPlain())) {
                        plannedCourse.setNote(planItemInfo.getDescr().getPlain());
                    }
                    String courseID = planItemInfo.getRefObjectId();
                    String crossListedCode = getPlanHelper().getCrossListedCourse(planItemInfo.getAttributes());
                    //  Only create a data object for the specified type.
                    if (planItemInfo.getTypeKey().equals(planItemType)) {

                        plannedCourse.setPlanItemDataObject(PlanItemDataObject.build(planItemInfo));

                        //  If the course info lookup fails just log the error and omit the item.
                        try {
                            if (getCourseDetailsInquiryHelper().isCourseIdValid(courseID, crossListedCode)) {
                                CourseSummaryDetails courseDetails = getCourseDetailsInquiryHelper().retrieveCourseSummaryByIdAndCd(courseID, crossListedCode);
                                plannedCourse.setCourseDetails(courseDetails);
                            } else {
                                continue;
                            }
                            // TODO: Add Plan activities to this view

                        } catch (Exception e) {
                            logger.error(String.format("Unable to retrieve course info for plan item [%s].", planItemInfo.getId()), e);
                            continue;
                        }

                        if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItemInfo.getTypeKey())) {
                            plannedCourse.setAdviserName(getUserSessionHelper().getName(planItemInfo.getMeta().getCreateId()));
                            plannedCourse.setProposed(PlanConstants.LEARNING_PLAN_ITEM_PROPOSED_STATE_KEY.equals(planItemInfo.getStateKey()));
                        } else {
                            String atpId = planItemInfo.getPlanPeriods().get(0);
                            PlanItemInfo recommendedPlanItem = getPlanHelper().getPlanItemByAtpAndType(planItemInfo.getLearningPlanId(), planItemInfo.getRefObjectId(), atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, getPlanHelper().getCrossListedCourse(planItemInfo.getAttributes()));
                            plannedCourse.setAdviserRecommended(recommendedPlanItem != null && PlanConstants.LEARNING_PLAN_ITEM_ACCEPTED_STATE_KEY.equals(recommendedPlanItem.getStateKey()));
                        }

                        plannedCoursesList.add(plannedCourse);
                    }
                } else if (planItemInfo.getRefObjectType().equalsIgnoreCase(PlanConstants.SECTION_TYPE)) {
                    List<String> planPeriods = planItemInfo.getPlanPeriods();
                    String term = !planPeriods.isEmpty() ? planPeriods.get(0) : null;
                    if (null != term && AtpHelper.isAtpSetToPlanning(term)) {
                        List<ActivityOfferingItem> activityOfferingItems = new ArrayList<ActivityOfferingItem>();
                        String activityOfferingId = planItemInfo.getRefObjectId();
                        ActivityOfferingDisplayInfo activityDisplayInfo = null;
                        try {
                            activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityOfferingId, PlanConstants.CONTEXT_INFO);
                        } catch (Exception e) {
                            logger.error("Could not retrieve ActivityOffering data for" + activityOfferingId, e);
                            continue;
                        }
                        if (activityDisplayInfo != null) {
                            /*TODO: move this to Coursehelper to make it institution neutral*/
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

                            ActivityOfferingItem activityOfferingItem = getCourseDetailsInquiryHelper().getActivityItem(activityDisplayInfo, courseOfferingInfo, !AtpHelper.isAtpSetToPlanning(term), term, planItemInfo.getId());
                            activityOfferingItems.add(activityOfferingItem);
                            String key = generateKey(courseOfferingInfo.getSubjectArea(), courseOfferingInfo.getCourseNumberSuffix(), term);
                            if (plannedSections.containsKey(key)) {
                                plannedSections.get(key).add(activityOfferingItem);
                            } else {
                                plannedSections.put(key, activityOfferingItems);
                            }
                            if (PlanConstants.SUSPENDED_STATE.equalsIgnoreCase(activityOfferingItem.getStateKey())) {
                                if (sectionsSuspended.containsKey(key)) {
                                    sectionsSuspended.get(key).add(activityOfferingItem.getCode());
                                } else {
                                    List<String> suspendedSections = new ArrayList<String>();
                                    suspendedSections.add(activityOfferingItem.getCode());
                                    sectionsSuspended.put(key, suspendedSections);
                                }
                            } else if (PlanConstants.WITHDRAWN_STATE.equalsIgnoreCase(activityOfferingItem.getStateKey())) {
                                if (sectionsWithdrawn.containsKey(key)) {
                                    sectionsWithdrawn.get(key).add(activityOfferingItem.getCode());
                                } else {
                                    List<String> withdrawnSections = new ArrayList<String>();
                                    withdrawnSections.add(activityOfferingItem.getCode());
                                    sectionsWithdrawn.put(key, withdrawnSections);
                                }
                            }
                        }
                    }


                } else if (planItemInfo.getTypeKey().equals(planItemType) &&
                        (PlanConstants.PLACE_HOLDER_TYPE_GEN_ED.equals(planItemInfo.getRefObjectType()) ||
                                PlanConstants.PLACE_HOLDER_TYPE.equals(planItemInfo.getRefObjectType()))) {
                    PlannedCourseDataObject plannedCourse = new PlannedCourseDataObject();
                    PlanItemDataObject planItemData = PlanItemDataObject.build(planItemInfo);
                    plannedCourse.setPlanItemDataObject(planItemData);
                    plannedCourse.setPlaceHolder(true);
                    if (planItemInfo.getDescr() != null && StringUtils.hasText(planItemInfo.getDescr().getPlain())) {
                        plannedCourse.setNote(planItemInfo.getDescr().getPlain());
                    }
                    String placeHolderCode = EnumerationHelper.getEnumAbbrValForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.PLACE_HOLDER_ENUM_KEY);
                    String placeHolderValue = EnumerationHelper.getEnumValueForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.PLACE_HOLDER_ENUM_KEY);
                    if (placeHolderCode == null) {
                        placeHolderCode = EnumerationHelper.getEnumAbbrValForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.GEN_EDU_ENUM_KEY);
                        placeHolderValue = EnumerationHelper.getEnumValueForCodeByType(planItemInfo.getRefObjectId(), PlanConstants.GEN_EDU_ENUM_KEY);
                    }
                    plannedCourse.setPlaceHolderCode(placeHolderCode);
                    plannedCourse.setPlaceHolderValue(placeHolderValue);
                    plannedCourse.setCourseDetails(new CourseSummaryDetails());
                    plannedCourse.setPlaceHolderCredit(planItemInfo.getCredit() == null ? "" : String.valueOf(planItemInfo.getCredit().intValue()));
                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItemInfo.getTypeKey())) {
                        plannedCourse.setAdviserName(getUserSessionHelper().getName(planItemInfo.getMeta().getCreateId()));
                        plannedCourse.setProposed(PlanConstants.LEARNING_PLAN_ITEM_PROPOSED_STATE_KEY.equals(planItemInfo.getStateKey()));
                    } else {
                        String atpId = planItemInfo.getPlanPeriods().get(0);
                        PlanItemInfo recommendedPlanItem = getPlanHelper().getPlanItemByAtpAndType(planItemInfo.getLearningPlanId(), planItemInfo.getRefObjectId(), atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, getPlanHelper().getCrossListedCourse(planItemInfo.getAttributes()));
                        plannedCourse.setAdviserRecommended(recommendedPlanItem != null && PlanConstants.LEARNING_PLAN_ITEM_ACCEPTED_STATE_KEY.equals(recommendedPlanItem.getStateKey()));
                    }
                    plannedCoursesList.add(plannedCourse);

                } else if (planItemInfo.getTypeKey().equals(planItemType) &&
                        PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(planItemInfo.getRefObjectType())) {
                    PlannedCourseDataObject plannedCourse = new PlannedCourseDataObject();
                    PlanItemDataObject planItemData = PlanItemDataObject.build(planItemInfo);
                    plannedCourse.setPlanItemDataObject(planItemData);
                    plannedCourse.setPlaceHolder(true);
                    if (planItemInfo.getDescr() != null && StringUtils.hasText(planItemInfo.getDescr().getPlain())) {
                        plannedCourse.setNote(planItemInfo.getDescr().getPlain());
                    }
                    plannedCourse.setPlaceHolderCode(planItemInfo.getRefObjectId());
                    plannedCourse.setPlaceHolderValue(getCoursePlaceHolderTitle(planItemInfo.getRefObjectId(), subjectAreas));
                    plannedCourse.setCourseDetails(new CourseSummaryDetails());
                    plannedCourse.setPlaceHolderCredit(planItemInfo.getCredit() == null ? "" : String.valueOf(planItemInfo.getCredit().intValue()));
                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItemInfo.getTypeKey())) {
                        plannedCourse.setAdviserName(getUserSessionHelper().getName(planItemInfo.getMeta().getCreateId()));
                        plannedCourse.setProposed(PlanConstants.LEARNING_PLAN_ITEM_PROPOSED_STATE_KEY.equals(planItemInfo.getStateKey()));
                    } else {
                        String atpId = planItemInfo.getPlanPeriods().get(0);
                        PlanItemInfo recommendedPlanItem = getPlanHelper().getPlanItemByAtpAndType(planItemInfo.getLearningPlanId(), planItemInfo.getRefObjectId(), atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, getPlanHelper().getCrossListedCourse(planItemInfo.getAttributes()));
                        plannedCourse.setAdviserRecommended(recommendedPlanItem != null && PlanConstants.LEARNING_PLAN_ITEM_ACCEPTED_STATE_KEY.equals(recommendedPlanItem.getStateKey()));
                    }
                    plannedCoursesList.add(plannedCourse);

                }
            }
            for (PlannedCourseDataObject plannedCourse : plannedCoursesList) {
                if (!plannedCourse.isPlaceHolder() && plannedCourse.getCourseDetails() != null) {
                    String key = generateKey(plannedCourse.getCourseDetails().getSubjectArea(), plannedCourse.getCourseDetails().getCourseNumber(), plannedCourse.getPlanItemDataObject().getAtp());
                    List<ActivityOfferingItem> activityOfferingItems = plannedSections.get(key);
                    if (activityOfferingItems != null && activityOfferingItems.size() > 0) {
                        Collections.sort(activityOfferingItems, new Comparator<ActivityOfferingItem>() {
                            @Override
                            public int compare(ActivityOfferingItem item1, ActivityOfferingItem item2) {
                                return item1.getCode().compareTo(item2.getCode());
                            }
                        });
                    }
                    List<String> publishedTerms = AtpHelper.getPublishedTermsForCampus(plannedCourse.getCourseDetails().getCampusCd());
                    boolean scheduled = AtpHelper.isCourseOfferedInTerm(plannedCourse.getPlanItemDataObject().getAtp(), plannedCourse.getCourseDetails().getCode());
                    boolean timeScheduleOpen = publishedTerms.contains(plannedCourse.getPlanItemDataObject().getAtp());
                    if (timeScheduleOpen) {
                        plannedCourse.setShowAlert(!scheduled);
                    }
                    plannedCourse.setTimeScheduleOpen(timeScheduleOpen);
                    List<String> statusAlerts = new ArrayList<String>();
                    if (timeScheduleOpen && !scheduled) {
                        statusAlerts.add(String.format(PlanConstants.COURSE_NOT_SCHEDULE_ALERT, plannedCourse.getCourseDetails().getCode(), plannedCourse.getPlanItemDataObject().getTermName()));
                        plannedCourse.setSectionsAvailable(false);
                    }
                    if (sectionsWithdrawn.containsKey(key)) {
                        List<String> sectionList = sectionsWithdrawn.get(key);
                        String[] sections = sectionList.toArray(new String[sectionList.size()]);
                        statusAlerts.add(String.format(PlanConstants.WITHDRAWN_ALERT, getCourseHelper().joinStringsByDelimiter(',', sections)));
                        plannedCourse.setShowAlert(true);
                    }
                    if (sectionsSuspended.containsKey(key)) {
                        List<String> sectionList = sectionsSuspended.get(key);
                        String[] sections = sectionList.toArray(new String[sectionList.size()]);
                        statusAlerts.add(String.format(PlanConstants.SUSPENDED_ALERT, getCourseHelper().joinStringsByDelimiter(',', sections)));
                        plannedCourse.setShowAlert(true);
                    }
                    if (!statusAlerts.isEmpty()) {
                        plannedCourse.setStatusAlerts(statusAlerts);
                    }
                    plannedCourse.setPlanActivities(activityOfferingItems);
                }
            }
        }
        return plannedCoursesList;
    }

    /**
     * returns for COM 2xx ---> Communication 200 level
     *
     * @param coursePlaceHolder
     * @param subjectAreas
     * @return
     */
    private String getCoursePlaceHolderTitle(String coursePlaceHolder, Map<String, String> subjectAreas) {
        DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(coursePlaceHolder);
        String subjectTitle = subjectAreas.get(courseCode.getSubject());
        String subjectLevel = courseCode.getNumber().toUpperCase().replace(CourseSearchConstants.COURSE_LEVEL_XX, CourseSearchConstants.COURSE_LEVEL_ZERO);
        return String.format("%s %s level", subjectTitle, subjectLevel);
    }

    /**
     * returns key(eg: COM=240=kuali.uw.atp.2013.4)
     *
     * @param subject
     * @param number
     * @param atpId
     * @return
     */
    private String generateKey(String subject, String number, String atpId) {
        return getCourseHelper().joinStringsByDelimiter('=', subject, number, atpId);
    }

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = UwMyplanServiceLocator.getInstance().getCourseHelper();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    public synchronized CourseDetailsInquiryHelperImpl getCourseDetailsInquiryHelper() {
        if (this.courseDetailsInquiryHelper == null) {
            this.courseDetailsInquiryHelper = new CourseDetailsInquiryHelperImpl();
        }
        return courseDetailsInquiryHelper;
    }

    public void setCourseDetailsInquiryHelper(CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper) {
        this.courseDetailsInquiryHelper = courseDetailsInquiryHelper;
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

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
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
}
