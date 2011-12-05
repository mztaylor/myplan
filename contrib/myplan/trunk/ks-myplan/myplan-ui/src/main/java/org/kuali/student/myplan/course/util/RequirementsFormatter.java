package org.kuali.student.myplan.course.util;

import org.kuali.rice.core.web.format.Formatter;

import java.util.Collection;
import java.util.Iterator;

/**
 * This class is used to format Requirements String Lists. Eventually this should be changed with the HTML List formatter from RIce
 * User: kmuthu
 * Date: 12/5/11
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class RequirementsFormatter extends Formatter {

    private static final long serialVersionUID = 1074862354812893254L;

    /**
     * Formats a collection into a string that looks like an array.
     *
     * @see org.kuali.rice.core.web.format.Formatter#format(java.lang.Object)
     */
    @Override
    public Object format(Object value) {
        StringBuilder buf = new StringBuilder();
        buf.append("<ul>");

        if (!(value instanceof Collection)) {
            throw new IllegalArgumentException("value was not an instance of Collection, instead was: " + (value == null ? null : value.getClass()));
        }

        @SuppressWarnings("unchecked")
        Collection<Object> collection = (Collection<Object>) value;
        Iterator<Object> i = collection.iterator();
        boolean hasNext = i.hasNext();
        while (hasNext) {
            Object elem = i.next();
            buf.append("<ul>");
            buf.append(elem.toString());
            buf.append("</ul>");

            hasNext = i.hasNext();
        }
        buf.append("</ul>");
        return buf.toString();
    }

}
