package org.kuali.student.myplan.model;

import org.kuali.student.r2.common.entity.AttributeOwner;
import org.kuali.student.r2.common.entity.MetaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

/**
 *
 */
@Entity
@Table(name = "KSPL_LRNG_PLAN_ITEM")
public class PlanItemEntity  extends MetaEntity implements AttributeOwner<PlanItemAttributeEntity> {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private  List<PlanItemAttributeEntity> attributes;

    @Override
    public void setAttributes(List<PlanItemAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    @Override
    public List<PlanItemAttributeEntity> getAttributes() {
        return attributes;
    }
}
