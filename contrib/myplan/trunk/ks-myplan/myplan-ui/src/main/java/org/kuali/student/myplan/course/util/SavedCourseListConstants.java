package org.kuali.student.myplan.course.util;

import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.regex.Pattern;

public class SavedCourseListConstants {

    public static final String PLAN_ITEM_ID_PARAM_NAME = "planItemId";
    public static final String COURSE_ID = "courseId";
    public static final String TERM = "term";

    public static final String PLAN_ITEM_ADD_PAGE_ID = "add_plan_item_result";
    public static final String PLAN_ITEM_REMOVE_PAGE_ID = "remove_plan_item_result";

    public static final String COURSE_TYPE = LUConstants.CLU_TYPE_CREDIT_COURSE;

    //  Global context info for use in service methods which need caching, but don't use the context argument.
    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

}
