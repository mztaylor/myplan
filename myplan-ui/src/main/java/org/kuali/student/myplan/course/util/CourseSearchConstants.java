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

    /**
     * The key of the term drop-down any item.
     */
    public static final String SEARCH_TERM_ANY_ITEM = "any";

    public static final String GEN_EDU_REQUIREMENTS_PREFIX = "course.genedrequirement.";
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

    public static final String CAMPUS_LOCATION_ORG_TYPE = "kuali.org.type.campus";

    public static final String TIME_SCHEDULE_KEY = "TimeScheduleLinkAbbreviation";

    public static final String COURSE_OFFERING_INSTITUTE = "uw.course.offering.institute";

    public static final String ORG_QUERY_SEARCH_BY_TYPE_REQUEST = "org.search.orgInfoByType";

    public static final String ORG_QUERY_SEARCH_SUBJECT_AREAS = "org.search.orgCurriculum";

    public static final String ORG_TYPE_PARAM = "org_queryParam_orgType";

    public static final String SWS_SERVICES_STATUS = "swsServicesStatus";

    public static final String LINK = "<a href=\"%s/student/myplan/inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=%s#%s_tab-%s\">%s</a>";

    public static final String COURSE_DETAILS_URL = "/student/myplan/inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=%s&courseCd=%s";

    public static final String APP_URL = "appserver.url";

    public static final String COURSE_SEARCH_FOR_COURSE_ID = "myplan.course.getCourseTitleAndId";

    public static final String COURSE_SEARCH_FOR_DIVISION_LEVELS = "myplan.clu.division.level";

    public static final String SEARCH_REQUEST_SUBJECT_PARAM = "subject";

    public static final String SEARCH_REQUEST_NUMBER_PARAM = "number"; 
    
    public static final String COURSE_LEVEL_XX = "XX";
    
    public static final String COURSE_LEVEL_ZERO = "00";

    public static final String SEARCH_REQUEST_LAST_SCHEDULED_PARAM = "lastScheduledTerm";

    public static final String COURSE_SEARCH_FORM_VIEW = "CourseSearch-FormView";

    public static final String CAMPUS_PARAM_REGEX = "\\s*,\\s*";

    /*Regex to Split Digits and alphabets Eg: COM 348 --> COM  348*/
    public static final String SPLIT_DIGITS_ALPHABETS = "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)";

    /*Regex for validating the course Code eg: COM 301*/
    public static final String FORMATTED_COURSE_CODE_REGEX = "^[A-Z]{1}[A-Z &]{2,6}\\s[0-9]{3}$";

    /*Regex for validating the un-formatted courses eq:com131 or com    131 */
    public static final String UNFORMATTED_COURSE_CODE_REGEX = "^[a-zA-Z]{1}[a-zA-Z &]{2,7}[0-9]{3}$";

    public static final String COURSE_CODE_WITH_SECTION_REGEX = "^[A-Z]{1}[A-Z &]{2,6}\\s[0-9]{3}\\s[A-Z]{1}[A-Z0-9]{0,1}$";

    public static final String UNFORMATTED_COURSE_PLACE_HOLDER_REGEX = "^[a-zA-Z]{1}[a-zA-Z &]{2,7}[1-9](?i)XX$";

    /* for Enum service search */
    public static final String ENUM_CONTEXT_KEY_SEARCH_PLACEHOLDER_KEY = "uw.enumcontext.academicplan.placeholder";
    public static final String ENUM_CONTEXT_KEY_SEARCH_TYPE            = "enum.search.by.context.key";
    // param_name must be '_' separated, not '.' separated b/c we have code that mutiliates . into _ and that's
    // what it looks for.
    public static final String ENUM_CONTEXT_KEY_SEARCH_PARAM_NAME      = "enum_query_param_context_key";

    /*Activity Constants*/


    public static final String COURSE_COMMENTS = "CourseComments";
    public static final String CURRICULUM_COMMENTS = "CurriculumComments";
    public static final String PRIMARY_ACTIVITY_OFFERING_ID = "PrimaryActivityOfferingId";
    public static final String ACTIVITY_CAMPUS = "Campus";
    public static final String FEE_AMOUNT = "FeeAmount";
    public static final String SLN = "SLN";
    public static final String INSTITUTE_CODE = "InstituteCode";
    public static final String INSTITUTE_NAME = "InstituteName";
    public static final String SECTION_COMMENTS = "SectionComments";
    public static final String SUMMER_TERM = "SummerTerm";
    public static final String PRIMARY_ACTIVITY_OFFERING_CODE = "PrimaryActivityOfferingCode";
    public static final String SERVICE_LEARNING = "ServiceLearning";
    public static final String RESEARCH_CREDIT = "ResearchCredit";
    public static final String DISTANCE_LEARNING = "DistanceLearning";
    public static final String JOINT_SECTIONS = "JointSections";
    public static final String WRITING = "Writing";
    public static final String FINANCIAL_AID_ELIGIBLE = "FinancialAidEligible";
    public static final String ADD_CODE_REQUIRED = "AddCodeRequired";
    public static final String INDEPENDENT_STUDY = "IndependentStudy";
    public static final String ENROLLMENT_RESTRICTIONS = "EnrollmentRestrictions";
    public static final String DUPLICATE_ENROLLMENT_ALLOWED = "DuplicateEnrollmentAllowed";
    public static final String SYLLABUS_DESCRIPTION = "syllabusDescription";

    public static final String SEARCH_REQUEST_CREDITS_DETAILS = "myplan.course.info.credits.details";
    public static final String MYPLAN_SEARCH_RESULTS_MAX = "myplan.search.results.max";

}
