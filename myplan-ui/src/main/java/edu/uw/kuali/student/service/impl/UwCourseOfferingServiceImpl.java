package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.CourseOfferingServiceUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.criteria.Predicate;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.dto.*;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.AtpHelper.YearTerm;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.type.dto.TypeInfo;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import java.util.*;

/**
 * UW implementation of CourseOfferingService.
 */
public class UwCourseOfferingServiceImpl implements CourseOfferingService {

    private final static Logger logger = Logger.getLogger(UwCourseOfferingServiceImpl.class);

    private static int CRITERIA_LENGTH = 24;

    private transient CourseService courseService;

    private StudentServiceClient studentServiceClient;

    private CourseHelper courseHelper;

    private CourseOfferingServiceUtils offeringServiceUtils;

    public CourseOfferingServiceUtils getOfferingServiceUtils() {
        return offeringServiceUtils;
    }

    public void setOfferingServiceUtils(CourseOfferingServiceUtils offeringServiceUtils) {
        this.offeringServiceUtils = offeringServiceUtils;
    }

    public UwCourseOfferingServiceImpl() {
    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    protected synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    public synchronized void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    /**
     * This implementation actually returns course code ...
     * Student Service: curriculum abbreviation _ course number
     * MyPlan: subject _ number.
     *
     * @param termId
     * @param subjectArea
     * @param context
     * @return A list of course codes.
     */
    @Override
    public List<String> getCourseOfferingIdsByTermAndSubjectArea(@WebParam(name = "termId") String termId,
                                                                 @WebParam(name = "subjectArea") String subjectArea,
                                                                 @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {

        //  Query the web service. Because the results of the call to this method are cached it important that an
        //  an exception is thrown and the call doesn't complete successfully.
        try {
            YearTerm yt = AtpHelper.atpToYearTerm(termId);
            String year = yt.getYearAsString();
            String term = yt.getTermAsID();
            String responseText = studentServiceClient.getSectionInfo(year, term, subjectArea);

            Document document = offeringServiceUtils.newDocument(responseText);

            DefaultXPath xpath = offeringServiceUtils.newXPath("//s:Section");
            List sections = xpath.selectNodes(document);

            Set<String> offeringIds = new HashSet<String>(sections.size());
            for (Object node : sections) {
                Element section = (Element) node;
                String number = section.elementText("CourseNumber");
                String ca = section.elementText("CurriculumAbbreviation");
                String sec = section.elementText("SectionID");
                String offeringId = getCourseHelper().joinStringsByDelimiter(':', yt.getYearAsString(), yt.getTermAsString(), ca, number, sec);
                offeringIds.add(offeringId);
            }

            return new ArrayList<String>(offeringIds);

        } catch (Exception e) {
            throw new OperationFailedException("Call to the student service failed.", e);
        }

    }

    @Override
    public List<CourseOfferingInfo> getCourseOfferingsByTermAndInstructor(@WebParam(name = "termId") String termId, @WebParam(name = "instructorId") String instructorId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getCourseOfferingIdsByTermAndUnitsContentOwner(@WebParam(name = "termId") String termId, @WebParam(name = "unitsContentOwnerId") String unitsContentOwnerId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getCourseOfferingIdsByType(@WebParam(name = "typeKey") String typeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getValidCanonicalCourseToCourseOfferingOptionKeys(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> getValidRolloverOptionKeys(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public CourseOfferingInfo createCourseOffering(@WebParam(name = "courseId") String courseId, @WebParam(name = "termId") String termId, @WebParam(name = "courseOfferingTypeKey") String courseOfferingTypeKey, @WebParam(name = "courseOfferingInfo") CourseOfferingInfo courseOfferingInfo, @WebParam(name = "optionKeys") List<String> optionKeys, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public CourseOfferingInfo rolloverCourseOffering(@WebParam(name = "sourceCourseOfferingId") String sourceCourseOfferingId, @WebParam(name = "targetTermId") String targetTermId, @WebParam(name = "optionKeys") List<String> optionKeys, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }


    @Override
    public CourseOfferingAdminDisplayInfo getCourseOfferingAdminDisplay(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<CourseOfferingAdminDisplayInfo> getCourseOfferingAdminDisplaysByIds(@WebParam(name = "courseOfferingIds") List<String> courseOfferingIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ActivityOfferingAdminDisplayInfo getActivityOfferingAdminDisplay(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ActivityOfferingAdminDisplayInfo> getActivityOfferingAdminDisplaysByIds(@WebParam(name = "activityOfferingIds") List<String> activityOfferingIds, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ActivityOfferingAdminDisplayInfo> getActivityOfferingAdminDisplaysForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public TypeInfo getCourseOfferingType(@WebParam(name = "courseOfferingTypeKey") String courseOfferingTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<TypeInfo> getCourseOfferingTypes(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<TypeInfo> getInstructorTypesForCourseOfferingType(@WebParam(name = "courseOfferingTypeKey") String courseOfferingTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public CourseOfferingInfo getCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        String[] list = courseOfferingId.split(":");
        String year = list[0];
        String term = list[1];
        String curriculum = list[2];
        String number = list[3];
        String sectionID = list[4];
        YearTerm yearTerm = new YearTerm(Integer.parseInt(year), Integer.parseInt(term));
        String quarter = yearTerm.getTermAsID();
        DefaultXPath primarySectionPath = offeringServiceUtils.newXPath("/s:Section/s:PrimarySection");

        Document secondaryDoc = null;
        try {
            String xml = studentServiceClient.getSecondarySections(year, quarter, curriculum, number, sectionID);
            secondaryDoc = offeringServiceUtils.newDocument(xml);
        } catch (ServiceException e) {
            logger.warn(e);
            // Skip this section ID if it fails
        } catch (DocumentException e) {
            logger.warn(e);
        }

        Element primarySectionElement = (Element) primarySectionPath.selectSingleNode(secondaryDoc);

        String primaryID = primarySectionElement.elementText("SectionID");
        //If passed in section Id is not a primary section
        if (!primaryID.equalsIgnoreCase(sectionID)) {
            throw new DoesNotExistException();
        }
        CourseOfferingInfo info = new CourseOfferingInfo();
        info = offeringServiceUtils.buildCourseOfferingInfo(secondaryDoc, null);
        return info;
    }

    @Override
    public List<CourseOfferingInfo> getCourseOfferingsByIds(@WebParam(name = "courseOfferingIds") List<String> courseOfferingIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<CourseOfferingInfo> getCourseOfferingsByCourse(@WebParam(name = "courseId") String courseId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }


    @Override
    public List<String> getCourseOfferingIdsByTerm(@WebParam(name = "termId") String termId, @WebParam(name = "useIncludedTerm") Boolean useIncludedTerm, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public CourseOfferingInfo updateCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "courseOfferingInfo") CourseOfferingInfo courseOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public CourseOfferingInfo updateCourseOfferingFromCanonical(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "optionKeys") List<String> optionKeys, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteCourseOfferingCascaded(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateCourseOffering(@WebParam(name = "validationType") String validationType, @WebParam(name = "courseOfferingInfo") CourseOfferingInfo courseOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateCourseOfferingFromCanonical(@WebParam(name = "courseOfferingInfo") CourseOfferingInfo courseOfferingInfo, @WebParam(name = "optionKeys") List<String> optionKeys, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public FormatOfferingInfo getFormatOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<FormatOfferingInfo> getFormatOfferingsByCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public FormatOfferingInfo createFormatOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "formatId") String formatId, @WebParam(name = "formatOfferingType") String formatOfferingType, @WebParam(name = "formatOfferingInfo") FormatOfferingInfo formatOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public FormatOfferingInfo updateFormatOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "formatOfferingInfo") FormatOfferingInfo formatOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateFormatOffering(@WebParam(name = "validationType") String validationType, @WebParam(name = "formatOfferingInfo") FormatOfferingInfo formatOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteFormatOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DependentObjectsExistException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteFormatOfferingCascaded(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }


    @Override
    public TypeInfo getActivityOfferingType(@WebParam(name = "activityOfferingTypeKey") String activityOfferingTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getActivityOfferingTypes(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<TypeInfo> getActivityOfferingTypesForActivityType(@WebParam(name = "activityTypeKey") String activityTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getInstructorTypesForActivityOfferingType(@WebParam(name = "activityOfferingTypeKey") String activityOfferingTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ActivityOfferingInfo getActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ActivityOfferingDisplayInfo getActivityOfferingDisplay(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        String[] list = activityOfferingId.split(":");
        String year = list[0];
        String term = list[1];
        String curriculum = list[2];
        String courseNumber = list[3];
        String sectionID = list[4];
        YearTerm yearTerm = new YearTerm(Integer.parseInt(year), Integer.parseInt(term));
        String quarter = yearTerm.getTermAsID();
        Document doc = null;
        try {
            // Skips section ID if it fails
            String xml = studentServiceClient.getSecondarySections(year, quarter, curriculum, courseNumber, sectionID);
            doc = offeringServiceUtils.newDocument(xml);
        } catch (Exception e) {
            logger.warn(e);
        }

        ActivityOfferingDisplayInfo info = offeringServiceUtils.buildActivityOfferingDisplayInfo(doc);

        return info;

    }

    @Override
    public List<ActivityOfferingInfo> getActivityOfferingsByIds(@WebParam(name = "activityOfferingIds") List<String> activityOfferingIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ActivityOfferingInfo> getActivityOfferingsByCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ActivityOfferingInfo> getActivityOfferingsByFormatOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ActivityOfferingInfo> getActivityOfferingsByFormatOfferingWithoutRegGroup(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ActivityOfferingInfo createActivityOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "activityId") String activityId, @WebParam(name = "activityOfferingTypeKey") String activityOfferingTypeKey, @WebParam(name = "activityOfferingInfo") ActivityOfferingInfo activityOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ActivityOfferingInfo copyActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ActivityOfferingInfo> generateActivityOfferings(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "activityOfferingType") String activityOfferingType, @WebParam(name = "quantity") Integer quantity, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public ActivityOfferingInfo updateActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "activityOfferingInfo") ActivityOfferingInfo activityOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteActivityOfferingCascaded(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateActivityOffering(@WebParam(name = "validationType") String validationType, @WebParam(name = "activityOfferingInfo") ActivityOfferingInfo activityOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public Float calculateInClassContactHoursForTerm(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public Float calculateOutofClassContactHoursForTerm(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public Float calculateTotalContactHoursForTerm(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public RegistrationGroupInfo getRegistrationGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<RegistrationGroupInfo> getRegistrationGroupsByIds(@WebParam(name = "registrationGroupIds") List<String> registrationGroupIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<RegistrationGroupInfo> getRegistrationGroupsForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<RegistrationGroupInfo> getRegistrationGroupsWithActivityOfferings(@WebParam(name = "activityOfferingIds") List<String> activityOfferingIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<RegistrationGroupInfo> getRegistrationGroupsByFormatOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public RegistrationGroupInfo createRegistrationGroup(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "registrationGroupType") String registrationGroupType, @WebParam(name = "registrationGroupInfo") RegistrationGroupInfo registrationGroupInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<RegistrationGroupInfo> generateRegistrationGroupsForTemplate(@WebParam(name = "registrationGroupTemplateId") String registrationGroupTemplateId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<RegistrationGroupInfo> generateRegistrationGroupsForFormatOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public RegistrationGroupInfo updateRegistrationGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "registrationGroupInfo") RegistrationGroupInfo registrationGroupInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteRegistrationGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteRegistrationGroupsByFormatOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteGeneratedRegistrationGroupsByFormatOffering(@WebParam(name = "formatOfferingId") String formatOfferingId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteGeneratedRegistrationGroupsForTemplate(@WebParam(name = "registrationGroupTemplateId") String registrationGroupTemplateId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateRegistrationGroup(@WebParam(name = "validationType") String validationType, @WebParam(name = "registrationGroupInfo") RegistrationGroupInfo registrationGroupInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public RegistrationGroupTemplateInfo getRegistrationGroupTemplate(@WebParam(name = "registrationGroupTemplateId") String registrationGroupTemplateId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<ValidationResultInfo> validateRegistrationGroupTemplate(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "registrationGroupTemplateInfo") RegistrationGroupTemplateInfo registrationGroupTemplateInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public RegistrationGroupTemplateInfo createRegistrationGroupTemplate(@WebParam(name = "registrationGroupTemplateInfo") RegistrationGroupTemplateInfo registrationGroupTemplateInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, ReadOnlyException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public RegistrationGroupTemplateInfo updateRegistrationGroupTemplate(@WebParam(name = "registrationGroupTemplateId") String registrationGroupTemplateId, @WebParam(name = "registrationGroupTemplateInfo") RegistrationGroupTemplateInfo registrationGroupTemplateInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteRegistrationGroupTemplate(@WebParam(name = "registrationGroupTemplateId") String registrationGroupTemplateId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SeatPoolDefinitionInfo getSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<SeatPoolDefinitionInfo> getSeatPoolDefinitionsForActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public SeatPoolDefinitionInfo createSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionInfo") SeatPoolDefinitionInfo seatPoolDefinitionInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public SeatPoolDefinitionInfo updateSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "seatPoolDefinitionInfo") SeatPoolDefinitionInfo seatPoolDefinitionInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateSeatPoolDefinition(@WebParam(name = "validationTypeKey") String validationTypeKey, @WebParam(name = "seatPoolDefinitionInfo") SeatPoolDefinitionInfo seatPoolDefinitionInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo deleteSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo addSeatPoolDefinitionToActivityOffering(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public StatusInfo removeSeatPoolDefinitionFromActivityOffering(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<CourseOfferingInfo> searchForCourseOfferings(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        // The right strategy would be using the multiple equal predicates joined using an and
        Predicate p = criteria.getPredicate();
        String str = p.toString();
        str = str.substring(CRITERIA_LENGTH, str.length() - 1);
        String[] strings = str.split(",");
        if (strings != null && strings.length == 3) {
            String year = strings[0].trim();
            String curriculum = strings[1].trim();
            String courseNumber = strings[2].trim();
            //default future terms to look for is 44 i.e.(11 years * 4 terms)
            try {
                String responseText = studentServiceClient.getSections(year, curriculum, courseNumber, 44);
                Document document = offeringServiceUtils.newDocument(responseText);
                DefaultXPath sectionPath = offeringServiceUtils.newXPath("/s:SearchResults/s:Sections/s:Section");
                List<YearTerm> yearTermList = new ArrayList<YearTerm>();
                List<Object> nodes = sectionPath.selectNodes(document);
                for (Object a : nodes) {
                    Element e = (Element) a;
                    String q = e.elementText("Quarter");
                    String y = e.elementText("Year");
                    YearTerm yt = AtpHelper.quarterYearToYearTerm(q, y);
                    yearTermList.add(yt);
                }
                if (!yearTermList.isEmpty()) {
                    Collections.sort(yearTermList, Collections.reverseOrder());
                    YearTerm last = yearTermList.get(0);
                    String termId = last.getTermAsID() + " " + last.getYearAsString();
                    CourseOfferingInfo courseOfferingInfo = new CourseOfferingInfo();
                    courseOfferingInfo.setTermId(termId);
                    List<CourseOfferingInfo> courseOfferingInfos = new ArrayList<CourseOfferingInfo>();
                    courseOfferingInfos.add(courseOfferingInfo);
                    return courseOfferingInfos;
                }
            } catch (Exception e) {
                throw new OperationFailedException("Call to the student service failed.", e);
            }

        }

        return null;
    }

    @Override
    public List<String> searchForCourseOfferingIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ActivityOfferingInfo> searchForActivityOfferings(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForActivityOfferingIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<RegistrationGroupInfo> searchForRegistrationGroups(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> searchForRegistrationGroupIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<SeatPoolDefinitionInfo> searchForSeatpoolDefinitions(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<String> searchForSeatpoolDefinitionIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public List<CourseOfferingInfo> getCourseOfferingsByCourseAndTerm(@WebParam(name = "courseId") String courseId, @WebParam(name = "termId") String termId, @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {

        try {
            List<CourseOfferingInfo> list = new ArrayList<CourseOfferingInfo>();
            YearTerm yt = AtpHelper.atpToYearTerm(termId);
            String year = yt.getYearAsString();
            String quarter = yt.getTermAsID();

            CourseService courseService = getCourseService();
            CourseInfo courseInfo = courseService.getCourse(courseId);
            String curric = courseInfo.getSubjectArea().trim();
            String num = courseInfo.getCourseNumberSuffix();

            String xml = studentServiceClient.getSections(year, quarter, curric, num);

            DefaultXPath sectionPath = offeringServiceUtils.newXPath("/s:SearchResults/s:Sections/s:Section");
            DefaultXPath primarySectionPath = offeringServiceUtils.newXPath("/s:Section/s:PrimarySection");

            Document doc = offeringServiceUtils.newDocument(xml);

            List sections = sectionPath.selectNodes(doc);
            for (Object object : sections) {
                Element primarySectionNode = (Element) object;
                String primarySectionID = primarySectionNode.elementText("SectionID");
                Document secondaryDoc;
                try {
                    String secondaryXML = studentServiceClient.getSecondarySections(year, quarter, curric, num, primarySectionID);
                    secondaryDoc = offeringServiceUtils.newDocument(secondaryXML);
                } catch (ServiceException e) {
                    logger.warn(e);
                    // Skip this section ID if it fails
                    continue;
                }
                Element primarySectionElement = (Element) primarySectionPath.selectSingleNode(secondaryDoc);
                String primaryID = primarySectionElement.elementText("SectionID");
                if (primarySectionID.equals(primaryID)) {
                    CourseOfferingInfo info = offeringServiceUtils.buildCourseOfferingInfo(secondaryDoc, courseInfo);
                    if (info != null) {
                        list.add(info);
                    }
                }
            }
            return list;

        } catch (Exception e) {
            logger.error(e);
            throw new OperationFailedException(e.getMessage());
        }
    }


    @Override
    public List<ActivityOfferingDisplayInfo> getActivityOfferingDisplaysForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingID, @WebParam(name = "contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<ActivityOfferingDisplayInfo> result = new ArrayList<ActivityOfferingDisplayInfo>();
        try {
            String[] list = courseOfferingID.split(":");
            String year = list[0];
            String term = list[1];
            String curriculum = list[2];
            String number = list[3];
            String primarySectionID = list[4];
            YearTerm yearTerm = new YearTerm(Integer.parseInt(year), Integer.parseInt(term));
            String quarter = yearTerm.getTermAsID();
            List<String> sectionList = new ArrayList<String>();
            sectionList.add(primarySectionID);
            {
                try {
                    String xml = studentServiceClient.getSecondarySections(year, quarter, curriculum, number, primarySectionID);
                    DefaultXPath sectionListPath = offeringServiceUtils.newXPath("//s:LinkedSection/s:Section");
                    Document doc = offeringServiceUtils.newDocument(xml);
                    List sections = sectionListPath.selectNodes(doc);
                    for (Object node : sections) {
                        Element section = (Element) node;
                        String secondarySectionID = section.elementText("SectionID");
                        sectionList.add(secondarySectionID);
                    }
                } catch (ServiceException e) {
                    logger.warn(e);
                }
            }

            for (String sectionID : sectionList) {
                Document doc = null;
                try {
                    // Skips section ID if it fails
                    String xml = studentServiceClient.getSecondarySections(year, quarter, curriculum, number, sectionID);
                    doc = offeringServiceUtils.newDocument(xml);
                } catch (Exception e) {
                    logger.warn(e);
                    continue;
                }
                ActivityOfferingDisplayInfo info = offeringServiceUtils.buildActivityOfferingDisplayInfo(doc);
                if (info != null) {
                    result.add(info);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }

        return result;
    }
}
