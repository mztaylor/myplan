package org.kuali.student.myplan.course.dataobject;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 10/31/13
 * Time: 3:15 PM
 * To change this template use File | Settings | File Templates.
 */
public class SyllabusItem {
    private String description;
    private String subject;
    private String number;
    private String title;
    private String credit;
    private String year;
    private String term;
    private String activityCode;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getActivityCode() {
        return activityCode;
    }

    public void setActivityCode(String activityCode) {
        this.activityCode = activityCode;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }
}
