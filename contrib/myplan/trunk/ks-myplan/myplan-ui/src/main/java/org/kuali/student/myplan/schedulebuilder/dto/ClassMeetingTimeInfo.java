package org.kuali.student.myplan.schedulebuilder.dto;

import org.kuali.student.myplan.schedulebuilder.infc.ClassMeetingTime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClassMeetingTimeInfo", propOrder = { "id", "uniqueId",
		"instructorName", "location", "building", "campus", "arranged" })
public class ClassMeetingTimeInfo extends ScheduleBuildEventInfo implements
        ClassMeetingTime {

	private static final long serialVersionUID = 137433871719708085L;

	@XmlAttribute
	private String id;

	@XmlAttribute
	private String uniqueId;

	@XmlAttribute
	private String instructorName;

	@XmlAttribute
	private String location;

    @XmlAttribute
	private String building;

    @XmlAttribute
	private String campus;

	@XmlAttribute
	private boolean arranged;

	@XmlAttribute
	private boolean tba;

	public ClassMeetingTimeInfo() {
	}

	public ClassMeetingTimeInfo(ClassMeetingTime copy) {
		super(copy);
		id = copy.getId();
		uniqueId = copy.getUniqueId();
		instructorName = copy.getInstructorName();
		location = copy.getLocation();
		building = copy.getBuilding();
		campus = copy.getCampus();
		arranged = copy.isArranged();
		tba = copy.isTba();
	}

	@Override
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getUniqueId() {
		return uniqueId;
	}

	public void setUniqueId(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	@Override
	public String getInstructorName() {
		return instructorName;
	}

	public void setInstructorName(String instructorName) {
		this.instructorName = instructorName;
	}

	@Override
	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

    @Override
    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    @Override
    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }

    @Override
	public boolean isArranged() {
		return arranged;
	}

	public void setArranged(boolean arranged) {
		this.arranged = arranged;
	}

	@Override
	public boolean isTba() {
		return tba;
	}

	public void setTba(boolean tba) {
		this.tba = tba;
	}

	@Override
	public String toString() {
		return "ClassMeetingTimeInfo [id=" + id + ", uniqueId=" + uniqueId
				+ ", instructorName=" + instructorName + ", location="
				+ location + ", building="
				+ building + ", campus="
				+ campus + ", getDescription()=" + getDescription()
				+ ", isAllDay()=" + isAllDay() + ", getDaysAndTimes()="
				+ getDaysAndTimes() + ", getStartDate()=" + getStartDate()
				+ ", isSunday()=" + isSunday() + ", isMonday()=" + isMonday()
				+ ", isTuesday()=" + isTuesday() + ", isWednesday()="
				+ isWednesday() + ", isThursday()=" + isThursday()
				+ ", isFriday()=" + isFriday() + ", isSaturday()="
				+ isSaturday() + ", getUntilDate()=" + getUntilDate() + "]";
	}

}
