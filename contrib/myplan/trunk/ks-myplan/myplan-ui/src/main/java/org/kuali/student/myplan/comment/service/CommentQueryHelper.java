package org.kuali.student.myplan.comment.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.comment.dto.CommentInfo;
import org.kuali.student.core.comment.service.CommentService;
import org.kuali.student.myplan.comment.CommentConstants;
import org.kuali.student.myplan.comment.dataobject.CommentDataObject;
import org.kuali.student.myplan.comment.dataobject.MessageDataObject;
import org.apache.log4j.Logger;
import org.kuali.student.myplan.utils.UserSessionHelper;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

public class CommentQueryHelper {

    private final Logger logger = Logger.getLogger(CommentQueryHelper.class);

    private transient CommentService commentService;

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
     * @param messageId
     * @return
     */
    public synchronized MessageDataObject getMessage(String messageId) {
        CommentInfo commentInfo;
        try {
            commentInfo = getCommentService().getComment(messageId);
        } catch (Exception e) {
            logger.error(String.format("Query for comment [%s] failed.", messageId), e);
            return null;
        }
        return makeMessageDataObject(commentInfo);
    }

    private MessageDataObject makeMessageDataObject(CommentInfo commentInfo) {
        MessageDataObject messageDataObject = new MessageDataObject();
        messageDataObject.setCreateDate(commentInfo.getMetaInfo().getCreateTime());
        messageDataObject.setSubject(commentInfo.getAttributes().get(CommentConstants.SUBJECT_ATTRIBUTE_NAME));
        messageDataObject.setBody(commentInfo.getCommentText().getPlain());
        messageDataObject.setFrom(UserSessionHelper.getName(commentInfo.getMetaInfo().getCreateId()));

        //  Pass the id of the message to get the comments associated with this message.
        messageDataObject.setComments(getComments(commentInfo.getId()));
        return messageDataObject;
    }

    /**
     * Get all messages for a particular student.
     * @param studentId
     * @return
     */
    public synchronized List<MessageDataObject> getMessages(String studentId) {
        List<MessageDataObject> messages = new ArrayList<MessageDataObject>();

        List<CommentInfo> commentInfos = new ArrayList<CommentInfo>();
        try {
            commentInfos = getCommentService().getComments(studentId, CommentConstants.MESSAGE_REF_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (CommentInfo ci : commentInfos) {
            MessageDataObject messageDataObject = makeMessageDataObject(ci);
            messages.add(messageDataObject);
        }
        return messages;
    }

    private List<CommentDataObject> getComments(String messageId) {
        List<CommentDataObject> comments = new ArrayList<CommentDataObject>();
        List<CommentInfo> commentInfos = new ArrayList<CommentInfo>();
        try {
            commentInfos = getCommentService().getComments(messageId, CommentConstants.COMMENT_REF_TYPE);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (CommentInfo ci : commentInfos) {
            CommentDataObject commentDataObject = new CommentDataObject();
            commentDataObject.setCreateDate(ci.getMetaInfo().getCreateTime());
            commentDataObject.setBody(ci.getCommentText().getPlain());
            commentDataObject.setFrom(UserSessionHelper.getName(ci.getMetaInfo().getCreateId()));
        }
        return comments;
    }
}
