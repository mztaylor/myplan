package edu.uw.kuali.student.myplan.util;

import edu.uw.kuali.student.lib.client.studentservice.StudentServiceClient;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.sampleplan.util.SamplePlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.util.constants.ProgramServiceConstants;
import org.kuali.student.r2.lum.program.dto.MajorDisciplineInfo;
import org.kuali.student.r2.lum.program.service.ProgramService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 12/27/13
 * Time: 9:11 AM
 * To change this template use File | Settings | File Templates.
 */
public class SamplePlanInterceptor implements HandlerInterceptor {
    private final Logger logger = Logger.getLogger(HandlerInterceptor.class);

    @Autowired
    private UserSessionHelper userSessionHelper;

    private final String USER_AGENT = "User-Agent";

    private static final String MESSAGE_BANNER_PARAM = "ksap.application.banner.message";

    private final String BROWSER_INCOMPATIBLE = "/samplePlan/browserIncompatible";

    private final String USER_UNAUTHORIZED = "/samplePlan/unauthorized";

    private final String MESSAGE_BANNER_TEXT = "messageBannerText";

    private final String BANNER_MESSAGE_LOCATION = ConfigContext.getCurrentContextConfig().getProperty(MESSAGE_BANNER_PARAM);

    private final String HOST_NAME = "hostName";

    private final String IE7_AGENT = "MSIE 7.0;";

    private final String IE6_AGENT = "MSIE 6.0;";

    private transient PermissionService permissionService;

    private transient String ADVISE_NM_CODE;

    private transient List<String> advisePermNames;

    private transient ProgramService programService;

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

        isAuthorized();

        //Set Banner Message if exists
        setBannerMessage(request);

        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        if (GlobalVariables.getUserSession().retrieveObject(PlanConstants.SESSION_KEY_IS_ADVISER_MANAGE_PLAN) == null) {
            request.getRequestDispatcher(USER_UNAUTHORIZED).forward(request, response);
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Object o, Exception e) throws Exception {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    /**
     * Authorization verified if user has Adviser affiliation
     *
     * @return
     */
    private boolean isAuthorized() {
        boolean authorized = false;
        if (GlobalVariables.getUserSession().retrieveObject(PlanConstants.SESSION_KEY_IS_ADVISER_MANAGE_PLAN) == null) {
            UserSession session = GlobalVariables.getUserSession();
            getPermissionService();
            for (String adviseNm : advisePermNames) {
                if (getPermissionService().hasPermission(session.getPrincipalId(), ADVISE_NM_CODE, adviseNm.trim())) {
                    authorized = true;

                    /*Setting session key for manage a plan authorization*/
                    session.addObject(PlanConstants.SESSION_KEY_IS_ADVISER_MANAGE_PLAN, true);

                    /*Adding majors to UserSession*/
                    try {
                        List<MajorDisciplineInfo> majorDisciplineInfos = getProgramService().getMajorDisciplinesByIds(new ArrayList<String>(), SamplePlanConstants.CONTEXT_INFO);
                        if (!CollectionUtils.isEmpty(majorDisciplineInfos)) {
                            session.addObject(PlanConstants.SESSION_KEY_ADVISER_MAJORS, majorDisciplineInfos);
                        }
                    } catch (Exception e) {
                        logger.error("Could not get majors Disciplines for adviser: " + session.getPrincipalId(), e);
                    }

                    break;
                }
            }
        } else {
            authorized = true;
        }
        return authorized;
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

    public synchronized PermissionService getPermissionService() {
        if (permissionService == null) {

            ADVISE_NM_CODE = ConfigContext.getCurrentContextConfig().getProperty("myplan.advise.namespacecode");
            advisePermNames = Arrays.asList(ConfigContext.getCurrentContextConfig().getProperty("myplan.advise.permissionname").split(","));

            permissionService = KimApiServiceLocator.getPermissionService();
        }

        return this.permissionService;
    }

    public void setPermissionService(PermissionService permissionService) {
        this.permissionService = permissionService;
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

    public ProgramService getProgramService() {
        if (programService == null) {
            programService = (ProgramService)
                    GlobalResourceLoader.getService(new QName(ProgramServiceConstants.PROGRAM_NAMESPACE, "ProgramService"));
        }
        return programService;
    }

    public void setProgramService(ProgramService programService) {
        this.programService = programService;
    }

}
