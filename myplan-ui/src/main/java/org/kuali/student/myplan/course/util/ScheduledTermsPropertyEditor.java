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

import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import java.beans.PropertyEditorSupport;
import java.io.Serializable;
import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;

public class ScheduledTermsPropertyEditor extends CollectionListPropertyEditor {

    private final static Logger logger = Logger.getLogger(ScheduledTermsPropertyEditor.class);

    @Override
    public String getAsText() {

        Collection<Object> collection = (Collection<Object>) super.getValue();

        /*
         *  If the collection is empty and no empty list message is defined then return an empty string.
         *  Otherwise, determine the style class.
         */
        String styleClassNames = "";
        if (collection.isEmpty()) {
            if (this.emptyListMessage.length() == 0) {
                return "";
            } else {
                styleClassNames = getEmptyListStyleClassesAsString();
            }
        } else {
            styleClassNames = getStyleClassesAsString();
        }

        StringBuffer formattedText = new StringBuffer();
        formattedText.append("<" + listType.getListElementName() + " class=\"" + styleClassNames + "\">" );

        Element listElement = DocumentHelper.createElement(listType.getListElementName());
        listElement.addAttribute("class", styleClassNames);

        if (collection.isEmpty()) {
            Element itemElement = listElement.addElement(listType.getListItemElementName());
            itemElement.setText(this.emptyListMessage);
        } else {
            Iterator<Object> i = collection.iterator();
            while (i.hasNext()) {
                String term = (String) i.next();
                String elemTxt = term;

                // Convert Winter 2012 to WI 12
                Matcher m = CourseSearchConstants.TERM_PATTERN.matcher(term);
                if(m.matches()) {
                    elemTxt = m.group(1).substring(0,2).toUpperCase() + " " + m.group(2);
                }

                Element itemElement = listElement.addElement(listType.getListItemElementName());
                itemElement.setText(elemTxt);
            }
        }

        return listElement.asXML();
    }
}
