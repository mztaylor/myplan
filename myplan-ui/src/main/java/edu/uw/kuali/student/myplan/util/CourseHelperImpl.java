package edu.uw.kuali.student.myplan.util;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.exceptions.DoesNotExistException;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.common.search.dto.SearchResultCell;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.*;
import java.util.regex.Pattern;

public class CourseHelperImpl implements CourseHelper {

    private final Logger logger = Logger.getLogger(CourseHelperImpl.class);
    private StudentServiceClient studentServiceClient;

    private CourseService courseService;

    private CourseOfferingService courseOfferingService;

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
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

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    public StudentServiceClient getStudentServiceClient() {
        if (studentServiceClient == null) {
            studentServiceClient = (StudentServiceClient) GlobalResourceLoader.getService(StudentServiceClient.SERVICE_NAME);
        }
        return studentServiceClient;
    }

    private static transient LuService luService;

    protected static LuService getLuService() {
        if (luService == null) {
            luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return luService;
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

    public LinkedHashMap<String, LinkedHashMap<String, Object>> getAllSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> mapmap, AtpHelper.YearTerm yt,
                                                                                    String curric, String num) throws ServiceException, DocumentException {
        StudentServiceClient client = getStudentServiceClient();

        String year = yt.getYearAsString();
        String quarter = yt.getTermAsID();
        String xml = client.getSections(year, quarter, curric, num);
        Document doc = newDocument(xml);
        DefaultXPath statusPath = newXPath("/s:SearchResults/s:Sections/s:Section/s:SectionID");
        List list = statusPath.selectNodes(doc);
        for (Object node : list) {
            Element element = (Element) node;
            String section = element.getTextTrim();

            doSectionStatus(mapmap, client, yt, curric, num, section);

        }
        return mapmap;
    }


    /**
     * Used to Split the course code into division and Code.
     * eg: "COM 243" is returned as CourseCode with division=COM and number=243 and section=null.
     * eg: "COM 243 A" is returned as CourseCode with division=COM , number=243 and section=A.
     *
     * @param courseCode
     * @return
     */
    @Override
    public DeconstructedCourseCode getCourseDivisionAndNumber(String courseCode) {
        String subject = null;
        String number = null;
        String activityCd = null;
        if (courseCode.matches(CourseSearchConstants.FORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.COURSE_CODE_WITH_SECTION_REGEX)) {
            activityCd = courseCode.substring(courseCode.lastIndexOf(" "), courseCode.length()).trim();
            courseCode = courseCode.substring(0, courseCode.lastIndexOf(" ")).trim();
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        }
        return new DeconstructedCourseCode(subject, number, activityCd);
    }

    /**
     * Used to get the course Id for the given subject area and course number (CHEM, 120)
     * Uses last scheduled term to calculate the course Id
     *
     * @param subjectArea
     * @param number
     * @return
     */
    @Override
    public String getCourseId(String subjectArea, String number) {
        return getCourseIdForTerm(subjectArea, number, AtpHelper.getLastScheduledAtpId());
    }


    /**
     * Used to get the course Id for the given subject area and course number (CHEM, 120) for a given term
     *
     * @param subjectArea
     * @param number
     * @return
     */
    @Override
    public String getCourseIdForTerm(String subjectArea, String number, String termId) {
        List<SearchRequest> requests = new ArrayList<SearchRequest>();
        SearchRequest request = new SearchRequest(CourseSearchConstants.COURSE_SEARCH_FOR_COURSE_ID);
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_SUBJECT_PARAM, subjectArea.trim());
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_NUMBER_PARAM, number.trim());
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_LAST_SCHEDULED_PARAM, termId);
        requests.add(request);
        SearchResult searchResult = new SearchResult();
        try {
            searchResult = getLuService().search(request);
        } catch (org.kuali.student.common.exceptions.MissingParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        String courseId = null;
        if (searchResult.getRows().size() > 0) {
            courseId = searchResult.getRows().get(0).getCells().get(0).getValue();
        }
        return courseId;
    }

    /**
     * returns the courseInfo for the given courseId by verifying the courId to be a verifiedcourseId
     *
     * @param courseId
     * @return
     */
    public CourseInfo getCourseInfo(String courseId) {

        CourseInfo courseInfo = null;
        try {
            courseInfo = getCourseService().getCourse(getVerifiedCourseId(courseId));
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }
        return courseInfo;
    }


    private static int getAsInteger(Element element, String name) {
        int result = 0;
        try {
            String enrollmentLimit = element.elementText(name);
            result = Integer.valueOf(enrollmentLimit);
        } catch (Exception e) {
        }

        return result;
    }


    private void doSectionStatus(LinkedHashMap<String, LinkedHashMap<String, Object>> parent, StudentServiceClient client, AtpHelper.YearTerm yt,
                                 String curric, String num, String sectionID)
            throws ServiceException, DocumentException {
        String year = yt.getYearAsString();
        String quarter = yt.getTermAsID();
        String xml = client.getSectionStatus(year, quarter, curric, num, sectionID);
        Document doc = newDocument(xml);

        DefaultXPath statusPath = newXPath("/s:SectionStatus");
        Element status = (Element) statusPath.selectSingleNode(doc);

        String sln = status.elementText("SLN");
        int enrollMaximum = getAsInteger(status, "LimitEstimateEnrollment");
        int enrollCount = getAsInteger(status, "CurrentEnrollment");
        String yea = status.elementText("Status");
        boolean enrollOpen = "open".equalsIgnoreCase(yea);
        String limitEstimateEnrollmentIndicator = status.elementText("LimitEstimateEnrollmentIndicator");
        boolean enrollEstimate = "estimate".equalsIgnoreCase(limitEstimateEnrollmentIndicator);

        LinkedHashMap<String, Object> childmap = new LinkedHashMap<String, Object>();
        childmap.put("enrollCount", enrollCount);
        childmap.put("enrollMaximum", enrollMaximum);
        childmap.put("enrollOpen", enrollOpen);
        childmap.put("enrollEstimate", enrollEstimate);
        String atpId = yt.toATP().replace('.', '-');
        String key = "enrl_" + atpId + "_" + sln;
        parent.put(key, childmap);
    }

    /**
     * @param delimiter
     * @param list
     * @return
     */
    public String joinStringsByDelimiter(char delimiter, String... list) {
        return StringUtils.join(list, delimiter);
    }

    /**
     * Takes a courseId that can be either a version independent Id or a version dependent Id and
     * returns a version dependent Id. In case of being passed in a version depend
     *
     * @param courseId
     * @return
     */
    @Override
    public String getVerifiedCourseId(String courseId) {
        return getCourseVersionIdByTerm(courseId, AtpHelper.getLastScheduledAtpId());
    }

    /**
     * returns a course version Id for given term and courseId
     *
     * @param courseId
     * @param termId
     * @return
     */
    @Override
    public String getCourseVersionIdByTerm(String courseId, String termId) {
        String courseVersionId = null;
        try {
            SearchRequest req = new SearchRequest("myplan.course.version.id");
            req.addParam("courseId", courseId);
            req.addParam("courseId", courseId);
            req.addParam("lastScheduledTerm", termId);
            SearchResult result = getLuService().search(req);
            for (SearchResultRow row : result.getRows()) {
                for (SearchResultCell cell : row.getCells()) {
                    if ("lu.resultColumn.cluId".equals(cell.getKey())) {
                        courseVersionId = cell.getValue();
                    }
                }
            }
        } catch (Exception e) {
            logger.error("version verified Id retrieval failed", e);
        }
        return courseVersionId;
    }


    /**
     * returns the course code from given activityId
     * <p/>
     * eg: for activityId '2013:2:CHEM:152:A' course code CHEM  152 is returned
     *
     * @param activityId
     * @return
     */
    public String getCourseCdFromActivityId(String activityId) {
        ActivityOfferingDisplayInfo activityDisplayInfo = null;
        try {
            activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not retrieve ActivityOffering data for" + activityId, e);
        }
        if (activityDisplayInfo != null) {
            /*TODO: move this to Coursehelper to make it institution neutral*/
            String courseOfferingId = null;
            for (AttributeInfo attributeInfo : activityDisplayInfo.getAttributes()) {
                if ("PrimaryActivityOfferingId".equalsIgnoreCase(attributeInfo.getKey())) {
                    courseOfferingId = attributeInfo.getValue();
                    break;
                }
            }
            CourseOfferingInfo courseOfferingInfo = null;
            try {
                courseOfferingInfo = getCourseOfferingService().getCourseOffering(courseOfferingId, CourseSearchConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Could not retrieve CourseOffering data for" + courseOfferingId, e);
            }

            if (courseOfferingInfo != null) {
                return courseOfferingInfo.getCourseCode();
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    /**
     * returns the Activity code from given activityId
     * <p/>
     * eg: for activityId '2013:2:CHEM:152:A' activity code A is returned
     *
     * @param activityId
     * @return
     */
    public String getCodeFromActivityId(String activityId) {
        ActivityOfferingDisplayInfo activityDisplayInfo = null;
        try {
            activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not retrieve ActivityOffering data for" + activityId, e);
        }
        if (activityDisplayInfo != null) {
            return activityDisplayInfo.getActivityOfferingCode();
        } else {
            return null;
        }
    }

    @Override
    public boolean isCourseInOfferingIds(String subjectArea, String courseNumber, Set<String> courseOfferingIds) {
        // UW Implementation checks by breaking down courseOfferingId. KSAP should use courseOffering service to accomplish the same
        for (String offeringId : courseOfferingIds) {
            if (offeringId.contains(":" + subjectArea + ":" + courseNumber)) {
                return true;
            }
        }
        return false;
    }

    /**
     * retuns a SLN for given params
     *
     * @param year
     * @param term
     * @param subject
     * @param number
     * @param activityCd
     * @return
     */
    public String getSLN(String year, String term, String subject, String number, String activityCd) {
        String sln = null;
        String activityId = joinStringsByDelimiter(':', year, term, subject, number, activityCd);
        ActivityOfferingDisplayInfo activityOfferingInfo = null;
        try {
            activityOfferingInfo = getCourseOfferingService().getActivityOfferingDisplay(activityId, CourseSearchConstants.CONTEXT_INFO);
            if (activityOfferingInfo != null) {
                for (AttributeInfo attributeInfo : activityOfferingInfo.getAttributes()) {
                    if (attributeInfo.getKey().equalsIgnoreCase("SLN")) {
                        sln = attributeInfo.getValue();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("could not load the ActivityOfferinInfo from SWS", e);
        }

        return sln;
    }

    /**
     * builds the refObjId for activity PlanItems (eg: '2013:2:CHEM:152:A')
     *
     * @param atpId
     * @param subject
     * @param number
     * @param activityCd
     * @return
     */
    public String buildActivityRefObjId(String atpId, String subject, String number, String activityCd) {
        AtpHelper.YearTerm yearTerm = AtpHelper.atpToYearTerm(atpId);
        return joinStringsByDelimiter(':', yearTerm.getYearAsString(), yearTerm.getTermAsString(), subject, number, activityCd);
    }
}
