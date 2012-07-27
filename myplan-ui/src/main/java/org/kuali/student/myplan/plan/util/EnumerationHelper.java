package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.myplan.course.util.CourseSearchConstants;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 7/27/12
 * Time: 10:28 AM
 * To change this template use File | Settings | File Templates.
 */
public class EnumerationHelper {

    private static final Logger logger = Logger.getLogger(EnumerationHelper.class);

    private static transient EnumerationManagementService enumService;

    private static HashMap<String, List<EnumeratedValueInfo>> enumServiceCache;

    public static HashMap<String, List<EnumeratedValueInfo>> getEnumServiceCache() {
        if (EnumerationHelper.enumServiceCache == null) {
            EnumerationHelper.enumServiceCache = new HashMap<String, List<EnumeratedValueInfo>>();
        }
        return enumServiceCache;
    }

    public static void setEnumServiceCache(HashMap<String, List<EnumeratedValueInfo>> enumServiceCache) {
        EnumerationHelper.enumServiceCache = enumServiceCache;
    }

    protected static synchronized EnumerationManagementService getEnumerationService() {
        if (EnumerationHelper.enumService == null) {
            EnumerationHelper.enumService = (EnumerationManagementService) GlobalResourceLoader
                    .getService(new QName(CourseSearchConstants.ENUM_SERVICE_NAMESPACE, "EnumerationManagementService"));
        }
        return EnumerationHelper.enumService;
    }


    public static EnumeratedValueInfo getGenEdReqEnumInfo(String key) {
        EnumeratedValueInfo enumValueInfo = null;
        try {
            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!getEnumServiceCache().containsKey("kuali.uw.lu.genedreq")) {
                enumeratedValueInfoList = getEnumerationValueInfoList("kuali.uw.lu.genedreq");
            } else {
                enumeratedValueInfoList = getEnumServiceCache().get("kuali.uw.lu.genedreq");
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String enumCode = enumVal.getCode();
                if (enumCode.equalsIgnoreCase(key)) {
                    enumValueInfo = enumVal;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not load genEdReqValue");
        }
        return enumValueInfo;
    }


    public static List<EnumeratedValueInfo> getEnumerationValueInfoList(String param) {
        List<EnumeratedValueInfo> enumeratedValueInfoList = null;
        try {
            enumeratedValueInfoList = getEnumerationService().getEnumeratedValues(param, null, null, null);
            getEnumServiceCache().put(param, enumeratedValueInfoList);
        } catch (Exception e) {
            logger.error("No Values for campuses found", e);
        }
        return enumeratedValueInfoList;
    }

    public static String getEnumAbbrValForCode(String code) {
        String enumAbbrValue = null;
        try {

            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!getEnumServiceCache().containsKey("kuali.uw.lu.genedreq")) {
                enumeratedValueInfoList = getEnumerationValueInfoList("kuali.uw.lu.genedreq");
            } else {
                enumeratedValueInfoList = getEnumServiceCache().get("kuali.uw.lu.genedreq");
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String enumCode = enumVal.getCode();
                if (enumCode.equalsIgnoreCase(code)) {
                    enumAbbrValue = enumVal.getAbbrevValue();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not load genEdReqValue");
        }
        return enumAbbrValue;

    }

    public static String getEnumValForCode(String code) {
        String enumAbbrValue = null;
        try {

            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!getEnumServiceCache().containsKey("kuali.uw.lu.genedreq")) {
                enumeratedValueInfoList = getEnumerationValueInfoList("kuali.uw.lu.genedreq");
            } else {
                enumeratedValueInfoList = getEnumServiceCache().get("kuali.uw.lu.genedreq");
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String enumCode = enumVal.getCode();
                if (enumCode.equalsIgnoreCase(code)) {
                    enumAbbrValue = enumVal.getValue();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not load genEdReqValue");
        }
        return enumAbbrValue;

    }

    public static String getEnumCodeForAbbrVal(String abbrVal) {
        String enumCode = null;
        try {

            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!getEnumServiceCache().containsKey("kuali.uw.lu.genedreq")) {
                enumeratedValueInfoList = getEnumerationValueInfoList("kuali.uw.lu.genedreq");
            } else {
                enumeratedValueInfoList = getEnumServiceCache().get("kuali.uw.lu.genedreq");
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String enumAbbrVal = enumVal.getAbbrevValue();
                if (enumAbbrVal.equalsIgnoreCase(abbrVal)) {
                    enumCode = enumVal.getCode();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not load genEdReqValue");
        }
        return enumCode;

    }


}
