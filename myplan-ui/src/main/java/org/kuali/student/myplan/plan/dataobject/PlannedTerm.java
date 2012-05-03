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
    // TODO: This is actually the ATPid. FIXME!
    private String planItemId;
    private String qtrYear;

    private List<PlannedCourseDataObject> plannedList = new ArrayList<PlannedCourseDataObject>();
    private List<PlannedCourseDataObject> backupList = new ArrayList<PlannedCourseDataObject>();
    private List<AcademicRecordDataObject> academicRecord=new ArrayList<AcademicRecordDataObject>();
    private String credits = null;

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

    public String getPlanItemId() {
        return planItemId;
    }

    public String getQtrYear() {
        return qtrYear;
    }

    public void setQtrYear(String qtrYear) {
        this.qtrYear = qtrYear;
    }

    public void setPlanItemId(String planItemId) {
        this.planItemId = planItemId;
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
        int totalMin = 0;
        int totalMax = 0;
        for (PlannedCourseDataObject pc : getPlannedList()) {
            String[] str = pc.getCourseDetails().getCredit().split("\\D");
            int min = Integer.parseInt(str[0]);
            totalMin += min;
            int max = Integer.parseInt(str[str.length - 1]);
            totalMax += max;
        }
        String totalCredits = Integer.toString(totalMin);
        if (totalMin != totalMax) {
            totalCredits = totalCredits + "-" + Integer.toString(totalMax);
        }

        return totalCredits;
    }

    public List<AcademicRecordDataObject> getAcademicRecord() {
        return academicRecord;
    }

    public void setAcademicRecord(List<AcademicRecordDataObject> academicRecord) {
        this.academicRecord = academicRecord;
    }
}

