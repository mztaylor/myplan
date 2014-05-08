package org.kuali.student.myplan.registration.form;

import org.apache.log4j.Logger;
import org.kuali.rice.krad.web.form.UifFormBase;
import org.kuali.student.enrollment.acal.infc.Term;
import org.kuali.student.myplan.registration.util.RegistrationForm;
import org.kuali.student.myplan.schedulebuilder.dto.RegistrationDetailsInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by hemanthg on 4/22/2014.
 */
public class DefaultRegistrationForm extends UifFormBase implements RegistrationForm {

    private final Logger logger = Logger.getLogger(DefaultRegistrationForm.class);

    private Term term;
    private String termId;
    private String uniqueId;
    private boolean pinned;
    private String requestedLearningPlanId;
    private Set<String> selectedRegistrationCodes;
    private Map<String, String> plannedItems;
    private RegistrationDetailsInfo registrationDetails;

    @Override
    public String getUniqueId() {
        return uniqueId;
    }

    public void setUniqueId(String uniqueId) {
        this.uniqueId = uniqueId;
    }

    @Override
    public String getRequestedLearningPlanId() {
        return requestedLearningPlanId;
    }

    public void setRequestedLearningPlanId(String requestedLearningPlanId) {
        this.requestedLearningPlanId = requestedLearningPlanId;
    }

    @Override
    public Term getTerm() {
        return term;
    }

    public void setTerm(Term term) {
        this.term = term;
    }

    @Override
    public Set<String> getSelectedRegistrationCodes() {
        if (selectedRegistrationCodes == null) {
            selectedRegistrationCodes = new HashSet<String>();
        }
        return selectedRegistrationCodes;
    }

    public void setSelectedRegistrationCodes(Set<String> selectedRegistrationCodes) {
        this.selectedRegistrationCodes = selectedRegistrationCodes;
    }

    @Override
    public String getTermId() {
        return termId;
    }

    public void setTermId(String termId) {
        this.termId = termId;
    }

    @Override
    public RegistrationDetailsInfo getRegistrationDetails() {
        return registrationDetails;
    }

    public void setRegistrationDetails(RegistrationDetailsInfo registrationDetails) {
        this.registrationDetails = registrationDetails;
    }

    @Override
    public Map<String, String> getPlannedItems() {
        if (plannedItems == null) {
            plannedItems = new HashMap<String, String>();
        }
        return plannedItems;
    }

    public void setPlannedItems(Map<String, String> plannedItems) {
        this.plannedItems = plannedItems;
    }

    @Override
    public boolean isPinned() {
        return pinned;
    }

    public void setPinned(boolean pinned) {
        this.pinned = pinned;
    }
}
