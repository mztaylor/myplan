package org.kuali.student.myplan.audit;

import org.junit.Test;
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
    public void requestDegreeAudit() {
        try
        {
            DegreeAuditService degreeAuditService = getDegreeAuditService();
            String studentId = "0";
            String programId = "0MATH  0011";
            String auditTypeKey = "blah";
            ContextInfo context = new ContextInfo();

            AuditReportInfo report = degreeAuditService.runAudit( studentId, programId, auditTypeKey, context );
            String auditID = report.getAuditId();

            // TODO: service only returns audittext field for new requests, pending requests don't have this field
            // asked Susan Archdeacon to add that field to all responses.
            if( auditID == null )
            {
                auditID = "2012031413361642";
            }

            while( true )
            {
                StatusInfo info = degreeAuditService.getAuditRunStatus(auditID, context);
                System.out.println( info.getMessage() );
                if( info.getIsSuccess() ) break;
            }
            System.out.println( "ugh" );
        }
        catch( Exception e )
        {
            System.out.println("ugh");

        }
    }
}
