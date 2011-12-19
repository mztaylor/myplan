package edu.uw.kuali.student.lib.client.studentservice;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Rest client for the Student Service.
 */
public class FauxStudentServiceClientImpl
	implements StudentServiceClient {

    private static Log logger = LogFactory.getLog(StudentServiceClientImpl.class);

    public FauxStudentServiceClientImpl() {}

    /**
     * This constructor is here to make the Spring config consistent with the other implementation.
     */
    public FauxStudentServiceClientImpl(String baseUrl, String trustStoreFilename, String trustStorePasswd,
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
    public String getTermInfo(String year, String term) throws ServiceException {
        //  Only return the elements that are used.
        //  https://ucswseval1.cac.washington.edu/student/v4/public/term/2011,autumn.xml
        StringBuilder sb = new StringBuilder();
        sb.append("<Term xmlns=\"http://webservices.washington.edu/student/\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance/\">")
            .append("<LastDropDay>2011-11-13</LastDropDay>")
            .append("</Term>");
        return sb.toString();
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
}
