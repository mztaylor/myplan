package org.kuali.student.myplan.registration.dataobject;

/**
 * Created by hemanth on 9/4/14.
 */
public class RegistrationTerm {

    private String termId;
    private String possibleScheduleUniqueId;
    private String requestedLearningPlanId;
    private int courseRegistrationCount;
    private boolean openForRegistration;
    private boolean plannedCourses;
    private boolean plannedActivities;

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
}
