package org.kuali.rice.krad.uif.container.extension;

import org.kuali.rice.krad.datadictionary.parse.BeanTagAttribute;
import org.kuali.rice.krad.uif.UifConstants;
import org.kuali.rice.krad.uif.component.Component;
import org.kuali.rice.krad.uif.container.CollectionGroup;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.widget.Tabs;
import org.kuali.rice.krad.uif.widget.extension.KSAPTabs;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * CollectionGroup that holds a Tab widget to enable rendering of each item in the collection as
 * a tab of its own.
 * <p/>
 * <p>
 * All tab widget configuration should be done using the templateOptionsJSString on the tabWidget
 * instead of the CollectionGroup.
 * </p>
 *
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *         Date: 2/13/13
 */
public class TabCollectionGroup extends CollectionGroup {
    private static final long serialVersionUID = 3L;

    private KSAPTabs tabsWidget;

    public TabCollectionGroup() {
        super();
    }


    /**
     * @see org.kuali.rice.krad.uif.component.ComponentBase#getComponentsForLifecycle()
     */
    @Override
    public List<Component> getComponentsForLifecycle() {
        List<Component> components = super.getComponentsForLifecycle();

        components.add(tabsWidget);

        return components;
    }

    @Override
    public void performFinalize(View view, Object model, Component parent) {
        super.performFinalize(view, model, parent);
        this.addDataAttribute(UifConstants.DataAttributes.TYPE, "Uif-TabCollectionGroup");
    }

    /**
     * Only groups are supported for this group.
     *
     * @see org.kuali.rice.krad.web.view.container.ContainerBase#getSupportedComponents()
     */
    @Override
    public Set<Class<? extends Component>> getSupportedComponents() {
        Set<Class<? extends Component>> supportedComponents = new HashSet<Class<? extends Component>>();
        supportedComponents.add(Group.class);

        return supportedComponents;
    }


    /**
     * @see org.kuali.rice.krad.uif.component.ComponentBase#copy()
     */
    @Override
    protected <T> void copyProperties(T component) {
        super.copyProperties(component);
        TabCollectionGroup tabCollectionGroupCopy = (TabCollectionGroup) component;

        if(tabsWidget != null) {
            tabCollectionGroupCopy.setTabsWidget((KSAPTabs)this.getTabsWidget().copy());
        }
    }


    /**
     * Gets the widget which contains any configuration for the tab widget component used to render
     * this TabGroup
     *
     * @return the tabsWidget
     */
    @BeanTagAttribute(name = "tabsWidget", type = BeanTagAttribute.AttributeType.SINGLEBEAN)
    public KSAPTabs getTabsWidget() {
        return this.tabsWidget;
    }

    /**
     * @param tabsWidget the tabsWidget to set
     */
    public void setTabsWidget(KSAPTabs tabsWidget) {
        this.tabsWidget = tabsWidget;
    }

}
