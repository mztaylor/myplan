package org.kuali.student.myplan.plan.dataobject;

import org.apache.commons.lang.StringUtils;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;

import java.util.ArrayList;
import java.util.List;

/**
 * Captures a course detail object along with a single instance of its planned information.
 * <p/>
 * Date: 4/26/12
 */
public class PlannedCourseDataObject implements Comparable {

    private transient PlanItemDataObject planItemDataObject;

    private List<ActivityOfferingItem> planActivities;

    private transient CourseSummaryDetails courseDetails;

    private transient boolean showAlert;

    // TODO: KULRICE-9003. This should be on plannedTerm once the jira is resolved
    private transient boolean timeScheduleOpen;

    public CourseSummaryDetails getCourseDetails() {
        if (courseDetails == null) {
            System.out.println("COURSE DETAILS ARE NULL!!!");
        }

        return courseDetails;
    }

    public void setCourseDetails(CourseSummaryDetails courseDetails) {
        this.courseDetails = courseDetails;
    }

    public PlanItemDataObject getPlanItemDataObject() {
        return planItemDataObject;
    }

    public void setPlanItemDataObject(PlanItemDataObject planItemDataObject) {
        this.planItemDataObject = planItemDataObject;
    }

    public boolean isShowAlert() {
        return showAlert;
    }

    public void setShowAlert(boolean showAlert) {
        this.showAlert = showAlert;
    }

    @Override
    public int compareTo(Object object) {
        PlannedCourseDataObject that = (PlannedCourseDataObject) object;
        return this.getPlanItemDataObject().getDateAdded().compareTo(that.getPlanItemDataObject().getDateAdded()) * -1;
    }

    public boolean isTimeScheduleOpen() {
        return timeScheduleOpen;
    }

    public void setTimeScheduleOpen(boolean timeScheduleOpen) {
        this.timeScheduleOpen = timeScheduleOpen;
    }

    public List<ActivityOfferingItem> getPlanActivities() {
        if (planActivities == null) {
            planActivities = new ArrayList<ActivityOfferingItem>();
        }
        return planActivities;
    }

    public void setPlanActivities(List<ActivityOfferingItem> planActivities) {
        this.planActivities = planActivities;
    }

    /*Added this for getting the Sections planned as a String to show in PlanView courses
    * For eg: COM 322 "A, AA,.."*/
    public String getSections() {
        List<String> sections = new ArrayList<String>();
        if (getPlanActivities() != null && getPlanActivities().size() > 0) {
            for (ActivityOfferingItem activityOfferingItem : getPlanActivities()) {
                if (sections.size() == 2) {
                    return String.format("%s,..", StringUtils.join(sections.toArray(), ", "));
                }
                sections.add(activityOfferingItem.getCode());
            }
        }
        return StringUtils.join(sections.toArray(), ", ");
    }


    public String getCredit() {
        String credit = null;
        if (!getPlanActivities().isEmpty()) {
            ArrayList<String> creditList = new ArrayList<String>();
            for (ActivityOfferingItem item : getPlanActivities()) {
                if (item.isPrimary()) {
                    String credits = item.getCredits();
                    creditList.add(credits);
                }
            }
            credit = PlannedTermsHelperBase.sumCreditList(creditList);
        } else if (courseDetails != null) {
            credit = courseDetails.getCredit();
        }

        return credit;
    }


}
