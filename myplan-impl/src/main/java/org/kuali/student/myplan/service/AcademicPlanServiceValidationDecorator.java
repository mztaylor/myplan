package org.kuali.student.myplan.service;

import org.kuali.student.enrollment.lui.dto.LuiInfo;
import org.kuali.student.enrollment.lui.dto.LuiLuiRelationInfo;
import org.kuali.student.enrollment.lui.service.LuiServiceDecorator;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceDecorator;
import org.kuali.student.r2.common.datadictionary.DataDictionaryValidator;
import org.kuali.student.r2.common.datadictionary.service.DataDictionaryService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.infc.HoldsDataDictionaryService;
import org.kuali.student.r2.common.infc.HoldsValidator;
import org.kuali.student.r2.core.service.util.ValidationUtils;

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
}
