package org.kuali.student.myplan.audit.service;

import static org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.model.Count;
import org.kuali.student.myplan.audit.service.model.Credits;
import org.kuali.student.myplan.audit.service.model.GPA;
import org.kuali.student.myplan.audit.service.model.Report;
import org.kuali.student.myplan.audit.service.model.Requirement;
import org.kuali.student.myplan.audit.service.model.Section;
import org.kuali.student.myplan.audit.service.model.Subrequirement;
import org.kuali.student.myplan.audit.service.model.CourseAcceptable;
import org.kuali.student.myplan.audit.service.model.CourseTaken;
import org.kuali.student.myplan.model.AuditDataSource;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.util.constants.CourseOfferingServiceConstants;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uachieve.apis.audit.JobQueueRun;
import uachieve.apis.audit.JobQueueSubreq;
import uachieve.apis.audit.JobQueueOut;
import uachieve.apis.audit.JobQueueReq;
import uachieve.apis.audit.JobQueueReqText;
import uachieve.apis.audit.JobQueueSubreqText;
import uachieve.apis.audit.JobQueueAccept;
import uachieve.apis.audit.dao.JobQueueRunDao;
import uachieve.apis.audit.jobqueueloader.JobQueueRunLoader;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kuali.student.common.search.dto.*;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DegreeAuditServiceImpl implements DegreeAuditService {

    private transient LuService luService;
    private transient CourseOfferingService courseOfferingService;
    private JobQueueRunLoader jobQueueRunLoader;

    private JobQueueRunDao jobQueueRunDao;

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

    public JobQueueRunLoader getJobQueueRunLoader()
    {
    	return jobQueueRunLoader;
    }

    public void setJobQueueRunLoader( JobQueueRunLoader loader )
    {
    	this.jobQueueRunLoader = loader;
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
            Velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
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

        Timestamp runDate = run.getRundate();
        SimpleDateFormat formatter = new SimpleDateFormat( "MMM d, yyyy h:mm a");
        report.setDatePrepared(formatter.format(runDate));
//        report.setDatePrepared(runDate.toString());

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

//                LuService luService = getLuService();

                // Acceptable courses
                List<JobQueueAccept> acceptList = jqsr.getJobQueueAccepts();
                for (JobQueueAccept accept : acceptList) {
                    String dept = accept.getDept();
                    if (dept.startsWith("**")) continue;
                    String number = accept.getCrsno();
                    if (number.length() == 0) continue;

                    CourseAcceptable courseAcceptable = new CourseAcceptable();
                    courseAcceptable.setDept(dept);
                    courseAcceptable.setNumber(number);
                    subrequirement.addCourseAcceptable(courseAcceptable);

                    {
                        SearchRequest searchRequest = new SearchRequest("myplan.course.getcluid");
                        searchRequest.addParam("number", number);
                        searchRequest.addParam("subject", dept.trim());
                        try {

                            SearchResult searchResult = getLuService().search(searchRequest);
                            for (SearchResultRow row : searchResult.getRows()) {
                                String courseId = getCellValue(row, "lu.resultColumn.cluId");
                                courseAcceptable.setCluId( courseId );
//                            CourseOfferingService courseOfferingService = getCourseOfferingService();

//                            try {
//
//                                ContextInfo contextInfo = new ContextInfo();
//                                CourseOfferingInfo coi = courseOfferingService.getCourseOffering(courseId, contextInfo);
//
//                                String title = coi.getCourseTitle();
//                                courseAcceptable.setDescription( title );
//
//                            } catch ( DoesNotExistException e) {
//                                throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
//                            } catch (Exception e) {
//                                throw new RuntimeException("Query failed.", e);
//                            }
                            break;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                    {
                        SearchRequest searchRequest = new SearchRequest("myplan.course.info");
                        searchRequest.addParam("courseID", courseAcceptable.getCluid() );
                        try {

                            SearchResult searchResult = getLuService().search(searchRequest);
                            for (SearchResultRow row : searchResult.getRows()) {
                                String name = getCellValue(row, "course.name");
                                courseAcceptable.setDescription(name);

                                break;
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                }
                {
                    subrequirement.addCourseTaken( new CourseTaken() );
                    subrequirement.addCourseTaken(new CourseTaken());
                    subrequirement.addCourseTaken(new CourseTaken());
                    subrequirement.addCourseTaken(new CourseTaken());
                }
            }
        }

        return report;
    }

    public String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }


//    @Override
//    public AuditReportSummaryInfo getAuditSummaryReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

    @Override
    public List<AuditReportInfo> getAuditsForStudentInDateRange(@WebParam(name = "studentId") String studentId,
                                                                @WebParam(name = "startDate") Date startDate,
                                                                @WebParam(name = "endDate") Date endDate,
                                                                @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {

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