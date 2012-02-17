package org.kuali.student.myplan.course.util;
/**
 * Copyright 2005-2012 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.student.myplan.course.dataobject.CourseDetails;

import java.beans.PropertyEditorSupport;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TimeScheduleLinksListPropertyEditor extends PropertyEditorSupport implements Serializable {

    private final static Logger logger = Logger.getLogger(TimeScheduleLinksListPropertyEditor.class);

    private String baseUrl = "";
    private String label = "";
    private String title = label;

    private List<String> styleClasses;

    private List<String> emptyListStyleClasses;
    private final static CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.UL;

    public TimeScheduleLinksListPropertyEditor() {
        styleClasses = new ArrayList<String>();
        emptyListStyleClasses = new ArrayList<String>();
    }

    @Override
    public void setValue(Object value) {
	    if (value == null) {
            throw new IllegalArgumentException("CourseDetails object was null.");
        }

        if ( ! (value instanceof CourseDetails)) {
            throw new IllegalArgumentException("Value was not an instance of CourseDetails, instead was: "
                    + (value == null ? null : value.getClass()));
        }
        super.setValue(value);
    }

    @Override
    public String getAsText() {
        CourseDetails courseDetails = (CourseDetails) super.getValue();

        /*
         *  If the collection is empty and no empty list message is defined then return an empty string.
         *  Otherwise, add an empty list message to the list.
         */
        String styleClassNames = getEmptyListStyleClassesAsString();

        List<String> scheduledTerms = courseDetails.getScheduledTerms();

        StringBuffer formattedText = new StringBuffer();
        formattedText.append("<" + listType.getListElementName() + " class=\"" + styleClassNames + "\">" );

        for (String scheduledTerm : scheduledTerms) {
            String url = makeUrl(scheduledTerm, courseDetails.getCode());
            formattedText
                .append("<" + listType.getListItemElementName() + ">")
                .append("<a href=\"" + url + "' title='" + title + "\">")
                .append(label)
                .append(" ")
                .append(scheduledTerm)
                .append("</a>")
                .append("</" + listType.getListItemElementName() + ">");
        }

        formattedText.append("</" + listType.getListElementName() + ">");
        return formattedText.toString();
    }

    /**
     * Format a UW SWS URL as: SPR2012/chem.html#chem142
     *                         term/curriculmum_abbreviation#curriculmum_abbreviation + course number
     *
     * @param term
     * @param courseCode
     * @return
     */
    private String makeUrl(String term, String courseCode) {
        StringBuilder url = new StringBuilder(baseUrl);
        //  Convert term to SWS format
        String swsTerm = "SPR2012";
        url.append(swsTerm).append("/");

        // SPR2012/chem.html#chem142

        String curriculumAbbreviation = "chem";
        url.append(curriculumAbbreviation);
        url.append(".html#");
        url.append(curriculumAbbreviation);
        String courseNumber = "142";
        url.append(courseNumber);
        return url.toString();
    }

    public List<String> getStyleClasses() {
        return this.styleClasses;
    }

    public void setStyleClasses(List<String> styleClasses) {
        this.styleClasses = styleClasses;
    }

    public String getStyleClassesAsString() {
        if (styleClasses != null) {
            return StringUtils.join(styleClasses, " ");
        }
        return "";
    }

    public List<String> getEmptyListStyleClasses() {
        return this.emptyListStyleClasses;
    }

    public void setEmptyListStyleClasses(List<String> styleClasses) {
        this.emptyListStyleClasses = styleClasses;
    }

    public String getEmptyListStyleClassesAsString() {
        if (emptyListStyleClasses != null) {
            return StringUtils.join(emptyListStyleClasses, " ");
        }
        return "";
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }
}
