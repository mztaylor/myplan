package org.kuali.student.myplan.plan.util;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;

public interface EnrollmentStatusHelper {


    void setStudentServiceClient(StudentServiceClient studentServiceClient);


    void populateEnrollmentFields(ActivityOfferingDisplayInfo activity, String year, String quarter, String curric, String num, String sectionID) throws Exception;

}
