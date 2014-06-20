package org.kuali.student.myplan.schedulebuilder.controller;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.exception.AuthorizationException;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.SecondaryActivityOptionsInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilderConstants;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleForm;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.json.stream.JsonGenerator;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping(value = "/sb")
public class ScheduleBuildController extends UifControllerBase {

    private static final Logger LOG = Logger
            .getLogger(ScheduleBuildController.class);

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private static CourseHelper courseHelper;

    private UserSessionHelper userSessionHelper;

    private PlanHelper planHelper;


    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return (UifFormBase) getScheduleBuildStrategy().getInitialForm();
    }

    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form,
                              BindingResult result, HttpServletRequest request,
                              HttpServletResponse response) {
        if (getUserSessionHelper().isAdviser()) {
            LOG.info("UNAUTHORIZED Access: " + GlobalVariables.getUserSession().getPerson().getPrincipalId());
            ModelAndView modelAndView = new ModelAndView(UifConstants.REDIRECT_PREFIX + "/myplan/unauthorized");
            return modelAndView;
        }
        super.start(form, result, request, response);

        ScheduleForm sbform = (ScheduleForm) form;
        try {
            sbform.reset();
        } catch (AuthorizationException e) {
            LOG.info("UNAUTHORIZED Access: " + GlobalVariables.getUserSession().getPerson().getPrincipalId());
            try {
                response.sendRedirect("/student/myplan/unauthorized");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        } catch (RuntimeException e) {
            GlobalVariables.getMessageMap().putWarningForSectionId(ScheduleBuilderConstants.SCHEDULE_BUILDER_PAGE_ID,
                    ScheduleBuilderConstants.WARNING_STUDENT_SERVICES_DOWN);
        }
        return getUIFModelAndView(form);
    }


    @RequestMapping(params = "methodToCall=build")
    public ModelAndView build(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ScheduleBuildForm sbform = (ScheduleBuildForm) form;
        try {
            sbform.buildSchedules();
        } catch (AuthorizationException e) {
            LOG.info("UNAUTHORIZED Access: " + GlobalVariables.getUserSession().getPerson().getPrincipalId());
            try {
                response.sendRedirect("/student/myplan/unauthorized");
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return null;
        }
        request.getSession().setAttribute("possibleSchedules", sbform.getPossibleScheduleOptions());
        return getUIFModelAndView(form);
    }

    @RequestMapping(params = "methodToCall=save")
    public ModelAndView saveSchedule(@ModelAttribute("KualiForm") ScheduleBuildForm form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {

        List<PossibleScheduleOption> savedSchedules;
        try {
            savedSchedules = getScheduleBuildStrategy().getSchedulesForTerm(form.getRequestedLearningPlanId(), form.getTermId());
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException(
                    "Failed to refresh saved schedules", e);
        }

        StringWriter stringWriter = new StringWriter();
        JsonGenerator jPso = Json.createGenerator(stringWriter);
        PossibleScheduleOption sso = null;
        boolean limitReached = false;
        if (!CollectionUtils.isEmpty(savedSchedules) && savedSchedules.size() == 3) {
            limitReached = true;
        } else {
            sso = form.saveSchedule();
        }


        jPso.writeStartObject();

        if (sso != null) {
            jPso.write("success", true);
            jPso.writeStartArray("savedSchedules");
            int index = 1;
            for (PossibleScheduleOption savedSchedule : savedSchedules) {
                jPso.writeStartObject();
                jPso.write("id", savedSchedule.getId());
                jPso.write("uniqueId", savedSchedule.getUniqueId());
                jPso.write("index", "P" + index);
                jPso.writeEnd();
                index++;
            }
            jPso.writeStartObject();
            jPso.write("id", sso.getId());
            jPso.write("uniqueId", sso.getUniqueId());
            jPso.write("recentlyAdded", true);
            jPso.write("index", "P" + index);
            jPso.writeEnd();

            jPso.writeEnd().flush();
        } else {
            jPso.write("success", false);
            if (limitReached) {
                jPso.write("limitReached", true);
            }
        }
        jPso.writeEnd().flush();

        response.setHeader("content-type", "application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");
        response.getWriter().println(stringWriter.toString());

        return null;
    }

    @RequestMapping(params = "methodToCall=removeSchedule")
    public ModelAndView removeSchedule(@ModelAttribute("KualiForm") ScheduleBuildForm form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String uniqueId = null;

        /*Getting pinned schedules v=before removal to find the unique id*/
        List<PossibleScheduleOption> savedScheduleList;
        try {
            savedScheduleList = getScheduleBuildStrategy().getSchedulesForTerm(form.getRequestedLearningPlanId(), form.getTermId());
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException(
                    "Failed to refresh saved schedules", e);
        }
        for (PossibleScheduleOption saved : savedScheduleList) {
            if (saved.getId().equals(form.getUniqueId())) {
                uniqueId = saved.getUniqueId();
                break;
            }
        }

        String removedId = form.removeSchedule();

        StringWriter stringWriter = new StringWriter();
        JsonGenerator jPso = Json.createGenerator(stringWriter);

        /*Getting the pinned schedules after removal*/
        List<PossibleScheduleOption> savedSchedules = null;
        try {
            savedSchedules = getScheduleBuildStrategy().getSchedulesForTerm(form.getRequestedLearningPlanId(), form.getTermId());
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException(
                    "Failed to refresh saved schedules", e);
        }

        jPso.writeStartObject();
        jPso.write("success", true);
        jPso.write("uniqueIdRemoved", uniqueId);
        jPso.write("scheduleIdRemoved", removedId);

        if (!CollectionUtils.isEmpty(savedSchedules)) {
            jPso.writeStartArray("savedSchedules");
            int index = 1;
            for (PossibleScheduleOption savedSchedule : savedSchedules) {
                jPso.writeStartObject();
                jPso.write("id", savedSchedule.getId());
                jPso.write("uniqueId", savedSchedule.getUniqueId());
                jPso.write("index", "P" + index);
                jPso.writeEnd();
                index++;
            }
            jPso.writeEnd();
        }

        jPso.writeEnd().flush();

        response.setContentType("application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");
        response.getWriter().println(stringWriter.toString());

        return null;
    }

    @RequestMapping(params = "methodToCall=removeReservedTime")
    public ModelAndView removeReservedTime(@ModelAttribute("KualiForm") ScheduleBuildForm form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String removedId = form.removeSchedule();

        String uniqueId = "";

        for (ReservedTime reservedTime : form.getReservedTimes()) {
            if (reservedTime.getId().equals(form.getUniqueId())) {
                uniqueId = reservedTime.getUniqueId();
                break;
            }
        }

        JsonObjectBuilder json = Json.createObjectBuilder();

        json.add("success", true);
        json.add("uniqueIdRemoved", uniqueId);
        json.add("scheduleIdRemoved", removedId != null ? removedId : "");

        response.setContentType("application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");
        JsonWriter jwriter = Json.createWriter(response.getWriter());
        jwriter.writeObject(json.build());
        jwriter.close();

        return null;
    }

    /**
     * Recursive method which prepares a list of activity options which are selected for registration
     *
     * @param activityOptions
     * @param slnCount
     * @return
     */
    private List<ActivityOption> getSelectedActivitiesForReg(List<ActivityOption> activityOptions, int slnCount) {
        List<ActivityOption> newAOList = new ArrayList<ActivityOption>();
        for (ActivityOption activityOption : activityOptions) {
            if (!activityOption.getSelectedForReg().equals("true")) {
                continue;
            }
            ActivityOptionInfo ao = (ActivityOptionInfo) activityOption;
            List<ActivityOption> alternateActivities = getSelectedActivitiesForReg(ao.getAlternateActivties(), slnCount);
            ao.setAlternateActivities(alternateActivities);
            for (SecondaryActivityOptions secondaryActivityOptions : ao.getSecondaryOptions()) {
                List<ActivityOption> activityOptionList = getSelectedActivitiesForReg(secondaryActivityOptions.getActivityOptions(), slnCount);
                ((SecondaryActivityOptionsInfo) secondaryActivityOptions).setActivityOptions(activityOptionList);
            }
            slnCount++;
            newAOList.add(ao);
        }
        return newAOList;

    }

    public ScheduleBuildStrategy getScheduleBuildStrategy() {
        if (scheduleBuildStrategy == null) {
            scheduleBuildStrategy = UwMyplanServiceLocator.getInstance().getScheduleBuildStrategy();
        }
        return scheduleBuildStrategy;
    }

    public void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        this.scheduleBuildStrategy = scheduleBuildStrategy;
    }

    public static CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = KsapFrameworkServiceLocator.getCourseHelper();
        }
        return courseHelper;
    }

    public static void setCourseHelper(CourseHelper courseHelper) {
        ScheduleBuildController.courseHelper = courseHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = UwMyplanServiceLocator.getInstance().getPlanHelper();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }
}
