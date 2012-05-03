package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Iterator;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/3/12
 * Time: 10:48 AM
 * To change this template use File | Settings | File Templates.
 */
public class academicTermsMessageEditor extends CollectionListPropertyEditor {

    private final static Logger logger = Logger.getLogger(academicTermsMessageEditor.class);

     @Override
    protected String makeHtmlList(Collection c) {
        Iterator<Object> i = c.iterator();
        StringBuffer sb = new StringBuffer();
        while (i.hasNext()) {
            String term = (String) i.next();
            sb = sb.append("<dd class= >").append("<li>").append("You took this course on ").append("<a href=lookup?methodToCall=search&viewId=PlannedCourses-LookupView>").append(term).append("</a>").append("</li>");
        }
        return sb.toString();
    }
}
