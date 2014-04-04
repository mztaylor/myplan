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
import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.PossibleScheduleOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ScheduleBuildFiltersInfo;
import org.kuali.student.myplan.schedulebuilder.dto.SecondaryActivityOptionsInfo;
import org.kuali.student.myplan.schedulebuilder.infc.*;
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
import java.util.*;

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

        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        List<PossibleScheduleOption> savedSchedulesList = new ArrayList<PossibleScheduleOption>();
        if (StringUtils.hasText(termId) && StringUtils.hasText(requestedLearningPlanId)) {

            Term term = getTermHelper().getTermByAtpId(termId);
            List<PossibleScheduleOption> savedSchedules;
            try {
                savedSchedules = sb.getSchedules(requestedLearningPlanId);
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
            PossibleScheduleOption registeredPossibleSchedule = getScheduleBuildStrategy().getScheduleBuilder(term, registeredCourseOptions, reservedTimes, savedSchedules, new ScheduleBuildFiltersInfo()).getRegistered();
            if (registeredPossibleSchedule != null) {
                ((PossibleScheduleOptionInfo) registeredPossibleSchedule).setLockedIn(true);
            }

            for (PossibleScheduleOption possibleScheduleOption : savedSchedules) {
                if (termId.equals(possibleScheduleOption.getTermId())) {
                    PossibleScheduleErrorsInfo possibleScheduleErrorsInfo = new PossibleScheduleErrorsInfo();
                    Map<String, Map<String, List<String>>> invalidOptions = new LinkedHashMap<String, Map<String, List<String>>>();
                    List<ActivityOption> validatedActivities = validatedSavedActivities(possibleScheduleOption.getActivityOptions(), invalidOptions, reservedTimes == null ? new ArrayList<ReservedTime>() : reservedTimes, new ArrayList<String>(plannedItems.keySet()), registeredPossibleSchedule);
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
            }
            savedSchedulesList.add(registeredPossibleSchedule);
        } else {
            logger.error("Missing required parameters termId and LearningPlanId");
        }
        return savedSchedulesList;
    }

    /**
     * Validates Saved activities against current versions.
     *
     * @param activityOptions
     * @param invalidOptions
     * @return ActivityOption list which are validated items and also populates all invalidActivityOptions course code wise.
     */
    protected List<ActivityOption> validatedSavedActivities(List<ActivityOption> activityOptions, Map<String, Map<String, List<String>>> invalidOptions, List<ReservedTime> reservedTimes, List<String> plannedActivities, PossibleScheduleOption registered) {
        List<ActivityOption> activityOptionList = new ArrayList<ActivityOption>();
        for (ActivityOption activityOption : activityOptions) {
            ActivityOptionInfo savedActivity = new ActivityOptionInfo(activityOption);
            List<ActivityOption> validatedAlternates = new ArrayList<ActivityOption>();
            if (!CollectionUtils.isEmpty(savedActivity.getSecondaryOptions())) {
                boolean isValid = true;
                for (SecondaryActivityOptions secondaryActivityOption : savedActivity.getSecondaryOptions()) {
                    boolean containsPlannedItems = activityOptionsContainsPlannedItems(secondaryActivityOption.getActivityOptions(), plannedActivities);
                    if (!CollectionUtils.isEmpty(secondaryActivityOption.getActivityOptions())) {
                        List<ActivityOption> validatedSecondaryActivities = validatedSavedActivities(secondaryActivityOption.getActivityOptions(), invalidOptions, reservedTimes, plannedActivities, registered);
                        if (CollectionUtils.isEmpty(validatedSecondaryActivities)) {
                            isValid = false;
                            break;
                        } else {
                            ((SecondaryActivityOptionsInfo) secondaryActivityOption).setActivityOptions(validatedSecondaryActivities);
                            /*because there is atleast one valid secondary activity options we can remove the existing invalid secondary activities form the invalid options only for closed and enrollment restriction error reasons*/
                            if (!CollectionUtils.isEmpty(invalidOptions) && invalidOptions.containsKey(activityOption.getCourseCd())) {
                                List<String> keySet = new ArrayList<String>(invalidOptions.get(activityOption.getCourseCd()).keySet());
                                for (String errorKey : keySet) {
                                    if (!containsPlannedItems) {
                                        invalidOptions.get(activityOption.getCourseCd()).put(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_ERROR, invalidOptions.get(activityOption.getCourseCd()).get(errorKey));
                                        invalidOptions.get(activityOption.getCourseCd()).remove(errorKey);
                                    }
                                }
                            }
                        }
                    }
                }
                /*NO secondaries available for this primary are available*/
                if (!isValid) {
                    if (invalidOptions == null) {
                        invalidOptions = new HashMap<String, Map<String, List<String>>>();
                    }
                    Map<String, List<String>> errorList = invalidOptions.get(savedActivity.getCourseCd());
                    if (errorList == null) {
                        errorList = new HashMap<String, List<String>>();
                        errorList.put(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES, new ArrayList<String>());
                    }
                    List<String> activityList = errorList.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES);
                    if (CollectionUtils.isEmpty(activityList)) {
                        activityList = new ArrayList<String>();
                    }
                    activityList.add(savedActivity.getActivityCode());
                    errorList.put(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES, activityList);
                    invalidOptions.put(savedActivity.getCourseCd(), errorList);
                    continue;
                }
            } else if (!CollectionUtils.isEmpty(savedActivity.getAlternateActivties())) {
                validatedAlternates = savedActivity.getAlternateActivties() == null ? new ArrayList<ActivityOption>() : validatedSavedActivities(savedActivity.getAlternateActivties(), invalidOptions, reservedTimes, plannedActivities, registered);
            }

            ActivityOption currentActivity = getScheduleBuildStrategy().getActivityOption(savedActivity.getTermId(), savedActivity.getCourseId(), savedActivity.getCourseCd(), savedActivity.getActivityCode());
            String reasonForChange = areEqual(savedActivity, currentActivity, reservedTimes, registered);
            if (StringUtils.isEmpty(reasonForChange)) {
                savedActivity.setAlternateActivities(validatedAlternates);
                activityOptionList.add(savedActivity);
            } else if (StringUtils.hasText(reasonForChange) && !CollectionUtils.isEmpty(validatedAlternates)) {
                ActivityOptionInfo alAo = (ActivityOptionInfo) validatedAlternates.get(0);
                validatedAlternates.remove(0);
                alAo.setAlternateActivities(validatedAlternates.size() > 0 ? validatedAlternates : new ArrayList<ActivityOption>());
                activityOptionList.add(alAo);
            } else if (StringUtils.hasText(reasonForChange) && (ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED.equals(reasonForChange) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR.equals(reasonForChange) || ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED.equals(reasonForChange))) {
                savedActivity.setAlternateActivities(validatedAlternates);
                activityOptionList.add(savedActivity);
                if (invalidOptions == null) {
                    invalidOptions = new HashMap<String, Map<String, List<String>>>();
                }
                Map<String, List<String>> errorList = invalidOptions.get(savedActivity.getCourseCd());
                if (errorList == null) {
                    errorList = new HashMap<String, List<String>>();
                    errorList.put(reasonForChange, new ArrayList<String>());
                }
                List<String> activityList = errorList.get(reasonForChange);
                if (CollectionUtils.isEmpty(activityList)) {
                    activityList = new ArrayList<String>();
                }
                activityList.add(savedActivity.getActivityCode());
                errorList.put(reasonForChange, activityList);
                invalidOptions.put(savedActivity.getCourseCd(), errorList);
            } else {
                if (invalidOptions == null) {
                    invalidOptions = new HashMap<String, Map<String, List<String>>>();
                }
                Map<String, List<String>> errorList = invalidOptions.get(savedActivity.getCourseCd());
                if (errorList == null) {
                    errorList = new HashMap<String, List<String>>();
                    errorList.put(reasonForChange, new ArrayList<String>());
                }
                List<String> activityList = errorList.get(reasonForChange);
                if (CollectionUtils.isEmpty(activityList)) {
                    activityList = new ArrayList<String>();
                }
                activityList.add(savedActivity.getActivityCode());
                errorList.put(reasonForChange, activityList);
                invalidOptions.put(savedActivity.getCourseCd(), errorList);

            }
        }

        return activityOptionList;

    }

    /**
     * recursive method used to check if a activity cd is present in any of the primary, secondary or alternate activity options
     *
     * @param activityOptions
     * @param activityIds
     * @return
     */
    private boolean activityOptionsContainsPlannedItems(List<ActivityOption> activityOptions, List<String> activityIds) {
        for (ActivityOption activityOption : activityOptions) {
            if (activityIds.contains(activityOption.getActivityOfferingId())) {
                return true;
            }
            if (activityOptionsContainsPlannedItems(activityOption.getAlternateActivties(), activityIds)) {
                return true;
            }

            for (SecondaryActivityOptions so : activityOption.getSecondaryOptions()) {
                if (activityOptionsContainsPlannedItems(so.getActivityOptions(), activityIds)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compares the saved ActivityOption with withdrawnFlag, current ActivityOption, plannedActivities and ReservedTimes
     *
     * @param saved
     * @param current
     * @return (((currentStatusClosed) OR currentIsWithdrawn OR ((currentMeeting and savedMeeting times vary) OR (reservedTime and savedMeeting time conflict))) AND savedActivity is not in PlannedActivities) then false otherwise true.
     */
    private String areEqual(ActivityOption saved, ActivityOption current, List<ReservedTime> reservedTimes, PossibleScheduleOption registered) {
        long[][] savedClassMeetingTime = getScheduleBuildHelper().xlateClassMeetingTimeList2WeekBits(saved.getClassMeetingTimes());
        long[][] currentClassMeetingTime = getScheduleBuildHelper().xlateClassMeetingTimeList2WeekBits(current.getClassMeetingTimes());
        long[][] reservedTime = getScheduleBuildHelper().xlateClassMeetingTimeList2WeekBits(reservedTimes);
        if (!Arrays.deepEquals(savedClassMeetingTime, currentClassMeetingTime)) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED;
        } else if (getScheduleBuildHelper().checkForConflictsWeeks(savedClassMeetingTime, reservedTime)) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED;
        } else if (current.isWithdrawn()) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN;
        } else if (current.isSuspended()) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED;
        } else if (conflictsWithRegistered(registered, saved)) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED;
        } else if (current.isClosed()) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED;
        } else if (current.isEnrollmentRestriction()) {
            return ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR;
        }
        return null;
    }


    /**
     * Checks to see if the activityOption conflicts with any of the registered activity class meeting times
     *
     * @param registered
     * @param ao
     * @return
     */
    private boolean conflictsWithRegistered(PossibleScheduleOption registered, ActivityOption ao) {
        long[][] aoClassMeetingTime = getScheduleBuildHelper().xlateClassMeetingTimeList2WeekBits(ao.getClassMeetingTimes());
        if (registered != null) {
            for (ActivityOption registeredAO : registered.getActivityOptions()) {
                long[][] registeredTime = getScheduleBuildHelper().xlateClassMeetingTimeList2WeekBits(registeredAO.getClassMeetingTimes());
                if (getScheduleBuildHelper().checkForConflictsWeeks(aoClassMeetingTime, registeredTime)) {
                    return true;
                }
            }
        }
        return false;
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
