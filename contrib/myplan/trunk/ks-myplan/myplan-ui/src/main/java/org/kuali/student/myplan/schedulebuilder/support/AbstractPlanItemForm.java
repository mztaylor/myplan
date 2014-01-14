package org.kuali.student.myplan.schedulebuilder.support;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.schedulebuilder.util.PlanItemForm;
import org.kuali.student.myplan.schedulebuilder.util.TermHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.common.exceptions.InvalidParameterException;
import org.kuali.student.r2.common.exceptions.MissingParameterException;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.lum.course.infc.Course;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.List;

public abstract class AbstractPlanItemForm extends UifFormBase implements PlanItemForm {

    private static final long serialVersionUID = -6749034321198329558L;

    private static final Logger LOG = Logger.getLogger(AbstractPlanItemForm.class);

    private String uniqueId;

    private String learningPlanId;
    private String termId;
    private String planItemId;
    private String courseId;
    private AcademicPlanServiceConstants.ItemCategory expectedPlanItemCategory;

    private transient AcademicPlanService academicPlanService;
    @Autowired
    private transient CourseHelper courseHelper;
    @Autowired
    private UserSessionHelper userSessionHelper;

    @Autowired
    private PlanHelper planHelper;

    private transient LearningPlan learningPlan;
    private transient Term term;
    private transient PlanItem planItem;

    private transient Course course;

    private transient TermHelper termHelper;

    private transient List<PlanItem> existingPlanItems;

    @Override
    public String getLearningPlanId() {
        return learningPlanId;
    }

    public void setLearningPlanId(String learningPlanId) {
        this.learningPlanId = StringUtils.hasText(learningPlanId) ? learningPlanId : null;
        this.learningPlan = null;
        this.planItem = null;
        this.course = null;
        this.existingPlanItems = null;
    }

    @Override
    public LearningPlan getLearningPlan() {
        if (learningPlan == null) {

            if (!StringUtils.hasText(learningPlanId)) {
                learningPlan = getPlanHelper().getLearningPlan(getUserSessionHelper().getStudentId());
            } else {
                try {
                    learningPlan = getAcademicPlanService().getLearningPlan(learningPlanId, PlanConstants.CONTEXT_INFO);
                } catch (DoesNotExistException e) {
                    LOG.warn("Learning plan " + learningPlanId + " does not exist", e);
                } catch (InvalidParameterException e) {
                    LOG.warn("Invalid learning plan ID " + learningPlanId, e);
                } catch (MissingParameterException e) {
                    throw new IllegalStateException("LP lookup failure", e);
                } catch (OperationFailedException e) {
                    throw new IllegalStateException("LP lookup failure", e);
                }
            }

        }

        return learningPlan;
    }

    @Override
    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = StringUtils.hasText(termId) ? termId : null;
        this.term = null;
    }

    @Override
    public boolean isPlanning() {
        return termId != null && getTermHelper().isPlanning(termId);
    }

    @Override
    public boolean isOfficial() {
        return termId != null && getTermHelper().isOfficial(termId);
    }

    @Override
    public Term getTerm() {
        return term == null && termId != null ? term = getTermHelper().getTermByAtpId(termId)
                : term;
    }

    @Override
    public String getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(String planItemId) {
        this.planItemId = StringUtils.hasText(planItemId) ? planItemId : null;
        this.planItem = null;
        this.course = null;
        this.existingPlanItems = null;
    }

    @Override
    public PlanItem getPlanItem() {
        if (planItem == null && planItemId != null) {
            try {
                planItem = getAcademicPlanService().getPlanItem(planItemId,
                        PlanConstants.CONTEXT_INFO);
            } catch (DoesNotExistException e) {
                LOG.warn("Plan item " + planItemId + " does not exist", e);
            } catch (InvalidParameterException e) {
                LOG.warn("Invalid plan item ID " + planItemId, e);
            } catch (MissingParameterException e) {
                throw new IllegalStateException("LP lookup failure", e);
            } catch (OperationFailedException e) {
                throw new IllegalStateException("LP lookup failure", e);
            }
        }
        return planItem;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = StringUtils.hasText(courseId) ? courseId : null;
        this.course = null;
        this.existingPlanItems = null;
    }

    @Override
    public Course getCourse() {
        if (course == null) {
            PlanItem planItem = getPlanItem();
            if (planItem != null) {
                course = getCourseHelper().getCourseInfo(planItem.getRefObjectId());
            } else if (courseId != null) {
                course = getCourseHelper().getCourseInfo(courseId);
            }
        }
        return course;
    }

    @Override
    public List<PlanItem> getExistingPlanItems() {
        if (existingPlanItems == null) {
            LearningPlan plan = getLearningPlan();
            PlanItem planItem = getPlanItem();
            if (planItem != null || courseId != null)
                try {
                    existingPlanItems = Collections.<PlanItem>unmodifiableList(getAcademicPlanService().getPlanItemsInPlanByRefObjectIdByRefObjectType(plan.getId(),
                            planItem == null ? courseId : planItem.getRefObjectId(), PlanConstants.COURSE_TYPE,
                            PlanConstants.CONTEXT_INFO));
                } catch (DoesNotExistException e) {
                    throw new IllegalArgumentException("LP lookup error", e);
                } catch (InvalidParameterException e) {
                    throw new IllegalArgumentException("LP lookup error", e);
                } catch (MissingParameterException e) {
                    throw new IllegalArgumentException("LP lookup error", e);
                } catch (OperationFailedException e) {
                    throw new IllegalStateException("LP lookup error", e);
                }
        }
        return existingPlanItems;
    }

    @Override
    public AcademicPlanServiceConstants.ItemCategory getExpectedPlanItemCategory() {
        return expectedPlanItemCategory;
    }

    @Override
    public void setExpectedPlanItemCategory(AcademicPlanServiceConstants.ItemCategory category) {
        this.expectedPlanItemCategory = category;
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

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = UwMyplanServiceLocator.getInstance().getCourseHelper();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    public TermHelper getTermHelper() {
        if (termHelper == null) {
            termHelper = UwMyplanServiceLocator.getInstance().getTermHelper();
        }
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
    }

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = UwMyplanServiceLocator.getInstance().getPlanHelper();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

}
