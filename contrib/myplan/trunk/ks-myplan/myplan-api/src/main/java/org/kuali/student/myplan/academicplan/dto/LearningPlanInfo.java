package org.kuali.student.myplan.academicplan.dto;

import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.dto.TypeStateEntityInfo;
import org.w3c.dom.Element;

import javax.xml.bind.annotation.*;
import java.util.List;

/**
 * LearningPlan message structure
 *
 * @Author kmuthu
 * Date: 1/5/12
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LearningPlanInfo", propOrder = {"id", "typeKey", "stateKey", "descr", "meta", "attributes", "_futureElements"})
public class LearningPlanInfo extends TypeStateEntityInfo implements LearningPlan {

    @XmlAttribute
    private String id;

    @XmlElement
    private RichTextInfo descr;

    @XmlAnyElement
    private List<Element> _futureElements;


    public LearningPlanInfo() {
        this.id = null;
        this.descr = null;
        this._futureElements = null;
    }

    public LearningPlanInfo(LearningPlan plan) {
        super(plan);

        if(null != plan) {
            this.id = plan.getId();
            this.descr = (null != plan.getDescr()) ? new RichTextInfo(plan.getDescr()) : null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public RichTextInfo getDescr() {
        return descr;
    }

    public void setDescr(RichTextInfo descr) {
        this.descr = descr;
    }
}
