package org.kuali.student.ksap.schedulebuilder;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.kuali.student.enrollment.acal.dto.TermInfo;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ClassMeetingTimeInfo;
import org.kuali.student.myplan.schedulebuilder.dto.CourseOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ReservedTimeInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.ClassMeetingTime;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.infc.RichText;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static org.junit.Assert.assertTrue;

/**
 * kmuthu Don't forget to add comment
 *
 * @Author kmuthu
 * Date: 1/28/14
 */
//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:myplan-test-context.xml"})

public class ScheduleBuilderTest {

    private final Logger logger = Logger.getLogger(ScheduleBuilderTest.class);

    private TermInfo getTermInfo()
    {
        TermInfo termInfo;

        logger.setLevel(Level.DEBUG);
        termInfo =  new TermInfo();
        termInfo.setCode("20141");  // maybe just null?
        try {
            Date startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2014-01-06");
            termInfo.setStartDate(startDate);
            Date endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2014-03-21");
            termInfo.setEndDate(endDate);
         } catch (Exception e) {
            logger.fatal("Could not convert dates for TermInfo", e);
        }
        termInfo.setId("20141");
        termInfo.setName("Winter 2014");
        termInfo.setDescr(null);
        termInfo.setStateKey(null);
        termInfo.setMeta(null);
        termInfo.setTypeKey(null);

        AttributeInfo attr1, attr2, attr3;
        attr1 = new AttributeInfo("last_add_day", "2014-02-01");
        attr2 = new AttributeInfo("priority_one_registration_start", "2013-11-25");
        attr3 = new AttributeInfo("priority_one_registration_end", "2014-01-12");
        List<AttributeInfo> attrs = new LinkedList<AttributeInfo>();
        attrs.add(attr1);
        attrs.add(attr2);
        attrs.add(attr3);
        termInfo.setAttributes(attrs);

        logger.info("TermInfo created for term " + termInfo.getName());
        return termInfo;
    }

