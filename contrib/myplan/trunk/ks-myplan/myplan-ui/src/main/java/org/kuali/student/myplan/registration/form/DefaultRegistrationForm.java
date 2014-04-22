package org.kuali.student.myplan.registration.form;

import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.registration.util.RegistrationForm;
import org.kuali.student.myplan.schedulebuilder.dto.*;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.PossibleScheduleErrorsInfo;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildHelper;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilderConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 4/22/2014.
 */
public class DefaultRegistrationForm extends UifFormBase implements RegistrationForm {

    private Term term;
    private String termId;
    private String uniqueId;
    private String requestedLearningPlanId;
    private int selectedRegistrationCodeCount;
    private RegistrationDetailsInfo registrationDetails;


    private transient TermHelper termHelper;
    private transient PlanHelper planHelper;
    private transient UserSessionHelper userSessionHelper;
    private transient ScheduleBuildHelper scheduleBuildHelper;
    private transient ScheduleBuildStrategy scheduleBuildStrategy;

    @Override
    public void buildRegistrationDetails() {
        if (getTermId() == null) {
            return;
        } else {
            setTerm(getTermHelper().getTermByAtpId(getTermId()));
        }

        if (getTerm() == null) {
            return;
        }

        setSelectedRegistrationCodeCount(0);
        RegistrationDetailsInfo registrationDetails = null;
        if (getPageId().equals(ScheduleBuilderConstants.REGISTRAION_PAGE_1)) {
            registrationDetails = new RegistrationDetailsInfo();

            /*ReservedTimes*/
            List<ReservedTime> reservedTimes;
            try {
                reservedTimes = getScheduleBuildStrategy().getReservedTimesForTermId(getRequestedLearningPlanId(), getTermId());
            } catch (PermissionDeniedException e) {
                throw new IllegalStateException("Failed to refresh reserved times", e);
            }


            /*SavedSchedules*/
            List<PossibleScheduleOption> pinnedSchedules;
            try {
                pinnedSchedules = getScheduleBuildStrategy().getSchedulesForTerm(getRequestedLearningPlanId(), getTermId());
            } catch (PermissionDeniedException e) {
                throw new IllegalStateException(
                        "Failed to refresh saved schedules", e);
            }

            /*populating the registered courses*/
            List<CourseOption> registeredCourseOptions = getScheduleBuildStrategy().getRegisteredCourseOptions(getUserSessionHelper().getStudentId(), getTermId(), new ScheduleBuildFiltersInfo());
            registrationDetails.setRegisteredCourses(registeredCourseOptions);


            /*Planned Activity map*/
            Map<String, String> plannedItems = getPlanHelper().getPlanItemIdAndRefObjIdByRefObjType(getRequestedLearningPlanId(), PlanConstants.SECTION_TYPE, getTermId());


            /*RegisteredPossibleSchedule*/
            PossibleScheduleOption registeredPossibleSchedule = null;
            if (getTerm() != null) {
                registeredPossibleSchedule = getScheduleBuildStrategy().getScheduleBuilder(getTerm(), registeredCourseOptions, reservedTimes, pinnedSchedules, new ScheduleBuildFiltersInfo()).getRegistered();
            }
            if (registeredPossibleSchedule != null) {
                ((PossibleScheduleOptionInfo) registeredPossibleSchedule).setLockedIn(true);
            }


            /*Selecting Requested Pinned schedule*/
            PossibleScheduleOption selectedPinnedSchedule = null;
            for (PossibleScheduleOption possibleScheduleOption : pinnedSchedules) {
                if (possibleScheduleOption.getId().equals(getUniqueId())) {
                    selectedPinnedSchedule = possibleScheduleOption;
                    break;
                }
            }

            /*Validating Pinned schedule*/
            if (selectedPinnedSchedule != null) {
                List<CourseOption> plannedCourseOptions = new ArrayList<CourseOption>();

                for (ActivityOption activityOption : selectedPinnedSchedule.getActivityOptions()) {
                    CourseOptionInfo courseOption = new CourseOptionInfo();
                    courseOption.setCourseId(activityOption.getCourseId());
                    courseOption.setCourseCode(activityOption.getCourseCd());
                    courseOption.setCourseTitle(activityOption.getCourseTitle());
                    courseOption.setCredits(new BigDecimal(activityOption.getCourseCredit()));
                    for (SecondaryActivityOptions secondaryActivityOptions : activityOption.getSecondaryOptions()) {
                        List<ActivityOption> secondaryActivities = new ArrayList<ActivityOption>();
                        for (ActivityOption secondaryActivity : secondaryActivityOptions.getActivityOptions()) {
                            ((ActivityOptionInfo) secondaryActivity).setSelectedForReg(secondaryActivity.getRegistrationCode());
                            secondaryActivities.add(secondaryActivity);
                            break;
                        }
                        ((SecondaryActivityOptionsInfo) secondaryActivityOptions).setActivityOptions(secondaryActivities);
                    }
                    List<ActivityOption> activityOptions = courseOption.getActivityOptions();
                    ((ActivityOptionInfo) activityOption).setSelectedForReg(activityOption.getRegistrationCode());
                    activityOptions.add(activityOption);
                    Map<String, Map<String, List<String>>> invalidOptions = new LinkedHashMap<String, Map<String, List<String>>>();
                    PossibleScheduleErrorsInfo possibleScheduleErrorsInfo = new PossibleScheduleErrorsInfo();
                    List<ActivityOption> validatedActivities = getScheduleBuildHelper().validatedSavedActivities(activityOptions, invalidOptions, reservedTimes, new ArrayList<String>(plannedItems.keySet()), registeredPossibleSchedule);
                    if (!CollectionUtils.isEmpty(validatedActivities) && validatedActivities.size() == activityOptions.size()) {
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
                    List<ActivityOption> validatedActivityOptionList = new ArrayList<ActivityOption>();
                    /*Validate activityOptions for ErrorMessages*/
                    if (StringUtils.hasText(possibleScheduleErrorsInfo.getErrorType())) {
                        List<String> invalidActivities = new ArrayList<String>();
                        getScheduleBuildHelper().validateForErrors(possibleScheduleErrorsInfo, courseOption.getCourseCode(), activityOptions, invalidActivities);
                        possibleScheduleErrorsInfo.setErrorType(possibleScheduleErrorsInfo.getErrorType());
                        possibleScheduleErrorsInfo.setErrorMessage(possibleScheduleErrorsInfo.getErrorMessage());
                        /*TODO:remove invalid activities from activityOptions list*/
                        validatedActivityOptionList.addAll(getValidActivitiesForReg(activityOptions, invalidActivities));
                    } else {
                        validatedActivityOptionList.addAll(activityOptions);
                    }
                    courseOption.setPossibleErrors(possibleScheduleErrorsInfo);
                    courseOption.setActivityOptions(validatedActivityOptionList);
                    plannedCourseOptions.add(courseOption);
                }


                registrationDetails.setPlannedCourses(plannedCourseOptions);

            }

        } else if (getPageId().equals(ScheduleBuilderConstants.REGISTRAION_PAGE_2)) {
            if (getRegistrationDetails() != null) {
                registrationDetails = (RegistrationDetailsInfo) getRegistrationDetails();
                List<CourseOption> courseOptionList = new ArrayList<CourseOption>();
                List<String> selectedRegistrationCodes = new ArrayList<String>();
                for (CourseOption courseOption : registrationDetails.getPlannedCourses()) {
                    for (ActivityOption activityOption : courseOption.getActivityOptions()) {
                        if (StringUtils.hasText(activityOption.getSelectedForReg())) {
                            selectedRegistrationCodes.add(activityOption.getSelectedForReg());
                        }
                        for (SecondaryActivityOptions secondaryActivityOption : activityOption.getSecondaryOptions()) {
                            for (ActivityOption secondaryActivity : secondaryActivityOption.getActivityOptions()) {
                                if (StringUtils.hasText(activityOption.getSelectedForReg())) {
                                    selectedRegistrationCodes.add(activityOption.getSelectedForReg());
                                }
                            }
                        }
                    }


                    List<ActivityOption> selectedActivities = getSelectedActivitiesForReg(courseOption.getActivityOptions(), selectedRegistrationCodes);
                    ((CourseOptionInfo) courseOption).setActivityOptions(selectedActivities);
                    courseOptionList.add(courseOption);
                }
                registrationDetails.setPlannedCourses(courseOptionList);
                setPageId(ScheduleBuilderConstants.REGISTRAION_PAGE_3);
            }
        }


        if (getSelectedRegistrationCodeCount() > 0 && getSelectedRegistrationCodeCount() <= 8) {
            registrationDetails.setRegistrationUrl("placeholderUrl");
        }
        setRegistrationDetails(registrationDetails);
    }


    /**
     * Recursive method which prepares a list of activity options that are not in the invalidActivityCodes list.
     *
     * @param activityOptions
     * @param invalidActivityCodes
     * @return
     */
    private List<ActivityOption> getValidActivitiesForReg(List<ActivityOption> activityOptions, List<String> invalidActivityCodes) {
        List<ActivityOption> newAOList = new ArrayList<ActivityOption>();
        for (ActivityOption activityOption : activityOptions) {
            if (invalidActivityCodes.contains(activityOption.getActivityCode())) {
                continue;
            }
            ActivityOptionInfo ao = (ActivityOptionInfo) activityOption;
            List<ActivityOption> alternateActivities = getValidActivitiesForReg(ao.getAlternateActivties(), invalidActivityCodes);
            ao.setAlternateActivities(alternateActivities);
            for (SecondaryActivityOptions secondaryActivityOptions : ao.getSecondaryOptions()) {
                List<ActivityOption> activityOptionList = getValidActivitiesForReg(secondaryActivityOptions.getActivityOptions(), invalidActivityCodes);
                ((SecondaryActivityOptionsInfo) secondaryActivityOptions).setActivityOptions(activityOptionList);
            }
            newAOList.add(ao);
        }
        return newAOList;

    }


    /**
     * Recursive method which prepares a list of activity options which are selected for registration
     *
     * @param activityOptions
     * @return
     */
    private List<ActivityOption> getSelectedActivitiesForReg(List<ActivityOption> activityOptions, List<String> selectedRegistrationCodes) {
        List<ActivityOption> newAOList = new ArrayList<ActivityOption>();
        for (ActivityOption activityOption : activityOptions) {
            ActivityOptionInfo ao = (ActivityOptionInfo) activityOption;
            for (SecondaryActivityOptions secondaryActivityOptions : ao.getSecondaryOptions()) {
                List<ActivityOption> activityOptionList = getSelectedActivitiesForReg(secondaryActivityOptions.getActivityOptions(), selectedRegistrationCodes);
                ((SecondaryActivityOptionsInfo) secondaryActivityOptions).setActivityOptions(activityOptionList);
            }
            List<ActivityOption> alternateActivities = getSelectedActivitiesForReg(ao.getAlternateActivties(), selectedRegistrationCodes);
            ao.setAlternateActivities(alternateActivities);
            if (!selectedRegistrationCodes.contains(activityOption.getRegistrationCode())) {
                continue;
            }
            selectedRegistrationCodeCount++;
            newAOList.add(ao);
        }
        return newAOList;

    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getRequestedLearningPlanId() {
        return requestedLearningPlanId;
    }

    public void setRequestedLearningPlanId(String requestedLearningPlanId) {
        this.requestedLearningPlanId = requestedLearningPlanId;
    }

    @Override
    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    public int getSelectedRegistrationCodeCount() {
        return selectedRegistrationCodeCount;
    }

    public void setSelectedRegistrationCodeCount(int selectedRegistrationCodeCount) {
        this.selectedRegistrationCodeCount = selectedRegistrationCodeCount;
    }

    @Override
    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    @Override
    public RegistrationDetailsInfo getRegistrationDetails() {
        return registrationDetails;
    }

    public void setRegistrationDetails(RegistrationDetailsInfo registrationDetails) {
        this.registrationDetails = registrationDetails;
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


    public ScheduleBuildStrategy getScheduleBuildStrategy() {
        if (scheduleBuildStrategy == null) {
            scheduleBuildStrategy = UwMyplanServiceLocator.getInstance().getScheduleBuildStrategy();
        }
        return scheduleBuildStrategy;
    }

    public void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        this.scheduleBuildStrategy = scheduleBuildStrategy;
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
