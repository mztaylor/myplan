package org.kuali.student.myplan.sampleplan.util;

import org.kuali.rice.core.api.config.property.ConfigContext;
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

    public static final int SAMPLE_PLAN_YEAR_COUNT = ConfigContext.getCurrentContextConfig().getProperty("samplePlan.year.count") != null ? Integer.parseInt(ConfigContext.getCurrentContextConfig().getProperty("samplePlan.year.count")) : 7;
    public static final int SAMPLE_PLAN_ITEMS_COUNT = ConfigContext.getCurrentContextConfig().getProperty("samplePlan.items.count") != null ? Integer.parseInt(ConfigContext.getCurrentContextConfig().getProperty("samplePlan.items.count")) : 6;


    public static final String SAMPLE_PLAN_YEAR = "YEAR %s";
    public static final String SAMPLE_PLAN_ATP_FORMAT = "%sYear%s";
    public static final String CODE_VALIDATION_ERROR_FORMAT = "samplePlanYears[%s].samplePlanTerms[%s].samplePlanItems[%s].code";
    public static final String ALT_CODE_VALIDATION_ERROR_FORMAT = "samplePlanYears[%s].samplePlanTerms[%s].samplePlanItems[%s].alternateCode";

    public static final String DUPLICATE_ERROR = "myplan.text.error.samplePlan.alreadyExists";
    public static final String REG_COURSE_MISSING = "myplan.text.error.samplePlan.regCourseMissing";


    public static final String PUBLISHED = "Published";
    public static final String DRAFT = "Draft";

    public static final String STATEMENT_TYPE_RECOMMENDED = "kuali.statement.type.academicplan.recommendation";
    public static final String REQ_COMP_TYPE_COURSE = "kuali.reqComponent.type.planitem.course";
    public static final String REQ_COMP_TYPE_PLACEHOLDER = "kuali.reqComponent.type.planitem.placeholder";
    public static final String REQ_COMP_FIELD_TYPE_CREDIT = "kuali.reqComponent.field.type.planitem.credit";
    public static final String REQ_COMP_FIELD_TYPE_COURSE = "kuali.reqComponent.field.type.planitem.course";
    public static final String REQ_COMP_FIELD_TYPE_PLACEHOLDER = "kuali.reqComponent.field.type.planitem.placeholder";
    public static final String REQ_COMP_FIELD_TYPE_COURSE_PLACEHOLDER = "kuali.reqComponent.field.type.planitem.course.placeholder";

    //  Global context info for use in service methods which need caching, but don't use the context argument.
    public static final ContextInfo CONTEXT_INFO = new ContextInfo();
}
