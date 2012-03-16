package org.kuali.student.myplan.academicplan.model;

import org.kuali.student.r2.common.entity.BaseAttributeEntity;
import org.kuali.student.r2.common.infc.Attribute;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "KSPL_LRNG_PLAN_ITEM_TYPE_ATTR")
public class PlanItemTypeAttributeEntity extends BaseAttributeEntity<PlanItemTypeEntity> {

    @ManyToOne
    @JoinColumn(name = "OWNER")
    private PlanItemTypeEntity owner;

    public PlanItemTypeAttributeEntity() {}

    public PlanItemTypeAttributeEntity(String key, String value) {
        super(key, value);
    }

    public PlanItemTypeAttributeEntity(Attribute att) {
        super(att);
    }

    @Override
    public void setOwner(PlanItemTypeEntity owner) {
        this.owner = owner;
    }

    @Override
    public PlanItemTypeEntity getOwner() {
        return owner;
    }
}
