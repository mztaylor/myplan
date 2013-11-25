package org.kuali.student.myplan;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.audit.form.PlanAuditForm;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.core.versionmanagement.dto.VersionInfo;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.service.CourseService;
import org.kuali.student.r2.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.r2.lum.lrc.dto.ResultValuesGroupInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 7/17/13
 * Time: 12:30 PM
 * To change this template use File | Settings | File Templates.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:myplan-test-context.xml"})
public class DegreeAuditHelperTest {

    private final Logger logger = Logger.getLogger(DegreeAuditHelperTest.class);

    @Autowired
    public DegreeAuditHelper degreeAuditHelper;

    @Autowired
    public AcademicPlanService academicPlanService;

    @Autowired
    public CourseService courseServiceMock;

    @Autowired
    public CourseHelper courseHelper;

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

    public AcademicPlanService getAcademicPlanService() {
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }

    public DegreeAuditHelper getDegreeAuditHelper() {
        return degreeAuditHelper;
    }

    public void setDegreeAuditHelper(DegreeAuditHelper degreeAuditHelper) {
        this.degreeAuditHelper = degreeAuditHelper;
    }

    /**
     * verify that course which have been retired since being added to the plan are not submitted for a plan audit
     * (appear in ignored list)
     */
    @Test
    public void testHandOffCase1() {

        /* No example to check this unless we manipulate the DB to check this case so commented

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String courseId = "674aca4c-45df-4968-9113-5f87b9846c43";
        String versionId = "1d1c4b53-70e0-4307-a076-6701b67726aa";
        String atpId = "kuali.uw.atp.2014.1";
        String subject = "ENGL";
        String suffix = "202";
        String credit = "5";       

        addPlanItem("planItem1", versionId, PlanConstants.COURSE_TYPE, atpId, true);

        addCourseInfo(courseId, versionId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

        PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

        assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
        assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
        assertTrue(!CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
        assertTrue(planAuditForm.getIgnoreList().size() == 1);
        assertTrue(planAuditForm.getIgnoreList().get(0).getCourseItemList().size() == 1);
        assertTrue(planAuditForm.getIgnoreList().get(0).getCourseItemList().get(0).getCourseId().equals(versionId));*/
    }


    /**
     * verify that when all selected sections of a course have been withdrawn since being added to the plan,
     * the course is not submitted for a plan audit (appears in ignored list).
     */
    @Test
    public void testHandOffCase2() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String activityPlan = "2013:4:ENGL:242:B";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "ENGL";
        String suffix = "242";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", activityPlan, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getIgnoreList().size() == 1);
            assertTrue(planAuditForm.getIgnoreList().get(0).getCourseItemList().size() == 1);
            assertTrue(planAuditForm.getIgnoreList().get(0).getCourseItemList().get(0).getCourseId().equals(courseId));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /*No Hand-off screen needed for Future, unscheduled quarters and Scheduled quarters with no planned courses*/
    @Test
    public void testHandOffCase3() {
        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";

        try {
            getAcademicPlanService().deletePlanItemSet(null, new ContextInfo());
        } catch (Exception e) {
            logger.error("failed to delete plan");
        }

        PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

        assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
        assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
        assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
    }

