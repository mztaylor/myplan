package org.kuali.student.myplan.sampleplan.controller;

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
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.sampleplan.dataobject.SamplePlanItem;
import org.kuali.student.myplan.sampleplan.dataobject.SamplePlanTerm;
import org.kuali.student.myplan.sampleplan.dataobject.SamplePlanYear;
import org.kuali.student.myplan.sampleplan.form.SamplePlanForm;
import org.kuali.student.myplan.sampleplan.util.SamplePlanConstants;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.myplan.utils.UrlLinkBuilder;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.MetaInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.versionmanagement.dto.VersionInfo;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.service.CourseService;
import org.kuali.student.r2.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.r2.lum.lrc.dto.ResultValueRangeInfo;
import org.kuali.student.r2.lum.lrc.dto.ResultValuesGroupInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.springframework.util.StringUtils.hasText;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 12/12/13
 * Time: 4:04 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:myplan-test-context.xml"})
public class SamplePlanControllerTest {
    private final Logger logger = Logger.getLogger(SamplePlanControllerTest.class);
    @Autowired
    private SamplePlanController samplePlanController;

    @Autowired
    private AcademicPlanService academicPlanService;

    @Autowired
    private CourseHelper courseHelper;

    @Autowired
    public CourseService courseServiceMock;


