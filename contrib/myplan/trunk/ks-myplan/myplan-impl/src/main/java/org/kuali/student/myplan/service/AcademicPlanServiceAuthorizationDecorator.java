package org.kuali.student.myplan.service;

import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.student.enrollment.lui.dto.LuiCapacityInfo;
import org.kuali.student.enrollment.lui.dto.LuiInfo;
import org.kuali.student.enrollment.lui.dto.LuiLuiRelationInfo;
import org.kuali.student.enrollment.lui.service.LuiServiceDecorator;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceDecorator;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.infc.HoldsPermissionService;

import java.util.List;

public class AcademicPlanServiceAuthorizationDecorator
        extends AcademicPlanServiceDecorator
        implements HoldsPermissionService {

	private PermissionService permissionService;
		
	@Override
	public PermissionService getPermissionService() {
		return permissionService;
	}

	@Override
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

     @Override
    public LearningPlan getLearningPlan(String learningPlanId, ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        if (null == context) {
            throw new MissingParameterException();
        }

        if (permissionService.isAuthorized(context.getPrincipalId(), AcademicPlanServiceConstants.NAMESPACE,
                AcademicPlanServiceConstants.SERVICE_NAME + "/getLearningPlan", null, null)) {
	        return getNextDecorator().getLearningPlan(learningPlanId, context);
        } else {
        	throw new OperationFailedException("Permission Denied.");
        }
    }
}
