package org.kuali.student.myplan.course.util;

import org.apache.commons.lang.StringUtils;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.lum.lrc.dto.ResultComponentInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Turns credits info into Strings.
 */
public class CreditsFormatter {

    final static Logger logger = Logger.getLogger(CreditsFormatter.class);

    /**
     * Formats credit options list as a String.
     *
     * @param courseInfo
     * @return
     */
    public static String formatCredits(CourseInfo courseInfo) {
        String credits = "";

        List<ResultComponentInfo> options = courseInfo.getCreditOptions();
        if (options.size() == 0) {
            logger.warn("Credit options list was empty.");
            return credits;
        }
        /* At UW this list should only contain one item. */
        if (options.size() > 1) {
            logger.warn("Credit option list contained more than one value.");
        }
        ResultComponentInfo rci = options.get(0);

        /**
         *  Credit values are provided in three formats: FIXED, LIST (Multiple), and RANGE (Variable). Determine the
         *  format and parse it into a String representation.
         */
        String type = rci.getType();
        if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED)) {
            credits = rci.getAttributes().get(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_FIXED_CREDIT_VALUE);
            credits = trimCredits(credits);
        } else if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE)) {
            StringBuilder cTmp = new StringBuilder();
            Collections.sort(rci.getResultValues(), new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    if (Double.parseDouble(o1) > Double.parseDouble(o2))
                        return +1;
                    else if (Double.parseDouble(o1) < Double.parseDouble(o2))
                        return -1;
                    else
                        return 0;


                }
            });
            for (String c : rci.getResultValues()) {
                if (cTmp.length() != 0) {
                    cTmp.append(", ");
                }
                cTmp.append(trimCredits(c));
            }
            credits = cTmp.toString();
        } else if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE)) {
            String minCredits = rci.getAttributes().get(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_MIN_CREDIT_VALUE);
            String maxCredits = rci.getAttributes().get(CourseAssemblerConstants.COURSE_RESULT_COMP_ATTR_MAX_CREDIT_VALUE);
            credits = trimCredits(minCredits) + "-" + trimCredits(maxCredits);
        } else {
            logger.error("Unknown Course Credit type [" + type + "].");
        }
        return credits;
    }

    /**
     * Drop the decimal point and and trailing zero from credits.
     *
     * @return The supplied value minus the trailing ".0"
     */
    public static String trimCredits(String credits) {
        if (StringUtils.isBlank(credits)) {
            return "";
        }
        credits = credits.trim();
        if (credits.endsWith(".0")) {
            credits = credits.substring(0, credits.indexOf("."));
        }
        return credits;
    }
}
