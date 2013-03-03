package org.kuali.student.myplan.course.dataobject;

import org.kuali.student.myplan.plan.util.AtpHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: jasonosgood
 * Date: 12/5/12
 * Time: 11:06 AM
 * To change this template use File | Settings | File Templates.
 * <p/>
 * <p/>
 * https://wiki.cac.washington.edu/display/MyPlan/MyPlan+Course+Section+Details+Data+Needs
 */
public class ActivityOfferingItem {

    private String code;
    private String campus;
    private String feeAmount;
    private String activityOfferingType;
    private String credits;
    private List<MeetingDetails> meetingDetailsList;

    // Section Line Number - UW-ism
    private String atpId;
    private String registrationCode;
    // Used by the UI to feed the QTRYR query parameter, like in the following:
    // https://sdb.admin.washington.edu/timeschd/uwnetid/registrationCode.asp?QTRYR=WIN+2013&amp;SLN=11944
    private String qtryr;
    private boolean enrollRestriction;
    private boolean enrollOpen;
    private String enrollCount;
    private String enrollMaximum;
    private String enrollEstimate;
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
    private boolean addCodeRequired;
    private boolean independentStudy;
    private String gradingOption;
    private String sectionComments;
    private String summerTerm;

    private String planItemId;

    private boolean primary = false;

    private String instituteCode;
    private String instituteName;


    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    public String getActivityOfferingType() {
        return activityOfferingType;
    }

    public void setActivityOfferingType(String activityOfferingType) {
        this.activityOfferingType = activityOfferingType;
    }

    public String getCredits() {
        return credits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }

    public List<MeetingDetails> getMeetingDetailsList() {
        if (meetingDetailsList == null) {
            meetingDetailsList = new ArrayList<MeetingDetails>();
        }
        return meetingDetailsList;
    }

    public void setMeetingDetailsList(List<MeetingDetails> meetingDetailsList) {
        this.meetingDetailsList = meetingDetailsList;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public String getQtryr() {
        return qtryr;
    }

    public void setQtryr(String qtryr) {
        this.qtryr = qtryr;
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

    public String getEnrollCount() {
        return enrollCount;
    }

    public void setEnrollCount(String enrollCount) {
        this.enrollCount = enrollCount;
    }

    public String getEnrollMaximum() {
        return enrollMaximum;
    }

    public void setEnrollMaximum(String enrollMaximum) {
        this.enrollMaximum = enrollMaximum;
    }

    public String getEnrollEstimate() {
        return enrollEstimate;
    }

    public void setEnrollEstimate(String enrollEstimate) {
        this.enrollEstimate = enrollEstimate;
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

    public void setDetails(String details) {
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

    public String getGradingOption() {
        return gradingOption;
    }

    public void setGradingOption(String gradingOption) {
        this.gradingOption = gradingOption;
    }


    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

//    public List<ActivityOfferingItem> getSecondaryList() {
//        if (secondaryList == null) {
//            secondaryList = new ArrayList<ActivityOfferingItem>();
//        }
//        return secondaryList;
//    }
//    
//    public void setSecondaryList(  List<ActivityOfferingItem> secondaryList ) {
//    	this.secondaryList = secondaryList;
//    }

    public boolean isStandalone() {
        return isPrimary();
//    	return isPrimary() && getSecondaryList().size() == 0;
    }

    public String getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(String planItemId) {
        this.planItemId = planItemId;
    }

    public String getAtpId() {
        return atpId;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }

    public String getFeeAmount() {
        return feeAmount;
    }

    public void setFeeAmount(String feeAmount) {
        this.feeAmount = feeAmount;
    }

    public boolean isAddCodeRequired() {
        return addCodeRequired;
    }

    public void setAddCodeRequired(boolean addCodeRequired) {
        this.addCodeRequired = addCodeRequired;
    }

    public boolean isIndependentStudy() {
        return independentStudy;
    }

    public void setIndependentStudy(boolean independentStudy) {
        this.independentStudy = independentStudy;
    }

    public String getSectionComments() {
        return sectionComments;
    }

    public void setSectionComments(String sectionComments) {
        this.sectionComments = sectionComments;
    }

    public String getInstituteCode() {
        return instituteCode;
    }

    public void setInstituteCode(String instituteCode) {
        this.instituteCode = instituteCode;
    }

    public String getInstituteName() {
        return instituteName;
    }

    public void setInstituteName(String instituteName) {
        this.instituteName = instituteName;
    }

    public String getSummerTerm() {
        return summerTerm;
    }

    public void setSummerTerm(String summerTerm) {
        this.summerTerm = summerTerm;
    }

    /*Used to get the Short term name for atp (WI 13 for kuali.uw.atp.2013.1)*/
    public String getShortTermName() {
        return AtpHelper.atpIdToShortTermName(this.getAtpId());
    }

    /*Used to get the Short term name for atp (Winter 13 for kuali.uw.atp.2013.1)*/
    public String getLongTermName() {
        return AtpHelper.atpIdToTermName(this.getAtpId());
    }

}
