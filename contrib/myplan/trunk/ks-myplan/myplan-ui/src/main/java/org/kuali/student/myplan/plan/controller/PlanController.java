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
import org.kuali.student.myplan.course.service.CourseDetailsInquiryViewHelperServiceImpl;
import org.kuali.student.myplan.plan.form.PlanForm;
import org.kuali.student.myplan.course.util.PlanConstants;
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

    private transient CourseDetailsInquiryViewHelperServiceImpl courseDetailsInquiryService;

    @Override
    protected PlanForm createInitialForm(HttpServletRequest request) {
        return new PlanForm();
    }

    @RequestMapping(params = "methodToCall=startAddPlannedCourseForm")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);

        PlanForm planForm =  (PlanForm) form;

        String courseId = planForm.getCourseId();
        if (StringUtils.isEmpty(courseId)) {
           return doAddPlanItemError(planForm, "Could not initialize form because Course ID was missing.", null);
        }

        //  Initialize the form with a course Id.
        planForm.setCourseId(courseId);

        //  Also, add a full CourseDetails object so that course details properties are available to be displayed on the form.
        try {
            planForm.setCourseDetails(getCourseDetailsInquiryService().retrieveCourseDetails(planForm.getCourseId()));
        } catch(Exception e) {
            return doAddPlanItemError(planForm, "Could not initialize form because Course ID was unknown.", null);
        }

        return getUIFModelAndView(planForm);
    }

    @RequestMapping(params = "methodToCall=addPlannedCourse")
    public ModelAndView addPlannedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String courseId = form.getCourseId();
        if (StringUtils.isEmpty(courseId)) {
           return doAddPlanItemError(form, "Course ID was missing.", null);
        }
        String termIdString = form.getTermsList();
        if (StringUtils.isEmpty(termIdString)) {
            return doAddPlanItemError(form, "Term IDs were missing.", null);
        }
        String[] t = termIdString.split(",");
        //  Using LinkedList so that remove() is supported.
        List<String> newTermIds = new LinkedList<String>(Arrays.asList(t));
        if (newTermIds.isEmpty()) {
            return doAddPlanItemError(form, "Could not parse term IDs.", null);
        }

        //  Check for an "other" item in the terms list.
        if (newTermIds.contains(PlanConstants.OTHER_TERM_KEY)) {
            //  Remove the "other" item from the list.
            newTermIds.remove(newTermIds.indexOf(PlanConstants.OTHER_TERM_KEY));

            //  Create an ATP id from the values in the year and term fields.
            String year = form.getYear();
            if (StringUtils.isBlank(year)) {
                return doAddPlanItemError(form, "Could not construct ATP id for 'other' option because year was blank.", null);
            }

            String term = form.getTerm();
            if (StringUtils.isBlank(term)) {
                return doAddPlanItemError(form, "Could not construct ATP id for 'other' option because term was blank.", null);
            }

            String newTermId = PlanConstants.TERM_ID_PREFIX + term + year;
            newTermIds.add(newTermId);
        }

        /*
         *  Before attempting to add a plan item, query for plan items for the requested course id. If a plan item of type
         *  saved course already exists then make this operation an update which adds in any new ATP ids.
         */
        Person user = GlobalVariables.getUserSession().getPerson();
        String studentId = user.getPrincipalId();

        LearningPlan plan = null;
        try {
            //  If something goes wrong with the query then a RuntimeException will be thrown. Otherwise, the method
            //  will return the default plan or null. Having multiple plans will also produce a RuntimeException.
            plan = getLearningPlan(studentId);
        } catch(RuntimeException e) {
            return doAddPlanItemError(form, "Query for default learning plan failed.", e);
        }

        /*
         *  Create a default learning plan if there isn't one already and skip querying for plan items.
         */
        PlanItem planItem = null;
        if (plan == null) {
            try {
                plan = createDefaultLearningPlan(studentId);
            } catch (Exception e) {
                return doAddPlanItemError(form, "Unable to create learning plan.", e);
            }
        } else {
            /* Check for an existing plan item for the given course id (refObjectId). The planItems list should contain
             * elements for a single course (refObjectId) and there should be only one item of planned course, so the
             * iterator which adds ATP ids can break once that item is processed.
             */
            List<PlanItemInfo> planItems = null;
            try {
                planItems = getAcademicPlanService().getPlanItemsInPlanByRefObjectIdByRefObjectType(plan.getId(), courseId,
                        LUConstants.CLU_TYPE_CREDIT_COURSE, PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                return doAddPlanItemError(form, "Unable to fetch plan items.", e);
            }

            /*
             *  Add the new ATP ids to the planned course.
             */
            for (PlanItemInfo pii : planItems) {
                if (pii.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
                    List<String> existingTermIds = pii.getPlanPeriods();
                    //  If the existing and new term ids have no overlap just add them all.
                    //  Otherwise, only add the non-duplicates.
                    if (Collections.disjoint(existingTermIds, newTermIds)) {
                        pii.getPlanPeriods().addAll(newTermIds);
                        planItem = pii;
                    } else {
                        //  Make a new list of all term Ids, then clobber any dups by putting them into a set. Then
                        //  add them to the plan item info.
                        List<String> allTermIds = new ArrayList<String>(existingTermIds);
                        allTermIds.addAll(newTermIds);
                        pii.setPlanPeriods(new ArrayList<String>(new HashSet<String>(allTermIds)));
                        planItem = pii;
                    }

                    if (planItem != null) {
                        break;
                    }
                }
            }
        }

        //  If an update-able plan item was found then go ahead and do an update. Otherwise, do an add.
        if (planItem != null) {
            try {
                academicPlanService.updatePlanItem(planItem.getId(), (PlanItemInfo) planItem, PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                return doAddPlanItemError(form, "Unable to update plan item.", e);
            }
        } else {
            try {
                 planItem = addPlanItem(plan, courseId, newTermIds, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);
            } catch(Exception e) {
                 return doAddPlanItemError(form, "Unable to add plan item.", e);
            }
        }

        //  Pass the IDs of the updated items back to the UI.
        form.setPlanItemId(planItem.getId());
        form.setCourseId(planItem.getRefObjectId());

        //  TODO: Hold on this for now. Unclear how meta data gets updated.
        //  Update the timestamp on the plan.
        //try {
        //    academicPlanService.updateLearningPlan(plan.getId(), (LearningPlanInfo) plan, PlanConstants.CONTEXT_INFO);
        //} catch (Exception e) {
        //    logger.error("Unable to update the plan.", e);
        //}

        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_ADD_PAGE_ID);
    }

    @RequestMapping(params = "methodToCall=addSavedCourse")
    public ModelAndView addSavedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                       HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        Person user = GlobalVariables.getUserSession().getPerson();
        String studentId = user.getPrincipalId();
        String courseId = form.getCourseId();

        LearningPlan plan = null;
        try {
            //  Throws RuntimeException is there is a problem. Otherwise, returns a plan or null.
            plan = getLearningPlan(studentId);
        } catch(RuntimeException e) {
            return doAddPlanItemError(form, "Query for default learning plan failed.", e);
        }

        /*
         *  Create a default learning plan if there isn't one already and skip querying for plan items.
         */
        if (plan == null) {
            try {
                plan = createDefaultLearningPlan(studentId);
            } catch (Exception e) {
                 return doAddPlanItemError(form, "Unable to create learning plan.", e);
            }
        }

        PlanItem item = null;
        try {
            item = addPlanItem(plan, courseId, null, PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        } catch(Exception e) {
             return doAddPlanItemError(form, "Unable to add plan item.", e);
        }

        form.setPlanItemId(item.getId());
        form.setCourseId(item.getRefObjectId());

        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_ADD_PAGE_ID);
    }

    /**
     *  Blow up on failed plan adds.
     */
    private ModelAndView doAddPlanItemError(PlanForm form, String errorMessage, Exception e) {
        if (e != null) {
            logger.error(errorMessage, e);
        } else {
            logger.error(errorMessage);
        }
        GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_ADD_SECTION_ID, PlanConstants.ERROR_KEY_OPERATION_FAILED);
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_ADD_PAGE_ID);
    }

    /**
     * Adds a plan item for the given course id and ATPs.
     *
     * @param plan  The learning plan to add the item to.
     * @param courseId  The id of the course.
     * @param termIds A list of ATP/term ids if the plan item is a planned course.
     * @param planItemType  Saved couse or planned course.
     * @return  The newly created plan item or the existing plan item where a plan item already exists for the given course.
     * @throws RuntimeException on errors.
     */
    protected PlanItem addPlanItem(LearningPlan plan, String courseId, List<String> termIds, String planItemType) {

        if (StringUtils.isEmpty(courseId)) {
            // TODO: DO ERROR.
            throw new RuntimeException("Empty Course ID");
        }

        PlanItem newPlanItem = null;

        PlanItemInfo pii = new PlanItemInfo();
        pii.setLearningPlanId(plan.getId());
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
            //  The course id was already in the saved courses list.
            logger.warn("This item was a duplicate. Fetching the existing plan item.", e);
            newPlanItem = getPlanItemByCourseIdAndType(courseId, planItemType);
        } catch (Exception e) {
            throw new RuntimeException("Could not create plan item.", e);
        }

        return newPlanItem;
    }

    /*
     *  If the saved course item couldn't be added because the course already exists then lookup the id of the existing
     *  plan item.
     *  @return Returns a plan item if one is found for the given courseId. Otherwise, returns null.
     *  @throws RuntimeException on errors.
     */
    protected PlanItem getPlanItemByCourseIdAndType(String courseId, String planItemType) {

        Person user = GlobalVariables.getUserSession().getPerson();
        String studentId = user.getPrincipalId();
        LearningPlan learningPlan = getLearningPlan(studentId);
        if (learningPlan == null) {
            throw new RuntimeException("Could not find the default plan for [%].");
        }

        List<PlanItemInfo> planItems = null;
        PlanItem item = null;

        try {
            planItems = getAcademicPlanService().getPlanItemsInPlanByType(learningPlan.getId(), planItemType, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        if (planItems == null || planItems.isEmpty()) {
            throw new RuntimeException("Could not retrieve plan items.");
        } else {
            for (PlanItem p : planItems) {
                if (p.getRefObjectId().equals(courseId)) {
                    item = p;
                    break;
                }
            }
        }
        //  A null here means that the duplicate plan item couldn't be found.
        return item;
    }

    /**
     * Retrieve a student's LearningPlan.
     *
     * @param studentId
     * @return A LearningPlan or null on errors.
     * @throws RuntimeException if the query fails.
     */
    private LearningPlan getLearningPlan(String studentId) {
        /*
         *  First fetch the student's learning plan.
         */
        List<LearningPlanInfo> learningPlans = null;
        try {
            learningPlans = getAcademicPlanService().getLearningPlansForStudentByType(studentId,
                    PlanConstants.LEARNING_PLAN_TYPE_PLAN, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not fetch plan for user [%s].", studentId), e);
        }

        if (learningPlans == null) {
            throw new RuntimeException(String.format("Could not fetch plan for user [%s]. The query returned null.", studentId));
        }

        //  There should currently only be a single learning plan. This may change in the future.
        if (learningPlans.size() > 1) {
            throw new RuntimeException(String.format("User [%s] has more than one plan.", studentId));
        }

        LearningPlan learningPlan = null;
        if (learningPlans.size() != 0) {
            learningPlan = learningPlans.get(0);
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
            GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_REMOVE_SECTION_ID, PlanConstants.ERROR_KEY_OPERATION_FAILED);
        }

        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_REMOVE_PAGE_ID);
    }
    @RequestMapping(params = "methodToCall=populateMenuItems")
    public ModelAndView populateMenuItems(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);

        PlanForm planForm =  (PlanForm) form;

        String courseId = planForm.getCourseId();
        if (StringUtils.isEmpty(courseId)) {
            return doAddPlanItemError(planForm, "Could not initialize form because Course ID was missing.", null);
        }

        //  Initialize the form with a course Id.
        planForm.setCourseId(courseId);

        return getUIFModelAndView(planForm);
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