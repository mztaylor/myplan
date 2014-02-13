package org.kuali.student.myplan.schedulebuilder.service;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.dto.PossibleScheduleOptionInfo;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.myplan.schedulebuilder.util.ShoppingCartForm;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by hemanthg on 2/11/14.
 */
public class PossibleSchedulesLookupableHelperImpl extends MyPlanLookupableImpl {

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private ScheduleBuildForm scheduleBuildForm;

    private TermHelper termHelper;

    private static int DEFAULT_SCHEDULE_COUNT = 25;

    private final Logger logger = Logger.getLogger(PossibleSchedulesLookupableHelperImpl.class);

    @Override
    protected List<PossibleScheduleOption> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();

        String termId = request.getParameter(PlanConstants.TERM_ID_KEY);
        Term term = getTermHelper().getTermByAtpId(termId);
        String requestedLearningPlanId = request.getParameter(PlanConstants.LEARNING_PLAN_KEY);
        List<CourseOption> courseOptions = getScheduleBuildStrategy().getCourseOptions(requestedLearningPlanId, termId);
        List<ReservedTime> reservedTimes = null;
        try {
            reservedTimes = getScheduleBuildStrategy().getReservedTimes(requestedLearningPlanId);
        } catch (Exception e) {
            logger.error("Could not load reservedTimes for learningPlan: " + requestedLearningPlanId, e);
        }
        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(term, courseOptions, reservedTimes);
        List<PossibleScheduleOption> possibleScheduleOptions = scheduleBuilder.getNext(DEFAULT_SCHEDULE_COUNT, Collections.<PossibleScheduleOption>emptySet());
        return possibleScheduleOptions;
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

    public ScheduleBuildForm getScheduleBuildForm() {
        if (scheduleBuildForm == null) {
            scheduleBuildForm = UwMyplanServiceLocator.getInstance().getScheduleBuildForm();
        }
        return scheduleBuildForm;
    }

    public void setScheduleBuildForm(ScheduleBuildForm scheduleBuildForm) {
        this.scheduleBuildForm = scheduleBuildForm;
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

}
