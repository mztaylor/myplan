package org.kuali.student.myplan.comment.form;

import org.kuali.rice.krad.web.form.UifFormBase;

public class CommentForm extends UifFormBase {
    private String subject;
    private String body;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
