package org.kuali.student.myplan.comment.util;

import org.kuali.student.common.exceptions.*;
import org.kuali.student.core.comment.dto.CommentInfo;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/25/13
 * Time: 8:03 AM
 * To change this template use File | Settings | File Templates.
 */
public interface CommentHelper {


    public CommentInfo createMessage(String subjectText, String messageText) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    public CommentInfo createComment(String referenceId, String commentText) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    public void sendMessageEmailNotification(String subjectText, String messageText) throws MissingParameterException;

    public void sendCommentEmailNotification(CommentInfo commentInfo, String message) throws MissingParameterException;
}
