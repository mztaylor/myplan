package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.student.myplan.comment.CommentConstants;

/**
 * Created by hemanthg on 2/13/14.
 */
public class ScheduleBuilderConstants {

    public static final String LEARNING_PLAN_KEY = "learningPlanId";

    public static final String INVALID_PINNED_SCHEDULE = "myplan.text.error.scheduleBuilder.invalid.pinned.schedule";
    public static final String VALID_PINNED_SCHEDULE_WITHDRAWN = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.withdrawn";
    public static final String VALID_PINNED_SCHEDULE_TIME_CHANGED = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.time.change";
    public static final String VALID_PINNED_SCHEDULE_RESERVED_CONFLICT = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.reserved.conflict";
    public static final String CLOSED_FILTER = "CLOSED";
    public static final String USER_SELECTED_FILTER = "USER_SELECTED";
    public static final String RESTRICTION_FILTER = "RESTRICTION";
    public static final String OVERLAPPED_FILTER = "OVERLAPPED";
    public static final String OTHER_INSTITUTE_FILTER = "OTHER_INSTITUTE";


    public static final String ZERO_RESULTS_REASON_CLOSED = "Have closed sections";
    public static final String ZERO_RESULTS_REASON_RESTRICTION = "Have enrollment restrictions";
    public static final String ZERO_RESULTS_REASON_OVERLAPPED = "Overlap with my registered courses";
    public static final String ZERO_RESULTS_REASON_OTHER_INSTITUTES = "Are UW PCE courses";

    public static final String PINNED_SCHEDULES_PASSIVE_ERROR = "passive_error";
    public static final String PINNED_SCHEDULES_MODAL_ERROR = "modal_error";

    public static final String PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN = "Withdrawn";
    public static final String PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED = "Meeting_Time_Changed";
    public static final String PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED = "Conflicts_Reserved_Time";
    public static final String PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES = "Secondaries_Unavailable";

    public static final String REGULAR_INSTITUTE_CODE = "0";
    public static final String PCE_INSTITUTE_CODE = "95";
    public static final String ROTC_INSTITUTE_CODE = "88";

    public static final String PCE_INSTITUTE_NAME = "PCE";
    public static final String ROTC_INSTITUTE_NAME = "ROTC";


    public static final int POSSIBLE_SCHEDULES_MAX_COUNT = ConfigContext.getCurrentContextConfig().getProperty("ks.possible.schedules.count") != null ? Integer.parseInt(ConfigContext.getCurrentContextConfig().getProperty("ks.possible.schedules.count")) : 1000;
}
