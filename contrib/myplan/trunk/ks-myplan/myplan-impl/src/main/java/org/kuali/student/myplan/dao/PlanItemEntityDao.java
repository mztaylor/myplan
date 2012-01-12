package org.kuali.student.myplan.dao;

import org.kuali.student.enrollment.dao.GenericEntityDao;
import org.kuali.student.myplan.model.PlanItemEntity;

import java.util.List;

/**
 *  DAO for working with learning plan items.
 */
public class PlanItemEntityDao extends GenericEntityDao<PlanItemEntity> {
    /**
     * Get all plan items for a particular learning plan.
     *
     * @param learningPlanId
     * @return
     */
    public List<PlanItemEntity> getPlanItems(String learningPlanId) {
        return em.createQuery("from PlanItemEntity pie where pie.learningPlan.id =:learningPlanId")
                .setParameter("learningPlanId", learningPlanId).getResultList();
    }
}
