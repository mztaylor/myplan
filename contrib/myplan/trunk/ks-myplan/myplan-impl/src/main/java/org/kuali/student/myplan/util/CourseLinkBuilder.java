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
        COURSE_DETAILS("<a href=\"/student/plan&param={params}\" title=\"{title}\">{label}</a>"),
        COURSE_SEARCH ("<a href=\"/student/course&searchParams={params}\" title=\"{title}\">{label}</a>");

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

        //  Match simple course codes.
		Matcher matcher = simpleCourseCodePattern.matcher(rawText);
		while (matcher.find()) {
			String m = matcher.group();
            placeHolders.put(m, makeLink("p1", "Title Text", m, template));
		}

        //  Substitute plain text with links.
        for (Map.Entry<String, String> entry : placeHolders.entrySet()) {
            rawText = rawText.replace(entry.getKey(), entry.getValue());
        }
        return rawText;
    }

    private static String makeLink (String paramText, String titleText, String linkText, LINK_TEMPLATE template) {
        return template.getTemplateText()
                    .replace("{params}", paramText)
                    .replace("{title}", titleText)
                    .replace("{label}", linkText);
    }
}
