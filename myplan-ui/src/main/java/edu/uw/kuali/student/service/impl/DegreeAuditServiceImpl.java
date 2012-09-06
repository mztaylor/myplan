package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.log4j.Logger;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.audit.service.model.AuditDataSource;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.myplan.util.DegreeAuditAtpHelper;
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
import uachieve.apis.audit.*;
import uachieve.apis.audit.dao.JobQueueListDao;
import uachieve.apis.audit.dao.JobQueueRunDao;
import uachieve.apis.audit.jobqueueloader.JobQueueRunLoader;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeoutException;

import uachieve.apis.requirement.dao.hibernate.DprogHibernateDao;
import uachieve.apis.requirement.dao.DprogDao;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DegreeAuditServiceImpl implements DegreeAuditService {

    public static void main( String[] args )
        throws Exception
    {
        DegreeAuditServiceImpl impl = new DegreeAuditServiceImpl();

        String studentId = "D8D636BEB4CC482884420724BF152709";
        String programId = "1BISMCS0011";
        AuditReportInfo info = impl.runAudit( studentId, programId, null, null );

        System.out.println( impl.padfront( "  1 2 " ));
    }

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
        try
        {
            programId = programId.replace( '$', ' ' );
            // padding, because sometimes degree program ids are not 12 chars long
            programId = programId + "              ";

            String campus = "SEATTLE";
            char oof = programId.charAt(0 );
            switch( oof )
            {
                case '1': campus = "BOTHELL"; break;
                case '2': campus = "TACOMA"; break;
                default: break;
            }

            String major = programId.substring( 1, 7 ).trim();
            String pathway = programId.substring(7, 9);
            String level = programId.substring(9, 10);
            String type = programId.substring(10, 11);

            String stinker = new String( requestTemplate );
            stinker = stinker.replace("$campus", campus);
            stinker = stinker.replace("$level", level);
            stinker = stinker.replace("$type", type);
            stinker = stinker.replace("$major", major);
            stinker = stinker.replace("$pathway", pathway);
            stinker = stinker.replace("$regid", studentId);

            String postAuditRequestURL = studentServiceClient.getBaseUrl() + "/v5/degreeaudit.xml";

            Client client = studentServiceClient.getClient();

            Request request = new Request( Method.POST, postAuditRequestURL );

            StringRepresentation lame = new StringRepresentation( stinker );
            request.setEntity( lame );

            Response response = client.handle( request );
            Representation rep = response.getEntity();

            Status status = response.getStatus();
            if( Status.isSuccess( status.getCode() ))
            {
                SAXReader reader = new SAXReader();
                org.dom4j.Document document = reader.read(rep.getStream());
                String xml = document.asXML();


                Map<String, String> namespaces = new HashMap<String, String>();
                namespaces.put("x", "http://webservices.washington.edu/student/");
                DefaultXPath jobid1Path = new DefaultXPath("//x:DegreeAudit/x:JobId");
                DefaultXPath jobid2Path = new DefaultXPath("//x:DegreeAudit/x:JobID");
                jobid1Path.setNamespaceURIs(namespaces);
                jobid2Path.setNamespaceURIs(namespaces);

                org.dom4j.Node jobid1Node = jobid1Path.selectSingleNode(document);
                org.dom4j.Node jobid2Node = jobid2Path.selectSingleNode(document);

                String jobid = "missing jobid";
                if( jobid1Node != null )
                {
                    jobid = jobid1Node.getText();
                }
                else if( jobid2Node != null )
                {
                    jobid = jobid2Node.getText();
                }
                AuditReportInfo auditReportInfo = new AuditReportInfo();

                auditReportInfo.setAuditId(jobid);
                return auditReportInfo;
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                InputStream in = rep.getStream();
                int c = 0;
                while( ( c = in.read() ) != -1 )
                {
                    sb.append( (char) c );
                }
                throw new Exception( sb.toString() );
            }
        }
        catch( Exception e )
        {
            logger.error( e );
            throw new OperationFailedException( "cannot request audit", e );
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
    @Override
    public AuditReportInfo getAuditReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context)
        throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException
   {

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
            return getDARSReport(auditId);
//        }
//        if (AUDIT_TYPE_KEY_HTML.equals(auditTypeKey)) {
//            return getHTMLReport(auditId);
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
            if(out.getUserSeqNo()==0){
                darout=darout.substring(0,darout.trim().length()-9).trim();
            }
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
                    victim = padfront( victim );
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
                    darout = padfront( darout );
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
                    darout = padfront( darout );
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

    String padfront( String source )
    {
        StringBuilder sb = new StringBuilder();
        boolean front = true;
        for( int nth = 0; nth < source.length(); nth++ )
        {
            char c = source.charAt( nth );
            if( front && c == ' ' )
            {
                sb.append( "&nbsp;" );
            }
            else
            {
                front = false;
                sb.append( c );
            }
        }
        return sb.toString();
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

    public String convertSDBSyskeyToMyPlanStuno( String syskey )
    {
        // Convert systemKey to myplan stuno
        if( syskey.length() < 9 || !syskey.startsWith( "1" ))
        {
            while( syskey.startsWith( "0" ))
            {
                syskey = syskey.substring( 1 );
            }
            String myplan = "100000000";
            syskey = myplan.substring( 0, 9 - syskey.length() ) + syskey;
        }

        return syskey;
    }

    @Override
    public List<AuditReportInfo> getAuditsForStudentInDateRange(@WebParam(name = "studentId") String studentId,
                                                                @WebParam(name = "startDate") Date startDate,
                                                                @WebParam(name = "endDate") Date endDate,
                                                                @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {

        List<AuditReportInfo> list = new ArrayList<AuditReportInfo>();

        String stuno = convertSDBSyskeyToMyPlanStuno( studentId );
        logger.info( "getAuditsForStudentInDateRange studentid " + studentId + "  stuno " + stuno );


        // TODO: configurable constant for UW
        String instid = "4854";
        // TODO: configurable constant for UW
        String instidq = "72";
        // I have no idea what 'instcd' list does
        List<String> instcd = new ArrayList<String>();
        instcd.add("");
        try
        {
            JobQueueRunDao runrun = getJobQueueRunDao();
            List<JobQueueRun> load = runrun.load(instid, instidq, instcd, stuno);

            for (JobQueueRun jqr : load) {
                AuditReportInfo audit = new AuditReportInfo();
                audit.setAuditId(jqr.getJobid());
                audit.setReportType(DegreeAuditServiceConstants.AUDIT_TYPE_KEY_SUMMARY);
                audit.setStudentId(stuno);
                audit.setProgramId(jqr.getWebtitle());
                audit.setRunDate(jqr.getRundate());
                audit.setRequirementsSatisfied("Unknown");
                list.add(audit);
            }
        }
        catch( DataRetrievalFailureException e )
        {
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
        String hql = "SELECT dp.comp_id.dprog, dp.webtitle from Dprog dp WHERE (dp.comp_id.dprog LIKE '0%' or dp.comp_id.dprog LIKE '1%' or dp.comp_id.dprog LIKE '2%') AND dp.lyt>=? AND dp.webtitle IS NOT NULL AND dp.webtitle<>'' AND dp.dpstatus='W'";
        String[] currentTermAndYear=this.getCurrentYearAndTerm();
        int year=Integer.parseInt(currentTermAndYear[1])-10;
        StringBuffer termYear=new StringBuffer();
        termYear=termYear.append(year).append(currentTermAndYear[0]);
        String[] params = new String[]{termYear.toString()};
        List programs = dao.find(hql, params);
        for (int i = 0; i < programs.size(); i++) {
            AuditProgramInfo auditProgramInfo = new AuditProgramInfo();
            Object[] objs = (Object[]) programs.get(i);
            String programId = (String) objs[0];
            programId = programId.replace( ' ', '$' );
            auditProgramInfo.setProgramId( programId );
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

    public StudentServiceClient getStudentServiceClient() {
        return studentServiceClient;
    }

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }
}