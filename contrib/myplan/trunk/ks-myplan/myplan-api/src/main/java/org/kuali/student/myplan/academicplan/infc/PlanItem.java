package org.kuali.student.myplan.academicplan.infc;

import org.kuali.student.r2.common.infc.HasId;
import org.kuali.student.r2.common.infc.RichText;
import org.kuali.student.r2.common.infc.TypeStateEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * A single plan item. Plan items can link to different entities in the system based on the refObjectType.
 *
 * @Author Kamal
 */
public interface PlanItem extends HasId, TypeStateEntity {

    /**
     * A description of the Learning Plan Item
     * @name Description
     */
    public RichText getDescr();

    /**
     * Id to the reference object e.g Course, Requirements etc
     * @name Reference Object Id
     */
    public String getRefObjectId();

    /**
     * Type of the Reference Object
     * @name Reference Object Type
     */
    public String getRefObjectType();

    /**
     * Time Periods for which the item is planned for
     * @name  Time Periods ATPIds
     */
    public List<String> getPlanPeriods();

    /**
     * Containing learning plan
     * @name Learning Plan Id
     */
    public String getLearningPlanId();

     /**
     * Credit hours for the Learning Plan Item
     * @name Credits
     */
    public BigDecimal getCredit();

}
