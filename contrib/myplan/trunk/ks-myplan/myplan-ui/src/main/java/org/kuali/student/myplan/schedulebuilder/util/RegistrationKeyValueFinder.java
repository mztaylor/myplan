package org.kuali.student.myplan.schedulebuilder.util;

import org.apache.commons.lang.StringUtils;
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

        ActivityOption activityOption = ObjectPropertyUtils.getPropertyValue(model, field.getBindingInfo().getBindByNamePrefix());
        keyValues.add(new ConcreteKeyValue(activityOption.getRegistrationCode(), buildTemplate(activityOption)));
        for (ActivityOption alternate : activityOption.getAlternateActivties()) {
            keyValues.add(new ConcreteKeyValue(alternate.getRegistrationCode(), buildTemplate(alternate)));
        }
        setAddBlankOption(false);
        return keyValues;
    }


    private String buildTemplate(ActivityOption activityOption) {
        List<String> meetingDays = new ArrayList<String>();
        List<String> meetingTimes = new ArrayList<String>();
        List<String> meetingLocations = new ArrayList<String>();
        boolean tbd = false;
        for (ClassMeetingTime classMeetingTime : activityOption.getClassMeetingTimes()) {
            if (!classMeetingTime.isArranged()) {
                tbd = true;
            }
            meetingDays.add(StringUtils.join(classMeetingTime.getDays(), ""));
            meetingTimes.add(classMeetingTime.getTimes());
            meetingLocations.add(classMeetingTime.getLocation());
        }
        String instituteCd = "";
        if (ScheduleBuilderConstants.PCE_INSTITUTE_CODE.equals(activityOption.getInstituteCode())) {
            instituteCd = ScheduleBuilderConstants.PCE_INSTITUTE_NAME;
        } else if (ScheduleBuilderConstants.ROTC_INSTITUTE_CODE.equals(activityOption.getInstituteCode())) {
            instituteCd = ScheduleBuilderConstants.ROTC_INSTITUTE_NAME;
        }

        String template = "<div class=\"registrationActivity__code\">" + activityOption.getActivityCode() + "</div>";

        if (tbd) {
            template = template + "<div class=\"registrationActivity__tbd\"> To be arranged </div>";
        } else {
            template = template + "<div class=\"registrationActivity__meetingDays\">" + StringUtils.join(meetingDays, "<br/>") + "</div>" +
                    "<div class=\"registrationActivity__meetingTime\">" + StringUtils.join(meetingTimes, "<br/>") + "</div>" +
                    "<div class=\"registrationActivity__meetingLocation\">" + StringUtils.join(meetingLocations, "<br/>") + "</div>";
        }

        template = template + "<div class=\"registrationActivity__regCode\">" + activityOption.getRegistrationCode() + "</div>" +
                "<div class=\"registrationActivity__instituteCode\">" + instituteCd + "</div>" +
                "<div class=\"registrationActivity__enrollRest registrationActivity__enrollRest--" + String.valueOf(activityOption.isEnrollmentRestriction()) + "\">" + "<img src=\"../themes/ksap/images/pixel.gif\"/>" + "</div>" +
                "<div class=\"registrationActivity__enrollState\">" + String.format("%s/%s", activityOption.getFilledSeats(), activityOption.getTotalSeats()) + "</div>" +
                "<div class=\"registrationActivity__enrollStatus registrationActivity__enrollStatus--closed\">" + activityOption.getEnrollStatus() + "</div>";
        return template;
    }

}
