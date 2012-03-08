package org.kuali.student.myplan.audit.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.lookup.LookupableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.audit.dataobject.DegreeAuditItem;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryViewHelperServiceImpl;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;

import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

public class DegreeAuditsLookupableHelperImpl extends LookupableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryViewHelperServiceImpl.class);

    private transient DegreeAuditService degreeAuditService;

    @Override
    protected List<?> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        List<DegreeAuditItem> degreeAuditsList = new ArrayList<DegreeAuditItem>();
        try {
            Person user = GlobalVariables.getUserSession().getPerson();
            String studentID = user.getPrincipalId();
            studentID = "000083856";

            DegreeAuditService degreeAuditService = getDegreeAuditService();

            HashSet<String> programSet = new HashSet<String>();
            ContextInfo context = new ContextInfo();
            List<AuditReportInfo> auditList = degreeAuditService.getRecentAuditsForStudent(studentID, DegreeAuditServiceConstants.AUDIT_REPORT_TYPE_KEY_SUMMARY, context);
            for (AuditReportInfo audit : auditList) {
                String programID = audit.getProgramID();
                if( !programSet.contains( programID ))
                {
                    DegreeAuditItem da = new DegreeAuditItem();
                    da.setId( programID );
                    da.setRunDate(audit.getRunDate());
                    degreeAuditsList.add(da);
                    programSet.add( programID );
                }
            }

        } catch (Exception e) {
            logger.error("Could not retrieve degree audit items.", e);
            //throw new RuntimeException(e);
        }

        return degreeAuditsList;
    }

    public DegreeAuditService getDegreeAuditService() {
        if (degreeAuditService == null) {
            degreeAuditService = (DegreeAuditService)
                GlobalResourceLoader.getService(new QName(DegreeAuditServiceConstants.NAMESPACE,
                    DegreeAuditServiceConstants.SERVICE_NAME));
        }
        return degreeAuditService;
    }
}
