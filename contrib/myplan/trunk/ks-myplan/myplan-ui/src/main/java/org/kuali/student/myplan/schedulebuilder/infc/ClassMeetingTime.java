package org.kuali.student.myplan.schedulebuilder.infc;

import org.kuali.student.r2.common.infc.HasId;

/**
 * Represents a class meeting time associated with an activity offering.
 * 
 * @author Mark Fyffe <mwfyffe@indiana.edu>
 * @version 0.7
 */
public interface ClassMeetingTime extends ScheduleBuildEvent, HasId, HasUniqueId {

	/**
	 * Get the name of the instructor for the class meeting.
	 * 
	 * @return The name of the instrcutor for this class meeting.
	 */
	String getInstructorName();

	/**
	 * Determine if the class meeting should appear in the TBA row.
	 * 
	 * @return True if the class meeting should appear in the TBA row, false if
	 *         meeting times should be followed.
	 */
	boolean isTba();

	/**
	 * Determine if the class meeting has an arranged location.
	 * 
	 * @return True if the class meeting has an arranged location, false if the
	 *         location is to be arranged.
	 */
	boolean isArranged();

	/**
	 * Get the location of the class meeting.
	 * 
	 * @return The location of the class meeting.
	 */
	String getLocation();

    /**
	 * Get the location of the class meeting.
	 *
	 * @return The location of the class meeting.
	 */
	String getBuilding();

    /**
	 * Get the location of the class meeting.
	 *
	 * @return The location of the class meeting.
	 */
	String getCampus();

}
