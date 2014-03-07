package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildEvent;

import javax.json.JsonArrayBuilder;
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


}
