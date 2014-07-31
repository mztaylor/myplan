package org.kuali.rice.krad.uif.view;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.inquiry.InquiryViewAuthorizerBase;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.widget.Widget;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.utils.GlobalConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hemanthg on 6/26/2014.
 */
public class MyplanInquiryViewAuthorizer extends InquiryViewAuthorizerBase {

    private UserSessionHelper userSessionHelper;

    @Override
    public boolean canOpenView(View view, ViewModel model, Person user) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.putAll(view.getComponentSecurity().getAdditionalRoleQualifiers());
        roleQualifiers.put(GlobalConstants.MYPLAN_ADVISER, String.valueOf(getUserSessionHelper().isAdviser()));

        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), KRADConstants.KRAD_NAMESPACE, "View", new HashMap<String, String>(), roleQualifiers);

        return isAuthorized;
    }


    @Override
    public boolean canViewGroup(View view, ViewModel model, Group group, String groupId, Person user) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.putAll(group.getComponentSecurity().getAdditionalRoleQualifiers());
        roleQualifiers.put(GlobalConstants.MYPLAN_ADVISER, String.valueOf(getUserSessionHelper().isAdviser()));

        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), KRADConstants.KRAD_NAMESPACE, "View Group", new HashMap<String, String>(), roleQualifiers);

        return isAuthorized;
    }


    @Override
    public boolean canViewField(View view, ViewModel model, Field field, String propertyName, Person user) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.putAll(field.getComponentSecurity().getAdditionalRoleQualifiers());
        roleQualifiers.put(GlobalConstants.MYPLAN_ADVISER, String.valueOf(getUserSessionHelper().isAdviser()));

        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), KRADConstants.KRAD_NAMESPACE, "View Field", new HashMap<String, String>(), roleQualifiers);

        return isAuthorized;
    }

    @Override
    public boolean canViewWidget(View view, ViewModel model, Widget widget, String widgetId, Person user) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.putAll(widget.getComponentSecurity().getAdditionalRoleQualifiers());
        roleQualifiers.put(GlobalConstants.MYPLAN_ADVISER, String.valueOf(getUserSessionHelper().isAdviser()));

        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(user.getPrincipalId(), KRADConstants.KRAD_NAMESPACE, "View Widget", new HashMap<String, String>(), roleQualifiers);

        return isAuthorized;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }

        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

}
