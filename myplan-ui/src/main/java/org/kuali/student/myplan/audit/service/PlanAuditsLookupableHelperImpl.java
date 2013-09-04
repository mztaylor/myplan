package org.kuali.student.myplan.audit.service;

import edu.uw.kuali.student.myplan.util.DegreeAuditHelperImpl;
import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.audit.dataobject.PlanAuditItem;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.student.myplan.plan.PlanConstants.CONTEXT_INFO;
import static org.kuali.student.myplan.plan.PlanConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT;

public class PlanAuditsLookupableHelperImpl extends MyPlanLookupableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryHelperImpl.class);

    private transient DegreeAuditService degreeAuditService;

    private transient AcademicPlanService academicPlanService;

    private DegreeAuditHelper degreeAuditHelper;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Override
    protected List<PlanAuditItem> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        List<PlanAuditItem> planAuditItems = new ArrayList<PlanAuditItem>();
        try {
            String regId = getUserSessionHelper().getStudentId();
            //  TODO: Calculate dates that make sense.
            Date begin = new Date();
            Date end = new Date();
            Map<String, PlanAuditItem> auditsInLearningPlan = getDegreeAuditHelper().getPlanItemSnapShots(regId);

            List<AuditReportInfo> audits = getDegreeAuditService().getAuditsForStudentInDateRange(regId, begin, end, DegreeAuditConstants.CONTEXT_INFO);

            /**
             *  Make a list of PlanAuditItem, but only include the most recent audit for a particular program.
             */
//            HashSet<String> programSet = new HashSet<String>();
            for (AuditReportInfo audit : audits) {
                if (auditsInLearningPlan.containsKey(audit.getAuditId())) {
                    PlanAuditItem planAuditItem = auditsInLearningPlan.get(audit.getAuditId());
                    if (planAuditItem == null) {
                        planAuditItem = new PlanAuditItem();
                    }
                    planAuditItem.setReport(audit);
                    planAuditItem.setProgramTitle(audit.getProgramTitle());
                    planAuditItem.setProgramType("ProgramType");
                    planAuditItems.add(planAuditItem);
                }
            }
            if (planAuditItems.size() > 0) {
                planAuditItems.get(0).setRecentAudit(true);
            }
        } catch (Exception e) {
            logger.warn("cannot get list of recent audits", e);
        }

        return planAuditItems;
    }

    public DegreeAuditService getDegreeAuditService() {
        if (degreeAuditService == null) {
            degreeAuditService = (DegreeAuditService)
                    GlobalResourceLoader.getService(new QName(DegreeAuditServiceConstants.NAMESPACE,
                            DegreeAuditServiceConstants.SERVICE_NAME));
            if (degreeAuditService == null) {
                throw new RuntimeException("Degree audit service handle was null.");
            }
        }
        return degreeAuditService;
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

    public DegreeAuditHelper getDegreeAuditHelper() {
        if (degreeAuditHelper == null) {
            degreeAuditHelper = new DegreeAuditHelperImpl();
        }
        return degreeAuditHelper;
    }

    public void setDegreeAuditHelper(DegreeAuditHelper degreeAuditHelper) {
        this.degreeAuditHelper = degreeAuditHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        if(userSessionHelper == null){
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}
