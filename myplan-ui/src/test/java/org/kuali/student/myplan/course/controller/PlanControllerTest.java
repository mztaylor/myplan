package org.kuali.student.myplan.course.controller;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.rice.krad.datadictionary.exception.DuplicateEntryException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.controller.PlanController;
import org.kuali.student.myplan.plan.form.PlanForm;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.MetaInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.core.versionmanagement.dto.VersionInfo;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.service.CourseService;
import org.kuali.student.r2.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.r2.lum.lrc.dto.ResultValueRangeInfo;
import org.kuali.student.r2.lum.lrc.dto.ResultValuesGroupInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.springframework.util.StringUtils.hasText;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/12/12
 * Time: 10:32 AM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:myplan-test-context.xml"})


public class PlanControllerTest {

    private final Logger logger = Logger.getLogger(PlanControllerTest.class);
    @Autowired
    private PlanController planController;

    @Autowired
    public CourseService courseServiceMock;

    @Autowired
    private AcademicPlanService academicPlanService;

    @Autowired
    private CourseHelper courseHelper;

    @Autowired
    private UserSessionHelper userSessionHelper;


    /*Passing a course Id to build the dialog view*/
    @Test
    public void startAddPlannedCourseFormTest() {
        PlanForm planForm = new PlanForm();
        String atpId = "20141";
        String subject = "ENGL";
        String suffix = "242";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        planForm.setCourseId(courseId);
        planForm.setAtpId("20141");
        PlanController controller = getPlanController();
        controller.start(planForm, null, null, null);
        assertTrue(planForm.getCourseSummaryDetails() != null);
    }

