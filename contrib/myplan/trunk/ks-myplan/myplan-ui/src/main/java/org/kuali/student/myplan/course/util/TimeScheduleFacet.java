package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.*;

/**
*  Logic for building list of FacetItems and coding CourseSearchItems.
*/
public class TimeScheduleFacet extends AbstractFacet {

    public TimeScheduleFacet() {
        super();
    }

    HashSet<CourseSearchItem.TermOffered> termOfferedSet = new HashSet<CourseSearchItem.TermOffered>();

    @Override
    public List<FacetItem> getFacetItems() {
        ArrayList<CourseSearchItem.TermOffered> list = new ArrayList<CourseSearchItem.TermOffered>();
        list.addAll(termOfferedSet);
        Collections.sort(list);

        for( CourseSearchItem.TermOffered term : list ) {
            FacetItem item = new FacetItem();
            String display = term.facet;
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
        List<CourseSearchItem.TermOffered> list = course.getTermOfferedList();
        termOfferedSet.addAll( list );

        StringBuilder facet = new StringBuilder();
        for( CourseSearchItem.TermOffered term : list )
        {
            String key = FACET_KEY_DELIMITER + term.facet + FACET_KEY_DELIMITER;
            facet.append( key );
        }

        /*
         * Time schedule contains multiple items so use a delimiter to separate keys.
         */
        String times = course.getScheduledTime();

        if (times == null || times.equals(CourseSearchItem.EMPTY_RESULT_VALUE_KEY) || times.equals("")) {
            times = UNKNOWN_FACET_DISPLAY_NAME;
        }
 /*
        //  Parse the times string and make a list of keys.
        String[] names = times.replace(" ", "").split(",");

        StringBuilder facet = new StringBuilder();
        for (String name : names) {
            String key = null;
            if( UNKNOWN_FACET_DISPLAY_NAME.equals( name )) {
                key = UNKNOWN_FACET_DISPLAY_NAME;
            } else {
                key = name;
            }

            key = FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER;

            if (isNewFacetKey(key)) {
                FacetItem item = new FacetItem();
                item.setKey(key);
                item.setDisplayName(name);
                facetItems.add(item);
            }
            facet.append(key);
        }
 */
        //  Convert the list back into a string which can be matched against on the client.
        course.setTimeScheduleFacetKey(facet.toString());
    }
}
