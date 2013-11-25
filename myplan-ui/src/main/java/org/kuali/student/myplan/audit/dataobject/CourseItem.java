package org.kuali.student.myplan.audit.dataobject;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/13/13
 * Time: 8:35 PM
 * To change this template use File | Settings | File Templates.
 */
public class CourseItem {
    private String courseId;
    private String courseCode;
    private String subject;
    private String number;
    private String sectionCode;
    private String secondaryActivityCode;
    private String atpId;
    private String credit;
    private String title;

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getAtpId() {
        return atpId;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSecondaryActivityCode() {
        return secondaryActivityCode;
    }

    public void setSecondaryActivityCode(String secondaryActivityCode) {
        this.secondaryActivityCode = secondaryActivityCode;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}
