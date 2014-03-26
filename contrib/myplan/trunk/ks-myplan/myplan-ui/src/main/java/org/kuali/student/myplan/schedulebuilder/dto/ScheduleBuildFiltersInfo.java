package org.kuali.student.myplan.schedulebuilder.dto;

import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildFilters;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemanthg on 2/13/14.
 */
public class ScheduleBuildFiltersInfo implements ScheduleBuildFilters {
    private boolean showClosed;
    private boolean showRestricted;
    private boolean showOverlapped;
    private boolean showOtherInstitutes;
    private List<String> zeroResultsReasons;


    @Override
    public boolean isShowClosed() {
        return showClosed;
    }

    public void setShowClosed(boolean showClosed) {
        this.showClosed = showClosed;
    }

    @Override
    public boolean isShowRestricted() {
        return showRestricted;
    }

    public void setShowRestricted(boolean showRestricted) {
        this.showRestricted = showRestricted;
    }

    @Override
    public boolean isShowOverlapped() {
        return showOverlapped;
    }

    public void setShowOverlapped(boolean showOverlapped) {
        this.showOverlapped = showOverlapped;
    }

    @Override
    public boolean isShowOtherInstitutes() {
        return showOtherInstitutes;
    }

    public void setShowOtherInstitutes(boolean showOtherInstitutes) {
        this.showOtherInstitutes = showOtherInstitutes;
    }

    @Override
    public List<String> getZeroResultsReasons() {
        if (zeroResultsReasons == null) {
            zeroResultsReasons = new ArrayList<String>();
        }
        return zeroResultsReasons;
    }

    public void setZeroResultsReasons(List<String> zeroResultsReasons) {
        this.zeroResultsReasons = zeroResultsReasons;
    }
}
