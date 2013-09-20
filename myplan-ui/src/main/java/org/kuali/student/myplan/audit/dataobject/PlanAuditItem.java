package org.kuali.student.myplan.audit.dataobject;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/11/13
 * Time: 10:05 AM
 * To change this template use File | Settings | File Templates.
 */
public class PlanAuditItem extends DegreeAuditItem{
    private String auditedCoursesCount;
    private String totalAuditedCredit;
    private String auditedQuarterUpTo;
    private boolean runByAdviser;
    private String requestedBy;

    public String getAuditedCoursesCount() {
        return auditedCoursesCount;
    }

    public void setAuditedCoursesCount(String auditedCoursesCount) {
        this.auditedCoursesCount = auditedCoursesCount;
    }

    public String getTotalAuditedCredit() {
        return totalAuditedCredit;
    }

    public void setTotalAuditedCredit(String totalAuditedCredit) {
        this.totalAuditedCredit = totalAuditedCredit;
    }

    public String getAuditedQuarterUpTo() {
        return auditedQuarterUpTo;
    }

    public void setAuditedQuarterUpTo(String auditedQuarterUpTo) {
        this.auditedQuarterUpTo = auditedQuarterUpTo;
    }

    public boolean isRunByAdviser() {
        return runByAdviser;
    }

    public void setRunByAdviser(boolean runByAdviser) {
        this.runByAdviser = runByAdviser;
    }

    public String getRequestedBy() {
        return requestedBy;
    }

    public void setRequestedBy(String requestedBy) {
        this.requestedBy = requestedBy;
    }
}
