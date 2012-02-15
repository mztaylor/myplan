package org.kuali.student.myplan.dao;

import org.kuali.student.enrollment.dao.GenericEntityDao;
import org.kuali.student.myplan.model.PlanItemEntity;
import javax.persistence.Query;

import java.util.List;

/**
 *  DAO for working with learning plan items.
 */
public class PlanItemDao extends GenericEntityDao<PlanItemEntity> {
    /**
     * Get all plan items for a particular learning plan.
     *
     * @param learningPlanId
     * @return
     *
     * TODO: Refactor one of these as a named query or a regular query.
     */
    public List<PlanItemEntity> getLearningPlanItems(String learningPlanId) {
        return em.createQuery("from PlanItemEntity pie where pie.learningPlan.id =:learningPlanId")
            .setParameter("learningPlanId", learningPlanId).getResultList();
    }

    public List<PlanItemEntity> getLearningPlanItems(String learningPlanId, String learningPlanItemType) {
		Query query = em.createNamedQuery("LearningPlanItem.getPlanItemsByType");
		query.setParameter("learningPlanId", learningPlanId);
        query.setParameter("learningPlanItemType", learningPlanItemType);
		return query.getResultList();
	}

}
