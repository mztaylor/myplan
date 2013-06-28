package edu.uw.kuali.student.myplan.util;

import org.apache.log4j.Logger;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.enrollment.courseoffering.service.CourseOfferingService;
import org.kuali.student.lum.course.dto.CourseInfo;
import org.kuali.student.lum.course.service.CourseService;
import org.kuali.student.lum.course.service.CourseServiceConstants;
import org.kuali.student.myplan.academicplan.dto.LearningPlanInfo;
import org.kuali.student.myplan.academicplan.dto.PlanItemInfo;
import org.kuali.student.myplan.academicplan.service.AcademicPlanService;
import org.kuali.student.myplan.audit.dataobject.CourseItem;
import org.kuali.student.myplan.audit.dataobject.IgnoreTermDataObject;
import org.kuali.student.myplan.audit.dataobject.MessyItem;
import org.kuali.student.myplan.audit.dataobject.MessyTermDataObject;
import org.kuali.student.myplan.audit.form.PlanAuditForm;
import org.kuali.student.myplan.audit.service.DegreeAuditConstants;
import org.kuali.student.myplan.audit.util.DegreeAuditHelper;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.course.util.CourseSearchConstants;
import org.kuali.student.myplan.course.util.CreditsFormatter;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.springframework.util.StringUtils;

import javax.xml.namespace.QName;
import java.util.*;

