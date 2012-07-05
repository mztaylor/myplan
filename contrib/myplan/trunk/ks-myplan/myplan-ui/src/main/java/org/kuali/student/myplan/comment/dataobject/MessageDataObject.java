package org.kuali.student.myplan.comment.dataobject;

import java.util.ArrayList;
import java.util.List;

/**
 *  Data object for messages from advisers.
 */
public class MessageDataObject extends CommentDataObject {

    private String subject;
    private List<CommentDataObject> comments;

    public MessageDataObject() {
        comments = new ArrayList<CommentDataObject>();
    }

    public List<CommentDataObject> getComments() {
        return comments;
    }

    public void addComment(CommentDataObject comment) {
        comments.add(comment);
    }

    public void setComments(List<CommentDataObject> comments) {
        this.comments = comments;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}