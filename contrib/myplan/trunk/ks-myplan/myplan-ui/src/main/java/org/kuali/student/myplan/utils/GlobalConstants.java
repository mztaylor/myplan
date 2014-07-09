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
    public static final String MYPLAN_APP_CODE = ConfigContext.getCurrentContextConfig().getProperty(APP_CODE_PARAM);
}