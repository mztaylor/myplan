package org.kuali.student.myplan.dao;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import org.kuali.student.myplan.model.LearningPlanEntity;

import org.kuali.student.common.test.spring.AbstractTransactionalDaoTest;
import org.kuali.student.common.test.spring.Dao;
import org.kuali.student.common.test.spring.PersistenceFileLocation;

import java.util.List;

@PersistenceFileLocation("classpath:META-INF/lp-persistence.xml")
public class LearningPlanDaoTest extends AbstractTransactionalDaoTest {

    @Dao(value = "org.kuali.student.myplan.dao.LearningPlanDao", testSqlFile = "classpath:myplan-learning_plan.sql")
	private LearningPlanDao dao;

    @Test
    public void testGetAllLearningPlans() {
        List<LearningPlanEntity> obj = dao.findAll();
        assertEquals(0, obj.size());
        //assertEquals(1, obj.size());
    }

	/*@Test
	public void testGetLearningPlan(){
		try{
			LearningPlanEntity obj = dao.find("lp-1");
			assertNotNull(obj);
			assertEquals("Lui one", obj.getName());
	        //   assertEquals(LuiServiceConstants.LUI_DRAFT_STATE_KEY, obj.getLuiState().getId());
	        //   assertEquals(LuiServiceConstants.COURSE_OFFERING_TYPE_KEY, obj.getLuiType().getId());
	        assertEquals("Learning Plan Test 1", obj.getDescr().getPlain());
		}catch (Exception ex){
			ex.printStackTrace();
		}
	} */
}
