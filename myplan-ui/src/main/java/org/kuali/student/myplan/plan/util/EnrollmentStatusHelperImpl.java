package org.kuali.student.myplan.plan.util;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;

import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

public class EnrollmentStatusHelperImpl implements EnrollmentStatusHelper {

    private StudentServiceClient studentServiceClient;

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
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


    public void populateEnrollmentFields(ActivityOfferingDisplayInfo activity, String year, String quarter, String curric, String num, String sectionID)
            throws Exception {
//        String xml = studentServiceClient.getSectionStatus(year, quarter, curric, num, sectionID);
//        Document doc = newDocument(xml);
//        DefaultXPath statusPath = newXPath("/s:SectionStatus");
//        Element status = (Element) statusPath.selectSingleNode(doc);
//
//        List<AttributeInfo> attributes = activity.getAttributes();
//
//        String enrollmentLimit = status.elementText("LimitEstimateEnrollment");
//        String currentEnrollment = status.elementText("CurrentEnrollment");
//        String limitEstimate = status.elementText("LimitEstimateEnrollmentIndicator");
//        limitEstimate = "estimate".equalsIgnoreCase(limitEstimate) ? "E" : "";
//
//        attributes.add(new AttributeInfo("enrollmentLimit", enrollmentLimit));
//        attributes.add(new AttributeInfo("currentEnrollment", currentEnrollment));
//        attributes.add(new AttributeInfo("limitEstimate", limitEstimate));

    }

}
