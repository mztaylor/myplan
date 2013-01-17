package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClientImpl;
import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.audit.service.model.AuditDataSource;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.myplan.util.DegreeAuditAtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.util.Series;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import uachieve.apis.audit.*;
import uachieve.apis.audit.dao.JobQueueListDao;
import uachieve.apis.audit.dao.JobQueueRunDao;
import uachieve.apis.audit.dao.hibernate.JobQueueRunHibernateDao;
import uachieve.apis.audit.jobqueueloader.JobQueueRunLoader;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import uachieve.apis.requirement.dao.hibernate.DprogHibernateDao;
import uachieve.apis.requirement.dao.DprogDao;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.*;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DegreeAuditServiceImpl implements DegreeAuditService {

//    public static void main(String[] args)
//            throws Exception {
//        DegreeAuditServiceImpl impl = new DegreeAuditServiceImpl();
//        String studentId = "D8D636BEB4CC482884420724BF152709";
//        String programId = "1BISMCS0011";
//        AuditReportInfo info = impl.runAudit(studentId, programId, null, null);
//        AuditReportInfo info = impl.getAuditReport("2012100110472750", "kauli.audit.type.default", CourseSearchConstants.CONTEXT_INFO);
//        AuditReportInfo info = impl.getHTMLReport("2012100110472750");
//        System.out.println(DegreeAuditServiceImpl.padfront("  1 2 "));
//    }


    private final Logger logger = Logger.getLogger(DegreeAuditServiceImpl.class);

    public static final ContextInfo CONTEXT_INFO = new ContextInfo();


    private JobQueueRunLoader jobQueueRunLoader;

    private JobQueueRunDao jobQueueRunDao;

    private JobQueueListDao jobQueueListDao;

    private DprogDao dprogDao;

    private StudentServiceClient studentServiceClient;


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

    public final static String requestTemplate =
            "<DegreeAudit>" +
                    "<Campus>$campus</Campus>" +
                    "<PlanningAudit/>" +
                    "<DegreeLevel>$level</DegreeLevel>" +
                    "<DegreeType>$type</DegreeType>" +
                    "<MajorAbbreviation>$major</MajorAbbreviation>" +
                    "<Pathway>$pathway</Pathway>" +
                    "<RegID>$regid</RegID>" +
                    "</DegreeAudit>";

    @Override
    public AuditReportInfo runAudit(
            @WebParam(name = "studentId") String studentId,
            @WebParam(name = "programId") String programId,
            @WebParam(name = "auditTypeKey") String auditTypeKey,
            @WebParam(name = "context") ContextInfo useless
    )
            throws InvalidParameterException, MissingParameterException, OperationFailedException

    {
        try {
            programId = programId.replace('$', ' ');
            // padding, because sometimes degree program ids are not 12 chars long
            programId = programId + "              ";

            String campus = "SEATTLE";
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

            String major = programId.substring(1, 7).trim();
            String pathway = programId.substring(7, 9);
            String level = programId.substring(9, 10);
            String type = programId.substring(10, 11);

            String payload = new String(requestTemplate);
            payload = payload.replace("$campus", campus.replace("&", "&amp;"));
            payload = payload.replace("$level", level.replace("&", "&amp;"));
            payload = payload.replace("$type", type.replace("&", "&amp;"));
            payload = payload.replace("$major", major.replace("&", "&amp;"));
            payload = payload.replace("$pathway", pathway.replace("&", "&amp;"));
            payload = payload.replace("$regid", studentId.replace("&", "&amp;"));

            String postAuditRequestURL = studentServiceClient.getBaseUrl() + "/v5/degreeaudit.xml";

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
//                String xml = document.asXML();


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
                AuditReportInfo auditReportInfo = new AuditReportInfo();

                auditReportInfo.setAuditId(jobid);
                return auditReportInfo;
            } else {
                StringBuilder sb = new StringBuilder();
                InputStream in = rep.getStream();
                int c = 0;
                while ((c = in.read()) != -1) {
                    sb.append((char) c);
                }
                throw new Exception(sb.toString());
            }
        } catch (Exception e) {
            logger.error(e);
            throw new OperationFailedException("cannot request audit", e);
        }
    }

    @Override
    public String runAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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

    public int timeout = 30 * 1000; // 30 seconds
