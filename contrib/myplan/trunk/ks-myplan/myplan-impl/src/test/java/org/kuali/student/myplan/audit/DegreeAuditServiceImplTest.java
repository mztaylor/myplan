package org.kuali.student.myplan.audit;

import org.junit.Test;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:degree-audit-test-context.xml"})
@Transactional
public class DegreeAuditServiceImplTest {

    @Resource
    DegreeAuditService degreeAuditService = null;

    public DegreeAuditService getDegreeAuditService() {
        return degreeAuditService;
    }

    public void setDegreeAuditService( DegreeAuditService degreeAuditService ) {
        this.degreeAuditService = degreeAuditService;
    }

    @Test
    public void intellijIsJustShortOfPerfect() {
        String courseCd = "PSYCH 2XX01   ";
        String[] splitStr = courseCd.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
        for( String ugh : splitStr ) {
            System.out.println( ugh );
        }
    }
    @Test
    public void requestDegreeAudit() {
        if (true) return;
        try
        {
            DegreeAuditService degreeAuditService = getDegreeAuditService();
            String studentId = "0";
            String programId = "0CHEM  0011";
//            String programId = "0HIST  0011";
            String auditTypeKey = "blah";
            ContextInfo context = new ContextInfo();

            long start = System.currentTimeMillis();
            System.out.println("ugh");
            AuditReportInfo report = degreeAuditService.runAudit( studentId, programId, auditTypeKey, context );
            String auditID = report.getAuditId();

            // TODO: service only returns audittext field for new requests, pending requests don't have this field
            // asked Susan Archdeacon to add that field to all responses.
//            if( auditID == null )
//            {
//                auditID = "2012031413361642";
//            }

            String message = null;
            while( true )
            {
                StatusInfo info = degreeAuditService.getAuditRunStatus(auditID, context);
                String temp = info.getMessage();
                if( !temp.equals( message ))
                {
                    message = temp;
                    long elapsed = System.currentTimeMillis() - start;
                    System.out.println( message + " " + elapsed );
                }
                if( info.getIsSuccess() ) break;
            }
        }
        catch( Exception e )
        {
            System.out.println("ugh");

        }
    }

    @Test
    public void runRecentAuditList() {
        if( true ) return;

        try
        {
//        String studentId = "100190981";
        String studentId = "100190977";
        Date startDate = new Date();
        Date endDate = new Date();
        ContextInfo context = new ContextInfo();
        DegreeAuditService degreeAuditService = getDegreeAuditService();
        List<AuditReportInfo> list = degreeAuditService.getAuditsForStudentInDateRange
        ( studentId,  startDate, endDate,null);

            for(AuditReportInfo info : list)
            {
                      System.out.println( info.getProgramId());
            }
        }
        catch( Exception e )
        {
            Assert.fail("ugh");
        }
    }
}