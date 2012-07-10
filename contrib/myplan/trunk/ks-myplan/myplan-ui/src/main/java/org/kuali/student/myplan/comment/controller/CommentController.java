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
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.dto.MetaInfo;
import org.kuali.student.common.dto.RichTextInfo;
import org.kuali.student.core.comment.dto.CommentInfo;
import org.kuali.student.core.comment.service.CommentService;
import org.kuali.student.myplan.comment.CommentConstants;
import org.kuali.student.myplan.comment.dataobject.CommentDataObject;
import org.kuali.student.myplan.comment.dataobject.MessageDataObject;
import org.kuali.student.myplan.comment.form.CommentForm;
import org.kuali.student.myplan.comment.service.CommentQueryHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

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
                commentForm.setSubject(messageDataObject.getBody());
                commentForm.setFrom(messageDataObject.getFrom());
                commentForm.setCreatedDate(messageDataObject.getCreateDate());

                /*Testing purspoe*/
                CommentDataObject commentDataObject=new CommentDataObject();
                commentDataObject.setBody("hie hello");
                commentDataObject.setCreateDate(new Date());
                commentDataObject.setFrom("ME");
                messageDataObject.getComments().add(commentDataObject);
                /*test ends*/
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
        CommentInfo commentInfo = null;
        try {
            commentInfo = getCommentService().getComment(form.getMessageId());
        } catch (Exception e) {
            logger.error(String.format("Query for comment [%s] failed.", form.getMessageId()), e);
            return null;
        }
        /*If not a Adviser of if the user accesing is not the owner of the message*/
        /*if (!UserSessionHelper.isAdviser() && !principleId.equalsIgnoreCase(commentInfo.getReferenceId())) {
            String[] params = {};
            return doErrorPage(form, CommentConstants.ADVISER_ACCESS_ERROR, params);
        }*/
        CommentInfo ci = new CommentInfo();
        MetaInfo m = new MetaInfo();
        m.setCreateId(principleId);
        m.setUpdateId(principleId);
        ci.setMetaInfo(m);
        ci.setType(CommentConstants.COMMENT_TYPE);
        ci.setState("ACTIVE");
        RichTextInfo body = new RichTextInfo();
        body.setPlain(form.getBody());
        body.setFormatted(form.getBody());
        ci.setCommentText(body);
        ci.setReferenceId(commentInfo.getId());
        ci.setReferenceTypeKey(CommentConstants.COMMENT_REF_TYPE);

        try {
            CommentInfo x = getCommentService().addComment(commentInfo.getId(), CommentConstants.COMMENT_REF_TYPE, ci);
        } catch (Exception e) {
            e.printStackTrace();
        }


        return doSuccess(form, CommentConstants.SUCCESS_KEY_MESSAGE_ADDED, new String[0]);
    }

    @RequestMapping(params = "methodToCall=addMessage")
    public ModelAndView addMessage(@ModelAttribute("KualiForm") CommentForm form, BindingResult result,
                                   HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        /*if (!UserSessionHelper.isAdviser() || !form.getStudentId().equalsIgnoreCase(UserSessionHelper.getStudentId())) {
            String[] params = {};
            return doErrorPage(form, CommentConstants.ADVISER_ACCESS_ERROR, params);
        }*/


        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(CommentConstants.SUBJECT_ATTRIBUTE_NAME, form.getSubject());
        CommentInfo ci = new CommentInfo();
        ci.setAttributes(attributes);

        Person user = GlobalVariables.getUserSession().getPerson();
        String principleId = user.getPrincipalId();

        MetaInfo m = new MetaInfo();
        m.setCreateId(principleId);
        m.setUpdateId(principleId);
        ci.setMetaInfo(m);
        ci.setType(CommentConstants.MESSAGE_TYPE);
        ci.setState("ACTIVE");
        RichTextInfo body = new RichTextInfo();
        body.setPlain(form.getBody());
        body.setFormatted(form.getBody());
        ci.setCommentText(body);
        ci.setReferenceId(principleId);
        ci.setReferenceTypeKey(CommentConstants.MESSAGE_REF_TYPE);

        try {
            CommentInfo x = getCommentService().addComment(UserSessionHelper.getStudentId(), CommentConstants.MESSAGE_REF_TYPE, ci);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return doSuccess(form, CommentConstants.SUCCESS_KEY_MESSAGE_ADDED, new String[0]);
    }


    /**
     * Initializes the error page.
     */
    private ModelAndView doErrorPage(CommentForm form, String errorKey, String[] params) {
        GlobalVariables.getMessageMap().clearErrorMessages();
        GlobalVariables.getMessageMap().putErrorForSectionId(CommentConstants.MESSAGE_RESPONSE_PAGE, errorKey, params);
        return getUIFModelAndView(form, CommentConstants.MESSAGE_RESPONSE_PAGE);
    }


    private ModelAndView doSuccess(CommentForm form, String messageKey, String[] params) {
        GlobalVariables.getMessageMap().putInfoForSectionId("Page Id", messageKey, params);
        return getUIFModelAndView(form, "Page Id");
    }

    public CommentQueryHelper getCommentQueryHelper() {
        if (commentQueryHelper == null) {
            commentQueryHelper = new CommentQueryHelper();
        }
        return commentQueryHelper;
    }

    public void setCommentQueryHelper(CommentQueryHelper commentQueryHelper) {
        this.commentQueryHelper = commentQueryHelper;
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