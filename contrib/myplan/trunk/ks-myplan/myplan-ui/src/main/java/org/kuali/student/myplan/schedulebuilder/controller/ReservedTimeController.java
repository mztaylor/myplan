package org.kuali.student.myplan.schedulebuilder.controller;

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.krad.datadictionary.validation.result.ConstraintValidationResult;
import org.kuali.rice.krad.datadictionary.validation.result.DictionaryValidationResult;
import org.kuali.rice.krad.service.KRADServiceLocatorWeb;
import org.kuali.rice.krad.web.controller.extension.KsapControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.schedulebuilder.form.ReservedTimeForm;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildHelper;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;

@Controller
@RequestMapping(value = "/sb/reserved")
public class ReservedTimeController extends KsapControllerBase {

    private static final Logger LOG = Logger
            .getLogger(ReservedTimeController.class);

    private static final String FORM = "ScheduleBuild-ReservedTime-FormView";
    private static final String CREATE_PAGE = "sb_create_reserved_time_page";

    private static ScheduleBuildStrategy scheduleBuildStrategy;

    private TermHelper termHelper;

    private CalendarUtil calendarUtil;

    private ScheduleBuildHelper scheduleBuildHelper;


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
            HttpServletResponse response) throws IOException, ServletException, OperationFailedException {
        if (!authorize(form, request, response))
            return null;
        super.start((UifFormBase) form, result, request, response);
        form.setViewId(FORM);
        form.setView(super.getViewService().getViewById(FORM));

        if (StringUtils.isEmpty(form.getTermId())) {
            throw new OperationFailedException("No atpId is found");
        }

        Term term = getTermHelper().getTermByAtpId(form.getTermId());
        Date startDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date endDate = getCalendarUtil().getDateAfterXdays(startDate, 5);
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        form.setStartDateStr(dateFormat.format(startDate));
        form.setUntilDateStr(dateFormat.format(endDate));
        return getUIFModelAndView(form);
    }

    @RequestMapping(method = RequestMethod.POST, params = {
            "methodToCall=createReservedTime",
            "view.currentPageId=" + CREATE_PAGE})
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
        ReservedTime reservedTimeInfo;
        try {
            reservedTimeInfo = getScheduleBuildStrategy().createReservedTime(form.getRequestedLearningPlanId(), form);
            form.setComplete(true);
        } catch (PermissionDeniedException e) {
            throw new ServletException("Unexpected authorization failure", e);
        }
        Term term = getTermHelper().getTermByAtpId(form.getTermId());
        getScheduleBuildHelper().buildReservedTimeEvents(reservedTimeInfo, term);
        response.setHeader("content-type", "application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");
        response.getWriter().println(reservedTimeInfo.getEvent());

        return null;
    }

    public static ScheduleBuildStrategy getScheduleBuildStrategy() {
        if (scheduleBuildStrategy == null) {
            scheduleBuildStrategy = UwMyplanServiceLocator.getInstance().getScheduleBuildStrategy();
        }
        return scheduleBuildStrategy;
    }

    public static void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        ReservedTimeController.scheduleBuildStrategy = scheduleBuildStrategy;
    }

    public TermHelper getTermHelper() {
        if (termHelper == null) {
            termHelper = KsapFrameworkServiceLocator.getTermHelper();
        }
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
    }

    public CalendarUtil getCalendarUtil() {
        if (calendarUtil == null) {
            calendarUtil = KsapFrameworkServiceLocator.getCalendarUtil();
        }
        return calendarUtil;
    }

    public void setCalendarUtil(CalendarUtil calendarUtil) {
        this.calendarUtil = calendarUtil;
    }

    public ScheduleBuildHelper getScheduleBuildHelper() {
        if (scheduleBuildHelper == null) {
            scheduleBuildHelper = UwMyplanServiceLocator.getInstance().getScheduleBuildHelper();
        }
        return scheduleBuildHelper;
    }

    public void setScheduleBuildHelper(ScheduleBuildHelper scheduleBuildHelper) {
        this.scheduleBuildHelper = scheduleBuildHelper;
    }
}
