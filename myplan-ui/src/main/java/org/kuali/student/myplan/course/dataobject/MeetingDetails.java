package org.kuali.student.myplan.course.dataobject;


public class MeetingDetails {
	
	public final static String TO_BE_ARRANGED = "to be arranged";
	
	// eg MTWThF
	private String days = TO_BE_ARRANGED;
	// eg 10:30 AM - 11:20 AM
    private String time = "";
    private String building = TO_BE_ARRANGED;
    private String room = TO_BE_ARRANGED;
    private String campus = "";
    
    public String getDays() {
        return days;
    }

    public void setDays(String days) {
        this.days = days;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getBuilding() {
        return building;
    }

    public void setBuilding(String building) {
        this.building = building;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getCampus() {
        return campus;
    }

    public void setCampus(String campus) {
        this.campus = campus;
    }
}
