package org.kuali.student.myplan.plan.util;

import org.kuali.student.myplan.course.util.PlanConstants;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/23/12
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class AtpHelper {

    /*
   atpPrefix is the length of "kuali.uw.atp." prefix in "kuali.uw.atp.spring2014"
    */
    private int atpPrefix = 13;
    
    public String[] getTermAndYear(String atp){
        String qtrYr = atp.substring(atpPrefix, atp.length());
        String[] splitStr = qtrYr.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        splitStr[0]=splitStr[0].substring(0, 1).toUpperCase().concat(splitStr[0].substring(1, splitStr[0].length()));
        return  splitStr;
    }
    
    public String getAtpFromYearAndTerm(String term, String year){
        String newTermId = PlanConstants.TERM_ID_PREFIX + term + year;
        return newTermId;
    }
    
    
}
