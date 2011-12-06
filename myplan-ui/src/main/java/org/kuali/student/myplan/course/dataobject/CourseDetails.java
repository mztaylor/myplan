package org.kuali.student.myplan.course.dataobject;

import java.util.ArrayList;
import java.util.List;

/**
 *  Course Details
 */
public class CourseDetails {
    private String courseId;
    private String code;
    private String courseTitle;
    private String credit;
    private String courseDescription;

    private List<String> campusLocations;
    private List<String> termsOffered;
    private List<String> requisites;
    private List<String> genEdRequirements;

    public CourseDetails() {
        genEdRequirements = new ArrayList<String>();
        genEdRequirements.add("Not implemented.");
        genEdRequirements.add("Not implemented again.");
        requisites = new ArrayList<String>();
        termsOffered = new ArrayList<String>();
        termsOffered.add("Not implemented");
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getCourseDescription() {
        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public List<String> getGenEdRequirements() {
        return genEdRequirements;
    }

    public void setGenEdRequirements(List<String> genEdRequirements) {
        this.genEdRequirements = genEdRequirements;
    }

    public List<String> getRequisites() {
        return requisites;
    }

    public void setRequisites(List<String> requisites) {
        this.requisites = requisites;
    }

    public List<String> getTermsOffered() {
        return termsOffered;
    }

    public void setTermsOffered(List<String> termsOffered) {
        this.termsOffered = termsOffered;
    }
    public List<String> getCampusLocations() {
        return campusLocations;
    }

    public void setCampusLocations(List<String> campusLocations) {
        this.campusLocations = campusLocations;
    }
}