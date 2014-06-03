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
package org.kuali.student.myplan.course.form;

import org.kuali.rice.krad.web.form.UifFormBase;

import org.kuali.student.myplan.course.util.CourseSearchConstants;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CourseSearchForm extends UifFormBase {
    private static final long serialVersionUID = 4898118410378641665L;

    /**
     * Form fields
     */
    private boolean campusBothell;
    private boolean campusSeattle;
    private boolean campusTacoma;

    private String searchQuery;
    private String searchTerm = CourseSearchConstants.SEARCH_TERM_ANY_ITEM;
    private List<String> campusSelect;

    private String startTime;
    private String endTime;

    private List<String> selectedDays;

    private Map<String,List<String>> meetingFacets;

    public List<String> getCampusSelect() {
        return campusSelect;
    }

    public void setCampusSelect(List<String> campusSelect) {
        this.campusSelect = campusSelect;
    }

    public CourseSearchForm() {
        super();
    }

    public Boolean getCampusBothell() {
        return campusBothell;
    }

    public void setCampusBothell(Boolean campusBothell) {
        this.campusBothell = campusBothell;
    }

    public Boolean getCampusSeattle() {
        return campusSeattle;
    }

    public void setCampusSeattle(Boolean campusSeattle) {
        this.campusSeattle = campusSeattle;
    }

    public Boolean getCampusTacoma() {
        return campusTacoma;
    }

    public void setCampusTacoma(Boolean campusTacoma) {
        this.campusTacoma = campusTacoma;
    }

    public String getSearchQuery() {
        return searchQuery;
    }

    public void setSearchQuery(String searchQuery) {
        this.searchQuery = searchQuery;
    }

    public String getSearchTerm() {
        return searchTerm;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public List<String> getSelectedDays() {
        return selectedDays;
    }

    public void setSelectedDays(List<String> selectedDays) {
        this.selectedDays = selectedDays;
    }

    public Map<String, List<String>> getMeetingFacets() {
        if(meetingFacets == null){
            meetingFacets = new HashMap<String, List<String>>();
        }
        return meetingFacets;
    }

    public void setMeetingFacets(Map<String, List<String>> meetingFacets) {
        this.meetingFacets = meetingFacets;
    }
}
