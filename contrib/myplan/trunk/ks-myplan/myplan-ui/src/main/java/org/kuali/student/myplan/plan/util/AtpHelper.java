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

    private static String term1 = "winter";
    private static String term2 = "spring";
    private static String term3 = "summer";
    private static String term4 = "autumn";
    /*
   atpPrefix is the length of "kuali.uw.atp." prefix in "kuali.uw.atp.spring2014"
    */
    private static int atpPrefix = 13;

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
    /*for atp kuali.uw.atp.spring2014 returns string[] with term(String[0])="Spring" and year(string[1])=2014*/
    public static String[] getAlphaTermAndYearForAtp(String atp){
        atp = getTermAndYearFromAtp(atp);

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
        atp = getTermAndYearFromAtp(atp);

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
