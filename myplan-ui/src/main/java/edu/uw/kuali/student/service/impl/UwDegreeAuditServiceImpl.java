package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.model.AuditDataSource;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.restlet.Client;
import org.restlet.representation.StringRepresentation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeoutException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.xpath.DefaultXPath;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.util.Series;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public abstract  class
        UwDegreeAuditServiceImpl
implements
        DegreeAuditService
{
//    private StudentServiceClient studentServiceClient;
//
//    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
//        this.studentServiceClient = studentServiceClient;
//    }
//
//    public static void main( String[] args )
//        throws Exception
//    {
//        UwDegreeAuditServiceImpl impl = new UwDegreeAuditServiceImpl();
//
////        impl.getAuditsForStudentInDateRange( null, null, null, null );
////        AuditReportInfo garok = impl.runAudit( null, null, null, null );
//
//        List<AuditProgramInfo> list = impl.getAuditPrograms( null );
//        AuditReportInfo info = impl.getAuditReport("/student/v5/degreeaudit/SEATTLE,A%20A,00,1,6,9136CCB8F66711D5BE060004AC494FFE.xml", null, null);
//        System.out.println( info.getCampus() );
//    }
//
//
//    private final Logger logger = Logger.getLogger(UwDegreeAuditServiceImpl.class);
//    private static final String SERVICE_VERSION = "v4";
//
//    public UwDegreeAuditServiceImpl() {
//    }
//
//    String postAuditRequestURL = "https://ucswseval1.cac.washington.edu/student/v5/degreeaudit.xml";
//
//    public final static String requestTemplate =
//        "<DegreeAudit>" +
//        "<Campus>SEATTLE</Campus>" +
//        "<PlanningAudit/>" +
//        "<DegreeLevel>$level</DegreeLevel>" +
//        "<DegreeType>$type</DegreeType>" +
//        "<MajorAbbreviation>$major</MajorAbbreviation>" +
//        "<Pathway>$pathway</Pathway>" +
//        "<RegID>$regid</RegID>" +
//        "</DegreeAudit>";
//
//    @Override
//    public AuditReportInfo runAudit(
//        @WebParam(name = "studentId") String studentId,
//        @WebParam(name = "programInfo") AuditProgramInfo programInfo,
//        @WebParam(name = "auditTypeKey") String auditTypeKey,
//        @WebParam(name = "context") ContextInfo useless
//    )
//        throws InvalidParameterException, MissingParameterException, OperationFailedException
//    {
//        try {
//
////            String level = "1";
////            String type = "2";
////            String major = "ACCTG";
////            String pathway = "00";
////            String regid = "D8D636BEB4CC482884420724BF152709";
//
//            String level = programInfo.getDegreeLevel();
//            String type = programInfo.getDegreeType();
//            String major = programInfo.getProgramId();
//            String pathway = programInfo.getPathway();
//
//            String stinker = new String( requestTemplate );
//            stinker = stinker.replace("$level", level);
//            stinker = stinker.replace("$type", type);
//            stinker = stinker.replace("$major", major);
//            stinker = stinker.replace("$pathway", pathway);
//            stinker = stinker.replace("$regid", studentId);
//
////            Context context = new Context();
////            Series<Parameter> parameters = context.getParameters();
////            parameters.add("followRedirects", "false");
////            parameters.add("truststorePath", trustStoreFilename);
////            parameters.add("truststorePassword", trustStorePasswd);
////            parameters.add("keystorePath", keyStoreFilename);
////            parameters.add("keystorePassword", keyStorePasswd);
////
////            Client  client = new Client( Protocol.HTTPS );
////            client.setContext( context );
//
//            Client client = studentServiceClient.getClient();
//
//            Request request = new Request( Method.POST, postAuditRequestURL );
//            StringRepresentation lame = new StringRepresentation( stinker );
//            request.setEntity( lame );
//
//            Response response = client.handle( request );
//            Representation rep = response.getEntity();
//
//            Status status = response.getStatus();
//            if( Status.isSuccess( status.getCode() ))
//            {
//                SAXReader reader = new SAXReader();
//                Document document = reader.read(rep.getStream());
//
//
//                Map<String, String> namespaces = new HashMap<String, String>();
//                namespaces.put("x", "http://webservices.washington.edu/student/");
//                DefaultXPath auditURLPath = new DefaultXPath("//x:DegreeAudit/x:DegreeAuditStatusURI/x:Href");
//                auditURLPath.setNamespaceURIs(namespaces);
//
//                Node auditURLNode = auditURLPath.selectSingleNode( document );
//
//                String auditURL = auditURLNode.getText();
//                AuditReportInfo auditReportInfo = new AuditReportInfo();
//
//                auditReportInfo.setAuditId(auditURL);
//                return auditReportInfo;
//            }
//            else
//            {
//                StringBuilder sb = new StringBuilder();
//                InputStream in = rep.getStream();
//                int c = 0;
//                while( ( c = in.read() ) != -1 )
//                {
//                    sb.append( (char) c );
//                }
//                throw new Exception( sb.toString() );
//            }
//        }
//        catch( Exception e )
//        {
//            logger.error( e );
//            throw new OperationFailedException( "cannot request audit", e );
//        }
//    }
//
//    @Override
//    public String runAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public StatusInfo getAuditRunStatus(@WebParam(name = "auditId") String auditId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
//        String instcd = "";
//        String instid = "4854";
//        String instidq = "72";
////        int status = jobQueueListDao.checkJobQueueStatus(instidq, instid, instcd, auditId);
//        StatusInfo info = new StatusInfo();
////        info.setSuccess(status == 1);
////
////        switch (status) {
////            case -2:
////                info.setMessage("audit request not found");
////                break;
////            case -1:
////                info.setMessage("completed with errors");
////                break;
////            case 0:
////                info.setMessage("not finished processing");
////                break;
////            case 1:
//                info.setMessage("complete");
////                break;
////            default:
////                break;
////        }
//        return info;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    String swsURL = "https://ucswseval1.cac.washington.edu";
//
//    public int timeout = 30 * 1000; // 30 seconds
//    @Override
//    public AuditReportInfo getAuditReport(
//        @WebParam(name = "auditId") String auditId,
//        @WebParam(name = "auditTypeKey") String auditTypeKey,
//        @WebParam(name = "context") ContextInfo useless
//    )
//        throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException
//    {
//
//        long giveup = System.currentTimeMillis() + timeout;
//        try
//        {
//            while( true )
//            {
//                StatusInfo info = this.getAuditRunStatus( auditId, useless );
//                logger.info(info.getMessage());
//                if (info.getIsSuccess()) break;
//                Thread.currentThread().sleep(200);
//                if (System.currentTimeMillis() > giveup) {
//                    throw new TimeoutException("giving up after " + (timeout / 1000) + " seconds");
//                }
//            }
//
////            Context context = new Context();
////            Series<Parameter> parameters = context.getParameters();
////            parameters.add("followRedirects", "false");
////            parameters.add("truststorePath", trustStoreFilename);
////            parameters.add("truststorePassword", trustStorePasswd);
////            parameters.add("keystorePath", keyStoreFilename);
////            parameters.add("keystorePassword", keyStorePasswd);
////
////            Client  client = new Client(Protocol.HTTPS);
////            client.setContext( context );
//
//            Client client = studentServiceClient.getClient();
//
//
//            String twiggy = swsURL + auditId;
//            Request request = new Request( Method.GET, twiggy );
//
//            Response response = client.handle( request );
//
//            if( Status.isSuccess( response.getStatus().getCode() ))
//            {
//                Representation rep = response.getEntity();
//                SAXReader reader = new SAXReader();
//                Document document = reader.read(rep.getStream());
//
//                Map<String, String> namespaces = new HashMap<String, String>();
//                namespaces.put("x", "http://webservices.washington.edu/student/");
//                DefaultXPath ridiculousLinePath = new DefaultXPath( "/x:DegreeAudit/x:DegreeAuditReport/x:DegreeAuditLine/x:Line" );
//                DefaultXPath campusPath = new DefaultXPath( "/x:DegreeAudit/x:Campus" );
//                DefaultXPath majorPath = new DefaultXPath( "/x:DegreeAudit/x:MajorAbbreviation" );
//                ridiculousLinePath.setNamespaceURIs(namespaces);
//                campusPath.setNamespaceURIs(namespaces);
//                majorPath.setNamespaceURIs(namespaces);
//
//                Node campusNode = campusPath.selectSingleNode( document );
//                String campus = campusNode.getText();
//                Node majorNode = majorPath.selectSingleNode( document );
//                String major = majorNode.getText();
//                List<?> children = ridiculousLinePath.selectNodes( document );
//                StringWriter sw = new StringWriter();
//                PrintWriter pw = new PrintWriter(sw);
//                pw.println( "<pre>");
//                for( Object child : children )
//                {
//                    Node node = (Node) child;
//                    String line = node.getText();
//                    String victim = CourseLinkBuilder.makeLinks( line, courseLinkTemplateStyle );
//                    pw.println( victim );
//                }
//                pw.println("</pre>");
//                String html = sw.toString();
//                System.out.println( html );
//                AuditReportInfo info = new AuditReportInfo();
//                info.setAuditId( auditId );
//                info.setCampus( campus );
//                info.setProgramId( major );
//                AuditDataSource source = new AuditDataSource( html, auditId );
//                DataHandler handler = new DataHandler( source );
//                info.setReport( handler );
//                return info;
//            }
//        }
//        catch( Exception e )
//        {
//            logger.error(e);
//        }
//
//        return null;
//    }
//
//    // default is to create real links, unit tests should change to LINK_TEMPLATE.TEST
//    private CourseLinkBuilder.LINK_TEMPLATE courseLinkTemplateStyle = CourseLinkBuilder.LINK_TEMPLATE.COURSE_DETAILS;
////    private CourseLinkBuilder.LINK_TEMPLATE courseLinkTemplateStyle = CourseLinkBuilder.LINK_TEMPLATE.TEST;
//
//    public void setCourseLinkTemplateStyle(CourseLinkBuilder.LINK_TEMPLATE style ) {
//        courseLinkTemplateStyle = style;
//    }
//
//    String getStudentAuditListURL = "https://ucswseval1.cac.washington.edu/student/v5/degreeaudit.xml?reg_id=";
////    String personSearchURL = "https://ucswseval1.cac.washington.edu/student/v4/person.xml?student_system_key=";
//
//    @Override
//    public List<AuditReportInfo> getAuditsForStudentInDateRange(
//        @WebParam(name = "studentId") String studentId,
//        @WebParam(name = "startDate") Date startDate,
//        @WebParam(name = "endDate") Date endDate,
//        @WebParam(name = "context") ContextInfo useless
//    )
//        throws InvalidParameterException, MissingParameterException, OperationFailedException
//    {
//        // https://ucswseval1.cac.washington.edu/student/v4/person?reg_id=&net_id=&student_number=&employee_id=&student_system_key=000083856
////        String personSearchURL = "https://ucswseval1.cac.washington.edu/student/v4/person?student_system_key=000083856";
////        studentId = "000083856";
////        String ugh = personSearchURL + studentId;
//
//
//        try
//        {
//            HashMap<String,String> genius = new HashMap<String,String>();
//            List<AuditProgramInfo> fantastic = getAuditPrograms( null );
//            for( AuditProgramInfo brilliant : fantastic )
//            {
//                genius.put( brilliant.getProgramId(), brilliant.getProgramTitle() );
//            }
//
//
//            SAXReader reader = new SAXReader();
//
//
////            Context context = new Context();
////            Series<Parameter> parameters = context.getParameters();
////            parameters.add("followRedirects", "false");
////            parameters.add("truststorePath", trustStoreFilename);
////            parameters.add("truststorePassword", trustStorePasswd);
////            parameters.add("keystorePath", keyStoreFilename);
////            parameters.add("keystorePassword", keyStorePasswd);
////
////            Client  client = new Client(Protocol.HTTPS);
////
////            client.setContext( context );
//            Client client = studentServiceClient.getClient();
//
//
//            List<AuditReportInfo> list = new ArrayList<AuditReportInfo>();
//
//            String lame = getStudentAuditListURL + studentId;
//
//            Request request2 = new Request( Method.GET, lame );
//            Response response2 = client.handle( request2 );
//
//            if( Status.isSuccess( response2.getStatus().getCode() ))
//            {
//                Representation rep2 = response2.getEntity();
//                Document document2 = reader.read(rep2.getStream());
//
//                Map<String, String> namespaces = new HashMap<String, String>();
//                namespaces.put("x", "http://webservices.washington.edu/student/");
//                DefaultXPath degreeAuditPath = new DefaultXPath( "//x:SearchResults/x:DegreeAudits/x:DegreeAudit" );
//                DefaultXPath datePreparedPath = new DefaultXPath( "x:DatePrepared" );
//                DefaultXPath majorPath = new DefaultXPath( "x:DegreeAuditURI/x:MajorAbbreviation" );
//                DefaultXPath meaninglessDrivelPath = new DefaultXPath( "x:DegreeAuditURI/x:Href" );
//
//                degreeAuditPath.setNamespaceURIs(namespaces);
//                datePreparedPath.setNamespaceURIs(namespaces);
//                majorPath.setNamespaceURIs(namespaces);
//                meaninglessDrivelPath.setNamespaceURIs(namespaces);
//
//                List<?> children = degreeAuditPath.selectNodes( document2 );
//                for( Object child : children )
//                {
//                    Node datePreparedNode = datePreparedPath.selectSingleNode( child );
//                    Node majorNode = majorPath.selectSingleNode( child );
//                    Node meaninglessDrivelNode = meaninglessDrivelPath.selectSingleNode( child );
//
//                    String datePrepared = datePreparedNode.getText();
//                    datePrepared = datePrepared.replace( "a.m.", "AM" );
//                    datePrepared = datePrepared.replace( "p.m.", "PM" );
//                    Date bongo = new SimpleDateFormat( "hh:mm a MM/DD/yy" ).parse( datePrepared );
//                    String major = majorNode.getText();
//                    String meaninglessDrivel = meaninglessDrivelNode.getText();
//
//                    String title = genius.get( major );
//
//                    AuditReportInfo info = new AuditReportInfo();
//                    info.setAuditId( meaninglessDrivel );
//                    info.setProgramId(title);
//                    info.setRunDate( bongo );
//                    list.add( info );
//
//                }
//            }
//            return list;
//        }
//        catch( Exception e )
//        {
//            logger.error( e );
//            throw new OperationFailedException( "something bad happened", e );
//        }
//    }
//
//    @Override
//    public AuditReportInfo runWhatIfAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "academicPlan") LearningPlanInfo academicPlan, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public String runWhatIfAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "academicPlan") LearningPlanInfo academicPlan, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public AuditReportInfo runEmptyAudit(@WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public String runEmptyAuditAsync(@WebParam(name = "programId") String programId, @WebParam(name = "context") ContextInfo context)
//        throws InvalidParameterException, MissingParameterException, OperationFailedException
//    {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    String listProgramsURL = "https://ucswseval1.cac.washington.edu/student/v5/degreeauditprogram.xml";
//
//    String trustStoreFilename = "/Users/jasonosgood/kuali/main/certs/uw.jts";
//    String trustStorePasswd = "secret";
//    String keyStoreFilename = "/Users/jasonosgood/kuali/main/certs/uwkstest.jks";
//    String keyStorePasswd = "changeit";
//
//    @Override
//    public List<AuditProgramInfo> getAuditPrograms( @WebParam(name = "context") ContextInfo useless )
//        throws InvalidParameterException, MissingParameterException, OperationFailedException
//    {
//        try
//        {
//            Context context = new Context();
//            Series<Parameter> parameters = context.getParameters();
//            parameters.add("followRedirects", "false");
//            parameters.add("truststorePath", trustStoreFilename);
//            parameters.add("truststorePassword", trustStorePasswd);
//            parameters.add("keystorePath", keyStoreFilename);
//            parameters.add("keystorePassword", keyStorePasswd);
//
//            Client  client = new Client(Protocol.HTTPS);
//
//            client.setContext( context );
////            Client client = studentServiceClient.getClient();
//
//            Request request = new Request( Method.GET, listProgramsURL );
//
//            //  Send the request and parse the result.
//            Response response = client.handle( request );
//            Status status = response.getStatus();
//            System.out.println( status );
//            Representation rep = response.getEntity();
//            if( Status.isSuccess( status.getCode() ))
//            {
//                SAXReader reader = new SAXReader();
//                Document document = reader.read(rep.getStream());
//
//                Map<String, String> namespaces = new HashMap<String, String>();
//                namespaces.put("x", "http://webservices.washington.edu/student/");
//                DefaultXPath progPath = new DefaultXPath("//x:DegreeAuditProgram");
//                DefaultXPath titlePath = new DefaultXPath( "x:DegreeAuditProgramTitle" );
//                DefaultXPath extraPath = new DefaultXPath( "x:DegreeAuditProgramURI" );
//                DefaultXPath campusPath = new DefaultXPath( "x:Campus" );
//                DefaultXPath abbrevPath = new DefaultXPath( "x:MajorAbbreviation" );
//                DefaultXPath degreeLevelPath = new DefaultXPath( "x:DegreeLevel" );
//                DefaultXPath degreeTypePath = new DefaultXPath( "x:DegreeType" );
//                DefaultXPath pathwayPath = new DefaultXPath( "x:Pathway" );
//                progPath.setNamespaceURIs(namespaces);
//                titlePath.setNamespaceURIs( namespaces );
//                extraPath.setNamespaceURIs( namespaces );
//                campusPath.setNamespaceURIs( namespaces );
//                abbrevPath.setNamespaceURIs( namespaces );
//                degreeLevelPath.setNamespaceURIs( namespaces );
//                degreeTypePath.setNamespaceURIs( namespaces );
//                pathwayPath.setNamespaceURIs( namespaces );
//
//                List<?> nodes = progPath.selectNodes( document );
//
//                HashMap<String,AuditProgramInfo> map = new  HashMap<String,AuditProgramInfo>();
//                for( Object child : nodes )
//                {
//
//                    Node titleNode = titlePath.selectSingleNode( child );
//                    Node extraNode = extraPath.selectSingleNode( child );
//                    Node campusNode = campusPath.selectSingleNode( extraNode );
//                    Node abbrevNode = abbrevPath.selectSingleNode( extraNode );
//                    Node degreeLevelNode = degreeLevelPath.selectSingleNode( extraNode );
//                    Node degreeTypeNode = degreeTypePath.selectSingleNode( extraNode );
//                    Node pathwayNode = pathwayPath.selectSingleNode( extraNode );
//                    String title = titleNode.getText();
//                    if( title.endsWith( " - Seattle" ))
//                    {
//                        title = title.substring( 0, title.length() - " - Seattle".length() );
//                    }
//                    else if( title.endsWith( " - Tacoma" ))
//                    {
//                        title = title.substring( 0, title.length() - " - Seattle".length() );
//                    }
//                    else if( title.endsWith( " - Bothell" ))
//                    {
//                        title = title.substring( 0, title.length() - " - Seattle".length() );
//                    }
//                    String campus = campusNode.getText();
//                    String abbrev = abbrevNode.getText();
//                    String degreeLevel = degreeLevelNode.getText();
//                    String degreeType = degreeTypeNode.getText();
//                    String pathway = pathwayNode.getText();
//
////                    System.out.printf( "\n%s %s %s", abbrev, campus, title );
//                    AuditProgramInfo info = new AuditProgramInfo();
//                    info.setProgramId( abbrev );
//                    info.setProgramTitle(title);
//                    info.setCampus( campus );
//                    info.setDegreeLevel(degreeLevel);
//                    info.setDegreeType(degreeType);
//                    info.setPathway( pathway );
//                    map.put(abbrev, info);
//                }
//                List<AuditProgramInfo> list = new ArrayList<AuditProgramInfo>( map.values() );
////                Collections.sort( list );
//
//                return list;
//            }
//            else
//            {
//                StringBuilder sb = new StringBuilder();
//                InputStream in = rep.getStream();
//                int c = 0;
//                while( ( c = in.read() ) != -1 )
//                {
//                    sb.append( (char) c );
//                }
//                throw new Exception( sb.toString() );
//            }
//        }
//        catch( Exception e )
//        {
//            logger.error( e );
//            throw new OperationFailedException( "something bad happened", e );
//        }
//    }
}