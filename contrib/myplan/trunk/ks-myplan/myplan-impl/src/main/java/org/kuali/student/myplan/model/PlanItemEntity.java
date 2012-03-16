package org.kuali.student.myplan.model;

import java.util.*;

import com.sun.istack.NotNull;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.entity.AttributeOwner;
import org.kuali.student.r2.common.entity.MetaEntity;
import org.kuali.student.r2.core.class1.atp.model.AtpEntity;

import java.util.List;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 *
 */
@SuppressWarnings({"JpaDataSourceORMInspection"})
@Entity
@Table(name = "KSPL_LRNG_PLAN_ITEM")
@NamedQueries( {
    @NamedQuery(name = "LearningPlanItem.getLearningPlanItems",
            query = "SELECT r FROM PlanItemEntity r WHERE r.refObjectTypeKey = :refObjectTypeKey and r.refObjectId = :refObjectId"),
    @NamedQuery(name = "LearningPlanItem.getPlanItemsByType",
            query = "SELECT pi FROM PlanItemEntity pi, LearningPlanEntity p WHERE " +
                    "pi.learningPlan = p " +
                    "and p.id =:learningPlanId and pi.learningPlanItemType.id =:learningPlanItemType")
})
public class PlanItemEntity extends MetaEntity implements AttributeOwner<PlanItemAttributeEntity> {

    @NotNull
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

    @ElementCollection (fetch=FetchType.EAGER)
    @CollectionTable(name="KSPL_LRNG_PLAN_ITEM_ATP_ID",
        joinColumns=@JoinColumn(name="PLAN_ITEM_ID"))
    @Column(name="ATP_ID")
    //  TODO: I believe this should be a Set.
    private Set<String> planPeriods;

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

    public Set<String> getPlanPeriods() {
        return planPeriods;
    }

    public void setPlanPeriods(Set<String> planPeriods) {
        this.planPeriods = planPeriods;
    }

    /**
     * Add an ATP id to the set. No nulls or empty strings.
     *
     * @return
     */
    public boolean addPlanPeriod(String atpId) {
        if (atpId == null || atpId.trim().equals("")) {
            return false;
        }
        return this.planPeriods.add(atpId);
    }

    /**
     * Remove an ATP id from the Set.
     *
     * @param atpId
     * @return
     */
    public boolean removePlanPeriod(String atpId) {
        return this.planPeriods.remove(atpId);
    }

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

        if (this.getDescr() != null) {
            dto.setDescr(this.getDescr().toDto());
        }

        dto.setMeta(super.toDTO());

        dto.setPlanPeriods(this.getPlanPeriods());

        List<AttributeInfo> attributes = new ArrayList<AttributeInfo>();
        if (getAttributes() != null) {
            for (PlanItemAttributeEntity att : getAttributes()) {
                AttributeInfo attInfo = att.toDto();
                attributes.add(attInfo);
            }
        }
        dto.setAttributes(attributes);

        return dto;
    }

    @Override
    public String toString() {
        return "PlanItemEntity [" + getId() + "]";
    }
}
