package org.kuali.student.myplan.academicplan.service;

import org.kuali.student.common.util.UUIDHelper;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemSetInfo;
import org.kuali.student.myplan.academicplan.dao.LearningPlanDao;
import org.kuali.student.myplan.academicplan.dao.LearningPlanTypeDao;
import org.kuali.student.myplan.academicplan.dao.PlanItemDao;
import org.kuali.student.myplan.academicplan.dao.PlanItemTypeDao;
import org.kuali.student.myplan.academicplan.model.*;
import org.kuali.student.r2.common.exceptions.AlreadyExistsException;
import org.kuali.student.r2.common.exceptions.DataValidationErrorException;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.kuali.student.r2.common.infc.Attribute;
import org.kuali.student.r2.common.dto.*;
import org.springframework.transaction.annotation.Transactional;

import javax.jws.WebParam;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Academic Plan Service Implementation.
 */
@Transactional(readOnly = true, noRollbackFor = {DoesNotExistException.class}, rollbackFor = {Throwable.class})
public class AcademicPlanServiceImpl implements AcademicPlanService {

    private LearningPlanDao learningPlanDao;
    private LearningPlanTypeDao learningPlanTypeDao;
    private PlanItemDao planItemDao;
    private PlanItemTypeDao planItemTypeDao;
    private CourseService courseService;

    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    public CourseService getCourseService() {
        return this.courseService;
    }

    public PlanItemDao getPlanItemDao() {
        return planItemDao;
    }

    public void setPlanItemDao(PlanItemDao planItemDao) {
        this.planItemDao = planItemDao;
    }

    public PlanItemTypeDao getPlanItemTypeDao() {
        return planItemTypeDao;
    }

    public void setPlanItemTypeDao(PlanItemTypeDao planItemTypeDao) {
        this.planItemTypeDao = planItemTypeDao;
    }

    public LearningPlanDao getLearningPlanDao() {
        return learningPlanDao;
    }

    public void setLearningPlanDao(LearningPlanDao learningPlanDao) {
        this.learningPlanDao = learningPlanDao;
    }

    public LearningPlanTypeDao getLearningPlanTypeDao() {
        return learningPlanTypeDao;
    }

    public void setLearningPlanTypeDao(LearningPlanTypeDao learningPlanTypeDao) {
        this.learningPlanTypeDao = learningPlanTypeDao;
    }

    @Override
    public LearningPlanInfo getLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId,
                                        @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        LearningPlanEntity lpe = learningPlanDao.find(learningPlanId);
        if (null == lpe) {
            throw new DoesNotExistException(learningPlanId);
        }

