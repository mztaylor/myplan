package org.kuali.student.myplan.plan.dataobject;

import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;

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

    private List<PlannedCourseDataObject> plannedList = new ArrayList<PlannedCourseDataObject>();
    private List<PlannedCourseDataObject> backupList = new ArrayList<PlannedCourseDataObject>();
    private List<AcademicRecordDataObject> academicRecord = new ArrayList<AcademicRecordDataObject>();
    private String credits = null;


    /*These flags are used for styling, highlighting, naming  of terms in the quarter view */
    private boolean currentTermForView;
    private boolean completedTerm;
    private boolean openForPlanning;


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
        return plannedList;
    }

    public void setPlannedList(List<PlannedCourseDataObject> plannedList) {
        this.plannedList = plannedList;
    }

    public List<PlannedCourseDataObject> getBackupList() {
        return backupList;
    }

    public void setBackupList(List<PlannedCourseDataObject> backupList) {
        this.backupList = backupList;
    }

    public String getCredits() {
        String totalCredits = null;
        int plannedTotalMin = 0;
        int plannedTotalMax = 0;
        if (getPlannedList().size() > 0) {

            for (PlannedCourseDataObject pc : getPlannedList()) {
                if (pc.getCourseDetails() != null) {
                    String[] str = pc.getCourseDetails().getCredit().split("\\D");
                    int min = Integer.parseInt(str[0]);
                    plannedTotalMin += min;
                    int max = Integer.parseInt(str[str.length - 1]);
                    plannedTotalMax += max;

                }
            }
            totalCredits = Integer.toString(plannedTotalMin);

            if (plannedTotalMin != plannedTotalMax) {
                totalCredits = totalCredits + "-" + Integer.toString(plannedTotalMax);

            }
        }
        int academicTotalMin = 0;
        int academicTotalMax = 0;
        if (getAcademicRecord().size() > 0) {

            for (AcademicRecordDataObject ar : getAcademicRecord()) {
                if (ar.getCredit() != null) {
                    String[] str = ar.getCredit().split("\\D");
                    int min = Integer.parseInt(str[0]);
                    academicTotalMin += min;
                    int max = Integer.parseInt(str[str.length - 1]);
                    academicTotalMax += max;
                }
            }
            totalCredits = Integer.toString(academicTotalMin);

            if (academicTotalMin != academicTotalMax) {
                totalCredits = totalCredits + "-" + Integer.toString(academicTotalMax);


            }


        }

        /*TODO:Implement this based on the flags (past,present,future) logic*/
        if (getPlannedList().size() > 0 && getAcademicRecord().size() > 0) {
            if (plannedTotalMin != plannedTotalMax && academicTotalMin != academicTotalMax) {
                int minVal = 0;
                int maxVal = 0;
                minVal = Math.min(plannedTotalMin, academicTotalMin);
                maxVal = plannedTotalMax + academicTotalMax;
                totalCredits = minVal + "-" + maxVal;
            }
            if (plannedTotalMin == plannedTotalMax && academicTotalMin == academicTotalMax) {
                totalCredits = String.valueOf(plannedTotalMin + academicTotalMin);
            }
            if (plannedTotalMin != plannedTotalMax && academicTotalMin == academicTotalMax) {
                int minVal = 0;
                int maxVal = 0;
                minVal = plannedTotalMin;
                maxVal = plannedTotalMax + academicTotalMax;
                totalCredits = minVal + "-" + maxVal;

            }
            if (plannedTotalMin == plannedTotalMax && academicTotalMin != academicTotalMax) {
                int minVal = 0;
                int maxVal = 0;
                minVal = academicTotalMin;
                maxVal = plannedTotalMax + academicTotalMax;
                totalCredits = minVal + "-" + maxVal;
            }
        }
        return totalCredits;
    }

    public void setCredits(String credits) {
        this.credits = credits;
    }


    public List<AcademicRecordDataObject> getAcademicRecord() {
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
}

