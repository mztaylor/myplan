package org.kuali.student.myplan.schedulebuilder.util;

import edu.uw.kuali.student.myplan.util.TermHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.student.enrollment.acal.infc.Term;

/**
 * Data Storage for the Term and Year of a single atp. Formats different output
 * forms of the data
 */
public class DefaultYearTerm implements YearTerm, Comparable<YearTerm> {

    private static final Logger LOG = Logger.getLogger(DefaultYearTerm.class);

    private final String termId;
    private final String termType = "atpId";
    private final int year;

    private TermHelper termHelper;

    public DefaultYearTerm(String termId, String termType, int year) {
        /*if (!KsapHelperUtil.getTermTypes().contains(termType))
            throw new IllegalArgumentException("Term type " + termType
                    + " not supported");*/
        this.termId = termId;
        //this.termType = termType;
        this.year = year;
    }

    @Override
    public String getTermId() {
        return termId;
    }

    @Override
    public String getTermType() {
        return termType;
    }

    @Override
    public int getYear() {
        return year;
    }

    @Override
    public int compareTo(YearTerm o) {
        Term t1 = getTermHelper().getTerm(this);
        Term t2 = getTermHelper().getTerm(o);
        return t1.getStartDate().compareTo(t2.getStartDate());
    }

    @Override
    public String getTermName() {
        String rv = (rv = getLongName()) == null ? null : rv.trim();
        if (rv != null && rv.length() > 7
                && rv.endsWith(" " + Integer.toString(year)))
            return rv.substring(0, rv.length() - 5);
        LOG.warn("Not sure how to extract term name " + rv);
        return rv;
    }

    @Override
    public String getShortName() {
        String rv = (rv = getLongName()) == null ? null : rv.trim();
        if (rv != null && rv.length() > 7
                && rv.endsWith(" " + Integer.toString(year)))
            return rv.substring(0, 2).toUpperCase() + " " + year;
        LOG.warn("Not sure how to shorten term name " + rv);
        return rv;
    }

    @Override
    public String getLongName() {
        return getTermHelper().getTerm(this)
                .getName();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((termType == null) ? 0 : termType.hashCode());
        result = prime * result + year;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DefaultYearTerm other = (DefaultYearTerm) obj;
        if (termType == null) {
            if (other.termType != null)
                return false;
        } else if (!termType.equals(other.termType))
            return false;
        if (year != other.year)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "DefaultYearTerm [termType=" + termType + ", year=" + year + "]";
    }

    public TermHelper getTermHelper() {
        if (termHelper == null) {
            termHelper = new TermHelperImpl();
        }
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
    }
}
