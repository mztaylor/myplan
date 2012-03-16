package org.kuali.student.myplan.model;

import org.junit.Test;
import org.kuali.student.common.util.UUIDHelper;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.dao.LearningPlanDao;
import org.kuali.student.myplan.dao.PlanItemDao;
import org.kuali.student.myplan.dao.PlanItemTypeDao;

import java.util.*;

import static org.junit.Assert.*;


public class PlanItemEntityTest {

    @Test
    public void testExercisePlanPeriodsAddRemove() {
        PlanItemEntity pie = new PlanItemEntity();
        //  Provide an empty set since Hibernate isn't "hydrating" the entity.
        Set<String> planPeriods = new HashSet<String>();
        pie.setPlanPeriods(planPeriods);

        assertFalse(pie.addPlanPeriod(null));
        assertFalse(pie.addPlanPeriod(""));
        assertFalse(pie.addPlanPeriod(" "));
        assertTrue(pie.addPlanPeriod("pp1"));
        assertFalse(pie.addPlanPeriod("pp1"));
        assertFalse(pie.removePlanPeriod("unknown"));
        assertFalse(pie.removePlanPeriod(null));
        assertTrue(pie.removePlanPeriod("pp1"));
    }
}
