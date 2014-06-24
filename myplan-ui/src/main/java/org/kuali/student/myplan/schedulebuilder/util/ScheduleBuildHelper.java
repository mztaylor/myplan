package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.schedulebuilder.infc.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 3/7/14.
 */
public interface ScheduleBuildHelper {


    public long[] block(int fromSlot, int toSlot);

    public void unionWeeks(long[][] week, long[][] add);

    public boolean checkForConflictsWeeks(long[][] week1, long[][] week2);

    public boolean dayIntersects(long[] day1, long[] day2);

    public long[][] xlateClassMeetingTime2WeekOfBits(ScheduleBuildEvent event);

    public long[][] xlateClassMeetingTimeList2WeekBits(List<? extends ScheduleBuildEvent> meetingTimes);

    public void buildReservedTimeEvents(ReservedTime rt, Term term);

    public void buildPossibleScheduleEvents(PossibleScheduleOption pso, Term term);

    public List<ActivityOption> validatedSavedActivities(List<ActivityOption> activityOptions, Map<String, Map<String, List<String>>> invalidOptions, List<ReservedTime> reservedTimes, List<String> plannedActivities, PossibleScheduleOption registered, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData, Map<String, Map<String, ActivityOption>> courseCodeToActivities);

    public long[][] extractClassMeetingTimeWeekBitsFromAOList(List<ActivityOption> aoList);

    public long[][] extractClassMeetingTimeWeekBitsFromCourseOptionList(List<CourseOption> coList);

    public void validateForErrors(PossibleScheduleErrorsInfo possibleScheduleErrors, String courseCd, List<ActivityOption> activityOptions, List<String> invalidatedActivities);

    public void updateEnrollmentInfo(List<ActivityOption> activityOptions, LinkedHashMap<String, LinkedHashMap<String, Object>> enrollmentData);

    public LinkedHashMap<String, LinkedHashMap<String, Object>> getEnrollmentDataForActivities(List<ActivityOption> activityOptions);

    public LinkedHashMap<String, LinkedHashMap<String, Object>> getEnrollmentDataForPossibleSchedules(List<PossibleScheduleOption> possibleScheduleOptions);

    public String validateOrPopulateLearningPlanId(String requestedLearningPlanId);
}
