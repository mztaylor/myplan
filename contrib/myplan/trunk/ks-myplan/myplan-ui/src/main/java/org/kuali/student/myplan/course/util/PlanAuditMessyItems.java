package org.kuali.student.myplan.course.util;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.myplan.audit.dataobject.MessyItem;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 4/15/13
 * Time: 1:36 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlanAuditMessyItems extends KeyValuesBase {

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        MessyItem messyItem = (MessyItem) request.getSession().getAttribute("studentMessyItem");
        if (messyItem != null) {
            for (String credit : messyItem.getCredits()) {
                keyValues.add(new ConcreteKeyValue(credit, credit.split(":")[2]));
            }
            Collections.sort(keyValues, new Comparator<KeyValue>() {
                @Override
                public int compare(KeyValue val1, KeyValue val2) {
                    String compVal1 = val1.getKey().split(":")[1].trim();
                    String compVal2 = val2.getKey().split(":")[1].trim();
                    return Integer.valueOf(compVal1).compareTo(Integer.valueOf(compVal2));
                }
            });
        }

        return keyValues;
    }
}
