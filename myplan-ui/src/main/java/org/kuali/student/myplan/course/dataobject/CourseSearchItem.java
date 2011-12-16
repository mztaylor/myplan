package org.kuali.student.myplan.course.dataobject;

import org.springframework.util.StringUtils;

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

    public static final String EMPTY_RESULT_VALUE_KEY = "-";

    private String courseId;

    private String number;
    private String subject;
    private String code;
    private String level;
    private String courseName;
    private String credit;
    private float creditMin;
    private float creditMax;
    private CreditType creditType;
    private String scheduledTime = EMPTY_RESULT_VALUE_KEY;
    private String genEduReq = EMPTY_RESULT_VALUE_KEY;
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

    public String getNumber() {
        return number;
    }
    
    public void setNumber( String number ) {
        this.number = number;
    }
    
    // aka KSLU_CLU_IDENT.DIVISION
    public String getSubject() {
        return subject;
    }

    public void setSubject( String subject ) {
        this.subject = subject;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
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

    public float getCreditMin() {
        return creditMin;
    }

    public void setCreditMin(float creditMin) {
        this.creditMin = creditMin;
    }

    public float getCreditMax() {
        return creditMax;
    }

    public void setCreditMax(float creditMax) {
        this.creditMax = creditMax;
    }

    public enum CreditType { fixed, range, multiple, unknown };

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }

    public String getScheduledTime() {
        return scheduledTime;
    }

    public void setScheduledTime(String scheduledTime) {
        if(StringUtils.hasText(scheduledTime)) {
            this.scheduledTime = scheduledTime;
        }
    }

    public String getGenEduReq() {
        return genEduReq;
    }

    public void setGenEduReq(String genEduReq) {
        if(StringUtils.hasText(genEduReq)) {
            this.genEduReq = genEduReq;
        }
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