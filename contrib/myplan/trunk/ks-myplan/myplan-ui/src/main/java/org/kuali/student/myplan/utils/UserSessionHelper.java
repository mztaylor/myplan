package org.kuali.student.myplan.utils;

import org.kuali.student.r2.common.dto.ContextInfo;

/**
 * Provides an initialized Context which can be used for service requests.
 */
public interface UserSessionHelper {

    public ContextInfo makeContextInfoInstance();

    public boolean isAdviser();

    public boolean isStudent();

    public boolean isUserSession();

    public String getStudentId();

    public String getCurrentUserId();

    public String getStudentName();

    public String getName(String principleId);

    public String getFirstName(String principleId);

    public String getLastName(String principleId);

    public String getCapitalizedName(String principleId);

    public String getMailAddress(String principleId);

    public String getStudentNumber();

    public String getStudentExternalIdentifier();

    public String getExternalIdentifier(String regId);

}