 /*   public List<CourseOption> getCourseOptions(List<String> courseIds, String termId) {
            Term term = getTermHelper().getTermByAtpId(termId);
            CourseHelper courseHelper = getCourseHelper();
            ShoppingCartStrategy cartStrategy = getShoppingCartStrategy();
            DateFormat tdf = new SimpleDateFormat("h:mm a");
            DateFormat udf = new SimpleDateFormat("MM/dd/yyyy");
            DateFormat ddf = new SimpleDateFormat("M/d");
            Calendar sdcal = Calendar.getInstance();
            Calendar edcal = Calendar.getInstance();
            Calendar tcal = Calendar.getInstance();
            List<CourseOption> rv = new LinkedList<CourseOption>();
            courseHelper.frontLoad(courseIds, termId);
            int courseIndex = -1;
            for (String courseId : courseIds) {
                courseIndex++;
                Course c = courseHelper.getCourseInfo(courseId);
                if (c == null)
                    continue;
                CourseOptionInfo courseOption = new CourseOptionInfo();
                courseOption.setUniqueId(UUID.randomUUID().toString());
                courseOption.setCourseId(c.getId());

                StringBuilder code = new StringBuilder();
                String campusCode = null;
                for (Attribute ca : c.getAttributes())
                    if ("campusCode".equals(ca.getKey()))
                        code.append(campusCode = ca.getValue()).append(" ");
                if (!cartStrategy.isCartAvailable(termId, campusCode))
                    continue;

                code.append(c.getCode());
                courseOption.setCourseCode(code.toString());

                courseOption.setCourseTitle(c.getCourseTitle());
                List<ActivityOption> primaryActivities = new LinkedList<ActivityOption>();
                Map<String, Map<String, List<ActivityOptionInfo>>> secondaryActivities = new LinkedHashMap<String, Map<String, List<ActivityOptionInfo>>>();

                StringBuilder msg = null;
                if (LOG.isDebugEnabled())
                    msg = new StringBuilder();
                for (ActivityOfferingDisplayInfo aodi : courseHelper.getActivityOfferingDisplaysByCourseAndTerm(courseId,
                        termId)) {
                    ActivityOptionInfo activityOption = getActivityOption(term, aodi, courseIndex, courseId, campusCode, msg, tdf, udf, ddf, sdcal, edcal, tcal);

                    boolean enrollmentGroup = false;
                    String primaryOfferingId = null;
                    for (AttributeInfo attrib : aodi.getAttributes()) {
                        String key = attrib.getKey();
                        String value = attrib.getValue();

                        if ("PrimaryActivityOfferingId".equalsIgnoreCase(key))
                            primaryOfferingId = value;

                        if ("BlockEnrollment".equalsIgnoreCase(key))
                            enrollmentGroup = "true".equals(value);
                    }

                    if (activityOption.isPrimary()) {
                        activityOption
                                .setParentUniqueId(courseOption.getUniqueId());
                        if (msg != null)
                            msg.append("\nPrimary ")
                                    .append(activityOption.getUniqueId())
                                    .append(": ")
                                    .append(activityOption.getCourseOfferingCode())
                                    .append(" ")
                                    .append(activityOption.getRegistrationCode());
                        primaryActivities.add(activityOption);
                    } else {
                        Map<String, List<ActivityOptionInfo>> secondaryGroup = secondaryActivities
                                .get(primaryOfferingId);
                        if (msg != null)
                            msg.append("\nSecondary ")
                                    .append(activityOption.getUniqueId())
                                    .append(": ")
                                    .append(activityOption.getCourseOfferingCode())
                                    .append(" ")
                                    .append(activityOption.getRegistrationCode())
                                    .append(" -> ").append(primaryOfferingId);
                        if (secondaryGroup == null)
                            secondaryActivities
                                    .put(primaryOfferingId,
                                            secondaryGroup = new LinkedHashMap<String, List<ActivityOptionInfo>>());
                        String groupKey = enrollmentGroup ? "kuali.ap.enrollmentGroup"
                                : activityOption.getActivityTypeDescription();
                        List<ActivityOptionInfo> aol = secondaryGroup.get(groupKey);
                        if (aol == null)
                            secondaryGroup
                                    .put(groupKey,
                                            aol = new LinkedList<ActivityOptionInfo>());
                        aol.add(activityOption);
                    }
                }
                if (msg != null)
                    LOG.debug(msg.toString());

                for (ActivityOption primary : primaryActivities) {
                    String parentUniqueId = primary.getUniqueId();
                    Map<String, List<ActivityOptionInfo>> secondaryGroup = secondaryActivities
                            .get(primary.getActivityOfferingId());
                    if (secondaryGroup != null) {
                        List<SecondaryActivityOptions> sao = new ArrayList<SecondaryActivityOptions>();
                        int i = 0;
                        for (Map.Entry<String, List<ActivityOptionInfo>> e : secondaryGroup
                                .entrySet()) {
                            SecondaryActivityOptionsInfo secondaryOptions = new SecondaryActivityOptionsInfo();
                            secondaryOptions.setUniqueId(parentUniqueId);
                            secondaryOptions.setIndex(i);
                            secondaryOptions.setActivityTypeDescription(e.getKey());
                            secondaryOptions
                                    .setEnrollmentGroup("kuali.ap.enrollmentGroup"
                                            .equals(e.getKey()));
                            List<ActivityOptionInfo> secondaryOptionList = e
                                    .getValue();
                            for (ActivityOptionInfo secondaryOption : secondaryOptionList) {
                                secondaryOption.setParentUniqueId(parentUniqueId);
                                secondaryOption.setParentIndex(i);
                            }
                            Collections.sort(secondaryOptionList);
                            secondaryOptions
                                    .setActivityOptions(secondaryOptionList);
                            sao.add(secondaryOptions);
                            i++;
                        }
                        ((ActivityOptionInfo) primary).setSecondaryOptions(sao);
                    }
                }
                Collections.sort(primaryActivities);
                courseOption.setActivityOptions(primaryActivities);

                if (courseOption.getActivityCount(false) == 0) {
                    courseOption.setSelected(false);
                }

                rv.add(courseOption);
            }
            Collections.sort(rv);
            return rv;
        }*/

