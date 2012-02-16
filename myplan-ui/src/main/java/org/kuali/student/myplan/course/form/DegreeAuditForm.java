package org.kuali.student.myplan.course.form;

import org.kuali.rice.krad.web.form.UifFormBase;

public class DegreeAuditForm extends UifFormBase {

    public String getAuditText() {
        String text =
            "<div><pre>\n" +
            "this is\n" +
            "the textual\n" +
            "audit report\n" +
            "</pre></div>";
        return text;
    }

    public String getAuditHtml() {
        String html =
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
        return html;
    }
}
