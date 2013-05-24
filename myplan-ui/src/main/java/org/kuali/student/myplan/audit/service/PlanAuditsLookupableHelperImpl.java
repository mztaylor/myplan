package org.kuali.student.myplan.audit.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.audit.dataobject.PlanAuditItem;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;

import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.student.myplan.plan.PlanConstants.CONTEXT_INFO;
import static org.kuali.student.myplan.plan.PlanConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT;

public class PlanAuditsLookupableHelperImpl extends MyPlanLookupableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryHelperImpl.class);

    private transient DegreeAuditService degreeAuditService;

    private transient AcademicPlanService academicPlanService;

    @Override
    protected List<PlanAuditItem> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        List<PlanAuditItem> planAuditItems = new ArrayList<PlanAuditItem>();
        try {
            String regId = UserSessionHelper.getStudentRegId();
            //  TODO: Calculate dates that make sense.
            Date begin = new Date();
            Date end = new Date();
            Map<String, PlanAuditItem> auditsInLearningPlan = new HashMap<String, PlanAuditItem>();
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(regId, LEARNING_PLAN_TYPE_PLAN_AUDIT, CONTEXT_INFO);
            for (LearningPlanInfo learningPlanInfo : learningPlanList) {
                PlanAuditItem planAuditItem = new PlanAuditItem();
                String auditId = null;
                for (AttributeInfo attributeInfo : learningPlanInfo.getAttributes()) {
                    String key = attributeInfo.getKey();
                    String value = attributeInfo.getValue();
                    if ("forCourses".equalsIgnoreCase(key)) {
                        planAuditItem.setAuditedCoursesCount(value);
                    } else if ("forCredits".equalsIgnoreCase(key)) {
                        planAuditItem.setTotalAuditedCredit(value);
                    } else if ("forQuarter".equalsIgnoreCase(key)) {
                        planAuditItem.setAuditedQuarterUpTo(value);
                    } else if ("auditId".equalsIgnoreCase(key)) {
                        auditId = value;
                    }
                }


                if (auditId != null) {
                    auditsInLearningPlan.put(auditId, planAuditItem);
                }
            }

            DegreeAuditService degreeAuditService = getDegreeAuditService();
            List<AuditReportInfo> audits = degreeAuditService.getAuditsForStudentInDateRange(regId, begin, end, DegreeAuditConstants.CONTEXT_INFO);

            /**
             *  Make a list of PlanAuditItem, but only include the most recent audit for a particular program.
             */
//            HashSet<String> programSet = new HashSet<String>();
            for (AuditReportInfo audit : audits) {
                if (audit.isWhatIfAudit()) {
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

}
