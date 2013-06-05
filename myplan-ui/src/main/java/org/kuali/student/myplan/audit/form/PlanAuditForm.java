package org.kuali.student.myplan.audit.form;

import org.kuali.student.myplan.audit.dataobject.CourseItem;
import org.kuali.student.myplan.audit.dataobject.MessyItemDataObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/9/13
 * Time: 1:28 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlanAuditForm extends DegreeAuditForm {
    private String lastPlannedTerm;

    private boolean studentChoiceRequired;

    private List<CourseItem> cleanList;

    private List<CourseItem> ignoreList;

    private List<MessyItemDataObject> messyItems;

    public String getLastPlannedTerm() {
        return lastPlannedTerm;
    }

    public void setLastPlannedTerm(String lastPlannedTerm) {
        this.lastPlannedTerm = lastPlannedTerm;
    }

    public List<CourseItem> getCleanList() {
        if (cleanList == null) {
            cleanList = new ArrayList<CourseItem>();
        }
        return cleanList;
    }

    public void setCleanList(List<CourseItem> cleanList) {
        this.cleanList = cleanList;
    }

    public List<CourseItem> getIgnoreList() {
        if (ignoreList == null) {
            ignoreList = new ArrayList<CourseItem>();
        }
        return ignoreList;
    }

    public void setIgnoreList(List<CourseItem> ignoreList) {
        this.ignoreList = ignoreList;
    }

    public List<MessyItemDataObject> getMessyItems() {
        if(messyItems == null) {
            messyItems = new ArrayList<MessyItemDataObject>();
        }
        return messyItems;
    }

    public void setMessyItems(List<MessyItemDataObject> messyItems) {
        this.messyItems = messyItems;
    }

    public boolean isStudentChoiceRequired() {
        return studentChoiceRequired;
    }

    public void setStudentChoiceRequired(boolean studentChoiceRequired) {
        this.studentChoiceRequired = studentChoiceRequired;
    }

}
