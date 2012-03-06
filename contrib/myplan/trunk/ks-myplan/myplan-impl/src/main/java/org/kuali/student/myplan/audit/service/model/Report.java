package org.kuali.student.myplan.audit.service.model;

import java.util.ArrayList;

public class Report {
    private String webTitle;

    String degreeProgram = "XX Bachelor of Arts in Communication XX";
    String studentName = "XX Jana Winsfeld XX";
    String datePrepared = "XX Feb. 21, 2012 10:18 AM XX";
    String entryDateUW = "XX Autumn 2008 XX";
    String entryDateProgram = "XX Autumn 2010 XX";

    Requirement summaryUW;
    Requirement summaryProgram;

    public String getDegreeProgram() { return degreeProgram; }

    public void setDegreeProgram(String degreeProgram) {
        this.degreeProgram = degreeProgram;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName( String studentName ) { this.studentName = studentName; }

    public String getDatePrepared() {
        return datePrepared;
    }

    public void setDatePrepared( String datePrepared ) {
        this.datePrepared = datePrepared;
    }

    public String getEntryDateUW() { return entryDateUW; }

    public String getEntryDateProgram() {
        return entryDateProgram;
    }

    public Requirement getSummaryUW() { return summaryUW; }
    public Requirement getSummaryProgram() { return summaryProgram; }

    public ArrayList<Section> sectionList = new ArrayList<Section>();

    public void addSection( Section section ) {
        sectionList.add( section );
    }

    public ArrayList<Section> getSectionList() { return sectionList; }

    public String getWebTitle() { return webTitle; }
    public void setWebTitle(String webTitle) {
        this.webTitle = webTitle;
    }

}
