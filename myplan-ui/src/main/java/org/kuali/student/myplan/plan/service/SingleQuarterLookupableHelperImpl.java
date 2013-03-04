package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 1/25/13
 * Time: 11:14 AM
 * To change this template use File | Settings | File Templates.
 */
public class SingleQuarterLookupableHelperImpl extends PlanItemLookupableHelperBase {

    private final Logger logger = Logger.getLogger(SingleQuarterLookupableHelperImpl.class);

    private transient AcademicRecordService academicRecordService;

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


    @Override
    protected List<PlannedTerm> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String termAtpId = request.getParameter("term_atp_id");
        String studentId = UserSessionHelper.getStudentId();
        boolean isServiceStatusOK = true;
        /*Setting the Warning message if isServiceStatusOK is false*/
        if (!Boolean.valueOf(request.getAttribute(CourseSearchConstants.IS_ACADEMIC_CALENDER_SERVICE_UP).toString())
                || !Boolean.valueOf(request.getAttribute(CourseSearchConstants.IS_ACADEMIC_RECORD_SERVICE_UP).toString())) {
            isServiceStatusOK = false;
            AtpHelper.addServiceError("qtrYear");
        }
        String[] params = {};
        if (!isServiceStatusOK) {
            GlobalVariables.getMessageMap().putWarningForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.ERROR_TECHNICAL_PROBLEMS, params);
        }
        List<String> publishedTerms = AtpHelper.getPublishedTerms();

        /*************PlannedCourseList**************/
        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();
        if (isServiceStatusOK) {
            try {
                plannedCoursesList = getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, studentId, termAtpId);
            } catch (Exception e) {
                logger.error("Could not load plannedCourseslist", e);

            }
            for (PlannedCourseDataObject pl : plannedCoursesList) {
                pl.setShowAlert(!AtpHelper.isCourseOfferedInTerm(pl.getPlanItemDataObject().getAtp(), pl.getCourseDetails().getCode()));
                pl.setTimeScheduleOpen(publishedTerms.contains(pl.getPlanItemDataObject().getAtp()));
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
            for (PlannedCourseDataObject pl : backupCoursesList) {
                pl.setShowAlert(!AtpHelper.isCourseOfferedInTerm(pl.getPlanItemDataObject().getAtp(), pl.getCourseDetails().getCode()));
                pl.setTimeScheduleOpen(publishedTerms.contains(pl.getPlanItemDataObject().getAtp()));
            }
        }

        List<PlannedTerm> perfectPlannedTerms = SingleQuarterHelperBase.populatePlannedTerms(plannedCoursesList, backupCoursesList, studentCourseRecordInfos, termAtpId, isServiceStatusOK);
        return perfectPlannedTerms;
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

            for (PlanItemInfo planItem : planItemList) {
                if (planItem.getPlanPeriods() != null && planItem.getPlanPeriods().size() > 0 && planItem.getPlanPeriods().get(0).equalsIgnoreCase(termId) && planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
                    PlannedCourseDataObject plannedCourseDO = new PlannedCourseDataObject();
                    String courseID = planItem.getRefObjectId();
                    //  Only create a data object for the specified type.
                    if (planItem.getTypeKey().equals(planItemType)) {

                        plannedCourseDO.setPlanItemDataObject(PlanItemDataObject.build(planItem));

                        //  If the course info lookup fails just log the error and omit the item.
                        try {

                            plannedCourseDO.setCourseDetails( getCourseDetailsInquiryHelper().retrieveCourseSummaryById(courseID) );

                            // TODO: Add Plan activities to this view

                        } catch (Exception e) {
                            logger.error(String.format("Unable to retrieve course info for plan item [%s].", planItem.getId()), e);
                            continue;
                        }

                        plannedCoursesList.add(plannedCourseDO);
                    }
                }
            }
        }
        return plannedCoursesList;
    }

}
