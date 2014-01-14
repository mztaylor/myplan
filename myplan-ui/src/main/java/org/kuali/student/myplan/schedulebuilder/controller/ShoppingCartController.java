package org.kuali.student.myplan.schedulebuilder.controller;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.support.DefaultScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.support.DefaultShoppingCartStrategy;
import org.kuali.student.myplan.schedulebuilder.util.*;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.infc.Attribute;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping(value = "/sb/cart")
public class ShoppingCartController extends UifControllerBase {

    private static final String SB_CART_FORM = "ShoppingCart-FormView";
    private static final String SB_ADD_FROM_PLAN_PAGE = "sb_cart_add_from_plan_page";
    private static final String SB_ADD_FROM_SB = "sb_cart_add_from_sb_page";
    private static final String SB_REMOVE_PAGE = "sb_cart_remove_page";

    private ShoppingCartStrategy shoppingCartStrategy;

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private transient AcademicPlanService academicPlanService;

    private ModelAndView startPlannerDialog(
            @ModelAttribute("KualiForm") ShoppingCartForm form,
            BindingResult result, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        super.start((UifFormBase) form, result, request, response);

        PlanItem planItem = PlanItemControllerHelper.getValidatedPlanItem(form,
                request, response);
        if (planItem == null)
            return null;

        Term term = form.getTerm();
        if (!form.isOfficial() || !form.isPlanning()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Term "
                    + form.getTerm() + " not found");
            return null;
        }

        ShoppingCartStrategy shoppingCartStrategy = getShoppingCartStrategy();
        List<CourseOption> courseOptions = shoppingCartStrategy
                .getCourseOptionsForPlanItem(term, planItem);
        form.setCourseOptions(courseOptions);
        form.setShoppingCartRequests(shoppingCartStrategy.createRequests(term,
                courseOptions));