import static org.kuali.student.myplan.academicplan.service.AcademicPlanServiceConstants.LEARNING_PLAN_TYPE_PLAN;
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.HONORS_CREDIT;
import static org.kuali.student.myplan.audit.service.DegreeAuditConstants.WRITING_CREDIT;
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

    private static CourseHelper courseHelper;

    public static class Choice implements Cloneable {
        String credit = "";
        String section = null;
        String secondaryActivity = null;
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
            if (!(secondaryActivity == ((Choice) that).secondaryActivity)) return false;
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
    }

    /**
     * Processes the planned items and activities and provides clean and messy items to be chosen by student
     *
     * @param planAuditForm
     * @return
     */
    @Override
    public PlanAuditForm processHandOff(PlanAuditForm planAuditForm) {
        List<CourseItem> courseItems = planAuditForm.getCleanList();

        List<AtpHelper.YearTerm> publishedTerms = AtpHelper.getPublishedYearTermList();

        String startAtpId = null;
        for (AtpHelper.YearTerm yt : AtpHelper.getPublishedYearTermList()) {
            String atpId = yt.toATP();
            if (AtpHelper.isAtpSetToPlanning(atpId)) {
                startAtpId = atpId;
                break;
            }
        }

        Map<String, List<PlanItemInfo>> planItemsMap = populatePlanItemsMap();
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
                        boolean ignore = false;
                        if (courseId != null) {
                            CourseInfo courseInfo = null;
                            courseInfo = getCourseService().getCourse(courseId);
                            Set<Choice> choices = new HashSet<Choice>();
                            if (isTermPublished) {
                                List<ActivityOfferingItem> activities = new ArrayList<ActivityOfferingItem>();

                                if (plannedActivitiesMap.containsKey(courseInfo.getId())) {

                                    List<ActivityOfferingItem> withdrawnSections = new ArrayList<ActivityOfferingItem>();
                                    List<ActivityOfferingItem> nonWithdrawnSections = new ArrayList<ActivityOfferingItem>();
                                    for (ActivityOfferingItem activityOfferingItem : plannedActivitiesMap.get(courseInfo.getId())) {
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
                                    List<CourseOfferingInfo> courseOfferings = getCourseOfferingService().getCourseOfferingsByCourseAndTerm(courseId, atpId, CONTEXT_INFO);
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
                                            if (activityOfferingItem.isHonorsSection()) {
                                                honorsSecondaryActivity = activityOfferingItem.getCode();
                                            } else {
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
                                        choice.section = section;
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
                                        item.setCourseId(courseInfo.getVersionInfo().getVersionIndId());
                                        item.setCredit(credits);
                                        item.setSectionCode(section);
                                        item.setSecondaryActivityCode(secondaryActivity);
                                        courseItems.add(item);
                                    }
                                } else {
                                    Set<String> credits = new HashSet<String>();
                                    for (Choice choice : choices) {
                                        String formatted = choice.toString();
                                        credits.add(formatted);
                                    }

                                    String versionIndependentId = courseInfo.getVersionInfo().getVersionIndId();

                                    MessyItem item = new MessyItem();
                                    item.setCourseCode(courseInfo.getCode());
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
                        //ignore if no version available or course not scheduled
                        if (ignore) {
                            String course = getCourseHelper().getCourseVersionIdByTerm(planItem.getRefObjectId(), atpId);
                            if (course != null) {
                                CourseInfo courseInfo = getCourseService().getCourse(course);
                                //Adding course to ignore list if courseId not found
                                CourseItem item = new CourseItem();
                                item.setAtpId(atpId);
                                item.setCourseCode(courseInfo.getCode());
                                item.setTitle(courseInfo.getCourseTitle());
                                item.setCourseId(courseInfo.getVersionInfo().getVersionIndId());
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
     * populates the Map with courseId as key and list Of activities that are planned and associated to that course.
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
                        if ("PrimaryActivityOfferingId".equalsIgnoreCase(attributeInfo.getKey())) {
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
                    if (plannedActivitiesMap.containsKey(activityOfferingItem.getCourseId())) {
                        plannedActivitiesMap.get(activityOfferingItem.getCourseId()).add(activityOfferingItem);

                    } else {
                        List<ActivityOfferingItem> activityOfferingItems = new ArrayList<ActivityOfferingItem>();
                        activityOfferingItems.add(activityOfferingItem);
                        plannedActivitiesMap.put(activityOfferingItem.getCourseId(), activityOfferingItems);
                    }

                }

            }
        }
        return plannedActivitiesMap;
    }


    /**
     * @return
     */
    private Map<String, List<PlanItemInfo>> populatePlanItemsMap() {
        Map<String, List<PlanItemInfo>> planItemsMap = new HashMap<String, List<PlanItemInfo>>();
        try {
            List<LearningPlanInfo> learningPlanList = getAcademicPlanService().getLearningPlansForStudentByType(UserSessionHelper.getStudentRegId(), LEARNING_PLAN_TYPE_PLAN, CONTEXT_INFO);

            for (LearningPlanInfo learningPlanInfo : learningPlanList) {
                String learningPlanID = learningPlanInfo.getId();
                List<PlanItemInfo> planItemInfoList = getAcademicPlanService().getPlanItemsInPlan(learningPlanID, CONTEXT_INFO);
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
            if ("PrimaryActivityOfferingCode".equalsIgnoreCase(key)) {
                activity.setPrimaryActivityOfferingCode(value);
                activity.setPrimary(value.equalsIgnoreCase(activity.getCode()));
            }
            if ("PrimaryActivityOfferingId".equalsIgnoreCase(key)) {
                activity.setPrimaryActivityOfferingId(value);
            }
            Boolean flag = Boolean.valueOf(value);
            if ("Writing".equalsIgnoreCase(key)) {
                activity.setWritingSection(flag);
            }
        }
        activity.setHonorsSection(displayInfo.getIsHonorsOffering());
        activity.setGradingOption(courseOfferingInfo.getGradingOptionName());
        return activity;
    }

    protected CourseOfferingService getCourseOfferingService() {
        if (this.courseOfferingService == null) {
            //   TODO: Use constants for namespace.
            this.courseOfferingService = (CourseOfferingService) GlobalResourceLoader.getService(new QName("http://student.kuali.org/wsdl/courseOffering", "coService"));
        }
        return this.courseOfferingService;
    }

    public static CourseHelper getCourseHelper() {
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

}
