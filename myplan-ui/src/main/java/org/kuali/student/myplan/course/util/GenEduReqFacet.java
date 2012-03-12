package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import javax.xml.namespace.QName;
import java.util.*;

/**
*  Logic for building list of FacetItems and coding CourseSearchItems.
*/
public class GenEduReqFacet extends AbstractFacet {

    private final Logger logger = Logger.getLogger(GenEduReqFacet.class);

    private transient EnumerationManagementService enumService;
    private  HashMap<String,List<EnumeratedValueInfo>> hashMap=new HashMap<String, List<EnumeratedValueInfo>>();

    public HashMap<String, List<EnumeratedValueInfo>> getHashMap() {
        return hashMap;
    }

    public void setHashMap(HashMap<String, List<EnumeratedValueInfo>> hashMap) {
        this.hashMap = hashMap;
    }

    protected synchronized EnumerationManagementService getEnumerationService() {
        if (this.enumService == null) {
            this.enumService = (EnumerationManagementService) GlobalResourceLoader
                    .getService(new QName(CourseSearchConstants.ENUM_SERVICE_NAMESPACE, "EnumerationManagementService"));
        }
        return this.enumService;
    }


    private HashSet<String> GenEduReqFacetSet = new HashSet<String>();

    public GenEduReqFacet() {
        super();
        super.setShowUnknownKey(false);
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

        //  If no gen edu req info was set then setup for an "Unknown" facet.
        if (genEdString == null || genEdString.equals(CourseSearchItem.EMPTY_RESULT_VALUE_KEY) || genEdString.equals("")) {
            facetKeys.add(FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER);

        } else {
            //  TODO: UW SPECIFIC
            //  Remove white space before tokenizing.
            genEdString = genEdString.replaceAll("\\s+", "");
            String k[] = genEdString.split(",");
            List<String> keys = new ArrayList<String>(Arrays.asList(k));
            for (String key : keys) {
                if (isNewFacetKey( FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER )) {
                    String display = key;
                    key = FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER;

                    String title= this.getTitle(display);

                    if(title != null || title!= ""){
                        itemFacet.setTitle(title);
                        itemFacet.setKey( key );
                        itemFacet.setDisplayName(display);
                        facetItems.add(itemFacet);
                    }
                }
                facetKeys.add(FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER);
            }
        }
        item.setGenEduReqFacetKeys(facetKeys);
    }


    protected String getTitle(String key) {

               String titleValue=null;

               try {
                  List<EnumeratedValueInfo> enumeratedValueInfoList = getEnumerationService().getEnumeratedValues("kuali.uw.lu.genedreq", null, null, null);
                  for(EnumeratedValueInfo enumVal : enumeratedValueInfoList) {
                      String abbr = enumVal.getAbbrevValue();

                      if(abbr.equalsIgnoreCase(key)) {
                          titleValue=enumVal.getValue();
                          break;
                      }
                  }

              } catch (Exception e) {
                  logger.error("Could not load genEdReqValue");
              }
          return titleValue;
    }

}
