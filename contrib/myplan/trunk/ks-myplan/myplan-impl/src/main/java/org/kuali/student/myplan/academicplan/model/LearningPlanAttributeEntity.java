package org.kuali.student.myplan.academicplan.model;

import org.kuali.student.r2.common.entity.BaseAttributeEntity;
import org.kuali.student.r2.common.infc.Attribute;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "KSPL_LRNG_PLAN_ATTR")
@AttributeOverride(name="value", column=@Column(name="ATTR_VALUE", columnDefinition = "CLOB"))
public class LearningPlanAttributeEntity extends BaseAttributeEntity<LearningPlanEntity> {

    public LearningPlanAttributeEntity() {
    }

    public LearningPlanAttributeEntity(Attribute att, LearningPlanEntity owner) {
        super(att, owner);
    }
}
