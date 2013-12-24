package org.kuali.student.myplan.plan.util;

import edu.uw.kuali.student.myplan.util.CourseHelperImpl;
import edu.uw.kuali.student.myplan.util.UserSessionHelperImpl;
import org.kuali.student.r2.core.class1.type.dto.TypeInfo;
import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.DeconstructedCourseCode;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.constants.CommonServiceConstants;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.exceptions.OperationFailedException;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.kuali.student.r2.core.atp.dto.AtpInfo;
import org.kuali.student.r2.core.atp.service.AtpService;
import org.kuali.student.r2.core.class1.type.service.TypeService;
import org.kuali.student.r2.core.constants.TypeServiceConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.kuali.rice.core.api.criteria.PredicateFactory.equalIgnoreCase;
import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;
import static org.kuali.student.myplan.course.util.CourseSearchConstants.CONTEXT_INFO;

/**
 * Helper methods for dealing with ATPs.
 */
public class AtpHelper {

    public static final String PRIORITY_ONE_REGISTRATION_START = "priority_one_registration_start";
    public static final String PRIORITY_ONE_REGISTRATION_END = "priority_one_registration_end";
    public static final String LAST_ADD_DAY = "last_add_day";

    public static enum TERMS {Autumn, Winter, Spring, Summer};

    private static transient AcademicCalendarService academicCalendarService;

    private static transient AcademicPlanService academicPlanService;

    public static int TERM_COUNT = 4;

    private static final Logger logger = Logger.getLogger(AtpHelper.class);

    private static transient CourseOfferingService courseOfferingService;

    private static transient AtpService atpService;

    private static transient AcademicRecordService academicRecordService;

    private static transient TypeService typeService;

    private static transient CourseHelper courseHelper;

    @Autowired
    private static UserSessionHelper userSessionHelper;


