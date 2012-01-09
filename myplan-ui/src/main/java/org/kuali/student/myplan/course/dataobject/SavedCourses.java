package org.kuali.student.myplan.course.dataobject;

import java.util.ArrayList;
import java.util.List;

public class SavedCourses {

    public SavedCourses() {}


    private String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID( String userID ) {
        this.userID = userID;
    }

    private List<SavedCoursesItem> savedCoursesList = new ArrayList<SavedCoursesItem>();

    public List<SavedCoursesItem> getSavedCoursesList() {
        return savedCoursesList;
    }

    public void setSavedCoursesList( List<SavedCoursesItem> savedCoursesList ) {
        this.savedCoursesList = savedCoursesList;
    }

    public void addSavedCourses( SavedCoursesItem item ) {
        savedCoursesList.add( item );
    }

}
