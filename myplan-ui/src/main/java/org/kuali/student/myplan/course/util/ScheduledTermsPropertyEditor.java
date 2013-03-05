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
import org.kuali.student.myplan.course.dataobject.CourseOfferingInstitution;
import org.kuali.student.myplan.course.dataobject.CourseOfferingTerm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

public class ScheduledTermsPropertyEditor extends CollectionListPropertyEditor {

//    private final static Logger logger = Logger.getLogger(ScheduledTermsPropertyEditor.class);

    @Override
    protected String makeHtmlList(Collection c) {
    	
    	ArrayList<CourseOfferingInstitution> list = (ArrayList<CourseOfferingInstitution>) c;
    	StringBuilder sb = new StringBuilder();
    	for( CourseOfferingInstitution coi : list )
    	{
    		List<CourseOfferingTerm> terms = coi.getCourseOfferingTermList();
    		for( CourseOfferingTerm term : terms )
    		{
    			String text = term.getTerm();
    			
                // Convert Winter 2012 to WI 12
                Matcher m = CourseSearchConstants.TERM_PATTERN.matcher(text);
                if(m.matches()) {
                	text = m.group(1).substring(0,2).toUpperCase() + " " + m.group(2);
                }
                sb.append(wrapListItem(text));
    		}
    	}
    	return sb.toString();
    }
}
