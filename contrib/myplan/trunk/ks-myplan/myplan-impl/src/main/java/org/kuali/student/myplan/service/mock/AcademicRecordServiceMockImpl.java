package org.kuali.student.myplan.service.mock;

import org.kuali.student.common.util.UUIDHelper;
import org.kuali.student.enrollment.academicrecord.dto.*;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 3/20/14.
 */
public class AcademicRecordServiceMockImpl implements AcademicRecordService {

    private AcademicRecordService nextDecorator;

    private Map<String, List<StudentCourseRecordInfo>> studentRecords;

    public Map<String, List<StudentCourseRecordInfo>> getStudentRecords() {
        if (studentRecords == null) {
            studentRecords = new HashMap<String, List<StudentCourseRecordInfo>>();
        }
        return studentRecords;
    }

    public void setStudentRecords(Map<String, List<StudentCourseRecordInfo>> studentRecords) {
        this.studentRecords = studentRecords;
    }

    public AcademicRecordService getNextDecorator() {
        return nextDecorator;
    }

    public void setNextDecorator(AcademicRecordService nextDecorator) {
        this.nextDecorator = nextDecorator;
    }

    @Override
    public List<StudentCourseRecordInfo> getAttemptedCourseRecordsForTerm(String personId, String termId, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        if ("CLEAR".equals(personId)) {
            setStudentRecords(new HashMap<String, List<StudentCourseRecordInfo>>());
        } else if (personId.contains(":")) {
            String str[] = personId.split(":");
            StudentCourseRecordInfo studentCourseRecordInfo = new StudentCourseRecordInfo();
            studentCourseRecordInfo.setTermName(termId);
            studentCourseRecordInfo.setActivityCode(str[0]);
            studentCourseRecordInfo.setCourseCode(str[1]);
            studentCourseRecordInfo.setPersonId(str[2]);
            studentCourseRecordInfo.setId(str[3]);

            List<StudentCourseRecordInfo> studentCourseRecordInfos = getStudentRecords().get(str[2]);
            if (CollectionUtils.isEmpty(studentCourseRecordInfos)) {
                studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();
            }
            studentCourseRecordInfos.add(studentCourseRecordInfo);
            getStudentRecords().put(str[2], studentCourseRecordInfos);
            return studentCourseRecordInfos;
        }
        return new ArrayList<StudentCourseRecordInfo>();
    }

    @Override
    public List<StudentCourseRecordInfo> getCompletedCourseRecords(String personId, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<StudentCourseRecordInfo> studentCourseRecordInfos = getStudentRecords().get(personId);
        if (CollectionUtils.isEmpty(studentCourseRecordInfos)) {
            return getNextDecorator().getCompletedCourseRecords(personId, contextInfo);
        }
        return studentCourseRecordInfos;
    }

    @Override
    public List<StudentCourseRecordInfo> getCompletedCourseRecordsForCourse(String personId, String courseId, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public List<StudentCourseRecordInfo> getCompletedCourseRecordsForTerm(String personId, String termId, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public GPAInfo getGPAForTerm(String personId, String termId, String calculationTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public GPAInfo getCumulativeGPA(String personId, String calculationTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public GPAInfo getCumulativeGPAForProgram(String personId, String programId, String calculationTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public GPAInfo getCumulativeGPAForTermAndProgram(String personId, String programId, String termKey, String calculationTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public LoadInfo getLoadForTerm(String personId, String termId, String calculationTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public List<StudentProgramRecordInfo> getProgramRecords(String personId, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public List<StudentCredentialRecordInfo> getAwardedCredentials(String personId, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public List<StudentTestScoreRecordInfo> getTestScoreRecords(String personId, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public List<StudentTestScoreRecordInfo> getTestScoreRecordsByType(String personId, String testTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public String getEarnedCreditsForTerm(String personId, String termId, String calculationTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public String getEarnedCredits(String personId, String calculationTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }

    @Override
    public String getEarnedCumulativeCreditsForProgramAndTerm(String personId, String programId, String termId, String calculationTypeKey, ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;
    }
}
