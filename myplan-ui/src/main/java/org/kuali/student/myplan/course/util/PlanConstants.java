package org.kuali.student.myplan.course.util;

import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.regex.Pattern;

public class PlanConstants extends AcademicPlanServiceConstants {

    public static final String PLAN_ITEM_ID_PARAM_NAME = "planItemId";
    public static final String COURSE_ID = "courseId";
    public static final String TERM_ID = "term";

    public static final int PLANNED_PLAN_ITEM_CAPACITY = 8;
    public static final int BACKUP_PLAN_ITEM_CAPACITY = 8;

    public static final String PLAN_ITEM_RESPONSE_PAGE_ID = "plan_item_action_response_page";

    public static final String COURSE_TYPE = LUConstants.CLU_TYPE_CREDIT_COURSE;

    // CRUD operations positive feedback.
    public static final String SUCCESS_KEY = "myplan.text.success";
    public static final String SUCCESS_PLAN_ITEM_ADDED_KEY = "myplan.text.success.planItemAdded";
    public static final String SUCCESS_PLAN_ITEM_MOVED_KEY = "myplan.text.success.planItemMoved";
    public static final String SUCCESS_PLAN_ITEM_COPIED_KEY = "myplan.text.success.planItemCopied";
    public static final String SUCCESS_PLAN_ITEM_DELETED_KEY = "myplan.text.success.planItemCopied";
    public static final String SUCCESS_PLAN_ITEM_MARKED_BACKUP_KEY = "myplan.text.success.planItemDeleted";
    public static final String SUCCESS_PLAN_ITEM_MARKED_PLANNED_KEY = "myplan.text.success.planItemMarkedAsPlanned";

    //  CRUD error feedback
    public static final String ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS = "myplan.text.error.plannedCourseList.itemAlreadyExists";
    public static final String ERROR_KEY_PLANNED_ITEM_CAPACITY_EXCEEDED = "myplan.text.error.plannedCourseList.plannedCapacityExceeded";
    public static final String ERROR_KEY_BACKUP_ITEM_CAPACITY_EXCEEDED = "myplan.text.error.plannedCourseList.backupCapacityExceeded";

    public static final String ERROR_KEY_HISTORICAL_ATP = "myplan.text.error.plannedCourseList.historicalAtp";

    public static final String ERROR_KEY_PAGE_RESET_REQUIRED = "myplan.text.error.pageResetRequired";

    public static final String ERROR_KEY_OPERATION_FAILED = "myplan.text.error.operationFailed";
    public static final String ERROR_KEY_UNKNOWN_PLAN_ITEM = "myplan.text.error.savedCoursesList.unknownPlanItem";
    public static final String ERROR_KEY_DATA_VALIDATION_ERROR = "myplan.text.error.dataValidationError";
    public static final String ERROR_KEY_DUPLICATE_PLAN = "myplan.text.error.savedCoursesList.duplicatePlan";
    public static final String ERROR_KEY_DUPLICATE_PLAN_ITEM = "myplan.text.error.savedCoursesList.duplicatePlanItem";
    public static final String ERROR_KEY_INVALID_PARAM = "myplan.text.error.invalidParameter";
    public static final String ERROR_KEY_MISSING_PARAM = "myplan.text.error.missingParameter";
    public static final String ERROR_KEY_PERMISSION_DENIED = "myplan.text.error.permissionDenied";

    //  Global context info for use in service methods which need caching, but don't use the context argument.
    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

    public static final String OTHER_TERM_KEY = "other";
    public static final String TERM_ID_PREFIX = "kuali.uw.atp.";
    public static final String FOCUS_ATP_ID_KEY = "focusAtpId";
    
    
    /*Term Names Autumn,Winter,Spring,Summer*/
    public static final String TERM_1="Winter";
    public static final String TERM_2="Spring";
    public static final String TERM_3="Summer";
    public static final String TERM_4="Autumn";


    /**
     * Names of javascript events that can be scheduled in response to the outcome of a plan item request.
     */
    public static enum JS_EVENT_NAME {
        /* (atpId), type, courseId, courseCode, courseTitle, courseCredits */
        PLAN_ITEM_ADDED,
        /* atpId, type, courseId */
        PLAN_ITEM_DELETED,
        /* atpId, newTotalCredits */
        UPDATE_NEW_TERM_TOTAL_CREDITS,
        /*atpId, oldTotalCredits*/
        UPDATE_OLD_TERM_TOTAL_CREDITS
    }
}
