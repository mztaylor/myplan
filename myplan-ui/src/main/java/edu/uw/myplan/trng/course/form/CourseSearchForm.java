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
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.lum.course.dto.CourseInfo;

import java.util.List;


public class CourseSearchForm extends UifFormBase {
    private static final long serialVersionUID = 4898118410378641665L;

    private Boolean campusBothell;

    private Boolean campusSeattle;

    private Boolean campusTacoma;

    private String searchQuery;

    private String searchTerm;


    private List<CourseSearchItem> courseSearchResults;


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

    public void setSearchTerm(String searchTerm) {
        this.searchTerm = searchTerm;
    }
}
