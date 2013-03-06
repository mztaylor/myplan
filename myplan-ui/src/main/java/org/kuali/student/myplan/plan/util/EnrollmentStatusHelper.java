package org.kuali.student.myplan.plan.util;

import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;

public interface EnrollmentStatusHelper {


//    void setStudentServiceClient(StudentServiceClient studentServiceClient);


    public void populateEnrollmentFields(ActivityOfferingItem activity, String year, String quarter, String curric, String num, String sectionID) throws Exception;

}
