package edu.uw.kuali.student.lib.client.studentservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.engine.security.DefaultSslContextFactory;
import org.restlet.representation.Representation;
import org.restlet.util.Series;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Client for the Student Service.
 */
public class StudentServiceClientImpl
	implements StudentServiceClient {

	private static Log logger = LogFactory.getLog(StudentServiceClientImpl.class);

    private static final String SERVICE_VERSION = "v4";

    private String baseUrl;
	private SAXReader reader;
	private Client client;

    public StudentServiceClientImpl() {
		reader = new SAXReader();
	}

    /**
     *  Initialize for non-SSL connections.
     */
	public StudentServiceClientImpl(String baseUrl) {
		this();
		
		logger.info("Initializing for [" + baseUrl + "] version [" + SERVICE_VERSION + "].");
		
		setBaseUrl(baseUrl);
		
		client = new Client(Protocol.HTTP);
	}

    /**
     *  Initialize for SSL connections.
     */
	public StudentServiceClientImpl(String baseUrl,
                                    String trustStoreFilename, String trustStorePasswd,
                                    String keyStoreFilename, String keyStorePasswd) {
		this(baseUrl);

		logger.info("Initializing SSL with truststore [" + trustStoreFilename + "] and keystore [" + keyStoreFilename + "].");
		
		if (logger.isDebugEnabled()) {
			System.setProperty("javax.net.debug", "ssl");
		}
		//  This is needed for the re-negotiation to succeed. 
		System.setProperty("sun.security.ssl.allowUnsafeRenegotiation", "true");
	
		//  Configure for SSL connections with certificate authentication.
		Context context = new Context();
	
		Series<Parameter> parameters = context.getParameters();
        parameters.add("truststorePath", trustStoreFilename);
        parameters.add("truststorePassword", trustStorePasswd);
        parameters.add("keystorePath", keyStoreFilename);
        parameters.add("keystorePassword", keyStorePasswd);
        
        DefaultSslContextFactory contextFactory = new DefaultSslContextFactory();
        contextFactory.init(parameters);
        
        context.getAttributes().put("sslContextFactory", contextFactory);
 
		client = new Client(context, Protocol.HTTPS);
	}

    /**
     * @inheritDoc
     */
	public void setBaseUrl(String baseUrl) {
		this.baseUrl = baseUrl;
	}
	
	/**
     * @inheritDoc
	 */
	public String getBaseUrl() {
		return baseUrl;
	}

    /**
     * @inheritDoc
     */
	public String getServiceVersion() {
		return SERVICE_VERSION;
	}

	/**
	 * {@inheritDoc}
	 */
    @Override
	public List<String> getAvailableVersions() throws ServiceException {
		Request request = new Request(Method.GET, getBaseUrl() + "/");

		//  Send the request and parse the result.
		Response response = client.handle(request);
		
		Status status = response.getStatus();
		if ( ! (status.equals(Status.SUCCESS_OK))) {
			throw new ServiceException("Unable to get versions. " 
					+ status.getName() + " (" + status.getCode() + "): " + status.getDescription());
		}
		
		// !!! This can only be called once.
		Representation output = response.getEntity();
		Document document = null;
		
		try {
			document = reader.read(output.getStream());
		} catch (Exception e) {                     
			throw new ServiceException("Could not read the reply.", e);               
		}

        DefaultXPath xpath = new DefaultXPath("//x:a[@class='version']");
        Map<String,String> namespaces = new HashMap<String,String>();
        namespaces.put("x","http://www.w3.org/1999/xhtml");
        xpath.setNamespaceURIs(namespaces);

        List<?> nodes = xpath.selectNodes(document);

		List<String> versions = new ArrayList<String>();
		for (Object node : nodes) {
            Element e = (Element) node;
            versions.add(e.getTextTrim());
        }

		return versions;
	}

    /**
     *  Fetches section info as XML for a given year, quarter, and curriculum.
     *
     * @param year
     * @param quarter
     * @param curriculum
     * @return
     * @throws ServiceException
     */
    @Override
    public String getSectionInfo(String year, String quarter, String curriculum) throws ServiceException {
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
            .append("/").append("public/section.xml?")
            .append("year=").append(year).append("&")
            .append("quarter=").append(quarter).append("&")
            .append("curriculum_abbreviation=").append(curriculum);
        return sendQuery(url.toString());
    }

    @Override
    public String getTimeScheduleLinkAbbreviation(String year, String term, String curriculumCode) throws ServiceException {
        /*
         *  First get the section info for the given term and curriculum.
         */
        String sectionXml = getSectionInfo(year, term, curriculumCode);
        Document document;
        try {
			document = reader.read(new StringReader(sectionXml));
		} catch (Exception e) {
			throw new ServiceException("Could not read the reply.", e);
		}

        /*
         *  Parse a section href out of the response.
         */
        Map<String,String> namespaces = new HashMap<String,String>();
        namespaces.put("sws","http://webservices.washington.edu/student/");

        String xpathExpression = "//sws:Section/sws:Href";
        DefaultXPath xpath = new DefaultXPath(xpathExpression);
        xpath.setNamespaceURIs(namespaces);

        Node node = xpath.selectSingleNode(document);
        if (node == null) {
            throw new ServiceException(String.format("XPath query for [%s] produced no results.", xpathExpression));
        }

        Element e = null;
        try {
            e = (Element) node;
        } catch (ClassCastException cce) {
            throw new ServiceException("Href was not an Element.", cce);
        }

        /*
         *  Query up the section and read the TimeScheduleLinkAbbreviation from the response.
         */
        String sectionUrl = e.getTextTrim();
        //  Get rid of the service name since we supply it in the baseUrl.
        sectionUrl = sectionUrl.replace("student/", "");
        String url =  baseUrl + sectionUrl;

        logger.info("Querying SWS section service at [" + url + "].");
        String sectionInfoXml = sendQuery(baseUrl + sectionUrl);
        try {
			document = reader.read(new StringReader(sectionInfoXml));
		} catch (Exception ex) {
			throw new ServiceException("Could not read the reply.", ex);
		}

        xpathExpression = "/sws:Section/sws:Curriculum/sws:TimeScheduleLinkAbbreviation";
        xpath = new DefaultXPath(xpathExpression);
        xpath.setNamespaceURIs(namespaces);
        node = xpath.selectSingleNode(document);
        if (node == null) {
            throw new ServiceException(String.format("XPath query for [%s] produced no results.", xpathExpression));
        }

        try {
            e = (Element) node;
        } catch (ClassCastException cce) {
            throw new ServiceException("TimeScheduleLinkAbbreviation was not an Element.", cce);
        }

        return e.getTextTrim();
    }

    /**
     * @inheritDoc
     *
     * "/student/v4/public/term/current.xml"
     *
     * @return
     */
    @Override
    public String getCurrentTerm() throws ServiceException {
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
            .append("/").append("public/term/current.xml");
        return sendQuery(url.toString());
    }

    /**
     * @inheritDoc
     *
     * Query the term service as /student/v4/public/term/2009,winter.xml
     */
    @Override
    public String getTermInfo(String year, String quarter) throws ServiceException {
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/")
            .append(getServiceVersion())
            .append("/").append("public/term/")
            .append(year).append(",")
            .append(quarter).append(".xml");
        return sendQuery(url.toString());
    }

    /**
     *
     * @param year
     * @param Curriculum
     * @param courseNo
     * @return
     * @throws ServiceException
     */
    public String getSections(String year, String Curriculum, String courseNo) throws ServiceException{
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/section.xml?")
                .append("year=").append(year).append("&")
                .append("quarter=").append("&")
                .append("curriculum_abbreviation=").append(Curriculum).append("&")
        .append("course_number=").append(courseNo);
        return  sendQuery(url.toString().trim());




    }

    private String sendQuery(String url) throws ServiceException {
        Request request = new Request(Method.GET, url);

		//  Send the request and parse the result.
		Response response = client.handle(request);
		Status status = response.getStatus();

		if ( ! (status.equals(Status.SUCCESS_OK))) {
            throw new ServiceException(String.format("Query failed to URL [%s] - %s (%s): %s",
                    url, status.getName(), status.getCode(), status.getDescription()));
		}

        //  !!! getEntity() can only be called once.
        Representation representation = response.getEntity();
        String responseText = null;
        try {
            responseText = representation.getText();
        } catch (IOException e) {
            throw new ServiceException("Could not read response.", e);
        }
        return responseText;
    }
}
