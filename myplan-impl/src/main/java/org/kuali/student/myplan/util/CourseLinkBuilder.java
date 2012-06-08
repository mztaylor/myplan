package org.kuali.student.myplan.util;

import org.apache.commons.lang.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;


/**
 * Helper class to parse lines of text and create links where course codes are found.
 */
public class CourseLinkBuilder {

    private static final Logger logger = Logger.getLogger(CourseLinkBuilder.class);

    private transient static LuService luService;

    public enum LINK_TEMPLATE {
        COURSE_DETAILS("<a onclick=\"openCourse('{params}', event);\" href=\"#\" title=\"{title}\">{label}</a>"),
        TEST("[{params}::{title}::{label}]");

        private final String templateText;
        LINK_TEMPLATE(String tt) {
            templateText = tt;
        }

        public String getTemplateText() {
            return templateText;
        }

    }
    //  This expression defines a curriculum code up to 8 characters long: "CHEM", "A A", "A&E", "A &E", "FRENCH"
    private static final String curriculumAbbreviationRegex = "[A-Z]{1}[A-Z &]{2,7}";

    //  Groups a course code by looking for a curriculum code (at the beginning of a line or preceded by " " or "(" )
    //  followed by any amount of white space, followed by three digits.
    //  Note: Tried really hard to match on word boundary here finally gave up.
    private static final String curriculumAbbreviationGroupRegex = String.format("(^|[ (])(%s)\\s*[0-9]{3}", curriculumAbbreviationRegex);
    private static final Pattern curriculumAbbreviationGroupPattern;

    //  Groups text from the beginning of a line to a curriculum code.
    private static final String prefixRegex = String.format("^(.*?)%s", curriculumAbbreviationGroupRegex);
    private static final Pattern prefixPattern;

    //  Groups a line of input into sub-sections based on the beginning of a course abbreviation.
    private static final String subLinesRegex = String.format("(%s.*)", curriculumAbbreviationGroupRegex);
    private static final Pattern subLinesPattern;

    //  Groups a list of course codes separated by / and followed by 3 digits: "CS&SS/SOC/STAT 221"
    private static final String curriculumAbbreviationListRegex = "([A-Z]{1}[A-Z &]{1,}/[A-Z &/]{2,}\\s*[0-9]{3})";
    private static final Pattern curriculumAbbreviationListPattern;

    //  Matches a course number range: "200-202"
    private static final String courseNumberRangeRegex = "\\d{3}-\\d{3}";
    private static final Pattern courseNumberRangePattern;

    //  Matches " OR " or " AND " preceded and followed by 3 digits.
    private static final String uppercaseOrAndRegex = "\\d{3} OR \\d{3}";
    private static final Pattern uppercaseOrAndPattern;

    //  Groups curriculum abbreviation with a three digit number not followed by a dash: "E&C N 123", "CH M 101 and CHEM 102", but not "CHEM 101-102"
    private static final String simpleCourseCodeRegex = String.format("(?<=^|[ (])(%s\\d{3})", curriculumAbbreviationRegex);
    private static final Pattern simpleCourseCodePattern;

    //  Matches three digit course numbers. This expression uses "look back" notation to say "Find 3 digit number not
    //  followed by a curriculum abbreviation". We don't want to match those because they would have already been processed
    //  by the simple course code matcher above and the final text replacement would match and expand it again.
    private static final String courseNumberRegex = String.format("(?<!%s)(\\d{3})", curriculumAbbreviationRegex);
    private static final Pattern courseNumberPattern;

    static {
        prefixPattern = Pattern.compile(prefixRegex);
        subLinesPattern = Pattern.compile(subLinesRegex);
        curriculumAbbreviationGroupPattern = Pattern.compile(curriculumAbbreviationGroupRegex);
        curriculumAbbreviationListPattern = Pattern.compile(curriculumAbbreviationListRegex);
        simpleCourseCodePattern = Pattern.compile(simpleCourseCodeRegex);
        courseNumberPattern = Pattern.compile(courseNumberRegex);
        courseNumberRangePattern = Pattern.compile(courseNumberRangeRegex);
        uppercaseOrAndPattern = Pattern.compile(uppercaseOrAndRegex);
    }

    /**
     * Parses requirements lines of degree audit reports and replaces course code text into course code links.
     *
     * This method allow a link template to be specified. This is to make testing easier.
     *
     * @param rawText A line of text to transform.
     * @param template The link template to use.
     * @return
     */
    public static String makeLinks(String rawText, LINK_TEMPLATE template) {
        /**
         * Break the line of text into sub-lines which begin with a course abbreviation.
         * It simplifies the regular expressions if they only expect to deal with a single course abbreviation
         * in a particular block of text.
         */
        StringBuilder out = new StringBuilder();
        try {
            List<String> subLines = makeSublines(rawText);
            //  Transform each line.
            for (String line : subLines) {
                out.append(parseSublines(line, template));
            }
        } catch (Exception e) {
            logger.error(String.format("Could not parse input [%s].", rawText), e);
            out = new StringBuilder(rawText);
        }
        return out.toString();
    }

    /**
     * Parses requirements lines of degree audit reports and replaces course code text into course code links.
     * @param rawText
     * @return
     */
    public static String makeLinks(String rawText) {
        return makeLinks(rawText, LINK_TEMPLATE.COURSE_DETAILS);
    }

