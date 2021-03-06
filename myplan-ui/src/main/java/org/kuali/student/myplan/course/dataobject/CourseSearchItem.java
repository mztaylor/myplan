package org.kuali.student.myplan.course.dataobject;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.kuali.student.myplan.course.util.CollectionListPropertyEditorHtmlListType;
import org.kuali.student.myplan.course.util.FacetKeyFormatter;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: kmuthu
 * Date: 11/3/11
 * Time: 11:08 AM
 * <p/>
 * Wrapper for CourseInfo data.
 */
public class CourseSearchItem {

    public static final String EMPTY_RESULT_VALUE_KEY = "&mdash;";

    private String courseId;
    private String courseVersionIndependentId;
    private String code;

    private String number;
    private String subject;
    private String level;
    private String courseName;

    private String credit;
    private float creditMin;
    private float creditMax;
    private CreditType creditType;

    /*used for the section search*/
    private Set<String> meetingDaysAndTimes;
    /*Set to true if there are no secondary sections for a primary section OR primary sections has atLeast one secondary section*/
    private boolean sortToTop;

    private String genEduReq = EMPTY_RESULT_VALUE_KEY;

    public enum PlanState {
        UNPLANNED(""),
        SAVED("Bookmarked"),
        IN_PLAN("Planned");

        //  This is the value that will be displayed in the UI. (TODO: Read from properties file)
        private final String label;

        PlanState(String label) {
            this.label = label;
        }

        public String getLabel() {
            return this.label;
        }
    }

    private PlanState status = PlanState.UNPLANNED;

    /* Facet keys used for filtering in the view. */
    private Set<String> curriculumFacetKeys = new HashSet<String>();
    private Set<String> courseLevelFacetKeys = new HashSet<String>();
    private Set<String> genEduReqFacetKeys = new HashSet<String>();
    private Set<String> termsFacetKeys = new HashSet<String>();
    private Set<String> scheduledFacetKeys = new HashSet<String>();
    private Set<String> creditsFacetKeys = new HashSet<String>();
    private Set<String> meetingDayFacetKeys = new HashSet<String>();
    private Set<String> meetingTimeFacetKeys = new HashSet<String>();
    private Set<String> meetingDayTimeFacetKeys = new HashSet<String>();

    private List<TypeInfo> termInfoList;

    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    // aka KSLU_CLU_IDENT.DIVISION
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    public float getCreditMin() {
        return creditMin;
    }

    public void setCreditMin(float creditMin) {
        this.creditMin = creditMin;
    }

    public float getCreditMax() {
        return creditMax;
    }

    public void setCreditMax(float creditMax) {
        this.creditMax = creditMax;
    }

