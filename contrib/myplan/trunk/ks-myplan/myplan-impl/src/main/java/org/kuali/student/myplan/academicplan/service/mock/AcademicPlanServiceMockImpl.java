package org.kuali.student.myplan.academicplan.service.mock;

import org.kuali.student.common.util.UUIDHelper;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemSetInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultCellInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.dto.SearchResultRowInfo;
import org.kuali.student.r2.core.search.infc.SearchResultCell;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AcademicPlanServiceMockImpl implements AcademicPlanService {

    public List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
    public Map<String, List<PlanItemInfo>> planItemsMap = new HashMap<String, List<PlanItemInfo>>();
    public List<LearningPlanInfo> learningPlanItemInfos = new ArrayList<LearningPlanInfo>();

    public List<PlanItemInfo> getPlanItemInfos() {
        return planItemInfos;
    }

    public void setPlanItemInfos(List<PlanItemInfo> planItemInfos) {
        this.planItemInfos = planItemInfos;
    }

    public List<LearningPlanInfo> getLearningPlanItemInfos() {
        return learningPlanItemInfos;
    }

    public void setLearningPlanItemInfos(List<LearningPlanInfo> learningPlanItemInfos) {
        this.learningPlanItemInfos = learningPlanItemInfos;
    }

    public Map<String, List<PlanItemInfo>> getPlanItemsMap() {
        return planItemsMap;
    }

    public void setPlanItemsMap(Map<String, List<PlanItemInfo>> planItemsMap) {
        this.planItemsMap = planItemsMap;
    }

    @Override
    public LearningPlanInfo getLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        LearningPlanInfo learningPlanInfo = null;
        for (LearningPlanInfo planItemInfo1 : getLearningPlanItemInfos()) {
            if (planItemInfo1.getId().equals(learningPlanId)) {
                learningPlanInfo = planItemInfo1;
            }
        }
        if (learningPlanInfo == null) {
            throw new DoesNotExistException();
        }
        return learningPlanInfo;
    }

    @Override
    public PlanItemInfo getPlanItem(@WebParam(name = "planItemId") String planItemId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        PlanItemInfo planItemInfo = null;
        for (PlanItemInfo planItemInfo1 : getPlanItemInfos()) {
            if (planItemInfo1.getId().equals(planItemId)) {
                planItemInfo = planItemInfo1;
            }
        }
        if (planItemInfo == null) {
            throw new DoesNotExistException();
        }
        return planItemInfo;
    }


    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByType(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> planItemInfoList = new ArrayList<PlanItemInfo>();
        for (PlanItemInfo planItemInfo : planItemInfos) {
            if (planItemInfo.getTypeKey().equals(planItemTypeKey)) {
                planItemInfoList.add(planItemInfo);
            }
        }
        return planItemInfoList;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        if (planItemsMap.containsKey(learningPlanId)) {
            return planItemsMap.get(learningPlanId);
        }
        return new ArrayList<PlanItemInfo>();
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByAtp(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
        return planItemInfos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByAtpAndRefObjType(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "refObjectType") String refObjectType, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
        return planItemInfos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByRefObjectIdByRefObjectType(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "refObjectType") String refObjectType, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
        if (planItemsMap.containsKey(learningPlanId)) {
            for (PlanItemInfo planItemInfo : planItemsMap.get(learningPlanId)) {
                if (planItemInfo.getRefObjectId().equals(refObjectId) && planItemInfo.getRefObjectType().equals(refObjectType)) {
                    planItemInfos.add(planItemInfo);
                }
            }
        }
        return planItemInfos;
    }

    @Override
    public PlanItemSetInfo getPlanItemSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LearningPlanInfo> getLearningPlansForStudentByType(@WebParam(name = "studentId") String studentId, @WebParam(name = "planTypeKey") String planTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<LearningPlanInfo> list = new ArrayList<LearningPlanInfo>();
        LearningPlanInfo plan = new LearningPlanInfo();
        plan.setStudentId(studentId);
        plan.setId("learningPlan1");
        plan.setTypeKey("kuali.academicplan.type.plan");
        list.add(plan);
        return list;
    }

    @Override
    public List<LearningPlanInfo> getLearningPlansForPlanProgramByType(@WebParam(name = "name") String planProgram, @WebParam(name = "planTypeKey") String planTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;
    }

    @Override
    public LearningPlanInfo createLearningPlan(@WebParam(name = "learningPlan") LearningPlanInfo learningPlan, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {

        try {
            validateLearningPlan(null, learningPlan, new ContextInfo());
        } catch (DoesNotExistException e) {
            //nothing to do
        }
        learningPlan.setId(UUIDHelper.genStringUUID());
        learningPlanItemInfos.add(learningPlan);
        return learningPlan;
    }

    public LearningPlanInfo copyLearningPlan(@WebParam(name = "learningPlanId") String fromLearningPlanId, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null; //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public PlanItemInfo createPlanItem(@WebParam(name = "planItem") PlanItemInfo planItem, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        try {
            validatePlanItem(null, planItem, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        planItem.setId(UUIDHelper.genStringUUID());
        planItemInfos.add(planItem);
        if (planItemsMap.containsKey(planItem.getLearningPlanId())) {
            planItemsMap.get(planItem.getLearningPlanId()).add(planItem);
        } else {
            List<PlanItemInfo> planItemInfoList = new ArrayList<PlanItemInfo>();
            planItemInfoList.add(planItem);
            planItemsMap.put(planItem.getLearningPlanId(), planItemInfoList);
        }
        return planItem;
    }

    @Override
    public PlanItemSetInfo createPlanItemSet(@WebParam(name = "planItemSet") PlanItemSetInfo planItemSet, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LearningPlanInfo updateLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "learningPlan") LearningPlanInfo learningPlan, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DoesNotExistException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PlanItemInfo updatePlanItem(@WebParam(name = "planItemId") String planItemId, @WebParam(name = "planItem") PlanItemInfo planItem, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DoesNotExistException {
        PlanItemInfo planItemInfo = getPlanItem(planItemId, context);
        planItemInfo.setRefObjectId(planItem.getRefObjectId());
        planItemInfo.setRefObjectType(planItem.getRefObjectType());
        planItemInfo.setCredit(planItem.getCredit());
        planItemInfo.setPlanPeriods(planItem.getPlanPeriods());

        for (PlanItemInfo planItemInfo1 : planItemInfos) {
            if (planItemInfo1.getId().equals(planItemId)) {
                planItemInfo1.setRefObjectId(planItem.getRefObjectId());
                planItemInfo1.setRefObjectType(planItem.getRefObjectType());
                planItemInfo1.setCredit(planItem.getCredit());
                planItemInfo1.setPlanPeriods(planItem.getPlanPeriods());
            }
        }

        for (PlanItemInfo planItemInfo1 : planItemsMap.get(planItemInfo.getLearningPlanId())) {
            if (planItemInfo1.getId().equals(planItemId)) {
                planItemInfo1.setRefObjectId(planItem.getRefObjectId());
                planItemInfo1.setRefObjectType(planItem.getRefObjectType());
                planItemInfo1.setCredit(planItem.getCredit());
                planItemInfo1.setPlanPeriods(planItem.getPlanPeriods());
            }
        }

        return planItemInfo;
    }

    @Override
    public PlanItemSetInfo updatePlanItemSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "planItemSet") PlanItemSetInfo planItemSet, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deletePlanItem(@WebParam(name = "planItemId") String planItemId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deletePlanItemSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        setPlanItemInfos(new ArrayList<PlanItemInfo>());
        return null;
    }

    @Override
    public List<ValidationResultInfo> validateLearningPlan(@WebParam(name = "validationType") String validationType, @WebParam(name = "learningPlanInfo") LearningPlanInfo learningPlanInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, AlreadyExistsException {
        List<ValidationResultInfo> validationResultInfos = new ArrayList<ValidationResultInfo>();
        for (LearningPlanInfo learningPlanInfo1 : getLearningPlanItemInfos()) {
            if (AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_TEMPLATE.equals(learningPlanInfo.getTypeKey())) {
                if (learningPlanInfo1.getName().equals(learningPlanInfo.getName()) && learningPlanInfo1.getPlanProgram().equals(learningPlanInfo.getPlanProgram())) {
                    throw new AlreadyExistsException();
                }
            } else {
                if (learningPlanInfo1.getId().equals(learningPlanInfo.getId())) {
                    throw new OperationFailedException();
                }
            }
        }
        return validationResultInfos;
    }

    @Override
    public List<ValidationResultInfo> validatePlanItem(@WebParam(name = "validationType") String validationType,
                                                       @WebParam(name = "planItemInfo") PlanItemInfo planItemInfo,
                                                       @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, AlreadyExistsException {

        List<ValidationResultInfo> validationResultInfos = new ArrayList<ValidationResultInfo>();
        /*Duplicate check should only be for course and sections not for placeholders*/
        if (AcademicPlanServiceConstants.COURSE_TYPE.equals(planItemInfo.getRefObjectType()) || AcademicPlanServiceConstants.SECTION_TYPE.equals(planItemInfo.getRefObjectType())) {
            for (PlanItemInfo planItem : getPlanItemInfos()) {
                if (planItem.getRefObjectId().equals(planItemInfo.getRefObjectId()) && planItem.getRefObjectType().equals(planItemInfo.getRefObjectType()) && planItem.getPlanPeriods().get(0).equals(planItemInfo.getPlanPeriods().get(0))) {
                    throw new AlreadyExistsException();
                }
            }
        }


        return validationResultInfos;
    }

    @Override
    public List<ValidationResultInfo> validatePlanItemSet(@WebParam(name = "validationType") String validationType, @WebParam(name = "planItemSetInfo") PlanItemSetInfo planItemSetInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeInfo> getSearchTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypeInfo getSearchType(@WebParam(name = "searchTypeKey") String searchTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchResultInfo search(SearchRequestInfo searchRequestInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws MissingParameterException, InvalidParameterException, OperationFailedException, PermissionDeniedException {
        SearchResultInfo result = new SearchResultInfo();
        String programId = null;
        String title = null;
        String planType = null;
        if (searchRequestInfo.getSearchKey().equals("learningPlan.id.by.programAndName")) {
            for (LearningPlanInfo learningPlanInfo : learningPlanItemInfos) {
                if (learningPlanInfo.getPlanProgram().equals(programId) && learningPlanInfo.getName().equals(title)) {
                    List<SearchResultRowInfo> rowInfos = new ArrayList<SearchResultRowInfo>();
                    SearchResultRowInfo rowInfo = new SearchResultRowInfo();
                    rowInfo.addCell("learningPlan.id", learningPlanInfo.getId());
                    rowInfos.add(rowInfo);
                    result.setRows(rowInfos);
                    return result;
                }
            }
        }
        return result;
    }
}
