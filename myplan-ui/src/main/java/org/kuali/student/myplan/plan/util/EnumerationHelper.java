package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.r2.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.r2.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.infc.SearchResultRow;

import javax.xml.namespace.QName;
import java.util.ArrayList;
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

    public static void setEnumService(EnumerationManagementService enumService) {
        EnumerationHelper.enumService = enumService;
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
                enumeratedValueInfoList = getEnumerationService().getEnumeratedValues(key, null, null, null, CourseSearchConstants.CONTEXT_INFO);
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

    /**
     * returns all the Enumerations associated with a specific context, potentially for different enum_keys.
     * So, if a subset of enum_key foo values are associated with context bar and a subset of enum_key boo values
     * are associated with context bar then a get for context bar values will retrieve both the foo and boo values.
     *
     * @param contextKey - the context to fetch enums for; in database this is value of KSEM_CTX_T.CTX_KEY
     * @return list of EnumeratedValueInfo for the enums joined to the requested contextKey
     */
    public static List<EnumeratedValueInfo> getEnumsByContext(String contextKey) {
        List<EnumeratedValueInfo> enumeratedValueInfoList = new ArrayList<EnumeratedValueInfo>();
        SearchRequestInfo searchRequest = new SearchRequestInfo(CourseSearchConstants.ENUM_CONTEXT_KEY_SEARCH_TYPE);
        searchRequest.addParam(CourseSearchConstants.ENUM_CONTEXT_KEY_SEARCH_PARAM_NAME, contextKey);
        SearchResultInfo searchResult = new SearchResultInfo();
        try {
            searchResult = getEnumerationService().search(searchRequest, CourseSearchConstants.CONTEXT_INFO);
        } catch (Exception e) {
                logger.error("Search Failed in getEnumsByContext to get the Enumeration Data for " + contextKey +
                        ". Exception info: " +  e);
                return enumeratedValueInfoList;   // an empty list
        }

        for (SearchResultRow row : searchResult.getRows()) {
            EnumeratedValueInfo enumInfo = new EnumeratedValueInfo();
            enumInfo.setCode          (SearchHelper.getCellValue(row, "enumeration.resultColumn.code"));
            enumInfo.setAbbrevValue   (SearchHelper.getCellValue(row, "enumeration.resultColumn.abbrevValue"));
            enumInfo.setValue         (SearchHelper.getCellValue(row, "enumeration.resultColumn.value"));
            enumInfo.setEnumerationKey(SearchHelper.getCellValue(row, "enumeration.resultColumn.enumKey"));
            enumeratedValueInfoList.add(enumInfo);
        }

        return enumeratedValueInfoList;
    }
}
