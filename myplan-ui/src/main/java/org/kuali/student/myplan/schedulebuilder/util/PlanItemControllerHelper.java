package org.kuali.student.myplan.schedulebuilder.util;

import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public final class PlanItemControllerHelper {

    @Autowired
    private static UserSessionHelper userSessionHelper;

	public static LearningPlan getAuthorizedLearningPlan(PlanItemForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		if (getUserSessionHelper().isAdviser()) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, "Advisor access denied");
			return null;
		}

		LearningPlan plan = form.getLearningPlan();
		if (plan == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Invalid learning plan ID " + form.getLearningPlanId());
			return null;
		}

		String studentId = getUserSessionHelper().getStudentId();
		if (!studentId.equals(plan.getStudentId())) {
			response.sendError(HttpServletResponse.SC_FORBIDDEN, request.getRemoteUser()
					+ " is not allowed to update plan " + plan.getId());
			return null;
		}

		return plan;
	}

	public static PlanItem getValidatedPlanItem(PlanItemForm form, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {

		LearningPlan plan = getAuthorizedLearningPlan(form, request, response);
		if (plan == null)
			return null;

		String planItemId = form.getPlanItemId();

		PlanItem planItem = form.getPlanItem();
		if (planItem == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid plan item ID " + form.getPlanItemId());
			return null;
		}

		if (!planItem.getLearningPlanId().equals(plan.getId())) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					"Plan item " + planItemId + " is not associated with learning plan " + plan.getId() + ", found "
							+ planItem.getLearningPlanId());
			return null;
		}

		if (!PlanConstants.COURSE_TYPE.equals(planItem.getRefObjectType())) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Plan item " + planItemId
					+ " does not refer to a course, found " + planItem.getRefObjectType());
			return null;
		}

		Course course = form.getCourse();
		if (course == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Plan item " + planItemId
					+ " does not refer to a course, found " + planItem.getRefObjectId());
			return null;
		}

		AcademicPlanServiceConstants.ItemCategory expectedCategory = form.getExpectedPlanItemCategory();
		if (expectedCategory != null && !planItem.getTypeKey().equals(expectedCategory)) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Plan item " + planItemId + " not of expected type "
					+ expectedCategory + ", found " + planItem.getTypeKey());
			return null;
		}

		String expectedTermId = form.getTermId();
		if (expectedTermId != null) {
			List<String> planPeriods = planItem.getPlanPeriods();
			if (planPeriods == null || planPeriods.isEmpty() || !expectedTermId.equals(planPeriods.get(0))) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Plan item " + planItemId
						+ " not from expected term " + expectedCategory + ", found " + planItem.getTypeKey());
				return null;
			}
		}

		return planItem;
	}

	private PlanItemControllerHelper() {
	}

    public static UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public static void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        PlanItemControllerHelper.userSessionHelper = userSessionHelper;
    }


}
