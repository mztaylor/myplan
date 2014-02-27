package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildFilters;

import java.util.List;

/**
 * Created by hemanthg on 2/26/14.
 */
public interface ScheduleForm {

    /**
     * Get the term for which to build a schedule.
     *
     * @return The term for which to build a schedule.
     */
    Term getTerm();


    /**
     * Get the requested learning plan ID.
     *
     * @return The requested learning plan ID.
     */
    String getRequestedLearningPlanId();


    /**
     * Get the reserved time options.
     *
     * @return The reserved time options.
     */
    List<ReservedTime> getReservedTimes();


    /**
     * If a possible schedule has any activityOffering which is offered in weekend
     *
     * @return true if weekend exists otherwise false
     */
    boolean isWeekend();


    /**
     * Gets the min time the calendar has to show on UI
     *
     * @return Min date to show
     */
    long getMinTime();

    /**
     * Gets the max time the calendar has to show on UI
     *
     * @return Max date to show
     */
    long getMaxTime();


    /**
     * If any possible schedules has any activityOffering in which the meeting time is TBD
     *
     * @return true if TBD exists otherwise false
     */
    boolean isTbd();


    /**
     * Possible schedule build filters.
     * Closed, Enrollment restriction, overlapped
     *
     * @return ScheduleBuildFilters object with filter properties populated
     */
    ScheduleBuildFilters getBuildFilters();


    /**
     * Determine if closed activity offerings should be included in building
     * schedules.
     *
     * @return True if closed activity offerings should be included, false if
     * not.
     */
    boolean isIncludeClosed();

    /**
     * Get the index of a reserved time to remove from the list.
     *
     * @return The index of a reserved time to remove from the list.
     */
    Integer getRemoveReserved();


    /**
     * Reset the form to its initial state.
     */
    void reset();


    /**
     * Term Id for which the SB is required
     *
     * @return term Id
     */
    String getTermId();


}
