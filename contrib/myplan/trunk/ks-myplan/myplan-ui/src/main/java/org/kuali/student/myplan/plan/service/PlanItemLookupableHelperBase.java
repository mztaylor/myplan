package org.kuali.student.myplan.plan.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.lookup.LookupableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryViewHelperServiceImpl;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Base lookup helper for plan items.
 */
public class PlanItemLookupableHelperBase  extends LookupableImpl {

    private transient AcademicPlanService academicPlanService;
    private transient CourseDetailsInquiryViewHelperServiceImpl courseDetailsInquiryService;


    protected List<PlanItemDataObject> getPlanItems(String planItemType)
            throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {

        List<PlanItemDataObject> plannedCoursesList = new ArrayList<PlanItemDataObject>();

        AcademicPlanService academicPlanService = getAcademicPlanService();

        Person user = GlobalVariables.getUserSession().getPerson();

        ContextInfo context = CourseSearchConstants.CONTEXT_INFO;

        String studentID = user.getPrincipalId();

        String planTypeKey = PlanConstants.LEARNING_PLAN_TYPE_PLAN;

        List<LearningPlanInfo> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentID, planTypeKey, CourseSearchConstants.CONTEXT_INFO);
        for (LearningPlanInfo learningPlan : learningPlanList) {
            String learningPlanID = learningPlan.getId();
            List<PlanItemInfo> planItemList = academicPlanService.getPlanItemsInPlan(learningPlanID, context);

            for (PlanItemInfo planItem : planItemList) {
                String courseID = planItem.getRefObjectId();
                //  Only create a data object for the specified type.
                if (planItem.getTypeKey().equals(planItemType)) {
                    PlanItemDataObject item = new PlanItemDataObject();
                    item.setId(planItem.getId());
                    item.setCourseDetails(getCourseDetailsInquiryService().retrieveCourseDetails(courseID));
                    item.setDateAdded(planItem.getMeta().getCreateTime());
                    //  Only load ATP info for planned courses.
                    if (planItemType.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
                        item.setAtpIds(planItem.getPlanPeriods());
                    }
                    plannedCoursesList.add(item);
                }
            }
        }
        return plannedCoursesList;
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
        if(this.courseDetailsInquiryService == null) {
            this.courseDetailsInquiryService =  new CourseDetailsInquiryViewHelperServiceImpl();
        }
        return courseDetailsInquiryService;
    }

    public void setCourseDetailsInquiryService(CourseDetailsInquiryViewHelperServiceImpl courseDetailsInquiryService) {
        this.courseDetailsInquiryService = courseDetailsInquiryService;
    }
}
