package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildEvent;

import java.util.List;

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

}
