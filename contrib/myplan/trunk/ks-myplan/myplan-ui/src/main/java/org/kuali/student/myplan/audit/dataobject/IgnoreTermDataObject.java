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
public class IgnoreTermDataObject {

    private List<CourseItem> courseItemList;

    private String atpId;

    public List<CourseItem> getCourseItemList() {
        if (courseItemList == null) {
            courseItemList = new ArrayList<CourseItem>();
        }
        return courseItemList;
    }

    public void setCourseItemList(List<CourseItem> courseItemList) {
        this.courseItemList = courseItemList;
    }

    public String getAtpId() {
        return atpId;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }
}
