package org.kuali.student.myplan.plan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.keyvalues.KeyValuesBase;

import java.util.ArrayList;
import java.util.List;

/**
 *  Assembles a list of published terms.
 */
public class TermsListBuilder extends KeyValuesBase {

    private final Logger logger = Logger.getLogger(TermsListBuilder.class);

    private int yearCount = 5;

    /**
     *  Build and returns the list of available terms.
     *
     * @return A List of available terms as KeyValue items.
     */
    @Override
    public List<KeyValue> getKeyValues() {

        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        //  FIXME: Get this from the term service.
        keyValues.add(new ConcreteKeyValue("autumn", "Autumn"));
        keyValues.add(new ConcreteKeyValue("winter", "Winter"));
        keyValues.add(new ConcreteKeyValue("spring", "Spring"));
        keyValues.add(new ConcreteKeyValue("summer", "Summer"));

        return keyValues;
    }
}
