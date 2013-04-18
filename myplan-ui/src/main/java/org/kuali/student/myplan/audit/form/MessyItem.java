package org.kuali.student.myplan.audit.form;

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
    private List<String> sections;
    private Set<String> credits;
    private String selectedSection;
    private String selectedCredit;


    public List<String> getSections() {
        if (sections == null) {
            sections = new ArrayList<String>();
        }
        return sections;
    }

    public void setSections(List<String> sections) {
        this.sections = sections;
    }

    public Set<String> getCredits() {
        if (credits == null) {
            credits = new HashSet<String>();
        }
        return credits;
    }

    public void setCredits(Set<String> credits) {
        this.credits = credits;
    }

    public String getSelectedSection() {
        return selectedSection;
    }

    public void setSelectedSection(String selectedSection) {
        this.selectedSection = selectedSection;
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

    /*Used to set the MessyItem to request and return back a AtpId. Used in the planAuditMessyItems to build the drop downs*/
    public String getAtpAndAddMessyItemToReq() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        request.getSession().setAttribute("studentMessyItem", this);
        return atpId;
    }

}
