package org.kuali.student.myplan.academicplan.service;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.common.UUIDHelper;
import org.kuali.student.myplan.academicplan.dao.LearningPlanDao;
import org.kuali.student.myplan.academicplan.dao.LearningPlanTypeDao;
import org.kuali.student.myplan.academicplan.dao.PlanItemDao;
import org.kuali.student.myplan.academicplan.dao.PlanItemTypeDao;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemSetInfo;
import org.kuali.student.myplan.academicplan.model.*;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.kuali.student.r2.common.dto.StatusInfo;
import org.kuali.student.r2.common.dto.ValidationResultInfo;
import org.kuali.student.r2.common.exceptions.*;
import org.kuali.student.r2.common.infc.Attribute;
import org.kuali.student.r2.common.infc.ValidationResult;
import org.kuali.student.r2.core.atp.service.AtpService;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.kuali.student.r2.core.search.dto.SearchRequestInfo;
import org.kuali.student.r2.core.search.dto.SearchResultInfo;
import org.kuali.student.r2.core.search.infc.SearchResultCell;
import org.kuali.student.r2.core.search.infc.SearchResultRow;
import org.kuali.student.r2.core.search.service.SearchManager;
import org.kuali.student.r2.lum.clu.service.CluService;
import org.kuali.student.r2.lum.course.service.CourseService;
import org.kuali.student.r2.lum.util.constants.CluServiceConstants;
import org.kuali.student.r2.lum.util.constants.CourseServiceConstants;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.jws.WebParam;
import javax.xml.namespace.QName;
import java.util.*;

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
    private AtpService atpService;
    private CluService luService;
    private PersonService personService;
    private SearchManager searchManager;

    /**
     * This method provides a way to manually provide a CourseService implementation during testing.
     *
     * @param courseService
     */
    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    protected synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    protected synchronized CluService getLuService() {
        if (this.luService == null) {
            this.luService = (CluService) GlobalResourceLoader.getService(new QName(CluServiceConstants.CLU_NAMESPACE, "CluService"));
        }
        return this.luService;
    }


    /**
     * Provides an instance of the AtpService client.
     */
    protected AtpService getAtpService() {
        if (atpService == null) {
            // TODO: Namespace should not be hard-coded.
            atpService = (AtpService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/atp", "AtpService"));
        }
        return this.atpService;
    }

    /**
     * This method provides a way to manually provide a CourseService implementation during testing.
     *
     * @param atpService
     */
    public void setAtpService(AtpService atpService) {
        this.atpService = atpService;
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

    public SearchManager getSearchManager() {
        return searchManager;
    }

    public void setSearchManager(SearchManager searchManager) {
        this.searchManager = searchManager;
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
            throw new DoesNotExistException(String.format("Plan item with Id [%s] does not exist", planItemId));
        }

        return planItem.toDto();
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByType(@WebParam(name = "learningPlanId") String learningPlanId,
                                                       @WebParam(name = "planItemTypeKey") String planItemTypeKey,
                                                       @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
        if (learningPlanId != null) {
            List<PlanItemEntity> planItemEntities = planItemDao.getLearningPlanItems(learningPlanId, planItemTypeKey);
            if (null == planItemEntities) {
                throw new DoesNotExistException(String.format("Plan item with learning plan Id [%s] does not exist", learningPlanId));
            } else {
                for (PlanItemEntity planItemEntity : planItemEntities) {
                    planItemInfos.add(planItemEntity.toDto());
                }
            }
        }
        return planItemInfos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlan(@WebParam(name = "learningPlanId") String learningPlanId,
                                                 @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        List<PlanItemInfo> dtos = new ArrayList<PlanItemInfo>();

        if (learningPlanId != null) {
            List<PlanItemEntity> planItems = planItemDao.getLearningPlanItems(learningPlanId);
            for (PlanItemEntity pie : planItems) {
                dtos.add(pie.toDto());
            }
        }
        return dtos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByAtp(@WebParam(name = "learningPlanId") String learningPlanId,
                                                      @WebParam(name = "atpKey") String atpKey,
                                                      @WebParam(name = "planItemTypeKey") String planItemTypeKey,
                                                      @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        List<PlanItemEntity> planItemsList = planItemDao.getLearningPlanItems(learningPlanId, planItemTypeKey);

        List<PlanItemInfo> planItemDtos = new ArrayList<PlanItemInfo>();
        for (PlanItemEntity pie : planItemsList) {
            if (pie.getPlanPeriods().contains(atpKey)) {
                planItemDtos.add(pie.toDto());
            }
        }

        return planItemDtos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByAtpAndRefObjType(@WebParam(name = "learningPlanId") String learningPlanId, @WebParam(name = "atpKey") String atpKey, @WebParam(name = "refObjectType") String refObjectType, @WebParam(name = "context") ContextInfo context) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<PlanItemEntity> planItemsList = planItemDao.getLearningPlanItemsByRefObjectType(learningPlanId, refObjectType);

        List<PlanItemInfo> planItemDtos = new ArrayList<PlanItemInfo>();
        for (PlanItemEntity pie : planItemsList) {
            if (pie.getPlanPeriods().contains(atpKey)) {
                planItemDtos.add(pie.toDto());
            }
        }

        return planItemDtos;
    }

    @Override
    public List<PlanItemInfo> getPlanItemsInPlanByRefObjectIdByRefObjectType(@WebParam(name = "learningPlanId") String learningPlanId,
                                                                             @WebParam(name = "refObjectId") String refObjectId,
                                                                             @WebParam(name = "refObjectType") String refObjectType,
                                                                             @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        List<PlanItemEntity> planItemsList = planItemDao.getLearningPlanItemsByRefObjectId(learningPlanId, refObjectId, refObjectType);

        List<PlanItemInfo> planItemDtos = new ArrayList<PlanItemInfo>();
        for (PlanItemEntity pie : planItemsList) {
            planItemDtos.add(pie.toDto());
        }

        return planItemDtos;
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
        Collections.sort(learningPlanDtos, new Comparator<LearningPlanInfo>() {
            public int compare(LearningPlanInfo l1, LearningPlanInfo l2) {
                return l1.getMeta().getUpdateTime().compareTo(l2.getMeta().getUpdateTime());
            }
        });
        return learningPlanDtos;
    }

    @Override
    public List<LearningPlanInfo> getLearningPlansForPlanProgramByType(@WebParam(name = "name") String planProgram,
                                                                       @WebParam(name = "planTypeKey") String planTypeKey,
                                                                       @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        List<LearningPlanEntity> lpeList = learningPlanDao.getLearningPlansByTypeAndProgram(planProgram, planTypeKey);
        List<LearningPlanInfo> learningPlanDtos = new ArrayList<LearningPlanInfo>();
        for (LearningPlanEntity lpe : lpeList) {
            learningPlanDtos.add(lpe.toDto());
        }
        Collections.sort(learningPlanDtos, new Comparator<LearningPlanInfo>() {
            public int compare(LearningPlanInfo lp1, LearningPlanInfo lp2) {
                return lp1.getMeta().getUpdateTime().compareTo(lp2.getMeta().getUpdateTime());
            }
        });
        return learningPlanDtos;
    }

    @Override
    @Transactional
    public LearningPlanInfo createLearningPlan(@WebParam(name = "learningPlan") LearningPlanInfo learningPlan,
                                               @WebParam(name = "context") ContextInfo context)
            throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {

        //Adding the system key of student to Learning plan dynamic attributes
        addSysKeyToLearningPlanAttributes(learningPlan);

        LearningPlanEntity lpe = populateLearningPlanEntity(learningPlan, context);
        LearningPlanEntity existing = learningPlanDao.find(lpe.getId());
        if (existing != null) {
            throw new AlreadyExistsException();
        }
        learningPlanDao.persist(lpe);

        return learningPlanDao.find(lpe.getId()).toDto();
    }



   // @Override
    @Transactional
    public LearningPlanInfo copyLearningPlan(@WebParam(name = "learningPlanId") String fromLearningPlanId,
                                             @WebParam(name = "planTypeKey") String planTypeKey,
                                             @WebParam(name = "context") ContextInfo context)
            throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        //  Get the learningPlanInfo for the given learningPlanId
        LearningPlanEntity lpe = learningPlanDao.find(fromLearningPlanId);
        if (null == lpe) {
            throw new DoesNotExistException(fromLearningPlanId);
        }
        LearningPlanInfo dto = lpe.toDto();

        //  replacing the type key for above learningPlanInfo with new type key
        dto.setTypeKey(planTypeKey);
        UserSession userSession = GlobalVariables.getUserSession();
        Person user = userSession.getPerson();
        List<AttributeInfo> attributeInfos = new ArrayList<AttributeInfo>();
        attributeInfos.add(new AttributeInfo("auditId", ""));
        attributeInfos.add(new AttributeInfo("forCourses", ""));
        attributeInfos.add(new AttributeInfo("forCredits", ""));
        attributeInfos.add(new AttributeInfo("forQuarter", ""));
        attributeInfos.add(new AttributeInfo("requestedBy", user.getName().toUpperCase()));
        attributeInfos.add(new AttributeInfo("isAdviser", String.valueOf(userSession.retrieveObject(AcademicPlanServiceConstants.SESSION_KEY_IS_ADVISER) != null)));
        dto.setAttributes(attributeInfos);

        //  Creating a new LearningPlanEntity from above learningPLanInfo with new PlanTypeKey
        LearningPlanEntity newLearningPlan = populateLearningPlanEntity(dto, context);
        LearningPlanEntity alreadyExisting = learningPlanDao.find(newLearningPlan.getId());
        if (alreadyExisting != null) {
            throw new AlreadyExistsException();
        }

        //  Create a copy of learningPlan
        learningPlanDao.persist(newLearningPlan);

        // copy all the planItems
        List<PlanItemEntity> planItems = planItemDao.getLearningPlanItems(fromLearningPlanId);
        for (PlanItemEntity pie : planItems) {
            if (AcademicPlanServiceConstants.SECTION_TYPE.equals(pie.getRefObjectTypeKey()) || AcademicPlanServiceConstants.COURSE_TYPE.equals(pie.getRefObjectTypeKey())) {
                PlanItemInfo planItemInfo = pie.toDto();
                planItemInfo.setLearningPlanId(newLearningPlan.getId());
                PlanItemEntity planItemEntity = populatePlanItemEntity(planItemInfo, context);
                //  Save the new plan item.
                planItemDao.persist(planItemEntity);

                // update the learningPlan
                LearningPlanEntity plan = planItemEntity.getLearningPlan();
                plan.setUpdateId(context.getPrincipalId());
                plan.setUpdateTime(new Date());
                learningPlanDao.update(plan);
            }
        }

        return learningPlanDao.find(newLearningPlan.getId()).toDto();
    }

    /*   copyLearningPlan
     * @param fromLearningPlanId - the id of the existing learning plan to duplicate
     * @param context - a ContextInfo. we only use the principalId.
     *
     * copy an existing learning plan, that is, make a dupe of it.
     *
     */
     // if we finish Sample Plan:
     //     we can delete the version of  copyLearningPlan() above,
    //      change degreeAudit to call this new version
    //      move the degreeAudit specific code back to degreeAudit somewhere,
    //          ie., the code that create the dynamic  attributes.

    @Override
    @Transactional
    public LearningPlanInfo copyLearningPlan(@WebParam(name = "learningPlanId") String fromLearningPlanId,
                                             @WebParam(name = "context") ContextInfo context)
            throws AlreadyExistsException, DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {

        //  Get the learningPlanInfo for the given learningPlanId
        LearningPlanEntity lpe = learningPlanDao.find(fromLearningPlanId);
        if (null == lpe) {
            throw new DoesNotExistException(fromLearningPlanId);
        }
        LearningPlanInfo dto = lpe.toDto();

        //  Creating a new LearningPlanEntity from above learningPLanInfo with new PlanTypeKey
        LearningPlanEntity newLearningPlan = populateLearningPlanEntity(dto, context);
        LearningPlanEntity alreadyExisting = learningPlanDao.find(newLearningPlan.getId());
        if (alreadyExisting != null) {
            throw new AlreadyExistsException();
        }

        //  Create a copy of learningPlan
        learningPlanDao.persist(newLearningPlan);

        // copy all the planItems
        int nbrUpdates = 0;
        List<PlanItemEntity> planItems = planItemDao.getLearningPlanItems(fromLearningPlanId);
        for (PlanItemEntity pie : planItems) {
            if (AcademicPlanServiceConstants.SECTION_TYPE.equals(pie.getRefObjectTypeKey()) ||
                // if we finish Sample Plan:
                // need to handle more types, esp uw.cluset.type.course.level which is used for CHEM 3xx
                AcademicPlanServiceConstants.COURSE_TYPE.equals(pie.getRefObjectTypeKey())) {
                PlanItemInfo planItemInfo = pie.toDto();
                planItemInfo.setLearningPlanId(newLearningPlan.getId());
                PlanItemEntity planItemEntity = populatePlanItemEntity(planItemInfo, context);
                //  Save the new plan item.
                planItemDao.persist(planItemEntity);
                nbrUpdates++;
            }
        }
        // update the learningPlan
        if (nbrUpdates > 0) {
            newLearningPlan.setUpdateId(context.getPrincipalId());
            newLearningPlan.setUpdateTime(new Date());
            learningPlanDao.update(newLearningPlan);
        }

        return learningPlanDao.find(newLearningPlan.getId()).toDto();
    }


    @Override
    @Transactional
    public PlanItemInfo createPlanItem(@WebParam(name = "planItem") PlanItemInfo planItem,
                                       @WebParam(name = "context") ContextInfo context)
            throws AlreadyExistsException, DataValidationErrorException, InvalidParameterException, MissingParameterException,
            OperationFailedException, PermissionDeniedException {

        PlanItemEntity pie = populatePlanItemEntity(planItem, context);
        //  Save the new plan item.
        planItemDao.persist(pie);

        //  Update the metadata (timestamp, updated-by) on the plan.
        LearningPlanEntity plan = pie.getLearningPlan();
        plan.setUpdateId(context.getPrincipalId());
        plan.setUpdateTime(new Date());
        learningPlanDao.update(plan);

        return planItemDao.find(pie.getId()).toDto();
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
            MissingParameterException, OperationFailedException, PermissionDeniedException, DoesNotExistException, AlreadyExistsException {

        LearningPlanEntity lpe = learningPlanDao.find(learningPlanId);
        if (lpe == null) {
            throw new DoesNotExistException(learningPlanId);
        }

        lpe.setStudentId(learningPlan.getStudentId());
        // if we finish Sample Plan:
        // need to make sure these 3 lines work.
        lpe.setDescr(new LearningPlanRichTextEntity(learningPlan.getDescr()));
        lpe.setPlanProgram(learningPlan.getName());
        lpe.setPlanProgram(learningPlan.getPlanProgram());

        lpe.setAttributes(new HashSet<LearningPlanAttributeEntity>());
        if (null != learningPlan.getAttributes()) {
            for (Attribute att : learningPlan.getAttributes()) {
                LearningPlanAttributeEntity attEntity = new LearningPlanAttributeEntity(att, lpe);
                lpe.getAttributes().add(attEntity);
            }
        }

        lpe.setShared(learningPlan.getShared());
        //  Update meta data.
        lpe.setUpdateId(context.getPrincipalId());
        lpe.setUpdateTime(new Date());

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

        // If Plan type changes, create a new one and update the old one's state to DELETED
        String updatePlanTypeId = null;

        if (planItemEntity == null) {
            throw new DoesNotExistException(planItemId);
        }

        planItemEntity.setRefObjectId(planItem.getRefObjectId());
        planItemEntity.setRefObjectTypeKey(planItem.getRefObjectType());

        //  Update the plan item type if it has changed.
        boolean createNewPlanItem = false;
        if (!planItemEntity.getLearningPlanItemType().getId().equals(planItem.getTypeKey())
                && planItemEntity.getLearningPlanItemType().getId().equals(AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST)) {
            createNewPlanItem = true;
        }

        if (!planItemEntity.getLearningPlanItemType().getId().equals(planItem.getTypeKey())) {
            PlanItemTypeEntity planItemTypeEntity = planItemTypeDao.find(planItem.getTypeKey());
            if (planItemTypeEntity == null) {
                throw new InvalidParameterException(String.format("Unknown plan item type id [%s].", planItem.getTypeKey()));
            }

            // Reset the plan Item
            planItemEntity.setLearningPlanItemType(planItemTypeEntity);
            updatePlanTypeId = planItemEntity.getId();
        }

        //  Update plan periods.
        if (planItem.getPlanPeriods() != null) {
            //  Convert from List to Set.
            planItemEntity.setPlanPeriods(new HashSet<String>(planItem.getPlanPeriods()));
        }

        //  Update attributes.
        if (planItem.getAttributes() != null) {
            Set<PlanItemAttributeEntity> attributeEntities = new HashSet<PlanItemAttributeEntity>();
            Set<PlanItemAttributeEntity> tempAttributeEntities = new HashSet<PlanItemAttributeEntity>();

            for (PlanItemAttributeEntity planItemAttributeEntity : planItemEntity.getAttributes()) {
                planItemAttributeEntity.setValue("");
                tempAttributeEntities.add(planItemAttributeEntity);
            }


            for (AttributeInfo attributeInfo : planItem.getAttributes()) {
                if (!CollectionUtils.isEmpty(tempAttributeEntities)) {
                    for (PlanItemAttributeEntity planItemAttributeEntity : tempAttributeEntities) {
                        if (planItemAttributeEntity.getKey().equals(attributeInfo.getKey())) {
                            planItemAttributeEntity.setValue(attributeInfo.getValue());
                            attributeEntities.add(planItemAttributeEntity);
                        } else {
                            attributeEntities.add(new PlanItemAttributeEntity(attributeInfo, planItemEntity));
                        }
                    }
                } else {
                    attributeEntities.add(new PlanItemAttributeEntity(attributeInfo, planItemEntity));
                }

            }

            planItemEntity.setAttributes(attributeEntities);
        }

        //Update state
        planItemEntity.setState(planItem.getStateKey());

        //  Update text entity.
        planItemEntity.setDescr(new PlanItemRichTextEntity(planItem.getDescr()));

        //  Update meta data.
        planItemEntity.setUpdateId(context.getPrincipalId());
        planItemEntity.setUpdateTime(new Date());

        //   If the the learning plan has changed update the plan item and update the meta data (update date, user) on the old plan.
        LearningPlanEntity originalPlan = learningPlanDao.find(planItem.getLearningPlanId());
        if (originalPlan == null) {
            throw new InvalidParameterException(String.format("Unknown learning plan id [%s]", planItem.getLearningPlanId()));
        }

        LearningPlanEntity newPlan = null;
        if (!planItemEntity.getLearningPlan().getId().equals(planItem.getLearningPlanId())) {
            String planId = planItem.getLearningPlanId();
            if (planId == null) {
                throw new InvalidParameterException("Learning plan id was null.");
            }
            newPlan = learningPlanDao.find(planItem.getLearningPlanId());
            if (newPlan == null) {
                throw new InvalidParameterException(String.format("Unknown learning plan id [%s]", planItem.getLearningPlanId()));
            }
            planItemEntity.setLearningPlan(newPlan);
        }

        // If plan type changes create a new one and delete
        String updatePlanItemId = null;
        if (createNewPlanItem) {
            try {
                PlanItemInfo newpiInfo = createPlanItem(planItemEntity.toDto(), context);
                updatePlanItemId = newpiInfo.getId();
            } catch (AlreadyExistsException e) {
                throw new OperationFailedException(e.getMessage());
            }
            deletePlanItem(updatePlanTypeId, context);
        } else {
            updatePlanItemId = planItemEntity.getId();
            planItemDao.merge(planItemEntity);
        }

        //  Update meta data on the original plan.
        originalPlan.setUpdateId(context.getPrincipalId());
        originalPlan.setUpdateTime(new Date());
        learningPlanDao.update(originalPlan);

        //  Update the new plan meta data if the plan changed.
        if (newPlan != null) {
            newPlan.setUpdateId(context.getPrincipalId());
            newPlan.setUpdateTime(new Date());
            learningPlanDao.update(newPlan);
        }

        // update credits
        if (planItem.getCredit() != planItemEntity.getCredit()) {
            planItemEntity.setCredit(planItem.getCredit());
        }

        return planItemDao.find(updatePlanItemId).toDto();
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
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, AlreadyExistsException {
        return new ArrayList<ValidationResultInfo>();
    }

    @Override
    public List<ValidationResultInfo> validatePlanItem(@WebParam(name = "validationType") String validationType,
                                                       @WebParam(name = "planItemInfo") PlanItemInfo planItemInfo,
                                                       @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException, AlreadyExistsException {

        List<ValidationResultInfo> validationResultInfos = new ArrayList<ValidationResultInfo>();

        /*
         *  Validate that the course exists.
         * TODO: Move this validation to the data dictionary.
         */
        /*try {
            String verifiedCourseId = null;
            SearchRequest req = new SearchRequest("myplan.course.version.id");
            req.addParam("courseId", planItemInfo.getRefObjectId());
            req.addParam("courseId", planItemInfo.getRefObjectId());
            req.addParam("lastScheduledTerm", DegreeAuditAtpHelper.getLastScheduledAtpId());
            SearchResult result = getLuService().search(req);
            for (SearchResultRow row : result.getRows()) {
                for (SearchResultCell cell : row.getCells()) {
                    if ("lu.resultColumn.cluId".equals(cell.getKey())) {
                        verifiedCourseId = cell.getValue();
                    }
                }
            }
            if (verifiedCourseId == null) {
                validationResultInfos.add(makeValidationResultInfo("Invalid Course Id",
                        "refObjectId", ValidationResult.ErrorLevel.ERROR));
            }

        } catch (Exception e) {
            validationResultInfos.add(makeValidationResultInfo(e.getLocalizedMessage(),
                    "refObjectId", ValidationResult.ErrorLevel.ERROR));
        }
*/

        //  TODO: This validation should be implemented in the data dictionary when that possibility manifests.
        //  Make sure a plan period exists if type is planned course.
        if (AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equals(planItemInfo.getTypeKey())
                || AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP.equals(planItemInfo.getTypeKey()) || AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItemInfo.getTypeKey())) {
            if (planItemInfo.getPlanPeriods() == null || planItemInfo.getPlanPeriods().size() == 0) {
                validationResultInfos.add(makeValidationResultInfo(
                        String.format("Plan Item Type was [%s], but no plan periods were defined.", planItemInfo.getTypeKey()),
                        "typeKey", ValidationResult.ErrorLevel.ERROR));
            } else {
                //  Make sure the plan periods are valid. Note: There should never be more than one item in the collection.
                for (String atpId : planItemInfo.getPlanPeriods()) {
                    boolean valid = false;
                    try {
                        valid = isValidAtp(atpId, context);
                        if (!valid) {
                            validationResultInfos.add(makeValidationResultInfo(
                                    String.format("ATP ID [%s] was not valid.", atpId), "atpId", ValidationResult.ErrorLevel.ERROR));
                        }
                    } catch (Exception e) {
                        validationResultInfos.add(makeValidationResultInfo(
                                "ATP ID lookup failed.", "typeKey", ValidationResult.ErrorLevel.ERROR));
                    }
                }
            }
        }

        /*
         * Check for duplicate list items:
         *    Make sure a saved courses item with this course id doesn't already exist in the plan.
         *    Make sure a planned course item with the same ATP id doesn't exist in the plan.
         *
         * Note: This validation is last to insure that all of the other validations are performed on "update" operations.
         * The duplicate check throw an AlreadyExistsException on updates.
         *
         * TODO: Maybe there is a better way to deal with validating udpates?
         *
         * TODO: Move these validations to the data dictionary.
         */

        /*Duplicate check should only be for course and sections not for placeholders*/
        if (AcademicPlanServiceConstants.COURSE_TYPE.equals(planItemInfo.getRefObjectType()) || AcademicPlanServiceConstants.SECTION_TYPE.equals(planItemInfo.getRefObjectType())) {
            checkPlanItemDuplicate(planItemInfo);
        }


        return validationResultInfos;
    }

    @Override
    public List<ValidationResultInfo> validatePlanItemSet(@WebParam(name = "validationType") String validationType,
                                                          @WebParam(name = "planItemInfo") PlanItemSetInfo planItemSetInfo,
                                                          @WebParam(name = "context") ContextInfo context)
            throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return new ArrayList<ValidationResultInfo>();
    }

    /**
     * @throws AlreadyExistsException If the plan item is a duplicate.
     */
    private void checkPlanItemDuplicate(PlanItemInfo planItem) throws AlreadyExistsException {

        String planItemId = planItem.getLearningPlanId();
        String courseId = planItem.getRefObjectId();
        String planItemType = planItem.getTypeKey();
        String crossListedCourse = null;
        String crossListedSubject = null;
        String crossListedNumber = null;
        for (AttributeInfo attributeInfo : planItem.getAttributes()) {
            if (AcademicPlanServiceConstants.CROSS_LISTED_COURSE_ATTR_KEY.equals(attributeInfo.getKey())) {
                crossListedCourse = attributeInfo.getValue();
                break;
            }
        }

        if (StringUtils.hasText(crossListedCourse)) {
            String[] str = crossListedCourse.split(AcademicPlanServiceConstants.SPLIT_DIGITS_ALPHABETS);
            crossListedSubject = str[0].trim();
            crossListedNumber = str[1].trim();
        }


        /**
         * See if a duplicate item exits in the plan. If the type is wishlist then only the course id has to match to make
         * it a duplicate. If the type is planned course then the ATP must match as well.
         */
        List<PlanItemEntity> planItems = this.planItemDao.getLearningPlanItems(planItemId, planItemType);
        for (PlanItemEntity p : planItems) {
            if (p.getRefObjectId().equals(courseId)) {
                if (AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED.equals(planItemType) || AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP.equals(planItemType) || AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED.equals(planItemType)) {
                    for (String atpId : planItem.getPlanPeriods()) {
                        if (p.getPlanPeriods().contains(atpId)) {
                            /*If the course is a crossListed then we should check against the crossListed items in the planItemEntity for perfect validation*/
                            String plannedCrossListedCourse = null;
                            for (PlanItemAttributeEntity attributeEntity : p.getAttributes()) {
                                if (AcademicPlanServiceConstants.CROSS_LISTED_COURSE_ATTR_KEY.equals(attributeEntity.getKey()) && StringUtils.hasText(attributeEntity.getValue())) {
                                    plannedCrossListedCourse = attributeEntity.getValue();
                                }
                            }

                            if (StringUtils.hasText(plannedCrossListedCourse) && StringUtils.hasText(crossListedCourse)) {
                                String[] str = plannedCrossListedCourse.split(AcademicPlanServiceConstants.SPLIT_DIGITS_ALPHABETS);
                                if (crossListedSubject.equals(str[0].trim()) && crossListedNumber.equals(str[1].trim())) {
                                    throw new AlreadyExistsException(String.format("A plan item for plan [%s], course id [%s], course Cd [%s], and term [%s] already exists.",
                                            p.getLearningPlan().getId(), courseId, crossListedCourse, atpId));
                                }
                            } else if (StringUtils.isEmpty(plannedCrossListedCourse) && StringUtils.isEmpty(crossListedCourse)) {
                                throw new AlreadyExistsException(String.format("A plan item for plan [%s], course id [%s], course Cd [%s], and term [%s] already exists.",
                                        p.getLearningPlan().getId(), courseId, crossListedCourse, atpId));
                            }

                        }
                    }
                } else if (AcademicPlanServiceConstants.LEARNING_PLAN_ITEM_TYPE_WISHLIST.equals(planItemType)) {
                    /*If the course is a crossListed then we should check against the crossListed items in the planItemEntity for perfect validation*/
                    String plannedCrossListedCourse = null;
                    for (PlanItemAttributeEntity attributeEntity : p.getAttributes()) {
                        if (AcademicPlanServiceConstants.CROSS_LISTED_COURSE_ATTR_KEY.equals(attributeEntity.getKey()) && StringUtils.hasText(attributeEntity.getValue())) {
                            plannedCrossListedCourse = attributeEntity.getValue();
                        }
                    }

                    if (StringUtils.hasText(plannedCrossListedCourse) && StringUtils.hasText(crossListedCourse)) {
                        String[] str = plannedCrossListedCourse.split(AcademicPlanServiceConstants.SPLIT_DIGITS_ALPHABETS);
                        if (crossListedSubject.equals(str[0].trim()) && crossListedNumber.equals(str[1].trim())) {
                            throw new AlreadyExistsException(String.format("A plan item for plan [%s] and course id [%s] already exists.",
                                    p.getLearningPlan().getId(), courseId));
                        }
                    } else if (StringUtils.isEmpty(plannedCrossListedCourse) && StringUtils.isEmpty(crossListedCourse)) {
                        throw new AlreadyExistsException(String.format("A plan item for plan [%s] and course id [%s] already exists.",
                                p.getLearningPlan().getId(), courseId));
                    }
                }
            }
        }
    }


    /**
     * @param learningPlan
     * @param context
     * @return LearningPlanEntity
     * @throws InvalidParameterException
     */
    private LearningPlanEntity populateLearningPlanEntity(LearningPlanInfo learningPlan, ContextInfo context) throws InvalidParameterException {
        LearningPlanEntity lpe = new LearningPlanEntity();
        lpe.setId(UUIDHelper.genStringUUID());

        LearningPlanTypeEntity type = learningPlanTypeDao.find(learningPlan.getTypeKey());
        if (type == null) {
            throw new InvalidParameterException(String.format("Unknown type [%s].", learningPlan.getTypeKey()));
        }
        lpe.setLearningPlanType(type);

        lpe.setStudentId(learningPlan.getStudentId());
        lpe.setDescr(new LearningPlanRichTextEntity(learningPlan.getDescr()));
        //  Item meta
        lpe.setCreateId(context.getPrincipalId());
        lpe.setCreateTime(new Date());
        lpe.setUpdateId(context.getPrincipalId());
        lpe.setUpdateTime(new Date());
        lpe.setShared(learningPlan.getShared());
        lpe.setAttributes(new HashSet<LearningPlanAttributeEntity>());
        if (learningPlan.getAttributes() != null) {
            for (Attribute att : learningPlan.getAttributes()) {
                LearningPlanAttributeEntity attEntity = new LearningPlanAttributeEntity(att, lpe);
                lpe.getAttributes().add(attEntity);
            }
        }
        lpe.setState(learningPlan.getStateKey());
        lpe.setName(learningPlan.getName());
        lpe.setPlanProgram(learningPlan.getPlanProgram());
        return lpe;
    }

    /**
     * @param planItem
     * @param context
     * @return PlanItemEntity
     * @throws InvalidParameterException
     */
    private PlanItemEntity populatePlanItemEntity(PlanItemInfo planItem, ContextInfo context) throws InvalidParameterException {
        //  FIXME: For a given plan there should be only one planned course item per course id. So, do a lookup to see
        //  if a plan item exists if the type is "planned" and do an update of ATPid instead of creating a new plan item.
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

        pie.setState(planItem.getStateKey());

        //  Convert the List of plan periods to a Set.
        pie.setPlanPeriods(new HashSet<String>(planItem.getPlanPeriods()));

        //  Set attributes.
        pie.setAttributes(new HashSet<PlanItemAttributeEntity>());
        if (planItem.getAttributes() != null) {
            for (Attribute att : planItem.getAttributes()) {
                PlanItemAttributeEntity attEntity = new PlanItemAttributeEntity(att, pie);
                attEntity.setId(UUIDHelper.genStringUUID());
                pie.getAttributes().add(attEntity);
            }
        }

        //  Create text entity.
        pie.setDescr(new PlanItemRichTextEntity(planItem.getDescr()));

        //  Set meta data.
        pie.setCreateId(GlobalVariables.getUserSession().getPrincipalId());
        pie.setCreateTime(new Date());
        pie.setUpdateId(GlobalVariables.getUserSession().getPrincipalId());
        pie.setUpdateTime(new Date());

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

        pie.setCredit(planItem.getCredit());

        return pie;

    }

    private ValidationResultInfo makeValidationResultInfo(String errorMessage, String element, ValidationResult.ErrorLevel errorLevel) {
        ValidationResultInfo vri = new ValidationResultInfo();
        vri.setError(errorMessage);
        vri.setElement(element);
        vri.setLevel(errorLevel);
        return vri;
    }

    private boolean isValidAtp(String atpId, ContextInfo contextInfo) {
        try {
            getAtpService().getAtp(atpId, contextInfo);
        } catch (DoesNotExistException e) {
            return false;
        } catch (Exception e) {
            throw new RuntimeException("Query to ATP service failed.", e);
        }
        return true;
    }

    /**
     * Adds student system key to plan item attributes
     *
     * @param learningPlanInfo
     */
    private void addSysKeyToLearningPlanAttributes(LearningPlanInfo learningPlanInfo) {
        String externalIdentifier = ConfigContext.getCurrentContextConfig().getProperty(AcademicPlanServiceConstants.EXTERNAL_IDENTIFIER);

        if (StringUtils.hasText(externalIdentifier)) {

            UserSession session = GlobalVariables.getUserSession();
            String regId = session.getPerson().getPrincipalId();

            if (regId != null) {
                Person person = getPersonService().getPerson(regId);
                if (person != null && StringUtils.hasText(person.getExternalIdentifiers().get(externalIdentifier))) {
                    String systemKey = person.getExternalIdentifiers().get(externalIdentifier);
                    if (CollectionUtils.isEmpty(learningPlanInfo.getAttributes())) {
                        List<AttributeInfo> attributeInfos = new ArrayList<AttributeInfo>();
                        attributeInfos.add(new AttributeInfo("systemKey", systemKey));
                        learningPlanInfo.setAttributes(attributeInfos);
                    } else {
                        if (!sysKeyExists(learningPlanInfo.getAttributes())) {
                            learningPlanInfo.getAttributes().add(new AttributeInfo("systemKey", systemKey));
                        }
                    }
                }
            }

        }

    }

    /**
     * returns true if a attribute with key as systemKey exists
     *
     * @param attributeInfos
     * @return
     */
    private boolean sysKeyExists(List<AttributeInfo> attributeInfos) {
        for (AttributeInfo attributeInfo : attributeInfos) {
            if ("systemKey".equalsIgnoreCase(attributeInfo.getKey())) {
                return true;
            }
        }
        return false;
    }


    public PersonService getPersonService() {
        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        return personService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }

    @Override
    public List<TypeInfo> getSearchTypes(@WebParam(name = "contextInfo") ContextInfo contextInfo) throws InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public TypeInfo getSearchType(@WebParam(name = "searchTypeKey") String searchTypeKey, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws DoesNotExistException, InvalidParameterException, MissingParameterException, OperationFailedException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public SearchResultInfo search(SearchRequestInfo searchRequestInfo, @WebParam(name = "contextInfo") ContextInfo contextInfo) throws MissingParameterException, InvalidParameterException, OperationFailedException, PermissionDeniedException {

        return searchManager.search(searchRequestInfo, contextInfo);
    }
}
