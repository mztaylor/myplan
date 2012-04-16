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
    private int credits=0;
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

    public int getCredits() {
        return credits;
    }

    public void setCredits(int credits) {
        this.credits = credits;
    }

    public boolean isCurrentTerm() {
        return isCurrentTerm;
    }

    public void setCurrentTerm(boolean currentTerm) {
        isCurrentTerm = currentTerm;
    }
}

