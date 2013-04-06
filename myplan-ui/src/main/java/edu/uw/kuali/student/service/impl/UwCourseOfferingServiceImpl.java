package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
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
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.AtpHelper.YearTerm;
import org.kuali.student.myplan.utils.TimeStringMillisConverter;
import org.kuali.student.r2.common.dto.*;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.core.room.dto.BuildingInfo;
import org.kuali.student.r2.core.room.dto.RoomInfo;
import org.kuali.student.r2.core.scheduling.dto.TimeSlotInfo;
import org.kuali.student.r2.core.type.dto.TypeInfo;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UW implementation of CourseOfferingService.
 */
public class UwCourseOfferingServiceImpl implements CourseOfferingService {

    private final static Logger logger = Logger.getLogger(UwCourseOfferingServiceImpl.class);

    private static int CRITERIA_LENGTH = 24;

    private transient CourseService courseService;
    private StudentServiceClient studentServiceClient;

    private List<String> DAY_LIST = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static Pattern regexInstituteCodePrefix = Pattern.compile("([0-9]+)(.)*");

    public UwCourseOfferingServiceImpl() {
    }

    private static Document newDocument(String xml) throws DocumentException {
        SAXReader sax = new SAXReader();
        StringReader sr = new StringReader(xml);
        Document doc = sax.read(sr);
        return doc;
    }

    private static Map<String, String> NAMESPACES = new HashMap<String, String>() {{
        put("s", "http://webservices.washington.edu/student/");
    }};


    public static DefaultXPath newXPath(String expr) {
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
        StringBuffer courseComments = new StringBuffer();
        StringBuffer curriculumComments = new StringBuffer();
        String[] list = courseOfferingId.split("=");
        String termId = AtpHelper.getAtpIdFromTermAndYear(list[1], list[0]);
        String year = list[0];
        String quarter = list[1];
        String curric = list[2];
        String num = list[3];
        String sectionID = list[4];
        DefaultXPath secondaryPath = newXPath("/s:Section/s:PrimarySection");
        DefaultXPath secondarySectionPath = newXPath("/s:Section");
        DefaultXPath courseCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:CourseComments/s:Lines");
        DefaultXPath curriculumCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:CurriculumComments/s:Lines");

        Document secondaryDoc = null;
        try {
            String xml = studentServiceClient.getSecondarySections(year, quarter, curric, num, sectionID);
            secondaryDoc = newDocument(xml);
        } catch (ServiceException e) {
            logger.warn(e);
            // Skip this section ID if it fails
        } catch (DocumentException e) {
            logger.warn(e);
        }

        Element secondarySection = (Element) secondarySectionPath.selectSingleNode(secondaryDoc);
        Element dupeSectionElement = (Element) secondaryPath.selectSingleNode(secondaryDoc);
        Element courseCommentsNode = (Element) courseCommentsPath.selectSingleNode(secondaryDoc);
        Element curriculumCommentsNode = (Element) curriculumCommentsPath.selectSingleNode(secondaryDoc);
        List comments = courseCommentsNode.content();
        List curricComments = curriculumCommentsNode.content();
        for (Object ob : comments) {
            Element element = (Element) ob;
            String text = element.elementText("Text");
            if (text.startsWith("*****")) {
                courseComments = courseComments.append("<br>" + text + "</br> ");
            } else {
                courseComments = courseComments.append(text + " ");
            }
        }
        for (Object ob : curricComments) {
            Element element = (Element) ob;
            String text = element.elementText("Text");
            if (text.startsWith("*****")) {
                curriculumComments = curriculumComments.append("<br>" + text + "</br> ");
            } else {
                curriculumComments = curriculumComments.append(text + " ");
            }
        }

        String primaryID = dupeSectionElement.elementText("SectionID");


        CourseOfferingInfo info = new CourseOfferingInfo();
        info.setSubjectArea(curric);
        info.setCourseCode(num);
        info.setTermId(termId);
        info.setId(join("=", year, quarter, curric, num, sectionID));
        CourseHelper courseHelper = new CourseHelperImpl();
        String courseID = courseHelper.getCourseIdForTerm(curric, num, termId);
        info.setCourseId(courseID);
        info.getAttributes().add(new AttributeInfo("CourseComments", courseComments.toString()));
        info.getAttributes().add(new AttributeInfo("CurriculumComments", curriculumComments.toString()));
        info.getAttributes().add(new AttributeInfo("PrimarySectionId", primaryID));

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

            // Default values so its visually obvious when the mapping is incorrect
            String creditID = "X";
            String creditName = "X";

            if ("fixed credit".equals(creditControl)) {
                creditID = minCreditID;
                creditName = minCreditName;
            } else if ("variable credit - min to max credits".equals(creditControl)) {
                creditID = minCreditID + "-" + maxCreditID;
                creditName = minCreditName + "-" + maxCreditName;
            } else if ("variable credit - min or max credits".equals(creditControl)) {
                creditID = minCreditID + ", " + maxCreditID;
                creditName = minCreditName + ", " + maxCreditName;
            } else if ("variable credit - 1 to 25 credits".equals(creditControl)) {
                creditID = "1.0-25.0";
                creditName = "1-25";
            } else if ("zero credits".equals(creditControl)) {
                creditID = "0.0";
                creditName = "0";
            }

            creditID = "kuali.uw.resultcomponent.credit." + creditID;
            info.setCreditOptionId(creditID);
            info.setCreditOptionName(creditName);

        }
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
        ActivityOfferingInfo activityOfferingInfo = new ActivityOfferingInfo();

