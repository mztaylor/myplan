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
public class SamplePlanYear {
    private String yearName;
    private int year;
    private List<SamplePlanTerm> samplePlanTerms;
    private String totalYearCredits;

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getYearName() {
        return yearName;
    }

    public void setYearName(String yearName) {
        this.yearName = yearName;
    }

    public List<SamplePlanTerm> getSamplePlanTerms() {
        if (samplePlanTerms == null) {
            samplePlanTerms = new ArrayList<SamplePlanTerm>();
        }
        return samplePlanTerms;
    }

    public void setSamplePlanTerms(List<SamplePlanTerm> samplePlanTerms) {
        this.samplePlanTerms = samplePlanTerms;
    }

    public String getTotalYearCredits() {
        return totalYearCredits;
    }

    public void setTotalYearCredits(String totalYearCredits) {
        this.totalYearCredits = totalYearCredits;
    }

    public void addCredit(String credits) {
        setTotalYearCredits(CreditsFormatter.addStringCredits(credits, totalYearCredits));
    }
}
