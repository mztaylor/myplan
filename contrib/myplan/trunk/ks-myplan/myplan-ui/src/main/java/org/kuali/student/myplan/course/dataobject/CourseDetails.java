package org.kuali.student.myplan.course.dataobject;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseSummary;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * This data object records all instances where the course has been planned,
 * bookmarked, recommended or present in academic records of the student
 */
public class CourseDetails {

    private CourseSummaryDetails courseSummaryDetails;

    private List<CourseOfferingInstitution> courseOfferingInstitutionList;

    private PlannedCourseSummary plannedCourseSummary;


    public void CourseDetails() {
        plannedCourseSummary = new PlannedCourseSummary();
    }


    public CourseSummaryDetails getCourseSummaryDetails() {
        return courseSummaryDetails;
    }

    public void setCourseSummaryDetails(CourseSummaryDetails courseSummaryDetails) {
        this.courseSummaryDetails = courseSummaryDetails;
    }

    public List<CourseOfferingInstitution> getCourseOfferingInstitutionList() {
        if (courseOfferingInstitutionList == null) {
            courseOfferingInstitutionList = new ArrayList<CourseOfferingInstitution>();
        }
        return courseOfferingInstitutionList;
    }

    public void setCourseOfferingInstitutionList(List<CourseOfferingInstitution> courseOfferingInstitutionList) {
        this.courseOfferingInstitutionList = courseOfferingInstitutionList;
    }


    public PlannedCourseSummary getPlannedCourseSummary() {
        return plannedCourseSummary;
    }

    public void setPlannedCourseSummary(PlannedCourseSummary plannedCourseSummary) {
        this.plannedCourseSummary = plannedCourseSummary;
    }

    //TODO: Review why we really need this
    //  It's because we need access to more than on property in one of the property editors.
    @JsonIgnore
    public CourseDetails getThis() {
        return this;
    }

    //  Using this as a property for the crudMessageMatrixFormatter property editor
    // because getThis() is already used for the timeschedule property editor
    // so it overides crudmessage with timeschedule if we use  the same property .
    @JsonIgnore
    public CourseDetails getDetails() {
        return this;
    }

    //  Using this as a property for the scheduleterms property editor
    // because courseofferinginstitution  is already used for the scheduled terms property editor
    //In order to use the same list for another property we created this.
    @JsonIgnore
    public List<CourseOfferingInstitution> getInstitutionsList() {
        return this.getCourseOfferingInstitutionList();
    }


}