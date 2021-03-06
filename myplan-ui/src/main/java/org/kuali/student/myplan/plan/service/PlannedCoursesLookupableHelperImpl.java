package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.utils.AcademicRecordHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import org.kuali.rice.kim.api.identity.Person;

/**
 * Produce a list of planned course items.
 */
@Component
public class PlannedCoursesLookupableHelperImpl extends PlanItemLookupableHelperBase {

    private final Logger logger = Logger.getLogger(PlannedCoursesLookupableHelperImpl.class);

    private transient AcademicRecordService academicRecordService;


    private UserSessionHelper userSessionHelper;

    private AcademicRecordHelper academicRecordHelper;

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

    /**
     * Skip the validation so that we use the criteriaFields param to pass in args to the getSearchResults method.
     *
     * @param form
     * @param searchCriteria
     * @return
     */
    @Override
    public boolean validateSearchParameters(LookupForm form, Map<String, String> searchCriteria) {
        return true;
    }

    @Override
    protected List<PlannedTerm> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String focusAtpId = request.getParameter(PlanConstants.FOCUS_ATP_ID_KEY);
        String studentId = getUserSessionHelper().getStudentId();
        /*************PlannedCourseList**************/
        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();
        try {
            plannedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, studentId, true);
        } catch (Exception e) {
            logger.error("Could not load plannedCourseslist", e);

        }

        /****academic record SWS call to get the studentCourseRecordInfo list *****/
        List<StudentCourseRecordInfo> studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();

        try {
            studentCourseRecordInfos = getAcademicRecordHelper().getCompletedCourseRecordsForStudents(studentId);
        } catch (OperationFailedException ofe) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS due to OperationFailedException.", ofe);
            GlobalVariables.getMessageMap().putWarningForSectionId(PlanConstants.PLAN_COMPONENT_ID,
                     PlanConstants.ERROR_ACA_RECORD_SWS_PROBLEMS);
        } catch (Exception e) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS.", e);
        }


        /*************BackupCourseList**************/
        List<PlannedCourseDataObject> backupCoursesList = new ArrayList<PlannedCourseDataObject>();

        try {
            backupCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP, studentId, true);
        } catch (Exception e) {
            logger.error("Could not load backupCourseList", e);

        }

        /*************RecommendedCourseList**************/
        List<PlannedCourseDataObject> recommendedCoursesList = new ArrayList<PlannedCourseDataObject>();

        try {
            recommendedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, studentId, true);
        } catch (Exception e) {
            logger.error("Could not load recommendedCourseList", e);

        }


        List<PlannedTerm> perfectPlannedTerms = PlannedTermsHelperBase.populatePlannedTerms(plannedCoursesList, backupCoursesList, recommendedCoursesList, studentCourseRecordInfos, focusAtpId, 6, false);
        return perfectPlannedTerms;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper =  UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
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
