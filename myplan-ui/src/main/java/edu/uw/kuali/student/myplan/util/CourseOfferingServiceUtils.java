package edu.uw.kuali.student.myplan.util;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.student.enrollment.courseoffering.dto.ActivityOfferingDisplayInfo;
import org.kuali.student.enrollment.courseoffering.dto.CourseOfferingInfo;
import org.kuali.student.myplan.course.util.CourseHelper;
import org.kuali.student.myplan.plan.util.AtpHelper;
import org.kuali.student.myplan.utils.TimeStringMillisConverter;
import org.kuali.student.r2.common.dto.AttributeInfo;
import org.kuali.student.r2.common.dto.TimeOfDayInfo;
import org.kuali.student.r2.core.room.dto.BuildingInfo;
import org.kuali.student.r2.core.room.dto.RoomInfo;
import org.kuali.student.r2.core.scheduling.dto.ScheduleComponentDisplayInfo;
import org.kuali.student.r2.core.scheduling.dto.ScheduleDisplayInfo;
import org.kuali.student.r2.core.scheduling.dto.TimeSlotInfo;
import org.kuali.student.r2.core.scheduling.infc.ScheduleComponentDisplay;
import org.kuali.student.r2.core.scheduling.infc.TimeSlot;
import org.kuali.student.r2.lum.course.dto.CourseInfo;

import java.io.StringReader;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/20/13
 * Time: 3:32 PM
 * To change this template use File | Settings | File Templates.
 */
