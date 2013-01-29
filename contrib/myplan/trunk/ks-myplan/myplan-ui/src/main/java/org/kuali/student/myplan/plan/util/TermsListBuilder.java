package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;
import org.kuali.student.core.atp.dto.AtpInfo;
import org.kuali.student.core.atp.service.impl.AtpServiceImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Assembles a list of published terms.
 */
public class TermsListBuilder extends KeyValuesBase {

    // 5 years * 4 quarters = 20 terms
    private static int MAX_FUTURE_TERMS = 20;

    public static final Pattern atpRegex = Pattern.compile("kuali\\.uw\\.atp\\.([0-9]{4})\\.([1-4])");
    public static final String atpFormat = "kuali.uw.atp.%d.%d";

    private final Logger logger = Logger.getLogger(TermsListBuilder.class);


    /**
     * Creates KeyValue list containing MAX_FUTURE_TERMS terms, beginning with the current ATP
     *
     * @return A List of available terms as KeyValue items.
     */

    @Override

    public List<KeyValue> getKeyValues() {

        List<KeyValue> list = new ArrayList<KeyValue>(MAX_FUTURE_TERMS);

        /*TODO Once First and last dates in the Atp table are added change the way of populating the future terms by Using the method AtpService.getAtpsByDates*/
        String firstPlanATP = AtpHelper.getFirstPlanTerm();
        if (firstPlanATP != null) {
            Matcher m = atpRegex.matcher(firstPlanATP);
            if (m.find()) {
                int year = Integer.parseInt(m.group(1));
                int term = Integer.parseInt(m.group(2));

                for (int x = 0; x < MAX_FUTURE_TERMS; x++) {
                    String atp = String.format(atpFormat, year, term);
                    if (AtpHelper.doesAtpExist(atp)) {
                        String value = AtpHelper.atpIdToTermName(atp);
                        ConcreteKeyValue concrete = new ConcreteKeyValue(atp, value);
                        list.add(concrete);
                        term++;
                        if (term > 4) {
                            term = 1;
                            year++;
                        }
                    }
                }
            }
        }
        return list;
    }
}

