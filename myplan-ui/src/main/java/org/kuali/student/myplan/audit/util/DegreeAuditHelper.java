package org.kuali.student.myplan.audit.util;

import org.kuali.student.myplan.audit.dataobject.PlanAuditItem;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.form.DegreeAuditForm;
import org.kuali.student.myplan.audit.form.PlanAuditForm;

import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 6/24/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public interface DegreeAuditHelper {

    public PlanAuditForm processHandOff(PlanAuditForm planAuditForm, String studentId);

    public DegreeAuditForm copyCampusToForm(AuditReportInfo report, DegreeAuditForm form);

    public String getFormProgramID(DegreeAuditForm form);
    
    public Map<String, PlanAuditItem> getPlanItemSnapShots (String studentId);

}
