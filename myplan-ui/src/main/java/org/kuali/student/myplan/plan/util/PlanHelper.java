package org.kuali.student.myplan.plan.util;

import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.RecommendedItemDataObject;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/28/13
 * Time: 12:06 PM
 * To change this template use File | Settings | File Templates.
 */
public interface PlanHelper {

    public PlanItemInfo getPlanItemByAtpAndType(String learningPlanId, String refObjId, String atpId, String planItemType, String courseCd);

    public PlanItemInfo getPlannedOrBackupPlanItem(String courseId, String courseCd, String atpId);

    public List<PlanItemInfo> getPlanItemsByTypes(String studentId, List<String> planItemTypes);

    public LearningPlan getLearningPlan(String studentId);

    public List<RecommendedItemDataObject> getRecommendedItems(String refObjId);

    public String getCrossListedCourse(List<AttributeInfo> attributeInfoList);

    public Map<String, String> getPlanItemIdAndRefObjIdByRefObjType(String learningPlanId, String refObjType, String termId);

    public List<PlannedCourseDataObject> getPlanItemListByTermId(String planItemType, String studentId, String termId) throws InvalidParameterException, MissingParameterException, DoesNotExistException, OperationFailedException;


}
