package org.kuali.student.myplan.audit.service;

import static org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.dom4j.io.SAXReader;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.acal.constants.AcademicCalendarServiceConstants;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.darsws.AuditRequestSvc;
import org.kuali.student.myplan.audit.service.darsws.AuditRequestSvcSoap;
import org.kuali.student.myplan.audit.service.darsws.MPAuditResponse;
import org.kuali.student.myplan.audit.service.model.*;
import org.kuali.student.myplan.audit.service.model.Requirement;
import org.kuali.student.myplan.audit.service.model.Subrequirement;
import org.kuali.student.myplan.audit.service.model.AuditDataSource;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.myplan.util.DegreeAuditAtpHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.util.constants.CourseOfferingServiceConstants;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import uachieve.apis.audit.*;
import uachieve.apis.audit.dao.JobQueueListDao;
import uachieve.apis.audit.dao.JobQueueRunDao;
import uachieve.apis.audit.dao.hibernate.JobQueueRunHibernateDao;
import uachieve.apis.audit.jobqueueloader.JobQueueRunLoader;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.xml.bind.Element;
import javax.xml.namespace.QName;
import java.io.*;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

import org.kuali.student.common.search.dto.*;
import uachieve.apis.requirement.ReqMain;
import uachieve.apis.requirement.dao.hibernate.DprogHibernateDao;
import uachieve.apis.requirement.Dprog;
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
    private final Logger logger = Logger.getLogger(DegreeAuditServiceImpl.class);
    private transient LuService luService;
    private transient CourseOfferingService courseOfferingService;
    private JobQueueRunLoader jobQueueRunLoader;

    private JobQueueRunDao jobQueueRunDao;

    private JobQueueListDao jobQueueListDao;


    public static final ContextInfo CONTEXT_INFO = new ContextInfo();

    private DprogDao dprogDao;

    private static transient AcademicCalendarService academicCalendarService;


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

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            this.courseOfferingService = (CourseOfferingService)
                    GlobalResourceLoader.getService(new QName(CourseOfferingServiceConstants.NAMESPACE, CourseOfferingServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return this.courseOfferingService;
    }

    protected LuService getLuService() {
        if (this.luService == null) {
            this.luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    public JobQueueRunLoader getJobQueueRunLoader() {
        return jobQueueRunLoader;
    }

    public void setJobQueueRunLoader(JobQueueRunLoader loader) {
        this.jobQueueRunLoader = loader;
    }


    private static AcademicCalendarService getAcademicCalendarService() {
        if (academicCalendarService == null) {
            academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return academicCalendarService;
    }


    public DegreeAuditServiceImpl() {
        loadProperties();
    }

    enum Structure { Section, Requirement, Advisory }
    private HashMap<String, Structure> structureMap = new HashMap<String, Structure>();

    public void loadProperties()
    {
        structureMap.put("A&SGENTXT", Structure.Section);
        structureMap.put("AOKTEXT", Structure.Section);
        structureMap.put("AOKTXTRCS", Structure.Section);
        structureMap.put("ART-ADMIT", Structure.Section);
        structureMap.put("ARTADVISE", Structure.Advisory);
        structureMap.put("ASTERISKS", Structure.Section);
        structureMap.put("BUSADMIN", Structure.Section);
        structureMap.put("CHEMADV1", Structure.Advisory);
        structureMap.put("CHMMAJOR1", Structure.Section);
        structureMap.put("CIVMAJBNR", Structure.Section);
        structureMap.put("COMADVIS", Structure.Advisory);
        structureMap.put("ENGRGETXT", Structure.Section);
        structureMap.put("GENTEXTBA", Structure.Section);

        structureMap.put("GRADRQBNR", Structure.Section);
        structureMap.put("HUNGRY-EL", Structure.Section);
//        structureMap.put("", Structure.Section);
//        structureMap.put("", Structure.Section);
//        structureMap.put("", Structure.Section);

        structureMap.put("LINE1", Structure.Section);
        structureMap.put("MAJORTEXT", Structure.Section);
        structureMap.put("MATHADVIS", Structure.Advisory);
        structureMap.put("MIN2.0BNR", Structure.Section);
        structureMap.put("SISAPRERQ", Structure.Section);
        structureMap.put("UTEXT", Structure.Section);
        structureMap.put("WHAT-IF", Structure.Advisory);

        textMap.put("A&SGENTXT", "<div>COLLEGE OF ARTS AND SCIENCES</div><div>GENERAL EDUCATION REQUIREMENTS</div>" );
    }

    public Structure getStructure(JobQueueReq jqr )
    {
        Structure result = Structure.Requirement;
        String rname = jqr.getRname().trim();
        if( structureMap.containsKey( rname ))
        {
            result = structureMap.get( rname );
        }
        else
        {

            String text = getText(jqr, false).trim();
            if( text.startsWith( "*** " ) && text.endsWith( " ***" ))
            {
                result = Structure.Section;
                System.out.printf("\n\nrname %s (%s) not defined, assuming Structure.Section", rname, text);
            }
            else if (text.startsWith("** ") && text.endsWith(" **")) {
                result = Structure.Section;
                System.out.printf("\n\nrname %s (%s) not defined, assuming Structure.Section", rname, text);
            }
            else if( text.startsWith( "NOTE:" ))
            {
                result = Structure.Advisory;
                System.out.printf("\n\nrname %s (%s) not defined, assuming Structure.Advisory", rname, text);

            }
            else
            {
                System.out.printf("\n\nrname %s (%s) not defined, assuming Structure.Requirement", rname, text);

            }

        }
        return result;
    }

    private HashMap<String,String> textMap = new HashMap<String, String>();

    public String getText( JobQueueReq jqr, boolean scrub )
    {
        String result = "";
        String rname = jqr.getRname().trim();
        if(textMap.containsKey( rname))
        {
            result = textMap.get( rname );
        }
        else
        {
            StringBuilder buf = new StringBuilder();
            for (JobQueueReqText text : jqr.getJobQueueReqTexts()) {
                String temp = text.getText();
                buf.append(temp);
                buf.append(" ");

            }
            result = buf.toString();
            if( scrub ) {
                result = scrub(result);
            }
        }
        return result;
    }

    String join(Object... args) {
        StringBuilder sb = new StringBuilder();
        for (Object item : args) {
            sb.append(item.toString());
            sb.append("\t");
        }
        return sb.toString();
    }

    public final static String WSDL_LOCATION = "http://isntsis.cac.washington.edu/sisMyPlanws/MPAuditRequestSvc.asmx?wsdl";
    private static final QName AUDIT_SERVICE_NAME = AuditRequestSvc.SERVICE;

    @Override
    public AuditReportInfo runAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        try {
            studentId = hardcodedStudentID(studentId);
            if( studentId.startsWith( "1") && studentId.length() == 9 )
            {
                studentId = studentId.substring( 1 );
            }
            int systemKey = Integer.parseInt(studentId);

            URL wsdlURL = new URL(WSDL_LOCATION);

            AuditRequestSvc ss = new AuditRequestSvc(wsdlURL, AUDIT_SERVICE_NAME);
            AuditRequestSvcSoap port = ss.getAuditRequestSvcSoap();


            logger.info("Invoking mpRequestAudit...");

            int lineNo = 0;
            String origin = "M";
            MPAuditResponse response = port.mpRequestAudit(0, programId, lineNo, systemKey, origin);
            logger.info("error code: " + response.getErrorCode());
            logger.info("error msg: " + response.getErrorMsg());
            logger.info("audit id: " + response.getAuditText());

            AuditReportInfo auditReportInfo = new AuditReportInfo();
            String auditID = response.getAuditText().trim();
            if (auditID.length() == 0) {
                auditID = null;
            }
            auditReportInfo.setAuditId(auditID);
            return auditReportInfo;

        } catch (Exception e) {
            throw new OperationFailedException("velocity error", e);
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
        int status = jobQueueListDao.checkJobQueueStatus(instidq, instid, instcd, auditId);
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
    @Override
    public AuditReportInfo getAuditReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        long giveup = System.currentTimeMillis() + timeout;
        try {
            while (true) {
                StatusInfo info = this.getAuditRunStatus(auditId, context);
                logger.info(info.getMessage());
                if (info.getIsSuccess()) break;
                Thread.currentThread().sleep(200);
                if (System.currentTimeMillis() > giveup) {
                    throw new TimeoutException("giving up after " + (timeout / 1000) + " seconds");
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
//        if (AUDIT_TYPE_KEY_DEFAULT.equals(auditTypeKey))
//        {
//            return getDARSReport(auditId);
//        }
//        if (AUDIT_TYPE_KEY_HTML.equals(auditTypeKey)) {
            return getHTMLReport(auditId);
//        }
//        throw new InvalidParameterException("auditTypeKey: " + auditTypeKey);
    }

    // default is to create real links, unit tests should change to LINK_TEMPLATE.TEST
    private CourseLinkBuilder.LINK_TEMPLATE courseLinkTemplateStyle = CourseLinkBuilder.LINK_TEMPLATE.COURSE_DETAILS;
//    private CourseLinkBuilder.LINK_TEMPLATE courseLinkTemplateStyle = CourseLinkBuilder.LINK_TEMPLATE.TEST;

    public void setCourseLinkTemplateStyle(CourseLinkBuilder.LINK_TEMPLATE style ) {
        courseLinkTemplateStyle = style;
    }

    public AuditReportInfo getDARSReport(String auditId) {
        JobQueueRunLoader jqrl = getJobQueueRunLoader();
        JobQueueRun run = jqrl.loadJobQueueRun(auditId);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.println( "<pre>");

        List<JobQueueOut> outList = (List<JobQueueOut>) run.getJobQueueOuts();
        for (JobQueueOut out : outList) {
            String darout = out.getDarout();
            char lasera = out.getLasera().charAt( 0 );
            switch (lasera) {
                // Linkify these rows
                case 'a':
                case 'b':
                case 'c':
                case 'n':
                case 'A':
                case 'B':
                    String victim = CourseLinkBuilder.makeLinks(darout, courseLinkTemplateStyle);
                    pw.println(victim);
                    break;

                // Do not linkify these rows
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '7':
                case '8':
                case 'g':
                case 'j':
                case 'C':
                case 'D':
                case 'F':
                    pw.println(darout);
                    break;

                // Don't know what to do with these rows
                case '0':
                case '9':
                case 'e':
                case 'h':
                case 'i':
                case 'k':
                case 'l':
                case 'm':
                case 'U':
                default:
                    pw.println(darout);
                    break;
            }
       }
        pw.println("</pre>");
        String html = sw.toString();
        AuditReportInfo auditReportInfo = new AuditReportInfo();
        AuditDataSource dataSource = new AuditDataSource(html, auditId);
        DataHandler handler = new DataHandler(dataSource);
        auditReportInfo.setAuditId(auditId);
        auditReportInfo.setReport(handler);
        return auditReportInfo;

    }

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
            InputStream in = new ByteArrayInputStream(garbage.getBytes() );

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            DocumentBuilder builder = factory.newDocumentBuilder();

            Document doc = builder.parse(in);

            XPathFactory crapfactory = XPathFactory.newInstance();
            XPath xpath = crapfactory.newXPath();
            XPathExpression expr = xpath.compile("/html/body/*");
            Object ugh = expr.evaluate(doc, XPathConstants.NODESET);
            NodeList nodeList = (NodeList) ugh;

            System.out.println("found: " + nodeList.getLength());
            Node root = nodeList.item(0);

            String path = "//div[@class='reqText']/text()";
            crazyDOMLinkifier( doc, xpath, path, builder );

            path = "//span[@class='subreqTitleText']/text()";
            crazyDOMLinkifier(doc, xpath, path, builder);


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            transformer.setOutputProperty(OutputKeys.METHOD, "html");
            DOMSource source = new DOMSource(root);
            StringWriter sw = new StringWriter();
            StreamResult result = new StreamResult(sw);

            transformer.transform(source, result);
            answer = sw.toString();

        } catch (Exception e) {
            logger.error( "getHTMLReport failed", e);
        }
        AuditDataSource dataSource = new AuditDataSource(answer, auditId);
        DataHandler dh = new DataHandler(dataSource);
        auditReportInfo.setReport(dh);
        return auditReportInfo;
    }

    public void crazyDOMLinkifier( Document doc, XPath xpath, String path, DocumentBuilder builder ) throws XPathExpressionException, IOException, SAXException {
        XPathExpression godot = xpath.compile(path);
        Object godotSet = godot.evaluate(doc, XPathConstants.NODESET);
        NodeList godotList = (NodeList) godotSet;
        for (int nth = 0; nth < godotList.getLength(); nth++) {
            Node child = godotList.item(nth);
            String scurge = child.getTextContent();
            String victim = CourseLinkBuilder.makeLinks(scurge, courseLinkTemplateStyle);
            if (!scurge.equals(victim)) {
                victim = victim.replace( "&", "&amp;");

                victim = "<fake>" + victim + "</fake>";
                builder.reset();
                Document whoopie = builder.parse(new InputSource(new StringReader(victim)));
                Node fake = whoopie.getDocumentElement();

                Node parent = child.getParentNode();
                parent.removeChild(child);
                NodeList lamesters = fake.getChildNodes();
                for (int xing = 0; xing < lamesters.getLength(); xing++) {
                    Node tank = lamesters.item(xing);
                    Node crank = doc.importNode(tank, true);
                    parent.appendChild(crank);
                }
            }
        }
    }

    public AuditReportInfo getHTMLReportXYZ(String auditId) throws OperationFailedException {
        try {
            Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            Velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
            Velocity.init();

            Template template = Velocity.getTemplate("org/kuali/student/myplan/audit/service/report.vm");
            VelocityContext context = new VelocityContext();
            Report report = getAuditReport(auditId);
            context.put("report", report);
            context.put("courseLinkBuilder", CourseLinkBuilder.class);

            StringWriter sw = new StringWriter();
            template.merge(context, sw);
            String html = sw.toString();
            AuditReportInfo auditReportInfo = new AuditReportInfo();
            AuditDataSource dataSource = new AuditDataSource(html, auditId);
            DataHandler handler = new DataHandler(dataSource);
            auditReportInfo.setAuditId(auditId);
            auditReportInfo.setReport(handler);
            return auditReportInfo;
        } catch (Exception e) {
            throw new OperationFailedException("velocity error", e);
        }
    }

    String getDPFYT( String dprog )
    {
        String dpfyt = null;
        DprogHibernateDao dao = (DprogHibernateDao) getDprogDao();

        String sql = "FROM Dprog d WHERE d.comp_id.dprog = ?";
        Object[] params = new Object[]{dprog };
        List list = dao.find(sql, params);
        if (list.size() > 0) {
            Dprog d = (Dprog) list.get(0);
            dpfyt = d.getComp_id().getDpfyt();
        }
        return dpfyt;

    }

//    String reqFlagsHQL = "FROM ReqMain r WHERE r.comp_id.rname = ? and r.comp_id.rqfyt = ?";
    String reqFlagsHQL = "FROM ReqMain r WHERE r.comp_id.rname = ? and r.comp_id.rqfyt <= ? and ? <= r.lyt";

    // Requirement report suppression flags: true = render, false = hide
    class ReqFlags
    {
        String subreq = " "; // ReqMain.reqsrqf
        String credits = " ";  // ReqMain.reqhrsf
//        String gpa = " "; // ReqMain.reqgpaf
        String courses = " "; // ReqMain.reqctf
        String rqfyt = "99994"; //
        String nolist = null;
        String nocompl = " ";
    }

    public ReqFlags getReqFlags( String rname, String lyt )
    {
        ReqFlags result = new ReqFlags();

        DprogHibernateDao dao = (DprogHibernateDao) getDprogDao();

        Object[] params = new Object[]{ rname, lyt, lyt };
        List list = dao.find(reqFlagsHQL, params);
        if( list.size() > 0 )
        {
            ReqMain reqMain = (ReqMain) list.get( 0 );
            result.subreq = reqMain.getReqsrqf();
            result.credits = reqMain.getReqhrsf();
            result.courses = reqMain.getReqctf();
            result.rqfyt = reqMain.getComp_id().getRqfyt();
            result.nolist = reqMain.getNolist();
            result.nocompl = reqMain.getNocompl();
        }
        return result;
    }

    String subreqFlagsHQL = "SELECT s.reqhrsf, s.reqgpaf, s.reqctf, s.nolist FROM ReqMain r JOIN r.subReqs s WHERE  r.comp_id.rname = ? and s.sreqno = ?";
//    String subreqFlagsHQL = "SELECT * FROM ReqMain r JOIN r.subReqs s WHERE  r.comp_id.rname = ? and s.sreqno = ?";

    class SubreqFlags
    {
        String credits = " ";  // Subreq.reqhrsf
//        String gpa = " "; // Subreq.reqgpaf
        String courses = " "; // Subreq.reqctf
        String nolist = null;
    }

    public SubreqFlags getSubreqFlags(String rname, String rqfyt, Integer userSeqNo ) {
        SubreqFlags result = new SubreqFlags();

        DprogHibernateDao dao = (DprogHibernateDao) getDprogDao();

//        Object[] params = new Object[]{rname, rqfyt, userSeqNo};
        Object[] params = new Object[]{rname, userSeqNo};
        List list = dao.find(subreqFlagsHQL, params);
        for (Object first : list) {
            Object[] flags = (Object[]) first;
            result.credits = (String) flags[0];
//            result.gpa = (String) flags[1];
            result.courses = (String) flags[2];
            result.nolist = (String) flags[3];
            break;
        }

        return result;
    }


    public Report getAuditReport(String auditid) {

        Report report = new Report();

        JobQueueRunLoader jqrl = getJobQueueRunLoader();
        JobQueueRun run = jqrl.loadJobQueueRun(auditid);

        String dpfyt = getDPFYT(run.getDprog());

        report.setWebTitle(run.getDptitle1());
        report.setDegreeProgram(run.getDprog());

        Timestamp runDate = run.getRundate();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy h:mm a");
        report.setDatePrepared(formatter.format(runDate));

        String yearterm = run.getCatlyt();
        String readableyearterm = yearTermMagic( yearterm );
        report.setEntryDateProgram(readableyearterm );

        String complete = run.getComplete();
        report.setComplete( "C".equals( complete ));

        Section section = null;
        List<JobQueueReq> list = run.getJobQueueReqs();
        Comparator<JobQueueReq> reqSorter = new Comparator<JobQueueReq>()
        {
            public int compare(JobQueueReq o1, JobQueueReq o2) {
                String f1 = o1.getSortflg();
                if( f1 == null ) return -1;
                String f2 = o2.getSortflg();
                if( f2 == null ) return 1;
                int gah = f1.compareTo( f2 );
                return gah;
            }
        };

        Comparator<CourseTaken> takenSorter = new Comparator<CourseTaken>() {
            public int compare(CourseTaken t1, CourseTaken t2) {
                String d1 = t1.getDept();
                if (d1 == null) return -1;
                String d2 = t2.getDept();
                if (d2 == null) return 1;
                int compare = d1.compareTo( d2  );
                if( compare == 0 )
                {
                    String n1 = t1.getNumber();
                    if (n1 == null) return -1;
                    String n2 = t2.getNumber();
                    if (n2 == null) return 1;
                    compare = n1.compareTo(n2);
                    if (compare == 0) {
                        String q1 = t1.getQuarter();
                        if (q1 == null) return -1;
                        String q2 = t2.getQuarter();
                        if (q2 == null) return 1;
                        compare = q1.compareTo(q2);

                    }

                }
                return compare;
            }
        };

        Collections.sort( list, reqSorter );

        for (JobQueueReq jqr : list )
        {
            String rname = jqr.getRname();
            if ("".equals( rname )) continue;
            if ("H".equals(jqr.getHidden())) continue;


            String reqText = getText( jqr, true );

            String satisfied = jqr.getSatisfied().trim().toUpperCase();

            switch( getStructure( jqr ))
            {
                case Advisory:
                {
                    report.addAdvisory( reqText );
                    break;
                }

                case Section:
                {
                    section = report.newSection();
                    section.setCaption(reqText);
                    break;
                }

                case Requirement:
                {
                    ReqFlags reqFlags = getReqFlags(rname, dpfyt);

                    Requirement requirement = report.newRequirement();
                    requirement.setStatus(satisfied);
                    requirement.setCaption(reqText);
                    requirement.setNocompl( reqFlags.nocompl );

                    requirement.optional = "1".equals( jqr.getOptreq() );


                    if(!"X".equals( reqFlags.credits ))
                    {
                        float reqHrs = jqr.getReqhrs().floatValue();
                        float gotHrs = jqr.getGothrs().floatValue();
                        float needHrs = jqr.getNeedhrs().floatValue();
                        float ipHrs = jqr.getIphrs().floatValue();
                        if (reqHrs > 1.0f && reqHrs < 999.0f) {
                            Credits credits = new Credits();
                            credits.setFlag( reqFlags.credits );
//                            credits.setRequired(reqHrs);
                            credits.setEarned(gotHrs - ipHrs);
                            credits.setInprogress(ipHrs);
                            credits.setNeeds(needHrs);
                            requirement.setCredits(credits);
                        }
                    }

                    if (! equals( jqr.getReqgpaf(), "X" ))
//                    if (!equals(jqr.getReqgpaf(), "X", "A"))
                    {
                        float reqGPA = jqr.getReqgpa().floatValue();
                        float earned = jqr.getGotgpa().floatValue();

                        if (reqGPA > 0.0f) {
                            GPA gpa = new GPA();
                            gpa.setFlag(jqr.getReqgpaf());
                            gpa.setRequired(reqGPA);
                            gpa.setEarned(earned);
                            requirement.setGPA(gpa);
                        }
                    }

                    if (!"X".equals(reqFlags.courses))
                    {
                        int reqCourses = jqr.getReqct();
                        int needCourses = jqr.getNeedct();

                        if (reqCourses > 0) {
                            Count count = new Count();
                            count.setFlag(reqFlags.courses);
                            count.setRequired(reqCourses);
                            count.setNeeds(needCourses);
                            requirement.setCount(count);
                        }
                    }

                    List<JobQueueSubreq> subreqs = jqr.getJobQueueSubreqs();
                    for (JobQueueSubreq jqsr : subreqs ) {
                        if ("H".equals(jqsr.getHidden())) continue;

                        Integer sno = Integer.valueOf( jqsr.getSno() );
                        SubreqFlags subreqFlags = getSubreqFlags( rname, reqFlags.rqfyt, sno );

                        Subrequirement subrequirement = new Subrequirement();
                        requirement.addSubrequirement(subrequirement);
                        String subsatisfied = jqsr.getSatisfied();
                        subrequirement.setStatus(subsatisfied);
                        subrequirement.setNolist( subreqFlags.nolist );
                        subrequirement.optional = "X".equals(jqr.getOptreq());

                        {
                            StringBuilder buf = new StringBuilder();
                            for (JobQueueSubreqText text : jqsr.getJobQueueSubreqTexts()) {
                                String temp = text.getText();
                                buf.append(temp);
                                buf.append(" ");
                            }

                            String subReqText = scrub( buf.toString() );

                            if (subReqText.length() > 0) {
                                subrequirement.setCaption(subReqText);
                            }
                        }

                        if(!"X".equals(subreqFlags.credits ))
                        {
                            float reqHrs = jqsr.getReqhrs().floatValue();
                            float gotHrs = jqsr.getGothrs().floatValue();
                            float needHrs = jqsr.getNeedhrs().floatValue();
                            float ipHrs = jqsr.getIphrs().floatValue();
                            if (reqHrs > 1.0f && reqHrs < 999.0f) {
                                Credits credits = new Credits();
                                credits.setFlag(subreqFlags.credits);
                                credits.setEarned(gotHrs - ipHrs);
                                credits.setInprogress(ipHrs);
                                credits.setNeeds(needHrs);
                                subrequirement.setCredits(credits);
                            }
                        }

                        if (!equals(jqsr.getReqgpaf(), "X", "A"))
                        {
                            float reqGPA = jqr.getReqgpa().floatValue();
                            float gotGPA = jqr.getGotgpa().floatValue();

                            if (reqGPA > 0.0f) {
                                GPA gpa = new GPA();
                                gpa.setFlag(jqsr.getReqgpaf());
                                gpa.setRequired(reqGPA);
                                gpa.setEarned(gotGPA);
                                subrequirement.setGPA(gpa);
                            }
                        }

                        if (!"X".equals(subreqFlags.courses)) {
                            int reqCourses = jqsr.getReqct();
                            int needCourses = jqsr.getNeedct();

                            if (reqCourses > 0) {
                                Count count = new Count();
                                count.setFlag(subreqFlags.courses);
                                count.setRequired(reqCourses);
                                count.setNeeds(needCourses);
                                subrequirement.setCount(count);
                            }
                        }

                        if ( subrequirement.showTaken() )
                        {
                            for (JobQueueCourse jqc : jqsr.getJobQueueCourses()) {
                                CourseTaken taken = new CourseTaken();
                                String dept = "";
                                String number = "";

                                String course = jqc.getCourse().trim();
                                if ("FL-HS".equals(course)) {
                                    dept = course;
                                } else if ("NATSPEAK".equals(course)) {
                                    dept = course;
                                } else {
                                    course = course.substring(1);
                                    if (course.length() > 6) {
                                        dept = course.substring(0, 6).trim();
                                        number = course.substring(6).trim();
                                        if (number.length() > 3) {
                                            number = number.substring(0, 3).trim();
                                        }

                                    } else {
                                        dept = course.trim();
                                    }
                                }
                                taken.setDept(dept);
                                taken.setNumber(number);
                                taken.setGrade(jqc.getRgrade());
                                taken.setDescription(jqc.getCtitle());
                                taken.setCredits(jqc.getCredit().floatValue());
                                taken.setCredits(jqc.getCredit().floatValue());
                                taken.setQuarter(jqc.getEditYt());
                                boolean inProgress = "I".equals(jqc.getIp());
                                taken.setInProgress(inProgress);

                                subrequirement.addCourseTaken(taken);
                            }

                            List<CourseTaken> listx = subrequirement.getCourseTakenList();
                            Collections.sort(listx, takenSorter);
                        }

                        if( subrequirement.showAcceptable() )
                        {
                            // Acceptable courses
                            List<JobQueueAccept> acceptList = jqsr.getJobQueueAccepts();
                            for (JobQueueAccept accept : acceptList)
                            {
                                String dept = accept.getDept();
                                if (dept.startsWith("**")) continue;
                                String number = accept.getCrsno();
                                if (number.length() == 0) continue;

                                CourseAcceptable courseAcceptable = new CourseAcceptable();
                                courseAcceptable.setDept(dept);
                                courseAcceptable.setNumber(number);
                                subrequirement.addCourseAcceptable(courseAcceptable);
                            }
                        }
                    }
                }
            }
        }
        return report;
    }

    public boolean equals( Object victim, Object... list ) {
        if( victim == null ) return false;
        if( list == null ) return false;
        if( list.length == 0 ) return false;
        for( Object item : list )
        {
            if( item == null ) continue;
            if( victim.equals( item )) return true;

        }
        return false;
    }

    String scrub( String victim ) {
        victim = victim.replace("*", " ");
        victim = victim.replace("_", " ");
        victim = victim.replaceAll("\\s+", " ").trim();
        return victim;
    }

    public String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }


    public String hardcodedStudentID( String studentId ) {
        // Used by devs when logged in as admin
//        if( "admin".equals( studentId )) return   "100190981";
        // Used by Jill for demos
        if( "jjulius".equals( studentId )) return "101360188";

        if (true) return "101360188";

        // do nothing
        return studentId;
    }

    @Override
    public List<AuditReportInfo> getAuditsForStudentInDateRange(@WebParam(name = "studentId") String studentId,
                                                                @WebParam(name = "startDate") Date startDate,
                                                                @WebParam(name = "endDate") Date endDate,
                                                                @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {



        studentId = hardcodedStudentID( studentId );
        List<AuditReportInfo> list = new ArrayList<AuditReportInfo>();
        JobQueueRunDao runrun = getJobQueueRunDao();

        // TODO: configurable constant for UW
        String instid = "4854";
        // TODO: configurable constant for UW
        String instidq = "72";
        // I have no idea what 'instcd' list does
        List<String> instcd = new ArrayList<String>();
        instcd.add("");
        List<JobQueueRun> load = runrun.load(instid, instidq, instcd, studentId);

        for (JobQueueRun jqr : load) {
            AuditReportInfo audit = new AuditReportInfo();
            audit.setAuditId(jqr.getJobid());
            audit.setReportType(DegreeAuditServiceConstants.AUDIT_TYPE_KEY_SUMMARY);
            audit.setStudentId(studentId);
            audit.setProgramId(jqr.getWebtitle());
            audit.setRunDate(jqr.getRundate());
            audit.setRequirementsSatisfied("Unknown");
            list.add(audit);
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

    public static String yearTermMagic(String yearterm) {
        String result = "";
        if (yearterm.length() == 5) {
            String year = yearterm.substring(0, 4);
            switch (yearterm.charAt(4)) {
                case '1':
                    result = "WIN/" + year;
                    break;
                case '2':
                    result = "SPR/" + year;
                    break;
                case '3':
                    result = "SUM/" + year;
                    break;
                case '4':
                    result = "AUT/" + year;
                    break;
                default:
                    break;
            }
        }
        return result;
    }

    @Override
    public List<AuditProgramInfo> getAuditPrograms(@WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        DprogHibernateDao dao = (DprogHibernateDao) getDprogDao();
        List<AuditProgramInfo> auditProgramInfoList = new ArrayList<AuditProgramInfo>();
        String hql = "SELECT dp.comp_id.dprog, dp.webtitle from Dprog dp WHERE (dp.comp_id.dprog LIKE '0%' or dp.comp_id.dprog LIKE '1%' or dp.comp_id.dprog LIKE '2%') AND dp.lyt>=? AND dp.webtitle IS NOT NULL AND dp.webtitle<>'' AND dp.dpstatus='W'";
        String[] currentTermAndYear=this.getCurrentYearAndTerm();
        int year=Integer.parseInt(currentTermAndYear[1])-10;
        StringBuffer termYear=new StringBuffer();
        termYear=termYear.append(year).append(currentTermAndYear[0]);
        String[] params = new String[]{termYear.toString()};
        List programs = dao.find(hql, params);
        for (int i = 0; i < programs.size(); i++) {
            Object[] objs = null;
            AuditProgramInfo auditProgramInfo = new AuditProgramInfo();
            objs = (Object[]) programs.get(i);
            auditProgramInfo.setProgramId((String) objs[0]);
            auditProgramInfo.setProgramTitle((String) objs[1]);
            auditProgramInfoList.add(auditProgramInfo);
        }
        return auditProgramInfoList;
    }
    /*Implemented to get the current year and the term value from the academic calender service.*/
    public String[] getCurrentYearAndTerm() {
        String currentAtp = DegreeAuditAtpHelper.getCurrentAtpId();
        String[] termYear = DegreeAuditAtpHelper.atpIdToTermAndYear(currentAtp);
        return new String[]{termYear[0].trim(), termYear[1].trim()};
    }


}