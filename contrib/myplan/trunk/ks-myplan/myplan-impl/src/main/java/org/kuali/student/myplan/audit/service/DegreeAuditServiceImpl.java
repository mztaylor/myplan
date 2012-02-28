package org.kuali.student.myplan.audit.service;

import static org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.dto.AuditReportSummaryInfo;
import org.kuali.student.myplan.audit.service.model.*;
import org.kuali.student.myplan.model.AuditDataSource;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uachieve.apis.audit.JobQueueRun;
import uachieve.apis.audit.JobQueueSubreq;
import uachieve.apis.audit.JobQueueOut;
import uachieve.apis.audit.JobQueueReq;
import uachieve.apis.audit.JobQueueReqText;
import uachieve.apis.audit.JobQueueSubreqText;
import uachieve.apis.audit.JobQueueAccept;
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

    public AuditReportInfo getHTMLReport( String auditId ) throws OperationFailedException {
        try {
            Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            Velocity.init();

            Template template = Velocity.getTemplate("org/kuali/student/myplan/audit/service/report.vm");
            VelocityContext context = new VelocityContext();
            Report report = getAuditReport(auditId);
            context.put("report", report );

            StringWriter sw = new StringWriter();
            template.merge(context, sw);
            String html = sw.toString();
            AuditReportInfo auditReportInfo = new AuditReportInfo();
            AuditDataSource dataSource = new AuditDataSource(html, auditId);
            DataHandler handler = new DataHandler(dataSource);
            auditReportInfo.setAuditId(auditId);
            auditReportInfo.setReport(handler);
            return auditReportInfo;
        }
        catch( Exception e )
        {
            throw new OperationFailedException( "velocity error", e );
        }
    }

    public Report getAuditReport(String auditid) {

        Report report = new Report();

        JobQueueRunLoader jqrl = getJobQueueRunLoader();
        JobQueueRun run = jqrl.loadJobQueueRun(auditid);


        report.setStudentName(run.getStuno());
        report.setWebTitle(run.getWebtitle());
        report.setDegreeProgram(run.getDprog());

        Section section = new Section();
        report.addSection(section);

        for (JobQueueReq jqr : run.getJobQueueReqs()) {
            if ("".equals(jqr.getRname())) continue;
            if ("H".equals(jqr.getHidden())) continue;

            String satisfied = jqr.getSatisfied();
            boolean textual = "T".equals(satisfied);
            if (textual) {
                String rname = jqr.getRname();
//                if( "UTEXT".equals( rname ))
//                {
//                    // Set caption for existing Section
//                    section.setCaption( "University Requirements" );
//                    // Create new section
//                    section = new Section();
//                    report.addSection(section);
//                }

                // Hard coded until we figure out generic solution
                if ("A&SGENTXT".equals(rname)) {
                    // Set caption for existing Section
                    section.setCaption("General Education Requirements");
                    // Create new section
                    section = new Section();
                    report.addSection(section);
                } else if ("MAJORTEXT".equals(rname)) {
                    // Set caption for existing Section
                    section.setCaption("Departmental Requirements");
                    // Create new section
                    section = new Section();
                    report.addSection(section);
                }

                // Skip to next JobQueueReq
                continue;
            }

            Requirement requirement = new Requirement();
            requirement.setStatus(satisfied);
            section.addRequirement(requirement);

            {
                StringBuilder buf = new StringBuilder();
                for (JobQueueReqText text : jqr.getJobQueueReqTexts()) {
                    String temp = text.getText();
                    buf.append(temp);
                    buf.append(" ");
                }
                String reqText = buf.toString();
                reqText = reqText.replace("*", " ");
                reqText = reqText.replace("_", " ");
                reqText = reqText.replaceAll("\\s+", " ").trim();
                if (reqText.length() > 0) {
                    requirement.setCaption(reqText);
                }
            }
            {
                float reqHrs = jqr.getReqhrs().floatValue();
                float gotHrs = jqr.getGothrs().floatValue();
                float needHrs = jqr.getNeedhrs().floatValue();
                float ipHrs = jqr.getIphrs().floatValue();
                if (reqHrs > 1.0f && reqHrs < 999.0f) {
                    Credits credits = new Credits();
                    credits.setRequired(reqHrs);
                    credits.setEarned(gotHrs);
                    credits.setInprogress(ipHrs);
                    credits.setNeeds(needHrs);
                    requirement.setCredits(credits);
                }
            }

            {
                float reqGPA = jqr.getReqgpa().floatValue();
                float gotGPA = jqr.getGotgpa().floatValue();

                if (reqGPA > 0.0f) {
                    GPA gpa = new GPA();
                    gpa.setRequired(reqGPA);
                    gpa.setEarned(gotGPA);
                    requirement.setGPA(gpa);
                }
            }

            {
                int reqCourses = jqr.getReqct();
                int gotCourses = jqr.getGotct();
                int needCourses = jqr.getNeedct();

                if (reqCourses > 0) {
                    Count count = new Count();
                    count.setRequired(reqCourses);
                    count.setEarned(gotCourses);
                    count.setNeeds(needCourses);
                    requirement.setCount(count);

                }
            }
            for (JobQueueSubreq jqsr : jqr.getJobQueueSubreqs()) {
                if ("H".equals(jqsr.getHidden())) continue;

                Subrequirement subrequirement = new Subrequirement();
                requirement.addSubrequirement(subrequirement);
                String subsatisfied = jqsr.getSatisfied();
                subrequirement.setStatus(subsatisfied);

                {
                    StringBuilder buf = new StringBuilder();
                    for (JobQueueSubreqText text : jqsr.getJobQueueSubreqTexts()) {
                        String temp = text.getText();
                        buf.append(temp);
                        buf.append(" ");
                    }

                    String subReqText = buf.toString();
                    subReqText = subReqText.replace("*", " ");
                    subReqText = subReqText.replace("_", " ");
                    subReqText = subReqText.replaceAll("\\s+", " ").trim();
                    if (subReqText.length() > 0) {
                        subrequirement.setCaption(subReqText);
                    }
                }
                {
                    float reqHrs = jqsr.getReqhrs().floatValue();
                    float gotHrs = jqsr.getGothrs().floatValue();
                    float needHrs = jqsr.getNeedhrs().floatValue();
                    float ipHrs = jqsr.getIphrs().floatValue();
                    if (reqHrs > 1.0f && reqHrs < 999.0f) {
                        Credits credits = new Credits();
                        credits.setRequired(reqHrs);
                        credits.setEarned(gotHrs);
                        credits.setInprogress(ipHrs);
                        credits.setNeeds(needHrs);
                        subrequirement.setCredits(credits);
                    }
                }

                {
                    float reqGPA = jqr.getReqgpa().floatValue();
                    float gotGPA = jqr.getGotgpa().floatValue();

                    if (reqGPA > 0.0f) {
                        GPA gpa = new GPA();
                        gpa.setRequired(reqGPA);
                        gpa.setEarned(gotGPA);
                        subrequirement.setGPA(gpa);
                    }
                }

                {
                    int reqCourses = jqsr.getReqct();
                    int gotCourses = jqsr.getGotct();
                    int needCourses = jqsr.getNeedct();

                    if (reqCourses > 0) {
                        Count count = new Count();
                        count.setRequired(reqCourses);
                        count.setEarned(gotCourses);
                        count.setNeeds(needCourses);
                        subrequirement.setCount(count);
                    }
                }

                // Acceptable courses
                List<JobQueueAccept> acceptList = jqsr.getJobQueueAccepts();
                for (JobQueueAccept accept : acceptList) {
                    String dept = accept.getDept();
                    String number = accept.getCrsno();

                    if (number.length() == 0) continue;
                    if (dept.startsWith("**")) continue;
                    Course course = new Course();
                    course.setDept(dept);
                    course.setNumber(number);
                    subrequirement.addCourse(course);
                }
            }
        }

        return report;
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