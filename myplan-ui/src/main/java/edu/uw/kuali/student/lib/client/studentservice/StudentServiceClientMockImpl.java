package edu.uw.kuali.student.lib.client.studentservice;

import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.kuali.rice.kim.api.identity.Person;
import org.restlet.Client;
import org.restlet.ext.net.HttpClientHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Rest client for the Student Service.
 */
public class StudentServiceClientMockImpl implements StudentServiceClient {

    private static final Log logger = LogFactory.getLog(StudentServiceClientImpl.class);

    @Override
    public HttpClientHelper getClient() {
        return null;
    }

    public StudentServiceClientMockImpl() {
    }

    /**
     * This constructor is here to make the Spring config consistent with the other implementation.
     */
    public StudentServiceClientMockImpl(String baseUrl, String trustStoreFilename, String trustStorePasswd,
                                        String keyStoreFilename, String keyStorePasswd) {
    }

    /**
     * {@inheritDoc}
     */
    public void setBaseUrl(String baseUri) {
        //  Unused.
    }

    /**
     * {@inheritDoc}
     */
    public void setServiceVersion(String version) {
        //  Unused.
    }

    public String getBaseUrl() {
        return "Unset";
    }

    public String getServiceVersion() {
        return "v4";
    }

    public List<String> getAvailableVersions() throws ServiceException {
        List<String> versions = new ArrayList<String>();
        versions.add("v4");
        return versions;
    }

    @Override
    public String getCurrentTerm() throws ServiceException {
        //  Read a response for ...
        //  https://ucswseval1.cac.washington.edu/student/v4/public/term/current.xml
        //  ... from a text file.
        InputStream in = this.getClass().getResourceAsStream("/txt/student_service_current_term_response.xml");
        String out = null;
        try {
            out = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            logger.error("Could not read response file.", e);
        }
        return out;
    }

    @Override
    public String getTermInfo(String year, String term) throws ServiceException {
        //  Read a response for ...
        //  https://ucswseval1.cac.washington.edu/student/v4/public/term/2012,spring.xml
        //  ... from a text file.
        InputStream in = this.getClass().getResourceAsStream("/txt/student_service_term_response_spring_2012.xml");
        String out = null;
        try {
            out = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            logger.error("Could not read response file.", e);
        }
        return out;


    }

    @Override
    public String getSectionInfo(String year, String term, String curriculumCode) throws ServiceException {
        //  Read a response for ...
        //  https://ucswseval1.cac.washington.edu/student/v4/public/section.xml?year=2011&quarter=winter&curriculum_abbreviation=chem
        //  ... from a text file.
        InputStream in = this.getClass().getResourceAsStream("/txt/student_service_section_response.xml");
        String out = null;
        try {
            out = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            logger.error("Could not read response file.", e);
        }
        return out;
    }

    @Override
    public String getSections(String year, String Curriculum, String courseNo, int futureTerms) throws ServiceException {
        //  Read a response for ...
        //  https://ucswseval1.cac.washington.edu/student/v4/public/section?year=2002&quarter=&curriculum_abbreviation=ESS&course_number=101&reg_id=&search_by=Instructor
        //  ... from a text file.
        InputStream in = this.getClass().getResourceAsStream("/txt/student_service_section_response.xml");
        String out = null;
        try {
            out = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            logger.error("Could not read response file.", e);
        }
        return out;
    }

    @Override
    public String getSections(String year, String quarter, String abbrev, String num) throws ServiceException {
        //  Read a response for ...
        //  https://ucswseval1.cac.washington.edu/student/v4/public/section?year=2002&quarter=&curriculum_abbreviation=ESS&course_number=101&reg_id=&search_by=Instructor
        //  ... from a text file.
        InputStream in = this.getClass().getResourceAsStream("/txt/student_service_section_response.xml");
        String out = null;
        try {
            out = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            logger.error("Could not read response file.", e);
        }
        return out;
    }

    public String getSecondarySections(String year, String quarter, String abbrev, String num, String section)
            throws ServiceException {
        return null;
    }

    @Override
    public String getSectionStatus(String year, String quarter, String abbrev, String num, String section) throws ServiceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public String getAcademicRecords(String regId, String year, String term, String registrationUrl) throws ServiceException {
        //  Read a response for ...
        //  https://ucswseval1.cac.washington.edu/student/v4/enrollment?reg_id=9136CCB8F66711D5BE060004AC494FFE&verbose=on
        //  ... from a text file.
        InputStream in = this.getClass().getResourceAsStream("/txt/student_service_academic_record_response.xml");
        String out = null;
        try {
            out = IOUtils.toString(in, "UTF-8");
        } catch (IOException e) {
            logger.error("Could not read response file.", e);
        }
        return out;
    }


    @Override
    public String getTimeSchedules(String year, String term, String curriculum, String courseNumber, String sectionUrl) throws ServiceException {
        return curriculum;
    }

//    /**
//     * @param regId
//     * @return
//     * @throws edu.uw.kuali.student.lib.client.studentservice.ServiceException
//     *
//     */
//    @Override
//    public String getPersonByRegID(String regId) throws ServiceException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }

    @Override
    public Person getPersonBySysKey(String syskey) throws ServiceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public boolean connectionStatus(String url) {
        return false;
    }

    @Override
    public String getAllSectionsStatus(String year, String quarter, String abbrev, String num) throws ServiceException {
        return null;
    }

    @Override
    public String getCurriculumForSubject(String year, String quarter, String abbrev) throws ServiceException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }



}
