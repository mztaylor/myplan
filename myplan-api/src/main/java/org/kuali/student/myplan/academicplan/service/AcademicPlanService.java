package org.kuali.student.myplan.academicplan.service;

import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemSetInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.infc.PlanItemSet;
import org.kuali.student.r2.common.datadictionary.service.DataDictionaryService;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.service.TypeService;

import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import java.util.List;

/**
 * Academic Plan service provides a means for students to specify plans for taking courses and other requirements at various time periods to meet
 * certain program goals.
 *
 * Things available through learning plan :
 *
 * <ol>
 *     <li>Selecting courses, course sets, requirements, certifications etc for a specific time period</li>
 *     <li>Selecting courses and adding them to a wish list</li>
 *     <li>Selecting courses for doing a what if audit</li>
 *     <li>Creating multiple plans (pathways) to meet program goals</li>
 *     <li>Template plans</li>
 *     <li>Planning for </li>
 * </ol>
 *
 * @version 1.0 (Dev)
 *
 * @Author Kamal
 * Date: 1/6/12
 */
@WebService(name = "AcademicPlanService", serviceName ="AcademicPlanService", portName = "AcademicPlanService", targetNamespace = AcademicPlanServiceConstants.NAMESPACE)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface AcademicPlanService  extends DataDictionaryService, TypeService {


    public LearningPlan getLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;


    public PlanItem getPlanItem(@WebParam(name = "planItemId") String planItemId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;


    public List<PlanItem> getPlanItemsInPlanByType(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;


    public List<PlanItem> getPlanItemsInPlan(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;


    public List<PlanItem> getPlanItemsInPlanByAtp(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "planItemTypeKey") String planItemTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;


    public PlanItemSet getPlanItemSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;


    public List<PlanItem> getPlanItemsInSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;


    public List<LearningPlan> getLearningPlansForStudentByType(@WebParam(name = "studentId") String studentId, @WebParam(name = "planTypeKey") String planTypeKey, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException;


    public LearningPlanInfo createLearningPlan(@WebParam(name = "learningPlan")LearningPlanInfo learningPlan, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;


    public PlanItemInfo createPlanItem(@WebParam(name = "planItem")PlanItemInfo planItem, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;


    public PlanItemSetInfo createPlanItemSet(@WebParam(name = "planItemSet")PlanItemSetInfo planItemSet, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;


    public LearningPlanInfo updateLearningPlan(@WebParam(name = "learningPlanId")String learningPlanId, @WebParam(name = "learningPlan")LearningPlanInfo learningPlan, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;


    public PlanItemInfo updatePlanItem(@WebParam(name = "planItemId")String planItemId, @WebParam(name = "planItem")PlanItemInfo planItem, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;


    public PlanItemSetInfo updatePlanItemSet(@WebParam(name = "planItemSetId")String planItemSetId, @WebParam(name = "planItemSet")PlanItemSetInfo planItemSet, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;


    public StatusInfo deleteLearningPlan(@WebParam(name = "learningPlanId")String learningPlanId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    public StatusInfo deletePlanItem(@WebParam(name = "planItemId")String planItemId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;

    public StatusInfo deletePlanItemSet(@WebParam(name = "planItemSetId")String planItemSetId, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException;



}
