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

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
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
import org.kuali.student.myplan.comment.util.CommentHelper;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.dataobject.RecommendedItemDataObject;
import org.kuali.student.myplan.plan.form.PlanForm;
import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;
import org.kuali.student.myplan.plan.util.*;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.MetaInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.HtmlUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
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

    @Autowired
    private CommentHelper commentHelper;

    @Autowired
    private PlanHelper planHelper;

    @Autowired
    private UserSessionHelper userSessionHelper;

    private PlannedTermsHelperBase plannedTermsHelper;

    private transient CourseDetailsInquiryHelperImpl courseDetailsInquiryService;

    private transient CourseOfferingService courseOfferingService;

    //  Java to JSON outputter.
    private transient ObjectMapper mapper = new ObjectMapper();

    // Used for getting the term and year from Atp
    private transient AtpHelper atpHelper;

    private transient AcademicRecordService academicRecordService;


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

            plan = getAcademicPlanService().getLearningPlansForStudentByType(getUserSessionHelper().getStudentId(),
                    PlanConstants.LEARNING_PLAN_TYPE_PLAN, PlanConstants.CONTEXT_INFO);

            if (!CollectionUtils.isEmpty(plan)) {

                //A student should have only one learning plan associated to his Id
                LearningPlan learningPlan = plan.get(0);

                planForm.setEnableAdviserView(learningPlan.getShared().toString());

                List<PlanItemInfo> planItems = getAcademicPlanService().getPlanItemsInPlanByType(learningPlan.getId(),
                        PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, PlanConstants.CONTEXT_INFO);

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
            messages = commentQueryHelper.getMessages(getUserSessionHelper().getStudentId());
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
        // ignore the form returned by super.start()
        PlanForm planForm = (PlanForm) form;


        /**
         * Loading and returning quickAdd view if requested
         * Pre-populating the data for quickAdd view if requested for edit
         *
         */
        if (PlanConstants.ADD_DIALOG_PAGE.equals(form.getPageId()) ||
                PlanConstants.EDIT_NOTE_PAGE.equals(form.getPageId()) ||
                PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE.equals(form.getPageId())
                ) {

            if (hasText(planForm.getAtpId())) {
                String termYear = AtpHelper.atpIdToTermName(planForm.getAtpId());
                planForm.setTermName(termYear);
            } else {
                return doPageRefreshError(planForm, "Could not open Quick Add.", null);
            }

            if (hasText(planForm.getPlanItemId())) {
                try {
                    PlanItemInfo planItemInfo = getAcademicPlanService().getPlanItem(planForm.getPlanItemId(), PlanConstants.CONTEXT_INFO);
                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP.equalsIgnoreCase(planItemInfo.getTypeKey()) && !planForm.isBackup()) {
                        return doPageRefreshError(planForm, "Plan item not found.", null);
                    }

                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equalsIgnoreCase(planItemInfo.getTypeKey()) && planForm.isBackup()) {
                        return doPageRefreshError(planForm, "Plan item not found.", null);
                    }
                    if (hasText(planItemInfo.getDescr().getPlain())) {
                        planForm.setNote(planItemInfo.getDescr().getPlain());
                    }
                    if (isPlaceHolderType(planItemInfo.getRefObjectType())) {
                        if (hasText(planItemInfo.getRefObjectId())) {
                            if (PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(planItemInfo.getRefObjectType())) {
                                planForm.setCourseCd(planItemInfo.getRefObjectId());
                            } else {
                                planForm.setType(PlanConstants.GENERAL_TYPE);
                                planForm.setGeneralPlaceholder(String.format("%s|%s", planItemInfo.getRefObjectId(),
                                        planItemInfo.getRefObjectType()));
                                planForm.setPlaceholderCode(EnumerationHelper.getEnumAbbrValForCodeByType(planItemInfo.getRefObjectId(), planItemInfo.getRefObjectType()));
                            }
                            if (planItemInfo.getCredit() != null) {
                                planForm.setCredit(String.valueOf(planItemInfo.getCredit().intValue()));
                            }
                        }
                    } else {
                        if (hasText(planItemInfo.getRefObjectId())) {
                            CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(planItemInfo.getRefObjectId(), getPlanHelper().getCrossListedCourse(planItemInfo.getAttributes()));
                            if (courseInfo != null && hasText(courseInfo.getCode())) {
                                planForm.setCourseCd(courseInfo.getCode());
                            }
                        }
                    }
                } catch (DoesNotExistException e) {
                    return doPageRefreshError(planForm, "PlanItem with Id:" + planForm.getPlanItemId() + " does not exist", e);
                } catch (Exception e) {
                    return doOperationFailedError(planForm, "Could not open Quick Add.", null);
                }
            }
            return getUIFModelAndView(planForm);
        }


        //Plan activities are needed only for move, copy, delete functionality
        List<String> activitiesRequiredPages = Arrays.asList(PlanConstants.MOVE_DIALOG_PAGE,
                PlanConstants.COPY_DIALOG_PAGE, PlanConstants.DELETE_DIALOG_PAGE);
        PlanItemInfo planItem = null;
        String planItemAtpId = null;


        /**
         * Loading planItem for all Planned Items in plan with a planItemId (planned, backup, wishList)
         */
        if (planForm.getPlanItemId() != null) {

            try {

                planItem = getAcademicPlanService().getPlanItem(planForm.getPlanItemId(), PlanConstants.CONTEXT_INFO);

                if (planItem != null) {
                    if (!CollectionUtils.isEmpty(planItem.getPlanPeriods())) {
                        //Assuming plan Item can only have one plan period
                        planItemAtpId = planItem.getPlanPeriods().get(0);
                        if (planForm.getAtpId() != null && !planForm.getAtpId().equals(planItemAtpId)) {
                            return doPageRefreshError(planForm, "Plan item quarter is changed.", null);
                        }
                        if (planItemAtpId.equalsIgnoreCase(planForm.getAtpId()) && !planForm.isSetToPlanning()) {
                            planForm.setSetToPlanning(AtpHelper.isAtpSetToPlanning(planItemAtpId));
                        }
                        if (planForm.getAtpId() == null) {
                            planForm.setAtpId(planItemAtpId);
                        }
                        planForm.setTermName(AtpHelper.atpIdToTermName(planForm.getAtpId()));
                    }

                    planForm.setAdviserName(getUserSessionHelper().getCapitalizedName(planItem.getMeta().getCreateId()));
                    planForm.setDateAdded(planItem.getMeta().getCreateTime());


                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP.equalsIgnoreCase(planItem.getTypeKey()) && !planForm.isBackup()) {
                        return doPageRefreshError(planForm, "Plan item not found.", null);
                    }

                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equalsIgnoreCase(planItem.getTypeKey()) && planForm.isBackup()) {
                        return doPageRefreshError(planForm, "Plan item not found.", null);
                    }

                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItem.getTypeKey())) {
                        planForm.setOwner(getUserSessionHelper().getCurrentUserId().equals(planItem.getMeta().getCreateId()));
                        planForm.setRecommended(true);
                    }

                    if (hasText(planItem.getDescr().getPlain()) && !planForm.isSetToPlanning() && planForm.isRecommended()) {
                        planForm.setAdviserNote(planItem.getDescr().getPlain());
                    } else {
                        planForm.setNote(planItem.getDescr().getPlain());
                    }

                    if (PlanConstants.PLACE_HOLDER_TYPE_GEN_ED.equals(planItem.getRefObjectType())
                            || PlanConstants.PLACE_HOLDER_TYPE.equals(planItem.getRefObjectType())) {
                        planForm.setGeneralPlaceholder(String.format("%s|%s", planItem.getRefObjectId(), planItem.getRefObjectType()));
                        if (planItem.getCredit() != null) {
                            planForm.setCredit(String.valueOf(planItem.getCredit().intValue()));
                        }
                        planForm.setPlaceholderCode(EnumerationHelper.getEnumAbbrValForCodeByType(planItem.getRefObjectId(), planItem.getRefObjectType()));
                        planForm.setPlaceholderTitle(EnumerationHelper.getEnumValueForCodeByType(planItem.getRefObjectId(), planItem.getRefObjectType()));
                        planForm.setType(PlanConstants.GENERAL_TYPE);
                        return getUIFModelAndView(planForm);
                    } else if (PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(planItem.getRefObjectType())) {
                        planForm.setCourseCd(planItem.getRefObjectId());
                        DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(planItem.getRefObjectId());
                        Map<String, String> subjectAreas = OrgHelper.getTrimmedSubjectAreas();
                        String subjectTitle = subjectAreas.get(courseCode.getSubject());
                        String subjectLevel = courseCode.getNumber().toUpperCase().replace(CourseSearchConstants.COURSE_LEVEL_XX, CourseSearchConstants.COURSE_LEVEL_ZERO);
                        planForm.setPlaceholderTitle(String.format("%s %s level", subjectTitle, subjectLevel));
                        if (planItem.getCredit() != null) {
                            planForm.setCredit(String.valueOf(planItem.getCredit().intValue()));
                        }
                        return getUIFModelAndView(planForm);
                    } else {
                        planForm.setCourseId(planItem.getRefObjectId());
                        planForm.setCode(getPlanHelper().getCrossListedCourse(planItem.getAttributes()));
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

            planForm.setCourseSummaryDetails(getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(planForm.getCourseId(), planForm.getCode()));
            if (StringUtils.isEmpty(planForm.getCode()) && planForm.getCourseSummaryDetails() != null && planForm.getCourseSummaryDetails().getCourseId() != null) {
                planForm.setCode(planForm.getCourseSummaryDetails().getCode());
            }
        } catch (Exception e) {

            planForm.setCourseSummaryDetails(new CourseSummaryDetails());
            GlobalVariables.getMessageMap().clearErrorMessages();
            GlobalVariables.getMessageMap().putErrorForSectionId(PlanConstants.ACTION_MENU_PAGE_ID, PlanConstants.ERROR_KEY_UNKNOWN_COURSE);
            return doPageRefreshError(planForm, "Could not load course details.", e);

        }

        /**
         * populating the Plan related Information
         */
        planForm.setPlannedCourseSummary(getCourseDetailsInquiryService().getPlannedCourseSummaryByIdAndCd(planForm.getCourseId(), planForm.getCode(), getUserSessionHelper().getStudentId()));


        /*Setting the atpId to the first recommended unplanned term */
        if (!CollectionUtils.isEmpty(planForm.getPlannedCourseSummary().getRecommendedItemDataObjects()) && !PlanConstants.RECOMMENDED_DIALOG_PAGE.equals(planForm.getPageId())) {
            for (RecommendedItemDataObject recommendedItemDataObject : planForm.getPlannedCourseSummary().getRecommendedItemDataObjects()) {
                if (!recommendedItemDataObject.isPlanned() && AtpHelper.isAtpSetToPlanning(recommendedItemDataObject.getAtpId())) {
                    planForm.setAtpId(recommendedItemDataObject.getAtpId());
                    break;
                }
            }
        }


        /**
         * Populating the planActivities (Activity Offerings which are planned)
         */
        if (activitiesRequiredPages.contains(form.getPageId()) && planItemAtpId != null) {
            String atpId = CollectionUtils.isEmpty(planItem.getPlanPeriods()) ? null : planItem.getPlanPeriods().get(0);
            if (planForm.getCourseSummaryDetails() != null && planForm.getCourseSummaryDetails().getCode() != null && AtpHelper.isAtpSetToPlanning(atpId)) {
                planForm.setPlanActivities(getPlannedActivitiesByCourseAndTerm(planForm.getCourseSummaryDetails().getCode(), atpId));
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

        if (getUserSessionHelper().isAdviser()) {
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
        } catch (DoesNotExistException e) {
            return doPageRefreshError(form, "PlanItem with Id:" + planItemId + " doesnot exist", e);
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
            return doPageRefreshError(form, "Move planned item was not type planned.", null);
        }

        //  Validate: Capacity.
        boolean hasCapacity = false;
        try {
            hasCapacity = isAtpHasCapacity(getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId()),
                    planItemAtpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
        }

        if (!hasCapacity) {
            return doPlanCapacityExceededError(form, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        }

        //  Load course summary details.
        CourseSummaryDetails courseDetails = null;
        if (!isPlaceHolderType(planItem.getRefObjectType())) {
            try {
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(planItem.getRefObjectId(), getPlanHelper().getCrossListedCourse(planItem.getAttributes()));
            } catch (Exception e) {
                return doOperationFailedError(form, "Unable to retrieve Course Details.", e);
            }
        } else {
            courseDetails = new CourseSummaryDetails();
        }

        //  Make removed event before updating the plan item.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> removeEvent = makeRemoveEvent(planItem, courseDetails, form, null);

        //  Update
        planItem.setTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP);
        try {
            getAcademicPlanService().updatePlanItem(planItemId, planItem, getUserSessionHelper().makeContextInfoInstance());
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not update plan item.", e);
        }

        //  Make events (delete, add, update credits).
        //  Set the javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        events.putAll(removeEvent);
        events.putAll(makeAddUpdateEvent(planItem, courseDetails, form, false));

        if (courseDetails != null && hasText(courseDetails.getCode())) {
            addStatusAlertEvents(courseDetails.getCode(), planItemAtpId, events, false);
        }

        events.putAll(makeUpdateTotalCreditsEvent(planItemAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        form.setJavascriptEvents(events);

        //  Pass the ATP name in the params.
        String[] params = {makeLinkToAtp(planItemAtpId, AtpHelper.atpIdToTermName(planItemAtpId))};
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

        if (getUserSessionHelper().isAdviser()) {
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
        } catch (DoesNotExistException e) {
            return doPageRefreshError(form, "PlanItem with Id:" + planItemId + " doesnot exist", e);
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
            return doPageRefreshError(form, "Move planned item was not type backup.", null);
        }

        //  Validate: Capacity.
        boolean hasCapacity = false;
        try {
            hasCapacity = isAtpHasCapacity(getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId()),
                    planItemAtpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
        }

        if (!hasCapacity) {
            return doPlanCapacityExceededError(form, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);
        }

        //  Load course summary details.
        CourseSummaryDetails courseDetails = null;
        if (!isPlaceHolderType(planItem.getRefObjectType())) {
            try {
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(planItem.getRefObjectId(), getPlanHelper().getCrossListedCourse(planItem.getAttributes()));
            } catch (Exception e) {
                return doOperationFailedError(form, "Unable to retrieve Course Details.", e);
            }
        } else {
            courseDetails = new CourseSummaryDetails();
        }

        //  Make removed event before updating the plan item.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> removeEvent = makeRemoveEvent(planItem, courseDetails, form, null);

        //  Set type to "planned".
        planItem.setTypeKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED);

        //  Update
        try {
            getAcademicPlanService().updatePlanItem(planItemId, planItem, getUserSessionHelper().makeContextInfoInstance());
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not update plan item.", e);
        }

        //  Make events (delete, add, update credits).
        //  Set the javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        events.putAll(removeEvent);
        events.putAll(makeAddUpdateEvent(planItem, courseDetails, form, false));

        if (courseDetails != null && hasText(courseDetails.getCode())) {
            addStatusAlertEvents(courseDetails.getCode(), planItemAtpId, events, false);
        }

        events.putAll(makeUpdateTotalCreditsEvent(planItemAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        form.setJavascriptEvents(events);

        String[] params = {makeLinkToAtp(planItemAtpId, AtpHelper.atpIdToTermName(planItemAtpId))};
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
        if (getUserSessionHelper().isAdviser()) {
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
        } catch (DoesNotExistException e) {
            return doPageRefreshError(form, "PlanItem with Id:" + planItemId + " doesnot exist", e);
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not fetch plan item.", e);
        }

        if (planItem == null) {
            return doOperationFailedError(form, String.format("Could not fetch plan item."), null);
        }

        //  Lookup course details as they will be needed for errors.
        CourseSummaryDetails courseDetails = null;
        if (!isPlaceHolderType(planItem.getRefObjectType())) {
            try {
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(planItem.getRefObjectId(), getPlanHelper().getCrossListedCourse(planItem.getAttributes()));
            } catch (Exception e) {
                return doOperationFailedError(form, "Unable to retrieve Course Details.", null);
            }
        } else {
            courseDetails = new CourseSummaryDetails();
        }

        //  Make sure there isn't a plan item for the same course id in the destination ATP.
        // unless maybe it's a placeholder? could go either way, but assume user actually wants additional placeholder
        if (!isPlaceHolderType(planItem.getRefObjectType())) {
            PlanItemInfo existingPlanItem = null;
            try {
                existingPlanItem = getPlanHelper().getPlannedOrBackupPlanItem(planItem.getRefObjectId(), getPlanHelper().getCrossListedCourse(planItem.getAttributes()), newAtpId);
            } catch (RuntimeException e) {
                return doOperationFailedError(form, "Query for existing plan item failed.", null);
            }

            if (existingPlanItem != null) {
                String[] params = {courseDetails.getCode(), AtpHelper.atpIdToTermName(newAtpId)};
                return doErrorPage(form, PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
            }
        }

        /*Remove the Planned Sections for this course before moving the course to another term*/
        Map<String, String> planItemsToRemove = new HashMap<String, String>();

        if (courseDetails != null && courseDetails.getCourseId() != null && planItem != null && planItem.getPlanPeriods().size() > 0) {
            planItemsToRemove = getPlannedSectionsBySectionCd(courseDetails.getCode(), planItem, false, null);
        }

        try {

            if (!planItemsToRemove.isEmpty()) {
                for (String planItemIdToRemove : planItemsToRemove.keySet()) {
                    getAcademicPlanService().deletePlanItem(planItemIdToRemove, getUserSessionHelper().makeContextInfoInstance());
                }
            }

        } catch (Exception e) {
            return doOperationFailedError(form, "Could not delete plan item", e);
        }

        //  Create events before updating the plan item.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> originalRemoveEvents = makeRemoveEvent(planItem, courseDetails, form, null);
        //  Save the source ATP ID to create credit total updates later.
        String originalAtpId = planItem.getPlanPeriods().get(0);

        //  Do validations.
        //  Validate: Plan Size exceeded.
        LearningPlan learningPlan = null;
        boolean hasCapacity = false;
        try {
            learningPlan = getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId());
            hasCapacity = isAtpHasCapacity(getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId()), newAtpId, planItem.getTypeKey());
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
            getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, getUserSessionHelper().makeContextInfoInstance());
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not udpate plan item.", e);
        }

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);

        //  Make Javascript UI events (delete, add, update credits).
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        /*Recommended item updated to accepted if a course or placeholder added from recommended dialog
                or only for courses added, moved or copied from other qtr's*/
        if (PlanConstants.COURSE_TYPE.equals(planItem.getRefObjectType())) {
            updateRecommendedItem(planItem, learningPlan, events);
        }


        //  Add events generated for the plan item before it was updated.
        events.putAll(originalRemoveEvents);
        //  Create update total credits on source ATP.
        events.putAll(makeUpdateTotalCreditsEvent(originalAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_OLD_TERM_TOTAL_CREDITS));

        try {
            events.putAll(makeAddUpdateEvent(planItem, courseDetails, form, false));
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Unable to create add event.", e);
        }
        events.putAll(makeUpdateTotalCreditsEvent(newAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        form.setJavascriptEvents(events);

        String link = makeLinkToAtp(newAtpId, AtpHelper.atpIdToTermName(newAtpId));
        String[] params = {link};
        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_PLANNED_ITEM_MOVED, params);
    }

    /**
     * Copy a course planned to requested term
     * And also used for adding the Recommend Item from recommended Dialog
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
        if (getUserSessionHelper().isAdviser()) {
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
        } catch (DoesNotExistException e) {
            return doPageRefreshError(form, "PlanItem with Id:" + planItemId + " doesnot exist", e);
        } catch (Exception e) {
            return doOperationFailedError(form, "Could not fetch plan item.", e);
        }

        if (planItem == null) {
            return doOperationFailedError(form, String.format("Could not fetch plan item."), null);
        }
        /*isRecommended --> true (we are copying the course from recommended section of the qtr to planned section of same qtr)*/
        String planType = form.isRecommended() ? PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED : planItem.getTypeKey();

        boolean isCrossListedCourse = false;
        String credit = null;
        //  Lookup course details as they will be needed for errors.
        CourseSummaryDetails courseDetails = null;
        if (!isPlaceHolderType(planItem.getRefObjectType())) {
            try {
                String crossListedCourse = getPlanHelper().getCrossListedCourse(planItem.getAttributes());
                isCrossListedCourse = !StringUtils.isEmpty(crossListedCourse);
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(planItem.getRefObjectId(), crossListedCourse);
            } catch (Exception e) {
                return doOperationFailedError(form, "Unable to retrieve Course Details.", e);
            }
        } else {
            courseDetails = new CourseSummaryDetails();
            if (planItem.getCredit() != null) {
                credit = String.valueOf(planItem.getCredit().intValue());
            }
        }

        //  Make sure there isn't a plan item for the same course id in the destination ATP.
        // unless maybe it's a placeholder? could go either way, but assume user actually wants additional placeholder
        if (!isPlaceHolderType(planItem.getRefObjectType())) {
            PlanItemInfo existingPlanItem = null;
            try {
                existingPlanItem = getPlanHelper().getPlannedOrBackupPlanItem(planItem.getRefObjectId(), getPlanHelper().getCrossListedCourse(planItem.getAttributes()), newAtpId);
            } catch (RuntimeException e) {
                return doOperationFailedError(form, "Query for existing plan item failed.", e);
            }

            if (existingPlanItem != null) {
                String[] params = {courseDetails.getCode(), AtpHelper.atpIdToTermName(newAtpId)};
                return doErrorPage(form, PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
            }
        }

        //  Do validations.
        //  Validate: Plan Size exceeded.
        boolean hasCapacity = false;
        LearningPlan learningPlan = null;
        try {
            learningPlan = getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId());
            hasCapacity = isAtpHasCapacity(learningPlan, newAtpId, planType);
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
        }
        if (!hasCapacity) {
            return doPlanCapacityExceededError(form, planType);
        }

        //  Validate: Adding to historical term.
        if (!AtpHelper.isAtpSetToPlanning(newAtpId)) {
            return doCannotChangeHistoryError(form);
        }

        //  Update the plan item.
        planItem.setPlanPeriods(Arrays.asList(newAtpId));
        //  Do not allow diagonal moves .
        //planItem.setTypeKey(newType);

        //  Create the map of javascript event(s) that should be thrown in the UI.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        PlanItemInfo planItemCopy = null;
        try {
            String note = form.isRecommended() ? null : planItem.getDescr().getPlain();
            planItemCopy = addPlanItem(learningPlan, planItem.getRefObjectId(), planItem.getRefObjectType(),
                    newAtpId, planType, note, credit, isCrossListedCourse ? courseDetails.getCode() : null);
            /*Recommended item updated to accepted if a course or placeholder added from recommended dialog
            or only for courses added, moved or copied from other qtr's*/
            if (form.isRecommended() || PlanConstants.COURSE_TYPE.equals(planItemCopy.getRefObjectType())) {
                updateRecommendedItem(planItem, learningPlan, events);
            }
        } catch (DuplicateEntryException e) {
            return doDuplicatePlanItem(form, newAtpId, courseDetails.getCode());
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to add plan item.", e);
        }

        //  Set the status of the request for the UI.
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);


        try {
            events.putAll(makeAddUpdateEvent(planItemCopy, courseDetails, form, false));
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Unable to create add event.", e);
        }

        events.putAll(makeUpdateTotalCreditsEvent(newAtpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        //  Populate the form.
        form.setJavascriptEvents(events);

        String link = makeLinkToAtp(newAtpId, AtpHelper.atpIdToTermName(newAtpId));
        String[] params = {link};
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
    @RequestMapping(params = "methodToCall=addUpdatePlanItem")
    public ModelAndView addUpdatePlanItem(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                          HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (getUserSessionHelper().isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }
        /* Should the course be type 'planned' or 'backup'. Default to planned.*/
        String newType = form.isBackup() ? PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP : PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED;
        return addUpdateItem(form, newType);
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
    @RequestMapping(params = "methodToCall=addRecommendedPlanItem")
    public ModelAndView addRecommendedPlanItem(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                               HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (!getUserSessionHelper().isAdviser()) {
            return doStudentAccessError(form, "Student Access Denied", null);
        }
        return addUpdateItem(form, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED);
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
        if (getUserSessionHelper().isAdviser()) {
            String[] params = {};
            return doErrorPage(form, PlanConstants.ERROR_KEY_ADVISER_ACCESS, params);
        }
        List<LearningPlanInfo> plan = new ArrayList<LearningPlanInfo>();
        try {
            String studentId = getUserSessionHelper().getStudentId();
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
     * Update PlanItem Note
     *
     * @param form
     * @param result
     * @param httprequest
     * @param httpresponse
     * @return
     */
    @RequestMapping(params = "methodToCall=updateNote")
    public ModelAndView updateNote(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                   HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        PlanItemInfo planItemInfo = null;
        CourseSummaryDetails courseSummaryDetails = null;
        String atpId = null;
        if (form.getPlanItemId() != null) {

            try {
                planItemInfo = getAcademicPlanService().getPlanItem(form.getPlanItemId(), PlanConstants.CONTEXT_INFO);
                if (PlanConstants.PLACE_HOLDER_OTHER_CODE.equals(planItemInfo.getRefObjectId()) && !hasText(form.getNote())) {
                    return doErrorPage(form, "Note required", PlanConstants.NOTE_REQUIRED, new String[]{}, null);
                }
                planItemInfo.getDescr().setPlain(form.getNote());
                planItemInfo.getDescr().setFormatted(form.getNote());
                planItemInfo = getAcademicPlanService().updatePlanItem(form.getPlanItemId(), planItemInfo, PlanConstants.CONTEXT_INFO);
                if (!isPlaceHolderType(planItemInfo.getRefObjectType())) {
                    courseSummaryDetails = getVersionVerifiedCourseDetails(planItemInfo.getRefObjectId(), null);
                    atpId = planItemInfo.getPlanPeriods().get(0);
                }

            } catch (DoesNotExistException e) {
                return doPageRefreshError(form, "PlanItem with Id:" + form.getPlanItemId() + " doesnot exist", e);
            } catch (Exception e) {

                return doOperationFailedError(form, "Failed to get PlanItem for planItemId " + form.getPlanItemId(), e);

            }

        } else {

            return doOperationFailedError(form, "Failed to update note", null);

        }

        //  Create events
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        events.putAll(makeAddUpdateEvent(planItemInfo, courseSummaryDetails, form, true));

        if (courseSummaryDetails != null && hasText(courseSummaryDetails.getCode())) {
            addStatusAlertEvents(courseSummaryDetails.getCode(), atpId, events, true);
        }

        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
        form.setJavascriptEvents(events);

        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_UPDATED_ITEM, new String[]{});
    }


    /**
     * Method used to add and update planItems
     * Adds and updates a course, course placeHolder  general placeHolder planItems
     * Adds a recommended Course plan Item
     *
     * @param form
     * @param newType
     * @return
     */
    private ModelAndView addUpdateItem(PlanForm form, String newType) {
        /*
        * ************************************************************************************************************
        *                                  QuickAdd a course or placeholder to Plan
        * ************************************************************************************************************
        */


        boolean addCourse = true;
        boolean addPlaceHolder = false;
        boolean addPrimaryCourse = false;
        boolean addSecondaryCourse = false;
        boolean isCrossListedCourse = false;
        boolean isRecommendedItem = PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(newType);
        CourseSummaryDetails courseDetails = null;
        String studentId = getUserSessionHelper().getStudentId();


        String planItemId = form.getPlanItemId();

        String quickAddType = form.getType();
        String courseCd = form.getCourseCd();
        String placeHolderId = null;
        String placeHolderType = null;
        String placeHolderCd = null;
        String placeHolderTitle = null;
        if (hasText(form.getGeneralPlaceholder())) {
            if (PlanConstants.DEFAULT_KEY.equals(form.getGeneralPlaceholder())) {
                return doErrorPage(form, "Select a placeHolder", PlanConstants.DEFAULT_PLACEHOLDER_ERROR, new String[]{}, null);
            }
            String[] placeHolder = form.getGeneralPlaceholder().split(PlanConstants.CODE_KEY_SEPARATOR);
            placeHolderId = placeHolder[0];
            placeHolderType = placeHolder[1];
            placeHolderCd = EnumerationHelper.getEnumAbbrValForCodeByType(placeHolderId, placeHolderType);
            placeHolderTitle = EnumerationHelper.getEnumValueForCodeByType(placeHolderId, placeHolderType);
        }

        if (PlanConstants.ADD_DIALOG_PAGE.equals(form.getPageId()) || PlanConstants.ADD_RECOMMENDED_DIALOG_PAGE.equals(form.getPageId()) || PlanConstants.RECOMMENDED_DIALOG_PAGE.equals(form.getPageId())) {
            if (hasText(courseCd) || hasText(placeHolderId)) {
                /*Updating the plan item from quickAdd*/
                if (hasText(planItemId)) {
                    return updatePlaceHolder(form, planItemId);
                }

                if (PlanConstants.GENERAL_TYPE.equals(quickAddType)) {

                    addPlaceHolder = true;

                    if (PlanConstants.PLACE_HOLDER_OTHER_CODE.equals(placeHolderId) && !hasText(form.getNote())) {
                        return doErrorPage(form, "Note required", PlanConstants.NOTE_REQUIRED, new String[]{}, null);
                    }

                } else {


                    DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(courseCd);

                    if (courseCode.getSubject() != null && courseCode.getNumber() != null) {

                        String subject = courseCode.getSubject();
                        String number = courseCode.getNumber();

                        /*Differentiating normal course with course level PlaceHolder*/
                        if (number.matches(PlanConstants.COURSE_PLACEHOLDER_REGEX)) {
                            // validate the subject
                            HashMap<String, String> divisionMap = getCourseHelper().fetchCourseDivisions();
                            ArrayList<String> divisions = new ArrayList<String>();
                            getCourseHelper().extractDivisions(divisionMap, subject, divisions, false);
                            if (divisions.size() > 0) {
                                // subject was found, hence is valid
                                Map<String, String> subjectAreas = OrgHelper.getTrimmedSubjectAreas();
                                addPlaceHolder = true;
                                placeHolderType = PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL;
                                placeHolderId = String.format("%s %s", divisions.get(0).trim(), number);
                                placeHolderCd = placeHolderId;
                                String subjectTitle = subjectAreas.get(courseCode.getSubject());
                                String subjectLevel = courseCode.getNumber().toUpperCase().replace(CourseSearchConstants.COURSE_LEVEL_XX, CourseSearchConstants.COURSE_LEVEL_ZERO);
                                if (!getCourseHelper().isValidCourseLevel(courseCode.getSubject(), subjectLevel)) {
                                    return doErrorPage(form, "Course level not found", PlanConstants.COURSE_LEVEL_NOT_FOUND, new String[]{courseCd}, null);
                                }
                                placeHolderTitle = String.format("%s %s level", subjectTitle, subjectLevel);
                            } else {
                                return doErrorPage(form, "Curriculum is invalid", PlanConstants.CURRIC_NOT_FOUND, new String[]{subject}, null);
                            }

                        } else {

                            String courseId = getCourseHelper().getCourseId(subject, number);
                            form.setCourseId(courseId);
                            form.setCode(courseCd);

                        }

                    }

                    if (form.getCourseId() == null && !addPlaceHolder) {
                        return doErrorPage(form, "Course not found", PlanConstants.COURSE_NOT_FOUND, new String[]{courseCd}, null);
                    }

                }
            } else {
                return doErrorPage(form, "Empty Search", PlanConstants.EMPTY_SEARCH, new String[]{courseCd}, null);
            }
        }

        String code = form.getCode();

        if (!addPlaceHolder) {

            /* This method needs a Course ID and an ATP ID when Planning a course.*/
            String courseId = form.getCourseId();

            if (StringUtils.isEmpty(courseId)) {
                return doOperationFailedError(form, "Course ID was missing.", null);
            }

            if (StringUtils.isEmpty(code)) {
                return doOperationFailedError(form, "Course CD was missing.", null);
            }

            CourseInfo courseInfo = getCourseHelper().getCourseInfo(courseId);
            try {
                isCrossListedCourse = getCourseHelper().isCrossListedCourse(courseInfo, code);
            } catch (DoesNotExistException e) {
                return doErrorPage(form, "Course not found", PlanConstants.COURSE_NOT_FOUND, new String[]{code}, null);
            }

            courseDetails = getVersionVerifiedCourseDetails(courseId, code);

            if (courseDetails == null) {
                return doOperationFailedError(form, "Unable to retrieve Course Details for courseId " + courseId, null);
            }
        }

        String newAtpId = form.getAtpId();

        /*Term is required for this method to complete*/
        if (!hasText(newAtpId)) {
            return doOperationFailedError(form, "Missing Term to add course to plan", null);
        }

        /*Term format validation*/
        if (!AtpHelper.isAtpIdFormatValid(newAtpId)) {
            return doOperationFailedError(form, String.format("ATP ID [%s] was not formatted properly.", newAtpId), null);
        }

        /*Historical term validation*/
        if (!AtpHelper.isAtpSetToPlanning(newAtpId)) {
            return doCannotChangeHistoryError(form);
        }

        LearningPlan plan = getSynchronizedLearningPlan(studentId);
        if (plan == null) {
            return doOperationFailedError(form, "Unable to create/retrieve learning plan.", null);
        }


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
            List<ActivityOfferingItem> activityOfferings = getCourseDetailsInquiryService().getActivityOfferingItemsByIdAndCd(courseDetails.getCourseId(), isCrossListedCourse ? courseDetails.getCode() : null, form.getAtpId());
            Map<String, String> primaryActivityToRegCode = new HashMap<String, String>();
            /*Populate the primary and secondary flags*/
            for (ActivityOfferingItem activityOfferingItem : activityOfferings) {
                if (activityOfferingItem.isPrimary() && !form.isPrimary()) {
                    primaryActivityToRegCode.put(activityOfferingItem.getCode(), activityOfferingItem.getRegistrationCode());
                }
                if (activityOfferingItem.getCode().equalsIgnoreCase(form.getSectionCode())) {
                    if (activityOfferingItem.isPrimary()) {
                        PlanItemInfo coursePlanItem = getPlanHelper().getPlannedOrBackupPlanItem(courseDetails.getVersionIndependentId(), isCrossListedCourse ? courseDetails.getCode() : null, form.getAtpId());
                        if (coursePlanItem != null) {
                            addCourse = false;
                        }
                        addPrimaryCourse = true;
                        form.setPrimarySectionCode(activityOfferingItem.getCode());
                        form.setPrimaryRegistrationCode(activityOfferingItem.getRegistrationCode());
                        break;
                    } else {
                        PlanItemInfo primaryPlanItem = getPlanHelper().getPlannedOrBackupPlanItem(activityOfferingItem.getPrimaryActivityOfferingId(), null, form.getAtpId());
                        if (primaryPlanItem != null) {
                            addCourse = false;
                        } else {
                            addPrimaryCourse = true;
                            form.setPrimarySectionCode(getCourseHelper().getCodeFromActivityId(activityOfferingItem.getPrimaryActivityOfferingId()));
                            form.setPrimaryRegistrationCode(primaryActivityToRegCode.get(form.getPrimarySectionCode()));
                            PlanItemInfo coursePlanItem = getPlanHelper().getPlannedOrBackupPlanItem(courseDetails.getVersionIndependentId(), isCrossListedCourse ? courseDetails.getCode() : null, form.getAtpId());
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
        if (addCourse || addPlaceHolder) {
            boolean hasCapacity = false;
            if (isRecommendedItem) {
                /*Since recommendations has no limit*/
                hasCapacity = true;
            } else {
                try {
                    hasCapacity = isAtpHasCapacity(plan, newAtpId, newType);
                } catch (RuntimeException e) {
                    return doOperationFailedError(form, "Could not validate capacity for new plan item.", e);
                }
            }

            if (!hasCapacity) {
                return doPlanCapacityExceededError(form, newType);
            }
        }


        /*
        * ************************************************************************************************************
        *                                  Adding a course to Plan
        * ************************************************************************************************************
        */

        PlanItemInfo planItem = null;
        /*PlanItems for sections*/
        PlanItemInfo primaryPlanItem = null;
        PlanItemInfo secondaryPlanItem = null;

        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> wishlistEvents = null;

        if (!addPlaceHolder) {
            if (!isRecommendedItem && addCourse) {
                //  See if a wishList item exists for the course. If so, then update it. Otherwise create a new plan item.
                planItem = getWishlistPlanItem(courseDetails.getVersionIndependentId(), isCrossListedCourse ? code : null);
            }
            if (planItem == null && addCourse) {
                try {
                    // Don't allow duplicate recommended items.
                    if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(newType) && getPlanHelper().getPlanItemByAtpAndType(plan.getId(), courseDetails.getVersionIndependentId(), newAtpId, newType, isCrossListedCourse ? code : null) != null) {
                        return doDuplicateRecommendedItem(form, newAtpId, courseDetails.getCode());
                    }

                    planItem = addPlanItem(plan, courseDetails.getVersionIndependentId(), PlanConstants.COURSE_TYPE, newAtpId, newType, form.getNote(), null, isCrossListedCourse ? code : null);

                    if (isRecommendedItem) {
                        sendRecommendationNotification(AtpHelper.atpIdToTermName(newAtpId), courseDetails.getCode(), form.getNote(), null, false);
                    }

                } catch (DuplicateEntryException e) {
                    return doDuplicatePlanItem(form, newAtpId, courseDetails.getCode());
                } catch (Exception e) {
                    return doOperationFailedError(form, "Unable to add plan item.", e);
                }
            } else if (planItem != null && addCourse) {

                //  Check for duplicates since addPlanItem isn't being called.
                if (addCourse && isDuplicate(newAtpId, courseDetails.getVersionIndependentId(), newType, isCrossListedCourse ? courseDetails.getCode() : null)) {
                    return doDuplicatePlanItem(form, newAtpId, courseDetails.getCode());
                }

                //  Create wishList events before updating the plan item.
                wishlistEvents = makeRemoveEvent(planItem, courseDetails, form, null);
                planItem.setTypeKey(newType);
                planItem.setPlanPeriods(Arrays.asList(newAtpId));
                String note = hasText(form.getNote()) ? form.getNote() : "";
                planItem.setDescr(new RichTextInfo(note, note));

                try {
                    String oldPlanItemId = planItem.getId();
                    //New PlanItem
                    planItem = getAcademicPlanService().createPlanItem(planItem, getUserSessionHelper().makeContextInfoInstance());
                    //delete old wishList item
                    getAcademicPlanService().deletePlanItem(oldPlanItemId, getUserSessionHelper().makeContextInfoInstance());

                } catch (Exception e) {
                    return doOperationFailedError(form, "Could not add new plan item.", e);
                }
            }

            /*
            * *************************************************************************************************************
            *                             Adding activities to Plan
            * *************************************************************************************************************
            */


            if (addPrimaryCourse && form.getPrimarySectionCode() != null) {
                String refObjId = getCourseHelper().buildActivityRefObjId(newAtpId, courseDetails.getSubjectArea(), courseDetails.getCourseNumber(), form.getPrimarySectionCode());
                primaryPlanItem = addPlanItem(plan, refObjId, PlanConstants.SECTION_TYPE, newAtpId, newType, form.getNote(), null, null);
                form.setPrimaryPlanItemId(primaryPlanItem.getId());

            }

            if (addSecondaryCourse && form.getSectionCode() != null) {
                String refObjId = getCourseHelper().buildActivityRefObjId(newAtpId, courseDetails.getSubjectArea(), courseDetails.getCourseNumber(), form.getSectionCode());
                secondaryPlanItem = addPlanItem(plan, refObjId, PlanConstants.SECTION_TYPE, newAtpId, newType, form.getNote(), null, null);

            }

        }
        /*
        * *************************************************************************************************************
        *                   Adding placeholders to the plan
        * *************************************************************************************************************
        */
        else {
            try {

                planItem = addPlanItem(plan, placeHolderId, placeHolderType, newAtpId, newType, form.getNote(),
                        form.getCredit(), null);

                if (isRecommendedItem) {
                    /*Creating a message*/
                    sendRecommendationNotification(AtpHelper.atpIdToTermName(newAtpId), placeHolderCd, form.getNote(), placeHolderTitle, false);
                }

            } catch (DuplicateEntryException e) {
                return doDuplicatePlanItem(form, newAtpId, placeHolderCd != null ? placeHolderCd : courseCd);
            } catch (Exception e) {
                return doOperationFailedError(form, "Unable to add place Holder item of type." + placeHolderType, e);
            }
        }

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
                events.putAll(makeAddUpdateEvent(planItem, courseDetails, form, false));
                /*Recommended item updated to accepted if a course or placeholder added from recommended dialog
                or only for courses added, moved or copied from other qtr's*/
                if (!PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItem.getTypeKey()) && PlanConstants.COURSE_TYPE.equals(planItem.getRefObjectType())) {
                    updateRecommendedItem(planItem, plan, events);
                }
            }
            if (primaryPlanItem != null) {
                plannedTerm = primaryPlanItem.getPlanPeriods().get(0);
                events.putAll(makeAddUpdateEvent(primaryPlanItem, courseDetails, form, false));
            }
            if (secondaryPlanItem != null) {
                plannedTerm = secondaryPlanItem.getPlanPeriods().get(0);
                events.putAll(makeAddUpdateEvent(secondaryPlanItem, courseDetails, form, false));
            }
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Unable to create add event.", e);
        }

        events.putAll(makeUpdateTotalCreditsEvent(plannedTerm, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        form.setJavascriptEvents(events);

        String[] params = {};
        if (planItem != null) {
            params = new String[]{makeLinkToAtp(planItem.getPlanPeriods().get(0), AtpHelper.atpIdToTermName(planItem.getPlanPeriods().get(0)))};
        } else if (primaryPlanItem != null) {
            params = new String[]{makeLinkToAtp(primaryPlanItem.getPlanPeriods().get(0), AtpHelper.atpIdToTermName(primaryPlanItem.getPlanPeriods().get(0)))};
        } else if (secondaryPlanItem != null) {
            params = new String[]{makeLinkToAtp(secondaryPlanItem.getPlanPeriods().get(0), AtpHelper.atpIdToTermName(secondaryPlanItem.getPlanPeriods().get(0)))};
        }

        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_PLANNED_ITEM_ADDED, params);
    }

    /**
     * Recommended Item updated to accepted if a course or placeholder added from recommended dialog
     * or only for courses added, moved or copied from other qtr's
     * Also adds the delete recommendation event to the form events.
     *
     * @param planItem
     * @param learningPlan
     * @param events
     */
    private void updateRecommendedItem(PlanItemInfo planItem, LearningPlan learningPlan, Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events) {
        /*Adding the recommended events if a recommended item exists for the same refObjId and atp*/
        PlanItemInfo recommendedItem = getPlanHelper().getPlanItemByAtpAndType(learningPlan.getId(), planItem.getRefObjectId(), planItem.getPlanPeriods().get(0), PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, getPlanHelper().getCrossListedCourse(planItem.getAttributes()));
        if (recommendedItem != null && hasText(recommendedItem.getId())) {
            makeRecommendDeleteEvent(recommendedItem, events);
        }
        try {
            recommendedItem.setStateKey(PlanConstants.LEARNING_PLAN_ITEM_ACCEPTED_STATE_KEY);
            getAcademicPlanService().updatePlanItem(recommendedItem.getId(), recommendedItem, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Unable to update the recommended Item state", e);
        }

    }


    /**
     * Add events to Delete the recommended item once it is planned
     *
     * @param planItem
     * @param events
     */
    private void makeRecommendDeleteEvent(PlanItemInfo planItem, Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events) {
        Map<String, String> params = new HashMap<String, String>();
        params.put("planItemId", planItem.getId());
        params.put("planItemType", formatTypeKey(planItem.getTypeKey()));
        params.put("atpId", planItem.getPlanPeriods().get(0));
        events.put(PlanConstants.JS_EVENT_NAME.RECOMMENDED_ITEM_UPDATED, params);
    }


    /**
     * Creates a message for Student about recommended course from adviser
     * Sends a email notification to student about the recommended course from adviser
     *
     * @param term
     * @param courseCd
     * @param note
     * @param removed
     */
    private void sendRecommendationNotification(String term, String courseCd, String note, String title, boolean removed) {

        Properties pro = new Properties();
        InputStream file = getClass().getResourceAsStream(PlanConstants.PROPERTIES_FILE_PATH);
        try {
            pro.load(file);
        } catch (Exception e) {
            logger.error("Could not find the properties file" + e);
        }

        if (!CollectionUtils.isEmpty(pro)) {
            String adviserName = getUserSessionHelper().getCapitalizedName(getUserSessionHelper().getCurrentUserId());
            note = hasText(note) ? String.format("'%s'", WordUtils.wrap(note.trim().replace("\n", "<br/>"), 80, "<br/>", true)) : "";
            String dateAdded = DateFormatHelper.getDateFomatted(new java.sql.Date(System.currentTimeMillis()).toString());
            String planLink = makeLinkToAtp(AtpHelper.termToYearTerm(term).toATP(), "visit your plan page");
            String code = courseCd;
            courseCd = title != null ? (title.equals(courseCd) ? String.format("'%s' placeholder", courseCd) : String.format("%s '%s' placeholder", courseCd, title)) : courseCd;

            /*Creating a new Message from Adviser to student*/
            String emailSubject = "";
            String subject = "";
            String message = "";
            String emailMessage = "";
            if (!removed) {
                subject = String.format((String) pro.get(PlanConstants.ADD_RECOMMEND_NOTIFICATION_MESSAGE_SUBJECT), code);
                emailSubject = String.format((String) pro.get(PlanConstants.ADD_RECOMMEND_NOTIFICATION_SUBJECT), adviserName);
                message = String.format((String) pro.get(PlanConstants.ADD_RECOMMEND_NOTIFICATION_BODY), adviserName, courseCd, term, note);
                emailMessage = message + String.format((String) pro.get(PlanConstants.ADD_RECOMMEND_NOTIFICATION_INFO), dateAdded, planLink);
            } else {
                subject = String.format((String) pro.get(PlanConstants.REMOVED_RECOMMEND_NOTIFICATION_MESSAGE_SUBJECT), code);
                emailSubject = String.format((String) pro.get(PlanConstants.REMOVED_RECOMMEND_NOTIFICATION_SUBJECT), adviserName);
                message = String.format((String) pro.get(PlanConstants.REMOVED_RECOMMEND_NOTIFICATION_INFO), adviserName, courseCd, term, dateAdded, note);
                emailMessage = message;
            }


            try {
                getCommentHelper().createMessage(subject, message);
            } catch (Exception e) {
                logger.error("Error creating message for adviser recommended course", e);
            }

            /*Sending a email notification to student about the recommended course*/
            try {
                getCommentHelper().sendRecommendationEmailNotification(emailSubject, emailMessage);
            } catch (Exception e) {
                logger.error("Error sending message notification for adviser recommended course", e);
            }
        } else {
            logger.error("Could not find the properties file, failed to create and send message notification");
        }

    }


    /**
     * returns true if the reofbjtype is a placeholder
     *
     * @param refObjType
     * @return
     */
    private boolean isPlaceHolderType(String refObjType) {
        return PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(refObjType) || PlanConstants.PLACE_HOLDER_TYPE_GEN_ED.equals(refObjType) || PlanConstants.PLACE_HOLDER_TYPE.equals(refObjType);
    }


    /**
     * Updates the Placeholders or notes of a planItem
     *
     * @param form
     * @param planItemId
     * @return
     */
    private ModelAndView updatePlaceHolder(PlanForm form, String planItemId) {

        PlanItemInfo planItemInfo = null;
        CourseSummaryDetails courseSummaryDetails = null;
        try {
            planItemInfo = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);

            if (planItemInfo != null) {
                String atpId = planItemInfo.getPlanPeriods().get(0);
                String placeHolderId = null;
                String placeHolderType = null;
                String placeHolderCd = null;
                boolean creditUpdated = false;
                boolean isCrossListedCourse = false;

                /*General placeholder/ placeholder type and value update */
                if (hasText(form.getGeneralPlaceholder())) {
                    String[] placeHolder = form.getGeneralPlaceholder().split(PlanConstants.CODE_KEY_SEPARATOR);
                    placeHolderId = placeHolder[0];
                    placeHolderType = placeHolder[1];
                    placeHolderCd = EnumerationHelper.getEnumAbbrValForCodeByType(placeHolderId, placeHolderType);
                    if (PlanConstants.PLACE_HOLDER_OTHER_CODE.equals(placeHolderId) && !hasText(form.getNote())) {
                        return doErrorPage(form, "Note required", PlanConstants.NOTE_REQUIRED, new String[]{}, null);
                    }
                    planItemInfo.setRefObjectId(placeHolderId);
                    planItemInfo.setRefObjectType(placeHolderType);
                }

                /*Course level PlaceHolder type and value update*/
                if (hasText(form.getCourseCd())) {

                    DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(form.getCourseCd());

                    if (courseCode.getSubject() != null && courseCode.getNumber() != null) {
                        String subject = courseCode.getSubject();
                        String number = courseCode.getNumber();
                        if (number.matches(PlanConstants.COURSE_PLACEHOLDER_REGEX)) {
                            // validate the subject
                            HashMap<String, String> divisionMap = getCourseHelper().fetchCourseDivisions();
                            ArrayList<String> divisions = new ArrayList<String>();
                            getCourseHelper().extractDivisions(divisionMap, subject, divisions, false);
                            if (divisions.size() > 0) {
                                planItemInfo.setRefObjectId(String.format("%s %s", divisions.get(0).trim(), number));
                                planItemInfo.setRefObjectType(PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL);
                                String subjectLevel = number.toUpperCase().replace(CourseSearchConstants.COURSE_LEVEL_XX, CourseSearchConstants.COURSE_LEVEL_ZERO);
                                if (!getCourseHelper().isValidCourseLevel(courseCode.getSubject(), subjectLevel)) {
                                    return doErrorPage(form, "Course level not found", PlanConstants.COURSE_LEVEL_NOT_FOUND, new String[]{form.getCourseCd()}, null);
                                }
                            } else {
                                return doErrorPage(form, "Curriculum is invalid", PlanConstants.CURRIC_NOT_FOUND, new String[]{subject}, null);
                            }

                        } else {
                            String courseId = getCourseHelper().getCourseId(subject, number);
                            if (!hasText(courseId)) {
                                return doErrorPage(form, "Course not found", PlanConstants.COURSE_NOT_FOUND, new String[]{form.getCourseCd()}, null);
                            }
                            CourseInfo courseInfo = getCourseHelper().getCourseInfo(courseId);
                            try {
                                isCrossListedCourse = getCourseHelper().isCrossListedCourse(courseInfo, form.getCourseCd());
                            } catch (DoesNotExistException e) {
                                return doErrorPage(form, "Course not found", PlanConstants.COURSE_NOT_FOUND, new String[]{form.getCourseCd()}, null);
                            }
                            courseSummaryDetails = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(courseId, isCrossListedCourse ? form.getCourseCd() : null);
                            String versionId = courseSummaryDetails.getVersionIndependentId();
                            //  Check for duplicates since addPlanItem isn't being called.
                            if (!versionId.equals(planItemInfo.getRefObjectId()) && isDuplicate(atpId, versionId, planItemInfo.getTypeKey(), isCrossListedCourse ? form.getCourseCd() : null)) {
                                return doDuplicatePlanItem(form, atpId, courseSummaryDetails.getCode());
                            }
                            planItemInfo.setRefObjectId(versionId);
                            planItemInfo.setRefObjectType(PlanConstants.COURSE_TYPE);

                            /*If a crossListed course is being added in then courseCd needs to be added as dynamic attribute.
                             If a CrossListed course if being changed to regular course then the crossListed dynamic attribute should be removed*/
                            if (isCrossListedCourse) {
                                boolean added = false;
                                for (AttributeInfo attributeInfo : planItemInfo.getAttributes()) {
                                    if (PlanConstants.CROSS_LISTED_COURSE_ATTR_KEY.equals(attributeInfo.getKey())) {
                                        attributeInfo.setValue(form.getCourseCd());
                                        added = true;
                                        break;
                                    }
                                }
                                if (!added) {
                                    List<AttributeInfo> attributeInfos = planItemInfo.getAttributes();
                                    if (attributeInfos == null) {
                                        attributeInfos = new ArrayList<AttributeInfo>();
                                    }
                                    attributeInfos.add(new AttributeInfo(PlanConstants.CROSS_LISTED_COURSE_ATTR_KEY, form.getCourseCd()));
                                }
                            } else {
                                AttributeInfo attributeInfoToRemove = null;
                                for (AttributeInfo attributeInfo : planItemInfo.getAttributes()) {
                                    if (PlanConstants.CROSS_LISTED_COURSE_ATTR_KEY.equals(attributeInfo.getKey())) {
                                        attributeInfoToRemove = attributeInfo;
                                        break;
                                    }
                                }
                                if (attributeInfoToRemove != null) {
                                    planItemInfo.getAttributes().remove(attributeInfoToRemove);
                                }
                            }
                        }

                    } else {
                        return doErrorPage(form, "Course not found", PlanConstants.COURSE_NOT_FOUND, new String[]{form.getCourseCd()}, null);
                    }
                }


                /*Credit update: if there was none before and user added credit value OR they changed existing...*/
                if ((planItemInfo.getCredit() == null && form.getCredit() != null && hasText(form.getCredit())) ||
                        (planItemInfo.getCredit() != null && !planItemInfo.getCredit().toString().equals(form.getCredit()))) {
                    creditUpdated = true;
                    if (form.getCredit() != null && hasText(form.getCredit())) {
                        planItemInfo.setCredit(new BigDecimal(form.getCredit()));
                    } else { // placeholder changed from having credit to unspecified
                        planItemInfo.setCredit(null);
                    }
                }

                /*Note update*/
                RichTextInfo richTextInfo = new RichTextInfo();
                String note = hasText(form.getNote()) ? form.getNote().trim() : null;
                richTextInfo.setFormatted(note);
                richTextInfo.setPlain(note);
                planItemInfo.setDescr(richTextInfo);


                String[] params = {};
                planItemInfo = getAcademicPlanService().updatePlanItem(planItemId, planItemInfo, PlanConstants.CONTEXT_INFO);

                //  Create events
                Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
                events.putAll(makeAddUpdateEvent(planItemInfo, courseSummaryDetails, form, true));

                if (courseSummaryDetails != null && hasText(courseSummaryDetails.getCode())) {
                    addStatusAlertEvents(courseSummaryDetails.getCode(), atpId, events, true);
                }

                if (creditUpdated && PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equals(planItemInfo.getTypeKey())) {
                    events.putAll(makeUpdateTotalCreditsEvent(atpId, PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));
                }
                form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
                form.setJavascriptEvents(events);

                return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_UPDATED_ITEM, params);

            }

        } catch (DoesNotExistException e) {
            return doPageRefreshError(form, "PlanItem with Id:" + planItemId + " doesnot exist", e);
        } catch (Exception e) {
            return doErrorPage(form, "QuickAdd request to update failed", PlanConstants.UPDATE_FAILED, new String[]{form.getCourseCd() != null ? form.getCourseCd() : EnumerationHelper.getEnumAbbrValForCodeByType(form.getGeneralPlaceholder(), PlanConstants.PLACE_HOLDER_ENUM_KEY)}, e);
        }

        return doErrorPage(form, "QuickAdd request to update failed", PlanConstants.UPDATE_FAILED, new String[]{form.getCourseCd() != null ? form.getCourseCd() : EnumerationHelper.getEnumAbbrValForCodeByType(form.getGeneralPlaceholder(), PlanConstants.PLACE_HOLDER_ENUM_KEY)}, null);
    }

    /**
     * Adds the status alerts to the provided event if any planned activities are available
     *
     * @param courseCode
     * @param atpId
     */

    private void addStatusAlertEvents(String courseCode, String atpId, Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events, boolean update) {
        //Add additional add events for Activity data if present
        List<ActivityOfferingItem> plannedActivities = getPlannedActivitiesByCourseAndTerm(courseCode, atpId);
        PlanConstants.JS_EVENT_NAME eventType = update ? PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED : PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED;
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

            Collections.sort(plannedActivityCodes);
            String sections = StringUtils.join(plannedActivityCodes.toArray(), ", ");

            if (sections != null) {
                events.get(eventType).put("sections", sections);
            }

            if (!withdrawnActivityCodes.isEmpty() || !suspendedActivityCodes.isEmpty()) {

                events.get(eventType).put("showAlert", "true");

                String statusAlert = events.get(eventType).get("statusAlert");

                StringBuffer sb = new StringBuffer();

                sb = sb.append(statusAlert);

                if (!withdrawnActivityCodes.isEmpty()) {
                    sb = sb.append(String.format(PlanConstants.WITHDRAWN_ALERT, StringUtils.join(withdrawnActivityCodes.toArray(), ", ")));
                }

                if (!suspendedActivityCodes.isEmpty()) {
                    sb = sb.append(String.format(PlanConstants.SUSPENDED_ALERT, StringUtils.join(suspendedActivityCodes.toArray(), ", ")));
                }

                events.get(eventType).put("statusAlert", sb.toString());
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
                plan = getPlanHelper().getLearningPlan(studentId);
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
     * @param atpId
     * @param refObjId
     * @param planItemType
     * @return
     */
    private boolean isDuplicate(String atpId, String refObjId, String planItemType, String courseCd) {
        /*
         Make sure no dups exist. The rules are different for wishlist vs planned or backup courses.
        */
        boolean isDuplicate = false;
        if (planItemType.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
            if (getWishlistPlanItem(refObjId, courseCd) != null) {
                isDuplicate = true;
            }
        } else {
            if (getPlanHelper().getPlannedOrBackupPlanItem(refObjId, courseCd, atpId) != null) {
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
                if (p.getPlanPeriods().get(0).equals(atpId) && (PlanConstants.COURSE_TYPE.equalsIgnoreCase(p.getRefObjectType()) || isPlaceHolderType(p.getRefObjectType()))) {
                    counter++;
                }
            }
        }

        if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            return (counter >= PlanConstants.BACKUP_PLAN_ITEM_CAPACITY) ? false : true;
        } else if (typeKey.equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED)) {
            return (counter >= PlanConstants.PLANNED_PLAN_ITEM_CAPACITY) ? false : true;
        } else if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(typeKey)) {
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
        if (getUserSessionHelper().isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }

        String courseId = form.getCourseId();
        String courseCd = form.getCode();
        if (StringUtils.isEmpty(courseId)) {
            return doOperationFailedError(form, "Course ID was missing.", null);
        }

        if (StringUtils.isEmpty(courseCd)) {
            return doOperationFailedError(form, "Course CD was missing.", null);
        }

        boolean isCrossListedCourse = false;
        CourseInfo courseInfo = getCourseHelper().getCourseInfo(courseId);
        try {
            isCrossListedCourse = getCourseHelper().isCrossListedCourse(courseInfo, courseCd);
        } catch (DoesNotExistException e) {
            return doErrorPage(form, "Course not found", PlanConstants.COURSE_NOT_FOUND, new String[]{courseCd}, null);
        }

        String studentId = getUserSessionHelper().getStudentId();
        LearningPlan plan = getSynchronizedLearningPlan(studentId);
        if (plan == null) {
            return doOperationFailedError(form, "Unable to create learning plan.", null);
        }

        // Retrieve courseDetails based on the passed in CourseId and then update courseDetails based on the version independent Id
        CourseSummaryDetails courseDetails = getVersionVerifiedCourseDetails(courseId, isCrossListedCourse ? courseCd : null);
        if (courseDetails == null) {
            return doOperationFailedError(form, "Unable to retrieve Course Details.", null);
        }

        PlanItemInfo planItem = null;
        try {
            planItem = addPlanItem(plan, courseDetails.getVersionIndependentId(), PlanConstants.COURSE_TYPE, null, PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST, null, null, isCrossListedCourse ? courseCd : null);
        } catch (DuplicateEntryException e) {
            return doDuplicateBookmarkItem(form, courseDetails.getCode());
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to add plan item.", e);
        }

        //  Create events
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        events.putAll(makeAddUpdateEvent(planItem, courseDetails, form, false));

        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
        form.setJavascriptEvents(events);

        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_SAVED_ITEM_ADDED, new String[0]);
    }

    @RequestMapping(params = "methodToCall=removeItem")
    public ModelAndView removePlanItem(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                       HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (getUserSessionHelper().isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", null);
        }

        return removeItem(form);
    }


    /**
     * Method used to remove planItems
     * Removed course, course placeHolder,  general placeHolder, recommended planItems
     *
     * @param form
     * @return
     */
    private ModelAndView removeItem(PlanForm form) {
        String planItemId = form.getPlanItemId();
        String code = null;
        String title = null;
        boolean isRecommendedItem = false;
        String courseId = form.getCourseId();
        if (StringUtils.isEmpty(planItemId) && StringUtils.isEmpty(courseId)) {
            return doOperationFailedError(form, "Plan item id and courseId are missing.", null);
        }

        if (StringUtils.isEmpty(planItemId)) {
            CourseSummaryDetails course = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(courseId, form.getCode());
            planItemId = getPlanIdFromCourseId(course.getVersionIndependentId(), PlanConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);
        }

        String activityCode = null;
        //  See if the plan item exists.
        PlanItemInfo planItem = null;
        try {
            planItem = getAcademicPlanService().getPlanItem(planItemId, PlanConstants.CONTEXT_INFO);
            isRecommendedItem = PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItem.getTypeKey());
            if (isRecommendedItem) {
                if (!getUserSessionHelper().getCurrentUserId().equals(planItem.getMeta().getCreateId())) {
                    return doNonOwnerAccessError(form, "Non Owner Access Denied", null);
                }
            }
        } catch (DoesNotExistException e) {
            return doPageRefreshError(form, String.format("No plan item with id [%s] exists.", planItemId), e);
        } catch (Exception e) {
            return doOperationFailedError(form, "Query for plan item failed.", e);
        }
        String term = null;
        for (String atpId : planItem.getPlanPeriods()) {
            term = AtpHelper.atpIdToTermName(atpId);
            break;
        }
        CourseSummaryDetails courseDetail = null;
        if (PlanConstants.COURSE_TYPE.equals(planItem.getRefObjectType())) {
            courseDetail = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(planItem.getRefObjectId(), getPlanHelper().getCrossListedCourse(planItem.getAttributes()));
            courseId = courseDetail.getCourseId();
            code = courseDetail.getCode();
        } else if (PlanConstants.SECTION_TYPE.equals(planItem.getRefObjectType())) {
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
                courseDetail = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(courseId, courseOfferingInfo.getCourseCode());
                activityCode = activityDisplayInfo.getActivityOfferingCode();

            } else {
                return doOperationFailedError(form, "Could not delete plan item", null);
            }
        } else if ((PlanConstants.PLACE_HOLDER_TYPE_GEN_ED.equals(planItem.getRefObjectType()) ||
                PlanConstants.PLACE_HOLDER_TYPE.equals(planItem.getRefObjectType())) && getUserSessionHelper().isAdviser()) {
            code = EnumerationHelper.getEnumAbbrValForCodeByType(planItem.getRefObjectId(), planItem.getRefObjectType());
            title = EnumerationHelper.getEnumValueForCodeByType(planItem.getRefObjectId(), planItem.getRefObjectType());

        } else if (PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(planItem.getRefObjectType()) && getUserSessionHelper().isAdviser()) {
            code = planItem.getRefObjectId();
            DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(code);
            Map<String, String> subjectAreas = OrgHelper.getTrimmedSubjectAreas();
            String subjectTitle = subjectAreas.get(courseCode.getSubject());
            String subjectLevel = courseCode.getNumber().toUpperCase().replace(CourseSearchConstants.COURSE_LEVEL_XX, CourseSearchConstants.COURSE_LEVEL_ZERO);
            title = String.format("%s %s level", subjectTitle, subjectLevel);
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

        events.putAll(makeRemoveEvent(planItem, courseDetail, form, new ArrayList<String>(planItemsToRemove.values())));

        planItemsToRemove.put(planItemId, null);
        try {
            if (planItemsToRemove.size() > 0) {
                for (String planItemIdToRemove : planItemsToRemove.keySet()) {
                    getAcademicPlanService().deletePlanItem(planItemIdToRemove, getUserSessionHelper().makeContextInfoInstance());
                }
            }
            if (isRecommendedItem) {
                sendRecommendationNotification(term, code, form.getNote(), title, true);
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


    @RequestMapping(params = "methodToCall=removeRecommendedItem")
    public ModelAndView removeRecommendedItem(@ModelAttribute("KualiForm") PlanForm form, BindingResult result,
                                              HttpServletRequest httprequest, HttpServletResponse httpresponse) {
        if (!getUserSessionHelper().isAdviser()) {
            return doStudentAccessError(form, "Student Access Denied", null);
        }
        return removeItem(form);
    }

    /**
     * @param courseCode
     * @param termId
     * @return
     */
    private List<ActivityOfferingItem> getPlannedActivitiesByCourseAndTerm(String courseCode, String termId) {
        List<ActivityOfferingItem> activityOfferingItems = new ArrayList<ActivityOfferingItem>();
        try {
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(getUserSessionHelper().getStudentId(), PlanConstants.LEARNING_PLAN_TYPE_PLAN, CourseSearchConstants.CONTEXT_INFO);
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
     * Blow-up response for all plan item actions for the Student.
     */

    private ModelAndView doStudentAccessError(PlanForm form, String errorMessage, Exception e) {
        String[] params = {};
        return doErrorPage(form, errorMessage, PlanConstants.ERROR_KEY_STUDENT_ACCESS, params, e);
    }

    /**
     * Blow-up response for all plan item actions for the non Owners of recommendations.
     */

    private ModelAndView doNonOwnerAccessError(PlanForm form, String errorMessage, Exception e) {
        String[] params = {};
        return doErrorPage(form, errorMessage, PlanConstants.ERROR_KEY_NON_OWNER_ACCESS, params, e);
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
    private ModelAndView doDuplicatePlanItem(PlanForm form, String atpId, String code) {
        String[] params = {code, AtpHelper.atpIdToTermName(atpId)};
        return doErrorPage(form, PlanConstants.ERROR_KEY_PLANNED_ITEM_ALREADY_EXISTS, params);
    }


    /**
     * Blow-up response for all recommended item actions.
     */
    private ModelAndView doDuplicateRecommendedItem(PlanForm form, String atpId, String code) {
        String[] params = {code, AtpHelper.atpIdToTermName(atpId)};
        return doErrorPage(form, PlanConstants.ERROR_KEY_RECOMMENDED_ITEM_ALREADY_EXISTS, params);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doDuplicateBookmarkItem(PlanForm form, String code) {
        String[] params = {code};
        return doErrorPage(form, PlanConstants.ERROR_KEY_BOOKMARKED_ITEM_ALREADY_EXISTS, params);
    }


    /**
     * Success response for all plan item actions.
     */
    private ModelAndView doPlanActionSuccess(PlanForm form, String key, String[] params) {
        form.setRequestStatus(PlanForm.REQUEST_STATUS.SUCCESS);
        GlobalVariables.getMessageMap().putInfoForSectionId(PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID, key, params);
        return getUIFModelAndView(form, PlanConstants.PLAN_ITEM_RESPONSE_PAGE_ID);
    }

    /**
     * Retrieve courseDetails based on the passed in CourseId and then update courseDetails based on the version independent Id
     *
     * @param courseId
     * @return
     */
    private CourseSummaryDetails getVersionVerifiedCourseDetails(String courseId, String courseCd) {
        CourseSummaryDetails courseDetails = null;
        try {

            courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(courseId, courseCd);
            /* Switching the courseDetails based on the versionIndependent Id*/
            if (!courseId.equals(courseDetails.getVersionIndependentId())) {
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(courseDetails.getVersionIndependentId(), courseCd);
            }

        } catch (Exception e) {
            logger.error("Unable to load course details for courseId " + courseId);
        }
        return courseDetails;
    }


    // Course ID GUID, atp key id eg "uw.kuali.atp.2001.1"
    @RequestMapping(value = "/plan/enroll")
    public void getCourseSectionStatusAsJson(HttpServletResponse response, HttpServletRequest request) {
        try {
            String courseId = request.getParameter("courseId");
            String courseCd = request.getParameter("courseCd");
            String atpParam = request.getParameter("atpId");


            CourseSummaryDetails courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryByIdAndCd(courseId, courseCd);

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
     * @param refObjId     The id of the course.
     * @param atpId        A list of ATP/term ids if the plan item is a planned course.
     * @param planItemType Saved course or planned course.
     * @return The newly created plan item or the existing plan item where a plan item already exists for the given course.
     * @throws RuntimeException on errors.
     */
    protected PlanItemInfo addPlanItem(LearningPlan plan, String refObjId, String refObjType, String atpId, String planItemType, String note, String credit, String crossListedCourse)
            throws DuplicateEntryException {

        if (StringUtils.isEmpty(refObjId)) {
            throw new RuntimeException("Empty Course ID");
        }

        PlanItemInfo newPlanItem = null;

        PlanItemInfo pii = new PlanItemInfo();
        pii.setLearningPlanId(plan.getId());
        pii.setTypeKey(planItemType);
        pii.setRefObjectType(refObjType);
        pii.setRefObjectId(refObjId);

        pii.setStateKey(PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItemType) ? PlanConstants.LEARNING_PLAN_ITEM_PROPOSED_STATE_KEY : PlanConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);

        RichTextInfo rti = new RichTextInfo();
        rti.setFormatted(hasText(note) ? note : "");
        rti.setPlain(hasText(note) ? note : "");
        pii.setDescr(rti);

        if (null != atpId) {
            pii.setPlanPeriods(Arrays.asList(atpId));
        }

        //  Don't allow duplicates.
        if (!isPlaceHolderType(refObjType) && isDuplicate(atpId, refObjId, planItemType, crossListedCourse)) {
            throw new DuplicateEntryException("Duplicate plan item exists.");
        }

        if (isPlaceHolderType(refObjType) && hasText(credit)) {
            pii.setCredit(new BigDecimal(credit));
        }

        if (!StringUtils.isEmpty(crossListedCourse)) {
            List<AttributeInfo> attributeInfos = new ArrayList<AttributeInfo>();
            AttributeInfo attributeInfo = new AttributeInfo(PlanConstants.CROSS_LISTED_COURSE_ATTR_KEY, crossListedCourse);
            attributeInfos.add(attributeInfo);
            pii.setAttributes(attributeInfos);
        }

        try {
            newPlanItem = getAcademicPlanService().createPlanItem(pii, getUserSessionHelper().makeContextInfoInstance());
        } catch (AlreadyExistsException e) {
            logger.error("Could not create plan item.", e);
            throw new DuplicateEntryException("plan Item already exists", e);
        } catch (Exception e) {
            logger.error("Could not create plan item.", e);
            throw new RuntimeException("Could not create plan item.", e);
        }

        return newPlanItem;
    }

    /**
     * Gets a Plan Item of type "wishlist" for a particular course. There should only ever be one.
     *
     * @param courseId The id of the course.
     * @return A PlanItem of type wishlist.
     */
    protected PlanItemInfo getWishlistPlanItem(String courseId, String courseCd) {

        if (StringUtils.isEmpty(courseId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        String studentId = getUserSessionHelper().getStudentId();
        LearningPlan learningPlan = getPlanHelper().getLearningPlan(studentId);
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
                /*Multiple PlanItems with same refObjId can be present which are crossListed courses in same term,
                So filtering them by using the given crossListed courseCd to get the exact planItemInfo*/
                String crossListedCourse = getPlanHelper().getCrossListedCourse(p.getAttributes());
                //Planned parent course returned
                if (StringUtils.isEmpty(courseCd) && StringUtils.isEmpty(crossListedCourse)) {
                    item = p;
                    break;
                }
                //Planned crossListed course returned
                else if (!StringUtils.isEmpty(courseCd) && getCourseHelper().isSimilarCourses(courseCd, crossListedCourse)) {
                    item = p;
                    break;
                }
            }
        }

        //  A null here means that no plan item exists for the given course ID.
        return item;
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
        context.setPrincipalId(getUserSessionHelper().getStudentId());

        return getAcademicPlanService().createLearningPlan(plan, context);
    }

    /**
     * Creates events map for a remove.
     *
     * @param planItem
     * @return
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeRemoveEvent(PlanItemInfo planItem, CourseSummaryDetails courseDetails, PlanForm planForm, List<String> itemsToUpdate) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        Map<String, String> params = new HashMap<String, String>();

        params.put("planItemType", formatTypeKey(planItem.getTypeKey()));
        params.put("planItemId", planItem.getId());

        boolean placeHolder = isPlaceHolderType(planItem.getRefObjectType());
        params.put("placeHolder", String.valueOf(placeHolder));

        if (!placeHolder) {
            params.put("courseId", courseDetails.getCourseId());
            params.put("subject", courseDetails.getSubjectArea().trim());
            params.put("number", courseDetails.getCourseNumber().trim());
            params.put("courseCd", courseDetails.getCode());
        }

        //  Only planned or backup items get an atpId attribute.
        if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equals(planItem.getTypeKey()) ||
                PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP.equals(planItem.getTypeKey())
                || PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItem.getTypeKey())) {
            params.put("atpId", planItem.getPlanPeriods().get(0));

            if (PlanConstants.SECTION_TYPE.equals(planItem.getRefObjectType())) {
                String itemsToBeUpdated = null;
                if (itemsToUpdate != null && itemsToUpdate.size() > 0) {
                    itemsToBeUpdated = StringUtils.join(itemsToUpdate.toArray(), ",");
                }

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
            }


        }


        if (PlanConstants.SECTION_TYPE.equals(planItem.getRefObjectType())) {
            events.put(PlanConstants.JS_EVENT_NAME.SECTION_ITEM_DELETED, params);
        } else {
            events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, params);
        }

        return events;
    }

    /**
     * Creates an update credits event.
     *
     * @param atpId The id of the term.
     * @return
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeUpdateTotalCreditsEvent(String atpId,
                                                                                              PlanConstants.JS_EVENT_NAME eventName) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        Map<String, String> params = new HashMap<String, String>();

        params.put("atpId", atpId);
        String totalCredits = getPlannedTermsHelper().getTotalCredits(atpId);
        params.put("totalCredits", totalCredits);

        events.put(eventName, params);
        return events;
    }

    /**
     * Creates an add or update plan item event.
     *
     * @param planItem
     * @param courseDetails
     * @return
     * @throws RuntimeException if anything goes wrong.
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeAddUpdateEvent(PlanItemInfo planItem, CourseSummaryDetails courseDetails, PlanForm planForm, boolean updateEvent) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        Map<String, String> params = new HashMap<String, String>();
        params.put("planItemId", planItem.getId());
        params.put("planItemType", formatTypeKey(planItem.getTypeKey()));

        boolean placeHolder = isPlaceHolderType(planItem.getRefObjectType());

        //  Only planned or backup items get an atpId attribute.
        if (PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equals(planItem.getTypeKey()) ||
                PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP.equals(planItem.getTypeKey()) || PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItem.getTypeKey())) {

            String atpId = planItem.getPlanPeriods().get(0);
            String termName = AtpHelper.atpIdToTermName(atpId);

            params.put("atpId", atpId);
            params.put("termName", termName);

            boolean showAlert = false;
            StringBuffer statusAlert = new StringBuffer();
            if (!placeHolder) {
                // event for alert Icon
                List<String> publishedTerms = AtpHelper.getPublishedTermsForCampus(courseDetails.getCampusCd());
                boolean scheduled = AtpHelper.isCourseOfferedInTerm(atpId, courseDetails.getCode());
                boolean timeScheduleOpen = publishedTerms.contains(atpId);

                if (timeScheduleOpen) {
                    showAlert = !scheduled;
                }


                if (timeScheduleOpen && !scheduled) {
                    statusAlert = statusAlert.append(String.format(PlanConstants.COURSE_NOT_SCHEDULE_ALERT, courseDetails.getCode(), termName));
                }

            }

            if (getUserSessionHelper().isAdviser()) {
                params.put("adviserName", getUserSessionHelper().getName(planItem.getMeta().getCreateId()));
            }

            params.put("showAlert", String.valueOf(showAlert));
            params.put("statusAlert", statusAlert.toString());
            boolean adviserRecommended = false;
            if (!PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItem.getTypeKey())) {
                PlanItemInfo recommendedPlanItem = getPlanHelper().getPlanItemByAtpAndType(planItem.getLearningPlanId(), planItem.getRefObjectId(), atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, getPlanHelper().getCrossListedCourse(planItem.getAttributes()));
                adviserRecommended = recommendedPlanItem != null && PlanConstants.LEARNING_PLAN_ITEM_ACCEPTED_STATE_KEY.equals(recommendedPlanItem.getStateKey());
            }

            params.put("adviserRecommended", String.valueOf(adviserRecommended));

            if (PlanConstants.SECTION_TYPE.equals(planItem.getRefObjectType())) {
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

        String planItemShortTitle = null;
        String planItemLongTitle = null;
        String courseId = null;
        String courseCd = null;
        String subject = null;
        String number = null;
        String credit = null;
        String note = null;
        if (hasText(planItem.getDescr().getPlain())) {
            try {
                note = mapper.writeValueAsString(HtmlUtils.htmlEscape(planItem.getDescr().getPlain().replace("\n", "<br/>").replace("'", "\\'"))).replaceAll("^\"|\"$", "");
            } catch (IOException e) {
                logger.error("Could not add the note to add event");
            }
        }
        if (placeHolder) {
            if (PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(planItem.getRefObjectType())) {
                planItemShortTitle = planItem.getRefObjectId();
                planItemLongTitle = getCoursePlaceHolderTitle(planItem.getRefObjectId());
                if (planItem.getCredit() != null) {
                    credit = String.valueOf(planItem.getCredit().intValue());
                }
            } else {
                planItemShortTitle = EnumerationHelper.getEnumAbbrValForCodeByType(planItem.getRefObjectId(), planItem.getRefObjectType());
                planItemLongTitle = EnumerationHelper.getEnumValueForCodeByType(planItem.getRefObjectId(), planItem.getRefObjectType());
                if (planItem.getCredit() != null) {
                    credit = String.valueOf(planItem.getCredit().intValue());
                }
            }
        } else {
            planItemShortTitle = courseDetails.getCode();
            planItemLongTitle = courseDetails.getCourseTitle();
            courseId = courseDetails.getCourseId();
            credit = courseDetails.getCredit();
            subject = courseDetails.getSubjectArea().trim();
            number = courseDetails.getCourseNumber().trim();
            courseCd = courseDetails.getCode();
        }

        params.put("placeHolder", String.valueOf(placeHolder));
        params.put("planItemShortTitle", planItemShortTitle);
        params.put("planItemLongTitle", planItemLongTitle);
        params.put("courseId", courseId);
        params.put("courseCd", courseCd);
        params.put("subject", subject);
        params.put("number", number);
        params.put("credit", credit);
        params.put("note", note);


        if (PlanConstants.SECTION_TYPE.equals(planItem.getRefObjectType())) {
            events.put(PlanConstants.JS_EVENT_NAME.SECTION_ITEM_ADDED, params);
        } else {
            if (!updateEvent) {
                events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, params);
            } else {
                events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_UPDATED, params);
            }
        }

        return events;
    }

    /**
     * returns for COM 2xx ---> Communication 200 level
     *
     * @param coursePlaceHolder
     * @return
     */
    private String getCoursePlaceHolderTitle(String coursePlaceHolder) {
        Map<String, String> subjectAreas = OrgHelper.getTrimmedSubjectAreas();
        DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(coursePlaceHolder);
        String subjectTitle = subjectAreas.get(courseCode.getSubject());
        String subjectLevel = courseCode.getNumber().toUpperCase().replace(CourseSearchConstants.COURSE_LEVEL_XX, CourseSearchConstants.COURSE_LEVEL_ZERO);
        return String.format("%s %s level", subjectTitle, subjectLevel);
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
            String regId = getUserSessionHelper().getStudentId();
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

    public CommentHelper getCommentHelper() {
        return commentHelper;
    }

    public void setCommentHelper(CommentHelper commentHelper) {
        this.commentHelper = commentHelper;
    }

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
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
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

    public void setPlannedTermsHelper(PlannedTermsHelperBase plannedTermsHelper) {
        this.plannedTermsHelper = plannedTermsHelper;
    }

    public PlanHelper getPlanHelper() {
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
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