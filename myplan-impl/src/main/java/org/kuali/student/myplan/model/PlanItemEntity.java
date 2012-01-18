package org.kuali.student.myplan.model;

import java.util.ArrayList;
import java.util.List;

import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.entity.AttributeOwner;
import org.kuali.student.r2.common.entity.MetaEntity;
import org.kuali.student.r2.core.class1.atp.model.AtpEntity;

import java.util.Date;
import java.util.List;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

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

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "RT_DESCR_ID")
    private PlanItemRichTextEntity descr;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "OWNER")
    private List<PlanItemAttributeEntity> attributes;

    // TODO: This field isn't needed for wishlist items, so hold for now.
    //@ManyToMany(cascade = CascadeType.ALL)
    //@JoinTable(name = "KSPL_LRNG_PL_IT_ATP_RELTN", joinColumns = @JoinColumn(name = "PLAN_ITEM_ID"), inverseJoinColumns = @JoinColumn(name = "ATP_ID"))
    //private List<AtpEntity> planPeriods;

    public PlanItemEntity(){
        super();
    }

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

    public PlanItemRichTextEntity getDescr() {
        return descr;
    }

    public void setDescr(PlanItemRichTextEntity descr) {
        this.descr = descr;
    }
   /* public List<AtpEntity> getPlanPeriods() {
        return planPeriods;
    }

    public void setPlanPeriods(List<AtpEntity> planPeriods) {
        this.planPeriods = planPeriods;
    }*/

    /**
     * Provides and data transfer object representation of the plan item.
     * @return LearningPlanInfo
     */
    public PlanItemInfo toDto() {
        PlanItemInfo dto = new PlanItemInfo();

        dto.setId(getId());
        dto.setLearningPlanId(this.getLearningPlan().getId());
        dto.setRefObjectId(this.getRefObjectId());
        dto.setRefObjectType(this.getRefObjectTypeKey());
        // FIXME: dto.setDescr();
        // FIXME: dto.setPlanPeriods();

        List<AttributeInfo> attributes = new ArrayList<AttributeInfo>();
        for (PlanItemAttributeEntity att : getAttributes()) {
            AttributeInfo attInfo = att.toDto();
            attributes.add(attInfo);
        }
        dto.setAttributes(attributes);

        return dto;
    }

    @Override
    public String toString() {
        return "PlanItemEntity [" + getId() + "]";
    }
}
