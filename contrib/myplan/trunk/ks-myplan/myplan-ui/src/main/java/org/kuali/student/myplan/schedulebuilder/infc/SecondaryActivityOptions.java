package org.kuali.student.myplan.schedulebuilder.infc;

import java.io.Serializable;
import java.util.List;

/**
 * Groups secondary activities by activity type.
 * 
 * @author Mark Fyffe <mwfyffe@indiana.edu>
 * @version 0.7
 */
public interface SecondaryActivityOptions extends HasUniqueId, Serializable {

	/**
	 * Get the index number relative to the primary activity option.
	 * 
	 * @return The index number relative to the primary option.
	 */
	int getIndex();

	/**
	 * Get the unique ID of the primary activity option above this secondary
	 * options group.
	 * 
	 * @return The unique ID of the primary activity option above this secondary
	 *         options group.
	 */
	String getActivityUniqueId();

	/**
	 * Get the activity offering type description.
	 * 
	 * @return The activity offering type description.
	 */
	String getActivityTypeDescription();

	/**
	 * Get the number of activity options of this type.
	 * 
	 * @param includeClosed
	 *            True to include closed sections, false for only open sections.
	 * @return The number of activity options of this type.
	 */
	int getActivityCount(boolean includeClosed);

	/**
	 * Get the number of selected activity options of this type.
	 * 
	 * @param includeClosed
	 *            True to include closed sections, false for only open sections.
	 * @return The number of activity options of this type.
	 */
	int getSelectedActivityCount(boolean includeClosed);

	/**
	 * Determine if the secondary options represent an enrollment group.
	 * 
	 * @return True if the secondary options represent an enrollment group
	 *         (primary includes all), false if the secondary options are
	 *         (primary indicates one or more).
	 */
	boolean isEnrollmentGroup();

	/**
	 * Get the secondary activity options.
	 * 
	 * @return The secondary activity options.
	 */
	List<ActivityOption> getActivityOptions();

}
