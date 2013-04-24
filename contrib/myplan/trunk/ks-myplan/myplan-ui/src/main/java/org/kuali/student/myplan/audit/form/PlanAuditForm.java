package org.kuali.student.myplan.audit.form;

import org.kuali.student.myplan.audit.dataobject.CourseItem;
import org.kuali.student.myplan.audit.dataobject.MessyItemDataObject;

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

    private List<MessyItemDataObject> messyItems;

    public String getLastPlannedTerm() {
        return lastPlannedTerm;
    }

    public void setLastPlannedTerm(String lastPlannedTerm) {
        this.lastPlannedTerm = lastPlannedTerm;
    }

    public List<CourseItem> getCleanList() {
        return cleanList;
    }

    public void setCleanList(List<CourseItem> cleanList) {
        this.cleanList = cleanList;
    }

    public List<MessyItemDataObject> getMessyItems() {
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
