package org.kuali.student.myplan.schedulebuilder.infc;

import java.util.List;

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

    /**
     * Determine if the option has been selected for inclusion in other institute possible schedules.
     *
     * @return True if the option has been selected, false if not.
     */
    boolean isShowOtherInstitutes();

    /**
     * True if the reserved times need to be excluded as part of check for time conflicts.
     *
     * @return
     */
    boolean isExcludeReservedTimes();


    /**
     * Populated when no possible schedule results are returned because of above filters.
     *
     * @return List of error codes
     */
    List<String> getZeroResultsReasons();

    /**
     * Populated when no possible schedule results are returned because of the class data itself.
     *
     * @return List of error codes
     */
    List<String> getResultsNotPossibleReasons();


}
