package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.kuali.student.r2.lum.course.infc.Course;

import java.util.List;

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
    List<CourseOption> getCourseOptions(List<String> courseIds, String termId);

    /**
     * Load the course options to use as inputs for generating schedules.
     *
     * @param learningPlanId The learning plan ID.
     * @param termId         The term to get options for.
     * @return The course options to use as inputs for generating schedules.
     */
    List<CourseOption> getCourseOptions(String learningPlanId, String termId);

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
    List<ReservedTime> getReservedTimes(String requestedLearningPlanId)
            throws PermissionDeniedException;

    /**
     * Add a new reserved time on the current learning plan.
     *
     * @param reservedTime The reserved time to add.
     */
    void createReservedTime(String requestedLearningPlanId, ReservedTime reservedTime)
            throws PermissionDeniedException;

    /**
     * Add a new reserved time on the current learning plan.
     *
     * @param reservedTime The reserved time to add.
     */
    void updateReservedTime(String requestedLearningPlanId, ReservedTime reservedTime)
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
     * @param regCode  The registration code.
     * @return A populated activity option for a given term, course, and
     * registration code. Returns null if data is missing.
     */
    ActivityOption getActivityOption(String termId, String courseId, String regCode);

    /**
     * Get a CampusCode from the Course attributes.
     *
     * @param course
     * @return Campus code from the course attributes if present other wise null.
     */
    String getCampusCode(Course course);

}
