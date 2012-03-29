package org.kuali.student.myplan.course.util;

import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.regex.Pattern;

public class PlanConstants extends AcademicPlanServiceConstants {

    public static final String PLAN_ITEM_ID_PARAM_NAME = "planItemId";
    public static final String COURSE_ID = "courseId";
    public static final String TERM_ID = "term";

    public static final String PLAN_ITEM_ADD_PAGE_ID = "add_plan_item_result";
    public static final String PLAN_ITEM_REMOVE_PAGE_ID = "remove_plan_item_result";

    public static final String COURSE_TYPE = LUConstants.CLU_TYPE_CREDIT_COURSE;

    public static final String ERROR_KEY_OPERATION_FAILED = "error.myplan.savedCoursesList.operationFailed";

    public static final String ERROR_KEY_DATA_VALIDATION_ERROR = "error.myplan.savedCoursesList.dataValidationError";
    public static final String ERROR_KEY_DUPLICATE_PLAN = "error.myplan.savedCoursesList.duplicatePlan";
    public static final String ERROR_KEY_DUPLICATE_PLAN_ITEM = "error.myplan.savedCoursesList.duplicatePlanItem";
    public static final String ERROR_KEY_UNKNOWN_PLAN_ITEM = "error.myplan.savedCoursesList.unknownPlanItem";
    public static final String ERROR_KEY_INVALID_PARAM = "error.myplan.savedCoursesList.invalidParameter";
    public static final String ERROR_KEY_MISSING_PARAM = "error.myplan.savedCoursesList.missingParameter";
    public static final String ERROR_KEY_PERMISSION_DENIED = "error.myplan.savedCoursesList.permissionDenied";

    //  Global context info for use in service methods which need caching, but don't use the context argument.
    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

    public static final String OTHER_TERM_KEY = "other";
    public static final String TERM_ID_PREFIX = "kuali.uw.atp.type.";
}
