package org.kuali.student.myplan.audit.form;

import org.kuali.student.myplan.audit.dataobject.CourseItem;
import org.kuali.student.myplan.audit.dataobject.IgnoreTermDataObject;
import org.kuali.student.myplan.audit.dataobject.MessyItem;
import org.kuali.student.myplan.audit.dataobject.MessyTermDataObject;

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

    private boolean showHandOffScreen;

    private List<CourseItem> cleanList;

    private List<IgnoreTermDataObject> ignoreList;

    private List<MessyTermDataObject> messyItems;

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

    public List<IgnoreTermDataObject> getIgnoreList() {
        if (ignoreList == null) {
            ignoreList = new ArrayList<IgnoreTermDataObject>();
        }
        return ignoreList;
    }

    public void setIgnoreList(List<IgnoreTermDataObject> ignoreList) {
        this.ignoreList = ignoreList;
    }

    public List<MessyTermDataObject> getMessyItems() {
        if (messyItems == null) {
            messyItems = new ArrayList<MessyTermDataObject>();
        }
        return messyItems;
    }

    public void setMessyItems(List<MessyTermDataObject> messyItems) {
        this.messyItems = messyItems;
    }

    public boolean isShowHandOffScreen() {
        return showHandOffScreen;
    }

    public void setShowHandOffScreen(boolean showHandOffScreen) {
        this.showHandOffScreen = showHandOffScreen;
    }


    /**
     * Checks to see if there is a user selected value available for all Messy Items for every term.
     *
     * @return true: if all messy items have a credit value selected, else returns false.
     */
   public boolean isAllMessyItemsSelected() {
       for(MessyTermDataObject mtd : messyItems) {
           for(MessyItem item : mtd.getMessyItemList()) {
               if(null == item.getSelectedCredit()) return false;
           }
       }

       return true;
   }

}
