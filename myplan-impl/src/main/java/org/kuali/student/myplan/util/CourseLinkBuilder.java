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

    //  This expression defines a curriculum code: "CHEM", "A A", "A&E", "A &E", "FRENCH"
    private static final String courseAbbreviationRegex = "[A-Z]{1}[A-Z &]{2,7}";
    //  Groups a course code by looking for a curriculum code followed by any amount of white space, followed by three digits.
    private static final String courseAbbreviationGroupRegex = String.format("(%s)\\s*[0-9]{3}", courseAbbreviationRegex);
    private static final Pattern courseAbbreviationPattern;

    //  Groups text from the beginning of a line to a curriculum code.
    private static final String prefixRegex = String.format("^(.*?)%s", courseAbbreviationGroupRegex);
    private static final Pattern prefixPattern;

    //  Groups a line of input into sub-sections based on the beginning of a course abbreviation.
    //private static final String subLinesRegex = String.format("(%s([a-z0-9 \\-&,/:.]|And)*)", courseAbbreviationGroupRegex, courseAbbreviationRegex);
    private static final String subLinesRegex = String.format("(%s.*)", courseAbbreviationGroupRegex);
    private static final Pattern subLinesPattern;

    //  Groups curriculum abbreviation with a three digit number after it: "E&C N 123", "CH M 101 and CHEM 102"
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
        simpleCourseCodePattern = Pattern.compile(simpleCourseCodeRegex);
        courseNumberPattern =  Pattern.compile(courseNumberRegex);
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

        //  Determine the curriculum abbreviation.
        //  If there is no match here then no further processing is required.
        Matcher matcher = courseAbbreviationPattern.matcher(rawText);
        String curriculumAbbreviation = null;
        if (matcher.find()) {
			curriculumAbbreviation = matcher.group(1);
		} else {
            return rawText;
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
            //  Drop "," from the end of lines. It's probably possible to get this effect in the regex. Doing it this way for the sake of simplicity and timeliness.
            //groupStr = groupStr.replaceAll(",$", "");
            tokens.add(groupStr);
        }

        //matcher = suffixPattern.matcher(rawText);
        //  Select the text that comes after the last course number.
        //if (matcher.find()) {
        //    String suffix = matcher.group(1);
        //    if ( ! StringUtils.isEmpty(suffix)) {
        //        tokens.add(suffix);
        //    }
        //}
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
