package org.kuali.student.myplan.plan.util;

import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.plan.dataobject.RecommendedItemDataObject;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/28/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PlanHelper {

    public PlanItemInfo getPlanItemByAtpAndType(String planId, String courseId, String atpId, String planItemType);

    public PlanItemInfo getPlannedOrBackupPlanItem(String courseId, String atpId);

    public LearningPlan getLearningPlan(String studentId);

    public List<RecommendedItemDataObject> getRecommendedItems(String refObjId);


}
