package org.kuali.student.myplan.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.student.enrollment.courseoffering.dto.OfferingInstructorInfo;
import org.kuali.student.enrollment.lui.dto.LuiInfo;
import org.kuali.student.enrollment.lui.dto.LuiLuiRelationInfo;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.util.constants.LuiServiceConstants;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.junit.Assert.*;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:academic-plan-test-context.xml"})
@TransactionConfiguration(transactionManager = "JtaTxManager", defaultRollback = true)
@Transactional
public class AcademicPlanServiceImplTest {

    @Resource  // look up bean via variable name, then type
    private AcademicPlanService academicPlanService;

    public static final String principalId = "testuser";
    public ContextInfo context = ContextInfo.newInstance();

    @Before
    public void setUp() {
        context = ContextInfo.getInstance(context);
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
}
