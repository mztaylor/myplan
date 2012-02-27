package org.kuali.student.myplan.service;

import static org.kuali.student.myplan.service.uAchieveReportStatus.*;
import static org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants.*;

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

    @Override
    public AuditReportInfo runAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String runAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public StatusInfo getAuditRunStatus(@WebParam(name = "auditId") String auditId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public AuditReportInfo getAuditReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        if(AUDIT_TYPE_KEY_DEFAULT.equals( auditTypeKey )) {
            return getDARSReport( auditId );
        }
        if (AUDIT_TYPE_KEY_HTML.equals(auditTypeKey)) {
            return getHTMLReport(auditId);
        }
        if (AUDIT_TYPE_KEY_DEFAULT.equals(auditTypeKey)) {
            return getDARSReport(auditId);
        }
        throw new InvalidParameterException( "auditTypeKey: " + auditTypeKey );
    }

    public AuditReportInfo getDARSReport(String auditId) {
        JobQueueRunLoader jqrl = getJobQueueRunLoader();
        JobQueueRun run = jqrl.loadJobQueueRun(auditId);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        List<JobQueueOut> outList = (List<JobQueueOut>) run.getJobQueueOuts();
        for (JobQueueOut out : outList) {
            String dar = out.getDarout();
            pw.println( dar );
        }
        String html = sw.toString();
        AuditReportInfo auditReportInfo = new AuditReportInfo();
        AuditDataSource dataSource = new AuditDataSource(html, auditId);
        DataHandler handler = new DataHandler(dataSource);
        auditReportInfo.setAuditId(auditId);
        auditReportInfo.setReport(handler);
        return auditReportInfo;

    }

    public AuditReportInfo getHTMLReport( String auditId ) {
        JobQueueRunLoader jqrl = getJobQueueRunLoader();
        JobQueueRun run = jqrl.loadJobQueueRun(auditId);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        pw.printf("<div>Student: <b>%s</b></div>\n", run.getStuno());
        pw.printf("<div>Degree: <b>%s</b> [%s]</div>\n", run.getWebtitle(), run.getDprog());

        pw.println("<ul>");

        for (JobQueueReq req : run.getJobQueueReqs()) {
            if ("".equals(req.getRname())) continue;
            if ("H".equals(req.getHidden())) continue;
            String satisfied = req.getSatisfied();
            if (TEXTREQ.same(satisfied)) continue;
            pw.println();
            pw.println("<li>");
            pw.println(req.getPsname());

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
                reqText = reqText.replaceAll("\\s+", " ").trim();
                if (reqText.length() > 0) {
                    pw.println("<div> " + reqText + "</div>");
                }
                {
                    float reqHrs = req.getReqhrs().floatValue();
                    float gotHrs = req.getGothrs().floatValue();
                    float needHrs = req.getNeedhrs().floatValue();
                    float ipHrs = req.getIphrs().floatValue();
                    if (reqHrs > 1.0) {
                        pw.printf("<div>credits: %.1f  completed: %.1f  in-progress: %.1f  remaining: %.1f</div>\n", reqHrs, gotHrs, ipHrs, needHrs);
                    }
                }
                uAchieveReportStatus ugh = uAchieveReportStatus.translate(satisfied);
                pw.println("<div> status: " + ugh.getMessage() + " </div>");
            }


            {
                BigDecimal reqGPA = req.getReqgpa();
                BigDecimal gotGPA = req.getGotgpa();
//                BigDecimal needGPA = req.getNeedgpa();
                if (reqGPA.floatValue() > 0.0f) {
                    pw.printf("<div> GPA: %.1f  earned: %.1f </div>\n", reqGPA, gotGPA);
                }
            }
            pw.println("<ul>");

            for (JobQueueSubreq subreq : req.getJobQueueSubreqs()) {
                if ("H".equals(subreq.getHidden())) continue;

                StringBuilder buf = new StringBuilder();
                for (JobQueueSubreqText text : subreq.getJobQueueSubreqTexts()) {
                    String temp = text.getText();
                    buf.append(temp);
                    buf.append(" ");
                }
                String subReqText = buf.toString();
                subReqText = subReqText.replace("*", " ");
                subReqText = subReqText.replace("_", " ");
                subReqText = subReqText.replaceAll("\\s+", " ").trim();
                if (subReqText.length() > 0) {
                    pw.println("<li>");
                    pw.println("<div>" + subReqText + "</div>");
                    {
                        float reqHrs = subreq.getReqhrs().floatValue();
                        float gotHrs = subreq.getGothrs().floatValue();
                        float needHrs = subreq.getNeedhrs().floatValue();
                        float ipHrs = subreq.getIphrs().floatValue();
                        if (reqHrs > 1.0f && reqHrs < 999.0f) {
                            pw.printf("<div>credits: %.1f  completed: %.1f  in-progress: %.1f  remaining: %.1f</div>\n", reqHrs, gotHrs, ipHrs, needHrs);
                        }

                        BigDecimal reqGPA = subreq.getReqgpa();
                        BigDecimal gotGPA = subreq.getGotgpa();
                        BigDecimal needGPA = subreq.getNeedgpa();
                        if (reqGPA.floatValue() > 0.0f) {
                            pw.printf("<div>GPA: %.1f  earned: %.1f</div>\n", reqGPA, gotGPA);
                        }

                        int reqCourses = subreq.getReqct();
                        int gotCourses = subreq.getGotct();
                        int needCourses = subreq.getNeedct();

                        if (reqCourses > 0) {
                            pw.printf("<div>courses: %d  taken: %d  remaining: %d</div>\n", reqCourses, gotCourses, needCourses);

                        }
                        String subsatisfied = subreq.getSatisfied();
                        uAchieveReportStatus ugh = uAchieveReportStatus.translate(satisfied);
                        pw.println("<div> status: " + ugh.getMessage() + " </div>");


                        pw.println("<ul class=\"courses\">");
                        // Acceptable courses
                        List<JobQueueAccept> acceptList = subreq.getJobQueueAccepts();
                        for (JobQueueAccept accept : acceptList) {
                            String course = accept.getCourse();
                            String dept = accept.getDept();
                            String number = accept.getCrsno();
                            String pseudo = accept.getPseudoFlag();
                            String logic = accept.getLogic();
                            String required = accept.getRequired();

                            if (number.length() == 0) continue;
                            if (dept.startsWith("**")) continue;

                            String cluid = "fffa4b6d-e91c-4d25-90d1-d9b6b13cd567";
                            String title = "CALCULUS WITH ANALYTIC GEOMETRY I";
//                            ResolverService.CourseLink link = resolver.getCourseLink(dept, number);
//                            String cluid = link.cluid;
//                            String title = link.title;
                            String host = "https://uwksdev01.cac.washington.edu";

                            String href = host + "/myplan-embedded-dev/myplan/inquiry?methodToCall=start&dataObjectClassName=org.kuali.student.myplan.course.dataobject.CourseDetails&courseId=" + cluid;
                            pw.println("<li>");
                            pw.printf("<a href=\"%s\" title=\"%s\" target=\"_blank\">%s %s</a>\n", href, title, dept, number);
                            pw.println("</li>");
                        }
                        pw.println("</ul>");

                    }
                    pw.println("</li>");
                }

            }
            pw.println("</ul>");
            pw.println("</li>");

        }
        pw.println("</ul>");




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
    public String runWhatIfAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "academicPlan") LearningPlan academicPlan, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
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
}