        String[] params = activityOfferingId.split(",");

        String year = params[0];
        String term = params[1];
        String curric = params[2];
        String courseNumber = params[3];
        if (params.length == 4) {
            try {
                String allText = studentServiceClient.getTimeSchedules(year, term, curric, courseNumber, null);
                Document allDoc = newDocument(allText);

                DefaultXPath xpath = newXPath("//s:Sections/s:Section/s:Href");
                List sectionNodeList = xpath.selectNodes(allDoc);
                for (Object node : sectionNodeList) {
                    Element element = (Element) node;
                    String href = element.getText();
                    href = href.replace("/student", "").trim();
                    String hrefText = studentServiceClient.getTimeSchedules(null, null, null, null, href);
                    Document hrefDoc = newDocument(hrefText);
                    DefaultXPath xpath2 = newXPath("//s:Curriculum");
                    List curricNodeList = xpath2.selectNodes(hrefDoc);
                    for (Object curricNode : curricNodeList) {
                        String abbrev = ((Element) curricNode).elementText("TimeScheduleLinkAbbreviation");

                        AttributeInfo attrib = new AttributeInfo(CourseSearchConstants.TIME_SCHEDULE_KEY, abbrev);

                        activityOfferingInfo.getAttributes().add(attrib);
                    }
                }
            } catch (Exception e) {
                logger.error(e);
            }
        } else if (params.length == 5) {
            try {
                /* calls https://ucswseval1.cac.washington.edu/student/v4/course/2013,winter,ECON,200/BA
       * getting the sln for that section of the course*/
                String xml = studentServiceClient.getSecondarySections(year, term, curric, courseNumber, params[4]);
                Document doc = newDocument(xml);
                DefaultXPath sectionPath = newXPath("/s:Section");
                Element sectionNode = (Element) sectionPath.selectSingleNode(doc);
                AttributeInfo attributeInfo = new AttributeInfo("SLN", sectionNode.elementText("SLN"));
                activityOfferingInfo.getAttributes().add(attributeInfo);

            } catch (Exception e) {
                logger.error(e);
            }

        }

