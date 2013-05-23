package edu.uw.kuali.student.lib.client.studentservice;

import org.kuali.rice.core.api.config.property.ConfigContext;
import org.restlet.Client;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Status;
import org.restlet.representation.Representation;

import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/15/13
 * Time: 4:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class SolrServiceClientImpl implements SolrSeviceClient {

    private Client client;
    private String solrBaseUrl;

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public String getSolrBaseUrl() {
        return solrBaseUrl;
    }

    public void setSolrBaseUrl(String solrBaseUrl) {
        this.solrBaseUrl = solrBaseUrl;
    }

    public SolrServiceClientImpl(String baseUrl) {
        setSolrBaseUrl(baseUrl);
        client = new Client(Protocol.HTTP);
    }

    /**
     * url: https://uwksdev01.cac.washington.edu/solr/myplan/select?q=section.id:"2013:2:CHEM:152:A"&sort=section.id%20asc&fl=section.data&wt=xml&indent=true&rows=9999
     *
     * @param id
     * @return
     */
    public String getSectionById(String id) throws ServiceException {
        String url = getSolrBaseUrl() + "select?q=section.id:\"" + id + "\"&sort=section.id%20asc&fl=section.data&wt=xml&indent=true&rows=9999";
        return sendQuery(url);
    }

    /**
     * eg: https://uwksdev01.cac.washington.edu/solr/myplan/select?q=section.year:2013%20AND%20section.term:spring%20AND%20section.curriculum.abbreviation:CHEM%20AND%20section.course.number:152%20AND%20section.primary:true&sort=section.id%20asc&fl=section.data&wt=xml&indent=true&rows=9999
     * returns xml response which has the
     *
     * @param year
     * @param term
     * @param curriculumAbbreviation
     * @param courseNumber
     * @return
     */
    public String getPrimarySections(String year, String term, String curriculumAbbreviation, String courseNumber) throws ServiceException {
        String url = getSolrBaseUrl() + "select?q=section.year:" + year + "%20AND%20section.term:" + term + "%20AND%20section.curriculum.abbreviation:" + curriculumAbbreviation + "%20AND%20section.course.number:" + courseNumber + "%20AND%20section.primary:true&sort=section.id%20asc&fl=section.data&rows=9999";
        return sendQuery(url);
    }

    /**
     * returns only the secondary sections excluding the primary section using the primarySectionId
     *
     * @param primarySectionId
     * @return
     * @throws ServiceException
     */
    public String getSecondarySections(String primarySectionId) throws ServiceException {
        String url = getSolrBaseUrl() + "select?q=section.primary.id:\"" + primarySectionId + "\"%20AND%20section.primary:false&sort=section.id%20asc&fl=section.data&rows=9999";
        return sendQuery(url);

    }

    /**
     * returns both primary and secondary sections by PrimarySectionId
     *
     * @param primarySectionId
     * @return
     * @throws ServiceException
     */
    public String getPrimaryAndSecondarySections(String primarySectionId) throws ServiceException {
        /*TODO: Add a check to get only suspended and active sections*/
        String url = getSolrBaseUrl() + "select?q=section.id:\"" + primarySectionId + "\"%20OR%20section.primary.id:\"" + primarySectionId + "\"&sort=section.id%20asc&fl=section.data&rows=9999";
        return sendQuery(url);

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
