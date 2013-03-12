package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.kuali.student.myplan.course.util.CourseSearchConstants.CONTEXT_INFO;
import static org.kuali.student.myplan.plan.PlanConstants.LEARNING_PLAN_TYPE_PLAN;

/**
 * Base lookup helper for plan items.
 */
public class PlanItemLookupableHelperBase extends MyPlanLookupableImpl {
    private final Logger logger = Logger.getLogger(PlanItemLookupableHelperBase.class);
    private transient AcademicPlanService academicPlanService;
    private transient CourseDetailsInquiryHelperImpl courseDetailsInquiryHelper;

    protected List<PlannedCourseDataObject> getPlanItems(String planItemType, String studentId)
            throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {

        List<PlannedCourseDataObject> plannedCourseList = new ArrayList<PlannedCourseDataObject>();

        AcademicPlanService academicPlanService = getAcademicPlanService();

        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentId, LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);
        for (LearningPlanInfo learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemInfoList = academicPlanService.getPlanItemsInPlan(learningPlanID, CONTEXT_INFO);

            for (PlanItemInfo planItemInfo : planItemInfoList) {
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
                            List<String> planPeriods = planItemInfo.getPlanPeriods();
                            if (!planPeriods.isEmpty()) {
                                String term = planPeriods.get(0);
                                String courseId2 = plannedCourse.getCourseDetails().getCourseId();
                                List<ActivityOfferingItem> plannedSections = getPlannedSections(courseId2, term);
                                plannedCourse.setPlanActivities(plannedSections);
                            }
                        }
                    } catch (Exception e) {
                        logger.error(String.format("Unable to retrieve course info for plan item [%s].", planItemInfo.getId()), e);
                        continue;
                    }

                    plannedCourseList.add(plannedCourse);
                }
            }
        }
        return plannedCourseList;
    }

    /*Used to get the planned sections for a coursId and term excluding the completed terms*/
    private List<ActivityOfferingItem> getPlannedSections(String courseId, String term) {
        List<ActivityOfferingItem> sectionsPlanned = new ArrayList<ActivityOfferingItem>();
        if (!AtpHelper.isAtpCompletedTerm(term)) {
            List<ActivityOfferingItem> activityOfferingItems = getCourseDetailsInquiryHelper().getActivityOfferingItemsById(courseId, term);
            for (ActivityOfferingItem activityOfferingItem : activityOfferingItems) {
                if (activityOfferingItem.getPlanItemId() != null) {
                    sectionsPlanned.add(activityOfferingItem);
                }
            }
        }
        return sectionsPlanned;
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
}
