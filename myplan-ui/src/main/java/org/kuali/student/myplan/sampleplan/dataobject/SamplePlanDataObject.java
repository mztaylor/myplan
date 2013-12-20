package org.kuali.student.myplan.sampleplan.dataobject;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/7/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePlanDataObject {

    private String learningPlanId;

    private String degreeProgramTitle;

    private String planTitle;

    private String status;

    private String lastUpdated;

    private String lastCreated;

    private String createdBy;

    public String getLearningPlanId() {
        return learningPlanId;
    }

    public void setLearningPlanId(String learningPlanId) {
        this.learningPlanId = learningPlanId;
    }

    public String getDegreeProgramTitle() {
        return degreeProgramTitle;
    }

    public void setDegreeProgramTitle(String degreeProgramTitle) {
        this.degreeProgramTitle = degreeProgramTitle;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getLastCreated() {
        return lastCreated;
    }

    public void setLastCreated(String lastCreated) {
        this.lastCreated = lastCreated;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }
}
