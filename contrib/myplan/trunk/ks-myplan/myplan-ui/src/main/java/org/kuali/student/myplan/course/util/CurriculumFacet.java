package org.kuali.student.myplan.course.util;

import javax.xml.namespace.QName;

import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.student.core.organization.service.OrganizationService;
import org.kuali.student.myplan.course.dataobject.CourseSearchItem;
import org.kuali.student.myplan.course.dataobject.FacetItem;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *  Logic for building list of FacetItems and coding CourseSearchItems.
 */
public class CurriculumFacet extends AbstractFacet {

    public CurriculumFacet() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void process(CourseSearchItem course)
    {
        String key = course.getSubject();

        String displayName = unknownFacetDisplayName;

        if (key == null || key.equals("")) {
            key = unknownFacetKey;
        } else {
            // TODO: Use the Org Service to lookup curriculum name based on key.
            // For now just use the serviceArea code as the displayName.
            displayName = (key);
        }

        key = FACET_KEY_DELIMITER + key + FACET_KEY_DELIMITER;

        //  If it's a new facet key then create a new FacetItem and add it to the list.
        if (isNewFacetKey( key )) {
            facetItems.add(new FacetItem(key, displayName));
        }

        //  Code the item with the facet key.
        Set<String> keys = new HashSet<String>();
        keys.add(key);
        course.setCurriculumFacetKeys(keys);
    }
}
