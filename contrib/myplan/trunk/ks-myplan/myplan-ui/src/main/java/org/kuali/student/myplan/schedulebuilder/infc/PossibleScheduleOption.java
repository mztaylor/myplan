package org.kuali.student.myplan.schedulebuilder.infc;

import org.kuali.student.r2.common.infc.HasId;
import org.kuali.student.r2.common.infc.RichText;

import java.util.List;

/**
 * Represents a possible schedule option.
 *
 * @author Mark Fyffe <mwfyffe@iu.edu>
 * @version 1.1
 */
public interface PossibleScheduleOption extends HasId, ScheduleBuildOption {

    /**
     * Get the ID of the term this is a possible schedule option for.
     *
     * @return The id of the term
     */
    String getTermId();

    /**
     * Describe this possible schedule option as HTML.
     * <p/>
     * <p>
     * The method is used to dynamically refresh the possible schedule list on
     * the front end using JSON rather than KRAD rendering.
     * </p>
     *
     * @return An HTML description of this possible schedule option.
     */
    RichText getDescription();

    /**
     * Get the activity options associated with this possible schedule.
     *
     * @return The activity options associated with this possible schedule.
     */
    List<ActivityOption> getActivityOptions();

    /**
     * This is to return this PossibleScheduleOption.
     * Only used in property editor purpose()
     * TODO: Remove this once KULRICE-9735 is fixed.
     *
     * @return This possibleScheduleOption Object
     */
    PossibleScheduleOption getSchedule();

    /**
     * Get events for this possible schedule
     *
     * @return This possibleScheduleOption events
     */
    String getEvent();


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
     * If a possible schedule has any activityOffering in which the meeting time is TBD
     *
     * @return true if TBD exists otherwise false
     */
    boolean isTbd();

}
