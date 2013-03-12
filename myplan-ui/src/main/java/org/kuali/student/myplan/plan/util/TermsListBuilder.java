package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.myplan.plan.PlanConstants;

import static org.kuali.student.myplan.plan.util.AtpHelper.*;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

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

        /*TODO Once First and last dates in the Atp table are added change the way of populating the future terms by Using the method AtpService.getAtpsByDates*/
        String firstPlanATP = AtpHelper.getFirstPlanTerm();
        List<YearTerm> futureAtpIds = AtpHelper.getFutureYearTerms(firstPlanATP, null);
        for (YearTerm yearTerm : futureAtpIds) {
            ConcreteKeyValue concreteKeyValue = new ConcreteKeyValue(yearTerm.toATP(), yearTerm.toLabel());
            list.add(concreteKeyValue);
        }
        return list;
    }
}

