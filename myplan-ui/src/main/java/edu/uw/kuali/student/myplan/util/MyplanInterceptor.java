package edu.uw.kuali.student.myplan.util;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/27/12
 * Time: 10:50 PM
 * To change this template use File | Settings | File Templates.
 */
public class MyplanInterceptor implements HandlerInterceptor {

    private final Logger logger = Logger.getLogger(HandlerInterceptor.class);

    private StudentServiceClient studentServiceClient;

    @Autowired
    private UserSessionHelper userSessionHelper;


    private Map<String, List<String>> viewSwsResourceMapping;

    public void setStudentServiceClient(StudentServiceClient studentServiceClient) {
        this.studentServiceClient = studentServiceClient;
    }

    private static final String SWS_URL_PARAM = "uw.studentservice.url";

    private final String USER_AGENT = "User-Agent";

    private static final String MESSAGE_BANNER_PARAM = "ksap.application.banner.message";

    private final String BROWSER_INCOMPATIBLE = "/myplan/browserIncompatible";

    private final String USER_UNAUTHORIZED = "/myplan/unauthorized";

    private final String LAST_STATUS_CHECK_TIME = "LAST_STATUS_CHECK_TIME";

    private final String MESSAGE_BANNER_TEXT = "messageBannerText";

    private final String BANNER_MESSAGE_LOCATION = ConfigContext.getCurrentContextConfig().getProperty(MESSAGE_BANNER_PARAM);

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

        //Set Banner Message if exists
        setBannerMessage(request);

        //Add page level warning based on services status
        addServiceStatusForView(request);

        return true;
    }

    /**
     * sets the Banner message if one exists
     *
     * @param request
     * @return
     */
    private void setBannerMessage(HttpServletRequest request) {
        if (StringUtils.hasText(BANNER_MESSAGE_LOCATION) && request.getSession().getAttribute(MESSAGE_BANNER_TEXT) == null) {
            StringBuffer sb = new StringBuffer();
            try {
                FileInputStream fileInputStream = new FileInputStream(BANNER_MESSAGE_LOCATION);
                DataInputStream in = new DataInputStream(fileInputStream);
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String strLine;
                while ((strLine = br.readLine()) != null) {
                    sb = sb.append(strLine);
                }
                in.close();
            } catch (Exception e) {
                logger.error("Could not write the Banner Message" + e.getMessage());
            }
            if (StringUtils.hasText(sb)) {
                request.setAttribute(MESSAGE_BANNER_TEXT, sb.toString());
            }
        }
    }

    /**
     * checks if all the services associated to the view are running
     *
     * @param viewId
     * @return
     */
    private boolean getServicesStatus(String viewId) {
        List<String> services = viewSwsResourceMapping.get(viewId);
        for (String service : services) {
            if (!studentServiceClient.connectionStatus(ConfigContext.getCurrentContextConfig().getProperty(SWS_URL_PARAM) + service)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Adds a service warning on to the view if one exists
     *
     * @param request
     */
    private void addServiceStatusForView(HttpServletRequest request) {
        boolean serviceStatus = true;
        String viewId = request.getParameter("viewId");
        if (viewSwsResourceMapping.containsKey(viewId)) {
            Object requestAttribute = request.getSession().getAttribute(viewId);
            Date currentTime = new Date(System.currentTimeMillis());
            Long expiredTime = (Long) request.getSession().getAttribute(LAST_STATUS_CHECK_TIME);
            if (requestAttribute == null || (requestAttribute != null && expiredTime != null && currentTime.after(new Date(expiredTime.longValue())))) {
                serviceStatus = getServicesStatus(viewId);
                request.getSession().setAttribute(viewId, serviceStatus);
                request.getSession().setAttribute(LAST_STATUS_CHECK_TIME, System.currentTimeMillis() + STATUS_CHECK_INTERVAL_MILLIS);
            } else {
                serviceStatus = (Boolean) requestAttribute;
            }
        }
        if (!serviceStatus) {
            String[] params = {};
            GlobalVariables.getMessageMap().putWarningForSectionId(viewId, PlanConstants.ERROR_TECHNICAL_PROBLEMS, params);
        }
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
        if (!getUserSessionHelper().isAdviser()) {
            try {
                String systemKey = getUserSessionHelper().getStudentExternalIdentifier();
            } catch (DataRetrievalFailureException drfe) {
                logger.info("UNAUTHORIZED Access: " + GlobalVariables.getUserSession().getPerson().getPrincipalId());
                request.getRequestDispatcher(USER_UNAUTHORIZED).forward(request, response);
                return;
            }
        }
        /*If a adviser comes from sample Plan to myplan using myplan url with out student regId as req param*/
        else {
            try {
                String studentNumber = getUserSessionHelper().getStudentNumber();
            } catch (RuntimeException e) {
                logger.info("UNAUTHORIZED Access: " + GlobalVariables.getUserSession().getPerson().getPrincipalId());
                request.getRequestDispatcher(USER_UNAUTHORIZED).forward(request, response);
                return;
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<String, List<String>> getViewSwsResourceMapping() {
        return viewSwsResourceMapping;
    }

    public void setViewSwsResourceMapping(Map<String, List<String>> viewSwsResourceMapping) {
        this.viewSwsResourceMapping = viewSwsResourceMapping;
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
}
