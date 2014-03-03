package org.kuali.student.myplan.schedulebuilder.controller;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.controller.UifControllerBase;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleForm;
import org.kuali.student.myplan.schedulebuilder.util.ShoppingCartForm;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
@RequestMapping(value = "/sb")
public class ScheduleBuildController extends UifControllerBase {

    private static final Logger LOG = Logger
            .getLogger(ScheduleBuildController.class);

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private static CourseHelper courseHelper;


    @Override
    protected UifFormBase createInitialForm(HttpServletRequest request) {
        return (UifFormBase) getScheduleBuildStrategy().getInitialForm();
    }

    @RequestMapping(params = "methodToCall=start")
    public ModelAndView start(@ModelAttribute("KualiForm") UifFormBase form,
                              BindingResult result, HttpServletRequest request,
                              HttpServletResponse response) {
        super.start(form, result, request, response);

        ScheduleForm sbform = (ScheduleForm) form;
        sbform.reset();
        return getUIFModelAndView(form);
    }


    @RequestMapping(params = "methodToCall=build")
    public ModelAndView build(@ModelAttribute("KualiForm") UifFormBase form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {
        ScheduleBuildForm sbform = (ScheduleBuildForm) form;
        sbform.buildSchedules();
        return getUIFModelAndView(form);
    }

    @RequestMapping(params = "methodToCall=save")
    public ModelAndView saveSchedule(@ModelAttribute("KualiForm") ScheduleBuildForm form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {

        PossibleScheduleOption sso = form.saveSchedule();

        JsonObjectBuilder json = Json.createObjectBuilder();

        if (sso != null) {
            json.add("success", true);
            json.add("id", sso.getId());
            json.add("uniqueId", sso.getUniqueId());
        } else {
            json.add("success", false);
        }
        response.setContentType("application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");
        JsonWriter jwriter = Json.createWriter(response.getWriter());
        jwriter.writeObject(json.build());
        jwriter.close();

        return null;
    }

    @RequestMapping(params = "methodToCall=remove")
    public ModelAndView removeSchedule(@ModelAttribute("KualiForm") ScheduleBuildForm form, BindingResult result, HttpServletRequest request, HttpServletResponse response) throws IOException {

        String removedId = form.removeSchedule();

        JsonObjectBuilder json = Json.createObjectBuilder();

        json.add("success", true);
        json.add("scheduleIdRemoved", removedId);

        response.setContentType("application/json");
        response.setHeader("Cache-Control", "No-cache");
        response.setHeader("Cache-Control", "No-store");
        response.setHeader("Cache-Control", "max-age=0");
        JsonWriter jwriter = Json.createWriter(response.getWriter());
        jwriter.writeObject(json.build());
        jwriter.close();

        return null;
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
}
