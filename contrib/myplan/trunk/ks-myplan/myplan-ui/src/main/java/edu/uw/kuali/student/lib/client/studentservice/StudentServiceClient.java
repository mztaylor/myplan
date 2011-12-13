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

    public abstract String getTermInfo(String year, String term) throws ServiceException;

    public abstract String getSectionInfo(String year, String term, String curriculumCode) throws ServiceException;
}