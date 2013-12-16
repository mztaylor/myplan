package org.kuali.student.myplan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.infc.SearchResult;
import org.kuali.student.r2.core.search.infc.SearchResultCell;
import org.kuali.student.r2.core.search.infc.SearchResultRow;
import org.kuali.student.r2.lum.clu.service.CluService;
import org.kuali.student.r2.lum.util.constants.CluServiceConstants;

import javax.xml.namespace.QName;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Helper class to parse lines of text and create links where course codes are found.
 */
public class CourseLinkBuilder {

    private static final Logger logger = Logger.getLogger(CourseLinkBuilder.class);

    private transient static CluService luService;

    static String link = "<a onclick=\"openCourse('%s','%s %s', event);\" href=\"#\" title=\"%s\">%s</a>";

    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

    /**
     * References to course (subject + number) are found and converted to links.
     * <p/>
     * This strategy is single pass, vs mark and then replace. Steps thru the input string one character
     * at a time. When a linkable match is found, original text is replaced with a link. Also attempts
     * to skip content which is unlinkable, eg course ranges like "COM 100-120" and "COM 100 level".
     *
     * @param line  A line of text to transform.
     * @return
     */

    // matches "A A", "AB&CD", "ABC", etc
//    String subjectRegex = "([A-Z][A-Z &]{2,7})";

    // matches "ABC 100 level"
    String levelRegex = "^([A-Z][A-Z &]{2,7}) [1-9]00";
    Pattern levelPattern = Pattern.compile(levelRegex);

    // matches "ABC 100-200", "ABC 100 to 200", "ABC 100 TO 200"
    String rangeRegex = "^([A-Z][A-Z &]{2,7}) [1-9][0-9][0-9](-| (to|TO) )[1-9]?[0-9][0-9]";
    Pattern rangePattern = Pattern.compile(rangeRegex);

    // matches "ABC/XYZ 100-200", "ABC/XYZ 100 to 200", "ABC/XYZ 100 TO 200"
    String jointRangeRegex = "^([A-Z][A-Z &]{2,7})((/|\\\\)([A-Z][A-Z &]{2,7}))+ [1-9][0-9][0-9](-| (to|TO) )[1-9]?[0-9][0-9]";
    Pattern jointRangePattern = Pattern.compile(jointRangeRegex);

    // matches "ABC 100"
    String courseRegex = "^([A-Z][A-Z &]{2,7}) ([1-9][0-9][0-9])";
    Pattern coursePattern = Pattern.compile(courseRegex);

    // matches "ABC100"
    String courseNoSpaceRegex = "^([A-Z][A-Z &]{2,7})([1-9][0-9][0-9])";
    Pattern courseNoSpacePattern = Pattern.compile(courseNoSpaceRegex);

    // matches "ABC/XYZ 100"
    String jointRegex = "^([A-Z][A-Z &]{2,7})((/|\\\\)([A-Z][A-Z &]{2,7}))+ ([1-9][0-9][0-9])";
    Pattern jointPattern = Pattern.compile(jointRegex);

    // Matches "101" thru "899" and excludes 00 level numbers
    String numRegex = "^[1-9][0-9][0-9]";
    Pattern numPattern = Pattern.compile(numRegex);

    // Matches "100-899"
    String numRangeRegex = "^[1-9][0-9][0-9](-| (to|TO) )[1-9]?[0-9][0-9]";
    Pattern numRangePattern = Pattern.compile(numRangeRegex);

