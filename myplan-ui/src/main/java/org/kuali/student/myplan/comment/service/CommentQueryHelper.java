package org.kuali.student.myplan.comment.service;

import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.myplan.comment.CommentConstants;
import org.kuali.student.myplan.comment.dataobject.CommentDataObject;
import org.kuali.student.myplan.comment.dataobject.MessageDataObject;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.core.comment.dto.CommentInfo;
import org.kuali.student.r2.core.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class CommentQueryHelper {

    private final Logger logger = Logger.getLogger(CommentQueryHelper.class);

    private transient CommentService commentService;

    @Autowired
    private UserSessionHelper userSessionHelper;

    private CommentService getCommentService() {
        if (commentService == null) {
            commentService = (CommentService)
                GlobalResourceLoader.getService(new QName(CommentConstants.NAMESPACE, CommentConstants.SERVICE_NAME));
        }
        return commentService;
    }

    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }

    /**
     * Retrieve a single message.
     *
     * @param messageId
     * @return
     */
    public synchronized MessageDataObject getMessage(String messageId) {
        CommentInfo commentInfo;
        try {
            commentInfo = getCommentService().getComment(messageId, CommentConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error(String.format("Query for comment [%s] failed.", messageId), e);
            return null;
        }
        return makeMessageDataObject(commentInfo);
    }

    private MessageDataObject makeMessageDataObject(CommentInfo commentInfo) {
        MessageDataObject messageDataObject = new MessageDataObject();
        messageDataObject.setCreateDate(commentInfo.getMeta().getCreateTime());
        List<AttributeInfo> abbrAttributes = commentInfo.getAttributes();
        String subject = null;
        String createdBy = null;
        for (AttributeInfo entry : abbrAttributes) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (CommentConstants.SUBJECT_ATTRIBUTE_NAME.equals(key)) {
                subject = value;
            } else if (CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME.equals(key)) {
                createdBy = value;
            }
            if (subject != null && createdBy != null) {
                break;
            }
        }
        messageDataObject.setSubject(subject);
        messageDataObject.setBody(commentInfo.getCommentText().getPlain());
        messageDataObject.setFrom(getUserSessionHelper().getName(createdBy));
        messageDataObject.setMessageId(commentInfo.getId());

        //  Pass the id of the message to get the comments associated with this message.
        List<CommentDataObject> comments = getComments(commentInfo.getId());

        //  Determine the last update date for the message. If comments exist then use the most recent comment create date.
        //  Otherwise, use the message create date.
        Date lastCommentDate = null;
        String lastCommentBy = null;
        for (CommentDataObject comment : comments) {
            Date d = comment.getCreateDate();
            if (lastCommentDate == null || d.after(lastCommentDate)) {
                lastCommentDate = d;
                lastCommentBy = comment.getFrom();
            }
        }
        messageDataObject.setLastCommentBy(lastCommentBy);
        messageDataObject.setLastCommentDate(lastCommentDate);
        messageDataObject.setComments(comments);
        return messageDataObject;
    }

    /**
     * Get all messages for a particular student.
     *
     * @param studentId
     * @return
     */
    public synchronized List<MessageDataObject> getMessages(String studentId) {
        List<MessageDataObject> messages = new ArrayList<MessageDataObject>();
        List<CommentInfo> commentInfos = new ArrayList<CommentInfo>();
        try {
            commentInfos = getCommentService().getCommentsByReferenceAndType(studentId, CommentConstants.MESSAGE_REF_TYPE, CommentConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error(String.format("Failed to retrieve messages for student [%s].", studentId), e);
        }

        for (CommentInfo ci : commentInfos) {
            MessageDataObject messageDataObject = makeMessageDataObject(ci);
            messages.add(messageDataObject);
        }

        Collections.sort(messages);
        return messages;
    }

    private List<CommentDataObject> getComments(String messageId) {
        List<CommentDataObject> comments = new ArrayList<CommentDataObject>();
        List<CommentInfo> commentInfos = new ArrayList<CommentInfo>();
        try {
            commentInfos = getCommentService().getCommentsByReferenceAndType(messageId, CommentConstants.COMMENT_REF_TYPE, CommentConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error(String.format("Failed to retrieve messages for messages id [%s].", messageId), e);
        }

        for (CommentInfo ci : commentInfos) {
            CommentDataObject commentDataObject = new CommentDataObject();
            commentDataObject.setCreateDate(ci.getMeta().getCreateTime());
            commentDataObject.setBody(ci.getCommentText().getPlain());
            List<AttributeInfo> abbrAttributes = ci.getAttributes();
            String createdBy = null;
            for (AttributeInfo entry : abbrAttributes) {
                String key = entry.getKey();
                String value = entry.getValue();
                if (CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME.equals(key)) {
                    createdBy = value;
                    break;
                }
            }
            commentDataObject.setFrom(getUserSessionHelper().getName(createdBy));
            comments.add(commentDataObject);
        }

        Collections.sort(comments);
        return comments;
    }

    public UserSessionHelper getUserSessionHelper() {
        if(userSessionHelper == null){
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}