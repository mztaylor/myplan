package edu.uw.kuali.student.myplan.util.mock;

import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 9/3/13
 * Time: 6:27 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserSessionHelperMockImpl implements UserSessionHelper {
    private Map<String, String> mockData = new HashMap<String, String>();

    @Override
    public ContextInfo makeContextInfoInstance() {
        return new ContextInfo();
    }

    @Override
    public boolean isAdviser() {
        return Boolean.valueOf(mockData.get("isAdviser"));
    }

    @Override
    public boolean isAdviserForManagePlan() {
        return Boolean.valueOf(mockData.get("isAdviserManagePlan"));
    }

    @Override
    public boolean isStudent() {
        return !Boolean.valueOf(mockData.get("isAdviser"));
    }

    @Override
    public boolean isUserSession() {
        return true;
    }

    @Override
    public String getStudentId() {
        return mockData.get("principleId");
    }

    @Override
    public String getCurrentUserId() {
        return mockData.get("principleId");
    }

    @Override
    public String getStudentName() {
        return mockData.get("name");
    }

    @Override
    public String getName(String principleId) {
        if (principleId.contains("|")) {
            String[] properties = principleId.split("\\|");
            if (properties.length > 0) {
                for (String property : properties) {
                    String[] elements = property.split("=");
                    if (elements.length == 2) {
                        mockData.put(elements[0], elements[1]);
                    }
                }
            }
        }
        return mockData.get("name");
    }

    @Override
    public String getFirstName(String principleId) {
        return mockData.get("name");
    }

    @Override
    public String getLastName(String principleId) {
        return mockData.get("name");
    }

    @Override
    public String getCapitalizedName(String principleId) {
        return mockData.get("name").toUpperCase();
    }

    @Override
    public String getMailAddress(String principleId) {
        return mockData.get("email");
    }

    @Override
    public String getStudentNumber() {
        return mockData.get("studentId");
    }

    @Override
    public String getStudentExternalIdentifier() {
        return mockData.get("externalIdentifier");
    }

    @Override
    public String getExternalIdentifier(String regId) {
        return mockData.get("externalIdentifier");
    }
}
