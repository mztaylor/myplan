package org.kuali.student.myplan.model;

import org.kuali.student.r2.common.entity.BaseAttributeEntity;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.kuali.student.r2.common.infc.Attribute;

@Entity
@Table(name = "KSPL_LRNG_PLAN_ATTR")
public class LearningPlanAttributeEntity extends BaseAttributeEntity<LearningPlanEntity> {

    @ManyToOne
    @JoinColumn(name = "OWNER")
    private LearningPlanEntity owner;

    public LearningPlanAttributeEntity() {}

    public LearningPlanAttributeEntity(String key, String value) {
        super(key, value);
    }

    public LearningPlanAttributeEntity(Attribute att) {
        super(att);
    }

    @Override
    public void setOwner(LearningPlanEntity owner) {
        this.owner = owner;
    }

    @Override
    public LearningPlanEntity getOwner() {
        return owner;
    }
}
