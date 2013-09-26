package org.kuali.rice.krad.uif.widget.extension;

import org.kuali.rice.krad.datadictionary.parse.BeanTag;
import org.kuali.rice.krad.datadictionary.parse.BeanTagAttribute;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.component.ClientSideState;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.widget.WidgetBase;

/**
 * Widget used for configuring tab options, This one does not require TabGroup as its parent but otherwise is
 * similar to KRAD Tabs widget
 * See http://jqueryui.com/demos/tabs/ for usable options
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@BeanTag(name = "tabs-bean", parent = "Uif-KSAPTabs")
public class KSAPTabs extends WidgetBase {

    private static final long serialVersionUID = 2L;

    @ClientSideState(variableName = "activeTab")
    private String defaultActiveTabId;

    private UifConstants.Position position = UifConstants.Position.TOP;

    public KSAPTabs() {
        super();
    }

    /**
     * The following is performed:
     *
     * <ul>
     * <li>If the active tab id is configured, set the active plugin option</li>
     * </ul>
     */
    @Override
    public void performFinalize(View view, Object model, Component component) {
        super.performFinalize(view, model, component);
    }

    /**
     * Id for the group within the tab group that should be active (shown first), by default the first
     * group is active
     *
     * @return id for the group within the tab group that should be initially active
     */
    @BeanTagAttribute(name = "defaultActiveTabId")
    public String getDefaultActiveTabId() {
        return defaultActiveTabId;
    }

    /**
     * Setter for the active group id
     *
     * @param defaultActiveTabId
     */
    public void setDefaultActiveTabId(String defaultActiveTabId) {
        this.defaultActiveTabId = defaultActiveTabId;
    }

    /**
     * The position the tabs will appear related to the group, options are TOP, BOTTOM, RIGHT, or LEFT
     *
     * @return position for tabs
     */
    @BeanTagAttribute(name = "position")
    public UifConstants.Position getPosition() {
        return position;
    }

    /**
     * Setter for the tabs position
     *
     * @param position
     */
    public void setPosition(UifConstants.Position position) {
        this.position = position;
    }

    /**
     * @see org.kuali.rice.krad.uif.component.ComponentBase#copy()
     */
    @Override
    protected <T> void copyProperties(T component) {
        super.copyProperties(component);
        KSAPTabs tabsCopy = (KSAPTabs) component;
        tabsCopy.setDefaultActiveTabId(this.getDefaultActiveTabId());
        tabsCopy.setPosition(this.getPosition());
    }

}
