package org.kuali.student.myplan.audit.dataobject;

import org.kuali.student.myplan.course.util.PlanAuditMessyItems;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/13/13
 * Time: 8:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessyItem {
    private String atpId;
    private String courseCode;
    private String subject;
    private String number;
    private String courseId;
    private String versionIndependentId;
    private String courseTitle;
    private Set<String> credits;
    private String selectedCredit;

    public Set<String> getCredits() {
        if (credits == null) {
            credits = new HashSet<String>();
        }
        return credits;
    }

    public void setCredits(Set<String> credits) {
        this.credits = credits;
    }

    public String getSelectedCredit() {
        return selectedCredit;
    }

    public void setSelectedCredit(String selectedCredit) {
        this.selectedCredit = selectedCredit;
    }

    public String getAtpId() {
        return atpId;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }

    public String getCourseCode() {
        return courseCode;
    }

    public void setCourseCode(String courseCode) {
        this.courseCode = courseCode;
    }

    public String getCourseTitle() {
        return courseTitle;
    }

    public void setCourseTitle(String courseTitle) {
        this.courseTitle = courseTitle;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getVersionIndependentId() {
        return versionIndependentId;
    }

    public void setVersionIndependentId(String versionIndependentId) {
        this.versionIndependentId = versionIndependentId;
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

    /*Used to set the MessyItem to request and return back a AtpId. Used in the planAuditMessyItems to build the drop downs*/
    public String getAtpAndAddMessyItemToReq() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.setAttribute("studentMessyItem", this);
        return atpId;
    }

}
