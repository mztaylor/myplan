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

import org.apache.log4j.Logger;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.mail.*;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.exception.InvalidAddressException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.dto.RichTextInfo;
import org.kuali.student.core.comment.dto.CommentInfo;
import org.kuali.student.core.comment.service.CommentService;
import org.kuali.student.myplan.service.MyPlanMailService;
import org.kuali.student.myplan.comment.CommentConstants;
import org.kuali.student.myplan.comment.dataobject.MessageDataObject;
import org.kuali.student.myplan.comment.form.CommentForm;
import org.kuali.student.myplan.comment.service.CommentQueryHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.util.*;

@Controller
@RequestMapping(value = "/comment")
public class CommentController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(CommentController.class);

    private transient CommentService commentService;

    private transient CommentQueryHelper commentQueryHelper;

    private transient MyPlanMailService mailService;

    @Override
    protected CommentForm createInitialForm(HttpServletRequest request) {
        return new CommentForm();
    }

    @RequestMapping(params = "methodToCall=startCommentForm")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);
        CommentForm commentForm = (CommentForm) form;
        commentForm.setStudentName(UserSessionHelper.getStudentName());
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

        //  Look up the message
        try {
            messageInfo = getCommentService().getComment(form.getMessageId());
        } catch (Exception e) {
            logger.error(String.format("Query for comment [%s] failed.", form.getMessageId()), e);
            return null;
        }
        if (StringUtils.isEmpty(form.getCommentBody())){
            String[] params = {};
            return doErrorPage(form, CommentConstants.EMPTY_COMMENT, params,CommentConstants.COMMENT_RESPONSE_PAGE);
        }
        /*If not a Adviser or if the user accessing is not the owner of the message*/
        /*if (!UserSessionHelper.isAdviser() || !principleId.equalsIgnoreCase(commentInfo.getReferenceId())) {
            String[] params = {};
            return doErrorPage(form, CommentConstants.ADVISER_ACCESS_ERROR, params);
        }*/
        CommentInfo ci = new CommentInfo();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME, principleId);
        ci.setAttributes(attributes);
        ci.setType(CommentConstants.COMMENT_TYPE);
        ci.setState("ACTIVE");
        RichTextInfo rtiBody = new RichTextInfo();
        rtiBody.setPlain(form.getCommentBody());
        rtiBody.setFormatted(form.getCommentBody());
        ci.setCommentText(rtiBody);
        ci.getAttributes().put(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME, principleId);

        try {
            getCommentService().addComment(messageInfo.getId(), CommentConstants.COMMENT_REF_TYPE, ci);
        } catch (Exception e) {
            e.printStackTrace();
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
        String toId, toAddress, toName, fromId, fromAddress, fromName;

        fromId = principleId;

        if (UserSessionHelper.isAdviser()) {
            toId = UserSessionHelper.getStudentId();
            toName = UserSessionHelper.getStudentName();
        } else {
            //  Get the created by user Id from the message.
            toId = messageInfo.getAttributes().get(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME);
            toName = UserSessionHelper.getName(toId);
        }

        fromName = UserSessionHelper.getName(fromId);
        toAddress = UserSessionHelper.getMailAddress(toId);
        if(toAddress==null){
            String[] params = {};
            return doErrorPage(form, CommentConstants.EMPTY_TO_ADDRESS, params,CommentConstants.COMMENT_RESPONSE_PAGE);
        }
        fromAddress = ConfigContext.getCurrentContextConfig().getProperty("myplan.comment.fromAddress");
        String subject = String.format("[MyPlan] %s left a comment", fromName);
        String body = String.format("Hello %s,\nMyPlan is letting you know that %s left a comment to message [%s].",
                toName, fromName, messageInfo.getAttributes().get(CommentConstants.SUBJECT_ATTRIBUTE_NAME));

        if (StringUtils.isNotEmpty(toAddress)) {
            try {
               sendMessage(fromAddress, toAddress, subject, body);
            } catch (Exception e) {
               logger.error(String.format("Could not send e-mail from [%s] to [%s].", fromAddress, toAddress), e);
               GlobalVariables.getMessageMap().putErrorForSectionId("comment_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);
            }
        } else {
            logger.error(String.format("No e-mail address found for [%s].", toName));
            GlobalVariables.getMessageMap().putErrorForSectionId("comment_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);
        }

        return start(form, result, httprequest, httpresponse);
    }

    @RequestMapping(params = "methodToCall=addMessage")
    public ModelAndView addMessage(@ModelAttribute("KualiForm") CommentForm form, BindingResult result,
                                   HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        /* Add this int the if condition to check if the user in session and the user for which the message is added are equal.
         !form.getStudentId().equalsIgnoreCase(UserSessionHelper.getStudentId())*/
        if (!UserSessionHelper.isAdviser() ) {
            String[] params = {};
            return doErrorPage(form, CommentConstants.ADVISER_ACCESS_ERROR, params,CommentConstants.MESSAGE_RESPONSE_PAGE);
        }
        if(StringUtils.isEmpty(form.getBody()) || StringUtils.isEmpty(form.getSubject())){
            String[] params = {};
            return doErrorPage(form, CommentConstants.EMPTY_MESSAGE, params,CommentConstants.MESSAGE_RESPONSE_PAGE);
        }
        Person user = GlobalVariables.getUserSession().getPerson();
        String principleId = user.getPrincipalId();

        CommentInfo ci = new CommentInfo();
        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(CommentConstants.SUBJECT_ATTRIBUTE_NAME, form.getSubject());
        attributes.put(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME, principleId);
        ci.setAttributes(attributes);
        ci.setType(CommentConstants.MESSAGE_TYPE);
        ci.setState("ACTIVE");
        RichTextInfo rtiBody = new RichTextInfo();
        rtiBody.setPlain(form.getBody());
        rtiBody.setFormatted(form.getBody());
        ci.setCommentText(rtiBody);
        ci.getAttributes().put(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME, principleId);

        String studentPrincipleId = UserSessionHelper.getStudentId();

        try {
            getCommentService().addComment(studentPrincipleId, CommentConstants.MESSAGE_REF_TYPE, ci);
        } catch (Exception e) {
            e.printStackTrace();
        }
        form.setFeedBackMode(true);

        /**
         * Create an email notification. Messages are only initiated by an adviser.
         * The from address should always be the system default.
         */
        String studentName = UserSessionHelper.getStudentName();
        String adviserName = UserSessionHelper.getName(principleId);
        String toAddress = UserSessionHelper.getMailAddress(studentPrincipleId);        
            if(toAddress==null){
                    String[] params = {};
                    return doErrorPage(form, CommentConstants.EMPTY_TO_ADDRESS, params,CommentConstants.MESSAGE_RESPONSE_PAGE);
            }
        String fromAddress = ConfigContext.getCurrentContextConfig().getProperty("myplan.comment.fromAddress");
        String subject = String.format("[MyPlan] %s left a message for you", adviserName);
        String body = String.format("Hello %s,\nMyPlan is letting you know that %s left a message for you.", studentName, adviserName);

        if (StringUtils.isNotEmpty(toAddress)) {
            try {
               sendMessage(fromAddress, toAddress, subject, body);
            } catch (Exception e) {
               logger.error(String.format("Could not send e-mail from [%s] to [%s].", fromAddress, toAddress), e);
               GlobalVariables.getMessageMap().putErrorForSectionId("message_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);
            }
        } else {
            logger.error(String.format("No e-mail address found for [%s][%s].", studentName, studentPrincipleId));
            GlobalVariables.getMessageMap().putErrorForSectionId("message_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);
        }

        return start(form, result, httprequest, httpresponse);
    }

    /**
     * Initializes the error page.
     */
    private ModelAndView doErrorPage(CommentForm form, String errorKey, String[] params,String page) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        GlobalVariables.getMessageMap().putErrorForSectionId(page, errorKey, params);
        return getUIFModelAndView(form, page);
    }

    public CommentQueryHelper getCommentQueryHelper() {
        if (commentQueryHelper == null) {
            commentQueryHelper = new CommentQueryHelper();
        }
        return commentQueryHelper;
    }

    private void sendMessage(String fromAddress, String toAddress, String subjectText, String bodyText) throws MessagingException, InvalidAddressException {
        MailMessage mm = new MailMessage();
        mm.addToAddress(toAddress);
        mm.setFromAddress(fromAddress);
        mm.setSubject(subjectText);
        mm.setMessage(bodyText);
        getMailService().sendMessage(mm);
    }

    public String errorPage(@ModelAttribute("KualiForm") CommentForm form) {
        return "redirect:/myplan/unauthorized";
    }

    private MyPlanMailService getMailService() {
        if (mailService == null) {
            mailService = (MyPlanMailService) GlobalResourceLoader.getService(MyPlanMailService.SERVICE_NAME);
        }
        return mailService;
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
}