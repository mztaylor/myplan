package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.ap.framework.config.KsapFrameworkServiceLocator;
import org.kuali.student.ap.framework.context.TermHelper;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.schedulebuilder.dto.PossibleScheduleOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ReservedTimeInfo;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildFilters;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilderConstants;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DefaultScheduleBuildForm extends UifFormBase implements
        ScheduleBuildForm {

    private static final long serialVersionUID = 3274052642980656068L;

    private static final Logger LOG = LoggerFactory
            .getLogger(DefaultScheduleBuildForm.class);

    private int possibleScheduleSize = 10;
    private boolean overload;

    private String termId;
    private String requestedLearningPlanId;
    private boolean includeClosed;
    private boolean more;
    private Integer removeReserved;

    private String uniqueId;

    private Term term;
    private ScheduleBuilder scheduleBuilder;
    private List<CourseOption> courseOptions;
    private List<ReservedTime> reservedTimes;
    private List<PossibleScheduleOption> possibleScheduleOptions;
    private List<PossibleScheduleOption> savedSchedules;
    private ScheduleBuildFilters buildFilters;

    private List<String> includeFilters;

    private ScheduleBuildStrategy scheduleBuildStrategy;

    private TermHelper termHelper;

    private transient AcademicCalendarService academicCalendarService;

    @Override
    public void reset() {
        overload = false;
        includeClosed = false;

        TermHelper th = getTermHelper();
        if (termId == null)
            throw new IllegalArgumentException("Missing term ID");
        if (th.isCompleted(termId))
            throw new IllegalArgumentException("Term " + termId
                    + " has already been completed.");
        if (!th.isPlanning(termId))
            throw new IllegalArgumentException("Term " + termId
                    + " is no longer open for planning.");
        if (termId == null)
            throw new IllegalArgumentException("Missing term ID");


        term = null;
        StringBuilder pubs = new StringBuilder();
        for (Term t : th.getOfficialTerms()) {
            pubs.append(" ").append(t.getId());
            if (t.getId().equals(termId))
                term = t;
        }

        if (term == null)
            throw new IllegalArgumentException("Term " + termId
                    + " is not currently published." + pubs);

        ScheduleBuildStrategy strategy = getScheduleBuildStrategy();
        try {
            /*courseOptions = strategy.getCourseOptions(strategy.getLearningPlan(requestedLearningPlanId).getId(), termId);*/
            reservedTimes = strategy.getReservedTimes(requestedLearningPlanId);
        } catch (PermissionDeniedException e) {
            throw new IllegalArgumentException("Course options not permitted for requested learning plan", e);
        }

    }

    private void updateReservedTimesOnBuild() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        if (removeReserved != null && reservedTimes != null
                && removeReserved >= 0 && removeReserved < reservedTimes.size()) {
            try {
                sb.deleteReservedTime(requestedLearningPlanId, reservedTimes
                        .get(removeReserved).getId());
            } catch (PermissionDeniedException e) {
                throw new IllegalStateException(
                        "Failed to remove reserved time", e);
            }
        }
        removeReserved = null;

        List<ReservedTime> newReservedTimes;
        try {
            newReservedTimes = sb.getReservedTimes(requestedLearningPlanId);
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Failed to refresh reserved times",
                    e);
        }

        if (newReservedTimes != null && reservedTimes != null)
            for (int i = 0; i < newReservedTimes.size(); i++) {
                if (i < reservedTimes.size()) {
                    ReservedTimeInfo nrt = (ReservedTimeInfo) newReservedTimes
                            .get(i);
                    nrt.setSelected(reservedTimes.get(i).isSelected());
                }
            }
        reservedTimes = newReservedTimes;
    }

    private void updateSavedSchedulesOnBuild() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        List<PossibleScheduleOption> newSavedSchedules;
        try {
            newSavedSchedules = sb.getSchedules(requestedLearningPlanId);
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException(
                    "Failed to refresh saved schedules", e);
        }

        if (newSavedSchedules != null && savedSchedules != null) {
            Iterator<PossibleScheduleOption> nsi = newSavedSchedules.iterator();
            int i = 0;
            while (nsi.hasNext()) {
                PossibleScheduleOptionInfo nss = (PossibleScheduleOptionInfo) nsi.next();
                if (!getTermId().equals(nss.getTermId())) {
                    nsi.remove();
                }

                if (i < savedSchedules.size()) {
                    nss.setSelected(savedSchedules.get(i).isSelected());
                }
                i++;
            }
        }
        savedSchedules = newSavedSchedules;
    }

    @Override
    public void buildSchedules() {
        updateReservedTimesOnBuild();
        updateSavedSchedulesOnBuild();

        if (more) {

            if (scheduleBuilder == null)
                throw new IllegalStateException(
                        "Schedule builder not initialized");

            if (possibleScheduleOptions == null)
                throw new IllegalStateException(
                        "Possible schedule options not initialized");

            // preserve order specified by front-end "shuffle"
            Collections.sort(possibleScheduleOptions,
                    new Comparator<PossibleScheduleOption>() {
                        @Override
                        public int compare(PossibleScheduleOption o1,
                                           PossibleScheduleOption o2) {
                            int s1 = o1.getShuffle();
                            int s2 = o2.getShuffle();
                            return s1 < s2 ? -1 : s1 == s2 ? 0 : 1;
                        }
                    });

            Set<PossibleScheduleOption> current = new HashSet<PossibleScheduleOption>();
            int nextn = possibleScheduleOptions.size();
            for (PossibleScheduleOption pso : possibleScheduleOptions) {
                if (pso.isSelected() || pso.isDiscarded())
                    current.add(pso);
                if (pso.isSelected())
                    nextn--;
            }

            if (nextn > 0) {
                List<PossibleScheduleOption> newOptions = scheduleBuilder
                        .getNext(nextn, current);
                int n = 0;
                ListIterator<PossibleScheduleOption> psi = possibleScheduleOptions
                        .listIterator();
                while (psi.hasNext() && n < newOptions.size()) {
                    PossibleScheduleOption psn = psi.next();
                    if (!psn.isSelected() && !psn.isDiscarded())
                        psi.set(newOptions.get(n++));
                }
            }

        } else {
            scheduleBuilder = new ScheduleBuilder(getTerm(), courseOptions, reservedTimes, savedSchedules, buildFilters);
            possibleScheduleOptions = getScheduleBuilder().getNext(possibleScheduleSize, Collections.<PossibleScheduleOption>emptySet());
        }
    }

    @Override
    public PossibleScheduleOption saveSchedule() {

        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        List<PossibleScheduleOption> possibleScheduleOptionList = (List<PossibleScheduleOption>) request.getSession().getAttribute(ScheduleBuilderConstants.SESSION_ATTRIBUTE_KEY);

        if (!CollectionUtils.isEmpty(possibleScheduleOptionList)) {
            getPossibleScheduleOptions().addAll(possibleScheduleOptionList);
        }

        if (possibleScheduleOptions == null || uniqueId == null
                || possibleScheduleOptions.isEmpty()) {
            LOG.error("No possible schedule options to save from - uniqueId = "
                    + uniqueId);
            return null;
        }

        PossibleScheduleOption saveMe = null;
        for (PossibleScheduleOption pso : possibleScheduleOptions)
            if (uniqueId.equals(pso.getUniqueId()))
                saveMe = pso;
        if (saveMe == null) {
            LOG.error("Invalid possible schedule unique ID " + uniqueId);
            return null;
        }

        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        try {
            PossibleScheduleOption rv = sb.createSchedule(
                    requestedLearningPlanId, saveMe);
            getSavedSchedules().add(rv);
            return rv;
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Unexpected authorization failure",
                    e);
        }
    }

    @Override
    public void removeSchedule() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        try {
            sb.deleteSchedule(requestedLearningPlanId, uniqueId);
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Failed to remove reserved time", e);
        }
    }

    public ScheduleBuilder getScheduleBuilder() {
        return scheduleBuilder;
    }

    public void setScheduleBuilder(ScheduleBuilder scheduleBuilder) {
        this.scheduleBuilder = scheduleBuilder;
    }

    /**
     * Template variable only.
     *
     * @return false.
     */
    public boolean isSelected() {
        return false;
    }

    public void setSelected(boolean selected) {
    }

    /**
     * Template variable only.
     *
     * @return false.
     */
    public boolean isDiscarded() {
        return false;
    }

    public void setDiscarded(boolean discarded) {
    }

    /**
     * Template variable only.
     *
     * @return 0.
     */
    public int getShuffle() {
        return 0;
    }

    public void setShuffle(int shuffle) {
    }

    @Override
    public boolean hasMore() {
        return scheduleBuilder != null && scheduleBuilder.hasMore();
    }

    public int getPossibleScheduleSize() {
        return possibleScheduleSize;
    }

    public void setPossibleScheduleSize(int possibleScheduleSize) {
        this.possibleScheduleSize = possibleScheduleSize == 0 ? 10
                : possibleScheduleSize;
    }

    public boolean isOverload() {
        return overload;
    }

    public void setOverload(boolean overload) {
        this.overload = overload;
    }

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

    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    @Override
    public List<CourseOption> getCourseOptions() {
        if (courseOptions == null) {

        }
        return courseOptions;
    }

    public void setCourseOptions(List<CourseOption> courseOptions) {
        this.courseOptions = courseOptions;
    }

    @Override
    public List<ReservedTime> getReservedTimes() {
        return reservedTimes;
    }

    public void setReservedTimes(List<ReservedTime> reservedTimes) {
        this.reservedTimes = reservedTimes;
    }

    @Override
    public List<PossibleScheduleOption> getPossibleScheduleOptions() {
        if (possibleScheduleOptions == null) {
            possibleScheduleOptions = new ArrayList<PossibleScheduleOption>();
        }
        return possibleScheduleOptions;
    }

    public void setPossibleScheduleOptions(
            List<PossibleScheduleOption> possibleScheduleOptions) {
        this.possibleScheduleOptions = possibleScheduleOptions;
    }

    @Override
    public List<PossibleScheduleOption> getSavedSchedules() {
        if (savedSchedules == null) {
            savedSchedules = new ArrayList<PossibleScheduleOption>();
        }
        return savedSchedules;
    }

    public void setSavedSchedules(
            List<PossibleScheduleOption> savedScheduleOptions) {
        this.savedSchedules = savedScheduleOptions;
    }

    @Override
    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
    }

    @Override
    public Integer getRemoveReserved() {
        return removeReserved;
    }

    public void setRemoveReserved(Integer removeReserved) {
        this.removeReserved = removeReserved;
    }

    @Override
    public boolean isIncludeClosed() {
        return includeClosed;
    }

    public void setIncludeClosed(boolean includeClosed) {
        this.includeClosed = includeClosed;
    }

    public String getFullTermWeekTitle() {
        Term t = getTerm();
        long ed = t.getEndDate().getTime();
        long sd = t.getStartDate().getTime();
        int weeksInTerm = (int) ((ed - sd) / 604800000);
        return "Weeks 1-" + weeksInTerm;
    }

    public String getFullTermWeekSubtitle() {
        Term t = getTerm();
        DateFormat df = new SimpleDateFormat("MMM d");
        return df.format(t.getStartDate()) + " - " + df.format(t.getEndDate());
    }

    @Override
    public String toString() {
        return "ScheduleBuildForm [termId=" + termId + "]";
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

    public TermHelper getTermHelper() {
        if (termHelper == null) {
            termHelper = KsapFrameworkServiceLocator.getTermHelper();
        }
        return termHelper;
    }

    public void setTermHelper(TermHelper termHelper) {
        this.termHelper = termHelper;
    }

    private AcademicCalendarService getAcademicCalendarService() {
        if (academicCalendarService == null) {
            academicCalendarService = (AcademicCalendarService) GlobalResourceLoader
                    .getService(new QName(AcademicCalendarServiceConstants.NAMESPACE,
                            AcademicCalendarServiceConstants.SERVICE_NAME_LOCAL_PART));
        }
        return academicCalendarService;
    }

    public void setAcademicCalendarService(AcademicCalendarService academicCalendarService) {
        this.academicCalendarService = academicCalendarService;
    }

    public List<String> getIncludeFilters() {
        return includeFilters;
    }

    public void setIncludeFilters(List<String> includeFilters) {
        this.includeFilters = includeFilters;
    }
}
