package org.kuali.rice.krad.uif.view;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.lookup.LookupViewAuthorizerBase;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.widget.Widget;
import org.kuali.rice.krad.util.KRADConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hemanthg on 6/26/2014.
 */
public class MyplanLookupViewAuthorizer extends LookupViewAuthorizerBase {

    @Override
    public boolean canOpenView(View view, ViewModel model, Person user) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.putAll(view.getComponentSecurity().getAdditionalRoleQualifiers());

        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), KRADConstants.KRAD_NAMESPACE, "View", new HashMap<String, String>(), roleQualifiers);

        return isAuthorized;
    }


    @Override
    public boolean canViewGroup(View view, ViewModel model, Group group, String groupId, Person user) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.putAll(group.getComponentSecurity().getAdditionalRoleQualifiers());

        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), KRADConstants.KRAD_NAMESPACE, "View Group", new HashMap<String, String>(), roleQualifiers);

        return isAuthorized;
    }


    @Override
    public boolean canViewField(View view, ViewModel model, Field field, String propertyName, Person user) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.putAll(field.getComponentSecurity().getAdditionalRoleQualifiers());

        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), KRADConstants.KRAD_NAMESPACE, "View Field", new HashMap<String, String>(), roleQualifiers);

        return isAuthorized;
    }

    @Override
    public boolean canViewWidget(View view, ViewModel model, Widget widget, String widgetId, Person user) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.putAll(widget.getComponentSecurity().getAdditionalRoleQualifiers());

        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), KRADConstants.KRAD_NAMESPACE, "View Widget", new HashMap<String, String>(), roleQualifiers);

        return isAuthorized;
    }

}
