package org.kuali.student.myplan.academicplan.dao;

import org.kuali.student.myplan.academicplan.model.PlanItemEntity;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.r2.common.dao.GenericEntityDao;

import javax.persistence.Query;
import java.util.List;

/**
 *  DAO for working with learning plan items.
 */
public class PlanItemDao extends GenericEntityDao<PlanItemEntity> {
    /**
     * Get all plan items for a particular learning plan.
     */
    public List<PlanItemEntity> getLearningPlanItems(String learningPlanId) {
        Query query = em.createNamedQuery("LearningPlanItem.getPlanItems");
        query.setParameter("learningPlanId", learningPlanId);
        return query.getResultList();
    }

    /**
     * Get all plan items for a particular learning plan by type.
     */
    public List<PlanItemEntity> getLearningPlanItems(String learningPlanId, String learningPlanItemType) {
        Query query = em.createNamedQuery("LearningPlanItem.getPlanItemsByType");
        query.setParameter("learningPlanId", learningPlanId);
        query.setParameter("learningPlanItemType", learningPlanItemType);
        return query.getResultList();
    }

    /**
     * Get all plan items for a particular learning plan by refObjectId.
     */
    public List<PlanItemEntity> getLearningPlanItemsByRefObjectId(String learningPlanId, String refObjectId, String refObjectTypeKey) {
        Query query = em.createNamedQuery("LearningPlanItem.getPlanItemsByRefObjectId");
        query.setParameter("learningPlanId", learningPlanId);
        query.setParameter("refObjectId", refObjectId);
        query.setParameter("refObjectTypeKey", refObjectTypeKey);
        return query.getResultList();
    }

    /**
     * Get all plan items for a particular learning plan by category.
     */
    @SuppressWarnings("unchecked")
    public List<PlanItemEntity> getLearningPlanItems(String learningPlanId, AcademicPlanServiceConstants.ItemCategory category) {
        Query query = em.createNamedQuery("LearningPlanItem.getPlanItemsByCategory");
        query.setParameter("learningPlanId", learningPlanId);
        query.setParameter("category", category.toString());
        return query.getResultList();
    }
}
