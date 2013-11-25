package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.myplan.plan.util.OrgHelper;
import org.kuali.student.r2.core.organization.dto.OrgInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class CampusSearch extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(CampusSearch.class);

    private boolean blankOption;


    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        if (blankOption) {
            keyValues.add(new ConcreteKeyValue("", ""));
        }

        List<OrgInfo> orgInfoList = OrgHelper.getAvailableCampuses();

        if (orgInfoList != null && orgInfoList.size() > 0) {
            for (OrgInfo entry : orgInfoList) {
                keyValues.add(new ConcreteKeyValue(entry.getId(), entry.getLongName() + " campus"));
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
