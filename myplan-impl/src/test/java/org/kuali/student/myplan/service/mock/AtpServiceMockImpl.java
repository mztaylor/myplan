package org.kuali.student.myplan.service.mock;

import org.kuali.student.common.dictionary.dto.ObjectStructureDefinition;
import org.kuali.student.common.dto.StatusInfo;
import org.kuali.student.common.exceptions.*;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.common.validation.dto.ValidationResultInfo;
import org.kuali.student.core.atp.dto.*;
import org.kuali.student.core.atp.service.AtpService;

import javax.jws.WebParam;
import java.util.Date;
import java.util.List;

/**
 * Mock AtpService to be use with AcademicPlanServiceImpl.
 */
public class AtpServiceMockImpl implements AtpService {
    @Override
    public List<AtpTypeInfo> getAtpTypes() throws OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpTypeInfo getAtpType(@WebParam(name = "atpTypeKey") String atpTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpSeasonalTypeInfo> getAtpSeasonalTypes() throws OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpSeasonalTypeInfo getAtpSeasonalType(@WebParam(name = "atpSeasonalTypeKey") String atpSeasonalTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpDurationTypeInfo> getAtpDurationTypes() throws OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpDurationTypeInfo getAtpDurationType(@WebParam(name = "atpDurationTypeKey") String atpDurationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneTypeInfo> getMilestoneTypes() throws OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public MilestoneTypeInfo getMilestoneType(@WebParam(name = "milestoneTypeKey") String milestoneTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneTypeInfo> getMilestoneTypesForAtpType(@WebParam(name = "atpTypeKey") String atpTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<DateRangeTypeInfo> getDateRangeTypes() throws OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public DateRangeTypeInfo getDateRangeType(@WebParam(name = "dateRangeTypeKey") String dateRangeTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<DateRangeTypeInfo> getDateRangeTypesForAtpType(@WebParam(name = "atpTypeKey") String atpTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateAtp(@WebParam(name = "validationType") String validationType, @WebParam(name = "atpInfo") AtpInfo atpInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateMilestone(@WebParam(name = "validationType") String validationType, @WebParam(name = "milestoneInfo") MilestoneInfo milestoneInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateDateRange(@WebParam(name = "validationType") String validationType, @WebParam(name = "dateRangeInfo") DateRangeInfo dateRangeInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpInfo getAtp(@WebParam(name = "atpKey") String atpKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        AtpInfo atp = new AtpInfo();
        atp.setId(atpKey);
        return atp;
    }

    @Override
    public List<AtpInfo> getAtpsByDate(@WebParam(name = "searchDate") Date searchDate) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByDates(@WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<AtpInfo> getAtpsByAtpType(@WebParam(name = "atpTypeKey") String atpTypeKey) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public MilestoneInfo getMilestone(@WebParam(name = "milestoneKey") String milestoneKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getMilestonesByAtp(@WebParam(name = "atpKey") String atpKey) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getMilestonesByDates(@WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<MilestoneInfo> getMilestonesByDatesAndType(@WebParam(name = "milestoneTypeKey") String milestoneTypeKey, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public DateRangeInfo getDateRange(@WebParam(name = "dateRangeKey") String dateRangeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<DateRangeInfo> getDateRangesByAtp(@WebParam(name = "atpKey") String atpKey) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<DateRangeInfo> getDateRangesByDate(@WebParam(name = "searchDate") Date searchDate) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpInfo createAtp(@WebParam(name = "atpTypeKey") String atpTypeKey, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "atpInfo") AtpInfo atpInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public AtpInfo updateAtp(@WebParam(name = "atpKey") String atpKey, @WebParam(name = "atpInfo") AtpInfo atpInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteAtp(@WebParam(name = "atpKey") String atpKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public MilestoneInfo addMilestone(@WebParam(name = "atpKey") String atpKey, @WebParam(name = "milestoneKey") String milestoneKey, @WebParam(name = "milestoneInfo") MilestoneInfo milestoneInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public MilestoneInfo updateMilestone(@WebParam(name = "milestoneKey") String milestoneKey, @WebParam(name = "milestoneInfo") MilestoneInfo milestoneInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo removeMilestone(@WebParam(name = "milestoneKey") String milestoneKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public DateRangeInfo addDateRange(@WebParam(name = "atpKey") String atpKey, @WebParam(name = "dateRangeKey") String dateRangeKey, @WebParam(name = "dateRangeInfo") DateRangeInfo dateRangeInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public DateRangeInfo updateDateRange(@WebParam(name = "dateRangeKey") String dateRangeKey, @WebParam(name = "dateRangeInfo") DateRangeInfo dateRangeInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo removeDateRange(@WebParam(name = "dateRangeKey") String dateRangeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getObjectTypes() {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ObjectStructureDefinition getObjectStructure(@WebParam(name = "objectTypeKey") String objectTypeKey) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<SearchTypeInfo> getSearchTypes() throws OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SearchTypeInfo getSearchType(@WebParam(name = "searchTypeKey") String searchTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<SearchTypeInfo> getSearchTypesByResult(@WebParam(name = "searchResultTypeKey") String searchResultTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<SearchTypeInfo> getSearchTypesByCriteria(@WebParam(name = "searchCriteriaTypeKey") String searchCriteriaTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<SearchResultTypeInfo> getSearchResultTypes() throws OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SearchResultTypeInfo getSearchResultType(@WebParam(name = "searchResultTypeKey") String searchResultTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<SearchCriteriaTypeInfo> getSearchCriteriaTypes() throws OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SearchCriteriaTypeInfo getSearchCriteriaType(@WebParam(name = "searchCriteriaTypeKey") String searchCriteriaTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SearchResult search(SearchRequest searchRequest) throws MissingParameterException {
        throw new RuntimeException("Not implemented");
    }
}