    @Test
    public void startNewSamplePlanFormTest1() {
        SamplePlanForm samplePlanForm = new SamplePlanForm();
        samplePlanController.startNew(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears()));
    }

    @Test
    public void startNewSamplePlanFormTest2() {
        SamplePlanForm samplePlanForm = new SamplePlanForm();
        LearningPlanInfo learningPlanInfo = createLearningPlan("This is short descr", "Track 1", "00-1-6BSCE", SamplePlanConstants.DRAFT);
        samplePlanForm.setLearningPlanId(learningPlanInfo.getId());
        samplePlanController.startNew(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears()) && samplePlanForm.getSamplePlanYears().size() == 7);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms()) && samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().size() == 4);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems()) && samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().size() == 6);
    }

    @Test
    public void startNewSamplePlanFormTest3() {
        SamplePlanForm samplePlanForm = new SamplePlanForm();
        LearningPlanInfo learningPlanInfo = createLearningPlan("This is short descr", "Track 2", "00-1-6BSCE", SamplePlanConstants.DRAFT);
        String atpId = "20141";
        String subject = "COM";
        String suffix = "210";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        PlanItemInfo planItemInfo = addPlanItem(learningPlanInfo, courseId, PlanConstants.COURSE_TYPE, "AutumnYear1", PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, null, null, null);
        samplePlanForm.setLearningPlanId(learningPlanInfo.getId());
        samplePlanController.startNew(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears()) && samplePlanForm.getSamplePlanYears().size() == 7);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms()) && samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().size() == 4);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems()) && samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().size() == 6);
        assertTrue(StringUtils.hasText(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode()) && samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode().equals(String.format("%s %s", subject, suffix)));
    }

    @Test
    public void startNewSamplePlanFormTest4() {
        SamplePlanForm samplePlanForm = new SamplePlanForm();
        LearningPlanInfo learningPlanInfo = createLearningPlan("This is short descr", "Track 3", "00-1-6BSCE", SamplePlanConstants.DRAFT);
        PlanItemInfo planItemInfo = addPlanItem(learningPlanInfo, "uw.academicplan.placeholder.other", "uw.academicplan.placeholder", "AutumnYear1", PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, null, "5", null);
        samplePlanForm.setLearningPlanId(learningPlanInfo.getId());
        samplePlanController.startNew(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears()) && samplePlanForm.getSamplePlanYears().size() == 7);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms()) && samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().size() == 4);
        assertTrue(!CollectionUtils.isEmpty(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems()) && samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().size() == 6);
        assertTrue(StringUtils.hasText(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode()) && samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode().equals(String.format("%s|%s", "uw.academicplan.placeholder.other", "uw.academicplan.placeholder")));

    }


    /**
     * Testing to save a new Sample plan learning Plan
     */
    @Test
    public void saveSamplePlanTest() {
        SamplePlanForm samplePlanForm = new SamplePlanForm();
        samplePlanForm.setDescription("This is a short Description");
        samplePlanForm.setPlanTitle("Track 1");
        samplePlanForm.setDegreeProgramTitle("00-1-6BSCE");
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(StringUtils.hasText(samplePlanForm.getLearningPlanId()));

         /*Testing sample plan with a empty sample form*/
        samplePlanForm.setSamplePlanYears(getDefaultSamplePlanTable());
        assertTrue(StringUtils.hasText(samplePlanForm.getLearningPlanId()) && StringUtils.isEmpty(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode()));

        /*Adding a non existing course*/
        String atpId = "20141";
        String subject = "CHEM";
        String suffix = "210";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        String versionId = getCourseHelper().getCourseVersionIdByTerm(courseId, atpId);
        addCourseInfo(courseId, versionId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setCode(String.format("%s %s", subject, suffix));
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setNote("This is a note updated Note");
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()) && !CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[0].code")) && PlanConstants.COURSE_NOT_FOUND.equals(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[0].code").get(0).getErrorKey()));
        GlobalVariables.getMessageMap().clearErrorMessages();

        /*Adding a course code to sample plan term AutumnYear1*/
        String atpId1 = "20141";
        String subject1 = "COM";
        String suffix1 = "210";
        String credit1 = "5";
        String courseId1 = getCourseHelper().getCourseIdForTerm(subject1, suffix1, atpId1);
        String versionId1 = getCourseHelper().getCourseVersionIdByTerm(courseId1, atpId1);
        addCourseInfo(courseId1, versionId1, subject1, suffix1, credit1, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setCode(String.format("%s %s", subject1, suffix1));
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setNote("This is a note for planned course");
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode().equals(String.format("%s %s", subject1, suffix1)));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getNote().equals("This is a note for planned course"));

        /*updating course ---> course*/
        String atpId2 = "20141";
        String subject2 = "COM";
        String suffix2 = "220";
        String credit2 = "5";
        String courseId2 = getCourseHelper().getCourseIdForTerm(subject2, suffix2, atpId2);
        String versionId2 = getCourseHelper().getCourseVersionIdByTerm(courseId2, atpId2);
        addCourseInfo(courseId2, versionId2, subject2, suffix2, credit2, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setCode(String.format("%s %s", subject2, suffix2));
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setNote("This is a note updated Note");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setItemIndex(0);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode().equals(String.format("%s %s", subject2, suffix2)));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getNote().equals("This is a note updated Note"));

        /*updating course ---> course PlaceHolder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setCode("COM 2xx");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setNote("This is to test note for COM 2xx");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setItemIndex(0);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode().equals("COM 2xx"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getNote().equals("This is to test note for COM 2xx"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCredit() == null);

        /*updating course PlaceHolder ---> course*/
        String atpId3 = "20141";
        String subject3 = "COM";
        String suffix3 = "220";
        String credit3 = "5";
        String courseId3 = getCourseHelper().getCourseIdForTerm(subject3, suffix3, atpId3);
        String versionId3 = getCourseHelper().getCourseVersionIdByTerm(courseId3, atpId3);
        addCourseInfo(courseId3, versionId3, subject3, suffix3, credit3, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setCode(String.format("%s %s", subject3, suffix3));
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setNote("This is a note updated Note");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).setItemIndex(0);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode().equals(String.format("%s %s", subject3, suffix3)));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getNote().equals("This is a note updated Note"));

        /*Adding a course code to sample plan term AutumnYear1*/
        String atpId4 = "20141";
        String subject4 = "COM";
        String suffix4 = "210";
        String credit4 = "5";
        String courseId4 = getCourseHelper().getCourseIdForTerm(subject4, suffix4, atpId4);
        String versionId4 = getCourseHelper().getCourseVersionIdByTerm(courseId4, atpId4);
        addCourseInfo(courseId4, versionId4, subject4, suffix4, credit4, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCode(String.format("%s %s", subject4, suffix4));
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setNote("This is a note for planned course");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setItemIndex(1);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCode().equals(String.format("%s %s", subject4, suffix4)));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getNote().equals("This is a note for planned course"));


        /*Updating course ---> placeHolder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCode("uw.academicplan.placeholder.elective|uw.academicplan.placeholder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setNote("This is a note for placeHolder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCredit("5");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setItemIndex(1);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCode().equals("uw.academicplan.placeholder.elective|uw.academicplan.placeholder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getNote().equals("This is a note for placeHolder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCredit().equals("5"));

        /*updating placeHolder ---> course PlaceHolder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCode("COM 2xx");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setNote("This is to test note for COM 2xx");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCredit(null);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setItemIndex(1);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCode().equals("COM 2xx"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getNote().equals("This is to test note for COM 2xx"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCredit() == null);

         /*updating course placeHolder ---> course PlaceHolder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCode("MATH 2xx");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCredit("5");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setNote("This is to test note for MATH 2xx");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setItemIndex(1);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCode().equals("MATH 2xx"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getNote().equals("This is to test note for MATH 2xx"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCredit().equals("5"));

        /*Updating course placeholder ---> placeHolder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCode("course.genedrequirement.aofk.vlpa_ind|uw.course.genedrequirement");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setNote("This is a note for placeHolder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCredit("5");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setItemIndex(1);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCode().equals("course.genedrequirement.aofk.vlpa_ind|uw.course.genedrequirement"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getNote().equals("This is a note for placeHolder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCredit().equals("5"));

        /*Updating placeholder ---> placeHolder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCode("uw.academicplan.placeholder.studyabroad|uw.academicplan.placeholder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setNote("This is a note for placeHolder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCredit(null);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setItemIndex(1);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCode().equals("uw.academicplan.placeholder.studyabroad|uw.academicplan.placeholder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getNote().equals("This is a note for placeHolder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCredit() == null);

        /*Updating placeholder ---> course*/
        String atpId5 = "20141";
        String subject5 = "COM";
        String suffix5 = "210";
        String credit5 = "5";
        String courseId5 = getCourseHelper().getCourseIdForTerm(subject5, suffix5, atpId5);
        String versionId5 = getCourseHelper().getCourseVersionIdByTerm(courseId5, atpId5);
        addCourseInfo(courseId5, versionId5, subject5, suffix5, credit5, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setCode(String.format("%s %s", subject5, suffix5));
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setNote("This is a note for planned course");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).setItemIndex(1);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCode().equals(String.format("%s %s", subject5, suffix5)));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getNote().equals("This is a note for planned course"));


        /*Adding a other placeHolder with no note*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCode("uw.academicplan.placeholder.other|uw.academicplan.placeholder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCredit("5");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setItemIndex(2);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()) && !CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code")) && PlanConstants.NOTE_REQUIRED.equals(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code").get(0).getErrorKey()));
        GlobalVariables.getMessageMap().clearErrorMessages();



        /*Adding a non existing course placeHolder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCode("BOY 2xx");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCredit("5");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setNote("This is to test note for MATH 2xx");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setItemIndex(2);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()) && !CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code")) && PlanConstants.CURRIC_NOT_FOUND.equals(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code").get(0).getErrorKey()));
        GlobalVariables.getMessageMap().clearErrorMessages();

        /*Adding a non existing place holder test*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCode("uw.academicplan.placeholder.abcdefg|uw.academicplan.placeholder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setNote("This is a note for placeHolder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setItemIndex(2);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()) && !CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code")) && PlanConstants.ERROR_KEY_UNKNOWN_COURSE.equals(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code").get(0).getErrorKey()));
        GlobalVariables.getMessageMap().clearErrorMessages();

        /*Adding a invalid place holder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCode("uw.academicplan.placeholder.abcdefg|");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setNote("This is a note for placeHolder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setItemIndex(2);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()) && !CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code")) && PlanConstants.ERROR_KEY_UNKNOWN_COURSE.equals(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code").get(0).getErrorKey()));
        GlobalVariables.getMessageMap().clearErrorMessages();


        /*Adding just course code*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCode("MATH");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCredit("5");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setItemIndex(2);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()) && !CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code")) && PlanConstants.ERROR_KEY_UNKNOWN_COURSE.equals(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code").get(0).getErrorKey()));
        GlobalVariables.getMessageMap().clearErrorMessages();

        /*Adding duplicate course in same term*/
        String atpId6 = "20141";
        String subject6 = "COM";
        String suffix6 = "210";
        String credit6 = "5";
        String courseId6 = getCourseHelper().getCourseIdForTerm(subject6, suffix6, atpId6);
        String versionId6 = getCourseHelper().getCourseVersionIdByTerm(courseId6, atpId6);
        addCourseInfo(courseId6, versionId6, subject6, suffix6, credit6, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCode(String.format("%s %s", subject6, suffix6));
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setNote("This is a note for planned course");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setItemIndex(2);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(!CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()) && !CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code")) && PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS.equals(GlobalVariables.getMessageMap().getErrorMessages().get("samplePlanYears[0].samplePlanTerms[0].samplePlanItems[2].code").get(0).getErrorKey()));
        GlobalVariables.getMessageMap().clearErrorMessages();

        /*Adding placeholder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCode("uw.academicplan.placeholder.studyabroad|uw.academicplan.placeholder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setNote("This is a note for placeHolder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setCredit(null);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).setItemIndex(2);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).getCode().equals("uw.academicplan.placeholder.studyabroad|uw.academicplan.placeholder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).getNote().equals("This is a note for placeHolder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).getCredit() == null);

        /*Adding placeholder*/
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).setCode("course.genedrequirement.aofk.is_ind|uw.course.genedrequirement");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).setNote("This is a note for placeHolder");
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).setCredit(null);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).setYearIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).setTermIndex(0);
        samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).setItemIndex(3);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).getCode().equals("course.genedrequirement.aofk.is_ind|uw.course.genedrequirement"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).getNote().equals("This is a note for placeHolder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).getCredit() == null);


        /*Adding course place Holder to another year and term*/
        samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).setCode("COM 2xx");
        samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).setNote("This is to test note for COM 2xx");
        samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).setCredit(null);
        samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).setYearIndex(1);
        samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).setTermIndex(1);
        samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).setItemIndex(0);
        samplePlanController.saveSamplePlan(samplePlanForm, null, null, null);
        assertTrue(CollectionUtils.isEmpty(GlobalVariables.getMessageMap().getErrorMessages()));
        assertTrue(samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).getCode().equals("COM 2xx"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).getNote().equals("This is to test note for COM 2xx"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).getCredit() == null);


        samplePlanController.startNew(samplePlanForm, null, null, null);

        /*Verifying expected items in sample Plan form*/
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(0).getCode().equals(String.format("%s %s", subject3, suffix3)));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(1).getCode().equals(String.format("%s %s", subject5, suffix5)));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(2).getCode().equals("uw.academicplan.placeholder.studyabroad|uw.academicplan.placeholder"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(0).getSamplePlanTerms().get(0).getSamplePlanItems().get(3).getCode().equals("course.genedrequirement.aofk.is_ind|uw.course.genedrequirement"));
        assertTrue(samplePlanForm.getSamplePlanYears().get(1).getSamplePlanTerms().get(1).getSamplePlanItems().get(0).getCode().equals("COM 2xx"));


    }


    /**
     * Create a mock LearningPlan
     *
     * @param description
     * @param programTitle
     * @param planTitle
     * @param state
     * @return
     */
    private LearningPlanInfo createLearningPlan(String description, String programTitle, String planTitle, String state) {
        LearningPlanInfo plan = new LearningPlanInfo();
        plan.setTypeKey(PlanConstants.LEARNING_PLAN_TYPE_PLAN_TEMPLATE);
        RichTextInfo rti = new RichTextInfo();
        CourseLinkBuilder courseLinkBuilder = new CourseLinkBuilder();
        rti.setFormatted(StringUtils.hasText(description) ? courseLinkBuilder.makeLinks(UrlLinkBuilder.buildLinksForText(description)) : "");
        rti.setPlain(StringUtils.hasText(description) ? description : "");
        plan.setShared(true);
        plan.setDescr(rti);
        plan.setStudentId(null);
        plan.setStateKey(state);
        plan.setMeta(new MetaInfo());
        plan.setName(planTitle);
        plan.setPlanProgram(programTitle);
        try {
            return academicPlanService.createLearningPlan(plan, new ContextInfo());
        } catch (Exception e) {
            logger.error("Could not save learningPlan", e);
        }
        return null;
    }

    /**
     * @param plan
     * @param refObjId
     * @param refObjType
     * @param atpId
     * @param planItemType
     * @param note
     * @param credit
     * @param crossListedCourse
     * @return
     * @throws DuplicateEntryException
     */
    private PlanItemInfo addPlanItem(LearningPlan plan, String refObjId, String refObjType, String atpId, String planItemType, String note, String credit, String crossListedCourse)
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

        pii.setStateKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItemType) ? PlanConstants.LEARNING_PLAN_ITEM_PROPOSED_STATE_KEY : PlanConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted(hasText(note) ? note : "");
        rti.setPlain(hasText(note) ? note : "");
        pii.setDescr(rti);

        if (null != atpId) {
            pii.setPlanPeriods(Arrays.asList(atpId));
        }

        if (hasText(credit)) {
            pii.setCredit(new BigDecimal(credit));
        }

        if (!org.apache.commons.lang.StringUtils.isEmpty(crossListedCourse)) {
            List<AttributeInfo> attributeInfos = new ArrayList<AttributeInfo>();
            AttributeInfo attributeInfo = new AttributeInfo(PlanConstants.CROSS_LISTED_COURSE_ATTR_KEY, crossListedCourse);
            attributeInfos.add(attributeInfo);
            pii.setAttributes(attributeInfos);
        }

        try {
            newPlanItem = academicPlanService.createPlanItem(pii, new ContextInfo());
        } catch (AlreadyExistsException e) {
            logger.error("Could not create plan item.", e);
            throw new DuplicateEntryException("plan Item already exists", e);
        } catch (Exception e) {
            logger.error("Could not create plan item.", e);
            throw new RuntimeException("Could not create plan item.", e);
        }

        return newPlanItem;
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

    /**
     * Builds a default template view for sample plans
     *
     * @return
     */
    private List<SamplePlanYear> getDefaultSamplePlanTable() {
        List<SamplePlanYear> samplePlanYears = new ArrayList<SamplePlanYear>();
        for (int i = 1; i <= SamplePlanConstants.SAMPLE_PLAN_YEAR_COUNT; i++) {
            SamplePlanYear samplePlanYear = new SamplePlanYear();
            samplePlanYear.setYearName(String.format(SamplePlanConstants.SAMPLE_PLAN_YEAR, i));
            samplePlanYear.setYear(i);
            List<SamplePlanTerm> samplePlanTerms = samplePlanYear.getSamplePlanTerms();
            for (String term : AtpHelper.getTerms()) {
                SamplePlanTerm samplePlanTerm = new SamplePlanTerm();
                samplePlanTerm.setTermName(term);
                samplePlanTerm.setYear(i);
                List<SamplePlanItem> planItems = samplePlanTerm.getSamplePlanItems();
                for (int j = 1; j <= SamplePlanConstants.SAMPLE_PLAN_ITEMS_COUNT; j++) {
                    planItems.add(new SamplePlanItem());
                }
                samplePlanTerms.add(samplePlanTerm);
            }
            samplePlanYear.setSamplePlanTerms(samplePlanTerms);
            samplePlanYears.add(samplePlanYear);
        }
        return samplePlanYears;
    }

    public SamplePlanController getSamplePlanController() {
        return samplePlanController;
    }

    public void setSamplePlanController(SamplePlanController samplePlanController) {
        this.samplePlanController = samplePlanController;
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
}
