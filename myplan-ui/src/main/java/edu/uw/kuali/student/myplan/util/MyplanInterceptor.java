package edu.uw.kuali.student.myplan.util;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.comment.CommentConstants;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/27/12
 * Time: 10:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyplanInterceptor implements HandlerInterceptor {

    private static final String SWS_URL_PARAM = "uw.studentservice.url";

    private final Logger logger = Logger.getLogger(HandlerInterceptor.class);

    private StudentServiceClient studentServiceClient;

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    private final String USER_AGENT = "User-Agent";

    private final String ACADEMIC_CALENDER_SERVICE_URL = ConfigContext.getCurrentContextConfig().getProperty(SWS_URL_PARAM) + "/v4/public/term/current";

    private final String COURSE_OFFERING_SERVICE_URL = ConfigContext.getCurrentContextConfig().getProperty(SWS_URL_PARAM) + "/v4/public/section";

    private final String ACADEMIC_RECORD_SERVICE_URL_1 = ConfigContext.getCurrentContextConfig().getProperty(SWS_URL_PARAM) + "/v4/enrollment";

    private final String ACADEMIC_RECORD_SERVICE_URL_2 = ConfigContext.getCurrentContextConfig().getProperty(SWS_URL_PARAM) + "/v4/registration/";

    private final String AUDIT_SERVICE_URL = ConfigContext.getCurrentContextConfig().getProperty(SWS_URL_PARAM) + "/v5/degreeaudit";

    private final String BROWSER_INCOMPATIBLE = "/student/myplan/browserIncompatible";

    private final String USER_UNAUTHORIZED = "/student/myplan/unauthorized";
    
    private final String STUDENT="STDNT";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        boolean isAcademicCalenderServiceRunning = studentServiceClient.connectionStatus(ACADEMIC_CALENDER_SERVICE_URL);
        boolean isCourseOfferingServiceRunning = studentServiceClient.connectionStatus(COURSE_OFFERING_SERVICE_URL);
        boolean isAcademicRecordServiceRunning = false;
        if (studentServiceClient.connectionStatus(ACADEMIC_RECORD_SERVICE_URL_1) && studentServiceClient.connectionStatus(ACADEMIC_RECORD_SERVICE_URL_2)) {
            isAcademicRecordServiceRunning = true;
        }
        boolean isAuditServiceRunning = studentServiceClient.connectionStatus(AUDIT_SERVICE_URL);

        request.setAttribute(CourseSearchConstants.IS_ACADEMIC_CALENDER_SERVICE_UP, isAcademicCalenderServiceRunning);
        request.setAttribute(CourseSearchConstants.IS_COURSE_OFFERING_SERVICE_UP, isCourseOfferingServiceRunning);
        request.setAttribute(CourseSearchConstants.IS_ACADEMIC_RECORD_SERVICE_UP, isAcademicRecordServiceRunning);
        request.setAttribute(DegreeAuditConstants.IS_AUDIT_SERVICE_UP, isAuditServiceRunning);
        String userAgent = request.getHeader(USER_AGENT);
        if (userAgent.contains("MSIE 7.0;") || userAgent.contains("MSIE 6.0;")) {
            response.sendRedirect(BROWSER_INCOMPATIBLE);
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        /*If you not an adviser and  if you are not a  student then you dont have access to myplan*/
        if (!UserSessionHelper.isAdviser())  {
            try {
                String systemKey = UserSessionHelper.getAuditSystemKey();
            } catch (DataRetrievalFailureException drfe) {
                logger.info("UNAUTHORIZED Access: " + GlobalVariables.getUserSession().getPerson().getPrincipalId());
                response.sendRedirect(USER_UNAUTHORIZED);
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
