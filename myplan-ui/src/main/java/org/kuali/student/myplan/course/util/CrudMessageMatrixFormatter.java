package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.DateFormatHelper;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/3/12
 * Time: 10:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrudMessageMatrixFormatter extends PropertyEditorSupport {
    private final static Logger logger = Logger.getLogger(CrudMessageMatrixFormatter.class);


    @Override
    public void setValue(Object value) {
        super.setValue(value);
    }

    @Override
    public String getAsText() {

        CourseDetails courseDetails = (CourseDetails) super.getValue();
        StringBuffer sb = new StringBuffer();
        boolean currentTermRegistered = false;

        /*When academic terms are not null then populating message
        *"You took this course on Winter 2012" or
        *"This course was withdrawn on week 6 in Spring 2012" or
        *"You're enrolled in this course for Autumn 2012" */
        if (courseDetails.getAcademicTerms().size() > 0) {
            List<String> withDrawnCourseTerms = new ArrayList<String>();
            List<String> nonWithDrawnCourseTerms = new ArrayList<String>();

            for (String term : courseDetails.getAcademicTerms()) {
                String[] str = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                String atpId = AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim());
                for (AcademicRecordDataObject academicRecordDataObject : courseDetails.getAcadRecList()) {
                    if (academicRecordDataObject.getCourseId() != null
                            && academicRecordDataObject.getCourseId().equalsIgnoreCase(courseDetails.getCourseId())
                            && atpId.equalsIgnoreCase(academicRecordDataObject.getAtpId())
                            && academicRecordDataObject.getGrade().startsWith(PlanConstants.WITHDRAWN_GRADE)) {
                        withDrawnCourseTerms.add(term);
                    }
                    if (academicRecordDataObject.getCourseId() != null
                            && academicRecordDataObject.getCourseId().equalsIgnoreCase(courseDetails.getCourseId())
                            && atpId.equalsIgnoreCase(academicRecordDataObject.getAtpId())
                            && !academicRecordDataObject.getGrade().startsWith(PlanConstants.WITHDRAWN_GRADE)) {
                        nonWithDrawnCourseTerms.add(term);
                    }
                }

            }
            int counter = 0;
            for (String withdrawnTerm : withDrawnCourseTerms) {
                String term = withdrawnTerm;
                String[] splitStr = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                String atpId = AtpHelper.getAtpIdFromTermAndYear(splitStr[0].trim(), splitStr[1].trim());

                if (counter == 0) {
                    sb = sb.append("<dd>").append("This course was withdrawn in ")
                            .append("<a href=lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=")
                            .append(atpId).append(">")
                            .append(term).append("</a>");
                }
                if (counter > 0) {
                    sb = sb.append(",").append("<a href=lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=")
                            .append(atpId).append(">")
                            .append(term).append("</a>");
                }
                counter++;
            }


            int counter2 = 0;
            int counter3 = 0;
            for (String nonWithdrawnTerm : nonWithDrawnCourseTerms) {
                String term = nonWithdrawnTerm;
                String[] splitStr = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                String atpId = AtpHelper.getAtpIdFromTermAndYear(splitStr[0].trim(), splitStr[1].trim());
                List<TermInfo> scheduledTerms = null;
                String currentTerm = AtpHelper.getCurrentAtpId();
                if (atpId.compareToIgnoreCase(currentTerm) >= 0) {
                    if (counter2 == 0) {
                        sb = sb.append("<dd>").append("You're currently enrolled in this course for ")
                                .append("<a href=lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");
                        currentTermRegistered = true;
                    }
                    if (counter3 > 0) {
                        sb = sb.append(",").append("<a href=lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");
                        currentTermRegistered = true;
                    }
                    counter2++;
                } else {
                    if (counter3 == 0) {
                        sb = sb.append("<dd>").append("You took this course on ")
                                .append("<a href=lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");

                    }
                    if (counter3 > 0) {
                        sb = sb.append(",").append("<a href=lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");
                    }
                    counter3++;
                }


            }

        }

        /*When plannedList or backupList are not null then populating message
            *"Added to Spring 2013 Plan, Spring 2014 Plan on 01/18/2012" or
            *"Added to Spring 2013 Plan on 01/18/2012 and Spring 2014 Plan on 09/18/2012" */
        if (courseDetails.getPlannedList() != null || courseDetails.getBackupList() != null) {
            List<PlanItemDataObject> planItemDataObjects = new ArrayList<PlanItemDataObject>();
            if (courseDetails.getPlannedList() != null) {
                for (PlanItemDataObject pl : courseDetails.getPlannedList()) {
                    planItemDataObjects.add(pl);
                }
            }
            if (courseDetails.getBackupList() != null) {
                for (PlanItemDataObject bl : courseDetails.getBackupList()) {
                    planItemDataObjects.add(bl);
                }
            }
            /*Dividing the plan items on same date and different date*/
            Map<String, String> planItemsMap = new HashMap<String, String>();
            if (planItemDataObjects.size() > 0) {

                for (PlanItemDataObject pl : planItemDataObjects) {
                    String[] str = AtpHelper.atpIdToTermNameAndYear(pl.getAtp());
                    String date = DateFormatHelper.getDateFomatted(pl.getDateAdded().toString());
                    if (planItemsMap.containsKey(date)) {
                        StringBuffer sbuf = new StringBuffer();
                        sbuf = sbuf.append(planItemsMap.get(date)).append(",").append(str[0]).append(" ").append(str[1]);
                        planItemsMap.put(date, sbuf.toString());
                    } else {
                        planItemsMap.put(date, str[0] + " " + str[1]);
                    }
                }

            }
            int count = 0;
            StringBuffer startsSub = new StringBuffer();
            if(sb.toString().length()>0){
                startsSub=startsSub.append(sb);
            }
            if (!currentTermRegistered) {
                startsSub = startsSub.append("<dd>").append("Added to ");
            } else {
                startsSub = startsSub.append("<dd>").append("This course was also added to ");
            }
            StringBuffer sub = new StringBuffer();
            sub = sub.append(" and ");
            for (String key : planItemsMap.keySet()) {

                if (count == 0) {
                    if (planItemsMap.get(key).contains(",")) {
                        String[] terms = planItemsMap.get(key).split(",");
                        for (String term : terms) {
                            String[] str = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                            sb = startsSub.append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim())).append("\">").append(term).append(" plan").append("</a> ").append(", ");
                        }
                        String formattedString = sb.substring(0, sb.lastIndexOf(",") - 1);
                        StringBuffer formattedSubBuf = new StringBuffer();
                        formattedSubBuf = formattedSubBuf.append(formattedString);
                        sb = formattedSubBuf.append(" on ").append(key);
                    } else {
                        String[] str = planItemsMap.get(key).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        String atpId = AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim());
                        if (!currentTermRegistered) {
                            sb = sb.append("<dd>").append("Added to ").append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                                    .append(" on ").append(key).append(" ");
                        } else {
                            sb = sb.append("<dd>").append("This course was also added to ").append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                                    .append(" on ").append(key).append(" ");
                        }
                    }

                }
                if (count > 0) {
                    if (planItemsMap.get(key).contains(",")) {
                        String[] terms = planItemsMap.get(key).split(",");
                        for (String term : terms) {
                            String[] str = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                            sb = sub.append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim())).append("\">").append(term).append(" plan").append("</a> ").append(",");
                        }
                        String formattedString = sb.substring(0, sb.lastIndexOf(",") - 1);
                        StringBuffer formattedSubBuf = new StringBuffer();
                        formattedSubBuf = formattedSubBuf.append(formattedString);
                        sb = formattedSubBuf.append(" on ").append(key);
                    } else {
                        String[] str = planItemsMap.get(key).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        String atpId = AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim());
                        sb = sb.append(" and ").append("<a href=\"lookup?methodToCall=search&viewId=PlannedCourses-LookupView&criteriaFields['focusAtpId']=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                                .append(" on ").append(key);
                    }

                }
                count++;

            }
        }
        /*When savedItemId and savedItemDateCreated are not null then populating message
            *"Saved to Your Bookmark List on 8/15/2012" or
            *"Had saved to Your Bookmark List on 8/15/2012"*/
        if (courseDetails.getSavedItemId() != null && courseDetails.getSavedItemDateCreated() != null) {
            /*When planned List or backup list are equal to null then show message "Saved to Your Bookmark List on 8/15/2012"*/
            if (courseDetails.getPlannedList() == null && courseDetails.getBackupList() == null) {
                sb = sb.append("<dd>").append("Saved to your ")
                        .append("<a href=lookup?methodToCall=search&viewId=SavedCoursesDetail-LookupView>").append("Bookmarked Courses List").append("</a>").append(" on ").append(courseDetails.getSavedItemDateCreated());
            }
            /*When planned List or backup list are not null then show message "Had saved to Your Courses List on 8/15/2012"*/
            if (courseDetails.getPlannedList() != null || courseDetails.getBackupList() != null) {
                sb = sb.append("<dd>").append("Had saved to your ")
                        .append("<a href=lookup?methodToCall=search&viewId=SavedCoursesDetail-LookupView>").append("Bookmarked Courses List").append("</a>").append(" on ").append(courseDetails.getSavedItemDateCreated());
            }
        }


        return sb.toString();
    }

}
