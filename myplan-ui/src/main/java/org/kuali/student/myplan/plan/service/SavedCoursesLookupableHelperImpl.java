package org.kuali.student.myplan.plan.service;

import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.course.util.PlanConstants;

import java.util.*;

public class SavedCoursesLookupableHelperImpl extends PlanItemLookupableHelperBase {

    @Override
    protected List<PlanItemDataObject> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        try {
            List<PlanItemDataObject> plannedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
            Collections.sort(plannedCoursesList);
            return plannedCoursesList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
