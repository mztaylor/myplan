package org.kuali.student.myplan.audit.service;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.audit.dataobject.DegreeAuditItem;
import org.kuali.student.myplan.audit.dataobject.PlanAuditItem;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.util.DegreeAuditDataObjectHelper;
import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.util.*;

@Component
public class DegreeAuditsLookupableHelperImpl extends MyPlanLookupableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryHelperImpl.class);

    private transient DegreeAuditService degreeAuditService;

    private DegreeAuditHelper degreeAuditHelper;


    private UserSessionHelper userSessionHelper;

    @Override
    protected List<DegreeAuditItem> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        List<DegreeAuditItem> degreeAuditItems = new ArrayList<DegreeAuditItem>();
        try {
            String regId = getUserSessionHelper().getStudentId();
            //  TODO: Calculate dates that make sense.
            Date begin = new Date();
            Date end = new Date();

            Map<String, PlanAuditItem> auditsInLearningPlan = getDegreeAuditHelper().getPlanItemSnapShots(regId);
            DegreeAuditService degreeAuditService = getDegreeAuditService();
            List<AuditReportInfo> audits = degreeAuditService.getAuditsForStudentInDateRange(regId, begin, end, DegreeAuditConstants.CONTEXT_INFO);

            /**
             *  Make a list of DegreeAuditItem, but only include the most recent audit for a particular program.
             */
            HashSet<String> programSet = new HashSet<String>();
            for (AuditReportInfo audit : audits) {
                String programId = audit.getProgramId();
                if (!programSet.contains(programId) && !audit.isWhatIfAudit() && !auditsInLearningPlan.containsKey(audit.getAuditId())) {
                    programSet.add(programId);
                    DegreeAuditItem item = DegreeAuditDataObjectHelper.makeDegreeAuditDataObject(audit);
                    degreeAuditItems.add(item);
                }
            }
            if (degreeAuditItems.size() > 0) {
                degreeAuditItems.get(0).setRecentAudit(true);
            }
        } catch (Exception e) {
            logger.warn("cannot get list of recent audits", e);
        }

        return degreeAuditItems;
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
        if(userSessionHelper == null){
            userSessionHelper =  UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }


    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}
