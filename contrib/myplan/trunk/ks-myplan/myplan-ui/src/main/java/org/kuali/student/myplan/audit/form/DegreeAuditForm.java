package org.kuali.student.myplan.audit.form;

import org.kuali.rice.krad.web.form.UifFormBase;

public class DegreeAuditForm extends UifFormBase {


    private String auditHtml =
            "<div>" +
                "<ul>\n" +
                "<li>\n" +
                "this is\n" +
                "</li>\n" +
                "<li>\n" +
                "the textual\n" +
                "</li>\n" +
                "<li>\n" +
                "audit report\n" +
                "</li>\n" +
                "</ul>\n" +
                "</div>";
    private String programParam;
    private String campusParam;
    private String auditId;

    public String getAuditId() {
        return auditId;
    }

    public void setAuditId(String auditId) {
        this.auditId = auditId;
    }

    public String getProgramParam() {
        return programParam;
    }

    public void setProgramParam(String programParam) {
        this.programParam = programParam;
    }

    public String getCampusParam() {
        return campusParam;
    }

    public void setCampusParam(String campusParam) {
        this.campusParam = campusParam;
    }

    public String getAuditHtml() {
        return auditHtml;
    }

    public void setAuditHtml( String auditHtml ) {
        this.auditHtml = auditHtml;
    }

}