    /*passing a planItemId to build the dialog view*/
    @Test
    public void startAddPlannedCourseFormTest2() {
        PlanForm planForm = new PlanForm();
        String atpId = "20141";
        String subject = "ENGL";
        String suffix = "242";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), courseId, PlanConstants.COURSE_TYPE, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().start(planForm, null, null, null);
        assertTrue(planForm.getCourseSummaryDetails() != null);
    }

    /*Adding a new course PlanItem */
    @Test
    public void addPlannedCourseTest() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "202";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        planForm.setCourseId(courseId);
        planForm.setAtpId(atpId);
        planForm.setCode(String.format("%s %s", subject, suffix));
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")));
    }

    /*Adding a course to bookMark*/
    @Test
    public void addSavedCourseTest() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "401";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        planForm.setCourseId(courseId);
        planForm.setAtpId(atpId);
        planForm.setCode(String.format("%s %s", subject, suffix));
        getPlanController().addSavedCourse(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")));
    }


    /*Removing a course planItem*/
    @Test
    public void removePlanItemTest() {
        createStudentUserSession();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "201";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), courseId, PlanConstants.COURSE_TYPE, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().removePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> removeEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED);
        assertTrue(StringUtils.hasText(removeEvents.get("planItemId")) && removeEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(removeEvents.get("atpId")) && removeEvents.get("atpId").equals("20141"));

    }


    /**
     * **********************************************************************************************************************
     * Add, update, copy, move, delete Place holders test cases
     * **********************************************************************************************************************
     */

    @Test
    public void addCoursePlaceHolderTest1() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("COM 2xx");
        planForm.setAtpId("20142");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && StringUtils.hasText(addEvents.get("planItemShortTitle")));

    }

    @Test
    public void addCoursePlaceHolderTest2() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("COM");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equals(PlanConstants.COURSE_NOT_FOUND));

    }

    @Test
    public void addCoursePlaceHolderTest3() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("BLAH 2xx");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.CURRIC_NOT_FOUND));

    }

    @Test
    public void addCoursePlaceHolderTest4() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("COM 2xx");
        planForm.setAtpId("20123");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_HISTORICAL_ATP));

    }

    @Test
    public void addCoursePlaceHolderTest5() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("COM 2xx");
        planForm.setCredit("5");
        planForm.setAtpId("20144");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && StringUtils.hasText(addEvents.get("planItemShortTitle")) && StringUtils.hasText(addEvents.get("credit")));
    }


    @Test
    public void addCoursePlaceHolderTest6() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equals(PlanConstants.EMPTY_SEARCH));

    }


    @Test
    public void addGeneralPlaceHolderTest1() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.elective|uw.academicplan.placeholder");
        planForm.setAtpId("20144");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("Elective"));

    }

    @Test
    public void addGeneralPlaceHolderTest2() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.NOTE_REQUIRED));

    }

    @Test
    public void addGeneralPlaceHolderTest3() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        planForm.setAtpId("20142");
        planForm.setNote("This is a test Note");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("Other"));
        assertTrue(StringUtils.hasText(addEvents.get("note")) && addEvents.get("note").equalsIgnoreCase("This is a test Note"));

    }

    @Test
    public void addGeneralPlaceHolderTest4() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.foreignlanguage|uw.academicplan.placeholder");
        planForm.setAtpId("20143");
        planForm.setNote("This is a test Note");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("FL"));
        assertTrue(StringUtils.hasText(addEvents.get("note")) && addEvents.get("note").equalsIgnoreCase("This is a test Note"));

    }

    @Test
    public void addGeneralPlaceHolderTest5() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.tjhkjdahsd|uw.academicplan.hkfajs");
        planForm.setAtpId("20141");
        planForm.setNote("This is a test Note");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_OPERATION_FAILED));


    }

    @Test
    public void updateCoursePlaceHolderTest1() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "COM 2xx", PlanConstants.PLACE_HOLDER_TYPE, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, "5", "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setCourseCd("MATH 3xx");
        planForm.setCredit("10");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("credit")) && addEvents.get("credit").equals("10"));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equals("MATH 3xx"));
    }

    @Test
    public void updateCoursePlaceHolderTest2() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId("mockId");
        planForm.setCourseCd("MATH 3xx");
        planForm.setCredit("10");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED));

    }

    @Test
    public void updateGeneralPlaceHolderTest1() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.foreignlanguage", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");

        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        planForm.setAtpId("20141");
        planForm.setNote("This is a test Note");
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("Other"));
        assertTrue(StringUtils.hasText(addEvents.get("note")) && addEvents.get("note").equalsIgnoreCase("This is a test Note"));


    }

    @Test
    public void updateGeneralPlaceHolderTest2() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        planForm.setAtpId("20141");
        planForm.setNote("This is a test Note");
        planForm.setPlanItemId("mockId");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED));
    }

    @Test
    public void updateCourseToCoursePlaceHolderTest() {
        createStudentUserSession();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "489";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), courseId, PlanConstants.COURSE_TYPE, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setCourseCd("COM 3xx");
        planForm.setCredit("10");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("credit")) && addEvents.get("credit").equals("10"));
        assertTrue(StringUtils.hasText(addEvents.get("placeHolder")) && addEvents.get("placeHolder").equals("true"));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equals("COM 3xx"));
    }


    @Test
    public void updateCourseToGeneralPlaceHolderTest() {
        createStudentUserSession();
        String atpId = "20141";
        String subject = "ENGL";
        String suffix = "111";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), courseId, PlanConstants.COURSE_TYPE, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        planForm.setAtpId("20141");
        planForm.setNote("This is a test Note");
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("Other"));
        assertTrue(StringUtils.hasText(addEvents.get("note")) && addEvents.get("note").equalsIgnoreCase("This is a test Note"));
        assertTrue(StringUtils.hasText(addEvents.get("placeHolder")) && addEvents.get("placeHolder").equalsIgnoreCase("true"));


    }

    @Test
    public void updateCoursePlaceHolderToGeneralPlaceHolderTest() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "COM 2xx", PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        planForm.setAtpId("20141");
        planForm.setNote("This is a test Note");
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("Other"));
        assertTrue(StringUtils.hasText(addEvents.get("note")) && addEvents.get("note").equalsIgnoreCase("This is a test Note"));
        assertTrue(StringUtils.hasText(addEvents.get("placeHolder")) && addEvents.get("placeHolder").equalsIgnoreCase("true"));


    }

    @Test
    public void updateGeneralPlaceHolderToCoursePlaceHolderTest() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setCourseCd("COM 3xx");
        planForm.setCredit("10");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("credit")) && addEvents.get("credit").equals("10"));
        assertTrue(StringUtils.hasText(addEvents.get("placeHolder")) && addEvents.get("placeHolder").equals("true"));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equals("COM 3xx"));

    }

    @Test
    public void updateGeneralPlaceHolderToCourseTest() {
        createStudentUserSession();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "201";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setCourseCd("COM 201");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("courseId")) && addEvents.get("courseId").equals(courseId));
        assertTrue(StringUtils.hasText(addEvents.get("credit")) && addEvents.get("credit").equals("5"));
        assertTrue(StringUtils.hasText(addEvents.get("placeHolder")) && addEvents.get("placeHolder").equals("false"));

    }

    @Test
    public void updateCoursePlaceHolderToCourseTest() {
        createStudentUserSession();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "201";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "com 2xx", PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setCourseCd("COM 201");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().addUpdatePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("courseId")) && addEvents.get("courseId").equals(courseId));
        assertTrue(StringUtils.hasText(addEvents.get("credit")) && addEvents.get("credit").equals("5"));
        assertTrue(StringUtils.hasText(addEvents.get("placeHolder")) && addEvents.get("placeHolder").equals("false"));

    }

    @Test
    public void copyCoursePlaceHolder() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "com 2xx", PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setAtpId("20143");
        getPlanController().copyPlannedCourse(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && !addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("atpId")) && addEvents.get("atpId").equals("20143"));
    }

    @Test
    public void moveCoursePlaceHolder() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "com 2xx", PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setAtpId("20143");
        getPlanController().movePlannedCourse(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("atpId")) && addEvents.get("atpId").equals("20143"));
    }

    @Test
    public void removeCoursePlaceHolder() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "com 2xx", PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().removePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> removeEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED);
        assertTrue(StringUtils.hasText(removeEvents.get("planItemId")) && removeEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(removeEvents.get("atpId")) && removeEvents.get("atpId").equals("20141"));
    }


    @Test
    public void copyGeneralPlaceHolder() {
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setAtpId("20141");
        getPlanController().copyPlannedCourse(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && !addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("atpId")) && addEvents.get("atpId").equals("20141"));
    }

    @Test
    public void moveGeneralPlaceHolder() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setAtpId("20141");
        getPlanController().movePlannedCourse(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("atpId")) && addEvents.get("atpId").equals("20141"));
    }

    @Test
    public void removeGeneralPlaceHolder() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().removePlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> removeEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED);
        assertTrue(StringUtils.hasText(removeEvents.get("planItemId")) && removeEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(removeEvents.get("atpId")) && removeEvents.get("atpId").equals("20141"));
    }


    /**
     * ********************************************************************************************************************
     * Update Note test cases
     * ********************************************************************************************************************
     */

    @Test
    public void updateNoteTest1() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, "This is original note", "5", "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setAtpId("20141");
        planForm.setNote("This is a updated Note");
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().updateNote(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("Other"));
        assertTrue(StringUtils.hasText(addEvents.get("note")) && addEvents.get("note").equalsIgnoreCase("This is a updated Note"));
        assertTrue(StringUtils.hasText(addEvents.get("placeHolder")) && addEvents.get("placeHolder").equalsIgnoreCase("true"));

    }

    @Test
    public void updateNoteTest2() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, "This is original note", "5", "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setAtpId("20141");
        planForm.setNote("");
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().updateNote(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.NOTE_REQUIRED));

    }

    @Test
    public void updateNoteTest3() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, "5", "730FA4DCAE3411D689DA0004AC494FFE");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setAtpId("20141");
        planForm.setNote("This is a updated Note");
        planForm.setPlanItemId(planItemInfo.getId());
        planForm.setPageId(PlanConstants.ADD_DIALOG_PAGE);
        getPlanController().updateNote(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("Other"));
        assertTrue(StringUtils.hasText(addEvents.get("note")) && addEvents.get("note").equalsIgnoreCase("This is a updated Note"));

    }

    /**
     * ********************************************************************************************************************
     * Recommended courses test cases
     * ********************************************************************************************************************
     */

    /*Adding a recommended course with no issues*/
    @Test
    public void recommendedCourseTest1() {
        createAdviserUserSession();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "220";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

        PlanForm planForm = new PlanForm();
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        planForm.setCourseCd("COM 220");
        planForm.setAtpId("20141");
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.SUCCESS));
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(hasText(addEvents.get("planItemId")));
        assertTrue(addEvents.get("courseId").equals(courseId));

    }

    /*Adding a Course which does not exist*/

    @Test
    public void recommendedCourseTest2() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        planForm.setCourseCd("COM 201");
        planForm.setAtpId("20141");
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_OPERATION_FAILED));

    }


    /*Adding a recommended course to past*/
    @Test
    public void recommendedCourseTest3() {
        createAdviserUserSession();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "201";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

        PlanForm planForm = new PlanForm();
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        planForm.setCourseCd("COM 201");
        planForm.setAtpId("20133");
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_HISTORICAL_ATP));

    }

    /*Adding a recommended course placeholder*/
    @Test
    public void recommendedCourseTest4() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        planForm.setCourseCd("COM 2xx");
        planForm.setAtpId("20141");
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")));

    }


    /*Adding a already existing recommended course */
    @Test
    public void recommendedCourseTest5() {
        createAdviserUserSession();
        String atpId = "20141";
        String subject = "BIOL";
        String suffix = "180";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), courseId, PlanConstants.COURSE_TYPE, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, null, null, "7EDFB986D97A4CD084F26C43813D4B90");
        PlanForm planForm = new PlanForm();
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        planForm.setCourseCd("BIOL 180");
        planForm.setAtpId("20141");
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_OPERATION_FAILED));

    }


    /*Adding a general placeholder recommended course */
    @Test
    public void recommendedCourseTest6() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        planForm.setAtpId("20141");
        planForm.setNote("This is a test Note");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")));
        assertTrue(StringUtils.hasText(addEvents.get("planItemShortTitle")) && addEvents.get("planItemShortTitle").equalsIgnoreCase("Other"));
        assertTrue(StringUtils.hasText(addEvents.get("note")) && addEvents.get("note").equalsIgnoreCase("This is a test Note"));
        assertTrue(StringUtils.hasText(addEvents.get("placeHolder")) && addEvents.get("placeHolder").equalsIgnoreCase("true"));
    }

    /*Adding a other general placeholder recommended course without note */
    @Test
    public void recommendedCourseTest7() {

        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setType(PlanConstants.GENERAL_TYPE);
        planForm.setGeneralPlaceholder("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.NOTE_REQUIRED));

    }

    /*Adding a incomplete course code recommended course */
    @Test
    public void recommendedCourseTest8() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("COM");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equals(PlanConstants.COURSE_NOT_FOUND));

    }

    /*Adding a does not exist course placeholder recommended course */
    @Test
    public void recommendedCourseTest9() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("BLAH 2xx");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.CURRIC_NOT_FOUND));

    }

    /*Adding a blank course placeholder recommended course */
    @Test
    public void recommendedCourseTest10() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.EMPTY_SEARCH));

    }


    /*Adding a junk course placeholder recommended course */
    @Test
    public void recommendedCourseTest11() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("fjhsjflksjfls");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.COURSE_NOT_FOUND));

    }

    /*Adding a junk course placeholder recommended course */
    @Test
    public void recommendedCourseTest12() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("COM 2xx");
        planForm.setAtpId("20141kjhs");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_OPERATION_FAILED));

    }

    /*Adding a multiple course placeholder recommended course */
    @Test
    public void recommendedCourseTest13() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("COM 2xx COM 4xx");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.COURSE_NOT_FOUND));

    }

    /*Adding a recommended course by student*/
    @Test
    public void recommendedCourseTest14() {
        createStudentUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setCourseCd("COM 2xx");
        planForm.setAtpId("20141");
        planForm.setPageId(PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE);
        getPlanController().addRecommendedPlanItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_STUDENT_ACCESS));

    }

    /*Deleting a recommended course*/
    @Test
    public void deleteRecommendedCourseTest1() {
        createAdviserUserSession();
        String atpId = "20141";
        String subject = "COM";
        String suffix = "233";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), courseId, PlanConstants.COURSE_TYPE, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, null, null, "7EDFB986D97A4CD084F26C43813D4B90");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().removeRecommendedItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.SUCCESS));
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("atpId")) && addEvents.get("atpId").equalsIgnoreCase("20141"));
        assertTrue(StringUtils.hasText(addEvents.get("planItemType")) && addEvents.get("planItemType").equalsIgnoreCase("recommended"));
    }


    /*Deleting a recommended placeholder*/
    @Test
    public void deleteRecommendedCourseTest2() {
        createAdviserUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, null, "5", "7EDFB986D97A4CD084F26C43813D4B90");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().removeRecommendedItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.SUCCESS));
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("atpId")) && addEvents.get("atpId").equalsIgnoreCase("20141"));
        assertTrue(StringUtils.hasText(addEvents.get("planItemType")) && addEvents.get("planItemType").equalsIgnoreCase("recommended"));
    }

    /*Deleting a recommended course placeholder*/
    @Test
    public void deleteRecommendedCourseTest3() {
        createAdviserUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "com 2xx", PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, null, null, "7EDFB986D97A4CD084F26C43813D4B90");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().removeRecommendedItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.SUCCESS));
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = planForm.getJavascriptEvents();
        Map<String, String> addEvents = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED);
        assertTrue(StringUtils.hasText(addEvents.get("planItemId")) && addEvents.get("planItemId").equals(planItemInfo.getId()));
        assertTrue(StringUtils.hasText(addEvents.get("atpId")) && addEvents.get("atpId").equalsIgnoreCase("20141"));
        assertTrue(StringUtils.hasText(addEvents.get("planItemType")) && addEvents.get("planItemType").equalsIgnoreCase("recommended"));
    }

    /*Non Owner Deleting a recommended course*/
    @Test
    public void deleteRecommendedCourseTest4() {
        createAdviserUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFF"), "com 2xx", PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, null, null, "CD1C227DE3094092B257CBDD4869CABA");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().removeRecommendedItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_NON_OWNER_ACCESS));

    }

    /*Deleting a non existing recommended course*/
    @Test
    public void deleteRecommendedCourseTest5() {
        createAdviserUserSession();
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId("plan1");
        getPlanController().removeRecommendedItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED));

    }

    /*Student Deleting a recommended course*/
    @Test
    public void deleteRecommendedCourseTest6() {
        createStudentUserSession();
        PlanItemInfo planItemInfo = addPlanItem(getLearningPlan("730FA4DCAE3411D689DA0004AC494FFE"), "com 2xx", PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL, "20141", PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, null, null, "CD1C227DE3094092B257CBDD4869CABA");
        PlanForm planForm = new PlanForm();
        planForm.setPlanItemId(planItemInfo.getId());
        getPlanController().removeRecommendedItem(planForm, null, null, null);
        assertTrue(planForm.getRequestStatus().equals(PlanForm.REQUEST_STATUS.ERROR));
        assertTrue(GlobalVariables.getMessageMap().getErrorMessages().get("plan_item_action_response_page").get(0).getErrorKey().equalsIgnoreCase(PlanConstants.ERROR_KEY_STUDENT_ACCESS));

    }


    /**
     * @param studentId
     * @return
     */
    private LearningPlan getLearningPlan(String studentId) {
        /*
        *  First fetch the student's learning plan.
        */
        List<LearningPlanInfo> learningPlans = null;
        try {
            learningPlans = getAcademicPlanService().getLearningPlansForStudentByType(studentId,
                    PlanConstants.LEARNING_PLAN_TYPE_PLAN, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not fetch plan for user [%s].", studentId), e);
        }

        if (learningPlans == null) {
            throw new RuntimeException(String.format("Could not fetch plan for user [%s]. The query returned null.", studentId));
        }

        //  There should currently only be a single learning plan. This may change in the future.
        if (learningPlans.size() > 1) {
            throw new RuntimeException(String.format("User [%s] has more than one plan.", studentId));
        }

        LearningPlan learningPlan = null;
        if (learningPlans.size() != 0) {
            learningPlan = learningPlans.get(0);
        }

        return learningPlan;
    }


    /**
     * @param plan
     * @param refObjId
     * @param refObjType
     * @param atpId
     * @param planItemType
     * @param note
     * @param credit
     * @return
     * @throws DuplicateEntryException
     */
    private PlanItemInfo addPlanItem(LearningPlan plan, String refObjId, String refObjType, String atpId, String planItemType, String note, String credit, String createId)
            throws DuplicateEntryException {

        if (org.apache.commons.lang.StringUtils.isEmpty(refObjId)) {
            throw new RuntimeException("Empty Course ID");
        }

        PlanItemInfo newPlanItem = null;

        PlanItemInfo pii = new PlanItemInfo();
        pii.setLearningPlanId(plan.getId());
        pii.setTypeKey(planItemType);
        pii.setRefObjectType(refObjType);
        pii.setRefObjectId(refObjId);

        pii.setStateKey(PlanConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted(hasText(note) ? note : "");
        rti.setPlain(hasText(note) ? note : "");
        pii.setDescr(rti);

        if (null != atpId) {
            pii.setPlanPeriods(Arrays.asList(atpId));
        }

        if (isPlaceHolderType(refObjType) && hasText(credit)) {
            pii.setCredit(new BigDecimal(credit));
        }

        MetaInfo metaInfo = new MetaInfo();
        metaInfo.setCreateId(createId);
        pii.setMeta(metaInfo);

        try {
            newPlanItem = getAcademicPlanService().createPlanItem(pii, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not create plan item.", e);
            throw new RuntimeException("Could not create plan item.", e);
        }

        return newPlanItem;
    }

    /**
     * returns true if the reofbjtype is a placeholder
     *
     * @param refObjType
     * @return
     */
    private boolean isPlaceHolderType(String refObjType) {
        return PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(refObjType) || PlanConstants.PLACE_HOLDER_TYPE_GEN_ED.equals(refObjType) || PlanConstants.PLACE_HOLDER_TYPE.equals(refObjType);
    }

    /**
     * Builds a courseInfo and adds it to the CourseServiceMock
     *
     * @param courseId
     * @param versionId
     * @param subject
     * @param suffix
     * @param credit
     * @param creditType
     * @return
     */
    private CourseInfo addCourseInfo(String courseId, String versionId, String subject, String suffix, String credit, String creditType) {

        CourseInfo courseInfo = new CourseInfo();
        courseInfo.setId(courseId);
        courseInfo.setSubjectArea(subject);
        courseInfo.setCourseNumberSuffix(suffix);
        courseInfo.setCode(String.format("%s %s", subject, suffix));
        VersionInfo versionInfo = new VersionInfo();
        versionInfo.setVersionIndId(versionId);
        courseInfo.setVersion(versionInfo);

        List<ResultValuesGroupInfo> options = new ArrayList<ResultValuesGroupInfo>();

        ResultValuesGroupInfo resultComponentInfo = new ResultValuesGroupInfo();
        resultComponentInfo.setType(creditType);

        List<AttributeInfo> attributes = new ArrayList<AttributeInfo>();
        if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED.equals(creditType)) {
            attributes.add(new AttributeInfo(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_FIXED_CREDIT_VALUE, credit));
        } else if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE.equals(creditType)) {
            resultComponentInfo.setResultValueKeys(Arrays.asList(credit.split(",")));
        } else if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE.equals(creditType)) {
            String[] credits = credit.split("-");
            attributes.add(new AttributeInfo(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_MIN_CREDIT_VALUE, credits[0]));
            attributes.add(new AttributeInfo(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_MAX_CREDIT_VALUE, credits[1]));
        }

        ResultValueRangeInfo resultValueRangeInfo = new ResultValueRangeInfo();
        if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED.equals(creditType)) {
            resultValueRangeInfo.setMaxValue(credit);
            resultValueRangeInfo.setMinValue(credit);
        } else if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE.equals(creditType)) {
            String[] credits = credit.split(",");
            resultValueRangeInfo.setMaxValue(credits[0]);
            resultValueRangeInfo.setMinValue(credits[1]);
        } else if (CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE.equals(creditType)) {
            String[] credits = credit.split("-");
            resultValueRangeInfo.setMaxValue(credits[0]);
            resultValueRangeInfo.setMinValue(credits[1]);
        }

        resultComponentInfo.setResultValueRange(resultValueRangeInfo);
        options.add(resultComponentInfo);
        courseInfo.setCreditOptions(options);

        try {
            getCourseServiceMock().createCourse(courseInfo, DegreeAuditConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("failed to create course");
        }

        return courseInfo;
    }

    private void createStudentUserSession() {
        makeDefaultUserSession("730FA4DCAE3411D689DA0004AC494FFE", "JJulius", "false", "000018235", "1033333");
    }

    private void createAdviserUserSession() {
        makeDefaultUserSession("7EDFB986D97A4CD084F26C43813D4B90", "Tom", "true", null, null);
    }


    /**
     * Creates a mock user session
     *
     * @param principleId
     * @param name
     * @param isAdviser
     * @param externalIdentifier
     */
    private void makeDefaultUserSession(String principleId, String name, String isAdviser, String externalIdentifier, String studentNumber) {
        getUserSessionHelper().getName(String.format("principleId=%s|name=%s|isAdviser=%s|externalIdentifier=%s|studentNumber=%s", principleId, name, isAdviser, externalIdentifier, studentNumber));
    }


    public AcademicPlanService getAcademicPlanService() {
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }

    public CourseHelper getCourseHelper() {
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    public CourseService getCourseServiceMock() {
        return courseServiceMock;
    }

    public void setCourseServiceMock(CourseService courseServiceMock) {
        this.courseServiceMock = courseServiceMock;
    }

    public PlanController getPlanController() {
        return planController;
    }

    public void setPlanController(PlanController planController) {
        this.planController = planController;
    }

    public UserSessionHelper getUserSessionHelper() {
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}
