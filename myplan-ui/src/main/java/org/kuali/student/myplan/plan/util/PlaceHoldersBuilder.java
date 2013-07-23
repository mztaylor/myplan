package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.core.enumerationmanagement.dto.EnumeratedValueInfo;

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

        List<KeyValue> list = new ArrayList<KeyValue>();

        List<EnumeratedValueInfo> enums = new ArrayList<EnumeratedValueInfo>();
        enums.addAll(EnumerationHelper.getEnumerationValueInfoList("uw.academicplan.placeholder"));
        enums.addAll(EnumerationHelper.getEnumerationValueInfoList("kuali.uw.lu.genedreq"));


        for (EnumeratedValueInfo enumValue : enums) {
            list.add(new ConcreteKeyValue(enumValue.getCode(), String.format("%s (%s) - Gen. Edu. Req.", enumValue.getAbbrevValue(), enumValue.getValue())));
        }

        return list;
    }
}
