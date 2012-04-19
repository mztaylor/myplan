package org.kuali.student.myplan.audit.service;

import static org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants.*;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.log4j.Logger;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.darsws.AuditRequestSvc;
import org.kuali.student.myplan.audit.service.darsws.AuditRequestSvcSoap;
import org.kuali.student.myplan.audit.service.darsws.MPAuditResponse;
import org.kuali.student.myplan.audit.service.model.*;
import org.kuali.student.myplan.audit.service.model.Requirement;
import org.kuali.student.myplan.audit.service.model.Subrequirement;
import org.kuali.student.myplan.audit.service.model.AuditDataSource;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.util.constants.CourseOfferingServiceConstants;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uachieve.apis.audit.*;
import uachieve.apis.audit.dao.JobQueueListDao;
import uachieve.apis.audit.dao.JobQueueRunDao;
import uachieve.apis.audit.jobqueueloader.JobQueueRunLoader;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import javax.xml.namespace.QName;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;

import org.kuali.student.common.search.dto.*;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DegreeAuditServiceImpl implements DegreeAuditService {
    private final Logger logger = Logger.getLogger(DegreeAuditServiceImpl.class);
    private transient LuService luService;
    private transient CourseOfferingService courseOfferingService;
    private JobQueueRunLoader jobQueueRunLoader;

    private JobQueueRunDao jobQueueRunDao;

    private JobQueueListDao jobQueueListDao;


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
            MPAuditResponse response = port.mpRequestAudit(systemKey, programId, lineNo, systemKey, origin);
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
                Thread.currentThread().sleep( 200 );
                if( System.currentTimeMillis() > giveup )
                {
                    throw new TimeoutException( "giving up after " + (timeout / 1000 ) + " seconds" );
                }
            }
        } catch (Exception e) {
            logger.error(e);
        }
        if (AUDIT_TYPE_KEY_DEFAULT.equals(auditTypeKey)) {
            return getDARSReport(auditId);
        }
        if (AUDIT_TYPE_KEY_HTML.equals(auditTypeKey)) {
            return getHTMLReport(auditId);
        }
        if (AUDIT_TYPE_KEY_DEFAULT.equals(auditTypeKey)) {
            return getDARSReport(auditId);
        }
        throw new InvalidParameterException("auditTypeKey: " + auditTypeKey);
    }

    public AuditReportInfo getDARSReport(String auditId) {
        JobQueueRunLoader jqrl = getJobQueueRunLoader();
        JobQueueRun run = jqrl.loadJobQueueRun(auditId);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);

        List<JobQueueOut> outList = (List<JobQueueOut>) run.getJobQueueOuts();
        for (JobQueueOut out : outList) {
            String dar = out.getDarout();
            pw.println(dar);
        }
        String html = sw.toString();
        AuditReportInfo auditReportInfo = new AuditReportInfo();
        AuditDataSource dataSource = new AuditDataSource(html, auditId);
        DataHandler handler = new DataHandler(dataSource);
        auditReportInfo.setAuditId(auditId);
        auditReportInfo.setReport(handler);
        return auditReportInfo;

    }

    public AuditReportInfo getHTMLReport(String auditId) throws OperationFailedException {
        try {
            Velocity.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
            Velocity.setProperty("classpath.resource.loader.class", ClasspathResourceLoader.class.getName());
            Velocity.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.NullLogSystem");
            Velocity.init();

            Template template = Velocity.getTemplate("org/kuali/student/myplan/audit/service/report.vm");
            VelocityContext context = new VelocityContext();
            Report report = getAuditReport(auditId);
            context.put("report", report);

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

    public Report getAuditReport(String auditid) {

        Report report = new Report();

        JobQueueRunLoader jqrl = getJobQueueRunLoader();
        JobQueueRun run = jqrl.loadJobQueueRun(auditid);

        report.setStudentName(run.getStuno());
        report.setWebTitle(run.getWebtitle());
        report.setDegreeProgram(run.getDprog());

        Timestamp runDate = run.getRundate();
        SimpleDateFormat formatter = new SimpleDateFormat("MMM d, yyyy h:mm a");
        report.setDatePrepared(formatter.format(runDate));

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

                    String courseID = getCourseID( dept.trim(), number );
                    if( courseID != null )
                    {
                        courseAcceptable.setCluId(courseID);
                        {
                            SearchRequest searchRequest = new SearchRequest("myplan.course.info");
                            searchRequest.addParam("courseID", courseAcceptable.getCluid());
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

                }
                List<JobQueueCourse> takenList = jqsr.getJobQueueCourses();
                for (JobQueueCourse taken : takenList) {
                    CourseTaken temp = new CourseTaken();
                    String dept = "";
                    String number = "";

                    String course = taken.getCourse().trim();
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
                    temp.setDept(dept);
                    temp.setNumber(number);
                    temp.setGrade(taken.getGpa().toString());
                    temp.setDescription(taken.getCtitle());
                    temp.setCredits(taken.getCredit().toString());
                    temp.setQuarter(taken.getEditYt());
                    boolean inProgress = "I".equals(taken.getIp());
                    temp.setInProgress(inProgress);
                    String courseID = getCourseID( dept, number );
                    temp.setCluid( courseID );

                    subrequirement.addCourseTaken(temp);
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


    public String hardcodedStudentID( String studentId ) {
        // Used by devs when logged in as admin
        if( "admin".equals( studentId )) return "100190981";
        // Used by Jill for demos
        if( "jjulius".equals( studentId )) return "101167127";
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


    public String getCourseID( String dept, String number )
    {
        try {
            SearchRequest searchRequest = new SearchRequest("myplan.course.getcluid");
            searchRequest.addParam("number", number);
            searchRequest.addParam("subject", dept.trim());
            SearchResult searchResult = getLuService().search(searchRequest);
            for (SearchResultRow row : searchResult.getRows()) {
                String courseID = getCellValue(row, "lu.resultColumn.cluId");
                return courseID;
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}