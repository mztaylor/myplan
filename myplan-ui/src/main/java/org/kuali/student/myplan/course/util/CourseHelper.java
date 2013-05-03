package org.kuali.student.myplan.course.util;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import org.dom4j.DocumentException;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.AtpHelper;

import java.util.LinkedHashMap;

public interface CourseHelper {


    public LinkedHashMap<String, LinkedHashMap<String, Object>> getAllSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> mapmap, AtpHelper.YearTerm yt,
                                                                                    String curric, String num) throws ServiceException, DocumentException;

    public DeconstructedCourseCode getCourseDivisionAndNumber(String courseCode);

    public String getCourseId(String subjectArea, String number);

    public String getCourseIdForTerm(String subjectArea, String number, String termId);

    public String joinStringsByDelimiter(char delimiter, String... list);

    public String getVerifiedCourseId(String courseId);

    public CourseInfo getCourseInfo(String courseId);

}
