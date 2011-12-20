package org.kuali.student.myplan.course.controller;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.course.form.CourseSearchForm;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * User: jasonosgood
 * Date: 12/5/11
 * Time: 10:45 AM
 */
public class CourseSearchStrategy
{
    private transient LuService luService;

    //Note: here I am using r1 LuService implementation!!!
    protected LuService getLuService() {
        if (luService == null) {
            luService = (LuService) GlobalResourceLoader.getService(new QName(LuServiceConstants.LU_NAMESPACE, "LuService"));
        }
        return this.luService;
    }

    public HashMap<String,String> fetchCourseDivisions()
    {
        HashMap<String,String> map = new HashMap<String,String>();
        try
        {
            SearchRequest request = new SearchRequest( "myplan.distinct.clu.divisions" );

            SearchResult result = getLuService().search( request );

            for( SearchResultRow row : result.getRows() )
            {
                for( SearchResultCell cell : row.getCells() )
                {
                    String division = cell.getValue();
                    // Store both trimmed and original, because source data
                    // is sometimes space padded.
                    String key = division.trim().replaceAll( "\\s+", "" );
                    map.put( key, division );
                }
            }
        }
        catch( Exception e )
        {
            // TODO: Handle this exception better
            e.printStackTrace();
        }
        return map;
    }


    public List<SearchRequest> queryToRequests( CourseSearchForm courseSearchForm )
        throws Exception
    {
        ArrayList<SearchRequest> requests = new ArrayList<SearchRequest>();

        HashMap<String,String> divisionMap = fetchCourseDivisions();


        String query = courseSearchForm.getSearchQuery().toUpperCase();
        QueryTokenizer tokenizer = new QueryTokenizer();

        List<String> levels = tokenizer.extractCourseLevels(query);
        for( String level : levels )
        {
            query = query.replace( level, "" );
        }
        List<String> codes = tokenizer.extractCourseCodes(query);
        for( String code : codes )
        {
            query = query.replace( code, "" );
        }

        // Remove spaces, make upper case
        query = query.trim().replaceAll( "\\s+", " " );

        ArrayList<String> divisions = new ArrayList<String>();

        boolean match = true;
        while( match )
        {
            match = false;
            List<QueryTokenizer.Token> tokens = tokenizer.tokenize( query );
            List<String> terms = new TokenPairs( tokens ).sortedLongestFirst();

            Iterator<String> i = terms.iterator();
            while( match == false && i.hasNext() )
            {
                String term = i.next();

                String key = term.replace( " ", "" );
                if( divisionMap.containsKey( key ))
                {
                    String division = divisionMap.get( key );
                    divisions.add( division );
                    query = query.replace( term, "" );
                    match = true;
                }
            }
        }

        // Remove spaces
        query = query.trim().replaceAll( "\\s+", " " );
        List<QueryTokenizer.Token> tokens = tokenizer.tokenize( query );

        String campus1 = "-1";
        String campus2 = "-1";
        String campus3 = "-1";
        if( courseSearchForm.getCampusSeattle() )
        {
            campus1 = "0";
        }
        if( courseSearchForm.getCampusTacoma() )
        {
            campus2 = "1";
        }
        if( courseSearchForm.getCampusBothell() )
        {
            campus3 = "2";
        }

        for( String division : divisions )
        {
            boolean needDivisionQuery = true;

            for( String code : codes )
            {
                needDivisionQuery = false;
                SearchRequest request = new SearchRequest( "myplan.lu.search.divisionAndCode" );
                request.addParam( "division", division );
                request.addParam( "code", code );
                request.addParam( "campus1", campus1 );
                request.addParam( "campus2", campus2 );
                request.addParam( "campus3", campus3 );
                requests.add( request );
            }

            for( String level : levels )
            {
                needDivisionQuery = false;

                level = level.substring( 0, 1 ) + "00";

                SearchRequest request = new SearchRequest( "myplan.lu.search.divisionAndLevel" );
                request.addParam("division", division);
                request.addParam("level", level);
                request.addParam("campus1", campus1);
                request.addParam("campus2", campus2);
                request.addParam("campus3", campus3);
                requests.add( request );
            }

            if( needDivisionQuery )
            {
                SearchRequest request = new SearchRequest( "myplan.lu.search.division" );
                request.addParam("division", division);
                request.addParam("campus1", campus1);
                request.addParam("campus2", campus2);
                request.addParam("campus3", campus3);
                requests.add( request );
            }
        }
        for( QueryTokenizer.Token token : tokens )
        {
            String queryText = null;
            switch( token.rule )
            {
                case WORD:
                    queryText = token.value;
                    break;
                case QUOTED:
                    queryText = token.value;
                    queryText = queryText.substring( 1, queryText.length() - 1 );
                    break;
                default:
                    break;
            }
            SearchRequest request = new SearchRequest( "myplan.lu.search.fulltext" );
            request.addParam("queryText", queryText);
            request.addParam("campus1", campus1);
            request.addParam("campus2", campus2);
            request.addParam("campus3", campus3);
            requests.add( request );
        }

        return requests;
    }
}
