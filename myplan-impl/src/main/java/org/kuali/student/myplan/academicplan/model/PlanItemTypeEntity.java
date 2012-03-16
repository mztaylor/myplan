package org.kuali.student.myplan.academicplan.model;

import org.kuali.student.r2.core.class1.type.entity.TypeEntity;


import javax.persistence.Entity;
import javax.persistence.Table;

/**
 */
@Entity
@Table(name = "KSPL_LRNG_PLAN_ITEM_TYPE")
public class PlanItemTypeEntity extends TypeEntity<PlanItemAttributeEntity> {}
