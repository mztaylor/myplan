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
package edu.uw.myplan.trng.course.form;

import edu.uw.myplan.trng.course.dataobject.CourseSearchItem;
import edu.uw.myplan.trng.course.dataobject.FacetItem;
import org.kuali.rice.krad.web.form.UifFormBase;

import java.util.List;

public class CourseSearchForm extends UifFormBase {
    private static final long serialVersionUID = 4898118410378641665L;

    /** Form fields */
    private Boolean campusBothell;
    private Boolean campusSeattle;
    private Boolean campusTacoma;
    private String searchQuery;
    private String searchTerm;

    /** Search results list. */
    private List<CourseSearchItem> courseSearchResults;

    /** Facet data lists */
    private List<FacetItem> curriculumFacetItems;
    private List<FacetItem> courseLevelFacetItems;
    private List<FacetItem> timeScheduleFacetItems;
    private List<FacetItem> genEduReqFacetItems;
    private List<FacetItem> creditsFacetItems;

    public CourseSearchForm() {
        super();
    }

    public List<CourseSearchItem> getCourseSearchResults() {
        return courseSearchResults;
    }

    public void setCourseSearchResults(List<CourseSearchItem> courseSearchResults) {
        this.courseSearchResults = courseSearchResults;
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

    public List<FacetItem> getCurriculumFacetItems() {
        return curriculumFacetItems;
    }

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }

    public void setCurriculumFacetItems(List<FacetItem> curriculumFacetItems) {
        this.curriculumFacetItems = curriculumFacetItems;
    }

    public List<FacetItem> getCourseLevelFacetItems() {
        return courseLevelFacetItems;
    }

    public void setCourseLevelFacetItems(List<FacetItem> courseLevelFacetItems) {
        this.courseLevelFacetItems = courseLevelFacetItems;
    }

    public List<FacetItem> getTimeScheduleFacetItems() {
        return timeScheduleFacetItems;
    }

    public void setTimeScheduleFacetItems(List<FacetItem> timeScheduleFacetItems) {
        this.timeScheduleFacetItems = timeScheduleFacetItems;
    }

    public List<FacetItem> getGenEduReqFacetItems() {
        return genEduReqFacetItems;
    }

    public void setGenEduReqFacetItems(List<FacetItem> genEduReqFacetItems) {
        this.genEduReqFacetItems = genEduReqFacetItems;
    }

    public List<FacetItem> getCreditsFacetItems() {
        return creditsFacetItems;
    }

    public void setCreditsFacetItems(List<FacetItem> creditsFacetItems) {
        this.creditsFacetItems = creditsFacetItems;
    }
}
