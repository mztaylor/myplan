package org.kuali.student.myplan.service.mock;

import org.kuali.student.common.util.UUIDHelper;
import org.kuali.student.r1.common.dictionary.dto.ObjectStructureDefinition;
import org.kuali.student.r1.common.dto.StatusInfo;
import org.kuali.student.r1.core.statement.dto.*;
import org.kuali.student.r1.core.statement.service.StatementService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;

import javax.jws.WebParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 1/6/14
 * Time: 11:25 AM
 * To change this template use File | Settings | File Templates.
 */
public class StatementServiceMockImpl implements StatementService {

    Map<String, StatementTreeViewInfo> statementTreeViewInfoMap = new HashMap<String, StatementTreeViewInfo>();
    Map<String, ReqComponentInfo> reqComponentInfoHashMap = new HashMap<String, ReqComponentInfo>();


    @Override
    public List<String> getRefObjectTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getRefObjectSubTypes(@WebParam(name = "objectTypeKey") String objectTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<NlUsageTypeInfo> getNlUsageTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public NlUsageTypeInfo getNlUsageType(@WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RefStatementRelationInfo createRefStatementRelation(@WebParam(name = "refStatementRelationInfo") RefStatementRelationInfo refStatementRelationInfo) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RefStatementRelationInfo updateRefStatementRelation(@WebParam(name = "refStatementRelationId") String refStatementRelationId, @WebParam(name = "refStatementRelationInfo") RefStatementRelationInfo refStatementRelationInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteRefStatementRelation(@WebParam(name = "refStatementRelationId") String refStatementRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateRefStatementRelation(@WebParam(name = "validationType") String validationType, @WebParam(name = "refStatementRelationInfo") RefStatementRelationInfo refStatementRelationInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RefStatementRelationInfo getRefStatementRelation(@WebParam(name = "refStatementRelationId") String refStatementRelationId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<RefStatementRelationInfo> getRefStatementRelationsByRef(@WebParam(name = "refObjectTypeKey") String refObjectTypeKey, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<RefStatementRelationInfo> getRefStatementRelationsByStatement(@WebParam(name = "statementId") String statementId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getNaturalLanguageForStatement(@WebParam(name = "statementId") String statementId, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getNaturalLanguageForRefStatementRelation(@WebParam(name = "refStatementRelationId") String refStatementRelationId, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getNaturalLanguageForReqComponent(@WebParam(name = "reqComponentId") String reqComponentId, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String translateStatementTreeViewToNL(@WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String translateReqComponentToNL(@WebParam(name = "reqComponentInfo") ReqComponentInfo reqComponentInfo, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateReqComponent(@WebParam(name = "validationType") String validationType, @WebParam(name = "reqComponentInfo") ReqComponentInfo reqComponentInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateStatement(@WebParam(name = "validationType") String validationType, @WebParam(name = "statementInfo") StatementInfo statementInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementInfo getStatement(@WebParam(name = "statementId") String statementId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<StatementInfo> getStatementsByType(@WebParam(name = "statementTypeKey") String statementTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ReqComponentInfo getReqComponent(@WebParam(name = "reqComponentId") String reqComponentId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return reqComponentInfoHashMap.get(reqComponentId);
    }

    @Override
    public List<ReqComponentInfo> getReqComponentsByType(@WebParam(name = "reqComponentTypeKey") String reqComponentTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<StatementInfo> getStatementsUsingReqComponent(@WebParam(name = "reqComponentId") String reqComponentId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<StatementInfo> getStatementsUsingStatement(@WebParam(name = "statementId") String statementId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ReqComponentInfo createReqComponent(@WebParam(name = "reqComponentType") String reqComponentType, @WebParam(name = "reqComponentInfo") ReqComponentInfo reqComponentInfo) throws AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteReqComponent(@WebParam(name = "reqComponentId") String reqComponentId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementInfo createStatement(@WebParam(name = "statementType") String statementType, @WebParam(name = "statementInfo") StatementInfo statementInfo) throws AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementInfo updateStatement(@WebParam(name = "statementId") String statementId, @WebParam(name = "statementInfo") StatementInfo statementInfo) throws CircularReferenceException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteStatement(@WebParam(name = "statementId") String statementId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementTypeInfo getStatementType(@WebParam(name = "statementTypeKey") String statementTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<StatementTypeInfo> getStatementTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getStatementTypesForStatementType(@WebParam(name = "statementTypeKey") String statementTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ReqComponentTypeInfo> getReqComponentTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ReqComponentTypeInfo getReqComponentType(@WebParam(name = "reqComponentTypeKey") String reqComponentTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ReqComponentTypeInfo> getReqComponentTypesForStatementType(@WebParam(name = "statementTypeKey") String statementTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<RefStatementRelationTypeInfo> getRefStatementRelationTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RefStatementRelationTypeInfo getRefStatementRelationType(@WebParam(name = "refStatementRelationTypeKey") String refStatementRelationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getStatementTypesForRefStatementRelationType(@WebParam(name = "refStatementRelationTypeKey") String refStatementRelationTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getRefStatementRelationTypesForRefObjectSubType(@WebParam(name = "refSubTypeKey") String refSubTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ReqComponentInfo updateReqComponent(@WebParam(name = "reqComponentId") String reqComponentId, @WebParam(name = "reqComponentInfo") ReqComponentInfo reqComponentInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementTreeViewInfo getStatementTreeView(@WebParam(name = "statementId") String statementId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return statementTreeViewInfoMap.get(statementId);
    }

    @Override
    public StatementTreeViewInfo getStatementTreeViewForNlUsageType(String statementId, String nlUsageTypeKey, String language) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementTreeViewInfo updateStatementTreeView(@WebParam(name = "statementId") String statementId, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo) throws CircularReferenceException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        statementTreeViewInfoMap.put(statementId, statementTreeViewInfo);
        return statementTreeViewInfoMap.get(statementId);
    }

    @Override
    public StatementTreeViewInfo createStatementTreeView(@WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo) throws CircularReferenceException, AlreadyExistsException, DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<ReqComponentInfo> reqComponentInfos = statementTreeViewInfo.getReqComponents();
        for (ReqComponentInfo reqComponentInfo : reqComponentInfos) {
            reqComponentInfo.setId(UUIDHelper.genStringUUID());
            reqComponentInfoHashMap.put(reqComponentInfo.getId(), reqComponentInfo);
        }
        statementTreeViewInfo.setId(UUIDHelper.genStringUUID());
        statementTreeViewInfoMap.put(statementTreeViewInfo.getId(), statementTreeViewInfo);
        return statementTreeViewInfoMap.get(statementTreeViewInfo.getId());
    }

    @Override
    public StatusInfo deleteStatementTreeView(@WebParam(name = "statementId") String statementId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        statementTreeViewInfoMap.remove(statementId);
        return new StatusInfo();
    }

    @Override
    public List<String> getObjectTypes() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ObjectStructureDefinition getObjectStructure(@WebParam(name = "objectTypeKey") String s) {
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
}
