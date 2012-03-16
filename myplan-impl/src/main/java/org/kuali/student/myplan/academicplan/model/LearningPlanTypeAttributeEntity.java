package org.kuali.student.myplan.academicplan.model;

import org.kuali.student.r2.common.entity.BaseAttributeEntity;
import org.kuali.student.r2.common.infc.Attribute;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "KSPL_LRNG_PLAN_TYPE_ATTR")
public class LearningPlanTypeAttributeEntity extends BaseAttributeEntity<LearningPlanTypeEntity> {

    @ManyToOne
    @JoinColumn(name = "OWNER")
    private LearningPlanTypeEntity owner;

    public LearningPlanTypeAttributeEntity() {}

    public LearningPlanTypeAttributeEntity(String key, String value) {
        super(key, value);
    }

    public LearningPlanTypeAttributeEntity(Attribute att) {
        super(att);
    }

    @Override
    public void setOwner(LearningPlanTypeEntity owner) {
        this.owner = owner;
    }

    @Override
    public LearningPlanTypeEntity getOwner() {
        return owner;
    }
}
