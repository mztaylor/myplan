package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 7/19/13
 * Time: 11:38 AM
 * To change this template use File | Settings | File Templates.
 */
public class PlaceHoldersBuilder extends KeyValuesBase {
    private final Logger logger = Logger.getLogger(PlaceHoldersBuilder.class);

    @Override
    public List<KeyValue> getKeyValues() {
        String value, key;
        List<KeyValue> kvList = new ArrayList<KeyValue>();

        List<EnumeratedValueInfo> enums =  EnumerationHelper.getEnumsByContext(
                CourseSearchConstants.ENUM_CONTEXT_KEY_SEARCH_PLACEHOLDER_KEY);

        for (EnumeratedValueInfo enumValue : enums) {
            key   = String.format("%s|%s",   enumValue.getCode(), enumValue.getEnumerationKey());
            // arbitrarily, we sometimes use both AbbrevValue and Value, sometimes not
            if (enumValue.getAbbrevValue().equals(enumValue.getValue()) ||
                enumValue.getAbbrevValue().equals("Elective") ) {
                value = String.format("%s", enumValue.getAbbrevValue());
            }  else {
                value = String.format("%s (%s)", enumValue.getAbbrevValue(), enumValue.getValue());
            }
            if ( enumValue.getEnumerationKey().equals(PlanConstants.GEN_EDU_ENUM_KEY) ) {
                value += " - Gen. Ed. Req.";
            }
            if ( enumValue.getCode().equals(PlanConstants.PLACE_HOLDER_OTHER_CODE) ) {
                value += " - Enter description in 'Notes' below (REQUIRED)";
            }
            kvList.add(new ConcreteKeyValue(key,value));
        }

        return kvList;
    }
}
