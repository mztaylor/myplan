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
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;

import java.beans.PropertyEditorSupport;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

public class ScheduledTermsPropertyEditor extends PropertyEditorSupport {
    protected CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.DL;
    protected String emptyListMessage = "";
    private List<String> styleClasses;
    private boolean applyClassOnItem = false;

    public String getEmptyListMessage() {
        return emptyListMessage;
    }

    public void setEmptyListMessage(String emptyListMessage) {
        this.emptyListMessage = emptyListMessage;
    }

    public List<String> getStyleClasses() {
        return styleClasses;
    }

    public void setStyleClasses(List<String> styleClasses) {
        this.styleClasses = styleClasses;
    }

    public boolean isApplyClassOnItem() {
        return applyClassOnItem;
    }

    public void setApplyClassOnItem(boolean applyClassOnItem) {
        this.applyClassOnItem = applyClassOnItem;
    }

    public String getStyleClassesAsString() {
        if (styleClasses != null) {
            return StringUtils.join(styleClasses, " ");
        }
        return "";
    }

    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

    /*TODO: KUALI-RICE: 6286 Upgrade to list with rice 2.2.1*/
    @Override
    public String getAsText() {
        CourseSummaryDetails courseSummaryDetails = (CourseSummaryDetails) super.getValue();
        StringBuffer formattedText = new StringBuffer();
        formattedText.append("<" + listType.getListElementName() + " class=\"" + getStyleClassesAsString() + "\">");
        if (courseSummaryDetails != null && courseSummaryDetails.getScheduledTerms() != null && courseSummaryDetails.getScheduledTerms().size() > 0) {
            formattedText.append(makeHtmlList(courseSummaryDetails.getScheduledTerms()));
        }else {
            formattedText.append("Not currently scheduled");
        }
        formattedText.append("</" + listType.getListElementName() + ">");
        return formattedText.toString();
    }

    protected String makeHtmlList(Collection c) {
        StringBuilder sb = new StringBuilder();
        for (Object item : c) {
            String text = item.toString();
            // Convert Winter 2012 to WI 12
            Matcher m = CourseSearchConstants.TERM_PATTERN.matcher(text);
            if (m.matches()) {
                text = m.group(1).substring(0, 2).toUpperCase() + " " + m.group(2);
            }
            sb.append(wrapListItem(text));
        }
        return sb.toString();
    }

    protected String wrapListItem(String value) {
        StringBuilder elementText = new StringBuilder();

        // Strip out any numbers from the value and use the text for class on the list item
        // Apply style class on item only if specified to do so
        String liClass = (applyClassOnItem && value != emptyListMessage) ? " class=\"" + value.replaceAll("\\d*$", "").trim() + "\"" : "";
        elementText.append("<" + listType.getListItemElementName() + liClass + ">");
        elementText.append(value);
        elementText.append("</" + listType.getListItemElementName() + ">");
        return elementText.toString();
    }
}
