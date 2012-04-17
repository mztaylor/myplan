package org.kuali.student.myplan.course.dataobject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnore;

/**
 *  Course Details
 */
public class CourseDetails {

    // Indicates if only summary information is stored in course details
    private boolean summaryOnly = true;

    // List of fileds populated when only summary information is loaded
    private String courseId;
    private String code;
    private String courseTitle;
    private String credit;
    private String courseDescription;
    private List<String> termsOffered;


    // Rest of these properties are populated as part of full details

    private String curriculumTitle;
    private String lastOffered;

    //  Planned course properties
    private boolean inPlannedCourseList;

    // Saved course properties
    private boolean inSavedCourseList;
    private String savedCourseItemId;
    private Date savedCourseDateCreated;

    private List<String> campusLocations;
    private List<String> scheduledTerms;
    private List<String> requisites;
    private List<String> genEdRequirements;
    private List<String> abbrGenEdRequirements;

    public String getLastOffered() {
        return lastOffered;
    }

    public void setLastOffered(String lastOffered) {
        this.lastOffered = lastOffered;
    }

    public CourseDetails() {
        genEdRequirements = new ArrayList<String>();
        requisites = new ArrayList<String>();
        termsOffered = new ArrayList<String>();
    }

    public String getCurriculumTitle() {
        return curriculumTitle;
    }

    public void setCurriculumTitle(String curriculumTitle) {
        this.curriculumTitle = curriculumTitle;
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
        // TODO quickfix Put in just for usability testing, remove asap
        if (courseDescription == null) {
            courseDescription = "";
        }
        String quickfix = courseDescription;
        int n = quickfix.lastIndexOf( "Prerequisite:" );
        if( n > -1  )
        {
            quickfix = quickfix.substring( 0, n );
        }
        return quickfix;
//        return courseDescription;
    }

    public void setCourseDescription(String courseDescription) {
        this.courseDescription = courseDescription;
    }

    public boolean isInPlannedCourseList() {
        return inPlannedCourseList;
    }

    public void setInPlannedCourseList(boolean inPlannedCourseList) {
        this.inPlannedCourseList = inPlannedCourseList;
    }

    public boolean isInSavedCourseList() {
        return inSavedCourseList;
    }

    public void setInSavedCourseList(boolean inSavedCourseList) {
        this.inSavedCourseList = inSavedCourseList;
    }

    public String getSavedCourseItemId() {
        return savedCourseItemId;
    }

    public void setSavedCourseItemId(String savedCourseItemId) {
        this.savedCourseItemId = savedCourseItemId;
    }

    public Date getSavedCourseDateCreated() {
        return savedCourseDateCreated;
    }

    public void setSavedCourseDateCreated(Date savedCourseDateCreated) {
        this.savedCourseDateCreated = savedCourseDateCreated;
    }

    public List<String> getGenEdRequirements() {
        return genEdRequirements;
    }

    public void setGenEdRequirements(List<String> genEdRequirements) {
        this.genEdRequirements = genEdRequirements;
    }

    public List<String> getAbbrGenEdRequirements() {
        return abbrGenEdRequirements;
    }

    public void setAbbrGenEdRequirements(List<String> abbrGenEdRequirements) {
        this.abbrGenEdRequirements = abbrGenEdRequirements;
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

    public List<String> getScheduledTerms() {
        return scheduledTerms;
    }

    public void setScheduledTerms(List<String> scheduledTerms) {
        this.scheduledTerms = scheduledTerms;
    }

    public boolean isSummaryOnly() {
        return summaryOnly;
    }

    public void setSummaryOnly(boolean summaryOnly) {
        this.summaryOnly = summaryOnly;
    }



    //TODO: Review why we really need this
    //  It's because we need access to more than on property in one of the property editors.
    @JsonIgnore
    public CourseDetails getThis() {
        return this;
    }



}