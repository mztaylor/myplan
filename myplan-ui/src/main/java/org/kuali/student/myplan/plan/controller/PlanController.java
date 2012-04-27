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
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
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
import java.text.ParseException;
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
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new HashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

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

        String planItemId = form.getPlanItemId();
        if (StringUtils.isEmpty(planItemId)) {
            return doPlanActionError(form, "Plan Item ID was missing.", null);
        }
        String termIdString = form.getAtpId();
        if (StringUtils.isEmpty(termIdString)) {
            return doPlanActionError(form, "ATP ID was missing.", null);
        }

        ///  TODO: FIXME: This method will only consider the first ATP id.
        String[] t = termIdString.split(",");
        //  Using LinkedList so that remove() is supported.
        List<String> newTermIds = new LinkedList<String>(Arrays.asList(t));
        if (newTermIds.isEmpty()) {
            return doPlanActionError(form, "Could not parse term IDs.", null);
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


            newTermIds.add(getAtpHelper().getAtpFromYearAndTerm(term, year));
        }

        PlanItemInfo planItem = null;
        try {
            // First load the plan item and retrieve the courseId
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doPlanActionError(form, "Could not fetch plan item.", e);
        }

        //  Verify that the plan item type is "planned".
//        if (!planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
//            return doPlanActionError(form, "Move planned item was not type planned.", null);
//        }

        //  TODO: FIXME: Only dealing with a single atpid
        String oldAtpId = planItem.getPlanPeriods().get(0);


        //  Update
        planItem.setPlanPeriods(newTermIds);
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

        String typeKey = planItem.getTypeKey();

        //  Make a delete event for the old atp.  /* atpId, type, courseId */
        Map<String, String> jsDeleteEventParams = new HashMap<String, String>();
        //  TODO: FIXME: Assuming one ATP per plan item here. Add planned course actually supports multiples.
        jsDeleteEventParams.put("atpId", formatAtpIdForUI(oldAtpId));
        jsDeleteEventParams.put("planItemType", formatTypeKey(typeKey));
        jsDeleteEventParams.put("planItemId", planItemId);
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, jsDeleteEventParams);

        String newTermId = newTermIds.get(0);
        //  Make an add event for the new atp.
        Map<String, String> addPlannedItemEventParams = new HashMap<String, String>();
        addPlannedItemEventParams.put("planItemId", planItem.getId());
        addPlannedItemEventParams.put("planItemType", formatTypeKey(typeKey));
        //  TODO: FIXME: Assuming one ATP per plan item here. Add planned course actually supports multiples.
        addPlannedItemEventParams.put("atpId", formatAtpIdForUI(newTermId));
        addPlannedItemEventParams.put("courseDetails", getCourseDetailsAsJson(planItem.getRefObjectId()));
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, addPlannedItemEventParams);

        //  TODO: This logic may get updated if the user is allowed to switch from backup to planning within this method.
        if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
            //  Make an "Update total credits" for the new term.
            Map<String, String> updateCreditsEventParamsOld = new HashMap<String, String>();
            updateCreditsEventParamsOld.put("atpId", formatAtpIdForUI(newTermId));
            String totalCredits = this.getTotalCredits(newTermId);
            updateCreditsEventParamsOld.put("totalCredits", totalCredits );
            events.put(PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS, updateCreditsEventParamsOld);

            //  Make an "Update total credits" for the old term.
            Map<String, String> updateCreditsEventParamsNew = new HashMap<String, String>();
            updateCreditsEventParamsNew.put("atpId", formatAtpIdForUI(oldAtpId));
            String totalCredits2 = this.getTotalCredits(oldAtpId);
            updateCreditsEventParamsNew.put("totalCredits", totalCredits2 );

            events.put(PlanConstants.JS_EVENT_NAME.UPDATE_OLD_TERM_TOTAL_CREDITS, updateCreditsEventParamsNew);
        }

        form.setJavascriptEvents(events);

        return doPlanActionSuccess(form);
    }

    @RequestMapping(params = "methodToCall=addPlannedCourse")
    public ModelAndView addPlannedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        String courseId = form.getCourseId();
        if (StringUtils.isEmpty(courseId)) {
            return doPlanActionError(form, "Course ID was missing.", null);
        }

        /* This method only accepts a single ATP ID though the backend is able to deal with multiple ATP IDs for a
         * single planned course.
         */
        String atpId = form.getAtpId();
        //  Further validation of ATP IDs will happen in the service validation methods.
        if (StringUtils.isEmpty(atpId)) {
            return doPlanActionError(form, "Term ID was missing.", null);
        }

        //  Should the course be type 'planned' or 'backup'. Defaults to planned.
        boolean backup = form.isBackup();

        //  Using LinkedList so that remove() is supported.
        List<String> newTermIds = new LinkedList<String>();
        newTermIds.add(atpId);

        //  Check for an "other" item in the terms list and assemble an ATP ID from the year and term fields.
        if (newTermIds.contains(PlanConstants.OTHER_TERM_KEY)) {
            //  Remove the "other" item from the list.
            newTermIds.remove(newTermIds.indexOf(PlanConstants.OTHER_TERM_KEY));

            //  Create an ATP id from the values in the year and term fields.
            String year = form.getYear();
            if (StringUtils.isBlank(year)) {
                return doPlanActionError(form, "Could not construct ATP id for 'other' option because year was blank.", null);
            }

            String term = form.getTerm();
            if (StringUtils.isBlank(term)) {
                return doPlanActionError(form, "Could not construct ATP id for 'other' option because term was blank.", null);
            }

            newTermIds.add(getAtpHelper().getAtpFromYearAndTerm(term, year));
        }

        Person user = GlobalVariables.getUserSession().getPerson();

        String studentId = user.getPrincipalId();

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
        //TODO: There is a potential (small) for multiple plan's created in this model coz of multi threading. There should be a check
        // at the db level to restrict a single plan of a given type to a student
        PlanItem planItem = null;
        boolean doWishlistEvents = false;
        if (plan == null) {
            try {
                plan = createDefaultLearningPlan(studentId);
            } catch (Exception e) {
                return doPlanActionError(form, "Unable to create learning plan.", e);
            }
        } else {
            /*
             *  Before attempting to add a plan item, query for plan items for the requested course id.
             *
             *  If a plan item of type "wishlist" (saved) already exists then make this operation an update which changes the plan
             *  item type and adds the ATP id specified in the form.
             *
             *  If a plan item of type "planned" or "backup" already exists and the associated ATP IDs match the ATP ID that
             *  was submitted in the form then error out.
             *
             *  If a plan item of type "planned" or "backup" already exists and matches the type specified in the form data
             *  (via the backup "backup" field) and the ATP ID is NOT the same as specified in the form then append the append
             *  the new ATP ID to the existing record. However, if the existing type and the form type DO NOT match then
             *  create a new plan item.
             *
             */
            List<PlanItemInfo> existingPlanItems = null;
            try {
                existingPlanItems = getAcademicPlanService().getPlanItemsInPlanByRefObjectIdByRefObjectType(plan.getId(), courseId,
                    PlanConstants.COURSE_TYPE, PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                return doPlanActionError(form, "Unable to fetch plan items.", e);
            }

            for (PlanItemInfo pii : existingPlanItems) {
                if (pii.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
                    //  Wislist/saved plan items don't have ATP IDs so update the existing item.
                    if (backup) {
                        pii.setTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
                    } else {
                        pii.setTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);
                    }
                    pii.getPlanPeriods().addAll(newTermIds);
                    planItem = pii;
                    doWishlistEvents = true;
                } else {
                    //  Check if Planned and backup items have any ATP IDs in common with the ones submitted in the form.
                    //  If so, that's an error.
                    List<String> existingTermIds = pii.getPlanPeriods();
                    boolean isNoCommonAtps = Collections.disjoint(existingTermIds, newTermIds);
                    if ( ! isNoCommonAtps ) {
                        return doPlanActionError(form, String.format("A plan item for %s already exists.", newTermIds), null);
                    } else {
                        //  If there are no ATP IDs in common then determine whether the new ATP IDs should be appended
                        //  to the existing plan item or a new plan item should be created.
                        if ((pii.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP) && backup) ||
                            (pii.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) && ! backup)) {
                            //  Types match so append the new ATP ID(s) and update
                            pii.getPlanPeriods().addAll(newTermIds);
                            planItem = pii;
                        } else if ((pii.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP) && ! backup) ||
                            pii.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) && backup) {
                            //  Types don't match so create new item below.
                        }
                    }
                }
            }
        }

        //  If an update-able plan item was found then go ahead and do an update. Otherwise, do an add.
        if (planItem != null) {
            try {
                academicPlanService.updatePlanItem(planItem.getId(), (PlanItemInfo) planItem, PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                return doPlanActionError(form, "Unable to update plan item.", e);
            }
        } else {
            try {
                if (backup) {
                    planItem = addPlanItem(plan, courseId, newTermIds, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
                } else {
                    planItem = addPlanItem(plan, courseId, newTermIds, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);
                }
            } catch (Exception e) {
                return doPlanActionError(form, "Unable to add plan item.", e);
            }
        }

        //  Lookup course details, convert to Jason, include in the response.
        CourseDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseDetails(courseId);
        } catch (Exception e) {
            return doPlanActionError(form, "Unable to retrieve Course Details.", null);
        }

        String courseDetailsAsJson;
        try {
            //  Turn the list of javascript events into a string of JSON.
            courseDetailsAsJson = mapper.writeValueAsString(courseDetails);
        } catch (Exception e) {
            return doPlanActionError(form, "Could not convert javascript events to JSON.", e);
        }

        System.err.println("JSON: " + courseDetailsAsJson);

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);

        String typeKey = planItem.getTypeKey();

        //  Create and set the javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new HashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        if (doWishlistEvents) {
            Map<String, String> addPlannedItemEventParams = new HashMap<String, String>();
            addPlannedItemEventParams.put("planItemId", planItem.getId());
            addPlannedItemEventParams.put("planItemType", formatTypeKey(typeKey));
            addPlannedItemEventParams.put("courseDetails", courseDetailsAsJson);
            events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, addPlannedItemEventParams);
        }

        for (String termId : newTermIds) {
            //  One "plan item added" for each new Term ID.
            Map<String, String> addPlannedItemEventParams = new HashMap<String, String>();
            addPlannedItemEventParams.put("planItemId", planItem.getId());
            addPlannedItemEventParams.put("planItemType", formatTypeKey(typeKey));
            addPlannedItemEventParams.put("atpId", formatAtpIdForUI(termId));
            addPlannedItemEventParams.put("courseDetails", courseDetailsAsJson);
            events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, addPlannedItemEventParams);

            //  One "update total credits" for each new Term ID.
            Map<String, String> updateCreditsEventParams = new HashMap<String, String>();
            updateCreditsEventParams.put("atpId", formatAtpIdForUI(termId));
            String totalCredits = this.getTotalCredits(termId);
            updateCreditsEventParams.put("totalCredits", totalCredits);
            events.put(PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS, updateCreditsEventParams);
        }

        form.setJavascriptEvents(events);
        //  Set success text.
        GlobalVariables.getMessageMap().putInfoForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.SUCCESS_KEY);

        //  TODO: These can go away once the transition to JS events is complete.
        form.setPlanItemId(planItem.getId());
        form.setCourseId(planItem.getRefObjectId());

        //  TODO: Hold on this for now. Unclear how meta data gets updated.
        //  Update the timestamp on the plan.
        //try {
        //    academicPlanService.updateLearningPlan(plan.getId(), (LearningPlanInfo) plan, PlanConstants.CONTEXT_INFO);
        //} catch (Exception e) {
        //    logger.error("Unable to update the plan.", e);
        //}
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
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
        } catch (RuntimeException e) {
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

        PlanItem planItem = null;
        try {
            planItem = addPlanItem(plan, courseId, null, PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        } catch (Exception e) {
            return doAddPlanItemError(form, "Unable to add plan item.", e);
        }

        //  Also, add a full CourseDetails object so that course details properties are available to be displayed on the form.
        CourseDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseDetails(courseId);
        } catch (Exception e) {
            return doAddPlanItemError(form, "Unable to retrieve Course Details.", null);
        }

        String courseDetailsAsJson;
        try {
            //  Turn the list of javascript events into a string of JSON.
            courseDetailsAsJson = mapper.writeValueAsString(courseDetails);
        } catch (Exception e) {
            return doAddPlanItemError(form, "Could not convert javascript events to JSON.", e);
        }

        String typeKey = planItem.getTypeKey();

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
        //  Queue the javascript event(s) that should be thrown in the UI.
        Map<String, String> jsEventParams = new HashMap<String, String>();
        jsEventParams.put("planItemId", planItem.getId());
        jsEventParams.put("planItemType", formatTypeKey(typeKey));
        jsEventParams.put("courseDetails", courseDetailsAsJson);

        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new HashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, jsEventParams);
        form.setJavascriptEvents(events);
        //  Set success text.
        GlobalVariables.getMessageMap().putInfoForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, PlanConstants.SUCCESS_KEY);

        //  TODO: These can go away once the transition to JS events is complete.
        form.setPlanItemId(planItem.getId());
        form.setCourseId(planItem.getRefObjectId());

        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    /**
     * Blow up on failed plan adds.
     *
     * @deprecated Use doPlanActionError() instead.
     */
    private ModelAndView doAddPlanItemError(PlanForm form, String errorMessage, Exception e) {
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
    protected PlanItem addPlanItem(LearningPlan plan, String courseId, List<String> termIds, String planItemType) {

        if (StringUtils.isEmpty(courseId)) {
            // TODO: DO ERROR.
            throw new RuntimeException("Empty Course ID");
        }

        PlanItem newPlanItem = null;

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
            //TODO: Once all courseId dependencies are removed from form, delete this section
            PlanItemInfo planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
            String courseId = planItem.getRefObjectId();
            form.setCourseId(courseId);

            // Now Delete the plan item
            getAcademicPlanService().deletePlanItem(planItemId, PlanConstants.CONTEXT_INFO);

            //  Set the status of the request for the UI.
            form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
            form.setJavascriptEvents(makeRemoveEvents(planItem));


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
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeRemoveEvents(PlanItemInfo planItem) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new HashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        String termId = (planItem.getPlanPeriods() == null || planItem.getPlanPeriods().size() == 0) ? "" : planItem.getPlanPeriods().get(0);

        Map<String, String> jsEventParams = new HashMap<String, String>();
        if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) ||
                planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            jsEventParams.put("atpId", formatAtpIdForUI(planItem.getPlanPeriods().get(0)));
        }
        jsEventParams.put("planItemType", formatTypeKey(planItem.getTypeKey()));
        jsEventParams.put("planItemId", planItem.getId());
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, jsEventParams);

        if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
            //  Queue the javascript event(s) that should be thrown in the UI.
            //  One "update total credits" for each ATP id.
            Map<String, String> updateCreditsEventParams = new HashMap<String, String>();
            updateCreditsEventParams.put("atpId", formatAtpIdForUI(termId));
            String totalCredits = this.getTotalCredits(termId);
            updateCreditsEventParams.put("totalCredits", totalCredits);
            events.put(PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS, updateCreditsEventParams);
        }

        return events;
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