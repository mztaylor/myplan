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
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.datadictionary.exception.DuplicateEntryException;
import org.kuali.rice.krad.uif.field.AttributeQueryResult;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.comment.dataobject.MessageDataObject;
import org.kuali.student.myplan.comment.service.CommentQueryHelper;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.form.PlanForm;
import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.MetaInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.util.*;

import static org.springframework.util.StringUtils.hasText;

@Controller
@RequestMapping(value = "/plan/**")
public class PlanController extends UifControllerBase {

    private final Logger logger = Logger.getLogger(PlanController.class);

    private transient AcademicPlanService academicPlanService;

    private transient DegreeAuditService degreeAuditService;

    @Autowired
    private transient CourseHelper courseHelper;

    private PlannedTermsHelperBase plannedTermsHelper;

    private transient CourseDetailsInquiryHelperImpl courseDetailsInquiryService;

    private transient CourseOfferingService courseOfferingService;

    //  Java to JSON outputter.
    private transient ObjectMapper mapper = new ObjectMapper();

    // Used for getting the term and year from Atp
    private transient AtpHelper atpHelper;
    private transient AcademicRecordService academicRecordService;

    public AcademicRecordService getAcademicRecordService() {
        if (this.academicRecordService == null) {
            //   TODO: Use constants for namespace.
            this.academicRecordService = (AcademicRecordService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/academicrecord", "arService"));
        }
        return this.academicRecordService;
    }

    public void setAcademicRecordService(AcademicRecordService academicRecordService) {
        this.academicRecordService = academicRecordService;
    }

    public CourseHelper getCourseHelper() {
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    public PlannedTermsHelperBase getPlannedTermsHelper() {
        if (plannedTermsHelper == null) {
            plannedTermsHelper = new PlannedTermsHelperBase();
        }
        return plannedTermsHelper;
    }

    @Override
    protected PlanForm createInitialForm(HttpServletRequest request) {
        return new PlanForm();
    }

    /**
     * plan Access form for student to provide viewing plan access to adviser
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=startPlanAccessForm")
    public ModelAndView startPlanAccessForm(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                            HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);

        /**
         * Initializing
         */
        PlanForm planForm = (PlanForm) form;
        List<LearningPlanInfo> plan = null;

        /**
         * Loading Bookmark Plan Items count and adviser sharing flag
         */
        try {

            plan = getAcademicPlanService().getLearningPlansForStudentByType(UserSessionHelper.getStudentRegId(), PlanConstants.LEARNING_PLAN_TYPE_PLAN, PlanConstants.CONTEXT_INFO);

            if (!CollectionUtils.isEmpty(plan)) {

                //A student should have only one learning plan associated to his Id
                LearningPlan learningPlan = plan.get(0);

                planForm.setEnableAdviserView(learningPlan.getShared().toString());

                List<PlanItemInfo> planItems = getAcademicPlanService().getPlanItemsInPlanByType(learningPlan.getId(), PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, PlanConstants.CONTEXT_INFO);

                if (!CollectionUtils.isEmpty(planItems)) {
                    planForm.setBookmarkedCount(planItems.size());
                }

            }

        } catch (Exception e) {

            return doOperationFailedError(planForm, "Could not load the plan items", e);

        }

        /**
         * Loading the messages count
         */
        List<MessageDataObject> messages = null;

        try {
            CommentQueryHelper commentQueryHelper = new CommentQueryHelper();
            messages = commentQueryHelper.getMessages(UserSessionHelper.getStudentRegId());
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve messages.", e);
        }

        if (!CollectionUtils.isEmpty(messages)) {
            planForm.setMessagesCount(messages.size());
        }

        return getUIFModelAndView(planForm);
    }

    /**
     * Initial Plan page loading
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=start")
    public ModelAndView get(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                            HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);
        PlanForm planForm = (PlanForm) form;
        planForm.setNewUser(isNewUser());
        return getUIFModelAndView(planForm);
    }

    /**
     * Used for initializing all the popups, dialogs, menus
     *
     * @param form
     * @param result
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(params = "methodToCall=startAddPlannedCourseForm")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        /**
         * Initializing
         */
        super.start(form, result, request, response);
        PlanForm planForm = (PlanForm) form;


        /**
         * Loading and returning quickAdd view if requested
         *
         */
        if (PlanConstants.QUICK_ADD_DIALOG_PAGE.equals(form.getPageId()) || PlanConstants.ADD_DIALOG_PAGE.equals(form.getPageId())) {

            if (hasText(planForm.getAtpId())) {
                String termYear = AtpHelper.atpIdToTermName(planForm.getAtpId());
                planForm.setTermName(termYear);
            } else {
                return doPageRefreshError(planForm, "Could not open Quick Add.", null);
            }
            return getUIFModelAndView(planForm);
        }


        //Plan activities are needed only for move, copy, delete functionality
        List<String> activitiesRequiredPages = Arrays.asList(PlanConstants.MOVE_DIALOG_PAGE, PlanConstants.COPY_DIALOG_PAGE, PlanConstants.DELETE_DIALOG_PAGE);
        PlanItemInfo planItem = null;
        String planItemAtpId = null;


        /**
         * Loading planItem for all Planned Items in plan with a planItemId (planned, backup, wishList)
         */
        if (planForm.getPlanItemId() != null) {

            try {

                planItem = getAcademicPlanService().getPlanItem(planForm.getPlanItemId(), PlanConstants.CONTEXT_INFO);

                if (planItem != null) {
                    planForm.setCourseId(planItem.getRefObjectId());

                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP.equalsIgnoreCase(planItem.getTypeKey())) {
                        planForm.setBackup(true);
                    }

                    if (!CollectionUtils.isEmpty(planItem.getPlanPeriods())) {
                        //Assuming plan Item can only have one plan period
                        planItemAtpId = planItem.getPlanPeriods().get(0);
                        if (planItemAtpId.equalsIgnoreCase(planForm.getAtpId()) && !planForm.isSetToPlanning()) {
                            planForm.setSetToPlanning(AtpHelper.isAtpSetToPlanning(planItemAtpId));
                        }
                        if (planForm.getAtpId() == null) {
                            planForm.setAtpId(planItemAtpId);
                        }
                    }


                } else {

                    return doPageRefreshError(planForm, "Plan item not found.", null);

                }
            } catch (Exception e) {
                return doPageRefreshError(planForm, "Plan item not found.", e);
            }

        }

        //TODO: Clean up with courseId removal
        if (StringUtils.isEmpty(planForm.getCourseId())) {

            return doOperationFailedError(planForm, "Could not initialize form because Course ID was missing.", null);

        }

        planForm.setTermName(AtpHelper.atpIdToTermName(planForm.getAtpId()));

