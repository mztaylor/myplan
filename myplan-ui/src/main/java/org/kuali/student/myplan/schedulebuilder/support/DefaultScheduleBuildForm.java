package org.kuali.student.myplan.schedulebuilder.support;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.enrollment.acal.service.AcademicCalendarService;
import org.kuali.student.myplan.config.UwMyplanServiceLocator;
import org.kuali.student.myplan.schedulebuilder.dto.PossibleScheduleOptionInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ReservedTimeInfo;
import org.kuali.student.myplan.schedulebuilder.dto.ScheduleBuildFiltersInfo;
import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.PossibleScheduleOption;
import org.kuali.student.myplan.schedulebuilder.infc.ReservedTime;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildForm;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuildStrategy;
import org.kuali.student.myplan.schedulebuilder.util.ScheduleBuilder;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.exceptions.PermissionDeniedException;
import org.kuali.student.r2.common.util.constants.AcademicCalendarServiceConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.namespace.QName;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class DefaultScheduleBuildForm extends DefaultScheduleForm implements
        ScheduleBuildForm {

    private static final long serialVersionUID = 3274052642980656068L;

    private static final Logger LOG = LoggerFactory.getLogger(DefaultScheduleBuildForm.class);


    private boolean more;
    private String uniqueId;

    private int possibleScheduleSize;
    private ScheduleBuilder scheduleBuilder;
    private List<CourseOption> courseOptions;
    private List<PossibleScheduleOption> savedSchedules;
    private List<PossibleScheduleOption> possibleScheduleOptions;
    private PossibleScheduleOption registeredSchedule;

    private int selectedSlnCount;

    private transient AcademicCalendarService academicCalendarService;

    private transient UserSessionHelper userSessionHelper;


    private void updateReservedTimesOnBuild() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        if (getRemoveReserved() != null && getReservedTimes() != null
                && getRemoveReserved() >= 0 && getRemoveReserved() < getReservedTimes().size()) {
            try {
                sb.deleteReservedTime(getRequestedLearningPlanId(), getReservedTimes()
                        .get(getRemoveReserved()).getId());
            } catch (PermissionDeniedException e) {
                throw new IllegalStateException(
                        "Failed to remove reserved time", e);
            }
        }
        setRemoveReserved(null);

        List<ReservedTime> newReservedTimes;
        try {
            newReservedTimes = sb.getReservedTimesForTermId(getRequestedLearningPlanId(), getTermId());
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Failed to refresh reserved times",
                    e);
        }

        if (newReservedTimes != null && getReservedTimes() != null)
            for (int i = 0; i < newReservedTimes.size(); i++) {
                if (i < getReservedTimes().size()) {
                    ReservedTimeInfo nrt = (ReservedTimeInfo) newReservedTimes
                            .get(i);
                    nrt.setSelected(getReservedTimes().get(i).isSelected());
                }
            }
        setReservedTimes(newReservedTimes);
    }

    private void updateSavedSchedulesOnBuild() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        List<PossibleScheduleOption> newSavedSchedules;
        try {
            newSavedSchedules = sb.getSchedulesForTerm(getRequestedLearningPlanId(), getTermId());
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
        for (PossibleScheduleOption possibleScheduleOption : getSavedSchedules()) {
            getScheduleBuildHelper().buildPossibleScheduleEvents(possibleScheduleOption, getTerm());
        }

    }

    /*Defaulting to 8:00Am*/
    protected long getDefaultMinTime() {
        Calendar defaultStart = Calendar.getInstance();
        defaultStart.setTime(getTerm().getStartDate());
        defaultStart.set(defaultStart.get(Calendar.YEAR), defaultStart.get(Calendar.MONTH), defaultStart.get(Calendar.DATE), 8, 0);
        return defaultStart.getTime().getTime();
    }

    /*Defaulting to 5:00Pm*/
    protected long getDefaultMaxTime() {
        Calendar defaultEnd = Calendar.getInstance();
        defaultEnd.setTime(getTerm().getEndDate());
        defaultEnd.set(defaultEnd.get(Calendar.YEAR), defaultEnd.get(Calendar.MONTH), defaultEnd.get(Calendar.DATE), 17, 0);
        return defaultEnd.getTime().getTime();
    }

    @Override
    public void buildSchedules() {
        LOG.info("Implementing build schedule");
        setRequestedLearningPlanId(getScheduleBuildHelper().validateOrPopulateLearningPlanId(getRequestedLearningPlanId()));
        updateReservedTimesOnBuild();
        updateSavedSchedulesOnBuild();

        /*Clear all Zero results errors*/
        ((ScheduleBuildFiltersInfo) getBuildFilters()).setZeroResultsReasons(new ArrayList<String>());
        ((ScheduleBuildFiltersInfo) getBuildFilters()).setResultsNotPossibleReasons(new ArrayList<String>());

        courseOptions = getScheduleBuildStrategy().getCourseOptions(getRequestedLearningPlanId(), getTermId(), getBuildFilters());

        validateAndGenerateTerm();

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
                    }
            );

            Set<PossibleScheduleOption> current = new HashSet<PossibleScheduleOption>();
            int nextn = getPossibleScheduleOptions().size();
            for (PossibleScheduleOption pso : getPossibleScheduleOptions()) {
                if (pso.isSelected() || pso.isDiscarded())
                    current.add(pso);
                if (pso.isSelected())
                    nextn--;
            }

            if (nextn > 0) {
                List<PossibleScheduleOption> newOptions = getScheduleBuilder().getNext(nextn, current);
                int n = 0;
                ListIterator<PossibleScheduleOption> psi = getPossibleScheduleOptions().listIterator();
                while (psi.hasNext() && n < newOptions.size()) {
                    PossibleScheduleOption psn = psi.next();
                    if (!psn.isSelected() && !psn.isDiscarded())
                        psi.set(newOptions.get(n++));
                }
            }

        } else {
            scheduleBuilder = getScheduleBuildStrategy().getScheduleBuilder(getTerm(), getCourseOptions(), getReservedTimes(), getSavedSchedules(), getBuildFilters());
            possibleScheduleOptions = scheduleBuilder.getNext(getPossibleScheduleSize(), Collections.<PossibleScheduleOption>emptySet());
            registeredSchedule = scheduleBuilder.getRegistered();
        }

        setMinTime(getDefaultMinTime());
        setMaxTime(getDefaultMaxTime());

        /*Calculating min & max times of all possibleSchedules combined and If there is any weekend or TBD in any of the possible schedules*/
        for (PossibleScheduleOption possibleScheduleOption : getPossibleScheduleOptions()) {
            if (!isTbd() && possibleScheduleOption.isTbd()) {
                setTbd(possibleScheduleOption.isTbd());
            }
            if (!isWeekend() && possibleScheduleOption.isWeekend()) {
                setWeekend(possibleScheduleOption.isWeekend());
            }

            if (possibleScheduleOption.getMinTime() < getMinTime()) {
                setMinTime(possibleScheduleOption.getMinTime());
            }

            if (possibleScheduleOption.getMaxTime() > getMaxTime()) {
                setMaxTime(possibleScheduleOption.getMaxTime());
            }

        }
        /*Calculating min & max times for registered schedules and If there is any weekend or TBD in any of the registered possible schedules*/
        if (getRegisteredSchedule() != null) {

            ((PossibleScheduleOptionInfo) getRegisteredSchedule()).setLockedIn(true);

            PossibleScheduleOption possibleScheduleOption = getRegisteredSchedule();
            if (!isTbd() && possibleScheduleOption.isTbd()) {
                setTbd(possibleScheduleOption.isTbd());
            }
            if (!isWeekend() && possibleScheduleOption.isWeekend()) {
                setWeekend(possibleScheduleOption.isWeekend());
            }

            if (possibleScheduleOption.getMinTime() < getMinTime()) {
                setMinTime(possibleScheduleOption.getMinTime());
            }

            if (possibleScheduleOption.getMaxTime() > getMaxTime()) {
                setMaxTime(possibleScheduleOption.getMaxTime());
            }
        }


    }

    @Override
    public PossibleScheduleOption saveSchedule() {
        LOG.info("Implementing save schedule");
        if (getPossibleScheduleOptions() == null || getUniqueId() == null
                || getPossibleScheduleOptions().isEmpty()) {
            LOG.error("No possible schedule options to save from - uniqueId = " + getUniqueId());
            return null;
        }

        PossibleScheduleOption saveMe = null;
        for (PossibleScheduleOption pso : getPossibleScheduleOptions())
            if (getUniqueId().equals(pso.getUniqueId()))
                saveMe = pso;
        if (saveMe == null) {
            LOG.error("Invalid possible schedule unique ID " + getUniqueId());
            return null;
        }

        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        try {
            PossibleScheduleOption rv = sb.createSchedule(
                    getRequestedLearningPlanId(), saveMe);
            getSavedSchedules().add(rv);
            return rv;
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Unexpected authorization failure",
                    e);
        }
    }

    @Override
    public String removeSchedule() {
        ScheduleBuildStrategy sb = getScheduleBuildStrategy();
        try {
            sb.deleteSchedule(getRequestedLearningPlanId(), getUniqueId());
        } catch (PermissionDeniedException e) {
            throw new IllegalStateException("Failed to remove reserved time", e);
        }
        return getUniqueId();
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
        if (possibleScheduleSize == 0) {
            possibleScheduleSize = 25;
        }
        return possibleScheduleSize;
    }

    public void setPossibleScheduleSize(int possibleScheduleSize) {
        this.possibleScheduleSize = possibleScheduleSize;
    }

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }


    @Override
    public List<CourseOption> getCourseOptions() {
        if (courseOptions == null) {
            courseOptions = new ArrayList<CourseOption>();
        }
        return courseOptions;
    }

    public void setCourseOptions(List<CourseOption> courseOptions) {
        this.courseOptions = courseOptions;
    }


    @Override
    public List<PossibleScheduleOption> getPossibleScheduleOptions() {
        LOG.info("Form key: " + getFormKey());
        if (possibleScheduleOptions == null) {
            possibleScheduleOptions = new ArrayList<PossibleScheduleOption>();
        }
        return possibleScheduleOptions;
    }

    public void setPossibleScheduleOptions(
            List<PossibleScheduleOption> possibleScheduleOptions) {
        LOG.info("Form key: " + getFormKey());
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
    public PossibleScheduleOption getRegisteredSchedule() {
        return registeredSchedule;
    }

    public void setRegisteredSchedule(PossibleScheduleOption registeredSchedule) {
        this.registeredSchedule = registeredSchedule;
    }

    @Override
    public boolean isMore() {
        return more;
    }

    public void setMore(boolean more) {
        this.more = more;
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
        return "ScheduleBuildForm [termId=" + getTermId() + "]";
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

    public UserSessionHelper getUserSessionHelper() {
        if (userSessionHelper == null) {
            userSessionHelper = UwMyplanServiceLocator.getInstance().getUserSessionHelper();
        }
        return userSessionHelper;
    }

    public void setUserSessionHelper(UserSessionHelper userSessionHelper) {
        this.userSessionHelper = userSessionHelper;
    }

    public int getSelectedSlnCount() {
        return selectedSlnCount;
    }

    public void setSelectedSlnCount(int selectedSlnCount) {
        this.selectedSlnCount = selectedSlnCount;
    }
}
