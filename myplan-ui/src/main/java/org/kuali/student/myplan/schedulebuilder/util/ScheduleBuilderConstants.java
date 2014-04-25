package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.student.myplan.comment.CommentConstants;

/**
 * Created by hemanthg on 2/13/14.
 */
public class ScheduleBuilderConstants {

    public static final String LEARNING_PLAN_KEY = "learningPlanId";

    public static final String SCHEDULE_BUILDER_PAGE_ID = "schedule_build";

    public static final String INVALID_PINNED_SCHEDULE = "myplan.text.error.scheduleBuilder.invalid.pinned.schedule";

    public static final String VALID_PINNED_SCHEDULE_WITHDRAWN_SINGLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.withdrawn.single";
    public static final String VALID_PINNED_SCHEDULE_WITHDRAWN_MULTIPLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.withdrawn.multiple";

    public static final String VALID_PINNED_SCHEDULE_CLOSED_SINGLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.closed.single";
    public static final String VALID_PINNED_SCHEDULE_CLOSED_MULTIPLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.closed.multiple";

    public static final String VALID_PINNED_SCHEDULE_SUSPENDED_SINGLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.suspended.single";
    public static final String VALID_PINNED_SCHEDULE_SUSPENDED_MULTIPLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.suspended.multiple";

    public static final String VALID_PINNED_SCHEDULE_ENROLL_RESTR_SINGLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.enroll.restriction.single";
    public static final String VALID_PINNED_SCHEDULE_ENROLL_RESTR_MULTIPLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.enroll.restriction.multiple";

    public static final String VALID_PINNED_SCHEDULE_TIME_CHANGED_SINGLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.time.change.single";
    public static final String VALID_PINNED_SCHEDULE_TIME_CHANGED_MULTIPLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.time.change.multiple";

    public static final String VALID_PINNED_SCHEDULE_RESERVED_CONFLICT_SINGLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.reserved.conflict.single";
    public static final String VALID_PINNED_SCHEDULE_RESERVED_CONFLICT_MULTIPLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.reserved.conflict.multiple";

    public static final String VALID_PINNED_SCHEDULE_REGISTERED_CONFLICT_SINGLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.registered.conflict.single";
    public static final String VALID_PINNED_SCHEDULE_REGISTERED_CONFLICT_MULTIPLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.registered.conflict.multiple";

    public static final String VALID_PINNED_SCHEDULE_NO_SECONDARIES_SINGLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.no.secondaries.single";
    public static final String VALID_PINNED_SCHEDULE_NO_SECONDARIES_MULTIPLE = "myplan.text.error.scheduleBuilder.valid.pinned.schedule.no.secondaries.multiple";


    public static final String CLOSED_FILTER = "CLOSED";
    public static final int    CLOSED_FILTER_ID = 687;  // entirely arbitrary
    public static final String USER_SELECTED_FILTER = "USER_SELECTED";
    public static final int    USER_SELECTED_FILTER_ID = 688;
    public static final String RESTRICTION_FILTER = "RESTRICTIONS";
    public static final int    RESTRICTION_FILTER_ID = 689;
    public static final String OVERLAPPED_FILTER = "OVERLAPPED";
    public static final int    OVERLAPPED_FILTER_FILTER_ID = 700;
    public static final String OTHER_INSTITUTE_FILTER = "OTHER_INSTITUTE";
    public static final int    OTHER_INSTITUTE_FILTER_ID = 701;
    public static final String REGISTERED_UNPLANNED_FILTER_DESC = "Unplanned section for a registered course";
    public static final int    REGISTERED_UNPLANNED_FILTER_ID = 702;

    public static final String SECTION_COALESCOR_PROCESSOR_DESC = "Section coalescor processor";
    public static final int    SECTION_COALESCOR_PROCESSOR_ID = 703;
    public static final String SECTION_ALT_ACTIVITY_SORTER_PROCESSOR_DESC = "Section alternate actibity sorter processor";
    public static final int    SECTION_ALT_ACTIVITY_SORTER_PROCESSOR_ID = 704;

    public static final String AO_MUTATOR_SET_LOCKED_IN = "Set activity option LockedIn";
    public static final int    AO_MUTATOR_SET_LOCKED_IN_ID = 801;  // entirely arbitrary

    public static final String ZERO_RESULTS_REASON_CLOSED = "Are currently closed";
    public static final String ZERO_RESULTS_REASON_RESTRICTION = "Have enrollment restrictions";
    public static final String ZERO_RESULTS_REASON_OVERLAPPED = "Overlap with my registered courses";
    public static final String ZERO_RESULTS_REASON_OTHER_INSTITUTES = "Are UW PCE courses";

    public static final String RESULTS_NOT_POSSIBLE_REASON_NO_DEFINED_SECTIONS = "One or more of your planned courses is not offered this quarter.";

    public static final String PINNED_SCHEDULES_PASSIVE_ERROR = "info";
    public static final String PINNED_SCHEDULES_MODAL_ERROR = "error";
    public static final String PINNED_SCHEDULES_NO_ERROR = "no_error";


    public static final String PINNED_SCHEDULES_ERROR_REASON_WITHDRAWN = "Withdrawn";
    public static final String PINNED_SCHEDULES_ERROR_REASON_CLOSED = "Closed";
    public static final String PINNED_SCHEDULES_ERROR_REASON_SUSPENDED = "Suspended";
    public static final String PINNED_SCHEDULES_ERROR_REASON_ENROLL_RESTR = "EnrollmentRestriction";
    public static final String PINNED_SCHEDULES_ERROR_REASON_TIME_CHANGED = "Meeting_Time_Changed";
    public static final String PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_RESERVED = "Conflicts_Reserved_Time";
    public static final String PINNED_SCHEDULES_ERROR_REASON_CONFLICTS_REGISTERED = "Conflicts_Registered_Time";
    public static final String PINNED_SCHEDULES_ERROR_REASON_NO_SECONDARIES = "Secondaries_Unavailable";
    public static final String PINNED_SCHEDULES_ERROR_REASON_NO_ERROR = "No_Error";

    public static final String REGULAR_INSTITUTE_CODE = "0";
    public static final String PCE_INSTITUTE_CODE = "95";
    public static final String ROTC_INSTITUTE_CODE = "88";

    public static final String PCE_INSTITUTE_NAME = "PCE";
    public static final String ROTC_INSTITUTE_NAME = "ROTC";


    public static final int POSSIBLE_SCHEDULES_MAX_COUNT = ConfigContext.getCurrentContextConfig().getProperty("ks.possible.schedules.count") != null ? Integer.parseInt(ConfigContext.getCurrentContextConfig().getProperty("ks.possible.schedules.count")) : 1000;

    public static final String WARNING_STUDENT_SERVICES_DOWN = "myplan.text.error.technicalProblems";
}
