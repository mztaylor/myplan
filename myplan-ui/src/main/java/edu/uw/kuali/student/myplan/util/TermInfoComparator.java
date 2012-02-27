package edu.uw.kuali.student.myplan.util;

import org.kuali.student.core.atp.dto.AtpTypeInfo;

import java.util.Comparator;
import java.util.List;

/**
 * Sorts the AtpTypesInfo in order of terms in a academic calendar
 * User: Kamal
 * Date: 12/16/11
 * Time: 10:30 AM
 * To change this template use File | Settings | File Templates.
 */
public class TermInfoComparator implements Comparator<AtpTypeInfo> {

    @Override
    public int compare(AtpTypeInfo o1, AtpTypeInfo o2) {
        String atpKey1 = o1.getId().replaceAll("\\.","_").toUpperCase();
        String atpKey2 = o2.getId().replaceAll("\\.","_").toUpperCase();
        return TermOrder.valueOf(atpKey1).compareTo(TermOrder.valueOf(atpKey2));
    }

    private enum TermOrder {
          KUALI_UW_ATP_TYPE_AUTUMN,
          KUALI_UW_ATP_TYPE_WINTER,
          KUALI_UW_ATP_TYPE_SPRING,
          KUALI_UW_ATP_TYPE_SUMMER
    }
}
