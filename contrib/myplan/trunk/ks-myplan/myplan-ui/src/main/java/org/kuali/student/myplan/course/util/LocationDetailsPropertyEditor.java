package org.kuali.student.myplan.course.util;

import org.apache.log4j.Logger;
import org.kuali.student.myplan.course.dataobject.ActivityOfferingItem;
import org.kuali.student.myplan.course.dataobject.MeetingDetails;
import org.kuali.student.myplan.plan.PlanConstants;

import java.beans.PropertyEditorSupport;

public class LocationDetailsPropertyEditor extends PropertyEditorSupport {
    private final static Logger logger = Logger.getLogger(LocationDetailsPropertyEditor.class);

    @Override
    public void setValue(Object value) {
        if (value == null) {
            logger.error("LocationDetails was null");
            return;
        }
        super.setValue(value);
    }

    @Override
    public String getAsText() {
        ActivityOfferingItem activityItem = (ActivityOfferingItem) super.getValue();

        if (activityItem.getMeetingDetailsList() != null && activityItem.getMeetingDetailsList().size() > 0) {
            StringBuilder location = new StringBuilder();
            location.append("<div class='locationdetailslist'>");

            for (MeetingDetails l : activityItem.getMeetingDetailsList()) {

                StringBuilder locationItem = new StringBuilder();
                locationItem.append("<div class='locationdetails'>");

                String building = l.getBuilding();
                String room = l.getRoom();
                String campus = l.getCampus();


                if (building == null) building = "";
                if (room == null) room = "";
                if (campus == null) campus = "";

                if (!building.equals("NOC") && !building.startsWith("*") && campus.equalsIgnoreCase("seattle")) {
                    locationItem.append("<a href='" + PlanConstants.BUILDING_URL + building.toLowerCase() + "' target='_blank'>" + building + "</a>");
                } else {
                    locationItem.append(building);
                }

                locationItem.append(" " + room);

                locationItem.append("</div>");

                location.append(locationItem);
            }

            location.append("</div>");

            return location.toString().replace('\'', '\"');
        } else {
            return "";
        }

    }

}
