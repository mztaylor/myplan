package org.kuali.student.myplan.plan.dataobject;

import org.kuali.student.myplan.course.dataobject.CourseDetails;

import java.util.Date;
import java.util.List;

public class PlanItemDataObject implements Comparable {

    //  Common properties.
    private String id;
    @Deprecated
    private CourseDetails courseDetails;
    private Date dateAdded;
    private String planType;
    private String atp;
    private String refObjId;
    private String refObjType;
    private String term;
    private int year;
    private String creditPref;



    //  Planned course specific properties.

    //  Ids of the ATPs associated with this course.
    private List<String> atpIds;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Deprecated
    public CourseDetails getCourseDetails() {
        return courseDetails;
    }

    @Deprecated
    public void setCourseDetails(CourseDetails courseDetails) {
        this.courseDetails = courseDetails;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getPlanType() {
        return planType;
    }

    public void setPlanType(String planType) {
        this.planType = planType;
    }

    public String getAtp() {
        return atp;
    }

    public void setAtp(String atp) {
        this.atp = atp;
    }

    public String getRefObjId() {
        return refObjId;
    }

    public void setRefObjId(String refObjId) {
        this.refObjId = refObjId;
    }

    public String getRefObjType() {
        return refObjType;
    }

    public void setRefObjType(String refObjType) {
        this.refObjType = refObjType;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getCreditPref() {
        return creditPref;
    }

    public void setCreditPref(String creditPref) {
        this.creditPref = creditPref;
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
