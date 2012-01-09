package org.kuali.student.myplan.model;

import org.kuali.student.r2.common.entity.TypeEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 *  Learning Plan Type
 */
@Entity
@Table(name = "KSPL_LRNG_PLAN_TYPE")
public class LearningPlanTypeEntity extends TypeEntity<LearningPlanTypeAttributeEntity> {}
