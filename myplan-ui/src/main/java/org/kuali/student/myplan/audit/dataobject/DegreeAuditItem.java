package org.kuali.student.myplan.audit.dataobject;

import org.kuali.student.myplan.audit.infc.AuditReport;

public class DegreeAuditItem implements Comparable {

    private AuditReport auditReport;

    private String programTitle;
    private String programType;

    public String getProgramType() {
        return programType;
    }

    public void setProgramType(String programType) {
        this.programType = programType;
    }

    public String getProgramTitle() {
        return programTitle;
    }

    public void setProgramTitle(String programTitle) {
        this.programTitle = programTitle;
    }

    public AuditReport getReport() {
        return auditReport;
    }

    public void setReport(AuditReport report) {
        this.auditReport = report;
    }

    public String getReportAsText(){
        return "The Report!";
    }

    @Override
    public int compareTo( Object object ) {
        DegreeAuditItem that = (DegreeAuditItem) object;
        //  TODO: Check for nulls.
        return this.getReport().getRunDate().compareTo(that.getReport().getRunDate()) * -1;
    }
}
