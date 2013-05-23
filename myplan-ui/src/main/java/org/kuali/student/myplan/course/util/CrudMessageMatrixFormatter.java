package org.kuali.student.myplan.course.util;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.dataobject.CourseOfferingInstitution;
import org.kuali.student.myplan.course.dataobject.CourseOfferingTerm;
import org.kuali.student.myplan.course.service.CourseDetailsInquiryHelperImpl;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlanItemDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.DateFormatHelper;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.springframework.util.StringUtils;

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

    private CourseHelper courseHelper;


    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

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
        if (courseDetails.getPlannedCourseSummary().getAcademicTerms().size() > 0) {
            List<String> withDrawnCourseTerms = new ArrayList<String>();
            List<String> nonWithDrawnCourseTerms = new ArrayList<String>();

            for (String term : courseDetails.getPlannedCourseSummary().getAcademicTerms()) {
                String atpId = AtpHelper.termToYearTerm(term).toATP();
                for (AcademicRecordDataObject academicRecordDataObject : courseDetails.getPlannedCourseSummary().getAcadRecList()) {
                    if (atpId.equalsIgnoreCase(academicRecordDataObject.getAtpId())
                            && academicRecordDataObject.getGrade().contains(PlanConstants.WITHDRAWN_GRADE)) {
                        if (!withDrawnCourseTerms.contains(term)) {
                            withDrawnCourseTerms.add(term);
                        }
                    }
                    if (atpId.equalsIgnoreCase(academicRecordDataObject.getAtpId())
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
                String atpId = AtpHelper.termToYearTerm(term).toATP();
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
                String atpId = AtpHelper.termToYearTerm(term).toATP();
                List<String> sections = getSections(courseDetails, term);
                String currentTerm = AtpHelper.getCurrentAtpId();
                if (atpId.compareToIgnoreCase(currentTerm) >= 0) {
                    if (counter2 == 0) {
                        String message = "You are enrolled in ";
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
                        sb = sb.append("<dd>").append(message).append(sec).append(" for ")
                                .append("<a href=plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=")
                                .append(atpId).append(">")
                                .append(term).append("</a>");
                        currentTermRegistered = true;
                    }
                    if (counter2 > 0) {
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
        if ((courseDetails.getPlannedCourseSummary().getPlannedList() != null && courseDetails.getPlannedCourseSummary().getPlannedList().size() > 0) || (courseDetails.getPlannedCourseSummary().getBackupList() != null && courseDetails.getPlannedCourseSummary().getBackupList().size() > 0)) {
            List<PlanItemDataObject> planItemDataObjects = new ArrayList<PlanItemDataObject>();
            if (courseDetails.getPlannedCourseSummary().getPlannedList() != null) {
                for (PlanItemDataObject pl : courseDetails.getPlannedCourseSummary().getPlannedList()) {
                    planItemDataObjects.add(pl);
                }
            }
            if (courseDetails.getPlannedCourseSummary().getBackupList() != null) {
                for (PlanItemDataObject bl : courseDetails.getPlannedCourseSummary().getBackupList()) {
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
                            sb = startsSub.append("<a href=\"plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=").append(AtpHelper.termToYearTerm(term).toATP()).append("\">").append(term).append(" plan").append("</a>").append(", ");
                        }
                        String formattedString = sb.substring(0, sb.lastIndexOf(","));
                        StringBuffer formattedSubBuf = new StringBuffer();
                        formattedSubBuf = formattedSubBuf.append(formattedString);
                        sb = formattedSubBuf.append(" on ").append(key);
                    } else {
                        String atpId = AtpHelper.termToYearTerm(planItemsMap.get(key)).toATP();
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
                            sb = sb.append("<a href=\"plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=").append(AtpHelper.termToYearTerm(term).toATP()).append("\">").append(term).append(" plan").append("</a> ").append(",");
                        }
                        String formattedString = sb.substring(0, sb.lastIndexOf(",") - 1);
                        StringBuffer formattedSubBuf = new StringBuffer();
                        formattedSubBuf = formattedSubBuf.append(formattedString);
                        sb = formattedSubBuf.append(" on ").append(key);
                    } else {
                        String atpId = AtpHelper.termToYearTerm(planItemsMap.get(key)).toATP();
                        sb = sb.append(" and ").append("<a href=\"plan?methodToCall=start&viewId=PlannedCourses-FormView&focusAtpId=").append(atpId).append("\">").append(planItemsMap.get(key)).append(" plan").append("</a> ")
                                .append(" on ").append(key);
                    }

                }
                count++;

            }
        }
        /*When savedItemId and savedItemDateCreated are not null then populating message
            *"Bookmarked on 8/15/2012"*/
        if (courseDetails.getPlannedCourseSummary().getSavedItemId() != null && courseDetails.getPlannedCourseSummary().getSavedItemDateCreated() != null) {
            sb = sb.append("<dd>").append("<a href=lookup?methodToCall=search&viewId=SavedCoursesDetail-LookupView>").append("Bookmarked").append("</a>").append(" on ").append(courseDetails.getPlannedCourseSummary().getSavedItemDateCreated());

        }


        return sb.toString();
    }

    /**
     * returns the section links string for given courseId and term in the form
     * ("<a href="http://localhost:8080/student/myplan/inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=60325fa8-7307-454a-be73-3cc1c642122d#kuali-uw-atp-2013-1-19889"> Section (SLN) </a>")
     *
     * @param courseDetails
     * @param term
     * @return
     */
    private List<String> getSections(CourseDetails courseDetails, String term) {
        AtpHelper.YearTerm yearTerm = AtpHelper.termToYearTerm(term);
        List<String> sections = new ArrayList<String>();
        List<String> sectionAndSln = new ArrayList<String>();
        for (AcademicRecordDataObject acr : courseDetails.getPlannedCourseSummary().getAcadRecList()) {
            if (acr.getAtpId().equalsIgnoreCase(AtpHelper.getAtpIdFromTermYear(term)) && StringUtils.hasText(acr.getActivityCode())) {
                sections.add(acr.getActivityCode());
            }
        }
        for (String section : sections) {
            String sln = getCourseHelper().getSLN(yearTerm.getYearAsString(), yearTerm.getTermAsString(), courseDetails.getCourseSummaryDetails().getSubjectArea(), courseDetails.getCourseSummaryDetails().getCourseNumber(), section);
            String sectionSln = String.format("Section %s (%s)", section, sln);
            String sec = null;
            CourseDetailsInquiryHelperImpl courseHelper = new CourseDetailsInquiryHelperImpl();
            List<String> terms = new ArrayList<String>();
            terms.add(term);
            List<CourseOfferingInstitution> courseOfferingInstitutions = courseHelper.getCourseOfferingInstitutionsById(courseDetails.getCourseSummaryDetails().getCourseId(), terms);
            if (AtpHelper.getPublishedTerms().contains(yearTerm.toATP()) && courseOfferingInstitutions != null && courseOfferingInstitutions.size() > 0) {
                sec = String.format("<a href=\"%s\">%s</a>", ConfigContext.getCurrentContextConfig().getProperty("appserver.url") + "/student/myplan/inquiry?methodToCall=start&viewId=CourseDetails-InquiryView&courseId=" + courseDetails.getCourseSummaryDetails().getCourseId() + "#" + AtpHelper.getAtpIdFromTermYear(term).replace(".", "-") + "-" + sln, sectionSln);
            } else {
                sec = sectionSln;
            }

            sectionAndSln.add(sec);
        }
        return sectionAndSln;
    }

}
