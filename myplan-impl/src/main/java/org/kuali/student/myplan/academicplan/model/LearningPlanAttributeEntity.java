package org.kuali.student.myplan.academicplan.model;

import org.kuali.student.r2.common.entity.BaseAttributeEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.kuali.student.r2.common.infc.Attribute;

@Entity
@Table(name = "KSPL_LRNG_PLAN_ATTR")
public class LearningPlanAttributeEntity extends BaseAttributeEntity<LearningPlanEntity> {

    public LearningPlanAttributeEntity() {}

    public LearningPlanAttributeEntity(Attribute att, LearningPlanEntity owner) {
        super(att, owner);
    }
}
