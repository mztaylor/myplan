package org.kuali.student.myplan.schedulebuilder.util;

import org.apache.log4j.Logger;
import org.joda.time.DateTimeComparator;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.CourseHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.schedulebuilder.dto.*;
import org.kuali.student.myplan.schedulebuilder.infc.*;
import org.kuali.student.myplan.utils.CalendarUtil;
import org.springframework.util.StringUtils;

import javax.json.*;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class ScheduleBuilder implements Serializable {

    private static final long serialVersionUID = 4792345964542902431L;

    private static CalendarUtil calendarUtil;
    private static CourseHelper courseHelper;

    private static final Logger LOG = Logger.getLogger(ScheduleBuilder.class);

    private static long[][][] BLOCK_CACHE = new long[288][288][];

    private static long[] block(int fromSlot, int toSlot) {
        long[] rv = BLOCK_CACHE[fromSlot][toSlot];
        if (rv != null)
            return rv;
        rv = new long[5];
        for (int i = fromSlot; i <= toSlot; i++)
            rv[i / 64] |= 1L << i % 64;
        return BLOCK_CACHE[fromSlot][toSlot] = rv;
    }

    private static boolean intersects(long[] day, int fromSlot, int toSlot) {
        boolean rv = false;
        long[] block = block(fromSlot, toSlot);
        for (int i = 0; !rv && i < 5; i++)
            rv |= 0L != (day[i] & block[i]);
        return rv;
    }

    private static void union(long[] day, int fromSlot, int toSlot) {
        long[] block = block(fromSlot, toSlot);
        for (int i = 0; i < 5; i++)
            day[i] |= block[i];
    }

    private static boolean isEpoch(Calendar c) {
        return c.get(Calendar.YEAR) == 1970
                && c.get(Calendar.MONDAY) == Calendar.JANUARY
                && c.get(Calendar.DATE) == 1;
    }

    private static boolean checkForConflicts(ScheduleBuildEvent event,
                                             Date[] sundays, long[][][] days, Calendar c) {
        if (event.isAllDay())
            return true;

        for (int i = 0; i < sundays.length; i++) {

            c.setTime(event.getStartDate());
            int fromSlot = c.get(Calendar.HOUR_OF_DAY) * 12
                    + (c.get(Calendar.MINUTE) / 5);
            if (!isEpoch(c)) {
                c.add(Calendar.DATE, -7);
                if (!c.getTime().before(sundays[i]))
                    continue;
            }

            c.setTime(event.getUntilDate());
            int toSlot = c.get(Calendar.HOUR_OF_DAY) * 12
                    + (c.get(Calendar.MINUTE) / 5);
            if (!isEpoch(c) && !c.getTime().after(sundays[i]))
                continue;

            if (event.isSunday()) {
                if (intersects(days[i][0], fromSlot, toSlot))
                    return false;
                else
                    union(days[i][0], fromSlot, toSlot);
            }
            if (event.isMonday()) {
                if (intersects(days[i][1], fromSlot, toSlot))
                    return false;
                else
                    union(days[i][1], fromSlot, toSlot);
            }
            if (event.isTuesday()) {
                if (intersects(days[i][2], fromSlot, toSlot))
                    return false;
                else
                    union(days[i][2], fromSlot, toSlot);
            }
            if (event.isWednesday()) {
                if (intersects(days[i][3], fromSlot, toSlot))
                    return false;
                else
                    union(days[i][3], fromSlot, toSlot);
            }
            if (event.isThursday()) {
                if (intersects(days[i][4], fromSlot, toSlot))
                    return false;
                else
                    union(days[i][4], fromSlot, toSlot);
            }
            if (event.isFriday()) {
                if (intersects(days[i][5], fromSlot, toSlot))
                    return false;
                else
                    union(days[i][5], fromSlot, toSlot);
            }
            if (event.isSaturday()) {
                if (intersects(days[i][6], fromSlot, toSlot))
                    return false;
                else
                    union(days[i][6], fromSlot, toSlot);
            }
        }
        return true;
    }

    private static boolean checkForConflicts(ActivityOption ao, Date[] sundays,
                                             long[][][] days, Calendar c) {
        for (ClassMeetingTime meetingTime : ao.getClassMeetingTimes())
            if (!checkForConflicts(meetingTime, sundays, days, c))
                return false;
        return true;
    }

    private static Date[] getSundays(Term term, Calendar c) {
        List<Date> sundayList = new ArrayList<Date>(16);
        c.setTime(term.getStartDate());
        c.add(Calendar.DATE, -(c.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
        while (c.getTime().before(term.getEndDate())) {
            sundayList.add(c.getTime());
            c.add(Calendar.DATE, 7);
        }
        return sundayList.toArray(new Date[sundayList.size()]);
    }

    private final Term term;
    private final List<CourseOption> courseOptions;
    private final List<ReservedTime> reservedTimes;
    private final List<PossibleScheduleOption> savedSchedules;
    private final ScheduleBuildFilters buildFilters;
    private final Set<Long> primaryConflicts = new HashSet<Long>();
    private final boolean empty;

    private int currentCourseIndex;
    private final int[] currentPrimaryActivityIndex;
    private final int[][] currentSecondaryOptionIndex;
    private final int[][][] currentSecondaryActivityIndex;
    private final int[][][] limitSecondaryOption;

    private int scheduleNumber;
    private boolean hasMore;
    private Set<PossibleScheduleOption> generated;

    @Override
    public String toString() {
        String rv = "ScheduleBuilder\n  currentCourse " + currentCourseIndex
                + "\n  currentPrimary "
                + Arrays.toString(currentPrimaryActivityIndex)
                + "\n  currentSecondary ";
        for (int[] cs : currentSecondaryOptionIndex)
            rv += "\n      " + Arrays.toString(cs);
        rv += "\n  currentSecondaryOption\n    ";
        for (int[][] cso : currentSecondaryActivityIndex)
            for (int[] cs : cso)
                rv += "," + Arrays.toString(cs);
        rv += "\n  limitSecondaryOption\n    ";
        for (int[][] cso : limitSecondaryOption)
            for (int[] cs : cso)
                rv += "," + Arrays.toString(cs);
        return rv;
    }

    public ScheduleBuilder(Term term, List<CourseOption> courseOptions,
                           List<ReservedTime> reservedTimes, List<PossibleScheduleOption> savedSchedules, ScheduleBuildFilters buildFilters) {
        this.term = term;
        this.reservedTimes = reservedTimes != null ? reservedTimes : new ArrayList<ReservedTime>();
        this.savedSchedules = savedSchedules != null ? savedSchedules : new ArrayList<PossibleScheduleOption>();
        this.buildFilters = buildFilters;

        boolean empty = false;
        List<CourseOption> co = new ArrayList<CourseOption>(
                courseOptions == null ? 0 : courseOptions.size());
        if (courseOptions != null)
            course:for (CourseOption c : courseOptions)
                if (c.isSelected()) {
                    List<ActivityOption> oaol = c.getActivityOptions();
                    List<ActivityOption> aol = new ArrayList<ActivityOption>();
                    for (ActivityOption ao : oaol)
                        if (ao.isSelected() || ao.isLockedIn()) {
                            List<SecondaryActivityOptions> osol = ao
                                    .getSecondaryOptions();
                            List<SecondaryActivityOptions> sol = new ArrayList<SecondaryActivityOptions>(
                                    osol.size());
                            for (SecondaryActivityOptions so : osol) {
                                List<ActivityOption> osaol = so
                                        .getActivityOptions();
                                List<ActivityOption> saol = new ArrayList<ActivityOption>(
                                        osaol.size());
                                for (ActivityOption sao : osaol)
                                    if (sao.isSelected() || sao.isLockedIn())
                                        saol.add(sao);
                                if (saol.isEmpty()) {
                                    LOG.warn("Course option is selected, but has no selected activities for "
                                            + ao.getRegistrationCode()
                                            + " "
                                            + so.getActivityTypeDescription()
                                            + " "
                                            + c.getCourseId()
                                            + " "
                                            + c.getCourseCode());
                                    empty = true;
                                    break course;
                                }
                                SecondaryActivityOptionsInfo soi = new SecondaryActivityOptionsInfo(
                                        so);
                                soi.setActivityOptions(saol);
                                sol.add(soi);
                            }
                            ActivityOptionInfo aoi = new ActivityOptionInfo(ao);
                            aoi.setSecondaryOptions(sol);
                            aol.add(aoi);
                        }
                    if (aol.isEmpty()) {
                        LOG.warn("Course option is selected, but has no selected activities "
                                + c.getCourseId() + " " + c.getCourseCode());
                        empty = true;
                        break course;
                    }
                    CourseOptionInfo ci = new CourseOptionInfo(c);
                    ci.setActivityOptions(aol);
                    co.add(ci);
                }
        this.empty = empty;
        this.courseOptions = Collections.unmodifiableList(co);

        currentPrimaryActivityIndex = new int[co.size()];
        currentSecondaryOptionIndex = new int[co.size()][];
        currentSecondaryActivityIndex = new int[co.size()][][];
        limitSecondaryOption = new int[co.size()][][];

        for (int i = 0; i < co.size(); i++) {
            List<ActivityOption> primaryActivityOptions = co.get(i)
                    .getActivityOptions();
            currentSecondaryOptionIndex[i] = new int[primaryActivityOptions
                    .size()];
            currentSecondaryActivityIndex[i] = new int[primaryActivityOptions
                    .size()][];
            limitSecondaryOption[i] = new int[primaryActivityOptions.size()][];
            for (int j = 0; j < primaryActivityOptions.size(); j++) {
                List<SecondaryActivityOptions> secondaryActivityOptions = primaryActivityOptions
                        .get(j).getSecondaryOptions();
                currentSecondaryActivityIndex[i][j] = new int[secondaryActivityOptions
                        .size()];
                limitSecondaryOption[i][j] = new int[secondaryActivityOptions
                        .size()];
                for (int k = 0; k < secondaryActivityOptions.size(); k++) {
                    SecondaryActivityOptions secondaryActivityOption = secondaryActivityOptions
                            .get(k);
                    if (secondaryActivityOption.isEnrollmentGroup()) {
                        // enrollment groups must include all secondary options
                        limitSecondaryOption[i][j][k] = 0;
                    } else {
                        // for non-enrollment secondary options, select one at a
                        // time
                        limitSecondaryOption[i][j][k] = secondaryActivityOptions
                                .get(k).getActivityOptions().size();
                    }
                }
            }
        }
    }

    /**
     * Queue course indexes for the current run, considering locked-in courses
     * first followed by selected course.
     *
     * @param courseQueue
     */
    private void prepareCourseQueue(Queue<Integer> courseQueue) {
        courseQueue.clear();
        int courseOptionsLength = courseOptions.size();
        for (int i = 0; i < courseOptionsLength; i++) {
            int courseIndex = currentCourseIndex + i;
            if (courseIndex >= courseOptionsLength)
                courseIndex -= courseOptionsLength;
            CourseOption courseOption = courseOptions.get(courseIndex);

            if (courseOption.isLockedIn())
                courseQueue.offer(courseIndex);
        }
        for (int i = 0; i < courseOptionsLength; i++) {
            int courseIndex = currentCourseIndex + i;
            if (courseIndex >= courseOptionsLength)
                courseIndex -= courseOptionsLength;
            CourseOption courseOption = courseOptions.get(courseIndex);

            if (!courseOption.isLockedIn() && courseOption.isSelected())
                courseQueue.offer(courseIndex);
        }
    }

    /**
     * Step forward to the next course and activity option combination.
     *
     * @return True if this step has rolled the options over to the initial
     * state, false if the initial state has not reached.
     */
    private boolean step(Queue<Integer> courseQueue) {
        if (currentPrimaryActivityIndex.length < 1)
            return true;

        boolean conflict;

        do {
            increment:
            {
                // Operate on the next course option, stepping backward
                int lastCourseIndex = courseOptions.size() - 1;
                currentCourseIndex = currentCourseIndex == 0 ? lastCourseIndex
                        : currentCourseIndex - 1;

                int primaryActivityIndex = currentPrimaryActivityIndex[currentCourseIndex];
                int[] secondaryActivityIndex = currentSecondaryActivityIndex[currentCourseIndex][primaryActivityIndex];
                if (secondaryActivityIndex.length > 0) {
                    // increment current secondary option activity index,
                    // stepping
                    // forward.
                    int secondaryOptionIndex = currentSecondaryOptionIndex[currentCourseIndex][primaryActivityIndex];
                    int secondaryOptionActivityIndex = secondaryActivityIndex[secondaryOptionIndex] + 1;
                    if (secondaryOptionActivityIndex >= limitSecondaryOption[currentCourseIndex][primaryActivityIndex][secondaryOptionIndex])
                        secondaryOptionActivityIndex = 0;
                    secondaryActivityIndex[secondaryOptionIndex] = secondaryOptionActivityIndex;

                    // increment current secondary option index, stepping
                    // forward.
                    secondaryOptionIndex += 1;
                    if (secondaryOptionIndex >= secondaryActivityIndex.length)
                        secondaryOptionIndex = 0;
                    currentSecondaryOptionIndex[currentCourseIndex][primaryActivityIndex] = secondaryOptionIndex;

                    // Not the last secondary option for the current primary,
                    // return
                    // without rollover
                    if (secondaryOptionActivityIndex > 0
                            || secondaryOptionIndex > 0)
                        break increment;
                }

                // increment current primary activity index, stepping forward.
                primaryActivityIndex += 1;
                if (primaryActivityIndex >= currentSecondaryActivityIndex[currentCourseIndex].length)
                    primaryActivityIndex = 0;
                currentPrimaryActivityIndex[currentCourseIndex] = primaryActivityIndex;

                // Determine whether or not rollover has taken place, and return
                if (currentCourseIndex == 0) {
                    assert currentPrimaryActivityIndex.length == currentSecondaryActivityIndex.length;
                    for (int i = 0; i < currentSecondaryActivityIndex.length; i++) {
                        if (currentPrimaryActivityIndex[i] > 0)
                            break increment;
                        for (int j = 0; j < currentSecondaryActivityIndex[i].length; j++)
                            for (int k = 0; k < currentSecondaryActivityIndex[i][j].length; k++)
                                if (currentSecondaryActivityIndex[i][j][k] > 0)
                                    break increment;
                    }
                    primaryConflicts.clear();
                    return true;
                }
            }

            conflict = false;
            long hash = (long) currentCourseIndex;
            prepareCourseQueue(courseQueue);
            conflict:
            while (!conflict && !courseQueue.isEmpty()) {

                int courseIndex = courseQueue.poll();
                int activityOptionIndex = currentPrimaryActivityIndex[courseIndex];
                hash = hash * 127L + (long) activityOptionIndex;
                if (conflict = primaryConflicts.contains(hash))
                    break conflict;

                int[] secondaryActivityIndex = currentSecondaryActivityIndex[courseIndex][activityOptionIndex];
                long shash = hash;
                for (int j = 0; j < secondaryActivityIndex.length; j++) {
                    int sidx = secondaryActivityIndex[j];
                    shash = shash * 31L + (long) sidx;
                    if (conflict = primaryConflicts.contains(shash))
                        break conflict;
                }
            }

        } while (conflict);

        return false;
    }

    public List<PossibleScheduleOption> getNext(int count, Set<PossibleScheduleOption> current) {
        if (empty) {
            if (LOG.isDebugEnabled())
                LOG.debug("Schedule builder is marked as empty, at least one course option has not associated activity options");
            return Collections.emptyList();
        }


        boolean rolled = false;
        boolean done = false;
        Calendar tempCalendar = Calendar.getInstance();
        Date[] sundays = getSundays(term, tempCalendar);
        long[][][] days = new long[sundays.length][7][5];
        int iterationCount = 0;
        int iterationsSinceLast = 0;
        if (generated == null) {
            assert !hasMore;
            generated = new HashSet<PossibleScheduleOption>();
        }

        Queue<Integer> courseQueue = new LinkedList<Integer>();
        List<PossibleScheduleOption> rv = new ArrayList<PossibleScheduleOption>(count);
        List<ActivityOption> possibleActivityOptions = new LinkedList<ActivityOption>();

        StringBuilder msg = null;
        if (LOG.isDebugEnabled()) {
            msg = new StringBuilder("Schedule build run\nCourses:");
            for (CourseOption co : courseOptions) {
                msg.append("\n  ").append(co.getCourseCode());
                if (co.isSelected())
                    msg.append(" selected");
                if (co.isLockedIn())
                    msg.append(" locked-in");
                for (ActivityOption pao : co.getActivityOptions())
                    if (pao.isSelected() || pao.isLockedIn()) {
                        msg.append("\n    Primary ").append(
                                pao.getRegistrationCode());
                        if (pao.isSelected())
                            msg.append(" selected");
                        if (pao.isLockedIn())
                            msg.append(" locked-in");
                        for (SecondaryActivityOptions so : pao
                                .getSecondaryOptions()) {
                            for (ActivityOption sao : so.getActivityOptions()) {
                                if (sao.isSelected() || sao.isLockedIn()) {
                                    msg.append("\n    ")
                                            .append(so
                                                    .getActivityTypeDescription())
                                            .append(" ")
                                            .append(pao.getRegistrationCode());
                                    if (sao.isSelected())
                                        msg.append(" selected");
                                    if (sao.isLockedIn())
                                        msg.append(" locked-in");
                                }
                            }
                        }
                    }
            }
        }

        do {
            possibleActivityOptions.clear();
            for (int i = 0; i < sundays.length; i++)
                for (int j = 0; j < 7; j++)
                    for (int k = 0; k < 5; k++)
                        days[i][j][k] = 0L;
            for (ReservedTime reservedTime : reservedTimes)
                if (reservedTime.isSelected())
                    checkForConflicts(reservedTime, sundays, days, tempCalendar);

            boolean allCourses = true;
            if (msg != null)
                msg.append("\nIteration ").append(++iterationCount);
            iterationsSinceLast++;

            long hash = (long) currentCourseIndex;
            prepareCourseQueue(courseQueue);
            course:
            while (!courseQueue.isEmpty()) {
                int courseIndex = courseQueue.poll();
                CourseOption courseOption = courseOptions.get(courseIndex);
                if (msg != null)
                    msg.append(" co ").append(courseOption.getCourseCode());

                int activityOptionIndex = currentPrimaryActivityIndex[courseIndex];
                hash = hash * 127L + (long) activityOptionIndex;

                ActivityOption primary = courseOption.getActivityOptions().get(
                        activityOptionIndex);
                if (msg != null)
                    msg.append(" pri ").append(primary.getRegistrationCode());

                if (!checkForConflicts(primary, sundays, days, tempCalendar)
                        && !courseOption.isLockedIn()) {
                    if (msg != null)
                        msg.append(" conflict");
                    primaryConflicts.add(hash);
                    allCourses = false;
                    break course;
                }

                int[] secondaryActivityIndex = currentSecondaryActivityIndex[courseIndex][activityOptionIndex];
                if (primary.isEnrollmentGroup()) {
                    if (msg != null)
                        msg.append(" block");
                    for (int j = 0; j < secondaryActivityIndex.length; j++) {
                        List<ActivityOption> secondaryActivityOptions = primary
                                .getSecondaryOptions().get(j)
                                .getActivityOptions();
                        if (msg != null)
                            msg.append(" sec ").append(
                                    secondaryActivityOptions.size());
                        for (ActivityOption secondary : secondaryActivityOptions) {
                            if (msg != null)
                                msg.append(" ").append(
                                        secondary.getRegistrationCode());
                            if (!checkForConflicts(secondary, sundays, days,
                                    tempCalendar)) {
                                if (msg != null)
                                    msg.append(" conflict");
                                allCourses = false;
                                break course;
                            }
                        }
                    }
                } else {
                    long shash = hash;
                    for (int j = 0; j < secondaryActivityIndex.length; j++) {
                        int sidx = secondaryActivityIndex[j];
                        ActivityOption secondary = primary
                                .getSecondaryOptions().get(j)
                                .getActivityOptions().get(sidx);
                        shash = shash * 31L + (long) sidx;
                        if (msg != null)
                            msg.append(" sec ").append(
                                    secondary.getRegistrationCode());
                        if ((!secondary.isSelected() && !secondary.isLockedIn())
                                || !checkForConflicts(secondary, sundays, days,
                                tempCalendar)) {
                            primaryConflicts.add(shash);
                            if (msg != null)
                                msg.append(" conflict");
                            allCourses = false;
                            break course;
                        }
                    }
                }

                possibleActivityOptions.add(primary);
                for (int j = 0; j < secondaryActivityIndex.length; j++) {
                    SecondaryActivityOptions secondaryActivityOptions = primary
                            .getSecondaryOptions().get(j);
                    if (secondaryActivityOptions.isEnrollmentGroup()) {
                        for (ActivityOption secondary : secondaryActivityOptions
                                .getActivityOptions())
                            possibleActivityOptions.add(secondary);
                    } else
                        possibleActivityOptions.add(secondaryActivityOptions
                                .getActivityOptions().get(
                                        secondaryActivityIndex[j]));
                }
            }

            if (allCourses) {
                PossibleScheduleOptionInfo pso = new PossibleScheduleOptionInfo();
                pso.setUniqueId(UUID.randomUUID().toString());
                Collections.sort(possibleActivityOptions,
                        new Comparator<ActivityOption>() {
                            @Override
                            public int compare(ActivityOption o1,
                                               ActivityOption o2) {
                                String cc1 = o1.getCourseOfferingCode();
                                String cc2 = o2.getCourseOfferingCode();
                                if (cc1 == null && cc2 != null)
                                    return 1;
                                if (cc1 != null && cc2 == null)
                                    return -1;
                                if (cc1 != null && cc2 != null) {
                                    int ccc = cc1.compareTo(cc2);
                                    if (ccc != 0)
                                        return ccc;
                                }
                                return o1.compareTo(o2);
                            }
                        });
                pso.setActivityOptions(possibleActivityOptions);
                if (!generated.add(pso)) {
                    if (msg != null)
                        msg.append(" dup-rolled");
                } else if (current.contains(pso)) {
                    scheduleNumber++;
                    if (msg != null)
                        msg.append(" dup-current");
                } else {
                    iterationsSinceLast = 0;
                    String descr = "Schedule " + (++scheduleNumber);
                    pso.setTermId(term.getId());
                    pso.setDescription(descr);
                    int saveIndex = savedSchedules.indexOf(pso);
                    if (saveIndex != -1) {
                        pso.setId(savedSchedules.get(saveIndex).getId());
                    }
                    buildPossibleScheduleEvents(pso);
                    rv.add(pso);
                    if (msg != null) {
                        msg.append("\nPossible option #").append(rv.size());
                        msg.append(" ").append(descr).append(" ");
                        for (ActivityOption ao : pso.getActivityOptions())
                            msg.append(" ").append(ao.getCourseOfferingCode())
                                    .append(" ")
                                    .append(ao.getRegistrationCode());
                    }
                }
            }

            boolean justRolled = step(courseQueue);
            if (justRolled) {
                scheduleNumber = 0;
                rolled = true;
                hasMore = generated.size() > count;
                if (msg != null)
                    msg.append("\nJust rolled. Count: ").append(count)
                            .append(" Current: ").append(current.size())
                            .append(" Generated: ").append(generated.size())
                            .append(" More? ").append(hasMore);
                generated = new HashSet<PossibleScheduleOption>();
            }
            done = rv.size() >= count || (justRolled && !hasMore);
            if (iterationsSinceLast > 50000) {
                done = true;
                if (msg != null)
                    msg.append(" !loop");
            }
            if (msg != null) {
                if (justRolled)
                    msg.append(" just");
                if (rolled)
                    msg.append(" rolled");
                if (done)
                    msg.append(" done");
            }
        } while (!done);
        if (msg != null) {
            LOG.debug(msg);
        }
        hasMore = hasMore || (rv.size() >= count && !rolled);
        return rv;
    }

    public boolean hasMore() {
        return hasMore;
    }

    private static class EventAggregateData {
        private final Calendar cal = Calendar.getInstance();
        private final Date minDate;
        private final Date maxDate;
        private final Date displayDate;
        private final Set<Date> breakDates = new TreeSet<Date>();

        private boolean weekends;
        private int minTime = 8;
        private int maxTime = 17;
        private Date lastUntilDate;

        private EventAggregateData(Date minDate, Date maxDate, Date displayDate) {
            this.minDate = minDate;
            this.maxDate = maxDate;
            this.displayDate = displayDate;
        }

        private Date getDatePortion(Date date) {
            if (date == null)
                return null;

            cal.setTime(date);
            if (cal.get(Calendar.YEAR) == 1970
                    && cal.get(Calendar.MONTH) == Calendar.JANUARY
                    && cal.get(Calendar.DATE) == 1)
                return null;

            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            return cal.getTime();
        }

        private Date getTimePortion(Date date) {
            if (date == null) {
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
            } else
                cal.setTime(date);

            cal.set(Calendar.YEAR, 1970);
            cal.set(Calendar.MONTH, Calendar.JANUARY);
            cal.set(Calendar.DATE, 1);
            return cal.getTime();
        }

        private void addBreakDate(Date date) {
            getDatePortion(date); // adjusts cal
            cal.add(Calendar.DATE,
                    -(cal.get(Calendar.DAY_OF_WEEK) - Calendar.SUNDAY));
            breakDates.add(minDate.after(cal.getTime()) ? minDate : cal
                    .getTime());
        }

        private void updateLastUntilDate(Date untilDate) {
            getDatePortion(untilDate); // adjusts cal
            cal.add(Calendar.DATE,
                    Calendar.SUNDAY + 7 - cal.get(Calendar.DAY_OF_WEEK));
            if (maxDate.after(cal.getTime()))
                breakDates.add(cal.getTime());
            if (!maxDate.before(untilDate)
                    && (lastUntilDate == null || (untilDate != null && lastUntilDate
                    .before(untilDate))))
                lastUntilDate = untilDate;
        }

        private void updateMinMaxTime(Date eventStart, Date eventEnd) {
            cal.setTime(eventStart);
            minTime = Math.min(minTime, cal.get(Calendar.HOUR_OF_DAY));
            cal.setTime(eventEnd);
            maxTime = Math.max(maxTime,
                    cal.get(Calendar.HOUR_OF_DAY)
                            + (cal.get(Calendar.MINUTE) > 0 ? 1 : 0));
        }

        public void updateWeekends(boolean weekend) {
            weekends = weekend;
        }

        private Date addOneWeek(Date eventStart) {
            cal.setTime(eventStart);
            cal.add(Calendar.DATE, 7);
            return cal.getTime();
        }

        private void addWeekBreaks(JsonArrayBuilder jweeks, Term term) {
            int weekNumber = 1;
            Iterator<Date> weekBreaks = breakDates.iterator();
            Date start = weekBreaks.hasNext() ? weekBreaks.next() : term.getStartDate();
            if (lastUntilDate == null)
                lastUntilDate = term.getEndDate();
            DateFormat df = new SimpleDateFormat("MMM d");
            boolean done;
            do {
                Date end = (done = !weekBreaks.hasNext()) ? lastUntilDate
                        : weekBreaks.next();
                long sd = start.getTime();
                long ed = end.getTime();
                int weeks = (int) ((ed - sd) / 604800000);
                JsonObjectBuilder jweek = Json.createObjectBuilder();
                jweek.add("title", "Week" + (weeks > 1 ? "s " : " ")
                        + weekNumber
                        + (weeks > 1 ? "-" + (weekNumber + weeks - 1) : ""));
                jweek.add("subtitle", df.format(start) + " - " + df.format(end));
                cal.setTime(start);
                jweek.add("gotoYear", cal.get(Calendar.YEAR));
                jweek.add("gotoMonth", cal.get(Calendar.MONTH) + 1);
                jweek.add("gotoDate", cal.get(Calendar.DATE));
                jweeks.add(jweek);
                weekNumber += weeks;
                start = end;
            } while (!done);
        }
    }


    /**
     * Builds the json string with events  required for the calendar and sets that to the reservedTime event property
     *
     * @param rt
     */
    public void buildReservedTimeEvents(ReservedTime rt) {
        Date minDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date maxDate = getCalendarUtil().getDateAfterXdays(minDate, 5);
        Date displayDate = term.getEndDate();
        SimpleDateFormat ddf = new SimpleDateFormat("MM/dd/yyyy");

        EventAggregateData aggregate = new EventAggregateData(minDate, maxDate, displayDate);
        JsonObjectBuilder rto = Json.createObjectBuilder();
        JsonArrayBuilder jevents = Json.createArrayBuilder();
        rto.add("id", rt.getId());
        rto.add("uniqueId", rt.getUniqueId());
        rto.add("daysTimes", rt.getDaysAndTimes());
        rto.add("startDate", ddf.format(rt.getStartDate()));
        rto.add("untilDate", ddf.format(rt.getUntilDate()));
        JsonArrayBuilder acss = Json.createArrayBuilder();
        addEvents(term, rt, null, jevents, aggregate, null, rt.getUniqueId());
        rto.add("events", jevents);
        ReservedTimeInfo reservedTimeInfo = (ReservedTimeInfo) rt;
        JsonObject obj = rto.build();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        JsonWriter jwriter = Json.createWriter(outStream);
        jwriter.writeObject(obj);
        jwriter.close();
        reservedTimeInfo.setEvent(outStream.toString());

    }

    /**
     * Builds the json string with events  required for the calendar and sets that to the possibleScheduleOption event property
     *
     * @param pso
     */
    public void buildPossibleScheduleEvents(PossibleScheduleOption pso) {
        Date minDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date maxDate = getCalendarUtil().getDateAfterXdays(minDate, 6);
        Date displayDate = term.getEndDate();
        EventAggregateData aggregate = new EventAggregateData(minDate, maxDate, displayDate);
        if (pso.isDiscarded()) {
            return;
        }

        Map<String, List<ActivityOption>> scheduledCourseActivities = new LinkedHashMap<String, List<ActivityOption>>();
        for (ActivityOption activityOption : pso.getActivityOptions()) {
            String key = activityOption.getCourseCd();
            if (scheduledCourseActivities.containsKey(key)) {
                scheduledCourseActivities.get(key).add(activityOption);
            } else {
                List<ActivityOption> activityOptions = new ArrayList<ActivityOption>();
                activityOptions.add(activityOption);
                scheduledCourseActivities.put(key, activityOptions);
            }
        }

        JsonObjectBuilder jpso = Json.createObjectBuilder();
        JsonArrayBuilder jevents = Json.createArrayBuilder();
        jpso.add("uniqueId", pso.getUniqueId());
        jpso.add("selected", pso.isSelected());
        if (StringUtils.hasText(pso.getId())) {
            jpso.add("id", pso.getId());
        }

        /*Defaultng to 8:00Am*/
        Calendar defaultStart = Calendar.getInstance();
        defaultStart.set(defaultStart.get(Calendar.YEAR), defaultStart.get(Calendar.MONTH), defaultStart.get(Calendar.DATE), 10, 0);

        /*Defaulting to 4:00Pm*/
        Calendar defaultEnd = Calendar.getInstance();
        defaultEnd.set(defaultEnd.get(Calendar.YEAR), defaultEnd.get(Calendar.MONTH), defaultEnd.get(Calendar.DATE), 16, 0);


        boolean weekend = false;
        boolean tbd = false;
        for (ActivityOption ao : pso.getActivityOptions()) {
            if (!ao.isPrimary() || !ao.isEnrollmentGroup()) {
                for (ClassMeetingTime meeting : ao.getClassMeetingTimes()) {
                    if (!weekend) {
                        weekend = meeting.isSaturday() || meeting.isSunday();
                    }
                    if (!tbd) {
                        tbd = meeting.isArranged();
                    }
                    if (meeting.isArranged()) {
                        Calendar startCal = Calendar.getInstance();
                        startCal.setTime(meeting.getStartDate());
                        Calendar endCal = Calendar.getInstance();
                        endCal.setTime(meeting.getUntilDate());

                        DateTimeComparator comparator = DateTimeComparator.getTimeOnlyInstance();
                        if (comparator.compare(startCal.getTime(), defaultStart.getTime()) == -1) {
                            defaultStart.set(startCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), startCal.get(Calendar.DATE), startCal.get(Calendar.HOUR), startCal.get(Calendar.MINUTE));
                        }
                        if (comparator.compare(endCal.getTime(), defaultEnd.getTime()) == 1) {
                            defaultEnd.set(endCal.get(Calendar.YEAR), startCal.get(Calendar.MONTH), endCal.get(Calendar.DATE), endCal.get(Calendar.HOUR), endCal.get(Calendar.MINUTE));
                        }

                        addEvents(term, meeting, ao, jevents, aggregate, scheduledCourseActivities, pso.getUniqueId());
                    } else {
                        /*Creating minimal event for TBD activities*/
                        JsonObjectBuilder event = Json.createObjectBuilder();
                        event.add("id", pso.getUniqueId());
                        event.add("courseCd", ao.getCourseCd());
                        event.add("courseId", ao.getCourseId());
                        event.add("courseTitle", ao.getCourseTitle());
                        event.add("sectionCd", ao.getRegistrationCode());
                        event.add("tbd", true);
                        jevents.add(event);
                    }
                }
            }
        }


        jpso.add("events", jevents);
        PossibleScheduleOptionInfo possibleScheduleOptionInfo = (PossibleScheduleOptionInfo) pso;
        JsonObject obj = jpso.build();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        JsonWriter jwriter = Json.createWriter(outStream);
        jwriter.writeObject(obj);
        jwriter.close();
        possibleScheduleOptionInfo.setEvent(outStream.toString());
        possibleScheduleOptionInfo.setMinTime(defaultStart.getTime().getTime());
        possibleScheduleOptionInfo.setMaxTime(defaultEnd.getTime().getTime());
        possibleScheduleOptionInfo.setWeekend(weekend);
        possibleScheduleOptionInfo.setTbd(tbd);

    }


    /**
     * Adds events for given params
     *
     * @param term
     * @param meeting
     * @param ao
     * @param jevents
     * @param aggregate
     * @param scheduledCourseActivities
     * @param parentUniqueId
     */
    public void addEvents(Term term, ScheduleBuildEvent meeting, ActivityOption ao, JsonArrayBuilder jevents, EventAggregateData aggregate, Map<String, List<ActivityOption>> scheduledCourseActivities, String parentUniqueId) {
        /**
         * This is used to adjust minDate and maxDate which is a week from min date and adjust min date to be monday if it is not.
         * Used in building a week worth of schedules instead of whole term.
         * */
        Date termStartDate = getCalendarUtil().getNextMonday(term.getStartDate());
        Date termEndDate = getCalendarUtil().getDateAfterXdays(termStartDate, 5);
        Date meetingStartDate = getCalendarUtil().getNextMonday(meeting.getStartDate());
        Date until = getCalendarUtil().getNextMonday(meeting.getStartDate());
        until.setTime(meeting.getUntilDate().getTime());
        Date meetingEndDate = getCalendarUtil().getDateAfterXdays(until, 5);

        Date startDate = aggregate.getDatePortion(meetingStartDate);
        if (startDate == null || startDate.before(termStartDate))
            startDate = aggregate.getDatePortion(termStartDate);
        aggregate.addBreakDate(startDate);

        Date untilDate = aggregate.getDatePortion(meetingEndDate);
        if (untilDate == null || untilDate.after(termEndDate))
            untilDate = aggregate.getDatePortion(termEndDate);
        aggregate.updateLastUntilDate(untilDate);

        aggregate.updateWeekends(meeting.isSunday() || meeting.isSaturday());

        Date eventStart = aggregate.getTimePortion(meetingStartDate);
        long durationSeconds;
        if (meeting.isAllDay())
            durationSeconds = 0L;
        else {
            Date eventEnd = aggregate.getTimePortion(meetingEndDate);
            durationSeconds = (eventEnd.getTime() - eventStart.getTime()) / 1000;
            aggregate.updateMinMaxTime(eventStart, eventEnd);
        }

        while (!startDate.after(untilDate)) {
            if (meeting.isSunday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.SUNDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId));
            if (meeting.isMonday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.MONDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId));
            if (meeting.isTuesday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.TUESDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId));
            if (meeting.isWednesday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.WEDNESDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId));
            if (meeting.isThursday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.THURSDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId));
            if (meeting.isFriday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.FRIDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId));
            if (meeting.isSaturday())
                jevents.add(createEvent(startDate, eventStart, aggregate.cal,
                        Calendar.SATURDAY, durationSeconds, ao,
                        scheduledCourseActivities, parentUniqueId));
            startDate = aggregate.addOneWeek(startDate);
        }
    }

    /**
     * Builds the actual Json events required for calendar
     *
     * @param startDate
     * @param eventStart
     * @param cal
     * @param dow
     * @param durationSeconds
     * @param ao
     * @param scheduledCourseActivities
     * @param parentUniqueId
     * @return
     */
    private JsonObjectBuilder createEvent(Date startDate,
                                          Date eventStart, Calendar cal, int dow, long durationSeconds,
                                          ActivityOption ao, Map<String, List<ActivityOption>> scheduledCourseActivities, String parentUniqueId) {

        // Calculate the date for the event in seconds since the epoch
        cal.setTime(startDate);
        cal.add(Calendar.DATE, dow - cal.get(Calendar.DAY_OF_WEEK));
        long eventStartSeconds = cal.getTime().getTime() / 1000L;

        // Add the time for the event in seconds since midnight GMT
        cal.setTime(eventStart);
        eventStartSeconds += cal.getTime().getTime() / 1000L;

        // Adjust for time zone by subtracting midnight prior to the event.
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        eventStartSeconds -= cal.getTime().getTime() / 1000L;

        JsonObjectBuilder event = Json.createObjectBuilder();
        /*using parentUniqueId for UI purpose of hiding and showing the events based on scheduleId*/
        event.add("id", parentUniqueId);
        event.add("tbd", false);
        /*Title value is populated in JS because we dont know what is the index value of the possibleSchedule*/
        event.add("title", "");
        event.add("start", eventStartSeconds);
        if (durationSeconds == 0) {
            event.add("allDay", true);
        } else {
            event.add("allDay", false);
            event.add("end", eventStartSeconds + durationSeconds);
        }
        if (ao != null) {
            JsonObjectBuilder popoverEvent = Json.createObjectBuilder();
            popoverEvent.add("courseCd", ao.getCourseCd());
            popoverEvent.add("courseId", ao.getCourseId());
            popoverEvent.add("courseTitle", ao.getCourseTitle().trim());

            List<ActivityOption> activityOptions = scheduledCourseActivities.get(ao.getCourseCd());

            JsonArrayBuilder activityArray = Json.createArrayBuilder();
            for (ActivityOption activityOption : activityOptions) {
                JsonObjectBuilder activity = Json.createObjectBuilder();
                activity.add("sectionCd", activityOption.getRegistrationCode());
                JsonArrayBuilder meetingArray = Json.createArrayBuilder();
                JsonObjectBuilder meeting = Json.createObjectBuilder();
                for (ClassMeetingTime meetingTime : activityOption.getClassMeetingTimes()) {
                    meeting.add("meetingTime", meetingTime.getDaysAndTimes());
                    meeting.add("location", meetingTime.getLocation());
                    String campus = meetingTime.getCampus();
                    String building = "";
                    String buildingUrl = "";
                    if (meetingTime.getBuilding() != null) {
                        if (!"NOC".equals(meetingTime.getBuilding()) && !meetingTime.getBuilding().startsWith("*") && campus.equalsIgnoreCase("seattle")) {
                            building = meetingTime.getBuilding();
                            buildingUrl = PlanConstants.BUILDING_URL + building;
                        } else {
                            building = meetingTime.getBuilding();
                        }
                    }
                    meeting.add("building", building);
                    meeting.add("buildingUrl", buildingUrl);
                    meetingArray.add(meeting);
                }

                activity.add("meetings", meetingArray);
                activityArray.add(activity);
            }


            popoverEvent.add("activities", activityArray);
            popoverEvent.add("termId", ao.getTermId());
            event.add("popoverContent", popoverEvent);
        }
        return event;
    }

    public CalendarUtil getCalendarUtil() {
        if (calendarUtil == null) {
            calendarUtil = KsapFrameworkServiceLocator.getCalendarUtil();
        }
        return calendarUtil;
    }

    public void setCalendarUtil(CalendarUtil calendarUtil) {
        this.calendarUtil = calendarUtil;
    }

    public CourseHelper getCourseHelper() {
        if (courseHelper == null) {
            courseHelper = KsapFrameworkServiceLocator.getCourseHelper();
        }
        return courseHelper;
    }

    public void setCourseHelper(CourseHelper courseHelper) {
        this.courseHelper = courseHelper;
    }
}