    /*No Hand-off screen needed for Future, unscheduled quarters with planned course(s) with non-variable credit*/
    @Test
    public void testHandOffCase4() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2014.4";
        String subject = "ENGL";
        String suffix = "242";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course, no section selected, all section profiles the same
     * Section A 3
     * Section B 3
     * Section C 3
     * Expected: No hand-off – uses section A
     */
    @Test
    public void testHandOffCase5() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "ENGL";
        String suffix = "102";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("A"));
        } else {
            logger.error("Could not find course for test");
        }
    }


    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course, one section selected with non-variable credit
     * Section A 3
     * Section B 3 W
     * Section C 3 planned
     * Expected: No hand-off – uses section C
     */
    @Test
    public void testHandOffCase6() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String activityPlan = "2013:4:ENGL:242:C";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "ENGL";
        String suffix = "242";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", activityPlan, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("C"));
        } else {
            logger.error("Could not find course for test");
        }
    }


    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course, one section selected with non-variable credit
     * Section A 3
     * Section B 3 CR/NC  planned
     * Expected: No hand-off – uses section B
     */
    @Test
    public void testHandOffCase7() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String activityPlan = "2013:4:BIOL:502:B";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "BIOL";
        String suffix = "502";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", activityPlan, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("B"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course,
     * one primary section with non-variable credit selected, one secondary section selected
     * Section A 3  planned
     * Section AA
     * Section AD H planned
     * Expected: No hand-off – uses section A and secondary AD
     */
    @Test
    public void testHandOffCase8() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String primaryActivityPlan = "2013:4:INFO:101:A";
        String secondaryActivityPlan = "2013:4:INFO:101:AD";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "INFO";
        String suffix = "101";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", primaryActivityPlan, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem3", secondaryActivityPlan, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("A"));
            assertTrue(planAuditForm.getCleanList().get(0).getSecondaryActivityCode().equalsIgnoreCase("AD"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course,
     * one primary section with non-variable credit selected, multiple secondary sections with same profile selected
     * Section A 3  planned
     * Section AA   planned
     * Section AC   planned
     * Section AD H
     * Expected: No hand-off – uses section A and secondary AA
     */
    @Test
    public void testHandOffCase9() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String primaryActivityPlan = "2013:4:INFO:101:A";
        String secondaryActivityPlan1 = "2013:4:INFO:101:AA";
        String secondaryActivityPlan2 = "2013:4:INFO:101:AC";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "INFO";
        String suffix = "101";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", primaryActivityPlan, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem3", secondaryActivityPlan1, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem4", secondaryActivityPlan2, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("A"));
            assertTrue(planAuditForm.getCleanList().get(0).getSecondaryActivityCode().equalsIgnoreCase("AA"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course,
     * multiple sections available with different profiles,
     * multiple sections selected but selected sections all have same profile
     * Section A 3  planned
     * Section B 3 W
     * Section C 3  planned
     * Expected: No hand-off – uses section A
     */
    @Test
    public void testHandOffCase10() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String primaryActivityPlan1 = "2013:4:ENGL:242:A";
        String primaryActivityPlan2 = "2013:4:ENGL:242:C";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "ENGL";
        String suffix = "242";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", primaryActivityPlan1, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem1", primaryActivityPlan2, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("A"));
        } else {
            logger.error("Could not find course for test");
        }
    }


    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course,
     * no section selected,
     * multiple sections available but all have same profile – the only exception being an Honors section
     * Section A 5 H
     * Section B 5
     * Expected: No hand-off – uses section B
     */
    @Test
    public void testHandOffCase11() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "ENGL";
        String suffix = "281";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("B"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course,
     * no section selected,
     * multiple sections available but all have same profile – the only exception being a Credit/No Credit section
     * Section A 5
     * Section B 5 CR/NC
     * Expected: No hand-off – uses section A
     */
    @Test
    public void testHandOffCase12() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "BIOL";
        String suffix = "502";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("A"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course,
     * no section selected,
     * multiple sections available, all sections are non-default (Honors or CR/NC)
     * Section A 5 H
     * Section B 5 H
     * Expected: No hand-off – uses section A
     */
    @Test
    public void testHandOffCase13() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "ENGL";
        String suffix = "494";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("A"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course,
     * no section selected,
     * one non-default section available (Honors or CR/NC).
     * Section A 5 CR/NC
     * Expected: No hand-off – uses section A
     */
    @Test
    public void testHandOffCase14() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "BIO A";
        String suffix = "590";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("A"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * No Hand-off screen needed for Future, Scheduled quarters planned course,
     * no section selected,
     * one non-default section available (Honors or CR/NC).
     * Section A 5 H
     * Expected: No hand-off – uses section A
     */
    @Test
    public void testHandOffCase15() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "CHEM";
        String suffix = "335";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);
        if (courseId != null) {

            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getSectionCode().equalsIgnoreCase("A"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * Hand-off screen needed For ignore list only,
     * one planned course which requires no hand-off and one course to ignore
     */
    @Test
    public void testHandOffCase16() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId1 = "kuali.uw.atp.2014.4";
        String atpId2 = "kuali.uw.atp.2013.4";
        String subject1 = "CHEM";
        String subject2 = "ENGL";
        String suffix1 = "335";
        String suffix2 = "101";
        String credit = "5";
        String courseId1 = getCourseHelper().getCourseIdForTerm(subject1, suffix1, atpId1);
        String courseId2 = getCourseHelper().getCourseIdForTerm(subject2, suffix2, atpId2);

        if (courseId1 != null && courseId2 != null) {
            addPlanItem("planItem1", courseId1, PlanConstants.COURSE_TYPE, atpId1, true);
            addPlanItem("planItem2", courseId2, PlanConstants.COURSE_TYPE, atpId2, false);

            addCourseInfo(courseId1, courseId1, subject1, suffix1, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);
            addCourseInfo(courseId2, courseId2, subject2, suffix2, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));

            assertTrue(planAuditForm.getCleanList().size() == 1);
            assertTrue(planAuditForm.getCleanList().get(0).getAtpId().equals(atpId1));
            assertTrue(planAuditForm.getCleanList().get(0).getCourseId().equals(courseId1));

            assertTrue(planAuditForm.getIgnoreList().size() == 1);
            assertTrue(planAuditForm.getIgnoreList().get(0).getCourseItemList().size() == 1);
            assertTrue(planAuditForm.getIgnoreList().get(0).getCourseItemList().get(0).getAtpId().equals(atpId2));
            assertTrue(planAuditForm.getIgnoreList().get(0).getCourseItemList().get(0).getCourseId().equals(courseId2));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * Hand-off screen needed Future, unscheduled quarters,
     * planned course(s) with variable credit
     */
    @Test
    public void testHandOffCase17() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2014.4";
        String subject = "CHEM";
        String suffix = "335";
        String credit = "5,8";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 2);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("::5:5"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("::8:8"));
        } else {
            logger.error("Could not find course for test");
        }

    }

    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, no section selected, multiple sections available with different profiles
     * Section A 5 W
     * Section B 3
     * Section D 5
     * <p/>
     * Expected: hand-off – shows
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 5  W  -------> A::5:5 -- Writing
     * 3     -------> B::3:3
     * 5     -------> D:DA:5:5
     */
    @Test
    public void testHandOffCase18() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "BIOL";
        String suffix = "250";
        String credit = "3,5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 3);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::5:5 -- Writing"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("D:DA:5:5"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::3:3"));
        } else {
            logger.error("Could not find course for test");
        }
    }


    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, no section selected, multiple sections available with different profiles
     * Section A 1-15
     * Section B 6 W
     * <p/>
     * Expected: hand-off – shows
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1     -------> A::1:1
     * 2     -------> A::2:2
     * 3     -------> A::3:3
     * ...
     * 6     -------> A::6:6
     * 6 W   -------> B::6:6 -- Writing
     * ...
     * 15    -------> A::15:15
     */
    @Test
    public void testHandOffCase19() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "BIOL";
        String suffix = "479";
        String credit = "1-15";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 16);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::6:6"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::15:15"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::6:6 -- Writing"));
        } else {
            logger.error("Could not find course for test");
        }

    }


    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, no section selected, multiple sections available with different profiles
     * Section A 5 W
     * Section B 5
     * Section D 5
     * <p/>
     * Expected: hand-off – shows
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 5  W  -------> A::5:5 -- Writing
     * 5     -------> D::5:5
     */
    @Test
    public void testHandOffCase20() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "ENGL";
        String suffix = "242";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 2);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("D::5:5"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::5:5 -- Writing"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, no section selected, multiple sections available with different profiles
     * Section A 1-5
     * Section B 2
     * Section D 1-5 H
     * <p/>
     * Expected: hand-off – shows with un-planned Honors section D is ignored
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1    -------> A::1:1
     * 2    -------> A::2:2
     * 3    -------> A::3:3
     * 4    -------> A::4:4
     * 5    -------> A::5:5
     */
    @Test
    public void testHandOffCase21() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "DRAMA";
        String suffix = "499";
        String credit = "1-5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 5);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::2:2"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::3:3"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::4:4"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::5:5"));
            assertTrue(!planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("D::1:1 -- Honors"));
        } else {
            logger.error("Could not find course for test");
        }
    }


    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, no section selected, only section(s) available have variable credits
     * Section A 1-25
     * Section B 1-25
     * <p/>
     * Expected: hand-off – shows
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1     -------> A::1:1
     * 2     -------> A::2:2
     * 3     -------> A::3:3
     * ...
     * 6     -------> A::6:6
     * ...
     * 25    -------> A::25:25
     */
    @Test
    public void testHandOffCase22() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "GREEK";
        String suffix = "490";
        String credit = "1-25";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 25);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::6:6"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::15:15"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::20:20"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::25:25"));
        } else {
            logger.error("Could not find course for test");
        }

    }

    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, one section selected with variable credit
     * Section A 1-15 planned
     * Section B 6 W
     * <p/>
     * Expected: hand-off – shows  ignored Section B as it is not planned
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1     -------> A::1:1
     * 2     -------> A::2:2
     * 3     -------> A::3:3
     * ...
     * 6     -------> A::6:6
     * ...
     * 15    -------> A::15:15
     */
    @Test
    public void testHandOffCase23() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String activityPlan = "2013:4:BIOL:479:A";
        String subject = "BIOL";
        String suffix = "479";
        String credit = "1-15";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", activityPlan, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 15);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::6:6"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::15:15"));
            assertTrue(!planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::6:6 -- Writing"));
        } else {
            logger.error("Could not find course for test");
        }

    }

    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, multiple sections selected, selected sections do not all have the same profiles
     * Section A 5 W planned
     * Section C 5
     * Section D 5   planned
     * <p/>
     * Expected: hand-off – shows
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 5  W  -------> A::5:5 -- Writing
     * 5     -------> D::5:5
     */
    @Test
    public void testHandOffCase24() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String primaryActivity1 = "2013:4:ENGL:242:A";
        String primaryActivity2 = "2013:4:ENGL:242:D";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "ENGL";
        String suffix = "242";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", primaryActivity1, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem3", primaryActivity2, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 2);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("D::5:5"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::5:5 -- Writing"));
        } else {
            logger.error("Could not find course for test");
        }
    }

    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, one section selected with variable credit
     * Section A 1-10       planned
     * Section B H 1-10     planned
     * Section C CR/NC 1-10
     * <p/>
     * Expected: hand-off – shows  ignored Section B as it is not planned
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1     -------> A::1:1
     * ...
     * 10    -------> A::10:10
     * 1 H     -------> B::1:1 -- Honors
     * ...
     * 10 H   -------> B::10:10 -- Honors
     */
    @Test
    public void testHandOffCase25() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String activityPlan1 = "2013:4:PHYS:401:A";
        String activityPlan2 = "2013:4:PHYS:401:B";
        String subject = "PHYS";
        String suffix = "401";
        String credit = "1-25";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", activityPlan1, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem3", activityPlan2, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 20);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::6:6"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::10:10"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::1:1 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::8:8 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::10:10 -- Honors"));
        } else {
            logger.error("Could not find course for test");
        }

    }

    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, one section selected with variable credit
     * Section A 1-10       planned
     * Section B 1-10       planned
     * Section C CR/NC 1-10 planned
     * <p/>
     * Expected: hand-off – shows  ignored Section B as it is not planned
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1     -------> A::1:1
     * ...
     * 10    -------> A::10:10
     * 1 CR/NC     -------> C::1:1 -- Credit/No-Credit grading
     * ...
     * 10 CR/NC   -------> C::10:10 -- Credit/No-Credit grading
     */
    @Test
    public void testHandOffCase26() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String activityPlan1 = "2013:4:MATH:600:A";
        String activityPlan2 = "2013:4:MATH:600:B";
        String activityPlan3 = "2013:4:MATH:600:C";
        String subject = "MATH";
        String suffix = "600";
        String credit = "1-25";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", activityPlan1, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem3", activityPlan2, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem4", activityPlan3, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 20);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::6:6"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::10:10"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("C::1:1 -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("C::6:6 -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("C::10:10 -- Credit/No-Credit grading"));
        } else {
            logger.error("Could not find course for test");
        }

    }

    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, one primary selected, multiple secondary sections with different profiles selected
     * Section A 5  planned
     * Section AA   panned
     * Section AD W H  planned
     * <p/>
     * Expected: hand-off – shows
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 5  H  -------> A:AD:5:5 -- Honors
     * 5     -------> A:AA:5:5
     */
    @Test
    public void testHandOffCase27() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String primaryActivityPlan = "2013:4:INFO:101:A";
        String secondaryActivityPlan1 = "2013:4:INFO:101:AA";
        String secondaryActivityPlan2 = "2013:4:INFO:101:AD";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "INFO";
        String suffix = "101";
        String credit = "5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", primaryActivityPlan, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem3", secondaryActivityPlan1, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem4", secondaryActivityPlan2, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 2);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A:AA:5:5"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A:AD:5:5 -- Honors"));
        } else {
            logger.error("Could not find course for test");
        }

    }


    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, no sections selected, multiple sections with different profiles,
     * all are non-default (Honors and/or CR/NC).
     * Section A 1 H CR/NC  planned
     * Section B 1 H        planned
     * Section C 3 H        planned
     * <p/>
     * Expected: hand-off – shows
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1  H CR/NC  -------> A::1:1 -- Honors -- Credit/No-Credit grading
     * 1  H   -------> B::1:1 -- Honors
     * 3  H   -------> C::3:3 -- Honors
     */
    @Test
    public void testHandOffCase28() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "HONORS";
        String suffix = "397";
        String credit = "1-5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 3);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::1:1 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("C::3:3 -- Honors"));
        } else {
            logger.error("Could not find course for test");
        }

    }


    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, one section selected with variable credit
     * Section A 1-5 H CR/NC
     * Section B 1-5 H
     * <p/>
     * Expected: hand-off – shows  ignored Section B as it is not planned
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1 H CR/NC     -------> A::1:1 -- Honors -- Credit/No-Credit grading
     * ...
     * 5 H CR/NC    -------> A::5:5 -- Honors -- Credit/No-Credit grading
     * 1 H     -------> B::1:1 -- Honors
     * ...
     * 5 H   -------> B::5:5 -- Honors
     */
    @Test
    public void testHandOffCase29() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String subject = "HONORS";
        String suffix = "499";
        String credit = "1-5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 10);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::2:2 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::3:3 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::4:4 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::5:5 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::1:1 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::2:2 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::3:3 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::4:4 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::5:5 -- Honors"));
        } else {
            logger.error("Could not find course for test");
        }

    }

    /**
     * Hand-off screen needed Future, Scheduled quarters,
     * planned course, one section selected with variable credit
     * Section A 1-5 H CR/NC
     * Section B 1-5 H
     * <p/>
     * Expected: hand-off – shows  ignored Section B as it is not planned
     * **************************************
     * Credit ------> equivalent key in messyItems
     * ***************************************
     * 1 H CR/NC     -------> A::1:1 -- Honors -- Credit/No-Credit grading
     * ...
     * 5 H CR/NC    -------> A::5:5 -- Honors -- Credit/No-Credit grading
     * 1 H     -------> B::1:1 -- Honors
     * ...
     * 5 H   -------> B::5:5 -- Honors
     */
    @Test
    public void testHandOffCase30() {

        String studentId = "730FA4DCAE3411D689DA0004AC494FFE";
        String atpId = "kuali.uw.atp.2013.4";
        String primaryActivityPlan1 = "2013:4:HONORS:499:A";
        String primaryActivityPlan2 = "2013:4:HONORS:499:B";
        String subject = "HONORS";
        String suffix = "499";
        String credit = "1-5";
        String courseId = getCourseHelper().getCourseIdForTerm(subject, suffix, atpId);

        if (courseId != null) {
            addPlanItem("planItem1", courseId, PlanConstants.COURSE_TYPE, atpId, true);
            addPlanItem("planItem2", primaryActivityPlan1, PlanConstants.SECTION_TYPE, atpId, false);
            addPlanItem("planItem3", primaryActivityPlan2, PlanConstants.SECTION_TYPE, atpId, false);

            addCourseInfo(courseId, courseId, subject, suffix, credit, CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE);

            PlanAuditForm planAuditForm = degreeAuditHelper.processHandOff(new PlanAuditForm(), studentId);

            assertTrue(CollectionUtils.isEmpty(planAuditForm.getCleanList()));
            assertTrue(!CollectionUtils.isEmpty(planAuditForm.getMessyItems()));
            assertTrue(CollectionUtils.isEmpty(planAuditForm.getIgnoreList()));
            assertTrue(planAuditForm.getMessyItems().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().size() == 1);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCourseId().equals(courseId));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().size() == 10);
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::1:1 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::2:2 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::3:3 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::4:4 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("A::5:5 -- Honors -- Credit/No-Credit grading"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::1:1 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::2:2 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::3:3 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::4:4 -- Honors"));
            assertTrue(planAuditForm.getMessyItems().get(0).getMessyItemList().get(0).getCredits().contains("B::5:5 -- Honors"));
        } else {
            logger.error("Could not find course for test");
        }

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

        resultComponentInfo.setAttributes(attributes);
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
     * creates a plan item and adds it to academicPlanService mock
     *
     * @param planItemId
     * @param refObjId
     * @param refObjType
     * @param atpId
     * @param clearAlreadyExistingItems
     * @return
     */
    private PlanItemInfo addPlanItem(String planItemId, String refObjId, String refObjType, String atpId, boolean clearAlreadyExistingItems) {
        PlanItemInfo plan = new PlanItemInfo();
        plan.setId(planItemId);
        RichTextInfo richText = new RichTextInfo();
        plan.setDescr(richText);


        plan.setRefObjectId(refObjId);
        plan.setRefObjectType(refObjType);
        List<String> atps = new ArrayList<String>();
        atps.add(atpId);
        plan.setPlanPeriods(atps);
        ContextInfo contextInfo = new ContextInfo();

        try {

            if (clearAlreadyExistingItems) {
                getAcademicPlanService().deletePlanItemSet(null, contextInfo);
            }

            getAcademicPlanService().createPlanItem(plan, contextInfo);
        } catch (Exception e) {
            logger.error("failed to create plan");
        }

        return plan;
    }

}
