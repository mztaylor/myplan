package org.kuali.student.myplan.schedulebuilder.dto;

import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SecondaryActivityOptionsInfo", propOrder = { "index",
		"uniqueId", "activityUniqueId", "activityTypeDescription", "enrollmentGroup",
		"activityOptions", "_futureElements" })
public class SecondaryActivityOptionsInfo implements SecondaryActivityOptions,
		Serializable {

	private static final long serialVersionUID = -6831820824316403614L;

	@XmlAttribute
	private int index;

	@XmlAttribute
	private String uniqueId;

	@XmlAttribute
	private String activityUniqueId;

	@XmlAttribute
	private String activityTypeDescription;

	@XmlAttribute
	private boolean enrollmentGroup;

	@XmlElement
	private List<ActivityOptionInfo> activityOptions;

	@XmlAnyElement
	private List<?> _futureElements;

	public SecondaryActivityOptionsInfo() {
	}

	public SecondaryActivityOptionsInfo(SecondaryActivityOptions copy) {
		uniqueId = copy.getUniqueId();
		activityUniqueId = copy.getActivityUniqueId();
		activityTypeDescription = copy.getActivityTypeDescription();
		enrollmentGroup = copy.isEnrollmentGroup();
		setActivityOptions(copy.getActivityOptions());
	}

	@Override
	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public String getActivityUniqueId() {
		return activityUniqueId;
	}

	public void setActivityUniqueId(String activityUniqueId) {
		this.activityUniqueId = activityUniqueId;
	}

	@Override
	public String getActivityTypeDescription() {
		return activityTypeDescription;
	}

	public void setActivityTypeDescription(String activityTypeDescription) {
		this.activityTypeDescription = activityTypeDescription;
	}

	@Override
	public int getActivityCount(boolean includeClosed) {
		if (includeClosed)
			return activityOptions == null ? 0 : activityOptions.size();
		int c = 0;
		if (activityOptions != null)
			for (ActivityOption ao : activityOptions)
				if (ao.isLockedIn() || includeClosed || !ao.isClosed())
					c++;
		return c;
	}

	@Override
	public int getSelectedActivityCount(boolean includeClosed) {
		int c = 0;
		if (activityOptions != null)
			for (ActivityOption ao : activityOptions)
				if (ao.isLockedIn() || ((includeClosed || !ao.isClosed()) && ao.isSelected()))
					c++;
		return c;
	}

	public boolean isEnrollmentGroup() {
		return enrollmentGroup;
	}

	public void setEnrollmentGroup(boolean enrollmentGroup) {
		this.enrollmentGroup = enrollmentGroup;
	}

	@Override
	public List<ActivityOption> getActivityOptions() {
		return activityOptions == null ? Collections
				.<ActivityOption> emptyList() : Collections
				.<ActivityOption> unmodifiableList(activityOptions);
	}


	public List<ActivityOption> getSelectedSecondaryActivityOptions() {
         if (activityOptions == null) {
            return Collections.<ActivityOption> emptyList();
        }

        List<ActivityOption> secondaryActivities = new ArrayList<ActivityOption>();
        for (ActivityOption thisSecondary : this.getActivityOptions()) {
            if (thisSecondary.isSelected()) {
                secondaryActivities.add(thisSecondary);
            }
        }

        return secondaryActivities;
	}


	public void setActivityOptions(
			List<? extends ActivityOption> secondaryOptions) {
		if (secondaryOptions != null) {
			List<ActivityOptionInfo> meetingTimes = new java.util.ArrayList<ActivityOptionInfo>(
					secondaryOptions.size());
			for (ActivityOption secondaryOption : secondaryOptions) {
				meetingTimes.add(new ActivityOptionInfo(secondaryOption));
			}
			this.activityOptions = meetingTimes;
		} else {
			this.activityOptions = null;
		}
	}

	@Override
	public String toString() {
		return "SecondaryActivityOptionsInfo [activityTypeDescription="
				+ activityTypeDescription + ", enrollmentGroup="
				+ enrollmentGroup + ", activityOptions=" + activityOptions
				+ "]";
	}

}
