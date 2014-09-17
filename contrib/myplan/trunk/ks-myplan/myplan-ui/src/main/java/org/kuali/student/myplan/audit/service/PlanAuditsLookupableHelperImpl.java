package org.kuali.student.myplan.audit.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.audit.dataobject.PlanAuditItem;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.util.*;

@Component
public class PlanAuditsLookupableHelperImpl extends MyPlanLookupableImpl {

    private final Logger logger = Logger.getLogger(PlanAuditsLookupableHelperImpl.class);

    private transient DegreeAuditService degreeAuditService;

    private transient AcademicPlanService academicPlanService;

    private transient DegreeAuditHelper degreeAuditHelper;

    private transient UserSessionHelper userSessionHelper;

    @Override
    protected List<PlanAuditItem> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        List<PlanAuditItem> planAuditItems = new ArrayList<PlanAuditItem>();
        try {
            String regId = getUserSessionHelper().getStudentId();
            Map<String, PlanAuditItem> auditsInLearningPlan = getDegreeAuditHelper().getPlanItemSnapShots(regId);

            /**
             *  Make a list of PlanAuditItem, but only include the most recent audit for a particular program.
             */
            int planAuditsCount = 0;
            for (String auditId : auditsInLearningPlan.keySet()) {
                planAuditsCount++;
                if (planAuditsCount > DegreeAuditConstants.DEFAULT_PLAN_AUDITS_VIEWABLE) {
                    break;
                }
                ContextInfo contextInfo = DegreeAuditConstants.CONTEXT_INFO;
                contextInfo.setAttributes(Arrays.asList(new AttributeInfo(DegreeAuditConstants.USE_DOES_NOT_EXIST_EXCEPTION, "true")));

                AuditReportInfo audit = null;
                try {
                    audit = getDegreeAuditService().getAuditReport(auditId, DegreeAuditConstants.AUDIT_TYPE_KEY_DEFAULT, contextInfo);
                } catch (DoesNotExistException e) {
                    logger.error("Could not find a plan Audit with auditId: " + auditId);
                    continue;
                }
                if (audit == null) {
                    continue;
                }
                PlanAuditItem planAuditItem = auditsInLearningPlan.get(audit.getAuditId());
                if (planAuditItem == null) {
                    planAuditItem = new PlanAuditItem();
                }
                planAuditItem.setReport(audit);
                planAuditItem.setProgramTitle(audit.getProgramTitle());
                planAuditItem.setProgramType("ProgramType");
                planAuditItems.add(planAuditItem);
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
            degreeAuditHelper = UwMyplanServiceLocator.getInstance().getDegreeAuditHelper();
        }
        return degreeAuditHelper;
    }


    public void setDegreeAuditHelper(DegreeAuditHelper degreeAuditHelper) {
        this.degreeAuditHelper = degreeAuditHelper;
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
}
