package org.kuali.student.myplan.course.dataobject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: kmuthu
 * Date: 11/3/11
 * Time: 11:08 AM
 *
 *  Wrapper for CourseInfo data.
 *
 */
public class CourseSearchItem {
    private String courseId;

    private String code;
    private String courseName;
    private String credit;
    private String scheduledTime;
    private String genEduReq;
    private String status;
    /* Facet keys used for filtering in the view. The value of the Map Entry isn't used. */
    private String curriculumFacetKey;

    private String courseLevelFacetKey;
    private String genEduReqFacetKey;
    private String timeScheduleFacetKey;
    private String creditsFacetKey;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

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

    public String getCurriculumFacetKey() {
        return curriculumFacetKey;
    }

    public void setCurriculumFacetKey(String curriculumFacetKey) {
        this.curriculumFacetKey = curriculumFacetKey;
    }

    public String getCourseLevelFacetKey() {
        return courseLevelFacetKey;
    }

    public void setCourseLevelFacetKey(String courseLevelFacetKey) {
        this.courseLevelFacetKey = courseLevelFacetKey;
    }

    public String getGenEduReqFacetKey() {
        return genEduReqFacetKey;
    }

    public void setGenEduReqFacetKey(String genEduReqFacetKey) {
        this.genEduReqFacetKey = genEduReqFacetKey;
    }

    public String getTimeScheduleFacetKey() {
        return timeScheduleFacetKey;
    }

    public void setTimeScheduleFacetKey(String timeScheduleFacetKey) {
        this.timeScheduleFacetKey = timeScheduleFacetKey;
    }

    public String getCreditsFacetKey() {
        return creditsFacetKey;
    }

    public void setCreditsFacetKey(String creditsFacetKey) {
        this.creditsFacetKey = creditsFacetKey;
    }
}