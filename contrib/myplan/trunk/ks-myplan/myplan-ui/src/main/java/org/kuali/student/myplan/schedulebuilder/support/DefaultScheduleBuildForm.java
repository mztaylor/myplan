package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.dto.*;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.*;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DefaultScheduleBuildForm extends DefaultScheduleForm implements
        ScheduleBuildForm {

    private static final long serialVersionUID = 3274052642980656068L;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultScheduleBuildForm.class);


    private boolean more;
    private String uniqueId;

    private int possibleScheduleSize;
    private ScheduleBuilder scheduleBuilder;
    private List<CourseOption> courseOptions;
    private List<PossibleScheduleOption> savedSchedules;
    private List<PossibleScheduleOption> possibleScheduleOptions;
    private PossibleScheduleOption registeredSchedule;
    private RegistrationDetailsInfo registrationDetails;

    private transient AcademicCalendarService academicCalendarService;

    private transient UserSessionHelper userSessionHelper;


    private void updateReservedTimesOnBuild() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        if (getRemoveReserved() != null && getReservedTimes() != null
                && getRemoveReserved() >= 0 && getRemoveReserved() < getReservedTimes().size()) {
            try {
                sb.deleteReservedTime(getRequestedLearningPlanId(), getReservedTimes()
                        .get(getRemoveReserved()).getId());
            } catch (PermissionDeniedException e) {
                throw new IllegalStateException(
                        "Failed to remove reserved time", e);
            }
        }
        setRemoveReserved(null);

        List<ReservedTime> newReservedTimes;
        try {
            newReservedTimes = sb.getReservedTimesForTermId(getRequestedLearningPlanId(), getTermId());
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Failed to refresh reserved times",
                    e);
        }

        if (newReservedTimes != null && getReservedTimes() != null)
            for (int i = 0; i < newReservedTimes.size(); i++) {
                if (i < getReservedTimes().size()) {
                    ReservedTimeInfo nrt = (ReservedTimeInfo) newReservedTimes
                            .get(i);
                    nrt.setSelected(getReservedTimes().get(i).isSelected());
                }
            }
        setReservedTimes(newReservedTimes);
    }

    private void updateSavedSchedulesOnBuild() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        List<PossibleScheduleOption> newSavedSchedules;
        try {
            newSavedSchedules = sb.getSchedulesForTerm(getRequestedLearningPlanId(), getTermId());
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException(
                    "Failed to refresh saved schedules", e);
        }

        if (newSavedSchedules != null && savedSchedules != null) {
            Iterator<PossibleScheduleOption> nsi = newSavedSchedules.iterator();
            int i = 0;
            while (nsi.hasNext()) {
                PossibleScheduleOptionInfo nss = (PossibleScheduleOptionInfo) nsi.next();
                if (!getTermId().equals(nss.getTermId())) {
                    nsi.remove();
                }

                if (i < savedSchedules.size()) {
                    nss.setSelected(savedSchedules.get(i).isSelected());
                }
                i++;
            }
        }
        savedSchedules = newSavedSchedules;
        for (PossibleScheduleOption possibleScheduleOption : getSavedSchedules()) {
            getScheduleBuildHelper().buildPossibleScheduleEvents(possibleScheduleOption, getTerm());
        }

    }

    /*Defaulting to 8:00Am*/
    protected long getDefaultMinTime() {
        Calendar defaultStart = Calendar.getInstance();
        defaultStart.setTime(getTerm().getStartDate());
        defaultStart.set(defaultStart.get(Calendar.YEAR), defaultStart.get(Calendar.MONTH), defaultStart.get(Calendar.DATE), 8, 0);
        return defaultStart.getTime().getTime();
    }

    /*Defaulting to 5:00Pm*/
    protected long getDefaultMaxTime() {
        Calendar defaultEnd = Calendar.getInstance();
        defaultEnd.setTime(getTerm().getEndDate());
        defaultEnd.set(defaultEnd.get(Calendar.YEAR), defaultEnd.get(Calendar.MONTH), defaultEnd.get(Calendar.DATE), 17, 0);
        return defaultEnd.getTime().getTime();
    }

    @Override
    public void buildSchedules() {
        updateReservedTimesOnBuild();
        updateSavedSchedulesOnBuild();

        /*Clear all Zero results errors*/
        ((ScheduleBuildFiltersInfo) getBuildFilters()).setZeroResultsReasons(new ArrayList<String>());
        ((ScheduleBuildFiltersInfo) getBuildFilters()).setResultsNotPossibleReasons(new ArrayList<String>());

        courseOptions = getScheduleBuildStrategy().getCourseOptions(getRequestedLearningPlanId(), getTermId(), getBuildFilters());

        validateAndGenerateTerm();

        if (more) {

            if (scheduleBuilder == null)
                throw new IllegalStateException(
                        "Schedule builder not initialized");

            if (possibleScheduleOptions == null)
                throw new IllegalStateException(
                        "Possible schedule options not initialized");

            // preserve order specified by front-end "shuffle"
            Collections.sort(possibleScheduleOptions,
                    new Comparator<PossibleScheduleOption>() {
                        @Override
                        public int compare(PossibleScheduleOption o1,
                                           PossibleScheduleOption o2) {
                            int s1 = o1.getShuffle();
                            int s2 = o2.getShuffle();
                            return s1 < s2 ? -1 : s1 == s2 ? 0 : 1;
                        }
                    }
            );

            Set<PossibleScheduleOption> current = new HashSet<PossibleScheduleOption>();
            int nextn = getPossibleScheduleOptions().size();
            for (PossibleScheduleOption pso : getPossibleScheduleOptions()) {
                if (pso.isSelected() || pso.isDiscarded())
                    current.add(pso);
                if (pso.isSelected())
                    nextn--;
            }

            if (nextn > 0) {
                List<PossibleScheduleOption> newOptions = getScheduleBuilder().getNext(nextn, current);
                int n = 0;
                ListIterator<PossibleScheduleOption> psi = getPossibleScheduleOptions().listIterator();
                while (psi.hasNext() && n < newOptions.size()) {
                    PossibleScheduleOption psn = psi.next();
                    if (!psn.isSelected() && !psn.isDiscarded())
                        psi.set(newOptions.get(n++));
                }
            }

        } else {
            scheduleBuilder = getScheduleBuildStrategy().getScheduleBuilder(getTerm(), getCourseOptions(), getReservedTimes(), getSavedSchedules(), getBuildFilters());
            possibleScheduleOptions = scheduleBuilder.getNext(getPossibleScheduleSize(), Collections.<PossibleScheduleOption>emptySet());
            registeredSchedule = scheduleBuilder.getRegistered();
        }

        setMinTime(getDefaultMinTime());
        setMaxTime(getDefaultMaxTime());

        /*Calculating min & max times of all possibleSchedules combined and If there is any weekend or TBD in any of the possible schedules*/
        for (PossibleScheduleOption possibleScheduleOption : getPossibleScheduleOptions()) {
            if (!isTbd() && possibleScheduleOption.isTbd()) {
                setTbd(possibleScheduleOption.isTbd());
            }
            if (!isWeekend() && possibleScheduleOption.isWeekend()) {
                setWeekend(possibleScheduleOption.isWeekend());
            }

            if (possibleScheduleOption.getMinTime() < getMinTime()) {
                setMinTime(possibleScheduleOption.getMinTime());
            }

            if (possibleScheduleOption.getMaxTime() > getMaxTime()) {
                setMaxTime(possibleScheduleOption.getMaxTime());
            }

        }
        /*Calculating min & max times for registered schedules and If there is any weekend or TBD in any of the registered possible schedules*/
        if (getRegisteredSchedule() != null) {

            ((PossibleScheduleOptionInfo) getRegisteredSchedule()).setLockedIn(true);

            PossibleScheduleOption possibleScheduleOption = getRegisteredSchedule();
            if (!isTbd() && possibleScheduleOption.isTbd()) {
                setTbd(possibleScheduleOption.isTbd());
            }
            if (!isWeekend() && possibleScheduleOption.isWeekend()) {
                setWeekend(possibleScheduleOption.isWeekend());
            }

            if (possibleScheduleOption.getMinTime() < getMinTime()) {
                setMinTime(possibleScheduleOption.getMinTime());
            }

            if (possibleScheduleOption.getMaxTime() > getMaxTime()) {
                setMaxTime(possibleScheduleOption.getMaxTime());
            }
        }


    }

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

        int slnCount = 0;
        RegistrationDetailsInfo registrationDetails = null;
        if (getPageId().equals(ScheduleBuilderConstants.REGISTRAION_PAGE_1)) {
            registrationDetails = new RegistrationDetailsInfo();
            updateReservedTimesOnBuild();
            updateSavedSchedulesOnBuild();

            /*populating the registered courses*/
            List<CourseOption> registeredCourseOptions = getScheduleBuildStrategy().getRegisteredCourseOptions(getUserSessionHelper().getStudentId(), getTermId(), new ScheduleBuildFiltersInfo());
            registrationDetails.setRegisteredCourses(registeredCourseOptions);


            /*Planned Activity map*/
            Map<String, String> plannedItems = getPlanHelper().getPlanItemIdAndRefObjIdByRefObjType(getRequestedLearningPlanId(), PlanConstants.SECTION_TYPE, getTermId());

            List<PossibleScheduleOption> pinnedSchedules = getSavedSchedules();
            /*RegisteredPossibleSchedule*/
            PossibleScheduleOption registeredPossibleSchedule = null;
            if (getTerm() != null) {
                registeredPossibleSchedule = getScheduleBuildStrategy().getScheduleBuilder(getTerm(), registeredCourseOptions, getReservedTimes(), pinnedSchedules, new ScheduleBuildFiltersInfo()).getRegistered();
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
                            List<ActivityOption> alternates = secondaryActivity.getAlternateActivties();
                            ((ActivityOptionInfo) secondaryActivity).setAlternateActivities(new ArrayList<ActivityOption>());
                            ((ActivityOptionInfo) secondaryActivity).setSelectedForReg("true");
                            secondaryActivities.add(secondaryActivity);
                            secondaryActivities.addAll(alternates);
                            break;
                        }
                        ((SecondaryActivityOptionsInfo) secondaryActivityOptions).setActivityOptions(secondaryActivities);
                    }
                    List<ActivityOption> activityOptions = courseOption.getActivityOptions();
                    List<ActivityOption> alternates = activityOption.getAlternateActivties();
                    ((ActivityOptionInfo) activityOption).setAlternateActivities(new ArrayList<ActivityOption>());
                    ((ActivityOptionInfo) activityOption).setSelectedForReg("true");
                    activityOptions.add(activityOption);
                    activityOptions.addAll(alternates);
                    Map<String, Map<String, List<String>>> invalidOptions = new LinkedHashMap<String, Map<String, List<String>>>();
                    PossibleScheduleErrorsInfo possibleScheduleErrorsInfo = new PossibleScheduleErrorsInfo();
                    List<ActivityOption> validatedActivities = getScheduleBuildHelper().validatedSavedActivities(activityOptions, invalidOptions, getReservedTimes(), new ArrayList<String>(plannedItems.keySet()), registeredPossibleSchedule);
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
                        validateForErrors(possibleScheduleErrorsInfo, courseOption.getCourseCode(), activityOptions, invalidActivities);
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
                for (CourseOption courseOption : registrationDetails.getPlannedCourses()) {
                    List<ActivityOption> selectedActivities = getSelectedActivitiesForReg(courseOption.getActivityOptions(), slnCount);
                    ((CourseOptionInfo) courseOption).setActivityOptions(selectedActivities);
                    courseOptionList.add(courseOption);
                }
                registrationDetails.setPlannedCourses(courseOptionList);
                setPageId(ScheduleBuilderConstants.REGISTRAION_PAGE_3);
            }
        }


        if (slnCount <= 8) {
            registrationDetails.setRegistrationUrl("placeholderUrl");
        }
        setRegistrationDetails(registrationDetails);
    }

    private void validateForErrors(PossibleScheduleErrorsInfo possibleScheduleErrors, String courseCd, List<ActivityOption> activityOptions, List<String> invalidatedActivities) {
        List<String> activitiesToExclude = new ArrayList<String>();
        Properties properties = new Properties();
        InputStream file = getClass().getResourceAsStream(PlanConstants.PROPERTIES_FILE_PATH);
        try {
            properties.load(file);
        } catch (Exception e) {
            LOG.error("Could not find the properties file" + e);
        }
        if (possibleScheduleErrors != null && StringUtils.hasText(possibleScheduleErrors.getErrorType()) && !CollectionUtils.isEmpty(possibleScheduleErrors.getInvalidOptions()) && !CollectionUtils.isEmpty(possibleScheduleErrors.getInvalidOptions().get(courseCd))) {
            Map<String, List<String>> invalidActivities = possibleScheduleErrors.getInvalidOptions().get(courseCd);

            List<String> withdrawnErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN);
            List<String> closedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CLOSED);
            List<String> suspendedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_SUSPENDED);
            List<String> enrollErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR);
            List<String> timeChangedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED);
            List<String> conflictedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED);
            List<String> registeredConflictedErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED);
            List<String> unAvailableSecondariesErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES);
            List<String> noErrorActivities = invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_ERROR) == null ? new ArrayList<String>() : invalidActivities.get(ScheduleBuilderConstants.PINNED_SCHEDULES_ERROR_REASON_NO_ERROR);

            activitiesToExclude.addAll(withdrawnErrorActivities);
            invalidatedActivities.addAll(withdrawnErrorActivities);
            activitiesToExclude.addAll(closedErrorActivities);
            activitiesToExclude.addAll(enrollErrorActivities);
            activitiesToExclude.addAll(suspendedErrorActivities);
            invalidatedActivities.addAll(suspendedErrorActivities);
            activitiesToExclude.addAll(timeChangedErrorActivities);
            invalidatedActivities.addAll(timeChangedErrorActivities);
            activitiesToExclude.addAll(conflictedErrorActivities);
            invalidatedActivities.addAll(conflictedErrorActivities);
            activitiesToExclude.addAll(registeredConflictedErrorActivities);
            activitiesToExclude.addAll(unAvailableSecondariesErrorActivities);
            invalidatedActivities.addAll(unAvailableSecondariesErrorActivities);
            activitiesToExclude.addAll(noErrorActivities);
            invalidatedActivities.addAll(noErrorActivities);
            Collections.sort(activitiesToExclude);
            Collections.sort(invalidatedActivities);
            if (activityOptionsContains(activityOptions, activitiesToExclude)) {
                if (invalidatedActivities.size() != activitiesToExclude.size()) {
                    StringBuffer errorMessage = new StringBuffer();
                    if (!CollectionUtils.isEmpty(withdrawnErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                        if (withdrawnErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_WITHDRAWN_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(withdrawnErrorActivities.subList(0, withdrawnErrorActivities.size() - 1), ", "), withdrawnErrorActivities.get(withdrawnErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_WITHDRAWN_SINGLE), courseCd, withdrawnErrorActivities.get(0))).append("</p>");
                        }

                    }
                    if (!CollectionUtils.isEmpty(closedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                        if (closedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_CLOSED_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(closedErrorActivities.subList(0, closedErrorActivities.size() - 1), ", "), closedErrorActivities.get(closedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_CLOSED_SINGLE), courseCd, closedErrorActivities.get(0))).append("</p>");
                        }

                    }
                    if (!CollectionUtils.isEmpty(suspendedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                        if (suspendedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_SUSPENDED_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(suspendedErrorActivities.subList(0, suspendedErrorActivities.size() - 1), ", "), suspendedErrorActivities.get(suspendedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_SUSPENDED_SINGLE), courseCd, suspendedErrorActivities.get(0))).append("</p>");
                        }

                    }
                    if (!CollectionUtils.isEmpty(enrollErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {

                        if (enrollErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_ENROLL_RESTR_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(enrollErrorActivities.subList(0, enrollErrorActivities.size() - 1), ", "), enrollErrorActivities.get(enrollErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_ENROLL_RESTR_SINGLE), courseCd, enrollErrorActivities.get(0))).append("</p>");
                        }

                    }
                    if (!CollectionUtils.isEmpty(timeChangedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                        if (timeChangedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_TIME_CHANGED_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(timeChangedErrorActivities.subList(0, timeChangedErrorActivities.size() - 1), ", "), timeChangedErrorActivities.get(timeChangedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_TIME_CHANGED_SINGLE), courseCd, timeChangedErrorActivities.get(0))).append("</p>");
                        }
                    }
                    if (!CollectionUtils.isEmpty(conflictedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                        if (conflictedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_RESERVED_CONFLICT_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(conflictedErrorActivities.subList(0, conflictedErrorActivities.size() - 1), ", "), conflictedErrorActivities.get(conflictedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_RESERVED_CONFLICT_SINGLE), courseCd, conflictedErrorActivities.get(0))).append("</p>");
                        }
                    }
                    if (!CollectionUtils.isEmpty(registeredConflictedErrorActivities) && CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                        if (registeredConflictedErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_REGISTERED_CONFLICT_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(registeredConflictedErrorActivities.subList(0, registeredConflictedErrorActivities.size() - 1), ", "), registeredConflictedErrorActivities.get(registeredConflictedErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_REGISTERED_CONFLICT_SINGLE), courseCd, registeredConflictedErrorActivities.get(0))).append("</p>");
                        }
                    }
                    if (!CollectionUtils.isEmpty(unAvailableSecondariesErrorActivities)) {
                        unAvailableSecondariesErrorActivities.addAll(withdrawnErrorActivities);
                        unAvailableSecondariesErrorActivities.addAll(timeChangedErrorActivities);
                        unAvailableSecondariesErrorActivities.addAll(conflictedErrorActivities);
                        if (unAvailableSecondariesErrorActivities.size() > 1) {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_NO_SECONDARIES_MULTIPLE), courseCd, org.apache.commons.lang.StringUtils.join(unAvailableSecondariesErrorActivities.subList(0, unAvailableSecondariesErrorActivities.size() - 1), ", "), unAvailableSecondariesErrorActivities.get(unAvailableSecondariesErrorActivities.size() - 1))).append("</p>");
                        } else {
                            errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.VALID_PINNED_SCHEDULE_NO_SECONDARIES_SINGLE), courseCd, unAvailableSecondariesErrorActivities.get(0))).append("</p>");
                        }
                    }
                    if (StringUtils.hasText(errorMessage)) {
                        possibleScheduleErrors.setErrorType(ScheduleBuilderConstants.PINNED_SCHEDULES_PASSIVE_ERROR);
                        possibleScheduleErrors.setErrorMessage(errorMessage.toString());
                    }
                } else if (invalidatedActivities.size() == activitiesToExclude.size()) {
                    StringBuffer errorMessage = new StringBuffer();
                    errorMessage = errorMessage.append("<p>").append(String.format(properties.getProperty(ScheduleBuilderConstants.INVALID_PINNED_SCHEDULE))).append("</p>");
                    if (StringUtils.hasText(errorMessage)) {
                        possibleScheduleErrors.setErrorType(ScheduleBuilderConstants.PINNED_SCHEDULES_MODAL_ERROR);
                        possibleScheduleErrors.setErrorMessage(errorMessage.toString());
                    }
                }
            }
        }
    }

    /**
     * recursive method used to check if a activity cd is present in any of the primary, secondary or alternate activity options
     *
     * @param activityOptions
     * @param activityCds
     * @return
     */
    private boolean activityOptionsContains(List<ActivityOption> activityOptions, List<String> activityCds) {
        for (ActivityOption activityOption : activityOptions) {
            if (activityCds.contains(activityOption.getActivityCode())) {
                return true;
            }
            if (activityOptionsContains(activityOption.getAlternateActivties(), activityCds)) {
                return true;
            }

            for (SecondaryActivityOptions so : activityOption.getSecondaryOptions()) {
                if (activityOptionsContains(so.getActivityOptions(), activityCds)) {
                    return true;
                }
            }
        }
        return false;
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
            slnCount = slnCount++;
            newAOList.add(ao);
        }
        return newAOList;

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

    @Override
    public PossibleScheduleOption saveSchedule() {

        if (getPossibleScheduleOptions() == null || getUniqueId() == null
                || getPossibleScheduleOptions().isEmpty()) {
            LOG.error("No possible schedule options to save from - uniqueId = " + getUniqueId());
            return null;
        }

        PossibleScheduleOption saveMe = null;
        for (PossibleScheduleOption pso : getPossibleScheduleOptions())
            if (getUniqueId().equals(pso.getUniqueId()))
                saveMe = pso;
        if (saveMe == null) {
            LOG.error("Invalid possible schedule unique ID " + getUniqueId());
            return null;
        }

        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        try {
            PossibleScheduleOption rv = sb.createSchedule(
                    getRequestedLearningPlanId(), saveMe);
            getSavedSchedules().add(rv);
            return rv;
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Unexpected authorization failure",
                    e);
        }
    }

    @Override
    public String removeSchedule() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        try {
            sb.deleteSchedule(getRequestedLearningPlanId(), getUniqueId());
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Failed to remove reserved time", e);
        }
        return getUniqueId();
    }

    public ScheduleBuilder getScheduleBuilder() {
        return scheduleBuilder;
    }

    public void setScheduleBuilder(ScheduleBuilder scheduleBuilder) {
        this.scheduleBuilder = scheduleBuilder;
    }

    /**
     * Template variable only.
     *
     * @return false.
     */
    public boolean isSelected() {
        return false;
    }

    public void setSelected(boolean selected) {
    }

    /**
     * Template variable only.
     *
     * @return false.
     */
    public boolean isDiscarded() {
        return false;
    }

    public void setDiscarded(boolean discarded) {
    }

    /**
     * Template variable only.
     *
     * @return 0.
     */
    public int getShuffle() {
        return 0;
    }

    public void setShuffle(int shuffle) {
    }

    @Override
    public boolean hasMore() {
        return scheduleBuilder != null && scheduleBuilder.hasMore();
    }

    public int getPossibleScheduleSize() {
        if (possibleScheduleSize == 0) {
            possibleScheduleSize = 25;
        }
        return possibleScheduleSize;
    }

    public void setPossibleScheduleSize(int possibleScheduleSize) {
        this.possibleScheduleSize = possibleScheduleSize;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }


    @Override
    public List<CourseOption> getCourseOptions() {
        if (courseOptions == null) {
            courseOptions = new ArrayList<CourseOption>();
        }
        return courseOptions;
    }

    public void setCourseOptions(List<CourseOption> courseOptions) {
        this.courseOptions = courseOptions;
    }


    @Override
    public List<PossibleScheduleOption> getPossibleScheduleOptions() {
        if (possibleScheduleOptions == null) {
            possibleScheduleOptions = new ArrayList<PossibleScheduleOption>();
        }
        return possibleScheduleOptions;
    }

    public void setPossibleScheduleOptions(
            List<PossibleScheduleOption> possibleScheduleOptions) {
        this.possibleScheduleOptions = possibleScheduleOptions;
    }

    @Override
    public List<PossibleScheduleOption> getSavedSchedules() {
        if (savedSchedules == null) {
            savedSchedules = new ArrayList<PossibleScheduleOption>();
        }
        return savedSchedules;
    }

    public void setSavedSchedules(
            List<PossibleScheduleOption> savedScheduleOptions) {
        this.savedSchedules = savedScheduleOptions;
    }

    @Override
    public PossibleScheduleOption getRegisteredSchedule() {
        return registeredSchedule;
    }

    public void setRegisteredSchedule(PossibleScheduleOption registeredSchedule) {
        this.registeredSchedule = registeredSchedule;
    }

    @Override
    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    public String getFullTermWeekTitle() {
        Term t = getTerm();
        long ed = t.getEndDate().getTime();
        long sd = t.getStartDate().getTime();
        int weeksInTerm = (int) ((ed - sd) / 604800000);
        return "Weeks 1-" + weeksInTerm;
    }

    public String getFullTermWeekSubtitle() {
        Term t = getTerm();
        DateFormat df = new SimpleDateFormat("MMM d");
        return df.format(t.getStartDate()) + " - " + df.format(t.getEndDate());
    }

    @Override
    public String toString() {
        return "ScheduleBuildForm [termId=" + getTermId() + "]";
    }

    private AcademicCalendarService getAcademicCalendarService() {
        if (academicCalendarService == null) {
            academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return academicCalendarService;
    }

    public void setAcademicCalendarService(AcademicCalendarService academicCalendarService) {
        this.academicCalendarService = academicCalendarService;
    }

    public RegistrationDetails getRegistrationDetails() {
        return registrationDetails;
    }

    public void setRegistrationDetails(RegistrationDetailsInfo registrationDetails) {
        this.registrationDetails = registrationDetails;
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
