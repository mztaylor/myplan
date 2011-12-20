package org.kuali.student.myplan.course.dataobject;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.myplan.course.util.CollectionListFormatterHtmlListType;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: kmuthu
 * Date: 11/3/11
 * Time: 11:08 AM
 *
 *  Wrapper for CourseInfo data.
 *
 */
public class CourseSearchItem {

    public static final String EMPTY_RESULT_VALUE_KEY = "&mdash;";

    private String courseId;
    private String code;

    private String number;
    private String subject;
    private String level;
    private String courseName;

    private String credit;
    private float creditMin;
    private float creditMax;
    private CreditType creditType;

    private String genEduReq = EMPTY_RESULT_VALUE_KEY;
    private String status;
    /* Facet keys used for filtering in the view. The value of the Map Entry isn't used. */
    private String curriculumFacetKey;

    private String courseLevelFacetKey;
    private String genEduReqFacetKey;
    private String termsFacetKey;
    private String scheduledFacetKey;
    private String creditsFacetKey;

    private List<AtpTypeInfo> termInfoList;
    private List<String> scheduledTermsList;


    public String getCourseId() {
        return courseId;
    }

    public void setCourseId(String courseId) {
        this.courseId = courseId;
    }

    public String getNumber() {
        return number;
    }
    
    public void setNumber( String number ) {
        this.number = number;
    }
    
    // aka KSLU_CLU_IDENT.DIVISION
    public String getSubject() {
        return subject;
    }

    public void setSubject( String subject ) {
        this.subject = subject;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getCodeFormatted() {
        return subject + "_" + number;
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

    public enum CreditType { fixed, range, multiple, unknown };

    public CreditType getCreditType() {
        return creditType;
    }

    public void setCreditType(CreditType creditType) {
        this.creditType = creditType;
    }

    /**
     * TODO: This should be handled with a Formatter.
     * @return
     */
    public String getScheduledAndOfferedTerms() {

        CollectionListFormatterHtmlListType listType = CollectionListFormatterHtmlListType.DD;

        Element termsList = DocumentHelper.createElement(listType.getListElementName()); // dl

        if (scheduledTermsList != null && scheduledTermsList.size() > 0) {
            Element termsListItem = termsList.addElement(listType.getListItemElementName()); // dd
            Element scheduledListElement = termsListItem.addElement(listType.getListElementName()); //  dl
            for (String scheduledTerm : scheduledTermsList) {
                Element scheduledListItem = scheduledListElement.addElement(listType.getListItemElementName()); //  dd
                String termAbbreviation = scheduledTerm.substring(0, 2).toUpperCase();
                String year = scheduledTerm.substring(scheduledTerm.length() - 2);
                scheduledListItem.setText(String.format("%s %s", termAbbreviation, year));
            }
        }

        if (termInfoList != null && termInfoList.size() > 0) {
            Element termsListItem = termsList.addElement(listType.getListItemElementName()); // dd
            Element termListElement = termsListItem.addElement(listType.getListElementName()); // dl
            for (AtpTypeInfo term : termInfoList) {
                Element scheduledListItem = termListElement.addElement(listType.getListItemElementName());
                scheduledListItem.setText(term.getName().substring(0, 2).toUpperCase());
            }
        }
        return termsList.asXML();
    }

    public String getGenEduReq() {
        return genEduReq;
    }

    public void setGenEduReq(String genEduReq) {
        if(StringUtils.hasText(genEduReq)) {
            this.genEduReq = genEduReq;
        }
    }

    public String getStatus() {
        return this.status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCurriculumFacetKey() {
        return curriculumFacetKey;
    }

    public void setCurriculumFacetKey(String curriculumFacetKey) {
        this.curriculumFacetKey = curriculumFacetKey;
    }

    public String getCourseLevelFacetKey() {
        return courseLevelFacetKey;
    }

    public void setCourseLevelFacetKey(String courseLevelFacetKey) {
        this.courseLevelFacetKey = courseLevelFacetKey;
    }

    public String getGenEduReqFacetKey() {
        return genEduReqFacetKey;
    }

    public void setGenEduReqFacetKey(String genEduReqFacetKey) {
        this.genEduReqFacetKey = genEduReqFacetKey;
    }

    public String getTermsFacetKey() {
        return termsFacetKey;
    }

    public void setTermsFacetKey(String termsFacetKey) {
        this.termsFacetKey = termsFacetKey;
    }

    public String getScheduledFacetKey() {
        return scheduledFacetKey;
    }

    public void setScheduledFacetKey(String scheduledFacetKey) {
        this.scheduledFacetKey = scheduledFacetKey;
    }

    public String getCreditsFacetKey() {
        return creditsFacetKey;
    }

    public void setCreditsFacetKey(String creditsFacetKey) {
        this.creditsFacetKey = creditsFacetKey;
    }

    public List<AtpTypeInfo> getTermInfoList() {
        return termInfoList;
    }

    public void setTermInfoList(List<AtpTypeInfo> termInfoList) {
        this.termInfoList = termInfoList;
    }

    public List<String> getScheduledTermsList() {
        return this.scheduledTermsList;
    }

    public void setScheduledTerms(List<String> scheduledTermsList) {
        this.scheduledTermsList = scheduledTermsList;
    }

    @Override
    public String toString() {
        return String.format("%s: %s", getCodeFormatted(), getCourseId());
    }
}