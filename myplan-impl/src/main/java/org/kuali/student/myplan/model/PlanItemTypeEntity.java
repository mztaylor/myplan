package org.kuali.student.myplan.model;

import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.r2.common.entity.TypeEntity;

import javax.persistence.Entity;
import javax.persistence.Table;

/**
 */
@Entity
@Table(name = "KSPL_LRNG_PLAN_ITEM_TYPE")
public class PlanItemTypeEntity extends TypeEntity<PlanItemAttributeEntity> {}
