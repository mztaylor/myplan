package org.kuali.student.myplan.audit.form;

import org.kuali.rice.krad.web.form.UifFormBase;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/9/13
 * Time: 4:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class AuditForm extends UifFormBase {

    private DegreeAuditForm degreeAudit;
    private PlanAuditForm planAudit;
    private boolean planExists;

    public DegreeAuditForm getDegreeAudit() {
        if (degreeAudit == null) {
            degreeAudit = new DegreeAuditForm();
        }
        return degreeAudit;
    }

    public void setDegreeAudit(DegreeAuditForm degreeAudit) {
        this.degreeAudit = degreeAudit;
    }

    public PlanAuditForm getPlanAudit() {
        if (planAudit == null) {
            planAudit = new PlanAuditForm();
        }
        return planAudit;
    }

    public void setPlanAudit(PlanAuditForm planAudit) {
        this.planAudit = planAudit;
    }

    public boolean isPlanExists() {
        return planExists;
    }

    public void setPlanExists(boolean planExists) {
        this.planExists = planExists;
    }
}
