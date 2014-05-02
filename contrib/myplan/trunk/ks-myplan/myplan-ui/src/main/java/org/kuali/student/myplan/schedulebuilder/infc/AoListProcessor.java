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

    // count is provided to track the number of AO that are affected by the processor;
    // e.g., for a filter processor, it counts the number of AO that are deleted.
    // the processor can decide what is worth counting.  Use is optional.
    void resetCount();

    int incCount();

    int incCount(int addend);

    int getCount();

    void setCount(int count);

    // return a string that describes the processor, presumably to bubble up to a UI somewhere
    // so user can see something like "17 sections unavailable because they are closed". Optional.
    String getProcessorDescription();

    void setProcessorDescription(String processorDescription);

    // return a code that identifies the processor, presumably defined elsewhere. Optional.
    int getProcessorCode();

    void setProcessorCode(int processorCode);
}
