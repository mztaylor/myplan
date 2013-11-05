package org.kuali.student.myplan.plan.dataobject;

import org.apache.commons.lang.StringUtils;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.plan.service.PlannedTermsHelperBase;
import org.springframework.util.CollectionUtils;

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

    private transient boolean sectionsAvailable = true;

    private transient List<String> statusAlerts;

    // TODO: KULRICE-9003. This should be on plannedTerm once the jira is resolved
    private transient boolean timeScheduleOpen;

    private String note;

    /*Place Holder*/

    /*Needed for differentiating course and placeholders in UI*/
    private boolean placeHolder;

    private String placeHolderCode;

    private String placeHolderValue;

    private String placeHolderCredit;

    /*recommended adviser name*/
    private String adviserName;

    /*recommended item planned*/
    private boolean proposed;

    /*Boolean for planned or backup Items to know
    if they are added from adviser recommendation*/
    private boolean adviserRecommended;

    public CourseSummaryDetails getCourseDetails() {
        if (courseDetails == null) {
            System.out.println("COURSE DETAILS ARE NULL!!!");
        }

        return courseDetails;
    }

    public boolean isSectionsAvailable() {
        return sectionsAvailable;
    }

    public void setSectionsAvailable(boolean sectionsAvailable) {
        this.sectionsAvailable = sectionsAvailable;
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

    public List<String> getStatusAlerts() {
        if (statusAlerts == null) {
            statusAlerts = new ArrayList<String>();
        }
        return statusAlerts;
    }

    public void setStatusAlerts(List<String> statusAlerts) {
        this.statusAlerts = statusAlerts;
    }

    public String getPlaceHolderCode() {
        return placeHolderCode;
    }

    public void setPlaceHolderCode(String placeHolderCode) {
        this.placeHolderCode = placeHolderCode;
    }

    public String getPlaceHolderCredit() {
        return placeHolderCredit;
    }

    public void setPlaceHolderCredit(String placeHolderCredit) {
        this.placeHolderCredit = placeHolderCredit;
    }

    public boolean isPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(boolean placeHolder) {
        this.placeHolder = placeHolder;
    }

    public String getPlaceHolderValue() {
        return placeHolderValue;
    }

    public void setPlaceHolderValue(String placeHolderValue) {
        this.placeHolderValue = placeHolderValue;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        if (note != null) {
            note = note.replaceAll("\\u00a0", " ");
        }
        this.note = note;
    }

    public String getAdviserName() {
        return adviserName;
    }

    public void setAdviserName(String adviserName) {
        this.adviserName = adviserName;
    }

    public boolean isProposed() {
        return proposed;
    }

    public void setProposed(boolean proposed) {
        this.proposed = proposed;
    }

    public boolean isAdviserRecommended() {
        return adviserRecommended;
    }

    public void setAdviserRecommended(boolean adviserRecommended) {
        this.adviserRecommended = adviserRecommended;
    }

    //Used to get the list strings as a single string
    public String getAlertsAsString() {
        if (!CollectionUtils.isEmpty(getStatusAlerts())) {
            return StringUtils.join(getStatusAlerts(), "");
        }
        return null;
    }

    /*Added this for getting the Sections planned as a String to show in PlanView courses
* For eg: COM 322 "A, AA,.."*/
    public String getSections() {
        List<String> sections = new ArrayList<String>();
        if (getPlanActivities() != null && getPlanActivities().size() > 0) {
            for (ActivityOfferingItem activityOfferingItem : getPlanActivities()) {
                sections.add(activityOfferingItem.getCode());
            }
        }
        return StringUtils.join(sections.toArray(), ", ");
    }

    private String credit = null;

    public String getCredit() {
        if (credit == null) {
            if (!getPlanActivities().isEmpty()) {
                ArrayList<String> creditList = new ArrayList<String>();
                for (ActivityOfferingItem item : getPlanActivities()) {
                    if (item.isPrimary()) {
                        String credits = item.getCredits();
                        creditList.add(credits);
                    }
                }
                credit = PlannedTermsHelperBase.unionCreditList(creditList);
            } else if (placeHolderCredit != null) {
                credit = placeHolderCredit;
            } else if (courseDetails != null) {
                credit = courseDetails.getCredit();
            }
        }
        return credit;
    }


}
