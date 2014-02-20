package org.kuali.student.ap.framework.context.support;

import org.dom4j.DocumentException;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.infc.Course;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

/**
 * PLACE HOLDER CLASS
 * This class should be replaced by one from KSAP's ks-ap-framework module
 */
public class DefaultCourseHelper implements CourseHelper {
    @Override
    public void frontLoad(List<String> courseIds, String... termId) {

    }

    @Override
    public CourseInfo getCourseInfo(String courseId) {
        return null;
    }

    @Override
    public List<ActivityOfferingDisplayInfo> getActivityOfferingDisplaysByCourseAndTerm(String courseId, String termId) {
        return null;
    }

    @Override
    public void getAllSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> mapmap, String termId, String curric, String num) throws DocumentException {

    }

    @Override
    public DeconstructedCourseCode getCourseDivisionAndNumber(String courseCode) {
        return null;
    }

    @Override
    public String getLastOfferedTermId(Course course) {
        return null;
    }

    @Override
    public List<String> getScheduledTerms(Course course) {
        return null;
    }

    @Override
    public String getCourseId(String subjectArea, String number) {
        return null;
    }

    @Override
    public String getCourseIdForTerm(String subjectArea, String number, String termId) {
        return null;
    }

    @Override
    public String buildActivityRefObjId(String atpId, String subject, String number, String activityCd) {
        return null;
    }

    @Override
    public String getSLN(String year, String term, String subject, String number, String activityCd) {
        return null;
    }

    @Override
    public String joinStringsByDelimiter(char delimiter, String... list) {
        return null;
    }

    @Override
    public String getVerifiedCourseId(String courseId) {
        return null;
    }

    @Override
    public String getCourseCdFromActivityId(String activityId) {
        return null;
    }

    @Override
    public String getCodeFromActivityId(String activityId) {
        return null;
    }

    @Override
    public List<Course> getCoursesByCode(String courseCd) {
        return null;
    }

    @Override
    public boolean isCourseOffered(Term term, Course course) {
        return false;
    }

    @Override
    public String getCourseVersionIdByTerm(String courseId, String termId) {
        return null;
    }

    @Override
    public HashMap<String, String> fetchCourseDivisions() {
        return null;
    }

    @Override
    public String extractDivisions(HashMap<String, String> divisionMap, String query, List<String> divisions, boolean isSpaceAllowed) {
        return null;
    }

    @Override
    public boolean isCourseInOfferingIds(String subjectArea, String courseNumber, Set<String> courseOfferingIds) {
        return false;
    }

    @Override
    public boolean isValidCourseLevel(String division, String level) {
        return false;
    }

    @Override
    public String getKeyForCourseOffering(String courseId, String subject, String number) {
        return null;
    }

    @Override
    public boolean isCrossListedCourse(CourseInfo courseInfo, String courseCd) throws DoesNotExistException {
        return false;
    }

    @Override
    public boolean isSimilarCourses(String courseCd1, String courseCd2) {
        return false;
    }

    @Override
    public CourseInfo getCourseInfoByIdAndCd(String courseId, String courseCd) {
        return null;
    }
}
