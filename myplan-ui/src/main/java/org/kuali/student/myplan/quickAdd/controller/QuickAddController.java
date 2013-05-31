package org.kuali.student.myplan.quickAdd.controller;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.datadictionary.exception.DuplicateEntryException;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.controller.CourseSearchController;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.dataobject.ServicesStatusDataObject;
import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.quickAdd.QuickAddConstants;
import org.kuali.student.myplan.quickAdd.form.QuickAddForm;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.MetaInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.*;

import static org.apache.commons.lang.StringUtils.*;
import static org.kuali.rice.core.api.criteria.PredicateFactory.equalIgnoreCase;
import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP;
import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED;
import static org.kuali.student.myplan.plan.PlanConstants.*;
import static org.springframework.util.StringUtils.hasText;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 9/25/12
 * Time: 9:40 AM
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping(value = "/quickAdd/**")
public class QuickAddController extends UifControllerBase {
    public final Logger logger = Logger.getLogger(QuickAddController.class);

    private CourseSearchController searchController = new CourseSearchController();

    private transient boolean isAcademicCalendarServiceUp = true;

    private transient boolean isAcademicRecordServiceUp = true;

    private transient boolean isCourseOfferingServiceUp = true;

    private transient CourseOfferingService courseOfferingService;

    private transient AcademicCalendarService academicCalendarService;

    private transient AcademicPlanService academicPlanService;

    private transient CourseDetailsInquiryHelperImpl courseDetailsInquiryService;

    @Autowired
    private CourseHelper courseHelper;

    private PlannedTermsHelperBase plannedTermsHelper;


    //  Java to JSON outputter.
    public transient ObjectMapper mapper = new ObjectMapper();

    public transient AcademicRecordService academicRecordService;

    private transient LuService luService;

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

