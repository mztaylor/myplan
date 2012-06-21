package org.kuali.student.myplan.plan.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;

import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryViewHelperServiceImpl;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Base lookup helper for plan items.
 */
public class PlanItemLookupableHelperBase extends MyPlanLookupableImpl {
    private final Logger logger = Logger.getLogger(PlanItemLookupableHelperBase.class);
    private transient AcademicPlanService academicPlanService;
    private transient CourseDetailsInquiryViewHelperServiceImpl courseDetailsInquiryService;

    protected List<PlannedCourseDataObject> getPlanItems(String planItemType, boolean loadSummaryInfoOnly, String studentId)
            throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {

        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();

        AcademicPlanService academicPlanService = getAcademicPlanService();
        ContextInfo context = CourseSearchConstants.CONTEXT_INFO;

        String planTypeKey = PlanConstants.LEARNING_PLAN_TYPE_PLAN;

        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentId, planTypeKey, CourseSearchConstants.CONTEXT_INFO);
        for (LearningPlanInfo learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemList = academicPlanService.getPlanItemsInPlan(learningPlanID, context);

            for (PlanItemInfo planItem : planItemList) {
                PlannedCourseDataObject plannedCourseDO = new PlannedCourseDataObject();
                String courseID = planItem.getRefObjectId();
                //  Only create a data object for the specified type.
                if (planItem.getTypeKey().equals(planItemType)) {

                    plannedCourseDO.setPlanItemDataObject(PlanItemDataObject.build(planItem));

                    //  If the course info lookup fails just log the error and omit the item.
                    try {
                        if (loadSummaryInfoOnly) {
                            plannedCourseDO.setCourseDetails(getCourseDetailsInquiryService().retrieveCourseSummary(courseID, studentId));
                        } else {
                            plannedCourseDO.setCourseDetails(getCourseDetailsInquiryService().retrieveCourseDetails(courseID, studentId));
                        }
                    } catch (Exception e) {
                        logger.error(String.format("Unable to retrieve course info for plan item [%s].", planItem.getId()), e);
                        continue;
                    }

                    plannedCoursesList.add(plannedCourseDO);
                }
            }
        }
        return plannedCoursesList;
    }


    /**
     * Override and ignore criteria validation
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

    public synchronized CourseDetailsInquiryViewHelperServiceImpl getCourseDetailsInquiryService() {
        if (this.courseDetailsInquiryService == null) {
            this.courseDetailsInquiryService = new CourseDetailsInquiryViewHelperServiceImpl();
        }
        return courseDetailsInquiryService;
    }

    public void setCourseDetailsInquiryService(CourseDetailsInquiryViewHelperServiceImpl courseDetailsInquiryService) {
        this.courseDetailsInquiryService = courseDetailsInquiryService;
    }
}
