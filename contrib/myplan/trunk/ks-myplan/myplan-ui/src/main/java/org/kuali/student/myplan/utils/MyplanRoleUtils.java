package org.kuali.student.myplan.utils;

import org.kuali.rice.krad.uif.view.ViewAuthorizerBase;
import org.kuali.rice.krad.util.KRADConstants;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hemanth on 7/29/14.
 */
public class MyplanRoleUtils extends ViewAuthorizerBase {


    public static final String AUTHORIZED_TO_VIEW = "authorizedToView";

    /**
     * This Utility Method is used to define if the user is authorized to view the component or not.
     *
     * @param principalId
     * @param expectedRoles
     * @return
     */
    public static boolean principalHasRole(String principalId, String expectedRoles) {
        Map<String, String> roleQualifiers = new HashMap<String, String>();
        roleQualifiers.put(AUTHORIZED_TO_VIEW, expectedRoles);
        boolean isAuthorized = getPermissionService().isAuthorizedByTemplate(principalId, KRADConstants.KRAD_NAMESPACE, "View", new HashMap<String, String>(), roleQualifiers);
        return isAuthorized;

    }

}
