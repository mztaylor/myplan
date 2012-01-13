package org.kuali.student.myplan.academicplan.service.mock;

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
            String cluID = "75ca71e8-034e-45bf-8e45-84ab3f1d3d88";
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
            String cluID = "5d61c370-d2e7-453a-8e1d-689ecaa150fd";
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
            String cluID = "96dd52bf-978e-4c9b-a115-1ad0345ed7cc";
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
            String cluID = "dab829f3-e642-4479-aba3-f3e67d66f8df";
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
            String cluID = "dbf117fc-20ea-4177-acd4-c53249be3dcb";
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
    public LearningPlanInfo updateLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "learningPlan") LearningPlanInfo learningPlan, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public PlanItemInfo updatePlanItem(@WebParam(name = "planItemId") String planItemId, @WebParam(name = "planItem") PlanItemInfo planItem, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
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
    public List<String> getDataDictionaryEntryKeys(@WebParam(name = "context") ContextInfo contextInfo) throws OperationFailedException, MissingParameterException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DictionaryEntryInfo getDataDictionaryEntry(@WebParam(name = "entryKey") String s, @WebParam(name = "context") ContextInfo contextInfo) throws OperationFailedException, MissingParameterException, PermissionDeniedException, DoesNotExistException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypeInfo getType(@WebParam(name = "typeKey") String s, @WebParam(name = "context") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeInfo> getTypesByRefObjectURI(@WebParam(name = "refObjectURI") String s, @WebParam(name = "context") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeInfo> getAllowedTypesForType(@WebParam(name = "ownerTypeKey") String s, @WebParam(name = "relatedRefObjectURI") String s1, @WebParam(name = "context") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeTypeRelationInfo> getTypeRelationsByOwnerType(@WebParam(name = "ownerTypeKey") String s, @WebParam(name = "relationTypeKey") String s1, @WebParam(name = "context") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
