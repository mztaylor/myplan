package org.kuali.student.myplan.service.mock;

import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.atp.dto.AtpAtpRelationInfo;
import org.kuali.student.r2.core.atp.dto.AtpInfo;
import org.kuali.student.r2.core.atp.dto.MilestoneInfo;
import org.kuali.student.r2.core.atp.service.AtpService;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;

import javax.jws.WebParam;
import java.util.Date;
import java.util.List;

/**
 * Mock AtpService to be use with AcademicPlanServiceImpl.
 */
public class AtpServiceMockImpl implements AtpService {

    @Override
    public AtpInfo getAtp(@WebParam(name = "atpId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return new AtpInfo();
    }

    @Override
    public List<AtpInfo> getAtpsByIds(@WebParam(name = "atpIds") List<String> strings, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getAtpIdsByType(@WebParam(name = "atpTypeKey") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByCode(@WebParam(name = "code") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByDate(@WebParam(name = "date") Date date, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByDateAndType(@WebParam(name = "date") Date date, @WebParam(name = "atpTypeKey") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByDates(@WebParam(name = "startDate") Date date, @WebParam(name = "endDate") Date date1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByDatesAndType(@WebParam(name = "startDate") Date date, @WebParam(name = "endDate") Date date1, @WebParam(name = "atpTypeKey") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByStartDateRange(@WebParam(name = "dateRangeStart") Date date, @WebParam(name = "dateRangeEnd") Date date1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByStartDateRangeAndType(@WebParam(name = "dateRangeStart") Date date, @WebParam(name = "dateRangeEnd") Date date1, @WebParam(name = "atpTypeKey") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> searchForAtpIds(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> searchForAtps(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateAtp(@WebParam(name = "validationTypeKey") String s, @WebParam(name = "atpTypeKey") String s1, @WebParam(name = "atpInfo") AtpInfo atpInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpInfo createAtp(@WebParam(name = "atpTypeKey") String s, @WebParam(name = "atpInfo") AtpInfo atpInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpInfo updateAtp(@WebParam(name = "atpId") String s, @WebParam(name = "atpInfo") AtpInfo atpInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteAtp(@WebParam(name = "atpId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AtpAtpRelationInfo getAtpAtpRelation(@WebParam(name = "atpAtpRelationId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpAtpRelationInfo> getAtpAtpRelationsByIds(@WebParam(name = "atpAtpRelationIds") List<String> strings, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getAtpAtpRelationIdsByType(@WebParam(name = "atpAtpRelationTypeKey") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpAtpRelationInfo> getAtpAtpRelationsByAtp(@WebParam(name = "atpId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpAtpRelationInfo> getAtpAtpRelationsByAtps(@WebParam(name = "atpId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpAtpRelationInfo> getAtpAtpRelationsByTypeAndAtp(@WebParam(name = "atpId") String s, @WebParam(name = "atpAtpRelationTypeKey") String s1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> searchForAtpAtpRelationIds(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpAtpRelationInfo> searchForAtpAtpRelations(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateAtpAtpRelation(@WebParam(name = "validationTypeKey") String s, @WebParam(name = "atpId") String s1, @WebParam(name = "atpPeerId") String s2, @WebParam(name = "atpAtprelationTypeKey") String s3, @WebParam(name = "atpAtpRelationInfo") AtpAtpRelationInfo atpAtpRelationInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpAtpRelationInfo createAtpAtpRelation(@WebParam(name = "atpId") String s, @WebParam(name = "relatedAtpId") String s1, @WebParam(name = "atpAtpRelationTypeKey") String s2, @WebParam(name = "atpAtpRelationInfo") AtpAtpRelationInfo atpAtpRelationInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpAtpRelationInfo updateAtpAtpRelation(@WebParam(name = "atpAtpRelationId") String s, @WebParam(name = "atpAtpRelationInfo") AtpAtpRelationInfo atpAtpRelationInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteAtpAtpRelation(@WebParam(name = "atpAtpRelationId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public MilestoneInfo getMilestone(@WebParam(name = "milestoneId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getMilestonesByIds(@WebParam(name = "milestoneIds") List<String> strings, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getMilestoneIdsByType(@WebParam(name = "milestoneTypeKey") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getMilestonesByDates(@WebParam(name = "startDate") Date date, @WebParam(name = "endDate") Date date1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getMilestonesForAtp(@WebParam(name = "atpId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getMilestonesByDatesForAtp(@WebParam(name = "atpId") String s, @WebParam(name = "startDate") Date date, @WebParam(name = "endDate") Date date1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getMilestonesByTypeForAtp(@WebParam(name = "atpId") String s, @WebParam(name = "milestoneTypeKey") String s1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getImpactedMilestones(@WebParam(name = "milestoneId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> searchForMilestoneIds(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> searchForMilestones(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateMilestone(@WebParam(name = "validationTypeKey") String s, @WebParam(name = "milestoneInfo") MilestoneInfo milestoneInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public MilestoneInfo createMilestone(@WebParam(name = "milestoneTypeKey") String s, @WebParam(name = "milestoneInfo") MilestoneInfo milestoneInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public MilestoneInfo updateMilestone(@WebParam(name = "milestoneId") String s, @WebParam(name = "milestoneInfo") MilestoneInfo milestoneInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteMilestone(@WebParam(name = "milestoneId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public MilestoneInfo calculateMilestone(@WebParam(name = "milestoneId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo addMilestoneToAtp(@WebParam(name = "milestoneId") String s, @WebParam(name = "atpId") String s1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo removeMilestoneFromAtp(@WebParam(name = "milestoneId") String s, @WebParam(name = "atpId") String s1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<TypeInfo> getSearchTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public TypeInfo getSearchType(@WebParam(name = "searchTypeKey") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SearchResultInfo search(SearchRequestInfo searchRequestInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws MissingParameterException, InvalidParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }
}
