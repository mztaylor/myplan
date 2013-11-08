package edu.uw.kuali.student.lib.client.studentservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Text;
import org.dom4j.io.SAXReader;
import org.dom4j.xpath.DefaultXPath;
import org.kuali.rice.kim.api.identity.Person;
import org.kuali.rice.kim.impl.identity.PersonImpl;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Parameter;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.ext.net.HttpClientHelper;
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
    private HttpClientHelper client;

    @Override
    public HttpClientHelper getClient() {
        return client;
    }

//    public StudentServiceClientImpl() {
//    }

    /**
     * Initialize for non-SSL connections.
     */
//    public StudentServiceClientImpl(String baseUrl) {
//        this();
//
//        logger.info("Initializing for [" + baseUrl + "] version [" + SERVICE_VERSION + "].");
//
//        setBaseUrl(baseUrl);
//
//        client = new Client(Protocol.HTTP);
//    }

    /**
     * Initialize for SSL connections.
     */
    public StudentServiceClientImpl(String baseUrl, String keyStoreFilename, String keyStorePasswd, String trustStoreFilename, String trustStorePasswd) {
//        this(baseUrl);
        setBaseUrl(baseUrl);

        logger.info("Initializing SSL with truststore [" + trustStoreFilename + "] and keystore [" + keyStoreFilename + "].");

        /*if (logger.isDebugEnabled()) {*/
//        System.setProperty("javax.net.debug", "ssl");
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

        client = new HttpClientHelper(new Client(context, Protocol.HTTPS));
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
        Response response = new Response(request);
        client.handle(request, response);
        Status status = response.getStatus();
        if (status.isError()) {
            throw new ServiceException("Unable to get available versions: " + status);
        }


        try {
            SAXReader reader = new SAXReader();
            // !!! This can only be called once.
            Representation output = response.getEntity();
            Document document = reader.read(output.getStream());
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
        } catch (Exception e) {
            throw new ServiceException("Could not read the reply.", e);
        }

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

        curriculum = urlEscape(curriculum);
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/section.xml?")
                .append("year=").append(year).append("&")
                .append("quarter=").append(quarter).append("&")
                .append("curriculum_abbreviation=").append(curriculum).append("&delete_flag=suspended,active");
        return sendQuery(url.toString());
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
     * https://ucswseval1.cac.washington.edu/student/v4/public/section.xml?year=2003&quarter=&curriculum_abbreviation=ECON&course_number=299
     *
     * @param year
     * @param abbrev
     * @param num
     * @return
     * @throws ServiceException
     */
    public String getSections(String year, String abbrev, String num, int futureTerms) throws ServiceException {

        abbrev = urlEscape(abbrev);
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/section.xml?")
                .append("year=").append(year).append("&")
                .append("quarter=").append("&")
                .append("curriculum_abbreviation=").append(abbrev).append("&")
                .append("course_number=").append(num).append("&delete_flag=suspended,active&future_terms=").append(futureTerms);
        return sendQuery(url.toString().trim());
    }

    /**
     * /student/v4/public/section?year=2013&quarter=spring&curriculum_abbreviation=ENGL&course_number=102&include_secondaries=on
     *
     * @param year
     * @param quarter
     * @param abbrev
     * @param num
     * @return
     * @throws ServiceException
     */
    public String getSections(String year, String quarter, String abbrev, String num) throws ServiceException {

        abbrev = urlEscape(abbrev);
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/section.xml?")
                .append("year=").append(year).append("&")
                .append("quarter=").append(quarter).append("&")
                .append("curriculum_abbreviation=").append(abbrev).append("&")
                .append("course_number=").append(num).append("&delete_flag=suspended,active");
        return sendQuery(url.toString().trim());
    }

    /*
        /student/v4/public/course/2012,autumn,CHEM,110/A.xml
     */
    public String getSecondarySections(String year, String quarter, String abbrev, String num, String section)
            throws ServiceException {

        String base = getBaseUrl();
        String ver = getServiceVersion();
        abbrev = urlEscape(abbrev);
        String url = String.format("%s/%s/public/course/%s,%s,%s,%s/%s.xml", base, ver, year, quarter, abbrev, num, section);
        return sendQuery(url);
    }

    /**
     * Lighter weight query used for checking just enrollment info: current #, limit #, estimate/limited, etc.
     * <p/>
     * /student/v4/course/2013,winter,ASTR,101/A/status.xml
     */
    public String getSectionStatus(String year, String quarter, String abbrev, String num, String section)
            throws ServiceException {

        String base = getBaseUrl();
        String ver = getServiceVersion();
        abbrev = urlEscape(abbrev);
        String url = String.format("%s/%s/course/%s,%s,%s,%s/%s/status.xml", base, ver, year, quarter, abbrev, num, section);
        return sendQuery(url);
    }

    /**
     * Get enrollment info for all sections of a course
     * hits SWS with url of .../student/v4/course/2013,winter,ASTR,101/status.xml
     *
     * @param year    year for term to get info for
     * @param quarter quarter for term to get info for
     * @param abbrev  curriculum for course to get info for
     * @param num     course number for course to get info for
     * @return potentially very lengthy xml formatted string of results from SWS query
     * @throws ServiceException
     */
    public String getAllSectionsStatus(String year, String quarter, String abbrev, String num) throws ServiceException {

        String base = getBaseUrl();
        String ver = getServiceVersion();
        abbrev = urlEscape(abbrev);
        String url = String.format("%s/%s/course/%s,%s,%s,%s/status.xml", base, ver, year, quarter, abbrev, num);
        return sendQuery(url);
    }

    @Override
    public String getCurriculumForSubject(String year, String quarter, String abbrev) throws ServiceException {
        abbrev = urlEscape(abbrev);
        String url = String.format("%s/%s/public/curriculum.xml?year=%s&quarter=%s&department_abbreviation=%s",getBaseUrl(),getServiceVersion(), year, quarter, abbrev);
        return sendQuery(url);
    }

    /**
     * @param regId
     * @return
     * @throws ServiceException
     */
    @Override
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
    public String getTimeSchedules(String year, String term, String curriculum, String courseNumber, String sectionUrl)
            throws ServiceException {

        curriculum = urlEscape(curriculum);
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

//    /**
//     * https://ucswseval1.cac.washington.edu/student/v4/person/{regid}.xml
//     *
//     * @param regId
//     * @return
//     * @throws ServiceException
//     */
//    @Override
//    public String getPersonByRegID(String regId) throws ServiceException {
//
//        String base = getBaseUrl();
//        String ver = getServiceVersion();
//
//        String url = String.format("%s/%s/person/%s.xml", base, ver, regId);
//        return sendQuery(url);
//    }


    static class MyPerson extends PersonImpl {
        public MyPerson(String regid, String first, String last) {
            principalId = regid;
            firstName = first;
            lastName = last;
        }


        @Override
        public String getFirstName() {
            return firstName;
        }

        @Override
        public String getLastName() {
            return lastName;
        }

        @Override
        public String getPrincipalId() {
            return principalId;
        }
    }

    ;

    /**
     * https://ucswseval1.cac.washington.edu/student/v4/person.xml?student_system_key=01234567
     *
     * @param syskey
     * @return
     * @throws ServiceException
     */
    @Override
    public Person getPersonBySysKey(String syskey) throws ServiceException {

        try {
            String base = getBaseUrl();
            String ver = getServiceVersion();

            String url = String.format("%s/%s/person.xml?student_system_key=%s", base, ver, syskey);
            String xml = sendQuery(url);
            SAXReader sax = new SAXReader();
            StringReader sr = new StringReader(xml);
            Document doc = sax.read(sr);
            Map<String, String> namespaces = new HashMap<String, String>();
            namespaces.put("x", "http://webservices.washington.edu/student/");
            DefaultXPath firstNamePath = new DefaultXPath("/x:SearchResults/x:Persons/x:Person/x:FirstName/text()");
            firstNamePath.setNamespaceURIs(namespaces);
            DefaultXPath lastNamePath = new DefaultXPath("/x:SearchResults/x:Persons/x:Person/x:LastName/text()");
            lastNamePath.setNamespaceURIs(namespaces);
            DefaultXPath regidPath = new DefaultXPath("/x:SearchResults/x:Persons/x:Person/x:RegID/text()");
            regidPath.setNamespaceURIs(namespaces);

            Text firstNameText = (Text) firstNamePath.selectSingleNode(doc);
            Text lastNameText = (Text) lastNamePath.selectSingleNode(doc);
            Text regidText = (Text) regidPath.selectSingleNode(doc);
            String firstName = firstNameText.getText();
            String lastName = lastNameText.getText();
            String regid = lastNameText.getText();
            MyPerson person = new MyPerson(regid, firstName, lastName);
            return person;
        } catch (Exception e) {
            throw new ServiceException("cannot get person", e);
        }


    }

    @Override
    public boolean connectionStatus(String url) {
        boolean connectionEstablished = true;
        Request request = new Request(Method.GET, url);

        //  Send the request and parse the result.
        Response response = new Response(request);
        client.handle(request, response);
        Status status = response.getStatus();
        if (status.isError()) {
            connectionEstablished = false;
            String msg = String.format("Query failed to URL [%s] - %s", url, status.toString());
            logger.error(msg);

        }
        logger.debug("connect status: " + url + " " + connectionEstablished);
        return connectionEstablished;
    }

    public String sendQuery(String url) throws ServiceException {
        System.out.println(url);
        Request request = new Request(Method.GET, url);

        //  Send the request and parse the result.
        Response response = new Response(request);
        client.handle(request, response);
        Status status = response.getStatus();

        if (!(status.equals(Status.SUCCESS_OK))) {
            throw new ServiceException(String.format("Query failed to URL [%s] - %s (%s): %s",
                    url, status.getName(), status.getCode(), status.getDescription()));
        }

        //  !!! getEntity() can only be called once.
        Representation representation = response.getEntity();
        try {
            String responseText = representation.getText();
            return responseText;
        } catch (IOException e) {
            throw new ServiceException("Could not read response.", e);
        }
    }

    private String urlEscape(String text) {
        text = text.replace(" ", "%20");
        text = text.replace("&", "%26");
        return text;
    }

}
