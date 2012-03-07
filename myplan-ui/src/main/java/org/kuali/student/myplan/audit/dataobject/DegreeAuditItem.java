package org.kuali.student.myplan.audit.dataobject;

import org.kuali.student.myplan.course.dataobject.CourseDetails;

import java.util.Date;

public class DegreeAuditItem implements Comparable {

    private String id;
    private Date runDate;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getRunDate() {
        return runDate;
    }

    public void setRunDate(Date runDate) {
        this.runDate = runDate;
    }

    @Override
    public int compareTo( Object object ) {
        DegreeAuditItem that = (DegreeAuditItem) object;
        return this.getRunDate().compareTo(that.getRunDate()) * -1;
    }
}
