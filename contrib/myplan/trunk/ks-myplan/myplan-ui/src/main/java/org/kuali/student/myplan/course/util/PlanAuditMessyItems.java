package org.kuali.student.myplan.course.util;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.myplan.audit.dataobject.MessyItem;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
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

    private static final int DISPLAY_INDEX = 3;

    private static final int CREDIT_INDEX = 2;

    @Override
    public List<KeyValue> getKeyValues() {
        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        List<KeyValue> normalCredits = new ArrayList<KeyValue>();
        List<KeyValue> honorCredits = new ArrayList<KeyValue>();
        List<KeyValue> writingCredits = new ArrayList<KeyValue>();
        List<KeyValue> bothHWCredits = new ArrayList<KeyValue>();
        List<KeyValue> crNcCredits = new ArrayList<KeyValue>();
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        MessyItem messyItem = (MessyItem) request.getAttribute("studentMessyItem");
        if (messyItem != null) {
            for (String credit : messyItem.getCredits()) {
                String display = credit.split(":")[DISPLAY_INDEX];
                if (display.contains("-- " + DegreeAuditConstants.WRITING_CREDIT + "-- " + DegreeAuditConstants.HONORS_CREDIT)) {
                    bothHWCredits.add(new ConcreteKeyValue(credit, display));
                } else if (display.contains("-- " + DegreeAuditConstants.HONORS_CREDIT)) {
                    honorCredits.add(new ConcreteKeyValue(credit, display));
                } else if (display.contains("-- " + DegreeAuditConstants.WRITING_CREDIT)) {
                    writingCredits.add(new ConcreteKeyValue(credit, display));
                } else if (display.contains("-- " + DegreeAuditConstants.CR_NO_CR_GRADING_OPTION)) {
                    crNcCredits.add(new ConcreteKeyValue(credit, display));
                } else {
                    normalCredits.add(new ConcreteKeyValue(credit, display));
                }
            }

            if (!bothHWCredits.isEmpty() || !honorCredits.isEmpty() || !writingCredits.isEmpty()) {
                keyValues.add(new ConcreteKeyValue(DegreeAuditConstants.DEFAULT_KEY, DegreeAuditConstants.DEFAULT_SELECT_AND_MORE));
            } else {
                keyValues.add(new ConcreteKeyValue(DegreeAuditConstants.DEFAULT_KEY, DegreeAuditConstants.DEFAULT_SELECT));
            }
            //Sorting and adding normal credits to keyValues
            Collections.sort(normalCredits, new Comparator<KeyValue>() {
                @Override
                public int compare(KeyValue val1, KeyValue val2) {
                    String compVal1 = val1.getKey().split(":")[CREDIT_INDEX].trim();
                    String compVal2 = val2.getKey().split(":")[CREDIT_INDEX].trim();
                    return Double.valueOf(compVal1).compareTo(Double.valueOf(compVal2));
                }
            });
            keyValues.addAll(normalCredits);
            //Sorting and adding writing credits to keyValues
            Collections.sort(writingCredits, new Comparator<KeyValue>() {
                @Override
                public int compare(KeyValue val1, KeyValue val2) {
                    String compVal1 = val1.getKey().split(":")[CREDIT_INDEX].trim();
                    String compVal2 = val2.getKey().split(":")[CREDIT_INDEX].trim();
                    return Double.valueOf(compVal1).compareTo(Double.valueOf(compVal2));
                }
            });
            keyValues.addAll(writingCredits);
            //Sorting and adding honor credits to keyValues
            Collections.sort(honorCredits, new Comparator<KeyValue>() {
                @Override
                public int compare(KeyValue val1, KeyValue val2) {
                    String compVal1 = val1.getKey().split(":")[CREDIT_INDEX].trim();
                    String compVal2 = val2.getKey().split(":")[CREDIT_INDEX].trim();
                    return Double.valueOf(compVal1).compareTo(Double.valueOf(compVal2));
                }
            });
            keyValues.addAll(honorCredits);
            //Sorting and adding bothHW credits to keyValues
            Collections.sort(bothHWCredits, new Comparator<KeyValue>() {
                @Override
                public int compare(KeyValue val1, KeyValue val2) {
                    String compVal1 = val1.getKey().split(":")[CREDIT_INDEX].trim();
                    String compVal2 = val2.getKey().split(":")[CREDIT_INDEX].trim();
                    return Double.valueOf(compVal1).compareTo(Double.valueOf(compVal2));
                }
            });
            keyValues.addAll(bothHWCredits);

            //Sorting and adding bothHW credits to keyValues
            Collections.sort(crNcCredits, new Comparator<KeyValue>() {
                @Override
                public int compare(KeyValue val1, KeyValue val2) {
                    String compVal1 = val1.getKey().split(":")[CREDIT_INDEX].trim();
                    String compVal2 = val2.getKey().split(":")[CREDIT_INDEX].trim();
                    return Double.valueOf(compVal1).compareTo(Double.valueOf(compVal2));
                }
            });
            keyValues.addAll(crNcCredits);
        }

        return keyValues;
    }
}
