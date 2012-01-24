package org.kuali.student.myplan.course.dataobject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SavedCoursesItem {

    private String id;
    private String courseID;
    private String code;
    private String title;
    private String credit;
    private List<String> scheduleList = new ArrayList<String>();
    private List<String> prereqList = new ArrayList<String>();
    private Date added;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCourseID() {
        return courseID;
    }

    public void setCourseID( String courseID ) {
        this.courseID = courseID;
    }

    public String getCode() {
        return code;
    }

    public void setCode( String code ) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle( String title ) {
        this.title = title;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit( String credit ) {
        this.credit = credit;
    }

    public List<String> getScheduleList() {
        return this.scheduleList;
    }

    public void setScheduleList(List<String> scheduleList) {
        this.scheduleList = scheduleList;
    }

    public void addSchedule( String term ) {
        scheduleList.add( term );
    }

    public List<String> getPrereqList() {
        return prereqList;
    }

    public void setPrereqList( List<String> prereqList ) {
        this.prereqList = prereqList;
    }

    public void addPrereq( String prereq ) {
        prereqList.add( prereq );
    }

    public Date getAdded() {
        return added;
    }

    public void setAdded( Date added ) {
        this.added = added;
    }
}
