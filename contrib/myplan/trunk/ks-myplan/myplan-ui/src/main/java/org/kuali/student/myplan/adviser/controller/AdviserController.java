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
package org.kuali.student.myplan.adviser.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kns.web.struts.form.KualiForm;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.audit.form.DegreeAuditForm;
import org.kuali.student.myplan.course.form.CourseSearchForm;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping(value = "/advise/**")
public class AdviserController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(AdviserController.class);

    private transient PersonService personService;

    public PersonService getPersonService() {

        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        return personService;

    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return new DegreeAuditForm();
    }

    @RequestMapping(value = "/advise", method = RequestMethod.GET)
    public String doGet(@ModelAttribute("KualiForm") UifFormBase form) {
        UserSession session = GlobalVariables.getUserSession();
        clearSession(session);
        form.setView(getViewService().getViewById("PlannedCourses-LookupView"));
        form.setRequestRedirect(true);
        GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_PAGE_ID, PlanConstants.ERROR_KEY_NO_STUDENT_PROXY_ID);

        return "redirect:/myplan/advise/error";
    }

    @RequestMapping(value = "/advise/", method = RequestMethod.GET)
    public String get(@ModelAttribute("KualiForm") UifFormBase form) {
        UserSession session = GlobalVariables.getUserSession();
        clearSession(session);
        form.setView(getViewService().getViewById("PlannedCourses-LookupView"));
        form.setRequestRedirect(true);
        GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_PAGE_ID, PlanConstants.ERROR_KEY_NO_STUDENT_PROXY_ID);

        return "redirect:/myplan/advise/error";
    }

    /**
     * This URL will be authenticated using a two-factor method (via an .htaccess file). This
     * method will then check for the existence of an "adviser" role and if the authenticated
     * user has an adviser role a flag will be set in the session which can be referenced from
     * other pages to indicate that adviser contextual behavior should be applied.
     *
     * @return A redirect to the start page.
     */
    @RequestMapping(value = "/advise/{studentId}", method = RequestMethod.GET)
    public String get(@PathVariable("studentId") String studentId, @ModelAttribute("KualiForm") UifFormBase form) {

        form.setView(getViewService().getViewById("PlannedCourses-LookupView"));
        form.setRequestRedirect(true);
        UserSession session = GlobalVariables.getUserSession();

        //  FIXME!!! ... Do stuff to verify that the user has an ASTRA adviser role.

        //  Set the adviser session flag. (The value isn't important)
        session.addObject(PlanConstants.SESSION_KEY_IS_ADVISER, true);

        //   Validate the student id
        if (StringUtils.isEmpty(studentId)) {
            GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_PAGE_ID, PlanConstants.ERROR_KEY_NO_STUDENT_PROXY_ID);
            studentId = "unset";
        } else {
            //  Check the session flag.
            String oldId = (String) session.retrieveObject(PlanConstants.SESSION_KEY_STUDENT_ID);
            if (!StringUtils.isEmpty(oldId) && !studentId.equals(oldId)) {
                GlobalVariables.getMessageMap().putWarningForSectionId(PlanConstants.PLAN_PAGE_ID, PlanConstants.WARNING_STUDENT_CONTEXT_SWITCH, oldId, studentId);
            }
        }

        //   Put the student Id in the session.
        session.addObject(PlanConstants.SESSION_KEY_STUDENT_ID, studentId);

        Person person = getPersonService().getPerson(studentId);
        if (person != null) {
            session.addObject(PlanConstants.SESSION_KEY_STUDENT_NAME, person.getFirstName() + " " + person.getLastName());
            return "redirect:/myplan/lookup?methodToCall=search&viewId=PlannedCourses-LookupView";

        } else {
            clearSession(session);
            return "redirect:/myplan/advise/error";

        }
    }
    @RequestMapping(value = "/advise/error", method = RequestMethod.GET)
   public ModelAndView returnErrorForm(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {

                UifFormBase formBase=(UifFormBase)form;
        formBase.setView(getViewService().getViewById("Advisor-FormView"));
        formBase.setPageId("advisor_page");
        GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_PAGE_ID, PlanConstants.ERROR_KEY_NO_STUDENT_PROXY_ID);
        return getUIFModelAndView(formBase);
    }

    private void clearSession(UserSession session) {
        session.removeObject(PlanConstants.SESSION_KEY_STUDENT_ID);
        session.addObject(PlanConstants.SESSION_KEY_STUDENT_ID, "");
        session.removeObject(PlanConstants.SESSION_KEY_STUDENT_NAME);
        session.addObject(PlanConstants.SESSION_KEY_STUDENT_NAME, "");
    }
}


