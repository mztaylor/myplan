package edu.uw.kuali.student.myplan.tests.unit;

import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.core.statement.dto.StatementTreeViewInfo;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.dto.RegistrationGroupInfo;
import org.kuali.student.enrollment.courseoffering.dto.SeatPoolDefinitionInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.enrollment.courseregistration.dto.CourseRegistrationInfo;
import org.kuali.student.r2.common.datadictionary.dto.DictionaryEntryInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.TypeInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.List;

public class FakeCourseOfferingService implements CourseOfferingService {
    @Override
    public CourseOfferingInfo getCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CourseOfferingInfo> getCourseOfferingsByIdList(@WebParam(name = "courseOfferingIds") List<String> courseOfferingIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CourseOfferingInfo> getCourseOfferingsForCourseAndTerm(@WebParam(name = "courseId") String courseId, @WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCourseOfferingIdsForTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "useIncludedTerm") Boolean useIncludedTerm, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCourseOfferingIdsByTermAndSubjectArea(@WebParam(name = "termKey") String termKey, @WebParam(name = "subjectArea") String subjectArea, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<String> strings = new ArrayList<String>();
        strings.add("CHEM");
        return strings;
    }

    @Override
    public List<String> getCourseOfferingIdsByTermAndInstructorId(@WebParam(name = "termKey") String termKey, @WebParam(name = "instructorId") String instructorId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getCourseOfferingIdsByTermAndUnitContentOwner(@WebParam(name = "termKey") String termKey, @WebParam(name = "unitOwnerId") String unitOwnerId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CourseOfferingInfo createCourseOfferingFromCanonical(@WebParam(name = "courseId") String courseId, @WebParam(name = "termKey") String termKey, @WebParam(name = "formatIdList") List<String> formatIdList, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CourseOfferingInfo updateCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "courseOfferingInfo") CourseOfferingInfo courseOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public CourseOfferingInfo updateCourseOfferingFromCanonical(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCourseOffering(@WebParam(name = "validationType") String validationType, @WebParam(name = "courseOfferingInfo") CourseOfferingInfo courseOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<StatementTreeViewInfo> getCourseOfferingRestrictions(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementTreeViewInfo createCourseOfferingRestriction(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementTreeViewInfo updateCourseOfferingRestriction(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException, CircularReferenceException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteCourseOfferingRestriction(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "restrictionId") String restrictionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateCourseOfferingRestriction(@WebParam(name = "validationType") String validationType, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypeInfo getActivityOfferingType(@WebParam(name = "activityOfferingTypeKey") String activityOfferingTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeInfo> getAllActivityOfferingTypes(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<TypeInfo> getActivityOfferingTypesForActivityType(@WebParam(name = "activityTypeKey") String activityTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ActivityOfferingInfo getActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ActivityOfferingInfo> getActivityOfferingsByIdList(@WebParam(name = "activityOfferingIds") List<String> activityOfferingIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ActivityOfferingInfo> getActivitiesForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ActivityOfferingInfo createActivityOffering(@WebParam(name = "courseOfferingIdList") List<String> courseOfferingIdList, @WebParam(name = "activityOfferingInfo") ActivityOfferingInfo activityOfferingInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo assignActivityToCourseOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "courseOfferingIdList") List<String> courseOfferingIdList, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public ActivityOfferingInfo updateActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "activityOfferingInfo") ActivityOfferingInfo activityOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateActivityOffering(@WebParam(name = "validationType") String validationType, @WebParam(name = "activityOfferingInfo") ActivityOfferingInfo activityOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<StatementTreeViewInfo> getActivityOfferingRestrictions(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementTreeViewInfo createActivityOfferingRestriction(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatementTreeViewInfo updateActivityOfferingRestriction(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException, CircularReferenceException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteActivityOfferingRestriction(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "restrictionId") String restrictionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateActivityOfferingRestriction(@WebParam(name = "validationType") String validationType, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Float calculateInClassContactHoursForTerm(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Float calculateOutofClassContactHoursForTerm(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public Float calculateTotalContactHoursForTerm(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ActivityOfferingInfo> copyActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "numberOfCopies") Integer numberOfCopies, @WebParam(name = "copyContextTypeKey") String copyContextTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RegistrationGroupInfo getRegistrationGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<RegistrationGroupInfo> getRegistrationGroupsByIdList(@WebParam(name = "registrationGroupIds") List<String> registrationGroupIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<RegistrationGroupInfo> getRegGroupsForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<RegistrationGroupInfo> getRegGroupsByFormatForCourse(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "formatTypeKey") String formatTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RegistrationGroupInfo createRegistrationGroup(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "registrationGroupInfo") RegistrationGroupInfo registrationGroupInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public RegistrationGroupInfo updateRegistrationGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "registrationGroupInfo") RegistrationGroupInfo registrationGroupInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteRegistrationGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ValidationResultInfo> validateRegistrationGroup(@WebParam(name = "validationType") String validationType, @WebParam(name = "registrationGroupInfo") RegistrationGroupInfo registrationGroupInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SeatPoolDefinitionInfo getSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SeatPoolDefinitionInfo> getSeatPoolsForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SeatPoolDefinitionInfo> getSeatPoolsForRegGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SeatPoolDefinitionInfo createSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionInfo") SeatPoolDefinitionInfo seatPoolDefinitionInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SeatPoolDefinitionInfo updateSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "seatPoolDefinitionInfo") SeatPoolDefinitionInfo seatPoolDefinitionInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo deleteSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CourseOfferingInfo> searchForCourseOfferings(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> searchForCourseOfferingIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<ActivityOfferingInfo> searchForActivityOfferings(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> searchForActivityOfferingIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<CourseRegistrationInfo> searchForRegistrationGroups(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> searchForRegistrationGroupIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<SeatPoolDefinitionInfo> searchForSeatpoolDefintions(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> searchForSeatpoolDefintionIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getDataDictionaryEntryKeys(@WebParam(name = "context") ContextInfo context) throws OperationFailedException, MissingParameterException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public DictionaryEntryInfo getDataDictionaryEntry(@WebParam(name = "entryKey") String entryKey, @WebParam(name = "context") ContextInfo context) throws OperationFailedException, MissingParameterException, PermissionDeniedException, DoesNotExistException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
