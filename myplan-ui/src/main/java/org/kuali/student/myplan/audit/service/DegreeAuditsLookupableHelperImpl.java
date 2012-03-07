package org.kuali.student.myplan.audit.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.lookup.LookupableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.audit.dataobject.DegreeAuditItem;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryViewHelperServiceImpl;
import org.kuali.student.r2.common.dto.ContextInfo;

import org.apache.log4j.Logger;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DegreeAuditsLookupableHelperImpl extends LookupableImpl {

    private final Logger logger = Logger.getLogger(CourseDetailsInquiryViewHelperServiceImpl.class);

    private transient DegreeAuditService degreeAuditService;

    @Override
    protected List<?> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        List<DegreeAuditItem> degreeAuditsList = new ArrayList<DegreeAuditItem>();
        try {
            DegreeAuditService degreeAuditService = getDegreeAuditService();

            Person user = GlobalVariables.getUserSession().getPerson();

            ContextInfo context = new ContextInfo();
            String studentID = user.getPrincipalId();

            DegreeAuditItem da1 = new DegreeAuditItem();
            da1.setId("Audit 1");
            da1.setRunDate(new java.util.Date());
            DegreeAuditItem da2 = new DegreeAuditItem();
            da2.setId("Audit 2");
            da2.setRunDate(new java.util.Date());

            degreeAuditsList.add(da1);
            degreeAuditsList.add(da2);

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
