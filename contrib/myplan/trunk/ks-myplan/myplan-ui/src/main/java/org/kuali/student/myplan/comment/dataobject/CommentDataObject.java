package org.kuali.student.myplan.comment.dataobject;

import java.util.Date;

/**
 *  Data object for comments.
 */
public class CommentDataObject implements Comparable<CommentDataObject> {
    private Date timestamp;
    private String body;
    private String from;

    public String getFrom() {
       return from;
    }

    public void setFrom(String from) {
       this.from = from;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public int compareTo(CommentDataObject other) {
        if (other == null) {
            return 1;
        }
        return this.getTimestamp().compareTo(other.getTimestamp());
    }
}
