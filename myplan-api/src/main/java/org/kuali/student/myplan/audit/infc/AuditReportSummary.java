package org.kuali.student.myplan.audit.infc;

import org.kuali.student.r2.common.infc.TypeStateEntity;


/**
 * Summary report for an audit
 *
 * @Author Kamal
 */
public interface AuditReportSummary extends TypeStateEntity {

    /**
     * Id of the Audit request
     * @name Audit Id
     */
    public String getAuditId();

}