//    public int timeout = 1000; // 30 seconds

    @Override
    public AuditReportInfo getAuditReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        long giveup = System.currentTimeMillis() + timeout;
        while( true ) 
        {
            StatusInfo info = this.getAuditRunStatus(auditId, context);
            logger.info(info.getMessage());
            if (info.getIsSuccess())
            {
            	return getHTMLReport(auditId);
            }

            try
            {
                Thread.sleep(200);
            }
            catch( InterruptedException e )
            {
                // do nothing
            }
            if (System.currentTimeMillis() > giveup) {
                String msg = "getAuditReport giving up after " + (timeout / 1000) + " seconds, auditId: " + auditId;
                logger.error( msg );
				throw new OperationFailedException(msg);
            }
        }
//        if (AUDIT_TYPE_KEY_DEFAULT.equals(auditTypeKey))
//        {
        /*return getDARSReport(auditId);*/
//        }
        //if (AUDIT_TYPE_KEY_HTML.equals(auditTypeKey)) {
        //return getHTMLReport(auditId);
        //}
//        throw new InvalidParameterException("auditTypeKey: " + auditTypeKey);
    }

    // default is to create real links, unit tests should change to LINK_TEMPLATE.TEST
    private CourseLinkBuilder.LINK_TEMPLATE courseLinkTemplateStyle = CourseLinkBuilder.LINK_TEMPLATE.COURSE_DETAILS;
//    private CourseLinkBuilder.LINK_TEMPLATE courseLinkTemplateStyle = CourseLinkBuilder.LINK_TEMPLATE.TEST;

    public void setCourseLinkTemplateStyle(CourseLinkBuilder.LINK_TEMPLATE style) {
        courseLinkTemplateStyle = style;
    }


