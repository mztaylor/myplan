package org.kuali.student.myplan.plan.util;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.PlanConstants;

import javax.xml.namespace.QName;
import java.util.List;

/**
 *  Helper methods for dealing with ATPs.
 */
public class AtpHelper {

    private static String term1 = "winter";
    private static String term2 = "spring";
    private static String term3 = "summer";
    private static String term4 = "autumn";

    private static transient AcademicCalendarService academicCalendarService;

    private static AcademicCalendarService getAcademicCalendarService() {
       if (academicCalendarService == null) {
           academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                   .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                           AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
       }
       return academicCalendarService;
    }

    /**
     * Query the Academic Record Service, determine the current ATP, and the return the ID.
     * @return The ID of the current ATP.
     * @throws RuntimeException if the query fails or if the return data set doesn't make sense.
     */
    public static String getCurrentAtpId() {
        //   The first arg here is "usageKey" which isn't used.
        List<TermInfo> scheduledTerms = null;
        try {
            scheduledTerms = getAcademicCalendarService().getCurrentTerms(CourseSearchConstants.PROCESS_KEY, PlanConstants.CONTEXT_INFO) ;
        } catch (Exception e) {
            throw new RuntimeException("Query to Academic Calendar Service failed.", e);
        }

        if (scheduledTerms == null || scheduledTerms.size() == 0) {
            throw new RuntimeException("No scheduled terms were found.");
        }

        //  The UW implementation of the AcademicCalendarService.getCurrentTerms() contains the "current term" logic so we can simply
        //  use the first item in the list. Although, TODO: Not sure if the order of the list is guaranteed, so maybe putting a sort here
        //  is the Right thing to do.
        TermInfo currentTerm = scheduledTerms.get(0);
        return currentTerm.getId();
    }

    /**
     * Gets the ATP ID of the first ATP in the current academic year.
     */
    public static String getFirstAtpIdOfAcademicYear(String atpId) {
        String firstAtpId = null;
        String atpSuffix = atpId.replace(PlanConstants.TERM_ID_PREFIX, "");
        String[] termYear = atpSuffix.split("\\.");
        String year = termYear[0];
        String term = termYear[1];

        //   If the term is not Autumn/4 then the beginning of the academic year is (year - 1) . 4
        if (term.equals("4")) {
            firstAtpId = atpId;
        } else {
            String y = String.valueOf(Integer.valueOf(year) - 1);
            firstAtpId = AtpHelper.getAtpFromNumTermAndYear("4", y);
        }
        return firstAtpId;
    }

    /**
     * Returns an String[] {term, year} given an ATP ID.
     */
    public static String[] atpIdToTermAndYear(String atpId) {
        String atpSuffix = atpId.replace(PlanConstants.TERM_ID_PREFIX, "");

        //  See if the ATP ID is nearly sane.
        if ( ! atpSuffix.matches("[0-9]{4}\\.[1-4]{1}")) {
            throw new RuntimeException(String.format("ATP ID [%s] isn't formatted correctly.", atpId));
        }

        String[] termYear = atpSuffix.split("\\.");
        String year = termYear[0];
        String term = termYear[1];
        return new String[] {term, year};
    }

    /**
     * Converts an ATP ID to a Term and Year ...
     *    "kuali.uw.atp.1991.1" -> {"Autumn", "1991"}
     * @return A String array containing a term and year.
     */
    public static String[] atpIdToTermNameAndYear(String atpId) {
        String[] termYear = atpIdToTermAndYear(atpId);
        String term = termYear[0];
        String year = termYear[1];

        if (term.equals("4")) {
            term = "Autumn";
        } else if (term.equals("1")) {
            term = "Winter";
        } else if (term.equals("2")) {
            term = "Spring";
        }  else if (term.equals("3")) {
            term = "Summer";
        }
        return new String[] {term, year};
    }

    /*  Retuns ATP ID in format kuali.uw.atp.1991.1 for term="Winter" and year = 1991*/
    public static String getAtpIdFromTermAndYear(String term, String year){
        int termVal=0;
        if (term.equalsIgnoreCase(term1)){
            termVal=1;
        }
        if (term.equalsIgnoreCase(term2)){
            termVal=2;
        }
        if (term.equalsIgnoreCase(term3)){
            termVal=3;
        }
        if (term.equalsIgnoreCase(term4)){
            termVal=4;
        }
        StringBuffer newAtpId=new StringBuffer();
        newAtpId = newAtpId.append(PlanConstants.TERM_ID_PREFIX).append(year).append(".").append(termVal);
        return newAtpId.toString();
    }

    /* Returns ATP ID as kuali.uw.atp.1991.1 for term=1 and year = 1991 */
    public static String getAtpFromNumTermAndYear(String term, String year){
        StringBuffer newAtpId = new StringBuffer();
        newAtpId = newAtpId.append(PlanConstants.TERM_ID_PREFIX).append(year).append(".").append(term);
        return newAtpId.toString();
    }

    /**
     * Gets term name as "Spring 2012" given an ATP ID.
     * @return
     */
    public static String atpIdToTermName(String atpId) {
        String[] termYear = atpIdToTermNameAndYear(atpId);
        return (termYear[0] + " " + termYear[1]);
    }

    /**
     * Returns true if an ATP is considered historical in the context of WHAT? Otherwise, false.
     * @param atpId
     * @return
     */
    public static boolean isAtpHistorical(String atpId) {
        return false;
    }
}
