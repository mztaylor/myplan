package org.kuali.student.myplan.course.controller;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.common.search.dto.*;
import org.kuali.student.lum.lu.service.LuService;
import org.kuali.student.lum.lu.service.LuServiceConstants;
import org.kuali.student.myplan.course.form.CourseSearchForm;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
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
            List<SearchParam> params = new ArrayList<SearchParam>();
            SearchRequest request = new SearchRequest();
            request.setSearchKey( "myplan.distinct.clu.divisions" );
            request.setParams( params );

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

        List<QueryTokenizer.Token> tokens = tokenizer.tokenize( query );
//        TokenPairs pairs = ;
        List<String> terms = new TokenPairs( tokens ).sortedLongestFirst();
        for( String term :  terms )
        {
            if( divisionMap.containsKey( term ))
            {
                String division = divisionMap.get( term );
                divisions.add( division );
                query = query.replace( term, "" );
            }
        }
        for( String term : terms )
        {
            if( divisionMap.containsKey( term ))
            {
                String division = divisionMap.get( term );
                divisions.add( division );
                query = query.replace( term, "" );
            }
        }

        // Remove spaces, make upper case
        query = query.trim().replaceAll( "\\s+", " " );
        tokens = tokenizer.tokenize( query );

        /*
        String campus1 = "XX";
        String campus2 = "XX";
        String campus3 = "XX";
        if( courseSearchForm.getCampusSeattle() )
        {
            campus1 = "NO";
        }
        if( courseSearchForm.getCampusTacoma() )
        {
            campus2 = "SO";
        }
        if( courseSearchForm.getCampusBothell() )
        {
            campus3 = "AL";
        }
 */
        boolean allcampus =
            courseSearchForm.getCampusSeattle() &&
            courseSearchForm.getCampusTacoma() &&
            courseSearchForm.getCampusBothell();

//            for( String term : terms )
            {
                /*
                SearchRequest searchRequest = new SearchRequest();
                if( allcampus )
                {
                    searchRequest.setSearchKey("myplan.lu.search.current.allcampus");
                }
                else
                {
                    searchRequest.setSearchKey("myplan.lu.search.current");
                }

                List<SearchParam> params = new ArrayList<SearchParam>();
                params.add( new SearchParam( "queryText", term ));
                params.add( new SearchParam( "queryLevel", level ));
                if( !allcampus )
                {
                params.add( new SearchParam( "campus1", campus1 ));
                params.add( new SearchParam( "campus2", campus2 ));
                params.add( new SearchParam( "campus3", campus3 ));
                }
                searchRequest.setParams(params);
                */
                for( String division : divisions )
                {
                    boolean needDivisionQuery = true;

                    for( String code : codes )
                    {
                        needDivisionQuery = false;
                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.setSearchKey( "myplan.lu.search.divisionAndCode" );
                        List<SearchParam> params = new ArrayList<SearchParam>();
                        params.add( new SearchParam( "division", division ));
                        params.add( new SearchParam( "code", code ));
                        searchRequest.setParams(params);
                        requests.add( searchRequest );

                    }

                    for( String level : levels )
                    {
                        needDivisionQuery = false;

                        level = level.substring( 0, 1 ) + "00";

                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.setSearchKey( "myplan.lu.search.divisionAndLevel" );
                        List<SearchParam> params = new ArrayList<SearchParam>();
                        params.add( new SearchParam( "division", division ));
                        params.add( new SearchParam( "level", level ));
                        searchRequest.setParams(params);

                        requests.add( searchRequest );
                    }

                    if( needDivisionQuery )
                    {
                        SearchRequest searchRequest = new SearchRequest();
                        searchRequest.setSearchKey( "myplan.lu.search.division" );
                        List<SearchParam> params = new ArrayList<SearchParam>();
                        params.add( new SearchParam( "division", division ));
                        searchRequest.setParams(params);

                        requests.add( searchRequest );
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
                            break;
                        default:
                            break;
                    }
                    SearchRequest searchRequest = new SearchRequest();
                    searchRequest.setSearchKey( "myplan.lu.search.fulltext" );
                    List<SearchParam> params = new ArrayList<SearchParam>();
                    params.add( new SearchParam( "queryText", queryText ));
                    searchRequest.setParams(params);

                    requests.add( searchRequest );
                }
            }

        return requests;
    }
}
