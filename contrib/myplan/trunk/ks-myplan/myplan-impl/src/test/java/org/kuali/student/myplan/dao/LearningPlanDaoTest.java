package org.kuali.student.myplan.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;

import org.kuali.student.myplan.model.LearningPlanEntity;

import org.kuali.student.common.test.spring.AbstractTransactionalDaoTest;
import org.kuali.student.common.test.spring.Dao;
import org.kuali.student.common.test.spring.PersistenceFileLocation;
import org.kuali.student.myplan.model.LearningPlanRichTextEntity;
import org.kuali.student.myplan.model.LearningPlanTypeEntity;

import java.util.Date;
import java.util.List;

@PersistenceFileLocation("classpath:META-INF/lp-persistence.xml")
public class LearningPlanDaoTest extends AbstractTransactionalDaoTest {

    @Dao(value = "org.kuali.student.myplan.dao.LearningPlanDao", testSqlFile = "classpath:myplan-learning_plan.sql")
	private LearningPlanDao learningPlanDao;

    @Dao(value = "org.kuali.student.myplan.dao.LearningPlanTypeDao")
	private LearningPlanTypeDao typeDao;

    @Test
    public void testGetAllLearningPlans() {
        List<LearningPlanEntity> obj = learningPlanDao.findAll();
        assertEquals(4, obj.size());
    }

    @Test
    public void testGetLearningPlanById() {
        String id = "lp1";
        LearningPlanEntity lp = learningPlanDao.find(id);
        assertNotNull(lp);
        assertEquals(id, lp.getId());
        assertEquals("student1", lp.getStudentId());

        assertEquals("Student 1 Learning Plan 1", lp.getDescr().getPlain());
    }

    @Test
    public void testSaveLearningPlan() {
        LearningPlanRichTextEntity lpDesc = new LearningPlanRichTextEntity();
        lpDesc.setFormatted("<span>New Plan</span>");
        lpDesc.setPlain("New Plan");

        LearningPlanTypeEntity learningPlanTypeEntity = typeDao.find("kuali.academicplan.type.plan");
        assertNotNull(learningPlanTypeEntity);

        String studentId = "new-student";

        LearningPlanEntity learningPlanEntity = new LearningPlanEntity();
        learningPlanEntity.setDescr(lpDesc);
        learningPlanEntity.setLearningPlanType(learningPlanTypeEntity);
        learningPlanEntity.setStudentId(studentId);
        learningPlanEntity.setCreateId(studentId);
        Date now = new Date();
        learningPlanEntity.setCreateTime(now);

        assertEquals(4, learningPlanDao.findAll().size());

        learningPlanDao.persist(learningPlanEntity);

        assertEquals(5, learningPlanDao.findAll().size());
    }

    @Test
    public void testGetLearningPlansByStudentId() {
        String studentId = "student1";
        List<LearningPlanEntity> planEntities = learningPlanDao.getLearningPlans(studentId);

        assertEquals(2, planEntities.size());

        for (LearningPlanEntity lpe : planEntities) {
            assertEquals(studentId, lpe.getStudentId());
        }
    }

    @Test
    public void testGetLearningPlansByStudentIdAndType() {
        String studentId = "student1";
        String typeId = "kuali.academicplan.type.plan";
        List<LearningPlanEntity> planEntities = learningPlanDao.getLearningPlansByType(studentId, typeId);

        assertEquals(2, planEntities.size());

        for (LearningPlanEntity lpe : planEntities) {
            assertEquals(studentId, lpe.getStudentId());
            assertEquals(typeId, lpe.getLearningPlanType().getId());
        }
    }
}