    /**
     * Query the Academic Calendar Service, determine the current ATP, and the return the ID.
     *
     * @return The ID of the current ATP.
     * @throws RuntimeException if the query fails or if the return data set doesn't make sense.
     */
    public static String getCurrentAtpId() {
        //   The first arg here is "usageKey" which isn't used.
        String currentAtpId = null;
        try {
            QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.INPROGRESS));
            List<TermInfo> inProgressTerms = getAcademicCalendarService().searchForTerms(query, CourseSearchConstants.CONTEXT_INFO);
            TermInfo currentTerm = inProgressTerms.get(0);
            currentAtpId = currentTerm.getId();
        } catch (Exception e) {
            logger.error("Query to Academic Calendar Service failed.", e);
            // If SWS Fails to load up scheduled Terms then current atp Id in TermInfo is populated from the calender month and year and set to the scheduledTerms list
            currentAtpId = getCurrentAtpIdFromCalender();
        }
        /*  The UW implementation of the AcademicCalendarService.getCurrentTerms() contains the "current term" logic so we can simply
            use the first item in the list. Although, TODO: Not sure if the order of the list is guaranteed, so maybe putting a sort here
            is the Right thing to do.*/
        return currentAtpId;
    }


    /**
     * Determines the current YearTerm
     *
     * @return YearTerm
     */
    public static YearTerm getCurrentYearTerm() {
        String atp = getCurrentAtpId();
        YearTerm yearTerm = atpToYearTerm(atp);
        return yearTerm;

    }

    /**
     * Query the Academic Calendar Service for terms that have offering's published, determine the last ATP, and return its ID.
     * <p/>
     * EXCEPTION FOR SUMMER: Change summer last published date to 3 weeks prior to registration for summer quarter to
     * update the course catalog for summer
     *
     * @return The ID of the last scheduled ATP.
     * @throws RuntimeException if the query fails or if the return data set doesn't make sense.
     */
    public static String getLastScheduledAtpId() {
        String lastAtpId;
        try {
            List<TermInfo> scheduledTerms = getAcademicCalendarService().searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PUBLISHED)), CourseSearchConstants.CONTEXT_INFO);

            //  The UW implementation of the AcademicCalendarService.getCurrentTerms() contains the "current term" logic so we can simply
            //  use the first item in the list. Although, TODO: Not sure if the order of the list is guaranteed, so maybe putting a sort here
            //  is the Right thing to do.
            TermInfo lastTerm = scheduledTerms.get(scheduledTerms.size() - 1);
            lastAtpId = lastTerm.getId();

            // SUMMER EXCEPTION
            if (lastAtpId.endsWith(PlanConstants.ATP_TERM_3)) {
                List<AttributeInfo> attrs = lastTerm.getAttributes();
                for (AttributeInfo attr : attrs) {
                    if (PRIORITY_ONE_REGISTRATION_START.equals(attr.getKey())) {
                        // Check to see if the priority registration is still more than 3 weeks away
                        DateTime regStart = new DateTime(attr.getValue());
                        if (regStart.minusWeeks(3).isAfterNow()) {
                            lastAtpId = scheduledTerms.get(scheduledTerms.size() - 2).getId();
                        }
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Query to Academic Calendar Service failed.", e);
            /*If SWS Fails to load up scheduled Terms then current atp Id in TermInfo is populated from the calender month and year and set to the scheduledTerms list*/
            lastAtpId = getCurrentAtpIdFromCalender();
        }


        return lastAtpId;
    }

    /**
     * Gets current calender yearTerm, Used when webservice is unavailable
     *
     * @return
     */
    public static YearTerm getCurrentYearTermFromCalender() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int term = monthToTerm(month);
        YearTerm yt = new YearTerm(year, term);
        return yt;
    }

    /**
     * Gets current calender atpId, Used when webservice is unavailable
     *
     * @return
     */
    public static String getCurrentAtpIdFromCalender() {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH);
        int term = monthToTerm(month);
        String atpId = YearTerm.toATP(year, term);
        return atpId;
    }

    /**
     * Java months are zero-based, January = 0 thru December = 11
     *
     * @param month
     * @return
     */
    private static int monthToTerm(int month) {
        int term = (month / 3) + 1;
        return term;
    }

    /**
     * Gets the ATP ID of the first ATP in the current academic year.
     *
     * @param atpId
     * @return
     */
    public static String getFirstAtpIdOfAcademicYear(String atpId) {
        String firstAtpId = null;
        YearTerm yearTerm = atpToYearTerm(atpId);

        //   If the term is not Autumn/4 then the beginning of the academic year is (year - 1) . 4
        if (yearTerm.getTermAsString().equals(PlanConstants.ATP_TERM_4)) {
            firstAtpId = atpId;
        } else {
            YearTerm yt = new YearTerm(yearTerm.getYear() - 1, Integer.valueOf(PlanConstants.ATP_TERM_4));
            firstAtpId = yt.toATP();
        }
        return firstAtpId;
    }

    /**
     * Returns an String[] {term, year} given an ATP ID.
     *
     * @param atpId
     * @return
     */
    public static String[] atpIdToTermAndYear(String atpId) {
        YearTerm yearTerm = atpToYearTerm(atpId);
        return new String[]{yearTerm.getTermAsString(), yearTerm.getYearAsString()};
    }


    /**
     * Returns ATP ID in format 19911 for term="Winter" and year = 1991
     *
     * @param termName
     * @param year
     * @return
     */
    public static String getAtpIdFromTermAndYear(String termName, String year) {
        return getAtpIdFromTermYear(String.format("%s %s", termName, year));
    }

    /*  Returns ATP ID in format 19911 for term="Winter 1991"*/
    public static String getAtpIdFromTermYear(String termYear) {
        YearTerm yearTerm = termToYearTerm(termYear);
        return yearTerm.toATP();
    }

    /**
     * Gets term name as "Spring 2012" given an ATP ID.
     *
     * @return
     */
    public static String atpIdToTermName(String atpId) {
        return atpId != null ? atpToYearTerm(atpId).toLabel() : null;
    }

    /**
     * Gives the short form of Term name
     * for given atp 20131 the short form is WI 13
     *
     * @param atpId
     * @return
     */
    public static String atpIdToShortTermName(String atpId) {
        return atpToYearTerm(atpId).toShortTermName();
    }

    /**
     * Returns true if an ATP is considered present or greater in the context of WHAT? Otherwise, false.
     *
     * @param atpId
     * @return
     */
    public static boolean isAtpSetToPlanning(String atpId) {
        YearTerm comparingYT = atpToYearTerm(atpId);
        YearTerm planningYT = null;
        try {
            List<TermInfo> planningTermInfo = getAcademicCalendarService().searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PLANNING)), CourseSearchConstants.CONTEXT_INFO);
            String planningAtpId = planningTermInfo.get(0).getId();
            planningYT = atpToYearTerm(planningAtpId);
        } catch (Exception e) {
            logger.error("Could not load planningTermInfo as service call failed", e);
            /*If SWS Fails to load up planningTermInfo  then current atp Id in TermInfo is populated from the calender month and year and set to the planningTermInfo list*/
            planningYT = AtpHelper.getCurrentYearTermFromCalender();
        }

        boolean isSetToPlanning = comparingYT.getValue() >= planningYT.getValue();

        return isSetToPlanning;
    }

    /**
     * Returns true if an ATP is considered a completed term Otherwise, false.
     *
     * @param atpId
     * @return
     */
    public static boolean isAtpCompletedTerm(String atpId) {
        return hasYearTermCompleted(atpToYearTerm(atpId));
    }


    /**
     * Returns true if an YearTerm is considered a completed term Otherwise, false.
     *
     * @param atp
     * @return
     */
    public static boolean hasYearTermCompleted(YearTerm atp) {
        YearTerm currentYT = getCurrentYearTerm();
        boolean completed = atp.getValue() < currentYT.getValue();
        return completed;
    }

    /**
     * Checks with the courseOffering service if the course is offered for the given term
     *
     * @param atp
     * @param course
     * @return
     */
    public static boolean isCourseOfferedInTerm(String atp, String course) {
        boolean isCourseOfferedInTerm = false;

        try {
            DeconstructedCourseCode courseCode = getCourseHelper().getCourseDivisionAndNumber(course);
            String courseId = getCourseHelper().getCourseId(courseCode.getSubject(), courseCode.getNumber());
            /*TODO: Replace the getCourseOfferingsByCourseAndTerm() with new one which accepts a composite key or courseId + course Cd instead of just a courseId*/
            String id = getCourseHelper().getKeyForCourseOffering(courseId, courseCode.getSubject().trim(), courseCode.getNumber().trim());
            List<CourseOfferingInfo> offerings = getCourseOfferingService().getCourseOfferingsByCourseAndTerm(id, atp, CourseSearchConstants.CONTEXT_INFO);
            if (offerings != null && !offerings.isEmpty()) {
                isCourseOfferedInTerm = true;
            }
        } catch (Exception e) {
            logger.error("Exception loading course offering for:" + course, e);
        }


        return isCourseOfferedInTerm;
    }

    /**
     * Gets published terms from Web service
     *
     * @return List of Published Terms as AtpId's
     */
    public static List<String> getPublishedTerms() {
        List<String> publishedTerms = new ArrayList<String>();
        try {
            List<TermInfo> termInfos = getAcademicCalendarService().searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PUBLISHED)), CourseSearchConstants.CONTEXT_INFO);
            for (TermInfo term : termInfos) {
                publishedTerms.add(term.getId());
            }
        } catch (Exception e) {
            logger.error("Web service call failed.", e);
            publishedTerms.add(getCurrentAtpIdFromCalender());
        }
        return publishedTerms;
    }


    /**
     * returns published terms for a campus
     *
     * @return
     */
    public static List<String> getPublishedTermsForCampus(String campusCd) {
        List<String> publishedTerms = new ArrayList<String>();
        try {
            List<TermInfo> termInfos = getAcademicCalendarService().searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", String.format("%s|%s", PlanConstants.PUBLISHED, campusCd))), CourseSearchConstants.CONTEXT_INFO);
            for (TermInfo term : termInfos) {
                publishedTerms.add(term.getId());
            }
        } catch (Exception e) {
            logger.error("Web service call failed.", e);
            publishedTerms.add(getCurrentAtpIdFromCalender());
        }
        return publishedTerms;
    }

    /**
     * Gets published terms from Web service
     *
     * @return List of Published Terms as YearTerms
     */
    public static List<YearTerm> getPublishedYearTermList() {
        List<YearTerm> publishedTerms = new ArrayList<YearTerm>();
        for (String publishedTerm : getPublishedTerms()) {
            publishedTerms.add(atpToYearTerm(publishedTerm));
        }
        return publishedTerms;
    }

    /**
     * Returns true if the given term has the registration Open or else false.
     *
     * @param atpId
     * @return
     */
    public static boolean isRegistrationOpen(String atpId) {
        boolean registrationOpen = false;
        try {
            List<TermInfo> termInfos = getAcademicCalendarService().searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PUBLISHED)), CourseSearchConstants.CONTEXT_INFO);
            for (TermInfo term : termInfos) {
                if (term.getId().equalsIgnoreCase(atpId)) {
                    for (AttributeInfo attributeInfo : term.getAttributes()) {
                        if (attributeInfo.getKey().equalsIgnoreCase(PRIORITY_ONE_REGISTRATION_END) && StringUtils.hasText(attributeInfo.getValue())) {
                            DateTime registrationEndDate = new DateTime(attributeInfo.getValue());
                            DateTime today = new DateTime();
                            if (today.isBefore(registrationEndDate)) {
                                registrationOpen = true;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Web service call failed.", e);
        }
        return registrationOpen;
    }

    /**
     * Returns the first term that is published
     *
     * @return
     */
    public static String getFirstPlanTerm() {
        String publishedTerms = null;
        try {
            List<TermInfo> termInfos = getAcademicCalendarService().searchForTerms(QueryByCriteria.Builder.fromPredicates(equalIgnoreCase("query", PlanConstants.PLANNING)), CourseSearchConstants.CONTEXT_INFO);
            if (termInfos != null && termInfos.size() > 0) {
                publishedTerms = termInfos.get(0).getId();
            }
        } catch (Exception e) {
            logger.error("Web service call failed.", e);
        }

        return publishedTerms;
    }

    /**
     * Checks with the atpService if the atpid exists.
     *
     * @param atpId
     * @return
     */
    public static boolean doesAtpExist(String atpId) {
        boolean doesAtpExist = false;
        try {
            AtpInfo atpInfo = getAtpService().getAtp(atpId, PlanConstants.CONTEXT_INFO);
            if (atpInfo != null) {
                doesAtpExist = true;
            }
        } catch (Exception e) {
            logger.error("Atp does not Exist", e);
        }
        return doesAtpExist;
    }

    /**
     * returns the List of YearTerms from the startAtp to LastAtp.
     * If lastAtp is null then the firstAtp year + MAX_FUTURE_YEARS(6) is used to get that
     *
     * @param startAtpId
     * @param lastAtpId
     * @return
     */
    public static List<YearTerm> getFutureYearTerms(String startAtpId, String lastAtpId) {
        List<YearTerm> futureAtpIds = new ArrayList<YearTerm>();
        if (StringUtils.hasText(startAtpId)) {
            YearTerm firstTerm = atpToYearTerm(startAtpId);
            YearTerm lastTerm = null;
            if (!StringUtils.hasText(lastAtpId)) {
                lastTerm = new YearTerm(firstTerm.getYear() + PlanConstants.MAX_FUTURE_YEARS, Integer.parseInt(PlanConstants.ATP_TERM_3));
            } else {
                lastTerm = atpToYearTerm(lastAtpId);
            }
            int year = firstTerm.getYear();
            int term = firstTerm.getTerm();
            YearTerm current = new YearTerm(0, 0);
            while (!current.equals(lastTerm)) {
                current = new YearTerm(year, term);
                if (AtpHelper.doesAtpExist(current.toATP())) {
                    futureAtpIds.add(current);
                }
                term++;
                if (term > 4) {
                    term = 1;
                    year++;
                }
            }
            if (futureAtpIds.size() > 0) {
                int index = futureAtpIds.size() - 1;
                while (futureAtpIds.get(index).getTerm() != Integer.parseInt(PlanConstants.ATP_TERM_3)) {
                    futureAtpIds.remove(futureAtpIds.get(index));
                    index--;
                    if (index < 0) {
                        break;
                    }
                }
            }

        }
        return futureAtpIds;
    }

    /**
     * Checks if the atpIf format is in valid form
     *
     * @param atpId
     * @return
     */

    public static boolean isAtpIdFormatValid(String atpId) {
        return atpId.matches(ATP_VALID_FORMAT);
    }


    /*Returns List of terms Available*/
    public static List<String> getTerms() {
        List<String> terms = new ArrayList<String>();
        for (TERMS terms1 : TERMS.values()) {
            terms.add(terms1.name());
        }
        return terms;
    }


    public static final Pattern ATP_REGEX = Pattern.compile("([0-9]{4})([1-4]{1})");
    public static final String ATP_VALID_FORMAT = "[0-9]{4}[1-4]{1}";
    public static final String ATP_FORMAT = "%d%d";

    public static List<String> TERM_ID_LIST = Arrays.asList("winter", "spring", "summer", "autumn");
    public static List<String> TERM_LABELS_LIST = Arrays.asList("Winter", "Spring", "Summer", "Autumn");

    public static class YearTerm implements Comparable<YearTerm>, Cloneable {
        private final int year;
        private final int term;

        public YearTerm(int year, int term) {
            this.year = year;
            this.term = term;
        }

        public int getYear() {
            return year;
        }

        public String getYearAsString() {
            return Integer.toString(getYear());
        }

        public String getTermAsString() {
            return Integer.toString(getTerm());
        }

        public int getTerm() {
            return term;
        }

        public String getTermAsID() {
            return TERM_ID_LIST.get(getTerm() - 1);
        }

        // "19991"
        public String toATP() {
            return toATP(year, term);
        }

        public static String toATP(int year, int term) {
            return String.format(ATP_FORMAT, year, term);
        }

        // "Winter 1999"
        public String toLabel() {
            return TERM_LABELS_LIST.get(getTerm() - 1) + " " + getYearAsString();
        }

        // "WIN+1999"
        public String toQTRYRParam() {
            return TERM_ID_LIST.get(getTerm() - 1).substring(0, 3).toUpperCase() + "+" + getYearAsString();
        }

        // 19911
        public String toUAchieveValue() {
            int value = year * 10 + term;
            String result = Integer.toString(value);
            return result;
        }

        /**
         * returns term as 'AU 13'
         *
         * @return
         */
        public String toShortTermName() {
            String a = TERM_ID_LIST.get(getTerm() - 1).substring(0, 2).toUpperCase();
            String b = getYearAsString().substring(2);
            return a + " " + b;
        }

        @Override
        public int compareTo(YearTerm that) {
            final int BEFORE = -1;
            final int EQUAL = 0;
            final int AFTER = 1;
            if (this == that) return EQUAL;
            int a = this.getValue();
            int b = that.getValue();
            if (a < b) return BEFORE;
            if (a > b) return AFTER;
            return EQUAL;
        }

        // Returns year + term as single value, so year 2013 term 2 returns value 20132
        public int getValue() {
            return year * 10 + term;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj instanceof YearTerm) {
                YearTerm that = (YearTerm) obj;
                return this.year == that.year && this.term == that.term;
            }
            return false;
        }

        public String toString() {
            return "year: " + year + " term: " + term + " (" + getTermAsID() + ")";
        }

        protected Object clone() throws CloneNotSupportedException {

            YearTerm clone = new YearTerm(year, term);
            return clone;
        }

    }

    /**
     * Converts Kuali UW ATP ids into a YearTerm object.
     * <p/>
     * eg "20121" becomes year = 2012, term = 1
     *
     * @param atp
     * @return
     */
    public static YearTerm atpToYearTerm(String atp) {
        if (!StringUtils.hasText(atp)) {
            throw new NullPointerException("atp");
        }

        Matcher m = ATP_REGEX.matcher(atp);
        if (m.find()) {
            int year = Integer.parseInt(m.group(1));
            int term = Integer.parseInt(m.group(2));
            return new YearTerm(year, term);
        }

        throw new IllegalArgumentException(atp);
    }

    public static final Pattern TERM_REGEX = Pattern.compile("(winter|spring|summer|autumn)\\s+([0-9]{4})");

    /**
     * Converts UW quarter string into a YearTerm object.
     * <p/>
     * eg "Winter 2012" becomes year = 2012, term = 1
     *
     * @param text
     * @return
     */
    public static YearTerm termToYearTerm(String text) {
        if (text == null) {
            throw new NullPointerException("text");
        }
        text = text.toLowerCase();

        Matcher m = TERM_REGEX.matcher(text);
        if (m.find()) {
            String temp = m.group(1);
            int term = TERM_ID_LIST.indexOf(temp) + 1;
            int year = Integer.parseInt(m.group(2));
            return new YearTerm(year, term);
        }
        throw new IllegalArgumentException(text);
    }

    /**
     * Converts UW quarter string into a YearTerm object.
     * <p/>
     * eg "Winter","2012" becomes year = 2012, term = 1
     *
     * @param
     * @return
     */
    public static YearTerm quarterYearToYearTerm(String quarter, String year) {
        if (quarter == null) {
            throw new NullPointerException("quarter");
        }
        if (year == null) {
            throw new NullPointerException("year");
        }
        quarter = quarter.toLowerCase();

        int term = TERM_ID_LIST.indexOf(quarter);
        if (term != -1) {
            term = term + 1;
            int y = Integer.parseInt(year);
            return new YearTerm(y, term);
        }
        throw new IllegalArgumentException(quarter + " " + year);
    }

    /**
     * Return ATP Type Name in display format
     * Takes TypeKey: 'kuali.uw.atp.type.winter' and returns TypeName: 'Winter'
     *
     * @param atpTypeKey Atp Type Key
     */
    public static String getAtpTypeName(String atpTypeKey) {
        String typeName = null;
        try {
            TypeInfo atpType = getTypeService().getType(atpTypeKey, PlanConstants.CONTEXT_INFO);
            if (atpType != null && !StringUtils.isEmpty(atpType.getName())) {
                typeName = WordUtils.capitalizeFully(atpType.getName().replace("quarter", "")).trim();
            }
        } catch (Exception e) {
            logger.error("ATP types lookup failed.", e);
        }
        return typeName;
    }


    /**
     * returns the previous AtpId. If not present returns null.
     *
     * @param atpId
     * @return
     */
    public static String getPreviousAtpId(String atpId) {
        YearTerm firstYearTermForStudent = getFirstAcademicPlanTermForStudent();
        YearTerm yt = AtpHelper.atpToYearTerm(atpId);
        if (yt.compareTo(firstYearTermForStudent) == 1) {
            int year = yt.getYear();
            int term = yt.getTerm() - 1;
            if (term == 0) {
                term = 4;
                year = year - 1;
            }
            String prev = YearTerm.toATP(year, term);
            if (doesAtpExist(prev)) {
                return prev;
            }
        }
        return null;
    }

    /**
     * returns the YearTerm of the firstTerm in student's AcademicPlan
     *
     * @return YearTerm
     */
    public static YearTerm getFirstAcademicPlanTermForStudent() {
        List<StudentCourseRecordInfo> studentCourseRecordInfos = null;
        try {
            studentCourseRecordInfos = getAcademicRecordService().getCompletedCourseRecords(getUserSessionHelper().getStudentId(), PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS");
        }
        return !CollectionUtils.isEmpty(studentCourseRecordInfos) ? atpToYearTerm(studentCourseRecordInfos.get(0).getTermName()) : atpToYearTerm(getCurrentAtpId());
    }


    /**
     * returns the YearTerm of the lastTerm in student's AcademicPlan
     *
     * @return YearTerm
     */
    public static YearTerm getLastAcademicPlanTerm() {
        List<YearTerm> futureTerms = getFutureYearTerms(AtpHelper.getCurrentAtpId(), null);
        return futureTerms.get(futureTerms.size() - 1);
    }


    /**
     * Provides the last planned or last registered term in academic plan
     *
     * @return
     */
    public static YearTerm getLastPlannedOrRegisteredTerm() {
        YearTerm lastPlannedTerm = atpToYearTerm(getCurrentAtpId());
        String regId = getUserSessionHelper().getStudentId();
        try {

            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(regId, LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);
            for (LearningPlanInfo learningPlan : learningPlanList) {
                String learningPlanID = learningPlan.getId();
                List<PlanItemInfo> planItemInfoList = getAcademicPlanService().getPlanItemsInPlan(learningPlanID, CONTEXT_INFO);
                for (PlanItemInfo planItemInfo : planItemInfoList) {
                    if ((planItemInfo.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) || planItemInfo.getTypeKey().equalsIgnoreCase(PlanConstants.LEARNING_PLAN_ITEM_TYPE_BACKUP))
                            && planItemInfo.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
                        if (planItemInfo.getPlanPeriods() != null && planItemInfo.getPlanPeriods().size() > 0) {
                            YearTerm yearTerm = atpToYearTerm(planItemInfo.getPlanPeriods().get(0));
                            if (yearTerm.compareTo(lastPlannedTerm) == 1) {
                                lastPlannedTerm = yearTerm;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Could not load the last Planned YearTerm");
        }

        List<StudentCourseRecordInfo> studentCourseRecordInfos = new ArrayList<StudentCourseRecordInfo>();

        try {
            studentCourseRecordInfos = getAcademicRecordService().getCompletedCourseRecords(regId, PlanConstants.CONTEXT_INFO);
        } catch (Exception e) {
            logger.error("Could not retrieve StudentCourseRecordInfo from the SWS.", e);
        }

        for (StudentCourseRecordInfo studentCourseRecordInfo : studentCourseRecordInfos) {
            YearTerm academicYearTerm = atpToYearTerm(studentCourseRecordInfo.getTermName());
            if (academicYearTerm.compareTo(lastPlannedTerm) == 1) {
                lastPlannedTerm = academicYearTerm;
            }
        }
        return lastPlannedTerm;
    }

    /**
     * returns the next closest atpId. If not present returns null.
     *
     * @param atpId
     * @return
     */
    public static String getNextAtpId(String atpId) {
        YearTerm lastYearTermForStudent = getLastAcademicPlanTerm();
        YearTerm yt = AtpHelper.atpToYearTerm(atpId);
        if (lastYearTermForStudent != null && lastYearTermForStudent.compareTo(yt) == 1) {
            int year = yt.getYear();
            int term = yt.getTerm() + 1;
            if (term == 5) {
                term = 1;
                year = year + 1;
            }
            String next = YearTerm.toATP(year, term);
            if (doesAtpExist(next)) {
                return next;
            }
        }
        return null;

    }

    public static CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public static void setCourseHelper(CourseHelper courseHelper) {
        AtpHelper.courseHelper = courseHelper;
    }


    public static AtpService getAtpService() {
        if (atpService == null) {
            atpService = (AtpService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/atp", "AtpService"));
        }
        return atpService;
    }

    public static void setAtpService(AtpService atpService) {
        AtpHelper.atpService = atpService;
    }

    private static CourseOfferingService getCourseOfferingService() {
        if (courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return courseOfferingService;
    }

    public static AcademicRecordService getAcademicRecordService() {
        if (academicRecordService == null) {
            //   TODO: Use constants for namespace.
            academicRecordService = (AcademicRecordService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/academicrecord", "arService"));
        }
        return academicRecordService;
    }


    private static AcademicCalendarService getAcademicCalendarService() {
        if (academicCalendarService == null) {
            academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return academicCalendarService;
    }

    public static AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public static void setAcademicPlanService(AcademicPlanService academicPlanService) {
        AtpHelper.academicPlanService = academicPlanService;
    }

    public static void setAcademicCalendarService(AcademicCalendarService academicCalendarService) {
        AtpHelper.academicCalendarService = academicCalendarService;
    }

    public static TypeService getTypeService() {
        if (typeService == null) {
            typeService = (TypeService)
                    GlobalResourceLoader.getService(new QName(TypeServiceConstants.NAMESPACE, TypeServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return typeService;
    }

    public static void setTypeService(TypeService typeService) {
        AtpHelper.typeService = typeService;
    }

    public static UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = new UserSessionHelperImpl();
        }
        return userSessionHelper;
    }

    public static void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        AtpHelper.userSessionHelper = userSessionHelper;
    }
}
