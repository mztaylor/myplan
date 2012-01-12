package org.kuali.student.myplan.model;

import java.util.List;
import org.kuali.student.r2.common.entity.AttributeOwner;
import org.kuali.student.r2.common.entity.MetaEntity;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 *
 */
@Entity
@Table(name = "KSPL_LRNG_PLAN_ITEM")
@NamedQueries( {
    @NamedQuery(name = "LearningPlanItem.getLearningPlanItems",
            query = "SELECT r FROM PlanItemEntity r WHERE r.refObjectTypeKey = :refObjectTypeKey and r.refObjectId = :refObjectId")
})
public class PlanItemEntity extends MetaEntity implements AttributeOwner<PlanItemAttributeEntity> {

    @Column(name="REF_OBJ_TYPE_KEY")
	private String refObjectTypeKey;

    @Column(name="REF_OBJ_ID")
	private String refObjectId;

    @ManyToOne()
    @JoinColumn(name = "TYPE_ID")
    private PlanItemTypeEntity learningPlanItemType;

    @ManyToOne()
    @JoinColumn(name = "PLAN_ID")
    private LearningPlanEntity learningPlan;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "OWNER")
    private List<PlanItemAttributeEntity> attributes;

	@Override
	public List<PlanItemAttributeEntity> getAttributes() {
		return this.attributes;
	}

	@Override
	public void setAttributes(List<PlanItemAttributeEntity> attributes) {
		this.attributes = attributes;
	}

	public String getRefObjectTypeKey() {
		return refObjectTypeKey;
	}

	public void setRefObjectTypeKey(String refObjectTypeKey) {
		this.refObjectTypeKey = refObjectTypeKey;
	}

	public String getRefObjectId() {
		return refObjectId;
	}

	public void setRefObjectId(String refObjectId) {
		this.refObjectId = refObjectId;
	}

    public PlanItemTypeEntity getLearningPlanItemType() {
        return learningPlanItemType;
    }

    public void setLearningPlanItemType(PlanItemTypeEntity learningPlanItemType) {
        this.learningPlanItemType = learningPlanItemType;
    }

    public LearningPlanEntity getLearningPlan() {
        return learningPlan;
    }

    public void setLearningPlan(LearningPlanEntity learningPlan) {
        this.learningPlan = learningPlan;
    }

    @Override
    public String toString() {
        return "PlanItemEntity [" + getId() + "]";
    }
}
