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


    /**
     * Returns a EnumerationValueInfo for given enum code and enum key
     *
     * @param code
     * @param key
     * @return
     */
    public static EnumeratedValueInfo getEnumValueInfoForCodeByType(String code, String key) {
        EnumeratedValueInfo enumValueInfo = null;
        try {
            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!getEnumServiceCache().containsKey(key)) {
                enumeratedValueInfoList = getEnumerationValueInfoList(key);
            } else {
                enumeratedValueInfoList = getEnumServiceCache().get(key);
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String enumCode = enumVal.getCode();
                if (enumCode.equalsIgnoreCase(code)) {
                    enumValueInfo = enumVal;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not load enumerationValueInfo for code " + code);
        }
        return enumValueInfo;
    }


    /**
     * Returns a list of EnumeratedValueInfo objects for given enum key
     *
     * @param key
     * @return
     */
    public static List<EnumeratedValueInfo> getEnumerationValueInfoList(String key) {
        List<EnumeratedValueInfo> enumeratedValueInfoList = null;
        try {
            enumeratedValueInfoList = getEnumerationService().getEnumeratedValues(key, null, null, null);
            getEnumServiceCache().put(key, enumeratedValueInfoList);
        } catch (Exception e) {
            logger.error("Could not load the enum list", e);
        }
        return enumeratedValueInfoList;
    }

    /**
     * Returns the abbreviated enum value for code by type
     *
     * @param code
     * @param key
     * @return
     */
    public static String getEnumAbbrValForCodeByType(String code, String key) {
        String enumAbbrValue = null;
        try {

            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!getEnumServiceCache().containsKey(key)) {
                enumeratedValueInfoList = getEnumerationValueInfoList(key);
            } else {
                enumeratedValueInfoList = getEnumServiceCache().get(key);
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String enumCode = enumVal.getCode();
                if (enumCode.equalsIgnoreCase(code)) {
                    enumAbbrValue = enumVal.getAbbrevValue();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not load enumeration value info for code " + code);
        }
        return enumAbbrValue;

    }

    /**
     * returns a Enumerated value for code by Type
     *
     * @param code
     * @param key
     * @return
     */
    public static String getEnumValueForCodeByType(String code, String key) {
        String enumAbbrValue = null;
        try {

            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!getEnumServiceCache().containsKey(key)) {
                enumeratedValueInfoList = getEnumerationValueInfoList(key);
            } else {
                enumeratedValueInfoList = getEnumServiceCache().get(key);
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String enumCode = enumVal.getCode();
                if (enumCode.equalsIgnoreCase(code)) {
                    enumAbbrValue = enumVal.getValue();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not get the Enum value for code " + code);
        }
        return enumAbbrValue;

    }

    /**
     * returns a Enumerated code for abbreviated value by Type
     *
     * @param abbrVal
     * @param key
     * @return
     */
    public static String getEnumCodeForAbbrValByType(String abbrVal, String key) {
        String enumCode = null;
        try {

            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!getEnumServiceCache().containsKey(key)) {
                enumeratedValueInfoList = getEnumerationValueInfoList(key);
            } else {
                enumeratedValueInfoList = getEnumServiceCache().get(key);
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String enumAbbrVal = enumVal.getAbbrevValue();
                if (enumAbbrVal.equalsIgnoreCase(abbrVal)) {
                    enumCode = enumVal.getCode();
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not get the code value for abbreviated value " + abbrVal);
        }
        return enumCode;

    }


}
