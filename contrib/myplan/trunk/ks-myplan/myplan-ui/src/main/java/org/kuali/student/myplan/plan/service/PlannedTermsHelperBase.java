package org.kuali.student.myplan.plan.service;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.academicrecord.dto.StudentCourseRecordInfo;
import org.kuali.student.enrollment.academicrecord.service.AcademicRecordService;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.dataobject.AcademicRecordDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedCourseDataObject;
import org.kuali.student.myplan.plan.dataobject.PlannedTerm;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.AtpHelper.YearTerm;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.DoesNotExistException;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.student.myplan.course.util.CourseSearchConstants.CONTEXT_INFO;
import static org.springframework.util.StringUtils.hasText;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/16/12
 * Time: 3:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class PlannedTermsHelperBase {

    private static final Logger logger = Logger.getLogger(PlannedTermsHelperBase.class);

    public transient AcademicRecordService academicRecordService;

    public transient AcademicPlanService academicPlanService;

    private transient CourseOfferingService courseOfferingService;

    private transient CourseHelper courseHelper;

    private static UserSessionHelper userSessionHelper;


    public static List<PlannedTerm> populatePlannedTerms(List<PlannedCourseDataObject> plannedCoursesList, List<PlannedCourseDataObject> backupCoursesList, List<PlannedCourseDataObject> recommendedCoursesList, List<StudentCourseRecordInfo> studentCourseRecordInfos, String focusAtpId, int futureTerms, boolean fullPlanView) {

        String globalCurrentAtpId = null;
        globalCurrentAtpId = AtpHelper.getCurrentAtpId();
        if (StringUtils.isEmpty(focusAtpId)) {
            focusAtpId = AtpHelper.getFirstPlanTerm();
        }

        String[] focusQuarterYear = new String[2];
        try {
            String firstAtpId = AtpHelper.getFirstAtpIdOfAcademicYear(focusAtpId);
            focusQuarterYear = AtpHelper.atpIdToTermAndYear(firstAtpId);
        } catch (Exception e) {
            //  Log and set the year to the current year.
            //  TODO: This logic isn't correct, but does position the quarter view pretty close.
            logger.error("Could not get the requested focus ATP, so using the current academic year.", e);
            String year = String.valueOf(Calendar.getInstance().get(Calendar.YEAR) - 1);
            focusQuarterYear[0] = PlanConstants.ATP_TERM_1;
            focusQuarterYear[1] = year;
        }


        /*
        *  Populating the PlannedTerm List.
        */
        List<PlannedTerm> plannedTerms = new ArrayList<PlannedTerm>();
        if (plannedCoursesList != null && plannedCoursesList.size() > 0) {

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
                boolean exists = false;
                for (PlannedTerm term : plannedTerms) {
                    if (term.getAtpId().equalsIgnoreCase(atp)) {
                        term.getPlannedList().add(plan);
                        exists = true;
                    }
                }
                if (!exists) {
                    PlannedTerm term = new PlannedTerm();
                    term.setAtpId(atp);
                    StringBuilder sb = new StringBuilder();
                    sb.append(AtpHelper.atpIdToTermName(atp));
                    String QtrYear = sb.substring(0, 1).toUpperCase().concat(sb.substring(1));
                    term.setQtrYear(QtrYear);
                    term.getPlannedList().add(plan);
                    plannedTerms.add(term);
                }
            }
        }
        /*
         * Populating the backup list for the Plans
        */
        if (backupCoursesList != null && backupCoursesList.size() > 0) {

            /*Sorting planned courses and placeHolders*/
            Collections.sort(backupCoursesList, new Comparator<PlannedCourseDataObject>() {
                @Override
                public int compare(PlannedCourseDataObject p1, PlannedCourseDataObject p2) {
                    boolean v1 = p1.isPlaceHolder();
                    boolean v2 = p2.isPlaceHolder();
                    return v1 == v2 ? 0 : (v1 ? 1 : -1);
                }
            });


            int count = plannedTerms.size();
            for (PlannedCourseDataObject bl : backupCoursesList) {
                String atp = bl.getPlanItemDataObject().getAtp();
                boolean added = false;
                for (int i = 0; i < count; i++) {
                    if (atp.equalsIgnoreCase(plannedTerms.get(i).getAtpId())) {
                        plannedTerms.get(i).getBackupList().add(bl);
                        added = true;
                    }
                }
                if (!added) {
                    PlannedTerm plannedTerm = new PlannedTerm();
                    plannedTerm.setAtpId(atp);
                    StringBuffer str = new StringBuffer();
                    str = str.append(AtpHelper.atpIdToTermName(atp));
                    String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
                    plannedTerm.setQtrYear(QtrYear);
                    plannedTerm.getBackupList().add(bl);
                    plannedTerms.add(plannedTerm);
                    count++;
                }
            }
        }

        /*
         * Populating the backup list for the Plans
        */
        if (recommendedCoursesList != null && recommendedCoursesList.size() > 0) {

            /*Sorting planned courses and placeHolders*/
            Collections.sort(recommendedCoursesList, new Comparator<PlannedCourseDataObject>() {
                @Override
                public int compare(PlannedCourseDataObject p1, PlannedCourseDataObject p2) {
                    boolean v1 = p1.isPlaceHolder();
                    boolean v2 = p2.isPlaceHolder();
                    return v1 == v2 ? 0 : (v1 ? 1 : -1);
                }
            });


            int count = plannedTerms.size();
            for (PlannedCourseDataObject bl : recommendedCoursesList) {
                String atp = bl.getPlanItemDataObject().getAtp();
                boolean added = false;
                for (int i = 0; i < count; i++) {
                    if (atp.equalsIgnoreCase(plannedTerms.get(i).getAtpId())) {
                        plannedTerms.get(i).getRecommendedList().add(bl);
                        added = true;
                    }
                }
                if (!added) {
                    PlannedTerm plannedTerm = new PlannedTerm();
                    plannedTerm.setAtpId(atp);
                    StringBuffer str = new StringBuffer();
                    str = str.append(AtpHelper.atpIdToTermName(atp));
                    String QtrYear = str.substring(0, 1).toUpperCase().concat(str.substring(1, str.length()));
                    plannedTerm.setQtrYear(QtrYear);
                    plannedTerm.getRecommendedList().add(bl);
                    plannedTerms.add(plannedTerm);
                    count++;
                }
            }
        }

        /*
        * Used for sorting the planItemDataobjects
        */
        Collections.sort(plannedTerms, new Comparator<PlannedTerm>() {
            @Override
            public int compare(PlannedTerm plannedTerm1, PlannedTerm plannedTerm2) {
                return plannedTerm1.getAtpId().compareTo(plannedTerm2.getAtpId());
            }
        });

        /*********** Implementation to populate the plannedTerm list with academic record and planned terms ******************/
        if ((studentCourseRecordInfos != null && studentCourseRecordInfos.size() > 0) || plannedTerms.size() > 0) {
            Map<String, PlannedTerm> termsList = new HashMap<String, PlannedTerm>();
            String minTerm = null;
            if (studentCourseRecordInfos != null && studentCourseRecordInfos.size() > 0) {
                minTerm = studentCourseRecordInfos.get(0).getTermName();
            } else {
                minTerm = globalCurrentAtpId;
            }
            String maxTerm = null;
            if (plannedTerms.size() > 0 && fullPlanView) {
                maxTerm = plannedTerms.get(plannedTerms.size() - 1).getAtpId();
            } else {
                maxTerm = globalCurrentAtpId;
            }
            populateMockList(minTerm, maxTerm, termsList, futureTerms);
            if (plannedTerms.size() > 0) {
                for (PlannedTerm plannedTerm : plannedTerms) {
                    if (termsList.containsKey(plannedTerm.getAtpId())) {
                        if (plannedTerm.getPlannedList().size() > 0 || plannedTerm.getBackupList().size() > 0 || plannedTerm.getRecommendedList().size() > 0) {
                            termsList.get(plannedTerm.getAtpId());
                            termsList.put(plannedTerm.getAtpId(), plannedTerm);
                        }
                    }
                }
            }
            Map<String, Map<String, AcademicRecordDataObject>> academicRecordsByTerm = new HashMap<String, Map<String, AcademicRecordDataObject>>();
            if (studentCourseRecordInfos != null && studentCourseRecordInfos.size() > 0) {
                for (StudentCourseRecordInfo studentInfo : studentCourseRecordInfos) {
                    if (termsList.containsKey(studentInfo.getTermName())) {
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
                            if (AtpHelper.isAtpSetToPlanning(studentInfo.getTermName())) {
                                List<String> activities = new ArrayList<String>();
                                activities.add(studentInfo.getActivityCode());
                                academicRecordDataObject.setActivityCode(activities);
                            }
                            if (!"X".equalsIgnoreCase(studentInfo.getCalculatedGradeValue())) {
                                academicRecordDataObject.setGrade(studentInfo.getCalculatedGradeValue());
                            } else if ("X".equalsIgnoreCase(studentInfo.getCalculatedGradeValue()) && AtpHelper.isAtpCompletedTerm(studentInfo.getTermName())) {
                                academicRecordDataObject.setGrade(studentInfo.getCalculatedGradeValue());
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
            for (String term : academicRecordsByTerm.keySet()) {
                for (String courseId : academicRecordsByTerm.get(term).keySet()) {
                    AcademicRecordDataObject academicRecordDataObject = academicRecordsByTerm.get(term).get(courseId);
                    termsList.get(term).getAcademicRecord().add(academicRecordDataObject);
                }
            }
            List<PlannedTerm> perfectPlannedTerms = new ArrayList<PlannedTerm>();
            for (String key : termsList.keySet()) {
                perfectPlannedTerms.add(termsList.get(key));
            }

            Collections.sort(perfectPlannedTerms,
                    new Comparator<PlannedTerm>() {
                        @Override
                        public int compare(PlannedTerm plannedTerm1, PlannedTerm plannedTerm2) {
                            return plannedTerm1.getAtpId().compareTo(plannedTerm2.getAtpId());
                        }
                    });
            //  Can't do this step until the sort has been done else the index won't be correct.
            int i = 0;
            for (PlannedTerm pt : perfectPlannedTerms) {
                String[] qy = AtpHelper.atpIdToTermAndYear(pt.getAtpId());
                if (qy[0].equals(focusQuarterYear[0])
                        && qy[1].equals(focusQuarterYear[1])) {
                    pt.setIndex(i);
                    break;
                }
                i++;
            }


            /*Implementation to set the conditional flags based on each plannedTerm atpId*/
            for (PlannedTerm pl : perfectPlannedTerms) {

                if (AtpHelper.isAtpSetToPlanning(pl.getAtpId())) {
                    pl.setOpenForPlanning(true);
                }
                if (AtpHelper.isAtpCompletedTerm(pl.getAtpId())) {
                    pl.setCompletedTerm(true);
                }
                if (globalCurrentAtpId.equalsIgnoreCase(pl.getAtpId())) {
                    pl.setCurrentTermForView(true);
                }

            }

            populateHelpIconFlags(perfectPlannedTerms);
            populateSingleQuarterAtpIds(perfectPlannedTerms);
            return perfectPlannedTerms;
        }

        /*Implementation to populate the future terms till 6 years from current term if academic record data and planned term data are NOT present*/
        else {
            List<PlannedTerm> plannedTermList = new ArrayList<PlannedTerm>();
            populateFutureData(globalCurrentAtpId, plannedTermList, futureTerms);
            /*Implementation to set the conditional flags based on each plannedTerm atpId*/

            for (PlannedTerm pl : plannedTermList) {

                if (AtpHelper.isAtpSetToPlanning(pl.getAtpId())) {
                    pl.setOpenForPlanning(true);
                }
                if (AtpHelper.isAtpCompletedTerm(pl.getAtpId())) {
                    pl.setCompletedTerm(true);
                }
                if (globalCurrentAtpId.equalsIgnoreCase(pl.getAtpId())) {
                    pl.setCurrentTermForView(true);
                }

            }

            //  Can't do this step until the sort has been done else the index won't be correct.
            int i = 0;
            for (PlannedTerm pt : plannedTermList) {
                String[] qy = AtpHelper.atpIdToTermAndYear(pt.getAtpId());
                if (qy[0].equals(focusQuarterYear[0])
                        && qy[1].equals(focusQuarterYear[1])) {
                    pt.setIndex(i);
                    break;
                }
                i++;
            }

            populateHelpIconFlags(plannedTermList);
            populateSingleQuarterAtpIds(plannedTermList);
            return plannedTermList;


        }
    }

    /**
     * Aap with key as atpId and value as PlannedTerm is populated starting from minTerm to ((year Of maxTerm) + futureTermsCount)
     *
     * @param minTerm
     * @param maxTerm
     * @param map
     * @param futureTermsCount
     */
    private static void populateMockList(String minTerm, String maxTerm, Map<String, PlannedTerm> map, int futureTermsCount) {
        YearTerm minYearTerm = AtpHelper.atpToYearTerm(minTerm);
        YearTerm maxYearTerm = AtpHelper.atpToYearTerm(maxTerm);

        if (minYearTerm.getTerm() != Integer.parseInt(PlanConstants.ATP_TERM_4)) {
            minTerm = new YearTerm(minYearTerm.getYear() - 1, Integer.parseInt(PlanConstants.ATP_TERM_4)).toATP();
        }
        maxTerm = new YearTerm(maxYearTerm.getYear() + futureTermsCount, Integer.parseInt(PlanConstants.ATP_TERM_3)).toATP();
        List<YearTerm> futureAtpIds = AtpHelper.getFutureYearTerms(minTerm, maxTerm);
        for (YearTerm yearTerm : futureAtpIds) {
            PlannedTerm plannedTerm = new PlannedTerm();
            plannedTerm.setAtpId(yearTerm.toATP());
            plannedTerm.setQtrYear(yearTerm.toLabel());
            map.put(yearTerm.toATP(), plannedTerm);
        }
    }


    /**
     * PlannedTermList is populated starting from the given atpId to ((year of atpId) + futureTermsCount)
     *
     * @param atpId
     * @param plannedTermList
     * @param futureTermsCount
     */
    private static void populateFutureData(String atpId, List<PlannedTerm> plannedTermList, int futureTermsCount) {
        YearTerm minYearTerm = AtpHelper.atpToYearTerm(atpId);
        if (minYearTerm.getTerm() != Integer.parseInt(PlanConstants.ATP_TERM_4)) {
            minYearTerm = new YearTerm(minYearTerm.getYear() - 1, Integer.parseInt(PlanConstants.ATP_TERM_4));
        }
        YearTerm maxYearTerm = new YearTerm(minYearTerm.getYear() + futureTermsCount, Integer.parseInt(PlanConstants.ATP_TERM_3));
        List<YearTerm> futureAtpIds = AtpHelper.getFutureYearTerms(minYearTerm.toATP(), maxYearTerm.toATP());
        for (YearTerm yearTerm : futureAtpIds) {
            PlannedTerm plannedTerm = new PlannedTerm();
            plannedTerm.setAtpId(yearTerm.toATP());
            plannedTerm.setQtrYear(yearTerm.toLabel());
            plannedTermList.add(plannedTerm);
        }

    }

    private static void populateHelpIconFlags(List<PlannedTerm> plannedTerms) {

        int index = plannedTerms.size() - 1;
        while (index >= 0) {
            for (int i = 4; i > 0; i--) {
                if (plannedTerms.get(index).isCurrentTermForView() || i == 1) {
                    plannedTerms.get(index).setDisplayBackupHelp(true);
                    plannedTerms.get(index).setDisplayPlannedHelp(true);
                    index = index - i;
                    break;
                }
                index--;
            }
        }
        index = 0;
        while (index < plannedTerms.size()) {
            for (int i = 1; i < 4; i++) {
                if (index < plannedTerms.size() && plannedTerms.get(index).isCompletedTerm() && i == 1) {
                    plannedTerms.get(index).setDisplayCompletedHelp(true);
                    index = index + (5 - i);
                    break;
                }
                index++;

            }
        }
        index = plannedTerms.size() - 1;
        while (index >= 0) {
            for (int i = 4; i > 0; i--) {
                if (plannedTerms.get(index).isCurrentTermForView() || !plannedTerms.get(index).isCompletedTerm() && (plannedTerms.get(index).getAcademicRecord().size() > 0 || !plannedTerms.get(index).isOpenForPlanning())) {
                    plannedTerms.get(index).setDisplayRegisteredHelp(true);
                    index = index - i;
                    break;
                }
                index--;
            }
        }


    }

    /**
     * Populating the single Quarter Term in plannedTerms for each academic year.
     *
     * @param plannedTerms
     */
    private static void populateSingleQuarterAtpIds(List<PlannedTerm> plannedTerms) {
        int index = 0;
        while (index < plannedTerms.size() - 1) {
            List<Integer> itemToUpdate = new ArrayList<Integer>();
            String singleQuarterAtpId = plannedTerms.get(index).getAtpId();
            boolean checkCompleted = false;
            boolean currentTermExists = false;
            for (int i = 0; i < 4; i++) {
                if (!checkCompleted && plannedTerms.get(index).isCurrentTermForView()) {
                    currentTermExists = true;
                }
                if (!checkCompleted && currentTermExists && plannedTerms.get(index).isOpenForPlanning()) {
                    singleQuarterAtpId = plannedTerms.get(index).getAtpId();
                    checkCompleted = true;
                }
                itemToUpdate.add(index);
                index++;
            }
            for (Integer i : itemToUpdate) {
                plannedTerms.get(i).setSingleQuarterAtp(singleQuarterAtpId);
            }
        }
    }

    /**
     * Creates the union of a list of credit ranges, returning the min and max credits found.
     * eg "1-2", "3-5" would return "1-5"
     */

    public static String unionCreditList(List<String> list) {
        if (list == null || list.isEmpty()) return "";
        float minCredits = Integer.MAX_VALUE;
        float maxCredits = Integer.MIN_VALUE;
        for (String item : list) {
            String[] split = item.split("[ ,/-]");

            String first = split[0];
            float min = Float.parseFloat(first);
            minCredits = Math.min(min, minCredits);

            String last = split[split.length - 1];
            float max = Float.parseFloat(last);
            maxCredits = Math.max(max, maxCredits);
        }

        String credits = Float.toString(minCredits);

        if (minCredits != maxCredits) {
            credits = credits + "-" + Float.toString(maxCredits);
        }

        credits = credits.replace(".0", "");
        return credits;
    }

    /**
     * returns a list of planned terms with Planned sections starting
     * from the current Atp which is open for planning.
     *
     * @return
     */
    public static List<PlannedTerm> getPlannedTermsFromStartAtp() {
        PlanItemLookupableHelperBase planItemLookupableHelperBase = new PlanItemLookupableHelperBase();
        List<PlannedCourseDataObject> plannedCourseDataObjects = new ArrayList<PlannedCourseDataObject>();
        String startAtp = AtpHelper.getFirstPlanTerm();
        try {
            plannedCourseDataObjects = planItemLookupableHelperBase.getPlannedCoursesFromAtp(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, getUserSessionHelper().getStudentId(), startAtp, false);
        } catch (Exception e) {
            logger.error("Could not retrieve the planItems" + e);
        }
        return populatePlannedTerms(plannedCourseDataObjects, null, null, null, null, 6, false);
    }


    /**
     * Creates the sum of a list of credit ranges, returning the cumulative min and max credits
     * eg "1-2", "3-5" would return "4-7"
     */

    public static String sumCreditList(List<String> list) {
        if (list == null || list.isEmpty()) return "0";
        float minCredits = 0;
        float maxCredits = 0;
        for (String item : list) {
            String[] split = item.split("[ ,/-]");

            String first = split[0];
            float min = Float.parseFloat(first);
            minCredits += min;

            String last = split[split.length - 1];
            float max = Float.parseFloat(last);
            maxCredits += max;
        }

        String credits = Float.toString(minCredits);

        if (minCredits != maxCredits) {
            credits = credits + "-" + Float.toString(maxCredits);
        }

        credits = credits.replace(".0", "");
        return credits;
    }

    /**
     * used to get the PlanItems for Audit learning plan type and for given refObjType
     *
     * @param refObjType
     * @param studentId
     * @return
     */
    public List<PlanItemInfo> getLatestSnapShotPlanItemsByRefObjType(String refObjType, String studentId) {
        List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
        try {
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(studentId, PlanConstants.LEARNING_PLAN_TYPE_PLAN_AUDIT, CONTEXT_INFO);
            if (learningPlanList.size() > 0) {
                LearningPlanInfo learningPlan = learningPlanList.get(learningPlanList.size() - 1);
                String learningPlanID = learningPlan.getId();
                List<PlanItemInfo> planItemInfoList = getAcademicPlanService().getPlanItemsInPlan(learningPlanID, CONTEXT_INFO);
                for (PlanItemInfo planItemInfo : planItemInfoList) {
                    if (planItemInfo.getTypeKey().equals(PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED) && planItemInfo.getRefObjectType().equalsIgnoreCase(refObjType)) {
                        planItemInfos.add(planItemInfo);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Cold not retrieve PlanItems", e);
        }
        return planItemInfos;
    }

    public AcademicRecordService getAcademicRecordService() {
        if (academicRecordService == null) {
            //   TODO: Use constants for namespace.
            academicRecordService = (AcademicRecordService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/academicrecord", "arService"));
        }
        return academicRecordService;
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }


    public void setAcademicRecordService(AcademicRecordService academicRecordService) {
        this.academicRecordService = academicRecordService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
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

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = UwMyplanServiceLocator.getInstance().getCourseHelper();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    public static UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public static void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        PlannedTermsHelperBase.userSessionHelper = userSessionHelper;
    }

    public String getTotalCredits(String termId) {
        String studentID = getUserSessionHelper().getStudentId();
        YearTerm yearTerm = AtpHelper.atpToYearTerm(termId);
        ArrayList<String> creditList = new ArrayList<String>();
        try {

            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(studentID,
                    PlanConstants.LEARNING_PLAN_TYPE_PLAN, CourseSearchConstants.CONTEXT_INFO);
            //This should be looping only once as student has only one learning plan of plan type
            for (LearningPlanInfo learningPlan : learningPlanList) {
                String learningPlanID = learningPlan.getId();

                List<PlanItemInfo> planItemList = getAcademicPlanService().getPlanItemsInPlanByAtp(learningPlanID,
                        termId, PlanConstants.LEARNING_PLAN_ITEM_TYPE_PLANNED, PlanConstants.CONTEXT_INFO);
                Map<String, List<String>> courseSectionCreditsMap = new HashMap<String, List<String>>();
                List<PlanItemInfo> plannedCourses = new ArrayList<PlanItemInfo>();
                for (PlanItemInfo planItem : planItemList) {
                    String luType = planItem.getRefObjectType();
                    if (PlanConstants.SECTION_TYPE.equalsIgnoreCase(luType)) {
                        CourseOfferingInfo courseOfferingInfo = null;
                        String courseOfferingId = planItem.getRefObjectId();
                        try {
                            courseOfferingInfo = getCourseOfferingService().getCourseOffering(courseOfferingId, CourseSearchConstants.CONTEXT_INFO);
                        } catch (DoesNotExistException e) {
                            //secondary section will return does not exist exceptions
                            continue;
                        }
                        if (courseOfferingInfo != null) {
                            String key = generateKey(courseOfferingInfo.getSubjectArea(), courseOfferingInfo.getCourseNumberSuffix());
                            if (courseSectionCreditsMap.containsKey(courseOfferingInfo.getCourseCode())) {
                                courseSectionCreditsMap.get(key).add(courseOfferingInfo.getCreditOptionName());
                            } else {
                                List<String> credits = new ArrayList<String>();
                                credits.add(courseOfferingInfo.getCreditOptionName());
                                courseSectionCreditsMap.put(key, credits);
                            }
                        }
                    } else {
                        plannedCourses.add(planItem);
                    }
                }
                for (PlanItemInfo planItemInfo : plannedCourses) {
                    String credit = null;
                    if (PlanConstants.COURSE_TYPE.equals(planItemInfo.getRefObjectType())) {
                        CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(planItemInfo.getRefObjectId(), null);
                        if (courseInfo != null) {
                            String key = generateKey(courseInfo.getSubjectArea().trim(), courseInfo.getCourseNumberSuffix());
                            List<String> sectionCreditRangeList = courseSectionCreditsMap.get(key);

                            if (sectionCreditRangeList != null && sectionCreditRangeList.size() > 0) {
                                credit = unionCreditList(sectionCreditRangeList);
                            } else {
                                credit = CreditsFormatter.formatCredits(courseInfo);
                            }
                        }
                    } else {
                        if ((PlanConstants.PLACE_HOLDER_TYPE_GEN_ED.equals(planItemInfo.getRefObjectType()) ||
                                PlanConstants.PLACE_HOLDER_TYPE.equals(planItemInfo.getRefObjectType()) ||
                                PlanConstants.PLACE_HOLDER_TYPE_COURSE_LEVEL.equals(planItemInfo.getRefObjectType())) &&
                                planItemInfo.getCredit() != null) {
                            credit = String.valueOf(planItemInfo.getCredit().intValue());
                        }
                    }
                    if (hasText(credit)) {
                        creditList.add(credit);
                    }
                }
                List<StudentCourseRecordInfo> recordList = getAcademicRecordService().getCompletedCourseRecords(studentID, PlanConstants.CONTEXT_INFO);
                for (StudentCourseRecordInfo record : recordList) {
                    if (record.getTermName().equalsIgnoreCase(termId)) {
                        String credit = record.getCreditsEarned();
                        if (hasText(credit)) {
                            creditList.add(credit);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("could not load total credits");
        }

        String credits = sumCreditList(creditList);
        return credits;
    }

    /**
     * returns a key in format 'subject=number', eg: CHEM=152
     *
     * @param subject
     * @param courseNumber
     * @return
     */
    private String generateKey(String subject, String courseNumber) {
        return getCourseHelper().joinStringsByDelimiter('=', subject, courseNumber);
    }

}
