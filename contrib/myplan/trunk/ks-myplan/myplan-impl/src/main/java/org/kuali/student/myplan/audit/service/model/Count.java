package org.kuali.student.myplan.audit.service.model;

public class Count {
    public String flag = " ";
    public int required = 100;
    public int earned = 1;
    public int needs = 99;

    public void setFlag(String flag) {
        this.flag = flag;
    }

    public void setRequired(int required) {
        this.required = required;
    }
    public int getRequired() { return required; }

    public boolean showRequired() {
        return !"R".equals(flag);
    }

    public void setEarned(int earned) {
        this.earned = earned;
    }
    public int getEarned() { return earned; }

    public boolean showEarned() {
        return !"E".equals(flag);
    }

    public void setNeeds(int needs) {
        this.needs = needs;
    }
    public int getNeeds() { return needs; }

    public boolean showNeeds() {
        return true;
    }

}
