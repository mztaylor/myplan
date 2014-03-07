package org.kuali.student.myplan.config;

import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.comment.util.CommentHelper;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildHelper;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.myplan.schedulebuilder.util.ShoppingCartStrategy;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;

import java.util.Comparator;

/**
 * Created with IntelliJ IDEA.
 * User: hemanthg
 * Date: 1/14/14
 * Time: 10:55 AM
 * To change this template use File | Settings | File Templates.
 */
/*TODO: Move this to KSAPframework*/
public class UwMyplanServiceLocator {

    private static UwMyplanServiceLocator instance;


    public static UwMyplanServiceLocator getInstance() {
        return instance == null ? instance = new UwMyplanServiceLocator() : instance;
    }

    public static void setInstance(UwMyplanServiceLocator instance) {
        UwMyplanServiceLocator.instance = instance;
    }

    public PlanHelper getPlanHelper() {
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }

    public UserSessionHelper getUserSessionHelper() {
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

    public DegreeAuditHelper getDegreeAuditHelper() {
        return degreeAuditHelper;
    }

    public void setDegreeAuditHelper(DegreeAuditHelper degreeAuditHelper) {
        this.degreeAuditHelper = degreeAuditHelper;
    }

    public CommentHelper getCommentHelper() {
        return commentHelper;
    }

    public void setCommentHelper(CommentHelper commentHelper) {
        this.commentHelper = commentHelper;
    }

    public Comparator<TypeInfo> getAtpTypeComparator() {
        return atpTypeComparator;
    }

    public void setAtpTypeComparator(Comparator<TypeInfo> atpTypeComparator) {
        this.atpTypeComparator = atpTypeComparator;
    }

    public ScheduleBuildStrategy getScheduleBuildStrategy() {
        return scheduleBuildStrategy;
    }

    public void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        this.scheduleBuildStrategy = scheduleBuildStrategy;
    }

    public ScheduleBuildForm getScheduleBuildForm() {
        return scheduleBuildForm;
    }

    public void setScheduleBuildForm(ScheduleBuildForm scheduleBuildForm) {
        this.scheduleBuildForm = scheduleBuildForm;
    }

    public CourseDetailsInquiryHelperImpl getCourseDetailsHelper() {
        return courseDetailsHelper;
    }

    public void setCourseDetailsHelper(CourseDetailsInquiryHelperImpl courseDetailsHelper) {
        this.courseDetailsHelper = courseDetailsHelper;
    }

    public ShoppingCartStrategy getShoppingCartStrategy() {
        return shoppingCartStrategy;
    }

    public void setShoppingCartStrategy(ShoppingCartStrategy shoppingCartStrategy) {
        this.shoppingCartStrategy = shoppingCartStrategy;
    }

    public Comparator<ActivityOption> getActivityOptionComparator() {
        return activityOptionComparator;
    }

    public void setActivityOptionComparator(Comparator<ActivityOption> activityOptionComparator) {
        this.activityOptionComparator = activityOptionComparator;
    }

    public ScheduleBuildHelper getScheduleBuildHelper() {
        return scheduleBuildHelper;
    }

    public void setScheduleBuildHelper(ScheduleBuildHelper scheduleBuildHelper) {
        this.scheduleBuildHelper = scheduleBuildHelper;
    }

    private PlanHelper planHelper;

    private UserSessionHelper userSessionHelper;

    private DegreeAuditHelper degreeAuditHelper;

    private CommentHelper commentHelper;

    private Comparator<TypeInfo> atpTypeComparator;

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private ScheduleBuildForm scheduleBuildForm;

    private CourseDetailsInquiryHelperImpl courseDetailsHelper;

    private ShoppingCartStrategy shoppingCartStrategy;

    private Comparator<ActivityOption> activityOptionComparator;

    private ScheduleBuildHelper scheduleBuildHelper;

}
