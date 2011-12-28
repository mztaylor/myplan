package org.kuali.student.myplan.course.controller;

import org.junit.Test;
import org.kuali.student.common.search.dto.SearchParam;
import org.kuali.student.common.search.dto.SearchRequest;
import org.kuali.student.myplan.course.form.CourseSearchForm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class CourseSearchStrategyTest {
    @Test
    public void testFetchCourseDivisions() throws Exception {

    }

    @Test
    public void testAddCampusParams() throws Exception {
        CourseSearchForm form = new CourseSearchForm();
        form.setCampusSeattle( true );
        form.setCampusTacoma( true );
        form.setCampusBothell( true );

        ArrayList<SearchRequest> requests = new ArrayList<SearchRequest>();
        requests.add( new SearchRequest( "test" ));

        CourseSearchStrategy strategy = new CourseSearchStrategy();
        strategy.addCampusParams(requests, form );

        SearchRequest request = requests.get( 0 );
        List<SearchParam> params = request.getParams();
        assertEquals( 3, params.size() );

        SearchParam param = null;
        param = params.get( 0 );
        assertEquals( CourseSearchStrategy.SEATTLE_CAMPUS, param.getValue() );
        param = params.get( 1 );
        assertEquals( CourseSearchStrategy.TACOMA_CAMPUS, param.getValue() );
        param = params.get( 2 );
        assertEquals( CourseSearchStrategy.BOTHELL_CAMPUS, param.getValue() );
    }

    @Test
    public void testAddCampusParams2() throws Exception {
        CourseSearchForm form = new CourseSearchForm();
        form.setCampusSeattle( false );
        form.setCampusTacoma( false );
        form.setCampusBothell( false );

        ArrayList<SearchRequest> requests = new ArrayList<SearchRequest>();
        requests.add( new SearchRequest( "test" ));

        CourseSearchStrategy strategy = new CourseSearchStrategy();
        strategy.addCampusParams(requests, form );

        SearchRequest request = requests.get( 0 );
        List<SearchParam> params = request.getParams();
        assertEquals( 3, params.size() );

        SearchParam param = null;
        param = params.get( 0 );
        assertEquals( CourseSearchStrategy.NO_CAMPUS, param.getValue() );
        param = params.get( 1 );
        assertEquals( CourseSearchStrategy.NO_CAMPUS, param.getValue() );
        param = params.get( 2 );
        assertEquals( CourseSearchStrategy.NO_CAMPUS, param.getValue() );
    }

    @Test
    public void testAddDivisionSearchesNothing()
    {
        CourseSearchStrategy strategy = new CourseSearchStrategy();
        ArrayList<String> divisions = new ArrayList<String>();
        ArrayList<String> levels = new ArrayList<String>();
        ArrayList<String> codes = new ArrayList<String>();
        ArrayList<SearchRequest> requests = new ArrayList<SearchRequest>();
        strategy.addDivisionSearches( divisions, levels, codes, requests );
        assertEquals( 0, requests.size() );
    }

    @Test
    public void testAddDivisionSearchesJustDivision()
    {
        CourseSearchStrategy strategy = new CourseSearchStrategy();
        ArrayList<String> divisions = new ArrayList<String>();
        divisions.add( "DIVISION" );
        ArrayList<String> codes = new ArrayList<String>();
        ArrayList<String> levels = new ArrayList<String>();
        ArrayList<SearchRequest> requests = new ArrayList<SearchRequest>();
        strategy.addDivisionSearches( divisions, codes, levels, requests );
        assertEquals( 1, requests.size() );
        SearchRequest request = requests.get(0);
        assertEquals("myplan.lu.search.division", request.getSearchKey());
        assertEquals("DIVISION", request.getParams().get(0).getValue());
    }

    @Test
    public void testAddDivisionSearchesDivisionAndCode()
    {
        CourseSearchStrategy strategy = new CourseSearchStrategy();
        ArrayList<String> divisions = new ArrayList<String>();
        divisions.add( "DIVISION" );
        ArrayList<String> codes = new ArrayList<String>();
        codes.add( "CODE" );
        ArrayList<String> levels = new ArrayList<String>();
        ArrayList<SearchRequest> requests = new ArrayList<SearchRequest>();
        strategy.addDivisionSearches( divisions, codes, levels, requests );
        assertEquals( 1, requests.size() );
        SearchRequest request = requests.get(0);
        assertEquals("myplan.lu.search.divisionAndCode", request.getSearchKey());
        assertEquals("DIVISION", request.getParams().get(0).getValue());
        assertEquals( "CODE", request.getParams().get(1).getValue() );
    }

    @Test
    public void testAddDivisionSearchesDivisionAndLevel()
    {
        CourseSearchStrategy strategy = new CourseSearchStrategy();
        ArrayList<String> divisions = new ArrayList<String>();
        divisions.add( "DIVISION" );
        ArrayList<String> codes = new ArrayList<String>();
        ArrayList<String> levels = new ArrayList<String>();
        levels.add( "100" );
        ArrayList<SearchRequest> requests = new ArrayList<SearchRequest>();
        strategy.addDivisionSearches( divisions, codes, levels, requests );
        assertEquals( 1, requests.size() );
        SearchRequest request = requests.get(0);
        assertEquals("myplan.lu.search.divisionAndLevel", request.getSearchKey());
        assertEquals( "DIVISION", request.getParams().get(0).getValue() );
        assertEquals("100", request.getParams().get(1).getValue());
    }

    @Test
    public void testAddFullTextSearches()
    {
        CourseSearchStrategy strategy = new CourseSearchStrategy();
        String query = "text \"text\"";
        ArrayList<SearchRequest> requests = new ArrayList<SearchRequest>();
        strategy.addFullTextSearches( query, requests);
        assertEquals( 2, requests.size() );
        assertEquals("myplan.lu.search.fulltext", requests.get(0).getSearchKey());
        assertEquals( "text", requests.get(0).getParams().get(0).getValue() );
        assertEquals( "text", requests.get(1).getParams().get(0).getValue() );
    }

    @Test
    public void testExtractDivisions() throws Exception {
        HashMap<String,String> map = new HashMap<String,String>();
        map.put( "A",   "A   " );
        map.put( "AB", "A B " );
        map.put( "B",   "B   " );
        map.put( "C",   "C   " );
        CourseSearchStrategy strategy = new CourseSearchStrategy();
        ArrayList<String> divisions = new ArrayList<String>();
        String query = "A B C";
        query = strategy.extractDivisions( map, query, divisions );
        assertEquals( "", query );
        assertEquals( 2, divisions.size() );
        assertEquals( "A B ", divisions.get( 0 ) );
        assertEquals( "C   ", divisions.get( 1 ) );
    }

    @Test
    public void testQueryToRequests() throws Exception {

    }
}
