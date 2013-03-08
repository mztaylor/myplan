package org.kuali.student.myplan.course.util;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import org.dom4j.DocumentException;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.AtpHelper;

import java.util.LinkedHashMap;

public interface CourseHelper {


    public LinkedHashMap<String, LinkedHashMap<String, Object>> getAllSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> mapmap, AtpHelper.YearTerm yt,
                                                                                    String curric, String num) throws ServiceException, DocumentException;

    public void populateEnrollmentFields(ActivityOfferingItem activity, String year, String quarter, String curric, String num, String sectionID) throws Exception;

    public DeconstructedCourseCode getCourseDivisionAndNumber(String courseCode);

    public String getCourseId(String subjectArea, String number);

}