public class CourseOfferingServiceUtils {
    private final static Logger logger = Logger.getLogger(CourseOfferingServiceUtils.class);
    private static CourseHelper courseHelper;
    private static List<String> DAY_LIST = Arrays.asList("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
    private static Pattern regexInstituteCodePrefix = Pattern.compile("([0-9]+)(.)*");


    public static CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = new CourseHelperImpl();
        }
        return courseHelper;
    }

    public static void setCourseHelper(CourseHelper courseHelper) {
        CourseOfferingServiceUtils.courseHelper = courseHelper;
    }

    /**
     * Builds the ActivityOfferingDisplayInfo for given document
     *
     * @param document
     * @return
     */
    public static ActivityOfferingDisplayInfo buildActivityOfferingDisplayInfo(Document document) {
        DefaultXPath sectionPath = newXPath("/s:Section");
        DefaultXPath coursePath = newXPath("/s:Section/s:Course");
        Element sectionNode = (Element) sectionPath.selectSingleNode(document);
        Element courseNode = (Element) coursePath.selectSingleNode(document);

        /*Calculating institute code and skipping if code is other than seattle and UWPCE*/
        String instituteCode = null;

        {
            DefaultXPath linkPath = newXPath("s:Curriculum/s:TimeScheduleLinkAbbreviation");
            Element link = (Element) linkPath.selectSingleNode(sectionNode);
            instituteCode = link.getTextTrim();

            // displaying main campus, PCE, and ROTC
            // refer to https://jira.cac.washington.edu/browse/MYPLAN-1583 for list of supported institute codes
            int instituteNumber = getInstituteNumber(instituteCode);
            switch (instituteNumber) {
                case 0: // Main campus
                case 95: // PCE
                case 88: // ROTC
                {
                    instituteCode = Integer.toString(instituteNumber);
                    break;
                }

                case -1: //  POA #1: Exclude sections with a null/blankTimeScheduleLinkAbbreviation.
                default: // Also omit all others not in above list
                {
                    return null;
                }
            }

        }

        String instituteName = sectionNode.elementTextTrim("InstituteName");

        ActivityOfferingDisplayInfo info = new ActivityOfferingDisplayInfo();


        String sectionComments = null;
        {
            DefaultXPath sectionCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:SectionComments/s:Lines");
            Element sectionCommentsNode = (Element) sectionCommentsPath.selectSingleNode(document);
            StringBuilder sb = new StringBuilder();
            List comments = sectionCommentsNode.content();
            for (Object ob : comments) {
                Element element = (Element) ob;
                sb.append(element.elementText("Text") + " ");
            }
            sectionComments = sb.toString();
        }


        String typeName = sectionNode.elementText("SectionType");
        info.setTypeName(typeName);

        String campus = sectionNode.elementText("CourseCampus");


        {
            ScheduleDisplayInfo scheduleDisplay = new ScheduleDisplayInfo();
            scheduleDisplay.setScheduleComponentDisplays(new ArrayList<ScheduleComponentDisplayInfo>());
            info.setScheduleDisplay(scheduleDisplay);

            DefaultXPath meetingPath = newXPath("/s:Section/s:Meetings/s:Meeting");

            List meetings = meetingPath.selectNodes(document);
            for (Object obj : meetings) {
                Element meetingNode = (Element) obj;

                ScheduleComponentDisplayInfo scdi = new ScheduleComponentDisplayInfo();
                scdi.setTimeSlots(new ArrayList<TimeSlotInfo>());
                List<? extends ScheduleComponentDisplay> scheduleComponentDisplays = scheduleDisplay.getScheduleComponentDisplays();
                List<ScheduleComponentDisplayInfo> scheduleComponentDisplayInfos = new ArrayList<ScheduleComponentDisplayInfo>();
                for (ScheduleComponentDisplay scheduleComponentDisplay : scheduleComponentDisplays) {
                    scheduleComponentDisplayInfos.add(new ScheduleComponentDisplayInfo(scheduleComponentDisplay));
                }
                scheduleComponentDisplayInfos.add(scdi);
                scheduleDisplay.setScheduleComponentDisplays(scheduleComponentDisplayInfos);

                TimeSlotInfo timeSlot = new TimeSlotInfo();
                List<? extends TimeSlot> timeSlots = scdi.getTimeSlots();
                List<TimeSlotInfo> timeSlotInfos = new ArrayList<TimeSlotInfo>();
                for (TimeSlot timeSlot1 : timeSlots) {
                    timeSlotInfos.add(new TimeSlotInfo(timeSlot1));
                }
                timeSlotInfos.add(timeSlot);
                scdi.setTimeSlots(timeSlotInfos);
                timeSlot.setWeekdays(new ArrayList<Integer>());

                String tba = meetingNode.elementText("DaysOfWeekToBeArranged");
                boolean tbaFlag = Boolean.parseBoolean(tba);
                if (!tbaFlag) {

                    DefaultXPath dayNamePath = newXPath("s:DaysOfWeek/s:Days/s:Day/s:Name");

                    List dayNameNodes = dayNamePath.selectNodes(meetingNode);
                    for (Object node : dayNameNodes) {
                        Element dayNameNode = (Element) node;
                        String day = dayNameNode.getTextTrim();
                        int weekday = DAY_LIST.indexOf(day);
                        if (weekday != -1) {
                            timeSlot.getWeekdays().add(weekday);
                        }
                    }

                    {
                        String time = meetingNode.elementText("StartTime");
                        long millis = TimeStringMillisConverter.militaryTimeToMillis(time);
                        TimeOfDayInfo timeInfo = new TimeOfDayInfo();
                        timeInfo.setMilliSeconds(millis);
                        timeSlot.setStartTime(timeInfo);
                    }

                    {
                        String time = meetingNode.elementText("EndTime");
                        long millis = TimeStringMillisConverter.militaryTimeToMillis(time);
                        TimeOfDayInfo timeInfo = new TimeOfDayInfo();
                        timeInfo.setMilliSeconds(millis);
                        timeSlot.setEndTime(timeInfo);
                    }
                }

                {
                    String buildingTBA = meetingNode.elementText("BuildingToBeArranged");
                    boolean buildingTBAFlag = Boolean.parseBoolean(buildingTBA);
                    if (!buildingTBAFlag) {
                        BuildingInfo buildingInfo = new BuildingInfo();
                        buildingInfo.setCampusKey(campus);
                        String building = meetingNode.elementText("Building");
                        buildingInfo.setBuildingCode(building);
                        scdi.setBuilding(buildingInfo);
                    }

                    String roomTBA = meetingNode.elementText("RoomToBeArranged");
                    boolean roomTBAFlag = Boolean.parseBoolean(roomTBA);
                    if (!roomTBAFlag) {

                        String roomNumber = meetingNode.elementText("RoomNumber");
                        RoomInfo roomInfo = new RoomInfo();
                        roomInfo.setRoomCode(roomNumber);
                        scdi.setRoom(roomInfo);
                    }
                }

                {
                    String name = "--";
                    String regid = "--";

                    DefaultXPath instructorPath = newXPath("/s:Section/s:Meetings/s:Meeting/s:Instructors/s:Instructor/s:Person");
                    List instructors = instructorPath.selectNodes(document);

                    for (Object node : instructors) {
                        Element instructor = (Element) node;
                        name = instructor.elementText("Name");
                        name = name.replaceFirst(",", ", ");
                        regid = instructor.elementText("RegID");
                        // Only show the first instructor
                        break;
                    }
                    info.setInstructorName(name);
                    info.setInstructorId(regid);
                }
            }
        }

        String feeAmount = sectionNode.elementTextTrim("FeeAmount");
        if (feeAmount.contains(".")) {
            feeAmount = feeAmount.substring(0, feeAmount.indexOf("."));
        }

        String summerTerm = sectionNode.elementText("SummerTerm");
        String sln = sectionNode.elementText("SLN");
        String sectionId = sectionNode.elementText("SectionID");

        String yr = courseNode.elementText("Year");
        String quarter = courseNode.elementText("Quarter");
        String curriculum = courseNode.elementText("CurriculumAbbreviation");
        String number = courseNode.elementText("CourseNumber");
        String title = courseNode.elementText("CourseTitle");
        String primaryActivityCd = sectionNode.element("PrimarySection").elementText("SectionID");

        AtpHelper.YearTerm yt = AtpHelper.quarterYearToYearTerm(quarter, yr);
        info.setCourseOfferingTitle(title);
        info.setId(buildId(yr, yt.getTermAsString(), curriculum, number, sectionId));

        info.setCourseOfferingCode(curriculum + " " + sectionId);
        info.setActivityOfferingCode(sectionId);
        info.setIsHonorsOffering(Boolean.valueOf(sectionNode.elementText("HonorsCourse")));
        info.setStateKey(sectionNode.elementText("DeleteFlag"));

        //Course Flags
        List<AttributeInfo> attributes = info.getAttributes();
        attributes.add(new AttributeInfo("InstituteCode", instituteCode));
        attributes.add(new AttributeInfo("InstituteName", instituteName));
        attributes.add(new AttributeInfo("Campus", campus));
        attributes.add(new AttributeInfo("Writing", String.valueOf(Boolean.valueOf(sectionNode.element("GeneralEducationRequirements").elementText("Writing")))));
        attributes.add(new AttributeInfo("ServiceLearning", String.valueOf(Boolean.valueOf(sectionNode.elementText("ServiceLearning")))));
        attributes.add(new AttributeInfo("ResearchCredit", String.valueOf(Boolean.valueOf(sectionNode.elementText("ResearchCredit")))));
        attributes.add(new AttributeInfo("DistanceLearning", String.valueOf(Boolean.valueOf(sectionNode.elementText("DistanceLearning")))));
        attributes.add(new AttributeInfo("JointSections", String.valueOf(sectionNode.element("JointSections").content().size() > 0)));
        attributes.add(new AttributeInfo("FinancialAidEligible", String.valueOf(Boolean.valueOf(sectionNode.elementText("FinancialAidEligible").length() > 0))));
        attributes.add(new AttributeInfo("AddCodeRequired", String.valueOf(Boolean.valueOf(sectionNode.elementText("AddCodeRequired")))));
        attributes.add(new AttributeInfo("IndependentStudy", String.valueOf(Boolean.valueOf(sectionNode.elementText("IndependentStudy")))));
        attributes.add(new AttributeInfo("EnrollmentRestrictions", String.valueOf(Boolean.valueOf(sectionNode.elementText("EnrollmentRestrictions")))));
        attributes.add(new AttributeInfo("FeeAmount", feeAmount));
        attributes.add(new AttributeInfo("SectionComments", sectionComments));
        attributes.add(new AttributeInfo("SummerTerm", summerTerm));
        attributes.add(new AttributeInfo("SLN", sln));
        attributes.add(new AttributeInfo("PrimaryActivityOfferingCode", primaryActivityCd));
        attributes.add(new AttributeInfo("PrimaryActivityOfferingId", buildId(yr, yt.getTermAsString(), curriculum, number, primaryActivityCd)));

        return info;
    }

    /**
     * Builds CourseOfferingInfo for given document
     *
     * @param document
     * @return
     */
    public static CourseOfferingInfo buildCourseOfferingInfo(Document document, CourseInfo courseInfo) {
        StringBuffer courseComments = new StringBuffer();
        StringBuffer curriculumComments = new StringBuffer();
        DefaultXPath curriculumCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:CurriculumComments/s:Lines");
        DefaultXPath courseCommentsPath = newXPath("/s:Section/s:TimeScheduleComments/s:CourseComments/s:Lines");
        DefaultXPath primarySectionPath = newXPath("/s:Section/s:PrimarySection");
        DefaultXPath secondarySectionPath = newXPath("/s:Section");
        DefaultXPath coursePath = newXPath("/s:Section/s:Course");

        Element curriculumCommentsNode = (Element) curriculumCommentsPath.selectSingleNode(document);
        Element secondarySection = (Element) secondarySectionPath.selectSingleNode(document);
        Element courseCommentsNode = (Element) courseCommentsPath.selectSingleNode(document);
        Element primarySectionNode = (Element) primarySectionPath.selectSingleNode(document);
        Element courseNode = (Element) coursePath.selectSingleNode(document);
        List comments = courseCommentsNode.content();
        List curricComments = curriculumCommentsNode.content();


        /*Calculating institute code and skipping if code is other than seattle and UWPCE*/
        String instituteCode = null;

        {
            DefaultXPath linkPath = newXPath("s:Curriculum/s:TimeScheduleLinkAbbreviation");
            Element link = (Element) linkPath.selectSingleNode(secondarySection);
            instituteCode = link.getTextTrim();

            // displaying main campus, PCE, and ROTC
            // refer to https://jira.cac.washington.edu/browse/MYPLAN-1583 for list of supported institute codes
            int instituteNumber = getInstituteNumber(instituteCode);
            switch (instituteNumber) {
                case 0: // Main campus
                case 95: // PCE
                case 88: // ROTC
                {
                    instituteCode = Integer.toString(instituteNumber);
                    break;
                }

                case -1: //  POA #1: Exclude sections with a null/blankTimeScheduleLinkAbbreviation.
                default: // Also omit all others not in above list
                {
                    return null;
                }
            }

        }


        for (Object ob : comments) {
            Element element = (Element) ob;
            String text = element.elementText("Text");
            if (text.startsWith("*****")) {
                courseComments = courseComments.append("<br>" + text + "</br> ");
            } else {
                courseComments = courseComments.append(text + " ");
            }
        }
        for (Object ob : curricComments) {
            Element element = (Element) ob;
            String text = element.elementText("Text");
            if (text.startsWith("*****")) {
                curriculumComments = curriculumComments.append("<br>" + text + "</br> ");
            } else {
                curriculumComments = curriculumComments.append(text + " ");
            }
        }
        String year = courseNode.elementText("Year");
        String quarter = courseNode.elementText("Quarter");
        String subject = courseNode.elementText("CurriculumAbbreviation");
        String number = courseNode.elementText("CourseNumber");
        String sectionId = secondarySection.elementText("SectionID");
        AtpHelper.YearTerm yearTerm = AtpHelper.quarterYearToYearTerm(quarter, year);

        CourseOfferingInfo info = new CourseOfferingInfo();
        info.setSubjectArea(subject);
        info.setCourseNumberSuffix(number);
        info.setTermId(yearTerm.toATP());
        info.setId(buildId(year, yearTerm.getTermAsString(), subject, number, sectionId));

        // If course info not specified calculate the courseVersionId and courseInfo from courseHelper
        if (courseInfo == null) {
            info.setCourseId(getCourseHelper().getCourseIdForTerm(subject, number, yearTerm.toATP()));
            courseInfo = getCourseHelper().getCourseInfoByIdAndCd(info.getCourseId(), getCourseHelper().getKeyForCourse(subject,number));
        } else {
            info.setCourseId(courseInfo.getId());
        }
        if (courseInfo != null) {
            info.setCourseCode(courseInfo.getCode());
        }
        info.getAttributes().add(new AttributeInfo("CourseComments", courseComments.toString()));
        info.getAttributes().add(new AttributeInfo("CurriculumComments", curriculumComments.toString()));
        info.getAttributes().add(new AttributeInfo("PrimarySectionCd", primarySectionNode.elementText("SectionID")));
        info.setStateKey(secondarySection.elementText("DeleteFlag"));

        {
            String gradingSystem = secondarySection.elementText("GradingSystem");
            if ("standard".equals(gradingSystem)) {
                info.setGradingOptionId("uw.result.group.grading.option.standard");
                info.setGradingOption(null);
            } else if ("credit/no credit".equals(gradingSystem)) {
                info.setGradingOptionId("uw.result.group.grading.option.crnc");
                info.setGradingOption("Credit/No-Credit grading");
            }
        }

        {
            String creditControl = secondarySection.elementText("CreditControl");
            String minCreditID = secondarySection.elementText("MinimumTermCredit");
            String minCreditName = minCreditID;
            if (minCreditName != null && minCreditName.endsWith(".0")) {
                minCreditName = minCreditName.substring(0, minCreditName.length() - 2);
            }
            String maxCreditID = secondarySection.elementText("MaximumTermCredit");
            String maxCreditName = maxCreditID;
            if (maxCreditName != null && maxCreditName.endsWith(".0")) {
                maxCreditName = maxCreditName.substring(0, maxCreditName.length() - 2);
            }

            // Default values so its visually obvious when the mapping is incorrect
            String creditID = "X";
            String creditName = "X";

            if ("fixed credit".equals(creditControl)) {
                creditID = minCreditID;
                creditName = minCreditName;
            } else if ("variable credit - min to max credits".equals(creditControl)) {
                creditID = minCreditID + "-" + maxCreditID;
                creditName = minCreditName + "-" + maxCreditName;
            } else if ("variable credit - min or max credits".equals(creditControl)) {
                creditID = minCreditID + ", " + maxCreditID;
                creditName = minCreditName + ", " + maxCreditName;
            } else if ("variable credit - 1 to 25 credits".equals(creditControl)) {
                creditID = "1.0-25.0";
                creditName = "1-25";
            } else if ("zero credits".equals(creditControl)) {
                creditID = "0.0";
                creditName = "0";
            }

            creditID = "kuali.uw.resultcomponent.credit." + creditID;
            info.setCreditOptionId(creditID);
            info.setCreditOptionName(creditName);

        }

        return info;
    }

    /**
     * eg for Id : "2013:4:ENGL:131:A"
     *
     * @param year
     * @param term
     * @param curriculum
     * @param number
     * @param sectionId
     * @return
     */
    private static String buildId(String year, String term, String curriculum, String number, String sectionId) {
        return getCourseHelper().joinStringsByDelimiter(':', year, term, curriculum, number, sectionId);
    }

    /**
     * @param expr
     * @return
     */
    public static DefaultXPath newXPath(String expr) {
        DefaultXPath path = new DefaultXPath(expr);
        path.setNamespaceURIs(NAMESPACES);
        return path;
    }

    private static Map<String, String> NAMESPACES = new HashMap<String, String>() {{
        put("s", "http://webservices.washington.edu/student/");
    }};

    /**
     * @param xml
     * @return
     * @throws DocumentException
     */
    public static Document newDocument(String xml) throws DocumentException {
        SAXReader sax = new SAXReader();
        StringReader sr = new StringReader(xml);
        Document doc = sax.read(sr);
        return doc;
    }

    /**
     * Extracts institute number prefix from the SWS link information, which is reused as the institutecode.
     * Blank (empty, null) returns -1.
     * <p/>
     * <p/>
     * "123abc" -> 123
     * "abc" -> 0
     * "" -> -1
     * null -> -1
     */

//  POA #1: Exclude sections with a null/blankTimeScheduleLinkAbbreviation.
    private static int getInstituteNumber(String link) {
        if (link == null) return -1;
        if ("".equals(link.trim())) return -1;

        int institute = 0;
        Matcher m = regexInstituteCodePrefix.matcher(link);
        if (m.find()) {
            String ugh = m.group(1);
            institute = Integer.parseInt(ugh);
        }

        return institute;
    }

}
