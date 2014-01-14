package org.kuali.student.myplan.config;

import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.comment.util.CommentHelper;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.myplan.schedulebuilder.util.TermHelper;
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

    public CourseHelper getCourseHelper() {
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
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

    public TermHelper getTermHelper() {
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
    }

    private CourseHelper courseHelper;

    private PlanHelper planHelper;

    private UserSessionHelper userSessionHelper;

    private DegreeAuditHelper degreeAuditHelper;

    private CommentHelper commentHelper;

    private TermHelper termHelper;

    private Comparator<TypeInfo> atpTypeComparator;

}
