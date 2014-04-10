package org.kuali.student.myplan.course.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.ClassMeetingTime;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.SecondaryActivityOptions;
import org.kuali.student.myplan.utils.TimeStringMillisConverter;
import org.springframework.util.CollectionUtils;

import java.beans.PropertyEditorSupport;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by hemanthg on 2/8/14.
 */
public class PossibleSchedulesPropertyEditor extends PropertyEditorSupport {
    private final static Logger logger = Logger.getLogger(PossibleSchedulesPropertyEditor.class);

    private boolean savedSchedule;

    private boolean tbdSchedule;

    public boolean isSavedSchedule() {
        return savedSchedule;
    }

    public void setSavedSchedule(boolean savedSchedule) {
        this.savedSchedule = savedSchedule;
    }

    public boolean isTbdSchedule() {
        return tbdSchedule;
    }

    public void setTbdSchedule(boolean tbdSchedule) {
        this.tbdSchedule = tbdSchedule;
    }

    @Override
    public void setValue(Object value) {
        if (value == null) {
            logger.error("PossibleScheduleDetails was null");
            return;
        }
        super.setValue(value);
    }

    String innerSpan = "<div class=\"\">";

    /**
     * Consider the possible schedule has following
     * Registered courses : COM 201 A
     * Planned Courses: COM 202 A, COM 202 AB, COM 203 AJ(Assume this is a TBD), COM 204 A(Assume this also is a TBD)
     * <p/>
     * For following scenarios considering the above assumption:
     * savedSchedule = false && tbdSchedule = false then the string displayed would be
     * <div>COM 201 A</div>
     * <div>COM 202</div>
     * <div>COM 203</div>
     * <div>COM 204</div>
     * <p/>
     * <p/>
     * savedSchedule = true && tbdSchedule = false then the string displayed would be
     * <p/>
     * <div>COM 201 A / COM 202 / COM 203 / COM 204</div>
     * <p/>
     * <p/>
     * savedSchedule = false && tbdSchedule = true then the string displayed would be
     * <div>COM 203 AJ</div>
     * <div>COM 204 A</div>
     *
     * @return HTML string of possible course options
     */
    @Override
    public String getAsText() {
        StringBuffer sb = new StringBuffer();
        PossibleScheduleOption pso = (PossibleScheduleOption) super.getValue();
        Map<String, List<String>> scheduledCourseActivities = new LinkedHashMap<String, List<String>>();
        processActivities(pso.getActivityOptions(), scheduledCourseActivities);
        if (isSavedSchedule()) {
            sb = sb.append(innerSpan);
        }
        int count = 0;
        for (String key : scheduledCourseActivities.keySet()) {
            count++;
            String value = StringUtils.join(scheduledCourseActivities.get(key), ", ");
            sb = sb.append(isSavedSchedule() ? "" : innerSpan).append("<b>").append(key).append("</b>").append(" ").append(value).append(isSavedSchedule() ? count < scheduledCourseActivities.size() ? " / " : "" : "</div>");
        }
        sb = sb.append(isSavedSchedule() ? "</div>" : "");

        return sb.toString();
    }

    protected void processActivities(List<ActivityOption> activityOptionList, Map<String, List<String>> scheduledCourseActivities) {
        for (ActivityOption activityOption : activityOptionList) {

            for (SecondaryActivityOptions secondaryActivityOption : activityOption.getSecondaryOptions()) {
                processActivities(secondaryActivityOption.getActivityOptions(), scheduledCourseActivities);
            }

            processActivities(activityOption.getAlternateActivties(), scheduledCourseActivities);

            String key = activityOption.getCourseCd();
            boolean isArranged = true;
            if (isTbdSchedule()) {
                for (ClassMeetingTime classMeetingTime : activityOption.getClassMeetingTimes()) {
                    if (isArranged && !classMeetingTime.isArranged()) {
                        isArranged = classMeetingTime.isArranged();
                        break;
                    }
                }
            }
            if (scheduledCourseActivities.containsKey(key)) {
                if (activityOption.isLockedIn()) {
                    scheduledCourseActivities.get(key).add(activityOption.getActivityCode());
                }
            } else {
                List<String> activityOptions = new ArrayList<String>();
                if (activityOption.isLockedIn()) {
                    activityOptions.add(activityOption.getActivityCode());
                }
                if (isSavedSchedule() && !CollectionUtils.isEmpty(activityOption.getClassMeetingTimes())) {
                    Set<String> times = new HashSet<String>();
                    for (ClassMeetingTime classMeetingTime : activityOption.getClassMeetingTimes()) {
                        if (classMeetingTime.isArranged()) {
                            times.add(TimeStringMillisConverter.millisToStandardTime(classMeetingTime.getStartDate().getTime(), "h:mm a"));
                        } else {
                            times.add("TBA");
                        }
                    }
                    activityOptions.add(StringUtils.join(times, " & "));
                }
                if ((isTbdSchedule() && !isArranged) || !isTbdSchedule()) {
                    scheduledCourseActivities.put(key, activityOptions);
                }
            }
        }
    }

}
