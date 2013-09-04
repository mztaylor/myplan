package org.kuali.student.myplan.service.mock;

import org.kuali.student.common.dictionary.old.dto.ObjectStructure;
import org.kuali.student.common.dto.StatusInfo;
import org.kuali.student.common.exceptions.*;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.common.validation.dto.ValidationResultInfo;
import org.kuali.student.core.organization.dto.*;
import org.kuali.student.core.organization.service.OrganizationService;

import javax.jws.WebParam;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/16/13
 * Time: 1:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class OrganizationServiceMockImpl implements OrganizationService {
    @Override
    public List<OrgHierarchyInfo> getOrgHierarchies() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgHierarchyInfo getOrgHierarchy(@WebParam(name = "orgHierarchyKey") String orgHierarchyKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgTypeInfo> getOrgTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgTypeInfo getOrgType(@WebParam(name = "orgTypeKey") String orgTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgOrgRelationTypeInfo> getOrgOrgRelationTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgOrgRelationTypeInfo getOrgOrgRelationType(@WebParam(name = "orgOrgRelationTypeKey") String orgOrgRelationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgOrgRelationTypeInfo> getOrgOrgRelationTypesForOrgType(@WebParam(name = "orgTypeKey") String orgTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgOrgRelationTypeInfo> getOrgOrgRelationTypesForOrgHierarchy(@WebParam(name = "orgHierarchyKey") String orgHierarchyKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgPersonRelationTypeInfo> getOrgPersonRelationTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgPersonRelationTypeInfo getOrgPersonRelationType(@WebParam(name = "orgPersonRelationTypeKey") String orgPersonRelationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgPersonRelationTypeInfo> getOrgPersonRelationTypesForOrgType(@WebParam(name = "orgTypeKey") String orgTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateOrg(@WebParam(name = "validationType") String validationType, @WebParam(name = "orgInfo") OrgInfo orgInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateOrgOrgRelation(@WebParam(name = "validationType") String validationType, @WebParam(name = "orgOrgRelationInfo") OrgOrgRelationInfo orgOrgRelationInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateOrgPersonRelation(@WebParam(name = "validationType") String validationType, @WebParam(name = "orgPersonRelationInfo") OrgPersonRelationInfo orgPersonRelationInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateOrgPositionRestriction(@WebParam(name = "validationType") String validationType, @WebParam(name = "orgPositionRestrictionInfo") OrgPositionRestrictionInfo orgPositionRestrictionInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgInfo getOrganization(@WebParam(name = "orgId") String orgId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgInfo> getOrganizationsByIdList(@WebParam(name = "orgIdList") List<String> orgIdList) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgOrgRelationInfo getOrgOrgRelation(@WebParam(name = "orgOrgRelationId") String orgOrgRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgOrgRelationInfo> getOrgOrgRelationsByIdList(@WebParam(name = "orgOrgRelationIdList") List<String> orgOrgRelationIdList) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgOrgRelationInfo> getOrgOrgRelationsByOrg(@WebParam(name = "orgId") String orgId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgOrgRelationInfo> getOrgOrgRelationsByRelatedOrg(@WebParam(name = "relatedOrgId") String relatedOrgId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Boolean hasOrgOrgRelation(@WebParam(name = "orgId") String orgId, @WebParam(name = "comparisonOrgId") String comparisonOrgId, @WebParam(name = "orgOrgRelationTypeKey") String orgOrgRelationTypeKey) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Boolean isDescendant(@WebParam(name = "orgId") String orgId, @WebParam(name = "descendantOrgId") String descendantOrgId, @WebParam(name = "orgHierarchy") String orgHierarchy) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllDescendants(@WebParam(name = "orgId") String orgId, @WebParam(name = "orgHierarchy") String orgHierarchy) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAllAncestors(@WebParam(name = "orgId") String orgId, @WebParam(name = "orgHierarchy") String orgHierarchy) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgPersonRelationInfo getOrgPersonRelation(@WebParam(name = "orgPersonRelationId") String orgPersonRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgPersonRelationInfo> getOrgPersonRelationsByIdList(@WebParam(name = "orgPersonRelationIdList") List<String> orgPersonRelationIdList) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getPersonIdsForOrgByRelationType(@WebParam(name = "orgId") String orgId, @WebParam(name = "orgPersonRelationTypeKey") String orgPersonRelationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgPersonRelationInfo> getOrgPersonRelationsByOrg(@WebParam(name = "orgId") String orgId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgPersonRelationInfo> getOrgPersonRelationsByPerson(@WebParam(name = "personId") String personId, @WebParam(name = "orgId") String orgId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgPersonRelationInfo> getAllOrgPersonRelationsByPerson(@WebParam(name = "personId") String personId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgPersonRelationInfo> getAllOrgPersonRelationsByOrg(@WebParam(name = "orgId") String orgId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Boolean hasOrgPersonRelation(@WebParam(name = "orgId") String orgId, @WebParam(name = "personId") String personId, @WebParam(name = "orgPersonRelationTypeKey") String orgPersonRelationTypeKey) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgPositionRestrictionInfo> getPositionRestrictionsByOrg(@WebParam(name = "orgId") String orgId) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, PermissionDeniedException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgInfo createOrganization(@WebParam(name = "orgTypeKey") String orgTypeKey, @WebParam(name = "orgInfo") OrgInfo orgInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgInfo updateOrganization(@WebParam(name = "orgId") String orgId, @WebParam(name = "orgInfo") OrgInfo orgInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteOrganization(@WebParam(name = "orgId") String orgId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgOrgRelationInfo createOrgOrgRelation(@WebParam(name = "orgId") String orgId, @WebParam(name = "relatedOrgId") String relatedOrgId, @WebParam(name = "orgOrgRelationTypeKey") String orgOrgRelationTypeKey, @WebParam(name = "orgOrgRelationInfo") OrgOrgRelationInfo orgOrgRelationInfo) throws AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, PermissionDeniedException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgOrgRelationInfo updateOrgOrgRelation(@WebParam(name = "orgOrgRelationId") String orgOrgRelationId, @WebParam(name = "orgOrgRelationInfo") OrgOrgRelationInfo orgOrgRelationInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeOrgOrgRelation(@WebParam(name = "orgOrgRelationId") String orgOrgRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgPersonRelationInfo createOrgPersonRelation(@WebParam(name = "orgId") String orgId, @WebParam(name = "personId") String personId, @WebParam(name = "orgPersonRelationTypeKey") String orgPersonRelationTypeKey, @WebParam(name = "orgPersonRelationInfo") OrgPersonRelationInfo orgPersonRelationInfo) throws AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, PermissionDeniedException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgPersonRelationInfo updateOrgPersonRelation(@WebParam(name = "orgPersonRelationId") String orgPersonRelationId, @WebParam(name = "orgPersonRelationInfo") OrgPersonRelationInfo orgPersonRelationInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeOrgPersonRelation(@WebParam(name = "orgPersonRelationId") String orgPersonRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgPositionRestrictionInfo addPositionRestrictionToOrg(@WebParam(name = "orgId") String orgId, @WebParam(name = "orgPersonRelationTypeKey") String orgPersonRelationTypeKey, @WebParam(name = "orgPositionRestrictionInfo") OrgPositionRestrictionInfo orgPositionRestrictionInfo) throws AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public OrgPositionRestrictionInfo updatePositionRestrictionForOrg(@WebParam(name = "orgId") String orgId, @WebParam(name = "orgPersonRelationTypeKey") String orgPersonRelationTypeKey, @WebParam(name = "orgPositionRestrictionInfo") OrgPositionRestrictionInfo orgPositionRestrictionInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removePositionRestrictionFromOrg(@WebParam(name = "orgId") String orgId, @WebParam(name = "orgPersonRelationTypeKey") String orgPersonRelationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<OrgTreeInfo> getOrgTree(@WebParam(name = "rootOrgId") String rootOrgId, @WebParam(name = "orgHierarchyId") String orgHierarchyId, @WebParam(name = "maxLevels") int maxLevels) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getObjectTypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ObjectStructure getObjectStructure(@WebParam(name = "objectTypeKey") String objectTypeKey) {
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
        return searchResult;
    }
}
