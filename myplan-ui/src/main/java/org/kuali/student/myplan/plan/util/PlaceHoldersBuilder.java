package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.uif.util.SimpleSuggestObject;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.r2.core.enumerationmanagement.dto.EnumeratedValueInfo;

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
        kvList.add(new ConcreteKeyValue(PlanConstants.DEFAULT_KEY, PlanConstants.DEFAULT_SELECT));
        List<EnumeratedValueInfo> enums = EnumerationHelper.getEnumsByContext(
                CourseSearchConstants.ENUM_CONTEXT_KEY_SEARCH_PLACEHOLDER_KEY);

        for (EnumeratedValueInfo enumValue : enums) {
            key = String.format("%s|%s", enumValue.getCode(), enumValue.getEnumerationKey());
            // arbitrarily, we sometimes use both AbbrevValue and Value, sometimes not
            if (enumValue.getAbbrevValue().equals(enumValue.getValue()) ||
                    enumValue.getAbbrevValue().equals("Elective")) {
                value = String.format("%s", enumValue.getAbbrevValue());
            } else {
                value = String.format("%s (%s)", enumValue.getAbbrevValue(), enumValue.getValue());
            }
            if (enumValue.getEnumerationKey().equals(PlanConstants.GEN_EDU_ENUM_KEY)) {
                value += " - Gen. Ed. Req.";
            }
            if (enumValue.getCode().equals(PlanConstants.PLACE_HOLDER_OTHER_CODE)) {
                value += " - Enter description in 'Notes' below (REQUIRED)";
            }
            kvList.add(new ConcreteKeyValue(key, value));
        }

        return kvList;
    }

    /**
     * PlaceHolders Id and values are returned as label and value
     * Used for SamplePlans ComboBox
     *
     * @return
     */
    public static List<SimpleSuggestObject> getAllKeyValueObjects() {
        List<SimpleSuggestObject> keyValues = new ArrayList<SimpleSuggestObject>();
        String label, value;
        List<EnumeratedValueInfo> enums = EnumerationHelper.getEnumsByContext(
                CourseSearchConstants.ENUM_CONTEXT_KEY_SEARCH_PLACEHOLDER_KEY);
        for (EnumeratedValueInfo enumValue : enums) {
            value = String.format("%s|%s", enumValue.getCode(), enumValue.getEnumerationKey());
            // arbitrarily, we sometimes use both AbbrevValue and Value, sometimes not
            if (enumValue.getAbbrevValue().equals(enumValue.getValue()) ||
                    enumValue.getAbbrevValue().equals("Elective")) {
                label = String.format("%s", enumValue.getAbbrevValue());
            } else {
                label = String.format("%s (%s)", enumValue.getAbbrevValue(), enumValue.getValue());
            }
            if (enumValue.getEnumerationKey().equals(PlanConstants.GEN_EDU_ENUM_KEY)) {
                label += " - Gen. Ed. Req.";
            }
            if (enumValue.getCode().equals(PlanConstants.PLACE_HOLDER_OTHER_CODE)) {
                label += " - Enter description in 'Notes' below (REQUIRED)";
            }
            keyValues.add(new SimpleSuggestObject(label, value));
        }
        return keyValues;
    }


}
