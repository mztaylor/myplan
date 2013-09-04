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

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.List;

public class AcademicPlanServiceMockImpl implements AcademicPlanService {

    public List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();

    public List<PlanItemInfo> getPlanItemInfos() {
        return planItemInfos;
    }

    public void setPlanItemInfos(List<PlanItemInfo> planItemInfos) {
        this.planItemInfos = planItemInfos;
    }

    @Override
    public LearningPlanInfo getLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return planItemInfos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> list = new ArrayList<PlanItemInfo>();
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId("planItem1");
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr(richText);
            plan.setLearningPlanId(learningPlanId);
            // ENGL 101
            String cluID = "059881be-4168-4f99-9e64-310c8c50ae7b";
            plan.setRefObjectId(cluID);
            String type = "";
            plan.setRefObjectType(type);
            List<String> atps = new ArrayList<String>();
            atps.add("kuali.uw.atp.autumn2011");
            plan.setPlanPeriods(atps);

            plan.setId("1");
            list.add(plan);
        }
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId("planItem2");
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr(richText);
            plan.setLearningPlanId(learningPlanId);
            // ENGL 101
            String cluID = "059b348d-8547-46ca-86ab-8240576b46ac";
            plan.setRefObjectId(cluID);
            String type = "";
            plan.setRefObjectType(type);
            List<String> atpList = new ArrayList<String>();
            String atp = "atp2";
            atpList.add(atp);
            plan.setPlanPeriods(atpList);

            plan.setId("2");
            list.add(plan);
        }
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId("planItem1");
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr(richText);
            plan.setLearningPlanId(learningPlanId);
            // ENGL 101
            String cluID = "05a2ec10-4c83-4f17-b3d1-938ffdab6ac6";
            plan.setRefObjectId(cluID);
            String type = "";
            plan.setRefObjectType(type);
            List<String> atps = new ArrayList<String>();
            atps.add("kuali.uw.atp.autumn2011");
            plan.setPlanPeriods(atps);

            plan.setId("1");
            list.add(plan);
        }
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId("planItem1");
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr(richText);
            plan.setLearningPlanId(learningPlanId);
            // CHEM 101
            String cluID = "05a9d09d-1d65-4c8b-a6d1-095baba5d7fe";
            plan.setRefObjectId(cluID);
            String type = "";
            plan.setRefObjectType(type);
            List<String> atps = new ArrayList<String>();
            atps.add("kuali.uw.atp.spring2011");
            plan.setPlanPeriods(atps);

            plan.setId("1");
            list.add(plan);
        }
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId("planItem1");
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr(richText);
            plan.setLearningPlanId(learningPlanId);
            // HIST 101
            String cluID = "05a9d09d-1d65-4c8b-a6d1-095baba5d7fe";
            plan.setRefObjectId(cluID);
            String type = "";
            plan.setRefObjectType(type);
            List<String> atps = new ArrayList<String>();
            atps.add("kuali.uw.atp.winter2011");
            plan.setPlanPeriods(atps);

            plan.setId("1");
            list.add(plan);
        }

        list.addAll(planItemInfos);

        // UnComment next line if testing for empty list and comment out 'return list';
        //return new ArrayList<PlanItem>();
        return list;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByAtp(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
        return planItemInfos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByRefObjectIdByRefObjectType(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "refObjectType") String refObjectType, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
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
    public LearningPlanInfo createLearningPlan(@WebParam(name = "learningPlan") LearningPlanInfo learningPlan, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public LearningPlanInfo copyLearningPlan(@WebParam(name = "learningPlanId") String fromLearningPlanId, @WebParam(name = "planTypeKey") String planTypeKey, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
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
    public List<ValidationResultInfo> validateLearningPlan(@WebParam(name = "validationType") String validationType, @WebParam(name = "learningPlanInfo") LearningPlanInfo learningPlanInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
}
