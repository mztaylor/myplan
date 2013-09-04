package org.kuali.student.myplan.service.mock;

import org.kuali.student.common.dictionary.dto.ObjectStructureDefinition;
import org.kuali.student.common.dto.ReferenceTypeInfo;
import org.kuali.student.common.dto.StatusInfo;
import org.kuali.student.common.exceptions.*;
import org.kuali.student.common.validation.dto.ValidationResultInfo;
import org.kuali.student.core.comment.dto.CommentInfo;
import org.kuali.student.core.comment.dto.CommentTypeInfo;
import org.kuali.student.core.comment.dto.TagInfo;
import org.kuali.student.core.comment.dto.TagTypeInfo;
import org.kuali.student.core.comment.service.CommentService;

import javax.jws.WebParam;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/26/13
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class CommentServiceMockImpl implements CommentService {
    @Override
    public List<ReferenceTypeInfo> getReferenceTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CommentTypeInfo> getCommentTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TagTypeInfo> getTagTypes() throws OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CommentTypeInfo> getCommentTypesForReferenceType(@WebParam(name = "referenceTypeKey") String referenceTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateComment(@WebParam(name = "validationType") String validationType, @WebParam(name = "commentInfo") CommentInfo commentInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CommentInfo> getComments(@WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TagInfo> getTags(@WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CommentInfo> getCommentsByType(@WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey, @WebParam(name = "commentTypeKey") String commentTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TagInfo> getTagsByType(@WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey, @WebParam(name = "tagTypeKey") String tagTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CommentInfo getComment(@WebParam(name = "commentId") String commentId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TagInfo getTag(@WebParam(name = "tagId") String tagId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CommentInfo addComment(@WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey, @WebParam(name = "commentInfo") CommentInfo commentInfo) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TagInfo addTag(@WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey, @WebParam(name = "tagInfo") TagInfo tagInfo) throws DataValidationErrorException, AlreadyExistsException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CommentInfo updateComment(@WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey, @WebParam(name = "commentInfo") CommentInfo commentInfo) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DoesNotExistException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeComment(@WebParam(name = "commentId") String commentId, @WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeTag(@WebParam(name = "tagId") String tagId, @WebParam(name = "referenceId") String referenceId, @WebParam(name = "referenceTypeKey") String referenceTypeKey) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeComments(@WebParam(name = "referenceId") String referenceId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo removeTags(@WebParam(name = "tagId") String tagId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
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
}
