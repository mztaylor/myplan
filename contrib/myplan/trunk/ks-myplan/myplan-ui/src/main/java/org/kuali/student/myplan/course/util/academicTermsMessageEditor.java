package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.student.myplan.plan.util.AtpHelper;

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
            String[] splitStr = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
            String atpId= AtpHelper.getAtpFromYearAndTerm(splitStr[0].trim(),splitStr[1].trim());
            sb = sb.append("<dd>").append("You took this course in ").append("<a href=lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(atpId).append(">").append(term).append("</a>");
        }
        return sb.toString();
    }
}
