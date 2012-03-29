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

import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.course.dataobject.CourseDetails;

import java.util.List;

public class PlanForm extends UifFormBase {
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

    //  Not implemented: Half way between a saved course and a planned course.
    private boolean backup;

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

    public PlanForm() {
        super();
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
}
