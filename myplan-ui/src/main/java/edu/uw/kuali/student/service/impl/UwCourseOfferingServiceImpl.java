package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.student.core.statement.dto.StatementTreeViewInfo;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.dto.RegistrationGroupInfo;
import org.kuali.student.enrollment.courseoffering.dto.SeatPoolDefinitionInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;

import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.kuali.student.enrollment.courseregistration.dto.CourseRegistrationInfo;
import org.kuali.student.r2.common.datadictionary.dto.DictionaryEntryInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.TypeInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;

import javax.jws.WebParam;
import java.io.StringReader;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UW implementation of CourseOfferingService.
 */
public class UwCourseOfferingServiceImpl implements CourseOfferingService {
    private final static Logger logger = Logger.getLogger(UwCourseOfferingServiceImpl.class);

    private static final String REGEX_TERM = "([a-z]*)\\d{4}$";
    private static final String REGEX_YEAR = "(\\d{4})$";
    private static Pattern patternTerm;
    private static Pattern patternYear;

    private SAXReader reader;

    private StudentServiceClient studentServiceClient;

    //  TODO: Make this static and synchronized.
    private static Map<String, Set<String>> courseOfferingCache;



    public UwCourseOfferingServiceImpl() {
        courseOfferingCache = new ConcurrentHashMap<String, Set<String>>(1000);

        //  Compile regexs for parsing term and year from termKey.
        patternTerm = Pattern.compile(REGEX_TERM, Pattern.CASE_INSENSITIVE);
        patternYear = Pattern.compile(REGEX_YEAR);

        this.reader = new SAXReader();

    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    @Override
    public List<String> getCourseOfferingIdsByTermAndSubjectArea(@WebParam(name = "termKey") String termKey,
                                                                 @WebParam(name = "subjectArea") String subjectArea,
                                                                 @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException,
                OperationFailedException, PermissionDeniedException {

        List<String> ids = new ArrayList<String>(100);

        String cacheKey = termKey + ":" + subjectArea;
        //   Use the cache if the info is available. Otherwise, query the student service and cache the results.
        if (courseOfferingCache.containsKey(cacheKey)) {
            Set<String> co = courseOfferingCache.get(cacheKey);
            ids.addAll(co);
        } else {
            //  Query the web service and cache the results if the call completes successfully.
            Set<String> courseCodes = null;
            try {
                courseCodes = getCourseOfferings(termKey, subjectArea);
            } catch (ServiceException e) {
                logger.error("Call to the student service failed.", e);
            }

            if (courseCodes != null) {
                courseOfferingCache.put(cacheKey, courseCodes);
                //  Add course codes to the return list.
                ids.addAll(courseCodes);
            }
        }
        return ids;
    }

    /**
     * Queries the student service and creates a collection of courseCodes for a given termKey (term, year)
     * and subject area (curriculum abbreviation)
     * @param termKey  A term key like 'kuali.uw.atp.winter1970'.
     * @param subjectArea A subject area like 'chem'.
     * @return A Set of course Codes.
     */
    private Set<String> getCourseOfferings(String termKey, String subjectArea) throws ServiceException {

        String term, year;

        Matcher matcher = patternTerm.matcher(termKey);
        if (matcher.find()) {
            term = matcher.group(1);
        } else {
            throw new RuntimeException("Term key did not contain a term.");
        }

        matcher = patternYear.matcher(termKey);
        if (matcher.find()) {
            year = matcher.group(1);
        } else {
            throw new RuntimeException("Term key did not contain a year.");
        }

        String responseText = studentServiceClient.getSectionInfo(year, term, subjectArea);

        Document document = null;
        try {
            document = reader.read(new StringReader(responseText));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse reply from the Student Term Service.", e);
        }

        Map map = new HashMap();
        DefaultXPath xpath = new DefaultXPath("//s:Section");
        Map<String,String> namespaces = new HashMap<String,String>();
        namespaces.put("s","http://webservices.washington.edu/student/");
        xpath.setNamespaceURIs(namespaces);

        Set<String> courseCodes = new HashSet<String>(100);

        List sections = xpath.selectNodes(document);
        for (Object node : sections) {
            Element section = (Element) node;
            String number = section.elementText("CourseNumber");
            String ca = section.elementText("CurriculumAbbreviation");
            courseCodes.add(ca + "_" + number);
        }

        return courseCodes;
    }

    /**
     *
<SearchResults xmlns="http://webservices.washington.edu/student/" xmlns:i="http://www.w3.org/2001/XMLSchema-instance">
<Sections>
<Section>
<Href>
/student/v4/public/course/2011,winter,CHEM,142/A.xml
</Href>
<CourseNumber>142</CourseNumber>
<CurriculumAbbreviation>CHEM</CurriculumAbbreviation>
<Quarter>winter</Quarter>
<SectionID>A</SectionID>
<Year>2011</Year>
</Section>
     *
     * @param courseOfferingId
     * @param context
     * @return
     * @throws DoesNotExistException
     * @throws InvalidParameterException
     * @throws MissingParameterException
     * @throws OperationFailedException
     * @throws PermissionDeniedException
     */


    @Override
    public CourseOfferingInfo getCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<CourseOfferingInfo> getCourseOfferingsByIdList(@WebParam(name = "courseOfferingIds") List<String> courseOfferingIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<CourseOfferingInfo> getCourseOfferingsForCourseAndTerm(@WebParam(name = "courseId") String courseId, @WebParam(name = "termKey") String termKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getCourseOfferingIdsForTerm(@WebParam(name = "termKey") String termKey, @WebParam(name = "useIncludedTerm") Boolean useIncludedTerm, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getCourseOfferingIdsByTermAndInstructorId(@WebParam(name = "termKey") String termKey, @WebParam(name = "instructorId") String instructorId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getCourseOfferingIdsByTermAndUnitContentOwner(@WebParam(name = "termKey") String termKey, @WebParam(name = "unitOwnerId") String unitOwnerId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public CourseOfferingInfo createCourseOfferingFromCanonical(@WebParam(name = "courseId") String courseId, @WebParam(name = "termKey") String termKey, @WebParam(name = "formatIdList") List<String> formatIdList, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public CourseOfferingInfo updateCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "courseOfferingInfo") CourseOfferingInfo courseOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public CourseOfferingInfo updateCourseOfferingFromCanonical(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateCourseOffering(@WebParam(name = "validationType") String validationType, @WebParam(name = "courseOfferingInfo") CourseOfferingInfo courseOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StatementTreeViewInfo> getCourseOfferingRestrictions(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatementTreeViewInfo createCourseOfferingRestriction(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatementTreeViewInfo updateCourseOfferingRestriction(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException, CircularReferenceException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteCourseOfferingRestriction(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "restrictionId") String restrictionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateCourseOfferingRestriction(@WebParam(name = "validationType") String validationType, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public TypeInfo getActivityOfferingType(@WebParam(name = "activityOfferingTypeKey") String activityOfferingTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getAllActivityOfferingTypes(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<TypeInfo> getActivityOfferingTypesForActivityType(@WebParam(name = "activityTypeKey") String activityTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public ActivityOfferingInfo getActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ActivityOfferingInfo> getActivityOfferingsByIdList(@WebParam(name = "activityOfferingIds") List<String> activityOfferingIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ActivityOfferingInfo> getActivitiesForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public ActivityOfferingInfo createActivityOffering(@WebParam(name = "courseOfferingIdList") List<String> courseOfferingIdList, @WebParam(name = "activityOfferingInfo") ActivityOfferingInfo activityOfferingInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo assignActivityToCourseOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "courseOfferingIdList") List<String> courseOfferingIdList, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
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
    public List<ValidationResultInfo> validateActivityOffering(@WebParam(name = "validationType") String validationType, @WebParam(name = "activityOfferingInfo") ActivityOfferingInfo activityOfferingInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<StatementTreeViewInfo> getActivityOfferingRestrictions(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "nlUsageTypeKey") String nlUsageTypeKey, @WebParam(name = "language") String language, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatementTreeViewInfo createActivityOfferingRestriction(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatementTreeViewInfo updateActivityOfferingRestriction(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, DataValidationErrorException, CircularReferenceException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteActivityOfferingRestriction(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "restrictionId") String restrictionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateActivityOfferingRestriction(@WebParam(name = "validationType") String validationType, @WebParam(name = "restrictionInfo") StatementTreeViewInfo restrictionInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
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
    public List<ActivityOfferingInfo> copyActivityOffering(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "numberOfCopies") Integer numberOfCopies, @WebParam(name = "copyContextTypeKey") String copyContextTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public RegistrationGroupInfo getRegistrationGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<RegistrationGroupInfo> getRegistrationGroupsByIdList(@WebParam(name = "registrationGroupIds") List<String> registrationGroupIds, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<RegistrationGroupInfo> getRegGroupsForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<RegistrationGroupInfo> getRegGroupsByFormatForCourse(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "formatTypeKey") String formatTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public RegistrationGroupInfo createRegistrationGroup(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "registrationGroupInfo") RegistrationGroupInfo registrationGroupInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DoesNotExistException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
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
    public List<ValidationResultInfo> validateRegistrationGroup(@WebParam(name = "validationType") String validationType, @WebParam(name = "registrationGroupInfo") RegistrationGroupInfo registrationGroupInfo, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public SeatPoolDefinitionInfo getSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<SeatPoolDefinitionInfo> getSeatPoolsForCourseOffering(@WebParam(name = "courseOfferingId") String courseOfferingId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<SeatPoolDefinitionInfo> getSeatPoolsForRegGroup(@WebParam(name = "registrationGroupId") String registrationGroupId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public SeatPoolDefinitionInfo createSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionInfo") SeatPoolDefinitionInfo seatPoolDefinitionInfo, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public SeatPoolDefinitionInfo updateSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "seatPoolDefinitionInfo") SeatPoolDefinitionInfo seatPoolDefinitionInfo, @WebParam(name = "context") ContextInfo context) throws DataValidationErrorException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException, VersionMismatchException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public StatusInfo deleteSeatPoolDefinition(@WebParam(name = "seatPoolDefinitionId") String seatPoolDefinitionId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<CourseOfferingInfo> searchForCourseOfferings(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
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
    public List<CourseRegistrationInfo> searchForRegistrationGroups(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForRegistrationGroupIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<SeatPoolDefinitionInfo> searchForSeatpoolDefintions(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> searchForSeatpoolDefintionIds(@WebParam(name = "criteria") QueryByCriteria criteria, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<String> getDataDictionaryEntryKeys(@WebParam(name = "context") ContextInfo context) throws OperationFailedException, MissingParameterException, PermissionDeniedException {
         throw new RuntimeException("Not implemented.");
    }

    @Override
    public DictionaryEntryInfo getDataDictionaryEntry(@WebParam(name = "entryKey") String entryKey, @WebParam(name = "context") ContextInfo context) throws OperationFailedException, MissingParameterException, PermissionDeniedException, DoesNotExistException {
         throw new RuntimeException("Not implemented.");
    }
}
