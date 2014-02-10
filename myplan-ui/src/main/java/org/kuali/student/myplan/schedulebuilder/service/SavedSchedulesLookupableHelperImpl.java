package org.kuali.student.myplan.schedulebuilder.service;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 2/7/14.
 */
public class SavedSchedulesLookupableHelperImpl extends MyPlanLookupableImpl {

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private UserSessionHelper userSessionHelper;

    private final Logger logger = Logger.getLogger(SavedSchedulesLookupableHelperImpl.class);

    @Override
    protected List<PossibleScheduleOption> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String termId = request.getParameter(PlanConstants.TERM_ID_KEY);
        String requestedLearningPlanId = request.getParameter(PlanConstants.LEARNING_PLAN_KEY);

        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        List<PossibleScheduleOption> savedSchedulesList = new ArrayList<PossibleScheduleOption>();
        if (StringUtils.hasText(termId) && StringUtils.hasText(requestedLearningPlanId)) {
            List<PossibleScheduleOption> savedSchedules;
            try {
                savedSchedules = sb.getSchedules(requestedLearningPlanId);
            } catch (PermissionDeniedException e) {
                throw new IllegalStateException(
                        "Failed to refresh saved schedules", e);
            }
            for (PossibleScheduleOption possibleScheduleOption : savedSchedules) {
                if (termId.equals(possibleScheduleOption.getTermId())) {
                    savedSchedulesList.add(possibleScheduleOption);
                }
            }
        } else {
            logger.error("Missing required parameters termId and LearningPlanId");
        }
        return savedSchedulesList;
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

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }
}