        return activityOfferingInfo;

    }

    @Override
    public ActivityOfferingDisplayInfo getActivityOfferingDisplay(@WebParam(name = "activityOfferingId") String activityOfferingId, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        String[] list = activityOfferingId.split("=");
        String year = list[0];
        String quarter = list[1];
        String curric = list[2];
        String num = list[3];
        String sectionID = list[4];
        Document doc = null;
        try {
            // Skips section ID if it fails
            String xml = studentServiceClient.getSecondarySections(year, quarter, curric, num, sectionID);
            doc = newDocument(xml);
        } catch (Exception e) {
            logger.warn(e);
        }

        DefaultXPath sectionPath = newXPath("/s:Section");
        Element sectionNode = (Element) sectionPath.selectSingleNode(doc);

        String instituteCode = null;

        {
            DefaultXPath linkPath = newXPath("s:Curriculum/s:TimeScheduleLinkAbbreviation");
            Element link = (Element) linkPath.selectSingleNode(sectionNode);
            instituteCode = link.getTextTrim();

// displaying main campus, PCE, and ROTC
// refer to https://jira.cac.washington.edu/browse/MYPLAN-1583 for list of supported institute codes
            int instituteNumber = getInstituteNumber(instituteCode);
            switch (instituteNumber) {
                case 0: // Main campus
                case 95: // PCE
                case 88: // ROTC
                {
                    instituteCode = Integer.toString(instituteNumber);
                    break;
                }

                case -1: //  POA #1: Exclude sections with a null/blankTimeScheduleLinkAbbreviation.
                default: // Also omit all others not in above list
            }

        }

        String instituteName = sectionNode.elementTextTrim("InstituteName");

        ActivityOfferingDisplayInfo info = new ActivityOfferingDisplayInfo();
        String sectionComments = null;
        {
            DefaultXPath sectionCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:SectionComments/s:Lines");
            Element sectionCommentsNode = (Element) sectionCommentsPath.selectSingleNode(doc);
            StringBuilder sb = new StringBuilder();
            List comments = sectionCommentsNode.content();
            for (Object ob : comments) {
                Element element = (Element) ob;
                sb.append(element.elementText("Text") + " ");
            }
            sectionComments = sb.toString();
        }


        String typeName = sectionNode.elementText("SectionType");
        info.setTypeName(typeName);

        String campus = sectionNode.elementText("CourseCampus");

        {
            DefaultXPath coursePath = newXPath("/s:Section/s:Course");
            Element courseNode = (Element) coursePath.selectSingleNode(doc);
            String title = courseNode.elementText("CourseTitle");
            info.setCourseOfferingTitle(title);
        }


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
                        buildingInfo.setCampusKey(campus);
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
                        scdi.setRoom(roomInfo);
                    }
                }

                {
                    String name = "--";
                    String regid = "--";

                    DefaultXPath instructorPath = newXPath("/s:Section/s:Meetings/s:Meeting/s:Instructors/s:Instructor/s:Person");
                    List instructors = instructorPath.selectNodes(doc);

                    for (Object node : instructors) {
                        Element instructor = (Element) node;
                        name = instructor.elementText("Name");
                        name = name.replaceFirst(",", ", ");
                        regid = instructor.elementText("RegID");
// Only show the first instructor
                        break;
                    }
                    info.setInstructorName(name);
                    info.setInstructorId(regid);
                }
            }
        }

        String feeAmount = sectionNode.elementTextTrim("FeeAmount");
        if (feeAmount.contains(".")) {
            feeAmount = feeAmount.substring(0, feeAmount.indexOf("."));
        }

        String summerTerm = sectionNode.elementText("SummerTerm");
        String sln = sectionNode.elementText("SLN");


        info.setCourseOfferingCode(curric + " " + sectionID);
        info.setActivityOfferingCode(sectionID);
        info.setIsHonorsOffering(Boolean.valueOf(sectionNode.elementText("HonorsCourse")));


