package org.kuali.student.myplan.util;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.r2.common.dto.ContextInfo;



import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equalIgnoreCase;

//TODO: THIS CLASS SHOULD BE MERGED WITH ATPHelper

/**
 * Helper methods for dealing with ATPs.
 */
@Deprecated
public class DegreeAuditAtpHelper {

    public static final String PRIORITY_ONE_REGISTRATION_START = "priority_one_registration_start";

    private static String term1 = "winter";
    private static String term2 = "spring";
    private static String term3 = "summer";
    private static String term4 = "autumn";

    public static final String TERM_ID_PREFIX = "kuali.uw.atp.";

    private static transient AcademicCalendarService academicCalendarService;

    public static int TERM_COUNT = 4;

    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

    private static final Logger logger = Logger.getLogger(DegreeAuditAtpHelper.class);

    private static AcademicCalendarService getAcademicCalendarService() {
        if (academicCalendarService == null) {
            academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return academicCalendarService;
    }


  /**
    * Query the Academic Calendar Service for terms that have offering's published, determine the last ATP, and return its ID.
    *
    * EXCEPTION FOR SUMMER: Change summer last published date to 3 weeks prior to registration for summer quarter to
    * update the course catalog for summer
    *
    * @return The ID of the last scheduled ATP.
    * @throws RuntimeException if the query fails or if the return data set doesn't make sense.
    */
    public static String getLastScheduledAtpId() {
        List<TermInfo> scheduledTerms = new ArrayList<TermInfo>();
        try {
            scheduledTerms = getAcademicCalendarService().searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", "PUBLISHED")), CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Query to Academic Calendar Service failed.", e);
            /*If SWS Fails to load up scheduled Terms then current atp Id in TermInfo is populated from the calender month and year and set to the scheduledTerms list*/
            scheduledTerms = populateAtpIdFromCalender();
        }
        //  The UW implementation of the AcademicCalendarService.getCurrentTerms() contains the "current term" logic so we can simply
        //  use the first item in the list. Although, TODO: Not sure if the order of the list is guaranteed, so maybe putting a sort here
        //  is the Right thing to do.
        TermInfo lastTerm = scheduledTerms.get( scheduledTerms.size() - 1 );

        // SUMMER EXCEPTION
        if(lastTerm.getId().endsWith(".3")) {
            List<AttributeInfo> attrs = lastTerm.getAttributes();
            for(AttributeInfo attr : attrs) {
                if(attr.getKey().equals(DegreeAuditAtpHelper.PRIORITY_ONE_REGISTRATION_START)){
                    // Check to see if the priority registration is still more than 3 weeks away
                    DateTime regStart = new DateTime(attr.getValue());
                    if(regStart.minusWeeks(3).isAfterNow()) {
                        lastTerm = scheduledTerms.get(scheduledTerms.size() - 2);
                    }
                }
            }
        }


        return lastTerm.getId();

    }


    private static List<TermInfo> populateAtpIdFromCalender() {
        List<TermInfo> scheduledTerms = new ArrayList<TermInfo>();
        TermInfo termInfo = new TermInfo();
        String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
        int month = Calendar.getInstance().get(Calendar.MONTH);
        String atp = null;
        if (month >= 1 && month <= 3) {
            atp = getAtpIdFromTermAndYear(term1, year);
        }
        if (month >= 4 && month <= 6) {
            atp = getAtpIdFromTermAndYear(term2, year);
        }
        if (month >= 7 && month <= 9) {
            atp = getAtpIdFromTermAndYear(term3, year);
        }
        if (month >= 10 && month <= 12) {
            atp = getAtpIdFromTermAndYear(term4, year);
        }
        termInfo.setId(atp);
        scheduledTerms.add(termInfo);
        return scheduledTerms;
    }


    /*  Retuns ATP ID in format kuali.uw.atp.1991.1 for term="Winter" and year = 1991*/
    private static String getAtpIdFromTermAndYear(String term, String year) {
        int termVal = 0;
        if (term.equalsIgnoreCase(term1)) {
            termVal = 1;
        }
        if (term.equalsIgnoreCase(term2)) {
            termVal = 2;
        }
        if (term.equalsIgnoreCase(term3)) {
            termVal = 3;
        }
        if (term.equalsIgnoreCase(term4)) {
            termVal = 4;
        }
        StringBuffer newAtpId = new StringBuffer();
        newAtpId = newAtpId.append(TERM_ID_PREFIX).append(year).append(".").append(termVal);
        return newAtpId.toString();
    }
}
