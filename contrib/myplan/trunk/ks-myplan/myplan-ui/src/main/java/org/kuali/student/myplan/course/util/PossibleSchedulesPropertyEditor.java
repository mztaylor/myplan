package org.kuali.student.myplan.course.util;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 2/8/14.
 */
public class PossibleSchedulesPropertyEditor extends PropertyEditorSupport {
    private final static Logger logger = Logger.getLogger(PossibleSchedulesPropertyEditor.class);

    private boolean savedSchedule;

    public boolean isSavedSchedule() {
        return savedSchedule;
    }

    public void setSavedSchedule(boolean savedSchedule) {
        this.savedSchedule = savedSchedule;
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

    @Override
    public String getAsText() {
        StringBuffer sb = new StringBuffer();
        PossibleScheduleOption pso = (PossibleScheduleOption) super.getValue();
        Map<String, List<String>> scheduledCourseActivities = new LinkedHashMap<String, List<String>>();
        for (ActivityOption activityOption : pso.getActivityOptions()) {
            String key = activityOption.getCourseCd();
            if (scheduledCourseActivities.containsKey(key)) {
                scheduledCourseActivities.get(key).add(activityOption.getRegistrationCode());
            } else {
                List<String> activityOptions = new ArrayList<String>();
                activityOptions.add(activityOption.getRegistrationCode());
                scheduledCourseActivities.put(key, activityOptions);
            }
        }
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

}
