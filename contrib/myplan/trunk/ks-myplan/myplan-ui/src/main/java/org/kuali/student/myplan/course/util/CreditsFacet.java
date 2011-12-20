package org.kuali.student.myplan.course.util;


import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.*;

/**
*  Logic for building list of FacetItems and coding CourseSearchItems.
*/
public class CreditsFacet extends AbstractFacet {

    private HashSet<Integer> creditFacetSet = new HashSet<Integer>();

    public CreditsFacet() {
        super();
    }

    /**
     * The credits facet list needs to be in numeric order rather than string order, so
     *
     * @return A list of FacetItems.
     */
    @Override
    public List<FacetItem> getFacetItems() {
        Integer[] list = creditFacetSet.toArray( new Integer[0] );
        Arrays.sort( list );

        for( Integer credit : list ) {
            FacetItem item = new FacetItem();
            String display = credit.toString();
            String key = FACET_KEY_DELIMITER + display + FACET_KEY_DELIMITER;
            item.setKey( key );
            item.setDisplayName(display);
            facetItems.add( item );
        }

        return facetItems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course) {
        int min = (int) course.getCreditMin();
        int max = (int) course.getCreditMax();

        ArrayList<Integer> list = new ArrayList<Integer>();
        switch( course.getCreditType() ) {
            case range:
                for( int x = min; x <= max; x++ ) {
                    list.add( x );
                }
                break;
            case fixed:
                list.add( min );
                break;
            case multiple:
                list.add( min );
                list.add( max );
                break;
            case unknown:
            default:
                list.add( min );
                break;

        }

        creditFacetSet.addAll( list );

        StringBuilder facet = new StringBuilder();
        for( Integer credit : list ) {
            facet.append(FACET_KEY_DELIMITER).append(credit.toString()).append(FACET_KEY_DELIMITER);
        }

        course.setCreditsFacetKey(facet.toString());
    }

}
