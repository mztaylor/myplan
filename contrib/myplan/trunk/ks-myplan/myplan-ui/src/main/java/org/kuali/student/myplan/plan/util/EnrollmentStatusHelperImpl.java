package org.kuali.student.myplan.plan.util;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.util.CourseSearchConstants;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EnrollmentStatusHelperImpl implements EnrollmentStatusHelper {

    private StudentServiceClient studentServiceClient;

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

    /**
     * CourseCode class holds the course division, number and section.
     */
    public static class CourseCode {
        private final String subject;
        private final String number;
        private final String section;

        public CourseCode(String subject, String number, String section) {
            this.subject = subject;
            this.number = number;
            this.section = section;

        }

        public String getSubject() {
            return subject;
        }

        public String getNumber() {
            return number;
        }

        public String getSection() {
            return section;
        }

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


    public void populateEnrollmentFields(ActivityOfferingItem activity, String year, String quarter, String curric, String num, String sectionID)
            throws Exception {
        StudentServiceClient client = getStudentServiceClient();
        String xml = client.getSectionStatus(year, quarter, curric, num, sectionID);
        Document doc = newDocument(xml);
        DefaultXPath statusPath = newXPath("/s:SectionStatus");
        Element status = (Element) statusPath.selectSingleNode(doc);

        String enrollmentLimit = status.elementText("LimitEstimateEnrollment");
        String currentEnrollment = status.elementText("CurrentEnrollment");
        String limitEstimate = status.elementText("LimitEstimateEnrollmentIndicator");
        limitEstimate = "estimate".equalsIgnoreCase(limitEstimate) ? "E" : "";

        activity.setEnrollCount(currentEnrollment);
        activity.setEnrollMaximum(enrollmentLimit);
        activity.setEnrollEstimate(limitEstimate);
    }

    /**
     * Used to Split the course code into division and Code.
     * eg: "COM 243" is returned as CourseCode with division=COM and number=243 and section=null.
     * eg: "COM 243 A" is returned as CourseCode with division=COM , number=243 and section=A.
     *
     * @param courseCode
     * @return
     */
    public static CourseCode getCourseDivisionAndNumber(String courseCode) {
        String subject = null;
        String number = null;
        String section = null;
        if (courseCode.matches(CourseSearchConstants.FORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.COURSE_CODE_WITH_SECTION_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
            section = splitStr[2].trim();
        } else if(courseCode.matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        }
        return new CourseCode(subject, number, section);
    }

    /**
     * Used to get the course Id for the given subject area and course number (CHEM, 120)
     *
     * @param subjectArea
     * @param number
     * @return
     */
    public static String getCourseId(String subjectArea, String number) {
        List<SearchRequest> requests = new ArrayList<SearchRequest>();
        SearchRequest request = new SearchRequest(CourseSearchConstants.COURSE_SEARCH_FOR_COURSE_ID);
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_SUBJECT_PARAM, subjectArea);
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_NUMBER_PARAM, number);
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_LAST_SCHEDULED_PARAM, AtpHelper.getLastScheduledAtpId());
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


}
