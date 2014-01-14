package org.kuali.student.myplan.course.util;

import org.dom4j.DocumentException;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.lum.course.dto.CourseInfo;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public interface CourseHelper {


    public void getAllSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> mapmap, AtpHelper.YearTerm yt,
                                    String curric, String num) throws DocumentException;

    public DeconstructedCourseCode getCourseDivisionAndNumber(String courseCode);

    public String getCourseId(String subjectArea, String number);

    public String getCourseIdForTerm(String subjectArea, String number, String termId);

    public String buildActivityRefObjId(String atpId, String subject, String number, String activityCd);

    public String getSLN(String year, String term, String subject, String number, String activityCd);

    public String joinStringsByDelimiter(char delimiter, String... list);

    public CourseInfo getCourseInfo(String courseId);

    public CourseInfo getCourseInfoByIdAndCd(String courseId, String courseCd);

    public String getVerifiedCourseId(String courseId);

    public String getCourseCdFromActivityId(String activityId);

    public String getCodeFromActivityId(String activityId);

    public String getCourseVersionIdByTerm(String courseId, String termId);

    public HashMap<String, String> fetchCourseDivisions();

    public String extractDivisions(HashMap<String, String> divisionMap, String query, List<String> divisions, boolean isSpaceAllowed);


    /**
     * Check to see if the course subjectArea and number is offered from the list of course offering Id set
     *
     * @param subjectArea
     * @param courseNumber
     * @param courseOfferingIds
     * @return
     */
    public boolean isCourseInOfferingIds(String subjectArea, String courseNumber, Set<String> courseOfferingIds);

    public boolean isValidCourseLevel(String division, String level);

    public String getKeyForCourseOffering(String courseId, String subject, String number);

    public boolean isCrossListedCourse(CourseInfo courseInfo, String courseCd) throws DoesNotExistException;

    public boolean isSimilarCourses(String courseCd1, String courseCd2);
}
