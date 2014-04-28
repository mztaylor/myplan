package org.kuali.student.myplan.registration.controller;

import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.registration.form.DefaultRegistrationForm;
import org.kuali.student.myplan.registration.util.RegistrationForm;
import org.kuali.student.myplan.registration.util.RegistrationHelper;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by hemanthg on 4/22/2014.
 */
@Controller
@RequestMapping(value = "/registration")
public class RegistrationController extends UifControllerBase {

    private RegistrationHelper registrationHelper;

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return (UifFormBase) getRegistrationHelper().getInitialForm();
    }

    @RequestMapping(params = "methodToCall=registrationDetails")
    public ModelAndView registrationDetails(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {
        RegistrationForm regForm = (RegistrationForm) form;
        regForm.buildRegistrationDetails();
        return getUIFModelAndView(form);
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
