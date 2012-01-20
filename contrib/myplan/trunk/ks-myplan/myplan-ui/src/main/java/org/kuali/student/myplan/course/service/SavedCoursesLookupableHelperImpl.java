package org.kuali.student.myplan.course.service;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.lookup.LookupableImpl;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.rice.krad.web.form.LookupForm;
import org.kuali.rice.kim.api.identity.Person;

import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;

import org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants;
import org.kuali.student.myplan.course.dataobject.SavedCoursesItem;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.r2.common.dto.ContextInfo;

import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.mock.AcademicPlanServiceMockImpl;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class SavedCoursesLookupableHelperImpl extends LookupableImpl {
    private transient CourseService courseService;

    private transient AcademicPlanService academicPlanService;

    @Override
    protected List<?> getSearchResults(LookupForm lookupForm, Map<String, String> fieldValues, boolean unbounded) {
        try {
            List<SavedCoursesItem> savedCoursesList = new ArrayList<SavedCoursesItem>();

            CourseService courseService = getCourseService();
            AcademicPlanService academicPlanService = getAcademicPlanService();

            Person user = GlobalVariables.getUserSession().getPerson();

            ContextInfo context = new ContextInfo();
            String studentID = user.getPrincipalId();
            String planTypeKey = AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;

            List<LearningPlan> learningPlanList = academicPlanService.getLearningPlansForStudentByType(studentID, planTypeKey, context);
            for (LearningPlan learningPlan : learningPlanList) {
                String learningPlanID = learningPlan.getId();
                List<PlanItem> planItemList = academicPlanService.getPlanItemsInPlan(learningPlanID, context);

                for (PlanItem planItem : planItemList) {
                    String courseID = planItem.getRefObjectId();
                    CourseInfo course = courseService.getCourse(courseID);

                    SavedCoursesItem item = new SavedCoursesItem();
                    item.setCourseID(courseID);
                    item.setCode(course.getCode());
                    item.setAdded(new Date());
                    item.setCredit(CreditsFormatter.formatCredits(course));
                    item.setPrereqList(null);
                    item.setScheduleList(null);
                    item.setTitle(course.getCourseTitle());
                    savedCoursesList.add(item);

                }
            }

            return savedCoursesList;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                GlobalResourceLoader.getService(new QName(AcademicPlanServiceConstants.NAMESPACE,
                    AcademicPlanServiceConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }


    protected synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    public synchronized void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

}
