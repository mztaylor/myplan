package edu.uw.kuali.student.lib.client.studentservice;

import org.dom4j.Document;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Rest client for the Student Service.
 */
public class FauxStudentServiceClientImpl
	implements StudentServiceClient {

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
        //   https://ucswseval1.cac.washington.edu/student/v4/public/section.xml?year=2009&quarter=winter&curriculum_abbreviation=chem
        StringBuilder sb = new StringBuilder();
        sb.append("<SearchResults xmlns=\"http://webservices.washington.edu/student/\" xmlns:i=\"http://www.w3.org/2001/XMLSchema-instance\">")
            .append("<Sections><Section>")
            .append("<Href>/student/v4/public/course/2009,winter,CHEM,142/A.xml</Href>")
            .append("<CourseNumber>142</CourseNumber><CurriculumAbbreviation>CHEM</CurriculumAbbreviation><Quarter>winter</Quarter>")
            .append("<SectionID>A</SectionID><Year>2009</Year></Section></Sections>")
            .append("</SearchResults>");
        return sb.toString();
    }
}
