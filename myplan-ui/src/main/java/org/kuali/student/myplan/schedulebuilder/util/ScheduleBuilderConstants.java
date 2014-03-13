package org.kuali.student.myplan.schedulebuilder.util;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.kuali.student.myplan.comment.CommentConstants;

/**
 * Created by hemanthg on 2/13/14.
 */
public class ScheduleBuilderConstants {

    public static final String LEARNING_PLAN_KEY = "learningPlanId";

    public static final String INVALID_SAVED_SCHEDULE = "myplan.text.error.scheduleBuilder.invalid.course";
    public static final String INVALID_SAVED_SCHEDULE_ACTIVITY = "myplan.text.error.scheduleBuilder.invalid.activities";
    public static final String SAVED_SCHEDULE_ACTIVITY_MOVED = "myplan.text.error.scheduleBuilder.moved.activity";

    public static final int POSSIBLE_SCHEDULES_MAX_COUNT = ConfigContext.getCurrentContextConfig().getProperty("ks.possible.schedules.count") != null ? Integer.parseInt(ConfigContext.getCurrentContextConfig().getProperty("ks.possible.schedules.count")) : 1000;
}
