package org.kuali.student.myplan.audit.service.model;

public class CourseTaken{
        String quarter="XX";
// aka subjectArea
private String dept = "XYZZY";
private String number = "000";


String description="Xxxxx xx Xxxxx";
float credits;

// IP, CR, 0.0,
String grade="X.X";
String cluid="XXXX";



public String getQuarter(){return quarter;}
public void setQuarter( String quarter ) {
        this.quarter = quarter;
        }

public String getDept() { return dept; }
public void setDept( String dept ) { this.dept = dept; }

public String getNumber() { return number; }
public void setNumber( String number ) { this.number = number; }

public String getDescription() { return description; }
public void setDescription( String description ) { this.description = description; }

public float getCredits(){return credits;}
public void setCredits( float credits ) {
        this.credits = credits;
        }

public String getGrade(){return grade;}
public void setGrade( String grade ) {
        this.grade = grade;
        }

}
