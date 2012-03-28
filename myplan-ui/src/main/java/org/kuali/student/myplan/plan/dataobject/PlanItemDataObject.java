package org.kuali.student.myplan.plan.dataobject;

import org.kuali.student.myplan.course.dataobject.CourseDetails;

import java.util.Date;
import java.util.List;

public class PlanItemDataObject implements Comparable {

    //  Common properties.
    private String id;
    private CourseDetails courseDetails;
    private Date dateAdded;

    //  Planned course specific properties.

    //  Ids of the ATPs associated with this course.
    private List<String> atpIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public CourseDetails getCourseDetails() {
        return courseDetails;
    }

    public void setCourseDetails(CourseDetails courseDetails) {
        this.courseDetails = courseDetails;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    @Override
    public int compareTo( Object object ) {
        PlanItemDataObject that = (PlanItemDataObject) object;
        return this.getDateAdded().compareTo( that.getDateAdded() ) * -1;
    }

    public List<String> getAtpIds() {
        return atpIds;
    }

    public void setAtpIds(List<String> atpIds) {
        this.atpIds = atpIds;
    }
}
