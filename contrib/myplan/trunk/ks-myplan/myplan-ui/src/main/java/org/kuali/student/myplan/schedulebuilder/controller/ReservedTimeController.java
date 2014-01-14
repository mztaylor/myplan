package org.kuali.student.myplan.schedulebuilder.controller;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.datadictionary.validation.result.ConstraintValidationResult;
import org.kuali.rice.krad.datadictionary.validation.result.DictionaryValidationResult;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.schedulebuilder.form.ReservedTimeForm;
import org.kuali.student.myplan.schedulebuilder.support.DefaultScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.PlanEventUtils;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Iterator;

@Controller
@RequestMapping(value = "/sb/reserved")
public class ReservedTimeController extends UifControllerBase {

	private static final Logger LOG = Logger
			.getLogger(ReservedTimeController.class);

	private static final String FORM = "ScheduleBuild-ReservedTime-FormView";
	private static final String CREATE_PAGE = "sb_create_reserved_time_page";

    private static ScheduleBuildStrategy scheduleBuildStrategy;

	private static boolean authorize(ReservedTimeForm form,
			HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		try {
			getScheduleBuildStrategy()
					.getLearningPlan(form.getRequestedLearningPlanId());
			return true;
		} catch (PermissionDeniedException e) {
			LOG.warn(
					"User "
							+ request.getRemoteUser()
							+ " is not permitted to build a schedule based on this learning plan.",
					e);
			response.sendError(
					HttpServletResponse.SC_FORBIDDEN,
					"User "
							+ request.getRemoteUser()
							+ " is not permitted to build a schedule based on this learning plan.");
			return false;
		}

	}

	@Override
	protected UifFormBase createInitialForm(HttpServletRequest request) {
		return new ReservedTimeForm();
	}

	@RequestMapping(params = "methodToCall=startDialog")
	public ModelAndView startDialog(
			@ModelAttribute("KualiForm") ReservedTimeForm form,
			BindingResult result, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		if (!authorize(form, request, response))
			return null;
		super.start((UifFormBase) form, result, request, response);
		form.setViewId(FORM);
		form.setView(super.getViewService().getViewById(FORM));
		return getUIFModelAndView(form);
	}

	@RequestMapping(method = RequestMethod.POST, params = {
			"methodToCall=createReservedTime",
			"view.currentPageId=" + CREATE_PAGE })
	public ModelAndView postCreate(
			@ModelAttribute("KualiForm") ReservedTimeForm form,
			BindingResult result, HttpServletRequest request,
			HttpServletResponse response) throws IOException, ServletException {
		if (!authorize(form, request, response))
			return null;

		if (result.hasErrors()) {
			LOG.warn("Errors validating reserve time form "
					+ result.getAllErrors());
			response.sendError(
					HttpServletResponse.SC_BAD_REQUEST,
					"Errors validating reserve time form "
							+ result.getAllErrors());
		}

		DictionaryValidationResult validationResult = KRADServiceLocatorWeb
				.getViewValidationService().validateView(form);
		if (validationResult.getNumberOfErrors() > 0) {
			StringBuilder sb = new StringBuilder(
					"Errors validating reserve time from");
			Iterator<ConstraintValidationResult> errIter = validationResult
					.iterator();
			while (errIter.hasNext()) {
				ConstraintValidationResult err = errIter.next();
				sb.append("\n    ");
				sb.append(err.getAttributeName());
				sb.append(" ");
				sb.append(err.getAttributePath());
				sb.append(" ");
				sb.append(err.getConstraintName());
				sb.append(" ");
				sb.append(err.getConstraintLabelKey());
				sb.append(" ");
				sb.append(err.getEntryName());
				sb.append(" ");
				sb.append(err.getErrorKey());
				sb.append(" ");
				sb.append(Arrays.toString(err.getErrorParameters()));
				sb.append(" ");
				sb.append(err.getStatus());
			}
			LOG.warn(sb);
			response.sendError(HttpServletResponse.SC_BAD_REQUEST,
					sb.toString());
			return null;
		}

		try {
			getScheduleBuildStrategy().createReservedTime(form.getRequestedLearningPlanId(), form);
			form.setComplete(true);
		} catch (PermissionDeniedException e) {
			throw new ServletException("Unexpected authorization failure", e);
		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("M/dd/yyyy");
		SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
		JsonObjectBuilder addedReservedTime = Json.createObjectBuilder();
		addedReservedTime.add("uid", form.getUniqueId());
		addedReservedTime.add("description", form.getDescription());
		addedReservedTime.add("allDay", form.isAllDay());
		addedReservedTime.add("startDate",
				dateFormat.format(form.getStartDate()));
		addedReservedTime.add("startTime",
				timeFormat.format(form.getStartDate()));
		addedReservedTime
				.add("endTime", timeFormat.format(form.getUntilDate()));
		addedReservedTime.add("untilDate",
				dateFormat.format(form.getUntilDate()));
		addedReservedTime.add("sunday", form.isSunday());
		addedReservedTime.add("monday", form.isMonday());
		addedReservedTime.add("tuesday", form.isTuesday());
		addedReservedTime.add("wednesday", form.isWednesday());
		addedReservedTime.add("thursday", form.isThursday());
		addedReservedTime.add("friday", form.isFriday());
		addedReservedTime.add("saturday", form.isSaturday());
		JsonObjectBuilder events = PlanEventUtils.getEventsBuilder();
		events.add("addedReservedTime", addedReservedTime);

		PlanEventUtils.sendJsonEvents(true, "Added reserved time", response);
		return null;
	}

    public static ScheduleBuildStrategy getScheduleBuildStrategy() {
        if(scheduleBuildStrategy == null){
            scheduleBuildStrategy = new DefaultScheduleBuildStrategy();
        }
        return scheduleBuildStrategy;
    }

    public static void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        ReservedTimeController.scheduleBuildStrategy = scheduleBuildStrategy;
    }
}
