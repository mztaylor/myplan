package org.kuali.student.myplan.comment;

import org.kuali.student.r2.common.util.constants.CommentServiceConstants;

public class CommentConstants extends CommentServiceConstants {
    public static final String SERVICE_NAME = "CommentService";

    public static final String MESSAGE_TYPE = "kuali.uw.comment.type.academicPlanning.message";
    public static final String COMMENT_TYPE = "kuali.uw.comment.type.academicPlanning.comment";
    public static final String MESSAGE_REF_TYPE = "kuali.uw.comment.reference.type.person";
    public static final String COMMENT_REF_TYPE = "kuali.uw.comment.reference.type.academicPlanning.message";

    public static final String SUBJECT_ATTRIBUTE_NAME = "subject";
    public static final String CREATED_BY_USER_ATTRIBUTE_NAME = "createdBy";

    public static final String SUCCESS_KEY_MESSAGE_ADDED = "myplan.text.success.comment.messageAdded";
    public static final String SUCCESS_KEY_COMMENT_ADDED = "myplan.text.success.comment.commentAdded";

    public static final String ERROR_KEY_NOTIFICATION_FAILED="myplan.text.error.comment.notificationFailed";

    public static final String MESSAGE_RESPONSE_PAGE="message_dialog_response_page";
    public static final String COMMENT_RESPONSE_PAGE="comment_dialog_response_page";
    public static final String ADVISER_ACCESS_ERROR="Only Adviser's can access!!";

    public static final String EMPTY_MESSAGE="Subject or Message Missing";
    public static final String EMPTY_COMMENT="Comment is Required";
    public static final String EMPTY_TO_ADDRESS="Cannot Process the Mail.";
    public static final String SPECIAL_CHARACTERS_ERROR="Special Characters are not allowed";



}
