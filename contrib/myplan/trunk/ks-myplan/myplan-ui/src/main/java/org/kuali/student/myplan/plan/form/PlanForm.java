/* Copyright 2011 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 1.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl1.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.kuali.student.myplan.plan.form;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.CourseDetails;
import org.kuali.student.myplan.course.dataobject.CourseSummaryDetails;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseSummary;
import org.kuali.student.myplan.plan.dataobject.RecommendedItemDataObject;
import org.kuali.student.myplan.plan.util.AtpHelper;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Form for all plan item actions.
 */
public class PlanForm extends UifFormBase {

    private final Logger logger = Logger.getLogger(PlanForm.class);

    /**
     * The return code for a plan item add, change, or delete.
     */
    public enum REQUEST_STATUS {
        //Requested operation succeeded
        SUCCESS,
        //Requested operation failed.
        ERROR
    }

    /**
     * Request status for add, change, or delete of a plan item.
     */
    private REQUEST_STATUS requestStatus;


    /**
     * ********************************************************
     * Common properties
     * ********************************************************
     */
    private String planItemId;

    private String courseId;

    private String code;

    private String atpId;

    private String termName;

    // Used for populating the menu options for the Academic record
    private String acadRecAtpId;

    //   based on this Add to plan page items are populated
    private boolean moveCourse = false;

    //Form checkbox to determine plan item type (planned or backup).
    private boolean backup = false;

    /*Flag used for populating the exact menu items for a course in past,present, future terms */
    private boolean setToPlanning = false;

    //Used as a flag to differentiate courses being planned from recommendations
    private boolean recommended = false;


    /**
     * *******************************************************
     * Activity Planning properties
     * ********************************************************
     */
    private String sectionCode;

    private String registrationCode;

    private String primarySectionCode;

    private String primaryPlanItemId;

    private String primaryRegistrationCode;

    private String instituteCode;

    private boolean primary;

    private List<String> sectionsToDelete;

    private String activityStateKey;

    private CourseSummaryDetails courseSummaryDetails;

    private List<ActivityOfferingItem> planActivities;

    private PlannedCourseSummary plannedCourseSummary;

    //Flag Used for student to hide or un hide plan view to adviser
    private String enableAdviserView = PlanConstants.LEARNING_PLAN_ITEM_SHARED_TRUE_KEY;


    /**
     * ********************************************************
     * Student Academic Planner properties
     * ********************************************************
     */
    private int messagesCount = 0;

    private int bookmarkedCount = 0;

    private boolean newUser;

    /**
     * ********************************************************
     * Quick Add properties
     * *******************************************************
     */
    private String courseCd;

    private String type = "course";

    private String credit;

    private String note;

    private Date dateAdded;

    private String generalPlaceholder;

    private String placeholderCode;

    private String placeholderTitle;

    /**
     * ********************************************************
     * Recommended courses/placeholders properties
     * *******************************************************
     */

    private boolean owner;

    private String adviserName;

    /*Used only on the past recommended courses*/
    private String adviserNote;

    /**
     * *******************************************************
     * A list of javascript events as:
     * EVENT_NAME
     * param1: p1
     * param2: p2
     * PLAN_ITEM_ADDED
     * itemType: plannedCourse
     * planItem: pi1
     * courseId: c1
     * *******************************************************
     */
    private Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> javascriptEvents;

    public int getBookmarkedCount() {
        return bookmarkedCount;
    }

    public void setBookmarkedCount(int bookmarkedCount) {
        this.bookmarkedCount = bookmarkedCount;
    }

    public int getMessagesCount() {
        return messagesCount;
    }

    public void setMessagesCount(int messagesCount) {
        this.messagesCount = messagesCount;
    }

    public boolean isSetToPlanning() {
        return setToPlanning;
    }

    public void setSetToPlanning(boolean setToPlanning) {
        this.setToPlanning = setToPlanning;
    }

    public String getTermName() {
        return termName;
    }

    public void setTermName(String termName) {
        this.termName = termName;
    }

    public Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> getJavascriptEvents() {
        return javascriptEvents;
    }

    public void setJavascriptEvents(Map<PlanConstants.JS_EVENT_NAME, Map<String, String>> javascriptEvents) {
        this.javascriptEvents = javascriptEvents;
    }

    public String getAtpId() {
        return atpId;
    }

    public void setAtpId(String atpId) {
        this.atpId = atpId;
    }

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPlanItemId() {
        return planItemId;
    }

    public void setPlanItemId(String planItemId) {
        this.planItemId = planItemId;
    }

    public boolean isBackup() {
        return backup;
    }

    public void setBackup(boolean backup) {
        this.backup = backup;
    }

    public CourseSummaryDetails getCourseSummaryDetails() {
        if (courseSummaryDetails == null) {
            courseSummaryDetails = new CourseSummaryDetails();
        }
        return this.courseSummaryDetails;
    }

    public void setCourseSummaryDetails(CourseSummaryDetails courseSummaryDetails) {
        this.courseSummaryDetails = courseSummaryDetails;
    }

    public PlannedCourseSummary getPlannedCourseSummary() {
        return plannedCourseSummary;
    }

    public void setPlannedCourseSummary(PlannedCourseSummary plannedCourseSummary) {
        this.plannedCourseSummary = plannedCourseSummary;
    }

    public REQUEST_STATUS getRequestStatus() {
        return requestStatus;
    }

    public void setRequestStatus(REQUEST_STATUS requestStatus) {
        this.requestStatus = requestStatus;
    }

    public boolean isMoveCourse() {
        return moveCourse;
    }

    public void setMoveCourse(boolean moveCourse) {
        this.moveCourse = moveCourse;
    }

