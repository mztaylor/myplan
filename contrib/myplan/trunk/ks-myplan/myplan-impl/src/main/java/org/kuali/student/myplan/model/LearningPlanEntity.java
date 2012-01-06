package org.kuali.student.myplan.model;

import org.kuali.student.r2.common.entity.AttributeOwner;
import org.kuali.student.r2.common.entity.MetaEntity;
import org.kuali.student.r2.common.entity.TypeEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.List;

@Entity
@Table(name = "KSAP_MP_PLAN")
public class LearningPlanEntity extends MetaEntity implements AttributeOwner<LearningPlanAttributeEntity> {

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "owner")
    private  List<LearningPlanAttributeEntity> attributes;

    @Override
    public void setAttributes(List<LearningPlanAttributeEntity> attributes) {
        this.attributes = attributes;
    }

    @Override
    public List<LearningPlanAttributeEntity> getAttributes() {
        return attributes;
    }
}