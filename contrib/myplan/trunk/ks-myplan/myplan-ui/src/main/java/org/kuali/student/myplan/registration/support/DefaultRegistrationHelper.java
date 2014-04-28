package org.kuali.student.myplan.registration.support;

import org.kuali.student.myplan.registration.form.DefaultRegistrationForm;
import org.kuali.student.myplan.registration.util.RegistrationForm;
import org.kuali.student.myplan.registration.util.RegistrationHelper;

/**
 * Created by hemanthg on 4/25/2014.
 */
public class DefaultRegistrationHelper implements RegistrationHelper {
    @Override
    public RegistrationForm getInitialForm() {
        return new DefaultRegistrationForm();
    }
}
