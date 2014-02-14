package org.kuali.student.myplan.schedulebuilder.dto;

import org.kuali.student.myplan.schedulebuilder.infc.ScheduleBuildFilters;

/**
 * Created by hemanthg on 2/13/14.
 */
public class ScheduleBuildFiltersInfo implements ScheduleBuildFilters {
    private boolean showClosed;
    private boolean showRestricted;
    private boolean showOverlapped;


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
}
