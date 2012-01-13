package org.kuali.student.myplan.course.dataobject;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.myplan.academicplan.infc.LearningPlan;
import org.kuali.student.myplan.academicplan.infc.PlanItem;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.academicplan.service.mock.AcademicPlanServiceMockImpl;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.r2.common.dto.ContextInfo;

import javax.xml.namespace.QName;

public class SavedCoursesService {

    private transient CourseService courseService;

    protected synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    public synchronized void setCourseService( CourseService courseService ) {
        this.courseService = courseService;
    }


    public SavedCoursesService() {}


    private String userID;

    public String getUserID() {
        return userID;
    }

    public void setUserID( String userID ) {
        this.userID = userID;
    }

    public List<SavedCoursesItem> getSavedCoursesList() {
        try {
            List<SavedCoursesItem> savedCoursesList = new ArrayList<SavedCoursesItem>();

            CourseService courseService = getCourseService();
            AcademicPlanService academicPlanService = getAcademicPlanService();

            ContextInfo context = new ContextInfo();
            String studentID = "student1";
            String planTypeKey = "planTypeKey";


            List<LearningPlan> learningPlanList = academicPlanService.getLearningPlansForStudentByType( studentID, planTypeKey, context );
            for( LearningPlan learningPlan : learningPlanList ) {
                String learningPlanID = learningPlan.getId();
                List<PlanItem> planItemList = academicPlanService.getPlanItemsInPlan( learningPlanID, context );

                for( PlanItem planItem : planItemList )
                {
                    String courseID = planItem.getRefObjectId();
                    CourseInfo course = courseService.getCourse(courseID);

                    SavedCoursesItem item = new SavedCoursesItem();
                    item.setCourseID(courseID);
                    item.setCode(course.getCode());
                    item.setAdded( new Date() );
                    item.setCredit(CreditsFormatter.formatCredits(course));
                    item.setPrereqList( null );
                    item.setScheduleList( null );
                    item.setTitle( course.getCourseTitle() );
                    savedCoursesList.add( item );

                }
            }

            return savedCoursesList;

        } catch( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    private AcademicPlanService academicPlanService;

    public AcademicPlanService getAcademicPlanService() {
        if( academicPlanService == null ) {

            // TODO: wire this up to the global service locator doohickie
            academicPlanService = new AcademicPlanServiceMockImpl();
        }
        return academicPlanService;
    }

    public void setAcademicPlanService( AcademicPlanService academicPlanService ) {
        this.academicPlanService = academicPlanService;
    }

}
