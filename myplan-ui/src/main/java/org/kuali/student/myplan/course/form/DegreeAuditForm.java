package org.kuali.student.myplan.course.form;

import org.kuali.rice.krad.web.form.UifFormBase;

public class DegreeAuditForm extends UifFormBase {

    private String auditText =
            "<div><pre>\n"+
            "this is\n"+
            "the textual\n"+
            "audit report\n"+
            "</pre></div>";

    public String getAuditText() {
        return auditText;
    }

    public void setAuditText( String auditText )
    {
        this.auditText = auditText;
    }

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

    public String getAuditHtml() {
        return auditHtml;
    }

    public void setAuditHtml( String auditHtml ) {
        this.auditHtml = auditHtml;
    }
}
