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

import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;
import org.kuali.student.myplan.course.dataobject.SavedCoursesItem;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CourseSearchForm extends UifFormBase {
    private static final long serialVersionUID = 4898118410378641665L;

    /** The key of the term drop-down any item. */
    public static final String SEARCH_TERM_ANY_ITEM = "any";

    /** Form fields */
    private boolean campusBothell;
    private boolean campusSeattle;
    private boolean campusTacoma;

    private String searchQuery;
    private String searchTerm = SEARCH_TERM_ANY_ITEM;

    /** Search results list. */
    private List<CourseSearchItem> courseSearchResults;

    /** Facet data lists */
    private List<FacetItem> curriculumFacetItems;
    private List<FacetItem> courseLevelFacetItems;
    private List<FacetItem> termsFacetItems;
    private List<FacetItem> genEduReqFacetItems;
    private List<FacetItem> creditsFacetItems;

    public CourseSearchForm() {
        super();
    }

    public List<org.kuali.student.myplan.course.dataobject.CourseSearchItem> getCourseSearchResults() {
        return courseSearchResults;
    }

    public void setCourseSearchResults(List<org.kuali.student.myplan.course.dataobject.CourseSearchItem> courseSearchResults) {
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

    public List<FacetItem> getTermsFacetItems() {
        return termsFacetItems;
    }

    public void setTermsFacetItems(List<FacetItem> termsFacetItems) {
        this.termsFacetItems = termsFacetItems;
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

    private String field1;
    private String field2;
    private String field3;

    public String getField1() {
        return this.field1;
    }

    public String getField2() {
//        return this.field2;
        return new Date().toString();
    }

    public String getField3() {
        return this.field3;
    }

    public void setField1(String field1) {
        this.field1 = field1;
    }

    public void setField2(String field2) {
        this.field2 = field2;
    }

    public void setField3(String field3) {
        this.field3 = field3;
    }

    public List<SavedCoursesItem> getSavedCoursesList()
    {
        List<SavedCoursesItem> list = new ArrayList<SavedCoursesItem>();
        {
            SavedCoursesItem item = new SavedCoursesItem();
            item.setCourseID( "abc" );
            item.setCode( "ENGL 101");
            item.setTitle( "English as a force multiplier" );
            item.setCredit( "(1-5, Max 10)" );
            list.add( item );
        }
        {
            SavedCoursesItem item = new SavedCoursesItem();
            item.setCourseID( "123" );
            item.setCode( "CHEM 451");
            item.setTitle( "Chemistry in our everyday lives" );
            item.setCredit( "(5)" );
            list.add( item );
        }
        {
            SavedCoursesItem item = new SavedCoursesItem();
            item.setCourseID( "xyz" );
            item.setCode( "HIST 101");
            item.setTitle( "History of the banana peeler" );
            item.setCredit( "(5)" );
            list.add( item );
        }
        {
            SavedCoursesItem item = new SavedCoursesItem();
            item.setCourseID( "yup" );
            item.setCode( "PHYS 301");
            item.setTitle( "Physics is what makes things go bang" );
            item.setCredit( "(5)" );
            list.add( item );
        }
        return list;

    }

}
