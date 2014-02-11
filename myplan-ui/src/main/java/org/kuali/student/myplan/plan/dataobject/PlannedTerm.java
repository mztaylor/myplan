package org.kuali.student.myplan.plan.dataobject;

import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/2/12
 * Time: 3:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlannedTerm {
    private String atpId;
    private String qtrYear;


    private List<PlannedCourseDataObject> plannedList = null;
    private List<PlannedCourseDataObject> backupList = null;
    private List<PlannedCourseDataObject> recommendedList = null;
    private List<AcademicRecordDataObject> academicRecord = null;

    /*These flags are used for help icons to display*/

    private boolean displayCompletedHelp;
    private boolean displayPlannedHelp;
    private boolean displayCreditsHelp = true;
    private boolean displayBackupHelp;
    private boolean displayRegisteredHelp;


    /*These flags are used for styling, highlighting, naming  of terms in the quarter view */
    private boolean currentTermForView;
    private boolean completedTerm;
    private boolean openForPlanning;

    /*This is atpId which used for navigating from the Plan page to Single Quarter view*/
    private String singleQuarterAtp;

    /*Used for schedule builder req param*/
    private String learningPlanId;


    /*
    *  The index of this item in a list of PlannedTerms. This is used by the UI to focus the "carousellite" javascript
    *  component. The value should be -1 unless this PlannedTerm should have focus.
    */
    private int index = -1;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getAtpId() {
        return atpId;
    }

    public String getQtrYear() {
        return qtrYear;
    }

    public void setQtrYear(String qtrYear) {
        this.qtrYear = qtrYear;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }

    public List<PlannedCourseDataObject> getPlannedList() {
        if (plannedList == null) {
            plannedList = new ArrayList<PlannedCourseDataObject>();
        }
        return plannedList;
    }

    public void setPlannedList(List<PlannedCourseDataObject> plannedList) {
        this.plannedList = plannedList;
    }

    public List<PlannedCourseDataObject> getBackupList() {
        if (backupList == null) {
            backupList = new ArrayList<PlannedCourseDataObject>();
        }
        return backupList;
    }

    public void setBackupList(List<PlannedCourseDataObject> backupList) {
        this.backupList = backupList;
    }

    public List<PlannedCourseDataObject> getRecommendedList() {
        if (recommendedList == null) {
            recommendedList = new ArrayList<PlannedCourseDataObject>();
        }
        return recommendedList;
    }

    public void setRecommendedList(List<PlannedCourseDataObject> recommendedList) {
        this.recommendedList = recommendedList;
    }

    private String credits = null;

    public String getCredits() {
        if (credits == null) {

            ArrayList<String> creditList = new ArrayList<String>();
            if (isOpenForPlanning()) {
                for (PlannedCourseDataObject pc : getPlannedList()) {
                    String credit = pc.getCredit();
                    if (credit == null) continue;
                    if ("".equals(credit)) continue;
                    creditList.add(credit);
                }
            }
            for (AcademicRecordDataObject ar : getAcademicRecord()) {
                String credit = ar.getCredit();
                if (credit == null) continue;
                if ("".equals(credit)) continue;
                creditList.add(credit);
            }

            credits = PlannedTermsHelperBase.sumCreditList(creditList);
        }
        return credits;
    }


    public List<AcademicRecordDataObject> getAcademicRecord() {
        if (academicRecord == null) {
            academicRecord = new ArrayList<AcademicRecordDataObject>();
        }
        return academicRecord;
    }

    public void setAcademicRecord(List<AcademicRecordDataObject> academicRecord) {
        this.academicRecord = academicRecord;
    }


    public boolean isCurrentTermForView() {
        return currentTermForView;
    }

    public void setCurrentTermForView(boolean currentTermForView) {
        this.currentTermForView = currentTermForView;
    }

    public boolean isCompletedTerm() {
        return completedTerm;
    }

    public void setCompletedTerm(boolean completedTerm) {
        this.completedTerm = completedTerm;
    }

    public boolean isOpenForPlanning() {
        return openForPlanning;
    }

    public void setOpenForPlanning(boolean openForPlanning) {
        this.openForPlanning = openForPlanning;
    }

    public boolean isDisplayCompletedHelp() {
        return displayCompletedHelp;
    }

    public void setDisplayCompletedHelp(boolean displayCompletedHelp) {
        this.displayCompletedHelp = displayCompletedHelp;
    }

    public boolean isDisplayPlannedHelp() {
        return displayPlannedHelp;
    }

    public void setDisplayPlannedHelp(boolean displayPlannedHelp) {
        this.displayPlannedHelp = displayPlannedHelp;
    }

    public boolean isDisplayCreditsHelp() {
        return displayCreditsHelp;
    }

    public void setDisplayCreditsHelp(boolean displayCreditsHelp) {
        this.displayCreditsHelp = displayCreditsHelp;
    }

    public boolean isDisplayBackupHelp() {
        return displayBackupHelp;
    }

    public void setDisplayBackupHelp(boolean displayBackupHelp) {
        this.displayBackupHelp = displayBackupHelp;
    }

    public boolean isDisplayRegisteredHelp() {
        return displayRegisteredHelp;
    }

    public void setDisplayRegisteredHelp(boolean displayRegisteredHelp) {
        this.displayRegisteredHelp = displayRegisteredHelp;
    }

    public String getSingleQuarterAtp() {
        return singleQuarterAtp;
    }

    public void setSingleQuarterAtp(String singleQuarterAtp) {
        this.singleQuarterAtp = singleQuarterAtp;
    }

    public String getLearningPlanId() {
        return learningPlanId;
    }

    public void setLearningPlanId(String learningPlanId) {
        this.learningPlanId = learningPlanId;
    }
}

