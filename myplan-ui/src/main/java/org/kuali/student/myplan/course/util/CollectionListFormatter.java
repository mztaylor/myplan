package org.kuali.student.myplan.course.util;


import org.kuali.rice.core.web.format.Formatter;

import java.util.Collection;
import java.util.Iterator;

import org.apache.log4j.Logger;

/**
 * Formats a collection in to a HTML data definition (<dl/>) list.
 */
@Deprecated
public class CollectionListFormatter extends Formatter {

    private final static Logger logger = Logger.getLogger(CollectionListFormatter.class);

    //  Default paramater values.
    protected CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.DL;
    protected String emptyListMessage = "";
    protected String styleClassName = "kr-collection-list";

    /**
     *  Formats a collection as an HTML list.
     */
    @Override
    public Object format(Object value) {
        if (value == null) {
            throw new IllegalArgumentException("Collection was null.");
        }

        if ( ! (value instanceof Collection)) {
            throw new IllegalArgumentException("value was not an instance of Collection, instead was: " + (value == null ? null : value.getClass()));
        }

        Collection<Object> collection = (Collection<Object>) value;

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

