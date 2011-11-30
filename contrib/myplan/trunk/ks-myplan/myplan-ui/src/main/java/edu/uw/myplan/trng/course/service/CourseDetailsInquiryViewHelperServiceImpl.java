package edu.uw.myplan.trng.course.service;

import java.util.Map;
import javax.xml.namespace.QName;

import org.kuali.student.common.exceptions.*;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kns.inquiry.KualiInquirableImpl;

public class CourseDetailsInquiryViewHelperServiceImpl extends KualiInquirableImpl {

    private transient CourseService courseService;

    @Override
    public CourseInfo retrieveDataObject(Map fieldValues) {
        String courseId = (String) fieldValues.get("courseId");

        CourseInfo course = null;
        try {
            course = getCourseService().getCourse(courseId);
        } catch (DoesNotExistException e) {
            throw new RuntimeException(String.format("Course [%s] not found.", courseId), e);
        } catch (InvalidParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (MissingParameterException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (OperationFailedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (PermissionDeniedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return course;
    }

    protected CourseService getCourseService() {
        if(courseService == null) {
            courseService = (CourseService)GlobalResourceLoader.getService(new QName(CourseServiceConstants.COURSE_NAMESPACE,"CourseService"));
        }
        return this.courseService;
    }
}