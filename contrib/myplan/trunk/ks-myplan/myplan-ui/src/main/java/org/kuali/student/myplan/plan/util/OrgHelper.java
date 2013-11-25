package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.core.organization.dto.OrgInfo;
import org.kuali.student.r2.core.organization.service.OrganizationService;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.infc.SearchResultRow;
import org.springframework.util.StringUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 7/24/12
 * Time: 9:36 AM
 * To change this template use File | Settings | File Templates.
 */
public class OrgHelper {

    private static final Logger logger = Logger.getLogger(OrgHelper.class);

    private static final SearchRequestInfo SUBJECT_AREA_SEARCH_REQUEST = new SearchRequestInfo(CourseSearchConstants.ORG_QUERY_SEARCH_SUBJECT_AREAS);

    public static OrganizationService organizationService;

    public static HashMap<String, List<OrgInfo>> orgTypeCache;

    public static HashMap<String, List<OrgInfo>> getOrgTypeCache() {
        if (OrgHelper.orgTypeCache == null) {
            OrgHelper.orgTypeCache = new HashMap<String, List<OrgInfo>>();
        }
        return OrgHelper.orgTypeCache;
    }

    public static void setOrgTypeCache(HashMap<String, List<OrgInfo>> orgTypeCache) {
        OrgHelper.orgTypeCache = orgTypeCache;
    }

    public static OrganizationService getOrganizationService() {
        if (OrgHelper.organizationService == null) {
            //   TODO: Use constants for namespace.
            OrgHelper.organizationService = (OrganizationService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/organization", "orgService"));
        }
        return OrgHelper.organizationService;
    }

    public static void setOrganizationService(OrganizationService organizationService) {
        OrgHelper.organizationService = organizationService;
    }


    public static List<OrgInfo> getOrgInfo(String param, String searchRequestKey, String paramKey) {
        if (OrgHelper.getOrgTypeCache() != null && OrgHelper.getOrgTypeCache().containsKey(param)) {
            return getOrgTypeCache().get(param);
        } else {
            List<OrgInfo> orgInfoList = new ArrayList<OrgInfo>();
            SearchRequestInfo searchRequest = new SearchRequestInfo(searchRequestKey);
            searchRequest.addParam(paramKey, param);
            SearchResultInfo searchResult = new SearchResultInfo();
            try {
                searchResult = getOrganizationService().search(searchRequest, CourseSearchConstants.CONTEXT_INFO);
            } catch (Exception e) {
                logger.error("Search Failed to get the Organization Data ", e);
            }
            for (SearchResultRow row : searchResult.getRows()) {
                OrgInfo orgInfo = new OrgInfo();
                orgInfo.setId(SearchHelper.getCellValue(row, "org.resultColumn.orgId"));
                orgInfo.setShortName(SearchHelper.getCellValue(row, "org.resultColumn.orgShortName"));
                orgInfo.setLongName(SearchHelper.getCellValue(row, "org.resultColumn.orgLongName"));
                orgInfoList.add(orgInfo);

            }
            if (orgInfoList.size() > 0) {
                OrgHelper.getOrgTypeCache().put(param, orgInfoList);
            }
            return orgInfoList;
        }
    }

    public static Map<String, String> getSubjectAreas() {
        Map<String, String> subjects = new HashMap<String, String>();
        SearchResultInfo searchResult = new SearchResultInfo();
        try {
            searchResult = getOrganizationService().search(SUBJECT_AREA_SEARCH_REQUEST, CourseSearchConstants.CONTEXT_INFO);
        } catch (MissingParameterException e) {
            logger.error("Search Failed to get the Organization Data ", e);
        } catch (Exception e) {
            logger.error("Search Failed to get the Organization Data ", e);
        }
        for (SearchResultRow row : searchResult.getRows()) {
            subjects.put(SearchHelper.getCellValue(row, "org.resultColumn.attrValue"), SearchHelper.getCellValue(row, "org.resultColumn.name"));

        }
        return subjects;
    }

    /*Used for the subjects area's with trimmed key value */
    public static Map<String, String> getTrimmedSubjectAreas() {
        Map<String, String> subjects = new HashMap<String, String>();
        SearchResultInfo searchResult = new SearchResultInfo();
        try {
            searchResult = getOrganizationService().search(SUBJECT_AREA_SEARCH_REQUEST, CourseSearchConstants.CONTEXT_INFO);
        } catch (MissingParameterException e) {
            logger.error("Search Failed to get the Organization Data ", e);
        } catch (Exception e) {
            logger.error("Search Failed to get the Organization Data ", e);
        }
        for (SearchResultRow row : searchResult.getRows()) {
            subjects.put(SearchHelper.getCellValue(row, "org.resultColumn.attrValue").trim(), SearchHelper.getCellValue(row, "org.resultColumn.name"));

        }
        return subjects;
    }


    /**
     * returns a list of orInfo's holding all the available campuses
     *
     * @return
     */
    public static List<OrgInfo> getAvailableCampuses() {
        List<OrgInfo> orgInfoList = new ArrayList<OrgInfo>();
        try {
            orgInfoList = OrgHelper.getOrgInfo(CourseSearchConstants.CAMPUS_LOCATION_ORG_TYPE, CourseSearchConstants.ORG_QUERY_SEARCH_BY_TYPE_REQUEST, CourseSearchConstants.ORG_TYPE_PARAM);
        } catch (Exception e) {
            logger.error("No Values for campuses found", e);
        }
        return orgInfoList;

    }

    /**
     * returns the name of the campus for the given campus code.
     *
     * @param campusCode
     * @return
     */
    public static String getCampusName(String campusCode) {
        if (StringUtils.hasText(campusCode)) {
            for (OrgInfo orgInfo : getAvailableCampuses()) {
                if (campusCode.equals(orgInfo.getId())) {
                    return orgInfo.getLongName();
                }
            }
        }
        return null;
    }
}
