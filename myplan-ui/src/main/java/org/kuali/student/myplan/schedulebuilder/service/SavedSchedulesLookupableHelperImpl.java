package org.kuali.student.myplan.schedulebuilder.service;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.schedulebuilder.dto.PossibleScheduleOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ScheduleBuildFiltersInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.util.PossibleScheduleErrorsInfo;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildHelper;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilderConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 2/7/14.
 */
public class SavedSchedulesLookupableHelperImpl extends MyPlanLookupableImpl {

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private UserSessionHelper userSessionHelper;

    private ScheduleBuildHelper scheduleBuildHelper;

    private TermHelper termHelper;

    private PlanHelper planHelper;

    private final Logger logger = Logger.getLogger(SavedSchedulesLookupableHelperImpl.class);

    @Override
    protected List<PossibleScheduleOption> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String termId = request.getParameter(PlanConstants.TERM_ID_KEY);
        String requestedLearningPlanId = request.getParameter(ScheduleBuilderConstants.LEARNING_PLAN_KEY);
        requestedLearningPlanId = getScheduleBuildHelper().validateOrPopulateLearningPlanId(requestedLearningPlanId);
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        List<PossibleScheduleOption> savedSchedulesList = new ArrayList<PossibleScheduleOption>();
        if (StringUtils.hasText(termId) && StringUtils.hasText(requestedLearningPlanId)) {

            Term term = getTermHelper().getTermByAtpId(termId);
            List<PossibleScheduleOption> savedSchedules;
            try {
                savedSchedules = sb.getSchedulesForTerm(requestedLearningPlanId, termId);
            } catch (PermissionDeniedException e) {
                throw new IllegalStateException(
                        "Failed to refresh saved schedules", e);
            }

            Map<String, String> plannedItems = getPlanHelper().getPlanItemIdAndRefObjIdByRefObjType(requestedLearningPlanId, PlanConstants.SECTION_TYPE, termId);
            List<ReservedTime> reservedTimes = null;
            try {
                reservedTimes = getScheduleBuildStrategy().getReservedTimesForTermId(requestedLearningPlanId, termId);
            } catch (PermissionDeniedException e) {
                e.printStackTrace();
            }

            /*Getting registered possible schedule*/
            List<CourseOption> registeredCourseOptions = getScheduleBuildStrategy().getRegisteredCourseOptions(getUserSessionHelper().getStudentId(), termId, new ScheduleBuildFiltersInfo());
            PossibleScheduleOption registeredPossibleSchedule = null;
            if (term != null) {
                registeredPossibleSchedule = getScheduleBuildStrategy().getScheduleBuilder(term, registeredCourseOptions, reservedTimes, savedSchedules, new ScheduleBuildFiltersInfo()).getRegistered();
            }
            if (registeredPossibleSchedule != null) {
                ((PossibleScheduleOptionInfo) registeredPossibleSchedule).setLockedIn(true);
            }
            LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData = getScheduleBuildHelper().getEnrollmentDataForPossibleSchedules(savedSchedules);
            for (PossibleScheduleOption possibleScheduleOption : savedSchedules) {
                getScheduleBuildHelper().updateEnrollmentInfo(possibleScheduleOption.getActivityOptions(), enrollmentData);
                PossibleScheduleErrorsInfo possibleScheduleErrorsInfo = new PossibleScheduleErrorsInfo();
                Map<String, Map<String, List<String>>> invalidOptions = new LinkedHashMap<String, Map<String, List<String>>>();
                Map<String, Map<String, ActivityOption>> currentActivityOptions = getScheduleBuildStrategy().getActivityOptionsForCoursesByActivities(possibleScheduleOption.getActivityOptions(), enrollmentData, plannedItems);
                List<ActivityOption> validatedActivities = getScheduleBuildHelper().validatedSavedActivities(possibleScheduleOption.getActivityOptions(), invalidOptions, reservedTimes == null ? new ArrayList<ReservedTime>() : reservedTimes, new ArrayList<String>(plannedItems.keySet()), registeredPossibleSchedule, enrollmentData, currentActivityOptions);
                if (!CollectionUtils.isEmpty(validatedActivities) && validatedActivities.size() == possibleScheduleOption.getActivityOptions().size()) {
                    if (!CollectionUtils.isEmpty(invalidOptions)) {
                        boolean otherErrors = false;
                        boolean noError = false;
                        for (String key : invalidOptions.keySet()) {
                            List<String> errorReasons = new ArrayList<String>(invalidOptions.get(key).keySet());
                            for (String errorReason : errorReasons) {
                                if (ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_ERROR.equals(errorReason) && !noError) {
                                    noError = true;
                                } else if (!ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_ERROR.equals(errorReason) && !otherErrors) {
                                    otherErrors = true;
                                }
                            }
                        }
                        if (noError && !otherErrors) {
                            possibleScheduleErrorsInfo.setErrorType(ScheduleBuilderConstants.PINNED_SCHEDULES_NO_ERROR);
                        } else if (otherErrors) {
                            possibleScheduleErrorsInfo.setErrorType(ScheduleBuilderConstants.PINNED_SCHEDULES_PASSIVE_ERROR);
                        }
                        possibleScheduleErrorsInfo.setInvalidOptions(invalidOptions);
                    }
                } else {
                    possibleScheduleErrorsInfo.setErrorType(ScheduleBuilderConstants.PINNED_SCHEDULES_MODAL_ERROR);
                    possibleScheduleErrorsInfo.setInvalidOptions(invalidOptions);
                }
                ((PossibleScheduleOptionInfo) possibleScheduleOption).setPossibleErrors(possibleScheduleErrorsInfo);
                getScheduleBuildHelper().buildPossibleScheduleEvents(possibleScheduleOption, term);
                savedSchedulesList.add(possibleScheduleOption);
            }
            savedSchedulesList.add(registeredPossibleSchedule);
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

    public TermHelper getTermHelper() {
        if (termHelper == null) {
            termHelper = KsapFrameworkServiceLocator.getTermHelper();
        }
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
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
