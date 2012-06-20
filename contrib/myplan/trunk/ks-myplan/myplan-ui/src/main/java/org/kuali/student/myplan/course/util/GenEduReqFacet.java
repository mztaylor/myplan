package org.kuali.student.myplan.course.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import javax.xml.namespace.QName;
import java.util.*;

/**
 * Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class GenEduReqFacet extends AbstractFacet {

    private final Logger logger = Logger.getLogger(GenEduReqFacet.class);

    private transient EnumerationManagementService enumService;

    private HashMap<String, List<EnumeratedValueInfo>> enumServiceCache = new HashMap<String, List<EnumeratedValueInfo>>();

    private HashSet<String> GenEduReqFacetSet = new HashSet<String>();

    public GenEduReqFacet() {
        super();
        super.setShowUnknownKey(false);
    }

    public HashMap<String, List<EnumeratedValueInfo>> getEnumServiceCache() {
        return enumServiceCache;
    }

    protected synchronized EnumerationManagementService getEnumerationService() {
        if (this.enumService == null) {
            this.enumService = (EnumerationManagementService) GlobalResourceLoader
                    .getService(new QName(CourseSearchConstants.ENUM_SERVICE_NAMESPACE, "EnumerationManagementService"));
        }
        return this.enumService;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem item) {

        FacetItem itemFacet = new FacetItem();
        String genEdString = item.getGenEduReq();
        //  Set of keys which pertain to this course.
        Set<String> facetKeys = new HashSet<String>();
        StringBuffer genEdus = new StringBuffer();
        //  If no gen edu req info was set then setup for an "Unknown" facet.
        if (genEdString == null || genEdString.equals(CourseSearchItem.EMPTY_RESULT_VALUE_KEY) || genEdString.equals("")) {
            facetKeys.add(FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER);
        } else {
            //  TODO: UW SPECIFIC
            //  Remove white space before tokenizing.
            genEdString = genEdString.replaceAll("\\s+", "");
            String k[] = genEdString.split(",");
            List<String> keys = new ArrayList<String>(Arrays.asList(k));
            String saveKey = null;
            for (String key : keys) {
                saveKey = key;
                if (isNewFacetKey(FACET_KEY_DELIMITER + saveKey + FACET_KEY_DELIMITER)) {
                    EnumeratedValueInfo e = getGenEdReqEnumInfo(CourseSearchConstants.GEN_EDU_REQUIREMENTS_PREFIX + key);
                    saveKey = e.getAbbrevValue();
                    genEdus=genEdus.append(saveKey).append(",");
                    String title = e.getValue();
                    if (!StringUtils.isEmpty(title)) {
                        itemFacet.setTitle(title);
                        itemFacet.setKey(FACET_KEY_DELIMITER + saveKey + FACET_KEY_DELIMITER);
                        itemFacet.setDisplayName(saveKey);
                        facetItems.add(itemFacet);
                    }
                }
                facetKeys.add(FACET_KEY_DELIMITER + saveKey + FACET_KEY_DELIMITER);
            }
        }
        if(!item.getGenEduReq().equalsIgnoreCase(CourseSearchItem.EMPTY_RESULT_VALUE_KEY )&& genEdus.toString().length()>0) {
        item.setGenEduReq(genEdus.substring(0,genEdus.lastIndexOf(",")));
        }
        item.setGenEduReqFacetKeys(facetKeys);
    }

    /**
     * FIXME: This code is duplicated in CourseDetailsInquiryViewHelperService.java. This code will go away with facet refactor
     */
    private EnumeratedValueInfo getGenEdReqEnumInfo(String key) {
        EnumeratedValueInfo enumValueInfo = null;
        try {
            List<EnumeratedValueInfo> enumeratedValueInfoList = null;
            if (!enumServiceCache.containsKey("kuali.uw.lu.genedreq")) {
                enumeratedValueInfoList = getEnumerationValueInfoList("kuali.uw.lu.genedreq");
            } else {
                enumeratedValueInfoList = enumServiceCache.get("kuali.uw.lu.genedreq");
            }
            for (EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                String abbr = enumVal.getCode();
                if (abbr.equalsIgnoreCase(key)) {
                    enumValueInfo = enumVal;
                    break;
                }
            }
        } catch (Exception e) {
            logger.error("Could not load genEdReqValue");
        }
        return enumValueInfo;
    }

    private List<EnumeratedValueInfo> getEnumerationValueInfoList(String param) {
        List<EnumeratedValueInfo> enumeratedValueInfoList = null;
        try {
            enumeratedValueInfoList = getEnumerationService().getEnumeratedValues(param, null, null, null);
            enumServiceCache.put(param, enumeratedValueInfoList);
        } catch (Exception e) {
            logger.error("No Values for campuses found", e);
        }
        return enumeratedValueInfoList;
    }
}
