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

    public static enum genEduVal {
        COURSEGENEDREQUIREMENTAOFKIS("IS"),
        COURSEGENEDREQUIREMENTAOFKVLPA("VLPA"),
        COURSEGENEDREQUIREMENTAOFKNW("NW"),
        COURSEGENEDREQUIREMENTWRITING("W"),
        COURSEGENEDREQUIREMENTENGLCOMP("C"),
        COURSEGENEDREQUIREMENTQSR("QSR");


        private final String genEdVal;

        genEduVal(String genEdVal) {
            this.genEdVal = genEdVal;
        }

        public String getGenEdVal() {
            return this.genEdVal;
        }
    }

    public static enum genEduKey {
        IS("COURSEGENEDREQUIREMENTAOFKIS"),
        VLPA("COURSEGENEDREQUIREMENTAOFKVLPA"),
        NW("COURSEGENEDREQUIREMENTAOFKNW"),
        W("COURSEGENEDREQUIREMENTWRITING"),
        C("COURSEGENEDREQUIREMENTENGLCOMP"),
        QSR("COURSEGENEDREQUIREMENTQSR");


        private final String genEduKey;

        genEduKey(String genEduKey) {
            this.genEduKey = genEduKey;
        }

        public String getGenEduKey() {
            return this.genEduKey;
        }
    }

    public static enum genEduKeyForEnum {
        COURSEGENEDREQUIREMENTAOFKIS("kuali.uw.lu.genedreq.IS"),
        COURSEGENEDREQUIREMENTAOFKVLPA("kuali.uw.lu.genedreq.VLPA"),
        COURSEGENEDREQUIREMENTAOFKNW("kuali.uw.lu.genedreq.NW"),
        COURSEGENEDREQUIREMENTWRITING("kuali.uw.lu.genedreq.W"),
        COURSEGENEDREQUIREMENTENGLCOMP("kuali.uw.lu.genedreq.C"),
        COURSEGENEDREQUIREMENTQSR("kuali.uw.lu.genedreq.QSR");


        private final String genEduKeyForEnum;

        genEduKeyForEnum(String genEduKeyForEnum) {
            this.genEduKeyForEnum = genEduKeyForEnum;
        }

        public String getGenEduKeyForEnum() {
            return this.genEduKeyForEnum;
        }
    }

    public static final String COURSE_SEARCH_PAGE = "course_search";

    public static final String COURSE_SEARCH_RESULT_PAGE = "course_search_result";

    //public static final String COURSE_SEARCH_EMPTY_RESULT_PAGE = "course_search_no_results";

    public static final String GEN_EDU_REQUIREMENTS_PREFIX = "courseGenedrequirement";

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

    public static final String CAMPUS_LOCATION = "kuali.uw.org.type.campus";

    public static final String TIME_SCHEDULE_KEY = "TimeScheduleLinkAbbreviation";
    
    public static final String COURSE_OFFERING_INSTITUTE ="kuali.uw.course.offering.institute";

    public static final String ORG_QUERY_SEARCH_BY_TYPE_REQUEST= "org.search.orgInfoByType";

    public static final String ORG_QUERY_PARAM = "org_queryParam_orgType";
}
