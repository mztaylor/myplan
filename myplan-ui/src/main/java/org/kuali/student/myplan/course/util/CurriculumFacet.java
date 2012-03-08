package org.kuali.student.myplan.course.util;

import javax.xml.namespace.QName;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.exceptions.DoesNotExistException;
import org.kuali.student.common.exceptions.InvalidParameterException;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.exceptions.OperationFailedException;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.core.organization.service.OrganizationService;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.*;

/**
 *  Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class CurriculumFacet extends AbstractFacet {

    private final Logger logger = Logger.getLogger(CurriculumFacet.class);

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
    public CurriculumFacet() {
        super();
    }

    private HashSet<String> curriculumFacetSet = new HashSet<String>();

    @Override
    public List<FacetItem> getFacetItems() {
        String[] list = curriculumFacetSet.toArray( new String[0] );
        Arrays.sort( list );



        for( String display : list ) {
            FacetItem item = new FacetItem();
                       
            String key = FACET_KEY_DELIMITER + display + FACET_KEY_DELIMITER;
            item.setKey( key );
            item.setDisplayName(display);
            String title= this.getTitle(display);
            if(title != null || title!= ""){
            item.setTitle(title);
            }
            facetItems.add( item );
        }

        return facetItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course)
    {
        // TODO: Use the Org Service to lookup curriculum name based on key.
        // For now just use the serviceArea code as the displayName.
        String subject = course.getSubject();

        if (subject == null || subject.equals("")) {
            subject = unknownFacetKey;
        }

        curriculumFacetSet.add( subject );

        String key = FACET_KEY_DELIMITER + subject + FACET_KEY_DELIMITER;

        //  Code the item with the facet key.
        Set<String> keys = new HashSet<String>();
        keys.add(key);
        course.setCurriculumFacetKeys(keys);
    }

    /**
     * To get the title for the respective display name
     * @param display
     * @return
     */
    protected String getTitle(String display) {
             String titleValue=null;
        List<EnumeratedValueInfo> enumeratedValueInfoList =null;
              try {
                  if(!this.getHashMap().containsKey("kuali.lu.subjectArea")) {
                 enumeratedValueInfoList=getEnumerationService().getEnumeratedValues("kuali.lu.subjectArea", null, null, null);
                      hashMap.put("kuali.lu.subjectArea",enumeratedValueInfoList);
                  }
                  else {
                      enumeratedValueInfoList=this.hashMap.get("kuali.lu.subjectArea");
                  }
                for(EnumeratedValueInfo enumVal : enumeratedValueInfoList)
                {
                    String code= enumVal.getCode().trim();
                    if(code.equalsIgnoreCase(display))
                    {
                        titleValue=enumVal.getValue().trim();
                        break;
                    }
                }

            } catch (Exception e) {
                logger.error("Could not load title value");
            }
        
        return titleValue;
    }
    
    
}
