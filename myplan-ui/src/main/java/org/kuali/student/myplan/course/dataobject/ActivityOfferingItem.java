package org.kuali.student.myplan.course.dataobject;

/**
 * Created with IntelliJ IDEA.
 * User: jasonosgood
 * Date: 12/5/12
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 *
 *
 * https://wiki.cac.washington.edu/display/MyPlan/MyPlan+Course+Section+Details+Data+Needs
 */
public class ActivityOfferingItem {

    private String code;
    private ActivityOfferingType activityOfferingType;
    private int credits;
    // eg MTWThF 10:30-11:20 AM
    private String meetingTime;
    private String location;
    // Section Line Number - UW-ism
    private int sln;
    private boolean enrollRestriction;
    private boolean enrollOpen;
    private int enrollCount;
    private int enrollMaximum;
    private String instructor;
    private String details;

    private boolean distanceLearning;
    private boolean honorsSection;
    private boolean jointOffering;
    private boolean research;
    private boolean writingSection;
    private boolean serviceLearning;
    private boolean newThisYear;
    private boolean ineligibleForFinancialAid;

     public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public ActivityOfferingType getActivityOfferingType() {
        return activityOfferingType;
    }

    public void setActivityOfferingType(ActivityOfferingType activityOfferingType) {
        this.activityOfferingType = activityOfferingType;
    }

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public String getMeetingTime() {
        return meetingTime;
    }

    public void setMeetingTime(String meetingTime) {
        this.meetingTime = meetingTime;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public int getSLN() {
        return sln;
    }

    public void setSLN(int sln) {
        this.sln = sln;
    }

    public boolean isEnrollRestriction() {
        return enrollRestriction;
    }

    public void setEnrollRestriction(boolean enrollRestriction) {
        this.enrollRestriction = enrollRestriction;
    }

    public boolean isEnrollOpen() {
        return enrollOpen;
    }

    public void setEnrollOpen(boolean enrollOpen) {
        this.enrollOpen = enrollOpen;
    }

    public int getEnrollCount() {
        return enrollCount;
    }

    public void setEnrollCount(int enrollCount) {
        this.enrollCount = enrollCount;
    }

    public int getEnrollMaximum() {
        return enrollMaximum;
    }

    public void setEnrollMaximum(int enrollMaximum) {
        this.enrollMaximum = enrollMaximum;
    }

    public String getInstructor() {
        return instructor;
    }

    public void setInstructor(String instructor) {
        this.instructor = instructor;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails( String details ) {
        this.details = details;
    }

    /*
        Other course attribute indicators

     */

    public boolean isDistanceLearning() {
        return distanceLearning;
    }

    public void setDistanceLearning(boolean distanceLearning) {
        this.distanceLearning = distanceLearning;
    }

    public boolean isHonorsSection() {
        return honorsSection;
    }

    public void setHonorsSection(boolean honorsSection) {
        this.honorsSection = honorsSection;
    }

    public boolean isJointOffering() {
        return jointOffering;
    }

    public void setJointOffering(boolean jointOffering) {
        this.jointOffering = jointOffering;
    }

    public boolean isResearch() {
        return research;
    }

    public void setResearch(boolean research) {
        this.research = research;
    }

    public boolean isWritingSection() {
        return writingSection;
    }

    public void setWritingSection(boolean writingSection) {
        this.writingSection = writingSection;
    }

    public boolean isServiceLearning() {
        return serviceLearning;
    }

    public void setServiceLearning(boolean serviceLearning) {
        this.serviceLearning = serviceLearning;
    }

    public boolean isNewThisYear() {
        return newThisYear;
    }

    public void setNewThisYear(boolean newThisYear) {
        this.newThisYear = newThisYear;
    }

    public boolean isIneligibleForFinancialAid() {
        return ineligibleForFinancialAid;
    }

    public void setIneligibleForFinancialAid(boolean ineligibleForFinancialAid) {
        this.ineligibleForFinancialAid = ineligibleForFinancialAid;
    }


}
