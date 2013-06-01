package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/16/12
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class SingleQuarterHelperBase {
    /*Count of no of future years to be shown the quarter view */
    private static int futureTermsCount = 6;

    private static final Logger logger = Logger.getLogger(SingleQuarterHelperBase.class);

    private static String atpTerm1 = "1";
    private static String atpTerm2 = "2";
    private static String atpTerm3 = "3";
    private static String atpTerm4 = "4";


    public static PlannedTerm populatePlannedTerms(List<PlannedCourseDataObject> plannedCoursesList, List<PlannedCourseDataObject> backupCoursesList, List<StudentCourseRecordInfo> studentCourseRecordInfos, String termAtp, boolean isServiceUp) {


        String globalCurrentAtpId = null;
        if (isServiceUp) {
            globalCurrentAtpId = AtpHelper.getCurrentAtpId();
        } else {
//            globalCurrentAtpId = AtpHelper.populateAtpIdFromCalender().get(0).getId();
            globalCurrentAtpId = AtpHelper.getCurrentAtpIdFromCalender();

        }

        /*
        *  Populating the PlannedTerm List.
        */
        PlannedTerm plannedTerm = new PlannedTerm();
        plannedTerm.setAtpId(termAtp);
        plannedTerm.setQtrYear(AtpHelper.atpIdToTermName(termAtp));
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

            for (PlannedCourseDataObject backup : backupCoursesList) {
                String atp = backup.getPlanItemDataObject().getAtp();
                if (termAtp.equalsIgnoreCase(atp)) {
                    plannedTerm.getBackupList().add(backup);
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
                        List<ActivityOfferingItem> activityOfferingItemList = courseDetailsHelper.getActivityOfferingItemsById(academicRecordDataObject.getCourseId(), academicRecordDataObject.getAtpId());
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


}
