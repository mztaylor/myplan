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
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.datadictionary.exception.DuplicateEntryException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryViewHelperServiceImpl;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.form.PlanForm;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(value = "/plan")
public class PlanController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(PlanController.class);

    private transient AcademicPlanService academicPlanService;

    private transient CourseDetailsInquiryViewHelperServiceImpl courseDetailsInquiryService;

    //  Java to JSON outputter.
    private transient ObjectMapper mapper = new ObjectMapper();

    // Used for gettign the term and year from Atp
    private transient AtpHelper atpHelper;

    @Override
    protected PlanForm createInitialForm(HttpServletRequest request) {
        return new PlanForm();
    }



    @RequestMapping(params = "methodToCall=startAddPlannedCourseForm")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);

        PlanForm planForm = (PlanForm) form;
        // First load the plan item and retrieve the courseId
        PlanItemInfo planItem = null;

        //TODO: find and remove any reference to courseId being passed in to PlanForm
        String courseId = null;
        if (planForm.getPlanItemId() != null) {
            try {
                planItem = getAcademicPlanService().getPlanItem(planForm.getPlanItemId(), PlanConstants.CONTEXT_INFO);
                courseId = planItem.getRefObjectId();
                planForm.setDateAdded(planItem.getMeta().getCreateTime().toString());
                String[] splitStr = getAtpHelper().getTermAndYear(planItem.getPlanPeriods().get(0));
                planForm.setTerm(splitStr[0]);
                planForm.setYear(splitStr[1]);

                //Following data used for the Dialog boxes
                if (planItem.getTypeKey().equalsIgnoreCase(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
                    planForm.setBackup(true);
                }

            } catch (Exception e) {
                logger.error("PlanItem not found");
            }
        } else {
            planItem = new PlanItemInfo();
            courseId = planForm.getCourseId();
        }

        //TODO: Clean up with courseId removal
        if (StringUtils.isEmpty(courseId)) {
            return doPlanActionError(planForm, "Could not initialize form because Course ID was missing.", null);
        }


        if (planForm.getDateAdded() != null) {
            String dateStr = planForm.getDateAdded().substring(0, 10);
            DateFormat dfYMD =
                    new SimpleDateFormat("yyyy-MM-dd");
            DateFormat dfDMY =
                    new SimpleDateFormat("dd/MM/yyyy");

            try {
                dateStr = dfDMY.format(dfYMD.parse(dateStr));
            } catch (Exception e) {
                logger.error("Cant parse date");
                return doPlanActionError(planForm, "Could not retrieve Plan date information.", null);
            }
            planForm.setDateAdded(dateStr);
        }
        //  Initialize the form with a course Id.
        planForm.setCourseId(courseId);

        //  Also, add a full CourseDetails object so that course details properties are available to be displayed on the form.
        try {
            planForm.setCourseDetails(getCourseDetailsInquiryService().retrieveCourseDetails(planForm.getCourseId()));
        } catch (Exception e) {
            return doPlanActionError(planForm, "Could not retrieve Course Details.", null);
        }

        return getUIFModelAndView(planForm);
    }

    @RequestMapping(params = "methodToCall=plannedToBackup")
    public ModelAndView plannedToBackup(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                        HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String planItemId = form.getPlanItemId();
        if (StringUtils.isEmpty(planItemId)) {
            return doPlanActionError(form, "Plan Item ID was missing.", null);
        }

        //  Verify the type is planned, change to backup, update, make events (delete, add, update credits).
        PlanItemInfo planItem = null;
        try {
            // First load the plan item and retrieve the courseId
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doPlanActionError(form, "Could not fetch plan item.", e);
        }

        //  Verify that the plan item type is "planned".
        if (!planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
            return doPlanActionError(form, "Move planned item was not type planned.", null);
        }

        //  Set type to "backup".
        planItem.setTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);

        //  Update
        try {
            getAcademicPlanService().updatePlanItem(planItemId, planItem, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doPlanActionError(form, "Could not update plan item.", e);
        }

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);

        //  Make events (delete, add, update credits).
        //  Set the javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        String termId = planItem.getPlanPeriods().get(0);
        String typeKey = planItem.getTypeKey();

        //  Make a delete event.  /* atpId, type, courseId */
        Map<String, String> jsDeleteEventParams = new HashMap<String, String>();
        //  TODO: FIXME: Assuming one ATP per plan item here. Add planned course actually supports multiples.
        jsDeleteEventParams.put("atpId", formatAtpIdForUI(termId));
        jsDeleteEventParams.put("planItemType", formatTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED));
        jsDeleteEventParams.put("planItemId", planItemId);
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, jsDeleteEventParams);

        //  Make an add event.
        Map<String, String> addPlannedItemEventParams = new HashMap<String, String>();
        addPlannedItemEventParams.put("planItemId", planItem.getId());
        addPlannedItemEventParams.put("planItemType", formatTypeKey(typeKey));
        //  TODO: FIXME: Assuming one ATP per plan item here. Add planned course actually supports multiples.
        addPlannedItemEventParams.put("atpId", formatAtpIdForUI(termId));
        addPlannedItemEventParams.put("courseDetails", getCourseDetailsAsJson(planItem.getRefObjectId()));
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, addPlannedItemEventParams);

        //  Make an "Update total credits".
        Map<String, String> updateCreditsEventParams = new HashMap<String, String>();
        updateCreditsEventParams.put("atpId", formatAtpIdForUI(termId));
        String totalCredits = this.getTotalCredits(termId);
        updateCreditsEventParams.put("totalCredits", totalCredits );
        events.put(PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS, updateCreditsEventParams);

        form.setJavascriptEvents(events);

        return doPlanActionSuccess(form);
    }

    @RequestMapping(params = "methodToCall=backupToPlanned")
    public ModelAndView backupToPlanned(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                        HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String planItemId = form.getPlanItemId();
        if (StringUtils.isEmpty(planItemId)) {
            return doPlanActionError(form, "Plan Item ID was missing.", null);
        }

        //  Verify type backup, change to planned, update, make events (delete, add, update credits).
        PlanItemInfo planItem = null;
        try {
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doPlanActionError(form, "Could not fetch plan item.", e);
        }

        //  Verify that the plan item type is "backup".
        if (!planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            return doPlanActionError(form, "Move planned item was not type backup.", null);
        }

        //  Set type to "planned".
        planItem.setTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);

        //  Update
        try {
            getAcademicPlanService().updatePlanItem(planItemId, planItem, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doPlanActionError(form, "Could not udpate plan item.", e);
        }

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);

        //  Make events (delete, add, update credits).
        //  Set the javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new HashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        String termId = planItem.getPlanPeriods().get(0);
        String typeKey = planItem.getTypeKey();

        //  Make a delete event.  /* atpId, type, courseId */
        Map<String, String> jsDeleteEventParams = new HashMap<String, String>();
        //  TODO: FIXME: Assuming one ATP per plan item here. Add planned course actually supports multiples.
        jsDeleteEventParams.put("atpId", formatAtpIdForUI(termId));
        jsDeleteEventParams.put("planItemType", formatTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP));
        jsDeleteEventParams.put("planItemId", planItemId);
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, jsDeleteEventParams);

        //  Make an add event.
        Map<String, String> addPlannedItemEventParams = new HashMap<String, String>();
        addPlannedItemEventParams.put("planItemId", planItem.getId());
        addPlannedItemEventParams.put("planItemType", formatTypeKey(typeKey));
        //  TODO: FIXME: Assuming one ATP per plan item here. Add planned course actually supports multiples.
        addPlannedItemEventParams.put("atpId", formatAtpIdForUI(termId));
        addPlannedItemEventParams.put("courseDetails", getCourseDetailsAsJson(planItem.getRefObjectId()));
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, addPlannedItemEventParams);

        //  Make an "Update total credits".
        Map<String, String> updateCreditsEventParams = new HashMap<String, String>();
        updateCreditsEventParams.put("atpId", formatAtpIdForUI(termId));
        String totalCredits = this.getTotalCredits(termId);
        updateCreditsEventParams.put("totalCredits",  totalCredits );
        events.put(PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS, updateCreditsEventParams);

        form.setJavascriptEvents(events);

        return doPlanActionSuccess(form);
    }

    @RequestMapping(params = "methodToCall=movePlannedCourse")
    public ModelAndView movePlannedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                          HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        /**
         * This method needs a Plan Item ID and an ATP ID.
         */
        String planItemId = form.getPlanItemId();
        if (StringUtils.isEmpty(planItemId)) {
            return doPlanActionError(form, "Plan Item ID was missing.", null);
        }

        //  This is the new/destination ATP.
        String newAtpId = form.getAtpId();
        //  Further validation of ATP IDs will happen in the service validation methods.
        if (StringUtils.isEmpty(newAtpId)) {
            return doPlanActionError(form, "ATP ID was missing.", null);
        }

        //  Should the course be type 'planned' or 'backup'. Default to planned.
        boolean backup = form.isBackup();

        String newType = PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED;
        if (backup) {
            newType = PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP;
        }

         //  This list can only contain one item, otherwise the backend validation will fail.
        //  Use LinkedList here so that the remove method works during "other" option processing.
        List<String> newAtpIds = null;
        try {
            newAtpIds = getNewTermIds(newAtpId, form);
        } catch (RuntimeException e) {
            return doPlanActionError(form, "Unable to process request.", e);
        }

        PlanItemInfo planItem = null;
        try {
            // First load the plan item and retrieve the courseId
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doPlanActionError(form, "Could not fetch plan item.", e);
        }

        if (planItem == null) {
            return doPlanActionError(form, String.format("Could not fetch plan item."), null);
        }

          //  Lookup course details as they will be needed for errors.
        CourseDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseDetails(planItem.getRefObjectId());
        } catch (Exception e) {
            return doPlanActionError(form, "Unable to retrieve Course Details.", null);
        }

        //  Make sure there isn't a plan item for the same course id in the destination ATP.
        PlanItemInfo existingPlanItem = null;
        try {
            existingPlanItem = getPlannedOrBackupPlanItem(planItem.getRefObjectId(), newAtpIds.get(0));
        } catch(RuntimeException e) {
            return doPlanActionError(form, String.format("Query for existing plan item failed."), null);
        }

        if (existingPlanItem != null) {
             GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID,
                PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, courseDetails.getCode(), formatAtpIdForUI(newAtpIds.get(0)));
            return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
        }

        //  Create events before updating the plan item.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> originalRemoveEvents = makeRemoveEvent(planItem);
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> originalUpdateTotalCredits = makeUpdateTotalCreditsEvent(planItem);

        //  Do validations.
        //  Validate: Plan Size exceeded.
        boolean hasCapacity = false;
        try {
            hasCapacity = isAtpHasCapacity(getLearningPlan(getUserId()), newAtpIds.get(0), newType);
        } catch(RuntimeException e) {
            return doPlanActionError(form, "Could not validate capacity for new plan item.", e);
        }
        if (! hasCapacity) {
            return doPlanCapacityExceededError(form);
        }

        //  Validate: Adding to historical term.
        if (isTermHistorical(newAtpIds.get(0))) {
            GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID,
                    PlanConstants.ERROR_KEY_HISTORICAL_ATP);
            return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
        }

        //  Update the plan item.
        planItem.setPlanPeriods(newAtpIds);
        planItem.setTypeKey(newType);

        try {
            getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doPlanActionError(form, "Could not udpate plan item.", e);
        }

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);

        //  Make Javascript UI events (delete, add, update credits).
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        //  Add events generated for the plan item before it was updated.
        events.putAll(originalRemoveEvents);
        events.putAll(originalUpdateTotalCredits);
        try {
            events.putAll(makeAddEvent(planItem, courseDetails));
        } catch(RuntimeException e) {
            return doPlanActionError(form, "Unable to create add event.", e);
        }
        events.putAll(makeUpdateTotalCreditsEvent(planItem));

        form.setJavascriptEvents(events);

        return doPlanActionSuccess(form);
    }

    @RequestMapping(params = "methodToCall=addPlannedCourse")
    public ModelAndView addPlannedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        /**
         * This method needs a Course ID and an ATP ID.
         */
        String courseId = form.getCourseId();
        if (StringUtils.isEmpty(courseId)) {
            return doPlanActionError(form, "Course ID was missing.", null);
        }

        String atpId = form.getAtpId();
        //  Further validation of ATP IDs will happen in the service validation methods.
        if (StringUtils.isEmpty(atpId)) {
            return doPlanActionError(form, "ATP ID was missing.", null);
        }

        //  Should the course be type 'planned' or 'backup'. Default to planned.
        boolean backup = form.isBackup();
        String newType = PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED;
        if (backup) {
            newType = PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP;
        }

        //  This list can only contain one item, otherwise the backend validation will fail.
        //  Use LinkedList here so that the remove method works during "other" option processing.
        List<String> newAtpIds = null;
        try {
            newAtpIds = getNewTermIds(atpId, form);
        } catch (RuntimeException e) {
            return doPlanActionError(form, "Unable to process request.", e);
        }

        String studentId = getUserId();

        LearningPlan plan = null;
        try {
            //  If something goes wrong with the query then a RuntimeException will be thrown. Otherwise, the method
            //  will return the default plan or null. Having multiple plans will also produce a RuntimeException.
            plan = getLearningPlan(studentId);
        } catch (RuntimeException e) {
            return doPlanActionError(form, "Query for default learning plan failed.", e);
        }

        /*
         *  Create a default learning plan if there isn't one already and skip querying for plan items.
         */
        // TODO: There is a potential (small) for multiple plan's created in this model coz of multi threading. There should be a check
        // at the db level to restrict a single plan of a given type to a student
        if (plan == null) {
            try {
                plan = createDefaultLearningPlan(studentId);
            } catch (Exception e) {
                return doPlanActionError(form, "Unable to create learning plan.", e);
            }
        }

        //  Lookup course details as well need them in case there is an error below.
        CourseDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseDetails(courseId);
        } catch (Exception e) {
            return doPlanActionError(form, "Unable to retrieve Course Details.", null);
        }

        /*  Do validations. */

        //  Plan Size exceeded.
        boolean hasCapacity = false;
        try {
            hasCapacity = isAtpHasCapacity(plan, newAtpIds.get(0), newType);
        } catch(RuntimeException e) {
            return doPlanActionError(form, "Could not validate capacity for new plan item.", e);
        }

        if (! hasCapacity) {
            return doPlanCapacityExceededError(form);
        }

        //  Validate: Adding to historical term.
        //  TODO: isTermHistorical() only returns false at the moment.
        if (isTermHistorical(newAtpIds.get(0))) {
            GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID,
                    PlanConstants.ERROR_KEY_HISTORICAL_ATP);
            return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
        }

        //  See if a wishlist item exists for the course. If so, then update it. Otherwise create a new plan item.
        PlanItemInfo planItem = getWishlistPlanItem(courseId);
        //  Storage for wishlist events.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> wishlistEvents = null;
        //  Create a new plan item if no wishlist exists. Otherwise, update the wishlist item.
        if (planItem == null) {
            try {
                planItem = addPlanItem(plan, courseId, newAtpIds, newType);
            } catch (DuplicateEntryException e) {
                return doDuplicatePlanItem(form, formatAtpIdForUI(newAtpIds.get(0)), courseDetails);
            } catch (Exception e) {
                return doPlanActionError(form, "Unable to add plan item.", e);
            }
        } else {
            //  Create wishlist events before updating the plan item.
            wishlistEvents = makeRemoveEvent(planItem);
            planItem.setTypeKey(newType);
            planItem.setPlanPeriods(newAtpIds);
            try {
                planItem = getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                return doPlanActionError(form, "Unable to update wishlist plan item.", e);
            }
        }

        //  Create the map of javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        //  If a wishlist item was clobbered then generate Javascript events.
        if (wishlistEvents != null) {
            events.putAll(wishlistEvents);
        }

        try {
            events.putAll(makeAddEvent(planItem, courseDetails));
        } catch(RuntimeException e) {
            return doPlanActionError(form, "Unable to create add event.", e);
        }

        events.putAll(makeUpdateTotalCreditsEvent(planItem));

        //  Populate the form.
        form.setJavascriptEvents(events);

        //  TODO: Hold on this for now. Unclear how meta data gets updated.
        //  Update the timestamp on the plan.
        //try {
        //    academicPlanService.updateLearningPlan(plan.getId(), (LearningPlanInfo) plan, PlanConstants.CONTEXT_INFO);
        //} catch (Exception e) {
        //    logger.error("Unable to update the plan.", e);
        //}
        return doPlanActionSuccess(form);
    }

    /**
     * Determines if the given ATP ID is before the current ATP.
     * @param atpId
     * @return Returns true if the ATP is historical. Otherwise, false.
     */
    private boolean isTermHistorical(String atpId) {
        // TODO: Logic needs to be more complex.
        return false;
    }

    /**
     * Gets a list of term ids taking into account "other" items. Currently this list should only contain a single item.
     * @param atpId
     * @return
     */
    private List<String> getNewTermIds(String atpId, PlanForm form) {
        List<String> newTermIds = new LinkedList<String>();
        newTermIds.add(atpId);

        //  Check for an "other" item in the terms list and assemble an ATP ID from the year and term fields.
        if (newTermIds.contains(PlanConstants.OTHER_TERM_KEY)) {
            //  Remove the "other" item from the list.
            newTermIds.remove(newTermIds.indexOf(PlanConstants.OTHER_TERM_KEY));

            //  Create an ATP id from the values in the year and term fields.
            String year = form.getYear();
            if (StringUtils.isBlank(year)) {
                throw new RuntimeException("Could not construct ATP id for 'other' option because year was blank.");
            }

            String term = form.getTerm();
            if (StringUtils.isBlank(term)) {
                throw new RuntimeException("Could not construct ATP id for 'other' option because term was blank.");
            }

            newTermIds.add(getAtpHelper().getAtpFromYearAndTerm(term, year));
        }
        return newTermIds;
    }

    /**
     * Determines if a plan has capacity in within a particular ATP for adding a new plan item of a specific type.
     *
     * @param plan
     * @param atpId
     * @param typeKey
     * @return True if the item can be added or false if not.
     * @throws RuntimeException if things go wrong.
     */
    private boolean isAtpHasCapacity(LearningPlan plan, String atpId, String typeKey) {
        if (plan == null) {
            throw new RuntimeException("Plan was NULL.");
        }

        if (StringUtils.isEmpty(atpId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        List<PlanItemInfo> planItems = null;
        PlanItem item = null;
        try {
            planItems = getAcademicPlanService().getPlanItemsInPlanByType(plan.getId(), typeKey, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        int counter = 0;
        if (planItems == null || planItems.isEmpty()) {
            throw new RuntimeException("Could not retrieve plan items.");
        } else {
            for (PlanItem p : planItems) {
                if (p.getRefObjectId().equals(atpId)) {
                    counter++;
                }
            }
        }

        if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            return (counter >= PlanConstants.BACKUP_PLAN_ITEM_CAPACITY) ? false : true;
        } else if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
            return (counter >= PlanConstants.PLANNED_PLAN_ITEM_CAPACITY) ? false : true;
        }

        throw new RuntimeException(String.format("Unknown plan item type [%s].", typeKey));
    }

    @RequestMapping(params = "methodToCall=addSavedCourse")
    public ModelAndView addSavedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                       HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String studentId = getUserId();
        String courseId = form.getCourseId();

        LearningPlan plan = null;
        try {
            //  Throws RuntimeException is there is a problem. Otherwise, returns a plan or null.
            plan = getLearningPlan(studentId);
        } catch (RuntimeException e) {
            return doPlanActionError(form, "Query for default learning plan failed.", e);
        }

        /*
         *  Create a default learning plan if there isn't one already and skip querying for plan items.
         */
        if (plan == null) {
            try {
                plan = createDefaultLearningPlan(studentId);
            } catch (Exception e) {
                return doPlanActionError(form, "Unable to create learning plan.", e);
            }
        }

        //  Grab course details.
        CourseDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseDetails(courseId);
        } catch (Exception e) {
            return doPlanActionError(form, String.format("Unable to retrieve Course Details for [%s].", courseId), e);
        }

        PlanItemInfo planItem = null;
        try {
            planItem = addPlanItem(plan, courseId, null, PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        } catch(DuplicateEntryException e) {
            return doDuplicatePlanItem(form, null, courseDetails);
        } catch(Exception e) {
            return doPlanActionError(form, "Unable to add plan item.", e);
        }

        //  Create events
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        events.putAll(makeAddEvent(planItem, courseDetails));

        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
        form.setJavascriptEvents(events);

        return doPlanActionSuccess(form);
    }

    /**
     * Blow up response of the plan capacity validation fails.
     * @param form
     * @return
     */
    private ModelAndView doPlanCapacityExceededError(PlanForm form) {
        String errorId = PlanConstants.ERROR_KEY_PLANNED_ITEM_CAPACITY_EXCEEDED;
        if (form.isBackup()) {
            errorId = PlanConstants.ERROR_KEY_BACKUP_ITEM_CAPACITY_EXCEEDED;
        }
        GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, errorId);
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doPlanActionError(PlanForm form, String errorMessage, Exception e) {
        form.setRequestStatus(PlanForm.REQUEST_STATUS.FAILURE);
        if (e != null) {
            logger.error(errorMessage, e);
        } else {
            logger.error(errorMessage);
        }
        GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.ERROR_KEY_OPERATION_FAILED);
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doDuplicatePlanItem(PlanForm form, String atpId, CourseDetails courseDetails) {
         GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID,
         PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, courseDetails.getCode(), atpId);
         return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doPlanActionSuccess(PlanForm form) {
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
        GlobalVariables.getMessageMap().putInfoForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.SUCCESS_KEY);
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    private String getCourseDetailsAsJson(String courseId) {
        //  Also, add a full CourseDetails object so that course details properties are available to be displayed on the form.
        CourseDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseDetails(courseId);
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve Course Details.", e);
        }

        String courseDetailsAsJson;
        try {
            //  Turn the list of javascript events into a string of JSON.
            courseDetailsAsJson = mapper.writeValueAsString(courseDetails);
        } catch (Exception e) {
            throw new RuntimeException("Could not convert javascript events to JSON.", e);
        }

        return courseDetailsAsJson;
    }

    /**
     * Adds a plan item for the given course id and ATPs.
     *
     * @param plan         The learning plan to add the item to.
     * @param courseId     The id of the course.
     * @param termIds      A list of ATP/term ids if the plan item is a planned course.
     * @param planItemType Saved couse or planned course.
     * @return The newly created plan item or the existing plan item where a plan item already exists for the given course.
     * @throws RuntimeException on errors.
     */
    protected PlanItemInfo addPlanItem(LearningPlan plan, String courseId, List<String> termIds, String planItemType)
        throws DuplicateEntryException {

        if (StringUtils.isEmpty(courseId)) {
            throw new RuntimeException("Empty Course ID");
        }

        PlanItemInfo newPlanItem = null;

        PlanItemInfo pii = new PlanItemInfo();
        pii.setLearningPlanId(plan.getId());
        pii.setTypeKey(planItemType);
        pii.setRefObjectType(PlanConstants.COURSE_TYPE);
        pii.setRefObjectId(courseId);

        pii.setStateKey(PlanConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted("");
        rti.setPlain("");
        pii.setDescr(rti);

        if (null != termIds) {
            pii.setPlanPeriods(termIds);
        }

        //  Make sure no dups exist
        if (planItemType.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
            if (getWishlistPlanItem(courseId) != null) {
                throw new DuplicateEntryException("Duplicate plan item exists.");
            }
        } else {
            if (getPlannedOrBackupPlanItem(courseId, termIds.get(0)) != null) {
                throw new DuplicateEntryException("Duplicate plan item exists.");
            }
        }

        try {
            newPlanItem = getAcademicPlanService().createPlanItem(pii, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException("Could not create plan item.", e);
        }

        return newPlanItem;
    }


    /**
     * Gets a Plan Item of type "wishlist" for a particular course. There should only ever be one.
     * @param courseId The id of the course.
     * @return A PlanItem of type wishlist.
     */
    protected PlanItemInfo getWishlistPlanItem(String courseId) {
        if (StringUtils.isEmpty(courseId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        String studentId = getUserId();
        LearningPlan learningPlan = getLearningPlan(studentId);
        if (learningPlan == null) {
            throw new RuntimeException(String.format("Could not find the default plan for [%s].", studentId));
        }

        List<PlanItemInfo> planItems = null;
        PlanItemInfo item = null;

        try {
            planItems = getAcademicPlanService().getPlanItemsInPlanByType(learningPlan.getId(),
                    PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        if (planItems == null || planItems.isEmpty()) {
            throw new RuntimeException("Could not retrieve plan items.");
        } else {
            for (PlanItemInfo p : planItems) {
                if (p.getRefObjectId().equals(courseId)) {
                    item = p;
                    break;
                }
            }
        }
        //  A null here means that no plan item exists for the given course ID.
        return item;
    }


    /**
     * Gets a Plan Item of type "planned" or "backup" for a particular course and ATP ID. Since we are enforcing a
     * data constraint of one "planned" or "backup" plan item per ATP ID this method only returns a single plan item.
     *
     * @param courseId
     * @return A "planned" or "backup" plan item. Or 'null' if none exists.
     * @throws RuntimeException on errors.
     */
    protected PlanItemInfo getPlannedOrBackupPlanItem(String courseId, String atpId) {
        if (StringUtils.isEmpty(courseId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        if (StringUtils.isEmpty(atpId)) {
            throw new RuntimeException("ATP Id was empty.");
        }

        String studentId = getUserId();
        LearningPlan learningPlan = getLearningPlan(studentId);
        if (learningPlan == null) {
            throw new RuntimeException(String.format("Could not find the default plan for [%s].", studentId));
        }

        List<PlanItemInfo> planItems = null;
        PlanItemInfo item = null;

        try {
            planItems = getAcademicPlanService().getPlanItemsInPlanByType(learningPlan.getId(),
                    PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        if (planItems == null || planItems.isEmpty()) {
            throw new RuntimeException("Could not retrieve plan items.");
        } else {
            for (PlanItemInfo p : planItems) {
                //  Make sure some ATP IDs exist on the plan item. There should never be more than one ATP ID however.
                List<String> planPeriods = p.getPlanPeriods();
                if (p.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)
                        || planPeriods == null || planPeriods.size() == 0) {
                    continue;
                }
                //  Throw an error if a plan item has more than one ATP ID associated with it.
                if (planPeriods.size() > 1) {
                    throw new RuntimeException(String.format("Learning Plan [%s] plan id [%s] has more than one associated ATP ID.",
                            learningPlan.getId(), p.getId()));
                }

                String atp = planPeriods.get(0);
                if (p.getRefObjectId().equals(courseId) && atp.equals(atpId)) {
                    item = p;
                    break;
                }
            }
        }
        //  A null here means that no plan item exists for the given course and ATP IDs.
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
            //TODO: Once all courseId dependencies are removed from form, delete this section
            PlanItemInfo planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
            String courseId = planItem.getRefObjectId();
            form.setCourseId(courseId);

            // Now Delete the plan item
            getAcademicPlanService().deletePlanItem(planItemId, PlanConstants.CONTEXT_INFO);

            //  Set the status of the request for the UI.
            form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
            form.setJavascriptEvents(makeRemoveEvent(planItem));


            //  Set success text.
            GlobalVariables.getMessageMap().putInfoForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.SUCCESS_KEY);
        } catch (DoesNotExistException e) {
            //  Assume the end-user already deleted this item and silently let this error go. Log it though.
            logger.warn("Tried to delete a plan item that doesn't exist.", e);
            //  Set the status of the request for the UI.
            form.setRequestStatus(PlanForm.REQUEST_STATUS.NOOP);
            //  Set the success message as well as a warning which explains why the request didn't complete successfully.
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putInfoForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.SUCCESS_KEY);
            GlobalVariables.getMessageMap().putWarningForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.ERROR_KEY_UNKNOWN_PLAN_ITEM);
        } catch (Exception e) {
            //  Give the end-user a generic error message, but log the exception.
            logger.error("Could not delete plan item.", e);
            form.setRequestStatus(PlanForm.REQUEST_STATUS.FAILURE);
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.ERROR_KEY_OPERATION_FAILED);
        }

        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    /**
     * Creates events map for a remove.
     *
     * @param planItem
     * @return
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeRemoveEvent(PlanItemInfo planItem) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        Map<String, String> params = new HashMap<String, String>();

        //  Only planned or backup items get an atpId attribute.
        if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) ||
                planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            params.put("atpId", formatAtpIdForUI(planItem.getPlanPeriods().get(0)));
        }
        params.put("planItemType", formatTypeKey(planItem.getTypeKey()));
        params.put("planItemId", planItem.getId());
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, params);
        return events;
    }

    /**
     * Creates an update credits event.
     * @param planItem
     * @return
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeUpdateTotalCreditsEvent(PlanItemInfo planItem) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        //  Only planned items need this event.
        if (! planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
            return events;
        }

        Map<String, String> params = new HashMap<String, String>();
        String termId = (planItem.getPlanPeriods() == null || planItem.getPlanPeriods().size() == 0) ? "" : planItem.getPlanPeriods().get(0);

        params.put("atpId", formatAtpIdForUI(termId));
        String totalCredits = this.getTotalCredits(termId);
        params.put("totalCredits", totalCredits);

        return events;
    }

    /**
     * Creates an add plan item event.
     * @param planItem
     * @param courseDetails
     * @return
     * @throws RuntimeException if anything goes wrong.
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeAddEvent(PlanItemInfo planItem, CourseDetails courseDetails) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("planItemId", planItem.getId());
        params.put("planItemType", formatTypeKey(planItem.getTypeKey()));
         //  Only planned or backup items get an atpId attribute.
        if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) ||
                planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            params.put("atpId", formatAtpIdForUI(planItem.getPlanPeriods().get(0)));
        }

        //  Create Javascript events.
        String courseDetailsAsJson;
        try {
            //  Serialize course details into a string of JSON.
            courseDetailsAsJson = mapper.writeValueAsString(courseDetails);
        } catch (Exception e) {
            throw new RuntimeException("Could not convert javascript events to JSON.");
        }
        params.put("courseDetails", courseDetailsAsJson);
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, params);
        return events;
    }

    private String getUserId() {
        Person user = GlobalVariables.getUserSession().getPerson();
        return user.getPrincipalId();
    }

    private String formatAtpIdForUI(String atpId) {
        return atpId.replaceAll("\\.", "-");
    }

    private String formatTypeKey(String typeKey) {
        return typeKey.substring(typeKey.lastIndexOf(".") + 1);
    }

    private String getTotalCredits(String termId) {
        int totalMin = 0;
        int totalMax = 0;
        Person user = GlobalVariables.getUserSession().getPerson();
        String studentID = user.getPrincipalId();

        String planTypeKey = PlanConstants.LEARNING_PLAN_TYPE_PLAN;
        ContextInfo context = CourseSearchConstants.CONTEXT_INFO;
        List<LearningPlanInfo> learningPlanList = null;
        List<PlanItemInfo> planItemList = null;

        try {
            learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(studentID, planTypeKey, CourseSearchConstants.CONTEXT_INFO);


            for (LearningPlanInfo learningPlan : learningPlanList) {
                String learningPlanID = learningPlan.getId();

                planItemList = getAcademicPlanService().getPlanItemsInPlanByType(learningPlanID, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, context);

                for (PlanItemInfo planItem : planItemList) {
                    String courseID = planItem.getRefObjectId();
                    for (String atp : planItem.getPlanPeriods()) {
                        if (atp.equalsIgnoreCase(termId)) {
                            CourseDetails courseDetails = getCourseDetailsInquiryService().retrieveCourseSummary(courseID);

                            String[] str = courseDetails.getCredit().split("\\D");
                            int min = Integer.parseInt( str[0] );
                            totalMin += min;
                            int max = Integer.parseInt( str[str.length-1] );
                            totalMax +=max;
                        }
                    }
                }
            }
        } catch (Exception e) {

            logger.error("could not load total credits");
        }

        String totalCredits = Integer.toString(totalMin);
        if( totalMin != totalMax )
        {
            totalCredits = totalCredits + "-" + Integer.toString(totalMax);
        }

        return totalCredits;
    }


    public synchronized AtpHelper getAtpHelper() {
        if (this.atpHelper == null) {
            this.atpHelper = new AtpHelper();
        }
        return atpHelper;
    }

    public void setAtpHelper(AtpHelper atpHelper) {
        this.atpHelper = atpHelper;
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
        if (this.courseDetailsInquiryService == null) {
            this.courseDetailsInquiryService = new CourseDetailsInquiryViewHelperServiceImpl();
        }
        return courseDetailsInquiryService;
    }

    public void setCourseDetailsInquiryService(CourseDetailsInquiryViewHelperServiceImpl courseDetailsInquiryService) {
        this.courseDetailsInquiryService = courseDetailsInquiryService;
    }
}