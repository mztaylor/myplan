package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.utils.AcademicRecordHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.io.IOException;
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
@Component
public class SingleQuarterInquiryHelperImpl extends KualiInquirableImpl {

    private final Logger logger = Logger.getLogger(SingleQuarterInquiryHelperImpl.class);

    private transient AcademicRecordService academicRecordService;

    private transient AcademicPlanService academicPlanService;

    private AcademicRecordHelper academicRecordHelper;


    private CourseHelper courseHelper;


    private UserSessionHelper userSessionHelper;


    private PlanHelper planHelper;


    @Override
    public PlannedTerm retrieveDataObject(Map fieldValues) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String termAtpId = request.getParameter("term_atp_id");
        String studentId = getUserSessionHelper().getStudentId();


        /*************PlannedCourseList**************/
        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();

        try {
            plannedCoursesList = getPlanHelper().getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, studentId, termAtpId);
        } catch (Exception e) {
            logger.error("Could not load plannedCourseslist", e);

        }

        /****academic record SWS call to get the studentCourseRecordInfo list *****/
        List<StudentCourseRecordInfo> studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();

        try {
            studentCourseRecordInfos = getAcademicRecordHelper().getCompletedCourseRecordsForStudents(studentId);
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
            backupCoursesList = getPlanHelper().getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP, studentId, termAtpId);
        } catch (Exception e) {
            logger.error("Could not load backupCourseList", e);

        }

        /*************RecommendedCourseList**************/
        List<PlannedCourseDataObject> recommendedCoursesList = new ArrayList<PlannedCourseDataObject>();

        try {
            recommendedCoursesList = getPlanHelper().getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, studentId, termAtpId);
        } catch (Exception e) {
            logger.error("Could not load recommendedCourseList", e);

        }


        PlannedTerm perfectPlannedTerm = SingleQuarterHelperBase.populatePlannedTerms(plannedCoursesList, backupCoursesList, recommendedCoursesList, studentCourseRecordInfos, termAtpId);

        Map<String, String> plannedItems = getPlanHelper().getPlanItemIdAndRefObjIdByRefObjType(perfectPlannedTerm.getLearningPlanId(), PlanConstants.SECTION_TYPE, perfectPlannedTerm.getAtpId());

        if (perfectPlannedTerm != null && !CollectionUtils.isEmpty(plannedItems)) {
            /*Creating json string which has the planned activities associated to their planItemId.. Used in SB UI for ability to delete planned activities*/
            ObjectMapper mapper = new ObjectMapper();
            try {
                perfectPlannedTerm.setPlannedActivities(mapper.writeValueAsString(plannedItems));
            } catch (IOException e) {
                logger.error("Could not build planned Activities json string", e);
            }
        }

        return perfectPlannedTerm;
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

    public AcademicRecordHelper getAcademicRecordHelper() {
        if (academicRecordHelper == null) {
            academicRecordHelper = UwMyplanServiceLocator.getInstance().getAcademicRecordHelper();
        }
        return academicRecordHelper;
    }

    public void setAcademicRecordHelper(AcademicRecordHelper academicRecordHelper) {
        this.academicRecordHelper = academicRecordHelper;
    }
}
