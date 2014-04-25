package org.kuali.student.myplan.registration.form;

import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.registration.util.RegistrationConstants;
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

import java.util.*;

/**
 * Created by hemanthg on 4/22/2014.
 */
public class DefaultRegistrationForm extends UifFormBase implements RegistrationForm {

    private Term term;
    private String termId;
    private String uniqueId;
    private String requestedLearningPlanId;
    private List<String> selectedRegistrationCodes;
    private Map<String, String> plannedItems;
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

        setSelectedRegistrationCodes(new ArrayList<String>());
        RegistrationDetailsInfo registrationDetails = null;
        if (getPageId().equals(RegistrationConstants.REGISTRATION_PAGE_1)) {
            registrationDetails = getRegistrationDetailsInfo();
        } else if (getPageId().equals(RegistrationConstants.REGISTRATION_PAGE_2)) {
            if (getRegistrationDetails() != null) {
                registrationDetails = getRegistrationDetails();
                List<CourseOption> courseOptionList = new ArrayList<CourseOption>();
                for (CourseOption courseOption : registrationDetails.getPlannedCourses()) {
                    populateSelectedActivitiesForReg(courseOption.getActivityOptions());
                    courseOptionList.add(courseOption);
                }
                registrationDetails.setPlannedCourses(courseOptionList);
                setPageId(RegistrationConstants.REGISTRATION_PAGE_3);
            }
        }


        if (registrationDetails != null && !CollectionUtils.isEmpty(getSelectedRegistrationCodes()) && getSelectedRegistrationCodes().size() > 0 && getSelectedRegistrationCodes().size() <= 8) {
            registrationDetails.setRegistrationUrl(buildRegistrationUrl());
        }
        setRegistrationDetails(registrationDetails);
    }

    /**
     * Builds the registration Url for given registration codes
     *
     * @return
     */
    protected String buildRegistrationUrl() {
        int index = 1;
        List<String> regUrlParams = new ArrayList<String>();
        for (String registrationCode : getSelectedRegistrationCodes()) {
            if (index > 8) {
                return null;
            }
            regUrlParams.add(String.format(RegistrationConstants.REGISTRATION_CODE_URL_PARAMS_FROMAT, index, registrationCode, index, index, index));
            index++;
        }
        AtpHelper.YearTerm yearTerm = AtpHelper.atpToYearTerm(getTermId());
        String regUrl = String.format(RegistrationConstants.REGISTRATION_URL_FORMAT, yearTerm.getTerm(), yearTerm.getYear(), "placeHolderToken", org.apache.commons.lang.StringUtils.join(regUrlParams, "&"));
        return regUrl;
    }

    /**
     * Logic for the initial page load with all the registration details
     *
     * @return RegistrationDetailsInfo
     */
    protected RegistrationDetailsInfo getRegistrationDetailsInfo() {
        RegistrationDetailsInfo registrationDetails;
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
        setPlannedItems(getPlanHelper().getPlanItemIdAndRefObjIdByRefObjType(getRequestedLearningPlanId(), PlanConstants.SECTION_TYPE, getTermId()));


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
                courseOption.setCredits(activityOption.getCourseCredit());
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
                List<ActivityOption> validatedActivities = getScheduleBuildHelper().validatedSavedActivities(activityOptions, invalidOptions, reservedTimes, new ArrayList<String>(getPlannedItems().keySet()), registeredPossibleSchedule);
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
        return registrationDetails;
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
    private void populateSelectedActivitiesForReg(List<ActivityOption> activityOptions) {
        for (ActivityOption activityOption : activityOptions) {
            ActivityOptionInfo ao = (ActivityOptionInfo) activityOption;
            for (SecondaryActivityOptions secondaryActivityOptions : ao.getSecondaryOptions()) {
                populateSelectedActivitiesForReg(secondaryActivityOptions.getActivityOptions());
            }
            populateSelectedActivitiesForReg(ao.getAlternateActivties());
            if (StringUtils.hasText(activityOption.getRegistrationCode())) {
                getSelectedRegistrationCodes().add(activityOption.getRegistrationCode());
            }
        }
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

    @Override
    public List<String> getSelectedRegistrationCodes() {
        if (selectedRegistrationCodes == null) {
            selectedRegistrationCodes = new ArrayList<String>();
        }
        return selectedRegistrationCodes;
    }

    public void setSelectedRegistrationCodes(List<String> selectedRegistrationCodes) {
        this.selectedRegistrationCodes = selectedRegistrationCodes;
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

    @Override
    public Map<String, String> getPlannedItems() {
        if (plannedItems == null) {
            plannedItems = new HashMap<String, String>();
        }
        return plannedItems;
    }

    public void setPlannedItems(Map<String, String> plannedItems) {
        this.plannedItems = plannedItems;
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
