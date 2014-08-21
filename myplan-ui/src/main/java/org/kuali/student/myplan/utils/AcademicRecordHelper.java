package org.kuali.student.myplan.utils;

import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.r2.common.exceptions.*;

import java.util.List;

/**
 * Created by hemanth on 8/19/14.
 */
public interface AcademicRecordHelper {

    /**
     * Pulls only Student records for Students.
     * Does not make a call to the academic record service if the person provided is a non student.
     *
     * @param personId
     * @return
     * @throws PermissionDeniedException
     * @throws MissingParameterException
     * @throws InvalidParameterException
     * @throws OperationFailedException
     * @throws DoesNotExistException
     */
    public List<StudentCourseRecordInfo> getCompletedCourseRecordsForStudents(String personId) throws PermissionDeniedException, MissingParameterException, InvalidParameterException, OperationFailedException, DoesNotExistException;
}
