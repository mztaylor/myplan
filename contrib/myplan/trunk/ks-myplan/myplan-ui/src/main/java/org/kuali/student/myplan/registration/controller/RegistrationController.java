package org.kuali.student.myplan.registration.controller;

import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.registration.util.RegistrationHelper;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by hemanthg on 4/22/2014.
 */
public abstract class RegistrationController extends UifControllerBase {
    private RegistrationHelper registrationHelper;

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return (UifFormBase) getRegistrationHelper().getInitialForm();
    }

    public RegistrationHelper getRegistrationHelper() {
        if (registrationHelper == null) {
            registrationHelper = UwMyplanServiceLocator.getInstance().getRegistrationHelper();
        }
        return registrationHelper;
    }

    public void setRegistrationHelper(RegistrationHelper registrationHelper) {
        this.registrationHelper = registrationHelper;
    }

}
