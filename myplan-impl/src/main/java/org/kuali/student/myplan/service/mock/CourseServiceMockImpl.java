package org.kuali.student.myplan.service.mock;


import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.r1.common.dictionary.dto.ObjectStructureDefinition;
import org.kuali.student.r1.core.statement.dto.StatementTreeViewInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.RichTextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.versionmanagement.dto.VersionDisplayInfo;
import org.kuali.student.r2.lum.course.dto.ActivityInfo;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.dto.FormatInfo;
import org.kuali.student.r2.lum.course.dto.LoDisplayInfo;
import org.kuali.student.r2.lum.course.service.CourseService;

import javax.jws.WebParam;
import java.util.*;

/**
 * Mock CourseService to be use with AcademicPlanServiceImpl.
 */
public class CourseServiceMockImpl implements CourseService {

    private Set<String> validCourseIds;

    public Map<String, CourseInfo> courseInfos;


    public Map<String, CourseInfo> getCourseInfos() {
        if (courseInfos == null) {
            courseInfos = new HashMap<String, CourseInfo>();
        }
        return courseInfos;
    }

    public void setCourseInfos(Map<String, CourseInfo> courseInfos) {
        this.courseInfos = courseInfos;
    }

    public Set<String> getValidCourseIds() {
        if (validCourseIds == null) {
            validCourseIds = new HashSet<String>();
        }
        return validCourseIds;
    }


    public void setValidCourseIds(Set<String> validCourseIds) {
        this.validCourseIds = validCourseIds;
    }

    /**
     * Allow the test context to set a list valid/existing course Ids.
     *
     * @param validCourseIds
     */
    public void setValidCourses(Set<String> validCourseIds) {
        this.validCourseIds = validCourseIds;
    }

    @Override
    public CourseInfo getCourse(@WebParam(name = "courseId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        if (!validCourseIds.contains(s)) {
            throw new DoesNotExistException();
        }
        if (getCourseInfos().containsKey(s)) {
            return courseInfos.get(s);
        }
        CourseInfo courseInfo = new CourseInfo();
        courseInfo.setId(s);
        RichTextInfo richTextInfo = new RichTextInfo();
        courseInfo.setDescr(richTextInfo);
        return courseInfo;
    }

    @Override
    public List<CourseInfo> getCoursesByIds(@WebParam(name = "courseIds") List<String> strings, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForCourseIds(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<CourseInfo> searchForCourses(@WebParam(name = "criteria") QueryByCriteria queryByCriteria, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public CourseInfo createCourse(@WebParam(name = "courseInfo") CourseInfo courseInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        getValidCourseIds().add(courseInfo.getId());
        getCourseInfos().put(courseInfo.getId(), courseInfo);
        return courseInfo;
    }

    @Override
    public CourseInfo updateCourse(@WebParam(name = "courseId") String s, @WebParam(name = "courseInfo") CourseInfo courseInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, VersionMismatchException, OperationFailedException, PermissionDeniedException, UnsupportedActionException, DependentObjectsExistException, AlreadyExistsException, CircularRelationshipException, CircularReferenceException, ReadOnlyException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteCourse(@WebParam(name = "courseId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException, DataValidationErrorException, AlreadyExistsException, UnsupportedActionException, DependentObjectsExistException, CircularRelationshipException, CircularReferenceException, ReadOnlyException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateCourse(String s, CourseInfo courseInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<FormatInfo> getCourseFormatsByCourse(@WebParam(name = "courseId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ActivityInfo> getCourseActivitiesByCourseFormat(@WebParam(name = "formatId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<LoDisplayInfo> getCourseLearningObjectivesByCourse(@WebParam(name = "courseId") String s, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StatementTreeViewInfo> getCourseStatements(@WebParam(name = "courseId") String s, @WebParam(name = "nlUsageTypeKey") String s1, @WebParam(name = "language") String s2, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<StatementTreeViewInfo> statementTreeViewInfos = new ArrayList<StatementTreeViewInfo>();
        return statementTreeViewInfos;
    }

    @Override
    public StatementTreeViewInfo createCourseStatement(@WebParam(name = "courseId") String s, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatementTreeViewInfo updateCourseStatement(@WebParam(name = "courseId") String s, @WebParam(name = "statementTreeId") String s1, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteCourseStatement(@WebParam(name = "courseId") String s, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateCourseStatement(@WebParam(name = "courseId") String s, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public CourseInfo createNewCourseVersion(@WebParam(name = "courseId") String s, @WebParam(name = "versionComment") String s1, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException, ReadOnlyException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo setCurrentCourseVersion(@WebParam(name = "courseVersionId") String s, @WebParam(name = "currentVersionStart") Date date, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, IllegalVersionSequencingException, DataValidationErrorException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public VersionDisplayInfo getCurrentVersion(@WebParam(name = "refObjectTypeURI") String s, @WebParam(name = "refObjectId") String s1, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<VersionDisplayInfo> getVersions(String s, String s1, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getObjectTypes() {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public ObjectStructureDefinition getObjectStructure(@WebParam(name = "objectTypeKey") String s) {
        throw new RuntimeException("Not implemented.");
    }
}
