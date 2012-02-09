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
import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

public class CollectionListPropertyEditor extends PropertyEditorSupport implements Serializable {

    private final static Logger logger = Logger.getLogger(CollectionListPropertyEditor.class);

    //  Default parameter values.
    protected CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.DL;
    protected String emptyListMessage = "";
    protected String styleClassName = "kr-collection-list";

    @Override
    public void setValue(Object value) {
	    if (value == null) {
            throw new IllegalArgumentException("Collection was null.");
        }

        if ( ! (value instanceof Collection)) {
            throw new IllegalArgumentException("Value was not an instance of Collection, instead was: "
                    + (value == null ? null : value.getClass()));
        }
        super.setValue(value);
    }

    @Override
    public String getAsText() {
        Collection<Object> collection = (Collection<Object>) super.getValue();

        /*
         *  If the collection is empty and no empty list message is defined then return an empty string.
         *  Otherwise, add an empty list message to the list.
         */
        if (collection.isEmpty()) {
            if (this.emptyListMessage.length() == 0) {
                return "";
            } else {
                collection.add(this.emptyListMessage);
            }
        }

        StringBuffer formattedText = new StringBuffer();
        formattedText.append("<" + listType.getListElementName() + " class=\"" + this.styleClassName + "\">" );

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

    /**
     * Set the CCS class name of the HTML list.
     * @param styleClassName
     */
    public void setStyleClassName(String styleClassName) {
        this.styleClassName = styleClassName;
    }

    public String getStyleClassName() {
        return this.styleClassName;
    }
}
