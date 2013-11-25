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
import org.kuali.student.myplan.plan.util.AtpHelper;

import java.beans.PropertyEditorSupport;
import java.security.PrivateKey;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

public class ScheduledTermsPropertyEditor extends PropertyEditorSupport {
    protected CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.DL;

    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

    /*TODO: KUALI-RICE: 6286 Upgrade to list with rice 2.2.1*/
    @Override
    public String getAsText() {
        CourseSummaryDetails courseSummaryDetails = (CourseSummaryDetails) super.getValue();
        StringBuffer formattedText = new StringBuffer();
        formattedText.append(String.format("<%s class=\"scheduled\">", listType.getListElementName()));
        if (courseSummaryDetails != null && courseSummaryDetails.getScheduledTerms() != null && courseSummaryDetails.getScheduledTerms().size() > 0) {
            for (Object term : courseSummaryDetails.getScheduledTerms()) {
                String text = term.toString();
                // Convert Winter 2012 to WI 12
                text = AtpHelper.termToYearTerm(text).toShortTermName();

                formattedText.append(String.format("<%s class=\"%s\">%s</%s>", listType.getListItemElementName(), text.replaceAll("\\d*$", "").trim(), text, listType.getListItemElementName()));
            }
        } else {
            formattedText.append("Not currently scheduled");
        }
        formattedText.append(String.format("</%s>", listType.getListElementName()));
        return formattedText.toString();
    }

}
