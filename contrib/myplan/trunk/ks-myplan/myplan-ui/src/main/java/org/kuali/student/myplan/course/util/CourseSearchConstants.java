package org.kuali.student.myplan.course.util;

import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: Kamal
 * Date: 11/4/11
 * Time: 10:22 AM
 */
public class CourseSearchConstants {

    public static final String COURSE_SEARCH_PAGE = "course_search";

    public static final String COURSE_SEARCH_RESULT_PAGE = "course_search_result";

    //public static final String COURSE_SEARCH_EMPTY_RESULT_PAGE = "course_search_no_results";

    public static final String GEN_EDU_REQUIREMENTS_PREFIX = "kuali.uw.lu.genedreq.";

    // Service Constants
    public static final String STATEMENT_SERVICE_NAMESPACE = "http://student.kuali.org/wsdl/statement";
    public static final String ENUM_SERVICE_NAMESPACE = "http://student.kuali.org/wsdl/enumerationmanagement";

    //  Global context info for use in service methods which need caching, but don't use the context argument.
    public static final ContextInfo CONTEXT_INFO = new ContextInfo();
    //  Process key for use in service methods which need caching, but don't use the process key argument.
    public static final String PROCESS_KEY = "Null";

    public static final Pattern TERM_PATTERN = Pattern.compile("([a-zA-Z]+)[\\s]+[0-9][0-9]([0-9][0-9])");
}