    // create a CourseOptionInfo. Maybe later let user set the fields in parent ScheduleBuildOption:
    //       booleans for selected, lockedIn, discarded; int shuffle
    // @param curric should be padded with spaces to length of 7
    // @param days - some or all of MTWRFA R for thursday, A for saturday
    private CourseOption buildCourseOption(String curric, String courseNumber, String sectionLetter,
                                            String courseTitle, String credits, int index, String activity,
                                            String days, String times) {
        // hard-coded values
        String campus = "Seattle ";
        String termYr = "2014";
        String termNbr = "1";
        String termName = "Winter";
        String termStartDate = "1/6";
        // schedule builder seems to use termStartDate (not actual first day of class) + class time,
        // don't know if that is important or if we'll fix to use actual class day & date
        // likewise for termEndDateDesc
        String termStartDateDesc = "Mon Jan 06 9:30:00 PST 2014";
        String termEndDate = "3/21";
        String termUntilDateDesc = "Fri Mar 21 10:20:00 PDT 2014";

        Date startDate = null;
        Date endDate   = null;
        try {
            startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2014-01-06");
            endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2014-03-21");
        } catch (Exception e) {
            logger.fatal("Could not convert dates for TermInfo", e);
        }

        CourseOptionInfo courseOption = new CourseOptionInfo();
        courseOption.setCourseId(UUID.randomUUID().toString());
        courseOption.setCourseCode(campus + curric + courseNumber);
        courseOption.setCourseTitle(courseTitle);
        courseOption.setCredits(BigDecimal.valueOf(Long.valueOf(credits)));  // always null in debugger?
        courseOption.setDiscarded(false);
        courseOption.setLockedIn(false);
        courseOption.setSelected(false);
        courseOption.setShuffle(0);
        courseOption.setUniqueId(UUID.randomUUID().toString());

        List<ActivityOption> primaryActivities = new LinkedList<ActivityOption>();

        ActivityOptionInfo activityOption =  new ActivityOptionInfo();
        activityOption.setParentUniqueId(UUID.randomUUID().toString());
        activityOption.setCourseIndex(index); // not sure this is right
        activityOption.setParentIndex(0);
        activityOption.setCourseId(UUID.randomUUID().toString());
        activityOption.setActivityOfferingId(termYr + ":" + termNbr + ":" + curric.trim() + ":"
                + courseNumber + ":" + sectionLetter);
        activityOption.setActivityTypeDescription(activity);
        activityOption.setCourseOfferingCode(campus + " " + curric.trim() + " " + sectionLetter);
        activityOption.setRegistrationCode(sectionLetter);
        activityOption.setAcademicSessionDescr(termName + " " + termYr + " " + termStartDate + " - " + termEndDate);
        activityOption.setActivityName(null);
        activityOption.setLockedIn(false);
        activityOption.setEnrollmentGroup(false);
        activityOption.setClosed(false);
        activityOption.setOpenSeats(0);
        activityOption.setTotalSeats(0);
        activityOption.setRequiresPermission(false);
        activityOption.setPrimary(true);
        activityOption.setMinCredits(BigDecimal.valueOf(Long.valueOf(0)));
        activityOption.setMaxCredits(BigDecimal.valueOf(Long.valueOf(0)));
        activityOption.setNotes(null);
        primaryActivities.add(activityOption);

        // secondary options
        List<SecondaryActivityOptions> secondaryActivities = new LinkedList<SecondaryActivityOptions>();
        //Map<String, Map<String, List<ActivityOptionInfo>>> secondaryActivities = new LinkedHashMap<String, Map<String, List<ActivityOptionInfo>>>();
        activityOption.setSecondaryOptions(secondaryActivities);

        ClassMeetingTimeInfo classMeetingTime =  new ClassMeetingTimeInfo();
        classMeetingTime.setId(null);
        classMeetingTime.setUniqueId(null);
        classMeetingTime.setInstructorName("Joe The Instructor");
        classMeetingTime.setLocation("99");
        // building?
        // campus?
        classMeetingTime.setArranged(true);
        classMeetingTime.setTba(false);
        classMeetingTime.setDescription(termStartDate + " - " + termEndDate);
        classMeetingTime.setAllDay(false);
        classMeetingTime.setDaysAndTimes(days + " " + times);
        classMeetingTime.setStartDate(startDate);
        //classMeetingTime.setEndTime ()  no setter, prob a bad field
        classMeetingTime.setUntilDate(endDate);

        classMeetingTime.setSunday(false);
        classMeetingTime.setMonday(false);
        classMeetingTime.setTuesday(false);
        classMeetingTime.setWednesday(false);
        classMeetingTime.setThursday(false);
        classMeetingTime.setFriday(false);
        classMeetingTime.setSaturday(false);
        if (days.indexOf('M') != -1)
            classMeetingTime.setMonday(true);
        if (days.indexOf('T') != -1)
            classMeetingTime.setTuesday(true);
        if (days.indexOf('W') != -1)
            classMeetingTime.setWednesday(true);
        if (days.indexOf('R') != -1)
            classMeetingTime.setThursday(true);
        if (days.indexOf('F') != -1)
            classMeetingTime.setFriday(true);
        if (days.indexOf('A') != -1)
            classMeetingTime.setSaturday(true);

        List<ClassMeetingTime> classMeetingTimes = new LinkedList<ClassMeetingTime>();
        classMeetingTimes.add(classMeetingTime);
        activityOption.setClassMeetingTimes(classMeetingTimes);

        Collections.sort(primaryActivities);  // not sure if this is needed
        courseOption.setActivityOptions(primaryActivities);

        return courseOption;
    }

