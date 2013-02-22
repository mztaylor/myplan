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
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.student.myplan.course.dataobject.CourseOfferingInstitution;
import org.kuali.student.myplan.course.dataobject.CourseOfferingTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.beans.PropertyEditorSupport;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Matcher;

public class ScheduledTermsPropertyEditor extends CollectionListPropertyEditor {

    private final static Logger logger = Logger.getLogger(ScheduledTermsPropertyEditor.class);

    private static String DLClass = "<dl class=\"scheduled\"> %s </dl>";

    private static String DDClass = "<dd class=\"%s\">%s</dd>";

    protected boolean fullLinks = false;

    public boolean isFullLinks() {
        return fullLinks;
    }

    public void setFullLinks(boolean fullLinks) {
        this.fullLinks = fullLinks;
    }

    @Override
    public String getAsText() {
        /*Logic to build the scheduled terms for given course offering institutions course offering terms. 
        *
        * if fullLinks boolean is false then the scheduled term badges are returned
        * returns following string if the course is scheduled for Summer 2013.
        * <dl class="scheduled"><dd class="SU"> SU 13 </dd></dl>
        *
        * if fullLinks boolean is true then the scheduled term links are returned
        * returns following string if the course is scheduled for Winter 2013 , Spring 2013.
        * view all sections for Winter 2013, Spring 2013.
        * */

        String courseId = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getParameter("courseId");
        StringBuilder sb = new StringBuilder();
        List<CourseOfferingInstitution> courseOfferingInstitutions = (List<CourseOfferingInstitution>) super.getValue();
        Map<String, String> scheduledTerms = new HashMap<String, String>();
        int counter = 0;
        for (CourseOfferingInstitution courseOfferingInstitution : courseOfferingInstitutions) {
            for (CourseOfferingTerm courseOfferingTerm : courseOfferingInstitution.getCourseOfferingTermList()) {
                if (scheduledTerms.get(AtpHelper.getAtpIdFromTermYear(courseOfferingTerm.getTerm())) == null) {
                    scheduledTerms.put(AtpHelper.getAtpIdFromTermYear(courseOfferingTerm.getTerm()), String.format("%s:%s", courseOfferingTerm.getTerm(), courseOfferingInstitution.getCode()));
                }
            }

        }
        if (scheduledTerms.size() > 0) {
            SortedSet<String> sortedKeys = new TreeSet<String>(scheduledTerms.keySet());
            for (String scheduledTerm : sortedKeys) {
                String[] values = scheduledTerms.get(scheduledTerm).split(":");
                String term = values[0];
                String instCode = values[1];
                String serverUrl = ConfigContext.getCurrentContextConfig().getProperty(CourseSearchConstants.APP_URL);
                String atpId = AtpHelper.getAtpIdFromTermYear(term).replace(".", "-");
                if (!fullLinks) {
                    Matcher m = CourseSearchConstants.TERM_PATTERN.matcher(term);
                    if (m.matches()) {
                        String elemTxt = m.group(1).substring(0, 2).toUpperCase() + " " + m.group(2);
                        String link = String.format(CourseSearchConstants.LINK, serverUrl, courseId, instCode, atpId, elemTxt);
                        sb.append(String.format(DDClass, elemTxt, link));
                    }
                } else {
                    String link = String.format(CourseSearchConstants.LINK, serverUrl, courseId, instCode, atpId, term);
                    if (counter == 0) {
                        sb.append(String.format("View all sections for %s", link));
                        counter++;

                    } else if (counter > 0 && !(counter == sortedKeys.size() - 1)) {
                        sb.append(String.format(", %s", link));
                        counter++;

                    } else if (counter > 0 && (counter == sortedKeys.size() - 1)) {
                        sb.append(String.format(" and %s", link));
                        counter++;
                    }

                }
            }

        }
        return String.format(DLClass, sb.toString());
    }

}

