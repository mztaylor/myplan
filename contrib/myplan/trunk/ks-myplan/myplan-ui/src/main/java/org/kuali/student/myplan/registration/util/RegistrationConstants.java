package org.kuali.student.myplan.registration.util;

import org.kuali.rice.core.api.config.property.ConfigContext;

/**
 * Created by hemanthg on 4/24/2014.
 */
public class RegistrationConstants {

    public static final String REGISTRATION_PAGE_1 = "scheduleView";
    public static final String REGISTRATION_PAGE_2 = "scheduleSelect";
    public static final String REGISTRATION_PAGE_3 = "scheduleConfirm";


    public static final String REGISTRATION_SERVER_PARAM = "myplan.registration.server";
    public static final String REGISTRATION_START_TIME_PARAM = "myplan.registration.start.time";
    public static final String REGISTRATION_END_TIME_PARAM = "myplan.registration.end.time";
    public static final String REGISTRATION_URL_FORMAT = ConfigContext.getCurrentContextConfig().getProperty(REGISTRATION_SERVER_PARAM) + "/students/UWNetID/register.asp?QTR=%s&YR=%s&INPUTFORM=UPDATE&PAC=0&MAXDROPS=0&_CW=%s&%s";
    public static final String REGISTRATION_OPEN_TIME = ConfigContext.getCurrentContextConfig().getProperty(REGISTRATION_START_TIME_PARAM);
    public static final String REGISTRATION_CLOSE_TIME = ConfigContext.getCurrentContextConfig().getProperty(REGISTRATION_END_TIME_PARAM);
    public static final String REGISTRATION_CODE_URL_PARAMS_FORMAT = "sln%s=%s&entcode%s=&credits%s=&gr_sys%s=";

    public static final String ERROR_COUNT_REGISTRATION_CODES = "myplan.text.error.check.registration.codes";
}
