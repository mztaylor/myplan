package org.kuali.student.myplan.course.dataobject;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Course details that comes from course catalog (KS CM)
 */
public class CourseSummaryDetails {

    private String courseId;
    private String versionIndependentId;
    private String code;
    private String campusCd;

    private String subjectArea;
    private String courseNumber;

    private String courseTitle;
    private String credit;
    private String courseDescription;
    private List<String> termsOffered;

    private String curriculumTitle;

    private List<String> campusLocations;
    private List<String> requisites;
    private List<String> genEdRequirements;
    private List<String> abbrGenEdRequirements;
    private List<String> crossListings;


    private String lastOffered;
    private List<String> scheduledTerms;


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
        //  Double quotes are very problematic in the serialization to JSON so change to single quotes.;
        if (StringUtils.isEmpty(courseTitle)) {
            return courseTitle;
        }
        return courseTitle.replaceAll("\"", "'");
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

    public List<String> getTermsOffered() {
        if (termsOffered == null) {
            termsOffered = new ArrayList<String>();
        }
        return termsOffered;
    }

    public void setTermsOffered(List<String> termsOffered) {
        this.termsOffered = termsOffered;
    }

    public String getVersionIndependentId() {
        return versionIndependentId;
    }

    public void setVersionIndependentId(String versionIndependentId) {
        this.versionIndependentId = versionIndependentId;
    }

    public String getCurriculumTitle() {
        return curriculumTitle;
    }

    public void setCurriculumTitle(String curriculumTitle) {
        this.curriculumTitle = curriculumTitle;
    }


    public List<String> getGenEdRequirements() {
        if (genEdRequirements == null) {
            genEdRequirements = new ArrayList<String>();
        }
        return genEdRequirements;
    }

    public void setGenEdRequirements(List<String> genEdRequirements) {
        this.genEdRequirements = genEdRequirements;
    }

    public List<String> getAbbrGenEdRequirements() {
        if (abbrGenEdRequirements == null) {
            abbrGenEdRequirements = new ArrayList<String>();
        }
        return abbrGenEdRequirements;
    }

    public void setAbbrGenEdRequirements(List<String> abbrGenEdRequirements) {
        this.abbrGenEdRequirements = abbrGenEdRequirements;
    }

    public List<String> getRequisites() {
        if (requisites == null) {
            requisites = new ArrayList<String>();
        }
        return requisites;
    }

    public void setRequisites(List<String> requisites) {
        this.requisites = requisites;
    }

    public List<String> getCampusLocations() {
        if (campusLocations == null) {
            campusLocations = new ArrayList<String>();
        }
        return campusLocations;
    }

    public List<String> getCrossListings() {
        return crossListings;
    }

    public void setCrossListings(List<String> crossListings) {
        this.crossListings = crossListings;
    }

    public void setCampusLocations(List<String> campusLocations) {
        this.campusLocations = campusLocations;
    }

    public String getSubjectArea() {
        return subjectArea;
    }

    public void setSubjectArea(String subjectArea) {
        this.subjectArea = subjectArea;
    }

    public String getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(String courseNumber) {
        this.courseNumber = courseNumber;
    }


    public String getLastOffered() {
        return lastOffered;
    }

    public void setLastOffered(String lastOffered) {
        this.lastOffered = lastOffered;
    }

    public List<String> getScheduledTerms() {
    	if (scheduledTerms == null) {
    		scheduledTerms = new ArrayList<String>();
    	}
        return scheduledTerms;
    }

    public void setScheduledTerms(List<String> scheduledTerms) {
        this.scheduledTerms = scheduledTerms;
    }

    public String getCampusCd() {
        return campusCd;
    }

    public void setCampusCd(String campusCd) {
        this.campusCd = campusCd;
    }
}