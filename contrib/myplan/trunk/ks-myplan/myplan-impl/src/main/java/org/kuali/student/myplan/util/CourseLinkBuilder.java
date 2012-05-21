package org.kuali.student.myplan.util;

import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to parse links from audit text.
 */
public class CourseLinkBuilder {

    enum LINK_TEMPLATE {
        COURSE_DETAILS("<a href=\"/student/plan&param={params}\" title=\"{title}\">{label}</a>"),
        COURSE_SEARCH ("<a href=\"/student/course&searchParams={params}\" title=\"{title}\">{label}</a>"),
        TEST("[{params}::{title}::{label}]");

        private final String templateText;

        LINK_TEMPLATE(String tt) {
            templateText = tt;
        }

        public String getTemplateText() {
            return templateText;
        }
    }
                                                       //  q(?!u) to the string Iraq
    //  This expression defines a curriculum code: "CHEM", "A A", "A&E", "A &E", "FRENCH"
    private static final String courseAbbreviationRegex = "[A-Z]{1}[A-Z &]{2,7}";
    //  Groups a course code by looking for a curriculum code (at the beginning of a line or preceded by a space or "(" )
    //  followed by any amount of white space, followed by three digits.
    //  Note: Tried really hard to match on word boundary here finally gave up.
    private static final String courseAbbreviationGroupRegex = String.format("(^|[ (])(%s)\\s*[0-9]{3}", courseAbbreviationRegex);
    private static final Pattern courseAbbreviationPattern;

    //  Groups text from the beginning of a line to a curriculum code.
    private static final String prefixRegex = String.format("^(.*?)%s", courseAbbreviationGroupRegex);
    private static final Pattern prefixPattern;

    //  Groups a line of input into sub-sections based on the beginning of a course abbreviation.
    private static final String subLinesRegex = String.format("(%s.*)", courseAbbreviationGroupRegex);
    private static final Pattern subLinesPattern;

    //  Groups a list of course codes separated by / and followed by 3 digits: "CS&SS/SOC/STAT 221"
    private static final String curriculumAbbreviationListRegex = "([A-Z]{1}[A-Z &]{1,}/[A-Z &/]{2,}\\s*[0-9]{3})";
    private static final Pattern curriculumAbbreviationListPattern;

    private static final String courseNumberRangeRegex = "\\d{3}-\\d{3}";
    private static final Pattern courseNumberRangePattern;

    private static final String uppercaseOrAndRegex = "\\d{3}( OR | AND )\\d{3}";
    private static final Pattern uppercaseOrAndPattern;

    //  Groups curriculum abbreviation with a three digit number not followed by a dash: "E&C N 123", "CH M 101 and CHEM 102", but not "CHEM 101-102"
    private static final String simpleCourseCodeRegex = String.format("(%s\\d{3})", courseAbbreviationRegex);
    private static final Pattern simpleCourseCodePattern;

    //  Matches three digit course numbers. This expression uses "look back" notation to say "Find 3 digit number not
    //  followed by a curriculum abbreviation". We don't want to match those because they would have already been processed
    //  by the simple course code matcher above and the final text replacement would match and expand it again.
    private static final String courseNumberRegex = String.format("(?<!%s)(\\d{3})", courseAbbreviationRegex);
    private static final Pattern courseNumberPattern;

    static {
        prefixPattern = Pattern.compile(prefixRegex);
        subLinesPattern = Pattern.compile(subLinesRegex);
        courseAbbreviationPattern = Pattern.compile(courseAbbreviationGroupRegex);
        curriculumAbbreviationListPattern = Pattern.compile(curriculumAbbreviationListRegex);
        simpleCourseCodePattern = Pattern.compile(simpleCourseCodeRegex);
        courseNumberPattern = Pattern.compile(courseNumberRegex);
        courseNumberRangePattern = Pattern.compile(courseNumberRangeRegex);
        uppercaseOrAndPattern = Pattern.compile(uppercaseOrAndRegex);
    }

    /**
     * Parses requirements lines of degree audit reports and replaces course code text into course code links.
     * @param rawText A line of text to transform.
     * @param template The link template to use.
     * @return
     */
    public static String makeLinks(String rawText, LINK_TEMPLATE template) {
        //  Break the line of text into sub-lines which begin with a course abbreviation.
        //  It simplifies the regular expressions if they only expect to deal with a single course abbreviation
        //  in a particular block of text.
        List<String> subLines = makeSublines(rawText);
        StringBuilder out = new StringBuilder();
        //  Transform each line.
        for (String line : subLines) {
            out.append(insertLinks(line, template));
        }

        return out.toString();
    }

    private static String insertLinks(String rawText, LINK_TEMPLATE template) {
        /**
         *  Evaluate each regular expression against the supplied text saving the matched text and the
         *  calculated link text into a hash map as a key/value pair. Hash map insures a particular link
         *  only gets expanded once. Once all regular expressions have been evaluated link substitutions are
         *  are made in the original text. Not altering the original text until the end prevents dependencies
         *  between the regular expressions.
         */

        //  Storage for a list of substitutions. Using LinkedHashMap to preserve order.
        Map<String, String> placeHolders = new LinkedHashMap<String, String>();

        //  Look for curriculum abbreviation lists. Note the short-circuit here.
        Matcher matcher = curriculumAbbreviationListPattern.matcher(rawText);
        if (matcher.find()) {
            return rawText;
        }

        //  Look for course number range and short circuit.
        matcher = courseNumberRangePattern.matcher(rawText);
        if (matcher.find()) {
            return rawText;
        }

        //  Look for course number range and short circuit.
        matcher = uppercaseOrAndPattern.matcher(rawText);
        boolean isUndoOrAnd = false;
        if (matcher.find()) {
            rawText = rawText.replace(" OR ", " or ");
            rawText = rawText.replace(" AND ", " and ");
            isUndoOrAnd = true;
        }

        //  Determine the curriculum abbreviation.
        //  If there is no match here then no further processing is required.
        matcher = courseAbbreviationPattern.matcher(rawText);
        String curriculumAbbreviation = null;
        if (matcher.find()) {
            curriculumAbbreviation = matcher.group(1);
        } else {
            curriculumAbbreviation = "";
            //return rawText;
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
            rawText = rawText.replace(" and ", " AND ");
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
     * @param courseCode
     * @param template
     * @return
     */
    private static String makeLink (String courseCode, String label, LINK_TEMPLATE template) {
        return template.getTemplateText()
                    .replace("{params}", "p1")
                    .replace("{title}", "Title Text")
                    .replace("{label}", label);
    }
}
