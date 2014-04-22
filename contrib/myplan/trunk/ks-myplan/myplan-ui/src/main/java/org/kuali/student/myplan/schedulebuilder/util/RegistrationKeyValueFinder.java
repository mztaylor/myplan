package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemanthg on 4/21/2014.
 */
public class RegistrationKeyValueFinder extends UifKeyValuesFinderBase {
    /**
     * @see org.kuali.rice.krad.uif.control.UifKeyValuesFinder#getKeyValues(org.kuali.rice.krad.uif.view.ViewModel, org.kuali.rice.krad.uif.field.InputField)
     */
    @Override
    public List<KeyValue> getKeyValues(ViewModel model, InputField field) {


        List<KeyValue> keyValues = new ArrayList<KeyValue>();

        ActivityOption activityOption = ObjectPropertyUtils.getPropertyValue(model, field.getBindingInfo().getBindByNamePrefix());
        keyValues.add(new ConcreteKeyValue(activityOption.getRegistrationCode(), "<div id='testinline'>" + activityOption.getActivityCode() + "</div>"));
        for (ActivityOption alternate : activityOption.getAlternateActivties()) {
            keyValues.add(new ConcreteKeyValue(alternate.getRegistrationCode(), "<div id='testinline'>" + alternate.getActivityCode() + "</div>"));
        }
        setAddBlankOption(false);
        return keyValues;
    }

}
