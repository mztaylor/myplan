package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 *  Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class AuditRunCampusParam extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(AuditRunCampusParam.class);

    private boolean blankOption;

    private transient EnumerationManagementService enumService;

    private HashMap<String,List<EnumeratedValueInfo>> hashMap=new HashMap<String, List<EnumeratedValueInfo>>();

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



    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        if(blankOption){
            keyValues.add(new ConcreteKeyValue("", ""));
        }
        List<EnumeratedValueInfo> enumeratedValueInfoList =null;
        try{
        if(!this.getHashMap().containsKey("kuali.lu.campusLocation")) {
        enumeratedValueInfoList = getEnumerationService().getEnumeratedValues("kuali.lu.campusLocation", null, null, null);
            hashMap.put("kuali.lu.campusLocation",enumeratedValueInfoList);
        }
            else {
            enumeratedValueInfoList=this.hashMap.get("kuali.lu.campusLocation");
        }
        }
        catch (Exception e)
        {
            logger.error("No Values for campuses found",e);
        }
        if (enumeratedValueInfoList != null) {
            //  Add the individual term items.
            for (EnumeratedValueInfo enumeratedValueInfo : enumeratedValueInfoList) {
                if(!enumeratedValueInfo.getCode().equalsIgnoreCase("AL")) {
                keyValues.add(new ConcreteKeyValue(enumeratedValueInfo.getCode(), enumeratedValueInfo.getValue()));
                }
            }
        }

        return keyValues;
    }

    public AuditRunCampusParam() {
        super();
    }

    public boolean isBlankOption() {
        return blankOption;
    }

    public void setBlankOption(boolean blankOption) {
        this.blankOption = blankOption;
    }
}
