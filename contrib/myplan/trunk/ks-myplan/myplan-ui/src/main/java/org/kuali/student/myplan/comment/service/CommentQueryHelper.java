package org.kuali.student.myplan.comment.service;


import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.comment.service.CommentService;
import org.kuali.student.myplan.comment.CommentConstants;

import javax.xml.namespace.QName;

public class CommentQueryHelper {
    private transient CommentService commentService;

    public CommentService getCommentService() {
        if (commentService == null) {
            commentService = (CommentService)
                    GlobalResourceLoader.getService(new QName(CommentConstants.NAMESPACE, CommentConstants.SERVICE_NAME));
        }
        return commentService;
    }

    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }
}
