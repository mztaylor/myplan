package edu.uw.kuali.student.myplan.util;

import edu.uw.kuali.student.lib.client.studentservice.ServiceException;
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
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.plan.util.AtpHelper;

import javax.xml.namespace.QName;
import java.io.StringReader;
import java.util.*;

public class CourseHelperImpl implements CourseHelper {


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
        } else if (courseCode.matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        }
        return new DeconstructedCourseCode(subject, number, section);
    }

    /**
     * Used to get the course Id for the given subject area and course number (CHEM, 120)
     *
     * @param subjectArea
     * @param number
     * @return
     */
    @Override
    public String getCourseId(String subjectArea, String number) {
        List<SearchRequest> requests = new ArrayList<SearchRequest>();
        SearchRequest request = new SearchRequest(CourseSearchConstants.COURSE_SEARCH_FOR_COURSE_ID);
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_SUBJECT_PARAM, subjectArea.trim());
        request.addParam(CourseSearchConstants.SEARCH_REQUEST_NUMBER_PARAM, number.trim());
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
}
