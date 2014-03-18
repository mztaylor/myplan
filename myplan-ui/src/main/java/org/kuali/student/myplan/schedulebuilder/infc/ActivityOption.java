package org.kuali.student.myplan.schedulebuilder.infc;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * Represents an activity offering option as an input for building a schedule.
 * <p/>
 * <p>
 * Natural ordering implied by the {@link Comparable} interface is to sort open
 * sections above closed sections, then with the highest number of open seats
 * listed on top.
 * </p>
 *
 * @author Mark Fyffe <mwfyffe@indiana.edu>
 * @version 1.1
 */
public interface ActivityOption extends ScheduleBuildOption, HasUniqueId, Comparable<ActivityOption>, Serializable {

    /**
     * Get the unique ID of the course option this activity refers to.
     *
     * @return The unique ID of the course option this activity refers to.
     */
    String getParentUniqueId();

    /**
     * Get the index number of the course options this activity is a member of.
     *
     * @return The index number of the course options this activity is a member
     * of.
     */
    int getCourseIndex();

    /**
     * Get the index number of the secondary options this activity is a member
     * of.
     *
     * @return The index number of the secondary options this activity is a
     * member of.
     */
    int getParentIndex();

    /**
     * Get the course ID.
     *
     * @return The course ID.
     */
    String getCourseId();

    /**
     * Get the course CD.
     *
     * @return The course CD.
     */
    String getCourseCd();

    /**
     * Get the course TITLE.
     *
     * @return The course TITLE.
     */
    String getCourseTitle();

    /**
     * Get the activity offering ID.
     *
     * @return The activity offering ID.
     */
    String getActivityOfferingId();

    /**
     * Get the activity type description.
     *
     * @return The activity type description.
     */
    String getActivityTypeDescription();

    /**
     * Get activity code.
     *
     * @return The activity code.
     */
    String getActivityCode();

    /**
     * Get registration code.
     *
     * @return The registration code.
     */
    String getRegistrationCode();

    /**
     * Get the course offering code.
     *
     * @return The course offering code.
     */
    String getCourseOfferingCode();

    /**
     * Get the name of the activity.
     *
     * @return The name of the activity
     */
    String getActivityName();

    /**
     * Get a description of the academic session.
     *
     * @return A description of the academic session.
     */
    String getAcademicSessionDescr();

    /**
     * Determine if the course is locked in.
     *
     * @return True if the course is locked in.
     */
    boolean isCourseLockedIn();

    /**
     * Determine if this option represents a primary activity offering.
     *
     * @return True if the option represents a primary offering, false if
     * secondary.
     */
    boolean isPrimary();

    /**
     * Determines if activity is not primary then this is parent ActivityId(primary activityId)
     *
     * @return parent activity Id for a secondary Activity
     * secondary.
     */
    String getParentActivityId();

    /**
     * Get the minimum number of credit hours for this activity.
     *
     * @return The minimum number of credit hours for this activity.
     */
    BigDecimal getMinCredits();

    /**
     * Get the maximum number of credit hours for this activity.
     *
     * @return The maximum number of credit hours for this activity.
     */
    BigDecimal getMaxCredits();

    /**
     * Determine if the class this meeting is associated with is closed for
     * registration.
     *
     * @return True if the class this meeting is associated with is closed for
     * registration, false if not.
     */
    boolean isClosed();

    /**
     * Get the number of open seats available in the class.
     *
     * @return The number of open seats available.
     */
    int getOpenSeats();

    /**
     * Get the number of open seats available in the class.
     *
     * @return The number of open seats available.
     */
    int getFilledSeats();

    /**
     * Get the total number of seats available for the class.
     *
     * @return The total number of seats available for the class.
     */
    int getTotalSeats();

    /**
     * Determine if permission is required for this class.
     *
     * @return True if permission is required for the class.
     */
    boolean isRequiresPermission();

    /**
     * Get additional notes related to this activity option.
     *
     * @return Additional notes related to this activity option.
     */
    List<String> getNotes();

    /**
     * Get the class meeting times for the activity offering.
     *
     * @return The class meeting times for the activity offering.
     */
    List<ClassMeetingTime> getClassMeetingTimes();

    /**
     * Get termId for activity.
     *
     * @return The TermId.
     */
    String getTermId();

    /**
     * Determine if at least one of the secondary options represents an
     * enrollment group for a primary activity option.
     *
     * @return True if at least one of the secondary options represents an
     * enrollment group for a primary activity option.
     */
    boolean isEnrollmentGroup();

    /**
     * Determine if at there are any enrollment restrictions for activity option.
     *
     * @return True if at enrollment restrictions is set tot true activity option.
     */
    boolean isEnrollmentRestriction();

    /**
     * Determine if at there are any duplicate enrollment allowed for activity option.
     *
     * @return True if at duplicate enrollment is allowed else false.
     */
    boolean isDuplicateEnrollmentAllowed();

    /**
     * Determine if ativity option is withdrawn.
     *
     * @return True if at activity is withdrawn else false.
     */
    boolean isWithdrawn();

    /**
     * Get the secondary options.
     *
     * @return The secondary options.
     */
    List<SecondaryActivityOptions> getSecondaryOptions();


    /**
     * Return alternate activity options that are of same type and meets on exact
     * same times
     *
     * @return
     */
    List<ActivityOption> getAlternateActivties();


    /**
     * Returns a PlanItemsId if the activity is planned
     *
     * @return
     */
    String getPlanItemId();

    /**
     * This is to return this ActivityOption.
     * Only used in property editor purpose()
     * TODO: Remove this once KULRICE-9735 is fixed.
     *
     * @return This activityOption Object
     */
    ActivityOption getActivity();

}
