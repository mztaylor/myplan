package edu.uw.kuali.student.myplan.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.exception.InvalidAddressException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.common.dto.RichTextInfo;
import org.kuali.student.common.exceptions.*;
import org.kuali.student.core.comment.dto.CommentInfo;
import org.kuali.student.core.comment.service.CommentService;
import org.kuali.student.myplan.comment.CommentConstants;
import org.kuali.student.myplan.comment.util.CommentHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.service.MyPlanMailService;
import org.kuali.student.myplan.utils.UserSessionHelper;

import javax.mail.MessagingException;
import javax.xml.namespace.QName;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/25/13
 * Time: 8:06 AM
 * To change this template use File | Settings | File Templates.
 */
public class CommentHelperImpl implements CommentHelper {

    private final Logger logger = Logger.getLogger(CommentHelperImpl.class);

    private CommentService commentService;

    private MyPlanMailService mailService;

    private transient String propertiesFilePath = "/org/kuali/student/myplan/KSMyPlan-ApplicationResources.properties";

    /**
     * Create a new Message.
     * Advisers can only initiate a new message
     *
     * @param subjectText
     * @param messageText
     * @return
     * @throws DataValidationErrorException
     * @throws InvalidParameterException
     * @throws MissingParameterException
     * @throws OperationFailedException
     * @throws PermissionDeniedException
     */
    @Override
    public CommentInfo createMessage(String subjectText, String messageText) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        CommentInfo ci = new CommentInfo();

        String studentPrincipleId = UserSessionHelper.getStudentRegId();
        String principleId = UserSessionHelper.getCurrentUserRegId();

        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(CommentConstants.SUBJECT_ATTRIBUTE_NAME, subjectText);
        attributes.put(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME, principleId);
        ci.setAttributes(attributes);
        ci.setType(CommentConstants.MESSAGE_TYPE);
        ci.setState("ACTIVE");
        RichTextInfo rtiBody = new RichTextInfo();
        rtiBody.setPlain(messageText);
        ci.setCommentText(rtiBody);
        ci.getAttributes().put(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME, principleId);


        ci = getCommentService().addComment(studentPrincipleId, CommentConstants.MESSAGE_REF_TYPE, ci);

