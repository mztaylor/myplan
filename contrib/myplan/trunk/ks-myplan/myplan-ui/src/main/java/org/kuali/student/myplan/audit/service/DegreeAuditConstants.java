package org.kuali.student.myplan.audit.service;

import org.kuali.student.r2.common.dto.ContextInfo;

public class DegreeAuditConstants extends DegreeAuditServiceConstants {

    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

    public static final String DEFAULT_KEY = "default";
    public static final String DEFAULT_SELECT = "Select Credit amount";
    public static final String DEFAULT_SELECT_AND_MORE = "Select Credit amount and more";
    public static final String DEFAULT_VALUE = "Select a degree program or minor";
    public static final String AUDIT_TYPE_DEGREE = "degree";
    public static final String AUDIT_TYPE_PLAN = "plan";
    public static final String AUDIT_PARAM_ID = "auditId";
    public static final String AUDIT_PARAM_TYPE = "auditType";
    public static final String AUDIT_EMPTY_PAGE = "degree_audit_empty_page";
    public static final String AUDIT_NON_STUDENT_PAGE = "degree_audit_non_student_page";
    public static final String AUDIT_PAGE = "degree_audit";
    public static final String AUDIT_REPORT_SECTION = "audit_report_section";
    public static final String AUDIT_EMPTY_PAGE_SECTION = "empty_degree_audits";

    public static final String DEGREE_AUDIT_VIEW_ID = "DegreeAudit-FormView";
    public static final String PLAN_AUDIT_VIEW_ID = "PlanAudit-FormView";

    public static final String ORG_QUERY_PARAM = "org_queryParam_orgType";

    public static final String CAMPUS_LOCATION = "kuali.org.type.campus";

    public static final String DEGREE_AUDIT_SERVICE_URL = "myplan.auditservice.url";

    public static final String AUDIT_RUN_FAILED = "myplan.text.error.message.auditIncomplete";
    public static final String AUDIT_RETRIEVAL_FAILED = "myplan.text.error.message.auditRetrievalFailed";
    public static final String AUDIT_SUMMARY_VIEW = "degree_audits_list";
    public static final String TECHNICAL_PROBLEM = "myplan.text.error.technicalProblems";
    public static final String NO_SYSTEM_KEY = "myplan.text.error.noSystemKey";
    public static final String CREDIT = "Credit";
    public static final String CHOICE = "Choice";
    public static final String HONORS_CREDIT = "Honors";
    public static final String WRITING_CREDIT = "Writing";
    public static final String SECTION = "Section";
    public static final String SECONDARY_ACTIVITY = "SecondaryActivity";
    public static final String BUCKET = "BUCKET";
    public static final String BUCKET_CLEAN = "BUCKET_CLEAN";
    public static final String BUCKET_MESSY = "BUCKET_MESSY";
    public static final String BUCKET_IGNORE = "BUCKET_IGNORE";

    public static final String CR_NO_CR_GRADING_OPTION_ID = "uw.result.group.grading.option.crnc";
    public static final String CR_NO_CR_GRADING_OPTION = "Credit/No-Credit grading";

    public static final String APPLICATION_URL = "application.url";

    public static final String AUDIT_FAILED_HTML = "<div class=\"alert alert-error\">" +
            "Audit processing failed due to '%s'" +
            "<input name=\"script\" type=\"hidden\" data-role=\"script\" value=\"jQuery.event.trigger('AUDIT_ERROR', {'auditType':'%s'});\"/>" +
            "</div>";
    public static final String IS_AUDIT_SERVICE_UP = "isAuditServiceRunning";

    public static final String AUDIT_STATUS_ERROR_MSG = "audit status returned error";


    public static final String DEGREE_AUDIT_PROGRAM_PARAM_SEATTLE = "degreeAudit.programParamSeattle";
    public static final String PLAN_AUDIT_PROGRAM_PARAM_SEATTLE = "planAudit.programParamSeattle";
}
