package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.MeetingDetails;
import org.kuali.student.myplan.schedulebuilder.dto.ActivityOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ClassMeetingTimeInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.ClassMeetingTime;

import java.beans.PropertyEditorSupport;
import java.util.List;

public class ClassMeetingsPropertyEditor extends PropertyEditorSupport {
    private final static Logger logger = Logger.getLogger(ClassMeetingsPropertyEditor.class);

    @Override
    public void setValue(Object value) {
        if (value == null) {
            logger.error("ClassMeetingDetails was null");
            return;
        }
        super.setValue(value);
    }

    String template =
            "<div class=\"uif-verticalBoxLayout clearfix\">" +
                    "<span class=\"uif-message uif-boxLayoutVerticalItem clearfix ksap-sb-activity ksap-sb-classtimes-daystimes\">%s</span>" +
                    "<span class=\"uif-message uif-boxLayoutVerticalItem clearfix ksap-sb-activity ksap-sb-classtimes-location\">%s</span>" +
                    "</div>";


    @Override
    public String getAsText() {
        StringBuffer sb = new StringBuffer();
        ActivityOption activityOptionInfo = (ActivityOption) super.getValue();
        List<ClassMeetingTime> classMeetingTimeInfos = activityOptionInfo.getClassMeetingTimes();
        for (ClassMeetingTime classMeetingTime : classMeetingTimeInfos) {
            sb = sb.append(String.format(template, !classMeetingTime.isAllDay() ? classMeetingTime.getDaysAndTimes() : "Meeting time to be arranged", String.format("%s %s %s", classMeetingTime.getInstructorName(), classMeetingTime.getInstructorName() != null ? "&bull;" : "", classMeetingTime.isArranged() ? classMeetingTime.getLocation() : "ARR")));
        }
        return sb.toString();
    }

}
