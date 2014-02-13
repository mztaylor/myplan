package org.kuali.student.myplan.schedulebuilder.service;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 2/11/14.
 */
public class PossibleSchedulesLookupableHelperImpl extends MyPlanLookupableImpl {

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private ScheduleBuildForm scheduleBuildForm;

    private TermHelper termHelper;

    private int defaultScheduleCount;

    private final Logger logger = Logger.getLogger(PossibleSchedulesLookupableHelperImpl.class);

    @Override
    protected List<PossibleScheduleOption> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        List<PossibleScheduleOption> possibleScheduleOptions = new ArrayList<PossibleScheduleOption>();
        String termId = request.getParameter(PlanConstants.TERM_ID_KEY);
        String requestedLearningPlanId = request.getParameter(PlanConstants.LEARNING_PLAN_KEY);
        if (StringUtils.hasText(termId) && StringUtils.hasText(requestedLearningPlanId)) {
            Term term = getTermHelper().getTermByAtpId(termId);
            List<CourseOption> courseOptions = getScheduleBuildStrategy().getCourseOptions(requestedLearningPlanId, termId);
            List<ReservedTime> reservedTimes = null;
            try {
                reservedTimes = getScheduleBuildStrategy().getReservedTimes(requestedLearningPlanId);
            } catch (Exception e) {
                logger.error("Could not load reservedTimes for learningPlan: " + requestedLearningPlanId, e);
            }

            List<PossibleScheduleOption> savedSchedulesList = new ArrayList<PossibleScheduleOption>();

            List<PossibleScheduleOption> savedSchedules;
            try {
                savedSchedules = getScheduleBuildStrategy().getSchedules(requestedLearningPlanId);
            } catch (PermissionDeniedException e) {
                throw new IllegalStateException("Failed to refresh saved schedules", e);
            }
            for (PossibleScheduleOption possibleScheduleOption : savedSchedules) {
                if (termId.equals(possibleScheduleOption.getTermId())) {
                    savedSchedulesList.add(possibleScheduleOption);
                }
            }

            ScheduleBuilder scheduleBuilder = new ScheduleBuilder(term, courseOptions, reservedTimes, savedSchedulesList);
            possibleScheduleOptions = scheduleBuilder.getNext(getDefaultScheduleCount(), Collections.<PossibleScheduleOption>emptySet());
        } else {
            logger.error("Missing required parameters termId and LearningPlanId");
        }
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

    public int getDefaultScheduleCount() {
        if (defaultScheduleCount == 0) {
            defaultScheduleCount = 25;
        }
        return defaultScheduleCount;
    }

    public void setDefaultScheduleCount(int defaultScheduleCount) {
        this.defaultScheduleCount = defaultScheduleCount;
    }
}
