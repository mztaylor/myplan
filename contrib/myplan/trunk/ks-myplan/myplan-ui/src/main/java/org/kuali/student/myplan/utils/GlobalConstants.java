package org.kuali.student.myplan.utils;

import org.kuali.rice.core.api.config.property.ConfigContext;

/**
 * Created by IntelliJ IDEA.
 * User: dbmc
 * Date: 7/1/14
 * Time: 2:55 PM
 *
 * A file to hold constants available to all parts of the application.
 */
public class GlobalConstants {

    // base part of application url, for example the student in http://student/myplan/...
    public static final String APP_CODE_PARAM = "app.code";
    public static final String SLN_URL_PARAM = "ks.myplan.externalizable.sln.url";
    public static final String MYPLAN_APP_CODE = ConfigContext.getCurrentContextConfig().getProperty(APP_CODE_PARAM);
    public static final String MYPLAN_SLN_URL = ConfigContext.getCurrentContextConfig().getProperty(SLN_URL_PARAM);

    public static final String MYPLAN_ADVISER = "adviser";

    public static final String NON_STUDENT_ROLE = "NON-STUDENT";
    public static final String APPLICANT_ROLE = "APPLICANT";
    public static final String STUDENT_ROLE = "STUDENT";
    public static final String ADVISER_ROLE = "ADVISER";
    public static final String MYPLAN_VIEW_COMPONENT_TEMPLATE_NAME = "View Myplan Component";
    public static final String AUTHORIZED_TO_VIEW = "authorizedToView";
}