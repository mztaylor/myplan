package org.kuali.student.myplan.academicplan.dao;

import org.kuali.student.myplan.academicplan.model.LearningPlanEntity;
import org.kuali.student.r2.common.dao.GenericEntityDao;

import java.util.List;

public class LearningPlanDao extends GenericEntityDao<LearningPlanEntity> {

    public List<LearningPlanEntity> getLearningPlans(String studentId) {
        return em.createQuery("select lp from LearningPlanEntity lp where lp.studentId =:studentId")
                .setParameter("studentId", studentId).getResultList();
    }

    public List<LearningPlanEntity> getLearningPlansByType(String studentId, String typeId) {
        Long stTime = System.currentTimeMillis();
        List<LearningPlanEntity> lpList = em.createQuery("select lp from LearningPlanEntity lp where lp.studentId =:studentId and lp.learningPlanType.id =:typeId")
                .setParameter("studentId", studentId).setParameter("typeId", typeId).getResultList();
        Long enTime = System.currentTimeMillis();
        Long difTime = enTime - stTime;
        if(difTime > 50)
        System.out.println("LP Student END  : " + studentId + ": Time:" + (enTime - stTime) );
        return lpList;
    }

    public List<LearningPlanEntity> getLearningPlansByTypeAndProgram(String planProgram, String typeId) {
        Long stTime = System.currentTimeMillis();
        List<LearningPlanEntity> lpList = em.createQuery("select lp from LearningPlanEntity lp where lp.planProgram =:planProgram and lp.learningPlanType.id =:typeId")
                .setParameter("planProgram", planProgram).setParameter("typeId", typeId).getResultList();
        Long enTime = System.currentTimeMillis();
        Long difTime = enTime - stTime;
        if (difTime > 50)
            System.out.println("LP PlanProgram END  : " + planProgram + ": Time:" + (enTime - stTime));
        return lpList;
    }
}
