package edu.uw.kuali.student.myplan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.audit.dataobject.*;
import org.kuali.student.myplan.audit.dto.AuditReportInfo;
import org.kuali.student.myplan.audit.form.DegreeAuditForm;
import org.kuali.student.myplan.audit.form.PlanAuditForm;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.plan.util.PlanHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.lum.course.dto.CourseInfo;
import org.kuali.student.r2.lum.course.service.CourseService;
import org.kuali.student.r2.lum.util.constants.CourseServiceConstants;
import org.springframework.util.StringUtils;

import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.*;
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.*;
import static org.kuali.student.myplan.course.util.CourseSearchConstants.CONTEXT_INFO;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 6/24/13
 * Time: 3:05 PM
 * To change this template use File | Settings | File Templates.
 */
public class DegreeAuditHelperImpl implements DegreeAuditHelper {

    private final Logger logger = Logger.getLogger(DegreeAuditHelperImpl.class);

    private transient AcademicPlanService academicPlanService;

    private transient CourseService courseService;

    private transient CourseOfferingService courseOfferingService;

    private CourseHelper courseHelper;

    private PlanHelper planHelper;

    public enum Campus {
        CAMPUS_306("0"),
        CAMPUS_310("1"),
        CAMPUS_323("2");

        private static final Map<String, Campus> lookup
                = new HashMap<String, Campus>();

        static {
            for (Campus w : EnumSet.allOf(Campus.class))
                lookup.put(w.getCode(), w);
        }

        private String code;

        private Campus(String code) {
            this.code = code;
        }

        //code is the prefix value of the audit programs  0-->seattle, 1-->bothell, 2-->tacoma
        public String getCode() {
            return code;
        }

        //Id is the org value for campuses 306-->seattle, 310-->bothell, 323-->tacoma
        public String getId() {
            return this.name().replace("CAMPUS_", "");
        }

        // Takes a code and passes out a Id  0-->306, 1-->310, 2-->323
        public static Campus getId(String code) {
            return lookup.get(code);
        }

        // Takes a Id and passes out a code 306-->0, 310-->1, 323-->2
        public static Campus getCampusCode(String Id) {
            for (Map.Entry<String, Campus> entry : lookup.entrySet()) {
                if (entry.getValue().getId().equalsIgnoreCase(Id)) {
                    return entry.getValue();
                }
            }
            return null;
        }

    }

    public static class Choice implements Cloneable {
        public String credit = "";
        public String section = "";
        public String secondaryActivity = "";
        boolean crNcGradingOption = false;
        boolean writing = false;
        boolean honors = false;

        public int hashCode() {
            return credit.hashCode();
        }

        public boolean equals(Object that) {
            if (that == null) return false;
            if (this == that) return true;
            if (!(that instanceof Choice)) return false;
            if (!(writing == ((Choice) that).writing)) return false;
            if (!(honors == ((Choice) that).honors)) return false;
            if (!(crNcGradingOption == ((Choice) that).crNcGradingOption)) return false;
            return credit.equals(((Choice) that).credit);
        }

        public Object clone() throws CloneNotSupportedException {
            return super.clone();
        }

        // section:credits:display, eg "A:5:5 -- Writing"
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(section);
            sb.append(':');
            sb.append(secondaryActivity);
            sb.append(':');
            sb.append(credit);
            sb.append(':');
            sb.append(credit);
            if (writing) {
                sb.append(" -- ");
                sb.append(WRITING_CREDIT);
            }
            if (honors) {
                sb.append(" -- ");
                sb.append(HONORS_CREDIT);
            }
            if (crNcGradingOption) {
                sb.append(" -- ");
                sb.append(DegreeAuditConstants.CR_NO_CR_GRADING_OPTION);
            }
            return sb.toString();
        }

