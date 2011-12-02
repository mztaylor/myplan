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

    private List<StringWrapper> programRequirements;
    private List<String> genEdRequirements;
    private List<String> prerequisites;
    private List<String> termsOffered;

    public CourseDetails() {
        programRequirements = new ArrayList<StringWrapper>();
        programRequirements.add(new StringWrapper("Not implemented."));
        genEdRequirements = new ArrayList<String>();
        genEdRequirements.add("Not implemented.");
        prerequisites = new ArrayList<String>();
        prerequisites.add("Not implemented.");
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

    public List<StringWrapper> getProgramRequirements() {
        return programRequirements;
    }

    public void setProgramRequirements(List<StringWrapper> programRequirements) {
        this.programRequirements = programRequirements;
    }

    public List<String> getGenEdRequirements() {
        return genEdRequirements;
    }

    public void setGenEdRequirements(List<String> genEdRequirements) {
        this.genEdRequirements = genEdRequirements;
    }

    public List<String> getPrerequisites() {
        return prerequisites;
    }

    public void setPrerequisites(List<String> prerequisites) {
        this.prerequisites = prerequisites;
    }

    public List<String> getTermsOffered() {
        return termsOffered;
    }

    public void setTermsOffered(List<String> termsOffered) {
        this.termsOffered = termsOffered;
    }
}