    public enum CreditType {fixed, range, multiple, unknown}

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }

    /**
     * TODO: This should be handled with a Formatter.
     *
     * @return
     */
    public String getScheduledAndOfferedTerms() {

        CollectionListPropertyEditorHtmlListType listType = CollectionListPropertyEditorHtmlListType.UL;

        Element termsList = DocumentHelper.createElement(listType.getListElementName()); // ul

        if (scheduledTermsList != null && scheduledTermsList.size() > 0) {
            Element termsListItem = termsList.addElement(listType.getListItemElementName()); // li
            termsListItem.addAttribute("class", "scheduled");
            Element scheduledListElement = termsListItem.addElement(listType.getListElementName()); //  ul
            for (String scheduledTerm : scheduledTermsList) {
                Element scheduledListItem = scheduledListElement.addElement(listType.getListItemElementName()); //  li
                String termAbbreviation = scheduledTerm.substring(0, 2).toUpperCase();
                scheduledListItem.addAttribute("class", termAbbreviation);
                String year = scheduledTerm.substring(scheduledTerm.length() - 2);
                scheduledListItem.setText(String.format("%s %s", termAbbreviation, year));
            }
        }

        if (termInfoList != null && termInfoList.size() > 0) {
            Element termsListItem = termsList.addElement(listType.getListItemElementName()); // li
            termsListItem.addAttribute("class", "projected");
            Element termListElement = termsListItem.addElement(listType.getListElementName()); // ul
            for (TypeInfo term : termInfoList) {
                Element scheduledListItem = termListElement.addElement(listType.getListItemElementName()); //li
                scheduledListItem.setText(term.getName().substring(0, 2).toUpperCase());
            }
        }
        return termsList.asXML();
    }

    public String getGenEduReq() {
        return genEduReq;
    }

    public void setGenEduReq(String genEduReq) {
        if (StringUtils.hasText(genEduReq)) {
            this.genEduReq = genEduReq;
        }
    }

    public PlanState getStatus() {
        return this.status;
    }

    public void setStatus(PlanState status) {
        this.status = status;
    }

    public boolean isStatusSaved() {
        return status == PlanState.SAVED;
    }

    public boolean isStatusInPlan() {
        return status == PlanState.IN_PLAN;
    }

    public boolean isStatusUnplanned() {
        return status == PlanState.UNPLANNED;
    }

    public Set<String> getCurriculumFacetKeys() {
        return curriculumFacetKeys;
    }

    public Set<String> getCourseLevelFacetKeys() {
        return courseLevelFacetKeys;
    }

    public Set<String> getGenEduReqFacetKeys() {
        return genEduReqFacetKeys;
    }

    public Set<String> getTermsFacetKeys() {
        return termsFacetKeys;
    }

    public Set<String> getScheduledFacetKeys() {
        return scheduledFacetKeys;
    }

    public Set<String> getCreditsFacetKeys() {
        return creditsFacetKeys;
    }

    /**
     * Get a combined set of terms and scheduled.
     *
     * @return
     */
    public Set<String> getQuartersFacetKeys() {
        Set<String> termsAndQuarters = new HashSet<String>();
        termsAndQuarters.addAll(scheduledFacetKeys);
        termsAndQuarters.addAll(termsFacetKeys);
        return termsAndQuarters;
    }

    public String getCourseLevelFacetKey() {
        return FacetKeyFormatter.format(courseLevelFacetKeys);
    }

    public String getCurriculumFacetKey() {
        return FacetKeyFormatter.format(curriculumFacetKeys);
    }

    public String getGenEduReqFacetKey() {
        return FacetKeyFormatter.format(genEduReqFacetKeys);
    }

    public String getTermsFacetKey() {
        return FacetKeyFormatter.format(termsFacetKeys);
    }

    public String getScheduledFacetKey() {
        return FacetKeyFormatter.format(scheduledFacetKeys);
    }

    public String getCreditsFacetKey() {
        return FacetKeyFormatter.format(creditsFacetKeys);
    }

    public String getQuartersFacetKey() {
        return FacetKeyFormatter.format(getQuartersFacetKeys());
    }

    public void setCurriculumFacetKeys(Set<String> curriculumFacetKeys) {
        this.curriculumFacetKeys = curriculumFacetKeys;
    }

    public void setCourseLevelFacetKeys(Set<String> courseLevelFacetKeys) {
        this.courseLevelFacetKeys = courseLevelFacetKeys;
    }

    public void setGenEduReqFacetKeys(Set<String> genEduReqFacetKeys) {
        this.genEduReqFacetKeys = genEduReqFacetKeys;
    }

    public void setTermsFacetKeys(Set<String> termsFacetKeys) {
        this.termsFacetKeys = termsFacetKeys;
    }

    public void setScheduledFacetKeys(Set<String> scheduledFacetKeys) {
        this.scheduledFacetKeys = scheduledFacetKeys;
    }

    public void setCreditsFacetKeys(Set<String> creditsFacetKeys) {
        this.creditsFacetKeys = creditsFacetKeys;
    }

    public List<TypeInfo> getTermInfoList() {
        return termInfoList;
    }

    public void setTermInfoList(List<TypeInfo> termInfoList) {
        this.termInfoList = termInfoList;
    }

    private List<String> scheduledTermsList = new ArrayList<String>();

    public List<String> getScheduledTermsList() {
        return this.scheduledTermsList;
    }

    public void setScheduledTerms(List<String> scheduledTermsList) {
        this.scheduledTermsList = scheduledTermsList;
    }

    public void addScheduledTerm(String term) {
        scheduledTermsList.add(term);
    }

    public String getCourseVersionIndependentId() {
        return courseVersionIndependentId;
    }

    public void setCourseVersionIndependentId(String courseVersionIndependentId) {
        this.courseVersionIndependentId = courseVersionIndependentId;
    }

    public Set<String> getMeetingDaysAndTimes() {
        if (meetingDaysAndTimes == null) {
            meetingDaysAndTimes = new HashSet<String>();
        }
        return meetingDaysAndTimes;
    }

    public void setMeetingDaysAndTimes(Set<String> meetingDaysAndTimes) {
        this.meetingDaysAndTimes = meetingDaysAndTimes;
    }

    public Set<String> getMeetingDayFacetKeys() {
        return meetingDayFacetKeys;
    }

    public void setMeetingDayFacetKeys(Set<String> meetingDayFacetKeys) {
        this.meetingDayFacetKeys = meetingDayFacetKeys;
    }

    public Set<String> getMeetingTimeFacetKeys() {
        return meetingTimeFacetKeys;
    }

    public void setMeetingTimeFacetKeys(Set<String> meetingTimeFacetKeys) {
        this.meetingTimeFacetKeys = meetingTimeFacetKeys;
    }

    public Set<String> getMeetingDayTimeFacetKeys() {
        return meetingDayTimeFacetKeys;
    }

    public void setMeetingDayTimeFacetKeys(Set<String> meetingDayTimeFacetKeys) {
        this.meetingDayTimeFacetKeys = meetingDayTimeFacetKeys;
    }

    public boolean isSortToTop() {
        return sortToTop;
    }

    public void setSortToTop(boolean sortToTop) {
        this.sortToTop = sortToTop;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getCode(), getCourseId());
    }
}