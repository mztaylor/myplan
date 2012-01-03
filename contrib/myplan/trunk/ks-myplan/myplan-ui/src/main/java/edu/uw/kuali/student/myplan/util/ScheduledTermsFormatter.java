package edu.uw.kuali.student.myplan.util;

import org.apache.log4j.Logger;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.kuali.student.myplan.course.util.CollectionListFormatter;
import org.kuali.student.myplan.course.util.CollectionListFormatterHtmlListType;
import org.kuali.student.myplan.course.util.CourseSearchConstants;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Formats scheduled terms list in to a HTML data definition (<dl/>) list.
 */
public class ScheduledTermsFormatter extends CollectionListFormatter {

    private final static Logger logger = Logger.getLogger(ScheduledTermsFormatter.class);

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
            return "Not currently scheduled";
        }

        Element listElement = DocumentHelper.createElement(listType.getListElementName());
        listElement.addAttribute("class", this.styleClassName);



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
        return listElement.asXML();
    }
}