        LearningPlanInfo dto = lpe.toDto();
        return dto;
    }

    @Override
    public PlanItemInfo getPlanItem(@WebParam(name = "planItemId") String planItemId,
                                @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        PlanItemEntity planItem = planItemDao.find(planItemId);
        if (null == planItem) {
            throw new DoesNotExistException("Plan item with Id " + planItemId + " does not exist");
        }

        return planItem.toDto();
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByType(@WebParam(name = "learningPlanId") String learningPlanId,
                                                   @WebParam(name = "planItemTypeKey") String planItemTypeKey,
                                                   @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlan(@WebParam(name = "learningPlanId") String learningPlanId,
                                             @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        List<PlanItemInfo> dtos = new ArrayList<PlanItemInfo>();

        List<PlanItemEntity> planItems = planItemDao.getLearningPlanItems(learningPlanId);
        for (PlanItemEntity pie : planItems) {
            dtos.add(pie.toDto());
        }
        return dtos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByAtp(@WebParam(name = "learningPlanId") String learningPlanId,
                                                  @WebParam(name = "atpKey") String atpKey,
                                                  @WebParam(name = "planItemTypeKey") String planItemTypeKey,
                                                  @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public PlanItemSetInfo getPlanItemSet(@WebParam(name = "planItemSetId") String planItemSetId,
                                      @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInSet(@WebParam(name = "planItemSetId") String planItemSetId,
                                            @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<LearningPlanInfo> getLearningPlansForStudentByType(@WebParam(name = "studentId") String studentId,
                                                               @WebParam(name = "planTypeKey") String planTypeKey,
                                                               @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        List<LearningPlanEntity> lpeList = learningPlanDao.getLearningPlansByType(studentId, planTypeKey);

        List<LearningPlanInfo> learningPlanDtos = new ArrayList<LearningPlanInfo>();
        for (LearningPlanEntity lpe : lpeList) {
            learningPlanDtos.add(lpe.toDto());
        }

        return learningPlanDtos;
    }

    @Override
    @Transactional
    public LearningPlanInfo createLearningPlan(@WebParam(name = "learningPlan") LearningPlanInfo learningPlan,
                                               @WebParam(name = "context") ContextInfo context)
            throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {

        LearningPlanEntity lpe = new LearningPlanEntity();
        lpe.setId(UUIDHelper.genStringUUID());

        LearningPlanTypeEntity type = learningPlanTypeDao.find(learningPlan.getTypeKey());
        if (type == null) {
            throw new InvalidParameterException(String.format("Unknown type [%s].", learningPlan.getTypeKey()));
        }
        lpe.setLearningPlanType(type);

        lpe.setStudentId(learningPlan.getStudentId());
        lpe.setDescr(new LearningPlanRichTextEntity(learningPlan.getDescr()));

        LearningPlanEntity existing = learningPlanDao.find(lpe.getId());
        if (existing != null) {
            throw new AlreadyExistsException();
        }

        learningPlanDao.persist(lpe);

        return learningPlanDao.find(lpe.getId()).toDto();
    }

    @Override
    @Transactional
    public PlanItemInfo createPlanItem(@WebParam(name = "planItem") PlanItemInfo planItem,
                                       @WebParam(name = "context") ContextInfo context)
            throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {

        /*
         *  Validate that the course exists.
         * TODO: Move this validation to the data dictionary.
         */
        //try {
        //    this.courseService.getCourse(planItem.getRefObjectId());
        //} catch (Exception e) {
        //    System.err.println();
        //}


        PlanItemEntity pie = new PlanItemEntity();
        String planItemId = UUIDHelper.genStringUUID();
        pie.setId(planItemId);

        pie.setRefObjectId(planItem.getRefObjectId());
        pie.setRefObjectTypeKey(planItem.getRefObjectType());

        PlanItemTypeEntity planItemTypeEntity = planItemTypeDao.find(planItem.getTypeKey());
        if (planItemTypeEntity == null) {
            throw new InvalidParameterException(String.format("Unknown plan item type id [%s].", planItem.getTypeKey()));
        }
        pie.setLearningPlanItemType(planItemTypeEntity);

        //  Convert the List of plan periods to a Set.
        pie.setPlanPeriods(new HashSet<String>(planItem.getPlanPeriods()));

        //  Set attributes.
        pie.setAttributes(new ArrayList<PlanItemAttributeEntity>());
        if (planItem.getAttributes() != null) {
            for (Attribute att : planItem.getAttributes()) {
                PlanItemAttributeEntity attEntity = new PlanItemAttributeEntity(att);
                pie.getAttributes().add(attEntity);
            }
        }

        //  Create text entity.
        pie.setDescr(new PlanItemRichTextEntity(planItem.getDescr()));

        //  Set the learning plan.
        String planId = planItem.getLearningPlanId();
        if (planId == null) {
            throw new InvalidParameterException("Learning plan id was null.");
        }
        LearningPlanEntity plan = learningPlanDao.find(planItem.getLearningPlanId());
        if (plan == null) {
            throw new InvalidParameterException(String.format("Unknown learning plan id [%s]", planItem.getLearningPlanId()));
        }
        pie.setLearningPlan(plan);

        /*
         * Make sure a saved courses item with this course id doesn't already exist.
         * TODO: Move this validation to the data dictionary.
         */
        if (planItem.getTypeKey().equals(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
            List<PlanItemEntity> savedCourseListItems =
                    this.planItemDao.getLearningPlanItems(plan.getId(), AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST);

            for (PlanItemEntity p : savedCourseListItems) {
                if (p.getRefObjectId().equals(planItem.getRefObjectId())) {
                    throw new AlreadyExistsException("This course id already exists in the user's saved course list.");
                }
            }
        }

        PlanItemEntity existing = planItemDao.find(planItemId);
        if (existing != null) {
            throw new AlreadyExistsException();
        }

        planItemDao.persist(pie);

        return planItemDao.find(planItemId).toDto();
    }

    @Override
    public PlanItemSetInfo createPlanItemSet(@WebParam(name = "planItemSet") PlanItemSetInfo planItemSet,
                                             @WebParam(name = "context") ContextInfo context)
            throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    @Transactional
    public LearningPlanInfo updateLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId,
                                               @WebParam(name = "learningPlan") LearningPlanInfo learningPlan,
                                               @WebParam(name = "context") ContextInfo context)
            throws DataValidationErrorException, InvalidParameterException,
            MissingParameterException, OperationFailedException, PermissionDeniedException, DoesNotExistException {

        LearningPlanEntity lpe = learningPlanDao.find(learningPlanId);
        if (lpe == null) {
            throw new DoesNotExistException(learningPlanId);
        }

        lpe.setStudentId(learningPlan.getStudentId());
        lpe.setDescr(new LearningPlanRichTextEntity(learningPlan.getDescr()));


        lpe.setAttributes(new ArrayList<LearningPlanAttributeEntity>());
        if (null != learningPlan.getAttributes()) {
            for (Attribute att : learningPlan.getAttributes()) {
                LearningPlanAttributeEntity attEntity = new LearningPlanAttributeEntity(att);
                lpe.getAttributes().add(attEntity);
            }
        }

        learningPlanDao.merge(lpe);
        return learningPlanDao.find(learningPlanId).toDto();
    }

    @Override
    @Transactional
    public PlanItemInfo updatePlanItem(@WebParam(name = "planItemId") String planItemId,
                                       @WebParam(name = "planItem") PlanItemInfo planItem,
                                       @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, DataValidationErrorException, InvalidParameterException,
            MissingParameterException, OperationFailedException, PermissionDeniedException {

        //  See if the plan item exists before trying to update it.
        PlanItemEntity planItemEntity = planItemDao.find(planItemId);
        if (planItemEntity == null) {
            throw new DoesNotExistException(planItemId);
        }

        planItemEntity.setRefObjectId(planItem.getRefObjectId());
        planItemEntity.setRefObjectTypeKey(planItem.getRefObjectType());

        //  Update the plan item type if it has changed.
        if ( ! planItemEntity.getLearningPlanItemType().getId().equals(planItem.getTypeKey())) {
            PlanItemTypeEntity planItemTypeEntity = planItemTypeDao.find(planItem.getTypeKey());
            if (planItemTypeEntity == null) {
                throw new InvalidParameterException(String.format("Unknown plan item type id [%s].", planItem.getTypeKey()));
            }
            planItemEntity.setLearningPlanItemType(planItemTypeEntity);
        }

        //  Update plan periods.
        if (planItem.getPlanPeriods() != null) {
            //  Convert from List to Set.
            planItemEntity.setPlanPeriods(new HashSet<String>(planItem.getPlanPeriods()));
        }

        //  Update attributes.
        if (planItem.getAttributes() != null) {
            List<PlanItemAttributeEntity> attributeEntities = new ArrayList<PlanItemAttributeEntity>();
            for (AttributeInfo att : planItem.getAttributes()) {
                PlanItemAttributeEntity ae = new PlanItemAttributeEntity();
                ae.setId(att.getId());
                ae.setKey(att.getKey());
                ae.setValue(att.getValue());
                ae.setOwner(planItemEntity);
                attributeEntities.add(ae);
            }
            planItemEntity.setAttributes(attributeEntities);
        }

        //  Update text entity.
        planItemEntity.setDescr(new PlanItemRichTextEntity(planItem.getDescr()));

        //  Update the learning plan.
        if (!planItemEntity.getLearningPlan().getId().equals(planItem.getLearningPlanId())) {
            String planId = planItem.getLearningPlanId();
            if (planId == null) {
                throw new InvalidParameterException("Learning plan id was null.");
            }
            LearningPlanEntity plan = learningPlanDao.find(planItem.getLearningPlanId());
            if (plan == null) {
                throw new InvalidParameterException(String.format("Unknown learning plan id [%s]", planItem.getLearningPlanId()));
            }
            planItemEntity.setLearningPlan(plan);
        }

        planItemDao.merge(planItemEntity);

        return planItemDao.find(planItemEntity.getId()).toDto();
    }

    @Override
    public PlanItemSetInfo updatePlanItemSet(@WebParam(name = "planItemSetId") String planItemSetId, @WebParam(name = "planItemSet") PlanItemSetInfo planItemSet, @WebParam(name = "context") ContextInfo context) throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    @Transactional
    public StatusInfo deleteLearningPlan(@WebParam(name = "learningPlanId") String learningPlanId,
                                         @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        StatusInfo status = new StatusInfo();
        status.setSuccess(Boolean.TRUE);

        LearningPlanEntity lpe = learningPlanDao.find(learningPlanId);
        if (lpe == null) {
            throw new DoesNotExistException(learningPlanId);
        }

        //  Delete plan items.
        List<PlanItemEntity> pies = planItemDao.getLearningPlanItems(learningPlanId);
        for (PlanItemEntity pie : pies) {
            //  TODO: May need to manually remove items from the ATP join table once that is implemented.
            planItemDao.remove(pie);
        }

        learningPlanDao.remove(lpe);

        return status;

    }

    @Override
    @Transactional
    public StatusInfo deletePlanItem(@WebParam(name = "planItemId") String planItemId,
                                     @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {

        StatusInfo status = new StatusInfo();
        status.setSuccess(true);

        PlanItemEntity pie = planItemDao.find(planItemId);
        if (pie == null) {
            throw new DoesNotExistException(String.format("Unknown plan item id [%s].", planItemId));
        }

        //  TODO: May need to manually remove items from the ATP join table once that is implemented.

        planItemDao.remove(pie);

        return status;
    }

    @Override
    public StatusInfo deletePlanItemSet(@WebParam(name = "planItemSetId") String planItemSetId,
                                        @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, PermissionDeniedException {
        throw new RuntimeException("Not implemented.");
    }

    @Override
    public List<ValidationResultInfo> validateLearningPlan(@WebParam(name = "validationType") String validationType,
                                                           @WebParam(name = "learningPlanInfo") LearningPlanInfo learningPlanInfo,
                                                           @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return new ArrayList<ValidationResultInfo>();
    }

    @Override
    public List<ValidationResultInfo> validatePlanItem(@WebParam(name = "validationType") String validationType,
                                                       @WebParam(name = "planItemInfo") PlanItemInfo planItemInfo,
                                                       @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return new ArrayList<ValidationResultInfo>();
    }

    @Override
    public List<ValidationResultInfo> validatePlanItemSet(@WebParam(name = "validationType") String validationType,
                                                          @WebParam(name = "planItemInfo") PlanItemSetInfo planItemSetInfo,
                                                          @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return new ArrayList<ValidationResultInfo>();
    }
}