        /**
         * Populated the course summary details using the courseId or the versionIndependentId (planItem refObjId)
         */
        try {

            planForm.setCourseSummaryDetails(getCourseDetailsInquiryService().retrieveCourseSummaryById(planForm.getCourseId()));

        } catch (Exception e) {

            planForm.setCourseSummaryDetails(new CourseSummaryDetails());
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.ACTION_MENU_PAGE_ID, PlanConstants.ERROR_KEY_UNKNOWN_COURSE);
            return doPageRefreshError(planForm, "Could not load course details.", e);

        }

        /**
         * populating the Plan related Information
         */
        planForm.setPlannedCourseSummary(getCourseDetailsInquiryService().getPlannedCourseSummaryById(planForm.getCourseId(), UserSessionHelper.getStudentRegId()));


        /**
         * Populating the planActivities (Activity Offerings which are planned)
         */
        if (activitiesRequiredPages.contains(form.getPageId()) && planItemAtpId != null) {
            if (planForm.getCourseSummaryDetails() != null && planForm.getCourseSummaryDetails().getCode() != null) {
                planForm.setPlanActivities(getPlannedActivitiesByCourseAndTerm(planForm.getCourseSummaryDetails().getCode(), planItem.getPlanPeriods().get(0)));
            }
        }


        /**
         *  For academic record Item (with No planItem exists) instead of atpId,
         *  acadRecAtpId is used to populate the setToPlanning Flag
         */
        if (planForm.getAcadRecAtpId() != null && !planForm.isSetToPlanning()) {

            planForm.setSetToPlanning(AtpHelper.isAtpSetToPlanning(planForm.getAcadRecAtpId()));

        }


        return getUIFModelAndView(planForm);
    }

    /**
     * Move a planned course from plan to backup
     *
     * @param form
     * @param result
     * @param httprequest
     * @param httpresponse
     * @return
     */
    @RequestMapping(params = "methodToCall=plannedToBackup")
    public ModelAndView plannedToBackup(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                        HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        if (UserSessionHelper.isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }

        String planItemId = form.getPlanItemId();
        if (StringUtils.isEmpty(planItemId)) {
            return doOperationFailedError(form, "Plan Item ID was missing.", null);
        }

        String planItemAtpId = null;

        //  Verify the type is planned, change to backup, update, make events (delete, add, update credits).
        PlanItemInfo planItem = null;
        try {
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not fetch plan item.", e);
        }

        if (planItem != null && !CollectionUtils.isEmpty(planItem.getPlanPeriods())) {
            planItemAtpId = planItem.getPlanPeriods().get(0);
        } else {
            return doOperationFailedError(form, "Could not fetch plan Item", null);
        }

        //  Verify that the plan item type is "planned".
        if (!PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equals(planItem.getTypeKey())) {
            return doOperationFailedError(form, "Move planned item was not type planned.", null);
        }

        //  Validate: Capacity.
        boolean hasCapacity = false;
        try {
            hasCapacity = isAtpHasCapacity(getLearningPlan(UserSessionHelper.getStudentRegId()),
                    planItemAtpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
        }

        if (!hasCapacity) {
            return doPlanCapacityExceededError(form, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        }

        //  Load course summary details.
        CourseSummaryDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(planItem.getRefObjectId());
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to retrieve Course Details.", e);
        }

        //  Make removed event before updating the plan item.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> removeEvent = makeRemoveEvent(planItem, courseDetails, planItem.getRefObjectId(), form, null);

        //  Update
        planItem.setTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        try {
            getAcademicPlanService().updatePlanItem(planItemId, planItem, UserSessionHelper.makeContextInfoInstance());
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not update plan item.", e);
        }

        //  Make events (delete, add, update credits).
        //  Set the javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        events.putAll(removeEvent);
        events.putAll(makeAddEvent(planItem, courseDetails, form));

        addStatusAlertEvents(courseDetails.getCode(), planItemAtpId, events);

        events.putAll(makeUpdateTotalCreditsEvent(planItemAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        form.setJavascriptEvents(events);

        //  Pass the ATP name in the params.
        String[] params = {AtpHelper.atpIdToTermName(planItemAtpId)};
        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_PLANNED_ITEM_MARKED_BACKUP, params);
    }

    /**
     * Move a planned course from backup to plan
     *
     * @param form
     * @param result
     * @param httprequest
     * @param httpresponse
     * @return
     */
    @RequestMapping(params = "methodToCall=backupToPlanned")
    public ModelAndView backupToPlanned(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                        HttpServletRequest httprequest, HttpServletResponse httpresponse) {

        if (UserSessionHelper.isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }

        String planItemId = form.getPlanItemId();
        if (StringUtils.isEmpty(planItemId)) {
            return doOperationFailedError(form, "Plan Item ID was missing.", null);
        }

        String planItemAtpId = null;


        //  Verify type backup, change to planned, update, make events (delete, add, update credits).
        PlanItemInfo planItem = null;
        try {
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not fetch plan item.", e);
        }

        if (planItem != null && !CollectionUtils.isEmpty(planItem.getPlanPeriods())) {
            planItemAtpId = planItem.getPlanPeriods().get(0);
        } else {
            return doOperationFailedError(form, "Could not fetch plan Item", null);
        }


        //  Verify that the plan item type is "backup".
        if (!PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP.equals(planItem.getTypeKey())) {
            return doOperationFailedError(form, "Move planned item was not type backup.", null);
        }

        //  Validate: Capacity.
        boolean hasCapacity = false;
        try {
            hasCapacity = isAtpHasCapacity(getLearningPlan(UserSessionHelper.getStudentRegId()),
                    planItemAtpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
        }

        if (!hasCapacity) {
            return doPlanCapacityExceededError(form, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        }

        //  Load course summary details.
        CourseSummaryDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(planItem.getRefObjectId());
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to retrieve Course Details.", e);
        }

        //  Make removed event before updating the plan item.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> removeEvent = makeRemoveEvent(planItem, courseDetails, planItem.getRefObjectId(), form, null);

        //  Set type to "planned".
        planItem.setTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);

        //  Update
        try {
            getAcademicPlanService().updatePlanItem(planItemId, planItem, UserSessionHelper.makeContextInfoInstance());
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not update plan item.", e);
        }

        //  Make events (delete, add, update credits).
        //  Set the javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        events.putAll(removeEvent);
        events.putAll(makeAddEvent(planItem, courseDetails, form));

        addStatusAlertEvents(courseDetails.getCode(), planItemAtpId, events);

        events.putAll(makeUpdateTotalCreditsEvent(planItemAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        form.setJavascriptEvents(events);

        String[] params = {AtpHelper.atpIdToTermName(planItemAtpId)};
        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_PLANNED_ITEM_MARKED_PLANNED, params);
    }


    /**
     * Move Planned courses to requested terms
     *
     * @param form
     * @param result
     * @param httprequest
     * @param httpresponse
     * @return
     */
    @RequestMapping(params = "methodToCall=movePlannedCourse")
    public ModelAndView movePlannedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                          HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (UserSessionHelper.isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }
        /**
         * This method needs a Plan Item ID and an ATP ID.
         */
        String planItemId = form.getPlanItemId();

        if (StringUtils.isEmpty(planItemId)) {
            return doOperationFailedError(form, "Plan Item ID was missing.", null);
        }

        //  Further validation of ATP IDs will happen in the service validation methods.
        if (StringUtils.isEmpty(form.getAtpId())) {
            return doOperationFailedError(form, "Term Year value missing", null);
        }

        String newAtpId = form.getAtpId();
        //  Can't validate further up because the new ATP ID can be "other".
        if (!AtpHelper.isAtpIdFormatValid(newAtpId)) {
            return doOperationFailedError(form, String.format("ATP ID [%s] was not formatted properly.", newAtpId), null);
        }


        PlanItemInfo planItem = null;
        try {
            // First load the plan item and retrieve the courseId
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not fetch plan item.", e);
        }

        if (planItem == null) {
            return doOperationFailedError(form, String.format("Could not fetch plan item."), null);
        }

        //  Lookup course details as they will be needed for errors.
        CourseSummaryDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(planItem.getRefObjectId());
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to retrieve Course Details.", null);
        }

        //  Make sure there isn't a plan item for the same course id in the destination ATP.
        PlanItemInfo existingPlanItem = null;
        try {
            existingPlanItem = getPlannedOrBackupPlanItem(planItem.getRefObjectId(), newAtpId);
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Query for existing plan item failed.", null);
        }

        if (existingPlanItem != null) {
            String[] params = {courseDetails.getCode(), AtpHelper.atpIdToTermName(newAtpId)};
            return doErrorPage(form, PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
        }

        /*Remove the Planned Sections for this course before moving the course to another term*/
        Map<String, String> planItemsToRemove = new HashMap<String, String>();

        if (courseDetails != null && courseDetails.getCourseId() != null && planItem != null && planItem.getPlanPeriods().size() > 0) {
            planItemsToRemove = getPlannedSectionsBySectionCd(courseDetails.getCode(), planItem, false, null);
        }

        try {

            if (!planItemsToRemove.isEmpty()) {
                for (String planItemIdToRemove : planItemsToRemove.keySet()) {
                    getAcademicPlanService().deletePlanItem(planItemIdToRemove, UserSessionHelper.makeContextInfoInstance());
                }
            }

        } catch (Exception e) {
            return doOperationFailedError(form, "Could not delete plan item", e);
        }

        //  Create events before updating the plan item.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> originalRemoveEvents = makeRemoveEvent(planItem, courseDetails, planItem.getRefObjectId(), form, null);
        //  Save the source ATP ID to create credit total updates later.
        String originalAtpId = planItem.getPlanPeriods().get(0);

        //  Do validations.
        //  Validate: Plan Size exceeded.
        boolean hasCapacity = false;
        try {
            hasCapacity = isAtpHasCapacity(getLearningPlan(UserSessionHelper.getStudentRegId()), newAtpId, planItem.getTypeKey());
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
        }
        if (!hasCapacity) {
            return doPlanCapacityExceededError(form, planItem.getTypeKey());
        }

        //  Validate: Adding to historical term.
        if (!AtpHelper.isAtpSetToPlanning(newAtpId)) {
            return doCannotChangeHistoryError(form);
        }

        //  Update the plan item.
        planItem.setPlanPeriods(Arrays.asList(newAtpId));
        //  Changing types not current supported in this operation.
        //planItem.setTypeKey(newType);

        try {
            getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, UserSessionHelper.makeContextInfoInstance());
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not udpate plan item.", e);
        }

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);

        //  Make Javascript UI events (delete, add, update credits).
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        //  Add events generated for the plan item before it was updated.
        events.putAll(originalRemoveEvents);
        //  Create update total credits on source ATP.
        events.putAll(makeUpdateTotalCreditsEvent(originalAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_OLD_TERM_TOTAL_CREDITS));

        try {
            events.putAll(makeAddEvent(planItem, courseDetails, form));
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Unable to create add event.", e);
        }
        events.putAll(makeUpdateTotalCreditsEvent(newAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        form.setJavascriptEvents(events);

        //String link = makeLinkToAtp(newAtpId, AtpHelper.atpIdToTermName(newAtpId));
        String[] params = {AtpHelper.atpIdToTermName(newAtpId)};
        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_PLANNED_ITEM_MOVED, params);
    }

    /**
     * Copy a course planned to requested term
     *
     * @param form
     * @param result
     * @param httprequest
     * @param httpresponse
     * @return
     */
    @RequestMapping(params = "methodToCall=copyPlannedCourse")
    public ModelAndView copyPlannedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                          HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (UserSessionHelper.isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }
        /**
         * This method needs a Plan Item ID and an ATP ID.
         */
        String planItemId = form.getPlanItemId();
        if (StringUtils.isEmpty(planItemId)) {
            return doOperationFailedError(form, "Plan Item ID was missing.", null);
        }

        // validation of Year and Term will happen in the service validation methods.
        if (StringUtils.isEmpty(form.getAtpId())) {
            return doOperationFailedError(form, "Term Year value missing", null);
        }

        String newAtpId = form.getAtpId();

        //  Can't validate further up because the new ATP ID can be "other".
        if (!AtpHelper.isAtpIdFormatValid(newAtpId)) {
            return doOperationFailedError(form, String.format("ATP ID [%s] was not formatted properly.", newAtpId), null);
        }

        PlanItemInfo planItem = null;
        try {
            // First load the plan item and retrieve the courseId
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not fetch plan item.", e);
        }

        if (planItem == null) {
            return doOperationFailedError(form, String.format("Could not fetch plan item."), null);
        }

        //  Lookup course details as they will be needed for errors.
        CourseSummaryDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(planItem.getRefObjectId());
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to retrieve Course Details.", e);
        }

        //  Make sure there isn't a plan item for the same course id in the destination ATP.
        PlanItemInfo existingPlanItem = null;
        try {
            existingPlanItem = getPlannedOrBackupPlanItem(planItem.getRefObjectId(), newAtpId);
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Query for existing plan item failed.", e);
        }

        if (existingPlanItem != null) {
            String[] params = {courseDetails.getCode(), AtpHelper.atpIdToTermName(newAtpId)};
            return doErrorPage(form, PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
        }

        //  Do validations.
        //  Validate: Plan Size exceeded.
        boolean hasCapacity = false;
        LearningPlan learningPlan = null;
        try {
            learningPlan = getLearningPlan(UserSessionHelper.getStudentRegId());
            hasCapacity = isAtpHasCapacity(learningPlan, newAtpId, planItem.getTypeKey());
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
        }
        if (!hasCapacity) {
            return doPlanCapacityExceededError(form, planItem.getTypeKey());
        }

        //  Validate: Adding to historical term.
        if (!AtpHelper.isAtpSetToPlanning(newAtpId)) {
            return doCannotChangeHistoryError(form);
        }

        //  Update the plan item.
        planItem.setPlanPeriods(Arrays.asList(newAtpId));
        //  Do not allow diagonal moves .
        //planItem.setTypeKey(newType);

        PlanItemInfo planItemCopy = null;
        try {
            String courseId = planItem.getRefObjectId();
            planItemCopy = addPlanItem(learningPlan, courseId, newAtpId, planItem.getTypeKey(), form.getNote());
        } catch (DuplicateEntryException e) {
            return doDuplicatePlanItem(form, formatAtpIdForUI(newAtpId), courseDetails);
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to add plan item.", e);
        }

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);

        //  Create the map of javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        try {
            events.putAll(makeAddEvent(planItemCopy, courseDetails, form));
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Unable to create add event.", e);
        }

        events.putAll(makeUpdateTotalCreditsEvent(newAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        //  Populate the form.
        form.setJavascriptEvents(events);

        //String link = makeLinkToAtp(newAtpId, AtpHelper.atpIdToTermName(newAtpId));
        String[] params = {AtpHelper.atpIdToTermName(newAtpId)};
        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_PLANNED_ITEM_COPIED, params);
    }

    /**
     * Adds a course to plan for requested academic term
     *
     * @param form
     * @param result
     * @param httprequest
     * @param httpresponse
     * @return
     */
    @RequestMapping(params = "methodToCall=addPlannedCourse")
    public ModelAndView addPlannedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                         HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (UserSessionHelper.isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }


        /*
        * ************************************************************************************************************
        *                                  QuickAdd a course or placeholder to Plan
        * ************************************************************************************************************
        */

        boolean placeHolder = false;
        /** Populating courseId from courseCode (QuickAdd view) */
        if (hasText(form.getCourseCd())) {
            HashMap<String, String> divisionMap = getCourseHelper().fetchCourseDivisions();
            DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(form.getCourseCd());
            if (courseCode.getSubject() != null && courseCode.getNumber() != null) {
                String subject = courseCode.getSubject();
                String number = courseCode.getNumber();
                ArrayList<String> divisions = new ArrayList<String>();
                getCourseHelper().extractDivisions(divisionMap, subject, divisions, false);
                if (divisions.size() > 0) {
                    subject = divisions.get(0);
                    String courseId = getCourseHelper().getCourseId(subject, number);
                    form.setCourseId(courseId);
                }
            }
            if (form.getCourseId() == null) {
                return doErrorPage(form, "Course not found", PlanConstants.COURSE_NOT_FOUND, new String[]{form.getCourseCd()}, null);
            }
        }


        /**
         * This method needs a Course ID and an ATP ID.
         */
        String courseId = form.getCourseId();


        if (StringUtils.isEmpty(courseId)) {
            return doOperationFailedError(form, "Course ID was missing.", null);
        }

        /*Retrieve courseDetails based on the passed in CourseId and then update courseDetails based on the version independent Id*/
        CourseSummaryDetails courseDetails = null;

        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseId);
            /* Switching the courseDetails based on the versionIndependent Id*/
            if (!courseId.equals(courseDetails.getVersionIndependentId())) {
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseDetails.getVersionIndependentId());
            }
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to retrieve Course Details.", null);
        }


        //  Further validation of ATP IDs will happen in the service validation methods.
        if (StringUtils.isEmpty(form.getAtpId())) {
            return doOperationFailedError(form, "Term Year value missing", null);
        }

        //  Should the course be type 'planned' or 'backup'. Default to planned.
        String newType = form.isBackup() ? PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP : PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED;

        String newAtpId = form.getAtpId();

        if (!AtpHelper.isAtpIdFormatValid(newAtpId)) {
            return doOperationFailedError(form, String.format("ATP ID [%s] was not formatted properly.", newAtpId), null);
        }

        String studentId = UserSessionHelper.getStudentRegId();

        LearningPlan plan = getSynchronizedLearningPlan(studentId);
        if (plan == null) {
            return doOperationFailedError(form, "Unable to create learning plan.", null);
        }

        boolean addCourse = true;
        boolean addPrimaryCourse = false;
        boolean addSecondaryCourse = false;

        /*
        * ************************************************************************************************************
        * Process the activities to know if course or primary activity or secondary activity or all need to be added
        * to the Plan
        *
        * Planning a course --> addCourse will be true.
        * Planning a primary activity and course not planned --> addCourse will be true and add primary will be true.
        * Planning a secondary activity and primary not planned and course not planned --> addCourse will be true and add
        * primary will be true and secondary will be true.
        * ************************************************************************************************************
        */

        if (form.getSectionCode() != null) {
            String primaryRegistrationCode = null;
            List<ActivityOfferingItem> activityOfferings = getCourseDetailsInquiryService().getActivityOfferingItemsById(courseDetails.getCourseId(), form.getAtpId());
            /*Populate the primary and secondary flags*/
            for (ActivityOfferingItem activityOfferingItem : activityOfferings) {
                if (activityOfferingItem.isPrimary() && !form.isPrimary() && form.getSectionCode().startsWith(activityOfferingItem.getCode())) {
                    primaryRegistrationCode = activityOfferingItem.getRegistrationCode();
                }
                if (activityOfferingItem.getCode().equalsIgnoreCase(form.getSectionCode())) {
                    if (activityOfferingItem.isPrimary()) {
                        PlanItemInfo coursePlanItem = getPlannedOrBackupPlanItem(courseDetails.getVersionIndependentId(), form.getAtpId());
                        if (coursePlanItem != null) {
                            addCourse = false;
                        }
                        addPrimaryCourse = true;
                        form.setPrimarySectionCode(activityOfferingItem.getCode());
                        form.setPrimaryRegistrationCode(activityOfferingItem.getRegistrationCode());
                        break;
                    } else {
                        PlanItemInfo primaryPlanItem = getPlannedOrBackupPlanItem(activityOfferingItem.getPrimaryActivityOfferingId(), form.getAtpId());
                        if (primaryPlanItem != null) {
                            addCourse = false;
                        } else {
                            addPrimaryCourse = true;
                            form.setPrimarySectionCode(getCourseHelper().getCodeFromActivityId(activityOfferingItem.getPrimaryActivityOfferingId()));
                            form.setPrimaryRegistrationCode(primaryRegistrationCode);
                            PlanItemInfo coursePlanItem = getPlannedOrBackupPlanItem(courseDetails.getVersionIndependentId(), form.getAtpId());
                            if (coursePlanItem != null) {
                                addCourse = false;
                            }
                        }
                        addSecondaryCourse = true;
                        break;
                    }
                }
            }
        }


        /*Plan capacity validation.*/
        if (addCourse) {
            boolean hasCapacity = false;
            try {
                hasCapacity = isAtpHasCapacity(plan, newAtpId, newType);
            } catch (RuntimeException e) {
                return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
            }

            if (!hasCapacity) {
                return doPlanCapacityExceededError(form, newType);
            }
        }


        /*Historical term validation*/
        if (!AtpHelper.isAtpSetToPlanning(newAtpId)) {
            return doCannotChangeHistoryError(form);
        }


        /*
         * ************************************************************************************************************
         *                                  Adding a course to Plan
         * ************************************************************************************************************
         */

        //  See if a wishList item exists for the course. If so, then update it. Otherwise create a new plan item.
        PlanItemInfo planItem = getWishlistPlanItem(courseDetails.getVersionIndependentId());

        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> wishlistEvents = null;

        if (planItem == null && addCourse) {
            try {

                planItem = addPlanItem(plan, courseDetails.getVersionIndependentId(), newAtpId, newType, form.getNote());

            } catch (DuplicateEntryException e) {
                return doDuplicatePlanItem(form, newAtpId, courseDetails);
            } catch (Exception e) {
                return doOperationFailedError(form, "Unable to add plan item.", e);
            }
        } else if (planItem != null && addCourse) {

            //  Check for duplicates since addPlanItem isn't being called.
            if (addCourse && isDuplicate(plan, newAtpId, courseDetails.getVersionIndependentId(), newType)) {
                return doDuplicatePlanItem(form, newAtpId, courseDetails);
            }

            //  Create wishList events before updating the plan item.
            wishlistEvents = makeRemoveEvent(planItem, courseDetails, planItem.getRefObjectId(), form, null);
            planItem.setTypeKey(newType);
            planItem.setPlanPeriods(Arrays.asList(newAtpId));

            try {

                planItem = getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, UserSessionHelper.makeContextInfoInstance());

            } catch (Exception e) {

            }
        }

        /*
        * *************************************************************************************************************
        *                             Adding activities to Plan
        * *************************************************************************************************************
        */

        /*PlanItems for sections*/
        PlanItemInfo primaryPlanItem = null;
        PlanItemInfo secondaryPlanItem = null;

        if (addPrimaryCourse && form.getPrimarySectionCode() != null) {

            primaryPlanItem = addActivityOfferingPlanItem(plan, getCourseHelper().buildActivityRefObjId(newAtpId, courseDetails.getSubjectArea(), courseDetails.getCourseNumber(), form.getPrimarySectionCode()), newAtpId, newType);
            form.setPrimaryPlanItemId(primaryPlanItem.getId());

        }

        if (addSecondaryCourse && form.getSectionCode() != null) {

            secondaryPlanItem = addActivityOfferingPlanItem(plan, getCourseHelper().buildActivityRefObjId(newAtpId, courseDetails.getSubjectArea(), courseDetails.getCourseNumber(), form.getSectionCode()), newAtpId, newType);

        }


        /*
        * *************************************************************************************************************
        *                   Adding placeholders to the plan
        * *************************************************************************************************************
        */


        /*
        * *************************************************************************************************************
        *                   Adding events to the form
        * *************************************************************************************************************
        */


        /*Create the map of javascript event(s) that should be thrown in the UI.*/
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        /*If a wishList item was clobbered then generate Javascript events.*/
        if (wishlistEvents != null) {
            events.putAll(wishlistEvents);
        }
        String plannedTerm = null;
        try {
            if (planItem != null) {
                plannedTerm = planItem.getPlanPeriods().get(0);
                events.putAll(makeAddEvent(planItem, courseDetails, form));
            }
            if (primaryPlanItem != null) {
                plannedTerm = primaryPlanItem.getPlanPeriods().get(0);
                events.putAll(makeAddEvent(primaryPlanItem, courseDetails, form));
            }
            if (secondaryPlanItem != null) {
                plannedTerm = secondaryPlanItem.getPlanPeriods().get(0);
                events.putAll(makeAddEvent(secondaryPlanItem, courseDetails, form));
            }
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Unable to create add event.", e);
        }

        events.putAll(makeUpdateTotalCreditsEvent(plannedTerm, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        form.setJavascriptEvents(events);

        String[] params = {};
        if (planItem != null) {
            params = new String[]{AtpHelper.atpIdToTermName(planItem.getPlanPeriods().get(0))};
        } else if (primaryPlanItem != null) {
            params = new String[]{AtpHelper.atpIdToTermName(primaryPlanItem.getPlanPeriods().get(0))};
        } else if (secondaryPlanItem != null) {
            params = new String[]{AtpHelper.atpIdToTermName(secondaryPlanItem.getPlanPeriods().get(0))};
        }

        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_PLANNED_ITEM_ADDED, params);
    }

    /**
     * Plan Access for Adviser is updated in this method
     *
     * @param form
     * @param result
     * @param httprequest
     * @param httpresponse
     * @return
     */

    @RequestMapping(params = "methodToCall=planAccess")
    public ModelAndView planAccess(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                   HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (UserSessionHelper.isAdviser()) {
            String[] params = {};
            return doErrorPage(form, PlanConstants.ERROR_KEY_ADVISER_ACCESS, params);
        }
        List<LearningPlanInfo> plan = new ArrayList<LearningPlanInfo>();
        try {
            String studentId = UserSessionHelper.getStudentRegId();
            // Synchronized is used to ensure only one learning plan is created for a given student Id
            synchronized (studentId) {
                plan = getAcademicPlanService().getLearningPlansForStudentByType(studentId, PlanConstants.LEARNING_PLAN_TYPE_PLAN, PlanConstants.CONTEXT_INFO);
                if (!CollectionUtils.isEmpty(plan)) {
                    LearningPlanInfo learningPlan = plan.get(0);
                    if (!learningPlan.getShared().toString().equalsIgnoreCase(form.getEnableAdviserView())) {
                        if (form.getEnableAdviserView().equalsIgnoreCase(PlanConstants.LEARNING_PLAN_ITEM_SHARED_TRUE_KEY)) {
                            learningPlan.setShared(true);
                        } else {
                            learningPlan.setShared(false);
                        }
                        learningPlan.setStateKey(PlanConstants.LEARNING_PLAN_ACTIVE_STATE_KEY);
                        getAcademicPlanService().updateLearningPlan(learningPlan.getId(), learningPlan, PlanConstants.CONTEXT_INFO);
                    }
                } else {
                    LearningPlanInfo planInfo = new LearningPlanInfo();
                    planInfo.setTypeKey(PlanConstants.LEARNING_PLAN_TYPE_PLAN);
                    RichTextInfo rti = new RichTextInfo();
                    rti.setFormatted("");
                    rti.setPlain("");
                    if (form.getEnableAdviserView().equalsIgnoreCase(PlanConstants.LEARNING_PLAN_ITEM_SHARED_TRUE_KEY)) {
                        planInfo.setShared(true);
                    } else {
                        planInfo.setShared(false);
                    }
                    planInfo.setDescr(rti);
                    planInfo.setStudentId(studentId);
                    planInfo.setStateKey(PlanConstants.LEARNING_PLAN_ACTIVE_STATE_KEY);
                    planInfo.setMeta(new MetaInfo());

                    ContextInfo context = new ContextInfo();
                    context.setPrincipalId(studentId);
                    getAcademicPlanService().createLearningPlan(planInfo, context);
                }
            }
        } catch (Exception e) {
            return doOperationFailedError(form, "Query for default learning plan failed.", e);
        }


        return getUIFModelAndView(form);


    }

    /**
     * Adds the status alerts to the provided event if any planned activities are available
     *
     * @param courseCode
     * @param atpId
     */
    private void addStatusAlertEvents(String courseCode, String atpId, Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events) {
        //Add additional add events for Activity data if present
        List<ActivityOfferingItem> plannedActivities = getPlannedActivitiesByCourseAndTerm(courseCode, atpId);
        if (!CollectionUtils.isEmpty(plannedActivities)) {

            List<String> plannedActivityCodes = new ArrayList<String>();
            List<String> suspendedActivityCodes = new ArrayList<String>();
            List<String> withdrawnActivityCodes = new ArrayList<String>();

            for (ActivityOfferingItem activityOfferingItem : plannedActivities) {

                if (PlanConstants.SUSPENDED_STATE.equalsIgnoreCase(activityOfferingItem.getStateKey())) {

                    suspendedActivityCodes.add(activityOfferingItem.getCode());

                } else if (PlanConstants.WITHDRAWN_STATE.equalsIgnoreCase(activityOfferingItem.getStateKey())) {

                    withdrawnActivityCodes.add(activityOfferingItem.getCode());

                }

                plannedActivityCodes.add(activityOfferingItem.getCode());

            }

            String sections = StringUtils.join(plannedActivities.toArray(), ", ");

            if (sections != null) {
                events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED).put("sections", sections);
            }

            if (!withdrawnActivityCodes.isEmpty() || !suspendedActivityCodes.isEmpty()) {

                events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED).put("showAlert", "true");

                String statusAlert = events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED).get("statusAlert");

                StringBuffer sb = new StringBuffer();

                sb = sb.append(statusAlert);

                if (!withdrawnActivityCodes.isEmpty()) {
                    sb = sb.append(String.format(PlanConstants.WITHDRAWN_ALERT, StringUtils.join(withdrawnActivityCodes.toArray(), ", ")));
                }

                if (!suspendedActivityCodes.isEmpty()) {
                    sb = sb.append(String.format(PlanConstants.SUSPENDED_ALERT, StringUtils.join(suspendedActivityCodes.toArray(), ", ")));
                }

                events.get(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED).put("statusAlert", sb.toString());
            }
        }
    }

    /**
     * Returns synchronized learning plan
     *
     * @param studentId
     * @return
     */
    private LearningPlan getSynchronizedLearningPlan(String studentId) {
        LearningPlan plan = null;
        // Synchronized is used to ensure only one learning plan is created for a given student Id
        synchronized (studentId) {
            try {
                //  If something goes wrong with the query then a RuntimeException will be thrown. Otherwise, the method
                //  will return the default plan or null. Having multiple plans will also produce a RuntimeException.
                plan = getLearningPlan(studentId);
            } catch (RuntimeException e) {
                return null;
            }

            /*
            *  Create a default learning plan if there isn't one already and skip querying for plan items.
            */
            if (plan == null) {
                try {
                    plan = createDefaultLearningPlan(studentId);
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return plan;
    }

    /**
     * Build an HTML link to a specific ATP in the quarter view.
     *
     * @param atpId
     * @param text
     * @return
     */
    private String makeLinkToAtp(String atpId, String text) {
        return PlanConstants.QUARTER_LINK.replace("{atpId}", atpId).replace("{label}", text);
    }

    /**
     * Check for duplicate plan items by type.
     *
     * @param plan
     * @param atpId
     * @param courseId
     * @param planItemType
     * @return
     */
    private boolean isDuplicate(LearningPlan plan, String atpId, String courseId, String planItemType) {
        /*
         Make sure no dups exist. The rules are different for wishlist vs planned or backup courses.
        */
        boolean isDuplicate = false;
        if (planItemType.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
            if (getWishlistPlanItem(courseId) != null) {
                isDuplicate = true;
            }
        } else {
            if (getPlannedOrBackupPlanItem(courseId, atpId) != null) {
                isDuplicate = true;
            }
        }
        return isDuplicate;
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
        if (planItems == null) {
            throw new RuntimeException("Could not retrieve plan items.");
        } else {
            for (PlanItem p : planItems) {
                if (p.getPlanPeriods().get(0).equals(atpId) && p.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
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

    /**
     * Adds the wishList Item
     *
     * @param form
     * @param result
     * @param httprequest
     * @param httpresponse
     * @return
     */
    @RequestMapping(params = "methodToCall=addSavedCourse")
    public ModelAndView addSavedCourse(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                       HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (UserSessionHelper.isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }

        String courseId = form.getCourseId();
        if (StringUtils.isEmpty(courseId)) {
            return doOperationFailedError(form, "Course ID was missing.", null);
        }

        String studentId = UserSessionHelper.getStudentRegId();
        LearningPlan plan = getSynchronizedLearningPlan(studentId);
        if (plan == null) {
            return doOperationFailedError(form, "Unable to create learning plan.", null);
        }

        // Retrieve courseDetails based on the passed in CourseId and then update courseDetails based on the version independent Id
        CourseSummaryDetails courseDetails = null;
        // Now switch to the details based on the version independent Id
        //  Lookup course details as well need them in case there is an error below.
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseId);

            // Now switch the courseDetails based on the versionIndependent Id
            if (!courseId.equals(courseDetails.getVersionIndependentId())) {
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseDetails.getVersionIndependentId());
            }
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to retrieve Course Details.", null);
        }

        PlanItemInfo planItem = null;
        try {
            planItem = addPlanItem(plan, courseDetails.getVersionIndependentId(), null, PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, form.getNote());
        } catch (DuplicateEntryException e) {
            return doDuplicatePlanItem(form, null, courseDetails);
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to add plan item.", e);
        }

        //  Create events
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        events.putAll(makeAddEvent(planItem, courseDetails, form));

        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
        form.setJavascriptEvents(events);

        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_SAVED_ITEM_ADDED, new String[0]);
    }

    @RequestMapping(params = "methodToCall=removeItem")
    public ModelAndView removePlanItem(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                       HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (UserSessionHelper.isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }

        String planItemId = form.getPlanItemId();
        String courseId = form.getCourseId();
        if (StringUtils.isEmpty(planItemId) && StringUtils.isEmpty(courseId)) {
            return doOperationFailedError(form, "Plan item id and courseId are missing.", null);
        }

        if (StringUtils.isEmpty(planItemId)) {
            CourseSummaryDetails course = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseId);
            planItemId = getPlanIdFromCourseId(course.getVersionIndependentId(), PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        }

        String activityCode = null;
        //  See if the plan item exists.
        PlanItemInfo planItem = null;
        try {
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
        } catch (DoesNotExistException e) {
            return doPageRefreshError(form, String.format("No plan item with id [%s] exists.", planItemId), e);
        } catch (Exception e) {
            return doOperationFailedError(form, "Query for plan item failed.", e);
        }
        List<String> terms = new ArrayList();
        for (String term : planItem.getPlanPeriods()) {
            terms.add(AtpHelper.atpIdToTermName(term));
        }
        CourseSummaryDetails courseDetail = null;
        if (planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
            courseDetail = getCourseDetailsInquiryService().retrieveCourseSummaryById(planItem.getRefObjectId());
            courseId = courseDetail.getCourseId();
        } else {
            String activityId = planItem.getRefObjectId();
            ActivityOfferingDisplayInfo activityDisplayInfo = null;
            CourseOfferingInfo courseOfferingInfo = null;
            try {
                activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityId, PlanConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Could not retrieve ActivityOffering data for" + activityId, e);
            }
            if (activityDisplayInfo != null) {
                /*TODO: move this to Coursehelper to make it institution neutral*/
                String courseOfferingId = null;
                for (AttributeInfo attributeInfo : activityDisplayInfo.getAttributes()) {
                    if ("PrimaryActivityOfferingId".equalsIgnoreCase(attributeInfo.getKey())) {
                        courseOfferingId = attributeInfo.getValue();
                        break;
                    }
                }

                try {
                    courseOfferingInfo = getCourseOfferingService().getCourseOffering(courseOfferingId, CourseSearchConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error("Could not retrieve CourseOffering data for" + courseOfferingId, e);
                }

            }
            if (courseOfferingInfo != null && activityDisplayInfo != null) {
                courseId = courseOfferingInfo.getCourseId();
                courseDetail = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseId);
                activityCode = activityDisplayInfo.getActivityOfferingCode();

            } else {
                return doOperationFailedError(form, "Could not delete plan item", null);
            }
        }
        Map<String, String> planItemsToRemove = new HashMap<String, String>();
        if (!AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST.equals(planItem.getTypeKey())) {
            if (planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
                planItemsToRemove = getPlannedSectionsBySectionCd(courseDetail.getCode(), planItem, false, null);
            } else if (form.isPrimary()) {
                planItemsToRemove = getPlannedSectionsBySectionCd(courseDetail.getCode(), planItem, true, activityCode);
            }
        }

        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        //  Make events ...

        events.putAll(makeRemoveEvent(planItem, null, courseId, form, new ArrayList<String>(planItemsToRemove.values())));
        planItemsToRemove.put(planItemId, null);
        try {
            if (planItemsToRemove.size() > 0) {
                for (String planItemIdToRemove : planItemsToRemove.keySet()) {
                    getAcademicPlanService().deletePlanItem(planItemIdToRemove, UserSessionHelper.makeContextInfoInstance());
                }
            }
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not delete plan item", e);
        }

        if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
            events.putAll(makeUpdateTotalCreditsEvent(planItem.getPlanPeriods().get(0), PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));
        }

        form.setJavascriptEvents(events);
        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_ITEM_DELETED, new String[0]);
    }

    /**
     * @param courseCode
     * @param termId
     * @return
     */
    private List<ActivityOfferingItem> getPlannedActivitiesByCourseAndTerm(String courseCode, String termId) {
        List<ActivityOfferingItem> activityOfferingItems = new ArrayList<ActivityOfferingItem>();
        try {
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(UserSessionHelper.getStudentRegId(), PlanConstants.LEARNING_PLAN_TYPE_PLAN, CourseSearchConstants.CONTEXT_INFO);
            //This should be looping only once as student has only one learning plan of plan type
            for (LearningPlanInfo learningPlan : learningPlanList) {
                String learningPlanID = learningPlan.getId();
                List<PlanItemInfo> planItemList = getAcademicPlanService().getPlanItemsInPlanByAtp(learningPlanID, termId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, PlanConstants.CONTEXT_INFO);
                for (PlanItemInfo planItem : planItemList) {
                    String luType = planItem.getRefObjectType();
                    //TODO: if condition assumes refObjId for activities contains course codes
                    if (PlanConstants.SECTION_TYPE.equalsIgnoreCase(luType)) {
                        String activityOfferingId = planItem.getRefObjectId();
                        ActivityOfferingDisplayInfo activityDisplayInfo = null;
                        try {
                            activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityOfferingId, PlanConstants.CONTEXT_INFO);
                        } catch (Exception e) {
                            logger.error("Could not retrieve ActivityOffering data for" + activityOfferingId, e);
                        }
                        if (activityDisplayInfo != null) {
                            /*TODO: move this to Coursehelper to make it institution neutral*/
                            String courseOfferingId = null;
                            for (AttributeInfo attributeInfo : activityDisplayInfo.getAttributes()) {
                                if ("PrimaryActivityOfferingId".equalsIgnoreCase(attributeInfo.getKey())) {
                                    courseOfferingId = attributeInfo.getValue();
                                    break;
                                }
                            }
                            CourseOfferingInfo courseOfferingInfo = null;
                            try {
                                courseOfferingInfo = getCourseOfferingService().getCourseOffering(courseOfferingId, CourseSearchConstants.CONTEXT_INFO);
                                if (courseCode.equalsIgnoreCase(courseOfferingInfo.getCourseCode())) {
                                    ActivityOfferingItem activityOfferingItem = getCourseDetailsInquiryService().getActivityItem(activityDisplayInfo, courseOfferingInfo, !AtpHelper.isAtpSetToPlanning(termId), termId, planItem.getId());
                                    activityOfferingItems.add(activityOfferingItem);
                                }
                            } catch (Exception e) {
                                logger.error("Could not retrieve CourseOffering data for" + courseOfferingId, e);
                            }

                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error("Could not retrieve activities for course:" + courseCode + "for term:" + termId, e);
        }
        return activityOfferingItems;
    }


    /**
     * Used to get the Planned sections as a map with plan item id's and section codes using following params
     *
     * @param courseCd
     * @param planItem
     * @param sectionPrimary
     * @param activityCode
     * @return
     */
    private Map<String, String> getPlannedSectionsBySectionCd(String courseCd, PlanItem planItem, boolean sectionPrimary, String activityCode) {
        Map<String, String> plannedSections = new HashMap<String, String>();
        List<ActivityOfferingItem> activityOfferingItems = getPlannedActivitiesByCourseAndTerm(courseCd, planItem.getPlanPeriods().get(0));
        for (ActivityOfferingItem activityOfferingItem : activityOfferingItems) {
            if (!activityOfferingItem.getPlanItemId().equalsIgnoreCase(planItem.getId())) {
                if (sectionPrimary && activityCode != null && activityOfferingItem.getCode().startsWith(activityCode) && !activityOfferingItem.isPrimary()) {
                    plannedSections.put(activityOfferingItem.getPlanItemId(), activityOfferingItem.getRegistrationCode());
                } else if (!sectionPrimary) {
                    plannedSections.put(activityOfferingItem.getPlanItemId(), activityOfferingItem.getRegistrationCode());
                }
            }
        }

        return plannedSections;
    }

    /**
     * Blow-up response for all plan item actions for the Adviser.
     */

    private ModelAndView doAdviserAccessError(PlanForm form, String errorMessage, Exception e) {
        String[] params = {};
        return doErrorPage(form, errorMessage, PlanConstants.ERROR_KEY_ADVISER_ACCESS, params, e);
    }


    /**
     * Blow up response of the plan capacity validation fails.
     *
     * @param form
     * @return
     */
    private ModelAndView doPlanCapacityExceededError(PlanForm form, String type) {
        String errorId = PlanConstants.ERROR_KEY_PLANNED_ITEM_CAPACITY_EXCEEDED;
        if (type.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            errorId = PlanConstants.ERROR_KEY_BACKUP_ITEM_CAPACITY_EXCEEDED;
        }
        return doErrorPage(form, errorId, new String[0]);
    }

    /**
     * Blow up response if the user tries to update plan items in past terms.
     *
     * @param form
     * @return
     */
    private ModelAndView doCannotChangeHistoryError(PlanForm form) {
        return doErrorPage(form, PlanConstants.ERROR_KEY_HISTORICAL_ATP, new String[0]);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doPageRefreshError(PlanForm form, String errorMessage, Exception e) {
        // <a href="/student/myplan/plan?methodToCall=start&viewId=PlannedCourses-FormView">Reset your academic plan</a>
        // Removed link because html string is being encoded in the view
        String[] params = {};
        if (e != null) {
            logger.error(errorMessage, e);
        } else {
            logger.error(errorMessage);
        }
        return doErrorPage(form, errorMessage, PlanConstants.ERROR_KEY_PAGE_RESET_REQUIRED, params, e);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doOperationFailedError(PlanForm form, String errorMessage, Exception e) {
        String[] params = {};
        if (e != null) {
            logger.error(errorMessage, e);
        } else {
            logger.error(errorMessage);
        }
        return doErrorPage(form, errorMessage, PlanConstants.ERROR_KEY_OPERATION_FAILED, params, e);
    }

    /**
     * Logs errors and passes the request on to the error page.
     */
    private ModelAndView doErrorPage(PlanForm form, String errorMessage, String errorKey, String[] params, Exception e) {
        if (e != null) {
            logger.error(errorMessage, e);
        } else {
            logger.error(errorMessage);
        }
        return doErrorPage(form, errorKey, params);
    }

    /**
     * Initializes the error page.
     */
    private ModelAndView doErrorPage(PlanForm form, String errorKey, String[] params) {
        form.setRequestStatus(PlanForm.REQUEST_STATUS.ERROR);
        GlobalVariables.getMessageMap().clearErrorMessages();
        GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, errorKey, params);
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doDuplicatePlanItem(PlanForm form, String atpId, CourseSummaryDetails courseDetails) {
        /*String[] t = {"?", "?"};
try {
    t = AtpHelper.atpIdToTermNameAndYear(atpId);
} catch (RuntimeException e) {
    logger.error("Could not convert ATP ID to a term and year.", e);
}
String term = t[0] + " " + t[1];*/
        String[] params = {courseDetails.getCode(), AtpHelper.atpIdToTermName(atpId)};
        return doErrorPage(form, PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
    }

    /**
     * Success response for all plan item actions.
     */
    private ModelAndView doPlanActionSuccess(PlanForm form, String key, String[] params) {
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
        GlobalVariables.getMessageMap().putInfoForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, key, params);
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    private String getCourseDetailsAsJson(String courseId) {
        try {
            //  Also, add a full CourseDetails object so that course details properties are available to be displayed on the form.
            CourseSummaryDetails courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseId);
            //  Turn the list of javascript events into a string of JSON.
            String courseDetailsAsJson = mapper.writeValueAsString(courseDetails);
            return courseDetailsAsJson;
        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve Course Details.", e);
        }

    }


    // Course ID GUID, atp key id eg "uw.kuali.atp.2001.1"
    @RequestMapping(value = "/plan/enroll")
    public void getCourseSectionStatusAsJson(HttpServletResponse response, HttpServletRequest request) {
        try {
            String courseId = request.getParameter("courseId");
            String atpParam = request.getParameter("atpId");

            CourseSummaryDetails courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseId);

            List<AtpHelper.YearTerm> atpList = new ArrayList<AtpHelper.YearTerm>();
            if (atpParam == null || "".equals(atpParam.trim())) {

                List<String> list = courseDetails.getScheduledTerms();
                for (String item : list) {
                    AtpHelper.YearTerm yt = AtpHelper.termToYearTerm(item);
                    atpList.add(yt);
                }
            } else {
                AtpHelper.YearTerm yt = AtpHelper.atpToYearTerm(atpParam);
                atpList.add(yt);
            }

            String curric = courseDetails.getSubjectArea();
            String num = courseDetails.getCourseNumber();

            LinkedHashMap<String, LinkedHashMap<String, Object>> payload = new LinkedHashMap<String, LinkedHashMap<String, Object>>();
            for (AtpHelper.YearTerm yt : atpList) {
                getCourseHelper().getAllSectionStatus(payload, yt, curric, num);
            }

            String json = mapper.writeValueAsString(payload);
            response.setHeader("content-type", "application/json");
            response.setHeader("Cache-Control", "No-cache");
            response.setHeader("Cache-Control", "No-store");
            response.setHeader("Cache-Control", "max-age=0");
            response.getWriter().println(json);

        } catch (Exception e) {
            throw new RuntimeException("Unable to retrieve Course Details.", e);
        }

    }

    /**
     * Adds a plan item for the given course id and ATPs.
     *
     * @param plan         The learning plan to add the item to.
     * @param courseId     The id of the course.
     * @param atpId        A list of ATP/term ids if the plan item is a planned course.
     * @param planItemType Saved couse or planned course.
     * @return The newly created plan item or the existing plan item where a plan item already exists for the given course.
     * @throws RuntimeException on errors.
     */
    protected PlanItemInfo addPlanItem(LearningPlan plan, String courseId, String atpId, String planItemType, String note)
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

        if (null != atpId) {
            pii.setPlanPeriods(Arrays.asList(atpId));
        }

        //  Don't allow duplicates.
        if (isDuplicate(plan, atpId, courseId, planItemType)) {
            throw new DuplicateEntryException("Duplicate plan item exists.");
        }

        try {
            newPlanItem = getAcademicPlanService().createPlanItem(pii, UserSessionHelper.makeContextInfoInstance());
        } catch (Exception e) {
            logger.error("Could not create plan item.", e);
            throw new RuntimeException("Could not create plan item.", e);
        }

        return newPlanItem;
    }

    /**
     * @param plan
     * @param refObjId
     * @param atpId
     * @param planItemType
     * @return
     * @throws DuplicateEntryException
     */
    protected PlanItemInfo addActivityOfferingPlanItem(LearningPlan plan, String
            refObjId, String atpId, String planItemType)
            throws DuplicateEntryException {

        if (StringUtils.isEmpty(refObjId)) {
            throw new RuntimeException("Empty RefObjId");
        }

        PlanItemInfo newPlanItem = null;

        PlanItemInfo pii = new PlanItemInfo();
        pii.setLearningPlanId(plan.getId());
        pii.setTypeKey(planItemType);
        pii.setRefObjectType(PlanConstants.SECTION_TYPE);
        pii.setRefObjectId(refObjId);

        pii.setStateKey(PlanConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted("");
        rti.setPlain("");
        pii.setDescr(rti);

        if (null != atpId) {
            pii.setPlanPeriods(Arrays.asList(atpId));
        }

        //  Don't allow duplicates.
        if (isDuplicate(plan, atpId, refObjId, planItemType)) {
            throw new DuplicateEntryException("Duplicate plan item exists.");
        }

        try {
            newPlanItem = getAcademicPlanService().createPlanItem(pii, UserSessionHelper.makeContextInfoInstance());
        } catch (Exception e) {
            logger.error("Could not create plan item.", e);
            throw new RuntimeException("Could not create plan item.", e);
        }

        return newPlanItem;
    }

    /**
     * Gets a plan item of a particular type for a particular ATP.
     *
     * @param planId       The id of the learning plan
     * @param courseId     The id of the course
     * @param atpId        The ATP id
     * @param planItemType The plan item type key.
     * @return A "planned" or "backup" plan item. Or 'null' if none exists.
     * @throws RuntimeException on errors.
     */
    private PlanItemInfo getPlanItemByAtpAndType(String planId, String courseId, String atpId, String planItemType) {
        if (StringUtils.isEmpty(planId)) {
            throw new RuntimeException("Plan Id was empty.");
        }

        if (StringUtils.isEmpty(courseId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        if (StringUtils.isEmpty(atpId)) {
            throw new RuntimeException("ATP Id was empty.");
        }

        List<PlanItemInfo> planItems = null;
        PlanItemInfo item = null;

        try {
            planItems = getAcademicPlanService().getPlanItemsInPlanByAtp(planId, atpId, planItemType, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        for (PlanItemInfo p : planItems) {
            if (p.getRefObjectId().equals(courseId) && p.getTypeKey().equals(planItemType)) {
                item = p;
                break;
            }
        }

        //  A null here means that no plan item exists for the given course and ATP IDs.
        return item;
    }

    /**
     * Gets a Plan Item of type "wishlist" for a particular course. There should only ever be one.
     *
     * @param courseId The id of the course.
     * @return A PlanItem of type wishlist.
     */
    protected PlanItemInfo getWishlistPlanItem(String courseId) {

        if (StringUtils.isEmpty(courseId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        String studentId = UserSessionHelper.getStudentRegId();
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


        for (PlanItemInfo p : planItems) {
            if (p.getRefObjectId().equals(courseId)) {
                item = p;
                break;
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
    public PlanItemInfo getPlannedOrBackupPlanItem(String courseId, String atpId) {
        String studentId = UserSessionHelper.getStudentRegId();
        LearningPlan learningPlan = getLearningPlan(studentId);
        if (learningPlan == null) {
            return null;
        }

        PlanItemInfo planItem = null;

        try {
            planItem = getPlanItemByAtpAndType(learningPlan.getId(), courseId, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);
        } catch (Exception e) {
            logger.error("Could not retrieve plan items.", e);
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        if (planItem == null) {
            try {
                planItem = getPlanItemByAtpAndType(learningPlan.getId(), courseId, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
            } catch (Exception e) {
                logger.error("Could not retrieve plan items.", e);
                throw new RuntimeException("Could not retrieve plan items.", e);
            }
        }

        //  A null here means that no plan item exists for the given course and ATP IDs.
        return planItem;
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
    private LearningPlan createDefaultLearningPlan(String studentId) throws
            InvalidParameterException, DataValidationErrorException,
            MissingParameterException, AlreadyExistsException, PermissionDeniedException, OperationFailedException {

        LearningPlanInfo plan = new LearningPlanInfo();
        plan.setTypeKey(PlanConstants.LEARNING_PLAN_TYPE_PLAN);
        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted("");
        rti.setPlain("");
        plan.setShared(true);
        plan.setDescr(rti);
        plan.setStudentId(studentId);
        plan.setStateKey(PlanConstants.LEARNING_PLAN_ACTIVE_STATE_KEY);
        plan.setMeta(new MetaInfo());

        //  Set the user id in the context used in the web service call.
        ContextInfo context = new ContextInfo();
        context.setPrincipalId(UserSessionHelper.getStudentRegId());

        return getAcademicPlanService().createLearningPlan(plan, context);
    }

    /**
     * Creates events map for a remove.
     *
     * @param planItem
     * @return
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeRemoveEvent(PlanItemInfo
                                                                                          planItem, CourseSummaryDetails courseDetails, String courseId, PlanForm planForm, List<String> itemsToUpdate) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        Map<String, String> params = new HashMap<String, String>();

        //  Only planned or backup items get an atpId attribute.
        if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) ||
                planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            params.put("atpId", formatAtpIdForUI(planItem.getPlanPeriods().get(0)));
        }
        params.put("planItemType", formatTypeKey(planItem.getTypeKey()));
        params.put("planItemId", planItem.getId());
        //  Create Javascript events.
        String courseDetailsAsJson;
        try {
            if (courseDetails == null) {
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseId);
            }
            //  Serialize course details into a string of JSON.
            courseDetailsAsJson = mapper.writeValueAsString(courseDetails);
        } catch (Exception e) {
            logger.error("Could not convert javascript events to JSON.", e);
            throw new RuntimeException("Could not convert javascript events to JSON.", e);
        }
        String itemsToBeUpdated = null;
        if (itemsToUpdate != null && itemsToUpdate.size() > 0) {
            itemsToBeUpdated = StringUtils.join(itemsToUpdate.toArray(), ",");
        }

        params.put("courseDetails", courseDetailsAsJson);
        if (planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
            events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, params);
        } else {
            params.put("SectionCode", planForm.getSectionCode());
            params.put("RegistrationCode", planForm.getRegistrationCode());
            params.put("InstituteCode", planForm.getInstituteCode());
            params.put("shortTermName", AtpHelper.atpIdToShortTermName(planItem.getPlanPeriods().get(0)));
            params.put("ItemsToUpdate", itemsToBeUpdated);
            params.put("ActivityStateKey", planForm.getActivityStateKey());
            if (courseDetails.getCourseId() != null && planForm.getInstituteCode() != null && planForm.getSectionCode() != null) {
                String sectionCode = null;
                List<String> sections = new ArrayList<String>();
                if (!planForm.isPrimary()) {
                    sectionCode = planForm.getSectionCode().substring(0, 1);
                    sections.add(sectionCode);
                    sections.addAll(getPlannedSectionsBySectionCd(courseDetails.getCode(), planItem, true, sectionCode).values());
                    Collections.sort(sections);
                    params.put("PrimaryDeleteHoverText", StringUtils.join(sections, ", "));
                }

            }
            events.put(PlanConstants.JS_EVENT_NAME.SECTION_ITEM_DELETED, params);
        }
        return events;
    }

    /**
     * Creates an update credits event.
     *
     * @param atpId The id of the term.
     * @return
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeUpdateTotalCreditsEvent(String
                                                                                                      atpId, PlanConstants.JS_EVENT_NAME eventName) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        Map<String, String> params = new HashMap<String, String>();

        params.put("atpId", formatAtpIdForUI(atpId));
        String totalCredits = getPlannedTermsHelper().getTotalCredits(atpId);
        params.put("totalCredits", totalCredits);

        events.put(eventName, params);
        return events;
    }

    /**
     * Creates an add plan item event.
     *
     * @param planItem
     * @param courseDetails
     * @return
     * @throws RuntimeException if anything goes wrong.
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeAddEvent(PlanItemInfo
                                                                                       planItem, CourseSummaryDetails courseDetails, PlanForm planForm) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("planItemId", planItem.getId());
        params.put("planItemType", formatTypeKey(planItem.getTypeKey()));
        //  Only planned or backup items get an atpId attribute.
        if (planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) ||
                planItem.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            String atpId = planItem.getPlanPeriods().get(0);
            String termName = AtpHelper.atpIdToTermName(atpId);
            params.put("atpId", formatAtpIdForUI(atpId));
            // event for aler Icon
            List<String> publishedTerms = AtpHelper.getPublishedTerms();
            boolean scheduled = AtpHelper.isCourseOfferedInTerm(atpId, courseDetails.getCode());
            boolean timeScheduleOpen = publishedTerms.contains(atpId);
            boolean showAlert = false;
            if (timeScheduleOpen) {
                showAlert = !scheduled;
            }
            StringBuffer statusAlert = new StringBuffer();
            if (timeScheduleOpen && !scheduled) {
                statusAlert = statusAlert.append(String.format(PlanConstants.COURSE_NOT_SCHEDULE_ALERT, courseDetails.getCode(), termName));
            }
            params.put("showAlert", String.valueOf(showAlert));
            params.put("termName", termName);
            params.put("timeScheduleOpen", String.valueOf(timeScheduleOpen));
            params.put("statusAlert", statusAlert.toString());
            if (planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.SECTION_TYPE)) {
                params.put("SectionCode", planForm.getSectionCode());
                params.put("RegistrationCode", planForm.getRegistrationCode());
                params.put("PrimarySectionCode", planForm.getPrimarySectionCode());
                params.put("InstituteCode", planForm.getInstituteCode());
                params.put("Primary", String.valueOf(planForm.isPrimary()));
                params.put("PrimaryPlanItemId", planForm.getPrimaryPlanItemId());
                params.put("ItemsToUpdate", planForm.getPrimaryRegistrationCode());
                if (planForm.getCourseId() != null && planForm.getInstituteCode() != null && planForm.getSectionCode() != null) {
                    String sectionCode = null;
                    List<String> sections = new ArrayList<String>();
                    if (planForm.isPrimary()) {
                        sectionCode = planForm.getSectionCode();
                        sections.add(sectionCode);
                    } else {
                        sectionCode = planForm.getSectionCode().substring(0, 1);
                        sections.add(sectionCode);
                        sections.add(planForm.getSectionCode());
                    }
                    sections.addAll(getPlannedSectionsBySectionCd(courseDetails.getCode(), planItem, true, sectionCode).values());
                    Collections.sort(sections);
                    params.put("PrimaryDeleteHoverText", StringUtils.join(sections, ", "));
                }
            }
        }


        //  Create Javascript events.
        String courseDetailsAsJson;
        try {
            //  Serialize course details into a string of JSON.
            mapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            courseDetailsAsJson = mapper.writeValueAsString(courseDetails);
        } catch (Exception e) {
            throw new RuntimeException("Could not convert javascript events to JSON.", e);
        }
        params.put("courseDetails", courseDetailsAsJson);
        if (planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
            events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, params);
        } else if (planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.SECTION_TYPE)) {
            events.put(PlanConstants.JS_EVENT_NAME.SECTION_ITEM_ADDED, params);
        }
        return events;
    }

    private String formatAtpIdForUI(String atpId) {
        return atpId.replaceAll("\\.", "-");
    }

    private String formatTypeKey(String typeKey) {
        return typeKey.substring(typeKey.lastIndexOf(".") + 1);
    }


    private String getPlanIdFromCourseId(String courseId, String planItemType) {
        String planItemId = null;
        Person user = GlobalVariables.getUserSession().getPerson();
        String studentID = user.getPrincipalId();

        String planTypeKey = PlanConstants.LEARNING_PLAN_TYPE_PLAN;
        List<LearningPlanInfo> learningPlanList = null;

        try {
            learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(studentID, planTypeKey, CourseSearchConstants.CONTEXT_INFO);
            for (LearningPlanInfo learningPlan : learningPlanList) {
                String learningPlanID = learningPlan.getId();

                List<PlanItemInfo> planItemList = getAcademicPlanService().getPlanItemsInPlanByType(learningPlanID, planItemType, PlanConstants.CONTEXT_INFO);

                for (PlanItemInfo planItem : planItemList) {
                    if (planItem.getRefObjectId().equalsIgnoreCase(courseId)) {
                        planItemId = planItem.getId();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("could not get the planItemId");
        }
        return planItemId;
    }

    /**
     * User who has no Learning Plan or auditInfo is considered a new User.
     *
     * @return
     */
    private boolean isNewUser() {
        boolean isNewUser = false;
        try {
            String regId = UserSessionHelper.getStudentRegId();
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(regId, PlanConstants.LEARNING_PLAN_TYPE_PLAN, CourseSearchConstants.CONTEXT_INFO);
            /*check if any audits are ran ! if no plans found*/
            if (learningPlanList.isEmpty()) {
                if (regId != null) {
                    Date startDate = new Date();
                    Date endDate = new Date();
                    List<AuditReportInfo> auditReportInfoList = getDegreeAuditService().getAuditsForStudentInDateRange(regId, startDate, endDate, DegreeAuditServiceConstants.DEGREE_AUDIT_SERVICE_CONTEXT);
                    isNewUser = auditReportInfoList.isEmpty();
                }
            }
        } catch (Exception e) {
            logger.error("Could not retrieve info", e);
        }
        return isNewUser;
    }

    public DegreeAuditService getDegreeAuditService() {
        if (degreeAuditService == null) {
            degreeAuditService = (DegreeAuditService)
                    GlobalResourceLoader.getService(new QName(DegreeAuditServiceConstants.NAMESPACE,
                            DegreeAuditServiceConstants.SERVICE_NAME));
        }
        return degreeAuditService;
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

    public synchronized CourseDetailsInquiryHelperImpl getCourseDetailsInquiryService() {
        if (courseDetailsInquiryService == null) {
            courseDetailsInquiryService = new CourseDetailsInquiryHelperImpl();
        }
        return courseDetailsInquiryService;
    }

    public void setCourseDetailsInquiryService(CourseDetailsInquiryHelperImpl courseDetailsInquiryService) {
        this.courseDetailsInquiryService = courseDetailsInquiryService;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }


    @Override
    @RequestMapping(method = RequestMethod.GET, params = "methodToCall=performFieldSuggest")
    public
    @ResponseBody
    AttributeQueryResult performFieldSuggest(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                                             HttpServletRequest request, HttpServletResponse response) {


        // invoke attribute query service to perform the query
        AttributeQueryResult queryResult = new AttributeQueryResult();

        Object[] placeHolders = new Object[]{
                "Capstone",
                "Composition",
                "Elective",
                "Foreign Language",
                "I&S (Individuals and Societies)",
                "Independent Study",
                "Internship",
                "Job",
                "NW (Natural World)",
                "Other",
                "Practicum",
                "Project",
                "Q/SR (Quantitative and Symbolic Reasoning)",
                "Research",
                "Seminar",
                "Study Abroad",
                "Thesis",
                "VLPA (Visual, Literary, and Performing Arts)",
                "W (Writing)"
        };

        queryResult.setResultData(Arrays.asList(placeHolders));

        return queryResult;
    }
}