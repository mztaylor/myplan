package org.kuali.student.myplan.audit.service;

import org.kuali.student.r2.common.dto.ContextInfo;

public class DegreeAuditConstants extends DegreeAuditServiceConstants {

    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

    public static final String AUDIT_PARAM_ID = "auditId";
    public static final String AUDIT_PARAM_TYPE = "auditType";
    public static final String AUDIT_EMPTY_PAGE ="degree_audit_empty_page";
    public static final String AUDIT_PAGE ="degree_audit";   
    public static final String AUDIT_REPORT_SECTION="audit_report_section";
    public static final String AUDIT_EMPTY_PAGE_SECTION="empty_degree_audits";

    public static final String ORG_QUERY_PARAM = "org_queryParam_orgType";

    public static final String CAMPUS_LOCATION = "kuali.uw.org.type.campus";

    public static final String AUDIT_RUN_FAILED="myplan.text.error.message.auditIncomplete";
    public static final String AUDIT_RETRIEVAL_FAILED="myplan.text.error.message.auditRetrievalFailed";
    public static final String AUDIT_SUMMARY_VIEW="degree_audits_list";
    public static final String TECHNICAL_PROBLEM="myplan.text.error.technicalProblems";




}
