package edu.uw.kuali.student.lib.client.studentservice;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.restlet.Client;
import org.restlet.data.Protocol;

import java.util.ArrayList;
import java.util.List;

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
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", "section.id:\"" + id + "\"");
        params.set("fl", "section.data");
        params.set("sort", "section.id asc");
        params.set("rows", "9999");
        List<SolrDocument> documents = sendQuery(params);
        if (documents != null && !documents.isEmpty()) {
            return documents.get(0).getFieldValue("section.data").toString();
        }
        return null;
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
    public List<String> getPrimarySections(String year, String term, String curriculumAbbreviation, String courseNumber) throws ServiceException {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", "section.year:" + year + " AND section.term:" + term + " AND section.curriculum.abbreviation:" + curriculumAbbreviation + " AND section.course.number:" + courseNumber + " AND section.primary:true");
        params.set("fl", "section.data");
        params.set("sort", "section.id asc");
        params.set("rows", "9999");
        List<SolrDocument> documents = sendQuery(params);
        List<String> sectionDataList = new ArrayList<String>();
        for (SolrDocument document : documents) {
            sectionDataList.add(document.getFieldValue("section.data").toString());
        }
        return sectionDataList;
    }

    /**
     * returns only the secondary sections excluding the primary section using the primarySectionId
     *
     * @param primarySectionId
     * @return
     * @throws ServiceException
     */
    public List<String> getSecondarySections(String primarySectionId) throws ServiceException {
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", "section.primary.id:\"" + primarySectionId + "\" AND section.primary:false");
        params.set("fl", "section.data");
        params.set("sort", "section.id asc");
        params.set("rows", "9999");
        List<SolrDocument> documents = sendQuery(params);
        List<String> sectionDataList = new ArrayList<String>();
        for (SolrDocument document : documents) {
            sectionDataList.add(document.getFieldValue("section.data").toString());
        }
        return sectionDataList;

    }

    /**
     * returns both primary and secondary sections by PrimarySectionId
     *
     * @param primarySectionId
     * @return
     * @throws ServiceException
     */
    public List<String> getPrimaryAndSecondarySections(String primarySectionId) throws ServiceException {
        /*TODO: Add a check to get only suspended and active sections*/
        ModifiableSolrParams params = new ModifiableSolrParams();
        params.set("q", "section.id:\"" + primarySectionId + "\" OR section.primary.id:\"" + primarySectionId + "\"");
        params.set("fl", "section.data");
        params.set("sort", "section.id asc");
        params.set("rows", "9999");
        List<SolrDocument> documents = sendQuery(params);
        List<String> sectionDataList = new ArrayList<String>();
        for (SolrDocument document : documents) {
            sectionDataList.add(document.getFieldValue("section.data").toString());
        }
        return sectionDataList;

    }

    public List<SolrDocument> sendQuery(ModifiableSolrParams params) throws ServiceException {
        List<SolrDocument> documents = new ArrayList<SolrDocument>();
        try {
            SolrServer server = new HttpSolrServer(solrBaseUrl);
            QueryResponse queryResponse = server.query(params);
            documents = queryResponse.getResults();

        } catch (Exception e) {
            throw new ServiceException("Could not read solr response.", e);
        }
        return documents;
    }


}
