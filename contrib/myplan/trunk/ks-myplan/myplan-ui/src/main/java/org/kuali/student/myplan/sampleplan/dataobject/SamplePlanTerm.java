package org.kuali.student.myplan.sampleplan.dataobject;

import org.kuali.student.myplan.course.util.CreditsFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 11/7/13
 * Time: 8:51 AM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePlanTerm {
    private String termName;
    private String termNote;
    /*Required in UI to show/hide SU*/
    private int year;
    private List<SamplePlanItem> samplePlanItems;
    private String totalTermCredits;

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public String getTermNote() {
        return termNote;
    }

    public void setTermNote(String termNote) {
        this.termNote = termNote;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<SamplePlanItem> getSamplePlanItems() {
        if (samplePlanItems == null) {
            samplePlanItems = new ArrayList<SamplePlanItem>();
        }
        return samplePlanItems;
    }

    public void setSamplePlanItems(List<SamplePlanItem> samplePlanItems) {
        this.samplePlanItems = samplePlanItems;
    }

    public String getTotalTermCredits() {
        return totalTermCredits;
    }

    public void setTotalTermCredits(String totalTermCredits) {
        this.totalTermCredits = totalTermCredits;
    }

    public void addCredit(String credits) {
        setTotalTermCredits(CreditsFormatter.addStringCredits(credits, totalTermCredits));
    }
}
