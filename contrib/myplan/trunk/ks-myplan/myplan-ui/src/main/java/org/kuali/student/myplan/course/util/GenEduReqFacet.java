package org.kuali.student.myplan.course.util;

import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.*;

/**
*  Logic for building list of FacetItems and coding CourseSearchItems.
*/
public class GenEduReqFacet extends AbstractFacet {

    public GenEduReqFacet() {
        super();
        unknownFacetKey =  "None";
        unknownFacetDisplayName = "None";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem item) {

        String genEdString = item.getGenEduReq();

        //  Set of keys which pertain to this course.
        Set<String> facetKeys = new HashSet<String>();

        //  If no gen edu req info was set then setup for an "Unknown" facet.
        if (genEdString == null || genEdString.equals(CourseSearchItem.EMPTY_RESULT_VALUE_KEY) || genEdString.equals("")) {
            facetKeys.add(FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER);
            setShowUnknownKey(true);
        } else {
            //  TODO: UW SPECIFIC
            //  Remove white space before tokenizing.
            genEdString = genEdString.replaceAll("\\s+", "");
            String k[] = genEdString.split(",");
            List<String> keys = new ArrayList<String>(Arrays.asList(k));
            for (String key : keys)
            {
                if (isNewFacetKey( FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER ))
                {
                    FacetItem fItem = new FacetItem();
                    String displayName = key;
                    fItem.setKey(FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER);
                    fItem.setDisplayName(displayName);
                    facetItems.add(fItem);
                }
                facetKeys.add(FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER);
            }
        }

        item.setGenEduReqFacetKeys(facetKeys);
    }
}
