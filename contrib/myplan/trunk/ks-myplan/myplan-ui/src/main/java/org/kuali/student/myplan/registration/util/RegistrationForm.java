package org.kuali.student.myplan.registration.util;

import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.schedulebuilder.infc.RegistrationDetails;

import java.util.List;
import java.util.Map;

/**
 * Created by hemanthg on 4/22/2014.
 */
public interface RegistrationForm {


    /**
     * RegistrationDetails holds the details required for registrationUI.
     *
     * @return RegistrationDetails
     */
    RegistrationDetails getRegistrationDetails();

    /**
     * TermId for which the registration details needs to be shown.
     *
     * @return termId
     */
    String getTermId();


    /**
     * Term for which the registration details needs to be shown.
     *
     * @return
     */
    Term getTerm();

    /**
     * UniqueId for which the registration details are required.
     *
     * @return
     */
    String getUniqueId();


    /**
     * Registration codes which are selected for registration.
     *
     * @return Registration codes selected
     */
    List<String> getSelectedRegistrationCodes();


    /**
     * Map of activityOfferingId associated to a planItemId
     *
     * @return
     */
    Map<String, String> getPlannedItems();


    /**
     * Learning PlanId of the student.
     *
     * @return
     */
    String getRequestedLearningPlanId();

    /**
     * Builds the registration details.
     */
    void buildRegistrationDetails();
}
