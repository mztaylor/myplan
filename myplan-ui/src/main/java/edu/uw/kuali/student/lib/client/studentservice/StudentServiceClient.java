package edu.uw.kuali.student.lib.client.studentservice;

import org.restlet.representation.Representation;

import java.util.List;
import java.util.Set;

public interface StudentServiceClient {

    public static final String SERVICE_NAME = "{MyPlan}StudentServiceClient";

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
     * @param Curriculum
     * @param courseNo
     * @return
     * @throws ServiceException
     */
    public abstract String getSections(String year, String Curriculum, String courseNo) throws ServiceException;

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

}