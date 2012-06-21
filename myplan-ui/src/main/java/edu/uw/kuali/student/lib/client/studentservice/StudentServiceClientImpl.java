package edu.uw.kuali.student.lib.client.studentservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.student.myplan.plan.util.AtpHelper;
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

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.util.*;


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
     * Initialize for non-SSL connections.
     */
    public StudentServiceClientImpl(String baseUrl) {
        this();

        logger.info("Initializing for [" + baseUrl + "] version [" + SERVICE_VERSION + "].");

        setBaseUrl(baseUrl);

        client = new Client(Protocol.HTTP);
    }

    /**
     * Initialize for SSL connections.
     */
    public StudentServiceClientImpl(String baseUrl, String keyStoreFilename, String keyStorePasswd, String trustStoreFilename, String trustStorePasswd) {
        this(baseUrl);

        logger.info("Initializing SSL with truststore [" + trustStoreFilename + "] and keystore [" + keyStoreFilename + "].");

        /*if (logger.isDebugEnabled()) {*/
        System.setProperty("javax.net.debug", "ssl");
        /*}*/
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
        if (!(status.equals(Status.SUCCESS_OK))) {
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
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("x", "http://www.w3.org/1999/xhtml");
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
     * Fetches section info as XML for a given year, quarter, and curriculum.
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

    public Set<String> getTimeSchedulesAbbreviations(String year, String term, String curriculumCode, String courseNumber) throws ServiceException {
        String responseText = null;
        Set<String> timeScheduleAbbr = new HashSet<String>();
        List<String> hrefs = new ArrayList<String>();
        try {
            responseText = getTimeSchedules(year, term, curriculumCode, courseNumber, null);
        } catch (ServiceException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        Document document = null;
        try {
            document = reader.read(new StringReader(responseText));
        } catch (Exception e) {
            throw new RuntimeException("Could not parse reply from the Student Term Service.", e);
        }

        DefaultXPath xpath = new DefaultXPath("//s:Sections");
        Map<String, String> namespaces = new HashMap<String, String>();
        namespaces.put("s", "http://webservices.washington.edu/student/");
        xpath.setNamespaceURIs(namespaces);
        List sections = xpath.selectNodes(document);
        if (sections != null) {
            StringBuffer cc = new StringBuffer();
            for (Object node : sections) {
                Element element = (Element) node;
                List<?> sectionlist = new ArrayList<Object>();
                sectionlist = element.elements("Section");
                for (Object section : sectionlist) {
                    Element secElement = (Element) section;
                    hrefs.add(secElement.elementText("Href"));
                }

            }
        }

        if (hrefs.size() > 0) {

            for (String href : hrefs) {
                href = href.replace("/student", "").trim();
                try {
                    responseText = getTimeSchedules(null, null, null, null, href);
                } catch (ServiceException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
                Document document2 = null;
                try {
                    document2 = reader.read(new StringReader(responseText));
                } catch (Exception e) {
                    throw new RuntimeException("Could not parse reply from the Student Term Service.", e);
                }
                DefaultXPath xpath2 = new DefaultXPath("//s:Curriculum");
                Map<String, String> namespaces2 = new HashMap<String, String>();
                namespaces2.put("s", "http://webservices.washington.edu/student/");
                xpath2.setNamespaceURIs(namespaces2);
                List curriculum = xpath2.selectNodes(document2);
                if (curriculum != null) {
                    StringBuffer cc = new StringBuffer();
                    for (Object node : curriculum) {
                        Element element = (Element) node;
                        timeScheduleAbbr.add(element.elementText("TimeScheduleLinkAbbreviation"));

                    }
                }

            }
        }

        return timeScheduleAbbr;
    }


    /**
     * @return
     * @inheritDoc "/student/v4/public/term/current.xml"
     */
    @Override
    public String getCurrentTerm() throws ServiceException {
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/term/current.xml");
        return sendQuery(url.toString());
    }

    /**
     * @inheritDoc Query the term service as /student/v4/public/term/2009,winter.xml
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
     * @param year
     * @param Curriculum
     * @param courseNo
     * @return
     * @throws ServiceException
     */
    public String getSections(String year, String Curriculum, String courseNo) throws ServiceException {
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/section.xml?")
                .append("year=").append(year).append("&")
                .append("quarter=").append("&")
                .append("curriculum_abbreviation=").append(Curriculum).append("&")
                .append("course_number=").append(courseNo);
        return sendQuery(url.toString().trim());

    }

    /**
     * @param regId
     * @return
     * @throws ServiceException
     */
    public String getAcademicRecords(String regId, String year, String term, String registrationUrl) throws ServiceException {
        StringBuilder url = new StringBuilder(getBaseUrl());
        if (registrationUrl != null) {
            url.append(registrationUrl);
        } else if (regId != null && year != null && term != null && registrationUrl == null) {
            url.append("/").append(getServiceVersion()).append("/").append("registration.xml?year=").append(year).append("&")
                    .append("quarter=").append(term).append("&").append("reg_id=").append(regId).append("&is_active=on");
        } else {
            url.append("/").append(getServiceVersion()).append("/")
                    .append("enrollment.xml?reg_id=").append(regId).append("&").append("verbose=on");
        }
        return sendQuery(url.toString().trim());

    }

    @Override
    public String getTimeSchedules(String year, String term, String curriculum, String courseNumber, String sectionUrl) throws ServiceException {
        StringBuilder url = new StringBuilder(getBaseUrl());
        if (sectionUrl != null) {
            url.append(sectionUrl);
        } else {
            url.append("/").append(getServiceVersion())
                    .append("/").append("public/section.xml?")
                    .append("year=").append(year).append("&")
                    .append("quarter=").append(term).append("&")
                    .append("curriculum_abbreviation=").append(curriculum).append("&course_number=").append(courseNumber);
        }
        return sendQuery(url.toString().trim());

    }

    private String sendQuery(String url) throws ServiceException {
        Request request = new Request(Method.GET, url);

        //  Send the request and parse the result.
        Response response = client.handle(request);
        Status status = response.getStatus();

        if (!(status.equals(Status.SUCCESS_OK))) {
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
