package org.kuali.student.myplan.utils;

import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.course.util.PlanConstants;
import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.Date;

/**
 * Provides an initialized Context which can be used for service requests.
 */
public class UserSessionHelper {
    public synchronized static ContextInfo makeContextInfoInstance() {
        ContextInfo contextInfo = new ContextInfo();
        Person user = GlobalVariables.getUserSession().getPerson();
        contextInfo.setPrincipalId(user.getPrincipalId());
        contextInfo.setCurrentDate(new Date());
        return contextInfo;
    }

    /**
     *  Returns true if the user has an adviser role. All sorts of conditional behavior depends on this method.
     *  @return True if the user is an adviser. Otherwise, false.
     */
    public synchronized static boolean isAdviser() {
        UserSession session = GlobalVariables.getUserSession();
        return session.retrieveObject(PlanConstants.SESSION_KEY_IS_ADVISER) != null;
    }

    /**
     *  Determines the student id that should be used for queries. If the user has the
     *  adviser flag set in the session then there should also be a student id. Otherwise,
     *  just return the principal it.
     *  @return The Id
     */
    public synchronized static String getStudentId() {
        UserSession session = GlobalVariables.getUserSession();
        String studentId;
        if (isAdviser()) {
            studentId = (String) session.retrieveObject(PlanConstants.SESSION_KEY_STUDENT_ID);
            if (studentId == null) {
                throw new RuntimeException("User is in adviser mode, but no student id was set in the session. (This shouldn't happen and should be reported).");
            }
        } else {
            studentId = session.getPerson().getPrincipalId();
        }
        return studentId;
    }
    /**
     *  Determines the student id that should be used for queries. If the user has the
     *  adviser flag set in the session then there should also be a student id. Otherwise,
     *  just return the principal it.
     *  @return Student Name
     */
    public synchronized static String getStudentName() {
        UserSession session = GlobalVariables.getUserSession();
        String studentName;
        if (isAdviser()) {
            studentName = (String) session.retrieveObject(PlanConstants.SESSION_KEY_STUDENT_NAME);
            if (studentName == null) {
                throw new RuntimeException("User is in adviser mode, but no student name was set in the session. (This shouldn't happen and should be reported).");
            }
        } else {
            studentName = session.getPerson().getFirstName()+" "+session.getPerson().getLastName();
        }
        return studentName;
    }
}
