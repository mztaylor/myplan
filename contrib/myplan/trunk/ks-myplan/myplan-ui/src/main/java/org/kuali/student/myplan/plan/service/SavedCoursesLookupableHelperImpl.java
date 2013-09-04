package org.kuali.student.myplan.plan.service;

import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SavedCoursesLookupableHelperImpl extends PlanItemLookupableHelperBase {

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Override
    protected List<PlannedCourseDataObject> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        String studentId = getUserSessionHelper().getStudentId();
        try {
            List<PlannedCourseDataObject> plannedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, studentId, false);
            Collections.sort(plannedCoursesList);
            return plannedCoursesList;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public UserSessionHelper getUserSessionHelper() {
        if(userSessionHelper == null){
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}
