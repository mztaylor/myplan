package org.kuali.student.myplan.audit.service.model;

import java.util.ArrayList;

public class Subrequirement {

    // complete, inprogress
    public String status = "complete";
    public String caption = null;
    public Count count;
    public GPA gpa;
    public Credits credits;
    public String nolist;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCaption() {
        return caption;
    }

    public boolean hasCaption() {
        return caption != null;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public boolean hasCount() {
        return count != null;
    }

    public Count getCount() {
        return count;
    }

    public void setCount(Count count) {
        this.count = count;
    }

    public boolean hasGPA() {
        return gpa != null;
    }

    public GPA getGpa() {
        return gpa;
    }

    public boolean hasCredits() {
        if( credits == null ) return false;
        if( "0".equals( credits.getInprogress()) && "0".equals( credits.getNeeds() ) ) return false;
        return true;
    }

    public Credits getCredits() {
        return credits;
    }

    public void setCredits(Credits credits) {
        this.credits = credits;
    }

    public void setGPA(GPA gpa) {
        this.gpa = gpa;
    }

    public ArrayList<CourseTaken> courseTakenList = new ArrayList<CourseTaken>();

    public boolean hasCourseTakenList() {
        return courseTakenList.size() > 0;
    }

    public ArrayList<CourseTaken> getCourseTakenList() {
        return courseTakenList;
    }

    public void addCourseTaken(CourseTaken course) {
        courseTakenList.add(course);
    }

    public void setNolist( String nolist ) {
        this.nolist = nolist;
    }

}
