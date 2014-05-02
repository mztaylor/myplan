package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.student.myplan.schedulebuilder.infc.AoListProcessor;


/**
 * Abstract implementaion of AoListProcessor that adds a few more methods and
 * adds default implementation.
 * Expect this to be extended by filter AoListProcessors especially if they
 * want to have a count of AO that are mutated.
 *
 */
public abstract class AbstractAoListProcessor implements AoListProcessor {
    // count is provided to track the number of AO that are affected by the processor;
    // e.g., for a filter processor, it counts the number of AO that are deleted.
    // the processor can decide what is worth counting.  Use is optional.
    private int count;

    // return a code that identifies the processor, presumably defined elsewhere. Optional.
    private int processorCode;

    // return a string that describes the processor, presumably to bubble up to a UI somewhere
    // so user can see something like "17 sections unavailable because they are closed". Optional.
    private String processorDescription;

    @Override
    public void resetCount() {
        count = 0;
    }

    @Override
    public int incCount() {
        return ++count;
    }

     @Override
     public int incCount(int addend) {
        count += addend;
        return count;
    }

    @Override
    public int getCount() {
        return count;
    }

    @Override
    public void setCount(int count) {
        this.count = count;
    }

    @Override
    public String getProcessorDescription() {
        return processorDescription;
    }

    @Override
    public void setProcessorDescription(String processorDescription) {
        this.processorDescription = processorDescription;
    }

    @Override
    public int getProcessorCode() {
        return processorCode;
    }

    @Override
    public void setProcessorCode(int processorCode) {
        this.processorCode = processorCode;
    }
}
