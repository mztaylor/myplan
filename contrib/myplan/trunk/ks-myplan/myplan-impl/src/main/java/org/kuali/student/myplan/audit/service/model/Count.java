package org.kuali.student.myplan.audit.service.model;

public class Count {
    public int required = 100;
    public int earned = 1;
    public int needs = 99;

    public int getRequired() { return required; }
    public int getEarned() { return earned; }
    public int getNeeds() { return needs; }

    public void setRequired(int required) {
        this.required = required;
    }

    public void setEarned(int earned) {
        this.earned = earned;
    }

    public void setNeeds(int needs) {
        this.needs = needs;
    }
}
