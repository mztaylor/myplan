package org.kuali.student.myplan.registration.dataobject;

import java.util.Collections;
import java.util.List;

/**
 * Created by hemanth on 9/4/14.
 */
public class RegistrationTerm {

    private String termId;
    private String possibleScheduleUniqueId;
    private String requestedLearningPlanId;
    private int courseRegistrationCount;
    private int plannedItemsCount;
    private boolean openForRegistration;
    private boolean plannedCourses;
    private boolean plannedActivities;
    private List<String> errorPlannedCourses;
    private String learningPlanId;

    public String getLearningPlanId() {
        return learningPlanId;
    }

    public void setLearningPlanId(String learningPlanId) {
        this.learningPlanId = learningPlanId;
    }

    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    public String getPossibleScheduleUniqueId() {
        return possibleScheduleUniqueId;
    }

    public void setPossibleScheduleUniqueId(String possibleScheduleUniqueId) {
        this.possibleScheduleUniqueId = possibleScheduleUniqueId;
    }

    public String getRequestedLearningPlanId() {
        return requestedLearningPlanId;
    }

    public void setRequestedLearningPlanId(String requestedLearningPlanId) {
        this.requestedLearningPlanId = requestedLearningPlanId;
    }

    public int getCourseRegistrationCount() {
        return courseRegistrationCount;
    }

    public void setCourseRegistrationCount(int courseRegistrationCount) {
        this.courseRegistrationCount = courseRegistrationCount;
    }

    public int getPlannedItemsCount() {
        return plannedItemsCount;
    }

    public void setPlannedItemsCount(int plannedItemsCount) {
        this.plannedItemsCount = plannedItemsCount;
    }

    public boolean isOpenForRegistration() {
        return openForRegistration;
    }

    public void setOpenForRegistration(boolean openForRegistration) {
        this.openForRegistration = openForRegistration;
    }

    public boolean isPlannedCourses() {
        return plannedCourses;
    }

    public void setPlannedCourses(boolean plannedCourses) {
        this.plannedCourses = plannedCourses;
    }

    public boolean isPlannedActivities() {
        return plannedActivities;
    }

    public void setPlannedActivities(boolean plannedActivities) {
        this.plannedActivities = plannedActivities;
    }

    public List<String> getErrorPlannedCourses() {
        return (null == errorPlannedCourses ? Collections.<String>emptyList() : errorPlannedCourses);
    }

    public void setErrorPlannedCourses(List<String> errorPlannedCourses) {
        this.errorPlannedCourses = errorPlannedCourses;
    }
}
