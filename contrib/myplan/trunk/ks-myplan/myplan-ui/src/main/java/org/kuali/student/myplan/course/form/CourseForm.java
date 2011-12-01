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

import org.kuali.student.lum.course.dto.CourseInfo;

import java.util.Date;
import org.kuali.rice.krad.web.form.UifFormBase;


public class CourseForm extends UifFormBase {
    private static final long serialVersionUID = 4898118410378641665L;

    private Boolean campusBothell;
    
    private Boolean campusSeattle;
    
    private Boolean campusTacoma;
    
    private CourseInfo courseInfo;
    
    private String courseCode;
    
    private Date startDate;

    private Date endDate;

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public CourseForm() {
        super();
    }

    public CourseInfo getCourseInfo() {
        return this.courseInfo;
    }

    public void setCourseInfo(CourseInfo courseInfo) {
        this.courseInfo = courseInfo;
    }

	public String getCourseCode() {
		return courseCode;
	}

	public void setCourseCode(String courseCode) {
		this.courseCode = courseCode;
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
	
	
        
}
