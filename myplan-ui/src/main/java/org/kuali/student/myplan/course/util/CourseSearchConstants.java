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

    public static final String GEN_EDU_REQUIREMENTS_PREFIX = "course.genEdRequirement.";
    public static final String CAMPUS_LOCATION_COURSE_ATTRIBUTE = "course.campus";

    // Service Constants
    public static final String STATEMENT_SERVICE_NAMESPACE = "http://student.kuali.org/wsdl/statement";
    public static final String ENUM_SERVICE_NAMESPACE = "http://student.kuali.org/wsdl/enumerationmanagement";
    public static final String ORG_SERVICE_NAMESPACE = "http://student.kuali.org/wsdl/organization";

    //  Global context info for use in service methods which need caching, but don't use the context argument.
    public static final ContextInfo CONTEXT_INFO = new ContextInfo();
    //  Process key for use in service methods which need caching, but don't use the process key argument.
    public static final String PROCESS_KEY = "Null";

    public static final Pattern TERM_PATTERN = Pattern.compile("([a-zA-Z]+)[\\s]+[0-9][0-9]([0-9][0-9])");

    public static final String SUBJECT_AREA = "kuali.lu.subjectArea";

    public static final String CAMPUS_LOCATION_ORG_TYPE = "kuali.uw.org.type.campus";

    public static final String TIME_SCHEDULE_KEY = "TimeScheduleLinkAbbreviation";

    public static final String COURSE_OFFERING_INSTITUTE = "kuali.uw.course.offering.institute";

    public static final String ORG_QUERY_SEARCH_BY_TYPE_REQUEST = "org.search.orgInfoByType";

    public static final String ORG_QUERY_SEARCH_SUBJECT_AREAS = "org.search.orgCurriculum";

    public static final String ORG_TYPE_PARAM = "org_queryParam_orgType";

    public static final String IS_ACADEMIC_CALENDER_SERVICE_UP = "isAcademicCalenderServiceRunning";

    public static final String IS_COURSE_OFFERING_SERVICE_UP = "isCourseOfferingServiceRunning";

    public static final String IS_ACADEMIC_RECORD_SERVICE_UP = "isAcademicRecordServiceRunning";

    public static final String LINK = "<a href=\"%s/student/myplan/inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=%s#%s_tab-%s\">%s</a>";

    public static final String APP_URL = "appserver.url";

    public static final String COURSE_SEARCH_FOR_COURSE_ID = "myplan.course.getcluid";

    public static final String SEARCH_REQUEST_SUBJECT_PARAM = "subject";

    public static final String SEARCH_REQUEST_NUMBER_PARAM = "number";

    public static final String SEARCH_REQUEST_LAST_SCHEDULED_PARAM = "lastScheduledTerm";

    /*Regex to Split Digits and alphabets Eg: COM 348 --> COM  348*/
    public static final String SPLIT_DIGITS_ALPHABETS = "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)";

    /*Regex for validating the course Code eg: COM 301*/
    public static final String COURSE_CODE_REGEX = "^[A-Z]{1}[A-Z &]{2,6}\\s[0-9]{3}$";
    
    public static final String COURSE_CODE_WITH_SECTION_REGEX = "^[A-Z]{1}[A-Z &]{2,6}\\s[0-9]{3}\\s[A-Z]{1}[A-Z0-9]{0,1}$";
    
}
