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
package org.kuali.student.myplan.plan.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryViewHelperServiceImpl;
import org.kuali.student.myplan.plan.form.PlanForm;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
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

    private transient CourseDetailsInquiryViewHelperServiceImpl courseDetailsInquiryService;

    @Override
    protected PlanForm createInitialForm(HttpServletRequest request) {
        return new PlanForm();
    }

    @RequestMapping(params = "methodToCall=startAddPlannedCourseForm")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);
        //  Initialize the form with a course Id.
        PlanForm planForm = (PlanForm) form;
        planForm.setCourseId(planForm.getCourseId());

        //  Also, add a full CourseDetails object so that course details properties are available to be displayed on the form.
        planForm.setCourseDetails(getCourseDetailsInquiryService().retrieveCourseDetails(planForm.getCourseId()));

        return getUIFModelAndView(planForm);
    }

    @RequestMapping(params = "methodToCall=addPlannedCourse")
    public ModelAndView addPlannedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String courseId = form.getCourseId();

        String termIdString = form.getTermsList();
        if (StringUtils.isEmpty(termIdString)) {
            //  DO ERROR.
            throw new RuntimeException("Terms List was empty.");
        }
        String[] t = termIdString.split(",");
        List<String> termIds = Arrays.asList(t);

        if (termIds.isEmpty()) {
            //  DO ERROR.
            throw new RuntimeException("Couldn't parse terms list.");
        }


        PlanItem item = addPlanItem(courseId, termIds, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);

        form.setPlanItemId(item.getId());
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_ADD_PAGE_ID);
    }

    @RequestMapping(params = "methodToCall=addSavedCourse")
    public ModelAndView addSavedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                       HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String courseId = form.getCourseId();

        PlanItem item = addPlanItem(courseId, null, PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);

        form.setPlanItemId(item.getId());
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_ADD_PAGE_ID);
    }


    protected PlanItem addPlanItem(String courseId, List<String> termIds, String planItemType) {

        if (StringUtils.isEmpty(courseId)) {
            // DO ERROR.
            throw new RuntimeException("Empty Course ID");
        }

        Person user = GlobalVariables.getUserSession().getPerson();

        /*
         * Get an existing plan or create a new one. This method will set error messages and return null if there are problems.
         */
        LearningPlan learningPlan = getLearningPlan(user.getPrincipalId());

        PlanItem newPlanItem = null;

        if (learningPlan != null) {
            PlanItemInfo pii = new PlanItemInfo();
            pii.setLearningPlanId(learningPlan.getId());
            pii.setTypeKey(planItemType);
            pii.setRefObjectType(LUConstants.CLU_TYPE_CREDIT_COURSE);
            pii.setRefObjectId(courseId);

            pii.setStateKey(PlanConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

            RichTextInfo rti = new RichTextInfo();
            rti.setFormatted("");
            rti.setPlain("");
            pii.setDescr(rti);


            if (null != termIds) {
                pii.setPlanPeriods(termIds);
            }

            try {
                newPlanItem = getAcademicPlanService().createPlanItem(pii, PlanConstants.CONTEXT_INFO);
            } catch (AlreadyExistsException e) {
                //  The course id was already in the saved courses list log the error and set the isDup flag to trigger
                //  a lookup which will find the id of the existing plan item.
                logger.warn("Item was already in wishlist.", e);
                newPlanItem = getPlanItemByCourseIdAndType(courseId, planItemType);
            } catch (Exception e) {
                //  Give the end-user a generic error message, but log the exception.
                logger.error("Could not create plan item.", e);
                //   Remove the errors, then add a more generic one.
                GlobalVariables.getMessageMap().clearErrorMessages();
                GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
            }
        }

        return newPlanItem;
    }


    /*
         *  If the wishlist item couldn't be added because the course already exists then lookup the id of the existing
         *  plan item.
         */

    protected PlanItem getPlanItemByCourseIdAndType(String courseId, String planItemType) {

        Person user = GlobalVariables.getUserSession().getPerson();
        LearningPlan learningPlan = getLearningPlan(user.getPrincipalId());

        List<PlanItemInfo> planItems = null;
        PlanItem item = null;

        try {
            planItems = getAcademicPlanService().getPlanItemsInPlanByType(learningPlan.getId(), planItemType, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
        }

        if (planItems == null || planItems.isEmpty()) {
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
        } else {
            for (PlanItem p : planItems) {
                if (p.getRefObjectId().equals(courseId)) {
                    item = p;
                    break;
                }
            }
            //  A null here means that the duplicate plan item couldn't be found.
            if (item == null) {
                GlobalVariables.getMessageMap().clearErrorMessages();
                //  TODO: Put in a more specific error message?
                GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
            }
        }

        return item;
    }


    /**
     * Retrieve a student's LearningPlan or create one if it doesn't exist.
     *
     * @param studentId
     * @return A LearningPlan or null on errors.
     */
    private LearningPlan getLearningPlan(String studentId) {
        /*
         *  First fetch the student's learning plan. If they don't have a plan then create one.
         */
        List<LearningPlanInfo> learningPlans = null;
        try {
            learningPlans = getAcademicPlanService().getLearningPlansForStudentByType(studentId,
                    PlanConstants.LEARNING_PLAN_TYPE_PLAN, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Learning plan query failed.", e);
            GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
            return null;
        }

        if (learningPlans == null) {
            logger.error("Learning Plans query produced null.");
            GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
            return null;
        }

        //  There should currently only be a single learning plan. This may change in the future.
        if (learningPlans.size() > 1) {
            logger.error(String.format("Student [%s] has more than one plan.", studentId));
            GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
            return null;
        }

        //  Use an existing plan or create a new one.
        LearningPlan learningPlan = null;
        if (learningPlans.size() != 0) {
            learningPlan = learningPlans.get(0);
        } else {
            try {
                learningPlan = createDefaultLearningPlan(studentId);
            } catch (Exception e) {
                //  End-users won't care about the details of what went wrong here, so provide a generic error message, but
                //  log the exception.
                logger.error("Unable to create learning plan.", e);
                GlobalVariables.getMessageMap().putErrorForSectionId("add_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
            }
        }
        return learningPlan;
    }

    /**
     * Create a new learning plan for the given student.
     *
     * @param studentId
     * @return The plan.
     */
    private LearningPlan createDefaultLearningPlan(String studentId) throws InvalidParameterException, DataValidationErrorException,
            MissingParameterException, AlreadyExistsException, PermissionDeniedException, OperationFailedException {

        LearningPlanInfo plan = new LearningPlanInfo();
        plan.setTypeKey(PlanConstants.LEARNING_PLAN_TYPE_PLAN);
        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted("");
        rti.setPlain("");
        plan.setDescr(rti);
        plan.setStudentId(studentId);
        plan.setStateKey(PlanConstants.LEARNING_PLAN_ACTIVE_STATE_KEY);

        LearningPlan newPlan = getAcademicPlanService().createLearningPlan(plan, PlanConstants.CONTEXT_INFO);

        return newPlan;
    }

    @RequestMapping(params = "methodToCall=removeItem")
    public ModelAndView removePlanItem(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                       HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String planItemId = form.getPlanItemId();

        try {

            // First load the plan item and retrieve the courseId
            PlanItemInfo planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
            form.setCourseId(planItem.getRefObjectId());

            // Now delete the plan item
            getAcademicPlanService().deletePlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (DoesNotExistException e) {
            //  Assume the end-user already deleted this item and silently let this error go. Log it though.
            logger.warn("Tried to delete a plan item that doesn't exist.", e);
        } catch (Exception e) {
            //  Give the end-user a generic error message, but log the exception.
            logger.error("Could not delete plan item.", e);
            //   Remove the errors, then add a more generic one.
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putErrorForSectionId("remove_plan_item_page", PlanConstants.ERROR_KEY_OPERATION_FAILED);
        }

        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_REMOVE_PAGE_ID);
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }

    public synchronized CourseDetailsInquiryViewHelperServiceImpl getCourseDetailsInquiryService() {
        if(this.courseDetailsInquiryService == null) {
            this.courseDetailsInquiryService =  new CourseDetailsInquiryViewHelperServiceImpl();
        }
        return courseDetailsInquiryService;
    }

}