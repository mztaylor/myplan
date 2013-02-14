package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.dataobject.CourseOfferingInstitution;
import org.kuali.student.myplan.course.dataobject.CourseOfferingTerm;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.DateFormatHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;

import javax.xml.namespace.QName;
import java.beans.PropertyEditorSupport;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/3/12
 * Time: 10:46 AM
 * To change this template use File | Settings | File Templates.
 */
public class CrudMessageMatrixFormatter extends PropertyEditorSupport {
    private final static Logger logger = Logger.getLogger(CrudMessageMatrixFormatter.class);

    private transient CourseOfferingService courseOfferingService;

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

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
                            && academicRecordDataObject.getGrade().contains(PlanConstants.WITHDRAWN_GRADE)) {
                        if (!withDrawnCourseTerms.contains(term)) {
                            withDrawnCourseTerms.add(term);
                        }
                    }
                    if (academicRecordDataObject.getCourseId() != null
                            && academicRecordDataObject.getCourseId().equalsIgnoreCase(courseDetails.getCourseId())
                            && atpId.equalsIgnoreCase(academicRecordDataObject.getAtpId())
                            && !academicRecordDataObject.getGrade().contains(PlanConstants.WITHDRAWN_GRADE)) {
                        if (!nonWithDrawnCourseTerms.contains(term)) {
                            nonWithDrawnCourseTerms.add(term);
                        }
                    }
                }
            }
            int counter = 0;
            for (String withdrawnTerm : withDrawnCourseTerms) {
                String term = withdrawnTerm;
                String[] splitStr = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                String atpId = AtpHelper.getAtpIdFromTermAndYear(splitStr[0].trim(), splitStr[1].trim());

                if (counter == 0) {
                    if (UserSessionHelper.isAdviser()) {
                        String user = UserSessionHelper.getStudentName();
                        sb = sb.append("<dd>").append(user + " withdrew from this course in ")
                                .append("<a href=plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");
                    } else {
                        sb = sb.append("<dd>").append("You withdrew from this course in ")
                                .append("<a href=plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");
                    }
                }
                if (counter > 0) {
                    sb = sb.append(",").append("<a href=plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=")
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
                List<String> sections = getSections(courseDetails, term);
                String currentTerm = AtpHelper.getCurrentAtpId();
                if (atpId.compareToIgnoreCase(currentTerm) >= 0) {
                    if (counter2 == 0) {
                        String message = "You're currently enrolled for ";
                        if (UserSessionHelper.isAdviser()) {
                            String user = UserSessionHelper.getStudentName();
                            message = user + ". currently enrolled in this course for ";
                        }
                        StringBuffer sec = new StringBuffer();
                        int count = 0;
                        for (String section : sections) {
                            if (count == 0) {
                                sec = sec.append(section);
                                count++;
                            } else {
                                sec = sec.append(" and ").append(section);
                                count++;
                            }
                        }
                        sb = sb.append("<dd>").append(message).append(sec).append(" in ")
                                .append("<a href=plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");
                        currentTermRegistered = true;
                    }
                    if (counter3 > 0) {
                        sb = sb.append(",").append("<a href=plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");
                        currentTermRegistered = true;
                    }
                    counter2++;
                } else {
                    if (counter3 == 0) {
                        String message = "You took this course in ";
                        if (UserSessionHelper.isAdviser()) {
                            String user = UserSessionHelper.getStudentName();
                            message = user + " took this course in ";
                        }
                        sb = sb.append("<dd>").append(message)
                                .append("<a href=plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");

                    }
                    if (counter3 > 0) {
                        sb = sb.append(", ").append("<a href=plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=")
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
        if ((courseDetails.getPlannedList() != null && courseDetails.getPlannedList().size() > 0) || (courseDetails.getBackupList() != null && courseDetails.getBackupList().size() > 0)) {
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
            if (sb.toString().length() > 0) {
                startsSub = startsSub.append(sb);
            }
            if (!currentTermRegistered) {
                startsSub = startsSub.append("<dd>").append("Added to ");
            } else {
                startsSub = startsSub.append("<dd>").append("This course was also added to ");
            }

            for (String key : planItemsMap.keySet()) {

                if (count == 0) {
                    if (planItemsMap.get(key).contains(",")) {
                        String[] terms = planItemsMap.get(key).split(",");
                        for (String term : terms) {
                            String[] str = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                            sb = startsSub.append("<a href=\"plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=").append(AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim())).append("\">").append(term).append(" plan").append("</a>").append(", ");
                        }
                        String formattedString = sb.substring(0, sb.lastIndexOf(","));
                        StringBuffer formattedSubBuf = new StringBuffer();
                        formattedSubBuf = formattedSubBuf.append(formattedString);
                        sb = formattedSubBuf.append(" on ").append(key);
                    } else {
                        String[] str = planItemsMap.get(key).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        String atpId = AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim());
                        if (!currentTermRegistered) {
                            sb = sb.append("<dd>").append("Added to ").append("<a href=\"plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                                    .append(" on ").append(key).append(" ");
                        } else {
                            sb = sb.append("<dd>").append("This course was also added to ").append("<a href=\"plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                                    .append(" on ").append(key).append(" ");
                        }
                    }

                }
                if (count > 0) {
                    if (planItemsMap.get(key).contains(",")) {
                        String[] terms = planItemsMap.get(key).split(",");
                        for (String term : terms) {
                            String[] str = term.split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                            sb = sb.append("<a href=\"plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=").append(AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim())).append("\">").append(term).append(" plan").append("</a> ").append(",");
                        }
                        String formattedString = sb.substring(0, sb.lastIndexOf(",") - 1);
                        StringBuffer formattedSubBuf = new StringBuffer();
                        formattedSubBuf = formattedSubBuf.append(formattedString);
                        sb = formattedSubBuf.append(" on ").append(key);
                    } else {
                        String[] str = planItemsMap.get(key).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");
                        String atpId = AtpHelper.getAtpIdFromTermAndYear(str[0].trim(), str[1].trim());
                        sb = sb.append(" and ").append("<a href=\"plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                                .append(" on ").append(key);
                    }

                }
                count++;

            }
        }
        /*When savedItemId and savedItemDateCreated are not null then populating message
            *"Bookmarked on 8/15/2012"*/
        if (courseDetails.getSavedItemId() != null && courseDetails.getSavedItemDateCreated() != null) {
            sb = sb.append("<dd>").append("<a href=lookup?methodToCall=search&viewId=SavedCoursesDetail-LookupView>").append("Bookmarked").append("</a>").append(" on ").append(courseDetails.getSavedItemDateCreated());

        }


        return sb.toString();
    }


    private List<String> getSections(CourseDetails courseDetails, String term) {
        String[] yearAndTerm = term.split(" ");
        String[] curricAndNum = courseDetails.getCode().split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)");

        List<String> sections = new ArrayList<String>();
        List<String> sectionAndSln = new ArrayList<String>();
        for (AcademicRecordDataObject acr : courseDetails.getAcadRecList()) {
            if (courseDetails.getCourseId().equalsIgnoreCase(acr.getCourseId())) {
                sections.add(acr.getActivityCode());
            }
        }
        for (String section : sections) {
            String sln = getSLN(yearAndTerm[1].trim(), yearAndTerm[0].trim(), curricAndNum[0].trim(), curricAndNum[1].trim(), section);
            String sectionSln = String.format("Section %s (%s)", section, sln);
            String sec = String.format("<a href=\"%s\">%s</a>", ConfigContext.getCurrentContextConfig().getProperty("appserver.url") + "/student/myplan/inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=" + courseDetails.getCourseId() + "#" + AtpHelper.getAtpIdFromTermYear(term).replace(".", "-") + "-" + sln, sectionSln);
            sectionAndSln.add(sec);
        }
        return sectionAndSln;
    }

    private String getSLN(String year, String term, String curriculum, String number, String section) {
        String sln = null;
        ActivityOfferingInfo activityOfferingInfo = new ActivityOfferingInfo();
        try {
            activityOfferingInfo = getCourseOfferingService().getActivityOffering(year + "," + term + "," + curriculum + "," + number + "," + section, CourseSearchConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("could not load the ActivityOfferinInfo from SWS", e);
        }
        for (AttributeInfo attributeInfo : activityOfferingInfo.getAttributes()) {
            if (attributeInfo.getKey().equalsIgnoreCase("SLN")) {
                sln = attributeInfo.getValue();
                break;
            }
        }
        return sln;
    }
}