/*Course Flags*/
        List<AttributeInfo> attributes = info.getAttributes();
        attributes.add(new AttributeInfo("InstituteCode", instituteCode));
        attributes.add(new AttributeInfo("InstituteName", instituteName));
        attributes.add(new AttributeInfo("Campus", campus));
        attributes.add(new AttributeInfo("Writing", String.valueOf(Boolean.valueOf(sectionNode.element("GeneralEducationRequirements").elementText("Writing")))));
        attributes.add(new AttributeInfo("ServiceLearning", String.valueOf(Boolean.valueOf(sectionNode.elementText("ServiceLearning")))));
        attributes.add(new AttributeInfo("ResearchCredit", String.valueOf(Boolean.valueOf(sectionNode.elementText("ResearchCredit")))));
        attributes.add(new AttributeInfo("DistanceLearning", String.valueOf(Boolean.valueOf(sectionNode.elementText("DistanceLearning")))));
        attributes.add(new AttributeInfo("JointSections", String.valueOf(sectionNode.element("JointSections").content().size() > 0)));
        attributes.add(new AttributeInfo("FinancialAidEligible", String.valueOf(Boolean.valueOf(sectionNode.elementText("FinancialAidEligible").length() > 0))));
        attributes.add(new AttributeInfo("AddCodeRequired", String.valueOf(Boolean.valueOf(sectionNode.elementText("AddCodeRequired")))));
        attributes.add(new AttributeInfo("IndependentStudy", String.valueOf(Boolean.valueOf(sectionNode.elementText("IndependentStudy")))));
        attributes.add(new AttributeInfo("EnrollmentRestrictions", String.valueOf(Boolean.valueOf(sectionNode.elementText("EnrollmentRestrictions")))));
        attributes.add(new AttributeInfo("FeeAmount", feeAmount));
        attributes.add(new AttributeInfo("SectionComments", sectionComments));
        attributes.add(new AttributeInfo("SummerTerm", summerTerm));
        attributes.add(new AttributeInfo("SLN", sln));
        attributes.add(new AttributeInfo("PrimaryActivityOfferingCode", sectionNode.element("PrimarySection").elementText("SectionID")));
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
            String courseCode = strings[2].trim();

            try {
                String responseText = studentServiceClient.getSections(year, curriculum, courseCode);
                Document document = newDocument(responseText);
                DefaultXPath sectionPath = newXPath("/s:SearchResults/s:Sections/s:Section");
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

            DefaultXPath sectionPath = newXPath("/s:SearchResults/s:Sections/s:Section");
            DefaultXPath secondaryPath = newXPath("/s:Section/s:PrimarySection");
            DefaultXPath secondarySectionPath = newXPath("/s:Section");
            DefaultXPath courseCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:CourseComments/s:Lines");
            DefaultXPath curriculumCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:CurriculumComments/s:Lines");


            Document doc = newDocument(xml);

            List sections = sectionPath.selectNodes(doc);
            for (Object object : sections) {
                StringBuffer courseComments = new StringBuffer();
                StringBuffer curriculumComments = new StringBuffer();
                Element primarySectionNode = (Element) object;
                String primarySectionID = primarySectionNode.elementText("SectionID");


                Document secondaryDoc;
                try {
                    String secondaryXML = studentServiceClient.getSecondarySections(year, quarter, curric, num, primarySectionID);
                    secondaryDoc = newDocument(secondaryXML);
                } catch (ServiceException e) {
                    logger.warn(e);
// Skip this section ID if it fails
                    continue;
                }

                Element secondarySection = (Element) secondarySectionPath.selectSingleNode(secondaryDoc);
                Element dupeSectionElement = (Element) secondaryPath.selectSingleNode(secondaryDoc);
                Element courseCommentsNode = (Element) courseCommentsPath.selectSingleNode(secondaryDoc);
                Element curriculumCommentsNode = (Element) curriculumCommentsPath.selectSingleNode(secondaryDoc);
                List comments = courseCommentsNode.content();
                List curricComments = curriculumCommentsNode.content();
                for (Object ob : comments) {
                    Element element = (Element) ob;
                    String text = element.elementText("Text");
                    if (text.startsWith("*****")) {
                        courseComments = courseComments.append("<br>" + text + "</br> ");
                    } else {
                        courseComments = courseComments.append(text + " ");
                    }
                }
                for (Object ob : curricComments) {
                    Element element = (Element) ob;
                    String text = element.elementText("Text");
                    if (text.startsWith("*****")) {
                        curriculumComments = curriculumComments.append("<br>" + text + "</br> ");
                    } else {
                        curriculumComments = curriculumComments.append(text + " ");
                    }
                }

                String primaryID = dupeSectionElement.elementText("SectionID");
                if (primarySectionID.equals(primaryID)) {
                    CourseOfferingInfo info = new CourseOfferingInfo();
                    info.setSubjectArea(curric);
                    info.setCourseCode(num);
                    info.setTermId(termId);
                    info.setId(join("=", year, quarter, curric, num, primarySectionID));
                    CourseHelper courseHelper = new CourseHelperImpl();
                    info.setCourseId(courseHelper.getCourseIdForTerm(curric, num, termId));
                    info.getAttributes().add(new AttributeInfo("CourseComments", courseComments.toString()));
                    info.getAttributes().add(new AttributeInfo("CurriculumComments", curriculumComments.toString()));
                    info.getAttributes().add(new AttributeInfo("PrimarySectionId", primarySectionID));

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

                        // Default values so its visually obvious when the mapping is incorrect
                        String creditID = "X";
                        String creditName = "X";

                        if ("fixed credit".equals(creditControl)) {
                            creditID = minCreditID;
                            creditName = minCreditName;
                        } else if ("variable credit - min to max credits".equals(creditControl)) {
                            creditID = minCreditID + "-" + maxCreditID;
                            creditName = minCreditName + "-" + maxCreditName;
                        } else if ("variable credit - min or max credits".equals(creditControl)) {
                            creditID = minCreditID + ", " + maxCreditID;
                            creditName = minCreditName + ", " + maxCreditName;
                        } else if ("variable credit - 1 to 25 credits".equals(creditControl)) {
                            creditID = "1.0-25.0";
                            creditName = "1-25";
                        } else if ("zero credits".equals(creditControl)) {
                            creditID = "0.0";
                            creditName = "0";
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
            String primarySectionID = list[4];

            List<String> sectionList = new ArrayList<String>();
            sectionList.add(primarySectionID);
            {
                try {
                    String xml = studentServiceClient.getSecondarySections(year, quarter, curric, num, primarySectionID);
                    DefaultXPath sectionListPath = newXPath("//s:LinkedSection/s:Section");
                    Document doc = newDocument(xml);
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
                    String xml = studentServiceClient.getSecondarySections(year, quarter, curric, num, sectionID);
                    doc = newDocument(xml);
                } catch (Exception e) {
                    logger.warn(e);
                    continue;
                }

                DefaultXPath sectionPath = newXPath("/s:Section");
                Element sectionNode = (Element) sectionPath.selectSingleNode(doc);

                String instituteCode = null;

                {
                    DefaultXPath linkPath = newXPath("s:Curriculum/s:TimeScheduleLinkAbbreviation");
                    Element link = (Element) linkPath.selectSingleNode(sectionNode);
                    instituteCode = link.getTextTrim();

// displaying main campus, PCE, and ROTC
// refer to https://jira.cac.washington.edu/browse/MYPLAN-1583 for list of supported institute codes
                    int instituteNumber = getInstituteNumber(instituteCode);
                    switch (instituteNumber) {
                        case 0: // Main campus
                        case 95: // PCE
                        case 88: // ROTC
                        {
                            instituteCode = Integer.toString(instituteNumber);
                            break;
                        }

                        case -1: //  POA #1: Exclude sections with a null/blankTimeScheduleLinkAbbreviation.
                        default: // Also omit all others not in above list
                        {

                            continue;
                        }
                    }

                }

                String instituteName = sectionNode.elementTextTrim("InstituteName");

                ActivityOfferingDisplayInfo info = new ActivityOfferingDisplayInfo();


                String sectionComments = null;
                {
                    DefaultXPath sectionCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:SectionComments/s:Lines");
                    Element sectionCommentsNode = (Element) sectionCommentsPath.selectSingleNode(doc);
                    StringBuilder sb = new StringBuilder();
                    List comments = sectionCommentsNode.content();
                    for (Object ob : comments) {
                        Element element = (Element) ob;
                        sb.append(element.elementText("Text") + " ");
                    }
                    sectionComments = sb.toString();
                }


                String typeName = sectionNode.elementText("SectionType");
                info.setTypeName(typeName);

                String campus = sectionNode.elementText("CourseCampus");

                {
                    DefaultXPath coursePath = newXPath("/s:Section/s:Course");
                    Element courseNode = (Element) coursePath.selectSingleNode(doc);
                    String title = courseNode.elementText("CourseTitle");
                    info.setCourseOfferingTitle(title);
                }


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
                                buildingInfo.setCampusKey(campus);
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
                                scdi.setRoom(roomInfo);
                            }
                        }

                        {
                            String name = "--";
                            String regid = "--";

                            DefaultXPath instructorPath = newXPath("/s:Section/s:Meetings/s:Meeting/s:Instructors/s:Instructor/s:Person");
                            List instructors = instructorPath.selectNodes(doc);

                            for (Object node : instructors) {
                                Element instructor = (Element) node;
                                name = instructor.elementText("Name");
                                name = name.replaceFirst(",", ", ");
                                regid = instructor.elementText("RegID");
// Only show the first instructor
                                break;
                            }
                            info.setInstructorName(name);
                            info.setInstructorId(regid);
                        }
                    }
                }

                String feeAmount = sectionNode.elementTextTrim("FeeAmount");
                if (feeAmount.contains(".")) {
                    feeAmount = feeAmount.substring(0, feeAmount.indexOf("."));
                }

                String summerTerm = sectionNode.elementText("SummerTerm");
                String sln = sectionNode.elementText("SLN");


                info.setCourseOfferingCode(curric + " " + sectionID);
                info.setActivityOfferingCode(sectionID);
                info.setIsHonorsOffering(Boolean.valueOf(sectionNode.elementText("HonorsCourse")));


/*Course Flags*/
                List<AttributeInfo> attributes = info.getAttributes();
                attributes.add(new AttributeInfo("InstituteCode", instituteCode));
                attributes.add(new AttributeInfo("InstituteName", instituteName));
                attributes.add(new AttributeInfo("Campus", campus));
                attributes.add(new AttributeInfo("Writing", String.valueOf(Boolean.valueOf(sectionNode.element("GeneralEducationRequirements").elementText("Writing")))));
                attributes.add(new AttributeInfo("ServiceLearning", String.valueOf(Boolean.valueOf(sectionNode.elementText("ServiceLearning")))));
                attributes.add(new AttributeInfo("ResearchCredit", String.valueOf(Boolean.valueOf(sectionNode.elementText("ResearchCredit")))));
                attributes.add(new AttributeInfo("DistanceLearning", String.valueOf(Boolean.valueOf(sectionNode.elementText("DistanceLearning")))));
                attributes.add(new AttributeInfo("JointSections", String.valueOf(sectionNode.element("JointSections").content().size() > 0)));
                attributes.add(new AttributeInfo("FinancialAidEligible", String.valueOf(Boolean.valueOf(sectionNode.elementText("FinancialAidEligible").length() > 0))));
                attributes.add(new AttributeInfo("AddCodeRequired", String.valueOf(Boolean.valueOf(sectionNode.elementText("AddCodeRequired")))));
                attributes.add(new AttributeInfo("IndependentStudy", String.valueOf(Boolean.valueOf(sectionNode.elementText("IndependentStudy")))));
                attributes.add(new AttributeInfo("EnrollmentRestrictions", String.valueOf(Boolean.valueOf(sectionNode.elementText("EnrollmentRestrictions")))));
                attributes.add(new AttributeInfo("FeeAmount", feeAmount));
                attributes.add(new AttributeInfo("SectionComments", sectionComments));
                attributes.add(new AttributeInfo("SummerTerm", summerTerm));
                attributes.add(new AttributeInfo("SLN", sln));
                attributes.add(new AttributeInfo("PrimaryActivityOfferingCode", sectionNode.element("PrimarySection").elementText("SectionID")));


                result.add(info);


            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new OperationFailedException(e.getMessage());
        }

        return result;
    }

    /**
     * Extracts institute number prefix from the SWS link information, which is reused as the institutecode.
     * Blank (empty, null) returns -1.
     * <p/>
     * <p/>
     * "123abc" -> 123
     * "abc" -> 0
     * "" -> -1
     * null -> -1
     */

//  POA #1: Exclude sections with a null/blankTimeScheduleLinkAbbreviation.
    private static int getInstituteNumber(String link) {
        if (link == null) return -1;
        if ("".equals(link.trim())) return -1;

        int institute = 0;
        Matcher m = regexInstituteCodePrefix.matcher(link);
        if (m.find()) {
            String ugh = m.group(1);
            institute = Integer.parseInt(ugh);
        }

        return institute;
    }
}
