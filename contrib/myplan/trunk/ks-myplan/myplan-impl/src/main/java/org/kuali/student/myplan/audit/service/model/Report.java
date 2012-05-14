package org.kuali.student.myplan.audit.service.model;

import org.apache.regexp.RE;

import java.util.ArrayList;
import java.util.List;

public class Report {
    private String webTitle;

    String degreeProgram = "XX Bachelor of Arts in Communication XX";
//    String studentName = "XX Jana Winsfeld XX";
    String datePrepared = "XX Feb. 21, 2012 10:18 AM XX";
    String entryDateUW = "XX Autumn 2008 XX";
    String entryDateProgram = "XX Autumn 2010 XX";

    Requirement summaryUW;
    Requirement summaryProgram;

    public ArrayList<Section> sectionList = new ArrayList<Section>();
    private ArrayList<String> advisoryList = new ArrayList<String>();

    public String getDegreeProgram() { return degreeProgram; }

    public void setDegreeProgram(String degreeProgram) {
        this.degreeProgram = degreeProgram;
    }

//    public String getStudentName() {
//        return studentName;
//    }
//
//    public void setStudentName( String studentName ) { this.studentName = studentName; }

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


//    public void addSection( Section section ) {
//        sectionList.add( section );
//    }

    public Section newSection() {
        Section section = new Section();
        sectionList.add(section);
        return section;
    }

    public ArrayList<Section> getSectionList() {
        return sectionList;
    }


    public Requirement newRequirement()
    {
        Requirement requirement = new Requirement();
        Section section = null;
        if( sectionList.isEmpty() )
        {
            section = newSection();
            section.setCaption( "(Oops)" );
        }

        section = sectionList.get( sectionList.size() - 1 );
        section.addRequirement( requirement );
        return requirement;
    }



    public String getWebTitle() { return webTitle; }
    public void setWebTitle(String webTitle) {
        this.webTitle = webTitle;
    }


    public void addAdvisory( String advisory ) {
        advisoryList.add( advisory );
    }

    public List<String> getAdvisoryList() {
        return advisoryList;
    }

}
