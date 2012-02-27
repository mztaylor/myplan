package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.common.exceptions.DoesNotExistException;
import org.kuali.student.common.exceptions.InvalidParameterException;
import org.kuali.student.common.exceptions.MissingParameterException;
import org.kuali.student.common.exceptions.OperationFailedException;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.core.enumerationmanagement.service.EnumerationManagementService;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import java.util.ArrayList;
import java.util.List;

import javax.xml.namespace.QName;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *  Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class CampusSearch extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(CampusSearch.class);

    private boolean blankOption;

    private transient EnumerationManagementService enumService;

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

        enumeratedValueInfoList = getEnumerationService().getEnumeratedValues("kuali.lu.campusLocation", null, null, null);
        }
        catch (Exception e)
        {
            logger.error("No Values for campuses found",e);
        }
        if (enumeratedValueInfoList != null) {
            //  Add the individual term items.
            for (EnumeratedValueInfo enumeratedValueInfo : enumeratedValueInfoList) {
                if(!enumeratedValueInfo.getCode().equalsIgnoreCase("AL")) {
                keyValues.add(new ConcreteKeyValue(enumeratedValueInfo.getCode(), enumeratedValueInfo.getValue()+" campus"));
                }
            }
        }

        return keyValues;
    }

    public CampusSearch() {
        super();
    }

    public boolean isBlankOption() {
        return blankOption;
    }

    public void setBlankOption(boolean blankOption) {
        this.blankOption = blankOption;
    }
}
