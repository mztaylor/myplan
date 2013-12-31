package org.kuali.student.myplan.course.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.myplan.sampleplan.util.ItemCredits;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.service.assembler.CourseAssemblerConstants;
import org.kuali.student.r2.lum.lrc.dto.ResultValueInfo;
import org.kuali.student.r2.lum.lrc.dto.ResultValuesGroupInfo;
import org.kuali.student.r2.lum.lrc.service.LRCService;
import org.kuali.student.r2.lum.util.constants.LrcServiceConstants;
import org.springframework.util.CollectionUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Turns credits info into Strings.
 */
public class CreditsFormatter {

    private static transient LRCService lrcService;

    final static Logger logger = Logger.getLogger(CreditsFormatter.class);

    /**
     * Formats credit options list as a String.
     *
     * @param courseInfo
     * @return
     */
    public static String formatCredits(CourseInfo courseInfo) {
        String credits = "";

        List<ResultValuesGroupInfo> options = courseInfo.getCreditOptions();
        if (options.size() == 0) {
            logger.warn("Credit options list was empty.");
            return credits;
        }
        /* At UW this list should only contain one item. */
        if (options.size() > 1) {
            logger.warn("Credit option list contained more than one value.");
        }
        ResultValuesGroupInfo rci = options.get(0);

        /**
         *  Credit values are provided in three formats: FIXED, LIST (Multiple), and RANGE (Variable). Determine the
         *  format and parse it into a String representation.
         */
        String type = rci.getType();
        if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_FIXED)) {

            /*Since it is fixed credit both the min and the max value are same*/
            credits = trimCredits(rci.getResultValueRange().getMaxValue());
        } else if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_MULTIPLE)) {
            StringBuilder cTmp = new StringBuilder();
            List<String> resultValueInfos = new ArrayList<String>();
            try {
                for (String resultValueKey : rci.getResultValueKeys()) {
                    ResultValueInfo resultValueInfo = getLrcService().getResultValue(resultValueKey, CourseSearchConstants.CONTEXT_INFO);
                    if (resultValueInfo != null && resultValueInfo.getValue() != null) {
                        resultValueInfos.add(resultValueInfo.getValue());
                    }
                }
            } catch (Exception e) {
                logger.error("Could not fetch the result values", e);
            }

            Collections.sort(resultValueInfos, new Comparator<String>() {
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

            for (String c : resultValueInfos) {
                if (cTmp.length() != 0) {
                    cTmp.append(", ");
                }
                cTmp.append(trimCredits(c));
            }
            credits = cTmp.toString();
        } else if (type.equals(CourseAssemblerConstants.COURSE_RESULT_COMP_TYPE_CREDIT_VARIABLE)) {
            String minCredits = StringUtils.isEmpty(rci.getResultValueRange().getMinValue()) ? null : rci.getResultValueRange().getMinValue();
            String maxCredits = StringUtils.isEmpty(rci.getResultValueRange().getMaxValue()) ? null : rci.getResultValueRange().getMaxValue();
            if (minCredits != null && maxCredits != null) {
            credits = trimCredits(minCredits) + "-" + trimCredits(maxCredits);
            }
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

    public static LRCService getLrcService() {
        if (lrcService == null) {
            lrcService = (LRCService) GlobalResourceLoader.getService(new QName(LrcServiceConstants.NAMESPACE, "LearningResultService"));
        }
        return lrcService;
    }

    public static void setLrcService(LRCService lrcService) {
        CreditsFormatter.lrcService = lrcService;
    }

    public static String addStringCredits(String newCredits, String totalCredits) {
        int minCredits, maxCredits;
        ItemCredits parsedNewCredits, parsedTotalCredits;
        String sumCredits = null;

        if (newCredits == null) {
            return totalCredits;
        }
        if (totalCredits == null) {
            return newCredits;
        }

        // must be able to add any of the following: "5", "5, 15", "5-15"
        // add fixed credit + multiple : result is (fixed + multiple-min),  (fixed + multiple-max)
        // add fixed credit + range    : result is (fixed + range-min)   -  (fixed + range-max)
        // add range + multiple        : result is a range :  (multiple-min+range-min) - (multiple-max+range-max)

        parsedNewCredits   = new ItemCredits(newCredits);
        parsedTotalCredits = new ItemCredits(totalCredits);

        // add the credits
        minCredits =  parsedNewCredits.getMin() + parsedTotalCredits.getMin();
        maxCredits =  parsedNewCredits.getMax() + parsedTotalCredits.getMax();

        // format credits as needed.
        if (parsedNewCredits.isRangeType() || parsedTotalCredits.isRangeType()) {
            sumCredits = Integer.toString(minCredits) + "-" + Integer.toString(maxCredits);
        }
        else if (parsedNewCredits.isMultipleType() || parsedTotalCredits.isMultipleType()) {
            sumCredits = Integer.toString(minCredits) + "," + Integer.toString(maxCredits);
        }
        else if (parsedNewCredits.isFixedType() && parsedTotalCredits.isFixedType()){
            sumCredits = Integer.toString(minCredits);
        }  // else, leave the string = null

        return sumCredits;
    }

}