    public String makeLinks(String line) {

        StringBuilder sb = new StringBuilder();
        int start = 0;
        int len = line.length();

        String subject = null;

        for (int nth = 0; nth < len; ) {
            String partial = line.substring(nth);
            Matcher m = null;

            // Remember subject then skip
            m = levelPattern.matcher(partial);
            if (m.find()) {
                int end = nth + m.end();
                String skipped = line.substring(start, end);
                sb.append(skipped);
                subject = m.group(1);
                nth = start = end;
                continue;
            }

            // Remember subject then skip
            m = rangePattern.matcher(partial);
            if (m.find()) {
                int end = nth + m.end();
                String skipped = line.substring(start, end);
                sb.append(skipped);
                subject = m.group(1);
                nth = start = end;
                continue;
            }

            // Remember subject then skip
            m = jointRangePattern.matcher(partial);
            if (m.find()) {
                int end = nth + m.end();
                String skipped = line.substring(start, end);
                sb.append(skipped);
                subject = m.group(4);
                nth = start = end;
                continue;
            }

            // Linkify
            m = coursePattern.matcher(partial);
            if (m.find()) {
                int end = nth + m.end();
                String skipped = line.substring(start, nth);
                sb.append(skipped);
                String found = line.substring(nth, end);
                subject = m.group(1);
                String num = m.group(2);
                String link = makeLink(subject, num, found);
                sb.append(link);
                nth = start = end;
                continue;
            }

            // Linkify
            m = courseNoSpacePattern.matcher(partial);
            if (m.find()) {
                int end = nth + m.end();
                String skipped = line.substring(start, nth);
                sb.append(skipped);
                String found = line.substring(nth, end);
                subject = m.group(1);
                String num = m.group(2);
                String link = makeLink(subject, num, found);
                sb.append(link);
                nth = start = end;
                continue;
            }

            // Linkify
            m = jointPattern.matcher(partial);
            if (m.find()) {
                int end = nth + m.end();
                String skipped = line.substring(start, nth);
                sb.append(skipped);
                String found = line.substring(nth, end);
                subject = m.group(4);
                String num = m.group(5);
                String link = makeLink(subject, num, found);
                sb.append(link);
                nth = start = end;
                continue;
            }

            // Skip
            m = numRangePattern.matcher(partial);
            if (m.find()) {
                int end = nth + m.end();
                String skipped = line.substring(start, end);
                sb.append(skipped);
                nth = start = end;
                continue;
            }

            // Linkify, if subject already defined
            m = numPattern.matcher(partial);
            if (m.find()) {
                int end = nth + m.end();
                String skipped = line.substring(start, nth);
                sb.append(skipped);
                String found = line.substring(nth, end);
                String num = m.group(0);
                if (subject != null) {
                    String link = makeLink(subject, num, found);
                    sb.append(link);
                } else {
                    sb.append(found);
                }
                nth = start = end;
                continue;
            }

            nth++;

        }
        String skipped = line.substring(start, len);
        sb.append(skipped);
        return sb.toString();
    }

    public static String makeLink(String subject, String num, String text) {
        // Do not link (skip) 100, 200, 300, 400, etc level courses
        if (!num.endsWith("00")) {
            try {
                SearchRequestInfo searchRequest = new SearchRequestInfo("myplan.course.getCourseTitleAndId");
                searchRequest.addParam("subject", subject.trim());
                searchRequest.addParam("number", num.trim());
                searchRequest.addParam("lastScheduledTerm", DegreeAuditAtpHelper.getLastScheduledAtpId());


                SearchResult searchResult = getLuService().search(searchRequest, CONTEXT_INFO);
                for (SearchResultRow row : searchResult.getRows()) {
                    String courseId = getCellValue(row, "lu.resultColumn.cluId");
                    String title = getCellValue(row, "id.lngName");
                    String temp = String.format(link, courseId, subject.trim(), num.trim(), title, text);
                    return temp;
                }

            } catch (Exception e) {
                String msg = String.format("cannot linkify subject '%s' number '%s'", subject, num);
                logger.error(msg, e);
            }
        }
        return text;
    }


    private static String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }

    protected synchronized static CluService getLuService() {
        if (luService == null) {
            luService = (CluService) GlobalResourceLoader.getService(new QName(CluServiceConstants.CLU_NAMESPACE, "CluService"));
        }
        return luService;
    }

    public static void setLuService(CluService luService) {
        CourseLinkBuilder.luService = luService;
    }

    public synchronized void setCourseService(CluService luService) {
        this.luService = luService;
    }
}
