package org.kuali.student.myplan.service;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.exception.InvalidAddressException;

import org.kuali.rice.core.api.mail.Mailer;
import org.kuali.rice.krad.util.GlobalVariables;

import javax.mail.MessagingException;
import java.util.HashSet;
import java.util.Set;
import org.apache.log4j.Logger;

/**
 * Wrapper service for the Rice mailer which allows the mailer to be put
 * in "test" mode where messages will only be sent to the logged in user.
 */
public class MyPlanMailServiceImpl implements MyPlanMailService {

    private final Logger logger = Logger.getLogger(MyPlanMailServiceImpl.class);

    static final boolean IS_EMAIL_TEST_MODE_DEFAULT = true;
    private Mailer mailer;

    /**
     * Spring-injected Mailer.
     */
    public void setMailer(Mailer mailer) {
    	this.mailer = mailer;
    }

    /**
     * Reads the mode from a system property.
     */
    private boolean isTestMode() {
        return ConfigContext.getCurrentContextConfig().getBooleanProperty(MODE_CONFIG_PROPERTY_NAME, IS_EMAIL_TEST_MODE_DEFAULT);
    }

    @Override
    public void sendMessage(MailMessage message) throws InvalidAddressException, MessagingException {
        if (isTestMode()) {
            Person user = GlobalVariables.getUserSession().getPerson();
            String emailAddress = user.getEmailAddress();
            logger.warn(String.format("Substituting 'to', 'cc', and 'bcc' fields to [%s] in message [%s] from [%s].", emailAddress, message.getSubject(), message.getFromAddress()));
            Set<String> to = new HashSet<String>();
            to.add(emailAddress);
            message.setToAddresses(to);
            if (message.getBccAddresses().size() > 0) {
                message.setBccAddresses(to);
            }
            if (message.getCcAddresses().size() > 0) {
                message.setCcAddresses(to);
            }
        } else {
            logger.info(String.format("Sending e-mail with no address substitutions in message [%s] from [%s].", message.getSubject(), message.getFromAddress()));
        }
        mailer.sendEmail(message);
    }
}
