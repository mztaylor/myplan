package org.kuali.student.myplan.model;

import org.kuali.student.r2.common.entity.AttributeOwner;
import org.kuali.student.r2.common.entity.MetaEntity;
import org.kuali.student.r2.common.entity.TypeEntity;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "KSPL_LRNG_PLAN")
public class LearningPlanEntity extends MetaEntity
        implements AttributeOwner<LearningPlanAttributeEntity>, Comparable<LearningPlanEntity> {

    @Column(name="STUDENT_ID")
	private String studentId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "RT_DESCR_ID")
    private LearningPlanRichTextEntity descr;

    @ManyToOne(optional=false)
    @JoinColumn(name = "TYPE_ID")
    private LearningPlanTypeEntity learningPlanType;

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

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public LearningPlanRichTextEntity getDescr() {
        return descr;
    }

    public void setDescr(LearningPlanRichTextEntity descr) {
        this.descr = descr;
    }

    public LearningPlanTypeEntity getLearningPlanType() {
        return learningPlanType;
    }

    public void setLearningPlanType(LearningPlanTypeEntity learningPlanType) {
        this.learningPlanType = learningPlanType;
    }

    @Override
    public String toString() {
        return String.format("LearningPlan [%s, %s]: %s", this.getId(), this.getObjectId(), this.getDescr().getPlain());
    }

    @Override
    public int compareTo(LearningPlanEntity other) {

        if (other == null) {
            return -1;
        }

        //  First check student id.
        if (! other.getStudentId().equals(this.getStudentId())) {
            return this.getStudentId().compareTo(other.getStudentId());
        }

        //  Could check type here.

        //  Check description text
        if (! this.getDescr().getPlain().equals(other.getDescr().getPlain())) {
            return this.getDescr().getPlain().compareTo(other.getDescr().getPlain());
        }

        return 0;
    }
}