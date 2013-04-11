package org.kuali.student.myplan.audit.service.mock;


import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.student.myplan.audit.dto.AuditProgramInfo;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants;
import org.kuali.student.myplan.audit.service.model.AuditDataSource;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uachieve.apis.audit.dao.JobQueueListDao;
import uachieve.apis.audit.dao.JobQueueRunDao;
import uachieve.apis.audit.jobqueueloader.JobQueueRunLoader;
import uachieve.apis.requirement.dao.DprogDao;

import javax.activation.DataHandler;
import javax.jws.WebParam;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Transactional(propagation = Propagation.REQUIRES_NEW)
public class DegreeAuditServiceMockImpl implements DegreeAuditService {

    private static final Log logger = LogFactory.getLog(DegreeAuditServiceMockImpl.class);

    //  These keep the spring bean definitions consistent between mock and real impls.
    public void setJobQueueRunDao(JobQueueRunDao jobQueueRunDao) {
    }

    public void setJobQueueRunLoader(JobQueueRunLoader loader) {
    }

    public void setJobQueueListDao(JobQueueListDao jobQueueListDao) {
    }

    public void setDprogDao(DprogDao dprogDao) {
    }


    @Override
    public AuditReportInfo runAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
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
    public AuditReportInfo getAuditReport(@WebParam(name = "auditId") String auditId,
                                          @WebParam(name = "auditTypeKey") String auditTypeKey,
                                          @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        InputStream in = this.getClass().getResourceAsStream("/txt/static_audit_content.txt");
        String html = null;
        try {
            html = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            logger.error("Could not read response file.", e);
        }

        AuditReportInfo auditReportInfo = new AuditReportInfo();

        AuditDataSource dataSource = new AuditDataSource(html, auditId);
        DataHandler handler = new DataHandler(dataSource);

        auditReportInfo.setAuditId(auditId);
        auditReportInfo.setReportType(auditTypeKey);
        auditReportInfo.setReport(handler);
        auditReportInfo.setProgramId("programId");
        auditReportInfo.setProgramTitle("programTitle");
        auditReportInfo.setRunDate(new java.util.Date());
        auditReportInfo.setReportContentTypeKey("html");
        auditReportInfo.setRequirementsSatisfied("Yes!");

        return auditReportInfo;
    }

//    @Override
//    public AuditReportSummaryInfo getAuditSummaryReport(@WebParam(name = "auditId") String auditId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

    @Override
    public List<AuditReportInfo> getAuditsForStudentInDateRange(@WebParam(name = "studentId") String studentId,
                                                                @WebParam(name = "startDate") Date startDate,
                                                                @WebParam(name = "endDate") Date endDate,
                                                                @WebParam(name = "context") ContextInfo context)
            throws InvalidParameterException, MissingParameterException, OperationFailedException {

        List<AuditReportInfo> list = new ArrayList<AuditReportInfo>();

        try {
            AuditReportInfo ari1 = getAuditReport("a1", DegreeAuditServiceConstants.AUDIT_TYPE_KEY_SUMMARY, new ContextInfo());
            AuditReportInfo ari2 = getAuditReport("a2", DegreeAuditServiceConstants.AUDIT_TYPE_KEY_SUMMARY, new ContextInfo());
            list.add(ari1);
            list.add(ari2);
        } catch (DoesNotExistException e) {
            throw new RuntimeException("Trouble creating fake audits.", e);
        }

        return list;
    }

    @Override
    public AuditReportInfo runWhatIfAudit(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "academicPlanId") String academicPlanId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String runWhatIfAuditAsync(@WebParam(name = "studentId") String studentId, @WebParam(name = "programId") String programId, @WebParam(name = "auditTypeKey") String auditTypeKey, @WebParam(name = "academicPlanId") String academicPlanId, @WebParam(name = "context") ContextInfo context) throws InvalidParameterException, MissingParameterException, OperationFailedException {
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
        List<AuditProgramInfo> auditProgramInfoList = new ArrayList<AuditProgramInfo>();

        try {
            AuditProgramInfo api0 = new AuditProgramInfo();
            api0.setProgramId("0api0");
            api0.setProgramTitle("Select a degree program or minor");
            AuditProgramInfo api1 = new AuditProgramInfo();
            api1.setProgramId("1api1");
            api1.setProgramTitle("Select a degree program or minor");
            AuditProgramInfo api2 = new AuditProgramInfo();
            api2.setProgramId("2api2");
            api2.setProgramTitle("Select a degree program or minor");
            auditProgramInfoList.add(api0);
            auditProgramInfoList.add(api1);
            auditProgramInfoList.add(api2);
        } catch (Exception e) {
            throw new RuntimeException("Trouble creating fake audit programs.", e);
        }

        return auditProgramInfoList;
    }

    public String getAuditStatus(String studentId, String programId, String recentAuditId) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


}