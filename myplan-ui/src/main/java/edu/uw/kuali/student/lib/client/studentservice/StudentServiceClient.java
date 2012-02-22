package edu.uw.kuali.student.lib.client.studentservice;

import org.restlet.representation.Representation;

import java.util.List;

public interface StudentServiceClient {
    /**
     * Set the base URL for the service.
     * @param baseUri A
     */
    public abstract void setBaseUrl(String baseUri);

    /**
     * Returns the base URI for the service.
     */
    public abstract String getBaseUrl();

    /**
     * Returns the version of the service the implementation is meant to work with.
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
     * @return The XML response as a String
     */
    public abstract String getCurrentTerm() throws ServiceException;

    /**
     * Query the student web service (term service) for information about a particular term
     * and return the response as an XML String.
     * @param year YYYY
     * @param term spring, summer, winter, autumn
     * @return The XML response as a String
     */
    public abstract String getTermInfo(String year, String term) throws ServiceException;

    /**
     * Query the student web service (section service) for information about a particular term and curriculum.
     * @param year
     * @param term
     * @param curriculumCode
     * @return The XML response as a String.
     */
    public abstract String getSectionInfo(String year, String term, String curriculumCode) throws ServiceException;

    /**
     * Query the student web service (course service) to convert a course abbreviation to a TimeScheduleLinkAbbreviation
     */
    public abstract String getTimeScheduleLinkAbbreviation(String year, String term, String curriculumCode) throws ServiceException;
}