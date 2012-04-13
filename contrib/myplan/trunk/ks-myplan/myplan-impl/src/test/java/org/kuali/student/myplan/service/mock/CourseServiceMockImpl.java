package org.kuali.student.myplan.service.mock;


import org.kuali.student.common.dictionary.dto.ObjectStructureDefinition;
import org.kuali.student.common.dto.RichTextInfo;
import org.kuali.student.common.dto.StatusInfo;
import org.kuali.student.common.exceptions.*;
import org.kuali.student.common.validation.dto.ValidationResultInfo;
import org.kuali.student.common.versionmanagement.dto.VersionDisplayInfo;
import org.kuali.student.core.statement.dto.StatementTreeViewInfo;
import org.kuali.student.lum.course.dto.ActivityInfo;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.dto.FormatInfo;
import org.kuali.student.lum.course.dto.LoDisplayInfo;
import org.kuali.student.lum.course.service.CourseService;

import javax.jws.WebParam;
import java.util.*;

/**
 * Mock CourseService to be use with AcademicPlanServiceImpl.
 */
public class CourseServiceMockImpl implements CourseService {

    private Set<String> validCourseIds;

    /**
     *  Allow the test context to set a list valid/existing course Ids.
     *
     * @param validCourseIds
     */
    public void setValidCourses(Set<String> validCourseIds) {
        this.validCourseIds = validCourseIds;
    }

    @Override
    public CourseInfo getCourse(@WebParam(name = "courseId") String courseId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        if ( ! validCourseIds.contains(courseId)) {
            throw new DoesNotExistException();
        }
        CourseInfo courseInfo = new CourseInfo();
        courseInfo.setId(courseId);
        RichTextInfo richTextInfo=new RichTextInfo();
        courseInfo.setDescr(richTextInfo);
        return courseInfo;
    }

    @Override
    public List<FormatInfo> getCourseFormats(@WebParam(name = "courseId") String courseId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ActivityInfo> getCourseActivities(@WebParam(name = "formatId") String formatId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<LoDisplayInfo> getCourseLos(@WebParam(name = "courseId") String courseId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StatementTreeViewInfo> getCourseStatements(@WebParam(name = "courseId") String courseId, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {

        List<StatementTreeViewInfo> statementTreeViewInfos=new ArrayList<StatementTreeViewInfo>();
        return statementTreeViewInfos;
    }

    @Override
    public CourseInfo createCourse(@WebParam(name = "courseInfo") CourseInfo courseInfo) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException, DoesNotExistException, CircularRelationshipException, DependentObjectsExistException, UnsupportedActionException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public CourseInfo updateCourse(@WebParam(name = "courseInfo") CourseInfo courseInfo) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, VersionMismatchException, OperationFailedException, PermissionDeniedException, AlreadyExistsException, CircularRelationshipException, DependentObjectsExistException, UnsupportedActionException, UnsupportedOperationException, CircularReferenceException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteCourse(@WebParam(name = "courseId") String courseId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException, DataValidationErrorException, AlreadyExistsException, CircularRelationshipException, DependentObjectsExistException, UnsupportedActionException, UnsupportedOperationException, CircularReferenceException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatementTreeViewInfo createCourseStatement(@WebParam(name = "courseId") String courseId, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatementTreeViewInfo updateCourseStatement(@WebParam(name = "courseId") String courseId, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException, CircularReferenceException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteCourseStatement(@WebParam(name = "courseId") String courseId, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateCourse(String validationType, CourseInfo courseInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateCourseStatement(@WebParam(name = "courseId") String courseId, @WebParam(name = "statementTreeViewInfo") StatementTreeViewInfo statementTreeViewInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public CourseInfo createNewCourseVersion(@WebParam(name = "courseId") String courseId, @WebParam(name = "versionComment") String versionComment) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo setCurrentCourseVersion(@WebParam(name = "courseVersionId") String courseVersionId, @WebParam(name = "currentVersionStart") Date currentVersionStart) throws DoesNotExistException, InvalidParameterException, MissingParameterException, IllegalVersionSequencingException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getObjectTypes() {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public ObjectStructureDefinition getObjectStructure(@WebParam(name = "objectTypeKey") String objectTypeKey) {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<VersionDisplayInfo> getVersions(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public VersionDisplayInfo getFirstVersion(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public VersionDisplayInfo getLatestVersion(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public VersionDisplayInfo getCurrentVersion(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public VersionDisplayInfo getVersionBySequenceNumber(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "sequence") Long sequence) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public VersionDisplayInfo getCurrentVersionOnDate(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "date") Date date) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<VersionDisplayInfo> getVersionsInDateRange(@WebParam(name = "refObjectTypeURI") String refObjectTypeURI, @WebParam(name = "refObjectId") String refObjectId, @WebParam(name = "from") Date from, @WebParam(name = "to") Date to) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }
}
