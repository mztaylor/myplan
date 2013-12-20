package org.kuali.student.myplan.sampleplan.util;

import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.Arrays;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/8/13
 * Time: 1:20 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePlanConstants {

    public static final List<String> TERM_LABELS_LIST = Arrays.asList("Autumn", "Winter", "Spring", "Summer");
    public static final int SAMPLE_PLAN_YEAR_COUNT = 7;
    public static final int SAMPLE_PLAN_ITEMS_COUNT = 6;
    public static final String SAMPLE_PLAN_YEAR = "YEAR %s";
    public static final String SAMPLE_PLAN_ATP_FORMAT = "%sYear%s";
    public static final String CODE_VALIDATION_ERROR_FORMAT = "samplePlanYears[%s].samplePlanTerms[%s].samplePlanItems[%s].code";
    public static final String DUPLICATE_ERROR = "myplan.text.error.samplePlan.alreadyExists";


    public static final String PUBLISHED = "Published";
    public static final String DRAFT = "Draft";

    public static final String STATEMENT_TYPE_RECOMMENDED = "Kuali.statement.type.academicplan.recommendation";
    public static final String REQ_COMP_TYPE_COURSE = "Kuali.reqComponent.type.planitem.course";
    public static final String REQ_COMP_TYPE_PLACEHOLDER = "Kuali.reqComponent.type.planitem.placeholder";
    public static final String REQ_COMP_FIELD_TYPE_COURSE = "Kuali.reqComponent.field.type.planitem.course";
    public static final String REQ_COMP_FIELD_TYPE_PLACEHOLDER = "Kuali.reqComponent.field.type.planitem.placeholder";


    //  Global context info for use in service methods which need caching, but don't use the context argument.
    public static final ContextInfo CONTEXT_INFO = new ContextInfo();
}
