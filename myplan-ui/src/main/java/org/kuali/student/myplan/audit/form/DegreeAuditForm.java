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

    public String getAuditHtml() {
        return auditHtml;
    }

    public void setAuditHtml( String auditHtml ) {
        this.auditHtml = auditHtml;
    }

}
