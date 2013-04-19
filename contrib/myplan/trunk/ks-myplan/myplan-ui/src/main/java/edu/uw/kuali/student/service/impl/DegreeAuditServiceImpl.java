package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.organization.dto.OrgInfo;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.audit.service.model.AuditDataSource;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.AtpHelper.YearTerm;
import org.kuali.student.myplan.plan.util.OrgHelper;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uachieve.apis.audit.JobQueueRun;
import uachieve.apis.audit.dao.JobQueueListDao;
import uachieve.apis.audit.dao.JobQueueRunDao;
import uachieve.apis.audit.dao.hibernate.JobQueueRunHibernateDao;
import uachieve.apis.audit.jobqueueloader.JobQueueRunLoader;
import uachieve.apis.requirement.dao.DprogDao;
import uachieve.apis.requirement.dao.hibernate.DprogHibernateDao;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;
import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DegreeAuditServiceImpl implements DegreeAuditService {

    private int TIMEOUT = 5 * 60 * 1000; // 5 minutes

    // default is to create real links, unit tests should change to LINK_TEMPLATE.TEST
    private CourseLinkBuilder.LINK_TEMPLATE courseLinkTemplateStyle = CourseLinkBuilder.LINK_TEMPLATE.COURSE_DETAILS;

    public static void main(String[] args)
            throws Exception {
        DegreeAuditServiceImpl impl = new DegreeAuditServiceImpl();
        String studentId = "D8D636BEB4CC482884420724BF152709";
        String programId = "1BISMCS0011";
//        AuditReportInfo info = impl.runAudit(studentId, programId, null, null);
//        AuditReportInfo info = impl.getAuditReport("2012100110472750", "kauli.audit.type.default", CourseSearchConstants.CONTEXT_INFO);
        AuditReportInfo info = impl.getHTMLReport("2013041811291367", null);
//        System.out.println(DegreeAuditServiceImpl.padfront("  1 2 "));
    }


    private final Logger logger = Logger.getLogger(DegreeAuditServiceImpl.class);

    public static final ContextInfo CONTEXT_INFO = new ContextInfo();


    private JobQueueRunLoader jobQueueRunLoader;

    private JobQueueRunDao jobQueueRunDao;

    private JobQueueListDao jobQueueListDao;

    private DprogDao dprogDao;

    private StudentServiceClient studentServiceClient;

    public transient AcademicPlanService academicPlanService;

    private CourseService courseService;

    @Autowired
    CourseHelper courseHelper;


    public DprogDao getDprogDao() {
        return dprogDao;
    }

    public void setDprogDao(DprogDao dprogDao) {
        this.dprogDao = dprogDao;
    }

    public JobQueueListDao getJobQueueListDao() {
        return jobQueueListDao;
    }

    public void setJobQueueListDao(JobQueueListDao jobQueueListDao) {
        this.jobQueueListDao = jobQueueListDao;
    }

    public JobQueueRunDao getJobQueueRunDao() {
        return jobQueueRunDao;
    }

    public void setJobQueueRunDao(JobQueueRunDao jobQueueRunDao) {
        this.jobQueueRunDao = jobQueueRunDao;
    }

    public JobQueueRunLoader getJobQueueRunLoader() {
        return jobQueueRunLoader;
    }

    public void setJobQueueRunLoader(JobQueueRunLoader loader) {
        this.jobQueueRunLoader = loader;
    }

    public DegreeAuditServiceImpl() {
    }

    static public class DegreeAuditRequest {
        String campus;
        String level;
        String type;
        String major;
        String pathway;
        String regid;

        ArrayList<DegreeAuditCourseRequest> courses = new ArrayList<DegreeAuditCourseRequest>();

        public DegreeAuditRequest(String regid, String programId) {
            this.regid = regid;

            programId = programId.replace('$', ' ');
            // padding, because sometimes degree program ids are not 12 chars long
            programId = programId + "              ";

            campus = "SEATTLE";
            char oof = programId.charAt(0);
            switch (oof) {
                case '1':
                    campus = "BOTHELL";
                    break;
                case '2':
                    campus = "TACOMA";
                    break;
                default:
                    break;
            }

            major = programId.substring(1, 7).trim();
            pathway = programId.substring(7, 9);
            level = programId.substring(9, 10);
            type = programId.substring(10, 11);

        }

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append("<DegreeAudit>\n");
            sb.append("<Campus>").append(campus.replace("&", "&amp;")).append("</Campus>\n");
            sb.append("<DegreeLevel>").append(level.replace("&", "&amp;")).append("</DegreeLevel>\n");
            sb.append("<DegreeType>").append(type.replace("&", "&amp;")).append("</DegreeType>\n");
            sb.append("<MajorAbbreviation>").append(major.replace("&", "&amp;")).append("</MajorAbbreviation>\n");
            sb.append("<Pathway>").append(pathway.replace("&", "&amp;")).append("</Pathway>\n");
            sb.append("<RegID>").append(regid.replace("&", "&amp;")).append("</RegID>\n");
            sb.append("<PlanningAudit>\n");
            for (DegreeAuditCourseRequest course : courses) {
                sb.append(course.toString());
            }
            sb.append("</PlanningAudit>\n");
            sb.append("</DegreeAudit>");
            return sb.toString();
        }
    }

    static public class DegreeAuditCourseRequest {
        String curric;
        String credit;
        String campus;
        String number;
        String quarter;
        String year;

        public String toString() {
            return
                    "<Course>\n" +
                            "<CurriculumAbbreviation>" + curric.replace("&", "&amp;") + "</CurriculumAbbreviation>\n" +
                            "<MinimumTermCredit>" + credit.replace("&", "&amp;") + "</MinimumTermCredit>\n" +
                            "<CourseCampus>" + campus.replace("&", "&amp;") + "</CourseCampus>\n" +
                            "<CourseNumber>" + number.replace("&", "&amp;") + "</CourseNumber>\n" +
                            "<Quarter>" + quarter.replace("&", "&amp;") + "</Quarter>\n" +
                            "<Year>" + year.replace("&", "&amp;") + "</Year>\n" +
                            "</Course>\n";
        }
    }

    @Override
    public AuditReportInfo runAudit(
            @WebParam(name = "studentId") String studentId,
            @WebParam(name = "programId") String programId,
            @WebParam(name = "auditTypeKey") String auditTypeKey,
            @WebParam(name = "context") ContextInfo useless
    )
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        String auditId = runAuditAsync(studentId, programId, auditTypeKey, useless);
        AuditReportInfo auditReportInfo = getAuditReport(auditId, auditTypeKey, useless);
        return auditReportInfo;

    }

    @Override
    public String runAuditAsync(
            @WebParam(name = "studentId") String studentId,
            @WebParam(name = "programId") String programId,
            @WebParam(name = "auditTypeKey") String auditTypeKey,
            @WebParam(name = "context") ContextInfo useless
    )
            throws InvalidParameterException, MissingParameterException, OperationFailedException {
        DegreeAuditRequest req = new DegreeAuditRequest(studentId, programId);
        return requestAudit(req);
    }


    @Override
    public AuditReportInfo runWhatIfAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "academicPlanId") String academicPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        String auditId = runWhatIfAuditAsync(studentId, programId, auditTypeKey, academicPlanId, context);
        AuditReportInfo auditReportInfo = getAuditReport(auditId, auditTypeKey, context);

        return auditReportInfo;
    }

    @Override
    public String runWhatIfAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "academicPlanId") String academicPlanId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {

        String auditId = null;
        DegreeAuditRequest req = new DegreeAuditRequest(studentId, programId);
        try {

            List<PlanItemInfo> planItems = getAcademicPlanService().getPlanItemsInPlan(academicPlanId, context);
            for (PlanItemInfo planItem : planItems) {
                //
                boolean isCourse = PlanConstants.COURSE_TYPE.equalsIgnoreCase(planItem.getRefObjectType());
                boolean isPlanned = AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equalsIgnoreCase(planItem.getTypeKey());
                if (isCourse && isPlanned) {
                    String versionIndependentId = planItem.getRefObjectId();

                    try {
                        String latestCourseId = getCourseHelper().getVerifiedCourseId(versionIndependentId);
                        CourseInfo courseInfo = getCourseService().getCourse(latestCourseId);

                        DegreeAuditCourseRequest course = new DegreeAuditCourseRequest();
                        course.curric = courseInfo.getSubjectArea().trim();
                        course.number = courseInfo.getCourseNumberSuffix().trim();
                        course.credit = "5";
                        {
                            course.campus = "Seattle";
                            List<OrgInfo> campusList = OrgHelper.getOrgInfo(CourseSearchConstants.CAMPUS_LOCATION_ORG_TYPE, CourseSearchConstants.ORG_QUERY_SEARCH_BY_TYPE_REQUEST, CourseSearchConstants.ORG_TYPE_PARAM);

                            Map<String, String> map = courseInfo.getAttributes();
                            if (map.containsKey(CourseSearchConstants.CAMPUS_LOCATION_COURSE_ATTRIBUTE)) {
                                String campusId = map.get(CourseSearchConstants.CAMPUS_LOCATION_COURSE_ATTRIBUTE);
                                for (OrgInfo campusOrg : campusList) {
                                    if (campusOrg.getId().equals(campusId)) {
                                        course.campus = campusOrg.getLongName();
                                    }
                                }
                            }

                        }

                        String atpId = planItem.getPlanPeriods().get(0);
                        AtpHelper.YearTerm yt = AtpHelper.atpToYearTerm(atpId);
                        course.quarter = yt.getTermAsID();
                        course.year = yt.getYearAsString();
                        req.courses.add(course);

                    } catch (Exception e) {
                        logger.warn("whatever", e);
                    }
                }

            }

            auditId = requestAudit(req);

        } catch (Exception e) {
            logger.warn("error retrieving plan items", e);
        }

        try {
            // getting the learning plan for given academicPlanId
            LearningPlanInfo learningPlanInfo = getAcademicPlanService().getLearningPlan(academicPlanId, context);
            YearTerm lastYT = AtpHelper.getLastPlannedTerm();
            int credits = 99;


            //updating the learning plan with new attribute values.
            for (AttributeInfo attributeInfo : learningPlanInfo.getAttributes()) {
                if ("forCourses".equalsIgnoreCase(attributeInfo.getKey())) {
                    attributeInfo.setValue(String.valueOf(req.courses.size()));
                } else if ("forCredits".equalsIgnoreCase(attributeInfo.getKey())) {
                    attributeInfo.setValue(String.valueOf(credits));
                } else if ("forQuarter".equalsIgnoreCase(attributeInfo.getKey())) {
                    attributeInfo.setValue(lastYT.toShortTermName());
                } else if ("auditId".equalsIgnoreCase(attributeInfo.getKey())) {
                    attributeInfo.setValue(auditId);
                }
            }
            learningPlanInfo.setStateKey(PlanConstants.LEARNING_PLAN_ACTIVE_STATE_KEY);
            getAcademicPlanService().updateLearningPlan(learningPlanInfo.getId(), learningPlanInfo, context);
        } catch (Exception e) {
            logger.error("Could not update the learningPlanInfo");
        }

        return auditId;
    }

    public String requestAudit(DegreeAuditRequest req) throws OperationFailedException {
        try {

            String payload = req.toString();

            String postAuditRequestURL = studentServiceClient.getBaseUrl() + "/v5/degreeaudit.xml";
            logger.debug("REST HTTP POST");
            logger.debug(postAuditRequestURL);
            logger.debug(payload);

            Client client = studentServiceClient.getClient();

            Request request = new Request(Method.POST, postAuditRequestURL);

            StringRepresentation entity = new StringRepresentation(payload);
            request.setEntity(entity);

            Response response = client.handle(request);
            Representation rep = response.getEntity();

            Status status = response.getStatus();
            if (Status.isSuccess(status.getCode())) {
                SAXReader reader = new SAXReader();
                org.dom4j.Document document = reader.read(rep.getStream());

                Map<String, String> namespaces = new HashMap<String, String>();
                namespaces.put("x", "http://webservices.washington.edu/student/");
                DefaultXPath jobid1Path = new DefaultXPath("//x:DegreeAudit/x:JobId");
                DefaultXPath jobid2Path = new DefaultXPath("//x:DegreeAudit/x:JobID");
                jobid1Path.setNamespaceURIs(namespaces);
                jobid2Path.setNamespaceURIs(namespaces);

                org.dom4j.Node jobid1Node = jobid1Path.selectSingleNode(document);
                org.dom4j.Node jobid2Node = jobid2Path.selectSingleNode(document);

                String jobid = "missing jobid";
                if (jobid1Node != null) {
                    jobid = jobid1Node.getText();
                } else if (jobid2Node != null) {
                    jobid = jobid2Node.getText();
                }
                return jobid;
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("HTTP Status: " + status.getCode());
                sb.append(" ");
                sb.append(rep.toString());
                sb.append(" ");
                InputStream in = rep.getStream();
                if (in != null) {
                    int c = 0;
                    while ((c = in.read()) != -1) {
                        sb.append((char) c);
                    }
                }
                throw new Exception(sb.toString());
            }
        } catch (Exception e) {
            logger.error(e);
            throw new OperationFailedException("cannot request audit", e);
        }
    }

    @Override
    public StatusInfo getAuditRunStatus(@WebParam(name = "auditId") String auditId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        String instcd = "";
        String instid = "4854";
        String instidq = "72";
        int status = getJobQueueListDao().checkJobQueueStatus(instidq, instid, instcd, auditId);
        StatusInfo info = new StatusInfo();
        info.setSuccess(status == 1);

        switch (status) {
            case -2:
                info.setMessage("audit request not found");
                break;
            case -1:
                info.setMessage("completed with errors");
                break;
            case 0:
                info.setMessage("not finished processing");
                break;
            case 1:
                info.setMessage("complete");
                break;
            default:
                break;
        }
        return info;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AuditReportInfo getAuditReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        long giveup = System.currentTimeMillis() + TIMEOUT;
        while (true) {
            StatusInfo info = this.getAuditRunStatus(auditId, context);
            logger.debug(info.getMessage());
            if (info.getIsSuccess()) {
                return getHTMLReport(auditId, auditTypeKey);
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                // do nothing
            }
            if (System.currentTimeMillis() > giveup) {
                String msg = "getAuditReport giving up after " + (TIMEOUT / 1000) + " seconds, auditId: " + auditId;
                logger.error(msg);
                throw new OperationFailedException(msg);
            }
        }
    }

    public void setCourseLinkTemplateStyle(CourseLinkBuilder.LINK_TEMPLATE style) {
        courseLinkTemplateStyle = style;
    }

    public AuditReportInfo getHTMLReport(String auditId, String auditTypeKey) throws OperationFailedException {
        AuditReportInfo auditReportInfo = new AuditReportInfo();
        auditReportInfo.setAuditId(auditId);
        String answer = "Audit ID " + auditId + " not available";
        try {
            JobQueueRunHibernateDao dao = (JobQueueRunHibernateDao) getJobQueueRunDao();
            String sql = "SELECT report.report, run.stuno FROM JobQueueRun run, JobQueueReport report  WHERE run.intSeqNo = report.jobqSeqNo AND run.jobid = ? AND run.reportType = 'HTM'";

            List list = dao.find(sql, new Object[]{auditId});
            Object[] item = (Object[]) list.get(0);
            byte[] report = (byte[]) item[0];
//            String stuno = (String) item[1];
//            stuno = convertMyPlanStunoToSDBSyskey(stuno);
            String garbage = new String(report);
            InputStream in = new ByteArrayInputStream(garbage.getBytes());

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            factory.setFeature("http://apache.org/xml/features/dom/include-ignorable-whitespace", false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(in);

            XPathFactory newInstanceXPathFactory = XPathFactory.newInstance();
            XPath xpath = newInstanceXPathFactory.newXPath();
            XPathExpression expr = xpath.compile("/html/div");
            Object untypedRootDegreeAuditHTMLContentDIVNodeList = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList rootDegreeAuditHTMLContentDIVNodeList = (NodeList) untypedRootDegreeAuditHTMLContentDIVNodeList;

            Node rootDegreeAuditHTMLContentDIV = rootDegreeAuditHTMLContentDIVNodeList.item(0);

            findClassLinkifyTextAndConvertCoursesToLinks(doc, builder);

            String path = "//*[contains(@class,'urlify')]";
            linkifyURLs(doc, xpath, path, builder);
            boolean isUserSession = UserSessionHelper.isUserSession();
            if (isUserSession) {
                path = "//span[contains(@class,'prepared-for-name')]/text()";
                XPathExpression godot = xpath.compile(path);
                Object godotSet = godot.evaluate(doc, XPathConstants.NODESET);
                NodeList godotList = (NodeList) godotSet;
                for (int nth = 0; nth < godotList.getLength(); nth++) {
                    Node child = godotList.item(nth);
                    String regId = UserSessionHelper.getStudentRegId();
                    String preparedFor = UserSessionHelper.getNameCapitalized(regId);
                    child.setTextContent(preparedFor);
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

            // Changed so that logged in users (via KRAD) receive HTML and webservices clients get XML
            //
            // Quickfix until KRAD/MyPlan has its MIME-Type changed from "text/html" to "application/xhtml+xml", which
            // forces all current web browsers to recognizes self-closing tags eg <br/> and <div/>. Otherwise they treat
            // content has HTML 4, failing or incorrectly rendering self-closing tags.
            //
            // Great explanation of this mess here: http://stackoverflow.com/a/206409
            String method = "html";
            if (DegreeAuditServiceConstants.AUDIT_TYPE_KEY_XML.equalsIgnoreCase(auditTypeKey)) {
//            	method = "xml";
            }
            transformer.setOutputProperty(OutputKeys.METHOD, method);
            DOMSource source = new DOMSource(rootDegreeAuditHTMLContentDIV);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            transformer.transform(source, result);
            answer = sw.toString();

        } catch (Exception e) {
            String msg = "getHTMLReport failed auditId: " + auditId;
            logger.error(msg, e);
            throw new OperationFailedException(msg, e);
        }
        AuditDataSource dataSource = new AuditDataSource(answer, auditId);
        DataHandler dh = new DataHandler(dataSource);
        auditReportInfo.setReport(dh);
        return auditReportInfo;
    }

    public void findClassLinkifyTextAndConvertCoursesToLinks(Document doc, DocumentBuilder builder) throws XPathExpressionException, IOException, SAXException {
        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        String path = "//*[contains(@class,'linkify')]/text()";
        XPathExpression xpathExpression = xpath.compile(path);
        Object untypedClassLinkifyNodeList = xpathExpression.evaluate(doc, XPathConstants.NODESET);
        NodeList classLinkifyNodeList = (NodeList) untypedClassLinkifyNodeList;
        for (int nth = 0; nth < classLinkifyNodeList.getLength(); nth++) {
            Node child = classLinkifyNodeList.item(nth);
            String textContent = child.getTextContent();
            textContent = textContent.replace('\n', ' ');
            textContent = textContent.replace('\t', ' ');
            textContent = textContent.replace("&", "&amp;");
            textContent = textContent.replace("<", "&lt;");
            textContent = textContent.replace(">", "&gt;");
            textContent = textContent.trim();
            String linkifiedTextContent = CourseLinkBuilder.makeLinks(textContent, courseLinkTemplateStyle);
            if (!textContent.equals(linkifiedTextContent)) {

                linkifiedTextContent = "<span>" + linkifiedTextContent + "</span>";

                builder.reset();
                try {
                    Document document = builder.parse(new InputSource(new StringReader(linkifiedTextContent)));
                    Node root = document.getDocumentElement();

                    Node parent = child.getParentNode();
                    Node newRoot = doc.importNode(root, true);
                    parent.replaceChild(newRoot, child);
                } catch (Exception e) {
                    logger.error("findClassLinkifyTextAndConvertCoursesToLinks failed on '" + linkifiedTextContent + "'", e);
                }
            }
        }
    }

    public void linkifyURLs(Document doc, XPath xpath, String path, DocumentBuilder builder) throws XPathExpressionException, IOException, SAXException {
        XPathExpression godot = xpath.compile(path);
        Object godotSet = godot.evaluate(doc, XPathConstants.NODESET);
        NodeList godotList = (NodeList) godotSet;
        for (int nth = 0; nth < godotList.getLength(); nth++) {
            Node child = godotList.item(nth);
            String victim = "";
            try {
                StringWriter stringWriter = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(child), new StreamResult(stringWriter));
                String scurge = stringWriter.toString(); //This is string data of xml file
//                System.out.println(scurge);
                victim = linkifyCourseSubjAndNum(scurge);
                if (!scurge.equals(victim)) {

//                    victim = "<span>" + victim + "</span>";
                    builder.reset();
                    Document whoopie = builder.parse(new InputSource(new StringReader(victim)));
                    Node fake = whoopie.getDocumentElement();

                    Node parent = child.getParentNode();
                    Node crank = doc.importNode(fake, true);
                    parent.replaceChild(crank, child);
                }
            } catch (Exception e) {
                logger.error("linkifyURLs failed on '" + victim + "'", e);
            }
        }
    }


    public static String linkifyCourseSubjAndNum(String initialText) {
        StringBuffer result = new StringBuffer(initialText.length());
        Pattern p = Pattern.compile("([a-zA-Z_0-9\\-]+@)?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\-=?\\+\\%/\\.\\w]+)?");

        Matcher m = p.matcher(initialText);
        while (m.find()) {
            String href = m.group();
            if (!href.contains("@") && href.contains("washington.edu")) {
                String period = "";
                if (href.endsWith(".")) {
                    href = href.substring(0, href.length() - 1);
                    period = ".";
                }
                String url = href;
                if (!url.startsWith("http://")) {
                    url = "http://" + url;
                }
                String trix = "<a href=\"" + url + "\" target=\"_blank\">" + href + "</a>" + period;
                m.appendReplacement(result, trix);
            }
        }
        m.appendTail(result);
        return result.toString();
    }

    public boolean equals(Object victim, Object... list) {
        if (victim == null) return false;
        if (list == null) return false;
        if (list.length == 0) return false;
        for (Object item : list) {
            if (item == null) continue;
            if (victim.equals(item)) return true;

        }
        return false;
    }

    // Convert systemKey to myplan stuno
    // eg "1000723033" becomes "000723033" (note the "1" prefix)
    public String convertSDBSyskeyToMyPlanStuno(String syskey) {
        if (syskey.length() < 9 || !syskey.startsWith("1")) {
            while (syskey.startsWith("0")) {
                syskey = syskey.substring(1);
            }
            String myplan = "100000000";
            syskey = myplan.substring(0, 9 - syskey.length()) + syskey;
        }

        return syskey;
    }

    // Convert  myplan stuno systemKey to
    // eg "000723033" becomes "1000723033" (note the "1" prefix)
    public String convertMyPlanStunoToSDBSyskey(String stuno) {
        if (stuno.length() == 9 && stuno.startsWith("1")) {
            stuno = stuno.substring(1);
        }
        return stuno;
    }

    @Override
    public List<AuditReportInfo> getAuditsForStudentInDateRange(@WebParam(name = "studentId") String studentId,
                                                                @WebParam(name = "startDate") Date startDate,
                                                                @WebParam(name = "endDate") Date endDate,
                                                                @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {


        List<AuditReportInfo> list = new ArrayList<AuditReportInfo>();
        try {
            // TODO: configurable constant for UW
            String instid = "4854";
            // TODO: configurable constant for UW
            String instidq = "72";
            // I have no idea what 'instcd' list does
            List<String> instcd = Arrays.asList("");

            String syskey = UserSessionHelper.getStudentSystemKey(studentId);
            String stuno = convertSDBSyskeyToMyPlanStuno(syskey);

            logger.info("getAuditsForStudentInDateRange syskey " + syskey + "  stuno " + stuno);

            JobQueueRunDao runrun = getJobQueueRunDao();
            List<JobQueueRun> load = runrun.load(instid, instidq, instcd, stuno);

            for (JobQueueRun jqr : load) {
                AuditReportInfo audit = new AuditReportInfo();
                audit.setAuditId(jqr.getJobid());
                audit.setReportType(DegreeAuditServiceConstants.AUDIT_TYPE_KEY_SUMMARY);
                audit.setStudentId(stuno);
                audit.setProgramId(jqr.getDprog().replace(" ", "$"));
                audit.setProgramTitle(jqr.getWebtitle());
                audit.setRunDate(jqr.getRundate());
                audit.setRequirementsSatisfied("Unknown");
                audit.setWhatIfAudit("W".equals(jqr.getIp()));

                list.add(audit);
            }
        } catch (DataRetrievalFailureException e) {
            // Stupid exception for when no results found. ignore it.
        }

        return list;
    }


    @Override
    public AuditReportInfo runEmptyAudit(@WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String runEmptyAuditAsync(@WebParam(name = "programId") String programId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<AuditProgramInfo> getAuditPrograms(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        DprogHibernateDao dao = (DprogHibernateDao) getDprogDao();
        List<AuditProgramInfo> auditProgramInfoList = new ArrayList<AuditProgramInfo>();
        String hql = "SELECT DISTINCT dp.comp_id.dprog, dp.webtitle from Dprog dp WHERE (dp.comp_id.dprog LIKE '0%' or dp.comp_id.dprog LIKE '1%' or dp.comp_id.dprog LIKE '2%') AND dp.lyt>=? AND dp.webtitle IS NOT NULL AND dp.webtitle<>'' AND dp.dpstatus='W'";
        String[] currentTermAndYear = this.getCurrentYearAndTerm();
        int year = Integer.parseInt(currentTermAndYear[1]) - 10;
        StringBuffer termYear = new StringBuffer();
        termYear = termYear.append(year).append(currentTermAndYear[0]);
//        YearTerm yt = AtpHelper.getCurrentYearTerm();
//        yt = new AtpHelper.YearTerm( yt.getYear() - 10, yt.getTerm() );
//        String whoof = yt.toUAchieveValue();
        String[] params = new String[]{termYear.toString()};
        List programs = dao.find(hql, params);
        for (int i = 0; i < programs.size(); i++) {
            AuditProgramInfo auditProgramInfo = new AuditProgramInfo();
            Object[] objs = (Object[]) programs.get(i);
            String programId = (String) objs[0];
            programId = programId.replace(' ', '$');
            auditProgramInfo.setProgramId(programId);
            auditProgramInfo.setProgramTitle((String) objs[1]);
            auditProgramInfoList.add(auditProgramInfo);
        }
        return auditProgramInfoList;
    }

    public String getAuditStatus(String studentId, String programId, String recentAuditId) {
        String syskey = UserSessionHelper.getStudentSystemKey();
        String stuno = convertSDBSyskeyToMyPlanStuno(syskey);

        String hql = null;
        Object[] params = null;
        if (StringUtils.hasText(recentAuditId)) {
            hql = "SELECT detail.status FROM JobQueueRun run, JobQueueDetail detail WHERE detail.jobQueueList.comp_id.jobid=run.jobid and run.rundate > (SELECT DISTINCT jqrun.rundate FROM JobQueueRun jqrun where jqrun.jobid= ?)  and detail.fdprog= ? and detail.stuno= ? order by run.rundate desc";
            params = new Object[]{recentAuditId, programId, stuno};
        } else {
            hql = "SELECT detail.status FROM JobQueueRun run, JobQueueDetail detail WHERE detail.jobQueueList.comp_id.jobid=run.jobid and detail.fdprog= ? and detail.stuno= ? order by run.rundate desc";
            params = new Object[]{programId, stuno};
        }

        DprogHibernateDao dao = (DprogHibernateDao) getDprogDao();
        List list = dao.find(hql, params);

        String auditStatus = "PENDING";
        if (list != null && list.size() > 0) {
            String first = list.get(0).toString();
            if ("D".equalsIgnoreCase(first)) {
                auditStatus = "DONE";
            } else if ("E".equalsIgnoreCase(first)) {
                auditStatus = "FAILED";
            }
            // Ignores audit status "X"
        }
        return auditStatus;
    }

    /*Implemented to get the current year and the term value from the academic calender service.*/
    private String[] getCurrentYearAndTerm() {
        String currentAtp = AtpHelper.getCurrentAtpId();
        String[] termYear = AtpHelper.atpIdToTermAndYear(currentAtp);
        return new String[]{termYear[0].trim(), termYear[1].trim()};
    }

    public StudentServiceClient getStudentServiceClient() {
        return studentServiceClient;
    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    public synchronized AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    // TODO: Springify this
    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }


}