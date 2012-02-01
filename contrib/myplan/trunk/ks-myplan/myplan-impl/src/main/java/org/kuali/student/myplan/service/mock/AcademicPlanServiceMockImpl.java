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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AcademicPlanServiceMockImpl implements AcademicPlanService {
    @Override
    public LearningPlan getLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PlanItem getPlanItem(@WebParam(name = "planItemId") String planItemId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PlanItem> getPlanItemsInPlanByType(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PlanItem> getPlanItemsInPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItem> list = new ArrayList<PlanItem>();
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
            List<AtpInfo> atpList = new ArrayList<AtpInfo>();
            AtpInfo atp = new AtpInfo();
            atp.setName( "atp1" );
            atp.setId( "atp1" );
            atp.setType( "atpType" );
            atp.setStartDate( new Date() );
            atp.setEndDate( new Date() );
            atpList.add( atp );
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
            List<AtpInfo> atpList = new ArrayList<AtpInfo>();
            AtpInfo atp = new AtpInfo();
            atp.setName( "atp2" );
            atp.setId( "atp2" );
            atp.setType( "atpType" );
            atp.setStartDate( new Date() );
            atp.setEndDate( new Date() );
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
            List<AtpInfo> atpList = new ArrayList<AtpInfo>();
            AtpInfo atp = new AtpInfo();
            atp.setName( "atp1" );
            atp.setId( "atp1" );
            atp.setType( "atpType" );
            atp.setStartDate( new Date() );
            atp.setEndDate( new Date() );
            atpList.add( atp );
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
            List<AtpInfo> atpList = new ArrayList<AtpInfo>();
            AtpInfo atp = new AtpInfo();
            atp.setName( "atp1" );
            atp.setId( "atp1" );
            atp.setType( "atpType" );
            atp.setStartDate( new Date() );
            atp.setEndDate( new Date() );
            atpList.add( atp );
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
            List<AtpInfo> atpList = new ArrayList<AtpInfo>();
            AtpInfo atp = new AtpInfo();
            atp.setName( "atp1" );
            atp.setId( "atp1" );
            atp.setType( "atpType" );
            atp.setStartDate( new Date() );
            atp.setEndDate( new Date() );
            atpList.add( atp );
            plan.setPlanPeriods( atpList );

            plan.setId( "1" );
            list.add( plan );
        }

        // UnComment next line if testing for empty list and comment out 'return list';
        //return new ArrayList<PlanItem>();
        return list;
    }

    @Override
    public List<PlanItem> getPlanItemsInPlanByAtp(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PlanItemSet getPlanItemSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<PlanItem> getPlanItemsInSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LearningPlan> getLearningPlansForStudentByType(@WebParam(name = "studentId") String studentId, @WebParam(name = "planTypeKey") String planTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<LearningPlan> list = new ArrayList<LearningPlan>();
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
    public List<ValidationResultInfo> validatePlanItemSet(@WebParam(name = "validationType") String validationType, @WebParam(name = "planItemInfo") PlanItemSetInfo planItemSetInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getDataDictionaryEntryKeys(@WebParam(name = "context") ContextInfo context) throws OperationFailedException, MissingParameterException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DictionaryEntryInfo getDataDictionaryEntry(@WebParam(name = "entryKey") String entryKey, @WebParam(name = "context") ContextInfo context) throws OperationFailedException, MissingParameterException, PermissionDeniedException, DoesNotExistException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypeInfo getType(@WebParam(name = "typeKey") String typeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeInfo> getTypesByRefObjectURI(@WebParam(name = "refObjectURI") String refObjectURI, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeInfo> getAllowedTypesForType(@WebParam(name = "ownerTypeKey") String ownerTypeKey, @WebParam(name = "relatedRefObjectURI") String relatedRefObjectURI, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeTypeRelationInfo> getTypeRelationsByOwnerType(@WebParam(name = "ownerTypeKey") String ownerTypeKey, @WebParam(name = "relationTypeKey") String relationTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
