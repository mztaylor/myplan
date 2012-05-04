package org.kuali.student.myplan.plan.util;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.r2.common.exceptions.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/23/12
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtpHelper {

    private static String term1 = "winter";
    private static String term2 = "spring";
    private static String term3 = "summer";
    private static String term4 = "autumn";
    /*
     * atpPrefix is the length of "kuali.uw.atp." prefix in "kuali.uw.atp.spring2014"
     */
    private static int atpPrefix = 13;

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
        String atpId = getTermAndYearFromAtp(currentTerm.getId());
        return atpId;
    }

    /**
     * Gets the ATP ID of the first ATP in the current academic year.
     */
    public static String getFirstAtpIdOfAcademicYear(String atpId) {
        String firstAtpId = null;
        String t[] = getTermAndYear(atpId);
        //   If the term is not Autumn/4 then the beginning of the academic year is (year - 1) . 4
        if (t[0].equals("1")) {
            firstAtpId = atpId;
        } else {
            String year = String.valueOf(Integer.valueOf(t[1]) - 1);
            firstAtpId = AtpHelper.getAtpFromYearAndNumTerm("4", year);
        }
        return firstAtpId;
    }

    /*for atp kuali.uw.atp.spring2014 returns string kuali.uw.atp.2014.2*/
    public static String getTermAndYearFromAtp(String atp){
        String qtrYr = atp.substring(atpPrefix, atp.length());
        String[] splitStr = qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        String year=splitStr[1];
        String term=splitStr[0];
        splitStr[0]=term.trim();
        splitStr[1]=year.trim();

        splitStr[0]=splitStr[0].substring(0, 1).toUpperCase().concat(splitStr[0].substring(1, splitStr[0].length()));

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
        newAtpId=newAtpId.append(PlanConstants.TERM_ID_PREFIX).append(year).append(".").append(termVal);
        return newAtpId.toString();

    }
    /*for atp kuali.uw.atp.spring2014 returns string[] with term(String[0])="Spring" and year(string[1])=2014*/
    public static String[] getAlphaTermAndYearForAtp(String atp){
        String qtrYr = atp.substring(atpPrefix, atp.length());
        String [] splitStr= new String[2];
        splitStr[0]=qtrYr.substring(qtrYr.lastIndexOf(".")+1);
        splitStr[1]=qtrYr.substring(0,qtrYr.lastIndexOf("."));
        if(splitStr[0].equalsIgnoreCase("1")){
            splitStr[0]=term1.substring(0, 1).toUpperCase().concat(term1.substring(1, term1.length()));
        }
        if(splitStr[0].equalsIgnoreCase("2")){
            splitStr[0]=term2.substring(0, 1).toUpperCase().concat(term2.substring(1, term2.length()));
        }
        if(splitStr[0].equalsIgnoreCase("3")){
            splitStr[0]=term3.substring(0, 1).toUpperCase().concat(term3.substring(1, term3.length()));
        }
        if(splitStr[0].equalsIgnoreCase("4")){
            splitStr[0]=splitStr[0].substring(0, 1).toUpperCase().concat(splitStr[0].substring(1, splitStr[0].length()));
            splitStr[0]=term4.substring(0, 1).toUpperCase().concat(term4.substring(1, term4.length()));
        }

        return  splitStr;

    }

        /*for atp kuali.uw.atp.spring2014 returns string[] with term(String[0])=2 and year(string[1])=2014*/
    public static String[] getTermAndYear(String atp){
        String qtrYr = atp.substring(atpPrefix, atp.length());
        String [] splitStr= new String[2];
        splitStr[0]=qtrYr.substring(qtrYr.lastIndexOf(".")+1);
        splitStr[1]=qtrYr.substring(0,qtrYr.lastIndexOf("."));
        return  splitStr;
    }
    /*retuns atp of this format kuali.uw.atp.1991.1 for term="Winter" and year = 1991*/
    public static String getAtpFromYearAndTerm(String term, String year){
        int termVal=0;
        if(term.equalsIgnoreCase(term1)){
            termVal=1;
        }
        if(term.equalsIgnoreCase(term2)){
            termVal=2;
        }
        if(term.equalsIgnoreCase(term3)){
            termVal=3;
        }
        if(term.equalsIgnoreCase(term4)){
            termVal=4;
        }
        StringBuffer newAtpId=new StringBuffer();
        newAtpId=newAtpId.append(PlanConstants.TERM_ID_PREFIX).append(year).append(".").append(termVal);
        return newAtpId.toString();
    }

    /*retuns atp of this format kuali.uw.atp.1991.1 for term=1 and year = 1991*/
    public static String getAtpFromYearAndNumTerm(String term, String year){
        StringBuffer newAtpId=new StringBuffer();
        newAtpId=newAtpId.append(PlanConstants.TERM_ID_PREFIX).append(year).append(".").append(term);
        return newAtpId.toString();
    }
}
