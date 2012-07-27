package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.common.search.dto.SearchResult;
import org.kuali.student.common.search.dto.SearchResultCell;
import org.kuali.student.common.search.dto.SearchResultRow;
import org.kuali.student.core.organization.dto.OrgInfo;
import org.kuali.student.core.organization.service.OrganizationService;
import org.kuali.student.myplan.course.util.CourseSearchConstants;

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
            SearchRequest searchRequest = new SearchRequest(searchRequestKey);
            searchRequest.addParam(paramKey, param);
            SearchResult searchResult = new SearchResult();
            try {
                searchResult = getOrganizationService().search(searchRequest);
            } catch (MissingParameterException e) {
                logger.error("Search Failed to get the Organization Data ", e);
            }
            for (SearchResultRow row : searchResult.getRows()) {
                OrgInfo orgInfo = new OrgInfo();
                orgInfo.setId(getCellValue(row, "org.resultColumn.orgId"));
                orgInfo.setShortName(getCellValue(row, "org.resultColumn.orgShortName"));
                orgInfo.setLongName(getCellValue(row, "org.resultColumn.orgLongName"));
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
        SearchRequest searchRequest = new SearchRequest(CourseSearchConstants.ORG_QUERY_SEARCH_SUBJECT_AREAS);
        SearchResult searchResult = new SearchResult();
        try {
            searchResult = getOrganizationService().search(searchRequest);
        } catch (MissingParameterException e) {
            logger.error("Search Failed to get the Organization Data ", e);
        }
        for (SearchResultRow row : searchResult.getRows()) {
            subjects.put(getCellValue(row, "org.resultColumn.attrValue"), getCellValue(row, "org.resultColumn.name"));

        }
        return subjects;
    }

    public static String getCellValue(SearchResultRow row, String key) {
        for (SearchResultCell cell : row.getCells()) {
            if (key.equals(cell.getKey())) {
                return cell.getValue();
            }
        }
        throw new RuntimeException("cell result '" + key + "' not found");
    }


}
