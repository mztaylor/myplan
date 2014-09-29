package org.kuali.student.myplan.registration.service;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.main.service.MyPlanLookupableImpl;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.registration.dataobject.RegistrationTerm;
import org.kuali.student.myplan.registration.util.RegistrationConstants;
import org.kuali.student.myplan.schedulebuilder.dto.*;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by hemanth on 9/4/14.
 */
public class RegistrationLookupableHelperImpl extends MyPlanLookupableImpl {
    private final Logger logger = Logger.getLogger(RegistrationLookupableHelperImpl.class);
    private TermHelper termHelper;
    private PlanHelper planHelper;
    private UserSessionHelper userSessionHelper;
    private ScheduleBuildStrategy scheduleBuildStrategy;

    /**
     * Skip the validation so that we use the criteriaFields param to pass in args to the getSearchResults method.
     *
     * @param form
     * @param searchCriteria
     * @return
     */
    @Override
    public boolean validateSearchParameters(LookupForm form, Map<String, String> searchCriteria) {
        return true;
    }

    @Override
    protected List<RegistrationTerm> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        List<RegistrationTerm> registrationTerms = new ArrayList<RegistrationTerm>();
        List<Term> termsPublished = getTermHelper().getOfficialTerms();
        List<PossibleScheduleOption> possibleScheduleOptions = new ArrayList<PossibleScheduleOption>();
        LearningPlan learningPlan = getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId());

        for (Term term : termsPublished) {
            RegistrationTerm registrationTerm = new RegistrationTerm();
            registrationTerm.setTermId(term.getId());
            registrationTerm.setOpenForRegistration(getTermHelper().isOpenForRegistration(term.getId(), new DateTime()));

            if (learningPlan != null) {

                registrationTerm.setLearningPlanId(learningPlan.getId());

                /*************PlannedCourseList**************/
                List<PlannedCourseDataObject> plannedCoursesList = new ArrayList<PlannedCourseDataObject>();
                Set<String> plannedCourseCodes = new HashSet<String>();

                try {
                    plannedCoursesList = getPlanHelper().getPlanItemListByTermId(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, learningPlan.getStudentId(), term.getId());
                    for (PlannedCourseDataObject pcdo : plannedCoursesList) {
                        if (!pcdo.isPlaceHolder()) {
                            plannedCourseCodes.add(pcdo.getCourseDetails().getCode());
                        }
                    }
                } catch (Exception e) {
                    logger.error("Could not load plannedCourseslist", e);
                }

                registrationTerm.setRequestedLearningPlanId(learningPlan.getId());

                if (CollectionUtils.isNotEmpty(plannedCoursesList)) {
                    registrationTerm.setPlannedItemsCount(plannedCourseCodes.size());
                    //There are planned courses for this term
                    registrationTerm.setPlannedCourses(true);

                    boolean plannedActivitiesExist = false;
                    for (PlannedCourseDataObject plannedCourseDataObject : plannedCoursesList) {
                        if (CollectionUtils.isNotEmpty(plannedCourseDataObject.getPlanActivities())) {
                            plannedActivitiesExist = true;
                            break;
                        }
                    }

                    if (plannedActivitiesExist) {
                        //There are planned activities for this term
                        registrationTerm.setPlannedActivities(true);

                        ScheduleBuildFiltersInfo scheduleBuildFiltersInfo = new ScheduleBuildFiltersInfo();
                        scheduleBuildFiltersInfo.setShowClosed(true);
                        scheduleBuildFiltersInfo.setShowOtherInstitutes(true);
                        scheduleBuildFiltersInfo.setShowRestricted(true);
                        scheduleBuildFiltersInfo.setExcludeReservedTimes(true);

                        List<CourseOption> registeredCourses = new ArrayList<CourseOption>();
                        List<CourseOption> courseSetsValidatedCourseOptions = new ArrayList<CourseOption>();
                        List<CourseOption> timeConflictsValidatedCourseOptions = new ArrayList<CourseOption>();

                        //Step 1: Get CourseOptions for the specific term and which do not conflict with already registered courses for the term.
                        List<CourseOption> courseOptionList = getScheduleBuildStrategy().getCourseOptions(learningPlan.getId(), term.getId(), scheduleBuildFiltersInfo);


                        //Step 2: Validate the courseOptionList and exclude the course Options which are not having complete course activities set.
                        validateForCompleteCourseSet(registeredCourses, courseSetsValidatedCourseOptions, courseOptionList);


                        //Step 3: Validate the courseSetsValidatedCourseOptions and exclude the course Options which are conflicting with each other.
                        validateForTimeConflicts(term, scheduleBuildFiltersInfo, courseSetsValidatedCourseOptions, timeConflictsValidatedCourseOptions);

                        //After completion of step 3 if there are still course options available then those course options are valid for registration.
                        if (CollectionUtils.isNotEmpty(timeConflictsValidatedCourseOptions)) {
                            ScheduleBuilder scheduleBuilder = getScheduleBuildStrategy().getScheduleBuilder(term, timeConflictsValidatedCourseOptions, new ArrayList<ReservedTime>(), new ArrayList<PossibleScheduleOption>(), scheduleBuildFiltersInfo);
                            List<PossibleScheduleOption> possibleScheduleOptionList = scheduleBuilder.getNext(1, Collections.<PossibleScheduleOption>emptySet());
                            if (CollectionUtils.isNotEmpty(possibleScheduleOptionList)) {
                                PossibleScheduleOptionInfo possibleScheduleOptionInfo = (PossibleScheduleOptionInfo) possibleScheduleOptionList.get(0);
                                //Doing this so that this registration component will be independent of number of tabs opened in a browser.
                                //Since we store these possible schedules in session and when registration controller tries to pick up the selected registration quarter this would help.
                                possibleScheduleOptionInfo.setId(term.getId());
                                possibleScheduleOptions.add(possibleScheduleOptionInfo);
                                for (ActivityOption ao : possibleScheduleOptionInfo.getActivityOptions()) {
                                    plannedCourseCodes.remove(ao.getCourseCd());
                                }
                                registrationTerm.setCourseRegistrationCount(timeConflictsValidatedCourseOptions.size());
                                registrationTerm.setPossibleScheduleUniqueId(possibleScheduleOptionInfo.getId());
                                registrationTerm.setErrorPlannedCourses(new ArrayList(plannedCourseCodes));
                            }
                        } else { // no validated course options
                            registrationTerm.setErrorPlannedCourses(new ArrayList(plannedCourseCodes));
                        }
                    }
                }
            }
            registrationTerms.add(registrationTerm);
        }

        request.getSession().setAttribute(RegistrationConstants.REGISTRATION_POSSIBLE_SCHEDULES, possibleScheduleOptions);

        return registrationTerms;
    }


    /**
     * Checks to see if any of the course options are conflicting with each other and excludes them from being considered as a valid course options.
     *
     * @param term
     * @param scheduleBuildFiltersInfo
     * @param courseSetsValidatedCourseOptions
     * @param timeConflictsValidatedCourseOptions
     */
    private void validateForTimeConflicts(Term term, ScheduleBuildFiltersInfo scheduleBuildFiltersInfo, List<CourseOption> courseSetsValidatedCourseOptions, List<CourseOption> timeConflictsValidatedCourseOptions) {
        for (CourseOption replica1CourseOption : courseSetsValidatedCourseOptions) {
            boolean addThisCourseOption = true;
            for (CourseOption replica2CourseOption : courseSetsValidatedCourseOptions) {
                if (!replica1CourseOption.getCourseCode().equals(replica2CourseOption.getCourseCode())) {
                    List<CourseOption> courseOptions = new ArrayList<CourseOption>();
                    courseOptions.add(replica1CourseOption);
                    courseOptions.add(replica2CourseOption);
                    ScheduleBuilder scheduleBuilder = getScheduleBuildStrategy().getScheduleBuilder(term, courseOptions, new ArrayList<ReservedTime>(), new ArrayList<PossibleScheduleOption>(), scheduleBuildFiltersInfo);
                    List<PossibleScheduleOption> possibleScheduleOptions = scheduleBuilder.getNext(1, Collections.<PossibleScheduleOption>emptySet());
                    if (CollectionUtils.isEmpty(possibleScheduleOptions)) {
                        addThisCourseOption = false;
                        break;
                    }
                }
            }
            if (addThisCourseOption) {
                timeConflictsValidatedCourseOptions.add(replica1CourseOption);
            }
        }
    }

    /**
     * Checks to see if course option has activities planned for each type of activity.
     * For Eg: If a course contains Lecture, Lab, Quiz then there should be only one activity planned from each group.
     *
     * @param registeredCourses
     * @param preliminaryValidatedCourseOptions
     * @param courseOptionList
     */
    private void validateForCompleteCourseSet(List<CourseOption> registeredCourses, List<CourseOption> preliminaryValidatedCourseOptions, List<CourseOption> courseOptionList) {
        for (CourseOption courseOption : courseOptionList) {

            CourseOptionInfo courseOptionInfo = (CourseOptionInfo) courseOption;
            if (CollectionUtils.isNotEmpty(courseOptionInfo.getActivityOptions()) && courseOptionInfo.getActivityOptions().size() == 1) {

                if (courseOptionInfo.getActivityOptions().get(0).isLockedIn()) {
                    //Registered Course option
                    registeredCourses.add(courseOptionInfo);

                } else if (StringUtils.isNotEmpty(courseOptionInfo.getActivityOptions().get(0).getPlanItemId()) && getPlannedActivityOptionsCount(courseOptionInfo.getActivityOptions().get(0).getAlternateActivties()) == 0) {
                    //A Course Option will have the activity options sorted based on whether they are planned or not. So if there is a activity option which is planned it will always be on top other planned activities which fall with in same time are added as alternated to the activity option.
                    //There is only one planned Activity Option planned and also there aren't any alternatives that are planned which is as expected behaviour
                    ActivityOptionInfo activityOptionInfo = (ActivityOptionInfo) courseOptionInfo.getActivityOptions().get(0);
                    List<SecondaryActivityOptions> secondaryActivityOptions = new ArrayList<SecondaryActivityOptions>();

                    //Need to clear all alternatives as we only need planned activity option
                    activityOptionInfo.setAlternateActivities(new ArrayList<ActivityOption>());

                    int size = activityOptionInfo.getSecondaryOptions().size();

                    for (SecondaryActivityOptions secondaryActivityOption : activityOptionInfo.getSecondaryOptions()) {
                        SecondaryActivityOptionsInfo secondaryActivityOptionsInfo = (SecondaryActivityOptionsInfo) secondaryActivityOption;
                        if (CollectionUtils.isNotEmpty(secondaryActivityOptionsInfo.getActivityOptions()) && secondaryActivityOptionsInfo.getActivityOptions().size() == 1 && StringUtils.isNotEmpty(secondaryActivityOptionsInfo.getActivityOptions().get(0).getPlanItemId()) && getPlannedActivityOptionsCount(secondaryActivityOptionsInfo.getActivityOptions().get(0).getAlternateActivties()) == 0) {
                            //Need to clear all alternatives as we only need planned activity option
                            ((ActivityOptionInfo) secondaryActivityOptionsInfo.getActivityOptions().get(0)).setAlternateActivities(new ArrayList<ActivityOption>());
                            secondaryActivityOptions.add(secondaryActivityOptionsInfo);
                        }
                    }

                    if (size == secondaryActivityOptions.size()) {
                        activityOptionInfo.setSecondaryOptions(secondaryActivityOptions);
                        courseOptionInfo.setActivityOptions(new ArrayList<ActivityOption>());
                        courseOptionInfo.getActivityOptions().add(activityOptionInfo);
                        preliminaryValidatedCourseOptions.add(courseOptionInfo);
                    }

                }
            }
        }
    }

    /**
     * Counts the number of planned activities in a list of activity options
     *
     * @param activityOptions
     * @return
     */
    private int getPlannedActivityOptionsCount(List<ActivityOption> activityOptions) {
        int count = 0;
        for (ActivityOption activityOption : activityOptions) {
            if (StringUtils.isNotBlank(activityOption.getPlanItemId())) {
                count++;
            }
        }
        return count;
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

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = UwMyplanServiceLocator.getInstance().getPlanHelper();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
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
