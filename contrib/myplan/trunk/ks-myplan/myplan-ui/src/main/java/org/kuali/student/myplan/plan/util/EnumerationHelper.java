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

        List<EnumeratedValueInfo> enumeratedValueInfoList = getEnumerationValueInfoList(key);
        for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
            String enumCode = enumVal.getCode();
            if (enumCode.equalsIgnoreCase(code)) {
                enumValueInfo = enumVal;
                break;
            }
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

        if (getEnumServiceCache().containsKey(key)) {
            enumeratedValueInfoList = getEnumServiceCache().get(key);
        } else {
            try {
                enumeratedValueInfoList = getEnumerationService().getEnumeratedValues(key, null, null, null);
                getEnumServiceCache().put(key, enumeratedValueInfoList);
            } catch (Exception e) {
                logger.error("Could not load the enum list", e);
            }
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
        List<EnumeratedValueInfo> enumeratedValueInfoList = getEnumerationValueInfoList(key);
        for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
            String enumCode = enumVal.getCode();
            if (enumCode.equalsIgnoreCase(code)) {
                enumAbbrValue = enumVal.getAbbrevValue();
                break;
            }
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

        List<EnumeratedValueInfo> enumeratedValueInfoList = getEnumerationValueInfoList(key);
        for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
            String enumCode = enumVal.getCode();
            if (enumCode.equalsIgnoreCase(code)) {
                enumAbbrValue = enumVal.getValue();
                break;
            }
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

        List<EnumeratedValueInfo> enumeratedValueInfoList = getEnumerationValueInfoList(key);
        for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
            String enumAbbrVal = enumVal.getAbbrevValue();
            if (enumAbbrVal.equalsIgnoreCase(abbrVal)) {
                enumCode = enumVal.getCode();
                break;
            }
        }

        return enumCode;
    }

}
