package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.FullPlanItemsDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.AtpHelper.YearTerm;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//import org.kuali.rice.kim.api.identity.Person;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/13/12
 * Time: 1:49 PM
 * To change this template use File | Settings | File Templates.
 */
@Component
public class FullPlanItemsLookupableHelperImpl extends PlanItemLookupableHelperBase {

    private final Logger logger = Logger.getLogger(FullPlanItemsLookupableHelperImpl.class);

    private transient AcademicRecordService academicRecordService;

    @Autowired
    private UserSessionHelper userSessionHelper;

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
    protected List<FullPlanItemsDataObject> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {


        String studentId = getUserSessionHelper().getStudentId();
        /*************PlannedCourseList**************/
        List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();
        try {
            plannedCoursesList = getPlannedCoursesFromAtp(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, studentId, AtpHelper.getFirstPlanTerm(), true);
        } catch (Exception e) {
            logger.error("Could not load plannedCourseslist", e);

        }
        /****academic record SWS call to get the studentCourseRecordInfo list *****/

        List<StudentCourseRecordInfo> studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();
        try {
            studentCourseRecordInfos = getAcademicRecordService().getCompletedCourseRecords(studentId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS.", e);
        }


        List<PlannedTerm> perfectPlannedTerms = PlannedTermsHelperBase.populatePlannedTerms(plannedCoursesList, null, null, studentCourseRecordInfos, null, 1, true);
        List<FullPlanItemsDataObject> fullPlanItemsDataObjectList = new ArrayList<FullPlanItemsDataObject>();
        int size = perfectPlannedTerms.size();
        for (int i = 0; size > 0; i++) {
            FullPlanItemsDataObject fullPlanItemsDataObject = new FullPlanItemsDataObject();
            List<PlannedTerm> plannedTermList = new ArrayList<PlannedTerm>();
            boolean currentYear = false;

            for (int j = 0; j < AtpHelper.TERM_COUNT; j++) {
                plannedTermList.add(perfectPlannedTerms.get(0));
                if (perfectPlannedTerms.get(0).isCurrentTermForView()) {
                    currentYear = true;
                }
                perfectPlannedTerms.remove(perfectPlannedTerms.get(0));
                size--;
            }

            YearTerm minYear = AtpHelper.atpToYearTerm(plannedTermList.get(0).getAtpId());
            YearTerm maxYear = AtpHelper.atpToYearTerm(plannedTermList.get(plannedTermList.size() - 1).getAtpId());
            StringBuffer yearRange = new StringBuffer();
            yearRange = yearRange.append(minYear.getYearAsString()).append(" - ").append(maxYear.getYearAsString());
            fullPlanItemsDataObject.setYearRange(yearRange.toString());
            fullPlanItemsDataObject.setTerms(plannedTermList);
            fullPlanItemsDataObjectList.add(fullPlanItemsDataObject);
            fullPlanItemsDataObject.setCurrentYear(currentYear);
            fullPlanItemsDataObject.setHasNote(doesPlanItemNoteExist(plannedTermList));
        }
        return fullPlanItemsDataObjectList;
    }

    /**
     * Used to know if any of the planned Terms items has a note (Needed for UI rendering)
     *
     * @param plannedTerms
     * @return
     */
    private boolean doesPlanItemNoteExist(List<PlannedTerm> plannedTerms) {
        for (PlannedTerm plannedTerm : plannedTerms) {
            for (PlannedCourseDataObject plannedCourseDataObject : plannedTerm.getPlannedList()) {
                if (StringUtils.hasText(plannedCourseDataObject.getNote())) {
                    return true;
                }
            }
        }
        return false;
    }

    public UserSessionHelper getUserSessionHelper() {
        if(userSessionHelper == null){
            userSessionHelper =  UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}
