package org.kuali.student.myplan.academicplan.service;

import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemSetInfo;
import org.kuali.student.r2.common.datadictionary.DataDictionaryValidator;
import org.kuali.student.r2.common.datadictionary.service.DataDictionaryService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.infc.HoldsDataDictionaryService;
import org.kuali.student.r2.common.infc.HoldsValidator;

import org.apache.commons.lang.StringUtils;
import org.kuali.student.r2.core.service.util.ValidationUtils;

import javax.jws.WebParam;
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
    public LearningPlanInfo getLearningPlan(String learningPlanId, ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        validateNullOrEmptyArgument("learningPlanId", learningPlanId);
        return getNextDecorator().getLearningPlan(learningPlanId, context);
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlan(String learningPlanId, ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
         validateNullOrEmptyArgument("learningPlanId", learningPlanId);
         return getNextDecorator().getPlanItemsInPlan(learningPlanId, context);
    }

    @Override
    public List<LearningPlanInfo> getLearningPlansForStudentByType(String studentId, String planTypeKey, ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        validateNullOrEmptyArgument("studentId", studentId);
        validateNullOrEmptyArgument("planTypeKey", planTypeKey);
        return getNextDecorator().getLearningPlansForStudentByType(studentId, planTypeKey, context);
    }

    @Override
    public LearningPlanInfo createLearningPlan(LearningPlanInfo learningPlan, ContextInfo context)
        throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {
        fullValidation(learningPlan, context);
        return getNextDecorator().createLearningPlan(learningPlan, context);
    }

    @Override
    public PlanItemInfo createPlanItem(PlanItemInfo planItem, ContextInfo context)
        throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {
        fullValidation(planItem, context);
        return getNextDecorator().createPlanItem(planItem, context);
    }

	@Override
	public List<ValidationResultInfo> validateLearningPlan(String validationType, LearningPlanInfo learningPlanInfo, ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        List<ValidationResultInfo> errors;
        try {
            errors = ValidationUtils.validateInfo(validator, validationType, learningPlanInfo, context);

            List<ValidationResultInfo> nextDecoratorErrors =
                getNextDecorator().validateLearningPlan(validationType, learningPlanInfo, context);

            if (null != nextDecoratorErrors) {
                errors.addAll(nextDecoratorErrors);
            }
        } catch (DoesNotExistException ex) {
            throw new OperationFailedException("Error validating learning plan.", ex);
        }
        return errors;
	}

    @Override
	public List<ValidationResultInfo> validatePlanItem(String validationType, PlanItemInfo planItemInfo, ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<ValidationResultInfo> errors;
        try {
            errors = ValidationUtils.validateInfo(validator, validationType, planItemInfo, context);

            List<ValidationResultInfo> nextDecoratorErrors =
                getNextDecorator().validatePlanItem(validationType, planItemInfo, context);

            if (null != nextDecoratorErrors) {
                errors.addAll(nextDecoratorErrors);
            }
        } catch (DoesNotExistException ex) {
            throw new OperationFailedException("Error validating plan item.", ex);
        }
        return errors;
	}

    @Override
    public List<ValidationResultInfo> validatePlanItemSet(@WebParam(name = "validationType") String validationType,
                                                          @WebParam(name = "planItemSetInfo") PlanItemSetInfo planItemSetInfo,
                                                          @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<ValidationResultInfo> errors;
        try {
            errors = ValidationUtils.validateInfo(validator, validationType, planItemSetInfo, context);

            List<ValidationResultInfo> nextDecoratorErrors =
                getNextDecorator().validatePlanItemSet(validationType, planItemSetInfo, context);

            if (null != nextDecoratorErrors) {
                errors.addAll(nextDecoratorErrors);
            }
        } catch (DoesNotExistException ex) {
            throw new OperationFailedException("Error validating plan item set.", ex);
        }
        return errors;
    }

    /**
     * Data dictionary validation for LearningPlanInfo.
     */
    private void fullValidation(LearningPlanInfo learningPlanInfo, ContextInfo context)
    		throws DataValidationErrorException, OperationFailedException, InvalidParameterException, MissingParameterException {

        if (learningPlanInfo == null) {
            throw new MissingParameterException("learningPlanInfo was null.");
        }

        try {
            List<ValidationResultInfo> errors =
                    this.validateLearningPlan(DataDictionaryValidator.ValidationType.FULL_VALIDATION.toString(), learningPlanInfo, context);
            if ( ! errors.isEmpty()) {
                throw new DataValidationErrorException("Error(s) validating learning plan.", errors);
            }
        } catch (DoesNotExistException ex) {
            throw new OperationFailedException("Error validating learning plan.", ex);
        }
    }

    /**
     * Data dictionary validation for PlanItemInfo.
     */
    private void fullValidation(PlanItemInfo planItemInfo, ContextInfo context)
    		throws DataValidationErrorException, OperationFailedException, InvalidParameterException, MissingParameterException {

        if (planItemInfo == null) {
            throw new MissingParameterException("planItemInfo was null.");
        }

        try {
            List<ValidationResultInfo> errors =
                    this.validatePlanItem(DataDictionaryValidator.ValidationType.FULL_VALIDATION.toString(), planItemInfo, context);
            if ( ! errors.isEmpty()) {
                throw new DataValidationErrorException("Error(s) validating plan item.", errors);
            }
        } catch (DoesNotExistException ex) {
            throw new OperationFailedException("Error validating plan item.", ex);
        }
    }

    @Override
    public LearningPlanInfo updateLearningPlan(String learningPlanId, LearningPlanInfo learningPlan, ContextInfo context)
            throws DataValidationErrorException, InvalidParameterException,
                MissingParameterException, OperationFailedException, PermissionDeniedException, DoesNotExistException {
        validateNullOrEmptyArgument("learningPlanId", learningPlanId);
        fullValidation(learningPlan, context);
		return getNextDecorator().updateLearningPlan(learningPlanId, learningPlan, context);
    }

    @Override
    public PlanItemInfo updatePlanItem(String planItemId, PlanItemInfo planItem, ContextInfo context)
            throws DoesNotExistException, DataValidationErrorException, InvalidParameterException,
                    MissingParameterException, OperationFailedException, PermissionDeniedException {
        validateNullOrEmptyArgument("planItemId", planItemId);
        fullValidation(planItem, context);
        return getNextDecorator().updatePlanItem(planItemId, planItem, context);
    }

    @Override
    public StatusInfo deleteLearningPlan(String learningPlanId, ContextInfo context)
        throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException,
            PermissionDeniedException {
        validateNullOrEmptyArgument("learningPlanId", learningPlanId);
        return getNextDecorator().deleteLearningPlan(learningPlanId, context);
    }

    @Override
    public StatusInfo deletePlanItem(String planItemId, ContextInfo context)
        throws OperationFailedException, InvalidParameterException, MissingParameterException, DoesNotExistException,
            PermissionDeniedException {
        validateNullOrEmptyArgument("planItemId", planItemId);
        return getNextDecorator().deletePlanItem(planItemId, context);
    }

    private void validateNullOrEmptyArgument(String fieldName, String id)
        throws MissingParameterException, InvalidParameterException {
        if (id == null) {
            throw new MissingParameterException(fieldName + " was Null");
        }

        if (StringUtils.isEmpty(id)) {
            throw new MissingParameterException(fieldName + " was an empty string.");
        }
    }

}
