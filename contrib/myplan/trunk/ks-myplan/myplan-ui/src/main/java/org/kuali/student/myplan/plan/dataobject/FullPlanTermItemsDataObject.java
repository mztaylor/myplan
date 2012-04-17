package org.kuali.student.myplan.plan.dataobject;

import org.kuali.student.myplan.course.dataobject.CourseDetails;

import java.util.ArrayList;
import java.util.List;
/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/13/12
 * Time: 1:52 PM
 * To change this template use File | Settings | File Templates.
 */
public class FullPlanTermItemsDataObject {
    private String term;
    private int totalCredits;
    private List<PlanItemDataObject> planItemDataObjects = new ArrayList<PlanItemDataObject>();

    public List<PlanItemDataObject> getPlanItemDataObjects() {
        return planItemDataObjects;
    }

    public void setPlanItemDataObjects(List<PlanItemDataObject> planItemDataObjects) {
        this.planItemDataObjects = planItemDataObjects;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public int getTotalCredits() {
        return totalCredits;
    }

    public void setTotalCredits(int totalCredits) {
        this.totalCredits = totalCredits;
    }
}