    public String getAcadRecAtpId() {
        return acadRecAtpId;
    }

    public void setAcadRecAtpId(String acadRecAtpId) {
        this.acadRecAtpId = acadRecAtpId;
    }

    public String getEnableAdviserView() {
        return enableAdviserView;
    }

    public void setEnableAdviserView(String enableAdviserView) {
        this.enableAdviserView = enableAdviserView;
    }

    public boolean isNewUser() {
        return newUser;
    }

    public void setNewUser(boolean newUser) {
        this.newUser = newUser;
    }

    public String getSectionCode() {
        return sectionCode;
    }

    public void setSectionCode(String sectionCode) {
        this.sectionCode = sectionCode;
    }

    public String getPrimarySectionCode() {
        return primarySectionCode;
    }

    public void setPrimarySectionCode(String primarySectionCode) {
        this.primarySectionCode = primarySectionCode;
    }

    public String getInstituteCode() {
        return instituteCode;
    }

    public void setInstituteCode(String instituteCode) {
        this.instituteCode = instituteCode;
    }

    public boolean isPrimary() {
        return primary;
    }

    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    public String getPrimaryPlanItemId() {
        return primaryPlanItemId;
    }

    public void setPrimaryPlanItemId(String primaryPlanItemId) {
        this.primaryPlanItemId = primaryPlanItemId;
    }

    public List<String> getSectionsToDelete() {
        return sectionsToDelete;
    }

    public void setSectionsToDelete(List<String> sectionsToDelete) {
        this.sectionsToDelete = sectionsToDelete;
    }

    public List<ActivityOfferingItem> getPlanActivities() {
        return planActivities;
    }

    public void setPlanActivities(List<ActivityOfferingItem> planActivities) {
        this.planActivities = planActivities;
    }

    public String getPrimaryRegistrationCode() {
        return primaryRegistrationCode;
    }

    public void setPrimaryRegistrationCode(String primaryRegistrationCode) {
        this.primaryRegistrationCode = primaryRegistrationCode;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public void setRegistrationCode(String registrationCode) {
        this.registrationCode = registrationCode;
    }

    public String getActivityStateKey() {
        return activityStateKey;
    }

    public void setActivityStateKey(String activityStateKey) {
        this.activityStateKey = activityStateKey;
    }

    public String getCourseCd() {
        return courseCd;
    }

    public void setCourseCd(String courseCd) {
        this.courseCd = courseCd;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Date getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(Date dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getGeneralPlaceholder() {
        return generalPlaceholder;
    }

    public void setGeneralPlaceholder(String generalPlaceholder) {
        this.generalPlaceholder = generalPlaceholder;
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

    public String getAdviserNote() {
        return adviserNote;
    }

    public void setAdviserNote(String adviserNote) {
        this.adviserNote = adviserNote;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
    }

    public String getAdviserName() {
        return adviserName;
    }

    public void setAdviserName(String adviserName) {
        this.adviserName = adviserName;
    }

    public boolean isRecommended() {
        return recommended;
    }

    public void setRecommended(boolean recommended) {
        this.recommended = recommended;
    }

    public String getPlaceholderTitle() {
        return placeholderTitle;
    }

    public void setPlaceholderTitle(String placeholderTitle) {
        this.placeholderTitle = placeholderTitle;
    }

    /**
     * Only used in the UI for getting the short Term
     */
    public String getShortTerm() {
        String shortTermName = "";
        if (getAtpId() != null) {
            shortTermName = AtpHelper.atpIdToShortTermName(getAtpId());
        }
        return shortTermName;
    }

    /**
     * Returns the list of events that should be
     */
    public String getJavascriptEventsAsJSON() {
        ObjectMapper mapper = new ObjectMapper();
        String jsonOut = null;
        try {
            //  Turn the list of javascript events into a string of JSON.
            jsonOut = mapper.writeValueAsString(javascriptEvents);
            jsonOut = StringEscapeUtils.unescapeJava(jsonOut);
        } catch (Exception e) {
            logger.error("Could not convert javascript events to JSON.", e);
            jsonOut = "";
        }

        //  TODO: Determine if there is a config that can be set to avoid having to do this.
        jsonOut = jsonOut.replaceAll("\"\\{", "{");
        jsonOut = jsonOut.replaceAll("}\"", "}");

        return jsonOut;
    }

    /**
     * Added this for using in the crud message matrix property editor
     */
    public CourseDetails getCourseAndPlanSummary() {
        CourseDetails courseDetails = new CourseDetails();
        courseDetails.setCourseSummaryDetails(this.getCourseSummaryDetails());
        courseDetails.setPlannedCourseSummary(this.getPlannedCourseSummary());
        return courseDetails;
    }


    /**
     * returns the recommended item data object for the term given.
     */
    public RecommendedItemDataObject getTermRecommendation() {
        for (RecommendedItemDataObject recommendedItem : getPlannedCourseSummary().getRecommendedItemDataObjects()) {
            if (recommendedItem.getAtpId().equals(getAtpId())) {
                return recommendedItem;
            }
        }
        return new RecommendedItemDataObject();
    }

    /**
     * Added this for using in the recommendations property editor
     */
    public CourseDetails getCoursePlanSummary() {
        return getCourseAndPlanSummary();
    }

    /*Added this because the List<?> is not accepted by the property editors.*/
    /*TODO: KUALI-RICE: Remove this once 6286 Upgrade to list with rice 2.2.1 and replace the prop name in definition*/
    public PlanForm getThis() {
        return this;
    }

    public String getPlaceholderCode() {
        return placeholderCode;
    }

    public void setPlaceholderCode(String placeholderCode) {
        this.placeholderCode = placeholderCode;
    }
}
