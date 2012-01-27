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
import org.kuali.rice.core.api.util.RiceKeyConstants;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.student.lum.lu.LUConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.form.SavedCoursesListForm;
import org.kuali.student.myplan.course.util.SavedCourseListConstants;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.infc.RichText;
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
public class SavedCoursesListController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(SavedCoursesListController.class);

    private transient AcademicPlanService academicPlanService;

    @Override
    protected SavedCoursesListForm createInitialForm(HttpServletRequest request) {
        return new SavedCoursesListForm();
    }

    @RequestMapping(params = "methodToCall=addItem")
    public ModelAndView addPlanItem(@ModelAttribute("KualiForm") SavedCoursesListForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        Person user = GlobalVariables.getUserSession().getPerson();

        boolean hasError = false;
        /*
         *  First fetch the student's learning plan. If they don't have a plan then create one.
         */
        List<LearningPlan> learningPlans = null;
        try {
            learningPlans = getAcademicPlanService().getLearningPlansForStudentByType(user.getPrincipalId(),
                    AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN, SavedCourseListConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Learning plan query failed.", e);
            GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            hasError = true;
        }

        if (learningPlans == null) {
            logger.error("Learning Plans query produced null.");
            GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            hasError = true;
        }

        //  There should currently only be a single learning plan. This may change in the future.
        if (learningPlans.size() > 1) {
            logger.error(String.format("Student [%s] has more than one plan.", user.getPrincipalId()));
            GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
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
                //  Assume an error here, but clear it if none occurs. This is to avoid flagging in every catch.
                hasError = true;
                learningPlan = createDefaultLearningPlan(user.getPrincipalId());
                hasError = false;
            } catch (AlreadyExistsException e) {
                GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_DUPLICATE_PLAN);
            } catch (DataValidationErrorException e) {
                GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_DATA_VALIDATION_ERROR, e.getMessage());
            } catch (InvalidParameterException e) {
                GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_INVALID_PARAM, e.getMessage());
            } catch (MissingParameterException e) {
                GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_MISSING_PARAM, e.getMessage());
            } catch (OperationFailedException e) {
                GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            } catch (PermissionDeniedException e) {
                GlobalVariables.getMessageMap().putError("planId", SavedCourseListConstants.ERROR_KEY_PERMISSION_DENIED);
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

            RichTextInfo rti = new RichTextInfo();
            rti.setFormatted("");
            rti.setPlain("");
            pii.setDescr(rti);

            PlanItem newPlanItem = null;
            try {
                newPlanItem = getAcademicPlanService().createPlanItem(pii, SavedCourseListConstants.CONTEXT_INFO);
                newPlanItemId = newPlanItem.getId();
            } catch (AlreadyExistsException e) {
                GlobalVariables.getMessageMap().putError("planItemId", SavedCourseListConstants.ERROR_KEY_DUPLICATE_PLAN_ITEM);
            } catch (DataValidationErrorException e) {
                GlobalVariables.getMessageMap().putError("courseId", SavedCourseListConstants.ERROR_KEY_DATA_VALIDATION_ERROR, e.getMessage());
            } catch (InvalidParameterException e) {
                GlobalVariables.getMessageMap().putError("courseId", SavedCourseListConstants.ERROR_KEY_INVALID_PARAM, e.getMessage());
            } catch (MissingParameterException e) {
                GlobalVariables.getMessageMap().putError("courseId", SavedCourseListConstants.ERROR_KEY_MISSING_PARAM, "courseId");
            } catch (OperationFailedException e) {
                GlobalVariables.getMessageMap().putError("courseId", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
            } catch (PermissionDeniedException e) {
                GlobalVariables.getMessageMap().putError("courseId", SavedCourseListConstants.ERROR_KEY_PERMISSION_DENIED);
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

        LearningPlan newPlan = getAcademicPlanService().createLearningPlan(plan, SavedCourseListConstants.CONTEXT_INFO);

        return newPlan;
    }

    @RequestMapping(params = "methodToCall=removeItem")
    public ModelAndView removePlanItem(@ModelAttribute("KualiForm") SavedCoursesListForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String planItemId = form.getPlanItemId();

        try {
            getAcademicPlanService().deletePlanItem(planItemId, SavedCourseListConstants.CONTEXT_INFO);
        } catch (DoesNotExistException e) {
            GlobalVariables.getMessageMap().putError("planItemId", SavedCourseListConstants.ERROR_KEY_UNKNOWN_PLAN_ITEM);
        } catch (InvalidParameterException e) {
            GlobalVariables.getMessageMap().putError("planItemId", SavedCourseListConstants.ERROR_KEY_INVALID_PARAM, e.getMessage());
        } catch (MissingParameterException e) {
            GlobalVariables.getMessageMap().putError("planItemId", SavedCourseListConstants.ERROR_KEY_INVALID_PARAM, "planItemId");
        } catch (OperationFailedException e) {
            GlobalVariables.getMessageMap().putError("planItemId", SavedCourseListConstants.ERROR_KEY_OPERATION_FAILED);
        } catch (PermissionDeniedException e) {
            GlobalVariables.getMessageMap().putError("planItemId", SavedCourseListConstants.ERROR_KEY_PERMISSION_DENIED);
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


