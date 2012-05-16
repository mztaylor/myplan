package org.kuali.student.myplan.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class to parse links from audit text.
 */
public class CourseLinkBuilder {

    enum LINK_TEMPLATE {
        COURSE_DETAILS("<a href=\"/student/plan&param={href}\" title=\"{title}\">{label}</a>"),
        COURSE_SEARCH ("<a href=\"/student/course&searchParams={href}\" title=\"{titel}\">{label}</a>");

        private final String templateText;

        LINK_TEMPLATE(String tt) {
            templateText = tt;
        }

        public String getTemplateText() {
            return templateText;
        }
    }

    //  Matches "EC& N 456"
    private static final String simpleCourseCode = "([A-Z]{1}[A-Z\\ \\&]{1,5}\\d{3})";
    private static final Pattern simpleCourseCodePattern;

    static {
        simpleCourseCodePattern = Pattern.compile(simpleCourseCode);
    }

    public static String insertLinks(String rawText, LINK_TEMPLATE template) {
        /**
         *  Evaluate each regular expression against the supplied text saving the matched text and the
         *  calculated link text into a hash map as a key/value pair. Hash map insures a particular link
         *  only gets expanded once. Once all regular expressions have been evaluated link substitutions are
         *  are made in the original text. Not altering the original text until the end prevents dependencies
         *  between the regular expressions.
         */

        //  Storage for a list of substitutions. Using LinkedHashMap to preserve order.
        Map<String, String> placeHolders = new LinkedHashMap<String, String>();

		Matcher matcher = simpleCourseCodePattern.matcher(rawText);
		while (matcher.find()) {
			String m = matcher.group();
            String url  = template.getTemplateText()
                    .replace("{href}", "p1")
                    .replace("{title}", "Title Text")
                    .replace("{label}", m);
            placeHolders.put(m, url);
		}

        //
        for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
            rawText = rawText.replace(entry.getKey(), entry.getValue());
        }

        return rawText;
    }
}
