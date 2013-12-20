package org.kuali.student.myplan.sampleplan.dataobject;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/7/13
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePlanItem {
    private String code;
    private String credit;
    private String note;
    private String planItemId;
    private String orCode;
    private String orCredit;
    private String orPlanItemId;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public String getOrCode() {
        return orCode;
    }

    public void setOrCode(String orCode) {
        this.orCode = orCode;
    }

    public String getOrCredit() {
        return orCredit;
    }

    public void setOrCredit(String orCredit) {
        this.orCredit = orCredit;
    }

    public String getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(String planItemId) {
        this.planItemId = planItemId;
    }

    public String getOrPlanItemId() {
        return orPlanItemId;
    }

    public void setOrPlanItemId(String orPlanItemId) {
        this.orPlanItemId = orPlanItemId;
    }
}
