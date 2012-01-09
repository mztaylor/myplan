package org.kuali.student.myplan.service;

import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.dao.LearningPlanDao;

/**
 * TODO: Placeholder.
 */
public class AcademicPlanServiceImpl implements AcademicPlanService {
    private LearningPlanDao learningPlanDao;

    public LearningPlanDao getLearningPlanDao() {
        return learningPlanDao;
    }

    public void setLearningPlanDao(LearningPlanDao learningPlanDao) {
        this.learningPlanDao = learningPlanDao;
    }
}
