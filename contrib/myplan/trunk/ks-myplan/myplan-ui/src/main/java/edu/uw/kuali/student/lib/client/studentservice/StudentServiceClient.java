package edu.uw.kuali.student.lib.client.studentservice;

import org.kuali.rice.kim.api.identity.Person;
import org.restlet.Client;
import org.restlet.ext.net.HttpClientHelper;

import java.util.List;

public interface StudentServiceClient {

    public static final String SERVICE_NAME = "{MyPlan}StudentServiceClient";

    public HttpClientHelper getClient();

    /**
     * Set the base URL for the service.
     *
     * @param baseUri A
     */
    public abstract void setBaseUrl(String baseUri);

    /**
     * Returns the base URI for the service.
     */
    public abstract String getBaseUrl();

    /**
     * Returns the version of the service the implementation is meant to work with.
     *
     * @return
     */
    public abstract String getServiceVersion();

    /**
     * Get available service versions from the service.
     */
    public abstract List<String> getAvailableVersions() throws ServiceException;

    /**
     * Query the student web service (term service) for information about the current term
     * and return the response as an XML String.
     *
     * @return The XML response as a String
     */
    public abstract String getCurrentTerm() throws ServiceException;

    /**
     * Query the student web service (term service) for information about a particular term
     * and return the response as an XML String.
     *
     * @param year YYYY
     * @param term spring, summer, winter, autumn
     * @return The XML response as a String
     */
    public abstract String getTermInfo(String year, String term) throws ServiceException;

    /**
     * Query the student web service (section service) for information about a particular term and curriculum.
     *
     * @param year
     * @param term
     * @param curriculumCode
     * @return The XML response as a String.
     */
    public abstract String getSectionInfo(String year, String term, String curriculumCode) throws ServiceException;

    /**
     * @param year
     * @param abbrev
     * @param num
     * @return
     * @throws ServiceException
     */
    public abstract String getSections(String year, String abbrev, String num, int futureTerms) throws ServiceException;

    /**
     * @param year
     * @param quarter
     * @param abbrev
     * @param num
     * @return
     * @throws ServiceException
     */
    public abstract String getSections(String year, String quarter, String abbrev, String num) throws ServiceException;


    public abstract String getSecondarySections(String year, String quarter, String abbrev, String num, String section) throws ServiceException;


    public abstract String getSectionStatus(String year, String quarter, String abbrev, String num, String section) throws ServiceException;

    public abstract String getAllSectionsStatus(String year, String quarter, String abbrev, String num) throws ServiceException;


    public abstract String getCurriculumForSubject(String year, String quarter, String abbrev) throws ServiceException;

    /**
     * @param regId
     * @return
     * @throws ServiceException
     */
    public abstract String getAcademicRecords(String regId, String year, String term, String registrationUrl) throws ServiceException;


    /**
     * @param year
     * @param term
     * @param curriculum
     * @param sectionUrl
     * @return
     * @throws ServiceException
     */
    public String getTimeSchedules(String year, String term, String curriculum, String courseNumber, String sectionUrl) throws ServiceException;

    /**
     * @param syskey
     * @return
     * @throws ServiceException
     */
    public Person getPersonBySysKey(String syskey) throws ServiceException;

//    /**
//     * @param regId
//     * @return
//     * @throws ServiceException
//     */
//    public String getPersonByRegID(String regId) throws ServiceException;

    /**
     * @param url
     * @return
     */
    public boolean connectionStatus(String url);

}