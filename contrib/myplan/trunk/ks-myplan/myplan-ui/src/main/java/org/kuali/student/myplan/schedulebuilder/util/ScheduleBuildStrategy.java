package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.kuali.student.r2.lum.course.infc.Course;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Defines service interaction and institutional override behavior for schedule
 * builder.
 *
 * @author Mark Fyffe <mwfyffe@indiana.edu>
 * @version 0.7.1
 */
public interface ScheduleBuildStrategy {

    /**
     * Get the initial schedule build form.
     *
     * @return The initial schedule build form.
     */
    ScheduleBuildForm getInitialForm();

    /**
     * Load course options for a term from a list of course IDs.
     *
     * @param courseIds Course IDs from the existing shopping cart.
     * @param termId    The term to get options for.
     * @return The course options to use as inputs for generating schedules.
     */
    List<CourseOption> getCourseOptions(List<String> courseIds, Map<String, String> courseIdsTOCourseCds, String termId);


    /**
     * Load course options for student registered courses.
     *
     * @param studentId
     * @param termId
     * @param buildFilters
     * @return The course options to use as inputs for generating registered schedules
     */
    List<CourseOption> getRegisteredCourseOptions(String studentId, String termId, ScheduleBuildFilters buildFilters);

    /**
     * Load the course options to use as inputs for generating schedules.
     *
     * @param learningPlanId The learning plan ID.
     * @param termId         The term to get options for.
     * @return The course options to use as inputs for generating schedules.
     */
    List<CourseOption> getCourseOptions(String learningPlanId, String termId, ScheduleBuildFilters buildFilters);

    /**
     * Get the learning plan for schedule build to use as inputs.
     *
     * @param requestedLearningPlanId The requested learning plan ID. May be null to get the first
     *                                learning plan of type
     *                                for the student.
     * @return The learning plan for schedule build to use as inputs.
     * @throws PermissionDeniedException If the current user does not have access to the requested
     *                                   learning plan.
     */
    LearningPlan getLearningPlan(String requestedLearningPlanId) throws PermissionDeniedException;

    /**
     * Get reserved times related to the current learning plan.
     *
     * @param requestedLearningPlanId See {@link #getLearningPlan(String)}.
     * @return The reserved times related to the current learning plan.
     */
    List<ReservedTime> getReservedTimesForTermId(String requestedLearningPlanId, String termId)
            throws PermissionDeniedException;

    /**
     * Add a new reserved time on the current learning plan.
     *
     * @param reservedTime The reserved time to add.
     */
    ReservedTime createReservedTime(String requestedLearningPlanId, ReservedTime reservedTime)
            throws PermissionDeniedException;

    /**
     * Add a new reserved time on the current learning plan.
     *
     * @param reservedTime The reserved time to add.
     */
    ReservedTime updateReservedTime(String requestedLearningPlanId, ReservedTime reservedTime)
            throws PermissionDeniedException;

    /**
     * Add a new reserved time on the current learning plan.
     *
     * @param reservedTimeId The ID of the reserved time to delete.
     */
    void deleteReservedTime(String requestedLearningPlanId, String reservedTimeId)
            throws PermissionDeniedException;

    /**
     * Get saved schedules related to the current learning plan.
     *
     * @param requestedLearningPlanId See {@link #getLearningPlan(String)}.
     * @return The saved schedules related to the current learning plan.
     */
    List<PossibleScheduleOption> getSchedules(String requestedLearningPlanId)
            throws PermissionDeniedException;

    /**
     * Get saved schedules related to the current learning plan and given term.
     *
     * @param requestedLearningPlanId See {@link #getLearningPlan(String)}.
     * @param termId
     * @return The saved schedules related to the current learning plan and term.
     */
    List<PossibleScheduleOption> getSchedulesForTerm(String requestedLearningPlanId, String termId)
            throws PermissionDeniedException;

    /**
     * Add a new reserved time on the current learning plan.
     *
     * @param requestedLearningPlanId The reserved time to add.
     */
    PossibleScheduleOption createSchedule(String requestedLearningPlanId,
                                          PossibleScheduleOption schedule) throws PermissionDeniedException;

    /**
     * Add a new saved schedule on the current learning plan.
     *
     * @param schedule The schedule to add.
     */
    void updateSchedule(String requestedLearningPlanId, PossibleScheduleOption schedule)
            throws PermissionDeniedException;

    /**
     * Add a new saved schedule on the current learning plan.
     *
     * @param scheduleId The ID of the schedule to delete.
     */
    void deleteSchedule(String requestedLearningPlanId, String scheduleId)
            throws PermissionDeniedException;

    /**
     * Get the initial shopping cart form.
     *
     * @return The initial shopping cart form.
     */
    ShoppingCartForm getInitialCartForm();

    /**
     * Get a populated activity option for a given term, course, and
     * registration code.
     *
     * @param termId   The term ID.
     * @param courseId The course ID.
     * @param courseCd The course CD.
     * @param regCode  The registration code.
     * @return A populated activity option for a given term, course, and
     * registration code. Returns null if data is missing.
     */
    ActivityOption getActivityOption(String termId, String courseId, String courseCd, String regCode, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData);

    /**
     * Get a CampusCode from the Course attributes.
     *
     * @param course
     * @return Campus code from the course attributes if present other wise null.
     */
    String getCampusCode(Course course);

    /**
     * Get the appropriate ScheduleBuilder for this strategy
     *
     * @param term
     * @param courseOptions
     * @param reservedTimes
     * @param savedSchedules
     * @param buildFilters
     * @return Campus code from the course attributes if present other wise null.
     */
    ScheduleBuilder getScheduleBuilder(Term term, List<CourseOption> courseOptions,
                                       List<ReservedTime> reservedTimes, List<PossibleScheduleOption> savedSchedules, ScheduleBuildFilters buildFilters);

    /**
     * coalesce the sections for lowest level of activity for a course (primary if there are no secondaries,
     * secondaries if there are no tertiaries, etc)
     *
     * @param co the courseOption to have sections coalesced
     */
    void coalesceLeafActivities(CourseOption co);


    /**
     * Used to populate enrollment information for activity Option from activityOffering display info and enrollment data
     *
     * @param activityOption
     * @param aodi
     * @param enrollmentData
     */
    void populateEnrollmentInfo(ActivityOptionInfo activityOption, ActivityOfferingDisplayInfo aodi, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData);

    /**
     * Reduces number of calls made to Course offering service for each activityOption for a course.
     * Gets the current activity options for each course in activityOptions.
     * Eg: CHEM 321 A, CHEM 321 B, COM 202 A, COM 202 B
     * then the calls that are made to CourseOfferingService would be only two to get the activityOptions instead of four.
     * Call 1 to get CHEM activityOptions and Call 2 to get COM activityOptions
     *
     * @param activityOptions
     * @return
     */
    Map<String, Map<String, ActivityOption>> getActivityOptionsForCoursesByActivities(List<ActivityOption> activityOptions, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData, Map<String, String> plannedActivities);
}
