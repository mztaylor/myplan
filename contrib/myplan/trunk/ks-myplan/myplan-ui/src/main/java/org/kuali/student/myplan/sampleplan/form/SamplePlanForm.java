package org.kuali.student.myplan.sampleplan.form;

import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.sampleplan.dataobject.SamplePlanYear;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/7/13
 * Time: 8:50 AM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePlanForm extends UifFormBase {

    private List<SamplePlanYear> samplePlanYears;

    private String generalNotes;

    private String planTitle;

    private String degreeProgramTitle;

    private String description;

    private String learningPlanId;

    private boolean preview;

    public List<SamplePlanYear> getSamplePlanYears() {
        if (samplePlanYears == null) {
            samplePlanYears = new ArrayList<SamplePlanYear>();
        }
        return samplePlanYears;
    }

    public void setSamplePlanYears(List<SamplePlanYear> samplePlanYears) {
        this.samplePlanYears = samplePlanYears;
    }

    public String getGeneralNotes() {
        return generalNotes;
    }

    public void setGeneralNotes(String generalNotes) {
        this.generalNotes = generalNotes;
    }

    public String getPlanTitle() {
        return planTitle;
    }

    public void setPlanTitle(String planTitle) {
        this.planTitle = planTitle;
    }

    public String getDegreeProgramTitle() {
        return degreeProgramTitle;
    }

    public void setDegreeProgramTitle(String degreeProgramTitle) {
        this.degreeProgramTitle = degreeProgramTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLearningPlanId() {
        return learningPlanId;
    }

    public void setLearningPlanId(String learningPlanId) {
        this.learningPlanId = learningPlanId;
    }

    public boolean isPreview() {
        return preview;
    }

    public void setPreview(boolean preview) {
        this.preview = preview;
    }
}
