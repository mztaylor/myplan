package org.kuali.student.myplan.plan.dataobject;

import org.kuali.student.myplan.course.dataobject.CourseDetails;

import java.util.Date;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/26/12
 * Time: 3:42 PM
 * To change this template use File | Settings | File Templates.
 */
public class CourseDetailsInquiryDataObject {

    private transient CourseDetails courseDetails;
    private transient List<PlanItemDataObject> plannedList;
    private transient List<PlanItemDataObject> backupList;
    private transient List<?> acadRecList;
    private transient String savedItemId;
    private Date savedItemDateCreated;


    public CourseDetails getCourseDetails() {
        return courseDetails;
    }

    public void setCourseDetails(CourseDetails courseDetails) {
        this.courseDetails = courseDetails;
    }

    public List<PlanItemDataObject> getPlannedList() {
        return plannedList;
    }

    public void setPlannedList(List<PlanItemDataObject> plannedList) {
        this.plannedList = plannedList;
    }

    public List<PlanItemDataObject> getBackupList() {
        return backupList;
    }

    public void setBackupList(List<PlanItemDataObject> backupList) {
        this.backupList = backupList;
    }

    public List<?> getAcadRecList() {
        return acadRecList;
    }

    public void setAcadRecList(List<?> acadRecList) {
        this.acadRecList = acadRecList;
    }

    public String getSavedItemId() {
        return savedItemId;
    }

    public void setSavedItemId(String savedItemId) {
        this.savedItemId = savedItemId;
    }

    public Date getSavedItemDateCreated() {
        return savedItemDateCreated;
    }

    public void setSavedItemDateCreated(Date savedItemDateCreated) {
        this.savedItemDateCreated = savedItemDateCreated;
    }
}
