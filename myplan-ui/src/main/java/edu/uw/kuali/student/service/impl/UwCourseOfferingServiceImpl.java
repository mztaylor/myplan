package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.criteria.Predicate;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.dto.*;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;

import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.AtpHelper.YearTerm;
import org.kuali.student.myplan.utils.TimeStringMillisConverter;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.TimeOfDayInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.room.dto.BuildingInfo;
import org.kuali.student.r2.core.room.dto.RoomInfo;
import org.kuali.student.r2.core.scheduling.dto.TimeSlotInfo;
import org.kuali.student.r2.core.type.dto.TypeInfo;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

/**
 * UW implementation of CourseOfferingService.
 */
public class UwCourseOfferingServiceImpl implements CourseOfferingService {

    public enum terms {autumn, winter, spring, summer}

    private final static Logger logger = Logger.getLogger(UwCourseOfferingServiceImpl.class);

    private static int CRITERIA_LENGTH = 24;

    private transient CourseService courseService;
    private StudentServiceClient studentServiceClient;

    private List<String> DAY_LIST = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");

    public UwCourseOfferingServiceImpl() {
    }

    private Document newDocument(String xml) throws DocumentException {
        SAXReader sax = new SAXReader();
        StringReader sr = new StringReader(xml);
        Document doc = sax.read(sr);
        return doc;
    }

    private static Map<String, String> NAMESPACES = new HashMap<String, String>() {{
        put("s", "http://webservices.washington.edu/student/");
    }};


