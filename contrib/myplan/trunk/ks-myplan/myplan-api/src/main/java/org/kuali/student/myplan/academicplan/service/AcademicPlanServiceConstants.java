package org.kuali.student.myplan.academicplan.service;

import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.r2.common.constants.CommonServiceConstants;

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
    public static final String LEARNING_PLAN_ITEM_TYPE_WISHLIST = "kuali.academicplan.item.wishlist";
    public static final String LEARNING_PLAN_ITEM_TYPE_WHATIF = "'kuali.academicplan.item.whatif";

    public static final String LEARNING_PLAN_ACTIVE_STATE_KEY = "kuali.academicplan.plan.state.active";
    public static final String LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY = "kuali.academicplan.planitem.state.active";
    public static final String LEARNING_PLAN_ITEM_SHARED_TRUE_KEY = "true";
    public static final String LEARNING_PLAN_ITEM_SHARED_FALSE_KEY = "false";

    /*RefObjTypes*/
    public static final String COURSE_TYPE = LUConstants.CLU_TYPE_CREDIT_COURSE;
    public static final String SECTION_TYPE = "kuali.lui.type.activity.offering";
    public static final String PLACE_HOLDER_TYPE_GEN_ED = "kuali.uw.lu.genedreq";
    public static final String PLACE_HOLDER_TYPE = "uw.academicplan.placeholder";
    public static final String PLACE_HOLDER_TYPE_COURSE_LEVEL = "uw.cluset.type.course.level";

    /* Keys for storing info in the session. */
    public static final String SESSION_KEY_IS_ADVISER = "kuali.uw.authz.adviser";
    public static final String SESSION_KEY_STUDENT_ID = "kuali.uw.authn.studentId";
    public static final String SESSION_KEY_STUDENT_NAME = "kuali.uw.authn.studentName";
    public static final String SESSION_KEY_STUDENT_NUMBER = "kuali.uw.authn.studentNumber";

    /*External Identifier*/
    public static final String EXTERNAL_IDENTIFIER = "ksap.persist.externalIdentifier";

}
