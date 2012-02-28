package org.kuali.student.myplan.audit.service.model;

public class GPA {
    public String caption = "* gpa caption *";
    public float required;
    public float earned;

    public float getRequired() { return required; }
    public float getEarned() { return earned; }

    public void setRequired(float required) {
        this.required = required;
    }

    public void setEarned(float earned) {
        this.earned = earned;
    }
}
