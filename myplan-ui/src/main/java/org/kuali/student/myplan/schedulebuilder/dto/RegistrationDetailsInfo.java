package org.kuali.student.myplan.schedulebuilder.dto;

import org.kuali.student.myplan.schedulebuilder.infc.CourseOption;
import org.kuali.student.myplan.schedulebuilder.infc.RegistrationDetails;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hemanthg on 4/16/2014.
 */
public class RegistrationDetailsInfo implements RegistrationDetails {

    private List<CourseOption> registeredCourses;
    private List<CourseOption> plannedCourses;
    private boolean openForRegistration;
    private String registrationOpenDate;
    private int pinnedIndex;
    private String registrationUrl;
    private boolean activitiesAvailable;

    @Override
    public List<CourseOption> getRegisteredCourses() {
        if (registeredCourses == null) {
            registeredCourses = new ArrayList<CourseOption>();
        }
        return registeredCourses;
    }

    public void setRegisteredCourses(List<CourseOption> registeredCourses) {
        this.registeredCourses = registeredCourses;
    }

    @Override
    public List<CourseOption> getPlannedCourses() {
        if (plannedCourses == null) {
            plannedCourses = new ArrayList<CourseOption>();
        }
        return plannedCourses;
    }

    public void setPlannedCourses(List<CourseOption> plannedCourses) {
        this.plannedCourses = plannedCourses;
    }

    @Override
    public boolean isOpenForRegistration() {
        return openForRegistration;
    }

    public void setOpenForRegistration(boolean openForRegistration) {
        this.openForRegistration = openForRegistration;
    }

    @Override
    public int getPinnedIndex() {
        return pinnedIndex;
    }

    public void setPinnedIndex(int pinnedIndex) {
        this.pinnedIndex = pinnedIndex;
    }

    public String getRegistrationUrl() {
        return registrationUrl;
    }

    public void setRegistrationUrl(String registrationUrl) {
        this.registrationUrl = registrationUrl;
    }

    @Override
    public String getRegistrationOpenDate() {
        return registrationOpenDate;
    }

    public void setRegistrationOpenDate(String registrationOpenDate) {
        this.registrationOpenDate = registrationOpenDate;
    }

    @Override
    public boolean isActivitiesAvailable() {
        return activitiesAvailable;
    }

    public void setActivitiesAvailable(boolean activitiesAvailable) {
        this.activitiesAvailable = activitiesAvailable;
    }
}
