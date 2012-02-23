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


import java.beans.PropertyEditorSupport;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import org.apache.log4j.Logger;

public class CollectionListPropertyEditor extends PropertyEditorSupport implements Serializable {

    private final static Logger logger = Logger.getLogger(CollectionListPropertyEditor.class);

    //  Default parameter values.
    protected CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.DL;
    protected String emptyListMessage = "";

    private List<String> styleClasses;
    private List<String> emptyListStyleClasses;

    public CollectionListPropertyEditor() {
        styleClasses = new ArrayList<String>();
        emptyListStyleClasses = new ArrayList<String>();
    }

    @Override
    public void setValue(Object value) {
	    if (value == null) {
            logger.error("Collection was null.");
            return;
        }

        if ( ! (value instanceof Collection)) {
            logger.error(String.format("Value was thype [%s] instead of Collection.", value.getClass()));
            return;
        }

        super.setValue(value);
    }

    @Override
    public String getAsText() {
        //  Don't alter the collection.
        final Collection<Object> collection = (Collection<Object>) super.getValue();

        if (collection == null) {
            return "";
        }

        /*
         *  If the collection is empty and no empty list message is defined then return an empty string.
         *  Otherwise, add an empty list message to the list.
         */
        String styleClassNames = "";
        if (collection.isEmpty()) {
            if (this.emptyListMessage.length() == 0) {
                return "";
            } else {
                styleClassNames = getEmptyListStyleClassesAsString();
                collection.add(this.emptyListMessage);
            }
        } else {
            styleClassNames = getStyleClassesAsString();
        }

        StringBuffer formattedText = new StringBuffer();
        formattedText.append("<" + listType.getListElementName() + " class=\"" + styleClassNames + "\">" );

        Iterator<Object> i = collection.iterator();
        while (i.hasNext()) {
            Object elem = i.next();
            formattedText.append("<" + listType.getListItemElementName() + ">");
            formattedText.append(elem.toString());
            formattedText.append("</" + listType.getListItemElementName() + ">");
        }

        formattedText.append("</" + listType.getListElementName() + ">");
        return formattedText.toString();
    }

    /**
     * Specifies the type of list to render.
     *
     * @param listType
     */
    public void setListType(CollectionListPropertyEditorHtmlListType listType) {
        this.listType = listType;
    }

    public CollectionListPropertyEditorHtmlListType getListType() {
        return this.listType;
    }

    /**
     * Specifies the text to use in case the provided Collection is null or empty.
     * If left unset an empty string will be return by format().
     *
     * @param emptyListMessage
     */
    public void setEmptyListMessage(String emptyListMessage) {
        this.emptyListMessage = emptyListMessage;
    }

    public String getEmptyListMessage() {
        return this.emptyListMessage;
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
}
