package org.kuali.student.myplan.course.util;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.rice.krad.uif.element.Message;
import org.kuali.student.myplan.audit.form.MessyItem;
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

    private String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        MessyItem messyItem = (MessyItem) request.getSession().getAttribute("studentMessyItem");
        if (messyItem != null) {
            if (type.equalsIgnoreCase("SECTIONS")) {
                for (String section : messyItem.getSections()) {
                    keyValues.add(new ConcreteKeyValue(section, section));
                }
            } else if (type.equalsIgnoreCase("CREDITS")) {

                for (String section : messyItem.getCredits()) {
                    keyValues.add(new ConcreteKeyValue(section, section));
                }
                Collections.sort(keyValues, new Comparator<KeyValue>() {
                    @Override
                    public int compare(KeyValue val1, KeyValue val2) {
                        return Integer.valueOf(val1.getValue()).compareTo(Integer.valueOf(val2.getValue()));
                    }
                });
            }
        }

        return keyValues;
    }
}
