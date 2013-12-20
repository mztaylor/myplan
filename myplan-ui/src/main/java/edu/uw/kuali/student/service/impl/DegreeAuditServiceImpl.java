package edu.uw.kuali.student.service.impl;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.PlanHelperImpl;
import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.log4j.Logger;
import org.dom4j.*;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.dom4j.tree.DefaultText;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.audit.service.model.AuditDataSource;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.AtpHelper.YearTerm;
import org.kuali.student.myplan.plan.util.OrgHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.util.CourseLinkBuilder;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.core.organization.dto.OrgInfo;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.service.CourseService;
import org.kuali.student.r2.lum.util.constants.CourseServiceConstants;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Status;
import org.restlet.ext.net.HttpClientHelper;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
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
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT;
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.*;


@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DegreeAuditServiceImpl implements DegreeAuditService {

    private int TIMEOUT = 5 * 60 * 1000; // 5 minutes

    private String postAuditRequestURL = ConfigContext.getCurrentContextConfig().getProperty(DEGREE_AUDIT_SERVICE_URL);


//    public static void main(String[] args)
//            throws Exception {
//        DegreeAuditServiceImpl impl = new DegreeAuditServiceImpl();
//        String studentId = "D8D636BEB4CC482884420724BF152709";
//        String programId = "1BISMCS0011";
////        AuditReportInfo info = impl.runAudit(studentId, programId, null, null);
////        AuditReportInfo info = impl.getAuditReport("2012100110472750", "kauli.audit.type.default", CourseSearchConstants.CONTEXT_INFO);
//        AuditReportInfo info = impl.getHTMLReport("2013041811291367", null);
////        System.out.println(DegreeAuditServiceImpl.padfront("  1 2 "));
//    }


    private final Logger logger = Logger.getLogger(DegreeAuditServiceImpl.class);

    public static final ContextInfo CONTEXT_INFO = new ContextInfo();


    private JobQueueRunLoader jobQueueRunLoader;

    private JobQueueRunDao jobQueueRunDao;

    private JobQueueListDao jobQueueListDao;

    private DprogDao dprogDao;

    private StudentServiceClient studentServiceClient;

    private transient AcademicPlanService academicPlanService;

    private CourseService courseService;

    @Autowired
    CourseHelper courseHelper;

    @Autowired
    private PlanHelper planHelper;

    @Autowired
    private UserSessionHelper userSessionHelper;


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

    static public class DegreeAuditCourseRequest implements Cloneable {
        String curric;
        String credit;
        String campus;
        String number;
        String quarter;
        String year;
        String activity;

        public String toString() {
            return
                    "<Course>\n" +
                            "<CurriculumAbbreviation>" + curric.replace("&", "&amp;") + "</CurriculumAbbreviation>\n" +
                            "<MinimumTermCredit>" + credit + "</MinimumTermCredit>\n" +
                            "<CourseCampus>" + campus.replace("&", "&amp;") + "</CourseCampus>\n" +
                            "<CourseNumber>" + number.replace("&", "&amp;") + "</CourseNumber>\n" +
                            "<Quarter>" + quarter.replace("&", "&amp;") + "</Quarter>\n" +
                            "<SectionID>" + activity + "</SectionID>\n" +
                            "<Year>" + year.replace("&", "&amp;") + "</Year>\n" +
                            "</Course>\n";
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
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
    public String runWhatIfAuditAsync(@WebParam(name = "studentId") String studentId,
                                      @WebParam(name = "programId") String programId,
                                      @WebParam(name = "auditTypeKey") String auditTypeKey,
                                      @WebParam(name = "academicPlanId") String academicPlanId,
                                      @WebParam(name = "context") ContextInfo context)
            throws InvalidParameterException, MissingParameterException, OperationFailedException {

        String auditId = null;
        DegreeAuditRequest req = new DegreeAuditRequest(studentId, programId);
        int totalCredits = 0;
        int totalPlanned = 0;
        try {

            List<PlanItemInfo> planItems = getAcademicPlanService().getPlanItemsInPlan(academicPlanId, context);
            for (PlanItemInfo planItem : planItems) {
                boolean isCourse = PlanConstants.COURSE_TYPE.equalsIgnoreCase(planItem.getRefObjectType());
                boolean isPlanned = AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equalsIgnoreCase(planItem.getTypeKey());
                if (isCourse && isPlanned) {

                    String bucketType = BUCKET_IGNORE;
                    String section = "";
                    String secondaryActivity = "";
                    for (AttributeInfo attrib : planItem.getAttributes()) {
                        String key = attrib.getKey();
                        String value = attrib.getValue();
                        if (BUCKET.equals(key)) {
                            bucketType = value;
                        } else if (SECTION.equals(key)) {
                            section = value != null ? value : "";
                        } else if (SECONDARY_ACTIVITY.equals(key)) {
                            secondaryActivity = value != null ? value : "";
                        }
                    }

                    if (BUCKET_IGNORE.equals(bucketType)) continue;
                    String versionIndependentId = planItem.getRefObjectId();
                    String crossListedCourse = getPlanHelper().getCrossListedCourse(planItem.getAttributes());
                    try {
                        String latestCourseId = getCourseHelper().getVerifiedCourseId(versionIndependentId);
                        if (latestCourseId != null) {
                            CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(latestCourseId, crossListedCourse);

                            DegreeAuditCourseRequest course = new DegreeAuditCourseRequest();
                            course.curric = courseInfo.getSubjectArea().trim();
                            course.number = courseInfo.getCourseNumberSuffix().trim();
                            course.credit = String.valueOf(planItem.getCredit().intValue());
                            course.activity = section;
                            {
                                course.campus = "Seattle";
                                List<OrgInfo> campusList = OrgHelper.getOrgInfo(CourseSearchConstants.CAMPUS_LOCATION_ORG_TYPE, CourseSearchConstants.ORG_QUERY_SEARCH_BY_TYPE_REQUEST, CourseSearchConstants.ORG_TYPE_PARAM);

                                List<AttributeInfo> attributes = courseInfo.getAttributes();
                                String campusId = null;
                                for (AttributeInfo attributeInfo : attributes) {
                                    if (CourseSearchConstants.CAMPUS_LOCATION_COURSE_ATTRIBUTE.equals(attributeInfo.getKey())) {
                                        campusId = attributeInfo.getValue();
                                    }
                                }
                                if (StringUtils.hasText(campusId)) {
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
                            totalPlanned++;
                            //Adding new course request if a secondary activity exists
                            //NOTE: secondary activity courses should no pass credit
                            if (StringUtils.hasText(secondaryActivity)) {
                                DegreeAuditCourseRequest secondaryActivityCourse = (DegreeAuditCourseRequest) course.clone();
                                secondaryActivityCourse.credit = "0";
                                secondaryActivityCourse.activity = secondaryActivity;
                                req.courses.add(secondaryActivityCourse);
                            }

                            totalCredits = totalCredits + Integer.parseInt(course.credit);
                        }
                    } catch (Exception e) {
                        logger.warn("whatever", e);
                    }
                }

            }

            auditId = requestAudit(req);

        } catch (Exception e) {
            throw new OperationFailedException("error retrieving learning plan items", e);

        }

        try {
            if (academicPlanId != null) {
                // getting the learning plan for given academicPlanId
                LearningPlanInfo learningPlanInfo = getAcademicPlanService().getLearningPlan(academicPlanId, context);
                YearTerm lastYT = AtpHelper.getLastPlannedOrRegisteredTerm();

                //updating the learning plan with new attribute values.
                for (AttributeInfo attributeInfo : learningPlanInfo.getAttributes()) {
                    String key = attributeInfo.getKey();
                    if ("forCourses".equalsIgnoreCase(key)) {
                        attributeInfo.setValue(String.valueOf(totalPlanned));
                    } else if ("forCredits".equalsIgnoreCase(key)) {
                        attributeInfo.setValue(String.valueOf(totalCredits));
                    } else if ("forQuarter".equalsIgnoreCase(key)) {
                        attributeInfo.setValue(lastYT.toShortTermName());
                    } else if ("auditId".equalsIgnoreCase(key)) {
                        attributeInfo.setValue(auditId);
                    }
                }
                learningPlanInfo.setStateKey(PlanConstants.LEARNING_PLAN_ACTIVE_STATE_KEY);
                getAcademicPlanService().updateLearningPlan(learningPlanInfo.getId(), learningPlanInfo, context);
            }
        } catch (Exception e) {
            throw new OperationFailedException("error updating learning plan items", e);
        }

        return auditId;
    }

    private String requestAudit(DegreeAuditRequest req) throws OperationFailedException {
        try {

            String payload = req.toString();
            logger.info("REST HTTP POST");
            logger.info(postAuditRequestURL);
            logger.info(payload);

            HttpClientHelper client = getStudentServiceClient().getClient();

            Request request = new Request(Method.POST, postAuditRequestURL);

            StringRepresentation entity = new StringRepresentation(payload);
            request.setEntity(entity);

            Response response = new Response(request);
            client.handle(request, response);
            Representation rep = response.getEntity();

            Status status = response.getStatus();
            if (Status.isSuccess(status.getCode())) {
                SAXReader reader = new SAXReader();
                Document document = reader.read(rep.getStream());

                Map<String, String> namespaces = new HashMap<String, String>();
                namespaces.put("x", "http://webservices.washington.edu/student/");
                DefaultXPath jobid1Path = new DefaultXPath("//x:DegreeAudit/x:JobId");
                DefaultXPath jobid2Path = new DefaultXPath("//x:DegreeAudit/x:JobID");
                jobid1Path.setNamespaceURIs(namespaces);
                jobid2Path.setNamespaceURIs(namespaces);

                Node jobid1Node = jobid1Path.selectSingleNode(document);
                Node jobid2Node = jobid2Path.selectSingleNode(document);

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
                info.setSuccess(null);
                info.setMessage("audit request not found");
                break;
            case -1:
                info.setSuccess(false);
                info.setMessage("completed with errors");
                break;
            case 0:
                info.setSuccess(null);
                info.setMessage("not finished processing");
                break;
            case 1:
                info.setSuccess(true);
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
            if (info.getIsSuccess() != null) {
                return getHTMLReport(auditId, auditTypeKey, info.getIsSuccess());
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

    private AuditReportInfo getHTMLReport(String auditId, String auditTypeKey, boolean success) throws OperationFailedException {

        AuditReportInfo auditReportInfo = new AuditReportInfo();
        auditReportInfo.setAuditId(auditId);
        String answer = "";
        try {
            JobQueueRunHibernateDao dao = (JobQueueRunHibernateDao) getJobQueueRunDao();
            String sql = "SELECT report.report, run.stuno, run.dprog, run.webtitle FROM JobQueueRun run, JobQueueReport report  WHERE run.intSeqNo = report.jobqSeqNo AND run.jobid = ? AND run.reportType = 'HTM'";

            List list = dao.find(sql, new Object[]{auditId});
            Object[] item = (Object[]) list.get(0);

            String stunoX = (String) item[1];
            String dprog = (String) item[2];
            auditReportInfo.setProgramId(dprog);
            String webtitle = (String) item[3];
            //reason for adding dprog if wetitle is null is specified in jira - 2175
            auditReportInfo.setProgramTitle(StringUtils.hasText(webtitle) ? webtitle : dprog);

            if (success) {
                byte[] report = (byte[]) item[0];
                String garbage = new String(report);
                InputStream in = new ByteArrayInputStream(garbage.getBytes());

                SAXReader sax = new SAXReader();
                Document doc = sax.read(in);

                findClassLinkifyTextAndConvertCoursesToLinks(doc);

                String name = null;
                Text preparedFor = (Text) doc.selectSingleNode("//span[contains(@class,'prepared-for-name')]/text()");
                boolean isUserSession = getUserSessionHelper().isUserSession();
                String regId = null;
                if (isUserSession) {
                    regId = getUserSessionHelper().getStudentId();
                    name = getUserSessionHelper().getCapitalizedName(regId);
                } else {
                    String stuno = preparedFor.getText();
                    String syskey = convertMyPlanStunoToSDBSyskey(stuno);
                    Person person = getStudentServiceClient().getPersonBySysKey(syskey);
                    name = person.getFirstName() + " " + person.getLastName();
                    regId = person.getPrincipalId();
                }
                preparedFor.setText(name);


                boolean planAudit = false;
                String forCourses = "";
                String forCredits = "";
                String forQuarter = "";
                String preparedBy = "";
                List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(regId, LEARNING_PLAN_TYPE_PLAN_AUDIT, CONTEXT_INFO);
                for (LearningPlanInfo learningPlanInfo : learningPlanList) {
//                PlanAuditItem planAuditItem = new PlanAuditItem();
                    for (AttributeInfo attributeInfo : learningPlanInfo.getAttributes()) {
                        String key = attributeInfo.getKey();
                        String value = attributeInfo.getValue();
                        if ("forCourses".equalsIgnoreCase(key)) {
                            forCourses = value;
                        } else if ("forCredits".equalsIgnoreCase(key)) {
                            forCredits = value;
                        } else if ("forQuarter".equalsIgnoreCase(key)) {
                            forQuarter = value;
                        } else if ("auditId".equalsIgnoreCase(key)) {
                            if (auditId.equals(value)) {
                                planAudit = true;
                            }
                        } else if ("requestedBy".equalsIgnoreCase(key)) {
                            preparedBy = value;
                        }
                    }
                    if (planAudit) break;
                }

                if (preparedBy != null) {
                    Text node = (Text) doc.selectSingleNode("//span[contains(@class,'prepared-by')]/text()");
                    if (node != null) {
                        node.setText(preparedBy);
                    }
                }

                if (planAudit) {
                    {
                        Text node = (Text) doc.selectSingleNode("//span[contains(@class,'for-credits')]/text()");
                        if (node != null) {
                            node.setText(forCredits);
                        }
                    }

                    {
                        Text node = (Text) doc.selectSingleNode("//span[contains(@class,'for-courses')]/text()");
                        if (node != null) {
                            node.setText(forCourses);
                        }
                    }

                    {
                        Text node = (Text) doc.selectSingleNode("//span[contains(@class,'for-quarter')]/text()");
                        if (node != null) {
                            node.setText(forQuarter);
                        }
                    }
                }

                Element root = (Element) doc.selectSingleNode("/html/div");
                // Replace the placeholder text for auditid
                Attribute a = root.attribute("auditid");
                if (a != null) {
                    a.setValue(auditId);
                }
                answer = writeXML(root);
            }
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

    public static String writeXML(Node doc) throws Exception {
        OutputFormat format = new OutputFormat();
        format.setExpandEmptyElements(true);
        StringWriter sw = new StringWriter();
        XMLWriter writer = new XMLWriter(sw, format);

        writer.write(doc);
        return sw.toString();
    }


    private CourseLinkBuilder courseLinkBuilder = new CourseLinkBuilder();

    private void findClassLinkifyTextAndConvertCoursesToLinks(Document doc) {
        List list = doc.selectNodes("//*[contains(@class,'linkify')]");
        for (Object o : list) {
            Element element = (Element) o;
            String original = element.getText();
            original = scrubForHTML(original);
            if (original.length() == 0) continue;
            String attribs = element.attributeValue("class");
            if (attribs.contains("flatten")) {
                original = extractJustText(element);
                element.clearContent();
                element.add(new DefaultText(original));
            }
            String linkified = urlify(original);
            linkified = courseLinkBuilder.makeLinks(linkified);
            if (!original.equals(linkified)) {

                try {
                    linkified = "<span> " + linkified + " </span>";
                    Document sub = DocumentHelper.parseText(linkified);
                    Element root = sub.getRootElement();
                    element.clearContent();
                    element.add(root);
                } catch (DocumentException e1) {
                    logger.error(e1);
                }
            }
        }
    }

    private String extractJustText(Element parent) {
        ArrayList<String> list = new ArrayList<String>();
        extractJustText(parent, list);
        StringBuilder sb = new StringBuilder();
        for (String gosh : list) {
            sb.append(gosh);
            sb.append(' ');
        }
        String result = sb.toString();
        result = scrubForHTML(result);
        return result;
    }

    private String scrubForHTML(String result) {
        if (result == null) return "";
        result = result.replace('\n', ' ');
        result = result.replace('\t', ' ');
        result = result.replace("&", "&amp;");
        result = result.replace("<", "&lt;");
        result = result.replace(">", "&gt;");
        result = result.replaceAll(" ,", ",");
        result = result.replaceAll(",", ", ");
        result = result.replaceAll("\\s+", " ");
        result = result.trim();
        return result;
    }

    private void extractJustText(Element parent, List<String> list) {
        for (Object child : parent.content()) {
            if (child instanceof Element) {
                extractJustText((Element) child, list);
            } else if (child instanceof Text) {
                Text t = (Text) child;
                String u = t.getText();
                list.add(u);
            }
        }
    }


    private static String urlify(String initialText) {
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

    private boolean equals(Object victim, Object... list) {
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
    private String convertSDBSyskeyToMyPlanStuno(String syskey) {
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
    private String convertMyPlanStunoToSDBSyskey(String stuno) {
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

            String syskey = getUserSessionHelper().getExternalIdentifier(studentId);
            String stuno = convertSDBSyskeyToMyPlanStuno(syskey);

            logger.info("getAuditsForStudentInDateRange syskey " + syskey + "  stuno " + stuno);

            JobQueueRunDao runrun = getJobQueueRunDao();
            List<JobQueueRun> load = runrun.load(instid, instidq, instcd, stuno);

            for (JobQueueRun jqr : load) {
                String reportType = jqr.getReportType();
                boolean failedAudit = jqr.getComplete().equalsIgnoreCase("E");
                //Add only reports of type HTM and not failed audits, or failed audits if it is the most recent audit.
                if ("HTM".equals(reportType) && (!failedAudit || (failedAudit && list.size() == 0))) {
                    AuditReportInfo audit = new AuditReportInfo();
                    audit.setAuditId(jqr.getJobid());
                    audit.setReportType(DegreeAuditServiceConstants.AUDIT_TYPE_KEY_SUMMARY);
                    audit.setStudentId(stuno);
                    audit.setProgramId(jqr.getDprog().replace(" ", "$"));
                    audit.setProgramTitle(StringUtils.hasText(jqr.getWebtitle()) ? jqr.getWebtitle() : jqr.getDprog());
                    audit.setRunDate(jqr.getRundate());
                    audit.setRequirementsSatisfied("Unknown");
                    String ip = jqr.getIp();
                    audit.setWhatIfAudit("W".equals(ip));

                    list.add(audit);
                }
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
        YearTerm currentTermAndYear = AtpHelper.getCurrentYearTerm();
        int year = currentTermAndYear.getYear() - 10;
        StringBuffer termYear = new StringBuffer();
        termYear = termYear.append(year).append(currentTermAndYear.getTerm());
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

    @Override
    public String getAuditStatus(String studentId, String programId, String recentAuditId) {
        String syskey = getUserSessionHelper().getStudentExternalIdentifier();
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

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = new PlanHelperImpl();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }
}