package org.kuali.student.myplan.course.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.document.MaintenanceDocument;
import org.kuali.rice.krad.maintenance.MaintainableImpl;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.web.form.MaintenanceForm;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.dataobject.SavedCoursesItem;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.util.constants.AtpServiceConstants;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.util.Map;

/**
 * Add, change, and delete plan items.
 *
 * http://localhost:8080/myplan-embedded-dev/myplan/maintenance?viewTypeName=MAINTENANCE&returnLocation=http://localhost:8080/myplan-embedded-dev&methodToCall=maintenanceEdit&dataObjectClassName=org.kuali.student.myplan.course.dataobject.SavedCoursesItem&id=1#
 *
 */
public class SavedCoursesMaintainableImpl extends MaintainableImpl {

    private transient AcademicPlanService academicPlanService;

    private static final String PLAN_ITEM_ID_PARAM_NAME = "planItemId";
    private static final String COURSE_ID = "courseId";

    private static final String COURSE_TYPE = LUConstants.CLU_TYPE_CREDIT_COURSE;

    @Override
    public void saveDataObject() {

        SavedCoursesItem planItem = (SavedCoursesItem) getDataObject();
        if (getMaintenanceAction().equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION)) {

        } else if (getMaintenanceAction().equalsIgnoreCase(KRADConstants.MAINTENANCE_DELETE_ACTION)) {

        } else {
            logAndThrowRuntime(String.format("Unknown maintenance action [%s].", getMaintenanceAction()));
        }

//        TermInfo termInfo = (TermInfo)getDataObject();
//
//        termInfo.setStateKey(AtpServiceConstants.ATP_OFFICIAL_STATE_KEY);
//
//        try{
//        	if(getMaintenanceAction().equals(KRADConstants.MAINTENANCE_NEW_ACTION) ||
//                getMaintenanceAction().equals(KRADConstants.MAINTENANCE_COPY_ACTION)) {
//        		termInfo = getAcademicCalendarService().createTerm(termInfo.getTypeKey(), termInfo, ContextInfo.newInstance());
//        	}
//        	else {
//        		termInfo = getAcademicCalendarService().updateTerm(termInfo.getId(), termInfo, ContextInfo.newInstance());
//        	}
//        } catch (Exception e) {
//
//        }
    }

    @Override
    public Object retrieveObjectForEditOrCopy(MaintenanceDocument document, Map<String, String> dataObjectKeys) {
        ContextInfo context = new ContextInfo();

        String planId = dataObjectKeys.get(PLAN_ITEM_ID_PARAM_NAME) ;

        LearningPlan plan = null;
//        try {
//            plan = getAcademicPlanService().getLearningPlan(planId, context);
//        } catch (DoesNotExistException e) {
//            logAndThrowRuntime(String.format("Unknown plan id [%s].", planId));
//        } catch (Exception e) {
//            throw new RuntimeException(String.format("Could not retrieve plan [%s].", planId), e);
//        }

        return plan;

    }




//    @Override
    //public void deleteDataObject() {
  //      if (getDataObject() == null) {
  //          return;
  //      }

//        if (dataObject instanceof PersistableBusinessObject) {
//            getBusinessObjectService().delete((PersistableBusinessObject) dataObject);
//            dataObject = null;
//        } else {
//            throw new RuntimeException(
//                    "Cannot delete object of type: " + dataObjectClass + " with business object service");
//        }
   // }


    @Override
    public void prepareForSave() {
        if (getMaintenanceAction().equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION)) {
            SavedCoursesItem planItem = (SavedCoursesItem) getDataObject();
        } else {

        }
        super.prepareForSave();


        //if (getMaintenanceAction().equalsIgnoreCase(KRADConstants.MAINTENANCE_NEW_ACTION)) {
        //	TermInfo newTerm = (TermInfo)getDataObject();
        //	newTerm.setStateKey(AtpServiceConstants.ATP_OFFICIAL_STATE_KEY);
        //}
        //super.prepareForSave();
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                GlobalResourceLoader.getService(new QName(AcademicPlanServiceConstants.NAMESPACE,
                    AcademicPlanServiceConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }
}