package org.kuali.student.myplan.dao;

import org.junit.Test;
import org.kuali.student.common.test.spring.AbstractTransactionalDaoTest;
import org.kuali.student.common.test.spring.Dao;
import org.kuali.student.common.test.spring.PersistenceFileLocation;
import org.kuali.student.common.util.UUIDHelper;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.model.LearningPlanEntity;
import org.kuali.student.myplan.model.PlanItemEntity;
import org.kuali.student.myplan.model.PlanItemTypeEntity;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@PersistenceFileLocation("classpath:META-INF/lp-persistence.xml")
public class PlanItemDaoTest extends AbstractTransactionalDaoTest {

    @Dao(value = "org.kuali.student.myplan.dao.PlanItemDao", testSqlFile = "classpath:learning_plan.sql")
	private PlanItemDao planItemDao;

    @Dao(value = "org.kuali.student.myplan.dao.PlanItemTypeDao")
    private PlanItemTypeDao planItemTypeDao;

    @Dao(value = "org.kuali.student.myplan.dao.LearningPlanDao")
	private LearningPlanDao learningPlanDao;

    @Test
    public void testGetAllLearningPlanItems() {
        List<PlanItemEntity> objs = planItemDao.findAll();
        assertEquals(14, objs.size());
    }

    @Test
    public void testGetPlanItemsByType() {

        String planId = "lp1";

        List<PlanItemEntity> planItems = planItemDao.getLearningPlanItems(planId, AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        assertEquals(3, planItems.size());
        for (PlanItemEntity pie : planItems) {
            assertEquals(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, pie.getLearningPlanItemType().getId());
            assertEquals("student1", pie.getLearningPlan().getStudentId());
        }

        planItems = planItemDao.getLearningPlanItems(planId, AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);
        assertEquals(3, planItems.size());
        for (PlanItemEntity pie : planItems) {
            assertEquals(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, pie.getLearningPlanItemType().getId());
            assertEquals("student1", pie.getLearningPlan().getStudentId());
        }
    }

    @Test
    public void testGetLearningPlanById() {
        String id = "lp1-i1";
        PlanItemEntity pie = planItemDao.find(id);
        assertNotNull(pie);
        assertEquals(id, pie.getId());
        assertEquals(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, pie.getLearningPlanItemType().getId());
        assertEquals("lp1", pie.getLearningPlan().getId());
    }

    @Test
    public void testSavePlanItem() {
        LearningPlanEntity learningPlanEntity = learningPlanDao.find("lp1");
        PlanItemTypeEntity planItemTypeEntity = planItemTypeDao.find(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);

        PlanItemEntity pie = new PlanItemEntity();
        String id = UUIDHelper.genStringUUID();
        pie.setId(id);
        pie.setLearningPlan(learningPlanEntity);
        pie.setLearningPlanItemType(planItemTypeEntity);
        pie.setRefObjectId("02711400-c66d-4ecb-aca5-565118f167cf");
        pie.setRefObjectTypeKey("kuali.lu.type.CreditCourse");

        planItemDao.persist(pie);

        PlanItemEntity newPie = planItemDao.find(id);
        assertNotNull(newPie);
        assertEquals(id, newPie.getId());
        assertEquals(learningPlanEntity.getId(), newPie.getLearningPlan().getId());
        assertEquals(planItemTypeEntity.getId(), newPie.getLearningPlanItemType().getId());
        assertEquals(pie.getRefObjectId(), newPie.getRefObjectId());
        assertEquals(pie.getRefObjectTypeKey(), newPie.getRefObjectTypeKey());
    }
}
