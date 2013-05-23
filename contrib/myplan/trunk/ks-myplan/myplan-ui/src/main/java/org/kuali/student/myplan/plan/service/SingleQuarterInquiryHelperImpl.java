package org.kuali.student.myplan.plan.service;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
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
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.*;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
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
public class SingleQuarterInquiryHelperImpl extends KualiInquirableImpl {

    private final Logger logger = Logger.getLogger(SingleQuarterInquiryHelperImpl.class);

    private transient AcademicRecordService academicRecordService;

    private transient AcademicPlanService academicPlanService;

    private transient CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper;

    private transient CourseOfferingService courseOfferingService;

    private transient CourseHelper courseHelper;

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
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


    @Override
    public PlannedTerm retrieveDataObject(Map fieldValues) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String termAtpId = request.getParameter("term_atp_id");
        String studentId = UserSessionHelper.getStudentRegId();
        boolean isServiceStatusOK = true;
        /*Setting the Warning message if isServiceStatusOK is false*/
        ServicesStatusDataObject servicesStatusDataObject = (ServicesStatusDataObject) request.getSession().getAttribute(CourseSearchConstants.SWS_SERVICES_STATUS);
        if (!servicesStatusDataObject.isAcademicCalendarServiceUp() || !servicesStatusDataObject.isAcademicRecordServiceUp()) {
            isServiceStatusOK = false;
            AtpHelper.addServiceError("qtrYear");
        }
        String[] params = {};
        if (!isServiceStatusOK) {
            GlobalVariables.getMessageMap().putWarningForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.ERROR_TECHNICAL_PROBLEMS, params);
        }

        /*************PlannedCourseList**************/
        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();
        if (isServiceStatusOK) {
            try {
                plannedCoursesList = getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, studentId, termAtpId);
            } catch (Exception e) {
                logger.error("Could not load plannedCourseslist", e);

            }
        }
        /****academic record SWS call to get the studentCourseRecordInfo list *****/
        List<StudentCourseRecordInfo> studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();
        if (isServiceStatusOK) {
            try {
                studentCourseRecordInfos = getAcademicRecordService().getCompletedCourseRecords(studentId, PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                GlobalVariables.getMessageMap().putWarningForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.ERROR_TECHNICAL_PROBLEMS, params);
                logger.error("Could not retrieve StudentCourseRecordInfo from the SWS.", e);
            }
        }

        /*************BackupCourseList**************/
        List<PlannedCourseDataObject> backupCoursesList = new ArrayList<PlannedCourseDataObject>();
        if (isServiceStatusOK) {
            try {
                backupCoursesList = getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP, studentId, termAtpId);
            } catch (Exception e) {
                logger.error("Could not load backupCourseList", e);

            }
        }

        PlannedTerm perfectPlannedTerm = SingleQuarterHelperBase.populatePlannedTerms(plannedCoursesList, backupCoursesList, studentCourseRecordInfos, termAtpId, isServiceStatusOK);
        return perfectPlannedTerm;
    }

    protected List<PlannedCourseDataObject> getPlanItemListByTermId(String planItemType, String studentId, String termId)
            throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {

        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();

        AcademicPlanService academicPlanService = getAcademicPlanService();
        ContextInfo context = CourseSearchConstants.CONTEXT_INFO;

        String planTypeKey = PlanConstants.LEARNING_PLAN_TYPE_PLAN;

        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentId, planTypeKey, CourseSearchConstants.CONTEXT_INFO);
        for (LearningPlanInfo learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemList = academicPlanService.getPlanItemsInPlan(learningPlanID, context);
            Map<String, List<String>> sectionsWithdrawn = new HashMap<String, List<String>>();
            Map<String, List<String>> sectionsSuspended = new HashMap<String, List<String>>();
            Map<String, List<ActivityOfferingItem>> plannedSections = new HashMap<String, List<ActivityOfferingItem>>();
            for (PlanItemInfo planItem : planItemList) {
                if (planItem.getPlanPeriods() != null && planItem.getPlanPeriods().size() > 0 && planItem.getPlanPeriods().get(0).equalsIgnoreCase(termId) && planItem.getTypeKey().equalsIgnoreCase(planItemType) && planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
                    PlannedCourseDataObject plannedCourseDO = new PlannedCourseDataObject();
                    String courseID = planItem.getRefObjectId();
                    //  Only create a data object for the specified type.
                    if (planItem.getTypeKey().equals(planItemType)) {

                        plannedCourseDO.setPlanItemDataObject(PlanItemDataObject.build(planItem));

                        //  If the course info lookup fails just log the error and omit the item.
                        try {

                            plannedCourseDO.setCourseDetails(getCourseDetailsInquiryHelper().retrieveCourseSummaryById(courseID));

                            // TODO: Add Plan activities to this view

                        } catch (Exception e) {
                            logger.error(String.format("Unable to retrieve course info for plan item [%s].", planItem.getId()), e);
                            continue;
                        }

                        plannedCoursesList.add(plannedCourseDO);
                    }
                } else if (planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.SECTION_TYPE)) {
                    List<String> planPeriods = planItem.getPlanPeriods();
                    String term = !planPeriods.isEmpty() ? planPeriods.get(0) : null;
                    if (null != term && !AtpHelper.isAtpCompletedTerm(term)) {
                        List<ActivityOfferingItem> activityOfferingItems = new ArrayList<ActivityOfferingItem>();
                        String activityOfferingId = planItem.getRefObjectId();
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

                            ActivityOfferingItem activityOfferingItem = getCourseDetailsInquiryHelper().getActivityItem(activityDisplayInfo, courseOfferingInfo, !AtpHelper.isAtpSetToPlanning(term), term, planItem.getId());
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


                }
            }
            for (PlannedCourseDataObject plannedCourse : plannedCoursesList) {
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
                List<String> publishedTerms = AtpHelper.getPublishedTerms();
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
        return plannedCoursesList;
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

}
