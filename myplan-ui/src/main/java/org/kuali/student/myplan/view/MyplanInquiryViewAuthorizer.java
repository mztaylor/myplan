package org.kuali.student.myplan.view;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.inquiry.InquiryViewAuthorizerBase;
import org.kuali.rice.krad.uif.container.Group;
import org.kuali.rice.krad.uif.field.Field;
import org.kuali.rice.krad.uif.view.View;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.rice.krad.uif.widget.Widget;
import org.kuali.rice.krad.util.KRADConstants;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.GlobalConstants;
import org.kuali.student.myplan.utils.MyplanRoleUtils;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hemanthg on 6/26/2014.
 */
public class MyplanInquiryViewAuthorizer extends InquiryViewAuthorizerBase {


    @Override
    public boolean canOpenView(View view, ViewModel model, Person user) {
        return MyplanRoleUtils.principalHasRole(user.getPrincipalId(),view.getComponentSecurity().getAdditionalRoleQualifiers().get(GlobalConstants.AUTHORIZED_TO_VIEW));
    }


    @Override
    public boolean canViewGroup(View view, ViewModel model, Group group, String groupId, Person user) {
        return MyplanRoleUtils.principalHasRole(user.getPrincipalId(),group.getComponentSecurity().getAdditionalRoleQualifiers().get(GlobalConstants.AUTHORIZED_TO_VIEW));
    }


    @Override
    public boolean canViewField(View view, ViewModel model, Field field, String propertyName, Person user) {
        return MyplanRoleUtils.principalHasRole(user.getPrincipalId(),field.getComponentSecurity().getAdditionalRoleQualifiers().get(GlobalConstants.AUTHORIZED_TO_VIEW));
    }

    @Override
    public boolean canViewWidget(View view, ViewModel model, Widget widget, String widgetId, Person user) {
        return MyplanRoleUtils.principalHasRole(user.getPrincipalId(),widget.getComponentSecurity().getAdditionalRoleQualifiers().get(GlobalConstants.AUTHORIZED_TO_VIEW));
    }



}
