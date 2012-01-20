package org.kuali.student.myplan.model;

import org.kuali.student.r2.common.entity.BaseAttributeEntity;
import org.kuali.student.r2.common.infc.Attribute;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "KSPL_LRNG_PLAN_ITEM_ATTR")
public class PlanItemAttributeEntity extends BaseAttributeEntity<PlanItemEntity> {

    @ManyToOne
    @JoinColumn(name = "OWNER")
    private PlanItemEntity owner;

    public PlanItemAttributeEntity() {}

    public PlanItemAttributeEntity(String key, String value) {
        super(key, value);
    }

    public PlanItemAttributeEntity(Attribute att) {
        super(att);
    }

    @Override
    public void setOwner(PlanItemEntity owner) {
        this.owner = owner;
    }

    @Override
    public PlanItemEntity getOwner() {
        return owner;
    }
}
