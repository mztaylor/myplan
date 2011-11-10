package edu.uw.myplan.trng.course.dataobject;

/**
 * Created by IntelliJ IDEA.
 * User: kmuthu
 * Date: 11/3/11
 * Time: 11:08 AM
 * To change this template use File | Settings | File Templates.
 */
public class CourseSearchItem {

    private String code;

    private String courseName;

    private String credit  = " ";

    private String scheduledTime  = " ";

    private String genEduReq  = " ";

    private String status = "--";

    private String level = "--";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        this.scheduledTime = scheduledTime;
    }

    public String getGenEduReq() {
        return genEduReq;
    }

    public void setGenEduReq(String genEduReq) {
        this.genEduReq = genEduReq;
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }
}

