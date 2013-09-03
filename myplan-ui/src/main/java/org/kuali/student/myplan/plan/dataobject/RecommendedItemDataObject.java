package org.kuali.student.myplan.plan.dataobject;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/27/13
 * Time: 5:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class RecommendedItemDataObject {

    private String adviserName;

    private String dateAdded;
    
    private String atpId;

    private String note;

    private boolean isPlanned;

    public String getAdviserName() {
        return adviserName;
    }

    public void setAdviserName(String adviserName) {
        this.adviserName = adviserName;
    }


    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getAtpId() {
        return atpId;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }

    public boolean isPlanned() {
        return isPlanned;
    }

    public void setPlanned(boolean planned) {
        isPlanned = planned;
    }
}
