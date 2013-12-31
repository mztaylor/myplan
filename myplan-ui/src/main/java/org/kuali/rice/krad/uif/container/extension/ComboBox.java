package org.kuali.rice.krad.uif.container.extension;

import org.kuali.rice.krad.datadictionary.parse.BeanTag;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.widget.Suggest;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 12/30/13
 * Time: 2:24 PM
 * To change this template use File | Settings | File Templates.
 */
@BeanTag(name = "suggest-bean", parent = "KSAP-ComboBox")
public class ComboBox extends Suggest {

    public final static String KEY_VALUE_SEPARATOR = ":";

    public ComboBox() {
        super();
    }

    /**
     * Adjusts the query field mappings on the query based on the binding configuration of the field
     *
     * @param view
     * @param model
     * @param parent
     */
    @Override
    public void performFinalize(View view, Object model, Component parent) {
        super.performFinalize(view, model, parent);
    }
}