//    String padfront(String source) {
//        StringBuilder sb = new StringBuilder();
//        boolean front = true;
//        for (int nth = 0; nth < source.length(); nth++) {
//            char c = source.charAt(nth);
//            if (front && c == ' ') {
//                sb.append("&nbsp;");
//            } else {
//                front = false;
//                sb.append(c);
//            }
//        }
//        return sb.toString();
//    }

    public AuditReportInfo getHTMLReport(String auditId) throws OperationFailedException {
        AuditReportInfo auditReportInfo = new AuditReportInfo();
        auditReportInfo.setAuditId(auditId);
        String answer = "Audit ID " + auditId + " not available";
        try {
            JobQueueRunHibernateDao dao = (JobQueueRunHibernateDao) getJobQueueRunDao();
            String sql = "SELECT report.report FROM JobQueueRun run, JobQueueReport report  WHERE run.intSeqNo = report.jobqSeqNo AND run.jobid = ? AND run.reportType = 'HTM'";

            List list = dao.find(sql, new Object[]{auditId});
            byte[] report = (byte[]) list.get(0);
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

            String path = "//*[contains(@class,'urlify')]";;
            ineptURLLinkifier( doc, xpath, path, builder );

//            path = "//div[contains(@class,'requirement')]/div[contains(@class,'header')]/div[contains(@class,'title')]/text()";
//            ineptURLLinkifier(doc, xpath, path, builder);
//
//            path = "//div[contains(@class,'bigsection')]/div[contains(@class,'heading')]/text()";
//            ineptURLLinkifier(doc, xpath, path, builder);

            {
                String preparedFor = UserSessionHelper.getNameCapitalized(UserSessionHelper.getStudentId());
                path = "//span[contains(@class,'prepared-for-name')]/text()";
                XPathExpression godot = xpath.compile(path);
                Object godotSet = godot.evaluate(doc, XPathConstants.NODESET);
                NodeList godotList = (NodeList) godotSet;
                for (int nth = 0; nth < godotList.getLength(); nth++) {
                    Node child = godotList.item(nth);
                    String scurge = child.getTextContent();
                    child.setTextContent( preparedFor );
                }
            }

            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            DOMSource source = new DOMSource(rootDegreeAuditHTMLContentDIV);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            transformer.transform(source, result);
            answer = sw.toString();

        } catch (Exception e) {
            String msg = "getHTMLReport failed auditId: " + auditId;
			logger.error(msg, e);
            throw new OperationFailedException( msg, e );
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
                try
                {
                    Document document = builder.parse(new InputSource(new StringReader(linkifiedTextContent)));
                    Node root = document.getDocumentElement();

                    Node parent = child.getParentNode();
                    Node newRoot = doc.importNode(root, true);
                    parent.replaceChild(newRoot, child);
                }
                catch( Exception e )
                {
                    logger.error("findClassLinkifyTextAndConvertCoursesToLinks failed on '" + linkifiedTextContent + "'", e);
                }
            }
        }
    }

    public void ineptURLLinkifier(Document doc, XPath xpath, String path, DocumentBuilder builder) throws XPathExpressionException, IOException, SAXException {
        XPathExpression godot = xpath.compile(path);
        Object godotSet = godot.evaluate(doc, XPathConstants.NODESET);
        NodeList godotList = (NodeList) godotSet;
        for (int nth = 0; nth < godotList.getLength(); nth++) {
            Node child = godotList.item(nth);
//            String scurge = child.getTextContent();
//            scurge = scurge.replace('\n', ' ');
//            scurge = scurge.replace('\t', ' ');
//            scurge = scurge.replace("&", "&amp;");
//            scurge = scurge.replace("<", "&lt;");
//            scurge = scurge.replace(">", "&gt;");
//            scurge = scurge.trim();

            String victim = "";
            try {
                StringWriter stringWriter = new StringWriter();
                Transformer transformer = TransformerFactory.newInstance().newTransformer();
                transformer.transform(new DOMSource(child), new StreamResult(stringWriter));
                String scurge = stringWriter.toString(); //This is string data of xml file
                System.out.println(scurge);
                victim = tangerine(scurge);
                if (!scurge.equals(victim)) {

//                    victim = "<span>" + victim + "</span>";
                    builder.reset();
                    Document whoopie = builder.parse(new InputSource(new StringReader(victim)));
                    Node fake = whoopie.getDocumentElement();

                    Node parent = child.getParentNode();
                    Node crank = doc.importNode(fake, true);
                    parent.replaceChild(crank, child);
                }
            }
            catch (Exception e) {
                logger.error("ineptURLLinkifier failed on '" + victim + "'", e);
            }
        }
    }


    public static String tangerine(String initialText) {
        StringBuffer result = new StringBuffer(initialText.length());
        Pattern p = Pattern.compile("([a-zA-Z_0-9\\-]+@)?(http://)?[a-zA-Z_0-9\\-]+(\\.\\w[a-zA-Z_0-9\\-]+)+(/[#&\\-=?\\+\\%/\\.\\w]+)?");

        Matcher m = p.matcher(initialText);
        while (m.find()) {
            String href = m.group();
            if (!href.contains("@") && href.contains( "washington.edu" ))  {
                String period = "";
                if (href.endsWith(".")) {
                    href = href.substring(0, href.length() - 1);
                    period = ".";
                }
                String url = href;
                if( !url.startsWith("http://") ) {
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

    public String convertSDBSyskeyToMyPlanStuno(String syskey) {
        // Convert systemKey to myplan stuno
        if (syskey.length() < 9 || !syskey.startsWith("1")) {
            while (syskey.startsWith("0")) {
                syskey = syskey.substring(1);
            }
            String myplan = "100000000";
            syskey = myplan.substring(0, 9 - syskey.length()) + syskey;
        }

        return syskey;
    }

    @Override
    public List<AuditReportInfo> getAuditsForStudentInDateRange(@WebParam(name = "studentId") String studentId,
                                                                @WebParam(name = "startDate") Date startDate,
                                                                @WebParam(name = "endDate") Date endDate,
                                                                @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {

        List<AuditReportInfo> list = new ArrayList<AuditReportInfo>();

        String stuno = convertSDBSyskeyToMyPlanStuno(studentId);
        logger.info("getAuditsForStudentInDateRange studentid " + studentId + "  stuno " + stuno);


        // TODO: configurable constant for UW
        String instid = "4854";
        // TODO: configurable constant for UW
        String instidq = "72";
        // I have no idea what 'instcd' list does
        List<String> instcd = new ArrayList<String>();
        instcd.add("");
        try {
            JobQueueRunDao runrun = getJobQueueRunDao();
            List<JobQueueRun> load = runrun.load(instid, instidq, instcd, stuno);

            for (JobQueueRun jqr : load) {
                AuditReportInfo audit = new AuditReportInfo();
                audit.setAuditId(jqr.getJobid());
                audit.setReportType(DegreeAuditServiceConstants.AUDIT_TYPE_KEY_SUMMARY);
                audit.setStudentId(stuno);
                audit.setProgramId(jqr.getDprog().replace(" ","$"));
                audit.setProgramTitle(jqr.getWebtitle());
                audit.setRunDate(jqr.getRundate());
                audit.setRequirementsSatisfied("Unknown");
                list.add(audit);
            }
        } catch (DataRetrievalFailureException e) {
            // Stupid exception for when no results found. ignore it.
        }

        return list;
    }

    @Override
    public AuditReportInfo runWhatIfAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "academicPlan") LearningPlanInfo academicPlan, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String runWhatIfAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "academicPlan") LearningPlanInfo academicPlan, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
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
        String auditStatus = "PENDING";
        String stuno = convertSDBSyskeyToMyPlanStuno(studentId);
        DprogHibernateDao dao = (DprogHibernateDao) getDprogDao();
        List list = null;
        String hql = "SELECT detail.status FROM JobQueueRun run, JobQueueDetail detail WHERE detail.jobQueueList.comp_id.jobid=run.jobid and run.rundate > (SELECT DISTINCT jqrun.rundate FROM JobQueueRun jqrun where jqrun.jobid= ?)  and detail.fdprog= ? and detail.stuno= ? order by run.rundate desc";
        if (!StringUtils.hasText(recentAuditId)) {
            hql = "SELECT detail.status FROM JobQueueRun run, JobQueueDetail detail WHERE detail.jobQueueList.comp_id.jobid=run.jobid and detail.fdprog= ? and detail.stuno= ? order by run.rundate desc";
            list = dao.find(hql, new Object[]{programId, stuno});
        } else {
            list = dao.find(hql, new Object[]{recentAuditId, programId, stuno});
        }


        if (list != null && list.size() > 0) {
            if (list.get(0).toString().length() > 0 && !list.get(0).toString().equalsIgnoreCase("X")) {
                auditStatus = list.get(0).toString();
            }
        }
        if (auditStatus.equalsIgnoreCase("D")) {
            auditStatus = "DONE";
        } else if (auditStatus.equalsIgnoreCase("E")) {
            auditStatus = "FAILED";
        }
        return auditStatus;
    }

    /*Implemented to get the current year and the term value from the academic calender service.*/
    public String[] getCurrentYearAndTerm() {
        String currentAtp = DegreeAuditAtpHelper.getCurrentAtpId();
        String[] termYear = DegreeAuditAtpHelper.atpIdToTermAndYear(currentAtp);
        return new String[]{termYear[0].trim(), termYear[1].trim()};
    }

    public StudentServiceClient getStudentServiceClient() {
        return studentServiceClient;
    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }
}