package edu.uw.kuali.student.myplan.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.RecommendedItemDataObject;
import org.kuali.student.myplan.plan.util.DateFormatHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 8/28/13
 * Time: 12:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlanHelperImpl implements PlanHelper {

    private final Logger logger = Logger.getLogger(PlanHelperImpl.class);

    private transient AcademicPlanService academicPlanService;

    @Autowired
    private transient CourseHelper courseHelper;

    @Autowired
    private transient UserSessionHelper userSessionHelper;

    /**
     * Gets a plan item of a particular type for a particular ATP.
     *
     * @param learningPlanId The id of the learning plan
     * @param refObjId       The id of the course
     * @param atpId          The ATP id
     * @param planItemType   The plan item type key.
     * @param courseCd       The crossListed course code.
     * @return A "planned" or "backup" plan item. Or 'null' if none exists.
     * @throws RuntimeException on errors.
     */
    @Override
    public PlanItemInfo getPlanItemByAtpAndType(String learningPlanId, String refObjId, String atpId, String planItemType, String courseCd) {
        if (StringUtils.isEmpty(learningPlanId)) {
            throw new RuntimeException("Learning Plan Id was empty.");
        }

        if (StringUtils.isEmpty(refObjId)) {
            throw new RuntimeException("Course Id was empty.");
        }

        if (StringUtils.isEmpty(atpId)) {
            throw new RuntimeException("ATP Id was empty.");
        }

        List<PlanItemInfo> planItems = null;
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
        PlanItemInfo item = null;

        try {
            planItems = getAcademicPlanService().getPlanItemsInPlanByAtp(learningPlanId, atpId, planItemType, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        for (PlanItemInfo p : planItems) {
            if (p.getRefObjectId().equals(refObjId) && p.getTypeKey().equals(planItemType)) {

                if (PlanConstants.COURSE_TYPE.equals(p.getRefObjectType())) {
                /*Multiple PlanItems with same refObjId can be present which are crossListed courses in same term,
                So filtering them by using the given crossListed courseCd to get the exact planItemInfo*/
                    String crossListedCourse = getCrossListedCourse(p.getAttributes());
                    //Planned parent course returned
                    if (StringUtils.isEmpty(courseCd) && StringUtils.isEmpty(crossListedCourse)) {
                        item = p;
                        break;
                    }
                    //Planned crossListed course returned
                    else if (!StringUtils.isEmpty(courseCd) && getCourseHelper().isSimilarCourses(courseCd, crossListedCourse)) {
                        item = p;
                        break;
                    }
                } else {
                    item = p;
                    break;
                }
            }
        }

        //  A null here means that no plan item exists for the given course and ATP IDs.
        return item;
    }

    /**
     * Gets a Plan Item of type "planned" or "backup" for a particular course and ATP ID. Since we are enforcing a
     * data constraint of one "planned" or "backup" plan item per ATP ID this method only returns a single plan item.
     *
     * @param courseId
     * @return A "planned" or "backup" plan item. Or 'null' if none exists.
     * @throws RuntimeException on errors.
     */
    @Override
    public PlanItemInfo getPlannedOrBackupPlanItem(String courseId, String courseCd, String atpId) {
        String studentId = getUserSessionHelper().getStudentId();
        LearningPlan learningPlan = getLearningPlan(studentId);
        if (learningPlan == null) {
            return null;
        }

        PlanItemInfo planItem = null;

        try {
            planItem = getPlanItemByAtpAndType(learningPlan.getId(), courseId, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, courseCd);
        } catch (Exception e) {
            logger.error("Could not retrieve plan items.", e);
            throw new RuntimeException("Could not retrieve plan items.", e);
        }

        if (planItem == null) {
            try {
                planItem = getPlanItemByAtpAndType(learningPlan.getId(), courseId, atpId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP, courseCd);
            } catch (Exception e) {
                logger.error("Could not retrieve plan items.", e);
                throw new RuntimeException("Could not retrieve plan items.", e);
            }
        }

        //  A null here means that no plan item exists for the given course and ATP IDs.
        return planItem;
    }


    /**
     * returns List of planItemInfo's for given planItem Types.
     *
     * @param studentId
     * @param planItemTypes
     * @return
     */
    public List<PlanItemInfo> getPlanItemsByTypes(String studentId, List<String> planItemTypes) {
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
        for (String planItemType : planItemTypes) {
            try {
                planItemInfos.addAll(getAcademicPlanService().getPlanItemsInPlanByType(getLearningPlan(studentId).getId(), planItemType, PlanConstants.CONTEXT_INFO));
            } catch (Exception e) {
                logger.error("Could not load planItems for student with regId" + studentId);
            }
        }
        return planItemInfos;
    }

    /**
     * Retrieve a student's LearningPlan.
     *
     * @param studentId
     * @return A LearningPlan or null on errors.
     * @throws RuntimeException if the query fails.
     */
    @Override
    public LearningPlan getLearningPlan(String studentId) {
        /*
        *  First fetch the student's learning plan.
        */
        List<LearningPlanInfo> learningPlans = null;
        try {
            learningPlans = getAcademicPlanService().getLearningPlansForStudentByType(studentId,
                    PlanConstants.LEARNING_PLAN_TYPE_PLAN, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            throw new RuntimeException(String.format("Could not fetch plan for user [%s].", studentId), e);
        }

        if (learningPlans == null) {
            throw new RuntimeException(String.format("Could not fetch plan for user [%s]. The query returned null.", studentId));
        }

        //  There should currently only be a single learning plan. This may change in the future.
        if (learningPlans.size() > 1) {
            throw new RuntimeException(String.format("User [%s] has more than one plan.", studentId));
        }

        LearningPlan learningPlan = null;
        if (learningPlans.size() != 0) {
            learningPlan = learningPlans.get(0);
        }

        return learningPlan;
    }

    /**
     * returns a list of recommended items that are there for a given refObjId (versionIndependentId)
     * Valid only for courses not for placeholders
     *
     * @param refObjId
     * @return
     */
    @Override
    public List<RecommendedItemDataObject> getRecommendedItems(String refObjId) {

        List<RecommendedItemDataObject> recommendedItemDataObjects = new ArrayList<RecommendedItemDataObject>();
        try {
            List<LearningPlanInfo> learningPlans = getAcademicPlanService().getLearningPlansForStudentByType(getUserSessionHelper().getStudentId(),
                    PlanConstants.LEARNING_PLAN_TYPE_PLAN, PlanConstants.CONTEXT_INFO);

            if (!CollectionUtils.isEmpty(learningPlans)) {

                //A student should have oNly one learning plan associated to his Id
                LearningPlan learningPlan = learningPlans.get(0);

                List<PlanItemInfo> planItems = getAcademicPlanService().getPlanItemsInPlanByType(learningPlan.getId(),
                        PlanConstants.LEARNING_PLAN_ITEM_TYPE_RECOMMENDED, PlanConstants.CONTEXT_INFO);

                if (!CollectionUtils.isEmpty(planItems)) {
                    for (PlanItemInfo planItemInfo : planItems) {
                        if (PlanConstants.COURSE_TYPE.equals(planItemInfo.getRefObjectType()) && planItemInfo.getRefObjectId().equals(refObjId)) {
                            RecommendedItemDataObject recommendedItemDataObject = new RecommendedItemDataObject();
                            recommendedItemDataObject.setAdviserName(getUserSessionHelper().getCapitalizedName(planItemInfo.getMeta().getCreateId()));
                            String dateAdded = DateFormatHelper.getDateFomatted(planItemInfo.getMeta().getCreateTime().toString());
                            recommendedItemDataObject.setDateAdded(dateAdded);
                            recommendedItemDataObject.setNote(planItemInfo.getDescr().getPlain());
                            recommendedItemDataObject.setAtpId(planItemInfo.getPlanPeriods().get(0));
                            PlanItemInfo plan = getPlannedOrBackupPlanItem(planItemInfo.getRefObjectId(), getCrossListedCourse(planItemInfo.getAttributes()), planItemInfo.getPlanPeriods().get(0));
                            if (plan != null && plan.getId() != null) {
                                recommendedItemDataObject.setPlanned(true);
                            }
                            recommendedItemDataObjects.add(recommendedItemDataObject);
                        }
                    }
                }

            }
        } catch (Exception e) {
            logger.error("Could not load recommended Items", e);
        }
        return recommendedItemDataObjects;
    }


    /**
     * Method used to get the crossListed value from attributeInfoList in PlanItemInfo
     * If not found null is returned which indicates that this is a normal course that is planned
     *
     * @param attributeInfoList
     * @return
     */
    public String getCrossListedCourse(List<AttributeInfo> attributeInfoList) {
        for (AttributeInfo attributeInfo : attributeInfoList) {
            if (PlanConstants.CROSS_LISTED_COURSE_ATTR_KEY.equals(attributeInfo.getKey()) && !StringUtils.isEmpty(attributeInfo.getValue())) {
                return attributeInfo.getValue();
            }
        }
        return null;
    }


    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }
}
