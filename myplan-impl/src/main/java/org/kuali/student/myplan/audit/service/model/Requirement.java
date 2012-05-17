package org.kuali.student.myplan.audit.service.model;

import java.util.ArrayList;

public class Requirement {

    // complete, inprogress
    public String status = "XX complete XX";
    public String caption = "XX requirement caption XX";
    public Count count;
    public GPA gpa;
    public Credits credits;

//    public String info = null;

    private ArrayList<Subrequirement> subrequirementList = new ArrayList<Subrequirement>();

    public ArrayList<Subrequirement> getSubrequirementList() { return subrequirementList; }
    public void addSubrequirement(Subrequirement subrequirement) { subrequirementList.add(subrequirement); }

    public boolean getShowTotal() {
        return getSubrequirementList().size() != 1;
    }

    public String getStatus() { return status; }

    public void setStatus( String status ) { this.status = status; }
    public boolean notComplete() {
        return !"C".equals( status );
    }

    public String getCaption() { return caption; }
    public void setCaption( String caption ) { this.caption = caption; }
    public boolean hasCount() { return count != null; }
    public boolean hasCountZeroEarned() { return count.getEarned() == 0; }

    public boolean hasCountZeroNeeds() {
        return count.getNeeds() == 0;
    }

    public Count getCount() { return count; }
    public boolean hasGPA() { return gpa != null; }
    public GPA getGpa() { return gpa; }
    public boolean hasCredits() { return credits != null; }
    public boolean hasCreditsZeroEarned()
    {
        return credits.earned < 0.0001f;
    }

    public boolean hasCreditsZeroNeeds() {
        return credits.needs < 0.0001f;
    }

    public Credits getCredits() { return credits; }
//    public boolean hasInfo() {
//        return info != null;
//    }
//
//    public String getInfo() {
//        return info;
//    }

    public void setCredits(Credits credits) {
        this.credits = credits;
    }

    public void setGPA(GPA gpa) {
        this.gpa = gpa;
    }

    public void setCount(Count count) {
        this.count = count;
    }
}
