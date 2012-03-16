package org.kuali.student.myplan.service.mock;

import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemSetInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.infc.PlanItemSet;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.r2.common.datadictionary.dto.DictionaryEntryInfo;
import org.kuali.student.r2.common.dto.*;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.core.atp.dto.AtpInfo;

import javax.jws.WebParam;
import java.util.*;

public class AcademicPlanServiceMockImpl implements AcademicPlanService {
    @Override
    public LearningPlanInfo getLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PlanItemInfo getPlanItem(@WebParam(name = "planItemId") String planItemId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByType(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> list = new ArrayList<PlanItemInfo>();
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId( "planItem1" );
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr( richText );
            plan.setLearningPlanId( learningPlanId );
            // ENGL 101
            String cluID = "059881be-4168-4f99-9e64-310c8c50ae7b";
            plan.setRefObjectId( cluID );
            String type = "";
            plan.setRefObjectType( type );
            Set<String> atpList = new HashSet<String>();
            atpList.add("atp1");
            plan.setPlanPeriods( atpList );

            plan.setId( "1" );
            list.add( plan );
        }
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId( "planItem2" );
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr( richText );
            plan.setLearningPlanId( learningPlanId );
            // ENGL 101
            String cluID = "059b348d-8547-46ca-86ab-8240576b46ac";
            plan.setRefObjectId( cluID );
            String type = "";
            plan.setRefObjectType( type );
            Set<String> atpList = new HashSet<String>();
            String atp = "atp2";
            atpList.add( atp );
            plan.setPlanPeriods( atpList );

            plan.setId( "2" );
            list.add( plan );
        }
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId( "planItem1" );
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr( richText );
            plan.setLearningPlanId( learningPlanId );
            // ENGL 101
            String cluID = "05a2ec10-4c83-4f17-b3d1-938ffdab6ac6";
            plan.setRefObjectId( cluID );
            String type = "";
            plan.setRefObjectType( type );
            Set<String> atpList = new HashSet<String>();
            AtpInfo atp = new AtpInfo();
            atpList.add("atp1");
            plan.setPlanPeriods( atpList );

            plan.setId( "1" );
            list.add( plan );
        }
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId( "planItem1" );
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr( richText );
            plan.setLearningPlanId( learningPlanId );
            // CHEM 101
            String cluID = "05a9d09d-1d65-4c8b-a6d1-095baba5d7fe";
            plan.setRefObjectId( cluID );
            String type = "";
            plan.setRefObjectType( type );
            Set<String> atpList = new HashSet<String>();
            atpList.add("atp1");
            plan.setPlanPeriods( atpList );

            plan.setId( "1" );
            list.add( plan );
        }
        {
            PlanItemInfo plan = new PlanItemInfo();
            plan.setId( "planItem1" );
            RichTextInfo richText = new RichTextInfo();
            plan.setDescr( richText );
            plan.setLearningPlanId( learningPlanId );
            // HIST 101
            String cluID = "05a9d09d-1d65-4c8b-a6d1-095baba5d7fe";
            plan.setRefObjectId( cluID );
            String type = "";
            plan.setRefObjectType( type );
            Set<String> atpList = new HashSet<String>();
            atpList.add( "atp1" );
            plan.setPlanPeriods( atpList );

            plan.setId( "1" );
            list.add( plan );
        }

        // UnComment next line if testing for empty list and comment out 'return list';
        //return new ArrayList<PlanItem>();
        return list;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByAtp(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        plan.setStudentId( studentId );
        plan.setId( "learningPlan1" );
        list.add( plan );
        return list;
    }

    @Override
    public LearningPlanInfo createLearningPlan(@WebParam(name = "learningPlan") LearningPlanInfo learningPlan, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PlanItemInfo createPlanItem(@WebParam(name = "planItem") PlanItemInfo planItem, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateLearningPlan(@WebParam(name = "validationType") String validationType, @WebParam(name = "learningPlanInfo") LearningPlanInfo learningPlanInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validatePlanItem(@WebParam(name = "validationType") String validationType, @WebParam(name = "planItemInfo") PlanItemInfo planItemInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validatePlanItemSet(@WebParam(name = "validationType") String validationType, @WebParam(name = "planItemSetInfo") PlanItemSetInfo planItemSetInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
