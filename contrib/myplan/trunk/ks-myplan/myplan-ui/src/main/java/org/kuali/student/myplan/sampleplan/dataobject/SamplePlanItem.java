package org.kuali.student.myplan.sampleplan.dataobject;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/7/13
 * Time: 12:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePlanItem {
    private String atpId;
    private String code;
    private String credit;
    private String note;
    private String planItemId;
    private String reqComponentId;
    private String alternateCode;
    private String alternateCredit;
    private String alternateReqComponentId;

    /*required for comboBox Display purpose*/
    private String displayCode;
    private String displayAlternateCode;

    /*Required for Error notification to a specific item*/
    private int yearIndex;
    private int termIndex;
    private int itemIndex;

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

    public String getAlternateCode() {
        return alternateCode;
    }

    public void setAlternateCode(String alternateCode) {
        this.alternateCode = alternateCode;
    }

    public String getAlternateCredit() {
        return alternateCredit;
    }

    public void setAlternateCredit(String alternateCredit) {
        this.alternateCredit = alternateCredit;
    }

    public String getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(String planItemId) {
        this.planItemId = planItemId;
    }

    public String getAlternateReqComponentId() {
        return alternateReqComponentId;
    }

    public void setAlternateReqComponentId(String alternateReqComponentId) {
        this.alternateReqComponentId = alternateReqComponentId;
    }

    public String getReqComponentId() {
        return reqComponentId;
    }

    public void setReqComponentId(String reqComponentId) {
        this.reqComponentId = reqComponentId;
    }

    public int getYearIndex() {
        return yearIndex;
    }

    public void setYearIndex(int yearIndex) {
        this.yearIndex = yearIndex;
    }

    public int getTermIndex() {
        return termIndex;
    }

    public void setTermIndex(int termIndex) {
        this.termIndex = termIndex;
    }

    public int getItemIndex() {
        return itemIndex;
    }

    public void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public String getAtpId() {
        return atpId;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }

    public String getDisplayCode() {
        return displayCode;
    }

    public void setDisplayCode(String displayCode) {
        this.displayCode = displayCode;
    }

    public String getDisplayAlternateCode() {
        return displayAlternateCode;
    }

    public void setDisplayAlternateCode(String displayAlternateCode) {
        this.displayAlternateCode = displayAlternateCode;
    }
}
