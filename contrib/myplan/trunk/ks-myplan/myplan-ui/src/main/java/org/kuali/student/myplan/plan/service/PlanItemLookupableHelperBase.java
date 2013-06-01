package org.kuali.student.myplan.plan.service;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.student.myplan.course.util.CourseSearchConstants.CONTEXT_INFO;
import static org.kuali.student.myplan.plan.PlanConstants.LEARNING_PLAN_TYPE_PLAN;

/**
 * Base lookup helper for plan items.
 */
public class PlanItemLookupableHelperBase extends MyPlanLookupableImpl {
    private final Logger logger = Logger.getLogger(PlanItemLookupableHelperBase.class);
    private transient AcademicPlanService academicPlanService;
    private transient CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper;
    private transient CourseOfferingService courseOfferingService;
    @Autowired
    private CourseHelper courseHelper;

    protected List<PlannedCourseDataObject> getPlanItems(String planItemType, String studentId)
            throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {

        List<PlannedCourseDataObject> plannedCourseList = new ArrayList<PlannedCourseDataObject>();

        AcademicPlanService academicPlanService = getAcademicPlanService();

        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentId, LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);
        for (LearningPlanInfo learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemInfoList = academicPlanService.getPlanItemsInPlan(learningPlanID, CONTEXT_INFO);
            Map<String, List<ActivityOfferingItem>> plannedSections = new HashMap<String, List<ActivityOfferingItem>>();
            Map<String, List<String>> sectionsWithdrawn = new HashMap<String, List<String>>();
            Map<String, List<String>> sectionsSuspended = new HashMap<String, List<String>>();
            for (PlanItemInfo planItemInfo : planItemInfoList) {
                populatePlannedCourseList(planItemInfo, planItemType, plannedCourseList, plannedSections, sectionsSuspended, sectionsWithdrawn);
            }
            if (!planItemType.equalsIgnoreCase(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
                addActivitiesToPlannedCourseList(plannedCourseList, plannedSections, sectionsSuspended, sectionsWithdrawn);
            }
        }
        return plannedCourseList;
    }

