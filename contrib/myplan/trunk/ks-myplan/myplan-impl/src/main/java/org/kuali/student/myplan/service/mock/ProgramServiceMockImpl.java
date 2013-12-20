package org.kuali.student.myplan.service.mock;

import org.kuali.student.r1.common.dictionary.dto.ObjectStructureDefinition;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.versionmanagement.dto.VersionDisplayInfo;
import org.kuali.student.r2.lum.program.dto.*;
import org.kuali.student.r2.lum.program.service.ProgramService;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/15/13
 * Time: 11:24 AM
 * To change this template use File | Settings | File Templates.
 */
public class ProgramServiceMockImpl implements ProgramService {
    @Override
    public CredentialProgramInfo getCredentialProgram(@WebParam(name = "credentialProgramId") String credentialProgramId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CredentialProgramInfo> getCredentialProgramsByIds(@WebParam(name = "credentialProgramIds") List<String> credentialProgramIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCredentialProgram(@WebParam(name = "validationType") String validationType, @WebParam(name = "credentialProgramInfo") CredentialProgramInfo credentialProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CredentialProgramInfo createCredentialProgram(@WebParam(name = "credentialProgramTypeKey") String credentialProgramTypeKey, @WebParam(name = "credentialProgramInfo") CredentialProgramInfo credentialProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CredentialProgramInfo createNewCredentialProgramVersion(@WebParam(name = "credentialProgramId") String credentialProgramId, @WebParam(name = "versionComment") String versionComment, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException, DataValidationErrorException, ReadOnlyException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo setCurrentCredentialProgramVersion(@WebParam(name = "credentialProgramId") String credentialProgramId, @WebParam(name = "currentVersionStart") Date currentVersionStart, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, IllegalVersionSequencingException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CredentialProgramInfo updateCredentialProgram(@WebParam(name = "credentialProgramId") String credentialProgramId, @WebParam(name = "credentialProgramInfo") CredentialProgramInfo credentialProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, VersionMismatchException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCredentialProgram(@WebParam(name = "credentialProgramId") String credentialProgramId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MajorDisciplineInfo getMajorDiscipline(@WebParam(name = "majorDisciplineId") String majorDisciplineId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        MajorDisciplineInfo majorDisciplineInfo = new MajorDisciplineInfo();
        if ("00-1-6BSCE".equals(majorDisciplineId)) {
            majorDisciplineInfo.setCode("00-1-6 BS in Civil Engineering");
            majorDisciplineInfo.setId("00-1-6BSCE");
        } else if ("00-1-7MSCE".equals(majorDisciplineId)) {
            majorDisciplineInfo.setCode("00-1-7 MS in Civil Engineering");
            majorDisciplineInfo.setId("00-1-7MSCE");
        }
        return majorDisciplineInfo;
    }

    @Override
    public List<MajorDisciplineInfo> getMajorDisciplinesByIds(@WebParam(name = "majorDisciplineIds") List<String> majorDisciplineIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<MajorDisciplineInfo> majorDisciplineInfos = new ArrayList<MajorDisciplineInfo>();
        MajorDisciplineInfo majorDisciplineInfo = new MajorDisciplineInfo();
        majorDisciplineInfo.setCode("00-1-6 BS in Civil Engineering");
        majorDisciplineInfo.setId("00-1-6BSCE");
        majorDisciplineInfos.add(majorDisciplineInfo);
        MajorDisciplineInfo majorDisciplineInfo2 = new MajorDisciplineInfo();
        majorDisciplineInfo2.setCode("00-1-7 MS in Civil Engineering");
        majorDisciplineInfo2.setId("00-1-7MSCE");
        majorDisciplineInfos.add(majorDisciplineInfo2);
        return majorDisciplineInfos;
    }

    @Override
    public List<String> getMajorDisciplineIdsByCredentialProgramType(@WebParam(name = "programType") String programType, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ProgramVariationInfo> getProgramVariationsByMajorDiscipline(@WebParam(name = "majorDisciplineId") String majorDisciplineId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateMajorDiscipline(@WebParam(name = "validationType") String validationType, @WebParam(name = "majorDisciplineInfo") MajorDisciplineInfo majorDisciplineInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MajorDisciplineInfo createMajorDiscipline(@WebParam(name = "majorDisciplineTypeKey") String majorDisciplineTypeKey, @WebParam(name = "majorDisciplineInfo") MajorDisciplineInfo majorDisciplineInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MajorDisciplineInfo updateMajorDiscipline(@WebParam(name = "majorDisciplineId") String majorDisciplineId, @WebParam(name = "majorDisciplineInfo") MajorDisciplineInfo majorDisciplineInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, VersionMismatchException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteMajorDiscipline(@WebParam(name = "majorDisciplineId") String majorDisciplineId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MajorDisciplineInfo createNewMajorDisciplineVersion(@WebParam(name = "majorDisciplineId") String majorDisciplineId, @WebParam(name = "versionComment") String versionComment, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException, DataValidationErrorException, ReadOnlyException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HonorsProgramInfo getHonorsProgram(@WebParam(name = "honorsProgramId") String honorsProgramId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<HonorsProgramInfo> getHonorsProgramsByIds(@WebParam(name = "honorsProgramIds") List<String> honorsProgramIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getHonorProgramIdsByCredentialProgramType(@WebParam(name = "programType") String programType, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateHonorsProgram(@WebParam(name = "validationType") String validationType, @WebParam(name = "honorsProgramInfo") HonorsProgramInfo honorsProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HonorsProgramInfo createHonorsProgram(@WebParam(name = "honorsProgramTypeKey") String honorsProgramTypeKey, @WebParam(name = "honorsProgramInfo") HonorsProgramInfo honorsProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public HonorsProgramInfo updateHonorsProgram(@WebParam(name = "honorsProgramId") String honorsProgramId, @WebParam(name = "honorsProgramTypeKey") String honorsProgramTypeKey, @WebParam(name = "honorsProgramInfo") HonorsProgramInfo honorsProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, VersionMismatchException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteHonorsProgram(@WebParam(name = "honorsProgramId") String honorsProgramId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CoreProgramInfo getCoreProgram(@WebParam(name = "coreProgramId") String coreProgramId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CoreProgramInfo> getCoreProgramsByIds(@WebParam(name = "coreProgramIds") List<String> coreProgramIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCoreProgram(@WebParam(name = "validationType") String validationType, @WebParam(name = "coreProgramInfo") CoreProgramInfo coreProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CoreProgramInfo createCoreProgram(@WebParam(name = "coreProgramTypeKey") String coreProgramTypeKey, @WebParam(name = "coreProgramInfo") CoreProgramInfo coreProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DoesNotExistException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CoreProgramInfo createNewCoreProgramVersion(@WebParam(name = "coreProgramId") String coreProgramId, @WebParam(name = "versionComment") String versionComment, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException, DataValidationErrorException, ReadOnlyException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo setCurrentCoreProgramVersion(@WebParam(name = "coreProgramId") String coreProgramId, @WebParam(name = "currentVersionStart") Date currentVersionStart, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, IllegalVersionSequencingException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CoreProgramInfo updateCoreProgram(@WebParam(name = "coreProgramId") String coreProgramId, @WebParam(name = "coreProgramTypeKey") String coreProgramTypeKey, @WebParam(name = "coreProgramInfo") CoreProgramInfo coreProgramInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, VersionMismatchException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCoreProgram(@WebParam(name = "coreProgramId") String coreProgramId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProgramRequirementInfo getProgramRequirement(@WebParam(name = "programRequirementId") String programRequirementId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ProgramRequirementInfo> getProgramRequirementsByIds(@WebParam(name = "programRequirementIds") List<String> programRequirementIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateProgramRequirement(@WebParam(name = "validationType") String validationType, @WebParam(name = "programRequirementInfo") ProgramRequirementInfo programRequirementInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProgramRequirementInfo createProgramRequirement(@WebParam(name = "programRequirementTypeKey") String programRequirementTypeKey, @WebParam(name = "programRequirementInfo") ProgramRequirementInfo programRequirementInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ProgramRequirementInfo updateProgramRequirement(@WebParam(name = "programRequirementId") String programRequirementId, @WebParam(name = "programRequirementTypeKey") String programRequirementTypeKey, @WebParam(name = "programRequirementInfo") ProgramRequirementInfo programRequirementInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, VersionMismatchException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteProgramRequirement(@WebParam(name = "programRequirementId") String programRequirementId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo setCurrentMajorDisciplineVersion(@WebParam(name = "majorDisciplineId") String majorDisciplineId, @WebParam(name = "currentVersionStart") Date currentVersionStart, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, IllegalVersionSequencingException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MinorDisciplineInfo getMinorDiscipline(@WebParam(name = "minorDisciplineId") String minorDisciplineId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getMinorsByCredentialProgramType(@WebParam(name = "programType") String programType, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateMinorDiscipline(@WebParam(name = "validationType") String validationType, @WebParam(name = "minorDisciplineInfo") MinorDisciplineInfo minorDisciplineInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MinorDisciplineInfo createMinorDiscipline(@WebParam(name = "minorDisciplineTypeKey") String minorDisciplineTypeKey, @WebParam(name = "minorDisciplineInfo") MinorDisciplineInfo minorDisciplineInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public MinorDisciplineInfo updateMinorDiscipline(@WebParam(name = "minorDisciplineId") String minorDisciplineId, @WebParam(name = "minorDisciplineTypeKey") String minorDisciplineTypeKey, @WebParam(name = "minorDisciplineInfo") MinorDisciplineInfo minorDisciplineInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, VersionMismatchException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteMinorDiscipline(@WebParam(name = "minorDisciplineId") String minorDisciplineId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ProgramVariationInfo> getVariationsByMajorDisciplineId(@WebParam(name = "majorDisciplineId") String majorDisciplineId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
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
    public List<TypeInfo> getSearchTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypeInfo getSearchType(@WebParam(name = "searchTypeKey") String searchTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchResultInfo search(SearchRequestInfo searchRequestInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws MissingParameterException, InvalidParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<VersionDisplayInfo> getVersions(@WebParam(name = "refObjectUri") String refObjectUri, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getFirstVersion(@WebParam(name = "refObjectUri") String refObjectUri, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getLatestVersion(@WebParam(name = "refObjectUri") String refObjectUri, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getCurrentVersion(@WebParam(name = "refObjectUri") String refObjectUri, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getVersionBySequenceNumber(@WebParam(name = "refObjectUri") String refObjectUri, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "sequence") Long sequence, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public VersionDisplayInfo getCurrentVersionOnDate(@WebParam(name = "refObjectUri") String refObjectUri, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "date") Date date, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<VersionDisplayInfo> getVersionsInDateRange(@WebParam(name = "refObjectUri") String refObjectUri, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "from") Date from, @WebParam(name = "to") Date to, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
