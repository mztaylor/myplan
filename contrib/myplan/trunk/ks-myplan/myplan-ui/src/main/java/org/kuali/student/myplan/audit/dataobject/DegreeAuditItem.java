package org.kuali.student.myplan.audit.dataobject;

import org.kuali.student.myplan.audit.infc.AuditReport;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

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

    /**
     * Returns the audit content as HTML.
     */
    public String getReportAsHtml(){
        InputStream in = null;
        try {
            in = this.auditReport.getReport().getDataSource().getInputStream();
        } catch (IOException e) {
            return "Could not read report contents.";
        }
        StringWriter sw = new StringWriter();

        int c = 0;
        try {
            while ((c = in.read()) != -1) {
                sw.append( (char) c );
            }
        } catch (IOException e) {
            return "Could not read report contents.";
        }

        String html = sw.toString();
        return html;
    }

    @Override
    public int compareTo( Object object ) {
        DegreeAuditItem that = (DegreeAuditItem) object;
        //  TODO: Check for nulls.
        return this.getReport().getRunDate().compareTo(that.getReport().getRunDate()) * -1;
    }
}
