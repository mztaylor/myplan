package edu.uw.kuali.student.lib.client.studentservice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Client for the Student Service.
 */
public class StudentServiceClientImpl
        implements StudentServiceClient {

    //	public static void main( String[] args ) throws Exception {
//		String baseUrl = "https://ucswseval1.cac.washington.edu/student";
//		String keyStoreFilename = "/Users/jasonosgood/kuali/main/hemanth/uwkstest.jks";
//		String keyStorePasswd = "changeit";
//		String trustStoreFilename = "/Users/jasonosgood/kuali/main/hemanth/uw.jts";
//		String trustStorePasswd = "secret";
//		StudentServiceClientImpl impl = new StudentServiceClientImpl( baseUrl, keyStoreFilename, keyStorePasswd, trustStoreFilename, trustStorePasswd );
//		// https://ucswseval1.cac.washington.edu/student/v4/course/2013,spring,ENGL,131/Z/status.xml
//		long quick = Long.MAX_VALUE;
//		long slow = Long.MIN_VALUE;
//		long zerosecond = 0;
//		long halfsecond = 0;
//		long onesecond = 0;
//		long twosecond = 0;
//		long threesecond = 0;
//		int count = 0;;
//		long start = System.currentTimeMillis();
//		for( int nth = 0; nth < 10; nth++ ) 
//		{
//		String xml = null;
//		for(char section = 'A'; section != 'Z'; section++ ) {
//			count++;
//			String gorp = Character.toString( section );
//			long kisskiss = System.currentTimeMillis();
//			xml = impl.getSectionStatus("2013", "spring", "ENGL", "131", gorp);
//			long bangbang = System.currentTimeMillis() - kisskiss;
//			quick = Math.min( bangbang, quick );
//			slow = Math.max( bangbang, slow );
//			if( bangbang < 500 ) zerosecond++;
//			if( bangbang > 499 && bangbang < 1000 ) halfsecond++;
//			if( bangbang > 999 && bangbang < 2000 ) onesecond++;
//			if( bangbang > 1999 && bangbang < 3000 ) twosecond++;
//			if( bangbang > 2999 ) threesecond++;
//			
//		}
//		}
//		long elapsed = System.currentTimeMillis() - start;
//		System.out.println( "done ");
//		System.out.println( "elapsed " + elapsed );
//		System.out.println( "count " + count );
//		System.out.println( "average " + ( elapsed / count ));
//		
//		System.out.println( "quickest " + quick );
//		System.out.println( "slowest " + slow );
//		
//		System.out.println( "zerosecond " + zerosecond );
//		System.out.println( "halfsecond " + halfsecond );
//		System.out.println( "onesecond " + onesecond );
//		System.out.println( "twosecond " + twosecond );
//		System.out.println( "threesecond " + threesecond );
//	}
    private static Log logger = LogFactory.getLog(StudentServiceClientImpl.class);

    private static final String SERVICE_VERSION = "v4";

    private String baseUrl;
    private Client client;

    @Override
    public Client getClient() {
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
        curriculum = curriculum.replace(" ", "%20");
        curriculum = curriculum.replace("&", "%26");
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/section.xml?")
                .append("year=").append(year).append("&")
                .append("quarter=").append(quarter).append("&")
                .append("curriculum_abbreviation=").append(curriculum);
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
     * https://ucswseval1.cac.washington.edu/student/v4/public/section.xml?year=2003&quarter=&curriculum_abbreviation=ECON&course_number=299&delete_flag=active
     *
     * @param year
     * @param abbrev
     * @param num
     * @return
     * @throws ServiceException
     */
    public String getSections(String year, String abbrev, String num) throws ServiceException {
        abbrev = abbrev.replace(" ", "%20");
        abbrev = abbrev.replace("&", "%26");
        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/section.xml?")
                .append("year=").append(year).append("&")
                .append("quarter=").append("&")
                .append("curriculum_abbreviation=").append(abbrev).append("&")
                .append("course_number=").append(num).append("&delete_flag=active");
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
        abbrev = abbrev.replace(" ", "%20");
        abbrev = abbrev.replace("&", "%26");

        StringBuilder url = new StringBuilder(getBaseUrl());
        url.append("/").append(getServiceVersion())
                .append("/").append("public/section.xml?")
                .append("year=").append(year).append("&")
                .append("quarter=").append(quarter).append("&")
                .append("curriculum_abbreviation=").append(abbrev).append("&")
                .append("course_number=").append(num)
//                .append("&include_secondaries=on")
        ;
        return sendQuery(url.toString().trim());
    }

    /*
        /student/v4/public/course/2012,autumn,CHEM,110/A.xml
     */
    public String getSecondarySections(String year, String quarter, String abbrev, String num, String section)
            throws ServiceException {
        String base = getBaseUrl();
        String ver = getServiceVersion();

        abbrev = abbrev.replace(" ", "%20");
        abbrev = abbrev.replace("&", "%26");
        String url = String.format("%s/%s/public/course/%s,%s,%s,%s/%s.xml", base, ver, year, quarter, abbrev, num, section);
        return sendQuery(url);
    }

    /**
     * Lighter weight query used for checking just enrollment info: current #, limit #, estimate/limited, etc.
     * <p/>
     * /student/v4/course/2013,winter,ASTR,101/A/status.xml
     */
    public String getSectionStatus(String year, String quarter, String abbrev, String num, String section) throws ServiceException {
        String base = getBaseUrl();
        String ver = getServiceVersion();

        abbrev = abbrev.replace(" ", "%20");
        abbrev = abbrev.replace("&", "%26");
        String url = String.format("%s/%s/course/%s,%s,%s,%s/%s/status.xml", base, ver, year, quarter, abbrev, num, section);
        return sendQuery(url);

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
        curriculum = curriculum.replace(" ", "%20");
        curriculum = curriculum.replace("&", "%26");
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

    @Override
    public boolean connectionStatus(String url) {
        boolean connectionEstablished = true;
        Request request = new Request(Method.GET, url);

        //  Send the request and parse the result.
        Response response = client.handle(request);
        Status status = response.getStatus();
        if (status.isError()) {
            connectionEstablished = false;
            String msg = String.format("Query failed to URL [%s] - %s", url, status.toString());
            logger.error(msg);

        }
        System.out.println("connect status: " + url + " " + connectionEstablished);
        return connectionEstablished;
    }

    public String sendQuery(String url) throws ServiceException {
        System.out.println(url);
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
        try {
            String responseText = representation.getText();
            return responseText;
        } catch (IOException e) {
            throw new ServiceException("Could not read response.", e);
        }
    }
}
