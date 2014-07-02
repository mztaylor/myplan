package org.kuali.student.myplan.schedulebuilder.util;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;
import org.kuali.rice.core.api.util.ConcreteKeyValue;
import org.kuali.rice.core.api.util.KeyValue;
import org.kuali.rice.krad.uif.control.UifKeyValuesFinderBase;
import org.kuali.rice.krad.uif.field.InputField;
import org.kuali.rice.krad.uif.util.ObjectPropertyUtils;
import org.kuali.rice.krad.uif.view.ViewModel;
import org.kuali.student.myplan.schedulebuilder.infc.ActivityOption;
import org.kuali.student.myplan.schedulebuilder.infc.ClassMeetingTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 4/21/2014.
 */
public class RegistrationKeyValueFinder extends UifKeyValuesFinderBase {
    /**
     * @see org.kuali.rice.krad.uif.control.UifKeyValuesFinder#getKeyValues(org.kuali.rice.krad.uif.view.ViewModel, org.kuali.rice.krad.uif.field.InputField)
     */

    @Override
    public List<KeyValue> getKeyValues(ViewModel model, InputField field) {

        List<KeyValue> keyValues = new ArrayList<KeyValue>();
        Map<String, String> plannedActivities = ObjectPropertyUtils.getPropertyValue(model, "plannedItems");
        ActivityOption activityOption = ObjectPropertyUtils.getPropertyValue(model, field.getBindingInfo().getBindByNamePrefix());
        keyValues.add(new ConcreteKeyValue(activityOption.getRegistrationCode(), buildTemplate(activityOption, new ArrayList<String>(plannedActivities.keySet()))));
        for (ActivityOption alternate : activityOption.getAlternateActivties()) {
            keyValues.add(new ConcreteKeyValue(alternate.getRegistrationCode(), buildTemplate(alternate, new ArrayList<String>(plannedActivities.keySet()))));
        }
        setAddBlankOption(false);
        return keyValues;
    }


    /**
     * Build template method builds the label for the radio buttons as Html...
     * <p/>
     * Example Template:
     * <p/>
     * <div class="registrationActivity__code">C</div>
     * <div class="registrationActivity__credits">(5)</div>
     * <div class="registrationActivity__meetingDays">MWF<br>Th</div>
     * <div class="registrationActivity__meetingTime">10:30 AM - 11:20 AM<br>5:00 PM - 6:20 PM</div>
     * <div class="registrationActivity__meetingLocation">PAA A102<br>SMI 120</div>
     * <div class="registrationActivity__regCode">19528</div>
     * <div class="registrationActivity__instituteCode registrationActivity__instituteCode--hide"></div>
     * <div class="registrationActivity__enrollRest registrationActivity__enrollRest--false"><img src="../themes/ksap/images/pixel.gif"></div>
     * <div class="registrationActivity__enrollState"><strong>0</strong> / 220</div>
     * <div class="registrationActivity__enrollStatus registrationActivity__enrollStatus--open">Open</div>
     *
     * @param activityOption
     * @param plannedActivityIds
     * @return
     */
    private String buildTemplate(ActivityOption activityOption, List<String> plannedActivityIds) {
        List<String> meetingDays = new ArrayList<String>();
        List<String> meetingTimes = new ArrayList<String>();
        List<String> meetingLocations = new ArrayList<String>();
        List<String> meetingInstructors = new ArrayList<String>();
        boolean tbd = false;
        for (ClassMeetingTime classMeetingTime : activityOption.getClassMeetingTimes()) {
            if (!classMeetingTime.isArranged()) {
                tbd = true;
            }
            meetingDays.add(StringUtils.join(classMeetingTime.getDays(), ""));
            meetingTimes.add(classMeetingTime.getTimes());
            meetingInstructors.add(classMeetingTime.getInstructorName() != null ? classMeetingTime.getInstructorName() : " ");
            meetingLocations.add(String.format("%s %s", classMeetingTime.getBuilding() != null ? classMeetingTime.getBuilding() : "", classMeetingTime.getLocation() != null ? classMeetingTime.getLocation() : ""));
        }
        String instituteCd = "";
        if (ScheduleBuilderConstants.PCE_INSTITUTE_CODE.equals(activityOption.getInstituteCode())) {
            instituteCd = ScheduleBuilderConstants.PCE_INSTITUTE_NAME;
        } else if (ScheduleBuilderConstants.ROTC_INSTITUTE_CODE.equals(activityOption.getInstituteCode())) {
            instituteCd = ScheduleBuilderConstants.ROTC_INSTITUTE_NAME;
        }

        List<String> cssClasses = new ArrayList<String>();
        cssClasses.add("registrationActivity__code");
        if (plannedActivityIds.contains(activityOption.getActivityOfferingId())) {
            cssClasses.add("registrationActivity__sectionCd--planned");
        }
        String template = "<div class=\"" + StringUtils.join(cssClasses, " ") + "\">" + activityOption.getActivityCode() + "</div>";

        template = template +
                "<div class=\"registrationActivity__regCode\">" + activityOption.getRegistrationCode() + "</div>" +
                "<div class=\"registrationActivity__type\">" + WordUtils.capitalize(activityOption.getActivityTypeDescription()) + (activityOption.isPrimary() ? " (" + activityOption.getCourseCredit() + ")" : "") + "</div>";

        if (tbd) {
            template = template +
                    "<div class=\"registrationActivity__meetingDays\">&nbsp;</div>" +
                    "<div class=\"registrationActivity__tbd\">To be arranged</div>";
        } else {
            template = template +
                    "<div class=\"registrationActivity__meetingDays\">" + StringUtils.join(meetingDays, "<br/>") + "</div>" +
                    "<div class=\"registrationActivity__meetingTime\">" + StringUtils.join(meetingTimes, "<br/>") + "</div>";
        }

        template = template +
                "<div class=\"registrationActivity__meetingLocation\">" + StringUtils.join(meetingLocations, "<br/>") + "</div>" +
                "<div class=\"registrationActivity__instructor\">" + StringUtils.join(meetingInstructors, "<br/>") + "</div>" +
                "<div class=\"registrationActivity__instituteCode registrationActivity__instituteCode--" + (!instituteCd.isEmpty() ? "show" : "hide") + "\">" + instituteCd + "</div>" +
                "<div class=\"registrationActivity__enrollRest registrationActivity__enrollRest--" + String.valueOf(activityOption.isEnrollmentRestriction()) + "\">" + "<img src=\"../themes/ksap/images/pixel.gif\"/>" + "</div>" +
                "<div class=\"registrationActivity__enrollState\">" + String.format("<strong>%s</strong> / %s", activityOption.getFilledSeats(), activityOption.getTotalSeats()) + "</div>" +
                "<div class=\"registrationActivity__enrollStatus registrationActivity__enrollStatus--" + activityOption.getEnrollStatus().toLowerCase() + "\">" + activityOption.getEnrollStatus() + "</div>";
        return template;
    }

}
