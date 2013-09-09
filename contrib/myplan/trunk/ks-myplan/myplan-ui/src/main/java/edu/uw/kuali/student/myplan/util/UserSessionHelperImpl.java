package edu.uw.kuali.student.myplan.util;

import org.apache.commons.lang.WordUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.kim.api.identity.IdentityService;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.api.identity.PersonService;
import org.kuali.rice.kim.api.identity.entity.Entity;
import org.kuali.rice.kim.api.identity.type.EntityTypeContactInfo;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.krad.UserSession;
import org.kuali.rice.krad.util.GlobalVariables;
import org.kuali.student.myplan.plan.PlanConstants;
import org.kuali.student.myplan.utils.UserSessionHelper;
import org.kuali.student.r2.common.dto.ContextInfo;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 9/3/13
 * Time: 3:03 PM
 * To change this template use File | Settings | File Templates.
 */
public class UserSessionHelperImpl implements UserSessionHelper {

    private static final Logger logger = Logger.getLogger(UserSessionHelperImpl.class);

    private transient IdentityService identityService;
    private transient PersonService personService;


    public IdentityService getIdentityService() {
        if (identityService == null) {
            identityService = KimApiServiceLocator.getIdentityService();
        }
        return identityService;
    }

    public PersonService getPersonService() {
        if (personService == null) {
            personService = KimApiServiceLocator.getPersonService();
        }
        return personService;
    }

    public void setIdentityService(IdentityService identityService) {
        this.identityService = identityService;
    }

    public void setPersonService(PersonService personService) {
        this.personService = personService;
    }


    /**
     * makes a default context info for the user
     *
     * @return
     */
    @Override
    public ContextInfo makeContextInfoInstance() {
        ContextInfo contextInfo = new ContextInfo();
        Person user = GlobalVariables.getUserSession().getPerson();
        contextInfo.setPrincipalId(user.getPrincipalId());
        return contextInfo;
    }

    /**
     * Returns true if a user has authenticated as a adviser.
     * All sorts of conditional behavior depends on this method.
     *
     * @return True if the user is an adviser. Otherwise, false.
     */
    @Override
    public boolean isAdviser() {
        UserSession session = GlobalVariables.getUserSession();
        return session.retrieveObject(PlanConstants.SESSION_KEY_IS_ADVISER) != null;
    }

    /**
     * @return true if the user is a student
     */
    @Override
    public boolean isStudent() {
        try {
            String regId = getStudentId();
            Person person = getPersonService().getPerson(regId);
            Map<String, String> map = person.getExternalIdentifiers();
            return map.containsKey("systemKey");
        } catch (Exception e) {
            logger.error("Could not load the Person Information", e);
        }
        return false;
    }

    /**
     * @return if a user is logged in, false means it's a webservice client.
     */
    @Override
    public boolean isUserSession() {
        UserSession session = GlobalVariables.getUserSession();
        return session != null;
    }

    /**
     * Determines the student id that should be used for queries. If the user has the
     * adviser flag set in the session then there should also be a student id. Otherwise, just return the principal it.
     *
     * @return The Id
     */
    @Override
    public String getStudentId() {
        UserSession session = GlobalVariables.getUserSession();
        String regId;
        if (isAdviser()) {
            regId = (String) session.retrieveObject(PlanConstants.SESSION_KEY_STUDENT_ID);
            if (regId == null) {
                throw new RuntimeException("User is in adviser mode, but no student id was set in the session. (This shouldn't happen and should be reported).");
            }
        } else {
            regId = session.getPerson().getPrincipalId();
        }
        return regId;
    }

    /**
     * @return current User Id(irrespective of affiliations)
     */
    @Override
    public String getCurrentUserId() {
        UserSession session = GlobalVariables.getUserSession();
        return session.getPerson().getPrincipalId();
    }

    /**
     *
     * @return Student Name in firstName LastName format
     */
    @Override
    public String getStudentName() {
        UserSession session = GlobalVariables.getUserSession();
        String studentName;
        if (isAdviser()) {
            studentName = (String) session.retrieveObject(PlanConstants.SESSION_KEY_STUDENT_NAME);
            if (studentName == null) {
                throw new RuntimeException("User is in adviser mode, but no student name was set in the session. (This shouldn't happen and should be reported).");
            }
        } else {
            studentName = getName(getStudentId());
        }
        return studentName;
    }

