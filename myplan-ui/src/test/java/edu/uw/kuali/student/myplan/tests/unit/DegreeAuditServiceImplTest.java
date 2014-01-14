package edu.uw.kuali.student.myplan.tests.unit;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.infc.AuditReport;
import org.kuali.student.myplan.audit.service.DegreeAuditService;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import static org.kuali.student.myplan.audit.service.DegreeAuditServiceConstants.AUDIT_TYPE_KEY_HTML;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:degree-audit-test-context.xml"})
@Transactional
public class DegreeAuditServiceImplTest {


    @Resource
    DegreeAuditService degreeAuditService = null;

    public DegreeAuditService getDegreeAuditService() {
        return degreeAuditService;
    }

    public void setDegreeAuditService(DegreeAuditService degreeAuditService) {
        this.degreeAuditService = degreeAuditService;
    }

    @Test
    public void intellijIsJustShortOfPerfect() {
        String courseCd = "PSYCH 2XX01   ";
        DeconstructedCourseCode courseCode = getCourseDivisionAndNumber(courseCd);
        System.out.println(courseCode.getSubject());
        System.out.println(courseCode.getNumber());
    }

    @Test
    public void requestDegreeAudit() {
        if (true) return;
        try {
            DegreeAuditService degreeAuditService = getDegreeAuditService();
            String regid = "0";
            String programId = "0CHEM  0011";
//            String programId = "0HIST  0011";
            String auditTypeKey = "blah";
            ContextInfo context = new ContextInfo();

            long start = System.currentTimeMillis();
            System.out.println("ugh");
            AuditReportInfo report = degreeAuditService.runAudit(regid, programId, auditTypeKey, context);
            String auditID = report.getAuditId();

            // TODO: service only returns audittext field for new requests, pending requests don't have this field
            // asked Susan Archdeacon to add that field to all responses.

            String message = null;
            while (true) {
                StatusInfo info = degreeAuditService.getAuditRunStatus(auditID, context);
                String temp = info.getMessage();
                if (!temp.equals(message)) {
                    message = temp;
                    long elapsed = System.currentTimeMillis() - start;
                    System.out.println(message + " " + elapsed);
                }
                if (info.getIsSuccess()) break;
            }
        } catch (Exception e) {
            System.out.println("ugh");

        }
    }

    @Test
    public void runRecentAuditList() {
//        if( true ) return;

        try {
            String regId = "730FA4DCAE3411D689DA0004AC494FFE";
            Date startDate = new Date();
            Date endDate = new Date();
            ContextInfo context = new ContextInfo();
            DegreeAuditService degreeAuditService = getDegreeAuditService();
            List<AuditReportInfo> list = degreeAuditService.getAuditsForStudentInDateRange(regId, startDate, endDate, null);

            for (AuditReportInfo info : list) {
                System.out.println(info.getProgramTitle());
            }
        } catch (Exception e) {
            Assert.fail("ugh");
        }
    }

    //@Test
    public void getAuditReport() {
        String auditID = "2012042713461525";
        auditID = "2012061115531178";
        DegreeAuditService degreeAuditService = getDegreeAuditService();
//        degreeAuditService.setCourseLinkTemplateStyle( CourseLinkBuilder.LINK_TEMPLATE.TEST );

        ContextInfo zero = new ContextInfo();
        try {
            AuditReport report = degreeAuditService.getAuditReport(auditID, AUDIT_TYPE_KEY_HTML, zero);
            InputStream in = report.getReport().getDataSource().getInputStream();

            File target = new File("auditreport.html");
            FileOutputStream out = new FileOutputStream(target);

            int c = 0;
            while ((c = in.read()) != -1) {
                out.write((char) c);
            }
            in.close();
            out.close();

            System.out.println("argh");

        } catch (DoesNotExistException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvalidParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MissingParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OperationFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public DeconstructedCourseCode getCourseDivisionAndNumber(String courseCode) {
        String subject = null;
        String number = null;
        String activityCd = null;
        if (courseCode.matches(CourseSearchConstants.FORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.COURSE_CODE_WITH_SECTION_REGEX)) {
            activityCd = courseCode.substring(courseCode.lastIndexOf(" "), courseCode.length()).trim();
            courseCode = courseCode.substring(0, courseCode.lastIndexOf(" ")).trim();
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.UNFORMATTED_COURSE_CODE_REGEX)) {
            String[] splitStr = courseCode.toUpperCase().split(CourseSearchConstants.SPLIT_DIGITS_ALPHABETS);
            subject = splitStr[0].trim();
            number = splitStr[1].trim();
        } else if (courseCode.matches(CourseSearchConstants.UNFORMATTED_COURSE_PLACE_HOLDER_REGEX)) {
            int size = courseCode.length();
            int splitIndex = size - 3;
            subject = courseCode.substring(0, splitIndex).trim();
            number = courseCode.substring(splitIndex, size).toLowerCase();
        }
        return new DeconstructedCourseCode(subject, number, activityCd);
    }

}