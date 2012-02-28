package org.kuali.student.myplan.audit.service.model;

public class Credits {
    public float minimum = -1;
    public float maximum = -99;
    public float required = 100;
    public float inprogress = 10;
    public float earned = 1;
    public float needs = 1;

    public float getMinimum() { return minimum; }
    public float getRequired() {
        return required;
    }

    public float getInprogress() { return inprogress; }

    public float getEarned() {
        return earned;
    }

    public float getNeeds() { return needs; }

    public void setRequired(float required) {
        this.required = required;
    }

    public void setEarned(float earned) {
        this.earned = earned;
    }

    public void setInprogress(float inprogress) {
        this.inprogress = inprogress;
    }

    public void setNeeds(float needs) {
        this.needs = needs;
    }
}
