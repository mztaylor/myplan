package org.kuali.student.myplan.audit.form;

import org.kuali.rice.krad.web.form.UifFormBase;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/9/13
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlanAuditForm extends DegreeAuditForm {
   private String lastPlannedTerm;

    public String getLastPlannedTerm() {
        return lastPlannedTerm;
    }

    public void setLastPlannedTerm(String lastPlannedTerm) {
        this.lastPlannedTerm = lastPlannedTerm;
    }
}
