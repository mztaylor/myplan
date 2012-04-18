/* Copyright 2011 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.student.myplan.plan.form;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.util.PlanConstants;

import java.util.Map;

/**
 * Form for all plan item actions.
 */
public class PlanForm extends UifFormBase {
    private final Logger logger = Logger.getLogger(PlanForm.class);

    /**
     *  The return code for a plan item add, change, or delete.
     */
    public enum REQUEST_STATUS {
        /*  The requested operation was successful. */
        SUCCESS,
        /*  The requested operation was unnecessary (e.g. the plan item was already deleted) */
        NOOP,
        /* The requested operation failed. */
        FAILURE
    }

    /**
     * Storage for the status of a request for add, change, or delete of a plan item.
     */
    private REQUEST_STATUS requestStatus;

    //  Saved courses params.
    private String planItemId;

    private String courseId;
    private CourseDetails courseDetails;

    //  Form fields.
    private String termsList;

    private boolean other = false;

    //  Additional fields needed for the Other option.
    private String term;
    private String year;

    //   Form checkbox to determine plan item type (planned or backup).
    private boolean backup;

    /**
     *  A list of javascript events as:
     *      EVENT_NAME
     *          param1: p1
     *          param2: p2
     *      PLAN_ITEM_ADDED
     *          itemType: plannedCourse
     *          planItem: pi1
     *          courseId: c1
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String,String>> javascriptEvents;

    private ObjectMapper mapper = new ObjectMapper();

    public PlanForm() {
        super();
    }

    public Map<PlanConstants.JS_EVENT_NAME, Map<String,String>> getJavascriptEvents() {
        return javascriptEvents;
    }

    public void setJavascriptEvents(Map<PlanConstants.JS_EVENT_NAME, Map<String,String>> javascriptEvents) {
        this.javascriptEvents = javascriptEvents;
    }

    public String getTermsList() {
        return termsList;
    }

    public void setTermsList(String termsList) {
        this.termsList = termsList;
    }

    public boolean isOther() {
        return other;
    }

    public void setOther(boolean other) {
        this.other = other;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(String planItemId) {
        this.planItemId = planItemId;
    }

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public String getYear() {
        return year;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public CourseDetails getCourseDetails() {
        return this.courseDetails;
    }

    public void setCourseDetails(CourseDetails courseDetails) {
        this.courseDetails = courseDetails;
    }

    public REQUEST_STATUS getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(REQUEST_STATUS requestStatus) {
        this.requestStatus = requestStatus;
    }

    /**
     *  Returns the list of events that should be
     */
    public String getJavascriptEventsAsJSON() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonOut = null;
        try {
            //  Turn the list of javascript events into a string of JSON.
            jsonOut = mapper.writeValueAsString(javascriptEvents);
        } catch (Exception e) {
            logger.error("Could not convert javascript events to JSON.", e);
            jsonOut = "";
        }

        //  TODO: Determine if there is a config that can be set to avoid having to do this.
        jsonOut = jsonOut.replaceAll("\"\\{", "{");
        jsonOut = jsonOut.replaceAll("}\"", "}");

        return jsonOut;
    }
}
