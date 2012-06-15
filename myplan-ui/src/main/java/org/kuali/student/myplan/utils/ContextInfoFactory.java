package org.kuali.student.myplan.utils;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.Date;

/**
 * Provides an initialized Context which can be used for service requests.
 */
public class ContextInfoFactory {
    public static ContextInfo makeContextInfoInstance() {
        ContextInfo contextInfo = new ContextInfo();
        Person user = GlobalVariables.getUserSession().getPerson();
        contextInfo.setPrincipalId(user.getPrincipalId());
        contextInfo.setCurrentDate(new Date());
        return contextInfo;
    }
}
