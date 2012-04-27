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
 *
 */
public class PlannedTerm {
    // TODO: This is actually the ATPid. FIXME!
    private String planItemId;
    private String qtrYear;

    private List<PlanItemDataObject> plannedList=new ArrayList<PlanItemDataObject>();
    private List<PlanItemDataObject> backupList=new ArrayList<PlanItemDataObject>();
    private String credits=null;
    private boolean isCurrentTerm;

    public String getPlanItemId() {
        return planItemId;
    }

    public String getQtrYear() {
        return qtrYear;
    }

    public void setQtrYear(String qtrYear) {
        this.qtrYear = qtrYear;
    }

    public List<PlanItemDataObject> getPlannedList() {
        return plannedList;
    }

    public void setPlannedList(List<PlanItemDataObject> plannedList) {
        this.plannedList = plannedList;
    }

    public void setPlanItemId(String planItemId) {
        this.planItemId = planItemId;
    }


    public List<PlanItemDataObject> getBackupList() {
        return backupList;
    }

    public void setBackupList(List<PlanItemDataObject> backupList) {
        this.backupList = backupList;
    }

    public String getCredits() {
        int totalMin = 0;
        int totalMax = 0;
        for (PlanItemDataObject item : getPlannedList()) {
            String[] str = item.getCourseDetails().getCredit().split("\\D");
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

//    public void setCredits(String credits) {
//        this.credits = credits;
//    }

    public boolean isCurrentTerm() {
        return isCurrentTerm;
    }

    public void setCurrentTerm(boolean currentTerm) {
        isCurrentTerm = currentTerm;
    }
}

