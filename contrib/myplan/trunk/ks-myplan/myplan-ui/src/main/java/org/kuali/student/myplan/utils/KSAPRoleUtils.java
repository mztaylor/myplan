package org.kuali.student.myplan.utils;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.uif.view.ViewAuthorizerBase;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hemanth on 7/29/14.
 */
public class KSAPRoleUtils extends ViewAuthorizerBase {

    private static UserSessionHelper userSessionHelper;


    /**
     * This Utility Method is used to define if the user is authorized to view the component or not.
     * <p/>
     * expectedRoles param takes in a comma separated role names.
     * <p/>
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
        return getUserSessionHelper().authorizedByTemplate(principalId, roleQualifiers, GlobalConstants.MYPLAN_VIEW_COMPONENT_TEMPLATE_NAME);
    }


    public static UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = (UserSessionHelper) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/userSession", "UserSessionHelper"));
        }

        return userSessionHelper;
    }

    public static void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        KSAPRoleUtils.userSessionHelper = userSessionHelper;
    }


}