    /**
     *  Matches regular expression against a line of text saving the matched text and the
     *  calculated link text into a hash map as a key/value pair. Once all regular expressions have
     *  been evaluated link substitutions are are made in the original text. The original text is never
     *  altered (except when it contains " OR ").
     *
     *  A fundamental assumption wihin this method is that the line of raw text only contains a single
     *  curriculum abbreviation or no curriculum abbreviation at all and therefore any 3 digit are
     *  assumed to belong to that curriculum abbreviation. If that constraint isn't followed the generated
     *  links might be inaccurate.
     */
    private static String parseSublines(String rawText, LINK_TEMPLATE template) {

        //  Storage for a list of substitutions. Using LinkedHashMap to preserve order.
        Map<String, String> placeHolders = new LinkedHashMap<String, String>();

        /*
         *  This match will short-circuit the method.
         */
        Matcher matcher = courseNumberRangePattern.matcher(rawText);
        if (matcher.find()) {
            return rawText;
        }

        //  Determine the curriculum abbreviation.
        //  If there is no match here then no further processing is required.
        matcher = curriculumAbbreviationGroupPattern.matcher(rawText);
        String curriculumAbbreviation = null;
        if (matcher.find()) {
            curriculumAbbreviation = matcher.group(2);
        } else {
            return rawText;
        }

        /*
         * Replace problematic "123 OR 234" syntax with "123 or 234".
         * This is the only place the original tampered with.
         */
        matcher = uppercaseOrAndPattern.matcher(rawText);
        boolean isUndoOrAnd = false;
        if (matcher.find()) {
            rawText = rawText.replace(" OR ", " or ");
            isUndoOrAnd = true;
        }

        //  Look for simple course codes.
		matcher = simpleCourseCodePattern.matcher(rawText);
		while (matcher.find()) {
			String courseCode = matcher.group(1);
            placeHolders.put(courseCode, makeLink(courseCode, courseCode, template));
		}

        //  Look for 3 digit numbers
        matcher = courseNumberPattern.matcher(rawText);
		while (matcher.find()) {
            String number = matcher.group(1);
            String courseCode = String.format("%s %s", curriculumAbbreviation, number);
            placeHolders.put(number, makeLink(courseCode, number, template));
		}

        //  Substitute plain text with links.
        for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
            rawText = rawText.replace(entry.getKey(), entry.getValue());
        }

        if (isUndoOrAnd) {
            rawText = rawText.replace(" or ", " OR ");
        }
        return rawText;
    }

    /**
     * Break up a line of text into sub-lines which begin with a course abbreviation.
     * @param rawText
     * @return
     */
    public static List<String> makeSublines(String rawText) {
        List<String> tokens = new ArrayList<String>();

        Matcher matcher = prefixPattern.matcher(rawText);
        //  Select the text that comes before any course codes.
        if (matcher.find()) {
            String pfx = matcher.group(1);
            if ( ! StringUtils.isEmpty(pfx)) {
                tokens.add(pfx);
            }
        }

        //  Get sub-lines by selecting curriculum codes and all text up to the next curriculum code.
        matcher = subLinesPattern.matcher(rawText);
        while (matcher.find()) {
            // Get all groups for this match
            String groupStr = matcher.group(1);
            tokens.add(groupStr);
        }

        if (tokens.size() == 0) {
            tokens.add(rawText);
        }

        return tokens;
    }

    /**
     * Build a link given a course code and a link template.
     *
     * @param courseCode
     * @param template
     * @return
     */
    private static String makeLink (String courseCode, String label, LINK_TEMPLATE template) {
        String courseTitle = "Unknown";
        String courseId = "unknown";

        //  Parse out the curriculum code and number.
        String number = courseCode.replaceAll("\\D*", "").trim();
        String code = courseCode.replaceAll("\\d*", "").trim();

        String link = null;

        if ( ! template.equals(LINK_TEMPLATE.TEST)) {
            try {
                Map<String, String> results = getCourseInfo(code, number);
                if (results.size() > 0) {
                    courseId = results.get("courseId");
                    if( courseId.contains( "\""))
                    {
                        courseId = courseId.replace("\"", "\\\"");
                    }
                    courseTitle = results.get("courseTitle");
                    courseTitle = courseTitle.replace("\"", "\\\"");
                } else {
                    link = label;
                }
            } catch (Exception e) {
                logger.error("Search for course info failed.", e);
                link = label;
            }
        }

        if (link == null) {
            link = template.getTemplateText()
                    .replace("{params}", courseId)
                    .replace("{title}", courseTitle)
                    .replace("{label}", label);
        }
        return link;
    }

    private static String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }

    private static synchronized Map<String, String> getCourseInfo(String curriculumCode, String courseNumber) {
        SearchRequest searchRequest = new SearchRequest("myplan.course.getCourseTitleAndId");
        searchRequest.addParam("subject", curriculumCode);
        searchRequest.addParam("number", courseNumber);

        SearchResult searchResult = null;
        try {
            searchResult = getLuService().search(searchRequest);
        } catch (MissingParameterException e) {
            throw new RuntimeException("Bad search.", e);
        }

        Map<String, String> result = new HashMap<String, String>();
        for (SearchResultRow row : searchResult.getRows()) {
            String courseId = getCellValue(row, "lu.resultColumn.cluId");
            String courseTitle = getCellValue(row, "id.lngName");
            result.put("courseId", courseId);
            result.put("courseTitle", courseTitle);
        }
        return result;
    }

    protected synchronized static LuService getLuService() {
        if (luService == null) {
            luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return luService;
    }

    public synchronized void setCourseService(LuService luService) {
        this.luService = luService;
    }
}