    @Test
    public void testScheduleOption1() {
        assertTrue(1 == 1);
        logger.info("Some msg");
     }

    @Test
    public void testScheduleOption2() {
        logger.info("Starting ScheduleBuilderUnitTest2");
        Date startDate = null;
        Date endDate   = null;
        try {
            startDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2014-01-06");
            endDate = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse("2014-03-21");
        } catch (Exception e) {
            logger.fatal("Could not convert dates for TermInfo", e);
        }

        Term term = getTermInfo();

        List<CourseOption> courseOptions = new LinkedList<CourseOption>();
        CourseOption courseOption1 = buildCourseOption(
        //curric,    courseNumber,sectionLetter,courseTitle,credits,index,activity,days,times
          "SOC    ", "101",       "A",          "Intro Soc","5",    1,   "lecture","MWF","9:30 AM - 10:20 AM");
        courseOptions.add(courseOption1);
        Collections.sort(courseOptions);

        ReservedTimeInfo rt1 = new ReservedTimeInfo();
        // from ReservedTimeInfo
            // String id;
            // String uniqueId;
	        //  boolean selected = true;
        // from base class  ScheduleBuildEventInfo implements ScheduleBuildEvent {
            // long serialVersionUID = 804871449240773901L;
            // String description;
            // boolean allDay;
            // String daysAndTimes;
            // Date startDate;
            // Date endTime;  // doesn't seem to be used anywhere, prob a mistake since untilDate is used for endTime
            //Date untilDate;
            // boolean sunday - saturday;

	    rt1.setId(UUID.randomUUID().toString());
        rt1.setUniqueId(UUID.randomUUID().toString());
        rt1.setSelected(true);
        rt1.setDescription("reserved time for sleeping");
        rt1.setAllDay(false);
        // days of the week in this order: "MTuWThFSaSu
        rt1.setDaysAndTimes("F 8:00 AM to 9:00 AM");
        rt1.setStartDate(startDate);
        rt1.setUntilDate(endDate);
        rt1.setFriday(true);
        List<ReservedTime> reservedTimes = new ArrayList<ReservedTime>();
        reservedTimes.add(rt1);

        ScheduleBuilder scheduleBuilder = new ScheduleBuilder(term, courseOptions, reservedTimes, null, null);
        int possibleScheduleSize = 1; // don't know what this means
        logger.info("ScheduleBuilderUnitTest2 results: " );
        List<PossibleScheduleOption> possibleScheduleOptions = scheduleBuilder.getNext(
                    possibleScheduleSize,
                    Collections.<PossibleScheduleOption>emptySet());
        PossibleScheduleOption pso =  possibleScheduleOptions.get(0);
        logger.info("\tcount of possible schedule options: " + possibleScheduleOptions.size() );
        if (possibleScheduleOptions.size() != 0) {
            logger.info("\tTermId: " + pso.getTermId());
            logger.info("\tDescription: " + pso.getDescription());
            logger.info("\tShuffle: " + pso.getShuffle());
            logger.info("\tID: " + pso.getId());
            List<ActivityOption> activityOptions = pso.getActivityOptions();
            logger.info("\t:ActivityOptions ");
            logger.info("\t\tcount of possible activity options: " + activityOptions.size() );
            if (activityOptions.size() != 0) {
                ActivityOption activityOption = activityOptions.get(0);
                logger.info("\t\t:ActivityOption 0 ");
                logger.info("\t\t\t:ActivityOfferingId: " + activityOption.getActivityOfferingId());
            }
        }


    }
}
