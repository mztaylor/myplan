package org.kuali.student.myplan.service;


import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.dto.AuditReportSummaryInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.model.AuditDataSource;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uachieve.apis.audit.*;
import uachieve.apis.audit.jobqueueloader.JobQueueRunLoader;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DegreeAuditServiceImpl implements DegreeAuditService {

    private JobQueueRunLoader jobQueueRunLoader;

    public JobQueueRunLoader getJobQueueRunLoader()
    {
    	return jobQueueRunLoader;
    }

    public void setJobQueueRunLoader( JobQueueRunLoader loader )
    {
    	this.jobQueueRunLoader = loader;
    }

    public void displayReqsEtc(@WebParam(name = "jobid") String jobid) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
    }

    String join( Object... args )
    {
        StringBuilder sb = new StringBuilder();
        for( Object item : args )
        {
            sb.append( item.toString() );
            sb.append( "\t" );
        }
        return sb.toString();
    }

    String translateSatified( String ugh )
    {
        if( "C".equals( ugh )) return "Complete";
        if( "F".equals( ugh )) return "Complete, but forced";
        if( "I".equals( ugh )) return "Complete using in progress courses";
        if( "T".equals( ugh )) return "Text requirement";
        if( "X".equals( ugh )) return "Requirement not used";
        if( "N".equals( ugh )) return "Not complete";
        if( "".equals( ugh )) return "";
        return "bad code '" + ugh + "'for satified";

    }

    @Override
    public AuditReportInfo runAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String runAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo getAuditRunStatus(@WebParam(name = "auditId") String auditId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AuditReportInfo getAuditReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        JobQueueRunLoader jqrl = getJobQueueRunLoader();
        JobQueueRun run = jqrl.loadJobQueueRun(auditId);

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter( sw );
        pw.printf("stuno: %s\n", run.getStuno());
        pw.printf("dprog: %s\n", run.getDprog());
        pw.printf("webtitle: %s\n", run.getWebtitle());

        for (JobQueueReq req : run.getJobQueueReqs()) {
            if ("".equals(req.getRname())) continue;
            if ("H".equals(req.getHidden())) continue;
            String satisfied = req.getSatisfied();
            if ("T".equals(satisfied)) continue;
            pw.println();
            pw.printf("req: %s (%s)\n", req.getRname(), req.getPsname());

            {
                StringBuilder buf = new StringBuilder();
                for (JobQueueReqText text : req.getJobQueueReqTexts()) {
                    String temp = text.getText();
                    buf.append(temp);
                    buf.append(" ");
                }
                String reqText = buf.toString();
                reqText = reqText.replace("*", " ");
                reqText = reqText.replace("_", " ");
//            reqText = reqText.replace( "\n", " " );
//            reqText = reqText.replace( "\t", " " );
                reqText = reqText.replaceAll("\\s+", " ").trim();
                if (reqText.length() > 0) {
                    pw.println("text: " + reqText);
                }
                {
                    float reqHrs = req.getReqhrs().floatValue();
                    float gotHrs = req.getGothrs().floatValue();
                    float needHrs = req.getNeedhrs().floatValue();
                    float ipHrs = req.getIphrs().floatValue();
                    if (reqHrs > 1.0) {
                        pw.printf("credits: %.1f  completed: %.1f  in-progress: %.1f  remaining: %.1f\n", reqHrs, gotHrs, ipHrs, needHrs);
                    }
                }
                satisfied = translateSatified(satisfied);
                pw.println("status: " + satisfied);
            }


            {
                BigDecimal reqGPA = req.getReqgpa();
                BigDecimal gotGPA = req.getGotgpa();
                BigDecimal needGPA = req.getNeedgpa();
                if (reqGPA.floatValue() > 0.0f) {
                    pw.printf("GPA: %.1f  earned: %.1f\n", reqGPA, gotGPA);
                }
            }

//    		System.out.println ( "*** Job Queue Req Texts" );
            for (JobQueueSubreq subreq : req.getJobQueueSubreqs()) {
//                System.out.println ( "$\t hidden: " + subreq.getHidden() );
                if ("H".equals(subreq.getHidden())) continue;


//        		System.out.println ( "label: " + subreq.getLabel() );
                StringBuilder buf = new StringBuilder();
                for (JobQueueSubreqText text : subreq.getJobQueueSubreqTexts()) {
                    String temp = text.getText();
                    buf.append(temp);
                    buf.append(" ");
                }
                String subReqText = buf.toString();
                subReqText = subReqText.replace("*", " ");
                subReqText = subReqText.replace("_", " ");
//                subReqText = subReqText.replace( "\n", " " );
//                subReqText = subReqText.replace( "\t", " " );
                subReqText = subReqText.replaceAll("\\s+", " ").trim();
                if (subReqText.length() > 0) {
                    pw.println("<ul>");
                    pw.println("\n\tsubreq: " + subReqText);
                    {
                        float reqHrs = subreq.getReqhrs().floatValue();
                        float gotHrs = subreq.getGothrs().floatValue();
                        float needHrs = subreq.getNeedhrs().floatValue();
                        float ipHrs = subreq.getIphrs().floatValue();
                        if (reqHrs > 1.0f && reqHrs < 999.0f) {
                            pw.printf("\tcredits: %.1f  completed: %.1f  in-progress: %.1f  remaining: %.1f\n", reqHrs, gotHrs, ipHrs, needHrs);
                        }

                        BigDecimal reqGPA = subreq.getReqgpa();
                        BigDecimal gotGPA = subreq.getGotgpa();
                        BigDecimal needGPA = subreq.getNeedgpa();
                        if (reqGPA.floatValue() > 0.0f) {
                            pw.printf("\tGPA: %.1f  earned: %.1f\n", reqGPA, gotGPA);
                        }

                        int reqCourses = subreq.getReqct();
                        int gotCourses = subreq.getGotct();
                        int needCourses = subreq.getNeedct();

                        if (reqCourses > 0) {
                            pw.printf("\tcourses: %d  taken: %d  remaining: %d\n", reqCourses, gotCourses, needCourses);

                        }
                        String subsatisfied = subreq.getSatisfied();
                        pw.println("\tstatus: " + translateSatified(subsatisfied));


                        // Acceptable courses
                        List<JobQueueAccept> acceptList = subreq.getJobQueueAccepts();
                        for (JobQueueAccept accept : acceptList) {
                            String course = accept.getCourse();
                            String dept = accept.getDept();
                            String number = accept.getCrsno();
                            String pseudo = accept.getPseudoFlag();
                            String logic = accept.getLogic();
                            String required = accept.getRequired();

//                            out.printf( "\t\t%s %s %s %s %s %s\n", course, dept, number, pseudo, logic, required );
                            if (number.length() > 0) {
                                pw.printf("\t\t%s %s\n", dept, number);
                            }
                        }
                    }
                }

            }
        }
        String html = sw.toString();
        AuditReportInfo auditReportInfo = new AuditReportInfo();
        AuditDataSource dataSource = new AuditDataSource( html, auditId );
        DataHandler handler = new DataHandler( dataSource );
        auditReportInfo.setAuditId( auditId );
        auditReportInfo.setReport( handler );
        return auditReportInfo;
    }

    @Override
    public AuditReportSummaryInfo getAuditSummaryReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public List<String> getAuditIdsForStudentInDateRange(@WebParam(name = "studentId") String studentId, @WebParam(name = "startDate") Date startDate, @WebParam(name = "endDate") Date endDate, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AuditReportInfo runWhatIfAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "academicPlan") LearningPlan academicPlan, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String runWhatIfAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "academicPlan") LearningPlan academicPlan, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AuditReportInfo runEmptyAudit(@WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String runEmptyAuditAsync(@WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}