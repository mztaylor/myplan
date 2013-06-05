package org.kuali.student.myplan.audit.dataobject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/22/13
 * Time: 10:08 PM
 * To change this template use File | Settings | File Templates.
 */
public class MessyTermDataObject {

    private List<MessyItem> messyItemList;

    private String atpId;

    public List<MessyItem> getMessyItemList() {
        if (messyItemList == null) {
            messyItemList = new ArrayList<MessyItem>();
        }
        return messyItemList;
    }

    public void setMessyItemList(List<MessyItem> messyItemList) {
        this.messyItemList = messyItemList;
    }

    public String getAtpId() {
        return atpId;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }
}
