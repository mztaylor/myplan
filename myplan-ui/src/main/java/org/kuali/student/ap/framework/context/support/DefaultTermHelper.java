package org.kuali.student.ap.framework.context.support;

import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.AcademicCalendar;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.schedulebuilder.util.YearTerm;

import java.util.Date;
import java.util.List;

/**
 * PLACE HOLDER CLASS
 * This class should be replaced by one from KSAP's ks-ap-framework module
 */
public class DefaultTermHelper implements TermHelper {


    @Override
    public void frontLoadForPlanner(String firstAtpId) {

    }

    @Override
    public Term getTermByAtpId(String atpId) {
        return null;
    }

    @Override
    public YearTerm getYearTerm(String atpId) {
        return null;
    }

    @Override
    public List<Term> getCurrentTerms() {
        return null;
    }

    @Override
    public Term getLastScheduledTerm() {
        return null;
    }

    @Override
    public Term getOldestHistoricalTerm() {
        return null;
    }

    @Override
    public Term getFirstTermOfAcademicYear(YearTerm yearTerm) {
        return null;
    }

    @Override
    public int getNumberOfTermsInAcademicYear(YearTerm yearTerm) {
        return 0;
    }

    @Override
    public List<Term> getTermsInAcademicYear(YearTerm yearTerm) {
        return null;
    }

    @Override
    public int getNumberOfTermsInAcademicYear() {
        return 0;
    }

    @Override
    public List<Term> getTermsInAcademicYear() {
        return null;
    }

    @Override
    public String getTermNameInAcadmicYear(int index) {
        return null;
    }

    @Override
    public boolean isPlanning(String termId) {
        return false;
    }

    @Override
    public boolean isOfficial(String termId) {
        return false;
    }

    @Override
    public boolean isCompleted(String atpId) {
        return false;
    }

    @Override
    public List<Term> getOfficialTerms() {
        return null;
    }

    @Override
    public List<Term> getPlanningTerms() {
        return null;
    }

    @Override
    public List<Term> getTermsByDateRange(Date startDate, Date endDate) {
        return null;
    }

    @Override
    public Term getTerm(YearTerm yearTerm) {
        return null;
    }

    @Override
    public YearTerm getYearTerm(Term term) {
        return null;
    }

    @Override
    public Term getCurrentTerm() {
        return null;
    }

    @Override
    public AcademicCalendar getCurrentAcademicCalendar() {
        return null;
    }

    @Override
    public List<Term> sortTermsByStartDate(List<Term> terms, boolean ascending) {
        return null;
    }

    @Override
    public List<Term> sortTermsByEndDate(List<Term> terms, boolean ascending) {
        return null;
    }
}