    public AcademicRecordService getAcademicRecordService() {
        if (this.academicRecordService == null) {
            //   TODO: Use constants for namespace.
            this.academicRecordService = (AcademicRecordService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/academicrecord", "arService"));
        }
        return this.academicRecordService;
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

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public boolean isAcademicCalendarServiceUp() {
        return isAcademicCalendarServiceUp;
    }

    public void setAcademicCalendarServiceUp(boolean academicCalendarServiceUp) {
        isAcademicCalendarServiceUp = academicCalendarServiceUp;
    }

    public boolean isAcademicRecordServiceUp() {
        return isAcademicRecordServiceUp;
    }

    public void setAcademicRecordServiceUp(boolean academicRecordServiceUp) {
        isAcademicRecordServiceUp = academicRecordServiceUp;
    }

    public boolean isCourseOfferingServiceUp() {
        return isCourseOfferingServiceUp;
    }

    public void setCourseOfferingServiceUp(boolean courseOfferingServiceUp) {
        isCourseOfferingServiceUp = courseOfferingServiceUp;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    protected AcademicCalendarService getAcademicCalendarService() {
        if (this.academicCalendarService == null) {
            this.academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return this.academicCalendarService;
    }

    protected LuService getLuService() {
        if (this.luService == null) {
            this.luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }


    @Override
    protected QuickAddForm createInitialForm(HttpServletRequest request) {
        return new QuickAddForm();
    }


    @RequestMapping(method = RequestMethod.GET)
    public ModelAndView get(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                            HttpServletRequest request, HttpServletResponse response) {

        super.start(form, result, request, response);
        form.setViewId("QuickAdd-FormView");
        form.setView(super.getViewService().getViewById("QuickAdd-FormView"));
        return getUIFModelAndView(form);
    }

    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result,
                              HttpServletRequest request, HttpServletResponse response) {
        super.start(form, result, request, response);
        ServicesStatusDataObject servicesStatusDataObject = (ServicesStatusDataObject) request.getSession().getAttribute(CourseSearchConstants.SWS_SERVICES_STATUS);
        if (!servicesStatusDataObject.isCourseOfferingServiceUp() || !servicesStatusDataObject.isAcademicCalendarServiceUp() || !servicesStatusDataObject.isAcademicRecordServiceUp()) {
            AtpHelper.addServiceError("courseCd");
            setAcademicCalendarServiceUp(servicesStatusDataObject.isAcademicCalendarServiceUp());
            setAcademicRecordServiceUp(servicesStatusDataObject.isAcademicRecordServiceUp());
            setCourseOfferingServiceUp(servicesStatusDataObject.isCourseOfferingServiceUp());
        }
        QuickAddForm searchForm = (QuickAddForm) form;
        if (hasText(searchForm.getAtpId()) && hasText(searchForm.getPlanType())) {
            String termYear = AtpHelper.atpIdToTermName(searchForm.getAtpId());
            if (searchForm.getPlanType().equalsIgnoreCase(QuickAddConstants.PLANNED_TYPE)) {
                termYear = termYear + QuickAddConstants.PLAN;
            } else if (searchForm.getPlanType().equalsIgnoreCase(QuickAddConstants.BACKUP_TYPE)) {
                termYear = termYear + QuickAddConstants.BACKUP;
            }
            searchForm.setTermYear(termYear);
        }
        return getUIFModelAndView(searchForm);
    }

    /*@RequestMapping(value = "autoSuggestions")
    public void autoSuggestions(HttpServletResponse response, HttpServletRequest request) {
        String queryText = request.getParameter("courseCd");
        String atpId = request.getParameter("atpId");
        List<String> results = new ArrayList<String>();
        if (queryText.length() >= 2) {
            if (hasText(queryText)) {
                SearchRequest searchRequest = null;
                HashMap<String, String> divisionMap = searchController.fetchCourseDivisions();
                *//*Params from the Url*//*
                String subject = null;
                String number = null;
                String searchText = upperCase(queryText);
                DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(searchText);
                if (courseCode.getSubject() != null && courseCode.getNumber() != null) {
                    number = courseCode.getNumber();
                    ArrayList<String> divisions = new ArrayList<String>();
                    subject = searchController.extractDivisions(divisionMap, courseCode.getSubject(), divisions, true);
                    if (divisions.size() > 0) {
                        subject = divisions.get(0);
                        searchRequest = new SearchRequest("myplan.clu.divisionAndCode");
                        results = searchController.getResults(searchRequest, subject, number);
                    }
                } else if (courseCode.getSubject() != null && !isNumeric(courseCode.getSubject())) {
                    ArrayList<String> divisions = new ArrayList<String>();
                    subject = searchController.extractDivisions(divisionMap, courseCode.getSubject(), divisions, true);
                    if (divisions.size() > 0) {
                        subject = divisions.get(0);
                        searchRequest = new SearchRequest("myplan.clu.division");
                        results = searchController.getResults(searchRequest, subject, number);
                    } else {
                        searchRequest = new SearchRequest("myplan.clu.division");
                        results = searchController.getResults(searchRequest, subject, number);
                    }
                }

                if (results.size() > 0) {
                    results = additionalFiltering(results, atpId);
                }

            } else {
                results.add("No courses found");
            }
        } else {
            results.add("Search Term Should be at least 2 characters");
        }
        StringBuilder jsonString = new StringBuilder();
        jsonString = jsonString.append("{ \"aaData\":[");
        for (String result : results) {
            jsonString.append("\"").append(result).append("\",");
        }
        String jsonStr = null;
        if (!jsonString.toString().equalsIgnoreCase("{ \"aaData\":[")) {
            jsonStr = jsonString.substring(0, jsonString.lastIndexOf(","));
        } else {
            jsonStr = jsonString.toString();
        }
        jsonStr = jsonStr + "]" + "}";
        try {
            response.setHeader("content-type", "application/json");
            response.setHeader("Cache-Control", "No-cache");
            response.setHeader("Cache-Control", "No-store");
            response.setHeader("Cache-Control", "max-age=0");
            response.getWriter().println(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }*/


    @RequestMapping(params = "methodToCall=quickAddCourse")
    public ModelAndView quickAddCourse(@ModelAttribute("KualiForm") QuickAddForm form, BindingResult result,
                                       HttpServletRequest request, HttpServletResponse response) {
        String[] parameters = {};
        if (UserSessionHelper.isAdviser()) {
            return doAdviserAccessError(form, "Adviser Access Denied", QuickAddConstants.ACCESS_DENIED, null);
        }
        if (!hasText(form.getCourseCd())) {
            return doOperationFailedError(form, "Course is missing", QuickAddConstants.EMPTY_SEARCH, null, parameters);
        } else if (form.getCourseCd().length() < 2) {
            return doOperationFailedError(form, "Course is missing", QuickAddConstants.EMPTY_SEARCH, null, parameters);
        }

        String courseId = null;
        HashMap<String, String> divisionMap = searchController.fetchCourseDivisions();
        DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(form.getCourseCd());
        if (courseCode.getSubject() != null && courseCode.getNumber() != null) {
            String subject = courseCode.getSubject();
            String number = courseCode.getNumber();
            if (number.length() != 3) {
                return doOperationFailedError(form, "Course number is wrong", QuickAddConstants.COURSE_NOT_FOUND, null, new String[]{form.getCourseCd()});
            }
            ArrayList<String> divisions = new ArrayList<String>();
            searchController.extractDivisions(divisionMap, subject, divisions, false);
            if (divisions.size() > 0) {
                subject = divisions.get(0);
                form.setCourseId(getCourseHelper().getCourseId(subject, number));
                courseId = form.getCourseId();
            } else {
                return doOperationFailedError(form, "Could not find course", QuickAddConstants.COURSE_NOT_FOUND, null, new String[]{form.getCourseCd()});
            }
        } else {
            return doOperationFailedError(form, "Could not find course", QuickAddConstants.COURSE_NOT_FOUND, null, new String[]{form.getCourseCd()});
        }

        if (!hasText(courseId)) {
            return doOperationFailedError(form, "Could not find course", QuickAddConstants.COURSE_NOT_FOUND, null, new String[]{form.getCourseCd()});
        }

        //  Further validation of ATP IDs will happen in the service validation methods.
        if (isEmpty(form.getAtpId())) {
            return doOperationFailedError(form, "Term Year value missing", QuickAddConstants.COURSE_NOT_FOUND, null, new String[]{form.getCourseCd()});
        }

        //  Should the course be type 'planned' or 'backup'. Default to planned.
        if (form.getPlanType() == null) {
            return doOperationFailedError(form, "Plan Type is missing", ERROR_KEY_OPERATION_FAILED, null, new String[]{});
        }
        boolean backup = form.getPlanType().equalsIgnoreCase("backup");
        String newType = LEARNING_PLAN_ITEM_TYPE_PLANNED;
        if (backup) {
            newType = LEARNING_PLAN_ITEM_TYPE_BACKUP;
        }

        //  This list can only contain one item, otherwise the backend validation will fail.
        List<String> newAtpIds = null;
        try {
            newAtpIds = new LinkedList<String>();
            //  Create an ATP id from the values in the year and term fields.
            if (isEmpty(form.getAtpId())) {
                throw new RuntimeException("Could not construct ATP id for Given TermYear option because year was blank.");
            }

            newAtpIds.add(form.getAtpId());
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Unable to process request.", PlanConstants.ERROR_KEY_OPERATION_FAILED, e, new String[]{});
        }
        if (!AtpHelper.isAtpIdFormatValid(newAtpIds.get(0))) {
            return doOperationFailedError(form, String.format("ATP ID [%s] was not formatted properly.", newAtpIds.get(0)), ERROR_KEY_OPERATION_FAILED, null, new String[]{});
        }

        String studentId = UserSessionHelper.getStudentRegId();

        LearningPlan plan = null;
        synchronized (studentId) {
            try {
                //  If something goes wrong with the query then a RuntimeException will be thrown. Otherwise, the method
                //  will return the default plan or null. Having multiple plans will also produce a RuntimeException.
                plan = getLearningPlan(studentId);
            } catch (RuntimeException e) {
                return doOperationFailedError(form, "Query for default learning plan failed.", ERROR_KEY_OPERATION_FAILED, e, new String[]{});
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
                    return doOperationFailedError(form, "Unable to create learning plan.", ERROR_KEY_OPERATION_FAILED, e, new String[]{});
                }
            }
        }
        //  Lookup course details as well need them in case there is an error below.
        CourseSummaryDetails courseDetails = null;
        try {
            courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(courseId);
        } catch (Exception e) {
            return doOperationFailedError(form, "Unable to retrieve Course Details.", ERROR_KEY_OPERATION_FAILED, null, new String[]{});
        }

        /*  Do validations. */
        //  Plan Size exceeded.
        boolean hasCapacity = false;
        try {
            hasCapacity = isAtpHasCapacity(plan, newAtpIds.get(0), newType);
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Could not validate capacity for new plan item.", ERROR_KEY_OPERATION_FAILED, e, new String[]{});
        }

        if (!hasCapacity) {
            return doPlanCapacityExceededError(form, newType);
        }

        //  Validate: Adding to historical term.
        if (!AtpHelper.isAtpSetToPlanning(newAtpIds.get(0))) {
            return doCannotChangeHistoryError(form);
        }

        //  See if a wishlist item exists for the course. If so, then update it. Otherwise create a new plan item.
        PlanItemInfo planItem = getWishlistPlanItem(courseDetails.getVersionIndependentId());
        //  Storage for wishlist events.
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> wishlistEvents = null;
        //  Create a new plan item if no wishlist exists. Otherwise, update the wishlist item.
        if (planItem == null) {
            try {
                planItem = addPlanItem(plan, courseDetails.getVersionIndependentId(), newAtpIds, newType);
            } catch (DuplicateEntryException e) {
                return doDuplicatePlanItem(form, newAtpIds.get(0), courseDetails);
            } catch (Exception e) {
                return doOperationFailedError(form, "Unable to add plan item.", ERROR_KEY_OPERATION_FAILED, e, new String[]{});
            }
        } else {
            //  Check for duplicates since addPlanItem isn't being called.
            if (isDuplicate(plan, newAtpIds.get(0), courseDetails.getVersionIndependentId(), newType)) {
                return doDuplicatePlanItem(form, newAtpIds.get(0), courseDetails);
            }
            //  Create wishlist events before updating the plan item.
            wishlistEvents = makeRemoveEvent(planItem, courseDetails);
            planItem.setTypeKey(newType);
            planItem.setPlanPeriods(newAtpIds);

            try {
                planItem = getAcademicPlanService().updatePlanItem(planItem.getId(), planItem, UserSessionHelper.makeContextInfoInstance());
            } catch (Exception e) {
                return doOperationFailedError(form, "Unable MetaENtito update wishlist plan item.", ERROR_KEY_OPERATION_FAILED, e, new String[]{});
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
        } catch (RuntimeException e) {
            return doOperationFailedError(form, "Unable to create add event.", ERROR_KEY_OPERATION_FAILED, e, new String[]{});
        }

        events.putAll(makeUpdateTotalCreditsEvent(planItem.getPlanPeriods().get(0), PlanConstants.JS_EVENT_NAME.UPDATE_NEW_TERM_TOTAL_CREDITS));

        //  Populate the form.
        form.setJavascriptEvents(events);
        String[] params = {AtpHelper.atpIdToTermName(planItem.getPlanPeriods().get(0))};
        return doPlanActionSuccess(form, PlanConstants.SUCCESS_KEY_PLANNED_ITEM_ADDED, params);

    }

    /**
     * Creates events map for a remove.
     *
     * @param planItem
     * @return
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeRemoveEvent(PlanItemInfo
                                                                                          planItem, CourseSummaryDetails courseDetails) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();
        Map<String, String> params = new HashMap<String, String>();

        //  Only planned or backup items get an atpId attribute.
        if (planItem.getTypeKey().equals(LEARNING_PLAN_ITEM_TYPE_PLANNED) ||
                planItem.getTypeKey().equals(LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            params.put("atpId", formatAtpIdForUI(planItem.getPlanPeriods().get(0)));
        }
        params.put("planItemType", formatTypeKey(planItem.getTypeKey()));
        params.put("planItemId", planItem.getId());
        //  Create Javascript events.
        String courseDetailsAsJson;
        try {
            if (courseDetails == null) {
                courseDetails = getCourseDetailsInquiryService().retrieveCourseSummaryById(planItem.getRefObjectId());
            }
            //  Serialize course details into a string of JSON.
            courseDetailsAsJson = mapper.writeValueAsString(courseDetails);
        } catch (Exception e) {
            logger.error("Could not convert javascript events to JSON.", e);
            throw new RuntimeException("Could not convert javascript events to JSON.", e);
        }
        params.put("courseDetails", courseDetailsAsJson);
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_DELETED, params);
        return events;
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
        String studentId = UserSessionHelper.getStudentRegId();
        LearningPlan learningPlan = getLearningPlan(studentId);
        if (learningPlan == null) {
            throw new RuntimeException(String.format("Could not find the default plan for [%s].", studentId));
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
        if (isEmpty(planId)) {
            throw new RuntimeException("Plan Id was empty.");
        }

        if (isEmpty(courseId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        if (isEmpty(atpId)) {
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

        if (isEmpty(courseId)) {
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

        String atpId = null;
        if (null != termIds) {
            pii.setPlanPeriods(termIds);
            atpId = termIds.get(0);
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
     * Creates an update credits event.
     *
     * @param atpId The id of the term.
     * @return
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeUpdateTotalCreditsEvent(String atpId, PlanConstants.JS_EVENT_NAME eventName) {
        Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> events = new LinkedHashMap<PlanConstants.JS_EVENT_NAME, Map<String, String>>();

        Map<String, String> params = new HashMap<String, String>();

        params.put("atpId", formatAtpIdForUI(atpId));
        String totalCredits = getPlannedTermsHelper().getTotalCredits(atpId);
        params.put("totalCredits", totalCredits);

        events.put(eventName, params);
        return events;
    }


    private String formatTypeKey(String typeKey) {
        return typeKey.substring(typeKey.lastIndexOf(".") + 1);
    }

    private String formatAtpIdForUI(String atpId) {
        return atpId.replaceAll("\\.", "-");
    }

    /**
     * Creates an add plan item event.
     *
     * @param planItem
     * @param courseDetails
     * @return
     * @throws RuntimeException if anything goes wrong.
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> makeAddEvent(PlanItemInfo planItem, CourseSummaryDetails courseDetails) {
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

            // event for alert Icon
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
        events.put(PlanConstants.JS_EVENT_NAME.PLAN_ITEM_ADDED, params);
        return events;
    }


    /**
     * Gets a Plan Item of type "wishlist" for a particular course. There should only ever be one.
     *
     * @param courseId The id of the course.
     * @return A PlanItem of type wishlist.
     */
    protected PlanItemInfo getWishlistPlanItem(String courseId) {

        if (isEmpty(courseId)) {
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

        if (isEmpty(atpId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        List<PlanItemInfo> planItems = null;
        try {
            planItems = getAcademicPlanService().getPlanItemsInPlanByType(plan.getId(), typeKey, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        if (planItems == null) {
            throw new RuntimeException("Could not retrieve plan items.");
        }

        int counter = 0;
        for (PlanItem p : planItems) {
            String plannedTerm = p.getPlanPeriods().get(0);
            String luType = p.getRefObjectType();
            if (plannedTerm.equals(atpId) && "kuali.lu.type.CreditCourse".equals(luType)) {
                counter++;
            }
        }

        int capacity = LEARNING_PLAN_ITEM_TYPE_BACKUP.equals(typeKey) ? BACKUP_PLAN_ITEM_CAPACITY : PLANNED_PLAN_ITEM_CAPACITY;
        boolean hasCapacity = counter < capacity;
        return hasCapacity;
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


    private List<String> getNewTermIds(QuickAddForm form) {
        List<String> newTermIds = new LinkedList<String>();
        //  Create an ATP id from the values in the year and term fields.
        if (isEmpty(form.getAtpId())) {
            throw new RuntimeException("Could not construct ATP id for Given TermYear option because year was blank.");
        }

        newTermIds.add(form.getAtpId());
        return newTermIds;
    }

    /**
     * Blow-up response for all plan item actions for the Adviser.
     */
    private ModelAndView doAdviserAccessError(QuickAddForm form, String errorMessage, String errorKey, Exception e) {
        String[] params = {};
        return doErrorPage(form, errorMessage, errorKey, params, e);
    }


    /**
     * Blow up response of the plan capacity validation fails.
     *
     * @param form
     * @return
     */
    private ModelAndView doPlanCapacityExceededError(QuickAddForm form, String type) {
        String errorId = ERROR_KEY_PLANNED_ITEM_CAPACITY_EXCEEDED;
        if (type.equals(LEARNING_PLAN_ITEM_TYPE_BACKUP)) {
            errorId = ERROR_KEY_BACKUP_ITEM_CAPACITY_EXCEEDED;
        }
        return doErrorPage(form, errorId, new String[0]);
    }

    /**
     * Blow up response if the user tries to update plan items in past terms.
     *
     * @param form
     * @return
     */
    private ModelAndView doCannotChangeHistoryError(QuickAddForm form) {
        return doErrorPage(form, PlanConstants.ERROR_KEY_HISTORICAL_ATP, new String[0]);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doPageRefreshError(QuickAddForm form, String errorMessage, Exception e) {
        // <a href="/student/myplan/plan?methodToCall=start&viewId=PlannedCourses-FormView">Reset your academic plan</a>
        // Removed link because html string is being encoded in the view
        String[] params = {};
        if (e != null) {
            logger.error(errorMessage, e);
        } else {
            logger.error(errorMessage);
        }
        return doErrorPage(form, errorMessage, ERROR_KEY_PAGE_RESET_REQUIRED, params, e);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doOperationFailedError(QuickAddForm form, String errorMessage, String errorKey, Exception e, String[] params) {
        if (e != null) {
            logger.error(errorMessage, e);
        } else {
            logger.error(errorMessage);
        }
        return doErrorPage(form, errorMessage, errorKey, params, e);
    }

    /**
     * Logs errors and passes the request on to the error page.
     */
    private ModelAndView doErrorPage(QuickAddForm form, String errorMessage, String errorKey, String[] params, Exception e) {
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
    private ModelAndView doErrorPage(QuickAddForm form, String errorKey, String[] params) {
        form.setRequestStatus(QuickAddForm.REQUEST_STATUS.ERROR);
        GlobalVariables.getMessageMap().clearErrorMessages();
        GlobalVariables.getMessageMap().putErrorForSectionId(QuickAddConstants.QUICK_ADD_RESPONSE_PAGE_ID, errorKey, params);
        return getUIFModelAndView(form, QuickAddConstants.QUICK_ADD_RESPONSE_PAGE_ID);
    }

    /**
     * Blow-up response for all plan item actions.
     */
    private ModelAndView doDuplicatePlanItem(QuickAddForm form, String atpId, CourseSummaryDetails courseDetails) {
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
    private ModelAndView doPlanActionSuccess(QuickAddForm form, String key, String[] params) {
        form.setRequestStatus(QuickAddForm.REQUEST_STATUS.SUCCESS);
        GlobalVariables.getMessageMap().putInfoForSectionId(QuickAddConstants.QUICK_ADD_RESPONSE_PAGE_ID, key, params);
        return getUIFModelAndView(form, QuickAddConstants.QUICK_ADD_RESPONSE_PAGE_ID);
    }

    /* public List<String> additionalFiltering(List<String> results, String atpId) {
int year = Calendar.getInstance().get(Calendar.YEAR) - 10;
int resultsSize = results.size();
if (isCourseOfferingServiceUp()) {
for (int i = 0; i < resultsSize; i++) {
DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(results.get(i));
String courseId = getCourseHelper().getCourseId(courseCode.getSubject(), courseCode.getNumber());
List<CourseOfferingInfo> courseOfferingInfo = null;
boolean removed = false;

try {

    *//*Filtering courses that are not offered in the given term*//*
                    List<CourseOfferingInfo> offerings = getCourseOfferingService().getCourseOfferingsByCourseAndTerm(courseId, atpId, CourseSearchConstants.CONTEXT_INFO);
                    if (offerings == null || offerings.size() == 0) {
                        results.remove(results.get(i));
                        resultsSize--;
                        removed = true;
                    }
                    *//*Filtering courses that are not offered for more than 10 years*//*
                    if (!removed) {
                        String values = String.format("%s, %s, %s", year, courseCode.getSubject(), courseCode.getNumber());
                        courseOfferingInfo = getCourseOfferingService()
                                .searchForCourseOfferings(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("values", values)), CourseSearchConstants.CONTEXT_INFO);
                        if (courseOfferingInfo == null) {
                            results.remove(results.get(i));
                            resultsSize--;
                        }
                    }

                } catch (Exception e) {
                    logger.error("Could not filter results as SWS call failed", e);
                }
            }
        }
        if (results.size() > 9) {
            List<String> trimmedList = new ArrayList<String>();
            for (String result : results) {
                trimmedList.add(result);
                if (trimmedList.size() == 10) {
                    break;
                }

            }
            results = new ArrayList<String>();
            results.addAll(trimmedList);
        }
        return results;
    }*/


}


