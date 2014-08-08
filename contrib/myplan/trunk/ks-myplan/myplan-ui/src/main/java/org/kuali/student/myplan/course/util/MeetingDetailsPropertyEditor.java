package org.kuali.student.myplan.course.util;

import java.beans.PropertyEditorSupport;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.kuali.student.enrollment.courseoffering.infc.ActivityOffering;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.MeetingDetails;
import org.kuali.student.myplan.plan.PlanConstants;

public class MeetingDetailsPropertyEditor extends PropertyEditorSupport {
    private final static Logger logger = Logger.getLogger(MeetingDetailsPropertyEditor.class);

    public final static String TO_BE_ARRANGED = "To be arranged";

    @Override
    public void setValue(Object value) {
        if (value == null) {
            logger.error("MeetingDetails was null");
            return;
        }
        super.setValue(value);
    }

    /*String template =
         "<div class='meetingdetails'>" +
             "<span class='meetingdays'>%s</span>" +
             "<span class='meetingtime'>%s</span>" +
             "<span class='meetingbuilding'>%s</span>" +
             "<span class='meetingroom'>%s</span>" +
         "</div>";*/


    @Override
    public String getAsText() {
        ActivityOfferingItem activityItem = (ActivityOfferingItem) super.getValue();

        if (activityItem.getMeetingDetailsList() != null && activityItem.getMeetingDetailsList().size() > 0) {
            StringBuilder meeting = new StringBuilder();
            meeting.append("<div class='meetingdetailslist'>");

            for (MeetingDetails m : activityItem.getMeetingDetailsList()) {

                boolean tba = false;

                StringBuilder meetingItem = new StringBuilder();
                meetingItem.append("<div class='meetingdetails'>");

                String days = m.getDays();
                String time = m.getTime();

                // If the days and building are empty, section is TBA
                if ((days == null || days.equals("")) && (time == null || time.equals(""))) {
                    tba = true;
                }

                if (days == null) days = "";
                if (time == null) time = "";

                if (!tba) {
                    meetingItem.append("<span class='meetingdays'>" + days + "</span>");
                    meetingItem.append("<span class='meetingtime'>" + time + "</span>");
                } else {
                    meetingItem.append("<span class='meetingtba'>" + TO_BE_ARRANGED + "</span>");
                }

                meetingItem.append("</div>");

                meeting.append(meetingItem);
            }

            meeting.append("</div>");

            return meeting.toString().replace('\'', '\"');
        } else {
            return "";
        }

    }

}
