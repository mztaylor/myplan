package org.kuali.student.myplan.dao;

import org.kuali.student.enrollment.dao.GenericEntityDao;
import org.kuali.student.myplan.model.LearningPlanEntity;

import java.util.List;

public class LearningPlanDao extends GenericEntityDao<LearningPlanEntity> {

    public List<LearningPlanEntity> getLearningPlans(String studentId) {
        return em.createQuery("from LearningPlanEntity lp where lp.studentId =:studentId")
                .setParameter("studentId", studentId).getResultList();
    }

    public List<LearningPlanEntity> getLearningPlansByType(String studentId, String typeId) {
        return em.createQuery("from LearningPlanEntity lp where lp.studentId =:studentId and lp.learningPlanType.id =:typeId")
                .setParameter("studentId", studentId).setParameter("typeId", typeId).getResultList();
    }
}