    /**
     * @param principleId
     * @return The name in firstName lastName format
     */
    @Override
    public String getName(String principleId) {
        Person person = null;
        try {
            person = getPersonService().getPerson(principleId);
        } catch (Exception e) {
            logger.error("Could not load the Person Information", e);
        }
        if (person != null) {
            String firstName = WordUtils.capitalize(person.getFirstName());
            String lastName = WordUtils.capitalize(person.getLastName());
            return String.format("%s %s", firstName, lastName);
        } else {
            return null;
        }
    }

    /**
     * @param principleId
     * @return The First name of the user for provided principleId
     */
    @Override
    public String getFirstName(String principleId) {
        Person person = null;
        try {
            person = getPersonService().getPerson(principleId);
        } catch (Exception e) {
            logger.error("Could not load the Person Information", e);
        }
        if (person != null) {
            return WordUtils.capitalizeFully(person.getFirstName());
        } else {
            return null;
        }
    }

    /**
     * @param principleId
     * @return The Last name of the user for provided principleId.
     */
    @Override
    public String getLastName(String principleId) {
        Person person = null;
        try {
            person = getPersonService().getPerson(principleId);
        } catch (Exception e) {
            logger.error("Could not load the Person Information", e);
        }
        if (person != null) {
            return WordUtils.capitalizeFully(person.getLastName());
        } else {
            return null;
        }
    }

    /**
     * @param principleId
     * @return Capitalized name of the user.
     */
    @Override
    public String getCapitalizedName(String principleId) {
        try {
            Person person = getPersonService().getPerson(principleId);
            return person.getName().toUpperCase();
        } catch (Exception e) {
            logger.error("Could not load the Person Information", e);
        }

        return null;
    }

    /**
     * @param principleId
     * @return Email Address of the user with given principle Id
     */
    @Override
    public String getMailAddress(String principleId) {
        try {
            Person user = GlobalVariables.getUserSession().getPerson();
            String emailAddress = user.getEmailAddress();

            Entity entity = getIdentityService().getEntityByPrincipalId(principleId);
            if (entity == null) {
                return null;
            }
            List<EntityTypeContactInfo> contactInfos = entity.getEntityTypeContactInfos();
            for (EntityTypeContactInfo ci : contactInfos) {
                emailAddress = ci.getDefaultEmailAddress().getEmailAddress();
            }
            return emailAddress;
        } catch (Exception e) {
            logger.error("Could not get the Email Address for the student" + e);
            return null;
        }
    }

    /**
     * @return student number value as-is, including null
     */
    @Override
    public String getStudentNumber() {
        try {
            String regId = getStudentId();
            Person person = getPersonService().getPerson(regId);
            Map<String, String> map = person.getExternalIdentifiers();

            // Rice KIM's equivalent to studentID is /Person/StudentNumber from SWS
            String systemNumber = map.get("studentID");
            return systemNumber;
        } catch (Exception e) {
            logger.error("Could not load the Person Information", e);
        }
        throw new DataRetrievalFailureException("Could not find the SystemNumber for the Student");
    }

    /**
     * @return Student ExternalIdentifier (SystemKey)
     */
    @Override
    public String getStudentExternalIdentifier() {
        return getExternalIdentifier(getStudentId());
    }

    /**
     * @param regId
     * @return User ExternalIdentifier (SystemKey)
     */
    @Override
    public String getExternalIdentifier(String regId) {
        try {
            Person person = getPersonService().getPerson(regId);
            if (person != null) {
                Map<String, String> idmap = person.getExternalIdentifiers();
                for (String key : idmap.keySet()) {
                    String value = idmap.get(key);
                    logger.info("identifier : " + key + " = " + value);

                }
                // Rice KIM's equivalent to systemKey is /Person/StudentSystemKey from SWS
                String systemKey = idmap.get("systemKey");
                if (StringUtils.hasText(systemKey)) {
                    return systemKey;
                }
            }
        } catch (Exception e) {
            logger.error("Could not load the Person Information", e);
        }

        throw new DataRetrievalFailureException("Could not find the SystemKey for the Student");
    }
}
