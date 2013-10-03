package org.kuali.student.myplan.service.mock;

import org.kuali.student.common.dictionary.dto.ObjectStructureDefinition;
import org.kuali.student.common.dto.StatusInfo;
import org.kuali.student.common.exceptions.*;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.common.validation.dto.ValidationResultInfo;
import org.kuali.student.common.versionmanagement.dto.VersionDisplayInfo;
import org.kuali.student.lum.lu.dto.*;
import org.kuali.student.lum.lu.service.LuService;

import javax.jws.WebParam;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 10/3/13
 * Time: 2:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class LuServiceMockImpl implements LuService {

    Map<String, SearchResult> searchResultMap = new HashMap<String, SearchResult>();

    @Override
    public List<DeliveryMethodTypeInfo> getDeliveryMethodTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DeliveryMethodTypeInfo getDeliveryMethodType(@WebParam(name = "deliveryMethodTypeKey") String deliveryMethodTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<InstructionalFormatTypeInfo> getInstructionalFormatTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public InstructionalFormatTypeInfo getInstructionalFormatType(@WebParam(name = "instructionalFormatTypeKey") String instructionalFormatTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuTypeInfo> getLuTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuTypeInfo getLuType(@WebParam(name = "luTypeKey") String luTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuCodeTypeInfo> getLuCodeTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuCodeTypeInfo getLuCodeType(@WebParam(name = "luCodeTypeKey") String luCodeTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuLuRelationTypeInfo> getLuLuRelationTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuLuRelationTypeInfo getLuLuRelationType(@WebParam(name = "luLuRelationTypeKey") String luLuRelationTypeKey) throws OperationFailedException, MissingParameterException, DoesNotExistException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllowedLuLuRelationTypesForLuType(@WebParam(name = "luTypeKey") String luTypeKey, @WebParam(name = "relatedLuTypeKey") String relatedLuTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuPublicationTypeInfo> getLuPublicationTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuPublicationTypeInfo getLuPublicationType(@WebParam(name = "luPublicationTypeKey") String luPublicationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getLuPublicationTypesForLuType(@WebParam(name = "luTypeKey") String luTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluResultTypeInfo> getCluResultTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluResultTypeInfo getCluResultType(@WebParam(name = "cluResultTypeKey") String cluResultTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluResultTypeInfo> getCluResultTypesForLuType(@WebParam(name = "luTypeKey") String luTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ResultUsageTypeInfo> getResultUsageTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ResultUsageTypeInfo getResultUsageType(@WebParam(name = "resultUsageTypeKey") String resultUsageTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllowedResultUsageTypesForLuType(@WebParam(name = "luTypeKey") String luTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllowedResultComponentTypesForResultUsageType(@WebParam(name = "resultUsageTypeKey") String resultUsageTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluLoRelationTypeInfo> getCluLoRelationTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluLoRelationTypeInfo getCluLoRelationType(@WebParam(name = "cluLoRelationTypeKey") String cluLoRelationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllowedCluLoRelationTypesForLuType(@WebParam(name = "luTypeKey") String luTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluSetTypeInfo> getCluSetTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluSetTypeInfo getCluSetType(@WebParam(name = "cluSetTypeKey") String cluSetTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluInfo getClu(@WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluInfo> getClusByIdList(@WebParam(name = "cluIdList") List<String> cluIdList) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluInfo> getClusByLuType(@WebParam(name = "luTypeKey") String luTypeKey, @WebParam(name = "luState") String luState) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCluIdsByLuType(@WebParam(name = "luTypeKey") String luTypeKey, @WebParam(name = "luState") String luState) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllowedLuLuRelationTypesByCluId(@WebParam(name = "cluId") String cluId, @WebParam(name = "relatedCluId") String relatedCluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluInfo> getClusByRelation(@WebParam(name = "relatedCluId") String relatedCluId, @WebParam(name = "luLuRelationType") String luLuRelationType) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCluIdsByRelation(@WebParam(name = "relatedCluId") String relatedCluId, @WebParam(name = "luLuRelationType") String luLuRelationType) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluInfo> getRelatedClusByCluId(@WebParam(name = "cluId") String cluId, @WebParam(name = "luLuRelationType") String luLuRelationType) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getRelatedCluIdsByCluId(@WebParam(name = "cluId") String cluId, @WebParam(name = "luLuRelationType") String luLuRelationType) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluCluRelationInfo getCluCluRelation(@WebParam(name = "cluCluRelationId") String cluCluRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluCluRelationInfo> getCluCluRelationsByClu(@WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluPublicationInfo> getCluPublicationsByCluId(@WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluPublicationInfo> getCluPublicationsByType(@WebParam(name = "luPublicationTypeKey") String luPublicationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluPublicationInfo getCluPublication(@WebParam(name = "cluPublicationId") String cluPublicationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluResultInfo getCluResult(@WebParam(name = "cluResultId") String cluResultId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluResultInfo> getCluResultByClu(@WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCluIdsByResultUsageType(@WebParam(name = "resultUsageTypeKey") String resultUsageTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCluIdsByResultComponent(@WebParam(name = "resultComponentId") String resultComponentId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluLoRelationInfo getCluLoRelation(@WebParam(name = "cluLoRelationId") String cluLoRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluLoRelationInfo> getCluLoRelationsByClu(@WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluLoRelationInfo> getCluLoRelationsByLo(@WebParam(name = "loId") String loId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getResourceRequirementsForCluId(@WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluSetInfo getCluSetInfo(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluSetTreeViewInfo getCluSetTreeView(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluSetInfo> getCluSetInfoByIdList(@WebParam(name = "cluSetIdList") List<String> cluSetIdList) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCluSetIdsFromCluSet(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Boolean isCluSetDynamic(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluInfo> getClusFromCluSet(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCluIdsFromCluSet(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CluInfo> getAllClusInCluSet(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllCluIdsInCluSet(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Boolean isCluInCluSet(@WebParam(name = "cluId") String cluId, @WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuiInfo getLui(@WebParam(name = "luiId") String luiId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuiInfo> getLuisByIdList(@WebParam(name = "luiIdList") List<String> luiIdList) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuiInfo> getLuisInAtpByCluId(@WebParam(name = "cluId") String cluId, @WebParam(name = "atpKey") String atpKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getLuiIdsByCluId(@WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getLuiIdsInAtpByCluId(@WebParam(name = "cluId") String cluId, @WebParam(name = "atpKey") String atpKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllowedLuLuRelationTypesByLuiId(@WebParam(name = "luiId") String luiId, @WebParam(name = "relatedLuiId") String relatedLuiId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuiInfo> getLuisByRelation(@WebParam(name = "relatedLuiId") String relatedLuiId, @WebParam(name = "luLuRelationType") String luLuRelationType) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getLuiIdsByRelation(@WebParam(name = "relatedLuiId") String relatedLuiId, @WebParam(name = "luLuRelationType") String luLuRelationType) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuiInfo> getRelatedLuisByLuiId(@WebParam(name = "luiId") String luiId, @WebParam(name = "luLuRelationType") String luLuRelationType) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getRelatedLuiIdsByLuiId(@WebParam(name = "luiId") String luiId, @WebParam(name = "luLuRelationType") String luLuRelationType) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuiLuiRelationInfo getLuiLuiRelation(@WebParam(name = "luiLuiRelationId") String luiLuiRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<LuiLuiRelationInfo> getLuiLuiRelationsByLui(@WebParam(name = "luiId") String luiId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateClu(@WebParam(name = "validationType") String validationType, @WebParam(name = "cluInfo") CluInfo cluInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluInfo createClu(@WebParam(name = "luTypeKey") String luTypeKey, @WebParam(name = "cluInfo") CluInfo cluInfo) throws AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluInfo updateClu(@WebParam(name = "cluId") String cluId, @WebParam(name = "cluInfo") CluInfo cluInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteClu(@WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, DependentObjectsExistException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluInfo createNewCluVersion(@WebParam(name = "cluId") String cluId, @WebParam(name = "versionComment") String versionComment) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo setCurrentCluVersion(@WebParam(name = "cluVersionId") String cluVersionId, @WebParam(name = "currentVersionStart") Date currentVersionStart) throws DoesNotExistException, InvalidParameterException, MissingParameterException, IllegalVersionSequencingException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluInfo updateCluState(@WebParam(name = "cluId") String cluId, @WebParam(name = "luState") String luState) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCluCluRelation(@WebParam(name = "validationType") String validationType, @WebParam(name = "cluCluRelationInfo") CluCluRelationInfo cluCluRelationInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluCluRelationInfo createCluCluRelation(@WebParam(name = "cluId") String cluId, @WebParam(name = "relatedCluId") String relatedCluId, @WebParam(name = "luLuRelationTypeKey") String luLuRelationTypeKey, @WebParam(name = "cluCluRelationInfo") CluCluRelationInfo cluCluRelationInfo) throws AlreadyExistsException, CircularRelationshipException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluCluRelationInfo updateCluCluRelation(@WebParam(name = "cluCluRelationId") String cluCluRelationId, @WebParam(name = "cluCluRelationInfo") CluCluRelationInfo cluCluRelationInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCluCluRelation(@WebParam(name = "cluCluRelationId") String cluCluRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCluPublication(@WebParam(name = "validationType") String validationType, @WebParam(name = "cluPublicationInfo") CluPublicationInfo cluPublicationInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluPublicationInfo createCluPublication(@WebParam(name = "cluId") String cluId, @WebParam(name = "luPublicationType") String luPublicationType, @WebParam(name = "cluPublicationInfo") CluPublicationInfo cluPublicationInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluPublicationInfo updateCluPublication(@WebParam(name = "cluPublicationId") String cluPublicationId, @WebParam(name = "cluPublicationInfo") CluPublicationInfo cluPublicationInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCluPublication(@WebParam(name = "cluPublicationId") String cluPublicationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, DependentObjectsExistException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCluResult(@WebParam(name = "validationType") String validationType, @WebParam(name = "cluResultInfo") CluResultInfo cluResultInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluResultInfo createCluResult(@WebParam(name = "cluId") String cluId, @WebParam(name = "cluResultType") String cluResultType, @WebParam(name = "cluResultInfo") CluResultInfo cluResultInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DoesNotExistException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluResultInfo updateCluResult(@WebParam(name = "cluResultId") String cluResultId, @WebParam(name = "cluResultInfo") CluResultInfo cluResultInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCluResult(@WebParam(name = "cluResultId") String cluResultId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, DependentObjectsExistException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCluLoRelation(@WebParam(name = "validationType") String validationType, @WebParam(name = "cluLoRelationInfo") CluLoRelationInfo cluLoRelationInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluLoRelationInfo createCluLoRelation(@WebParam(name = "cluId") String cluId, @WebParam(name = "loId") String loId, @WebParam(name = "cluLoRelationType") String cluLoRelationType, @WebParam(name = "cluLoRelationInfo") CluLoRelationInfo cluLoRelationInfo) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluLoRelationInfo updateCluLoRelation(@WebParam(name = "cluLoRelationId") String cluLoRelationId, @WebParam(name = "cluLoRelationInfo") CluLoRelationInfo cluLoRelationInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCluLoRelation(@WebParam(name = "cluLoRelationId") String cluLoRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo addCluResourceRequirement(@WebParam(name = "resourceTypeKey") String resourceTypeKey, @WebParam(name = "cluId") String cluId) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeCluResourceRequirement(@WebParam(name = "resourceTypeKey") String resourceTypeKey, @WebParam(name = "cluId") String cluId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCluSet(@WebParam(name = "validationType") String validationType, @WebParam(name = "cluSetInfo") CluSetInfo cluSetInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluSetInfo createCluSet(@WebParam(name = "cluSetType") String cluSetType, @WebParam(name = "cluSetInfo") CluSetInfo cluSetInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, UnsupportedActionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CluSetInfo updateCluSet(@WebParam(name = "cluSetId") String cluSetId, @WebParam(name = "cluSetInfo") CluSetInfo cluSetInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException, UnsupportedActionException, CircularRelationshipException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCluSet(@WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo addCluSetToCluSet(@WebParam(name = "cluSetId") String cluSetId, @WebParam(name = "addedCluSetId") String addedCluSetId) throws CircularRelationshipException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, UnsupportedActionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo addCluSetsToCluSet(@WebParam(name = "cluSetId") String cluSetId, @WebParam(name = "addedCluSetIdList") List<String> addedCluSetIdList) throws CircularRelationshipException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, UnsupportedActionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeCluSetFromCluSet(@WebParam(name = "cluSetId") String cluSetId, @WebParam(name = "removedCluSetId") String removedCluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, UnsupportedActionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo addCluToCluSet(@WebParam(name = "cluId") String cluId, @WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, UnsupportedActionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo addClusToCluSet(@WebParam(name = "cluIdList") List<String> cluIdList, @WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, UnsupportedActionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeCluFromCluSet(@WebParam(name = "cluId") String cluId, @WebParam(name = "cluSetId") String cluSetId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, UnsupportedActionException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateLui(@WebParam(name = "validationType") String validationType, @WebParam(name = "luiInfo") LuiInfo luiInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuiInfo createLui(@WebParam(name = "cluId") String cluId, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "luiInfo") LuiInfo luiInfo) throws AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        String[] idTitle = cluId.split(":");
        SearchResult searchResult = new SearchResult();
        List<SearchResultRow> resultRows = new ArrayList<SearchResultRow>();
        List<SearchResultCell> searchResultCells = new ArrayList<SearchResultCell>();
        searchResultCells.add(new SearchResultCell("lu.resultColumn.cluId", idTitle[0]));
        searchResultCells.add(new SearchResultCell("id.lngName", idTitle[1]));
        SearchResultRow searchResultRow = new SearchResultRow();
        searchResultRow.setCells(searchResultCells);
        resultRows.add(searchResultRow);
        searchResult.setRows(resultRows);
        searchResultMap.put(idTitle[0], searchResult);
        return null;
    }

    @Override
    public LuiInfo updateLui(@WebParam(name = "luiId") String luiId, @WebParam(name = "luiInfo") LuiInfo luiInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteLui(@WebParam(name = "luiId") String luiId) throws DependentObjectsExistException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuiInfo updateLuiState(@WebParam(name = "luiId") String luiId, @WebParam(name = "luState") String luState) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateLuiLuiRelation(@WebParam(name = "validationType") String validationType, @WebParam(name = "luiLuiRelationInfo") LuiLuiRelationInfo luiLuiRelationInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuiLuiRelationInfo createLuiLuiRelation(@WebParam(name = "luiId") String luiId, @WebParam(name = "relatedLuiId") String relatedLuiId, @WebParam(name = "luLuRelationType") String luLuRelationType, @WebParam(name = "luiLuiRelationInfo") LuiLuiRelationInfo luiLuiRelationInfo) throws AlreadyExistsException, CircularRelationshipException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public LuiLuiRelationInfo updateLuiLuiRelation(@WebParam(name = "luiLuiRelationId") String luiLuiRelationId, @WebParam(name = "luiLuiRelationInfo") LuiLuiRelationInfo luiLuiRelationInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteLuiLuiRelation(@WebParam(name = "luiLuiRelationId") String luiLuiRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getObjectTypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ObjectStructureDefinition getObjectStructure(@WebParam(name = "objectTypeKey") String objectTypeKey) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SearchTypeInfo> getSearchTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchTypeInfo getSearchType(@WebParam(name = "searchTypeKey") String searchTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SearchTypeInfo> getSearchTypesByResult(@WebParam(name = "searchResultTypeKey") String searchResultTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SearchTypeInfo> getSearchTypesByCriteria(@WebParam(name = "searchCriteriaTypeKey") String searchCriteriaTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SearchResultTypeInfo> getSearchResultTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchResultTypeInfo getSearchResultType(@WebParam(name = "searchResultTypeKey") String searchResultTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SearchCriteriaTypeInfo> getSearchCriteriaTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchCriteriaTypeInfo getSearchCriteriaType(@WebParam(name = "searchCriteriaTypeKey") String searchCriteriaTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchResult search(SearchRequest searchRequest) throws MissingParameterException {
        SearchResult searchResult = new SearchResult();
        searchResult.setRows(new ArrayList<SearchResultRow>());
        if (searchRequest.getParams() != null && searchRequest.getParams().size() >= 2) {
            String key = searchRequest.getParams().get(0).getValue() + " " + searchRequest.getParams().get(1).getValue();
            return searchResultMap.get(key) != null ? searchResultMap.get(key) : searchResult;
        }

        return searchResult;
    }

    @Override
    public List<VersionDisplayInfo> getVersions(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getFirstVersion(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getLatestVersion(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getCurrentVersion(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getVersionBySequenceNumber(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "sequence") Long sequence) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getCurrentVersionOnDate(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "date") Date date) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<VersionDisplayInfo> getVersionsInDateRange(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "from") Date from, @WebParam(name = "to") Date to) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
