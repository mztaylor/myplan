package org.kuali.student.myplan.course.util;

import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.regex.Pattern;

public class SavedCourseListConstants {

    private static final String PLAN_ITEM_ID_PARAM_NAME = "planItemId";
    private static final String COURSE_ID = "courseId";
    private static final String TERM = "term";

    private static final String COURSE_TYPE = LUConstants.CLU_TYPE_CREDIT_COURSE;

    //  Global context info for use in service methods which need caching, but don't use the context argument.
    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

}