    public DefaultXPath newXPath(String expr) {
        DefaultXPath path = new DefaultXPath(expr);
        path.setNamespaceURIs(NAMESPACES);
        return path;
    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
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

            Document document = newDocument(responseText);

            DefaultXPath xpath = newXPath("//s:Section");
            List sections = xpath.selectNodes(document);

            Set<String> courseCodes = new HashSet<String>(sections.size());
            for (Object node : sections) {
                Element section = (Element) node;
                String number = section.elementText("CourseNumber");
                String ca = section.elementText("CurriculumAbbreviation");

                //  This needs to be 10 characters wide. Curriculum code is 4-6 chars, number is 3.
                StringBuilder cc = new StringBuilder("          ");
                cc.replace(0, ca.length() - 1, ca);
                cc.replace(7, 9, number);

                courseCodes.add(cc.toString().trim());
            }

            return new ArrayList<String>(courseCodes);

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
        throw new RuntimeException("Not implemented.");
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
        ActivityOfferingInfo activityOfferingInfo = new ActivityOfferingInfo();

        String[] params = activityOfferingId.split(",");

        try {
            String allText = studentServiceClient.getTimeSchedules(params[0], params[1], params[2], params[3], null);
            Document allDoc = newDocument(allText);

            List<String> hrefs = new ArrayList<String>();

            DefaultXPath xpath = newXPath("//s:Sections/s:Section/s:Href");
            List sections = xpath.selectNodes(allDoc);
            for (Object node : sections) {
                Element element = (Element) node;
                String href = element.getText();
                hrefs.add(href);
            }

            for (String href : hrefs) {
                href = href.replace("/student", "").trim();
                String hrefText = studentServiceClient.getTimeSchedules(null, null, null, null, href);
                Document hrefDoc = newDocument(hrefText);
                DefaultXPath xpath2 = newXPath("//s:Curriculum");
                List curriculum = xpath2.selectNodes(hrefDoc);
                for (Object node : curriculum) {
                    Element element = (Element) node;

                    AttributeInfo attributeInfo = new AttributeInfo();
                    attributeInfo.setKey(CourseSearchConstants.TIME_SCHEDULE_KEY);
                    String abbrev = element.elementText("TimeScheduleLinkAbbreviation");
                    attributeInfo.setValue(abbrev);

                    activityOfferingInfo.getAttributes().add(attributeInfo);
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }

        return activityOfferingInfo;

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
        List<CourseOfferingInfo> courseOfferingInfos = new ArrayList<CourseOfferingInfo>();
        CourseOfferingInfo courseOfferingInfo = new CourseOfferingInfo();
        // The right strategy would be using the multiple equal predicates joined using an and
        Predicate p = criteria.getPredicate();
        String str = p.toString();
        String responseText = null;
        str = str.substring(CRITERIA_LENGTH, str.length() - 1);
        String[] strings = null;
        String year = null;
        String curriculum = null;
        String courseCode = null;
        strings = str.split(",");
        if (strings != null && strings.length == 3) {
            year = strings[0].trim();
            curriculum = strings[1].trim();
            courseCode = strings[2].trim();
        }

        try {
            responseText = studentServiceClient.getSections(year, curriculum, courseCode);
        } catch (Exception e) {
            logger.error(e);
        }
        Document document = null;
        try {
            SAXReader reader = new SAXReader();
            document = reader.read(new StringReader(responseText));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse reply from the Student Term Service.", e);
        }

        DefaultXPath xpath = newXPath("//s:Section");

        List sections = xpath.selectNodes(document);
        //  From the sections last offered for the course is figured out
        if (sections != null && sections.size() > 0) {
            StringBuffer cc = new StringBuffer();
            int maxQ = 0;
            String[] quarters = new String[sections.size()];
            Integer[] resultYear = new Integer[sections.size()];
            //Logical implementation to get the last offered year
            int actualYear = -1;
            String actualQuarter = null;
            int resultQuarter = 0;
            int count = 0;
            for (Object node : sections) {

                Element section = (Element) node;

                resultYear[count] = Integer.parseInt(section.elementText("Year"));
                if (resultYear[count] > actualYear) {
                    actualYear = resultYear[count];
                }
                quarters[count] = section.elementText("Quarter");
                count++;
            }
            //Logical implementation to get the last offered quarter
            for (int i = 0; i < quarters.length; i++) {
                String tempQuarter = quarters[i];
                terms fd = terms.valueOf(quarters[i]);
                switch (fd) {
                    case autumn:
                        resultQuarter = 1;
                        break;
                    case winter:
                        resultQuarter = 2;
                        break;
                    case spring:
                        resultQuarter = 3;
                        break;
                    case summer:
                        resultQuarter = 4;
                        break;
                }
                if (resultYear[i].equals(actualYear) && resultQuarter > maxQ) {
                    maxQ = resultQuarter;
                    actualQuarter = tempQuarter;

                }
            }
            if (actualQuarter != null && actualYear != -1) {
                cc = cc.append(actualQuarter).append(" ").append(actualYear);
                courseOfferingInfo.setTermId(cc.toString().trim());
                courseOfferingInfos.add(courseOfferingInfo);
            }
        }
        return courseOfferingInfos;
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

            DefaultXPath sectionPath = newXPath("/s:SearchResults/s:Sections/s:Section");

            Document doc = newDocument(xml);

            List sections = sectionPath.selectNodes(doc);
            for (Object object : sections) {
                Element primarySectionNode = (Element) object;
                String primarySectionID = primarySectionNode.elementText("SectionID");

                String secondaryXML;
                try {
                    // Skip this section ID if it fails
                    secondaryXML = studentServiceClient.getSecondarySections(year, quarter, curric, num, primarySectionID);
                } catch (ServiceException e) {

                    logger.warn(e);
                    continue;
                }


                DefaultXPath secondaryPath = newXPath("/s:Section/s:PrimarySection");
                DefaultXPath secondarySectionPath = newXPath("/s:Section");

                Document secondaryDoc = newDocument(secondaryXML);


                Element secondarySection = (Element) secondarySectionPath.selectSingleNode(secondaryDoc);
                Element dupeSectionElement = (Element) secondaryPath.selectSingleNode(secondaryDoc);

                String primaryID = dupeSectionElement.elementText("SectionID");
                if (primarySectionID.equals(primaryID)) {


                    CourseOfferingInfo info = new CourseOfferingInfo();
                    info.setSubjectArea(curric);
                    info.setCourseCode(num);
                    info.setTermId(termId);
                    info.setId(primarySectionID);
                    String courseID = join("=", year, quarter, curric, num, primarySectionID);
                    info.setCourseId(courseID);

                    {
                        DefaultXPath instructorPath = newXPath("/s:Section/s:Meetings/s:Meeting/s:Instructors/s:Instructor/s:Person");
                        List instructors = instructorPath.selectNodes(secondaryDoc);

                        for (Object gundar : instructors) {
                            Element instructor = (Element) gundar;
                            String name = instructor.elementText("Name");
                            String regid = instructor.elementText("RegID");
                            OfferingInstructorInfo temp = new OfferingInstructorInfo();
                            temp.setPersonName(name);
                            temp.setPersonId(regid);
                            info.getInstructors().add(temp);
                        }
                    }

                    {
                        String gradingSystem = secondarySection.elementText("GradingSystem");
                        if ("standard".equals(gradingSystem)) {
                            info.setGradingOptionId("kuali.uw.resultcomponent.grade.standard");
                            info.setGradingOptionName(null);
                        } else if ("credit/no credit".equals(gradingSystem)) {
                            info.setGradingOptionId("kuali.uw.resultcomponent.grade.crnc");
                            info.setGradingOptionName("Credit/No-Credit grading");
                        }
                    }

                    {
                        String creditControl = secondarySection.elementText("CreditControl");
                        String minCreditID = secondarySection.elementText("MinimumTermCredit");
                        String minCreditName = minCreditID;
                        if (minCreditName != null && minCreditName.endsWith(".0")) {
                            minCreditName = minCreditName.substring(0, minCreditName.length() - 2);
                        }
                        String maxCreditID = secondarySection.elementText("MaximumTermCredit");
                        String maxCreditName = maxCreditID;
                        if (maxCreditName != null && maxCreditName.endsWith(".0")) {
                            maxCreditName = maxCreditName.substring(0, maxCreditName.length() - 2);
                        }

                        String creditID = null;
                        String creditName = null;

                        if ("fixed credit".equals(creditControl)) {
                            creditID = minCreditID;
                            creditName = minCreditName;
                        }
                        if ("variable credit - min to max credits".equals(creditControl)) {
                            creditID = minCreditID + "-" + maxCreditID;
                            creditName = minCreditName + "-" + maxCreditName;
                        }
                        if ("variable credit - min or max credits".equals(creditControl)) {
                            creditID = minCreditID + ", " + maxCreditID;
                            creditName = minCreditName + ", " + maxCreditName;
                        }
                        if ("variable credit - 1 to 25 credits".equals(creditControl)) {
                            creditID = "1.0-25.0";
                            creditName = "1-25";
                        }

                        creditID = "kuali.uw.resultcomponent.credit." + creditID;
                        info.setCreditOptionId(creditID);
                        info.setCreditOptionName(creditName);

                    }
                    list.add(info);
                }
            }
            return list;

        } catch (Exception e) {
            logger.error(e);
            throw new OperationFailedException(e.getMessage());
        }
    }

    /* 
     * Joins an array of stings using a delimiter.
     *  
     * eg join( "," "a", "b", "c" ) => "a,b,c"
     * 
     */
    String join(String delim, String... list) {
        StringBuilder sb = new StringBuilder();
        boolean second = false;
        for (String item : list) {
            if (second) {
                sb.append(delim);
            } else {
                second = true;
            }
            sb.append(item);
        }
        return sb.toString();
    }

    @Override
    public List<ActivityOfferingDisplayInfo> getActivityOfferingDisplaysForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingID, @WebParam(name = "contextInfo") ContextInfo contextInfo)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        List<ActivityOfferingDisplayInfo> result = new ArrayList<ActivityOfferingDisplayInfo>();
        try {
            String[] list = courseOfferingID.split("=");
            String year = list[0];
            String quarter = list[1];
            String curric = list[2];
            String num = list[3];
            String sectionID = list[4];

            List<String> sectionList = new ArrayList<String>();
            sectionList.add(sectionID);
            {
                try {
                    String xml = studentServiceClient.getSecondarySections(year, quarter, curric, num, sectionID);
                    DefaultXPath sectionListPath = newXPath("//s:LinkedSection/s:Section");
                    Document doc = newDocument(xml);
                    List sections = sectionListPath.selectNodes(doc);
                    for (Object node : sections) {
                        Element section = (Element) node;
                        String secondaryid = section.elementText("SectionID");
                        sectionList.add(secondaryid);
                    }
                } catch (ServiceException e) {
                    logger.warn(e);
                }
            }

            for (String id : sectionList) {
                ActivityOfferingDisplayInfo info = new ActivityOfferingDisplayInfo();

                String xml;
                try {
                    // Skips section ID if it fails
                    xml = studentServiceClient.getSecondarySections(year, quarter, curric, num, id);
                } catch (ServiceException e) {

                    logger.warn(e);
                    continue;
                }


                Document doc = newDocument(xml);


                DefaultXPath sectionPath = newXPath("/s:Section");
                DefaultXPath coursePath = newXPath("/s:Section/s:Course");

                Element sectionNode = (Element) sectionPath.selectSingleNode(doc);
                String typeName = sectionNode.elementText("SectionType");
                String campus = sectionNode.elementText("CourseCampus");

                Element courseNode = (Element) coursePath.selectSingleNode(doc);
                String title = courseNode.elementText("CourseTitle");
                String sln = sectionNode.elementText("SLN");
                String instructorName = "--";
                String instructorId = "--";

                {
                    ScheduleDisplayInfo scheduleDisplay = new ScheduleDisplayInfo();
                    scheduleDisplay.setScheduleComponentDisplays(new ArrayList<ScheduleComponentDisplayInfo>());
                    info.setScheduleDisplay(scheduleDisplay);

                    DefaultXPath meetingPath = newXPath("/s:Section/s:Meetings/s:Meeting");

                    List meetings = meetingPath.selectNodes(doc);
                    for (Object obj : meetings) {
                        Element meetingNode = (Element) obj;

                        ScheduleComponentDisplayInfo scdi = new ScheduleComponentDisplayInfo();
                        scdi.setTimeSlots(new ArrayList<TimeSlotInfo>());
                        scheduleDisplay.getScheduleComponentDisplays().add(scdi);
                        

                        TimeSlotInfo timeSlot = new TimeSlotInfo();
                        scdi.getTimeSlots().add(timeSlot);
                        timeSlot.setWeekdays(new ArrayList<Integer>());
                        
                        String tba = meetingNode.elementText("DaysOfWeekToBeArranged");
                        boolean tbaFlag = Boolean.parseBoolean(tba);
                        if (!tbaFlag) {

                            DefaultXPath dayNamePath = newXPath("s:DaysOfWeek/s:Days/s:Day/s:Name");

                            List dayNameNodes = dayNamePath.selectNodes(meetingNode);
                            for (Object node : dayNameNodes) {
                                Element dayNameNode = (Element) node;
                                String day = dayNameNode.getTextTrim();
                                int weekday = DAY_LIST.indexOf(day);
                                if (weekday != -1) {
                                    timeSlot.getWeekdays().add(weekday);
                                }
                            }
                        
	                        {
	                            String time = meetingNode.elementText("StartTime");
	                            long millis = TimeStringMillisConverter.militaryTimeToMillis(time);
	                            TimeOfDayInfo timeInfo = new TimeOfDayInfo();
	                            timeInfo.setMilliSeconds(millis);
	                            timeSlot.setStartTime(timeInfo);
	                        }
	
	                        {
	                            String time = meetingNode.elementText("EndTime");
	                            long millis = TimeStringMillisConverter.militaryTimeToMillis(time);
	                            TimeOfDayInfo timeInfo = new TimeOfDayInfo();
	                            timeInfo.setMilliSeconds(millis);
	                            timeSlot.setEndTime(timeInfo);
	                        }
                        }

                        {
                            String buildingTBA = meetingNode.elementText("BuildingToBeArranged");
                            boolean buildingTBAFlag = Boolean.parseBoolean(buildingTBA);
                            if (!buildingTBAFlag) {
                            	BuildingInfo buildingInfo = new BuildingInfo();
                            	buildingInfo.setCampusKey( campus );
                            	String building = meetingNode.elementText("Building");
                            	buildingInfo.setBuildingCode(building);
                            	scdi.setBuilding(buildingInfo);
                            }
                            
                            String roomTBA = meetingNode.elementText("RoomToBeArranged");
                            boolean roomTBAFlag = Boolean.parseBoolean(roomTBA);
                            if (!roomTBAFlag) {

                            	String roomNumber = meetingNode.elementText("RoomNumber");
                            	RoomInfo roomInfo = new RoomInfo();
                            	roomInfo.setRoomCode(roomNumber);
//                            	roomInfo.setBuildingId(building);
                            	scdi.setRoom(roomInfo);
                            }
                        }

                        /*Instructor Details*/
                        {
                            DefaultXPath instructorPath = newXPath("/s:Section/s:Meetings/s:Meeting/s:Instructors");
                            List instructors = instructorPath.selectNodes(doc);
                            for (Object instObj : instructors) {
                                Element instructorNode = (Element) instObj;
                                Element instructor = instructorNode.element("Instructor");
                                if (instructor != null) {
                                    instructorName = instructor.element("Person").elementText("Name");
                                    instructorId = instructor.element("Person").elementText("RegID");
                                }
                            }
                        }

                    }
                }


                /*Course Flags*/
                {
                    info.getAttributes().add(new AttributeInfo("Writing", String.valueOf(Boolean.valueOf(sectionNode.element("GeneralEducationRequirements").elementText("Writing")))));
                    info.getAttributes().add(new AttributeInfo("ServiceLearning", String.valueOf(Boolean.valueOf(sectionNode.elementText("ServiceLearning")))));
                    info.getAttributes().add(new AttributeInfo("ResearchCredit", String.valueOf(Boolean.valueOf(sectionNode.elementText("ResearchCredit")))));
                    info.getAttributes().add(new AttributeInfo("DistanceLearning", String.valueOf(Boolean.valueOf(sectionNode.elementText("DistanceLearning")))));
                    info.getAttributes().add(new AttributeInfo("JointSections", String.valueOf(sectionNode.element("JointSections").content().size() > 0)));
                    info.getAttributes().add(new AttributeInfo("FinancialAidEligible", String.valueOf(Boolean.valueOf(sectionNode.elementText("FinancialAidEligible")))));
                }


                info.setTypeName(typeName);
                info.setInstructorName(instructorName);
                info.setInstructorId(instructorId);
                info.setCourseOfferingTitle(title);
//		        info.setCourseCode( curric );
                info.setCourseOfferingCode(curric + " " + id);
                info.setActivityOfferingCode(id);
                info.setIsHonorsOffering(Boolean.valueOf(sectionNode.elementText("HonorsCourse")));
                AttributeInfo attrib = new AttributeInfo("SLN", sln);
                info.getAttributes().add(attrib);

                result.add(info);

            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }

        return result;
    }
}
