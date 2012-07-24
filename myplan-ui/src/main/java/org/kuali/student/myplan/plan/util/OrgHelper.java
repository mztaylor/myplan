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

    public static HashMap<String, Map<String, String>> orgTypeCache;

    public static HashMap<String, Map<String, String>> getOrgTypeCache() {
        if(OrgHelper.orgTypeCache==null){
            OrgHelper.orgTypeCache=new HashMap<String, Map<String, String>>();
        }
        return OrgHelper.orgTypeCache;
    }

    public static void setOrgTypeCache(HashMap<String, Map<String, String>> orgTypeCache) {
        OrgHelper.orgTypeCache = orgTypeCache;
    }

    public static OrganizationService getOrganizationService() {
        if (OrgHelper.organizationService == null) {
            //   TODO: Use constants for namespace.
            OrgHelper.organizationService = (OrganizationService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/organization", "OrganizationService"));
        }
        return OrgHelper.organizationService;
    }

    public static void setOrganizationService(OrganizationService organizationService) {
        OrgHelper.organizationService = organizationService;
    }


    public static Map<String, String> getOrgInfoFromType(String param) {
        if (OrgHelper.getOrgTypeCache() != null && OrgHelper.getOrgTypeCache().containsKey(param)) {
            return getOrgTypeCache().get(param);
        } else {
            Map<String, String> orgTypes = new HashMap<String, String>();
            SearchRequest searchRequest = new SearchRequest(CourseSearchConstants.ORG_QUERY_SEARCH_REQUEST);
            searchRequest.addParam(CourseSearchConstants.ORG_QUERY_PARAM, param);
            SearchResult searchResult = new SearchResult();
            try {
                searchResult = getOrganizationService().search(searchRequest);
            } catch (MissingParameterException e) {
                logger.error("Search Failed to get the Organization Data ", e);
            }
            for (SearchResultRow row : searchResult.getRows()) {
                orgTypes.put(getCellValue(row, "org.resultColumn.orgId"), getCellValue(row, "org.resultColumn.orgShortName"));
            }
            if (orgTypes.size() > 0) {
                OrgHelper.getOrgTypeCache().put(param, orgTypes);
            }
            return orgTypes;
        }
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