        UifFormBase uifForm = (UifFormBase) form;
        uifForm.setJumpToId("popupForm");
        uifForm.setViewId(SB_CART_FORM);
        uifForm.setView(super.getViewService().getViewById(SB_CART_FORM));
        return getUIFModelAndView(uifForm);
    }

    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return (UifFormBase) getScheduleBuildStrategy().getInitialCartForm();
    }

    @RequestMapping(params = "pageId="
            + SB_ADD_FROM_SB)
    public ModelAndView startAddFromScheduleBuild(
            @ModelAttribute("KualiForm") ShoppingCartForm form,
            BindingResult result, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {

        LearningPlan plan = PlanItemControllerHelper.getAuthorizedLearningPlan(form, request,
                response);
        if (plan == null)
            return null;

        Term term = form.getTerm();
        if (!form.isOfficial() || !form.isPlanning()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Term "
                    + form.getTermId() + " not found");
            return null;
        }

        String psoid = form.getPossibleScheduleId();
        if (psoid == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Possible schedules ID not post");
            return null;
        }

        @SuppressWarnings("unchecked")
        Map<String, PossibleScheduleOption> psomap = (Map<String, PossibleScheduleOption>) GlobalVariables
                .getUserSession().getObjectMap()
                .get(ShoppingCartForm.POSSIBLE_OPTIONS_KEY);
        if (psomap == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Possible schedules have not been populated");
            return null;
        }

        PossibleScheduleOption pso = psomap.get(psoid);
        if (pso == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid possible schedule ID " + psoid);
            return null;
        }

        ShoppingCartStrategy shoppingCartStrategy = getShoppingCartStrategy();
        form.setShoppingCartRequests(shoppingCartStrategy.createRequests(
                plan.getId(), term, pso));

        UifFormBase uifForm = (UifFormBase) form;
        uifForm.setViewId(SB_CART_FORM);
        uifForm.setView(super.getViewService().getViewById(SB_CART_FORM));
        return getUIFModelAndView(uifForm);
    }

    @RequestMapping(params = "pageId="
            + SB_ADD_FROM_PLAN_PAGE)
    public ModelAndView startAddFromPlan(
            @ModelAttribute("KualiForm") ShoppingCartForm form,
            BindingResult result, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        // Add to the cart, we expect that item type is PLANNED
        // This will set courseOption.selected, and subsequently
        // shoppingCartRequest.addToCart, to true.
        form.setExpectedPlanItemCategory(AcademicPlanServiceConstants.ItemCategory.PLANNED);
        return startPlannerDialog(form, result, request, response);
    }

    @RequestMapping(params = "pageId="
            + SB_REMOVE_PAGE)
    public ModelAndView startRemove(
            @ModelAttribute("KualiForm") ShoppingCartForm form,
            BindingResult result, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        // Remove from the cart, we expect that item type is CART
        // This will set courseOption.selected, and subsequently
        // shoppingCartRequest.addToCart, to false.
        form.setExpectedPlanItemCategory(AcademicPlanServiceConstants.ItemCategory.CART);
        return startPlannerDialog(form, result, request, response);
    }

    @RequestMapping(method = RequestMethod.POST)
    public ModelAndView processRequests(
            @ModelAttribute("KualiForm") ShoppingCartForm form,
            BindingResult result, HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {

        LearningPlan plan = PlanItemControllerHelper.getAuthorizedLearningPlan(
                form, request, response);
        if (plan == null)
            return null;

        List<ShoppingCartRequest> cartRequests = form.getShoppingCartRequests();
        if (cartRequests == null || cartRequests.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "No shopping cart requests posted");
            return null;
        }

        ShoppingCartStrategy shoppingCartStrategy = getShoppingCartStrategy();

        for (ShoppingCartRequest cartRequest : cartRequests) {
            Term term = cartRequest.getTerm();
            if (term == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Cart request " + cartRequest + " is missing term");
                return null;
            }

            Course course = cartRequest.getCourse();
            if (course == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Cart request " + cartRequest + " is missing course");
                return null;
            }

            String campusCode = null;
            for (Attribute attr : course.getAttributes())
                if ("campusCode".equals(attr.getKey()))
                    campusCode = attr.getValue();
            if (campusCode == null) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Course " + course.getCode()
                                + " is missing campusCode attribute");
                return null;
            }

            String termId = cartRequest.getTerm().getId();
            if (!shoppingCartStrategy.isCartAvailable(termId, campusCode)) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "Shopping cart is not available for " + termId
                                + " on campus " + campusCode);
                return null;
            }
        }

        cartRequests = shoppingCartStrategy.processRequests(cartRequests);

        JsonArrayBuilder jresults = Json.createArrayBuilder();
        boolean success = true;
        StringBuilder message = new StringBuilder();
        for (ShoppingCartRequest cartRequest : cartRequests) {

            if (!cartRequest.isError()) {
                Course course = cartRequest.getCourse();
                if (cartRequest.isAddToCart()) {
                    PlanItemInfo planItemInfo = new PlanItemInfo();
                    planItemInfo
                            .setCategory(AcademicPlanServiceConstants.ItemCategory.CART);
                    planItemInfo.setTypeKey(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_COURSE);
                    planItemInfo
                            .setStateKey(PlanConstants.LEARNING_PLAN_ITEM_ACTIVE_STATE_KEY);
                    planItemInfo.setLearningPlanId(plan.getId());
                    planItemInfo.setRefObjectId(course.getId());
                    planItemInfo.setRefObjectType(PlanConstants.COURSE_TYPE);
                    List<String> planPeriods = new ArrayList<String>(
                            1);
                    planPeriods.add(cartRequest.getTerm().getId());
                    planItemInfo.setPlanPeriods(planPeriods);
                    planItemInfo.setCredit(cartRequest.getCredits());

                    String campusCode = null;
                    for (Attribute cattr : cartRequest.getCourse()
                            .getAttributes())
                        if ("campusCode".equals(cattr.getKey()))
                            campusCode = cattr.getValue();

                    StringBuilder activityCode = new StringBuilder(
                            cartRequest.getPrimaryRegistrationCode());
                    List<String> regCodes = cartRequest
                            .getSecondaryRegistrationCodes();
                    if (regCodes != null)
                        for (String regCode : regCodes)
                            activityCode.append(',').append(regCode);
                    try {
                        planItemInfo = getAcademicPlanService().createPlanItem(
                                        planItemInfo,
                                        PlanConstants.CONTEXT_INFO);

                        List<AttributeInfo> attrs = new ArrayList<AttributeInfo>(
                                2);
                        if (campusCode != null)
                            attrs.add(new AttributeInfo("campusCode",
                                    campusCode));
                        attrs.add(new AttributeInfo("activityCode",
                                activityCode.toString()));
                        planItemInfo.setAttributes(attrs);

                        planItemInfo = getAcademicPlanService().updatePlanItem(
                                        planItemInfo.getId(),
                                        planItemInfo,
                                        PlanConstants.CONTEXT_INFO);

                    } catch (AlreadyExistsException e) {
                        throw new IllegalArgumentException(
                                "LP service failure", e);
                    } catch (DataValidationErrorException e) {
                        throw new IllegalArgumentException(
                                "LP service failure", e);
                    } catch (InvalidParameterException e) {
                        throw new IllegalArgumentException(
                                "LP service failure", e);
                    } catch (MissingParameterException e) {
                        throw new IllegalArgumentException(
                                "LP service failure", e);
                    } catch (OperationFailedException e) {
                        throw new IllegalStateException("LP service failure", e);
                    } catch (PermissionDeniedException e) {
                        throw new IllegalStateException("LP service failure", e);
                    } catch (DoesNotExistException e) {
                        throw new IllegalStateException("LP service failure", e);
                    }

                    if (form.getPossibleScheduleId() == null) {
                        // Don't send planner events back to schedule build
                        PlanEventUtils.makeAddEvent(planItemInfo);
                        PlanEventUtils.updateTotalCreditsEvent(true, cartRequest
                                .getTerm().getId());
                    }

                } else {

                    try {
                        AcademicPlanService academicPlanService = getAcademicPlanService();
                        ContextInfo context = PlanConstants.CONTEXT_INFO;
                        List<PlanItemInfo> planItemInfos = academicPlanService
                                .getPlanItemsInPlanByRefObjectIdByRefObjectType(
                                        plan.getId(), course.getId(),
                                        PlanConstants.COURSE_TYPE, context);
                        for (PlanItemInfo planItemInfo : planItemInfos) {
                            if (planItemInfo.getCategory().equals(
                                    AcademicPlanServiceConstants.ItemCategory.CART)) {
                                if (!planItemInfo.getPlanPeriods().contains(
                                        cartRequest.getTerm().getId()))
                                    continue;
                                String activityCodeString = planItemInfo
                                        .getAttributeValue("activityCode");
                                List<String> activityCodes = activityCodeString == null ? null
                                        : Arrays.asList(activityCodeString
                                        .split(","));
                                if (activityCodes == null
                                        || activityCodes.contains(cartRequest
                                        .getPrimaryRegistrationCode())) {
                                    academicPlanService.deletePlanItem(
                                            planItemInfo.getId(), context);
                                    if (form.getUniqueId() != null) {
                                        // Send planner events back only when uniqueId is posted
                                        // to correspond to an element on the front-end.
                                        PlanEventUtils.makeRemoveEvent(
                                                form.getUniqueId(), planItemInfo);
                                        PlanEventUtils
                                                .updateTotalCreditsEvent(true,
                                                        cartRequest.getTerm().getId());
                                    }
                                }
                            }
                        }

                    } catch (DoesNotExistException e) {
                        throw new IllegalArgumentException(
                                "LP service failure", e);
                    } catch (InvalidParameterException e) {
                        throw new IllegalArgumentException(
                                "LP service failure", e);
                    } catch (MissingParameterException e) {
                        throw new IllegalArgumentException(
                                "LP service failure", e);
                    } catch (OperationFailedException e) {
                        throw new IllegalStateException("LP service failure", e);
                    } catch (PermissionDeniedException e) {
                        throw new IllegalStateException("LP service failure", e);
                    }

                }
            }

            JsonObjectBuilder jresult = Json.createObjectBuilder();
            jresult.add("uniqueId", cartRequest.getUniqueId());
            jresult.add("courseId", cartRequest.getCourse().getId());
            jresult.add("termId", cartRequest.getTerm().getId());
            if (cartRequest.isError()) {
                jresult.add("error", true);
                success = false;
            }

            if (cartRequest.getMessage() != null) {
                String msg = cartRequest.getMessage().getPlain();
                if (msg == null)
                    if (cartRequest.isAddToCart())
                        msg = cartRequest.isError() ? "Add failed" : "Added";
                    else
                        msg = cartRequest.isError() ? "Remove failed"
                                : "Removed";
                jresult.add("message", msg);
                message.append(cartRequest.getCourse().getCode());
                String primaryRegCode = cartRequest
                        .getPrimaryRegistrationCode();
                if (primaryRegCode != null) {
                    message.append(" Class #");
                    message.append(cartRequest.getPrimaryRegistrationCode());
                }
                message.append(": ");
                message.append(msg);
                message.append("\n");
            }

            if (cartRequest.isProcessed()) {
                jresult.add("processed", true);
            }

            jresults.add(jresult);
        }
        JsonObjectBuilder json = PlanEventUtils.getEventsBuilder();
        json.add("cartRequests", jresults);
        if (form.getPossibleScheduleId() != null)
            json.add("multi", true);
        PlanEventUtils.sendJsonEvents(success, message.toString(), response);
        return null;
    }

    public ShoppingCartStrategy getShoppingCartStrategy() {
        if (shoppingCartStrategy == null) {
            shoppingCartStrategy = new DefaultShoppingCartStrategy();
        }
        return shoppingCartStrategy;
    }

    public void setShoppingCartStrategy(ShoppingCartStrategy shoppingCartStrategy) {
        this.shoppingCartStrategy = shoppingCartStrategy;
    }

    public ScheduleBuildStrategy getScheduleBuildStrategy() {
        if(scheduleBuildStrategy == null){
            scheduleBuildStrategy = new DefaultScheduleBuildStrategy();
        }
        return scheduleBuildStrategy;
    }

    public void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        this.scheduleBuildStrategy = scheduleBuildStrategy;
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
}