    /**
     * returns the list of plannedCoursesDataObjects starting from the given atp
     *
     * @param planItemType
     * @param studentId
     * @param startAtp
     * @return
     * @throws InvalidParameterException
     * @throws MissingParameterException
     * @throws DoesNotExistException
     * @throws OperationFailedException
     */
    public List<PlannedCourseDataObject> getPlannedCoursesFromAtp(String planItemType, String studentId, String startAtp) throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {
        List<PlannedCourseDataObject> plannedCourseList = new ArrayList<PlannedCourseDataObject>();
        AcademicPlanService academicPlanService = getAcademicPlanService();
        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentId, LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);
        for (LearningPlanInfo learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemInfoList = academicPlanService.getPlanItemsInPlan(learningPlanID, CONTEXT_INFO);
            Map<String, List<ActivityOfferingItem>> plannedSections = new HashMap<String, List<ActivityOfferingItem>>();
            Map<String, List<String>> sectionsWithdrawn = new HashMap<String, List<String>>();
            Map<String, List<String>> sectionsSuspended = new HashMap<String, List<String>>();
            for (PlanItemInfo planItemInfo : planItemInfoList) {
                if (planItemInfo.getTypeKey().equalsIgnoreCase(planItemType) && planItemInfo.getPlanPeriods().get(0).compareTo(startAtp) >= 0) {
                    populatePlannedCourseList(planItemInfo, planItemType, plannedCourseList, plannedSections, sectionsSuspended, sectionsWithdrawn);
                }
            }
            addActivitiesToPlannedCourseList(plannedCourseList, plannedSections, sectionsSuspended, sectionsWithdrawn);
        }
        return plannedCourseList;
    }

    /**
     * Adds activities for each plannedCourseDataObject if present
     *
     * @param plannedCourseList
     * @param plannedSections
     */
    private void addActivitiesToPlannedCourseList(List<PlannedCourseDataObject> plannedCourseList, Map<String, List<ActivityOfferingItem>> plannedSections, Map<String, List<String>> sectionsSuspended, Map<String, List<String>> sectionsWithdrawn) {
        List<String> publishedTerms = AtpHelper.getPublishedTerms();
        for (PlannedCourseDataObject plannedCourse : plannedCourseList) {
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

    /**
     * populates the plannedCoursesLit for given params
     *
     * @param planItemInfo
     * @param planItemType
     * @param plannedCourseList
     * @param plannedSections
     */
    private void populatePlannedCourseList(PlanItemInfo planItemInfo, String planItemType, List<PlannedCourseDataObject> plannedCourseList, Map<String, List<ActivityOfferingItem>> plannedSections, Map<String, List<String>> sectionsSuspended, Map<String, List<String>> sectionsWithdrawn) {
        String courseID = planItemInfo.getRefObjectId();
        //  Only create a data object for the specified type.
        if (planItemInfo.getTypeKey().equals(planItemType) && planItemInfo.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {

            PlannedCourseDataObject plannedCourse = new PlannedCourseDataObject();
            PlanItemDataObject planItemData = PlanItemDataObject.build(planItemInfo);
            plannedCourse.setPlanItemDataObject(planItemData);

            //  If the course info lookup fails just log the error and omit the item.
            try {
                if (getCourseDetailsInquiryHelper().isCourseIdValid(courseID)) {
                    CourseSummaryDetails courseDetails = getCourseDetailsInquiryHelper().retrieveCourseSummaryById(courseID);
                    plannedCourse.setCourseDetails(courseDetails);
                    plannedCourseList.add(plannedCourse);
                }
            } catch (Exception e) {
                logger.error(String.format("Unable to retrieve course info for plan item [%s].", planItemInfo.getId()), e);
            }


        } else if (!planItemType.equalsIgnoreCase(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST) && planItemInfo.getRefObjectType().equalsIgnoreCase(PlanConstants.SECTION_TYPE)) {
            List<String> planPeriods = planItemInfo.getPlanPeriods();
            String termId = !planPeriods.isEmpty() ? planPeriods.get(0) : null;
            if (null != termId && AtpHelper.isAtpSetToPlanning(termId)) {
                List<ActivityOfferingItem> activityOfferingItems = new ArrayList<ActivityOfferingItem>();

                String activityOfferingId = planItemInfo.getRefObjectId();
                ActivityOfferingDisplayInfo activityDisplayInfo = null;
                try {
                    activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityOfferingId, PlanConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error("Could not retrieve ActivityOffering data for" + activityOfferingId, e);
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
                    }

                    ActivityOfferingItem activityOfferingItem = getCourseDetailsInquiryHelper().getActivityItem(activityDisplayInfo, courseOfferingInfo, !AtpHelper.isAtpSetToPlanning(termId), termId, planItemInfo.getId());
                    activityOfferingItems.add(activityOfferingItem);
                    String key = generateKey(courseOfferingInfo.getSubjectArea(), courseOfferingInfo.getCourseNumberSuffix(), termId);
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


    /**
     * Override and ignore criteria validation
     *
     * @param form
     * @param searchCriteria
     * @return
     */
    @Override
    public boolean validateSearchParameters(LookupForm form, Map<String, String> searchCriteria) {
        return true;
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

    public synchronized CourseDetailsInquiryHelperImpl getCourseDetailsInquiryHelper() {
        if (this.courseDetailsInquiryHelper == null) {
            this.courseDetailsInquiryHelper = new CourseDetailsInquiryHelperImpl();
        }
        return courseDetailsInquiryHelper;
    }

    public void setCourseDetailsInquiryHelper(CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper) {
        this.courseDetailsInquiryHelper = courseDetailsInquiryHelper;
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

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }
}
