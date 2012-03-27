package org.kuali.student.myplan.plan.dataobject;

import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.PlanItem;

public class PlannedCoursesItem implements Comparable {

    private PlanItem planItem;

    public PlannedCoursesItem() {
        super();
    }

    public void setPlanItem(PlanItemInfo planItem) {
        this.planItem = planItem;
    }

    public PlanItem getPlanItem() {
        return this.planItem;
    }

    @Override
    public int compareTo( Object object ) {
        PlannedCoursesItem that = (PlannedCoursesItem) object;
        return this.planItem.getMeta().getUpdateTime().compareTo(that.getPlanItem().getMeta().getUpdateTime()) * -1;
    }
}
