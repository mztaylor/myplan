package org.kuali.student.myplan.plan.service;

import org.apache.log4j.Logger;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

    private static UserSessionHelper userSessionHelper;

    private static CourseDetailsInquiryHelperImpl courseDetailsHelper;


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
        Map<String, Map<String, AcademicRecordDataObject>> academicRecordsByTerm = new HashMap<String, Map<String, AcademicRecordDataObject>>();
        /*********** Implementation to populate the plannedTerm list with academic record and planned terms ******************/
        if (studentCourseRecordInfos.size() > 0) {
            for (StudentCourseRecordInfo studentInfo : studentCourseRecordInfos) {
                if (termAtp.equalsIgnoreCase(studentInfo.getTermName())) {
                    /*Say If a course has A and AB activities then A is already added to the list then the next AB activity should not be created as a separate academicRecordDataObject it should be added the list of activities in academicRecordDataObject*/
                    if (academicRecordsByTerm.get(studentInfo.getTermName()) == null || (academicRecordsByTerm.get(studentInfo.getTermName()) != null && academicRecordsByTerm.get(studentInfo.getTermName()).get(studentInfo.getId()) == null)) {
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
                            List<String> activities = new ArrayList<String>();
                            activities.add(studentInfo.getActivityCode());
                            academicRecordDataObject.setActivityCode(activities);
                        }
                        academicRecordDataObject.setRepeated(studentInfo.getIsRepeated());
                        Map<String, AcademicRecordDataObject> academicRecordDataObjectMap = academicRecordsByTerm.get(studentInfo.getTermName());
                        if (academicRecordDataObjectMap == null) {
                            academicRecordDataObjectMap = new HashMap<String, AcademicRecordDataObject>();
                        }
                        academicRecordDataObjectMap.put(studentInfo.getId(), academicRecordDataObject);
                        academicRecordsByTerm.put(studentInfo.getTermName(), academicRecordDataObjectMap);
                    } else {
                        academicRecordsByTerm.get(studentInfo.getTermName()).get(studentInfo.getId()).getActivityCode().add(studentInfo.getActivityCode());
                    }
                }
            }
        }

        List<AcademicRecordDataObject> academicRecordDataObjectList = new ArrayList<AcademicRecordDataObject>();
        for (String term : academicRecordsByTerm.keySet()) {
            for (String courseId : academicRecordsByTerm.get(term).keySet()) {
                AcademicRecordDataObject academicRecordDataObject = academicRecordsByTerm.get(term).get(courseId);
                List<String> registeredActivities = academicRecordDataObject.getActivityCode();
                if (academicRecordDataObject.getCourseId() != null) {
                    List<ActivityOfferingItem> activityOfferingItemList = getCourseDetailsHelper().getActivityOfferingItemsByIdAndCd(academicRecordDataObject.getCourseId(), academicRecordDataObject.getCourseCode(), academicRecordDataObject.getAtpId());
                    for (ActivityOfferingItem activityOfferingItem : activityOfferingItemList) {
                        /*TODO: uncomment or remove once decision whether we have to show both primary and secondary activities is made
                        if (registeredActivities.contains(activityOfferingItem.getCode())) {
                            academicRecordDataObject.getActivityOfferingItem().add(activityOfferingItem);
                        } */
                        /*TODO: remove this if decision is to show both primary and secondary activities*/
                        if (registeredActivities.contains(activityOfferingItem.getCode()) && activityOfferingItem.isPrimary()) {
                            academicRecordDataObject.setActivityOfferingItem(activityOfferingItem);
                            break;
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

    public static CourseDetailsInquiryHelperImpl getCourseDetailsHelper() {
        if (courseDetailsHelper == null) {
            courseDetailsHelper = UwMyplanServiceLocator.getInstance().getCourseDetailsHelper();
        }
        return courseDetailsHelper;
    }

    public static void setCourseDetailsHelper(CourseDetailsInquiryHelperImpl courseDetailsHelper) {
        SingleQuarterHelperBase.courseDetailsHelper = courseDetailsHelper;
    }

    public static UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public static void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        SingleQuarterHelperBase.userSessionHelper = userSessionHelper;
    }
}
