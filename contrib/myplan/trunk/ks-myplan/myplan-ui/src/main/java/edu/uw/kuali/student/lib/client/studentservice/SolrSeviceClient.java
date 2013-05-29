package edu.uw.kuali.student.lib.client.studentservice;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: hemanthg
 * Date: 5/15/13
 * Time: 4:56 PM
 * To change this template use File | Settings | File Templates.
 */
public interface SolrSeviceClient {

    public String getSectionById(String id) throws ServiceException;

    public List<String> getPrimarySections(String year, String term, String curriculumAbbreviation, String courseNumber) throws ServiceException;

    public List<String> getSecondarySections(String primarySectionId) throws ServiceException;

    public List<String> getPrimaryAndSecondarySections(String primarySectionId) throws ServiceException;

}
