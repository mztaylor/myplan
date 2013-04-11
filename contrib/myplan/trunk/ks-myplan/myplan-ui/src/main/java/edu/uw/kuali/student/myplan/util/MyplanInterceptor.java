package edu.uw.kuali.student.myplan.util;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.dataobject.ServicesStatusDataObject;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetAddress;
import java.util.Date;

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

    private final String BROWSER_INCOMPATIBLE = "/myplan/browserIncompatible";

    private final String USER_UNAUTHORIZED = "/myplan/unauthorized";

    private final String LAST_STATUS_CHECK_TIME = "LAST_STATUS_CHECK_TIME";

    private final String HOST_NAME = "hostName";

    private final String IE7_AGENT = "MSIE 7.0;";

    private final String IE6_AGENT = "MSIE 6.0;";

    private final int STATUS_CHECK_INTERVAL_MILLIS = 300000;

    /**
     * Adds the services status to the request session.
     * Redirects to the Browser Incompatible page if User agents are IE7 or IE6
     * Adds  the name of the Host to the request attribute.(eg: uwkseval01)
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        ServicesStatusDataObject lastSwsServiceStatus = (ServicesStatusDataObject) request.getSession().getAttribute(CourseSearchConstants.SWS_SERVICES_STATUS);
        Date currentTime = new Date(System.currentTimeMillis());
        Long expiredTime = (Long) request.getSession().getAttribute(LAST_STATUS_CHECK_TIME);
        if (lastSwsServiceStatus == null || (lastSwsServiceStatus != null && expiredTime != null && currentTime.after(new Date(expiredTime.longValue())))) {
            request.getSession().setAttribute(CourseSearchConstants.SWS_SERVICES_STATUS, getServicesStatus());
            request.getSession().setAttribute(LAST_STATUS_CHECK_TIME, System.currentTimeMillis() + STATUS_CHECK_INTERVAL_MILLIS);
        }

        String userAgent = request.getHeader(USER_AGENT);
        if (userAgent.contains(IE6_AGENT) || userAgent.contains(IE7_AGENT)) {
            request.getRequestDispatcher(BROWSER_INCOMPATIBLE).forward(request, response);
            return false;
        }

        String hostName = InetAddress.getLocalHost().getHostName();
        if (hostName.contains(".")) {
            hostName = hostName.substring(0, hostName.indexOf("."));
        }
        request.setAttribute(HOST_NAME, hostName.toUpperCase());

        return true;
    }

    /**
     * Used to populate the ServicedStatusDataObject with the statuses of all the services
     *
     * @return
     */
    private ServicesStatusDataObject getServicesStatus() {
        boolean isAcademicCalenderServiceRunning = studentServiceClient.connectionStatus(ACADEMIC_CALENDER_SERVICE_URL);
        if (!isAcademicCalenderServiceRunning) {
            logger.info("Academic Calender Service is Down");
        }
        boolean isCourseOfferingServiceRunning = studentServiceClient.connectionStatus(COURSE_OFFERING_SERVICE_URL);
        if (!isCourseOfferingServiceRunning) {
            logger.info("Course Offering Service is Down");
        }
        boolean isAcademicRecordServiceRunning = false;
        if (studentServiceClient.connectionStatus(ACADEMIC_RECORD_SERVICE_URL_1) && studentServiceClient.connectionStatus(ACADEMIC_RECORD_SERVICE_URL_2)) {
            isAcademicRecordServiceRunning = true;
        }
        if (!isAcademicRecordServiceRunning) {
            logger.info("Academic Record Service is Down");
        }
        boolean isAuditServiceRunning = studentServiceClient.connectionStatus(AUDIT_SERVICE_URL);
        if (!isAuditServiceRunning) {
            logger.info("Audit Service is Down");
        }
        return new ServicesStatusDataObject(isAcademicCalenderServiceRunning, isAcademicRecordServiceRunning, isCourseOfferingServiceRunning, isAuditServiceRunning);
    }

    /**
     * Redirected to UnAuthorized page if not a student or an adviser
     *
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (!UserSessionHelper.isAdviser()) {
            try {
                String systemKey = UserSessionHelper.getStudentSystemKey();
            } catch (DataRetrievalFailureException drfe) {
                logger.info("UNAUTHORIZED Access: " + GlobalVariables.getUserSession().getPerson().getPrincipalId());
                request.getRequestDispatcher(USER_UNAUTHORIZED).forward(request, response);
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
