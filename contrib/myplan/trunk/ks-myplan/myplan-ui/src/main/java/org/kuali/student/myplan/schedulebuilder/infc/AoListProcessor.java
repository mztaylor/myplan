package org.kuali.student.myplan.schedulebuilder.infc;

import java.util.List;

/**
 * interface for a class that will process a list of ActivityOptions
 * as part of ScheduleBuilder. Processors might be filters, coalescers,
 * or execute other tasks against an entire list of AO as needed.
 *
 *
 */
public interface AoListProcessor {
    List<ActivityOption> apply(List<ActivityOption> aoList);
}
