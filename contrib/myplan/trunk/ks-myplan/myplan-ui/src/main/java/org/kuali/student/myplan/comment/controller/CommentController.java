/*
 * Copyright 2011 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.student.myplan.comment.controller;

import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.comment.CommentConstants;
import org.kuali.student.myplan.comment.dataobject.CommentDataObject;
import org.kuali.student.myplan.comment.dataobject.MessageDataObject;
import org.kuali.student.myplan.comment.form.CommentForm;
import org.kuali.student.myplan.comment.service.CommentQueryHelper;
import org.kuali.student.myplan.comment.util.CommentHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.core.comment.dto.CommentInfo;
import org.kuali.student.r2.core.comment.service.CommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.util.ArrayList;

@Controller
@RequestMapping(value = "/comment")
public class CommentController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(CommentController.class);

    private transient CommentService commentService;

    private transient CommentQueryHelper commentQueryHelper;

    @Autowired
    private CommentHelper commentHelper;

    @Autowired
    private UserSessionHelper userSessionHelper;

    @Override
    protected CommentForm createInitialForm(HttpServletRequest request) {
        return new CommentForm();
    }

    @RequestMapping(params = "methodToCall=startCommentForm")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);
        Person user = GlobalVariables.getUserSession().getPerson();
        String principleId = user.getPrincipalId();
        CommentForm commentForm = (CommentForm) form;
        commentForm.setStudentName(getUserSessionHelper().getStudentName());
        commentForm.setPersonName(getUserSessionHelper().getName(principleId));
        if (commentForm.getMessageId() != null) {
            MessageDataObject messageDataObject = null;
            try {
                messageDataObject = getCommentQueryHelper().getMessage(commentForm.getMessageId());
            } catch (Exception e) {
                logger.error(String.format("Query for comment [%s] failed.", commentForm.getMessageId()), e);
                return null;
            }
            if (messageDataObject != null) {
                commentForm.setSubject(messageDataObject.getSubject());
                commentForm.setBody(messageDataObject.getBody());
                commentForm.setFrom(messageDataObject.getFrom());
                commentForm.setCreatedDate(messageDataObject.getCreateDate());
                commentForm.setComments(messageDataObject.getComments());

            }
        }
        return getUIFModelAndView(commentForm);
    }

    @RequestMapping(params = "methodToCall=addComment")
    public ModelAndView addComment(@ModelAttribute("KualiForm") CommentForm form, BindingResult result,
                                   HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        Person user = GlobalVariables.getUserSession().getPerson();
        String principleId = user.getPrincipalId();
        CommentInfo messageInfo = null;
        String commentBodyText = form.getCommentBody();
        String messageText = form.getCommentBody();
        if (messageText.length() > 100) {
            messageText = messageText.substring(0, 100);
        }

        //  Look up the message
        try {
            messageInfo = getCommentService().getComment(form.getMessageId(), CommentConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error(String.format("Query for comment [%s] failed.", form.getMessageId()), e);
            return null;
        }


        try {
            getCommentHelper().createComment(messageInfo.getId(), commentBodyText);
        } catch (Exception e) {
            String subject = null;
            String createdBy = null;
            for (AttributeInfo attributeInfo : messageInfo.getAttributes()) {
                if (CommentConstants.SUBJECT_ATTRIBUTE_NAME.equals(attributeInfo.getKey())) {
                    subject = attributeInfo.getValue();
                } else if (CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME.equals(attributeInfo.getKey())) {
                    createdBy = attributeInfo.getValue();
                }
                if (subject != null && createdBy != null) {
                    break;
                }
            }
            form.setSubject(subject);
            form.setFrom(getUserSessionHelper().getName(createdBy));
            form.setBody(messageInfo.getCommentText().getPlain());
            form.setComments(new ArrayList<CommentDataObject>());
            logger.error("Could not add comment ", e);
            String[] params = {};
            return doErrorPage(form, CommentConstants.SPECIAL_CHARACTERS_ERROR, params, CommentConstants.COMMENT_RESPONSE_PAGE, CommentConstants.COMMENT_MESSAGE_BOX);
        }

        form.setCommentBody(null);
        form.setFeedBackMode(true);

        /**
         * Create an email notification. Comments can be from an adviser or a student.
         * The from address should always be the system default.
         * If user is an advisor then the "to" address should be the advised student. Otherwise, it
         * should be the e-mail address of the adviser who initiated the message.
         * (TODO: What if the student is commenting on a comment left by an adviser who didn't originate the thread)
         */

        try {
            getCommentHelper().sendCommentEmailNotification(messageInfo, messageText);
        } catch (Exception e) {
            String[] params = {};
            return doErrorPage(form, CommentConstants.EMPTY_TO_ADDRESS, params, CommentConstants.MESSAGE_RESPONSE_PAGE, CommentConstants.MESSAGE_RESPONSE_PAGE);
        }


        GlobalVariables.getMessageMap().clearErrorMessages();
        form.setPageId(CommentConstants.COMMENT_RESPONSE_PAGE);

        return start(form, result, httprequest, httpresponse);
    }

    @RequestMapping(params = "methodToCall=addMessage")
    public ModelAndView addMessage(@ModelAttribute("KualiForm") CommentForm form, BindingResult result,
                                   HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        /* Add this int the if condition to check if the user in session and the user for which the message is added are equal.
         !form.getStudentRegId().equalsIgnoreCase(getUserSessionHelper().getStudentId())*/
        if (!getUserSessionHelper().isAdviser()) {
            String[] params = {};
            return doErrorPage(form, CommentConstants.ADVISER_ACCESS_ERROR, params, CommentConstants.MESSAGE_RESPONSE_PAGE, CommentConstants.MESSAGE_RESPONSE_PAGE);
        }
        if (StringUtils.isEmpty(form.getBody()) || StringUtils.isEmpty(form.getSubject())) {
            String[] params = {};
            String section = null;
            if (StringUtils.isEmpty(form.getBody())) {
                section = CommentConstants.MESSAGE_MESSAGE_BOX;
            } else if (StringUtils.isEmpty(form.getSubject())) {
                section = CommentConstants.MESSAGE_SUBJECT_BOX;
            } else if (StringUtils.isEmpty(form.getBody()) && StringUtils.isEmpty(form.getSubject())) {
                section = CommentConstants.MESSAGE_RESPONSE_PAGE;
            }
            return doErrorPage(form, CommentConstants.EMPTY_MESSAGE, params, CommentConstants.MESSAGE_RESPONSE_PAGE, section);
        }

        String bodyText = form.getBody();
        String messageText = form.getBody();
        if (messageText.length() > 100) {
            messageText = messageText.substring(0, 100);
        }

        /*Creating a Message*/
        try {
            getCommentHelper().createMessage(form.getSubject(), bodyText);
        } catch (Exception e) {
            form.setStudentName(getUserSessionHelper().getStudentName());
            String[] params = {};
            return doErrorPage(form, CommentConstants.SPECIAL_CHARACTERS_ERROR, params, CommentConstants.MESSAGE_RESPONSE_PAGE, CommentConstants.MESSAGE_MESSAGE_BOX);
        }
        form.setFeedBackMode(true);


        /*Sending a Email notification to student*/
        try {
            getCommentHelper().sendMessageEmailNotification(form.getSubject(), messageText);
        } catch (Exception e) {
            String[] params = {};
            return doErrorPage(form, CommentConstants.EMPTY_TO_ADDRESS, params, CommentConstants.MESSAGE_RESPONSE_PAGE, CommentConstants.MESSAGE_RESPONSE_PAGE);
        }

        GlobalVariables.getMessageMap().clearErrorMessages();
        return start(form, result, httprequest, httpresponse);
    }

    /**
     * Initializes the error page.
     */
    private ModelAndView doErrorPage(CommentForm form, String errorKey, String[] params, String page, String section) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        GlobalVariables.getMessageMap().putErrorForSectionId(section, errorKey, params);
        return getUIFModelAndView(form, page);
    }

    public CommentQueryHelper getCommentQueryHelper() {
        if (commentQueryHelper == null) {
            commentQueryHelper = new CommentQueryHelper();
        }
        return commentQueryHelper;
    }

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

    public CommentHelper getCommentHelper() {
        return commentHelper;
    }

    public void setCommentHelper(CommentHelper commentHelper) {
        this.commentHelper = commentHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}
