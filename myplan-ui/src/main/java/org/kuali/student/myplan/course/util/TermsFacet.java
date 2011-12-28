package org.kuali.student.myplan.course.util;

import org.kuali.student.core.atp.dto.AtpTypeInfo;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.*;

/**
 * Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class TermsFacet extends AbstractFacet {

    public TermsFacet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course) {

        //  The Set of facet keys which pertain to this course.
        Set<String> facetKeys = new HashSet<String>();

        if (null == course.getTermInfoList() || 0 == course.getTermInfoList().size()) {
            String key = FACET_KEY_DELIMITER + getUnknownFacetKey() + FACET_KEY_DELIMITER;
            facetKeys.add(key);
            setShowUnknownKey(true);
        } else {
            for (AtpTypeInfo term : course.getTermInfoList()) {
                //  Title-case the term name.
                String termName =   term.getName().substring(0, 1).toUpperCase() + term.getName().substring(1);
                String key = FACET_KEY_DELIMITER + termName  + FACET_KEY_DELIMITER;

                //  If an FacetItem doesn't exist for this key then create one and add it to the Facet.
                if (isNewFacetKey( key )) {
                    FacetItem fItem = new FacetItem();
                    fItem.setKey(key);
                    fItem.setDisplayName(termName);
                    facetItems.add(fItem);
                }

                facetKeys.add(key);
            }
        }

        //  Add the set of keys to the courseSearchItem.
        course.setTermsFacetKeys(facetKeys);
    }
}
