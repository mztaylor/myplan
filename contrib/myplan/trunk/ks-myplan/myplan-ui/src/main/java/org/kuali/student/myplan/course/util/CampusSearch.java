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
import org.kuali.student.core.organization.dto.OrgInfo;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.myplan.plan.util.OrgHelper;

import java.util.*;

import javax.xml.namespace.QName;
import java.util.List;

/**
 * Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class CampusSearch extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(CampusSearch.class);

    private boolean blankOption;


    private HashMap<String, Map<String, String>> hashMap;

    public HashMap<String, Map<String, String>> getHashMap() {
        if (this.hashMap == null) {
            this.hashMap = new HashMap<String, Map<String, String>>();
        }
        return this.hashMap;
    }

    public void setHashMap(HashMap<String, Map<String, String>> hashMap) {
        this.hashMap = hashMap;
    }


    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        if (blankOption) {
            keyValues.add(new ConcreteKeyValue("", ""));
        }
        Map<String, String> campusValues = new HashMap<String, String>();
        try {
            if (!this.getHashMap().containsKey(CourseSearchConstants.CAMPUS_LOCATION)) {
                campusValues = OrgHelper.getOrgInfoFromType(CourseSearchConstants.CAMPUS_LOCATION);
                getHashMap().put(CourseSearchConstants.CAMPUS_LOCATION, campusValues);
            } else {
                campusValues = getHashMap().get(CourseSearchConstants.CAMPUS_LOCATION);
            }
        } catch (Exception e) {
            logger.error("No Values for campuses found", e);
        }
        if (campusValues != null) {
            for (Map.Entry<String, String> entry : campusValues.entrySet()) {
                keyValues.add(new ConcreteKeyValue(entry.getKey(), entry.getValue() + " campus"));
            }
        }
        Collections.sort(keyValues,
                new Comparator<KeyValue>() {
                    @Override
                    public int compare(KeyValue keyValue1, KeyValue keyValue2) {
                        return keyValue1.getKey().compareTo(keyValue2.getKey());
                    }
                });
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
