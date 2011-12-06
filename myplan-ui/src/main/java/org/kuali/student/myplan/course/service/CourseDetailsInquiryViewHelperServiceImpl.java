package org.kuali.student.myplan.course.service;

import java.util.List;
import java.util.Map;
import javax.xml.namespace.QName;

import org.kuali.student.common.exceptions.*;
import org.kuali.student.core.statement.dto.StatementTreeViewInfo;
import org.kuali.student.core.statement.service.StatementService;
import org.kuali.student.core.statement.util.StatementServiceConstants;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;
import org.kuali.student.myplan.course.dataobject.CourseDetails;

public class CourseDetailsInquiryViewHelperServiceImpl extends KualiInquirableImpl {

    private transient CourseService courseService;
    private transient StatementService statementService;

    @Override
    public CourseDetails retrieveDataObject(Map fieldValues) {
        //  Get the courseId from the query parameters.
        String courseId = (String) fieldValues.get("courseId");

        CourseInfo course = null;
        try {
            course = getCourseService().getCourse(courseId);
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (Exception e) {
            throw new RuntimeException("Query failed.", e);
        }

        CourseDetails courseDetails = new CourseDetails();

        courseDetails.setCourseId(course.getId());
        courseDetails.setCode(course.getCode());
        courseDetails.setCourseDescription(course.getDescr().getFormatted());
        courseDetails.setCredit("1-10");
        courseDetails.setCourseTitle(course.getCourseTitle());

        courseDetails.setCampusLocations(course.getCampusLocations());

        List<StatementTreeViewInfo> statements = null;
        try {
            statements = getCourseService().getCourseStatements(courseId, null, null);
        } catch (DoesNotExistException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvalidParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MissingParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OperationFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PermissionDeniedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        //for (StatementTreeViewInfo stvi : statements) {
        //    stvi.
        //}

        return courseDetails;
    }

    protected CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    protected StatementService getStatementService() {
        this.statementService = (StatementService) GlobalResourceLoader
                .getService(new QName(StatementServiceConstants.PREREQUISITE_STATEMENT_TYPE, "StatementService"));
        return this.statementService;
    }

}