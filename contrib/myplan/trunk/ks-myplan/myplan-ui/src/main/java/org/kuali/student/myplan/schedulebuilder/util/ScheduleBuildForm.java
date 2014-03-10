package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;

import java.util.List;

/**
 * Common interface for passing request attributes from the UI layer to service
 * layer.
 *
 * @author Mark Fyffe <mwfyffe@indiana.edu>
 * @version 0.7.1
 */
public interface ScheduleBuildForm extends ScheduleForm {

    /**
     * Get the course options the student is currently working with.
     *
     * @return The course options the student is currently working with.
     */
    List<CourseOption> getCourseOptions();

    /**
     * Get the list of possible schedule options.
     *
     * @return The list of possible schedule options.
     */
    List<PossibleScheduleOption> getPossibleScheduleOptions();

    /**
     * Get the saved schedule options.
     *
     * @return The saved schedule options.
     */
    List<PossibleScheduleOption> getSavedSchedules();

    /**
     * Determine if more schedule options based on the same criteria are
     * requested.
     *
     * @return True if more options based on the same criteria are requested,
     * false if the first set of options are requested.
     */
    boolean isMore();

    /**
     * Determine if more schedule options based on the current criteria are
     * available.
     *
     * @return True if more options are available beyond the first set, false if
     * the first set has all of the available options.
     */
    boolean hasMore();

    /**
     * Rebuild schedule options based on the most recent form data.
     */
    void buildSchedules();

    /**
     * Add the possible schedule option indicated by
     *
     * @return The ID of the saved schedule.
     */
    PossibleScheduleOption saveSchedule();

    /**
     *
     */
    String removeSchedule();

    /**
     * Json string which holds a map of activityOfferingId associated to a planItemId
     *
     * @return json object as string
     */
    String getPlannedActivities();

}