        return ci;

    }


    /**
     * Create a comment for already existing message thread
     * Both Advisers and students can initiate comments
     *
     * @param referenceId
     * @param commentText
     * @return
     * @throws DataValidationErrorException
     * @throws InvalidParameterException
     * @throws MissingParameterException
     * @throws OperationFailedException
     * @throws PermissionDeniedException
     */
    @Override
    public CommentInfo createComment(String referenceId, String commentText) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {

        CommentInfo ci = new CommentInfo();

        Person user = GlobalVariables.getUserSession().getPerson();
        String principleId = user.getPrincipalId();


        Map<String, String> attributes = new HashMap<String, String>();
        attributes.put(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME, principleId);
        ci.setAttributes(attributes);
        ci.setType(CommentConstants.COMMENT_TYPE);
        ci.setState("ACTIVE");
        RichTextInfo rtiBody = new RichTextInfo();
        rtiBody.setPlain(commentText);
        ci.setCommentText(rtiBody);
        ci.getAttributes().put(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME, principleId);

        ci = getCommentService().addComment(referenceId, CommentConstants.COMMENT_REF_TYPE, ci);

        return ci;

    }


    /**
     * Create an email notification. The from address should always be the system default.
     *
     * @param subjectText
     * @param messageText
     * @throws MissingParameterException
     */
    @Override
    public void sendMessageEmailNotification(String subjectText, String messageText) throws MissingParameterException {

        String studentPrincipleId = UserSessionHelper.getStudentRegId();
        String studentName = UserSessionHelper.getFirstName(studentPrincipleId);
        String principleId = UserSessionHelper.getCurrentUserRegId();

        Properties pro = new Properties();
        InputStream file = getClass().getResourceAsStream(propertiesFilePath);
        try {
            pro.load(file);
        } catch (Exception e) {
            logger.error("Could not find the properties file" + e);
        }

        String adviserName = UserSessionHelper.getName(principleId);

        String toAddress = UserSessionHelper.getMailAddress(studentPrincipleId);
        if (toAddress == null) {
            throw new MissingParameterException();
        }


        String messageLink = ConfigContext.getCurrentContextConfig().getProperty(CommentConstants.MESSAGE_LINK);
        String fromAddress = ConfigContext.getCurrentContextConfig().getProperty(CommentConstants.EMAIL_FROM);
        String subjectProp = pro.getProperty(CommentConstants.EMAIL_MESSAGE_SUBJECT);
        String emailBody = pro.getProperty(CommentConstants.EMAIL_BODY);
        String subject = String.format(subjectProp, adviserName);
        String emailSubject = String.format(pro.getProperty(CommentConstants.EMAIL_SUBJECT), subjectText);
        String body = String.format(emailBody, studentName, adviserName, emailSubject, messageText, messageLink);

        if (StringUtils.isNotEmpty(toAddress)) {
            try {
                sendMessage(fromAddress, toAddress, subject, body);
                logger.info("Sent message email (" + messageText + ") to student:" + studentName + "from adviser :" + adviserName);

            } catch (Exception e) {
                logger.error(String.format("Could not send e-mail from [%s] to [%s].", fromAddress, toAddress), e);
                GlobalVariables.getMessageMap().putErrorForSectionId("message_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);
            }

        } else {

            logger.error(String.format("No e-mail address found for [%s][%s].", studentName, studentPrincipleId));
            GlobalVariables.getMessageMap().putErrorForSectionId("message_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);

        }

    }


    /**
     * @param commentInfo
     * @param comment
     * @throws MissingParameterException
     */
    @Override
    public void sendCommentEmailNotification(CommentInfo commentInfo, String comment) throws MissingParameterException {

        String studentPrincipleId = UserSessionHelper.getStudentRegId();
        String studentName = UserSessionHelper.getFirstName(studentPrincipleId);
        String principleId = UserSessionHelper.getCurrentUserRegId();


        Properties pro = new Properties();
        InputStream file = getClass().getResourceAsStream(propertiesFilePath);
        try {
            pro.load(file);
        } catch (Exception e) {
            logger.error("Could not find the properties file" + e);
        }

        String toId, toAddress, toName, fromId, fromAddress, fromName;

        fromId = principleId;
        String messageLink = ConfigContext.getCurrentContextConfig().getProperty(CommentConstants.MESSAGE_LINK);
        if (UserSessionHelper.isAdviser()) {
            toId = studentPrincipleId;
            toName = studentName;

        } else {
            //  Get the created by user Id from the message.
            toId = commentInfo.getAttributes().get(CommentConstants.CREATED_BY_USER_ATTRIBUTE_NAME);
            toName = UserSessionHelper.getFirstName(toId);
            messageLink = ConfigContext.getCurrentContextConfig().getProperty(CommentConstants.ADVISER_MESSAGE_LINK) + fromId;

        }

        fromName = UserSessionHelper.getName(fromId);
        toAddress = UserSessionHelper.getMailAddress(toId);
        if (toAddress == null) {
            throw new MissingParameterException();
        }

        fromAddress = ConfigContext.getCurrentContextConfig().getProperty(CommentConstants.EMAIL_FROM);
        String subjectProp = pro.getProperty(CommentConstants.EMAIL_COMMENT_SUBJECT);
        String emailBody = pro.getProperty(CommentConstants.EMAIL_BODY);
        String subject = String.format(subjectProp, fromName);
        String emailSubject = "";
        String body = String.format(emailBody, toName, fromName, emailSubject, comment, messageLink);

        if (StringUtils.isNotEmpty(toAddress)) {
            try {
                sendMessage(fromAddress, toAddress, subject, body);
                logger.info("Sent comment email (" + comment + ") to: " + toAddress + " From: " + fromAddress);
            } catch (Exception e) {
                logger.error(String.format("Could not send e-mail from [%s] to [%s].", fromAddress, toAddress), e);
                GlobalVariables.getMessageMap().putErrorForSectionId("comment_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);
            }
        } else {
            logger.error(String.format("No e-mail address found for [%s].", toName));
            GlobalVariables.getMessageMap().putErrorForSectionId("comment_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);
        }
    }

    /**
     * Create an email notification. The from address should always be the system default.
     *
     * @param subjectText
     * @param messageText
     * @throws MissingParameterException
     */
    @Override
    public void sendRecommendationEmailNotification(String subjectText, String messageText) throws MissingParameterException {

        String studentPrincipleId = UserSessionHelper.getStudentRegId();
        String studentName = UserSessionHelper.getFirstName(studentPrincipleId);
        String principleId = UserSessionHelper.getCurrentUserRegId();

        Properties pro = new Properties();
        InputStream file = getClass().getResourceAsStream(propertiesFilePath);
        try {
            pro.load(file);
        } catch (Exception e) {
            logger.error("Could not find the properties file" + e);
        }

        String adviserName = UserSessionHelper.getName(principleId);

        String toAddress = UserSessionHelper.getMailAddress(studentPrincipleId);
        if (toAddress == null) {
            throw new MissingParameterException();
        }

        String fromAddress = ConfigContext.getCurrentContextConfig().getProperty(CommentConstants.EMAIL_FROM);
        String emailBody = pro.getProperty(PlanConstants.RECOMMENDATION_EMAIL_BODY);
        String body = String.format(emailBody, studentName, messageText);

        if (StringUtils.isNotEmpty(toAddress)) {
            try {
                sendMessage(fromAddress, toAddress, subjectText, body);
                logger.info("Sent message email (" + messageText + ") to student:" + studentName + "from adviser :" + adviserName);

            } catch (Exception e) {
                logger.error(String.format("Could not send e-mail from [%s] to [%s].", fromAddress, toAddress), e);
                GlobalVariables.getMessageMap().putErrorForSectionId("message_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);
            }

        } else {

            logger.error(String.format("No e-mail address found for [%s][%s].", studentName, studentPrincipleId));
            GlobalVariables.getMessageMap().putErrorForSectionId("message_dialog_response_page", CommentConstants.ERROR_KEY_NOTIFICATION_FAILED);

        }

    }


    private void sendMessage(String fromAddress, String toAddress, String subjectText, String bodyText) throws MessagingException, InvalidAddressException {
        MailMessage mm = new MailMessage();
        mm.addToAddress(toAddress);
        mm.setFromAddress(fromAddress);
        mm.setSubject(subjectText);
        mm.setMessage(bodyText);
        getMailService().sendMessage(mm);
    }

    private MyPlanMailService getMailService() {
        if (mailService == null) {
            mailService = (MyPlanMailService) GlobalResourceLoader.getService(MyPlanMailService.SERVICE_NAME);
        }
        return mailService;
    }

    public void setMailService(MyPlanMailService mailService) {
        this.mailService = mailService;
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
