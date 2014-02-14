package org.kuali.student.myplan.schedulebuilder.infc;

/**
 * Created by hemanthg on 2/13/14.
 */
public interface ScheduleBuildFilters {

    /**
     * Determine if the option has been selected for inclusion in closed activities.
     *
     * @return True if the option has been selected, false if not.
     */
    boolean isShowClosed();


    /**
     * Determine if the option has been selected for inclusion in enrollment restriction activities.
     *
     * @return True if the option has been selected, false if not.
     */
    boolean isShowRestricted();


    /**
     * Determine if the option has been selected for inclusion in overlapped possible schedules.
     *
     * @return True if the option has been selected, false if not.
     */
    boolean isShowOverlapped();


}
