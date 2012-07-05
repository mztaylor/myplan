package org.kuali.student.myplan.comment.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.core.comment.service.CommentService;
import org.kuali.student.myplan.comment.dataobject.MessageDataObject;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.util.constants.CommentServiceConstants;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;

public class MessagesLookupableHelperImpl extends MyPlanLookupableImpl {
    private final Logger logger = Logger.getLogger(MessagesLookupableHelperImpl.class);
    private transient CommentService commentService;

    @Override
    protected List<MessageDataObject> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        String studentId = UserSessionHelper.getStudentId();

        List<MessageDataObject> messages = new ArrayList<MessageDataObject>();
        try {
           // List<PlannedCourseDataObject> plannedCoursesList = getPlanItems(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, false, studentId);
            Collections.sort(messages);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        MessageDataObject m1 = new MessageDataObject();
        m1.setFrom("Glenn");
        m1.setSubject("Hello World");
        m1.setBody("This is a test body.");
        m1.setTimestamp(new java.util.Date());

        MessageDataObject m2 = new MessageDataObject();
        m2.setFrom("Hemanth");
        m2.setSubject("Hello from Hemanth");
        m2.setBody("That is a lot of bookkeeping.");
        m2.setTimestamp(new java.util.Date());

        MessageDataObject m3 = new MessageDataObject();
        m3.setFrom("Jason");
        m3.setSubject("My $.02 ...");
        m3.setBody("Result: An error message with red text.  The red text appears to have a transparent background because page elements behind it show through (see attachement from Firefox)");
        m3.setTimestamp(new java.util.Date());

        messages.add(m1);
        messages.add(m2);
        messages.add(m3);

        return messages;
    }

    public CommentService getCommentService() {
        if (commentService == null) {
            commentService = (CommentService)
                    GlobalResourceLoader.getService(new QName(CommentServiceConstants.NAMESPACE, "CommentService"));
        }
        return commentService;
    }

    public void setCommentService(CommentService commentService) {
        this.commentService = commentService;
    }
}
