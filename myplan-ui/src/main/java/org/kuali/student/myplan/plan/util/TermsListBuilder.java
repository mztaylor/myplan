package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.myplan.plan.dataobject.RecommendedItemDataObject;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

import static org.kuali.student.myplan.plan.util.AtpHelper.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Assembles a list of published terms.
 */
public class TermsListBuilder extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(TermsListBuilder.class);


    /**
     * Creates KeyValue list containing MAX_FUTURE_YEARS terms, beginning with the current ATP
     *
     * @return A List of available terms as KeyValue items.
     */

    @Override

    public List<KeyValue> getKeyValues() {

        List<KeyValue> list = new ArrayList<KeyValue>();

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        List<RecommendedItemDataObject> recommendedItemDataObjects = (List<RecommendedItemDataObject>) request.getAttribute("recommendedItems");
        List<String> recommendedTerms = new ArrayList<String>();
        if (!CollectionUtils.isEmpty(recommendedItemDataObjects)) {
            for (RecommendedItemDataObject recommendedItemDataObject : recommendedItemDataObjects) {
                recommendedTerms.add(recommendedItemDataObject.getAtpId());
            }
        }
        /*TODO Once First and last dates in the Atp table are added change the way of populating the future terms by Using the method AtpService.getAtpsByDates*/
        String firstPlanATP = AtpHelper.getFirstPlanTerm();
        List<YearTerm> futureAtpIds = AtpHelper.getFutureYearTerms(firstPlanATP, null);
        for (YearTerm yearTerm : futureAtpIds) {
            String atpId = yearTerm.toATP();
            String label = recommendedTerms.contains(atpId) ? String.format("%s - Adviser Recommended", yearTerm.toLabel()) : yearTerm.toLabel();
            ConcreteKeyValue concreteKeyValue = new ConcreteKeyValue(atpId, label);
            list.add(concreteKeyValue);
        }
        return list;
    }
}

