package org.kuali.student.myplan.audit.service.model;

public class Course {
    // aka subjectArea
    private String dept;
    private String number;

    String quarter = "SP99";
    String code = "MUSIC 331";
    String description = "History of Jazz";
    float credits;

    // IP, CR, 0.0,
    String grade = "5.5";
    String cluid = "123-ABC";

    public String getQuarter() { return quarter; }
    public String getCode() { return code; }
    public String getDescription() { return description; }
    public float getCredits() { return credits; }
    public String getGrade() { return grade; }
    public String getCluid() { return cluid; }

    public void setDept(String dept) {
        this.dept = dept;
    }

    public void setNumber(String number) {
        this.number = number;
    }
}
