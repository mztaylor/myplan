package org.kuali.student.myplan.plan.dataobject;

import java.util.*;
/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/13/12
 * Time: 1:50 PM
 * To change this template use File | Settings | File Templates.
 */

public class FullPlanItemsDataObject {
    /* Used for sorting purpose*/
    private int year;

    private String yearRange;

    private List<PlannedTerm> terms=new ArrayList<PlannedTerm>();

    public String getYearRange() {
        return yearRange;
    }

    public void setYearRange(String yearRange) {
        this.yearRange = yearRange;
    }

    public int getYear() {
        return year;

    }

    public void setYear(int year) {
        this.year = year;
    }

    public List<PlannedTerm> getTerms() {
        return terms;
    }

    public void setTerms(List<PlannedTerm> terms) {
        this.terms = terms;
    }
}
