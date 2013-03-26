package org.kuali.student.myplan.audit.service;

import org.kuali.student.r2.common.constants.CommonServiceConstants;
import org.kuali.student.r2.common.dto.ContextInfo;

/**
 * kmuthu Don't forget to add comment
 *
 * @Author kmuthu
 * Date: 1/6/12
 */
public class DegreeAuditServiceConstants {
    public static final String NAMESPACE = CommonServiceConstants.REF_OBJECT_URI_GLOBAL_PREFIX + "da";
    public static final String SERVICE_NAME = "DegreeAuditService";

    public static final String AUDIT_TYPE_KEY_DEFAULT = "kuali.audit.type.default";
    public static final String AUDIT_TYPE_KEY_HTML = "kuali.audit.type.html";
    public static final String AUDIT_TYPE_KEY_XML = "kuali.audit.type.xml";
    public static final String AUDIT_TYPE_KEY_PRINT = "kuali.audit.type.print";
    public static final String AUDIT_TYPE_KEY_SUMMARY = "kuali.audit.type.summary";


    public static final ContextInfo DEGREE_AUDIT_SERVICE_CONTEXT = new ContextInfo();
}
