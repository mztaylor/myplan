package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.schedulebuilder.dto.ScheduleBuildFiltersInfo;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildFilters;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleForm;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemanthg on 2/26/14.
 */
public class DefaultScheduleForm extends UifFormBase implements ScheduleForm {
    private Term term;
    private String requestedLearningPlanId;
    private List<ReservedTime> reservedTimes;
    private ScheduleBuildFilters buildFilters;
    private Integer removeReserved;
    private boolean includeClosed;
    private boolean overload;
    private String termId;
    private boolean weekend;
    private long minTime;
    private long maxTime;
    private boolean tbd;

    private TermHelper termHelper;
    private ScheduleBuildStrategy scheduleBuildStrategy;

    @Override
    public void reset() {
        overload = false;
        includeClosed = false;

        validateAndGenerateTerm();

        ScheduleBuildStrategy strategy = getScheduleBuildStrategy();
        try {
            reservedTimes = strategy.getReservedTimes(requestedLearningPlanId);
            ScheduleBuilder builder = new ScheduleBuilder(term, null, null, null, null);
            for (ReservedTime reservedTime : reservedTimes) {
                builder.buildReservedTimeEvents(reservedTime);
            }
        } catch (PermissionDeniedException e) {
            throw new IllegalArgumentException("Course options not permitted for requested learning plan", e);
        }

    }

    /**
     * Validates given TermId and sets the Term associated to given termId
     */
    protected void validateAndGenerateTerm() {
        TermHelper th = getTermHelper();
        if (getTermId() == null)
            throw new IllegalArgumentException("Missing term ID");
        if (th.isCompleted(getTermId()))
            throw new IllegalArgumentException("Term " + getTermId()
                    + " has already been completed.");
        if (!th.isPlanning(getTermId()))
            throw new IllegalArgumentException("Term " + getTermId()
                    + " is no longer open for planning.");
        if (getTermId() == null)
            throw new IllegalArgumentException("Missing term ID");


        setTerm(null);
        StringBuilder pubs = new StringBuilder();
        for (Term t : th.getOfficialTerms()) {
            pubs.append(" ").append(t.getId());
            if (t.getId().equals(termId)) {
                setTerm(t);
                break;
            }
        }

        if (getTerm() == null)
            throw new IllegalArgumentException("Term " + termId
                    + " is not currently published." + pubs);
    }

    @Override
    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    @Override
    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    @Override
    public String getRequestedLearningPlanId() {
        return requestedLearningPlanId;
    }

    public void setRequestedLearningPlanId(String requestedLearningPlanId) {
        this.requestedLearningPlanId = requestedLearningPlanId;
    }

    @Override
    public boolean isWeekend() {
        return weekend;
    }

    public void setWeekend(boolean weekend) {
        this.weekend = weekend;
    }

    @Override
    public long getMinTime() {
        return minTime;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    @Override
    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    @Override
    public boolean isTbd() {
        return tbd;
    }

    public void setTbd(boolean tbd) {
        this.tbd = tbd;
    }

    @Override
    public List<ReservedTime> getReservedTimes() {
        if (reservedTimes == null) {
            reservedTimes = new ArrayList<ReservedTime>();
        }
        return reservedTimes;
    }

    public void setReservedTimes(List<ReservedTime> reservedTimes) {
        this.reservedTimes = reservedTimes;
    }

    @Override
    public ScheduleBuildFilters getBuildFilters() {
        if (buildFilters == null) {
            buildFilters = new ScheduleBuildFiltersInfo();
        }
        return buildFilters;
    }

    public void setBuildFilters(ScheduleBuildFilters buildFilters) {
        this.buildFilters = buildFilters;
    }

    @Override
    public boolean isIncludeClosed() {
        return includeClosed;
    }

    public void setIncludeClosed(boolean includeClosed) {
        this.includeClosed = includeClosed;
    }

    @Override
    public Integer getRemoveReserved() {
        return removeReserved;
    }

    public void setRemoveReserved(Integer removeReserved) {
        this.removeReserved = removeReserved;
    }

    public boolean isOverload() {
        return overload;
    }

    public void setOverload(boolean overload) {
        this.overload = overload;
    }

    public TermHelper getTermHelper() {
        if (termHelper == null) {
            termHelper = KsapFrameworkServiceLocator.getTermHelper();
        }
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
    }

    public ScheduleBuildStrategy getScheduleBuildStrategy() {
        if (scheduleBuildStrategy == null) {
            scheduleBuildStrategy = UwMyplanServiceLocator.getInstance().getScheduleBuildStrategy();
        }
        return scheduleBuildStrategy;
    }

    public void setScheduleBuildStrategy(ScheduleBuildStrategy scheduleBuildStrategy) {
        this.scheduleBuildStrategy = scheduleBuildStrategy;
    }
}
