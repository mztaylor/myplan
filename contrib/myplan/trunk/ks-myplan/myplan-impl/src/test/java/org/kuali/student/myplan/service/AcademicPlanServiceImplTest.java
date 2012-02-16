package org.kuali.student.myplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:academic-plan-test-context.xml"})
@TransactionConfiguration(transactionManager = "JtaTxManager", defaultRollback = true)
@Transactional
public class AcademicPlanServiceImplTest {

    @Resource  // look up bean via variable name, then type
    private AcademicPlanService academicPlanService;

    public static final String principalId = "student1";
    public ContextInfo context;

    @Before
    public void setUp() {
        context = new ContextInfo();
        context.setPrincipalId(principalId);
    }

    @Test
    public void serviceSetup() {
        assertNotNull(academicPlanService);
    }

    @Test (expected = DoesNotExistException.class)
    public void getUnknownLearningPlan() throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {
         academicPlanService.getLearningPlan("unknown_plan", context);
    }


    @Test
    public void getLearningPlan() {
        String planId = "lp1";
        LearningPlan learningPlan = null;
        try {
            learningPlan = academicPlanService.getLearningPlan(planId, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        assertNotNull(learningPlan);
        assertEquals(planId, learningPlan.getId());
        assertEquals("student1", learningPlan.getStudentId());
        assertEquals("Student 1 Learning Plan 1", learningPlan.getDescr().getPlain());
    }

    @Test
    public void getLearningPlans() {
        String studentId = "student1";

        List<LearningPlan> learningPlans = null;
        try {
            learningPlans = academicPlanService.getLearningPlansForStudentByType(studentId, AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        assertNotNull(learningPlans);
        assertEquals(2, learningPlans.size());

        LearningPlan lp = learningPlans.get(0);
        assertEquals("lp1", lp.getId());

        lp = learningPlans.get(1);
        assertEquals("lp2", lp.getId());
    }


    @Test
    public void addLearningPlan() {
        LearningPlanInfo learningPlan = new LearningPlanInfo();
        learningPlan.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN);
        learningPlan.setStudentId(principalId);
        RichTextInfo desc = new RichTextInfo();
        String formattedDesc = "<span>My Plan</span>";
        String planDesc = "My Plan";
        desc.setFormatted(formattedDesc);
        desc.setPlain(planDesc);
        learningPlan.setDescr(desc);

        learningPlan.setStateKey(AcademicPlanServiceConstants.LEARNING_PLAN_ACTIVE_STATE_KEY);

        LearningPlanInfo newLearningPlan = null;
        try {
            newLearningPlan = academicPlanService.createLearningPlan(learningPlan, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        assertNotNull(newLearningPlan);
        assertEquals(AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN, newLearningPlan.getTypeKey());
        assertEquals(principalId, newLearningPlan.getStudentId());
        assertEquals(formattedDesc, newLearningPlan.getDescr().getFormatted());
        assertEquals(planDesc, newLearningPlan.getDescr().getPlain());
    }

    @Test (expected = DoesNotExistException.class)
    public void deleteLearningPlan() throws Exception {
        String id = "lp1";

        //  Make sure the plan exists and has some plan items.
        LearningPlan plan = null;
        try {
            plan = academicPlanService.getLearningPlan(id, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        int itemCount = academicPlanService.getPlanItemsInPlan(id, context).size();
        assertEquals(6, itemCount);

        //  Delete the plan
        try {
            academicPlanService.deleteLearningPlan(id, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        //  Make sure the plan items were cleaned up.
        itemCount = academicPlanService.getPlanItemsInPlan(id, context).size();
        assertEquals(0, itemCount);

        try {
            plan = academicPlanService.getLearningPlan(id, context);
        } catch (Exception e) {
            //  Swallow anything but DoesNotExistException.
            if (e instanceof DoesNotExistException) {
                  throw e;
            }
        }
    }

    @Test
    public void addAndGetPlanItem() throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException {
        String planId = "lp1";

        // Create a new plan item.
        PlanItemInfo planItem = new PlanItemInfo();

        RichTextInfo desc = new RichTextInfo();
        String formattedDesc = "<span>My Comment</span>";
        String planDesc = "My Comment";
        desc.setFormatted(formattedDesc);
        desc.setPlain(planDesc);
        planItem.setDescr(desc);

        planItem.setLearningPlanId(planId);
        planItem.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        String courseId = "02711400-c66d-4ecb-aca5-565118f167cf";
        String courseType = LUConstants.CLU_TYPE_CREDIT_COURSE;

        planItem.setRefObjectId(courseId);
        planItem.setRefObjectType(courseType);

        planItem.setStateKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        PlanItem newPlanItem = null;
        try {
            newPlanItem = academicPlanService.createPlanItem(planItem, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        assertNotNull(newPlanItem);
        assertNotNull(newPlanItem.getId());
        assertEquals(planId, newPlanItem.getLearningPlanId());
        assertEquals(formattedDesc, newPlanItem.getDescr().getFormatted());
        assertEquals(planDesc, newPlanItem.getDescr().getPlain());
        assertEquals(courseId, newPlanItem.getRefObjectId());
        assertEquals(courseType, newPlanItem.getRefObjectType());


        // Test getPlanItem

        PlanItem fetchedPlanItem =  academicPlanService.getPlanItem(newPlanItem.getId(), context);

        assertNotNull(fetchedPlanItem);
        assertNotNull(fetchedPlanItem.getId());
        assertEquals(planId, fetchedPlanItem.getLearningPlanId());
        assertEquals(formattedDesc, fetchedPlanItem.getDescr().getFormatted());
        assertEquals(planDesc, fetchedPlanItem.getDescr().getPlain());
        assertEquals(courseId, fetchedPlanItem.getRefObjectId());
        assertEquals(courseType, fetchedPlanItem.getRefObjectType());

    }

    @Test
    public void addPlanItemNullCourseType() {
        String planId = "lp1";

        PlanItemInfo planItem = new PlanItemInfo();

        RichTextInfo desc = new RichTextInfo();
        String formattedDesc = "<span>My Comment</span>";
        String planDesc = "My Comment";
        desc.setFormatted(formattedDesc);
        desc.setPlain(planDesc);
        planItem.setDescr(desc);

        planItem.setLearningPlanId(planId);
        planItem.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        String courseId = "02711400-c66d-4ecb-aca5-565118f167cf";
        String courseType = null;

        planItem.setRefObjectId(courseId);
        planItem.setRefObjectType(courseType);
        planItem.setStateKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        try {
            academicPlanService.createPlanItem(planItem, context);
        } catch (DataValidationErrorException dvee) {
            assertEquals(1, dvee.getValidationResults().size());
            ValidationResultInfo resultInfo =  dvee.getValidationResults().get(0);
            assertEquals("refObjectType", resultInfo.getElement());
            assertEquals("error.required", resultInfo.getMessage());
            assertEquals(null, resultInfo.getInvalidData());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void addPlanItemNullLearningPlan() {
        String planId = null;

        PlanItemInfo planItem = new PlanItemInfo();

        RichTextInfo desc = new RichTextInfo();
        String formattedDesc = "<span>My Comment</span>";
        String planDesc = "My Comment";
        desc.setFormatted(formattedDesc);
        desc.setPlain(planDesc);
        planItem.setDescr(desc);

        planItem.setLearningPlanId(planId);
        planItem.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);

        String courseId = "02711400-c66d-4ecb-aca5-565118f167cf";
        String courseType = LUConstants.CLU_TYPE_CREDIT_COURSE;

        planItem.setRefObjectId(courseId);
        planItem.setRefObjectType(courseType);
        planItem.setStateKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        try {
            academicPlanService.createPlanItem(planItem, context);
        } catch (DataValidationErrorException dvee) {
            assertEquals(1, dvee.getValidationResults().size());
            ValidationResultInfo resultInfo =  dvee.getValidationResults().get(0);
            assertEquals("learningPlanId", resultInfo.getElement());
            assertEquals("error.required", resultInfo.getMessage());
            assertEquals(null, resultInfo.getInvalidData());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void addPlanItemNullCourseId() {
        String planId = "lp1";

        PlanItemInfo planItem = new PlanItemInfo();

        RichTextInfo desc = new RichTextInfo();
        String formattedDesc = "<span>My Comment</span>";
        String planDesc = "My Comment";
        desc.setFormatted(formattedDesc);
        desc.setPlain(planDesc);
        planItem.setDescr(desc);

        planItem.setLearningPlanId(planId);
        planItem.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        String courseId = null;
        String courseType = LUConstants.CLU_TYPE_CREDIT_COURSE;

        planItem.setRefObjectId(courseId);
        planItem.setRefObjectType(courseType);
        planItem.setStateKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        try {
            academicPlanService.createPlanItem(planItem, context);
        } catch (DataValidationErrorException dvee) {
            assertEquals(dvee.getValidationResults().size(), 1);
            ValidationResultInfo resultInfo =  dvee.getValidationResults().get(0);
            assertEquals("refObjectId", resultInfo.getElement());
            assertEquals("error.required", resultInfo.getMessage());
            assertEquals(null, resultInfo.getInvalidData());
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
    }

    @Test
    public void addPlanItemToSavedCoursesListWithDuplicateCourseId() {
        String planId = "lp1";

        // Create a new plan item.
        PlanItemInfo planItem = new PlanItemInfo();

        RichTextInfo desc = new RichTextInfo();
        String formattedDesc = "<span>My Comment</span>";
        String planDesc = "My Comment";
        desc.setFormatted(formattedDesc);
        desc.setPlain(planDesc);
        planItem.setDescr(desc);

        planItem.setLearningPlanId(planId);
        planItem.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        String courseId = "02711400-c66d-4ecb-aca5-565118f167cf";
        String courseType = LUConstants.CLU_TYPE_CREDIT_COURSE;

        planItem.setRefObjectId(courseId);
        planItem.setRefObjectType(courseType);

        planItem.setStateKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        PlanItem newPlanItem = null;
        try {
            newPlanItem = academicPlanService.createPlanItem(planItem, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        try {
            List<PlanItem> x = academicPlanService.getPlanItemsInPlan(planId, context);
        } catch (DoesNotExistException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvalidParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MissingParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OperationFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        try {
            newPlanItem = academicPlanService.createPlanItem(planItem, context);
        } catch (AlreadyExistsException e) {
            //  This is what should should happen.
            return;
        } catch (Exception e) {
            //  Do nothing.
        }

        fail("Was able to add a duplicate course id to saved courses list.");

    }

    @Test
    public void deletePlanItem() {
        String id = "lp1";

        //  Make sure the plan exists and has some plan items.
        LearningPlan plan = null;
        try {
            plan = academicPlanService.getLearningPlan(id, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        List<PlanItem> planItems = null;
        try {
            planItems = academicPlanService.getPlanItemsInPlan(id, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        assertEquals(6, planItems.size());

        //  Delete a plan item.
        String planItemId = planItems.get(0).getId();
        try {
            academicPlanService.deletePlanItem(planItemId, context);
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }

        //  Make sure the plan items were cleaned up.
        int itemCount = 0;
        try {
            itemCount = academicPlanService.getPlanItemsInPlan(id, context).size();
        } catch (Exception e) {
            fail(e.getLocalizedMessage());
        }
        assertEquals(5, itemCount);
    }
}
