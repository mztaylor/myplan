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
package org.kuali.student.myplan.course.controller;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.form.PlanActionsForm;
import org.kuali.student.myplan.course.util.SavedCourseListConstants;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.exceptions.*;
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
@RequestMapping(value = "/plan")
public class PlanController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(PlanController.class);

    private transient AcademicPlanService academicPlanService;

    @Override
    protected PlanActionsForm createInitialForm(HttpServletRequest request) {
        return new PlanActionsForm();
    }

    @RequestMapping(params = "methodToCall=addSavedCourse")
    public ModelAndView addPlanItem(@ModelAttribute("KualiForm") PlanActionsForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        Person user = GlobalVariables.getUserSession().getPerson();

        boolean hasError = false;
        /*
         *  First fetch the student's learning plan. If they don't have a plan then create one.
         */
        List<LearningPlanInfo> learningPlans = null;
        try {
            learningPlans = getAcademicPlanService().getLearningPlansForStudentByType(user.getPrincipalId(),
                    AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN, SavedCourseListConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Learning plan query failed.", e);
            GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            hasError = true;
        }

        if (learningPlans == null) {
            logger.error("Learning Plans query produced null.");
            GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            hasError = true;
        }

        //  There should currently only be a single learning plan. This may change in the future.
        if (learningPlans.size() > 1) {
            logger.error(String.format("Student [%s] has more than one plan.", user.getPrincipalId()));
            GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            hasError = true;
        }

        //  Bail here if there were problems with the plan lookup.
        if (hasError) {
            form.setPlanItemId("");
            return getUIFModelAndView(form, SavedCourseListConstants.PLAN_ITEM_ADD_PAGE_ID);
        }

        //  Use an existing plan or create a new one.
        LearningPlan learningPlan = null;
        if (learningPlans.size() != 0) {
            learningPlan = learningPlans.get(0);
        } else {
            try {
                learningPlan = createDefaultLearningPlan(user.getPrincipalId());
            } catch (Exception e) {
                //  End-users won't care about the details of what went wrong here, so provide a generic error message, but
                //  log the exception.
                hasError = true;
                logger.error("Unable to create default learning plan.",  e);
                GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            }
        }

        String newPlanItemId = "";

        if ( ! hasError) {
            //  Course ID will be validated by the service.
            String courseId = form.getCourseId();

            PlanItemInfo pii = new PlanItemInfo();
            pii.setLearningPlanId(learningPlan.getId());
            pii.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
            pii.setRefObjectType(LUConstants.CLU_TYPE_CREDIT_COURSE);
            pii.setRefObjectId(courseId);

            pii.setStateKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

            RichTextInfo rti = new RichTextInfo();
            rti.setFormatted("");
            rti.setPlain("");
            pii.setDescr(rti);

            PlanItem newPlanItem = null;
            try {
                newPlanItem = getAcademicPlanService().createPlanItem(pii, SavedCourseListConstants.CONTEXT_INFO);
                newPlanItemId = newPlanItem.getId();
            } catch (AlreadyExistsException e) {
                //  The course id was already in the saved courses list. Let this error go unreported.
                logger.warn("Item was already in wishlist.", e);
            } catch (Exception e) {
                //  Give the end-user a generic error message, but log the exception.
                logger.error("Could not create plan item.", e);
                //   Remove the errors, then add a more generic one.
                GlobalVariables.getMessageMap().clearErrorMessages();
                GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            }
        }

        form.setPlanItemId(newPlanItemId);

        return getUIFModelAndView(form, SavedCourseListConstants.PLAN_ITEM_ADD_PAGE_ID);
    }

    /**
     * Create a new learning plan for the given student.
     * @param studentId
     * @return The plan.
     */
    private LearningPlan createDefaultLearningPlan(String studentId) throws InvalidParameterException, DataValidationErrorException,
            MissingParameterException, AlreadyExistsException, PermissionDeniedException, OperationFailedException {

        LearningPlanInfo plan = new LearningPlanInfo();
        plan.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN);
        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted("");
        rti.setPlain("");
        plan.setDescr(rti);
        plan.setStudentId(studentId);
        plan.setStateKey(AcademicPlanServiceConstants.LEARNING_PLAN_ACTIVE_STATE_KEY);

        LearningPlan newPlan = getAcademicPlanService().createLearningPlan(plan, SavedCourseListConstants.CONTEXT_INFO);

        return newPlan;
    }

    @RequestMapping(params = "methodToCall=removeItem")
    public ModelAndView removePlanItem(@ModelAttribute("KualiForm") PlanActionsForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String planItemId = form.getPlanItemId();

        try {

            // First load the plan item and retrieve the courseId
            PlanItemInfo planItem = getAcademicPlanService().getPlanItem(planItemId, SavedCourseListConstants.CONTEXT_INFO);
            form.setCourseId(planItem.getRefObjectId());

            // Now delete the plan item
            getAcademicPlanService().deletePlanItem(planItemId, SavedCourseListConstants.CONTEXT_INFO);
        } catch (DoesNotExistException e) {
            //  Assume the end-user already deleted this item and silently let this error go. Log it though.
            logger.warn("Tried to delete a plan item that doesn't exist.", e);
        } catch (Exception e) {
            //  Give the end-user a generic error message, but log the exception.
            logger.error("Could not delete plan item.", e);
            //   Remove the errors, then add a more generic one.
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putErrorForSectionId("remove_plan_item_page", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
        }

        return getUIFModelAndView(form, SavedCourseListConstants.PLAN_ITEM_REMOVE_PAGE_ID);
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                GlobalResourceLoader.getService(new QName(AcademicPlanServiceConstants.NAMESPACE,
                    AcademicPlanServiceConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }
}