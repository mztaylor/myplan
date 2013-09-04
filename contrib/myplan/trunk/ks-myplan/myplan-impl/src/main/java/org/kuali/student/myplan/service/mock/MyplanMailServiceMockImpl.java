package org.kuali.student.myplan.service.mock;

import org.kuali.rice.core.api.mail.MailMessage;
import org.kuali.rice.krad.exception.InvalidAddressException;
import org.kuali.student.myplan.service.MyPlanMailService;

import javax.mail.MessagingException;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/26/13
 * Time: 12:56 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyplanMailServiceMockImpl implements MyPlanMailService{
    @Override
    public void sendMessage(MailMessage message) throws InvalidAddressException, MessagingException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
