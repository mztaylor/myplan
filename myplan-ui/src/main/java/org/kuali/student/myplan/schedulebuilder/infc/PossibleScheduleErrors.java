package org.kuali.student.myplan.schedulebuilder.infc;

import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 3/27/2014.
 */
public interface PossibleScheduleErrors {

    /**
     * Holds the error type for the possible schedule activities
     * Eg: Passive or Modal
     *
     * @return
     */
    public String getErrorType();

    /**
     * Following holds data as Map<CourseCd, Map<ErrorReason, List<ActivityOfferingId>>>
     * Where ErrorReason is one of Withdrawn, time conflicts, meeting time changes etc..
     *
     * @return
     */
    public Map<String, Map<String, List<String>>> getInvalidOptions();


    /**
     * Holds the error message for the possible schedule activities
     *
     * @return
     */
    public String getErrorMessage();


}
