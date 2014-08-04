package org.kuali.student.myplan.utils;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.uif.view.ViewAuthorizerBase;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hemanth on 7/29/14.
 */
public class MyplanRoleUtils extends ViewAuthorizerBase {

    private static UserSessionHelper userSessionHelper;


    /**
     * This Utility Method is used to define if the user is authorized to view the component or not.
     *
     * expectedRoles param takes in a comma separated role names.
     *
     * For eg: principalHasRole("730FA4DCAE3411D689DA0004AC494FFE","STUDENT,NON-STUDENT,ADVISER")
     *
     * @param principalId
     * @param expectedRoles
     * @return
     */
    public static boolean principalHasRole(String principalId, String expectedRoles) {

        /*Short circuiting because if no roles are being expected out of the user then it is always true to render the component*/
        if (StringUtils.isBlank(expectedRoles)) {
            return true;
        }

        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.put(GlobalConstants.AUTHORIZED_TO_VIEW, expectedRoles);
        roleQualifiers.put(GlobalConstants.MYPLAN_ADVISER, String.valueOf(getUserSessionHelper().isAdviser()));
        return authorizedByTemplate(principalId, roleQualifiers, GlobalConstants.MYPLAN_VIEW_COMPONENT_TEMPLATE_NAME);
    }

    public static boolean authorizedByTemplate(String userId, Map<String, String> roleQualifiers, String permissionTemplateName) {
        return getPermissionService().isAuthorizedByTemplate(userId, KRADConstants.KRAD_NAMESPACE, permissionTemplateName, new HashMap<String, String>(), roleQualifiers);
    }


    public static UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }

        return userSessionHelper;
    }

    public static void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        MyplanRoleUtils.userSessionHelper = userSessionHelper;
    }


}
