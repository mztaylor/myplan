package org.kuali.student.myplan.academicplan.service;

import org.kuali.student.r2.common.constants.CommonServiceConstants;
import org.kuali.student.r2.common.util.constants.LuServiceConstants;

/**
 * kmuthu Don't forget to add comment
 *
 * @Author kmuthu
 * Date: 1/6/12
 */
public class AcademicPlanServiceConstants {
    public static final String NAMESPACE = CommonServiceConstants.REF_OBJECT_URI_GLOBAL_PREFIX + "acadplan";
    public static final String SERVICE_NAME = "AcademicPlanService";

    public static final String LEARNING_PLAN_TYPE_PLAN = "kuali.academicplan.type.plan";
    public static final String LEARNING_PLAN_TYPE_PLAN_AUDIT = "kuali.academicplan.type.plan.audit";
    public static final String LEARNING_PLAN_TYPE_PLAN_TEMPLATE = "kuali.academicplan.type.plan.template";

    public static final String LEARNING_PLAN_ITEM_TYPE_PLANNED = "kuali.academicplan.item.planned";
    public static final String LEARNING_PLAN_ITEM_TYPE_BACKUP = "kuali.academicplan.item.backup";
    public static final String LEARNING_PLAN_ITEM_TYPE_RECOMMENDED = "kuali.academicplan.item.type.recommended";
    public static final String LEARNING_PLAN_ITEM_TYPE_WISHLIST = "kuali.academicplan.item.wishlist";
    public static final String LEARNING_PLAN_ITEM_TYPE_WHATIF = "'kuali.academicplan.item.whatif";

    public static final String LEARNING_PLAN_ACTIVE_STATE_KEY = "kuali.academicplan.plan.state.active";
    public static final String LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY = "kuali.academicplan.planitem.state.active";
    public static final String LEARNING_PLAN_ITEM_ACCEPTED_STATE_KEY = "kuali.academicplan.planitem.state.accepted";
    public static final String LEARNING_PLAN_ITEM_PROPOSED_STATE_KEY = "kuali.academicplan.planitem.state.proposed";
    public static final String LEARNING_PLAN_ITEM_DRAFT_STATE_KEY = "kuali.academicplan.planitem.state.draft";
    public static final String LEARNING_PLAN_ITEM_PUBLISHED_STATE_KEY = "kuali.academicplan.planitem.state.published";
    public static final String LEARNING_PLAN_ITEM_SHARED_TRUE_KEY = "true";
    public static final String LEARNING_PLAN_ITEM_SHARED_FALSE_KEY = "false";

    /*RefObjTypes*/
    public static final String COURSE_TYPE = LuServiceConstants.CREDIT_COURSE_LU_TYPE_KEY;
    public static final String SECTION_TYPE = "kuali.lui.type.activity.offering";
    // todo change for CM 2.0  to uw.course.genedrequirement
    public static final String PLACE_HOLDER_TYPE_GEN_ED = "uw.course.genedrequirement";  // from KSEM_ENUM_T.enum_key
    public static final String PLACE_HOLDER_TYPE = "uw.academicplan.placeholder";  // from KSEM_ENUM_T.enum_key
    public static final String PLACE_HOLDER_TYPE_COURSE_LEVEL = "uw.cluset.type.course.level";
    public static final String STATEMENT_TYPE = "uw.academicplan.statement";

    /* Keys for storing info in the session. */
    public static final String SESSION_KEY_IS_ADVISER = "kuali.uw.authz.adviser";
    public static final String SESSION_KEY_IS_ADVISER_MANAGE_PLAN = "kuali.uw.authz.managePlan";
    public static final String SESSION_KEY_ADVISER_MAJORS = "kuali.uw.adviser.majors";
    public static final String SESSION_KEY_STUDENT_ID = "kuali.uw.authn.studentId";
    public static final String SESSION_KEY_STUDENT_NAME = "kuali.uw.authn.studentName";
    public static final String SESSION_KEY_STUDENT_NUMBER = "kuali.uw.authn.studentNumber";

    /*External Identifier*/
    public static final String EXTERNAL_IDENTIFIER = "ksap.persist.externalIdentifier";

    /*Dynamic Attributes*/
    public static final String CROSS_LISTED_COURSE_ATTR_KEY = "crossListed";

    /*Regex to Split Digits and alphabets Eg: COM 348 --> COM  348*/
    public static final String SPLIT_DIGITS_ALPHABETS = "(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)";

}
