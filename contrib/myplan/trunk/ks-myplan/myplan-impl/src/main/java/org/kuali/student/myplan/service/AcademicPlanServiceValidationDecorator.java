package org.kuali.student.myplan.service;

import org.apache.ws.security.util.StringUtil;
import org.kuali.student.enrollment.lui.dto.LuiInfo;
import org.kuali.student.enrollment.lui.dto.LuiLuiRelationInfo;
import org.kuali.student.enrollment.lui.service.LuiServiceDecorator;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceDecorator;
import org.kuali.student.r2.common.datadictionary.DataDictionaryValidator;
import org.kuali.student.r2.common.datadictionary.service.DataDictionaryService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.infc.HoldsDataDictionaryService;
import org.kuali.student.r2.common.infc.HoldsValidator;
import org.kuali.student.r2.core.service.util.ValidationUtils;

import org.apache.commons.lang.StringUtils;

import java.util.List;

public class AcademicPlanServiceValidationDecorator
        extends AcademicPlanServiceDecorator
        implements HoldsValidator, HoldsDataDictionaryService {

    private DataDictionaryValidator validator;

    private DataDictionaryService dataDictionaryService;

    @Override
    public DataDictionaryService getDataDictionaryService() {
        return dataDictionaryService;
    }

    @Override
    public void setDataDictionaryService(DataDictionaryService dataDictionaryService) {
        this.dataDictionaryService = dataDictionaryService;
    }

    @Override
    public DataDictionaryValidator getValidator() {
        return validator;
    }
    @Override
    public void setValidator(DataDictionaryValidator validator) {
        this.validator = validator;
    }

    @Override
    public StatusInfo deletePlanItem(String planItemId, ContextInfo context)
        throws OperationFailedException, InvalidParameterException, MissingParameterException, DoesNotExistException, PermissionDeniedException {
        validateNullOrEmptyPlanItemId(planItemId);
        return getNextDecorator().deletePlanItem(planItemId, context);
    }

    private void validateNullOrEmptyPlanItemId(String planItemId) throws MissingParameterException, InvalidParameterException {
        if (planItemId == null) {
            throw new MissingParameterException("planItemId was Null");
        }

        if (StringUtils.isEmpty(planItemId)) {
            throw new MissingParameterException("planItemId was an empty string.");
        }
    }

}
