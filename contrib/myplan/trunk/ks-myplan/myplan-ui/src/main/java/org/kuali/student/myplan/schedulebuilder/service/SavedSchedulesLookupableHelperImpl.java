package org.kuali.student.myplan.schedulebuilder.service;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.util.GlobalVariables;
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
import org.kuali.student.myplan.schedulebuilder.dto.SecondaryActivityOptionsInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildHelper;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
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
                reservedTimes = getScheduleBuildStrategy().getReservedTimes(requestedLearningPlanId);
            } catch (PermissionDeniedException e) {
                e.printStackTrace();
            }
            int count = 1;
            for (PossibleScheduleOption possibleScheduleOption : savedSchedules) {
                if (termId.equals(possibleScheduleOption.getTermId())) {
                    getScheduleBuildHelper().buildPossibleScheduleEvents(possibleScheduleOption, term);
                    HashMap<String, List<String>> invalidOptions = new LinkedHashMap<String, List<String>>();
                    HashMap<String, List<String>> infoOptions = new LinkedHashMap<String, List<String>>();
                    List<ActivityOption> validatedActivities = validatedSavedActivities(possibleScheduleOption.getActivityOptions(), plannedItems, invalidOptions, infoOptions, reservedTimes == null ? new ArrayList<ReservedTime>() : reservedTimes);
                    if (!CollectionUtils.isEmpty(validatedActivities)) {
                        for (String key : invalidOptions.keySet()) {
                            String[] params = {String.valueOf(count), key, org.apache.commons.lang.StringUtils.join(invalidOptions.get(key), ", ")};
                            GlobalVariables.getMessageMap().putInfoForSectionId("saved_schedules_summary", ScheduleBuilderConstants.INVALID_SAVED_SCHEDULE_ACTIVITY, params);
                        }
                        for (String key : infoOptions.keySet()) {
                            String[] params = {String.valueOf(count), key, org.apache.commons.lang.StringUtils.join(infoOptions.get(key), ", ")};
                            GlobalVariables.getMessageMap().putInfoForSectionId("saved_schedules_summary", ScheduleBuilderConstants.SAVED_SCHEDULE_ACTIVITY_MOVED, params);
                        }
                    } else {
                        for (String key : invalidOptions.keySet()) {
                            String[] params = {String.valueOf(count), key};
                            GlobalVariables.getMessageMap().putErrorForSectionId("saved_schedules_summary", ScheduleBuilderConstants.INVALID_SAVED_SCHEDULE, params);
                        }
                    }
                    ((PossibleScheduleOptionInfo) possibleScheduleOption).setActivityOptions(validatedActivities);
                    savedSchedulesList.add(possibleScheduleOption);
                    count++;
                }
            }
        } else {
            logger.error("Missing required parameters termId and LearningPlanId");
        }
        return savedSchedulesList;
    }


    /**
     * Validates Saved activities against current versions.
     *
     * @param activityOptions
     * @param plannedItems
     * @param invalidOptions
     * @return ActivityOption list which are validated items and also populates all invalidActivityOptions course code wise.
     */
    protected List<ActivityOption> validatedSavedActivities(List<ActivityOption> activityOptions, Map<String, String> plannedItems, HashMap<String, List<String>> invalidOptions, HashMap<String, List<String>> infoOptions, List<ReservedTime> reservedTimes) {
        List<ActivityOption> activityOptionList = new ArrayList<ActivityOption>();
        for (ActivityOption savedActivity : activityOptions) {
            List<ActivityOption> validatedAlternates = new ArrayList<ActivityOption>();
            if (!CollectionUtils.isEmpty(savedActivity.getSecondaryOptions())) {
                boolean isValid = true;
                for (SecondaryActivityOptions secondaryActivityOption : savedActivity.getSecondaryOptions()) {

                    List<ActivityOption> validatedSecondaryActivities = validatedSavedActivities(secondaryActivityOption.getActivityOptions(), plannedItems, invalidOptions, infoOptions, reservedTimes);
                    if (!CollectionUtils.isEmpty(secondaryActivityOption.getActivityOptions()) && CollectionUtils.isEmpty(validatedSecondaryActivities)) {
                        isValid = false;
                        break;
                    } else {
                        ((SecondaryActivityOptionsInfo) secondaryActivityOption).setActivityOptions(validatedSecondaryActivities);
                    }
                }
                if (!isValid) {
                    continue;
                }
            } else {
                validatedAlternates = savedActivity.getAlternateActivties() == null ? new ArrayList<ActivityOption>() : validatedSavedActivities(savedActivity.getAlternateActivties(), plannedItems, invalidOptions, infoOptions, reservedTimes);
            }

            ActivityOption currentActivity = getScheduleBuildStrategy().getActivityOption(savedActivity.getTermId(), savedActivity.getCourseId(), savedActivity.getRegistrationCode());
            boolean areEqual = areEqual(savedActivity, currentActivity, plannedItems, reservedTimes);
            if (areEqual) {
                ((ActivityOptionInfo) savedActivity).setAlternateActivities(validatedAlternates);
                activityOptionList.add(savedActivity);
            } else if (!areEqual && !CollectionUtils.isEmpty(validatedAlternates)) {
                ActivityOptionInfo alAo = (ActivityOptionInfo) validatedAlternates.get(0);
                validatedAlternates.remove(0);
                alAo.setAlternateActivities(validatedAlternates.size() > 0 ? validatedAlternates : new ArrayList<ActivityOption>());
                activityOptionList.add(alAo);
                List<String> activityList = infoOptions.get(alAo.getCourseCd());
                if (CollectionUtils.isEmpty(activityList)) {
                    activityList = new ArrayList<String>();
                }
                activityList.add(alAo.getRegistrationCode());
                infoOptions.put(alAo.getCourseCd(), activityList);
            } else {
                List<String> activityList = invalidOptions.get(savedActivity.getCourseCd());
                if (CollectionUtils.isEmpty(activityList)) {
                    activityList = new ArrayList<String>();
                }
                activityList.add(savedActivity.getRegistrationCode());
                invalidOptions.put(savedActivity.getCourseCd(), activityList);

            }
        }

        return activityOptionList;

    }


    /**
     * Compares the saved ActivityOption with withdrawnFlag, current ActivityOption, plannedActivities and ReservedTimes
     *
     * @param saved
     * @param current
     * @param plannedItems
     * @return ((currentIsWithdrawn OR ((currentMeeting and savedMeeting times vary) OR (reservedTime and savedMeeting time conflict))) AND savedActivity is not in PlannedActivities) then false otherwise true.
     */
    private boolean areEqual(ActivityOption saved, ActivityOption current, Map<String, String> plannedItems, List<ReservedTime> reservedTimes) {
        long[][] savedClassMeetingTime = getScheduleBuildHelper().xlateClassMeetingTimeList2WeekBits(saved.getClassMeetingTimes());
        long[][] currentClassMeetingTime = getScheduleBuildHelper().xlateClassMeetingTimeList2WeekBits(current.getClassMeetingTimes());
        long[][] reservedTime = getScheduleBuildHelper().xlateClassMeetingTimeList2WeekBits(reservedTimes);
        if ((current.isWithdrawn() || (!Arrays.deepEquals(savedClassMeetingTime, currentClassMeetingTime) || getScheduleBuildHelper().checkForConflictsWeeks(savedClassMeetingTime, reservedTime))) && !plannedItems.containsKey(saved.getActivityOfferingId())) {
            return false;
        }
        return true;
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
