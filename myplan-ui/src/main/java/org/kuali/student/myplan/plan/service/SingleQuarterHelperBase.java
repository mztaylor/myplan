package org.kuali.student.myplan.plan.service;

import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/16/12
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SingleQuarterHelperBase {

    private static final Logger logger = Logger.getLogger(SingleQuarterHelperBase.class);

    @Autowired
    private static UserSessionHelper userSessionHelper;


    public static PlannedTerm populatePlannedTerms(List<PlannedCourseDataObject> plannedCoursesList, List<PlannedCourseDataObject> backupCoursesList, List<PlannedCourseDataObject> recommendedCoursesList, List<StudentCourseRecordInfo> studentCourseRecordInfos, String termAtp) {


        String globalCurrentAtpId = AtpHelper.getCurrentAtpId();

        /*
        *  Populating the PlannedTerm List.
        */
        PlannedTerm plannedTerm = new PlannedTerm();
        plannedTerm.setAtpId(termAtp);
        plannedTerm.setQtrYear(AtpHelper.atpIdToTermName(termAtp));
        /*Sorting planned courses and placeHolders*/
        Collections.sort(plannedCoursesList, new Comparator<PlannedCourseDataObject>() {
            @Override
            public int compare(PlannedCourseDataObject p1, PlannedCourseDataObject p2) {
                boolean v1 = p1.isPlaceHolder();
                boolean v2 = p2.isPlaceHolder();
                return v1 == v2 ? 0 : (v1 ? 1 : -1);
            }
        });
        for (PlannedCourseDataObject plan : plannedCoursesList) {
            String atp = plan.getPlanItemDataObject().getAtp();
            if (termAtp.equalsIgnoreCase(atp)) {
                plannedTerm.getPlannedList().add(plan);
            }
        }

        /*
         * Populating the backup list for the Plans
        */

        if (backupCoursesList != null) {
            /*Sorting planned courses and placeHolders*/
            Collections.sort(backupCoursesList, new Comparator<PlannedCourseDataObject>() {
                @Override
                public int compare(PlannedCourseDataObject p1, PlannedCourseDataObject p2) {
                    boolean v1 = p1.isPlaceHolder();
                    boolean v2 = p2.isPlaceHolder();
                    return v1 == v2 ? 0 : (v1 ? 1 : -1);
                }
            });
            for (PlannedCourseDataObject backup : backupCoursesList) {
                String atp = backup.getPlanItemDataObject().getAtp();
                if (termAtp.equalsIgnoreCase(atp)) {
                    plannedTerm.getBackupList().add(backup);
                }
            }


        }

        /*
         * Populating the recommended list for the Plans
        */

        if (recommendedCoursesList != null) {
            /*Sorting planned courses and placeHolders*/
            Collections.sort(recommendedCoursesList, new Comparator<PlannedCourseDataObject>() {
                @Override
                public int compare(PlannedCourseDataObject p1, PlannedCourseDataObject p2) {
                    boolean v1 = p1.isPlaceHolder();
                    boolean v2 = p2.isPlaceHolder();
                    return v1 == v2 ? 0 : (v1 ? 1 : -1);
                }
            });

            for (PlannedCourseDataObject recommended : recommendedCoursesList) {
                String atp = recommended.getPlanItemDataObject().getAtp();
                if (termAtp.equalsIgnoreCase(atp)) {
                    plannedTerm.getRecommendedList().add(recommended);
                }
            }


        }

        List<AcademicRecordDataObject> academicRecordDataObjectList = new ArrayList<AcademicRecordDataObject>();

        /*********** Implementation to populate the plannedTerm list with academic record and planned terms ******************/
        if (studentCourseRecordInfos.size() > 0) {
            for (StudentCourseRecordInfo studentInfo : studentCourseRecordInfos) {
                if (termAtp.equalsIgnoreCase(studentInfo.getTermName())) {
                    CourseDetailsInquiryHelperImpl courseDetailsHelper = new CourseDetailsInquiryHelperImpl();
                    AcademicRecordDataObject academicRecordDataObject = new AcademicRecordDataObject();
                    academicRecordDataObject.setAtpId(studentInfo.getTermName());
                    academicRecordDataObject.setPersonId(studentInfo.getPersonId());
                    academicRecordDataObject.setCourseCode(studentInfo.getCourseCode());

                    /*TODO: StudentCourseRecordInfo does not have a courseId property so using Id to set the course Id*/
                    academicRecordDataObject.setCourseId(studentInfo.getId());
                    academicRecordDataObject.setCourseTitle(studentInfo.getCourseTitle());
                    academicRecordDataObject.setCredit(studentInfo.getCreditsEarned());
                    if (!"X".equalsIgnoreCase(studentInfo.getCalculatedGradeValue())) {
                        academicRecordDataObject.setGrade(studentInfo.getCalculatedGradeValue());
                    } else if ("X".equalsIgnoreCase(studentInfo.getCalculatedGradeValue()) && !AtpHelper.isAtpSetToPlanning(studentInfo.getTermName())) {
                        academicRecordDataObject.setGrade(studentInfo.getCalculatedGradeValue());
                    }
                    if (AtpHelper.isAtpSetToPlanning(studentInfo.getTermName())) {
                        academicRecordDataObject.setActivityCode(studentInfo.getActivityCode());
                    }
                    academicRecordDataObject.setRepeated(studentInfo.getIsRepeated());

                    //TODO: We should move these methods out of courseDetailsInquiryHelper
                    if (academicRecordDataObject.getCourseId() != null) {
                        List<ActivityOfferingItem> activityOfferingItemList = courseDetailsHelper.getActivityOfferingItemsByIdAndCd(academicRecordDataObject.getCourseId(), academicRecordDataObject.getCourseCode(), academicRecordDataObject.getAtpId());
                        for (ActivityOfferingItem activityOfferingItem : activityOfferingItemList) {
                            if (activityOfferingItem.getCode().equalsIgnoreCase(academicRecordDataObject.getActivityCode())) {
                                academicRecordDataObject.setActivityOfferingItem(activityOfferingItem);
                                break;
                            }

                        }
                    }


                    academicRecordDataObjectList.add(academicRecordDataObject);
                }
            }
        }

        plannedTerm.setAcademicRecord(academicRecordDataObjectList);


        /*Implementation to set the conditional flags based on each plannedTerm atpId*/


        if (AtpHelper.isAtpSetToPlanning(plannedTerm.getAtpId())) {
            plannedTerm.setOpenForPlanning(true);
        }
        if (AtpHelper.isAtpCompletedTerm(plannedTerm.getAtpId())) {
            plannedTerm.setCompletedTerm(true);
        }
        if (globalCurrentAtpId.equalsIgnoreCase(plannedTerm.getAtpId())) {
            plannedTerm.setCurrentTermForView(true);
        }

/*
        populateHelpIconFlags(perfectPlannedTerms);
*/
        return plannedTerm;

    }

    public static UserSessionHelper getUserSessionHelper() {
        if(userSessionHelper == null){
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public static void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        SingleQuarterHelperBase.userSessionHelper = userSessionHelper;
    }
}