        /**
         * Format for the key is same as value the toString method returns
         *
         * @param key
         * @return
         */
        public Choice build(String key) {
            Choice choice = new Choice();
            String[] str = key.split(":");
            choice.section = StringUtils.hasText(str[0]) ? str[0] : "";
            choice.secondaryActivity = StringUtils.hasText(str[1]) ? str[1] : "";
            choice.credit = StringUtils.hasText(str[2]) ? str[2] : "";
            choice.writing = str[3].contains(WRITING_CREDIT);
            choice.honors = str[3].contains(HONORS_CREDIT);
            choice.crNcGradingOption = str[3].contains(CR_NO_CR_GRADING_OPTION);
            return choice;
        }
    }

    /**
     * Processes the planned items and activities and provides clean and messy items to be chosen by student
     *
     * @param planAuditForm
     * @return
     */
    @Override
    public PlanAuditForm processHandOff(PlanAuditForm planAuditForm, String studentId) {
        List<CourseItem> courseItems = planAuditForm.getCleanList();

        List<AtpHelper.YearTerm> publishedTerms = AtpHelper.getPublishedYearTermList();

        String startAtpId = AtpHelper.getFirstPlanTerm();

        Map<String, List<PlanItemInfo>> planItemsMap = populatePlanItemsMap(studentId);
        logger.info("Retrieved planned courses in " + System.currentTimeMillis());

        //Get back future terms starting from the given atpId(currently open for planning)
        List<AtpHelper.YearTerm> planTerms = AtpHelper.getFutureYearTerms(startAtpId, null);

        try {

            //Processing hand off logic for each term
            for (AtpHelper.YearTerm yearTerm : planTerms) {
                // Additional condition to skip past terms
                if (AtpHelper.hasYearTermCompleted(yearTerm)) continue;

                String atpId = yearTerm.toATP();

                boolean isTermPublished = publishedTerms.contains(yearTerm);

                MessyTermDataObject messyTerm = new MessyTermDataObject();
                messyTerm.setAtpId(atpId);
                IgnoreTermDataObject ignoreTerm = new IgnoreTermDataObject();
                ignoreTerm.setAtpId(atpId);

                List<PlanItemInfo> planItemInfos = planItemsMap.get(atpId);
                if (planItemInfos == null) continue;
                Map<String, List<ActivityOfferingItem>> plannedActivitiesMap = getPlannedActivities(planItemInfos);

                for (PlanItemInfo planItem : planItemInfos) {
                    if (planItem.getRefObjectType().equalsIgnoreCase(PlanConstants.COURSE_TYPE)) {
                        String courseId = getCourseHelper().getCourseVersionIdByTerm(planItem.getRefObjectId(), atpId);
                        String crossListedCourse = getPlanHelper().getCrossListedCourse(planItem.getAttributes());
                        boolean ignore = false;
                        if (courseId != null) {
                            CourseInfo courseInfo = null;
                            courseInfo = getCourseHelper().getCourseInfoByIdAndCd(courseId, crossListedCourse);
                            if (courseInfo != null) {
                                Set<Choice> choices = new HashSet<Choice>();
                                if (isTermPublished) {
                                    List<ActivityOfferingItem> activities = new ArrayList<ActivityOfferingItem>();
                                    String activitiesMapKey = String.format("%s:%s:%s", courseInfo.getId(), courseInfo.getSubjectArea().trim(), courseInfo.getCourseNumberSuffix().trim());
                                    if (plannedActivitiesMap.containsKey(activitiesMapKey)) {

                                        List<ActivityOfferingItem> withdrawnSections = new ArrayList<ActivityOfferingItem>();
                                        List<ActivityOfferingItem> nonWithdrawnSections = new ArrayList<ActivityOfferingItem>();
                                        for (ActivityOfferingItem activityOfferingItem : plannedActivitiesMap.get(activitiesMapKey)) {
                                            if (PlanConstants.WITHDRAWN_STATE.equalsIgnoreCase(activityOfferingItem.getStateKey())) {
                                                withdrawnSections.add(activityOfferingItem);
                                            } else {
                                                nonWithdrawnSections.add(activityOfferingItem);
                                            }
                                        }

                                        //If all sections are withdrawn then we add the course to ignored
                                        if (withdrawnSections.size() > 0 && nonWithdrawnSections.isEmpty()) {
                                            ignore = true;
                                        } else {
                                            activities = nonWithdrawnSections;
                                        }


                                    }

                                    // If plan item activity list is empty, populate the activities offered for that course
                                    if (activities.isEmpty() && !ignore) {
                                        long start = System.currentTimeMillis();
                                    /*TODO: Replace the getCourseOfferingsByCourseAndTerm() with new one which accepts a composite key or courseId + course Cd instead of just a courseId*/
                                        String id = getCourseHelper().getKeyForCourseOffering(courseInfo.getId(), courseInfo.getSubjectArea().trim(), courseInfo.getCourseNumberSuffix().trim());
                                        List<CourseOfferingInfo> courseOfferings = getCourseOfferingService().getCourseOfferingsByCourseAndTerm(id, atpId, CONTEXT_INFO);
                                        List<ActivityOfferingItem> honorsCrNcActivities = new ArrayList<ActivityOfferingItem>();
                                        if (courseOfferings != null && !courseOfferings.isEmpty()) {
                                            for (CourseOfferingInfo courseOfferingInfo : courseOfferings) {


                                                String courseOfferingID = courseOfferingInfo.getId();

                                                List<ActivityOfferingDisplayInfo> aodiList = null;
                                                try {
                                                    aodiList = getCourseOfferingService().getActivityOfferingDisplaysForCourseOffering(courseOfferingID, CourseSearchConstants.CONTEXT_INFO);
                                                } catch (Exception e) {
                                                    logger.info("Not able to load activity offering for courseOffering: " + courseOfferingID + " Term:" + atpId);
                                                    continue;
                                                }

                                                List<ActivityOfferingItem> coActivities = new ArrayList<ActivityOfferingItem>();
                                                for (ActivityOfferingDisplayInfo displayInfo : aodiList) {
                                                    coActivities.add(buildActivityOfferingItemSummary(displayInfo, courseOfferingInfo));
                                                }

                                                if (courseOfferingInfo.getGradingOptionId().equalsIgnoreCase(DegreeAuditConstants.CR_NO_CR_GRADING_OPTION_ID)) {
                                                    honorsCrNcActivities.addAll(coActivities);
                                                } else {
                                                    for (ActivityOfferingItem coActivity : coActivities) {
                                                        if (coActivity.isHonorsSection()) {
                                                            honorsCrNcActivities.add(coActivity);

                                                        } else {
                                                            activities.add(coActivity);
                                                        }


                                                    }
                                                }

                                            }
                                            //All activities are honors activities
                                            if (activities.isEmpty() && honorsCrNcActivities.size() > 0) {
                                                activities.addAll(honorsCrNcActivities);
                                            }
                                        } else {
                                            ignore = true;
                                        }


                                        logger.info("Planned Activities time  " + (System.currentTimeMillis() - start) + " for " + courseInfo.getCode());
                                    }
                                    List<ActivityOfferingItem> processedActivities = new ArrayList<ActivityOfferingItem>();
                                    Map<String, List<ActivityOfferingItem>> secondaryActivityMap = new HashMap<String, List<ActivityOfferingItem>>();

                                    for (ActivityOfferingItem activityOfferingItem : activities) {
                                        if (activityOfferingItem.isPrimary()) {
                                            processedActivities.add(activityOfferingItem);
                                        } else {
                                            if (secondaryActivityMap.containsKey(activityOfferingItem.getPrimaryActivityOfferingId())) {
                                                secondaryActivityMap.get(activityOfferingItem.getPrimaryActivityOfferingId()).add(activityOfferingItem);
                                            } else {
                                                List<ActivityOfferingItem> secondaryActivities = new ArrayList<ActivityOfferingItem>();
                                                secondaryActivities.add(activityOfferingItem);
                                                secondaryActivityMap.put(activityOfferingItem.getPrimaryActivityOfferingId(), secondaryActivities);
                                            }
                                        }
                                    }

                                    for (ActivityOfferingItem activity : processedActivities) {
                                        String honorsSecondaryActivity = null;
                                        String nonHonorsSecondaryActivity = null;
                                        String credits = activity.getCredits();
                                        String section = activity.getCode();
                                        boolean honors = activity.isHonorsSection();
                                        boolean writing = activity.isWritingSection();
                                        boolean crNcGradingOption = activity.getGradingOption() != null && activity.getGradingOption().equalsIgnoreCase(DegreeAuditConstants.CR_NO_CR_GRADING_OPTION);

                                        List<ActivityOfferingItem> secondaryActivities = secondaryActivityMap.get(activity.getPrimaryActivityOfferingId());
                                        //If the primary activity is honors we don't need to check the secondary for honors we pick the primary activity
                                        if (!honors && secondaryActivities != null && !secondaryActivities.isEmpty()) {

                                            for (ActivityOfferingItem activityOfferingItem : secondaryActivities) {
                                                if (activityOfferingItem.isHonorsSection() && honorsSecondaryActivity == null) {
                                                    honorsSecondaryActivity = activityOfferingItem.getCode();
                                                } else if (nonHonorsSecondaryActivity == null) {
                                                    nonHonorsSecondaryActivity = activityOfferingItem.getCode();
                                                }
                                                if (honorsSecondaryActivity != null && nonHonorsSecondaryActivity != null) {
                                                    break;
                                                }
                                            }


                                        }
                                        String[] list = creditToList(credits);

                                        for (String temp : list) {
                                            Choice choice = new Choice();
                                            choice.credit = temp;
                                            choice.section = StringUtils.hasText(section) ? section : "";
                                            choice.honors = honors;
                                            choice.writing = writing;
                                            choice.secondaryActivity = StringUtils.hasText(nonHonorsSecondaryActivity) ? nonHonorsSecondaryActivity : "";
                                            choice.crNcGradingOption = crNcGradingOption;
                                            if (nonHonorsSecondaryActivity != null || honorsSecondaryActivity == null) {
                                                choices.add(choice);
                                            }
                                            if (honorsSecondaryActivity != null) {
                                                Choice honorsChoice = (Choice) choice.clone();
                                                honorsChoice.secondaryActivity = honorsSecondaryActivity;
                                                honorsChoice.honors = true;
                                                choices.add(honorsChoice);
                                            }
                                        }


                                    }

                                }

                                if (!ignore) {
                                    // Otherwise just use course's default credit choices
                                    if (choices.isEmpty()) {
                                        String credits = CreditsFormatter.formatCredits(courseInfo);
                                        String section = "";
                                        String secondaryActivity = "";
                                        String[] list = creditToList(credits);
                                        for (String temp : list) {
                                            Choice choice = new Choice();
                                            choice.credit = temp;
                                            choice.section = section;
                                            choice.secondaryActivity = secondaryActivity;
                                            choices.add(choice);
                                        }
                                    }

                                    if (choices.size() == 1) {
                                        for (Choice choice : choices) {
                                            String credits = choice.credit;
                                            String section = choice.section;
                                            String secondaryActivity = choice.secondaryActivity;
                                            CourseItem item = new CourseItem();
                                            item.setAtpId(atpId);
                                            item.setCourseCode(courseInfo.getCode());
                                            item.setSubject(courseInfo.getSubjectArea().trim());
                                            item.setNumber(courseInfo.getCourseNumberSuffix().trim());
                                            item.setCourseId(courseInfo.getVersion().getVersionIndId());
                                            item.setCredit(credits);
                                            item.setSectionCode(StringUtils.hasText(section) ? section : "");
                                            item.setSecondaryActivityCode(StringUtils.hasText(secondaryActivity) ? secondaryActivity : "");
                                            courseItems.add(item);
                                        }
                                    } else {
                                        Set<String> credits = new HashSet<String>();
                                        for (Choice choice : choices) {
                                            String formatted = choice.toString();
                                            credits.add(formatted);
                                        }

                                        String versionIndependentId = courseInfo.getVersion().getVersionIndId();

                                        MessyItem item = new MessyItem();
                                        item.setCourseCode(courseInfo.getCode());
                                        item.setSubject(courseInfo.getSubjectArea().trim());
                                        item.setNumber(courseInfo.getCourseNumberSuffix().trim());
                                        item.setCourseTitle(courseInfo.getCourseTitle());
                                        item.setCourseId(courseInfo.getId());
                                        item.setVersionIndependentId(versionIndependentId);
                                        item.setCredits(credits);
                                        item.setAtpId(atpId);

                                        messyTerm.getMessyItemList().add(item);
                                    }
                                }
                            } else {
                                ignore = true;
                            }
                        } else {
                            ignore = true;
                        }
                        //ignore if no version available or course not scheduled
                        if (ignore) {
                            String course = getCourseHelper().getVerifiedCourseId(planItem.getRefObjectId());
                            if (course != null) {
                                CourseInfo courseInfo = getCourseHelper().getCourseInfoByIdAndCd(course, crossListedCourse);
                                //Adding course to ignore list if courseId not found
                                CourseItem item = new CourseItem();
                                item.setAtpId(atpId);
                                item.setCourseCode(courseInfo.getCode());
                                item.setSubject(courseInfo.getSubjectArea().trim());
                                item.setNumber(courseInfo.getCourseNumberSuffix().trim());
                                item.setTitle(courseInfo.getCourseTitle());
                                item.setCourseId(courseInfo.getVersion().getVersionIndId());
                                item.setCredit(CreditsFormatter.formatCredits(courseInfo));
                                item.setSectionCode("");
                                ignoreTerm.getCourseItemList().add(item);
                            }
                        }

                    }

                }
                logger.info("Retrieved planned activities in " + System.currentTimeMillis() + "for term " + atpId);
                if (!messyTerm.getMessyItemList().isEmpty()) {
                    planAuditForm.getMessyItems().add(messyTerm);
                }
                if (!ignoreTerm.getCourseItemList().isEmpty()) {
                    planAuditForm.getIgnoreList().add(ignoreTerm);
                }
            }
        } catch (Exception e) {
            logger.error("Could not review the Plan ", e);
        }
        return planAuditForm;
    }

    /**
     * Adds the degree audit programId to the associated campus
     *
     * @param report
     * @param form
     * @return
     */
    @Override
    public DegreeAuditForm copyCampusToForm(AuditReportInfo report, DegreeAuditForm form) {
        String programId = report.getProgramId();
        String prefix = String.valueOf(programId.charAt(0));
        programId = programId.replace(' ', '$');

        // Impl to set the default values for campusParam and programParam properties
        Campus campus = Campus.getId(prefix);
        if (campus != null) {
            form.setCampusParam(campus.getId());
            switch (campus) {
                case CAMPUS_306:
                    form.setProgramParamSeattle(programId);
                    break;
                case CAMPUS_310:
                    form.setProgramParamBothell(programId);
                    break;
                case CAMPUS_323:
                    form.setProgramParamTacoma(programId);
                    break;
                default:
                    break;
            }
        }
        return form;
    }

    /**
     * Gets the program id for the selected campus
     *
     * @param form
     * @return
     */
    @Override
    public String getFormProgramID(DegreeAuditForm form) {
        String campusSelected = form.getCampusParam();
        Campus campus = Campus.getCampusCode(campusSelected);
        String programId = DegreeAuditConstants.DEFAULT_KEY;
        if (campus != null) {
            switch (campus) {
                case CAMPUS_306:
                    programId = form.getProgramParamSeattle();
                    break;
                case CAMPUS_310:
                    programId = form.getProgramParamBothell();
                    break;
                case CAMPUS_323:
                    programId = form.getProgramParamTacoma();
                    break;
                default:
                    break;
            }
        }
        return programId;
    }

    /**
     * populates the Map with courseId:subject:number as key and list Of activities that are planned and associated to that course.
     *
     * @param planItemInfos
     * @return
     */
    private Map<String, List<ActivityOfferingItem>> getPlannedActivities(List<PlanItemInfo> planItemInfos) {

        Map<String, List<ActivityOfferingItem>> plannedActivitiesMap = new HashMap<String, List<ActivityOfferingItem>>();
        for (PlanItemInfo planItemInfo : planItemInfos) {
            if (planItemInfo.getRefObjectType().equalsIgnoreCase(PlanConstants.SECTION_TYPE)) {
                String activityOfferingId = planItemInfo.getRefObjectId();
                ActivityOfferingDisplayInfo activityDisplayInfo = null;
                try {
                    activityDisplayInfo = getCourseOfferingService().getActivityOfferingDisplay(activityOfferingId, PlanConstants.CONTEXT_INFO);
                } catch (Exception e) {
                    logger.error("Could not retrieve ActivityOffering data for" + activityOfferingId, e);
                }
                if (activityDisplayInfo != null) {
                    /*TODO: move this to Coursehelper to make it institution neutral*/
                    String courseOfferingId = null;
                    for (AttributeInfo attributeInfo : activityDisplayInfo.getAttributes()) {
                        if (CourseSearchConstants.PRIMARY_ACTIVITY_OFFERING_ID.equalsIgnoreCase(attributeInfo.getKey())) {
                            courseOfferingId = attributeInfo.getValue();
                            break;
                        }
                    }
                    CourseOfferingInfo courseOfferingInfo = null;
                    try {
                        courseOfferingInfo = getCourseOfferingService().getCourseOffering(courseOfferingId, CourseSearchConstants.CONTEXT_INFO);
                    } catch (Exception e) {
                        logger.error("Could not retrieve CourseOffering data for" + courseOfferingId, e);
                    }
                    ActivityOfferingItem activityOfferingItem = buildActivityOfferingItemSummary(activityDisplayInfo, courseOfferingInfo);
                    String key = String.format("%s:%s:%s", activityOfferingItem.getCourseId(), courseOfferingInfo.getSubjectArea().trim(), courseOfferingInfo.getCourseNumberSuffix().trim());
                    if (plannedActivitiesMap.containsKey(key)) {
                        plannedActivitiesMap.get(key).add(activityOfferingItem);

                    } else {
                        List<ActivityOfferingItem> activityOfferingItems = new ArrayList<ActivityOfferingItem>();
                        activityOfferingItems.add(activityOfferingItem);
                        plannedActivitiesMap.put(key, activityOfferingItems);
                    }

                }

            }
        }
        return plannedActivitiesMap;
    }


    /**
     * populates a planned planItems Map
     *
     * @return
     */
    private Map<String, List<PlanItemInfo>> populatePlanItemsMap(String studentId) {
        Map<String, List<PlanItemInfo>> planItemsMap = new HashMap<String, List<PlanItemInfo>>();
        try {
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(studentId, LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);

            for (LearningPlanInfo learningPlanInfo : learningPlanList) {
                String learningPlanID = learningPlanInfo.getId();
                List<PlanItemInfo> planItemInfoList = getAcademicPlanService().getPlanItemsInPlanByType(learningPlanID, LEARNING_PLAN_ITEM_TYPE_PLANNED, CONTEXT_INFO);
                for (PlanItemInfo planItem : planItemInfoList) {
                    if (!planItem.getPlanPeriods().isEmpty()) {
                        String atpId = planItem.getPlanPeriods().get(0);
                        if (planItemsMap.containsKey(atpId)) {
                            planItemsMap.get(atpId).add(planItem);
                        } else {
                            List<PlanItemInfo> planItemInfos = new ArrayList<PlanItemInfo>();
                            planItemInfos.add(planItem);
                            planItemsMap.put(atpId, planItemInfos);
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error loading Plan items ", e);
        }
        return planItemsMap;
    }

    /**
     * if variable credit 1,2 ---> 1,2 are returned
     * if variable credit 1-5 ---> 1,2,3,4,5 are returned
     * if standard credit 1 ---> 1 is returned
     *
     * @param credit
     * @return
     */
    private String[] creditToList(String credit) {
        credit = credit.replace(" ", "");
        String[] result = null;
        if (credit.contains("-")) {
            String[] temp = credit.split("-");
            int min = Integer.valueOf(temp[0]);
            int max = Integer.valueOf(temp[1]);
            int len = max - min + 1;
            result = new String[len];
            for (int x = 0; x < len; x++) {
                result[x] = Integer.toString(min + x);
            }
        } else if (credit.contains(",")) {
            result = credit.split(",");
        } else {
            result = new String[]{credit};
        }
        return result;
    }

    /**
     * Summary of activityOfferingItem is populated which are required for Hand Off screen
     *
     * @param displayInfo
     * @param courseOfferingInfo
     * @return
     */
    private ActivityOfferingItem buildActivityOfferingItemSummary(ActivityOfferingDisplayInfo displayInfo, CourseOfferingInfo courseOfferingInfo) {
        ActivityOfferingItem activity = new ActivityOfferingItem();
        /*Data from ActivityOfferingDisplayInfo*/
        activity.setCourseId(courseOfferingInfo.getCourseId());
        activity.setCode(displayInfo.getActivityOfferingCode());
        activity.setStateKey(displayInfo.getStateKey());
        activity.setActivityOfferingType(displayInfo.getTypeName());
        activity.setCredits(courseOfferingInfo.getCreditOptionName());
        for (AttributeInfo attrib : displayInfo.getAttributes()) {
            String key = attrib.getKey();
            String value = attrib.getValue();
            if (CourseSearchConstants.PRIMARY_ACTIVITY_OFFERING_CODE.equalsIgnoreCase(key)) {
                activity.setPrimaryActivityOfferingCode(value);
                activity.setPrimary(value.equalsIgnoreCase(activity.getCode()));
            }
            if (CourseSearchConstants.PRIMARY_ACTIVITY_OFFERING_ID.equalsIgnoreCase(key)) {
                activity.setPrimaryActivityOfferingId(value);
            }
            Boolean flag = Boolean.valueOf(value);
            if (CourseSearchConstants.WRITING.equalsIgnoreCase(key)) {
                activity.setWritingSection(flag);
            }
        }
        activity.setHonorsSection(displayInfo.getIsHonorsOffering());
        activity.setGradingOption(courseOfferingInfo.getGradingOptionName());
        return activity;
    }

    public Map<String, PlanAuditItem> getPlanItemSnapShots(String studentId) {
        Map<String, PlanAuditItem> auditsInLearningPlan = new HashMap<String, PlanAuditItem>();
        try {
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(studentId, LEARNING_PLAN_TYPE_PLAN_AUDIT, PlanConstants.CONTEXT_INFO);
            for (LearningPlanInfo learningPlanInfo : learningPlanList) {
                PlanAuditItem planAuditItem = new PlanAuditItem();
                String auditId = null;

                for (AttributeInfo attributeInfo : learningPlanInfo.getAttributes()) {
                    String key = attributeInfo.getKey();
                    String value = attributeInfo.getValue();
                    if ("forCourses".equalsIgnoreCase(key)) {
                        planAuditItem.setAuditedCoursesCount(value);
                    } else if ("forCredits".equalsIgnoreCase(key)) {
                        planAuditItem.setTotalAuditedCredit(value);
                    } else if ("forQuarter".equalsIgnoreCase(key)) {
                        planAuditItem.setAuditedQuarterUpTo(value);
                    } else if ("isAdviser".equalsIgnoreCase(key)) {
                        planAuditItem.setRunByAdviser(Boolean.valueOf(value));
                    } else if ("requestedBy".equalsIgnoreCase(key)) {
                        planAuditItem.setRequestedBy(value);
                    } else if ("auditId".equalsIgnoreCase(key)) {
                        auditId = value;
                    }
                }


                if (auditId != null) {
                    auditsInLearningPlan.put(auditId, planAuditItem);
                }
            }
        } catch (Exception e) {
            logger.warn("cannot populate plan audit snapshots", e);
        }

        return auditsInLearningPlan;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    protected synchronized CourseService getCourseService() {
        if (this.courseService == null) {
            this.courseService = (CourseService) GlobalResourceLoader
                    .getService(new QName(CourseServiceConstants.COURSE_NAMESPACE, "CourseService"));
        }
        return this.courseService;
    }

    public AcademicPlanService getAcademicPlanService() {
        if (academicPlanService == null) {
            academicPlanService = (AcademicPlanService)
                    GlobalResourceLoader.getService(new QName(PlanConstants.NAMESPACE, PlanConstants.SERVICE_NAME));
        }
        return academicPlanService;
    }

    public void setAcademicPlanService(AcademicPlanService academicPlanService) {
        this.academicPlanService = academicPlanService;
    }

    public void setCourseService(CourseService courseService) {
        this.courseService = courseService;
    }

    public void setCourseOfferingService(CourseOfferingService courseOfferingService) {
        this.courseOfferingService = courseOfferingService;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }

    public PlanHelper getPlanHelper() {
        if (planHelper == null) {
            planHelper = new PlanHelperImpl();
        }
        return planHelper;
    }

    public void setPlanHelper(PlanHelper planHelper) {
        this.planHelper = planHelper;
    }
}
