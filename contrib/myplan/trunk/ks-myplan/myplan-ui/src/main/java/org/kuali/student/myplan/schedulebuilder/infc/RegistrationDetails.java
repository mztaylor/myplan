package org.kuali.student.myplan.schedulebuilder.infc;

import java.util.List;

/**
 * Created by hemanthg on 4/16/2014.
 */
public interface RegistrationDetails {

    /**
     * Registered course course Options
     *
     * @return
     */
    List<CourseOption> getRegisteredCourses();

    /**
     * Course option collection for course that are planned
     *
     * @return
     */
    List<CourseOption> getPlannedCourses();

    /**
     * Boolean which says if the registration button on UI has to show or not For the term
     *
     * @return
     */
    boolean isOpenForRegistration();

    /**
     * Pinned index is used in the UI for display purpose.
     *
     * @return
     */
    int getPinnedIndex();

    /**
     * Registration Url is used for forwarding the selected registration codes to registration service.
     *
     * @return
     */
    String getRegistrationUrl();
}
