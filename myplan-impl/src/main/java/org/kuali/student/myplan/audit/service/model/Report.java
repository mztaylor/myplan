package org.kuali.student.myplan.audit.service.model;

import java.util.ArrayList;

public class Report {
    private String webTitle;

    String degreeProgram = "$$ Bachelor of Arts in Communication";
    String studentName = "$$ Jana Winsfeld";
    String datePrepared = "$$ Feb. 21, 2012 10:18 AM";
    String entryDateUW = "$$ Autumn 2008";
    String entryDateProgram = "$$ Autumn 2010";

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
